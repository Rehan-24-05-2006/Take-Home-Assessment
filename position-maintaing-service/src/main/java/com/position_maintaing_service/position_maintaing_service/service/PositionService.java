package com.position_maintaing_service.position_maintaing_service.service;

import com.position_maintaing_service.position_maintaing_service.mode.OrderEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class PositionService {

    private final Map<String, Integer> positions = new HashMap<>();

    private final Set<String> processedEventIds = new HashSet<>();

    public synchronized void processEvent(OrderEvent event) {

        // Ignore duplicate events
        if (processedEventIds.contains(event.getEventId())) {
            return;
        }

        processedEventIds.add(event.getEventId());

        int currentPosition =
                positions.getOrDefault(event.getSymbol(), 0);

        if ("BUY".equals(event.getTransactionType())) {
            currentPosition += event.getQuantity();
        } else if ("SELL".equals(event.getTransactionType())) {
            currentPosition -= event.getQuantity();
        }

        // Store symbol even if position becomes zero
        positions.put(event.getSymbol(), currentPosition);
    }

    public synchronized Map<String, Integer> getPositions() {
        return new HashMap<>(positions);
    }

}
