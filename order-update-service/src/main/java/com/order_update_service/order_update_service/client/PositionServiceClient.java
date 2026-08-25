package com.order_update_service.order_update_service.client;

import com.order_update_service.order_update_service.model.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PositionServiceClient {

    private final RestClient restClient;
    private final String positionServiceUrl;

    public PositionServiceClient(
            RestClient restClient,
            @Value("${position.service.url}") String positionServiceUrl) {

        this.restClient = restClient;
        this.positionServiceUrl = positionServiceUrl;
    }

    public void sendEvent(OrderEvent event) {

        restClient.post()
                .uri(positionServiceUrl + "/events")
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }

}
