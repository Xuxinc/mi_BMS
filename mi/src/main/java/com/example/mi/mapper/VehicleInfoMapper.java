package com.example.mi.mapper;

import com.example.mi.model.VehicleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleInfoMapper {
    @Select("SELECT * FROM VehicleInfo WHERE carId = #{carId}")
    VehicleInfo selectVehicleInfoByCarId(int carId);
}