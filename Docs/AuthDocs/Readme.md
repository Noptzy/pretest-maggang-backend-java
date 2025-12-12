# Authenticate Docs API
BaseUrl : http://loalhost:8080/api/auth

## User Register
Endpoint : POST /register

Request Body :
```json
{
  "name": "user",
  "email": "user@gmail.com",
  "password": "12345678"
}
```

Response Body Success :

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "name": "user",
    "email": "user@gmail.com"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Email already registered"
}
```

## Seller Register
Endpoint : POST /register/seller

Request Body :
```json
{
  "name": "user",
  "email": "user@gmail.com",
  "password": "12345678"
}
```

Response Body Success :

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "name": "user",
    "email": "user@gmail.com"
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Email already registered"
}
```

## User Login
Endpoint : POST /login

Request Body :
```json
{
  "email": "user@gmail.com",
  "password": "12345678"
}
```

Response Body Success :

```json
{
  "success": true,
  "message": "User login successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwZW5nZ3VuYUBnbWFpbC5jb20iLCJyb2xlIjoiVVNFUiIsInVzZXJJZCI6Ijc2NGE5NjIyLTIyYWItNGFiMy04ZTA5LTFhZGYyYzhjZjllMSIsImlhdCI6MTc2NTU2MjYzMCwiZXhwIjoxNzY2MTY3NDMwfQ.5OBe0PTosNEN4LOK-eRt7ifNv_Lc69zcZAQ2ms3mUMc",
    "expiredAt": 1766167430499
  }
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Email not found"
}
```

## User Logout
Endpoint : POST /logout

Headers : Authorization: Bearer <token>

Response Body Success :

```json
{
  "success": true,
  "data": "Logout Success"
}
```

Response Body Error :

```json
{
  "success": false,
  "message": "Already Logged Out"
}
```