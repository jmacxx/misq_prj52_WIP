package com.misq.core.monerod;

import java.util.concurrent.CompletableFuture;

public class RpcServiceImpl extends com.misq.core.RpcService {

    private RpcInterface client;

    public RpcServiceImpl(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName, String walletPassword) {
        try {
            client = RpcInterface.builder()
                    .rpcHost(rpcHost)
                    .rpcPort(rpcPort)
                    .rpcUser(rpcUser)
                    .rpcPassword(rpcPassword)
                    .walletName(walletName)
                    .build();
            CompletableFuture.runAsync(() -> client.openWallet(walletName, walletPassword));    // should block?
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletableFuture<Long> requestChainHeadHeight() {
        final CompletableFuture<Long> future = CompletableFuture
                .supplyAsync(() -> client.getBlockCount().get("height"))
                .handle((msg, ex) -> {
                    if (ex != null) {
                        System.out.println(ex.toString());
                        return -1L;
                    } else {
                        return msg;
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<String> getBalance() {
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> client.getBalance().getBalance())
                .handle((msg, ex) -> {
                    if (ex != null) {
                        System.out.println(ex.toString());
                        return "ERROR";
                    } else {
                        return msg;
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress(String label) {
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> client.createAddress(0L, label).getAddress())
                .handle((msg, ex) -> {
                    if (ex != null) {
                        System.out.println(ex.toString());
                        return "ERROR";
                    } else {
                        return msg;
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "NOT_IMPLEMENTED";
        });
        return future;
    }
}
