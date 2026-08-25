package com.order_update_service.order_update_service.service;

import com.order_update_service.order_update_service.model.OrderEvent;
import org.springframework.stereotype.Service;

@Service
public class OrderValidationService {

    public String validate(OrderEvent event) {

        if (event == null) {
            return "event cannot be null";
        }

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            return "event_id must not be blank";
        }

        if (event.getSymbol() == null || event.getSymbol().isBlank()) {
            return "symbol must not be blank";
        }

        if (event.getTransactionType() == null || event.getTransactionType().isBlank()) {
            return "transaction_type must not be blank";
        }

        if (!event.getTransactionType().equals("BUY") && !event.getTransactionType().equals("SELL")) {
            return "transaction_type must be BUY or SELL";
        }

        if (event.getQuantity() == null) {
            return "quantity must not be null";
        }

        if (event.getQuantity() <= 0) {
            return "quantity must be a positive integer";
        }

        return null;
    }
}
