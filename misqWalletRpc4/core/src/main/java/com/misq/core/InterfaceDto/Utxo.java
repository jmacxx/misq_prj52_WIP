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
    public String scriptPubKey;

    public Utxo initFromUnspent(String txId, Integer vout, String scriptPubKey, String amount) {
        this.txId = txId;
        this.vout = vout;
        this.amount = amount;
        this.scriptPubKey = scriptPubKey;
        this.address = "";
        return this;
    }

    public Utxo initForSpending(String address, String amount) {
        this.address = address;
        this.amount = amount;
        this.txId = "";
        this.vout = 0;
        this.scriptPubKey = "";
        return this;
    }
}
