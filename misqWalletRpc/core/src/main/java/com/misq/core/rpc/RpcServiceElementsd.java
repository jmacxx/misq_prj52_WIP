package com.misq.core.rpc;

import com.misq.utils.Coin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RpcServiceElementsd extends RpcService {

    private ElementsdClient client;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    public RpcServiceElementsd(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName) {
        try {
            client = ElementsdClient.builder()
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
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    Map<String, String> balances = client.getBalance();
                    return balances.get("bitcoin");
                });
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress(String label) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            List<RawDtoAddressBalanceElementsd> addressBalanceList = client.listReceivedByAddress(0, true);
            for (RawDtoAddressBalanceElementsd info : addressBalanceList) {
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
}
