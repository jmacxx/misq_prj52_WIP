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
// https://www.getmonero.org/resources/developer-guides/wallet-rpc.html#create_address
public class Address {
    @JsonProperty("address")
    private String address;
    @JsonProperty("address_index")
    private Long addressIndex;

    public String getAddress() {
        return address;
    }
    public Long getAddressIndex() {
        return addressIndex;
    }
}
