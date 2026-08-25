package com.order_update_service.order_update_service;

import com.order_update_service.order_update_service.model.OrderEvent;
import com.order_update_service.order_update_service.service.OrderValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderUpdateServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    private OrderValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new OrderValidationService();
    }

    @Test
    void shouldAcceptValidBuyEvent() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                );

        String result = validationService.validate(event);

        assertNull(result);
    }

    @Test
    void shouldAcceptValidSellEvent() {
        OrderEvent event =
                new OrderEvent(
                        "evt-0002",
                        "TCS",
                        "SELL",
                        75
                );

        String result = validationService.validate(event);
        assertNull(result);
    }

    @Test
    void shouldRejectBlankEventId() {

        OrderEvent event =
                new OrderEvent(
                        "",
                        "RELIANCE",
                        "BUY",
                        90
                );

        String result = validationService.validate(event);

        assertEquals(
                "event_id must not be blank",
                result
        );
    }

    @Test
    void shouldRejectBlankSymbol() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "",
                        "BUY",
                        90
                );

        String result = validationService.validate(event);

        assertEquals(
                "symbol must not be blank",
                result
        );
    }

    @Test
    void shouldRejectInvalidTransactionType() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "HOLD",
                        90
                );

        String result = validationService.validate(event);

        assertEquals(
                "transaction_type must be BUY or SELL",
                result
        );
    }

    @Test
    void shouldRejectZeroQuantity() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        0
                );

        String result = validationService.validate(event);

        assertEquals(
                "quantity must be a positive integer",
                result
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        -10
                );

        String result = validationService.validate(event);

        assertEquals(
                "quantity must be a positive integer",
                result
        );
    }

    @Test
    void shouldRejectNullQuantity() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        null
                );

        String result = validationService.validate(event);

        assertEquals(
                "quantity must not be null",
                result
        );
    }

}
