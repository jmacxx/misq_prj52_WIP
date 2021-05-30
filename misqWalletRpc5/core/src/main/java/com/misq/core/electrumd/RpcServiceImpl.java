package com.misq.core.electrumd;

import com.googlecode.jsonrpc4j.JsonRpcClientException;
import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.utils.Coin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public CompletableFuture<MultisigInfo> createMultiSig(Integer numKeys, List<String> keys) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> multisigMap = client.createMultiSig(numKeys, keys);
            MultisigInfo multisigInfo = new MultisigInfo();
            multisigInfo.address = multisigMap.get("address");
            multisigInfo.witnessScript = multisigMap.get("redeemScript");
            return multisigInfo;
        });
    }

    public CompletableFuture<String> getPubKey(String address) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> keys = client.getPubKeys(address);
            if (keys.size() > 0)
                return keys.get(0);
            else
                return "UNKNOWN";
        });
    }

    // get all the UTXO contained in the wallet
    public CompletableFuture<List<Utxo>> listUnspent() {
        // translate from bitcoin RPC specific structs to interface representation
        return CompletableFuture.supplyAsync(() -> {
            List<Utxo> retVal = new ArrayList<>();
            List<TransactionOutput> rpcSpecificUtxos = client.listUnspent();
            for (TransactionOutput txo : rpcSpecificUtxos) {
                Utxo x = new Utxo().initFromUnspent(txo.txId, txo.vout, "???", txo.amount);
                x.address = txo.address;
                retVal.add(x);
            }
            return retVal;
        });
    }

    // get UTXO from a specific txId (not wallet specific)
    private String getAddressPubkey(String address) {
        List<String> keys = client.getAddressPubkey(address);
        if (keys.size() > 0)
            return keys.get(0);
        else
            return "UNKNOWN";
    }

    // get UTXO from a specific txId (not wallet specific)
    public CompletableFuture<List<Utxo>> listUnspent(String txId) {
        // translate from bitcoin RPC specific structs to interface representation
        return CompletableFuture.supplyAsync(() -> {
            List<Utxo> retVal = new ArrayList<>();
            String txHex = client.getTransaction(txId);
            Map<String, Object> txJson = client.deserialize(txHex);
            List<Map<String, String>> outputsJson = (List<Map<String, String>>) txJson.get("outputs");
            for (Map<String, String> txo : outputsJson) {
                retVal.add(new Utxo().initFromUnspent(txId, Integer.valueOf(txo.get("prevout_n")), txo.get("scriptPubKey"), txo.get("value"))); // JMC TODO convert value from sats
            }
            return retVal;
        });
    }

    public CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs) {
        return CompletableFuture.supplyAsync(() -> {
            List<Object> inputsArray = new ArrayList<>();
            List<Object> outputsArray = new ArrayList<>();
            for (Utxo txi : inputs) {
                Map<String, Object> inputsMap = new HashMap<>();
                inputsMap.put("output", txi.txId + ":" + txi.vout.toString());
                inputsMap.put("value_sats", Coin.parseCoin(txi.amount).longValue());
                inputsMap.put("value", Coin.parseCoin(txi.amount).longValue());
                inputsMap.put("address", txi.address);
                // look up the privkey in our wallet for signing whatever inputs we can
                try {
                    String privKey = client.getAddressPrvkey(txi.address);
                    if (privKey != null && privKey.length() > 0)
                        inputsMap.put("privkey", privKey);
                } catch (JsonRpcClientException ex) {
                    System.out.println(ex.getCause());
                }
                inputsArray.add(inputsMap);
            }
            for (Utxo txo : outputs) {
                Map<String, Object> outputsMap = new HashMap<>();
                outputsMap.put("address", txo.address);
                outputsMap.put("value_sats", Coin.parseCoin(txo.amount).longValue());
                outputsMap.put("value", Coin.parseCoin(txo.amount).longValue());
                outputsArray.add(outputsMap);
            }
            Map<String, Object> jsonO = new HashMap<>();
            jsonO.put("inputs", inputsArray);
            jsonO.put("outputs", outputsArray);
            String signed = client.serialize(jsonO);
            return signed;
        });
    }

    public String signTransaction(String txHex, String privKey) {
        //String retVal = client.signTransaction(txHex, privKey);
        return null;
    }

    public CompletableFuture<String> broadcastTransaction(String txHex) {
        return CompletableFuture.supplyAsync(() -> client.broadcastTransaction(txHex));
    }
}
