# Product Module API
BaseUrl : http://localhost:8080/api

## Create Product
Endpoint : POST /stores/{storeId}/products

Headers : Authorization: Bearer <token>

Request Body (Multipart/Form-Data) :
- `name`: String
- `description`: (Optional) String
- `price`: BigDecimal
- `stock`: Integer
- `category`: String
- `color`: (Optional) String
- `imageUrl`: (Optional) File

Response Body Success :

```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Laptop Gaming",
    "description": "High performance laptop",
    "price": 15000000,
    "stock": 5,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "http://localhost:8080/api/images/uuid-filename.jpg",
    "soldFor": 0,
    "rating": 0.0,
    "storeId": 1,
    "storeName": "Toko Curl",
    "storeLocation": "Jakarta",
    "storeRating": 0.0,
    "createdAt": "2024-05-20T10:00:00",
    "updatedAt": "2024-05-20T10:00:00"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "You do not own this store"
}
```

## Get Product Detail
Endpoint : GET /products/{productId}

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get product",
  "data": {
    "id": 1,
    "name": "Laptop Gaming",
    "description": "High performance laptop",
    "price": 15000000,
    "stock": 5,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "http://localhost:8080/api/images/uuid-filename.jpg",
    "soldFor": 0,
    "rating": 4.5,
    "storeId": 1,
    "storeName": "Toko Curl",
    "storeLocation": "Jakarta",
    "storeRating": 4.8,
    "createdAt": "2024-05-20T10:00:00",
    "updatedAt": "2024-05-20T10:00:00"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Product not found"
}
```

## List/Search All Products
Endpoint : GET /products?name=...&category=...&minPrice=...&maxPrice=...&page=0&limit=10

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get all products",
  "data": [
    {
        "id": 1,
        "name": "Laptop Gaming",
        "description": "High performance laptop",
        "price": 15000000,
        "stock": 5,
        "category": "Electronics",
        "color": "Black",
        "imageUrl": "http://localhost:8080/api/images/uuid-filename.jpg",
        "soldFor": 10,
        "rating": 4.5,
        "storeId": 1,
        "storeName": "Toko Curl",
        "storeLocation": "Jakarta",
        "storeRating": 4.8,
        "createdAt": "2024-05-20T10:00:00",
        "updatedAt": "2024-05-20T12:00:00"
    }
  ],
  "paging": {
    "currentPage": 0,
    "totalPage": 5,
    "limit": 10
  }
}
```

## Rate Product
Endpoint : POST /products/{productId}/rate

Headers : Authorization: Bearer <token>

Request Body :
```json
{
  "rating": 4.5
}
```

**Restrictions:**
- User must have purchased the product.
- User can only rate **once per purchase transaction**.
- Seller **cannot** rate their own products.

Response Body Success :

```json
{
  "success": true,
  "message": "Product rated successfully",
  "data": {
    "id": 1,
    "rating": 4.5,
    "storeRating": 4.8
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "You must purchase the product first to rate it"
}
```
