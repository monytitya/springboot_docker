package Online_Meansreang.Meangsreang.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private double amount;
    private String currency;
    private String orderId;
    private String customerName;
    private String email;
    private String phone;
}