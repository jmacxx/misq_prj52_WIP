package com.misq.core.monerod.RawDto;

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
@JsonPropertyOrder({"balance"})
// https://www.getmonero.org/resources/developer-guides/wallet-rpc.html#get_balance
public class Balance {
    @JsonProperty("balance")
    private String balance;

    public String getBalance() {
        return balance;
    }
}
