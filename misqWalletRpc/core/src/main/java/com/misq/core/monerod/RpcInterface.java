package com.misq.core.monerod;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

import com.googlecode.jsonrpc4j.*;

public interface RpcInterface {
    @JsonRpcMethod("open_wallet")
    void openWallet(@JsonRpcParam(value="filename") String filename, @JsonRpcParam(value="password") String password);

    @JsonRpcMethod("close_wallet")
    void closeWallet();

    @JsonRpcMethod("get_height")
    Map<String, Long> getBlockCount();

    @JsonRpcMethod("get_balance")
    RawDtoBalance getBalance();

    @JsonRpcMethod("create_address")
    RawDtoAddress createAddress(@JsonRpcParam(value="account_index") Long accountIndex, @JsonRpcParam(value="label") String label);

    @JsonRpcMethod("transfer")
    RawDtoTransferResult transfer(@JsonRpcParam(value="destinations") List<RawDtoTransfer> destinations);

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

        public RpcInterface build() throws MalformedURLException {
            var userPass = checkNotNull(rpcUser, "rpcUser not set") +
                    ":" + checkNotNull(rpcPassword, "rpcPassword not set");

            var headers = Collections.singletonMap("Authorization", "Basic " +
                    Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.US_ASCII)));

            var httpClient = new JsonRpcHttpClient(
                    new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true),
                    new URL("http", rpcHost, rpcPort, "/json_rpc", urlStreamHandler),
                    headers);
            Optional.ofNullable(requestIDGenerator).ifPresent(httpClient::setRequestIDGenerator);
            return ProxyUtil.createClientProxy(getClass().getClassLoader(), RpcInterface.class, httpClient);
        }
    }
}
