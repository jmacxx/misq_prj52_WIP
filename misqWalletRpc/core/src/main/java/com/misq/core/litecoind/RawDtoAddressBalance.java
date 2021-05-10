package com.misq.core.litecoind;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"address", "amount", "confirmations", "label", "txids"})
public class RawDtoAddressBalance {
    @JsonProperty("address")
    private String address;
    @JsonProperty("amount")
    String amount;
    @JsonProperty("confirmations")
    private Integer confirmations;
    @JsonProperty("label")
    private String label;
    @JsonProperty("txids")
    private List<String> txIds;

    public String getAmount() {
        return amount;
    }
    public String getAddress() {
        return address;
    }
}
