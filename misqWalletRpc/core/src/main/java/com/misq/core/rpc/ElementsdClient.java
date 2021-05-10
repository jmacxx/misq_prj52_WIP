package com.misq.core.rpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.googlecode.jsonrpc4j.RequestIDGenerator;

public interface ElementsdClient {
    @JsonRpcMethod("loadwallet")
    String loadWallet(String walletName);

    @JsonRpcMethod("getblockcount")
    Long getBlockCount();

    @JsonRpcMethod("getbalance")
    Map<String, String> getBalance();

    @JsonRpcMethod("getnewaddress")
    String getNewAddress(String label);

    @JsonRpcMethod("sendtoaddress")
    String sendToAddress(String address, String amount, String memo);

    @JsonRpcMethod("listreceivedbyaddress")
    List<RawDtoAddressBalanceElementsd> listReceivedByAddress(int minConf, boolean includeEmpty);

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String rpcHost;
        private int rpcPort = -1;
        private String rpcUser;
        private String rpcPassword;
        private URLStreamHandler urlStreamHandler;
        private RequestIDGenerator requestIDGenerator;
        private String walletName;

        public Builder rpcHost(String rpcHost) {
            this.rpcHost = rpcHost;
            return this;
        }

        public Builder rpcPort(int rpcPort) {
            this.rpcPort = rpcPort;
            return this;
        }

        public Builder rpcUser(String rpcUser) {
            this.rpcUser = rpcUser;
            return this;
        }

        public Builder rpcPassword(String rpcPassword) {
            this.rpcPassword = rpcPassword;
            return this;
        }

        public Builder walletName(String walletName) {
            this.walletName = walletName;
            return this;
        }

        public ElementsdClient build() throws MalformedURLException {
            var userPass = checkNotNull(rpcUser, "rpcUser not set") +
                    ":" + checkNotNull(rpcPassword, "rpcPassword not set");

            var headers = Collections.singletonMap("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.US_ASCII)));

            var httpClient = new JsonRpcHttpClient(
                    new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true),
                    new URL("http", rpcHost, rpcPort, "/wallet/" + walletName, urlStreamHandler),
                    headers);
            Optional.ofNullable(requestIDGenerator).ifPresent(httpClient::setRequestIDGenerator);
            return ProxyUtil.createClientProxy(getClass().getClassLoader(), ElementsdClient.class, httpClient);
        }
    }
}
