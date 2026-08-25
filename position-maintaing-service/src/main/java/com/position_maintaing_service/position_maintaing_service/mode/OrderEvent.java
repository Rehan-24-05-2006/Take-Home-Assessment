package com.position_maintaing_service.position_maintaing_service.mode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  OrderEvent {

    @JsonProperty("event_id")
    private String eventId;

    private String symbol;

    @JsonProperty("transaction_type")
    private String transactionType;

    private Integer quantity;

}
