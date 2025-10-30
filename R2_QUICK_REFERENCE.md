# Cloudflare R2 Quick Reference Card

## 🔑 Configuration Properties

```properties
# Required
cloudflare.r2.account-id=abc123def456
cloudflare.r2.access-key-id=your_access_key
cloudflare.r2.secret-access-key=your_secret_key
cloudflare.r2.bucket=postkar-media

# Optional
cloudflare.r2.endpoint=                    # Leave empty for default
cloudflare.r2.public-url=                  # Custom domain URL
signed.url.expiry.hours=24                 # Presigned URL expiry
app.signed-url-expiry-minutes=60           # Asset URL expiry
```

---

## 🌐 URL Formats

### Default R2 URL
```
https://<bucket>.<account-id>.r2.cloudflarestorage.com/<key>
```
**Example**:
```
https://postkar-media.abc123.r2.cloudflarestorage.com/markers/2025/01/30/xyz.jpg
```

### Custom Domain URL
```
https://<your-domain>/<key>
```
**Example**:
```
https://cdn.postkar.com/markers/2025/01/30/xyz.jpg
```

### Presigned URL (Private Buckets)
```
https://<bucket>.<account-id>.r2.cloudflarestorage.com/<key>?X-Amz-Algorithm=...
```

---

## 📁 File Organization

### By Date (Default)
```
bucket/
├── markers/2025/01/30/abc123_product.jpg
├── thumbnails/2025/01/30/abc123_thumb.jpg
└── videos/2025/01/30/abc123_demo.mp4
```

### By Category
```
bucket/
├── electronics/
│   ├── markers/smartphone.jpg
│   ├── thumbnails/smartphone.jpg
│   └── videos/smartphone_demo.mp4
└── furniture/
    ├── markers/chair.jpg
    └── videos/chair_demo.mp4
```

---

## 🔧 Common Commands

### Upload File via API
```bash
curl -X POST http://localhost:8080/api/admin/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "markerImage=@image.jpg" \
  -F "categoryId=CATEGORY_ID"
```

### Sync from S3 to R2
```bash
aws s3 sync s3://old-bucket/ s3://new-bucket/ \
  --endpoint-url https://ACCOUNT_ID.r2.cloudflarestorage.com
```

### List R2 Bucket Contents
```bash
aws s3 ls s3://your-bucket/ \
  --endpoint-url https://ACCOUNT_ID.r2.cloudflarestorage.com \
  --recursive
```

### Delete File from R2
```bash
aws s3 rm s3://your-bucket/path/to/file.jpg \
  --endpoint-url https://ACCOUNT_ID.r2.cloudflarestorage.com
```

---

## 🎯 Key Classes & Methods

### CloudflareR2Config
```java
@Configuration
public class CloudflareR2Config {
    @Bean S3Client s3Client()      // Configured for R2
    @Bean S3Presigner s3Presigner() // For presigned URLs
}
```

### FileUploadService
```java
// Upload file
String uploadFile(MultipartFile file, String folder)

// Upload by category
String uploadFileByCategory(MultipartFile file, String category, String type)

// Download file
void downloadFile(String fileUrl, File destination)

// Delete file
boolean deleteFile(String fileUrl)
```

### SignedUrlService
```java
// Generate presigned URL (24h default)
String generateSignedUrl(String fileUrl)

// Generate with custom expiry
String generateSignedUrl(String fileUrl, int hours)

// Generate from key
String generateSignedUrlFromKey(String key, int hours)
```

### AssetService
```java
// Upload file
String uploadFile(MultipartFile file, String key)

// Generate signed URL
String generateSignedUrl(String key)

// Compute checksum
String computeChecksum(MultipartFile file)
```

---

## 🔍 Supported URL Formats

The application can parse and handle:

| Format | Example | Status |
|--------|---------|--------|
| R2 URL | `https://bucket.account.r2.cloudflarestorage.com/key` | ✅ Primary |
| Custom Domain | `https://cdn.yourdomain.com/key` | ✅ Supported |
| Legacy S3 | `https://bucket.s3.region.amazonaws.com/key` | ✅ Migration |
| S3 Protocol | `s3://bucket/key` | ✅ Supported |
| R2 Protocol | `r2://bucket/key` | ✅ Supported |

---

