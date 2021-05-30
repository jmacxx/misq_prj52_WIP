
## Setting up an LND wallet (regtest)

Precondition: you have setup bitcoind for regtest and it is running.

[Setup LND](#setup)

[Create wallet](#create-wallet)

[Verify connectivity](#verify-connectivity)


---

### Setup

Download LND (or build from source).  You'll end up with `lnd` and `lncli` executables.

Set up your `~/.lnd/lnd.conf` config file, e.g.

```
[Application Options]
debuglevel=info
maxpendingchannels=10

[Bitcoin]
bitcoin.active=true
bitcoin.testnet=false
bitcoin.mainnet=false
bitcoin.regtest=true
bitcoin.node=bitcoind

[Bitcoind]
bitcoind.rpcuser=bisqdao
bitcoind.rpcpass=bsq
bitcoind.zmqpubrawblock=tcp://127.0.0.1:28332
bitcoind.zmqpubrawtx=tcp://127.0.0.1:28333

[neutrino]
neutrino.connect=127.0.0.1

```


### Create wallet


Start LND:


```
lnd --bitcoind.regtest --bitcoin.active
lncli create
```

Save the recovery phrase.

```
lncli unlock
```

---

### Verify connectivity

Once all that is running, verify RPC connectivity to bitcoind:
```
curl --user bisqdao --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:18443/
```

Advance a block on bitcoind, and observe that LND picks it up:

```
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq generatetoaddress 1 your_mining_address_eg_2N3siGQLvsyVB4ySs6skgXQGEVryiAH8KEN

```


```
2021-05-30 15:08:43.351 [INF] CRTR: Pruning channel graph using block 7fac056a0dd5deb56db739ee85aaaf824e81623522c25fe5a0ccc3de86a51d54 (height=398)
2021-05-30 15:08:43.352 [INF] NTFN: New block: height=398, sha=7fac056a0dd5deb56db739ee85aaaf824e81623522c25fe5a0ccc3de86a51d54
2021-05-30 15:08:43.352 [INF] UTXN: Attempting to graduate height=398: num_kids=0, num_babies=0
2021-05-30 15:08:43.475 [INF] CRTR: Block 7fac056a0dd5deb56db739ee85aaaf824e81623522c25fe5a0ccc3de86a51d54 (height=398) closed 0 channels
```

