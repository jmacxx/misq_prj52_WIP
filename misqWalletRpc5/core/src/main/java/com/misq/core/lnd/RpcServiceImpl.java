package com.misq.core.lnd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.misq.utils.Coin;
import io.grpc.ManagedChannel;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.MethodDescriptor;
import io.grpc.Attributes;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.codec.binary.Hex;
import lnrpc.LightningGrpc;
import lnrpc.LightningGrpc.LightningBlockingStub;
import lnrpc.Lnrpc;

public class RpcServiceImpl {
    static class MacaroonCallCredential implements CallCredentials {
        private final String macaroon;
        MacaroonCallCredential(String macaroon) {
            this.macaroon = macaroon;
        }
        public void thisUsesUnstableApi() {}
        public void applyRequestMetadata(
                MethodDescriptor < ? , ? > methodDescriptor,
                Attributes attributes,
                Executor executor,
                final MetadataApplier metadataApplier
        ) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        Metadata headers = new Metadata();
                        Metadata.Key < String > macaroonKey = Metadata.Key.of("macaroon", Metadata.ASCII_STRING_MARSHALLER);
                        headers.put(macaroonKey, macaroon);
                        metadataApplier.apply(headers);
                    } catch (Throwable e) {
                        metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                    }
                }
            });
        }
    }

    private LightningBlockingStub blockingStub;

    public RpcServiceImpl(String rpcHost, int rpcPort) throws IOException {
        final String CERT_PATH = "/home/yourusernamehere/.lnd/tls.cert";
        final String MACAROON_PATH = "/home/yourusernamehere/.lnd/data/chain/bitcoin/regtest/admin.macaroon";

        SslContext sslContext = GrpcSslContexts.forClient().trustManager(new File(CERT_PATH)).build();
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(rpcHost, rpcPort);
        ManagedChannel channel = channelBuilder.sslContext(sslContext).build();
        String macaroon = Hex.encodeHexString(Files.readAllBytes(Paths.get(MACAROON_PATH)));
        blockingStub = LightningGrpc
                .newBlockingStub(channel)
                .withCallCredentials(new MacaroonCallCredential(macaroon));

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public CompletableFuture<Long> getBlockCount() {
        return CompletableFuture.supplyAsync(() -> {
            Lnrpc.GetInfoRequest request = Lnrpc.GetInfoRequest.getDefaultInstance();
            Lnrpc.GetInfoResponse response = blockingStub.getInfo(request);
            return Long.valueOf((long)response.getBlockHeight());
        });
    }

    public CompletableFuture<String> getBalance() {
        return CompletableFuture.supplyAsync(() -> {
            Lnrpc.WalletBalanceRequest request = Lnrpc.WalletBalanceRequest.getDefaultInstance();
            Lnrpc.WalletBalanceResponse response = blockingStub.walletBalance(request);
            Coin balance = Coin.valueOf(response.getTotalBalance());
            return balance.toPlainString();
        });
    }

    public CompletableFuture<String> getNewAddress() {
        return CompletableFuture.supplyAsync(() -> {
            Lnrpc.NewAddressRequest request = Lnrpc.NewAddressRequest.getDefaultInstance();
            Lnrpc.NewAddressResponse response = blockingStub.newAddress(request);
            String receivingAddress = response.getAddress();
            return receivingAddress;
        });
    }

    public CompletableFuture<String> sendToAddress(String address, String amount, String memo) {
        return CompletableFuture.supplyAsync(() -> {
            Lnrpc.SendCoinsRequest request = Lnrpc.SendCoinsRequest.newBuilder()
                    .setAddr(address)
                    .setSendAll(true)
                    .setLabel(memo)
                    .build();
            Lnrpc.SendCoinsResponse response = blockingStub.sendCoins(request);
            String txId = response.getTxid();
            return txId;
        });
    }


}
