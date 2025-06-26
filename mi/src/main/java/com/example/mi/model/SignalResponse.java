package com.example.mi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignalResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("车架编号")
    private Integer carId;

    @JsonProperty("电池类型")
    private String batteryType;

    @JsonProperty("warnName")
    private String warnName;

    @JsonProperty("warnLevel")
    private Integer warnLevel;

}