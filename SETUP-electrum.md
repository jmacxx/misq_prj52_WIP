
## Setting up an electrum wallet (regtest)

Precondition: you have setup bitcoind for regtest and it is running.

[Create wallets](#create-wallets)

[Setup EPS & configure with the MPK](#setup-eps)

[Setup electrum daemon and load the wallet](#setup-electrum-daemon)

[Verify connectivity](#verify-connectivity)


Repeat steps again if you want a second user (bob) in order to trade.

(electrum wallet maintains a singleton configuration, so if you are running alice & bob, one of them will need to be on a separate machine).

---

### Create wallets

```
bitcoin-cli -regtest createwallet alice2
bitcoin-cli -regtest createwallet bob2
bitcoin-cli -regtest loadwallet alice2
bitcoin-cli -regtest loadwallet bob2
bitcoin-cli -regtest listwallets
```

Run electrum GUI and create new wallets for alice & bob.  Save the recovery phrase and the MPK.

---

### Setup EPS

You need to run an electrum server (such as electrum personal server or electrumx).  I run EPS; `config.ini` tells it the seed (MPK) for the wallet and the wallet name in bitcoin core:
Instructions [here](https://github.com/chris-belcher/electrum-personal-server#quick-start-on-a-debianubuntu-machine-with-a-running-bitcoin-full-node).
Customize `config.ini`

- enter the MPK and wallet file name (the name that was setup in bitcoind).
- point to the appropriate port on bitcoind (18443 for regtest)
- enter the RPC user and password (bisqdao:bsq)
- tell EPS the appropriate port to listen for electrum clients (I use 50007 & 50008 for alice & bob).


Run EPS:

```
python3.6 ./common.py ./configAlice.ini
python3.6 ./common.py ./configBob.ini
```
On the first run it imports addresses into the wallets, this takes some time.  When done, restart EPS with the same command.




---

### Setup electrum daemon

Run electrum as daemon and tell it to listen for alice or bob on the correct port, and load the wallet.

```electrum --regtest daemon
electrum --regtest setconfig rpcport 17777
electrum --regtest setconfig rpcuser bisqdao
electrum --regtest setconfig rpcpassword bsq
electrum --regtest daemon load_wallet -w /home/yourname/.electrum/regtest/wallets/alice2
```


---

### Verify connectivity

Once all that is running, verify RPC connectivity to bitcoind:
```
curl --user bisqdao --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getblockcount", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:18443/
```
Then verify RPC connectivity to electrum:
```
curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"version","params":[]}' http://bisqdao:bsq@127.0.0.1:17778
{"result": "3.3.8", "id": "curltext", "jsonrpc": "2.0"}

curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"is_synchronized","params":[]}' http://bisqdao:bsq@127.0.0.1:17778
{"result": true, "id": "curltext", "jsonrpc": "2.0"}

curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"getbalance","params":[]}' http://bisqdao:bsq@192.168.1.111:17777
{"result": {"confirmed": "0.4"}, "id": "curltext", "jsonrpc": "2.0"}
curl --data-binary '{"jsonrpc":"2.0","id":"curltext","method":"getbalance","params":[]}' http://bisqdao:bsq@127.0.0.1:17778
{"result": {"confirmed": "0.1"}, "id": "curltext", "jsonrpc": "2.0"}

```




