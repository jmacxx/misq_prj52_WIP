package com.misq.core.PrivateDto;

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
@JsonPropertyOrder({"txid", "vout", "scriptPubKey", "witnessScript", "amount"})
public class PrevTxInfo {
    @JsonProperty("txid")
    public String txId;
    @JsonProperty("vout")
    public Integer vout;
    @JsonProperty("scriptPubKey")
    public String scriptPubKey;
    @JsonProperty("witnessScript")
    public String witnessScript;
    @JsonProperty("amount")
    public String amount;
}
