package com.misq.core.monerod;

import com.misq.core.monerod.RawDto.IncomingTransfersResponse;
import com.misq.core.monerod.RawDto.IncomingTransfersDetail;
import com.misq.core.monerod.RawDto.Transfer;
import com.misq.core.monerod.RawDto.TransferResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RpcServiceImpl {

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

    public CompletableFuture<String> getBalance() {
        final CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> convertPiconeroToXmr(client.getBalance().getBalance()))
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

    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            Transfer e = new Transfer();
            e.setAddress(address);
            e.setAmount(convertXmrToPiconero(amount));
            List<Transfer> x = new ArrayList<>();
            x.add(e);
            TransferResult result = client.transfer(x);
            return result.getTxHash();
        });
        return future;
    }

    public CompletableFuture<List<String>> getIncomingTransfers() {
        final CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
            IncomingTransfersResponse x = client.getIncomingTransfers("all");
            List<String> results = new ArrayList<>();
            for (IncomingTransfersDetail info : x.transfers) {
                results.add(info.getTxHash());
            }
            return results;
        });
        return future;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // UTILITY
    ///////////////////////////////////////////////////////////////////////////////////////////

    private static String convertXmrToPiconero(String xmrAmount) {
        // input is in whole XMR (including decimal place).. convert to XMR atomic units (piconero)
        String[] tokens = xmrAmount.split("\\.");
        String xmr = "";
        String piconero = "";
        if (tokens.length > 0) {
            xmr = tokens[0];
            if (xmr.equalsIgnoreCase("0"))
                xmr = "";
        }
        if (tokens.length > 1) {
            piconero = tokens[1];
            // ensure there are 12 zeros after the decimal place
            while (piconero.length() < 12) {
                piconero = piconero + "0";
            }
        }
        return xmr + piconero;
    }

    private static String convertPiconeroToXmr(String input) {
        // input is in atomic units (piconero)
        String xmr = "0";
        String piconero = "";
        if (input.length() > 12) {
            // more than zero before the decimal
            xmr = input.substring(0, input.length()-12);
            piconero = input.substring(input.length()-12);
            return xmr + "." + piconero;
        } else {
            // zero before the decimal
            piconero = input;
            while (piconero.length() < 12) {
                piconero = "0" + piconero;
            }
            return "0." + piconero;
        }
    }


}
