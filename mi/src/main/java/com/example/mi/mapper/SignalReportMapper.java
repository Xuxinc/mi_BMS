package com.example.mi.mapper;

import com.example.mi.model.SignalReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SignalReportMapper {

    @Insert("INSERT INTO SignalReport (carId, warnId, signalString, batteryType, warnName, warnLevel) VALUES " +
            "(#{carId}, #{warnId}, #{signalString}, #{batteryType}, #{warnName}, #{warnLevel})")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "reportId", before = false, resultType = int.class)
    int insertSignalReport(SignalReport signalReport);

    @Select("SELECT * FROM SignalReport WHERE carId = #{carId}")
    List<SignalReport> findReportByCarId(@Param("carId") Integer carId);

    @Update("UPDATE SignalReport SET signalString = #{signalString}, warnLevel = #{warnLevel} WHERE carId = #{carId}")
    void updateSignalReport(SignalReport signalReport);

    @Delete("DELETE FROM SignalReport WHERE carId = #{id}")
    void deleteSignalReport(@Param("id") Integer id);

    @Select("SELECT * FROM SignalReport")
    List<SignalReport> getAllSignalReports();

}
