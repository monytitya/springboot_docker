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

    @GetMapping("/qr-info")
    public ResponseEntity<?> getQRInfo() {
        return ResponseEntity.ok(Map.of(
                "amount", 0,
                "currency", "USD",
                "isAmountFixed", false));
    }

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
            transactionService.markAsSuccess(transactionId);
        }

        return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "status", status));
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("Received Bakong Callback Payload: {}", payload);

        try {

            String transactionId = deepSearchValue(payload, List.of("externalRef", "external_ref", "externalReference",
                    "billNumber", "bill_number", "orderId", "order_id"));

            if (transactionId == null) {
                log.error(
                        "CRITICAL: Callback received but Transaction ID (externalRef) is missing from ANY field! Payload: {}",
                        payload);
                return ResponseEntity.badRequest().body("Error: Transaction ID not found in payload structure.");
            }

            String statusValue = deepSearchValue(payload, List.of("status", "transactionStatus", "paymentStatus"));
            String status = (statusValue != null) ? statusValue.toUpperCase() : "FAILED";

            log.info("Processing Callback for Transaction: {} with Status: {}", transactionId, status);

            if ("SUCCESS".equals(status) || "COMPLETED".equals(status)) {
                transactionService.markAsSuccess(transactionId);
                return ResponseEntity.ok("Callback Processed: SUCCESS");
            } else {
                transactionService.markAsFailed(transactionId);
                return ResponseEntity.ok("Callback Processed: " + status);
            }

        } catch (Exception e) {
            log.error("Fatal error during callback processing", e);
            return ResponseEntity.ok("Error handled: " + e.getMessage());
        }
    }

    private String deepSearchValue(Object source, List<String> targetKeys) {
        if (source instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) source;

            for (String key : targetKeys) {
                for (String actualKey : map.keySet()) {
                    if (actualKey.equalsIgnoreCase(key)) {
                        return String.valueOf(map.get(actualKey));
                    }
                }
            }

            for (Object value : map.values()) {
                String found = deepSearchValue(value, targetKeys);
                if (found != null)
                    return found;
            }
        } else if (source instanceof List) {
            List<?> list = (List<?>) source;
            for (Object item : list) {
                String found = deepSearchValue(item, targetKeys);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        return transactionService.getTransaction(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String findValueInMap(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
        }
        return null;
    }
}