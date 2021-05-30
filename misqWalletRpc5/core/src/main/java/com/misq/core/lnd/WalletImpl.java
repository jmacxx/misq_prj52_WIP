package com.misq.core.lnd;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.core.Wallet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WalletImpl implements Wallet {

    protected final Set<Listener> listeners = new HashSet<>();
    protected Long chainHeight;
    protected String myBalance;
    private RpcServiceImpl rpcService;

    public WalletImpl() {
        try {
            this.rpcService = new RpcServiceImpl("127.0.0.1", 10009);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    @Override
    public CompletableFuture<String> unlock(String password) {
        return null;    // TODO
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
        return rpcService.getNewAddress();
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
        return null;
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
        return null;
    }

    @Override
    public CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs) {
        return null;
    }

    @Override
    public CompletableFuture<List<Utxo>> listUnspent() {
        return null;
    }

    @Override
    public CompletableFuture<List<Utxo>> listUnspent(String txId) {
        return null;
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public String getTokenName() { return "BTC"; }

    @Override
    public String toString() { return "lnd:"; }

    @Override
    public EnumSet<Capability> getCapability() {
        return EnumSet.of(Capability.SEND_AND_RECEIVE, Capability.LAYER_2_LN);
    }
}
