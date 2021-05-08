package com.misq.core;

import com.misq.core.rpc.RpcService;
import com.misq.utils.UserThread;

import com.google.common.io.BaseEncoding;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WalletImplBitcoinCore implements Wallet{

    protected final Set<Listener> listeners = new HashSet<>();
    protected final RpcService rpcService;
    protected Long chainHeight;
    protected String myBalance;

    public WalletImplBitcoinCore(String walletName) {
        this.rpcService = new RpcService("bisqdao", "bsq", "127.0.0.1", 18443, walletName);
        zmqThread("tcp://127.0.0.1:28332").start();
        getBalance();
    }

    @Override
    public CompletableFuture<Long> getChainHeight() {
        CompletableFuture<Long> future = rpcService.requestChainHeadHeight();
        future.thenAccept(height -> {
            if (height != this.chainHeight) {
                this.chainHeight = height;
                listeners.forEach(e -> e.onNewChainHeight(height));
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<String> getFreshReceivingAddress() {
        return rpcService.getFreshReceivingAddress("receiving");
    }

    @Override
    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        return rpcService.sendToAddress(address, amount, memo);
    }

    @Override
    public CompletableFuture<String> getBalance() {
        CompletableFuture<String> future = rpcService.getBalance();
        future.thenAccept(balance -> {
            if (!balance.equals(myBalance)) {
                myBalance = balance;
                listeners.forEach(e -> e.onBalanceChanged(balance));
            }
        });
        return future;
    }

    @Override
    public Wallet addListener(Listener listener) {
        listeners.add(listener);
        return this;
    }

    private Thread zmqThread(String endpoint) {
        return new Thread() {
            private final BaseEncoding HEX = BaseEncoding.base16().lowerCase();
            private ZContext context;
            private ZMQ.Socket socket;
            @Override
            public void run() {
                context = new ZContext();
                socket = context.createSocket(SocketType.SUB);
                socket.connect(endpoint);
                socket.subscribe("");
                while(true) {
                    String work = "";
                    byte[] reply = socket.recv(0);
                    while (reply != null) {
                        work += HEX.encode(reply);
                        reply = socket.recv(ZMQ.NOBLOCK);
                    }
                    final String hexString = work;
                    UserThread.execute(() -> {
                        getChainHeight();
                        getBalance();
                        System.out.println("Received ZMQ update"); //: [" + hexString + "]");
                    });
                }
            }
        };
    }
}
