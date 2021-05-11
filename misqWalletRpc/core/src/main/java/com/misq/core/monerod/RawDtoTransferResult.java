package com.misq.core.monerod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawDtoTransferResult {
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("fee")
    private String fee;
    @JsonProperty("tx_hash")
    private String txHash;

    public String getTxHash() { return txHash; }
}
