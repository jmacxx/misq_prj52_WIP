
### misqWalletRpc

Java library containing an interface to multiple different wallet implementations.

At the moment, this proof-of-concept supports bitcoind, litecoind, elementsd, and monerod RPC wallets.

The wallet interface looks like this:

    // wallet specific
    CompletableFuture<String> getBalance();
    CompletableFuture<String> getFreshReceivingAddress();
    CompletableFuture<String> sendToAddress(String address, String amount, String memo);
    CompletableFuture<String> getPubKey(String address);
    CompletableFuture<String> getPrivKey(String address);
    CompletableFuture<String> signRawTransactionWithWallet(String txHex);
    CompletableFuture<List<Utxo>> listUnspent();

    // transaction building fns, not dependent on wallet
    CompletableFuture<MultisigInfo> createMultisig(List<String> keys);
    CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs);
    CompletableFuture<String> signRawTransactionWithKey(String txHex, String privKey, String depositTxId, int vout, String scriptPubKey, String witnessScript, String amount);

    // blockchain protocol interaction
    CompletableFuture<Long> getChainHeight();
    CompletableFuture<String> broadcastTransaction(String txHex);




New in this version is MultisigTest which does a 2-of-2 multisig trade between Alice and Bob.

Status for various wallets:

- bitcoind & litecoind fully supported
- elementsd: tracking down a bug (it fails when funding the multisig)
- monerod: not supported
- electrum: not tried yet, but electrum's RPC interface is very limited and does not provide the transaction building methods needed.


High level overview of the multisig trade setup:

```
    void setupMultisig(Wallet alice, Wallet bob) {
        MultisigTest multisigTest = new MultisigTest(alice, bob);
        try {
            reportBalances(alice, bob);
            multisigTest.setupMultisigKeys();
            multisigTest.getFunding();
            multisigTest.createMultisig();
            multisigTest.createSignedDepositTx();
            multisigTest.depositTxId = multisigTest.broadcastTx(multisigTest.signedDepositTx);
            sleep(10);
            multisigTest.createSignedPayoutTx();
            multisigTest.payoutTxId = multisigTest.broadcastTx(multisigTest.signedPayoutTx);
            sleep(10);
            reportBalances(alice, bob);
        } catch (IOException ex) {
            System.out.println("EXCEPTION: " + ex.toString());
        }
    }
```

---

Example output:

