
### misqWalletRpc EXPERIMENT 4

This version is the results of an attempt to get electrum to do the transaction building necessary to perform a 2-of-2 multisig trade between Alice & Bob.  After upgrading to electrum 4.1, alice & bob can sign their own part of the deposit transaction, then the PSBT signatures can be combined into the final complete transaction.  (PSBT parsing had to be coded on the fly and it is not very complete).



High level overview of the multisig trade setup:

```
    public void spendingFromTwoWalletsTest(com.misq.core.electrumd.WalletImpl alice, com.misq.core.electrumd.WalletImpl bob) {
        // experiment 4 : get funds from alice & bob, spend them into a new address
        alice.listUnspent().whenComplete((aliceUnspent, ex1) -> {
            bob.listUnspent().whenComplete((bobUnspent, ex2) -> {
                String addr = "2N38zZiAx3uVob9KULWCpH2uPtRAqTasyXn"; // TODO: hardcoded from bitcoin core
                List<Utxo> inputs = new ArrayList<>();
                inputs.add(aliceUnspent.get(0));
                inputs.add(bobUnspent.get(0));
                List<Utxo> outputs = new ArrayList<>();
                outputs.add(new Utxo().initForSpending(addr, "0.109"));
                alice.createRawTransaction(inputs, outputs).whenComplete((psbtAlice, b) -> {
                    System.out.println("Alice signed tx: " + psbtAlice);
                    bob.createRawTransaction(inputs, outputs).whenComplete((psbtBob, c) -> {
                        System.out.println("Bob signed tx: " + psbtBob);
                        String txUnsigned = extractTxFromPsbt(psbtAlice);
                        String signatureAlice = extractSignatureFromPsbt(psbtAlice);
                        String signatureBob = extractSignatureFromPsbt(psbtBob);
                        String signedTx = combineTxAndSignatures(txUnsigned, signatureBob, signatureAlice);
                        System.out.println("Complete tx: " + signedTx);
                        bob.broadcastTransaction(signedTx).whenComplete((txId, exBroadcast) -> {
                            System.out.println("Broadcasted txid: " + txId);
                        });
                    });
                });
            });
        });
    }```

---


