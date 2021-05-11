package com.misq.examples;

import com.misq.core.*;
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
        Wallet litecoinWallet = new com.misq.core.litecoind.WalletImpl("test123").addListener(this);
        Wallet elementsWallet = new com.misq.core.elementsd.WalletImpl("test123").addListener(this);
        Wallet moneroWallet = new com.misq.core.monerod.WalletImpl("test124", "test124").addListener(this);
        doWalletThings(bitcoinWallet);
        doWalletThings(litecoinWallet);
        doWalletThings(elementsWallet);
        doWalletThings(moneroWallet);

        sleep(5);
        // WIP .. attempt to get Tx list
        ((com.misq.core.monerod.WalletImpl)moneroWallet).getIncomingTransfers()
                .whenComplete((results, ex) -> {
                    if (results != null) {
                        for (String info : results) {
                            System.out.println("Monero TxId: " + info);
                        }
                    }
                });
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

    public static void main(String[] args) {
        System.out.println("starting..");
        Kit kit = new Kit();
        while (true) {
            sleep(60);
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

}
