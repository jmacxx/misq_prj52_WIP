
### misqWalletRpc

Java library containing an interface to multiple different wallet implementations.

At the moment, this proof-of-concept only uses bitcoind RPC.
It will be extended to support litecoind RPC, elements RPC and monero RPC.

The project includes an example 'Kit' app that uses the wallet to perform various functions: check balance, get chain height, get a receiving address, send funds.


Example output:

```
10:11:17 AM: Executing task 'Kit.main()'...

> Task :core:extractIncludeProto UP-TO-DATE
> Task :core:extractProto UP-TO-DATE
> Task :core:generateProto UP-TO-DATE
> Task :core:compileJava UP-TO-DATE
> Task :core:processResources NO-SOURCE
> Task :core:classes UP-TO-DATE
> Task :core:jar UP-TO-DATE
> Task :examples:compileJava
> Task :examples:processResources NO-SOURCE
> Task :examples:classes

> Task :examples:Kit.main()
starting..
sleeping..
===> balance notification from wallet: 5.9995088
read chain height from wallet: 230
===> chain height notification from wallet: 230
read balance from wallet: 5.9995088
read receiving address from wallet: 2Msp8DetrbEYscpQiKgCWUB6fE54PtDScNd
sleeping..
waking up
sent funds to my receiving address, txId: 176fd0223d6106178a0cb39d540460e272450566716b478b2138f081497c3ef4
Received ZMQ update
===> chain height notification from wallet: 230
===> balance notification from wallet: 5.9994576
waking up
sleeping..
Received ZMQ update
===> chain height notification from wallet: 231
Received ZMQ update
===> chain height notification from wallet: 232
Received ZMQ update
===> chain height notification from wallet: 233
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


