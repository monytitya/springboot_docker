package Online_Meansreang.Meangsreang.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Online_Meansreang.Meangsreang.entity.Transaction;
import Online_Meansreang.Meangsreang.model.PaymentRequest;
import Online_Meansreang.Meangsreang.model.PaymentResponse;
import Online_Meansreang.Meangsreang.service.BakongApiService;
import Online_Meansreang.Meangsreang.service.KhqrService;
import Online_Meansreang.Meangsreang.service.TransactionService;
import Online_Meansreang.Meangsreang.util.QrCodeUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    @Autowired
    private KhqrService khqrService;
    @Autowired
    private QrCodeUtil qrCodeUtil;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BakongApiService bakongApiService;

    @PostMapping("/generate-qr")
    public ResponseEntity<PaymentResponse> generateQr(@RequestBody PaymentRequest request) {
        try {
            String transactionId = "TXN" + System.currentTimeMillis();

            String qrString = khqrService.generateKhqr(
                    transactionId, request.getAmount(), request.getCurrency());

            String qrBase64 = qrCodeUtil.generateQrBase64(qrString, 300, 300);

            transactionService.createTransaction(
                    transactionId,
                    request.getAmount(),
                    request.getCurrency(),
                    qrString,
                    request.getCustomerName(),
                    request.getEmail(),
                    request.getPhone());

            return ResponseEntity.ok(PaymentResponse.builder()
                    .transactionId(transactionId)
                    .qrString(qrString)
                    .qrImageBase64("data:image/png;base64," + qrBase64)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status("PENDING")
                    .build());

        } catch (Exception e) {
            log.error("Error generating QR", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/check-status/{transactionId}")
    public ResponseEntity<?> checkStatus(@PathVariable String transactionId) {
        String status = bakongApiService.checkTransactionStatus(transactionId);

        if ("SUCCESS".equals(status)) {
            transactionService.markAsSuccess(transactionId); // ✅ update DB
        }

        return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "status", status));
    }

    // 3️⃣ Bakong Webhook → Update DB
    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("Received Bakong Callback: {}", payload);

        try {
            // Bakong sometimes nests data in a "data" object
            Map<String, Object> data = payload;
            if (payload.containsKey("data") && payload.get("data") instanceof Map) {
                data = (Map<String, Object>) payload.get("data");
            }

            // Extract externalRef (using safer String.valueOf to avoid ClassCastException)
            Object externalRefObj = data.getOrDefault("externalRef", data.get("external_ref"));
            String transactionId = externalRefObj != null ? String.valueOf(externalRefObj) : null;
            
            Object statusObj = data.get("status");
            String status = statusObj != null ? String.valueOf(statusObj) : null;

            if (transactionId == null) {
                log.warn("Callback received without externalRef. Payload: {}", payload);
                return ResponseEntity.badRequest().body("Error: Missing externalRef in payload");
            }

            if ("SUCCESS".equalsIgnoreCase(status)) {
                transactionService.markAsSuccess(transactionId);
                log.info("Successfully processed SUCCESS callback for transaction: {}", transactionId);
            } else {
                transactionService.markAsFailed(transactionId);
                log.info("Processed FAILED/OTHER callback for transaction: {} (Status: {})", transactionId, status);
            }

            return ResponseEntity.ok("Callback Processed Successfully");

        } catch (Exception e) {
            log.error("Fatal error during Bakong callback processing", e);
            // Return 200 with error details to avoid infinite retries from webhook provider
            return ResponseEntity.ok("Warning: Callback received but processing failed: " + e.getMessage());
        }
    }

    // 4️⃣ Get All Transactions (admin view)
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // 5️⃣ Get Single Transaction
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        return transactionService.getTransaction(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}