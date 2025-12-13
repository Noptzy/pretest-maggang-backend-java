# Auth Module API
BaseUrl : http://localhost:8080/api/auth

## Register User
Endpoint : POST /register

Request Body :
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "User Name"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "name": "User Name",
    "email": "user@example.com"
  }
}
```

## Register Seller
Endpoint : POST /register/seller

Request Body :
```json
{
  "email": "seller@example.com",
  "password": "password123",
  "name": "Seller Name"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Seller registered successfully",
  "data": {
    "name": "Seller Name",
    "email": "seller@example.com"
  }
}
```

## Login
Endpoint : POST /login

Request Body :
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "User login successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiredAt": 1700000000,
    "refreshToken": "uuid-refresh-token",
    "refreshTokenExpiredAt": 1700000000
  }
}
```

## Refresh Token
Endpoint : POST /refresh-token

Request Body :
```json
{
  "token": "uuid-refresh-token"
}
```

Response Body Success :
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiredAt": 1700000000,
    "refreshToken": "new-uuid-refresh-token",
    "refreshTokenExpiredAt": 1700000000
  }
}
```

## Logout
Endpoint : POST /logout

Headers : Authorization: Bearer <token>

Response Body Success :
```json
{
  "success": true,
  "data": "Logout Success"
}
```
