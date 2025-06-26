# Banking Service

A comprehensive Spring Boot-based banking service that provides account management and transaction processing capabilities with enterprise-grade features including custom exception handling, transaction audit trails, and comprehensive monitoring.

## 🏗️ Architecture

### Core Components

- **Account Management**: Create and retrieve bank accounts with balance validation
- **Transaction Processing**: Transfer funds between accounts with comprehensive validation
- **Audit Trail**: Complete transaction history including failed transactions
- **Exception Handling**: Custom exception hierarchy with structured error responses
- **Monitoring**: Health checks and service monitoring endpoints

### Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL with Liquibase migrations
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Documentation**: Custom API documentation
- **Monitoring**: Spring Boot Actuator

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Python 3.6+ (for testing scripts)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd bank
   ```

2. **Configure Database**
   ```bash
   # Create PostgreSQL database
   createdb banking_service
   
   # Update application.properties with your database credentials
   spring.datasource.url=jdbc:postgresql://localhost:5432/bank
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8080`

## 📚 API Documentation

### Account Management

#### Create Account
```http
POST /accounts
Content-Type: application/json

{
  "account_id": "ACC_123",
  "initial_balance": 1000.50
}
```

**Response (200 OK):**
```json
{
  "account_id": "ACC_123",
  "initial_balance": 1000.50
}
```

#### Get Account
```http
GET /accounts/{account_id}
```

**Response (200 OK):**
```json
{
  "account_id": "ACC_123",
  "balance": 1000.50
}
```

### Transaction Processing

#### Process Transaction
```http
POST /transactions
Content-Type: application/json

{
  "source_account_id": "ACC_123",
  "destination_account_id": "ACC_456",
  "amount": 100.25
}
```

**Successful Transaction (200 OK):**
```json
{
  "transaction_id": "uuid-string",
  "source_account_id": "ACC_123",
  "destination_account_id": "ACC_456",
  "amount": 100.25,
  "status": "COMPLETED",
  "error_message": null
}
```

**Failed Transaction (200 OK):**
```json
{
  "transaction_id": "uuid-string",
  "source_account_id": "ACC_123",
  "destination_account_id": "ACC_456",
  "amount": 100.25,
  "status": "FAILED",
  "error_message": "Insufficient balance in source account"
}
```

#### Get Transaction
```http
GET /transactions/{transaction_id}
```

### Health Check
```http
GET /test/ping
```

**Response:** `pong`

## 🧪 Testing

### Automated Business Logic Testing

The project includes a comprehensive Python test script (`test_banking_service.py`) that validates all business logic without relying on Swagger/OpenAPI documentation:

```bash
# Install Python dependencies
pip install requests

# Run all tests against default URL (http://localhost:8080)
python3 test_banking_service.py

# Run tests against different URL
python3 test_banking_service.py --url http://localhost:9090
```

### Test Script Features

- **17 Comprehensive Tests** covering all API endpoints
- **Real-time Results** with emoji indicators and timing
- **Detailed Reporting** with success rates and failure analysis
- **Resource Tracking** of created accounts and transactions
- **CI/CD Integration** with proper exit codes
- **Flexible Configuration** with custom URL support

### Test Coverage

The test suite includes **17 comprehensive tests** covering:

#### ✅ Service Health (1 test)
- Health check endpoint validation (`GET /test/ping`)

#### ✅ Account Management (7 tests)
- ✅ Account creation with valid data (positive balance required)
- ✅ Account retrieval by ID
- ❌ Negative balance validation (returns 400 with VALIDATION_ERROR)
- ❌ Zero balance validation (returns 400 with INVALID_BALANCE)
- ❌ Missing account ID validation (returns 400)
- ❌ Duplicate account prevention (returns 409 with ACCOUNT_ALREADY_EXISTS)
- ❌ Account not found scenarios (returns 404)

#### ✅ Transaction Processing (8 tests)
- ✅ Successful transaction processing (returns 200 with COMPLETED status)
- ✅ Transaction retrieval by ID
- ❌ Insufficient balance handling (returns 200 with FAILED status and transaction ID)
- ❌ Negative amount validation (returns 400 with VALIDATION_ERROR)
- ❌ Same account transfer prevention (returns 200 with FAILED status)
- ❌ Source account not found handling (returns 200 with FAILED status)
- ❌ Transaction not found scenarios (returns 500 or 404)
- ✅ Multiple transactions from same account

### Sample Test Output

```
🏦 Banking Service Business Logic Test Suite
============================================================
Test started at: 2025-06-26 18:14:35
Target service: http://localhost:8080

