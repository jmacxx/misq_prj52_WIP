
### misqWalletRpc EXPERIMENT 3

This version is the results of an attempt to get electrum to do the transaction building necessary to perform a 2-of-2 multisig trade between Alice & Bob.

It currently stands at being able to spend inputs from alice & bob combined to one UTXO similar to the deposit stage of the trade.

The limitation is that both private keys have to be provided together rather than in two separate signing calls.

This may be a limitation of electrum version 3.3, so I'm archiving this experiment before upgrading electrum and will continue in experiement 4.



High level overview of the multisig trade setup:

```
        // open wallets and do some stuff
        Wallet alice = new com.misq.core.electrumd.WalletImpl("alice2", "192.168.1.111", 17777).addListener(this);
        Wallet bob = new com.misq.core.electrumd.WalletImpl("bob2", "localhost", 17778).addListener(this);

        alice.listUnspent().whenComplete((aliceUnspent, ex1) -> {
            bob.listUnspent().whenComplete((bobUnspent, ex2) -> {
                alice.getFreshReceivingAddress().whenComplete((addr, ex0) -> {
                    List<Utxo> inputs = new ArrayList<>();
                    inputs.add(aliceUnspent.get(0));
                    inputs.add(bobUnspent.get(0));
                    List<Utxo> outputs = new ArrayList<>();
                    outputs.add(new Utxo().initForSpending(addr, "0.4999"));
                    alice.createRawTransaction(inputs, outputs).whenComplete((txHex, b) -> {
                        System.out.println(txHex);
                    });
                });
            });
        });
```

---

Example output:

```
"02000000000102ea032cd7f3cd794f71ac389da6e65aa30273bf668ee388d93f4e16e7aa16f46a0100000000feffffff146e055c524ab713c8a4d9e0d4c24711e316255caf3ebef921f5912cb624738e0000000000feffffff0170c9fa0200000000160014d48e1b70f89b6f8ddaac8a9b0b10b71a2338d4e00247304402200e5ba7bf488f37e1b00f34288c26d2721c945f0548736f46806a4c228051fedf022021c4a3cad9745bbcc568dbdb0cbac826f264fb2113f46ac986bebd6b6178f3d80121034b854b690485c9c9cbc375352eac53f0881c6f8c8fdaafdf8ba633891730bb260247304402206f178a2763c987f6c5b764c80a252d14aa2115db6d61fc4f6c38414c97515d0802205b5f7a5e6730d83979898d19214ed06f1ce18387e858531f4a639a1251c0e476012103707305760505c38c84cb16ca817f3bf7c2c07a689b8c3f84851d595b0c596d2000000000"

bitcoin-cli decoderawtransaction produces:

{
  "txid": "a68367aa5388ae1bcbd6728ebe7efd0b74eeceee96669e04f8e8fb07aba715a2",
  "hash": "04eb99fff02130daa625e276806642876f26939d109a7cb3c29ac3fefff11a91",
  "version": 2,
  "size": 339,
  "vsize": 177,
  "weight": 708,
  "locktime": 0,
  "vin": [
    {
      "txid": "6af416aae7164e3fd988e38e66bf7302a35ae6a69d38ac714f79cdf3d72c03ea",
      "vout": 1,
      "scriptSig": {
        "asm": "",
        "hex": ""
      },
      "txinwitness": [
        "304402200e5ba7bf488f37e1b00f34288c26d2721c945f0548736f46806a4c228051fedf022021c4a3cad9745bbcc568dbdb0cbac826f264fb2113f46ac986bebd6b6178f3d801",
        "034b854b690485c9c9cbc375352eac53f0881c6f8c8fdaafdf8ba633891730bb26"
      ],
      "sequence": 4294967294
    },
    {
      "txid": "8e7324b62c91f521f9be3eaf5c2516e31147c2d4e0d9a4c813b74a525c056e14",
      "vout": 0,
      "scriptSig": {
        "asm": "",
        "hex": ""
      },
      "txinwitness": [
        "304402206f178a2763c987f6c5b764c80a252d14aa2115db6d61fc4f6c38414c97515d0802205b5f7a5e6730d83979898d19214ed06f1ce18387e858531f4a639a1251c0e47601",
        "03707305760505c38c84cb16ca817f3bf7c2c07a689b8c3f84851d595b0c596d20"
      ],
      "sequence": 4294967294
    }
  ],
  "vout": [
    {
      "value": 0.49990000,
      "n": 0,
      "scriptPubKey": {
        "asm": "0 d48e1b70f89b6f8ddaac8a9b0b10b71a2338d4e0",
        "hex": "0014d48e1b70f89b6f8ddaac8a9b0b10b71a2338d4e0",
        "reqSigs": 1,
        "type": "witness_v0_keyhash",
        "addresses": [
          "bcrt1q6j8pku8cndhcmk4v32dsky9hrg3n348q2u4dmj"
        ]
      }
    }
  ]
}

```
