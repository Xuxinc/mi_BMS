package com.example.mi.controller;

import com.example.mi.model.Result;
import com.example.mi.model.SignalRequest;
import com.example.mi.model.SignalResponse;
import com.example.mi.service.SignalReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SignalReportController {
    @Autowired
    private SignalReportService signalReportService;

    // 提交预警报告
    @PostMapping("/warn")
    public Result reportSignal(@RequestBody SignalRequest[] signalRequests) {
        try {
            List<SignalResponse> signalResponses =  signalReportService.reportSignal(signalRequests);
            return Result.success(signalResponses);
        } catch (Exception e) {
            return Result.error("Internal Server Error: " + e.getMessage());
        }
    }

    // 根据id获取电池信号状态
    @GetMapping("/batterySignalStatus/{carId}")
    public Result getBatterySignalStatus(@PathVariable Integer carId) {
        try {
            String signalStatus = signalReportService.getBatterySignalStatus(carId);
            return Result.success(signalStatus);
        } catch (Exception e) {
            return Result.error("Internal Server Error: " + e.getMessage());
        }
    }

    // 根据id获取预警报告
    @GetMapping("/signalReport/{carId}")
    public Result getSignalReport(@PathVariable Integer carId) {
        try {
            List<SignalResponse> signalResponses = signalReportService.getSignalReport(carId);
            return Result.success(signalResponses);
        } catch (Exception e) {
            return Result.error("Internal Server Error: " + e.getMessage());
        }
    }

    // 修改预警报告
    @PutMapping("/signalReport/")
    public Result updateSignalReport(@RequestBody SignalRequest signalRequest) {
        try {
            signalReportService.updateSignalReport(signalRequest);
            return Result.success("Signal report updated successfully");
        } catch (Exception e) {
            return Result.error("Failed to update signal report: " + e.getMessage());
        }
    }

    // 根据id删除预警报告
    @DeleteMapping("/signalReport/{id}")
    public Result deleteSignalReport(@PathVariable Integer id) {
        try {
            signalReportService.deleteSignalReport(id);
            return Result.success("Signal report deleted successfully");
        } catch (Exception e) {
            return Result.error("Failed to delete signal report: " + e.getMessage());
        }
    }

    // 获取所有预警报告
    @GetMapping("/signalReports")
    public Result getAllSignalReports() {
        try {
            List<SignalResponse> signalResponses = signalReportService.getAllSignalReports();
            return Result.success(signalResponses);
        } catch (Exception e) {
            return Result.error("Failed to fetch signal reports: " + e.getMessage());
        }
    }
}