
### misqWalletRpc

### Setting up a bitcoind wallet (regtest)

These daemons were setup to run on the same dev machine as the misqWalletRpc process.  To run them on a remote machine might need a few tweaks (see monero setup below).

    bitcoind  -regtest -prune=0 -txindex=1 -debug=1 -peerbloomfilters=1 -server -rpcuser=bisqdao -rpcpassword=bsq -datadir=. -zmqpubhashtx=tcp://127.0.0.1:28332
    litecoind -regtest -prune=0 -txindex=1 -debug=1 -peerbloomfilters=1 -server -rpcuser=bisqdao -rpcpassword=bsq -datadir=. -zmqpubhashtx=tcp://127.0.0.1:29332


### Checking connectivity and creating/funding the wallet

```
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq getversion
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" getnewaddress
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" generatetoaddress 100 [address]
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" createwallet "test123"
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" sendtoaddress [address] 0.5
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="" generatetoaddress 1 [address]
bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet="test123" getbalance
```


### Setting up a monerod wallet (stagenet)

I ran monerod and monero-wallet-rpc on a node machine in my LAN.
Replace 192.168.1.111 with the address from your own setup.

```
monerod --stagenet --rpc-login=bisqdao:bsq --no-igd
monero-wallet-rpc --stagenet --daemon-login=bisqdao:bsq --rpc-bind-port 18083 -rpc-bind-ip 0.0.0.0 --confirm-external-bind --rpc-login=bisqdao:bsq --wallet-dir . --log-level 3 
```


### Checking connectivity and creating/funding the wallet

```
curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_version"}'

curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"create_wallet","params":{"filename":"test124","password":"test124","language":"English"}}'
curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"open_wallet","params":{"filename":"test124","password":"test124"}}'

curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"create_address","params":{"account_index":0,"label":"hello world"}}'
curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_address"}'
curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_balance"}'

// go to monero stagenet faucet and fund your address: https://community.xmr.to/faucet/stagenet/

curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_balance"}'

curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_version"}'
curl -u bisqdao:bsq --digest -X POST http://192.168.1.111:18083/json_rpc -d '{"method":"get_address","params":{"account_index":0}}'
```




## Setting up an elementsd wallet (regtest)

I ran elementsd locally (same as bitcoind/litecoind).

.elements/elements.conf:

```
mainchainrpcport=18443
mainchainrpcuser=bisqdao
mainchainrpcpassword=bsq
chain=elementsregtest
rpcuser=bisqdao
rpcpassword=bsq
elementsregtest.rpcport=18884
elementsregtest.port=18886
validatepegin=0
debug=1
peerbloomfilters=1
con_signed_blocks=1
con_blockheightinheader=1
zmqpubhashtx=tcp://127.0.0.1:29999
```


### Funding (peg-in) elements L-BTC for regtest

Requires an already setup bitcoind regtest.

```
elements-cli -rpcuser=bisqdao -rpcpassword=bsq getnewaddress
    Azpv2akNw17MnMJZ8NgT7pdHytFgp8JRSvjxdPqY4wRhbFyi1AyGNGZqetpbFB7ZsBr7FbpBZw5CUKZp

elements-cli -rpcuser=bisqdao -rpcpassword=bsq getblockcount
    1410

elements-cli -rpcuser=bisqdao -rpcpassword=bsq getbalance
    {
      "bitcoin": 0.00000000
    }

elements-cli -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet=test123 getpeginaddress
    {
      "mainchain_address": "2N3i4C56DiqfpdcAJsAdZd2xYpCQMRAroye",
      "claim_script": "0014fe701a576e501d37b163f57c976aa3711a5c395c"
    }

bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq -rpcwallet=test123 sendtoaddress 2N3i4C56DiqfpdcAJsAdZd2xYpCQMRAroye 1
    6912a30535c2f3f793825872f97ac214bdbb31e4035f7cfd52b4b9830ceafef6

bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq getrawtransaction 6912a30535c2f3f793825872f97ac214bdbb31e4035f7cfd52b4b9830ceafef6
    02000000000101d9d5ddaee78739859d7a34df8f5908d8a9b84ef90d77a6672a5888bb80657a5800000000171600149a84084ed677f9fe8df55332bf241b476c73966afeffffff024041a1cd0000000017a91474baa7efa5bb0a5394bbf5f35763e1bf3e9ea7418700e1f5050000000017a91472c44f957fc011d97e3406667dca5b1c930c4026870247304402204f6ec7f33a538fb08b509013ec5efa0b467385d2bd2ad1dceb2050996165a9ad02203b50108625337c5db73433f01411e7c5f017b1ae82ca78d37951d9b5b857307f0121038ced1ba02828ba94582e01204689d16ebbe3f84b3918500b27578b18caf15b9d38170000

bitcoin-cli -regtest -rpcuser=bisqdao -rpcpassword=bsq gettxoutproof "[\"6912a30535c2f3f793825872f97ac214bdbb31e4035f7cfd52b4b9830ceafef6\"]"
    00000020d99dd855d3ebf2fa7724017891a813c5d44d38359f240afb4eb5fd43fffbc86bba43497b0feb8115583007822ade4554d5a42e142d7796427301c69a9263cbf281798360ffff7f200200000002000000028fe3869d1bab6b636e5e6562d75de81714258a675f908b4f0644a8830bdb2ffbf6feea0c83b9b452fd7c5f03e431bbbd14c27af972588293f7f3c23505a312690105

elements-cli -rpcuser=bisqdao -rpcpassword=bsq claimpegin "02000000000101d9d5ddaee78739859d7a34df8f5908d8a9b84ef90d77a6672a5888bb80657a5800000000171600149a84084ed677f9fe8df55332bf241b476c73966afeffffff024041a1cd0000000017a91474baa7efa5bb0a5394bbf5f35763e1bf3e9ea7418700e1f5050000000017a91472c44f957fc011d97e3406667dca5b1c930c4026870247304402204f6ec7f33a538fb08b509013ec5efa0b467385d2bd2ad1dceb2050996165a9ad02203b50108625337c5db73433f01411e7c5f017b1ae82ca78d37951d9b5b857307f0121038ced1ba02828ba94582e01204689d16ebbe3f84b3918500b27578b18caf15b9d38170000" "00000020d99dd855d3ebf2fa7724017891a813c5d44d38359f240afb4eb5fd43fffbc86bba43497b0feb8115583007822ade4554d5a42e142d7796427301c69a9263cbf281798360ffff7f200200000002000000028fe3869d1bab6b636e5e6562d75de81714258a675f908b4f0644a8830bdb2ffbf6feea0c83b9b452fd7c5f03e431bbbd14c27af972588293f7f3c23505a312690105" "0014fe701a576e501d37b163f57c976aa3711a5c395c"
    ce3faa3d7f9fdaa5870097d312a7365eb83c2dfc7c47393fce2516aa90648eac

elements-cli -rpcuser=bisqdao -rpcpassword=bsq generatetoaddress 1 Azpv2akNw17MnMJZ8NgT7pdHytFgp8JRSvjxdPqY4wRhbFyi1AyGNGZqetpbFB7ZsBr7FbpBZw5CUKZp
    [
      "b7a147ed4756419287189eb043e37e64250e0c1958ade9812a610732c8f24cb2"
    ]

elements-cli -rpcuser=bisqdao -rpcpassword=bsq getbalance
    {
      "bitcoin": 0.99999310
    }

```



---


