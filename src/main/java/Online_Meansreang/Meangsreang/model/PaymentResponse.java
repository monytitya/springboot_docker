package Online_Meansreang.Meangsreang.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String transactionId;
    private String qrString;
    private String qrImageBase64; // base64 PNG image
    private double amount;
    private String currency;
    private String status; // PENDING, SUCCESS, FAILED
}
