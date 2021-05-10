package com.misq.core.rpc;

import com.misq.utils.Coin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RpcServiceBitcoind extends RpcService {

    private BitcoindClient client;

    public RpcServiceBitcoind(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName) {
        try {
            client = BitcoindClient.builder()
                    .rpcHost(rpcHost)
                    .rpcPort(rpcPort)
                    .rpcUser(rpcUser)
                    .rpcPassword(rpcPassword)
                    .walletName(walletName)
                    .build();
            CompletableFuture.supplyAsync(() -> client.loadWallet(walletName));
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletableFuture<Long> requestChainHeadHeight() {
        final CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> client.getBlockCount());
        return future;
    }

    @Override
    public CompletableFuture<String> getBalance() {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> client.getBalance());
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress(String label) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            List<RawDtoAddressBalanceBitcoind> addressBalanceList = client.listReceivedByAddress(0, true);
            for (RawDtoAddressBalanceBitcoind info : addressBalanceList) {
                if (Coin.parseCoin(info.getAmount()).equals(Coin.ZERO)) {
                    return info.getAddress();
                }
            }
            return client.getNewAddress(label);
        });
        return future;
    }

    @Override
    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    return client.sendToAddress(address, amount, memo);
                });
        return future;
    }

    /*
    @Override
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
}
