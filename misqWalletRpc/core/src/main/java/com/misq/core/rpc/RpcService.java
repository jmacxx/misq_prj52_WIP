package com.misq.core.rpc;

import com.misq.utils.Coin;
import com.google.common.util.concurrent.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class RpcService {

    private BitcoindClient client;

    // We could use multiple threads but then we need to support ordering of results in a queue
    // Keep that for optimization after measuring performance differences
    private final ListeningExecutorService executor = getSingleThreadListeningExecutor("RpcService");


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    public RpcService(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName) {
        try {
            client = BitcoindClient.builder()
                    .rpcHost(rpcHost)
                    .rpcPort(rpcPort)
                    .rpcUser(rpcUser)
                    .rpcPassword(rpcPassword)
                    .walletName(walletName)
                    .build();
            executor.submit(() -> {
                return client.loadWallet(walletName);
            });
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void shutDown() {
        executor.shutdown();
    }

    public CompletableFuture<Long> requestChainHeadHeight() {
        final CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> client.getBlockCount(), executor);
        return future;
    }

    public CompletableFuture<String> getBalance() {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> client.getBalance(), executor);
        return future;
    }

    public CompletableFuture<String> getFreshReceivingAddress(String label) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            List<RawDtoAddressBalance> addressBalanceList = client.listReceivedByAddress(0, true);
            for (RawDtoAddressBalance info : addressBalanceList) {
                if (Coin.parseCoin(info.getAmount()).equals(Coin.ZERO)) {
                    return info.getAddress();
                }
            }
            return client.getNewAddress(label);
        }, executor);
        return future;
    }

    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return client.sendToAddress(address, amount, memo);
        }, executor);
        return future;
    }

    /*
    public void listReceivedByAddress(Consumer<List<RawDtoAddressBalance>> resultHandler, Consumer<Throwable> errorHandler) {
        ListenableFuture<List<RawDtoAddressBalance>> future = executor.submit(client::listReceivedByAddress);
        Futures.addCallback(future, new FutureCallback<>() {
            public void onSuccess(List<RawDtoAddressBalance> results) {
                UserThread.execute(() -> resultHandler.accept(results));
            }

            public void onFailure(@NotNull Throwable throwable) {
                UserThread.execute(() -> errorHandler.accept(throwable));
            }
        }, MoreExecutors.directExecutor());
    }
    */

    private static ListeningExecutorService getSingleThreadListeningExecutor(String name) {
        return MoreExecutors.listeningDecorator(getSingleThreadExecutor(name));
    }

    private static ExecutorService getSingleThreadExecutor(String name) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(name)
                .setDaemon(true)
                .build();
        return Executors.newSingleThreadExecutor(threadFactory);
    }
}
