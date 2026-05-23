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

    public String generateDynamicQR() {
        return generateKhqr("TXN" + System.currentTimeMillis(), 0.0, "USD");
    }

    public String generateStaticQR(Double amount, String currency) {
        return generateKhqr("TXN" + System.currentTimeMillis(), amount, currency);
    }

    public String generateKhqr(String transactionId, Double amount, String currency) {
        String mId = merchantId != null ? merchantId.trim().replace("\"", "") : "";
        String mName = merchantName != null ? merchantName.trim().replace("\"", "") : "Mao Tityamony";
        String mCity = merchantCity != null ? merchantCity.trim().replace("\"", "") : "Phnom Penh";

        double inputAmount = (amount != null) ? amount : 0.0;

        StringBuilder khqr = new StringBuilder();

        khqr.append(formatTag("00", "01"));

        khqr.append(formatTag("01", "11"));

        StringBuilder merchantAccount = new StringBuilder();
        merchantAccount.append(formatTag("00", "kh.com.bakong")); 
        merchantAccount.append(formatTag("01", mId));
        khqr.append(formatTag("29", merchantAccount.toString()));

        khqr.append(formatTag("52", "0000"));

        String currencyCode = "USD".equalsIgnoreCase(currency) ? "840" : "116";
        khqr.append(formatTag("53", currencyCode));

        if (inputAmount > 0) {
            String amountStr = "KHR".equalsIgnoreCase(currency)
                    ? String.format("%.0f", inputAmount)
                    : String.format("%.2f", inputAmount);
            khqr.append(formatTag("54", amountStr));
        }

        khqr.append(formatTag("58", "KH"));

        khqr.append(formatTag("59", mName.toUpperCase().replace(" ", "")));

        khqr.append(formatTag("60", mCity.toUpperCase().replace(" ", "")));

        StringBuilder additionalData = new StringBuilder();
        additionalData.append(formatTag("01", transactionId)); // Bill Number
        khqr.append(formatTag("62", additionalData.toString()));

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