package com.example.mi.model;

import lombok.Data;

@Data
public class Rule {
    private Integer ruleId;
    private Integer ruleNo;
    private String name;
    private String batteryType;
    private String warningRule;
}
