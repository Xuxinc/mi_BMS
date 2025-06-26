package com.example.mi;

import com.example.mi.controller.SignalReportController;
import com.example.mi.model.SignalRequest;
import com.example.mi.model.SignalResponse;
import com.example.mi.model.VehicleInfo;
import com.example.mi.service.SignalReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignalReportController.class)
public class SignalReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignalReportService signalReportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testReportSignal() throws Exception {
        SignalRequest req = new SignalRequest();
        req.setCarId(1);
        req.setSignal("{\"Mx\":12.0,\"Mi\":0.6}");
        
        SignalResponse resp = new SignalResponse(1, "三元电池", "电压差报警", 0);
        Mockito.when(signalReportService.reportSignal(any())).thenReturn(Collections.singletonList(resp));

        mockMvc.perform(post("/api/warn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignalRequest[]{req})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].电池类型").value("三元电池"));
    }

    @Test
    public void testGetBatterySignalStatus() throws Exception {
        Mockito.when(signalReportService.getBatterySignalStatus(1)).thenReturn("{\"Mx\":12.0,\"Mi\":0.6}");

        mockMvc.perform(get("/api/batterySignalStatus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("{\"Mx\":12.0,\"Mi\":0.6}"));
    }

    @Test
    public void testGetSignalReport() throws Exception {
        SignalResponse resp = new SignalResponse(1, "三元电池", "电压差报警", 0);
        Mockito.when(signalReportService.getSignalReport(1)).thenReturn(Collections.singletonList(resp));

        mockMvc.perform(get("/api/signalReport/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].warnName").value("电压差报警"));
    }

    @Test
    public void testUpdateSignalReport() throws Exception {
        SignalRequest req = new SignalRequest();
        req.setCarId(1);
        req.setSignal("{\"Mx\":11.0,\"Mi\":9.6}");
        doNothing().when(signalReportService).updateSignalReport(any());

        mockMvc.perform(put("/api/signalReport/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    public void testDeleteSignalReport() throws Exception {
        doNothing().when(signalReportService).deleteSignalReport(1);

        mockMvc.perform(delete("/api/signalReport/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    public void testGetAllSignalReports() throws Exception {
        SignalResponse resp1 = new SignalResponse(1, "三元电池", "电压差报警", 1);
        SignalResponse resp2 = new SignalResponse(2, "铁锂电池", "电流差报警", 2);

        Mockito.when(signalReportService.getAllSignalReports()).thenReturn(Arrays.asList(resp1, resp2));

        mockMvc.perform(get("/api/signalReports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
