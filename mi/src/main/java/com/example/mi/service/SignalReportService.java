package com.example.mi.service;

import com.example.mi.model.SignalRequest;
import com.example.mi.model.SignalResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface SignalReportService {
    List<SignalResponse> reportSignal(SignalRequest[] signalRequests) throws JsonProcessingException;

    String getBatterySignalStatus(Integer carId) throws Exception;

    List<SignalResponse> getSignalReport(Integer carId) throws Exception;

    void updateSignalReport(SignalRequest signalRequest) throws Exception;

    void deleteSignalReport(Integer id);

    List<SignalResponse> getAllSignalReports();

}