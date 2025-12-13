# Cart Module API
BaseUrl : http://localhost:8080/api/carts

## Add To Cart
Endpoint : POST /

Headers : Authorization: Bearer <token>

**Note:** Only users with role `USER` can perform this action.

Request Body :
```json
{
  "productId": 1,
  "quantity": 2,
  "note": "Please pack safely"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Product added to cart successfully",
  "data": {
    "id": 1,
    "items": [
      {
        "id": 101,
        "productId": 1,
        "productName": "Laptop",
        "quantity": 2,
        "price": 15000000,
        "subtotal": 30000000,
        "note": "Please pack safely",
        "isSelected": true
      }
    ],
    "totalAmount": 30000000
  }
}
```

Response Body Error :
```json
{
  "success": false,
  "message": "Only users with role USER can shop"
}
```

## Get My Cart
Endpoint : GET /

Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully get cart",
  "data": {
    "id": 1,
    "items": [],
    "totalAmount": 0
  }
}
```

## Update Cart Item
Endpoint : PUT /{cartItemId}

Headers : Authorization: Bearer <token>

Request Body :
- `quantity`: (Optional) Integer
- `note`: (Optional) String
- `isSelected`: (Optional) Boolean

```json
{
  "quantity": 3,
  "note": "Updated note",
  "isSelected": false
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully update product in cart",
  "data": { ... }
}
```

## Delete Cart Item
Endpoint : DELETE /{cartItemId}

Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully delete product from cart",
  "data": { ... }
}
```
