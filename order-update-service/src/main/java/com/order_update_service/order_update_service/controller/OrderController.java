package com.order_update_service.order_update_service.controller;

import com.order_update_service.order_update_service.service.CsvProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CsvProcessingService csvProcessingService;

    @PostMapping("/process")
    public ResponseEntity<String> processOrders() {
        csvProcessingService.processCsv();
        return ResponseEntity.ok("Order processing started");
    }

}
