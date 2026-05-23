package Online_Meansreang.Meangsreang.service;

import Online_Meansreang.Meangsreang.entity.Transaction;
import Online_Meansreang.Meangsreang.enums.PaymentStatus;
import Online_Meansreang.Meangsreang.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(String transactionId, Double amount,
            String currency, String qrString,
            String customerName, String email, String phone) {

        String upperCurrency = currency != null ? currency.toUpperCase() : "USD";
        if (!upperCurrency.equals("USD") && !upperCurrency.equals("KHR")) {
            throw new RuntimeException("Currency ត្រូវតែជា USD ឬ KHR!");
        }

        double inputAmount = amount != null ? amount : 0.0;
        if (inputAmount < 0) {
            throw new RuntimeException("Amount មិនអាចតិចជាង 0 ទេ!");
        }

        double finalAmount = inputAmount;
        if (upperCurrency.equals("KHR") && inputAmount > 0) {
            finalAmount = inputAmount / 4100.0;
        }

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .amount(finalAmount)
                .currency(upperCurrency)
                .qrString(qrString)
                .customerName(customerName)
                .customerEmail(email)
                .customerPhone(phone)
                .status(PaymentStatus.PENDING)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: {}", transactionId);
        return saved;
    }

    public Transaction markAsSuccess(String transactionId) {
        Transaction transaction = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        transaction.setStatus(PaymentStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction SUCCESS: {}", transactionId);
        return updated;
    }

    public Transaction markAsFailed(String transactionId) {
        Transaction transaction = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        transaction.setStatus(PaymentStatus.FAILED);
        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction FAILED: {}", transactionId);
        return updated;
    }

    public Optional<Transaction> getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Scheduled(fixedRate = 60000)
    public void expirePendingTransactions() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);
        List<Transaction> expired = transactionRepository.findExpiredPendingTransactions(expiryTime);

        expired.forEach(t -> {
            t.setStatus(PaymentStatus.EXPIRED);
            transactionRepository.save(t);
            log.info("Transaction EXPIRED: {}", t.getTransactionId());
        });
    }
}
