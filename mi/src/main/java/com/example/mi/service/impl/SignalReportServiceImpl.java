package com.example.mi.service.impl;

import com.example.mi.mapper.RuleMapper;
import com.example.mi.mapper.SignalReportMapper;
import com.example.mi.mapper.VehicleInfoMapper;
import com.example.mi.model.*;
import com.example.mi.service.SignalReportService;
import com.example.mi.util.RedisCacheUtil;
import com.example.mi.util.WarningLevelUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class SignalReportServiceImpl implements SignalReportService {
    @Autowired
    private SignalReportMapper signalReportMapper;

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedissonClient redissonClient;

    // 创建一个固定大小的线程池，用于并发处理信号报告
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    /**
     * 上报信号数据，生成预警信息并返回。
     * 涉及数据库写操作，填加事务管理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SignalResponse> reportSignal(SignalRequest[] signalRequests) throws JsonProcessingException {
        // 存储所有请求的响应结果
        List<SignalResponse> signalResponses = new ArrayList<>();
        // 存储异步任务的结果
        List<Future<List<SignalResponse>>> futures = new ArrayList<>();

        // 提交每个信号请求的处理任务到线程池
        for (SignalRequest signalRequest : signalRequests) {
            Future<List<SignalResponse>> future = executorService.submit(() -> {
                List<SignalResponse> responses = processSingleRequest(signalRequest);
                return responses;
            });
            futures.add(future);
        }
        // 等待所有异步任务完成，并收集结果
        for (Future<List<SignalResponse>> future : futures) {
            try {
                signalResponses.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return signalResponses;
    }

    /**
     * 处理单个信号请求。
     */
    private List<SignalResponse> processSingleRequest(SignalRequest signalRequest) throws JsonProcessingException {
        List<SignalResponse> responses = new ArrayList<>();

        // 1. 解析信号数据
        String signal = signalRequest.getSignal();

        // 2. 根据车架编号查询车辆信息获取电池类型
        VehicleInfo vehicleInfo = vehicleInfoMapper.selectVehicleInfoByCarId(signalRequest.getCarId());
        String batteryType = vehicleInfo.getBatteryType();

        // 3. 根据warnId和电池类型查询预警规则，如果没有warnId，则查询所有规则
        List<Rule> rules = signalRequest.getWarnId() != null ?
                ruleMapper.findRulesByNoAndType(signalRequest.getWarnId(), batteryType) :
                ruleMapper.findAllRulesByBatteryType(batteryType);

        // 4. 根据查询到的规则计算预警等级
        for (Rule rule : rules) {
            Integer warnLevel = WarningLevelUtil.calculateWarningLevel(signal, rule.getWarningRule());

            // 5. 创建SignalReport对象并设置属性
            String warnName = rule.getName();
            SignalReport signalReport = new SignalReport();
            signalReport.setCarId(signalRequest.getCarId());
            signalReport.setWarnId(signalRequest.getWarnId());
            signalReport.setSignalString(signalRequest.getSignal());
            signalReport.setBatteryType(batteryType);
            signalReport.setWarnName(warnName);
            signalReport.setWarnLevel(warnLevel);

            // 6. 将SignalReport对象插入数据库
            signalReportMapper.insertSignalReport(signalReport);

            // 7.筛选出需要报警的信号,-1表示不报警
            if (warnLevel != -1) {
                SignalResponse signalResponse = new SignalResponse(
                        signalRequest.getCarId(),
                        batteryType,
                        warnName,
                        warnLevel);
                responses.add(signalResponse);
            }
        }
        return responses;
    }

    /**
     * 获取电池信号状态，先查本地缓存（Caffeine），再查Redis，最后查数据库。
     */
    @Override
    @Cacheable(value = "localBatteryStatus", key = "#carId") // 本地缓存
    public String getBatterySignalStatus(Integer carId) throws Exception {
        // Redis 缓存键
        String cacheKey = "batterySignalStatus:" + carId;

        // 1: 查询 Redis（分布式缓存）
        String signalStatus = (String) redisCacheUtil.getCache(cacheKey);
        if (signalStatus != null) {
            return signalStatus;
        }

        // 2: 查询数据库（缓存未命中）
        List<SignalReport> signalReports = signalReportMapper.findReportByCarId(carId);
        if (signalReports == null || signalReports.isEmpty()) {
            throw new Exception("未找到电池信号状态");
        }

        signalStatus = signalReports.get(0).getSignalString();

        // 3: 写入 Redis 缓存
        redisCacheUtil.setCache(cacheKey, signalStatus, 10, TimeUnit.SECONDS);

        // 4: 方法返回值自动写入 Caffeine 缓存（@Cacheable 自动处理）

        return signalStatus;
    }


    /**
     * 获取车辆的预警报告（含缓存逻辑）。
     */
    @Override
    public List<SignalResponse> getSignalReport(Integer carId) throws Exception {
        // 缓存键
        String cacheKey = "signalReport:" + carId;

        // 从 Redis 缓存中获取数据
        List<SignalResponse> cachedResponses = (List<SignalResponse>) redisCacheUtil.getCache(cacheKey);
        if (cachedResponses != null) {
            // 缓存命中，直接返回缓存数据
            return cachedResponses;
        }

        // 缓存未命中，从数据库中查询
        List<SignalReport> signalReports = signalReportMapper.findReportByCarId(carId);

        if (signalReports == null) {
            throw new Exception("未找到电池信号状态");
        }

        List<SignalResponse> signalResponses = new ArrayList<>();

        for (SignalReport signalReport : signalReports) {
            // 构造返回结果
            SignalResponse signalResponse = new SignalResponse();
            signalResponse.setCarId(carId);
            signalResponse.setBatteryType(signalReport.getBatteryType());
            signalResponse.setWarnName(signalReport.getWarnName());
            signalResponse.setWarnLevel(signalReport.getWarnLevel());
            signalResponses.add(signalResponse);
        }

        // 将结果存入 Redis 缓存，设置过期时间
        redisCacheUtil.setCache(cacheKey, signalResponses, 10, TimeUnit.SECONDS);

        return signalResponses;
    }

    /**
     * 更新指定车辆的预警报告，带有分布式锁控制并清理缓存。
     */
    @Override
    public void updateSignalReport(SignalRequest signalRequest) throws Exception {
        // 获取更新后的信号
        String signal = signalRequest.getSignal();

        // 分布式锁的键
        String lockKey = "signalReportLock:" + signalRequest.getCarId();
        // 获取分布式锁
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待 10 秒，锁的持有时间为 30 秒
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new Exception("Failed to acquire lock for carId: " + signalRequest.getCarId());
            }

            // 根据 carId 查询所有相关的 SignalReport
            List<SignalReport> signalReports = signalReportMapper.findReportByCarId(signalRequest.getCarId());
            for (SignalReport signalReport : signalReports) {
                Integer warnLevel = WarningLevelUtil.calculateWarningLevel(signal,signalReport.getSignalString());
                signalReport.setSignalString(signal);
                signalReport.setWarnLevel(warnLevel);
                signalReportMapper.updateSignalReport(signalReport);
            }

            // 第一次删除缓存
            String cacheKey = "signalReport:" + signalRequest.getCarId();
            redisCacheUtil.deleteCache(cacheKey);

            // 设置延迟删除
            long delayTime = 5; // 延迟时间，单位为秒
            redissonClient.getBucket(cacheKey).expire(delayTime, TimeUnit.SECONDS); // 设置过期时间
        } finally {
            // 释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 删除某条预警报告
     */
    @Override
    public void deleteSignalReport(Integer id){
        signalReportMapper.deleteSignalReport(id);
    }

    /**
     * 获取所有的预警报告
     */
    @Override
    public List<SignalResponse> getAllSignalReports(){
        List<SignalResponse> signalResponses = new ArrayList<>();
        List<SignalReport> signalReports =  signalReportMapper.getAllSignalReports();
        for (SignalReport signalReport : signalReports) {
            SignalResponse signalResponse = new SignalResponse();
            signalResponse.setCarId(signalReport.getCarId());
            signalResponse.setBatteryType(signalReport.getBatteryType());
            signalResponse.setWarnName(signalReport.getWarnName());
            signalResponse.setWarnLevel(signalReport.getWarnLevel());
            signalResponses.add(signalResponse);
        }
        return signalResponses;
    }

}