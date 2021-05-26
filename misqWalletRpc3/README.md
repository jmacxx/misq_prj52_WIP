
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

        // experiment 3 : get funds from alice & bob, spend them into a new address
        // the limitation with electrum is that createRawTransaction only works if you supply the private keys for the inputs
        // see core/electrumd/RpcServiceImpl::listUnspent() it obtains the private keys there
        // I've been unable to create an unsigned transaction with v3.3 of electrum.
        alice.listUnspent().whenComplete((aliceUnspent, ex1) -> {
            bob.listUnspent().whenComplete((bobUnspent, ex2) -> {
                alice.getFreshReceivingAddress().whenComplete((addr, ex0) -> {
                    List<Utxo> inputs = new ArrayList<>();
                    inputs.add(aliceUnspent.get(0));
                    inputs.add(bobUnspent.get(0));
                    List<Utxo> outputs = new ArrayList<>();
                    outputs.add(new Utxo().initForSpending(addr, "0.1"));
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
02000000
0001
02
ea032cd7f3cd794f71ac389da6e65aa30273bf668ee388d93f4e16e7aa16f46a 01000000 00 feffffff
146e055c524ab713c8a4d9e0d4c24711e316255caf3ebef921f5912cb624738e 00000000 00 feffffff
01
8096980000000000 160014d48e1b70f89b6f8ddaac8a9b0b10b71a2338d4e0
02
47 30440220604672a34bc626241d5351677ac0c0935c5406722cb792991c6c8627e82ee866022031b856850bf52a0f9e43ffc2e417ed7cdbc5a367633b2541bdc4ce9e66b173f701
21 034b854b690485c9c9cbc375352eac53f0881c6f8c8fdaafdf8ba633891730bb26
02
48 3045022100dce9f8390b2d3ca91a618b634c12986528398248cdbb60b1c500150a5b74038a022055acd3010976834a9ab7ed6251d46cbc64d0f728cbd5984e8b85b11a479c690b01
21 03707305760505c38c84cb16ca817f3bf7c2c07a689b8c3f84851d595b0c596d20
00000000

```


