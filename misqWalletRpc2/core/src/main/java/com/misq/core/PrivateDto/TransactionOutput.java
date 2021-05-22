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
@JsonPropertyOrder({"txid", "vout", "amount"})
public class TransactionOutput {
    @JsonProperty("txid")
    public String txId;
    @JsonProperty("vout")
    public Integer vout;
    @JsonProperty("amount")
    public String amount;
}


//createrawtransaction "[
// {\"txid\":\"63eb4808fd8a0513ddfcdba264d1d91b87ec73012e473509ecf9082ae70e5ef1\",\"vout\":0},
// {\"txid\":\"d25a44aa262d1a76cafa522e6830641d52f8ce670d4da2be460ae7602d37b3c8\",\"vout\":0}]" "[
// {\"bcrt1qv443fnx26pmca8ptlp8cytmx8n0fs7k4uxqqneety5cjj28faprqafjqet\":0.299}]"