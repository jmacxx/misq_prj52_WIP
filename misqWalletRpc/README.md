
### misqWalletRpc

Java library containing an interface to multiple different wallet implementations.

At the moment, this proof-of-concept supports bitcoind, litecoind, elementsd, and monerod RPC wallets.

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
        String getTokenName();      // e.g. selected wallet token BTC, LTC, XMR
        String toString();          // wallet impl name e.g. bitcoind, litecoind, monerod
    }



The project includes an example 'Kit' app that uses the wallet to perform various functions: check balance, get chain height, get a receiving address, send funds.


Example output:

```
> Task :examples:Kit.main()
starting..
bitcoind doingWalletThings
sleeping..
bitcoind read chain height: 234
bitcoind ===> chain height notification from wallet: 234
bitcoind read balance: 4.9957828 BTC
bitcoind ===> balance notification from wallet: 4.9957828 BTC
bitcoind receving address is: 2MwHQb4L5r4oTjvUinu8vCYqb3GAiP59tUS
bitcoind sending 0.5 BTC...
bitcoind sent funds to my receiving address, txId: d22804423e32b792ae6cde8e4f3ff8436329248376b5ef7784213ded9a02e9b0
tcp://127.0.0.1:28332 received ZMQ update
bitcoind ===> chain height notification from wallet: 234
bitcoind ===> balance notification from wallet: 4.9957316 BTC
waking up
litecoind doingWalletThings
sleeping..
litecoind read chain height: 102
litecoind ===> chain height notification from wallet: 102
litecoind read balance: 3.131144 LTC
litecoind ===> balance notification from wallet: 3.131144 LTC
litecoind receving address is: QTNbkiwtp8ro4exkZ15XRMDtziiK2aS6Q5
litecoind sending 0.5 LTC...
litecoind sent funds to my receiving address, txId: 6c79d81055ad9980711e1dc51dd7c0c547d979b652c26ebdc7cf45be1c3e1be9
tcp://127.0.0.1:29332 received ZMQ update
litecoind ===> balance notification from wallet: 3.130632 LTC
waking up
elementsd doingWalletThings
elementsd read chain height: 162
sleeping..
elementsd ===> chain height notification from wallet: 162
elementsd read balance: 0.99891774 L-BTC
elementsd ===> balance notification from wallet: 0.99891774 L-BTC
elementsd receving address is: AzpjkwthnpF6y1CcxW6yBzibZPKcStPXN3vzwykothUeMb9PLXgninoyXQ4xic8mrbBCEiPECGckNs5m
elementsd sending 0.5 L-BTC...
elementsd sent funds to my receiving address, txId: 21aac10dad793261c23e4c6e03614f75886ce7c42f8ad6012fe6406371414ec6
tcp://127.0.0.1:29999 received ZMQ update
elementsd ===> chain height notification from wallet: 162
elementsd ===> balance notification from wallet: 0.99886528 L-BTC
waking up
monerod doingWalletThings
sleeping..
monerod ===> balance notification from wallet: 0 XMR
monerod ===> chain height notification from wallet: 1
monerod read chain height: 1
monerod read balance: 0 XMR
monerod receving address is: 86evGU2rWzZcT1GH1jybe9HFHAiVekd2oYx7TKruyyqrVY55J1GnkhbVcgFQt6gAD4b3jAXgcJ55BFqhhXPfJLsiMhjvu1d
monerod sending 0.5 XMR...
monerod sent funds to my receiving address, txId: NOT_IMPLEMENTED
waking up
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


