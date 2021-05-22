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
@JsonPropertyOrder({"txid", "vout", "sequence"})
public class TransactionInput {
    public TransactionInput(String a, Integer b, Integer c) { this.txid=a; this.vout=b; this.sequence=c; }
    @JsonProperty("txid")
    public String txid;
    @JsonProperty("vout")
    public Integer vout;
    @JsonProperty("sequence")
    public Integer sequence;
}
