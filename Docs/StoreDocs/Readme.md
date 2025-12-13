# Store Module API
BaseUrl : http://localhost:8080/api/stores

## Create Store
Endpoint : POST /

Headers : Authorization: Bearer <token>

Request Body (Option 1: JSON) :
```json
{
  "name": "My Tech Store",
  "location": "Jakarta, Indonesia",
  "imageUrl": "http://example.com/image.jpg"
}
```

Request Body (Option 2: Multipart/Form-Data) :
- `name`: String
- `location`: String
- `imageUrl`: (Optional) File

Response Body Success :

```json
{
  "success": true,
  "message": "Store created successfully",
  "data": {
    "id": 1,
    "name": "My Tech Store",
    "location": "Jakarta, Indonesia",
    "rating": 0.0,
    "isOnline": true,
    "imageUrl": "http://example.com/image.jpg"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "User already has a store"
}
```

## Update My Store
Endpoint : POST /my-store

Headers : Authorization: Bearer <token>

Request Body (Option 1: JSON) :
```json
{
  "name": "My Tech Store Updated",
  "location": "Bandung, Indonesia",
  "imageUrl": "http://example.com/new-image.jpg"
}
```

Request Body (Option 2: Multipart/Form-Data) :
- `name`: (Optional) String
- `location`: (Optional) String
- `imageUrl`: (Optional) File

Response Body Success :

```json
{
  "success": true,
  "message": "Store updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Store Name",
    "location": "Bandung",
    "imageUrl": "http://link-to-new-image.com"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Store not found"
}
```

## Delete Store
Endpoint : DELETE /my-store

Headers : Authorization: Bearer <token>

Response Body Success :

```json
{
  "success": true,
  "message": "Store deleted successfully"
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Store not found"
}
```

## Create My Store Product
Endpoint : POST /my-store/products

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
    "name": "New Product",
    "price": 100000
  }
}
```

## Get All Stores
Endpoint : GET /?page=0&size=10

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get all stores",
  "data": [
    {
      "id": 1,
      "name": "Store A",
      "location": "Jakarta",
      "rating": 4.5
    }
  ],
  "paging": {
    "currentPage": 0,
    "totalPage": 5,
    "limit": 10
  }
}
```

## Get Store Detail
Endpoint : GET /{storeId}

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get store",
  "data": {
    "id": 1,
    "name": "My Tech Store",
    "products": []
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Store not found"
}
```

## Get My Store
Endpoint : GET /my-store

Headers : Authorization: Bearer <token>

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get my store",
  "data": {
    "id": 1,
    "name": "My Store"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Store not found for this user"
}
```

## Get Store Products
Endpoint : GET /{storeId}/products?page=0&limit=10

Request Body : None

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get store products",
  "data": [],
  "paging": {
    "currentPage": 0,
    "totalPage": 1,
    "limit": 10
  }
}
```

## Follow Store
Endpoint : POST /{storeId}/follow

Headers : Authorization: Bearer <token>

Response Body Success :

```json
{
  "success": true,
  "message": "Followed store successfully"
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Already following this store"
}
```

## Unfollow Store
Endpoint : DELETE /{storeId}/unfollow

Headers : Authorization: Bearer <token>

Response Body Success :

```json
{
  "success": true,
  "message": "Unfollowed store successfully"
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Not following this store"
}
```
