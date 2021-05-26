package com.misq.examples;

import com.misq.core.*;
import com.misq.core.InterfaceDto.Utxo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Kit implements Wallet.Listener{

    // wallet notifications
    @Override
    public void onNewChainHeight(Wallet wallet, Long height) {
        System.out.println(wallet.toString() + " ===> chain height notification from wallet: " + height.toString());
    }
    @Override
    public void onBalanceChanged(Wallet wallet, String balance) {
        System.out.println(wallet.toString() + " ===> balance notification from wallet: " + balance + " " + wallet.getTokenName());
    }

    Kit() {
        // open wallets and do some stuff
        Wallet alice = new com.misq.core.electrumd.WalletImpl("alice2", "192.168.1.111", 17777).addListener(this);
        Wallet bob = new com.misq.core.electrumd.WalletImpl("bob2", "localhost", 17778).addListener(this);

        // experiment 3 : get funds from alice & bob, spend them into a new address
        // the limitation with electrum is that createRawTransaction only works if you supply the private keys for the inputs
        // see core/electrumd/RpcServiceImpl::listUnspent() it obtains the private keys there
        // I've been unable to create an unsigned transaction with v3.3 of electrum.
        alice.listUnspent().whenComplete((aliceUnspent, ex1) -> {
            bob.listUnspent().whenComplete((bobUnspent, ex2) -> {
                alice.getFreshReceivingAddress().whenComplete((addr, ex0) -> {
                    List<Utxo> inputs = new ArrayList<>();
                    inputs.add(aliceUnspent.get(0));
                    inputs.add(bobUnspent.get(0));
                    List<Utxo> outputs = new ArrayList<>();
                    outputs.add(new Utxo().initForSpending(addr, "0.4999"));
                    alice.createRawTransaction(inputs, outputs).whenComplete((txHex, b) -> {
                        System.out.println(txHex);
                    });
                });
            });
        });



        //setupMultisig(alice, bob);
        sleep(20);

        //Wallet bitcoinWallet = new com.misq.core.bitcoind.WalletImpl("test123").addListener(this);
        //Wallet electrumWallet = new com.misq.core.electrumd.WalletImpl("test123").addListener(this);
        //Wallet litecoinWallet = new com.misq.core.litecoind.WalletImpl("test123").addListener(this);
        //Wallet elementsWallet = new com.misq.core.elementsd.WalletImpl("test123").addListener(this);
        //Wallet moneroWallet = new com.misq.core.monerod.WalletImpl("test124", "test124").addListener(this);
        //doWalletThings(electrumWallet);
        //doWalletThings(bitcoinWallet);
        //doWalletThings(litecoinWallet);
        //doWalletThings(elementsWallet);
        //doWalletThings(moneroWallet);

        // WIP .. attempt to get Tx list
/*        ((com.misq.core.monerod.WalletImpl)moneroWallet).getIncomingTransfers()
                .whenComplete((results, ex) -> {
                    if (results != null) {
                        for (String info : results) {
                            System.out.println("Monero TxId: " + info);
                        }
                    }
                }); */
    }

    void doWalletThings(Wallet wallet) {
        System.out.println(wallet.toString() + " doingWalletThings");
        wallet.getChainHeight().thenAccept(height -> {
            System.out.println(wallet.toString() + " read chain height: " + height);
        });

        wallet.getBalance().thenAccept(balance -> {
            System.out.println(wallet.toString() + " read balance: " + balance + " " + wallet.getTokenName());
        });

        wallet.getFreshReceivingAddress()
                .whenComplete((msg, ex) -> {
                    System.out.println(wallet.toString() + " receving address is: " + msg);
                })
                .thenCompose(address -> wallet.sendToAddress((address), "0.5", "test"))
                .whenComplete((msg, ex) -> {
                    System.out.println(wallet.toString() + " sending 0.5 " + wallet.getTokenName() + "...");
                    if (ex != null) {
                        System.out.println("EXCEPTION: " + ex.toString());
                    } else {
                        System.out.println(wallet.toString() + " sent funds to my receiving address, txId: " + msg);
                    }
                });
        sleep(5);
    }

    void reportBalances(Wallet alice, Wallet bob) throws IOException {
        CompletableFuture<String> a = alice.getBalance();
        a.whenComplete((balance, ex) -> {
            System.out.println(alice.toString() + " read balance: " + balance + " " + alice.getTokenName());
        });
        CompletableFuture<String> b = bob.getBalance();
        b.whenComplete((balance, ex) -> {
            System.out.println(bob.toString() + " read balance: " + balance + " " + bob.getTokenName());
        });
        waitForCompletionString(a, "alice");
        waitForCompletionString(b, "bob");
    }

    void setupMultisig(Wallet alice, Wallet bob) {
        MultisigTest multisigTest = new MultisigTest(alice, bob);
        try {
            reportBalances(alice, bob);
            multisigTest.setupMultisigKeys();
            multisigTest.getFunding();
            multisigTest.createMultisig();
            multisigTest.createSignedDepositTx();
            multisigTest.depositTxId = multisigTest.broadcastTx(multisigTest.signedDepositTx);
            sleep(10);
            multisigTest.createSignedPayoutTx();
            multisigTest.payoutTxId = multisigTest.broadcastTx(multisigTest.signedPayoutTx);
            sleep(10);
            reportBalances(alice, bob);
        } catch (IOException ex) {
            System.out.println("EXCEPTION: " + ex.toString());
        }
    }

    private static void sleep(int seconds) {
        System.out.println("sleeping..");
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println("waking up");
    }

    private void waitForCompletionString(CompletableFuture<String> task, String name) throws IOException {
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("task failed: " + name);
        }
    }

    public static void main(String[] args) {
        System.out.println("starting..");
        Kit kit = new Kit();
        while (true) {
            sleep(60);
        }
    }
}