## 🚨 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Failed to initialize R2 client" | Check account ID and credentials |
| "Access Denied" on upload | Verify API token has write permissions |
| Files upload but 404 on access | Enable public access or use presigned URLs |
| Custom domain not working | Check DNS propagation (wait 5-15 min) |
| Large files failing | Increase `spring.servlet.multipart.max-file-size` |

---

## 💡 Best Practices

### Security
- ✅ Never commit credentials to Git
- ✅ Use environment variables in production
- ✅ Rotate API tokens every 90 days
- ✅ Use presigned URLs for sensitive content
- ✅ Enable bucket versioning

### Performance
- ✅ Use custom domain for CDN benefits
- ✅ Compress images before upload
- ✅ Set appropriate cache headers
- ✅ Use presigned URLs with reasonable expiry

### Cost Optimization
- ✅ Delete unused files regularly
- ✅ Use lifecycle policies for old data
- ✅ Monitor storage usage in dashboard
- ✅ Optimize file sizes before upload

---

## 📊 Pricing (2025)

| Resource | Free Tier | Paid Tier |
|----------|-----------|-----------|
| Storage | 10 GB/month | $0.015/GB/month |
| Class A Ops (write) | 1M/month | $4.50/million |
| Class B Ops (read) | 10M/month | $0.36/million |
| Egress | **∞ FREE** | **∞ FREE** |

**Key Advantage**: Zero egress fees = massive savings!

---

## 🔗 Quick Links

- **R2 Dashboard**: https://dash.cloudflare.com/?to=/:account/r2
- **API Docs**: https://developers.cloudflare.com/r2/api/
- **S3 Compatibility**: https://developers.cloudflare.com/r2/api/s3/api/
- **Pricing**: https://developers.cloudflare.com/r2/pricing/

---

## 📝 File Size Limits

| File Type | Max Size | Property |
|-----------|----------|----------|
| Marker Image | 5 MB | `ALLOWED_IMAGE_TYPES` |
| Thumbnail | 5 MB | `ALLOWED_IMAGE_TYPES` |
| Video | 500 MB | `ALLOWED_VIDEO_TYPES` |
| ImgDB | 100 MB | Custom validation |
| Default | 10 MB | Fallback |

To change: Update `spring.servlet.multipart.max-file-size` in `application.properties`

---

## 🎨 Allowed File Types

### Images
- `image/jpeg`, `image/jpg`
- `image/png`
- `image/gif`
- `image/webp`

### Videos
- `video/mp4`
- `video/mpeg`
- `video/quicktime`
- `video/x-msvideo`

---

## 🧪 Testing Endpoints

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Upload Test
```bash
curl -X POST http://localhost:8080/api/admin/upload \
  -H "Authorization: Bearer TOKEN" \
  -F "markerImage=@test.jpg"
```

### Get Marker (with signed URLs)
```bash
curl http://localhost:8080/api/markers/MARKER_ID
```

---

## 📱 Integration Examples

### JavaScript/TypeScript
```typescript
const uploadFile = async (file: File, categoryId: string) => {
  const formData = new FormData();
  formData.append('markerImage', file);
  formData.append('categoryId', categoryId);
  
  const response = await fetch('/api/admin/upload', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  });
  
  return response.json();
};
```

### Java/Android
```java
MultipartBody.Part filePart = MultipartBody.Part.createFormData(
    "markerImage", 
    file.getName(),
    RequestBody.create(MediaType.parse("image/*"), file)
);

Call<UploadResponse> call = apiService.uploadFile(filePart, categoryId);
```

### Python
```python
import requests

files = {'markerImage': open('image.jpg', 'rb')}
data = {'categoryId': 'category_id'}
headers = {'Authorization': f'Bearer {token}'}

response = requests.post(
    'http://localhost:8080/api/admin/upload',
    files=files,
    data=data,
    headers=headers
)
```

---

## 🎯 Migration Checklist

- [ ] Create R2 bucket
- [ ] Generate API tokens
- [ ] Update `application.properties`
- [ ] Test file upload
- [ ] Test file download
- [ ] Migrate existing files (if any)
- [ ] Update DNS (if using custom domain)
- [ ] Monitor logs for errors
- [ ] Verify costs in Cloudflare dashboard

---

**Last Updated**: January 2025  
**Version**: 1.0  
**Status**: Production Ready ✅
