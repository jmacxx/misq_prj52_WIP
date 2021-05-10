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
        System.out.println(wallet.toString() + " ===> balance notification from wallet: " + balance);
    }

    public Wallet bitcoinWallet;
    public Wallet elementsWallet;

    Kit() {
        // open wallet and do some stuff
        bitcoinWallet = new WalletImplBitcoind("test123").addListener(this);
        elementsWallet = new WalletImplElementsd("test123").addListener(this);
    }

    void doWalletThings(Wallet wallet) {
        System.out.println(wallet.toString() + " doingWalletThings");

        wallet.getChainHeight().thenAccept(height -> {
            System.out.println(wallet.toString() + " read chain height: " + height);
        });

        wallet.getBalance().thenAccept(balance -> {
            System.out.println(wallet.toString() + " read balance: " + balance);
        });

        // example of chaining: get receiving address then send funds to the address
        wallet.getFreshReceivingAddress()
                .whenComplete((msg, ex) -> {
                    System.out.println(wallet.toString() + " receving address is: " + msg);
                })
                .thenCompose(address -> wallet.sendToAddress((address), "0.5", "test"))
                    .whenComplete((msg, ex) -> {
                        System.out.println(wallet.toString() + " sending 0.5 BTC...");
                        if (ex != null) {
                            System.out.println("EXCEPTION: " + ex.toString());
                        } else {
                            System.out.println(wallet.toString() + " sent funds to my receiving address, txId: " + msg);
                        }
                    });
    }

    public static void main(String[] args) {
        System.out.println("starting..");
        Kit kit = new Kit();
        sleep(1);
        kit.doWalletThings(kit.bitcoinWallet);
        sleep(5);
        kit.doWalletThings(kit.elementsWallet);
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
