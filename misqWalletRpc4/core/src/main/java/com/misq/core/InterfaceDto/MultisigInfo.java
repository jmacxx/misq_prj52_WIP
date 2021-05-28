package com.misq.core.InterfaceDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MultisigInfo {
    public String address;
    public String scriptPubKey;
    public String witnessScript;
}
