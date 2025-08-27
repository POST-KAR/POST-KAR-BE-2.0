# AR Marker App - API Documentation

## App Flow Overview

Your app follows this user journey:
1. **Category Selection Screen** → Shows all categories
2. **Marker Selection Screen** → Shows markers in selected category  
3. **Marker Details Screen** → Shows marker info with "View" button
4. **Scanner Screen** → Scans marker and plays video

## API Endpoints

### 1. Category Selection Screen

**Get All Categories**
```
GET /api/categories
```

**Response:**
```json
[
  {
    "id": "64f1b2c3d4e5f6789abcdef0",
    "name": "Animals",
    "description": "Animal markers collection",
    "markerCount": 5
  },
  {
    "id": "64f1b2c3d4e5f6789abcdef1", 
    "name": "Vehicles",
    "description": "Vehicle markers collection",
    "markerCount": 3
  }
]
```

### 2. Marker Selection Screen

**Get Category with Markers**
```
GET /api/categories/{categoryId}/markers
```

**Response:**
```json
{
  "id": "64f1b2c3d4e5f6789abcdef0",
  "name": "Animals",
  "description": "Animal markers collection",
  "markers": [
    {
      "id": "marker1",
      "markerId": "A01",
      "name": "Lion",
      "description": "African Lion marker",
      "thumbnailUrl": "https://signed-url-for-thumbnail.jpg",
      "isActive": true
    },
    {
      "id": "marker2", 
      "markerId": "A02",
      "name": "Elephant",
      "description": "African Elephant marker",
      "thumbnailUrl": "https://signed-url-for-thumbnail.jpg",
      "isActive": true
    }
  ]
}
```

### 3. Marker Details Screen

**Get Single Marker**
```
GET /api/markers/{markerId}
```

**Response:**
```json
{
  "id": "marker1",
  "markerId": "A01",
  "name": "Lion",
  "description": "African Lion marker with roaring animation",
  "physicalWidthMeters": 0.1,
  "markerImageUrl": "https://signed-url-for-marker-image.png",
  "thumbnailUrl": "https://signed-url-for-thumbnail.jpg",
  "videos": [
    {
      "id": "video1",
      "name": "Lion Roaring",
      "videoUrl": "https://signed-url-for-video.mp4",
      "format": "mp4",
      "durationSeconds": 30
    }
  ],
  "activeVideoId": "video1",
  "categoryId": "64f1b2c3d4e5f6789abcdef0",
  "isActive": true
}
```

### 4. Scanner Screen - Video Playback

**Get Active Video for Marker**
```
GET /api/markers/{markerId}/activeVideo
```

**Response:**
```json
{
  "id": "video1",
  "name": "Lion Roaring",
  "videoUrl": "https://signed-url-for-video.mp4",
  "variants": [
    {
      "quality": "720p",
      "url": "https://signed-url-720p.mp4"
    },
    {
      "quality": "1080p", 
      "url": "https://signed-url-1080p.mp4"
    }
  ],
  "format": "mp4",
  "durationSeconds": 30,
  "fileSizeBytes": 15728640
}
```

## Additional Useful Endpoints

### Check if Marker Exists
```
GET /api/markers/{markerId}/exists
```
Returns: `true` or `false`

### Get AR Database Version
```
GET /api/markers/version
```
Returns current .imgdb version for ARCore

### Get Updated Markers (for sync)
```
GET /api/markers/updated-since?since=2025-08-19T10:30:00
```
Returns markers updated after timestamp

## Error Handling

All endpoints return standard HTTP status codes:
- `200 OK` - Success
- `404 Not Found` - Resource not found
- `400 Bad Request` - Invalid parameters
- `500 Internal Server Error` - Server error

## Important Notes

1. **Signed URLs**: All image and video URLs are signed and valid for 24 hours
2. **Marker IDs**: Use `markerId` (business ID like "A01") for scanning, not database `id`
3. **Active Status**: Only active markers are returned in category listings
4. **Caching**: Consider caching category list as it changes infrequently

## Implementation Tips

1. **Category Screen**: Cache the categories list, refresh periodically
2. **Marker Selection**: Show thumbnails in a grid layout
3. **Scanner Integration**: Use `markerId` to identify scanned markers
4. **Video Playback**: Handle different video formats and qualities
5. **Error Handling**: Show user-friendly messages for network errors

## Base URL
```
https://your-api-domain.com
```

Replace with your actual API domain when deploying.
