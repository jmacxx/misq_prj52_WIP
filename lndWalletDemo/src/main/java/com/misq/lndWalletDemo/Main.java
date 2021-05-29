package com.misq.lndWalletDemo;

import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.codec.binary.Hex;
import lnrpc.LightningGrpc;
import lnrpc.LightningGrpc.LightningBlockingStub;
import lnrpc.Lnrpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

public class Main {
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

  private static final String CERT_PATH = "/.lnd/tls.cert";
  private static final String MACAROON_PATH = "/.lnd/data/chain/bitcoin/regtest/admin.macaroon";
  private static final String HOST = "127.0.0.1";
  private static final int PORT = 10009;

  public static void main(String...args) throws IOException {
    SslContext sslContext = GrpcSslContexts.forClient().trustManager(new File(CERT_PATH)).build();
    NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(HOST, PORT);
    ManagedChannel channel = channelBuilder.sslContext(sslContext).build();
    String macaroon = Hex.encodeHexString(Files.readAllBytes(Paths.get(MACAROON_PATH)));
    LightningBlockingStub stub = LightningGrpc
        .newBlockingStub(channel)
        .withCallCredentials(new MacaroonCallCredential(macaroon));

    String receivingAddress = "";
    {
      Lnrpc.GetInfoRequest request = Lnrpc.GetInfoRequest.getDefaultInstance();
      Lnrpc.GetInfoResponse response = stub.getInfo(request);
      System.out.println("Block height=" + response.getBlockHeight());
    }
    {
      Lnrpc.WalletBalanceRequest request = Lnrpc.WalletBalanceRequest.getDefaultInstance();
      Lnrpc.WalletBalanceResponse response = stub.walletBalance(request);
      System.out.println("Wallet balance=" + response.getTotalBalance());
    }
    {
      Lnrpc.NewAddressRequest request = Lnrpc.NewAddressRequest.getDefaultInstance();
      Lnrpc.NewAddressResponse response = stub.newAddress(request);
      receivingAddress = response.getAddress();
      System.out.println("Wallet receiving address=" + response.getAddress());
    }
    {
      Lnrpc.SendCoinsRequest request = Lnrpc.SendCoinsRequest.newBuilder()
              .setAddr(receivingAddress)
              .setSendAll(true)
              .setLabel("demo")
              .build();
      Lnrpc.SendCoinsResponse response = stub.sendCoins(request);
      System.out.println("Sent coins, txID=" + response.getTxid());
    }
  }
}
