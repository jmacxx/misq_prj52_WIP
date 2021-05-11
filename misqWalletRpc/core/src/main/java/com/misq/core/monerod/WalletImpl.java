package com.misq.core.monerod;

import com.misq.core.Wallet;
import com.misq.utils.UserThread;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WalletImpl implements Wallet {

    protected final Set<Listener> listeners = new HashSet<>();
    protected final RpcServiceImpl rpcService;
    protected Long chainHeight;
    protected String myBalance;

    public WalletImpl(String walletName, String walletPassword) {
        rpcService = new RpcServiceImpl("bisqdao", "bsq", "192.168.1.111", 18083, walletName, walletPassword);
        notificationThread().start();
    }

    @Override
    public CompletableFuture<Long> getChainHeight() {
        CompletableFuture<Long> future = rpcService.requestChainHeadHeight();
        future.handle((height, ex) -> {
            if (ex != null) {
                System.out.println("Ex." + ex.toString());
            } else {
                if (!height.equals(this.chainHeight)) {
                    this.chainHeight = height;
                    listeners.forEach(e -> e.onNewChainHeight(this, height));
                    getBalance();   // notify wallet clients if the balance has changed
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

    public CompletableFuture<List<String>> getIncomingTransfers() {
        return rpcService.getIncomingTransfers();
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    private Thread notificationThread() {
        return new Thread(() -> {
            boolean run = true;
            while(run) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    UserThread.execute(() -> getChainHeight());
                } catch (InterruptedException e) {
                    System.out.println("Notification thread exit");
                    run = false;
                }
            }
        });
    }

    @Override
    public String getTokenName() { return "XMR"; }

    @Override
    public String toString() { return "monerod"; }
}
