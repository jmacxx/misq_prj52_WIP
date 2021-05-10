package com.misq.core.elementsd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

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
    private Map<String, String> amounts;
    @JsonProperty("confirmations")
    private Integer confirmations;
    @JsonProperty("label")
    private String label;
    @JsonProperty("txids")
    private List<String> txIds;

    public String getAmount() {
        // in elements blockchain, the amount field is a JSON object like {bitcoin=0.0}
        return amounts.get("bitcoin").toString();
    }
    public String getAddress() {
        return address;
    }
}
