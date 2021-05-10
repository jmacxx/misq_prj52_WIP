
### misqWalletRpc

Java library containing an interface to multiple different wallet implementations.

At the moment, this proof-of-concept only uses bitcoind RPC.
It will be extended to support litecoind RPC, elements RPC and monero RPC.

The wallet interface looks like this:

    // a generic wallet interface, supporting multiple different chains
    public interface Wallet {
    
        interface Listener {
            void onNewChainHeight(Wallet wallet, Long height);
            void onBalanceChanged(Wallet wallet, String balance);
        }
    
        CompletableFuture<Long> getChainHeight();
        CompletableFuture<String> getFreshReceivingAddress();
        CompletableFuture<String> getBalance();
        CompletableFuture<String> sendToAddress(String address, String amount, String memo);
    
        Wallet addListener(Listener listener);
        String toString();
    }



The project includes an example 'Kit' app that uses the wallet to perform various functions: check balance, get chain height, get a receiving address, send funds.


Example output:

```
    > Task :examples:Kit.main()
    starting..
    sleeping..
    bitcoind ===> balance notification from wallet: 4.996687
    elementsd ===> balance notification from wallet: 0.99970464
    waking up
    bitcoind doingWalletThings
    sleeping..
    bitcoind read balance: 4.996687
    bitcoind read chain height: 234
    bitcoind ===> chain height notification from wallet: 234
    bitcoind receving address is: 2MvFTM4yEeJjNGdcKPiMtA24EYX8Bp5Vz4J
    bitcoind sending 0.5 BTC...
    bitcoind sent funds to my receiving address, txId: 0a7b1338eee6ca403a42488d5f9dfc6a1beece28705fd1de0c1a76a6d74ff73c
    tcp://127.0.0.1:28332 received ZMQ update
    bitcoind ===> chain height notification from wallet: 234
    bitcoind ===> balance notification from wallet: 4.9966538
    waking up
    elementsd doingWalletThings
    sleeping..
    elementsd read balance: 0.99970464
    elementsd read chain height: 162
    elementsd ===> chain height notification from wallet: 162
    elementsd receving address is: AzpoL1J4go6CkfqaCWK3ur9fQyhHERVLvi6mNZDKBzKyk75gMmuEptiJmcqx9sNZgvxvxe6Zd2Dp85PH
    elementsd sending 0.5 BTC...
    elementsd sent funds to my receiving address, txId: 723191924fc2d96dc4c678c5a1bc1bfd023f70290de3ede700b04f2121aa1678
    tcp://127.0.0.1:29999 eceived ZMQ update
    elementsd ===> chain height notification from wallet: 162
    elementsd ===> balance notification from wallet: 0.99965218
```



#### Building from the command line

```
gradle clean build
```

#### Building from an IDE

Alternatively, just import the project using your IDE.

### Example applications

These are found in the `examples` module.


