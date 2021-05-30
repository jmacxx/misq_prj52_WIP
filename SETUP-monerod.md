
# misqWalletRpc

&nbsp;



## Setting up a monerod wallet (stagenet)

I ran monerod and monero-wallet-rpc on a node machine in my LAN.
Replace 192.168.1.111 with the address from your own setup.

```
monerod --stagenet --rpc-login=bisqdao:bsq --no-igd
monero-wallet-rpc --stagenet --daemon-login=bisqdao:bsq --rpc-bind-port 18083 -rpc-bind-ip 0.0.0.0 --confirm-external-bind --rpc-login=bisqdao:bsq --wallet-dir . --log-level 3 
```


**Checking connectivity and creating/funding the wallet**

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



&nbsp;


---



