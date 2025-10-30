# Cloudflare R2 Deployment Checklist

## ✅ Pre-Deployment Setup

### 1. Cloudflare R2 Account Setup
- [ ] Sign up for Cloudflare account (if not already done)
- [ ] Enable R2 in Cloudflare dashboard
- [ ] Note your **Account ID** (found in R2 dashboard)

### 2. Create R2 Bucket
- [ ] Go to R2 in Cloudflare dashboard
- [ ] Click "Create bucket"
- [ ] Name your bucket (e.g., `postkar-media`)
- [ ] Choose location hint (optional)
- [ ] Create bucket

### 3. Generate API Tokens
- [ ] Go to "Manage R2 API Tokens"
- [ ] Click "Create API token"
- [ ] Set permissions: **Object Read & Write**
- [ ] Choose bucket scope (specific bucket or all)
- [ ] Create token
- [ ] **SAVE IMMEDIATELY**:
  - Access Key ID
  - Secret Access Key
  - Account ID

### 4. Configure Public Access (Choose One)

#### Option A: Public Bucket (Recommended for public content)
- [ ] Go to bucket settings
- [ ] Enable "Public Access"
- [ ] Confirm

#### Option B: Private Bucket with Presigned URLs (More secure)
- [ ] Keep bucket private (default)
- [ ] Application will auto-generate presigned URLs
- [ ] No additional setup needed

### 5. Custom Domain (Optional)
- [ ] Go to bucket settings → Custom Domains
- [ ] Click "Connect Domain"
- [ ] Enter domain (e.g., `cdn.postkar.com`)
- [ ] Follow DNS setup instructions
- [ ] Wait for DNS propagation (5-15 minutes)

---

## 🔧 Application Configuration

### 1. Create Configuration File
```bash
cd src/main/resources
cp application.properties.example application.properties
```

### 2. Update application.properties

Replace these values:
```properties
# ============================================
# Cloudflare R2 Storage Configuration
# ============================================
cloudflare.r2.account-id=YOUR_ACCOUNT_ID_HERE
cloudflare.r2.access-key-id=YOUR_ACCESS_KEY_ID_HERE
cloudflare.r2.secret-access-key=YOUR_SECRET_ACCESS_KEY_HERE
cloudflare.r2.bucket=postkar-media

# Optional: Custom endpoint (leave empty for default)
cloudflare.r2.endpoint=

# Optional: Custom domain (if configured in step 5)
cloudflare.r2.public-url=https://cdn.postkar.com

# Signed URL expiry
signed.url.expiry.hours=24
app.signed-url-expiry-minutes=60
```

### 3. Verify Other Required Properties
Ensure these are also configured:
```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/postkar

# JWT
jwt.secret=YOUR_JWT_SECRET_KEY_HERE
jwt.expiration=86400000

# Email (if using)
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

---

## 🏗️ Build & Deploy

### 1. Clean Build
```bash
./mvnw clean package
```
**Expected**: Build SUCCESS

### 2. Run Tests (if available)
```bash
./mvnw test
```

### 3. Start Application
```bash
./mvnw spring-boot:run
```

**Check logs for**:
```
Cloudflare R2 client initialized successfully for account: YOUR_ACCOUNT_ID
Cloudflare R2 Presigner initialized successfully for account: YOUR_ACCOUNT_ID
```

---

## 🧪 Testing

### 1. Test File Upload
```bash
curl -X POST http://localhost:8080/api/admin/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "markerImage=@test-image.jpg" \
  -F "categoryId=YOUR_CATEGORY_ID"
```

**Expected Response**:
```json
{
  "markerImageUrl": "https://postkar-media.ACCOUNT_ID.r2.cloudflarestorage.com/..."
}
```

### 2. Verify File in R2 Dashboard
- [ ] Go to R2 bucket in Cloudflare
- [ ] Navigate to uploaded file path
- [ ] Verify file exists

### 3. Test File Access
- [ ] Copy URL from upload response
- [ ] Open in browser
- [ ] Verify file loads correctly

### 4. Test Presigned URLs (if private bucket)
```bash
curl http://localhost:8080/api/markers/YOUR_MARKER_ID
```
**Check**: URLs should have query parameters like `?X-Amz-Algorithm=...`

### 5. Test All Upload Types
- [ ] Marker image upload
- [ ] Thumbnail upload
- [ ] Video upload
- [ ] Category-based upload

---

## 🔄 Migration from AWS S3 (If Applicable)

### 1. Install AWS CLI
```bash
# Windows
choco install awscli

# Mac
brew install awscli

# Linux
sudo apt install awscli
```

### 2. Configure AWS CLI for R2
```bash
aws configure
# AWS Access Key ID: YOUR_R2_ACCESS_KEY_ID
# AWS Secret Access Key: YOUR_R2_SECRET_ACCESS_KEY
# Default region name: auto
# Default output format: json
```

### 3. Sync Files from S3 to R2
```bash
aws s3 sync s3://your-old-s3-bucket/ s3://your-r2-bucket/ \
  --endpoint-url https://YOUR_ACCOUNT_ID.r2.cloudflarestorage.com \
  --source-region us-east-1
```

### 4. Verify Migration
```bash
aws s3 ls s3://your-r2-bucket/ \
  --endpoint-url https://YOUR_ACCOUNT_ID.r2.cloudflarestorage.com \
  --recursive
```

### 5. Update Database URLs (Optional)
If you want to update existing URLs in MongoDB:
```javascript
// Connect to MongoDB
use postkar;

