# 🔌 Explorer API Documentation

## Overview
The Explorer API provides endpoints for the AR experience discovery screen, allowing users to browse categories, subcategories, and launch AR experiences.

## Base URL
```
/api/v1/explorer
```

## Authentication
All Explorer endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

## Endpoints

### 1. Get All Categories
**GET** `/api/v1/explorer/categories`

Returns all categories for the Explorer main screen grid.

**Response:**
```json
[
  {
    "id": "64f1b2c3d4e5f6789abcdef0",
    "name": "Paisa Bolta Hai",
    "description": "Discover AR stories hidden in everyday money",
    "thumbnailUrl": "https://cdn.app.com/explorer/currency.png",
    "itemCount": 7,
    "isFeatured": true,
    "status": "available"
  },
  {
    "id": "64f1b2c3d4e5f6789abcdef1",
    "name": "Famous Paintings",
    "description": "Art comes alive in AR",
    "thumbnailUrl": "https://cdn.app.com/explorer/paintings.png",
    "itemCount": 0,
    "isFeatured": false,
    "status": "coming_soon"
  }
]
```

### 2. Get Subcategories by Category
**GET** `/api/v1/explorer/categories/{categoryId}/subcategories`

Returns all subcategories (AR experiences) under a specific category.

**Response:**
```json
[
  {
    "id": "marker_id_1",
    "markerId": "10-rupee-note",
    "name": "₹10 Note",
    "description": "The ₹10 note features the Konark Sun Temple...",
    "thumbnailUrl": "https://cdn.app.com/explorer/10note.png",
    "categoryId": "64f1b2c3d4e5f6789abcdef0",
    "categoryName": "Paisa Bolta Hai"
  }
]
```

### 3. Get Subcategory Detail
**GET** `/api/v1/explorer/subcategories/{subcategoryId}`

Returns detailed information for a specific subcategory.

**Response:**
```json
{
  "id": "marker_id_1",
  "markerId": "10-rupee-note",
  "title": "₹10 Note Experience",
  "description": "The ₹10 note features the Konark Sun Temple, a 13th-century CE Sun temple...",
  "thumbnailUrl": "https://cdn.app.com/explorer/10note.png",
  "mediaPreviewUrl": "https://cdn.app.com/ar/10note_preview.mp4",
  "arAssetUrl": "https://cdn.app.com/ar/10note_scene.mp4",
  "triggerMarkerUrl": "https://cdn.app.com/markers/10note_marker.png",
  "categoryId": "64f1b2c3d4e5f6789abcdef0",
  "categoryName": "Paisa Bolta Hai",
  "isActive": true
}
```

### 4. Get AR Preview Information
**GET** `/api/v1/explorer/subcategories/{subcategoryId}/ar-preview`

Returns AR setup information for scanning.

**Response:**
```json
{
  "markerId": "10-rupee-note",
  "arAssetUrl": "https://cdn.app.com/ar/10note_scene.mp4",
  "instructions": "Point your camera at the ₹10 Note to reveal AR content",
  "fallbackPreviewUrl": "https://cdn.app.com/ar/10note_preview.mp4",
  "markerImageUrl": "https://cdn.app.com/markers/10note_marker.png",
  "physicalWidthMeters": 0.117
}
```

### 5. Search
**GET** `/api/v1/explorer/search?q={query}`

Search across categories and subcategories.

**Parameters:**
- `q` (required): Search query string

**Response:**
```json
{
  "query": "currency",
  "totalResults": 8,
  "categories": [
    {
      "id": "64f1b2c3d4e5f6789abcdef0",
      "name": "Paisa Bolta Hai",
      "description": "Discover AR stories hidden in everyday money",
      "thumbnailUrl": "https://cdn.app.com/explorer/currency.png",
      "itemCount": 7,
      "isFeatured": true,
      "status": "available"
    }
  ],
  "subcategories": [
    {
      "id": "marker_id_1",
      "markerId": "10-rupee-note",
      "name": "₹10 Note",
      "description": "The ₹10 note features...",
      "thumbnailUrl": "https://cdn.app.com/explorer/10note.png",
      "categoryId": "64f1b2c3d4e5f6789abcdef0",
      "categoryName": "Paisa Bolta Hai"
    }
  ]
}
```

### 6. Get Featured Content
**GET** `/api/v1/explorer/featured`

Returns featured categories for the carousel.

**Response:**
```json
[
  {
    "id": "64f1b2c3d4e5f6789abcdef0",
    "name": "Paisa Bolta Hai",
    "description": "Discover AR stories hidden in everyday money",
    "thumbnailUrl": "https://cdn.app.com/explorer/currency.png",
    "itemCount": 7,
    "isFeatured": true,
    "status": "available"
  }
]
```

### 7. Track Analytics
**POST** `/api/v1/explorer/analytics/interaction`

Track user interactions for analytics.

**Request Body:**
```json
{
  "eventType": "category_view",
  "categoryId": "64f1b2c3d4e5f6789abcdef0",
  "subcategoryId": null,
  "searchQuery": null,
  "userId": "user_123",
  "timestamp": 1692454800000,
  "platform": "android",
  "appVersion": "1.0.0"
}
```

**Event Types:**
- `category_view`: User viewed a category
- `subcategory_view`: User viewed a subcategory detail
- `ar_preview`: User launched AR preview
- `search`: User performed a search

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Subcategory not found"
}
```

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["eventType is required"]
}
```

## Implementation Notes

1. **Signed URLs**: All asset URLs are pre-signed and valid for 24 hours
2. **Caching**: Categories can be cached locally, subcategories should be fetched fresh
3. **Pagination**: Not implemented yet, but can be added if needed
4. **Rate Limiting**: Consider implementing rate limiting for search and analytics endpoints
5. **Performance**: Use CDN for all static assets (thumbnails, videos, markers)

## Mobile Integration Flow

### Explorer Screen Flow:
1. **App Launch** → `GET /categories` → Display grid
2. **Category Tap** → `GET /categories/{id}/subcategories` → Display list
3. **Subcategory Tap** → `GET /subcategories/{id}` → Display detail
4. **Preview in AR** → `GET /subcategories/{id}/ar-preview` → Launch AR scanner
5. **Track Analytics** → `POST /analytics/interaction` → Log user behavior

### Search Flow:
1. **User Types** → `GET /search?q=query` → Display results
2. **Result Tap** → Follow normal category/subcategory flow

### Featured Content:
1. **App Launch** → `GET /featured` → Display carousel
2. **Featured Item Tap** → Follow normal category flow
