# Waitlist API Integration Guide

## API Endpoints

### Base URL
```
http://localhost:8080/api/waitlist
```

### 1. Join Waitlist (Public)
**Endpoint:** `POST /api/waitlist/join`

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+919876543210"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Successfully joined the waitlist!",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+919876543210",
    "createdAt": "2025-10-15T23:46:13"
  }
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "message": "Email already registered in waitlist"
}
```

### 2. Get Waitlist Count (Public)
**Endpoint:** `GET /api/waitlist/count`

**Success Response:**
```json
{
  "success": true,
  "count": 150
}
```

### 3. Get All Waitlist Entries (Protected - Admin Only)
**Endpoint:** `GET /api/waitlist/all`

**Success Response:**
```json
{
  "success": true,
  "count": 150,
  "data": [
    {
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "+919876543210",
      "createdAt": "2025-10-15T23:46:13"
    }
  ]
}
```

---

## React Integration Example

### Update your App.jsx handleSubmit function:

```javascript
const handleSubmit = async () => {
  if (email && phone && name) {
    try {
      const response = await fetch('http://localhost:8080/api/waitlist/join', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name,
          email: email,
          phone: phone
        })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        // Success
        setSubmitted(true);
        setEmail('');
        setPhone('');
        setName('');
        setTimeout(() => {
          setSubmitted(false);
          setShowModal(false);
        }, 2000);
      } else {
        // Handle error (e.g., duplicate email/phone)
        alert(data.message || 'Failed to join waitlist. Please try again.');
      }
    } catch (error) {
      console.error('Error joining waitlist:', error);
      alert('An error occurred. Please try again later.');
    }
  } else {
    alert('Please fill in all fields');
  }
};
```

---

## Testing with cURL

### Join Waitlist
```bash
curl -X POST http://localhost:8080/api/waitlist/join \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phone": "+919876543210"
  }'
```

### Get Count
```bash
curl http://localhost:8080/api/waitlist/count
```

### Get All Entries (requires authentication)
```bash
curl http://localhost:8080/api/waitlist/all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Validation Rules

- **Name:** Required, cannot be blank
- **Email:** Required, must be valid email format, must be unique
- **Phone:** Required, must match pattern `^[+]?[0-9]{10,15}$`, must be unique

---

## MongoDB Collection

The data is stored in the `waitlist` collection with the following structure:

```javascript
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+919876543210",
  "createdAt": ISODate("2025-10-15T18:16:13.000Z"),
  "updatedAt": ISODate("2025-10-15T18:16:13.000Z"),
  "_class": "com.postkar.project3dmodel.entity.Waitlist"
}
```

Indexes are automatically created on `email` and `phone` fields to ensure uniqueness.

---

## CORS Configuration

The API is configured to accept requests from any origin (`*`). The waitlist endpoints are publicly accessible without authentication.

---

## Next Steps

1. Start your Spring Boot application
2. Update your React frontend to use the API endpoint
3. Test the integration
4. Monitor the waitlist entries in MongoDB

For production deployment, update the API URL in your React app to point to your production backend URL.
