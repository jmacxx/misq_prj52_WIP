package com.misq.core.monerod;

import com.misq.core.Wallet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WalletImpl implements Wallet {

    protected final Set<Listener> listeners = new HashSet<>();
    protected final RpcServiceImpl rpcService;
    protected Long chainHeight;
    protected String myBalance;

    public WalletImpl(String walletName, String walletPassword) {
        rpcService = new RpcServiceImpl("bisqdao", "bsq", "192.168.1.111", 18082, walletName, walletPassword);
    }

    @Override
    public CompletableFuture<Long> getChainHeight() {
        CompletableFuture<Long> future = rpcService.requestChainHeadHeight();
        future.handle((height, ex) -> {
            if (ex != null) {
                System.out.println("Ex." + ex.toString());
            } else {
                if (height != this.chainHeight) {
                    this.chainHeight = height;
                    listeners.forEach(e -> e.onNewChainHeight(this, height));
                }
            }
            return height;
        });
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress() {
        return rpcService.getFreshReceivingAddress("receiving");
    }

    @Override
    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        return rpcService.sendToAddress(address, amount, memo);
    }

    @Override
    public CompletableFuture<String> getBalance() {
        CompletableFuture<String> future = rpcService.getBalance();
        future.thenAccept(balance -> {
            if (!balance.equals(myBalance)) {
                myBalance = balance;
                listeners.forEach(e -> e.onBalanceChanged(this, balance));
            }
        });
        return future;
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public String getTokenName() { return "XMR"; }

    @Override
    public String toString() { return "monerod"; }
}
