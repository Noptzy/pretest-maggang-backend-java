# Transaction Module API
BaseUrl : http://localhost:8080/api/transactions

## Checkout (Create Transaction)
Endpoint : POST /checkout

Headers : Authorization: Bearer <token>

Request Body : None (Utilizes currently `isSelected=true` items in the User's Cart)

Response Body Success :

```json
{
  "success": true,
  "message": "Checkout successful",
  "data": [
    {
      "id": 101,
      "invoiceNumber": "INV-1711123456-1",
      "totalAmount": 500000,
      "details": []
    }
  ]
}
```

Response Body Error :

Response Body Error :

```json
{
  "success": false,
  "message": "Cart is empty"
}
```

## List User Transactions (History)
Endpoint : GET /?page=0&limit=10&status=PENDING

Headers : Authorization: Bearer <token>

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully retrieved transactions",
  "data": [
    {
      "invoiceNumber": "INV-1711123456-1",
      "totalAmount": 500000,
      "paymentStatus": "PENDING",
      "shippingStatus": "PENDING",
      "createdAt": "2023-12-01T10:00:00"
    }
  ],
  "paging": {
    "currentPage": 0,
    "totalPage": 10,
    "limit": 10
  }
}
```

## Get Transaction Detail
Endpoint : GET /{invoiceNumber}

Headers : Authorization: Bearer <token>

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully retrieved transaction detail",
  "data": {
    "invoiceNumber": "INV-1711123456-1",
    "details": [
      {
        "productName": "Gaming Mouse",
        "quantity": 1,
        "price": 500000,
        "subtotal": 500000
      }
    ]
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Transaction not found"
}
```
