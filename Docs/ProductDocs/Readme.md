# Product Docs API
BaseUrl : http://localhost:8080/api/products

delete on db:
variant_name, condition_info, weight_in_grams

## Create Product
Endpoint : POST /
Headers : Authorization: Bearer <token>
Content-Type: multipart/form-data

Request Body (Form Data & Body):
- name: String
- description: String
- price: BigDecimal
- stock: Integer
- category: String
- color: String
- imageUrl: File (MultipartFile)

otomatic from system
- sold_for: Integer (new on db, after user transaction on this product will add 1+ )
- rating: Float (1.0-5.0, rule = final rating = all star from user / all user rating)

```json
    "name":"New Product",
    "description": "Product Description",
    "price": 50000,
    "stock": 100,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "path/to/image.jpg"
```


Response Body Success :
```json
{
  "success": true,  
  "message": "Product created successfully",
  "data": {
    "id": 101,
    "name": "New Product",
    "description": "Product Description",
    "price": 50000,
    "stock": 100,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "path/to/image.jpg"
  }
}
```

## Search Products
Endpoint : GET /?name=xxx&category=xxx&minPrice=0&maxPrice=100000&page=0&limit=10

Response Body Success :

```json
{
  "success": true,
  "message": "List products",
  "data": [
    {
      "id": 101,
      "name": "New Product",
      "price": 50000,
      "imageUrl": "path/to/image.jpg",
      "sold_for": 123,
      "rating": 3.2,
      "store_id": 1
    },
    {
      "id": 103,
      "name": "New Product",
      "price": 50000,
      "imageUrl": "path/to/image.jpg",
      "sold_for": 12,
      "rating": 4.5,
      "store_id": 2
    }
  ],
  "paging": {
    "currentPage": 0,
    "totalPage": 5,
    "limit": 10
  }
}
```

Response Body Failed :
```json
{
  "success": true,
  "message": "No product found, try different keyword"
}
```

## Delete Product
Endpoint : DELETE /{productId}
Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "message": "Product deleted successfully"
}
```

Response Body Failed :
```json
{
  "success": true,
  "message": "Product Not Found"
}
```

## Update Product
Endpoint : PUT /{productId}
Headers : Authorization: Bearer <token>
Content-Type: multipart/form-data

Request Parameters (Form Data):
- name: String
- description: String
- price: BigDecimal
- stock: Integer
- category: String
- color: String
- imageUrl: File (MultipartFile)

Request Body :
```json
    "name": "New Product",
    "description": "Product Description",
    "price": 50000,
    "stock": 100,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "path/to/image.jpg"
```

Response Body Success :
```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": {
    "id": 101,
    "name": "New Product",
    "description": "Product Description",
    "price": 50000,
    "stock": 100,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "path/to/image.jpg"
  }
}
```

Response Body Failed :
```json
{
  "success": true,
  "message": "Failed update product"
}
```

## Get Product By Id User
Endpoint : GET /{productId}

Response Body Success :

```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": {
    "id": 101,
    "name": "New Product",
    "description": "Product Description",
    "price": 50000,
    "stock": 100,
    "category": "Electronics",
    "color": "Black",
    "imageUrl": "path/to/image.jpg",
    "store_id": 1,
    "store_rating": 4.5
  }
}
```

Response Body Failed :
```json
{
  "success": true,
  "message": "Failed get product"
}
```