✅ Service Health Check: Service is healthy (0.007s)
✅ Account Creation - Success: Account TEST_ACC_1750941875_9347 created successfully (0.014s)
✅ Transaction Processing - Success: Transaction completed successfully: acbe7651-2fd0-4769-8cc9-96aba049755c (0.022s)
✅ Transaction - Insufficient Balance: Failed transaction recorded: 6b825ee2-fafe-45a3-aa45-5d8dd75c7b7a (0.019s)

============================================================
📊 TEST SUMMARY
============================================================
Total Tests: 17
✅ Passed: 17
❌ Failed: 0
⏭️  Skipped: 0
Success Rate: 100.0%
Total Duration: 0.210s

Test completed at: 2025-06-26 18:14:37
Created test accounts: 2
Created test transactions: 6
```

### Key Testing Features

- **Failed Transaction Recording**: Tests validate that failed transactions are saved to database with complete audit trail
- **Status Code Validation**: Ensures proper HTTP status codes (200, 400, 404, 409)
- **Error Message Validation**: Verifies custom exception handling and error codes
- **Business Rule Testing**: Validates account balance requirements, transaction limits, and validation rules
- **Resource Management**: Tracks and reports all created test data for cleanup

### Java Unit Tests

Run comprehensive Java unit tests:
```bash
mvn test
```

The project includes **23 unit tests** for service layer components:
- **AccountService**: 9 test cases covering account creation, validation, and retrieval
- **TransactionService**: 14 test cases with Mockito integration for transaction processing

**Unit Test Features:**
- **Mockito Integration**: Full mocking of repository dependencies
- **Comprehensive Coverage**: Success, validation, business rule, and system error scenarios
- **Fast Execution**: Pure unit tests with mocked dependencies
- **Deterministic Results**: Consistent test outcomes without database dependencies

## 🏛️ Business Rules

### Account Management
- ✅ Account IDs must be unique
- ✅ Initial balance must be positive (> 0)
- ✅ Account creation returns account details
- ❌ Negative or zero initial balances are rejected

### Transaction Processing
- ✅ Successful transactions update both account balances
- ✅ All transactions (successful and failed) are recorded for audit
- ❌ Insufficient balance transactions are rejected but recorded
- ❌ Same account transfers are not allowed
- ❌ Negative transaction amounts are rejected
- ❌ Transfers to/from non-existent accounts are rejected

### Transaction Status Lifecycle
1. **PROCESSING**: Transaction is being processed
2. **COMPLETED**: Transaction completed successfully
3. **FAILED**: Transaction failed due to business rule violation

## 🔧 Error Handling

The service implements comprehensive error handling with custom exceptions:

### Custom Exception Hierarchy

- **BankingException**: Base exception class
- **AccountNotFoundException**: Account lookup failures
- **AccountAlreadyExistsException**: Duplicate account creation
- **InvalidBalanceException**: Invalid initial balance operations
- **InsufficientBalanceException**: Insufficient funds
- **InvalidTransactionException**: Invalid transaction operations
- **TransactionNotFoundException**: Transaction lookup failures

### Error Response Format

```json
{
  "error": "BAD_REQUEST",
  "message": "Insufficient balance in source account. Available: 100.0, Required: 150.0",
  "errorCode": "INSUFFICIENT_BALANCE"
}
```

### HTTP Status Codes

- **200 OK**: Successful operations and recorded failed transactions
- **400 Bad Request**: Validation errors
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate resource creation
- **500 Internal Server Error**: System errors

## 📊 Monitoring & Observability

### Health Checks
- **Service Health**: `GET /test/ping`

### Audit Trail
- All transactions are recorded in the database
- Failed transactions include error messages
- Complete transaction history for compliance and debugging

## 🔄 Database Schema

### Account Entity
```sql
CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    ref_id VARCHAR(255) UNIQUE NOT NULL,
    balance DECIMAL(19,2) NOT NULL
);
```

### Transaction Entity
```sql
CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    ref_id VARCHAR(255) UNIQUE NOT NULL,
    source_account_ref_id VARCHAR(255),
    destination_account_ref_id VARCHAR(255),
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(500)
);
```

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Docker (if applicable)
```bash
docker compose up -d
```

**Built with ❤️ using Spring Boot and modern Java practices**
