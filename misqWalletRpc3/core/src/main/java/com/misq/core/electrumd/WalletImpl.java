package com.misq.core.electrumd;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.core.PrivateDto.TransactionOutput;
import com.misq.core.Wallet;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WalletImpl implements Wallet {

    protected final Set<Listener> listeners = new HashSet<>();
    protected final RpcServiceImpl rpcService;
    protected Long chainHeight;
    protected String myBalance;

    public WalletImpl(String walletName, String rpcHost, int rpcPort) {
        this.rpcService = new RpcServiceImpl("bisqdao", "bsq", rpcHost, rpcPort, walletName);
    }

    @Override
    public CompletableFuture<Long> getChainHeight() {
        CompletableFuture<Long> future = rpcService.getBlockCount();
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
        final CompletableFuture<String> future = rpcService.getBalance();
        future.thenAccept(balance -> {
            if (!balance.equals(myBalance)) {
                myBalance = balance;
                listeners.forEach(e -> e.onBalanceChanged(this, balance));
            }
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
        return null;
    }

    @Override
    public CompletableFuture<String> getPrivKey(String address) {
        return null;
    }

    @Override
    public CompletableFuture<String> getPubKey(String address) {
        return rpcService.getPubKey(address);
    }

    @Override
    public CompletableFuture<String> signRawTransactionWithWallet(String txHex) {
        return null;
    }

    @Override
    public CompletableFuture<String> signRawTransactionWithKey(String txHex, String privKey, String depositTxId, int vout, String scriptPubKey, String witnessScript, String amount) {
        return null;
    }

    @Override
    public CompletableFuture<MultisigInfo> createMultisig(List<String> keys) {
        return rpcService.createMultiSig(2, keys);
    }

    @Override
    public CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs) {
        return rpcService.createRawTransaction(inputs, outputs);
    }

    @Override
    public CompletableFuture<List<Utxo>> listUnspent() {
        return rpcService.listUnspent();
    }

    @Override
    public CompletableFuture<List<Utxo>> listUnspent(String txId) {
        return rpcService.listUnspent(txId);
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public String getTokenName() { return "BTC"; }

    @Override
    public String toString() { return "electrumd"; }
}
