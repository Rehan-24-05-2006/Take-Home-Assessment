package com.position_maintaing_service.position_maintaing_service;

import com.position_maintaing_service.position_maintaing_service.controller.PositionController;
import com.position_maintaing_service.position_maintaing_service.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PositionController.class)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PositionService positionService;

    @Test
    void shouldReturnCurrentPositions() throws Exception {

        when(positionService.getPositions())
                .thenReturn(
                        Map.of(
                                "RELIANCE", 90,
                                "TCS", -75
                        )
                );

        mockMvc.perform(
                        get("/position")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RELIANCE").value(90))
                .andExpect(jsonPath("$.TCS").value(-75));
    }
}