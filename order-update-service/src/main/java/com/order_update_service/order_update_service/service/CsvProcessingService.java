package com.order_update_service.order_update_service.service;

import com.order_update_service.order_update_service.client.PositionServiceClient;
import com.order_update_service.order_update_service.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
public class CsvProcessingService {

    private static final Logger log =
            LoggerFactory.getLogger(CsvProcessingService.class);

    private final OrderValidationService validationService;
    private final PositionServiceClient positionServiceClient;
    private final String csvPath;
    private final int maxEventsPerSecond;

    private final Set<String> seenEventIds = new HashSet<>();

    public CsvProcessingService(
            OrderValidationService validationService,
            PositionServiceClient positionServiceClient,
            @Value("${order.csv.path}") String csvPath,
            @Value("${max.events.per.second:50}") int maxEventsPerSecond) {

        this.validationService = validationService;
        this.positionServiceClient = positionServiceClient;
        this.csvPath = csvPath;
        this.maxEventsPerSecond = maxEventsPerSecond;
    }
    public void processCsv() {
        log.info("Starting CSV processing. File: {}", csvPath);
        Path path = Path.of(csvPath);

        if (!Files.exists(path)) {
            log.error("CSV file not found: {}", csvPath);
            return;
        }

        long delayMillis = 1000L / maxEventsPerSecond;

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {

                // Skip CSV header
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                if (line.isBlank()) {
                    log.warn("Skipping blank CSV row");
                    continue;
                }

                try {

                    OrderEvent event = parseRow(line);

                    // Validate event
                    String validationError = validationService.validate(event);

                    if (validationError != null) {
                        log.warn(
                                "Rejected event {}: {}",
                                event.getEventId(),
                                validationError
                        );
                        continue;
                    }

                    // Duplicate event_id check
                    if (seenEventIds.contains(event.getEventId())) {
                        log.warn(
                                "Duplicate event_id {} ignored",
                                event.getEventId()
                        );
                        continue;
                    }

                    // First valid event wins
                    seenEventIds.add(event.getEventId());

                    log.info(
                            "Accepted event: {}",
                            event.getEventId()
                    );

                    // Send event to Position Service
                    positionServiceClient.sendEvent(event);

                    log.info(
                            "Successfully sent event: {}",
                            event.getEventId()
                    );

                    // Maximum 50 events/sec
                    Thread.sleep(delayMillis);

                } catch (IllegalArgumentException e) {

                    log.warn(
                            "Rejected malformed CSV row: {}. Reason: {}",
                            line,
                            e.getMessage()
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    log.error(
                            "CSV processing interrupted"
                    );

                    return;
                } catch (Exception e) {

                    log.error(
                            "Error processing CSV row: {}. Reason: {}",
                            line,
                            e.getMessage()
                    );
                }
            }

            log.info("Input processing completed.");

        } catch (IOException e) {

            log.error(
                    "Failed to read CSV file: {}",
                    csvPath,
                    e
            );
        }
    }

    private OrderEvent parseRow(String line) {

        String[] values = line.split(",", -1);

        if (values.length != 4) {
            throw new IllegalArgumentException(
                    "Expected 4 columns but found " + values.length
            );
        }

        String eventId = values[0].trim();
        String symbol = values[1].trim();
        String transactionType = values[2].trim();
        String quantityValue = values[3].trim();

        Integer quantity;

        try {
            quantity = Integer.valueOf(quantityValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "quantity must be an integer"
            );
        }

        return new OrderEvent(
                eventId,
                symbol,
                transactionType,
                quantity
        );
    }
}
