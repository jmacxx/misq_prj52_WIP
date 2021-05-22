package com.misq.core.electrumd;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RpcServiceImpl {

    private RpcInterface client;
    private boolean walletIsOk;

    public RpcServiceImpl(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName) {
        try {
            client = RpcInterface.builder()
                    .rpcHost(rpcHost)
                    .rpcPort(rpcPort)
                    .rpcUser(rpcUser)
                    .rpcPassword(rpcPassword)
                    .walletName(walletName)
                    .build();
            walletIsOk = client.isSynchronized(); // blocking call
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public CompletableFuture<Long> getBlockCount() {
        // not supported by electrum
        return CompletableFuture.supplyAsync(() -> Long.valueOf(-1));
    }

    public CompletableFuture<String> getBalance() {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            Map<String, String> balanceObject = client.getBalance();
            String balance = balanceObject.get("confirmed");
            return balance;
        });
        return future;
    }

    public CompletableFuture<String> getNewAddress(String label) {
        return CompletableFuture.supplyAsync(() -> client.getNewAddress());
    }

    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> client.payTo(amount, address))
                .handle((txHex, ex) -> {
                    if (txHex != null) {
                        return txHex;
                    } else {
                        return "ERROR!";
                    }
                });
        return future;
    }
}
