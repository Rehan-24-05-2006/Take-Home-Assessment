package com.position_maintaing_service.position_maintaing_service;

import com.position_maintaing_service.position_maintaing_service.mode.OrderEvent;
import com.position_maintaing_service.position_maintaing_service.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PositionMaintaingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

    private PositionService positionService;

    @BeforeEach
    void setUp() {
        positionService = new PositionService();
    }

    @Test
    void shouldIncreasePositionForBuy() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                );

        positionService.processEvent(event);

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(
                90,
                positions.get("RELIANCE")
        );
    }

    @Test
    void shouldDecreasePositionForSell() {

        OrderEvent event =
                new OrderEvent(
                        "evt-0001",
                        "TCS",
                        "SELL",
                        75
                );

        positionService.processEvent(event);

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(
                -75,
                positions.get("TCS")
        );
    }

    @Test
    void shouldHandleMultipleSymbols() {

        positionService.processEvent(
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );

        positionService.processEvent(
                new OrderEvent(
                        "evt-0002",
                        "TCS",
                        "SELL",
                        75
                )
        );

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(90, positions.get("RELIANCE"));
        assertEquals(-75, positions.get("TCS"));
    }

    @Test
    void shouldAllowNegativePosition() {

        positionService.processEvent(
                new OrderEvent(
                        "evt-0001",
                        "TCS",
                        "SELL",
                        100
                )
        );

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(
                -100,
                positions.get("TCS")
        );
    }

    @Test
    void shouldKeepSymbolWhenPositionBecomesZero() {

        positionService.processEvent(
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );

        positionService.processEvent(
                new OrderEvent(
                        "evt-0002",
                        "RELIANCE",
                        "SELL",
                        90
                )
        );

        Map<String, Integer> positions =
                positionService.getPositions();

        assertTrue(
                positions.containsKey("RELIANCE")
        );

        assertEquals(
                0,
                positions.get("RELIANCE")
        );
    }

    @Test
    void shouldIgnoreDuplicateEventId() {

        OrderEvent firstEvent =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                );

        OrderEvent duplicateEvent =
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        50
                );

        positionService.processEvent(firstEvent);
        positionService.processEvent(duplicateEvent);

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(
                90,
                positions.get("RELIANCE")
        );
    }

    @Test
    void shouldProcessDifferentEventIds() {

        positionService.processEvent(
                new OrderEvent(
                        "evt-0001",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );

        positionService.processEvent(
                new OrderEvent(
                        "evt-0002",
                        "RELIANCE",
                        "BUY",
                        50
                )
        );

        Map<String, Integer> positions =
                positionService.getPositions();

        assertEquals(
                140,
                positions.get("RELIANCE")
        );
    }

}
