# Payment Module API
BaseUrl : http://localhost:8080/api/payments

## Pay (Create Payment)
Endpoint : POST /

Headers : Authorization: Bearer <token>

Request Body :
```json
{
  "invoiceNumber": "INV-1711123456-1",
  "amount": 500000,
  "method": "CREDIT_CARD" // e.g., TRANSFER, CREDIT_CARD, E-WALLET
}
```

Response Body Success :

```json
{
  "success": true,
  "message": "Payment successful",
  "data": {
    "id": 1,
    "invoiceNumber": "INV-1711123456-1",
    "totalAmount": 500000,
    "status": "PAID",
    "paymentMethod": "CREDIT_CARD",
    "paidAt": "2023-12-01T10:05:00"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Payment failed" // or "Invoice not found", "Amount mismatch"
}
```
