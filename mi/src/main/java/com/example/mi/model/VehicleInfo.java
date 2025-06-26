package com.example.mi.model;

import lombok.Data;

@Data
public class VehicleInfo {
    private String vid;
    private Integer carId;
    private String batteryType;
    private Integer totalMileage;
    private Integer batteryHealthStatus;
}
