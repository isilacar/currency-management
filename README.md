# Currency Management System API Documentation

This API allows you to query exchange rates, perform currency conversions, view conversion history and bulk currency conversion operation with CSV file.

> ⚠️ **Important:** The external API allows only 100 requests per month per API key under the free version.

## Table of Contents
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Error Codes](#error-codes)
- [Examples](#examples)

## Getting Started

### Requirements
- Java 17
- Maven
- Docker (optional)

### Installation
```bash
# Clone the project
git clone https://github.com/isilacar/currency-management.git

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Running with Docker
**Docker has to be up and running in your system**

```bash

# Build Docker image
docker docker build -t currency:latest . 

# Run container
docker  docker run -d -p 8080:8080 --name currency-container currency:latest 
```

## API Endpoints

### 1. Exchange Rate Query

**Endpoint:** `POST /api/v1/currency/exchange-rate`

**Description:** Returns the current exchange rate between two currencies.

**Query Parameters:**
- `base`: Source currency (e.g., USD)
- `target`: Target currency (e.g., EUR)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/currency/exchange-rate?base=USD&target=EUR"
```

**Success Response:**
```json
{
    "baseCurrency": "USD",
    "targetCurrency": "EUR",
    "exchangeRate": 1.5
}
```

### 2. Currency Conversion

**Endpoint:** `POST /api/v1/currency/convert`

**Description:** Converts a specified amount from one currency to another.

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/currency/convert?base=USD&target=EUR&amount=100"
```


**Request Body:**
```json
{
    "base": "USD",
    "target": "EUR",
    "amount": 100
}
```

**Success Response:**
```json
{
  "id": 6,
  "baseCurrency": "TRY",
  "targetCurrency": "EUR",
  "amount": 100,
  "convertedAmount": 2.2306,
  "exchangeRate": 0.022306,
  "transactionId": "TRX12345",
  "transactionDate": "2025-06-09"
}
```

### 3. Conversion History

**Endpoint:** `POST /api/v1/currency/history`

**Description:** Retrieve the currency conversion history base on the some creteria like transactionId or transactionDate.
- `At least one of the transactionId or transactionDate value is required.`

**Query Parameters:**
- `transactionId`: Transaction ID (optional)
- `transactionDate`: Transaction date (optional, format: yyyy-MM-dd)
- `pageNumber`: Page number (default: 0)
- `pageSize`: Records per page (default: 10)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/currency/history?transactionId=TRX123456&pageNumber=0&pageSize=10"
```

**Request Body With Both TransactionId and TransactionDate:**
```json
{
  "transactionId": "TRX12345",
  "transactionDate": "2025-06-10",
  "pageNumber": 0,
  "pageSize": 1
}
```

**Success Response:**
```json
{
    "currencyHistoryResponseList": [
        {
            "id":3,
            "baseCurrency": "USD",
            "targetCurrency": "EUR",
            "amount": 100,
            "convertedAmount": 150,
            "exchangeRate": 1.5,
            "transactionId": "TRX123456",
            "transactionDate": "2025-06-09"
        }
    ],
    "totalValue": 1,
    "totalPages": 1,
    "currentPage": 0,
    "viewedValueCount": 1
}
```

### 4. Bulk Conversion

**Endpoint:** `POST /api/v1/currency/bulk-convert`

**Description:** Performs bulk currency conversion using a CSV file.

**Request:**
- Content-Type: multipart/form-data
- File format: CSV
- CSV headers: base,target,amount

**Example CSV Content:**
```csv
base,target,amount
USD,EUR,100
EUR,TRY,200
```

**Success Response:**
```json
[
    {
        "baseCurrency": "USD",
        "targetCurrency": "EUR",
        "amount": 100,
        "convertedAmount": 150,
        "exchangeRate": 1.5,
        "transactionId": "TRX123456",
        "transactionDate": "2025-06-09"
    },
    {
        "baseCurrency": "EUR",
        "targetCurrency": "TRY",
        "amount": 200,
        "convertedAmount": 300,
        "exchangeRate": 1.5,
        "transactionId": "TRX123457",
        "transactionDate": "2025-06-09"
    }
]
```

## Error Codes

| Error Code | Description |
|------------|-------------|
| NULL_CURRENCY_SYMBOL | Currency symbols cannot be null |
| INVALID_CURRENCY_SYMBOL | Invalid currency code |
| TRANSACTION_PARAMETER_REQUIRED | At least one query parameter is required |
| FILE_UPLOAD_ERROR | File upload error |
| VALIDATION_ERROR | Invalid request parameters |
| INVALID_DATE_FORMAT | Invalid date format |

**Example Error Response:**
```json
{
    "timestamp": "2025-06-09 10:30:00",
    "errorCode": "INVALID_CURRENCY_SYMBOL",
    "message": "Invalid currency code: INVALID. Some Valid codes: [USD, EUR, TRY]",
    "path": "/api/v1/currency/exchange-rate"
}
```

## Some Supported Currency Symbols

- USD (US Dollar)
- EUR (Euro)
- TRY (Turkish Lira)
- GBP (British Pound)
- JPY (Japanese Yen)
- CHF (Swiss Franc)
- CAD (Canadian Dollar)
- AUD (Australian Dollar)
- CNY (Chinese Yuan)
- INR (Indian Rupee)

## Security

- API key required

## Cache

- Cache is cleared at midnight
- New cache is created on first request after expiration

## Version History

- v1.0.0: Initial release
    - Basic exchange rate query
    - Currency conversion
    - Conversion history
    - Bulk conversion support 