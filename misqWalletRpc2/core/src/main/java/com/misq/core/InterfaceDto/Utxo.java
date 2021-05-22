package com.misq.core.InterfaceDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Utxo {
    public String txId;
    public Integer vout;
    public String address;
    public String amount;

    public Utxo initFromUnspent(String txId, Integer vout, String amount) {
        this.txId = txId;
        this.vout = vout;
        this.amount = amount;
        this.address = "";
        return this;
    }

    public Utxo initForOutput(String address, String amount) {
        this.txId = "";
        this.vout = 0;
        this.address = address;
        this.amount = amount;
        return this;
    }
}
