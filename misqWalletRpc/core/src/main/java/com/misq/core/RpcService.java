package com.misq.core;

import java.util.concurrent.CompletableFuture;

public abstract class RpcService {

    public abstract CompletableFuture<Long> requestChainHeadHeight();
    public abstract CompletableFuture<String> getBalance();
    public abstract CompletableFuture<String> getFreshReceivingAddress(String label);
    public abstract CompletableFuture<String> sendToAddress(String address, String amount, String memo);
}
