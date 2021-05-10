package com.misq.core;

import java.util.concurrent.CompletableFuture;

// a generic wallet interface, supporting multiple different chains
public interface Wallet {

    interface Listener {
        void onNewChainHeight(Wallet wallet, Long height);
        void onBalanceChanged(Wallet wallet, String balance);
    }

    CompletableFuture<Long> getChainHeight();
    CompletableFuture<String> getFreshReceivingAddress();
    CompletableFuture<String> getBalance();
    CompletableFuture<String> sendToAddress(String address, String amount, String memo);

    Wallet addListener(Listener listener);
    String getTokenName();      // e.g. selected wallet token BTC, LTC, XMR
    String toString();          // wallet impl name e.g. bitcoind, litecoind, monerod
}
