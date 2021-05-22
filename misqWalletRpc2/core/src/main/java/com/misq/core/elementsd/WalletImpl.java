package com.misq.core.elementsd;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.core.PrivateDto.PrevTxInfo;
import com.misq.core.PrivateDto.TransactionInput;
import com.misq.core.PrivateDto.TransactionOutput;
import com.misq.core.Wallet;
import com.misq.utils.UserThread;

import com.google.common.io.BaseEncoding;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WalletImpl implements Wallet {

    protected final Set<Listener> listeners = new HashSet<>();
    protected final RpcServiceImpl rpcService;
    protected Long chainHeight;
    protected String myBalance;

    public WalletImpl(String walletName) {
        this.rpcService = new RpcServiceImpl("bisqdao", "bsq", "127.0.0.1", 18884, walletName);
        zmqThread("tcp://127.0.0.1:29999").start();
    }

    @Override
    public CompletableFuture<Long> getChainHeight() {
        CompletableFuture<Long> future = rpcService.requestChainHeadHeight();
        future.thenAccept(height -> {
            if (height != this.chainHeight) {
                this.chainHeight = height;
                listeners.forEach(e -> e.onNewChainHeight(this, height));
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<String> getBalance() {
        CompletableFuture<String> future = rpcService.getBalance();
        future.handle((balance, ex) -> {
            if (!balance.equals(myBalance)) {
                myBalance = balance;
                listeners.forEach(e -> e.onBalanceChanged(this, balance));
            }
            return balance;
        });
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress() {
        return rpcService.getNewAddress("receiving");
    }

    @Override
    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        return rpcService.sendToAddress(address, amount, memo);
    }


    @Override
    public CompletableFuture<String> broadcastTransaction(String txHex) {
        return rpcService.sendRawTransaction(txHex);
    }

    @Override
    public CompletableFuture<String> getPrivKey(String address) {
        return rpcService.getPrivKey(address);
    }

    @Override
    public CompletableFuture<String> getPubKey(String address) {
        return rpcService.getAddressInfo(address).handle((addressInfo,ex) -> {
            return addressInfo.getPubKey();
        });
    }

    @Override
    public CompletableFuture<String> signRawTransactionWithWallet(String txHex) {
        return rpcService.signRawTransactionWithWallet(txHex).handle((result,ex) -> {
            String x = (String) result.get("hex");
            return x;
        });
    }

    @Override
    public CompletableFuture<String> signRawTransactionWithKey(String txHex, String privKey, String depositTxId, int vout, String scriptPubKey, String witnessScript, String amount) {
        PrevTxInfo prevTxInfo = new PrevTxInfo();
        prevTxInfo.txId = depositTxId;
        prevTxInfo.vout = vout;
        prevTxInfo.scriptPubKey = scriptPubKey;
        prevTxInfo.witnessScript = witnessScript;
        prevTxInfo.amount = amount;
        return rpcService.signRawTransactionWithKey(txHex, privKey, prevTxInfo).handle((result,ex) -> {
            String x = (String) result.get("hex");
            return x;
        });
    }

    @Override
    public CompletableFuture<MultisigInfo> createMultisig(List<String> keys) {
        return rpcService.createMultiSig(2, keys, "bech32");
    }

    @Override
    public CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs) {
        // translate from interface representation to bitcoin RPC specific structs
        List<TransactionInput> txIns = new ArrayList<>();
        Map<String, String> txOuts = new HashMap<>();
        for (Utxo utxo : inputs) {
            txIns.add(new TransactionInput(utxo.txId, utxo.vout, 0));
        }
        for (Utxo utxo : outputs) {
            txOuts.put(utxo.address, utxo.amount);
        }
        return rpcService.createRawTransaction(txIns, txOuts);
    }

    @Override
    public CompletableFuture<List<Utxo>> listUnspent() {
        // translate from bitcoin RPC specific structs to interface representation
        return rpcService.listUnspent().handle((rpcSpecificUtxos,b) -> {
            List<Utxo> retVal = new ArrayList<>();
            for (TransactionOutput txo : rpcSpecificUtxos) {
                retVal.add(new Utxo().initFromUnspent(txo.txId, txo.vout, txo.amount));
            }
            return retVal;
        });
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    private Thread zmqThread(String endpoint) {
        return new Thread() {
            private final BaseEncoding HEX = BaseEncoding.base16().lowerCase();
            private ZContext context;
            private ZMQ.Socket socket;
            @Override
            public void run() {
                context = new ZContext();
                socket = context.createSocket(SocketType.SUB);
                socket.connect(endpoint);
                socket.subscribe("");
                while(true) {
                    String work = "";
                    byte[] reply = socket.recv(0);
                    while (reply != null) {
                        work += HEX.encode(reply);
                        reply = socket.recv(ZMQ.NOBLOCK);
                    }
                    final String hexString = work;
                    UserThread.execute(() -> {
                        getChainHeight();
                        getBalance();
                        System.out.println(endpoint + " received ZMQ update"); //: [" + hexString + "]");
                    });
                }
            }
        };
    }

    @Override
    public String getTokenName() { return "L-BTC"; }

    @Override
    public String toString() { return "elementsd"; }
}
