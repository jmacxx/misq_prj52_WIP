package com.misq.core.litecoind;

import com.misq.utils.Coin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RpcServiceImpl extends com.misq.core.RpcService {

    private com.misq.core.litecoind.RpcInterface client;

    public RpcServiceImpl(String rpcUser, String rpcPassword, String rpcHost, int rpcPort, String walletName) {
        try {
            client = RpcInterface.builder()
                    .rpcHost(rpcHost)
                    .rpcPort(rpcPort)
                    .rpcUser(rpcUser)
                    .rpcPassword(rpcPassword)
                    .walletName(walletName)
                    .build();
            if (!client.listWallets().contains(walletName)) {
                System.out.println("loading wallet: " + walletName);
                client.loadWallet(walletName); // blocking call
            }
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
            List<RawDtoAddressBalance> addressBalanceList = client.listReceivedByAddress(0, true);
            for (RawDtoAddressBalance info : addressBalanceList) {
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
