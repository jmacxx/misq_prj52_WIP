package com.misq.examples;

import com.misq.core.*;
import java.util.concurrent.*;

public class Kit implements Wallet.Listener{

    // wallet notifications
    @Override
    public void onNewChainHeight(Long height) {
        System.out.println("===> chain height notification from wallet: " + height.toString());
    }
    @Override
    public void onBalanceChanged(String balance) {
        System.out.println("===> balance notification from wallet: " + balance);
    }

    public Wallet wallet;

    Kit() {
        // open wallet and do some stuff
        wallet = new WalletImplBitcoinCore("test123").addListener(this);
        wallet.getChainHeight().thenAccept(height -> {
            System.out.println("read chain height from wallet: " + height);
        });
        wallet.getBalance().thenAccept(balance -> {
            System.out.println("read balance from wallet: " + balance);
        });
        wallet.getFreshReceivingAddress().thenAccept(newAddress -> {
            System.out.println("read receiving address from wallet: " + newAddress);
            sleep(5);
            wallet.sendToAddress(newAddress, "0.1", "test").thenAccept(txId -> {
                System.out.println("sent funds to my receiving address, txId: " + txId);
            });
        });
    }

    public static void main(String[] args) {
        System.out.println("starting..");
        new Kit();
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
