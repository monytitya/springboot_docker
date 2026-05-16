package Online_Meansreang.Meangsreang.service;

import Online_Meansreang.Meangsreang.model.PaymentRequest;
import Online_Meansreang.Meangsreang.model.PaymentResponse;
import Online_Meansreang.Meangsreang.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KhqrService {

    @Value("${bakong.api-url}")
    private String apiUrl;

    @Value("${bakong.merchant-id}")
    private String merchantId;

    @Value("${bakong.merchant-name}")
    private String merchantName;

    @Value("${bakong.merchant-city}")
    private String merchantCity;

    public String generateKhqr(String transactionId, double amount, String currency) {
        // Trim inputs to prevent hidden spaces from breaking the QR
        String mId = merchantId != null ? merchantId.trim() : "";
        String mName = merchantName != null ? merchantName.trim() : "Merchant";
        String mCity = merchantCity != null ? merchantCity.trim() : "Phnom Penh";
        // Manual KHQR (EMVCo) Generation
        StringBuilder khqr = new StringBuilder();
        
        // 00: Payload Format Indicator
        khqr.append(formatTag("00", "01"));
        
        // 01: Point of Initiation Method (12 = Dynamic)
        khqr.append(formatTag("01", "12"));
        
        // 29: Merchant Account Information (Bakong Individual)
        StringBuilder merchantAccount = new StringBuilder();
        merchantAccount.append(formatTag("00", "kh.com.bakong")); // Bakong GUID
        merchantAccount.append(formatTag("01", mId));
        khqr.append(formatTag("29", merchantAccount.toString()));
        
        // 52: Merchant Category Code
        khqr.append(formatTag("52", "5999"));
        
        // 53: Transaction Currency (840 = USD, 116 = KHR)
        String currencyCode = "USD".equalsIgnoreCase(currency) ? "840" : "116";
        khqr.append(formatTag("53", currencyCode));
        
        // 54: Transaction Amount
        String amountStr = "KHR".equalsIgnoreCase(currency) 
            ? String.format("%.0f", amount) 
            : String.format("%.2f", amount);
        khqr.append(formatTag("54", amountStr));
        
        // 58: Country Code
        khqr.append(formatTag("58", "KH"));
        
        // 59: Merchant Name
        khqr.append(formatTag("59", mName));
        
        // 60: Merchant City
        khqr.append(formatTag("60", mCity));
        
        // 62: Additional Data Field
        StringBuilder additionalData = new StringBuilder();
        additionalData.append(formatTag("01", transactionId)); // Bill Number
        khqr.append(formatTag("62", additionalData.toString()));
        
        // 63: CRC
        khqr.append("6304");
        String crc = calculateCRC16(khqr.toString());
        khqr.append(crc);
        
        return khqr.toString();
    }

    private String formatTag(String tag, String value) {
        if (value == null) value = "";
        return tag + String.format("%02d", value.length()) + value;
    }

    private String calculateCRC16(String input) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : input.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }
}