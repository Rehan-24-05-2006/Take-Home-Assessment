package com.order_update_service.order_update_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    @JsonProperty("event_id")
    @NotBlank(message = "event_id must not be blank")
    private String eventId;

    @NotBlank(message = "symbol must not be blank")
    private String symbol;

    @JsonProperty("transaction_type")
    @NotBlank(message = "transaction_type must not be blank")
    private String transactionType;

    @NotNull(message = "quantity must not be null")
    @Positive(message = "quantity must be a positive integer")
    private Integer quantity;

}
