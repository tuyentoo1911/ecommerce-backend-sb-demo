package com.example.dto.vnpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body that VNPay expects from IPN endpoint.
 * RspCode "00" = confirmed successfully. Any other code = error.
 */
@Data
@AllArgsConstructor
public class VNPayIpnResponse {
    @JsonProperty("RspCode")
    String rspCode;
    @JsonProperty("Message")
    String message;

    public static VNPayIpnResponse ok() {
        return new VNPayIpnResponse("00", "Confirm Success");
    }

    public static VNPayIpnResponse invalidSignature() {
        return new VNPayIpnResponse("97", "Invalid Signature");
    }

    public static VNPayIpnResponse orderNotFound() {
        return new VNPayIpnResponse("01", "Order Not Found");
    }

    public static VNPayIpnResponse alreadyConfirmed() {
        return new VNPayIpnResponse("02", "Order Already Confirmed");
    }

    public static VNPayIpnResponse invalidAmount() {
        return new VNPayIpnResponse("04", "Invalid Amount");
    }

    public static VNPayIpnResponse unknownError() {
        return new VNPayIpnResponse("99", "Unknown Error");
    }
}
