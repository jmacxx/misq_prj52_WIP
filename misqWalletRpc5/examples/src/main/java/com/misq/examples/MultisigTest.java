package com.misq.examples;

import com.google.common.io.BaseEncoding;
import com.misq.core.InterfaceDto.MultisigInfo;
import com.misq.core.InterfaceDto.Utxo;
import com.misq.core.Wallet;
import com.misq.utils.Coin;
import com.misq.utils.VarInt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MultisigTest {

    private int cursor;
    private byte[] buffer;
    private Wallet aliceWallet;
    private Wallet bobWallet;
    private String aliceAddress, alicePubkey, bobAddress, bobPubkey;
    private String alicePayoutAddress, bobPayoutAddress;
    private String aliceFundingAmount, bobFundingAmount;
    private MultisigInfo multisigDepositInfo;
    public String signedDepositTx, depositTxId;
    public String signedPayoutTx, payoutTxId;
    private List<Utxo> depositTransactionInputs = new ArrayList<>();
    private List<Utxo> payoutTransactionInputs = new ArrayList<>();

    public MultisigTest(Wallet alice, Wallet bob) {
        this.aliceWallet = alice;
        this.bobWallet = bob;
    }

    public void setupMultisigKeys() throws IOException {
        // create public keys used for controlling ownership of the multisig
        CompletableFuture<String> aliceFuture = aliceWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> {aliceAddress = a;})
                .thenCompose(aliceWallet::getPubKey)
                .whenComplete((a,b) -> {alicePubkey = a;});
        CompletableFuture<String> bobFuture = bobWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> {bobAddress = a;})
                .thenCompose(bobWallet::getPubKey)
                .whenComplete((a,b) -> {bobPubkey = a;});

        // create fresh addresses for multisig payout
        CompletableFuture<String> alicePayoutFuture = aliceWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> { alicePayoutAddress = a; });
        CompletableFuture<String> bobPayoutFuture = bobWallet.getFreshReceivingAddress()
                .whenComplete((a,b) -> { bobPayoutAddress = a; });

        waitForCompletionString(aliceFuture, "setupkeys(pubkeys)");
        waitForCompletionString(bobFuture, "setupkeys(pubkeys)");
        waitForCompletionString(alicePayoutFuture, "setupKeys(payouts)");
        waitForCompletionString(bobPayoutFuture, "setupKeys(payouts)");
    }

    public void getFunding() throws IOException {
        CompletableFuture<List<Utxo>> aliceUnspentFuture = aliceWallet.listUnspent();
        CompletableFuture<List<Utxo>> bobUnspentFuture = bobWallet.listUnspent();
        CompletableFuture<Boolean> gotAmounts = aliceUnspentFuture.thenCombine(bobUnspentFuture,(aliceunspent, bobunspent) -> {
            if (aliceunspent.size() < 1 || bobunspent.size() < 1) {
                System.out.println("alice unspents|bob unspents " + aliceunspent.size() + bobunspent.size() + " not enough UTXOs, abort!");
                return false;
            }
            Utxo aliceUtxo = aliceunspent.get(0);
            Utxo bobUtxo = bobunspent.get(0);
            depositTransactionInputs.add(aliceUtxo);
            depositTransactionInputs.add(bobUtxo);
            aliceFundingAmount = aliceUtxo.amount;
            bobFundingAmount = bobUtxo.amount;
            System.out.println(aliceWallet.toString() + " got unspents: " + aliceUtxo.amount);
            System.out.println(bobWallet.toString() + " got unspents: " + bobUtxo.amount);
            return true;
        });
        waitForCompletion(gotAmounts, "getFunding");
    }

    public void createMultisig() throws IOException {
        List<String> keys = new ArrayList<>();
        keys.add(alicePubkey);
        keys.add(bobPubkey);
        CompletableFuture<Boolean> done = aliceWallet.createMultisig(keys).handle((multisig, ex1) -> {
            this.multisigDepositInfo = multisig;
            return true;
        });
        waitForCompletion(done, "createMultisig");
    }

    public void createSignedDepositTx() throws IOException {
        Coin totalAmount = (Coin.parseCoin(aliceFundingAmount).add(Coin.parseCoin(bobFundingAmount)).subtract(Coin.valueOf(1000)));
        List<Utxo> transactionOutputs = new ArrayList<>();
        transactionOutputs.add(new Utxo().initForSpending(multisigDepositInfo.address, totalAmount.toPlainString()));

        CompletableFuture<String> psbtA = aliceWallet.createRawTransaction(depositTransactionInputs, transactionOutputs);
        CompletableFuture<String> psbtB = bobWallet.createRawTransaction(depositTransactionInputs, transactionOutputs);
        CompletableFuture<Boolean> signedTxF = psbtA.thenCombine(psbtB, (psbtAlice, psbtBob) -> {
                    System.out.println("Alice signed tx: " + psbtAlice);
                    System.out.println("Bob signed tx: " + psbtBob);
                    String txUnsigned = extractTxFromPsbt(psbtAlice);
                    String signatureAlice = extractSignatureFromPsbt(psbtAlice);
                    String signatureBob = extractSignatureFromPsbt(psbtBob);
                    this.signedDepositTx = combineTxAndSignatures(txUnsigned, signatureAlice, signatureBob);
                    System.out.println("Complete tx: " + this.signedDepositTx);
                    return true;
                });
        waitForCompletion(signedTxF, "createSignedDepositTx");
    }

    public void createSignedPayoutTx() throws IOException {
        String alicePriv = waitForCompletionString(aliceWallet.getPrivKey(aliceAddress), "alicepriv");
        String bobPriv = waitForCompletionString(bobWallet.getPrivKey(bobAddress), "bobpriv");

        // TODO this is a blockchain query not specific to alice wallet
        //CompletableFuture<Boolean> futureB = aliceWallet.listUnspent(depositTxId).handle((utxos,ex) -> {
        //    payoutTransactionInputs = utxos;
        //    return true;
        //});
        //waitForCompletion(futureB, "hello");
        Utxo utxo = new Utxo();
        utxo.txId = this.depositTxId;
        utxo.vout = 0;
        // TODO: electrum needs the amount and address included here, bitcoin core only requires txid and vout
        payoutTransactionInputs.add(utxo);

        // bob pays the miner fee
        String spendingAmount = (Coin.parseCoin(aliceFundingAmount).add(Coin.parseCoin(bobFundingAmount).subtract(Coin.valueOf(1000)))).toPlainString(); // todo use payoutTransactionInputs.amount???
        System.out.println("Spending amount = " + spendingAmount);
        String payoutMinusFee = (Coin.parseCoin(aliceFundingAmount).subtract(Coin.valueOf(2000))).toPlainString();
        List<Utxo> transactionOutputs = new ArrayList<>();
        transactionOutputs.add(new Utxo().initForSpending(alicePayoutAddress, bobFundingAmount));
        transactionOutputs.add(new Utxo().initForSpending(bobPayoutAddress, payoutMinusFee));
        System.out.println("payouts: " + transactionOutputs.toString());

        CompletableFuture<String> signedTx = aliceWallet.createRawTransaction(payoutTransactionInputs, transactionOutputs)
                .thenCompose((a) -> { return aliceWallet.signRawTransactionWithKey(a, alicePriv, this.depositTxId, 0, payoutTransactionInputs.get(0).scriptPubKey, multisigDepositInfo.witnessScript, spendingAmount);})
                .thenCompose((b) -> { return aliceWallet.signRawTransactionWithKey(b, bobPriv, this.depositTxId, 0,  payoutTransactionInputs.get(0).scriptPubKey, multisigDepositInfo.witnessScript, spendingAmount);})
                .handle((signed, ex) -> {
                    this.signedPayoutTx = signed;
                    System.out.println("signedtx: " + signed);
                    return signed;
                });
        waitForCompletionString(signedTx, "createSignedPayoutTx");
    }

    public String broadcastTx(String txHex) throws IOException {
        CompletableFuture<String> txIdFuture = aliceWallet.broadcastTransaction(txHex);
        txIdFuture.handle((txId, sendException) -> {
            if (sendException != null)
                return sendException.getCause().toString();
            System.out.println("broadcast: " + txId);
            return txId;
        });
        try {
            return txIdFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed (@broadcast)");
        }
    }

    private void waitForCompletion(CompletableFuture<Boolean> task, String name) throws IOException {
        try {
            if (!task.get()) {
                throw new IOException("multisig task failed: " + name);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed: " + name);
        }
    }

    private String waitForCompletionString(CompletableFuture<String> task, String name) throws IOException {
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("multisig task failed: " + name);
        }
    }

    /*
    70736274ff
    01007c
    0200000002d52227ead4ce8f97b82379
    221bc885ebb6cbbde489953200c0e5c6
    0ee669b7a10000000000feffffff48fa
    d28a9fcbab9eea48cd8cf0603c089ed9
    ddcbfa5810a97a1e66a688f7b5b80100
    000000feffffff012052a60000000000
    17a9146c838e79c5207a9fa43f618beb
    77b9b42d76d8a48700000000
    00
    0107
    00
    0108
    6b
    024730440220430274e09d4f15c91c77
    aec0a04fd8925f38470c09834a0eda3a
    41ddbac1682302203173a1fbb25a0a4d
    6a04fff4faea1985cd3a04cd0ce99b05
    1e2f6089d1248ba401210219219ca469
    d4c90074d265a3ce3c8f3436dea66405
    1be39b8a45005e8ca14ea1
    00
    0000
     */
    String extractTxFromPsbt(String psbt) {
        buffer = b64decode(psbt);
        cursor = 0;
        String psbtAsHex = dumpMemoryBlock(buffer.length);
        System.out.println(psbtAsHex);
        cursor = 0;
        cursor = seekPastPsbtMagic(buffer, cursor);
        String dummy = dumpMemoryBlock(1);
        String txHex = "";
        String sectionData = "";
        while (cursor < buffer.length) {
            long blockStart = readVarInt();
            long sectionCode = readVarInt();
            long sectionLength = readVarInt();
            if (sectionLength > 0) {
                sectionData = dumpMemoryBlock((int) sectionLength);
                long blockEndDummy = readVarInt();
                if (blockEndDummy != 0) {
                    return "FAIL BLOCKENDDUMMY != 0";
                }
            }
            if (blockStart == 1 && sectionCode == 0) {
                txHex = sectionData;
            }
        }

        if (cursor < 0)
            return "FAIL";
        return txHex;
    }

    String extractSignatureFromPsbt(String psbt) {
        buffer = b64decode(psbt);
        cursor = 0;
        String psbtAsHex = dumpMemoryBlock(buffer.length);
        System.out.println(psbtAsHex);
        cursor = 0;
        cursor = seekPastPsbtMagic(buffer, cursor);
        String dummy = dumpMemoryBlock(1);
        String signature = "";
        String sectionData = "";
        while (cursor < buffer.length) {
            long blockStart = readVarInt();
            if (blockStart == 0)
                blockStart = readVarInt();
            long sectionCode = readVarInt();
            long sectionLength = readVarInt();
            if (sectionLength > 0) {
                sectionData = dumpMemoryBlock((int) sectionLength);
                long blockEndDummy = readVarInt();
                if (blockEndDummy != 0) {
                    return "FAIL BLOCKENDDUMMY != 0";
                }
            }
            if (blockStart == 1 && sectionCode == 8) {  // 8 == SCRIPT WITNESS
                signature = sectionData;
            }
        }

        if (cursor < 0)
            return "FAIL";
        return signature;
    }

    String combineTxAndSignatures(String tx, String sigA, String sigB) {
        String signedTx = tx.substring(0, 8);                   // version
        signedTx = signedTx + "0001";                           // flag
        signedTx = signedTx + tx.substring(8, tx.length()-8);   // tx up to locktime
        signedTx = signedTx + sigA + sigB;                      // insert the signatures in witness section
        signedTx = signedTx + tx.substring(tx.length()-8);      // append the locktime
        return  signedTx;
    }


    public static int seekPastPsbtMagic(byte[] payload, int offset) {
        int cursor = offset;
        long packetMagic = 0x70736274;
        int magicCursor = 3;  // Which byte of the magic we're looking for currently.
        while (true) {
            if (cursor >= payload.length) {  // magic not found
                return -1;
            }
            byte b = payload[cursor];
            cursor++;
            // We're looking for a run of bytes that is the same as the packet magic but we want to ignore partial
            // magics that aren't complete. So we keep track of where we're up to with magicCursor.
            byte expectedByte = (byte)(0xFF & packetMagic >>> (magicCursor * 8));
            if (b == expectedByte) {
                magicCursor--;
                if (magicCursor < 0) {
                    // We found the magic sequence.
                    return cursor;
                } else {
                    // We still have further to go to find the next message.
                }
            } else {
                magicCursor = 3;
            }
        }
    }

    String dumpMemoryBlock(int size) {
        if (size == 0)
            return "";
        final BaseEncoding HEX = BaseEncoding.base16().lowerCase();
        byte[] dumpBuf = new byte[size];
        System.arraycopy(buffer, cursor, dumpBuf, 0, size);
        cursor = cursor + size;
        String z = HEX.encode(dumpBuf);
        return z;
    }

    protected long readVarInt() {
        try {
            VarInt varint = new VarInt(buffer, cursor);
            cursor += varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static byte[] b64decode(String base64) {
        return java.util.Base64.getDecoder().decode(base64);
    }

    public static String b64encode(byte[] bytes) {
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }


    public void spendingFromTwoWalletsTest(com.misq.core.electrumd.WalletImpl alice, com.misq.core.electrumd.WalletImpl bob) {
        // experiment 4 : get funds from alice & bob, spend them into a new address
        alice.listUnspent().whenComplete((aliceUnspent, ex1) -> {
            bob.listUnspent().whenComplete((bobUnspent, ex2) -> {
                String addr = "2N38zZiAx3uVob9KULWCpH2uPtRAqTasyXn"; // TODO: hardcoded output address
                List<Utxo> inputs = new ArrayList<>();
                inputs.add(aliceUnspent.get(0));
                inputs.add(bobUnspent.get(0));
                List<Utxo> outputs = new ArrayList<>();
                outputs.add(new Utxo().initForSpending(addr, "0.03999"));       // TODO: hardcoded output value
                alice.createRawTransaction(inputs, outputs).whenComplete((psbtAlice, b) -> {
                    System.out.println("Alice signed tx: " + psbtAlice);
                    bob.createRawTransaction(inputs, outputs).whenComplete((psbtBob, c) -> {
                        System.out.println("Bob signed tx: " + psbtBob);
                        String txUnsigned = extractTxFromPsbt(psbtAlice);
                        String signatureAlice = extractSignatureFromPsbt(psbtAlice);
                        String signatureBob = extractSignatureFromPsbt(psbtBob);
                        String signedTx = combineTxAndSignatures(txUnsigned, signatureAlice, signatureBob); // TODO: signatureA and signatureB need to be ordered correctly in the tx witness
                        System.out.println("Complete tx: " + signedTx);
                        bob.broadcastTransaction(signedTx).whenComplete((txId, exBroadcast) -> {
                            System.out.println("Broadcasted txid: " + txId);
                        });
                    });
                });
            });
        });
    }



}
