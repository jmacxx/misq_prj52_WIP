package com.misq.examples;

import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.core.Wallet;
import com.misq.utils.Coin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MultisigTest {

    private Wallet aliceWallet;
    private Wallet bobWallet;
    private String aliceAddress, alicePubkey, bobAddress, bobPubkey;
    private String alicePayoutAddress, bobPayoutAddress;
    private String aliceFundingAmount, bobFundingAmount;
    private MultisigInfo multisigDepositInfo;
    public String signedDepositTx, depositTxId;
    public String signedPayoutTx, payoutTxId;
    private List<Utxo> depositTransactionInputs = new ArrayList<>();
    private List<Utxo> payoutTransactionInputs = new ArrayList<>();

    public MultisigTest(Wallet alice, Wallet bob) {
        this.aliceWallet = alice;
        this.bobWallet = bob;
    }

    public void setupMultisigKeys() throws IOException {
        // create public keys used for controlling ownership of the multisig
        CompletableFuture<String> aliceFuture = aliceWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> {aliceAddress = a;})
                .thenCompose(aliceWallet::getPubKey)
                .whenComplete((a,b) -> {alicePubkey = a;});
        CompletableFuture<String> bobFuture = bobWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> {bobAddress = a;})
                .thenCompose(bobWallet::getPubKey)
                .whenComplete((a,b) -> {bobPubkey = a;});

        // create fresh addresses for multisig payout
        CompletableFuture<String> alicePayoutFuture = aliceWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> { alicePayoutAddress = a; });
        CompletableFuture<String> bobPayoutFuture = bobWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> { bobPayoutAddress = a; });

        waitForCompletionString(aliceFuture, "setupkeys(pubkeys)");
        waitForCompletionString(bobFuture, "setupkeys(pubkeys)");
        waitForCompletionString(alicePayoutFuture, "setupKeys(payouts)");
        waitForCompletionString(bobPayoutFuture, "setupKeys(payouts)");
    }

    public void getFunding() throws IOException {
        CompletableFuture<List<Utxo>> aliceUnspentFuture = aliceWallet.listUnspent();
        CompletableFuture<List<Utxo>> bobUnspentFuture = bobWallet.listUnspent();
        CompletableFuture<Boolean> gotAmounts = aliceUnspentFuture.thenCombine(bobUnspentFuture,(aliceunspent, bobunspent) -> {
            if (aliceunspent.size() < 1 || bobunspent.size() < 1) {
                System.out.println("alice unspents|bob unspents " + aliceunspent.size() + bobunspent.size() + " not enough UTXOs, abort!");
                return false;
            }
            Utxo aliceUtxo = aliceunspent.get(0);
            Utxo bobUtxo = bobunspent.get(0);
            aliceUtxo.scriptPubKey = "00141843beee10d5aa4cabb0b5d288a533e9383e4ef0";    // TODO: fix
            bobUtxo.scriptPubKey = "001474beb481645a4fd814cfbd2fb45103d5c5e02e2a";      // TODO: fix
            depositTransactionInputs.add(aliceUtxo);
            depositTransactionInputs.add(bobUtxo);
            aliceFundingAmount = aliceUtxo.amount;
            bobFundingAmount = bobUtxo.amount;
            System.out.println(aliceWallet.toString() + " got unspents: " + aliceUtxo.amount);
            System.out.println(bobWallet.toString() + " got unspents: " + bobUtxo.amount);
            return true;
        });
        waitForCompletion(gotAmounts, "getFunding");
    }

    public void createMultisig() throws IOException {
        List<String> keys = new ArrayList<>();
        keys.add(alicePubkey);
        keys.add(bobPubkey);
        CompletableFuture<Boolean> done = aliceWallet.createMultisig(keys).handle((multisig, ex1) -> {
            this.multisigDepositInfo = multisig;
            return true;
        });
        waitForCompletion(done, "createMultisig");
    }

    public void createSignedDepositTx() throws IOException {
        Coin totalAmount = (Coin.parseCoin(aliceFundingAmount).add(Coin.parseCoin(bobFundingAmount)).subtract(Coin.valueOf(1000)));
        List<Utxo> transactionOutputs = new ArrayList<>();
        transactionOutputs.add(new Utxo().initForSpending(multisigDepositInfo.address, totalAmount.toPlainString()));
        CompletableFuture<Boolean> signedTx = aliceWallet.createRawTransaction(depositTransactionInputs, transactionOutputs)
                .thenCompose(aliceWallet::signRawTransactionWithWallet)
                .thenCompose(bobWallet::signRawTransactionWithWallet)
                .handle((signed, ex) -> {
                    signedDepositTx = ex == null ? signed : ex.getCause().toString();
                    System.out.println("signedtx: " + signedDepositTx);
                    return ex == null;
                });
        waitForCompletion(signedTx, "createSignedDepositTx");
    }

    public void createSignedPayoutTx() throws IOException {
        String alicePriv = waitForCompletionString(aliceWallet.getPrivKey(aliceAddress), "alicepriv");
        String bobPriv = waitForCompletionString(bobWallet.getPrivKey(bobAddress), "bobpriv");

        // TODO this is a blockchain query not specific to alice wallet
        CompletableFuture<Boolean> futureB = aliceWallet.listUnspent(depositTxId).handle((utxos,ex) -> {
            payoutTransactionInputs = utxos;
            return true;
        });
        waitForCompletion(futureB, "hello");

        // bob pays the miner fee
        String spendingAmount = (Coin.parseCoin(aliceFundingAmount).add(Coin.parseCoin(bobFundingAmount).subtract(Coin.valueOf(1000)))).toPlainString(); // todo use payoutTransactionInputs.amount???
        System.out.println("Spending amount = " + spendingAmount);
        String payoutMinusFee = (Coin.parseCoin(aliceFundingAmount).subtract(Coin.valueOf(2000))).toPlainString();
        List<Utxo> transactionOutputs = new ArrayList<>();
        transactionOutputs.add(new Utxo().initForSpending(alicePayoutAddress, bobFundingAmount));
        transactionOutputs.add(new Utxo().initForSpending(bobPayoutAddress, payoutMinusFee));
        System.out.println("payouts: " + transactionOutputs.toString());

        CompletableFuture<String> signedTx = aliceWallet.createRawTransaction(payoutTransactionInputs, transactionOutputs)
                .thenCompose((a) -> { return aliceWallet.signRawTransactionWithKey(a, alicePriv, this.depositTxId, 0, payoutTransactionInputs.get(0).scriptPubKey, multisigDepositInfo.witnessScript, spendingAmount);})
                .thenCompose((b) -> { return aliceWallet.signRawTransactionWithKey(b, bobPriv, this.depositTxId, 0,  payoutTransactionInputs.get(0).scriptPubKey, multisigDepositInfo.witnessScript, spendingAmount);})
                .handle((signed, ex) -> {
                    this.signedPayoutTx = signed;
                    System.out.println("signedtx: " + signed);
                    return signed;
                });
        waitForCompletionString(signedTx, "createSignedPayoutTx");
    }

    public String broadcastTx(String txHex) throws IOException {
        CompletableFuture<String> txIdFuture = aliceWallet.broadcastTransaction(txHex);
        txIdFuture.handle((txId, sendException) -> {
            if (sendException != null)
                return sendException.getCause().toString();
            System.out.println("broadcast: " + txId);
            return txId;
        });
        try {
            return txIdFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed (@broadcast)");
        }
    }

    private void waitForCompletion(CompletableFuture<Boolean> task, String name) throws IOException {
        try {
            if (!task.get()) {
                throw new IOException("multisig task failed: " + name);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed: " + name);
        }
    }

    private String waitForCompletionString(CompletableFuture<String> task, String name) throws IOException {
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed: " + name);
        }
    }
}
