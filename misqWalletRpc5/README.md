
### misqWalletRpc EXPERIMENT 5

Adds LND to the list of supported wallets:

- bitcoind
- litecoind
- elementsd
- monerod
- electrum
- lnd

---

With capability, users of a wallet can enable/disable features depending on capability:


```
        if (wallet.getCapability().contains(Wallet.Capability.SEND_AND_RECEIVE)) {
            doSendAndReceiveTest(wallet);
        }
        if (wallet.getCapability().contains(Wallet.Capability.MULTISIG) && walletCounterparty != null) {
            doTradingTest(wallet, walletCounterparty);
        }
        if (wallet.getCapability().contains(Wallet.Capability.LAYER_2_LN)) {
            doLightningTest(wallet);
        }
```








### TODO: 

- clean up the wallet interface
- clean up the implementation of multisig trade test
- think about how to add lightning interface (the LND API is huge) 



---


