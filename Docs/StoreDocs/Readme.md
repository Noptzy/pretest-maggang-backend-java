# Store Docs API
BaseUrl : http://localhost:8080/api/stores

## Create Store
Endpoint : POST /
Headers : Authorization: Bearer <token> only role === SELLER

add description store

Request Body :

```json
{
  "name": "My Store",
  "location": "Jakarta",
  "imageUrl": "optional-image-url",
  "desc": "my store desc"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Store created successfully",
  "data": {
    "id": 1,
    "name": "My Store",
    "location": "Jakarta",
    "imageUrl": "optional-image-url",
    "desc": "my store desc"
  }
}
```

## Update Store
Endpoint : PATCH /
Headers : Authorization: Bearer <token>

Request Body :
```json
{
  "name": "Updated Store Name",
  "location": "New Location",
  "imageUrl": "new-image-url",
  "desc": "my store desc"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Store updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Store Name",
    "location": "New Location",
    "imageUrl": "new-image-url",
    "desc": "my store desc"
  }
}
```

## List Stores
Endpoint : GET /?&page=0&size=10

Response Body Success :

```json
{
  "success": true,
  "message": "Successfully get all stores",
  "data": [
    {
      "id": 1,
      "name": "My Store",
      "image_url": "path.img",
      "rating": 3.1
    }
  ],
  "paging": {
    "currentPage": 0,
    "totalPage": 10,
    "limit": 10
  }
}
```

Response Body Failed :

```json
{
  "success": false,
  "message": "No Stores are found"
}
```

## List Store Products
Endpoint : GET /{storeId}/products?page=0&limit=10

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully get store products",
  "data": [
    {
      "id": 1,
      "name": "Product Name",
      "price": 10000,
      ...
    }
  ],
  "paging": { ... }
}
```

## Get Product Detail
Endpoint : GET /{storeId}/products/{productId}

Response Body Success :
```json
{
  "success": true,
  "message": "Successfully get product",
  "data": {
    "id": 1,
    "name": "Product Name",
    ...
  }
}
```

    