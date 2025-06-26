package com.example.mi.model;

import lombok.Data;

@Data
public class SignalReport  {

    private Integer reportId;
    private Integer carId;
    private Integer warnId;
    private String signalString;
    private String batteryType;
    private String warnName;
    private Integer warnLevel;
}