```
starting..
bitcoind:bob read balance: 0.45 BTC
bitcoind:alice read balance: 0.25 BTC
bitcoind:alice got unspents: 0.25
bitcoind:bob got unspents: 0.45
signedtx: 02000000000102fa1b3b7db43176f8085d8abf8f09da30cdea036174adca0dee8c984ba6f58f9f0100000017160014f5d06a73130e9ac6f809de02bdaff185338ab7a3000000003d38d5b72176bd44547a89381569533d12d2715c5533881ed3ce130dce03f572000000001716001403dc4f84c56df32c525892ada6c2694d4b7710fb000000000198192c0400000000220020a6eefb5c546632680710de01b7bb0d2599d1b8a19f378a3dd495cf0ed57be5c70247304402200bd6ad22ead1462bc899d5a1abb54c927d365a71d29abe95cddd473a0995854b022062ca678e214539b1aee4f3053748ab63d8f9ca5558f7ea47b04bc49f7bf17bcb0121020d79c8b39b3105912df03cbb8ee072144214ebe2800fa6708c04ac41c503fa0a0247304402205ca3140533c656c75c58f69ae48a7b9b3c7d8a664cab077228fe83c43a37fc84022059b7756c23674b7a703db98df11c2dd78f8dafe3600cfa8f01d0d3fa5e6627f6012102c728858dc19503f75908fe3122e9b67ba2e884b5667b1ba146e8dae7078c4c5000000000
broadcast: f1019d1f2bff040c23ded81a837bc060d3b9937bd99ba1a23f20834cf047c584
sleeping..
waking up
Spending amount = 0.69999
signedtx: 0200000000010184c547f04c83203fa2a19bd97b93b9d360c07b831ad8de230c04ff2b1f9d01f10000000000000000000240a5ae020000000017a91477fac2b47bbe71d30762db3306bbf815ce07ecb18770707d010000000017a9143a7dc2e800722063df99bdcbff3cc80a3ad8e03c870400473044022001a314db0b9e076e2ed2873393165d2d340792ca402977e542484984fd246d1e0220490f3b8d47a678758990af1ff9ebfd234648d1aa583c7447894aa7b2bba63fa301473044022061efd3585514c003f3e7da08cdb8a5632028591486e2d399b80b3fce7b26e2990220438051fe3f98cc90a40575150536c63cf8e31d22e2c95ca309aa75a3798c591201475221031d2627a570f578e4c09ed5fc0832423affcd4ef44a2aa78063a9565ff03f316921038733130906b3f03cde3e0cb2bd07473c797c172fcc775daa3850af31c6cbbd3f52ae00000000
broadcast: 9390d1b34ee3d1b7221f568cb5a3a218be25e1faa6ca2dda15a858fb137893ac
sleeping..
waking up
bitcoind:bob read balance: 0.24998 BTC
bitcoind:alice read balance: 0.45 BTC
sleeping..



> Task :examples:Kit.main()
starting..
litecoind:alice read balance: 1.0 LTC
litecoind:bob read balance: 2.0 LTC
litecoind:alice got unspents: 1.0
litecoind:bob got unspents: 2.0
signedtx: 02000000000102797e7e0b718d6499d184bf422c57670c2b4838147b6ef17a2ab3bbb7eff50262010000001716001401906ce0c6d66428e5b266e5ac4545aa27bad6ed0000000091e3a1887a6b6c3dea739caeab03516b51fb580f3fe1d3820a1ccdf022fc4250010000001716001495c05850cd3829795a5d4ee0c0710aaed1d3ee070000000001189fe1110000000022002096fcde5d2394852b43daf8fc195789f86a86c677dcc6b8902a6fda327ad25ea1024730440220401bb4f1ee7d22c08de2d60ccb0e94f72e566cdf34c868d9740acf106479d86e02203ddda928c17ceaedf6af9624011779b952eb47f615bef14757c8a6b97379c5870121023c9830436261a7c75938a5279452f6719b7c71ac16119706061ddda35306bb24024730440220403552cf0b2509f4b3b2e1e19116c03b77e298ca10f17f68976518860292032e022064ac19719cdb71310e1cba12961eea9f57c0f78b9759c9cb359e4e3d72b8130d0121020584a345aacce74b1345be30fc816f59d3071d72a1b487070c1d9a5a8de7157400000000
broadcast: 5d56d0223c68906c478a3bf0dee7205b1c5859304bcfccc8387957cd57f89623
sleeping..
waking up
Spending amount = 2.99999
signedtx: 020000000001012396f857cd577938c8cccf4b3059581c5b20e7def03b8a476c90683c22d0565d0000000000000000000200c2eb0b0000000017a9146c01c3904b0f13a0cbb2404755df784b6adc9dde8730d9f5050000000017a9149edf14dec73daf4cc09ff66cac57e4c0c9c823db8704004730440220485916d0b98eb99f81c6ef1c98e09705f375499ece2a789b500c10f1d593f82f02200b5d1cefe90eb60f60de30199255af8012226713d44a241a39b0fb8c473346e301473044022071affc63eacb7b37bb985f4e9a7e8aa28588a9a3a96d50751f2ddab9af9cd8d402207bc96c2c91d10217573e0cee29eb9c50015e46ba3109093d8ad77a94787f3d070147522103de21a35c36d4ab98d7bb5fcec935a66a5eb5fd27d70586eb07c7b2cfe4e087f52103911f35f49890b75672b5ed0d989e8c2c0638f43da841d4d0a6d529fbee6221ca52ae00000000
broadcast: 4c32b05bb8eb23fe9395cc6f136a162b0ac9dd654d3358337b79c04f0a376f5a
sleeping..
waking up
litecoind:bob read balance: 0.99998 LTC
litecoind:alice read balance: 2.0 LTC
sleeping..



```



#### Building from the command line

```
gradle clean build
```

#### Building from an IDE

Alternatively, just import the project using your IDE.

### Example applications

These are found in the `examples` module.


