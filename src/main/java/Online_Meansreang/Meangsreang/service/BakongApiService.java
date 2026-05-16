package Online_Meansreang.Meangsreang.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class BakongApiService {

    @Value("${bakong.token}")
    private String token;

    @Value("${bakong.api-url}")
    private String apiUrl;

    private final WebClient webClient;

    public BakongApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String checkTransactionStatus(String transactionId) {
        try {
            // Ensure no double slashes if apiUrl ends with /
            String baseUrl = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
            
            // Bakong Open API endpoint for checking status by external reference
            Map response = webClient.post()
                    .uri(baseUrl + "/v1/check_transaction_by_external_ref")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("external_ref", transactionId))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("responseCode") != null && response.get("responseCode").toString().equals("0")) {
                Map data = (Map) response.get("data");
                if (data != null) {
                    // Check both 'status' and 'transactionStatus' fields
                    Object status = data.getOrDefault("status", data.get("transactionStatus"));
                    if ("SUCCESS".equals(status)) {
                        return "SUCCESS";
                    }
                }
            }
            return "PENDING";

        } catch (Exception e) {
            return "ERROR";
        }
    }
}