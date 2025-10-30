# POST-KAR Backend - Cloudflare R2 Storage

## 🎯 Overview

This project uses **Cloudflare R2** for object storage instead of AWS S3. R2 is S3-compatible and offers zero egress fees, making it cost-effective for serving AR content and media files.

## 🚀 Quick Start

### 1. Get R2 Credentials

1. Sign up at [cloudflare.com](https://cloudflare.com)
2. Enable R2 in your dashboard
3. Create a bucket (e.g., `postkar-media`)
4. Generate API tokens with "Object Read & Write" permissions
5. Note your **Account ID**, **Access Key ID**, and **Secret Access Key**

### 2. Configure Application

```bash
# Copy example configuration
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit application.properties and add your R2 credentials
```

```properties
cloudflare.r2.account-id=YOUR_ACCOUNT_ID
cloudflare.r2.access-key-id=YOUR_ACCESS_KEY_ID
cloudflare.r2.secret-access-key=YOUR_SECRET_ACCESS_KEY
cloudflare.r2.bucket=postkar-media
```

### 3. Run Application

```bash
./mvnw spring-boot:run
```

Look for successful initialization:
```
✅ Cloudflare R2 client initialized successfully for account: YOUR_ACCOUNT_ID
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| **[CLOUDFLARE_R2_SETUP.md](CLOUDFLARE_R2_SETUP.md)** | Complete setup guide with step-by-step instructions |
| **[R2_DEPLOYMENT_CHECKLIST.md](R2_DEPLOYMENT_CHECKLIST.md)** | Comprehensive deployment checklist |
| **[R2_QUICK_REFERENCE.md](R2_QUICK_REFERENCE.md)** | Quick reference for developers |
| **[MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** | Summary of changes from S3 to R2 |
| **[application.properties.example](src/main/resources/application.properties.example)** | Configuration template |

## 🏗️ Architecture

### Storage Service Layer

```
┌─────────────────────────────────────────┐
│         AdminController                  │
│    (File Upload Endpoints)              │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│       FileUploadService                  │
│  - Upload files to R2                   │
│  - Generate file URLs                   │
│  - Download/Delete files                │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│      SignedUrlService                    │
│  - Generate presigned URLs              │
│  - URL expiry management                │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│     CloudflareR2Config                   │
│  - S3Client (R2 endpoint)               │
│  - S3Presigner (R2 endpoint)            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Cloudflare R2                    │
│  - Object storage                       │
│  - Global CDN                           │
│  - Zero egress fees                     │
└─────────────────────────────────────────┘
```

### File Organization

```
R2 Bucket: postkar-media
├── markers/
│   └── 2025/01/30/
│       ├── abc123_product1.jpg
│       └── def456_product2.jpg
├── thumbnails/
│   └── 2025/01/30/
│       └── abc123_thumb.jpg
├── videos/
│   └── 2025/01/30/
│       └── abc123_demo.mp4
└── imgdb/
    └── database_v1.imgdb
```

## 🔌 API Endpoints

### Upload Files
```http
POST /api/admin/upload
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

Parameters:
- markerImage: File (optional)
- thumbnail: File (optional)
- video: File (optional)
- categoryId: String (optional)
```

**Response**:
```json
{
  "markerImageUrl": "https://postkar-media.abc123.r2.cloudflarestorage.com/markers/...",
  "thumbnailUrl": "https://postkar-media.abc123.r2.cloudflarestorage.com/thumbnails/...",
  "videoUrl": "https://postkar-media.abc123.r2.cloudflarestorage.com/videos/..."
}
```

## 💰 Cost Comparison

### Cloudflare R2 (Current)
```
Storage:    100 GB × $0.015/GB    = $1.50
Writes:     1M × $4.50/M          = $4.50
Reads:      10M × $0.36/M         = $3.60
Egress:     ∞ × $0                = $0.00
────────────────────────────────────────
TOTAL:                             $9.60/month
```

### AWS S3 (Previous)
```
Storage:    100 GB × $0.023/GB    = $2.30
Writes:     1M × $5.00/M          = $5.00
Reads:      10M × $0.40/M         = $4.00
Egress:     100 GB × $0.09/GB     = $9.00
────────────────────────────────────────
TOTAL:                             $20.30/month
```

**Savings**: ~52% reduction in costs! 💸

## ✨ Key Features

### Zero Egress Fees
- No charges for data transfer out
- Perfect for serving AR content and videos
- Unlimited bandwidth at no extra cost

### S3-Compatible API
- Uses standard AWS SDK
- No vendor lock-in
- Easy migration from S3

### Global CDN
- Cloudflare's edge network
- Low latency worldwide
- Automatic caching

### Presigned URLs
- Secure access to private files
- Configurable expiry (default: 24 hours)
- No public bucket required

### Backward Compatible
- Supports legacy S3 URLs
- Seamless migration
- No database updates required

## 🔒 Security

### Best Practices
- ✅ Never commit credentials to Git
- ✅ Use environment variables in production
- ✅ Rotate API tokens every 90 days
- ✅ Use presigned URLs for sensitive content
- ✅ Enable bucket versioning
- ✅ Monitor access logs

### Production Configuration
```bash
# Set environment variables
export CLOUDFLARE_R2_ACCOUNT_ID=your_account_id
export CLOUDFLARE_R2_ACCESS_KEY_ID=your_access_key
export CLOUDFLARE_R2_SECRET_ACCESS_KEY=your_secret_key
export CLOUDFLARE_R2_BUCKET=your_bucket_name
```

## 🧪 Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Test
```bash
# Upload test file
curl -X POST http://localhost:8080/api/admin/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "markerImage=@test-image.jpg"

# Verify in R2 dashboard
# Access the returned URL in browser
```

## 🚀 Deployment

### Development
```bash
./mvnw spring-boot:run
```

### Production (JAR)
```bash
./mvnw clean package
java -jar target/project3dmodel-0.0.1-SNAPSHOT.jar
```

### Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📊 Monitoring

### Cloudflare Dashboard
- Storage usage
- Request metrics
- Bandwidth usage
- Cost tracking

### Application Logs
```bash
# Watch for R2 initialization
tail -f logs/application.log | grep "R2"

# Monitor uploads
tail -f logs/application.log | grep "uploaded successfully"
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Failed to initialize R2 client
```
Solution: Verify account ID and credentials in application.properties
```

**Issue**: Access Denied on upload
```
Solution: Check API token has "Object Read & Write" permissions
```

**Issue**: Files upload but return 404
```
Solution: Enable public access on bucket OR use presigned URLs (already implemented)
```

**Issue**: Custom domain not working
```
Solution: Check DNS propagation with: nslookup cdn.yourdomain.com
```

See **[CLOUDFLARE_R2_SETUP.md](CLOUDFLARE_R2_SETUP.md)** for detailed troubleshooting.

## 🔄 Migration from S3

If migrating from AWS S3:

1. **Create R2 bucket** and get credentials
2. **Update configuration** with R2 properties
3. **Sync existing files** (optional):
   ```bash
   aws s3 sync s3://old-bucket/ s3://new-bucket/ \
     --endpoint-url https://ACCOUNT_ID.r2.cloudflarestorage.com
   ```
4. **Deploy updated application**
5. **Test thoroughly**

The application supports legacy S3 URLs, so no database updates are required!

## 📞 Support

- **Documentation**: See docs listed above
- **Cloudflare R2 Docs**: https://developers.cloudflare.com/r2/
- **S3 API Reference**: https://developers.cloudflare.com/r2/api/s3/api/
- **Community**: https://community.cloudflare.com/

## 🎯 Next Steps

1. ✅ Review [CLOUDFLARE_R2_SETUP.md](CLOUDFLARE_R2_SETUP.md)
2. ✅ Follow [R2_DEPLOYMENT_CHECKLIST.md](R2_DEPLOYMENT_CHECKLIST.md)
3. ✅ Configure your R2 credentials
4. ✅ Test file uploads
5. ✅ Deploy to production

## 📝 License

[Your License Here]

## 👥 Contributors

[Your Team Here]

---

**Built with ❤️ using Cloudflare R2**

**Last Updated**: January 2025  
**Version**: 2.0 (R2 Migration)  
**Status**: Production Ready ✅
