# Mobile Number + OTP Authentication System

This project has been updated to use **mobile number + OTP authentication** instead of the traditional email + password system. Users can now register and login using only their mobile number with OTP verification via MSG91.

## 🔄 Authentication Flow

### For New Users (Registration)
1. **Send OTP**: User enters mobile number → System sends OTP via MSG91
2. **Verify OTP**: User enters OTP → System verifies and creates account + logs in user
3. **Access Granted**: User receives JWT tokens and can access protected endpoints

### For Existing Users (Login)
1. **Send OTP**: User enters mobile number → System sends OTP via MSG91
2. **Verify OTP**: User enters OTP → System verifies and logs in user
3. **Access Granted**: User receives JWT tokens and can access protected endpoints

## 🛠️ Setup Instructions

### 1. MSG91 Configuration

Add the following environment variables or update `application.properties`:

```properties
# MSG91 Configuration
msg91.auth.key=YOUR_MSG91_AUTH_KEY
msg91.template.id=YOUR_MSG91_TEMPLATE_ID
msg91.sender.id=YOUR_SENDER_ID

# JWT Configuration
jwt.secret=your_jwt_secret_key_here
jwt.access.expiration=86400000
jwt.refresh.expiration=604800000

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/project3dmodel
```

### 2. MSG91 Account Setup

1. Create an account at [MSG91](https://msg91.com/)
2. Get your **Auth Key** from the dashboard
3. Create an **OTP Template** and note the Template ID
4. Configure your **Sender ID** (default: MSGIND)

### 3. Database Migration

The User entity has been simplified to only require:
- `phoneNumber` (primary identifier)
- `mobileVerified` (boolean)
- `isActive` (boolean)
- `otp` and `otpGeneratedAt` (for OTP verification)
- JWT token fields

**Note**: Existing users with email-based accounts will need to re-register with their mobile numbers.

## 📱 API Endpoints

### Authentication Endpoints

#### 1. Send OTP
```http
POST /api/auth/send-otp
Content-Type: application/json

{
    "mobileNumber": "+1234567890"
}
```

**Response:**
```json
{
    "message": "OTP sent to your mobile number for registration/login",
    "identifier": "+1234567890",
    "success": true
}
```

#### 2. Verify OTP (Login/Register)
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
    "mobileNumber": "+1234567890",
    "otp": "123456"
}
```

**Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "mobileNumber": "+1234567890",
        "isActive": true,
        "mobileVerified": true
    },
    "message": "Login successful" // or "Registration completed and logged in"
}
```

#### 3. Resend OTP
```http
POST /api/auth/resend-otp
Content-Type: application/json

{
    "mobileNumber": "+1234567890"
}
```

#### 4. Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json
Authorization: Bearer <refresh_token>

{
    "email": "+1234567890",  // Note: using email field for mobile number
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 🔒 Security Features

- **OTP Expiry**: OTPs expire after 10 minutes
- **JWT Tokens**: Access tokens for API authentication
- **Refresh Tokens**: 7-day expiry for token refresh
- **Rate Limiting**: Built-in protection against spam
- **Mobile Verification**: Users must verify their mobile number

## 🧪 Testing

### Using Postman/Curl

1. **Register/Login Flow:**
   ```bash
   # Step 1: Send OTP
   curl -X POST http://localhost:8080/api/auth/send-otp \
     -H "Content-Type: application/json" \
     -d '{"mobileNumber": "+1234567890"}'
   
   # Step 2: Verify OTP (check your phone for OTP)
   curl -X POST http://localhost:8080/api/auth/verify-otp \
     -H "Content-Type: application/json" \
     -d '{"mobileNumber": "+1234567890", "otp": "123456"}'
   
   # Step 3: Use the access token for protected endpoints
   curl -X GET http://localhost:8080/api/protected-endpoint \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
   ```

### Mobile Number Format

- Supports international format: `+1234567890`
- Validates mobile number format
- Minimum 10 digits, maximum 15 digits

## 🚀 Deployment

### Environment Variables

Set these environment variables in your deployment:

```bash
MSG91_AUTH_KEY=your_msg91_auth_key
MSG91_TEMPLATE_ID=your_template_id
MSG91_SENDER_ID=your_sender_id
JWT_SECRET=your_jwt_secret
MONGODB_URI=your_mongodb_connection_string
```

### Docker Deployment

```dockerfile
# Add to your Dockerfile
ENV MSG91_AUTH_KEY=${MSG91_AUTH_KEY}
ENV MSG91_TEMPLATE_ID=${MSG91_TEMPLATE_ID}
ENV JWT_SECRET=${JWT_SECRET}
```

## 📋 Migration Notes

### Breaking Changes

1. **Authentication Method**: Changed from email+password to mobile+OTP
2. **User Entity**: Simplified to remove email, username, password, name, DOB fields
3. **Endpoints**: Updated authentication endpoints
4. **JWT Subject**: Now uses mobile number instead of email

### Removed Features

- Email-based registration/login
- Username/password authentication
- Profile completion steps
- Facebook/Google OAuth (can be re-added if needed)

### Backward Compatibility

This is a **breaking change**. Existing users will need to:
1. Re-register using their mobile number
2. Update mobile apps to use new authentication flow
3. Update any stored tokens (they will be invalid)

## 🔧 Troubleshooting

### Common Issues

1. **OTP Not Received**
   - Check MSG91 account balance
   - Verify template ID and auth key
   - Check mobile number format

2. **JWT Token Issues**
   - Ensure JWT secret is properly configured
   - Check token expiry times
   - Verify mobile number in token subject

3. **Database Issues**
   - Ensure MongoDB is running
   - Check connection string
   - Verify User collection structure

### Logs

Enable debug logging:
```properties
logging.level.com.postkar.project3dmodel=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 📞 Support

For issues related to:
- **MSG91 Integration**: Check MSG91 documentation
- **JWT Tokens**: Verify configuration and expiry settings
- **Mobile Number Validation**: Check regex patterns in DTOs
- **Database**: Ensure MongoDB connection and User entity structure

## 🎯 Next Steps

1. Test the authentication flow thoroughly
2. Update mobile applications to use new endpoints
3. Configure MSG91 account and templates
4. Set up monitoring for OTP delivery rates
5. Consider adding rate limiting for OTP requests
