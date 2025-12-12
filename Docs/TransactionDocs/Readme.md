# Transaction Docs API
BaseUrl : http://localhost:8080/api/transactions

## Checkout
Endpoint : POST /checkout
Headers : Authorization: Bearer <token>
Description: Checkouts all items currently in the user's cart.

Response Body Success :
```json
{
  "success": true,
  "message": "Checkout successful",
  "data": [
      {
          "id": "transaction-uuid",
          "storeId": 1,
          "storeName": "Store Name",
          "totalAmount": 100000,
          "status": "PENDING",
          "createdAt": "...",
          "details": [
              {
                  "id": "detail-uuid",
                  "productName": "Product Name",
                  "quantity": 2,
                  "price": 50000,
                  "subtotal": 100000
              }
          ]
      }
  ]
}
```
