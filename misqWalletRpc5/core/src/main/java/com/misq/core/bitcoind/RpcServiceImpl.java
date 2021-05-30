package com.misq.core.bitcoind;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.PrivateDto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RpcServiceImpl {

    private RpcInterface client;

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

    public CompletableFuture<Long> getBlockCount() {
        return CompletableFuture.supplyAsync(() -> client.getBlockCount());
    }

    public CompletableFuture<String> getBalance() {
        return CompletableFuture.supplyAsync(() -> client.getBalance());
    }

    public CompletableFuture<String> getNewAddress(String label) {
        return CompletableFuture.supplyAsync(() -> client.getNewAddress(label));
    }

    public CompletableFuture<AddressInfo> getAddressInfo(String address) {
        return CompletableFuture.supplyAsync(() -> client.getAddressInfo(address));
    }

    public CompletableFuture<List<TransactionOutput>> listUnspent() {
        return CompletableFuture.supplyAsync(() -> client.listUnspent());
    }

    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        return CompletableFuture.supplyAsync(() -> client.sendToAddress(address, amount, memo));
    }

    public CompletableFuture<MultisigInfo> createMultiSig(Integer numKeys, List<String> keys, String addressType) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> multisigMap = client.createMultiSig(numKeys, keys, addressType);
            MultisigInfo multisigInfo = new MultisigInfo();
            multisigInfo.address = multisigMap.get("address");
            multisigInfo.witnessScript = multisigMap.get("redeemScript");
            Map<String, Object> scriptInfo = client.decodeScript(multisigInfo.witnessScript);
            Map<String, Object> segwitPortion = (Map<String, Object>) scriptInfo.get("segwit");
            multisigInfo.scriptPubKey = (String) segwitPortion.get("hex");
            return multisigInfo;
        });
    }

    public CompletableFuture<String> createRawTransaction(List<TransactionInput> utxos, Map<String, String> transactionOutputs) {
        return CompletableFuture.supplyAsync(() -> client.createRawTransaction(utxos, transactionOutputs));
    }

    public CompletableFuture<Map<String, Object>> signRawTransactionWithWallet(String txHex) {
        return CompletableFuture.supplyAsync(() -> client.signRawTransactionWithWallet(txHex));
    }

    public CompletableFuture<Map<String, Object>> signRawTransactionWithKey(String txHex, String privKey, PrevTxInfo prevTxInfo) {
        List<String> keys = new ArrayList<>();
        keys.add(privKey);
        List<PrevTxInfo>prevTxInfoList = new ArrayList<>();
        prevTxInfoList.add(prevTxInfo);
        return CompletableFuture.supplyAsync(() -> client.signRawTransactionWithKey(txHex, keys, prevTxInfoList));
    }

    public CompletableFuture<String> sendRawTransaction(String txHex) {
        return CompletableFuture.supplyAsync(() -> client.sendRawTransaction(txHex));
    }

    public CompletableFuture<String> getPrivKey(String address) {
        return CompletableFuture.supplyAsync(() -> client.getPrivKey(address));
    }
}
