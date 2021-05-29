
### lndWalletDemo

A simple use of the LND GRPC interface which queries the chain height, wallet balance, gets a new receiving address, and sends the wallet balance.

This was adapted from a LND Java example by https://github.com/willcl-ark which was very useful because figuring out how to use all the TLS and macaroon authentication would be an impossible task..

---

Code:

```
    String receivingAddress = "";
    {
      Lnrpc.GetInfoRequest request = Lnrpc.GetInfoRequest.getDefaultInstance();
      Lnrpc.GetInfoResponse response = stub.getInfo(request);
      System.out.println("Block height=" + response.getBlockHeight());
    }
    {
      Lnrpc.WalletBalanceRequest request = Lnrpc.WalletBalanceRequest.getDefaultInstance();
      Lnrpc.WalletBalanceResponse response = stub.walletBalance(request);
      System.out.println("Wallet balance=" + response.getTotalBalance());
    }
    {
      Lnrpc.NewAddressRequest request = Lnrpc.NewAddressRequest.getDefaultInstance();
      Lnrpc.NewAddressResponse response = stub.newAddress(request);
      receivingAddress = response.getAddress();
      System.out.println("Wallet receiving address=" + response.getAddress());
    }
    {
      Lnrpc.SendCoinsRequest request = Lnrpc.SendCoinsRequest.newBuilder()
              .setAddr(receivingAddress)
              .setSendAll(true)
              .setLabel("demo")
              .build();
      Lnrpc.SendCoinsResponse response = stub.sendCoins(request);
      System.out.println("Sent coins, txID=" + response.getTxid());
    }
```

---


Example output:

```
> Task :Main.main()
Block height=386
Wallet balance=49967078
Wallet receiving address=bcrt1q0fn2298lhej05hexgxjf3edsglmz06duwfd5x7
Sent coins, txID=bd93cfb57654cbce9ba27463dd38a150017391b47c0cd81d57b7892565b32a0f
```



#### Building from the command line

```
gradle clean build
```

#### Building from an IDE

Alternatively, just import the project using your IDE.


