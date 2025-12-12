# Cart Docs API
BaseUrl : http://localhost:8080/api/carts

## Add to Cart
Endpoint : POST /
Headers : Authorization: Bearer <token>

Request Body :
```json
{
  "productId": 101,
  "quantity": 2,
  "note": "Optional note"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Product added to cart successfully",
  "data": {
    "id": "cart-uuid",
    "totalPrice": 100000,
    "totalQuantity": 2,
    "items": [
        {
            "id": 1,
            "productId": 101,
            "productName": "Product Name",
            "quantity": 2,
            "price": 50000,
            "totalPrice": 100000,
            "note": "Optional note"
        }
    ]
  }
}
```

Response Body Failed :
```json
{
  "success": false,
  "message": "False to add product to cart"
}
```

## Get Cart
Endpoint : GET /
Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully get cart",
  "data": {
    "id": "1",
    "totalPrice": 100000,
    "items": [
      {
        "id": 1,
        "productId": 101,
        "productName": "Product Name",
        "quantity": 2,
        "price": 50000,
        "totalPrice": 100000,
        "note": "Optional note"
      }
    ]
  }
}
```

Response Body Failed :
```json
{
  "success": false,
  "message": "You haven't add product to cart"
}
```

## Delete Product From Cart
Endpoint : Delete /:id
Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully delete product from cart",
  "data": {
    "id": "1",
    "totalPrice": 100000,
    "items": [
      {
        "id": 1,
        "productId": 101,
        "productName": "Product Name",
        "quantity": 2,
        "price": 50000,
        "totalPrice": 100000,
        "note": "Optional note"
      }
    ]
  }
}
```

Response Body Failed :
```json
{
  "success": false,
  "message": "Failed delete product from cart"
}
```
