
## Setting up an bitcoind wallet (regtest)

[Setup bitcoind](#setup)

[Create wallet](#create-wallet)

[Verify connectivity](#verify-connectivity)



---

### Setup

Download bitcoind (or build from source).  You'll end up with `bitcoind` and `bitcoin-cli` executables.

Set up your `~/.bitcoin/bitcoin.conf` config file, e.g.

```
txindex=1
listen=1
peerbloomfilters=1
server=1
daemon=1
debug=0
testnet=0
regtest=1
discardfee=0.00000001
mintxfee=0.00000001
minrelaytxfee=0.00000001
rpcuser=bisqdao
rpcpassword=bsq
zmqpubrawblock=tcp://127.0.0.1:28332
zmqpubrawtx=tcp://127.0.0.1:28333
```


### Create wallet


Start bitcoind:

```
bitcoind -listen=1 -regtest -server
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" getnewaddress
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq generatetoaddress 100 my_new_mining_address_eg_2N3siGQLvsyVB4ySs6skgXQGEVryiAH8KEN
```

Create & fund wallets:

```
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq createwallet alice
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq createwallet bob
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="bob" getnewaddress
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" sendtoaddress _bob_new_address 0.5
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq generatetoaddress 1 my_new_mining_address_eg_2N3siGQLvsyVB4ySs6skgXQGEVryiAH8KEN

```


---

### Verify connectivity

Once all that is running, verify RPC connectivity to bitcoind:
```
curl --user bisqdao --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:18443/
```


