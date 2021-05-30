package com.misq.examples;

import com.misq.core.*;
import com.misq.utils.Coin;

import java.io.IOException;
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
        Wallet bitcoinWallet = new com.misq.core.bitcoind.WalletImpl("test123").addListener(this);
        Wallet lndWallet = new com.misq.core.lnd.WalletImpl("test123", "127.0.0.1", 10009).addListener(this);
        //Wallet electrumWallet = new com.misq.core.electrumd.WalletImpl("test123").addListener(this);
        //Wallet litecoinWallet = new com.misq.core.litecoind.WalletImpl("test123").addListener(this);
        //Wallet elementsWallet = new com.misq.core.elementsd.WalletImpl("test123").addListener(this);
        //Wallet moneroWallet = new com.misq.core.monerod.WalletImpl("test124", "test124").addListener(this);
        doWalletThings(bitcoinWallet);
        doWalletThings(lndWallet);
        //doWalletThings(electrumWallet);
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
            Coin balanceAsCoin = Coin.parseCoin(balance);
            String balanceToSend = balanceAsCoin.subtract(Coin.valueOf(5000)).toPlainString();
            System.out.println(wallet.toString() + " read balance: " + balance + " " + wallet.getTokenName());
            wallet.getFreshReceivingAddress()
                    .whenComplete((msg, ex) -> {
                        System.out.println(wallet.toString() + " receving address is: " + msg);
                    })
                    .thenCompose(address -> wallet.sendToAddress((address), balanceToSend, "test"))
                    .whenComplete((msg, ex) -> {
                        System.out.println(wallet.toString() + " sending " + balanceToSend + " " + wallet.getTokenName() + "...");
                        if (ex != null) {
                            System.out.println("EXCEPTION: " + ex.toString());
                        } else {
                            System.out.println(wallet.toString() + " sent funds to my receiving address, txId: " + msg);
                        }
                    });
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
        multisigTest.spendingFromTwoWalletsTest((com.misq.core.electrumd.WalletImpl)alice, (com.misq.core.electrumd.WalletImpl)bob);
        try {
            /*reportBalances(alice, bob);
            multisigTest.setupMultisigKeys();
            multisigTest.getFunding();
            multisigTest.createMultisig();
            multisigTest.createSignedDepositTx();
            multisigTest.depositTxId = multisigTest.broadcastTx(multisigTest.signedDepositTx);
            sleep(10);
            multisigTest.createSignedPayoutTx();
            multisigTest.payoutTxId = multisigTest.broadcastTx(multisigTest.signedPayoutTx);
            sleep(10); */
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