// Check current URLs
db.markers.find({}, {markerImageUrl: 1, thumbnailUrl: 1}).limit(5);

// Update URLs (example - adjust pattern as needed)
db.markers.updateMany(
  { markerImageUrl: /amazonaws\.com/ },
  [{
    $set: {
      markerImageUrl: {
        $replaceOne: {
          input: "$markerImageUrl",
          find: ".s3.us-east-1.amazonaws.com",
          replacement: ".YOUR_ACCOUNT_ID.r2.cloudflarestorage.com"
        }
      }
    }
  }]
);
```

**Note**: The application supports legacy S3 URLs, so this step is optional.

---

## 🔒 Security Checklist

- [ ] Never commit `application.properties` to Git
- [ ] Add `application.properties` to `.gitignore`
- [ ] Use environment variables in production
- [ ] Rotate API tokens regularly (every 90 days)
- [ ] Enable bucket versioning for important data
- [ ] Set up CORS policies in R2 dashboard if needed
- [ ] Monitor access logs in Cloudflare Analytics
- [ ] Use presigned URLs for sensitive content

### Environment Variables (Production)
```bash
export CLOUDFLARE_R2_ACCOUNT_ID=your_account_id
export CLOUDFLARE_R2_ACCESS_KEY_ID=your_access_key
export CLOUDFLARE_R2_SECRET_ACCESS_KEY=your_secret_key
export CLOUDFLARE_R2_BUCKET=your_bucket_name
```

Update `application.properties` for production:
```properties
cloudflare.r2.account-id=${CLOUDFLARE_R2_ACCOUNT_ID}
cloudflare.r2.access-key-id=${CLOUDFLARE_R2_ACCESS_KEY_ID}
cloudflare.r2.secret-access-key=${CLOUDFLARE_R2_SECRET_ACCESS_KEY}
cloudflare.r2.bucket=${CLOUDFLARE_R2_BUCKET}
```

---

## 📊 Monitoring

### 1. Cloudflare Dashboard
- [ ] Monitor storage usage
- [ ] Check request metrics
- [ ] Review access logs
- [ ] Monitor costs

### 2. Application Logs
Watch for these log messages:
```
✅ SUCCESS: "Cloudflare R2 client initialized successfully"
✅ SUCCESS: "File uploaded successfully to R2"
❌ ERROR: "Failed to initialize R2 client"
❌ ERROR: "Failed to upload file to R2"
```

### 3. Health Check Endpoint
Create a simple health check:
```bash
curl http://localhost:8080/actuator/health
```

---

## 🐛 Troubleshooting

### Issue: "Failed to initialize R2 client"
**Solution**:
- Verify account ID is correct
- Check API token permissions
- Ensure credentials are not expired

### Issue: "Access Denied" on upload
**Solution**:
- Regenerate API token with "Object Read & Write"
- Verify bucket name is correct
- Check token scope includes your bucket

### Issue: Files upload but return 404
**Solution**:
- Enable public access on bucket, OR
- Use presigned URLs (already implemented)

### Issue: Custom domain not working
**Solution**:
- Check DNS propagation: `nslookup cdn.yourdomain.com`
- Verify SSL certificate is active
- Wait up to 24 hours for full propagation

---

## 📈 Performance Optimization

### 1. Enable Caching
Configure cache headers in R2 dashboard:
```
Cache-Control: public, max-age=31536000
```

### 2. Use Custom Domain with CDN
- Cloudflare automatically provides CDN
- No additional configuration needed

### 3. Optimize File Sizes
- Compress images before upload
- Use appropriate video formats (H.264)
- Consider thumbnail generation

---

## 💰 Cost Estimation

### Example Usage:
- **Storage**: 100 GB
- **Class A Operations** (writes): 1M/month
- **Class B Operations** (reads): 10M/month
- **Egress**: Unlimited (FREE!)

### Monthly Cost:
```
Storage:    100 GB × $0.015 = $1.50
Class A:    1M × $4.50/M   = $4.50
Class B:    10M × $0.36/M  = $3.60
Egress:     ∞ × $0         = $0.00
─────────────────────────────────
TOTAL:                      $9.60/month
```

**Compare to AWS S3**: ~$35-50/month for same usage!

---

## ✅ Final Verification

Before going to production:

- [ ] All tests passing
- [ ] File uploads working
- [ ] File downloads working
- [ ] Presigned URLs working
- [ ] Custom domain working (if configured)
- [ ] Logs show no errors
- [ ] Security checklist completed
- [ ] Monitoring set up
- [ ] Backup strategy in place
- [ ] Team trained on new system

---

## 📞 Support Resources

- **Cloudflare R2 Docs**: https://developers.cloudflare.com/r2/
- **S3 API Compatibility**: https://developers.cloudflare.com/r2/api/s3/api/
- **Cloudflare Community**: https://community.cloudflare.com/
- **Project Documentation**: See `CLOUDFLARE_R2_SETUP.md`

---

## 🎉 Success Criteria

Your deployment is successful when:

1. ✅ Application starts without errors
2. ✅ Files upload to R2 successfully
3. ✅ Uploaded files are accessible via URL
4. ✅ No AWS S3 references in logs
5. ✅ Cost tracking shows R2 usage (not S3)
6. ✅ All existing features work as before

---

**Deployment Date**: _____________  
**Deployed By**: _____________  
**Environment**: [ ] Development [ ] Staging [ ] Production  
**Status**: [ ] In Progress [ ] Complete [ ] Rolled Back

---

**Good luck with your deployment! 🚀**
