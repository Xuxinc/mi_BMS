package com.example.mi.mapper;

import com.example.mi.model.Rule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RuleMapper {
    @Select("SELECT * FROM Rule WHERE ruleNo = #{ruleNo} AND batteryType = #{batteryType}")
    List<Rule> findRulesByNoAndType(@Param("ruleNo") Integer ruleNo, @Param("batteryType") String batteryType);

    @Select("SELECT * FROM Rule WHERE batteryType = #{batteryType}")
    List<Rule> findAllRulesByBatteryType(String batteryType);
}
