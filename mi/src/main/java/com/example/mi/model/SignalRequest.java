package com.example.mi.model;

import lombok.Data;

@Data
public class SignalRequest{

    private Integer carId;
    private Integer warnId;
    private String signal;
}
