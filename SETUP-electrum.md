
## Setting up an electrum wallet (regtest)


Precondition: you have setup bitcoind for regtest.

Steps:

- create the wallet in electrum, obtain the MPK & recovery phrase
- setup EPS & configure with the MPK
- setup electrum daemon and load the wallet
- verify connectivity

Repeat steps again if you want a second user (bob) in order to trade.

---


Create wallet in bitcoin core.
```bitcoin-cli -regtest createwallet alice2```

Run electrum GUI and create new wallets for alice & bob.  Save the details.

**Setup EPS**.  

You need to run an electrum server (such as electrum personal server or electrumx).  I run EPS; `config.ini` tells it the seed (MPK) for the wallet and the wallet name in bitcoin core:
Instructions [here](https://github.com/chris-belcher/electrum-personal-server#quick-start-on-a-debianubuntu-machine-with-a-running-bitcoin-full-node).
Customize `config.ini`

- enter the MPK and wallet file name.
- point to the appropriate port on bitcoind (18443 for regtest)
- enter the RPC user and password (bisqdao:bsq)
- tell EPS the appropriate port to listen for electrum clients (I use 50007 & 50008 for alice & bob).


Run EPS:

```python3.6 ./common.py ./config.ini```

Check for configuration/connectivity errors & rectify.



---


Run electrum as daemon and tell it to listen for alice or bob on the correct port, and load the wallet.

```electrum --regtest daemon
electrum --regtest setconfig rpcport 17777
electrum --regtest setconfig rpcuser bisqdao
electrum --regtest setconfig rpcpassword bsq
electrum --regtest daemon load_wallet -w /home/yourname/.electrum/regtest/wallets/alice
```

Once all that is running, verify RPC connectivity to bitcoind:
```
curl --user bisqdao --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:18443/
```
Then verify RPC connectivity to electrum:
```
curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"version","params":[]}' http://bisqdao:bsq@127.0.0.1:17777
{"result": "3.3.8", "id": "curltext", "jsonrpc": "2.0"}

curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"is_synchronized","params":[]}' http://bisqdao:bsq@127.0.0.1:17777
```


