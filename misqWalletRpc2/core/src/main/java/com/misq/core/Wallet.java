package com.misq.core;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// a generic wallet interface, supporting multiple different chains
public interface Wallet {

    interface Listener {
        void onNewChainHeight(Wallet wallet, Long height);
        void onBalanceChanged(Wallet wallet, String balance);
    }

    // wallet specific
    CompletableFuture<String> getBalance();
    CompletableFuture<String> getFreshReceivingAddress();
    CompletableFuture<String> sendToAddress(String address, String amount, String memo);
    CompletableFuture<String> getPubKey(String address);
    CompletableFuture<String> getPrivKey(String address);
    CompletableFuture<String> signRawTransactionWithWallet(String txHex);
    CompletableFuture<List<Utxo>> listUnspent();

    // transaction building fns, not dependent on wallet
    CompletableFuture<MultisigInfo> createMultisig(List<String> keys);
    CompletableFuture<String> createRawTransaction(List<Utxo> inputs, List<Utxo> outputs);
    CompletableFuture<String> signRawTransactionWithKey(String txHex, String privKey, String depositTxId, int vout, String scriptPubKey, String witnessScript, String amount);

    // blockchain protocol interaction
    CompletableFuture<Long> getChainHeight();
    CompletableFuture<String> broadcastTransaction(String txHex);


    Wallet addListener(Listener listener);
    String getTokenName();      // e.g. selected wallet token BTC, LTC, XMR
    String toString();          // wallet impl name e.g. bitcoind, litecoind, monerod
}
