package com.misq.core.litecoind;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

import com.googlecode.jsonrpc4j.*;
import com.misq.core.PrivateDto.*;

public interface RpcInterface {
    @JsonRpcMethod("getblockcount")
    Long getBlockCount();

    @JsonRpcMethod("getbalance")
    String getBalance();

    @JsonRpcMethod("getnewaddress")
    String getNewAddress(String label);

    @JsonRpcMethod("getaddressinfo")
    AddressInfo getAddressInfo(String address);

    @JsonRpcMethod("sendtoaddress")
    String sendToAddress(String address, String amount, String memo);

    @JsonRpcMethod("createmultisig")
    Map<String, String> createMultiSig(@JsonRpcParam(value="nrequired") Integer nrequired, @JsonRpcParam(value="keys") List<String> keys, @JsonRpcParam(value="address_type") String addressType);

    @JsonRpcMethod("decodescript")
    Map<String, Object> decodeScript(@JsonRpcParam(value="hexstring") String hexString);

    @JsonRpcMethod("createrawtransaction")
    String createRawTransaction(@JsonRpcParam(value="inputs") List<TransactionInput> inputs, @JsonRpcParam(value="outputs") Map<String, String> outputs);

    @JsonRpcMethod("signrawtransactionwithwallet")
    Map<String, Object> signRawTransactionWithWallet(@JsonRpcParam(value="hexstring") String txHex);

    @JsonRpcMethod("signrawtransactionwithkey")
    Map<String, Object> signRawTransactionWithKey(@JsonRpcParam(value="hexstring") String txHex, @JsonRpcParam(value="privkeys") List<String> privKeys, @JsonRpcParam(value="prevtxs") List<PrevTxInfo> prevTxs);

    @JsonRpcMethod("sendrawtransaction")
    String sendRawTransaction(String hex);

    @JsonRpcMethod("dumpprivkey")
    String getPrivKey(String address);

    @JsonRpcMethod("listunspent")
    List<TransactionOutput> listUnspent();

    @JsonRpcMethod("listwallets")
    List<String> listWallets();

    @JsonRpcMethod("loadwallet")
    Object loadWallet(String walletName);

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
                    new URL("http", rpcHost, rpcPort, "/wallet/" + walletName, urlStreamHandler),
                    headers);
            Optional.ofNullable(requestIDGenerator).ifPresent(httpClient::setRequestIDGenerator);
            return ProxyUtil.createClientProxy(getClass().getClassLoader(), RpcInterface.class, httpClient);
        }
    }
}
