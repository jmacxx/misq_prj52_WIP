package com.misq.core.monerod.RawDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
// https://www.getmonero.org/resources/developer-guides/wallet-rpc.html#transfer
public class TransferResult {
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("fee")
    private String fee;
    @JsonProperty("tx_hash")
    private String txHash;

    public String getTxHash() { return txHash; }
}
