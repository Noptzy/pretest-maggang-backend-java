package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.TransactionDetailResponse;
import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.entity.*;
import com.pretest.ecommerce.repository.CartRepository;
import com.pretest.ecommerce.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.Objects;

@Service
public class TransactionService {

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private CartRepository cartRepository;

        @Transactional
        public List<TransactionResponse> checkout(UUID userId) {
                Cart cart = cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Cart not found"));

                List<CartItem> selectedItems = cart.getCartItems().stream()
                        .filter(CartItem::getIsSelected)
                        .collect(Collectors.toList());

                if (selectedItems.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items selected for checkout");
                }

                Map<Store, List<CartItem>> itemsByStore = selectedItems.stream()
                        .collect(Collectors.groupingBy(item -> item.getProduct().getStore()));

                List<Transaction> transactions = new ArrayList<>();

                for (Map.Entry<Store, List<CartItem>> entry : itemsByStore.entrySet()) {
                        Store store = entry.getKey();
                        List<CartItem> storeItems = entry.getValue();

                        Transaction transaction = new Transaction();
                        transaction.setUser(cart.getUser());
                        transaction.setStore(store);
                        transaction.setInvoiceNumber("INV-" + System.currentTimeMillis() + "-" + store.getId());

                        transaction.setStatus("WAITING_PAYMENT");

                        transaction.setTransactionDate(LocalDateTime.now());
                        transaction.setCreatedAt(LocalDateTime.now());
                        transaction.setUpdatedAt(LocalDateTime.now());
                        transaction.setShippingCost(BigDecimal.ZERO);

                        List<TransactionDetail> details = new ArrayList<>();
                        BigDecimal totalAmount = BigDecimal.ZERO;

                        for (CartItem item : storeItems) {
                                TransactionDetail detail = new TransactionDetail();
                                detail.setTransaction(transaction);
                                detail.setProduct(item.getProduct());
                                detail.setProductNameSnapshot(item.getProduct().getName());
                                detail.setQuantity(item.getQuantity());
                                detail.setPriceAtPurchase(item.getProduct().getPrice());

                                if (item.getProduct().getStock() < item.getQuantity()) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Stock not enough for product: " + item.getProduct().getName());
                                }

                                item.getProduct().setStock(item.getProduct().getStock() - item.getQuantity());
                                Integer currentSold = item.getProduct().getSoldFor() == null ? 0
                                        : item.getProduct().getSoldFor();
                                item.getProduct().setSoldFor(currentSold + item.getQuantity());

                                BigDecimal subtotal = item.getProduct().getPrice()
                                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                                detail.setSubtotal(subtotal);
                                detail.setNote(item.getNote());
                                detail.setCreatedAt(LocalDateTime.now());
                                detail.setUpdatedAt(LocalDateTime.now());

                                details.add(detail);
                                totalAmount = totalAmount.add(subtotal);
                        }

                        transaction.setTransactionDetails(details);
                        transaction.setTotalAmount(totalAmount);
                        transactions.add(transaction);
                }

                transactionRepository.saveAll(transactions);

                cart.getCartItems().removeAll(selectedItems);
                cartRepository.save(cart);

                return transactions.stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
        }

        private TransactionResponse toResponse(Transaction transaction) {
                List<TransactionDetailResponse> detailResponses = transaction.getTransactionDetails().stream()
                        .map(detail -> TransactionDetailResponse.builder()
                                .id(detail.getId())
                                .productId(detail.getProduct().getId())
                                .productName(detail.getProductNameSnapshot())
                                .quantity(detail.getQuantity())
                                .price(detail.getPriceAtPurchase())
                                .subtotal(detail.getSubtotal())
                                .note(detail.getNote())
                                .imageUrl(detail.getProduct().getImageUrl())
                                .build())
                        .collect(Collectors.toList());

                return TransactionResponse.builder()
                        .id(transaction.getId())
                        .userId(transaction.getUser().getId().toString())
                        .storeId(transaction.getStore().getId())
                        .storeName(transaction.getStore().getName())
                        .invoiceNumber(transaction.getInvoiceNumber())
                        .totalAmount(transaction.getTotalAmount())
                        .shippingCost(transaction.getShippingCost())
                        .status(transaction.getStatus())
                        .createdAt(transaction.getCreatedAt())
                        .details(detailResponses)
                        .build();
        }

        @Transactional(readOnly = true)
        public Page<TransactionResponse> getUserTransactions(User user, int page, int limit, String status) {
                Specification<Transaction> specification = (root, query, builder) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(builder.equal(root.get("user").get("id"), user.getId()));

                        if (Objects.nonNull(status) && !status.isEmpty()) {
                                predicates.add(builder.equal(root.get("status"), status));
                        }

                        return query.where(predicates.toArray(new Predicate[0])).getRestriction();
                };

                Pageable pageable = PageRequest.of(page, limit);
                Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);

                List<TransactionResponse> responses = transactions.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

                return new PageImpl<>(responses, pageable, transactions.getTotalElements());
        }

        @Transactional(readOnly = true)
        public TransactionResponse getTransactionByInvoice(User user, String invoiceNumber) {
                Transaction transaction = transactionRepository.findByInvoiceNumber(invoiceNumber)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Transaction not found"));

                if (!transaction.getUser().getId().equals(user.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Transaction does not belong to user");
                }

                return toResponse(transaction);
        }
}