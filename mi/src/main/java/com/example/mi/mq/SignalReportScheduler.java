package com.example.mi.mq;

import com.example.mi.mapper.SignalReportMapper;
import com.example.mi.model.SignalReport;
import com.example.mi.model.SignalResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SignalReportScheduler {

    @Autowired
    private SignalReportMapper signalReportMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void scanAndSendSignalReports() {
        List<SignalReport> signalReports = signalReportMapper.getAllSignalReports();

        for (SignalReport signalReport : signalReports) {
            if (signalReport.getWarnLevel() != null && signalReport.getWarnLevel() != -1) {
                SignalResponse signalResponse = new SignalResponse();
                signalResponse.setCarId(signalReport.getCarId());
                signalResponse.setBatteryType(signalReport.getBatteryType());
                signalResponse.setWarnLevel(signalReport.getWarnLevel());
                signalResponse.setWarnName(signalReport.getWarnName());
                rocketMQTemplate.convertAndSend("signal-warning-topic", signalResponse);
                log.info("Sent message to topic 'signal-warning-topic': {}", signalResponse);
            }
        }

    }
}