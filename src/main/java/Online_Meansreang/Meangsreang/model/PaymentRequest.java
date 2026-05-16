package Online_Meansreang.Meangsreang.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @DecimalMin(value = "0.00", message = "Amount មិនអាចតិចជាង 0")
    private Double amount;

    @NotBlank(message = "Currency មិនអាចទទេបានទេ")
    @Pattern(regexp = "USD|KHR", message = "USD ឬ KHR ប៉ុណ្ណោះ")
    private String currency;

    private String orderId;
    private String customerName;
    private String email;
    private String phone;

    @Builder.Default
    private boolean isAmountEditable = true; // ← Frontend checks this field
}