package com.misq.core.electrumd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"address", "value", "prevout_n", "prevout_hash", "height"})
public class TransactionOutput {
    @JsonProperty("address")
    public String address;
    @JsonProperty("value")
    public String amount;
    @JsonProperty("prevout_n")
    public Integer vout;
    @JsonProperty("prevout_hash")
    public String txId;
    @JsonProperty("height")
    public Long height;
}

