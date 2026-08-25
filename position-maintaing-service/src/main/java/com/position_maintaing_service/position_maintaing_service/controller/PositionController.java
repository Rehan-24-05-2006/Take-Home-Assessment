package com.position_maintaing_service.position_maintaing_service.controller;

import com.position_maintaing_service.position_maintaing_service.mode.OrderEvent;
import com.position_maintaing_service.position_maintaing_service.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping("/events")
    public ResponseEntity<Void> receiveEvent(
            @RequestBody OrderEvent event) {

        positionService.processEvent(event);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/position")
    public ResponseEntity<Map<String, Integer>> getPositions() {

        return ResponseEntity.ok(
                positionService.getPositions()
        );
    }
}
