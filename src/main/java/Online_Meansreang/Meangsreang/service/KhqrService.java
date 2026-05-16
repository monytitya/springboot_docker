package Online_Meansreang.Meangsreang.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    // ✅ Dynamic QR - User enters Amount & Currency in their app
    public String generateDynamicQR() {
        return generateKhqr("TXN" + System.currentTimeMillis(), 0.0, "USD");
    }

    // ✅ Static QR - Locked amount and currency
    public String generateStaticQR(Double amount, String currency) {
        return generateKhqr("TXN" + System.currentTimeMillis(), amount, currency);
    }

    public String generateKhqr(String transactionId, Double amount, String currency) {
        // 1. Sanitize all inputs (Remove quotes and non-standard characters)
        String mId = merchantId != null ? merchantId.trim().replace("\"", "") : "";
        String mName = merchantName != null ? merchantName.trim().replace("\"", "") : "Mao Tityamony";
        String mCity = merchantCity != null ? merchantCity.trim().replace("\"", "") : "Phnom Penh";

        StringBuilder khqr = new StringBuilder();

        // 00: Payload Format Indicator
        khqr.append(formatTag("00", "01"));

        // 01: Initiation Method
        // 12 = Dynamic (Locked amount), 11 = Static (Unlocked amount)
        double inputAmount = (amount != null) ? amount : 0.0;
        String pointOfInitiation = (inputAmount > 0) ? "12" : "11";
        khqr.append(formatTag("01", pointOfInitiation));

        // 30: Individual Account Information (BEST for @bkrt accounts)
        StringBuilder individualAccount = new StringBuilder();
        individualAccount.append(formatTag("00", "kh.com.bakong")); // GUID
        individualAccount.append(formatTag("01", mId));             // Your Bakong ID
        khqr.append(formatTag("30", individualAccount.toString()));

        // 52: Merchant Category Code (0000 = Personal)
        khqr.append(formatTag("52", "0000"));

        // 53: Transaction Currency (840 = USD, 116 = KHR)
        String currencyCode = "USD".equalsIgnoreCase(currency) ? "840" : "116";
        khqr.append(formatTag("53", currencyCode));

        // 54: Transaction Amount
        if (inputAmount > 0) {
            String amountStr = "KHR".equalsIgnoreCase(currency)
                    ? String.format("%.0f", inputAmount)
                    : String.format("%.2f", inputAmount);
            khqr.append(formatTag("54", amountStr));
        }

        // 58: Country Code
        khqr.append(formatTag("58", "KH"));

        // 59: Merchant Name (Force Uppercase for better bank matching)
        khqr.append(formatTag("59", mName.toUpperCase()));

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
        if (value == null)
            value = "";
        return tag + String.format("%02d", value.length()) + value;
    }

    private String calculateCRC16(String input) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : input.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= ((b & 0xFF) << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc = crc << 1;
                }
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc).toUpperCase();
    }
}