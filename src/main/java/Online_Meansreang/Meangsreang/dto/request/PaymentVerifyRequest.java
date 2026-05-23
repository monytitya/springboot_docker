package Online_Meansreang.Meangsreang.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentVerifyRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;
}
