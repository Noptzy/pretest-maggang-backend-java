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

@Service
public class TransactionService {

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private CartRepository cartRepository;

        @Transactional
        public List<TransactionResponse> checkout(UUID userId) {
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Cart is empty"));

                List<CartItem> selectedItems = cart.getCartItems().stream()
                                .filter(CartItem::getIsSelected)
                                .collect(Collectors.toList());

                if (selectedItems.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items selected for checkout");
                }

                // Group items by Store
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
                        transaction.setPaymentStatus("PENDING");
                        transaction.setShippingStatus("PENDING");
                        transaction.setCreatedAt(LocalDateTime.now());
                        transaction.setUpdatedAt(LocalDateTime.now());
                        transaction.setShippingCost(BigDecimal.ZERO); // Determine shipping cost logic if needed

                        List<TransactionDetail> details = new ArrayList<>();
                        BigDecimal totalAmount = BigDecimal.ZERO;

                        for (CartItem item : storeItems) {
                                TransactionDetail detail = new TransactionDetail();
                                detail.setTransaction(transaction);
                                detail.setProduct(item.getProduct());
                                detail.setProductNameSnapshot(item.getProduct().getName());
                                // detail.setProductVariantSnapshot(); // Logic if variants exist
                                detail.setQuantity(item.getQuantity());
                                detail.setPriceAtPurchase(item.getProduct().getPrice());

                                // Check stock
                                if (item.getProduct().getStock() < item.getQuantity()) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                        "Stock not enough for product: " + item.getProduct().getName());
                                }

                                // Deduct stock
                                item.getProduct().setStock(item.getProduct().getStock() - item.getQuantity());

                                // Increment soldFor
                                Integer currentSold = item.getProduct().getSoldFor() == null ? 0
                                                : item.getProduct().getSoldFor();
                                item.getProduct().setSoldFor(currentSold + item.getQuantity());

                                // Product update will be cascaded or strictly saved if needed, but since
                                // item.getProduct() is managed entity (from cart->fetch join or lazy load in
                                // session), it should update.
                                // However, let's look at how cart was fetched. cartRepository.findByUserId
                                // probably fetches products too.
                                // To be safe, we might need to save product or let transaction commit handle it
                                // if attached.
                                // Since this is @Transactional, changes to managed entities are flushed.

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

                // Remove bought items from cart
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
                                .paymentStatus(transaction.getPaymentStatus())
                                .shippingStatus(transaction.getShippingStatus())
                                .createdAt(transaction.getCreatedAt())
                                .details(detailResponses)
                                .build();
        }
}
