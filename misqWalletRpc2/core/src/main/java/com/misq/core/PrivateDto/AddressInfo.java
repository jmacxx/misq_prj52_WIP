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
@JsonPropertyOrder({"address", "pubkey"})
public class AddressInfo {
    @JsonProperty("address")
    private String address;
    @JsonProperty("pubkey")
    private String pubKey;

    public String getAddress() {
        return address;
    }
    public String getPubKey() {
        return pubKey;
    }
}
