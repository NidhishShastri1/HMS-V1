package com.hms.backend.util;

import com.hms.backend.model.Bill;
import com.hms.backend.model.Payment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class RecordHasher {

    public String computeBillHash(Bill bill, String previousHash) {
        StringBuilder data = new StringBuilder();
        data.append(bill.getBillId()).append("|")
                .append(bill.getSubTotal().toString()).append("|")
                .append(bill.getGrandTotal().toString()).append("|")
                .append(bill.getStatus().name()).append("|")
                .append(bill.getCreatedBy().getUsername()).append("|")
                .append(previousHash != null ? previousHash : "ROOT");

        return sha256(data.toString());
    }

    public String computePaymentHash(Payment payment, String previousHash) {
        StringBuilder data = new StringBuilder();
        data.append(payment.getReceiptId()).append("|")
                .append(payment.getAmount().toString()).append("|")
                .append(payment.getPaymentMode().name()).append("|")
                .append(payment.getReceivedBy().getUsername()).append("|")
                .append(previousHash != null ? previousHash : "ROOT");

        return sha256(data.toString());
    }

    private String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withLowerCase().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found", e);
        }
    }
}
