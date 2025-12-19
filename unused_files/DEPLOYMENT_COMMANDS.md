# Deployment Commands - Run These in Terminal

## Step 1: Rebuild Only the Fraud Service Container
```bash
cd /Users/abdelhakmirbah/Desktop/EMSI/s9/PFAProject/MonProjetPFA

# Rebuild and restart only fraud-service (keeps DB and Kafka running)
docker-compose up -d --build fraud-service
```

## Step 2: Wait for Service to Start
```bash
# Watch the logs to see when it's ready
docker-compose logs -f fraud-service

# Look for this line:
# "✅ ONNX model loaded successfully!"
# Then press Ctrl+C to exit logs
```

## Step 3: Test the New API Endpoints
```bash
# Test records endpoint
curl http://localhost:8081/api/v1/records

# Test statistics endpoint
curl http://localhost:8081/api/v1/records/stats
```

## Step 4: Open the Viewer
```bash
# Open the data viewer in your browser
open viewer.html
```

---

## What Was Added:

### New Spring Boot Endpoints:
- `GET /api/v1/records` - Fetch all fraud check records (last 50)
- `GET /api/v1/records/stats` - Get statistics (total, high risk, low risk, avg score)

### Files Created:
- `RecordsController.java` - New REST controller for data access
- `viewer.html` - Beautiful data visualization page
- `dashboard.html` - Fraud check testing interface

### Files Modified:
- `FraudCheckRepository.java` - Added method to fetch last 50 records
- `CorsConfig.java` - Enabled CORS for browser access

---

## All Your Web Interfaces:

1. **Dashboard** (Test Fraud Detection):
   - File: `dashboard.html`
   - Use: Submit transactions and see fraud scores

2. **Viewer** (See Database Records):
   - File: `viewer.html`  
   - Use: View all stored fraud checks with statistics

3. **Adminer** (Database Admin):
   - URL: http://localhost:8082
   - Use: Full database management

4. **Kafka UI** (Message Queue):
   - URL: http://localhost:8083
   - Use: Monitor Kafka messages

---

## Quick Test:

After rebuilding, test everything works:

```bash
# 1. Submit a fraud check
curl -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{"amount":181,"oldBalance":181,"newBalance":0,"ip":"1.2.3.4","email":"test@test.com"}'

# 2. View the records
curl http://localhost:8081/api/v1/records

# 3. Check statistics
curl http://localhost:8081/api/v1/records/stats
```

All done! 🚀
