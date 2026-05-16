package Online_Meansreang.Meangsreang.controller;

import Online_Meansreang.Meangsreang.service.KhqrService;
import Online_Meansreang.Meangsreang.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QRController {

    private final KhqrService khqrService;
    private final QrCodeUtil qrCodeUtil;

    // ✅ Dynamic QR (User enters Amount & Currency themselves)
    @GetMapping("/dynamic")
    public ResponseEntity<?> getDynamicQR() {
        String qrString = khqrService.generateDynamicQR();
        
        // Convert to Base64 Image using our Utility
        String qrImage = qrCodeUtil.generateQrBase64(qrString, 300, 300);
        
        return ResponseEntity.ok(Map.of(
            "qrString", qrString,
            "qrImage", "data:image/png;base64," + qrImage,
            "type", "DYNAMIC",
            "message", "អ្នកប្រើអាចជ្រើស Amount និង Currency បាន (Unlocked)"
        ));
    }

    // ✅ Static QR (Lock amount)
    @GetMapping("/static")
    public ResponseEntity<?> getStaticQR(
            @RequestParam Double amount,
            @RequestParam String currency) {
        
        String qrString = khqrService.generateStaticQR(amount, currency);
        String qrImage = qrCodeUtil.generateQrBase64(qrString, 300, 300);
        
        return ResponseEntity.ok(Map.of(
            "qrString", qrString,
            "qrImage", "data:image/png;base64," + qrImage,
            "amount", amount,
            "currency", currency,
            "type", "STATIC",
            "message", "Amount ត្រូវបានចាក់សោ (Locked)"
        ));
    }
}
