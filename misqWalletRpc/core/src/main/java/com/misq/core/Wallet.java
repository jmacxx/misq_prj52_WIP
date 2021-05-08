package com.misq.core;

import java.util.concurrent.CompletableFuture;

// a generic wallet interface, supporting multiple different chains
public interface Wallet {

    interface Listener {
        void onNewChainHeight(Long height);
        void onBalanceChanged(String balance);
    }

    CompletableFuture<Long> getChainHeight();
    CompletableFuture<String> getFreshReceivingAddress();
    CompletableFuture<String> getBalance();
    CompletableFuture<String> sendToAddress(String address, String amount, String memo);

    Wallet addListener(Listener listener);
}
