package com.misq.core.electrumd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/*
    RPC methods exposed by electrum: (v3.3.4.1)

    addrequest addtransaction broadcast clearrequests commands convert_xkey create createmultisig createnewaddress decrypt
    deserialize dumpprivkeys encrypt freeze get getaddressbalance getaddresshistory getaddressunspent getalias getbalance
    getconfig getfeerate getmasterprivate getmerkle getmpk getprivatekeys getpubkeys getrequest getseed getservers
    gettransaction getunusedaddress help history importprivkey is_synchronized ismine listaddresses listcontacts
    listrequests listunspent make_seed notify password payto paytomany restore rmrequest searchcontacts serialize setconfig
    setlabel signmessage signrequest signtransaction sweep unfreeze validateaddress verifymessage version

 */

public interface RpcInterface {
    @JsonRpcMethod("is_synchronized")
    Boolean isSynchronized();

    @JsonRpcMethod("getbalance")
    Map<String, String> getBalance();

    @JsonRpcMethod("getunusedaddress")
    String getNewAddress();

    @JsonRpcMethod("payto")
    String payTo(@JsonRpcParam(value="amount") String amount, @JsonRpcParam(value="destination") String destination);

    static RpcInterface.Builder builder() {
        return new RpcInterface.Builder();
    }

    class Builder {
        private String rpcHost;
        private int rpcPort = -1;
        private String rpcUser;
        private String rpcPassword;
        private URLStreamHandler urlStreamHandler;
        private RequestIDGenerator requestIDGenerator;
        private String walletName;

        public RpcInterface.Builder rpcHost(String rpcHost) {
            this.rpcHost = rpcHost;
            return this;
        }

        public RpcInterface.Builder rpcPort(int rpcPort) {
            this.rpcPort = rpcPort;
            return this;
        }

        public RpcInterface.Builder rpcUser(String rpcUser) {
            this.rpcUser = rpcUser;
            return this;
        }

        public RpcInterface.Builder rpcPassword(String rpcPassword) {
            this.rpcPassword = rpcPassword;
            return this;
        }

        public RpcInterface.Builder walletName(String walletName) {
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
                    new URL("http", rpcHost, rpcPort, "", urlStreamHandler),
                    headers);
            Optional.ofNullable(requestIDGenerator).ifPresent(httpClient::setRequestIDGenerator);
            return ProxyUtil.createClientProxy(getClass().getClassLoader(), RpcInterface.class, httpClient);
        }
    }
}
