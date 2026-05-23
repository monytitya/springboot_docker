package Online_Meansreang.Meangsreang.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Amount exceeds maximum limit")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(USD|KHR)$", message = "Currency must be USD or KHR")
    private String currency;

    private String customerName;

    @Pattern(regexp = "^[+0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    /** Optional note for the transaction */
    private String note;
}
