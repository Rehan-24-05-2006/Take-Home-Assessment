# Indo Thai - Software Development Engineer Intern Take-Home Assessment

## Order Update & Position Maintaining Services

This project implements the Indo Thai Software Development Engineer Intern take-home assessment.

The application consists of two independent Spring Boot services that process order updates from a CSV file and maintain the current net position for each trading symbol.

The solution focuses on correctness, validation, testing, simple service communication, and maintainability without unnecessary infrastructure.

---

## 1. Architecture

The application contains two independently runnable services:

### Order Update Service

Responsibilities:

- Reads the CSV file incrementally, one row at a time.
- Validates every CSV row.
- Rejects invalid rows without stopping subsequent processing.
- Preserves CSV processing order.
- Detects duplicate `event_id` values.
- Sends valid events to the Position Maintaining Service.
- Limits event transmission to a maximum of 50 events per second.
- Logs accepted, rejected, and successfully sent events.
- Logs when CSV processing is completed.

### Position Maintaining Service

Responsibilities:

- Receives order events from the Order Update Service.
- Maintains current net positions in memory.
- BUY increases the position.
- SELL decreases the position.
- Ignores duplicate `event_id` values.
- Keeps symbols even when their net position becomes zero.
- Provides the current positions through `GET /position`.
- Keeps the API available while events are being processed.

---

## 2. High-Level Architecture

```text
                     order_updates.csv
                            |
                            v
              +----------------------------+
              |    Order Update Service     |
              |          :8080              |
              |                            |
              | CSV Processing              |
              | Validation                  |
              | Throttling                  |
              +-------------+--------------+
                            |
                            |
                       HTTP POST
                       /events
                            |
                            v
              +----------------------------+
              | Position Maintaining       |
              | Service                    |
              |          :8081              |
              |                            |
              | Position Calculation       |
              | Duplicate Detection        |
              | In-Memory State             |
              +-------------+--------------+
                            |
                            |
                     GET /position
                            |
                            v
                         JSON
```
## 3. Inter-Service Communication

HTTP was selected for communication between the two services.

The Order Update Service uses Spring RestClient to send events to the Position Maintaining Service.

Why HTTP?

HTTP was selected because:

It is simple and lightweight.
It does not require external infrastructure.
It is easy to run locally.
It is easy to test using Postman or curl.
It provides a clear interface between the two services.
HTTP communication is explicitly allowed by the assessment.

---
## 4. Event Contract

The services communicate using the following JSON payload:
```
{
  "event_id": "evt-0001",
  "symbol": "RELIANCE",
  "transaction_type": "BUY",
  "quantity": 90
}
```
Event fields
Field	Type	Validation
event_id	String	Must not be blank
symbol	String	Must not be blank
transaction_type	String	Must be exactly BUY or SELL
quantity	Integer	Must be greater than zero

---
## 5. Event Processing Rules
BUY

BUY increases the current position.
```
Example:

RELIANCE BUY 90
Result:
RELIANCE = 90
SELL
SELL decreases the current position.

Example:
TCS SELL 75
Result:
TCS = -75
```
Negative positions are valid.
---

## 6. Duplicate Event Handling

Every event_id uniquely identifies an event.

The first valid event received for an event_id wins.

Any later event with the same ID is ignored.

Example:
```
evt-0001,RELIANCE,BUY,90
evt-0001,RELIANCE,SELL,50

Only the first event is processed.

Final position:

RELIANCE = 90
```
Duplicate event IDs are maintained in memory.
---
## 7. CSV Processing

The Order Update Service reads the CSV incrementally using a BufferedReader.

The complete file is not loaded into memory.

Example CSV:
```
event_id,symbol,transaction_type,quantity
evt-0001,RELIANCE,BUY,90
evt-0002,TCS,SELL,75
evt-0003,HDFCBANK,BUY,60
```
Each row is processed sequentially.

Invalid rows are logged and skipped without stopping subsequent valid rows.
---
## 8. Validation

The following validation rules are implemented.
```
Blank event ID

Invalid:

,RELIANCE,BUY,90
Blank symbol

Invalid:

evt-0001,,BUY,90
Invalid transaction type

Only BUY and SELL are accepted.

Invalid:

evt-0001,RELIANCE,HOLD,90
Invalid quantity

Quantity must be a positive integer.

Invalid examples:

0
-10
abc
blank
```
Invalid rows are rejected and processing continues with subsequent rows.
---
## 9. Throttling

The Order Update Service limits event transmission to a maximum of 50 events per second.

The rate is configurable.

The implementation uses a simple delay between events.

Exact sub-millisecond timing is not required by the assessment.
---
## 10. Configuration
Order Update Service
```
Example application.yml:

server:
  port: 8080

spring:
  application:
    name: order-update-service

order:
  csv:
    path: data/order_updates.csv

position:
  service:
    url: http://localhost:8081

max:
  events:
    per:
      second: 50
Configuration properties
Property	Description	Example
server.port	Order service port	8080
order.csv.path	CSV input file path	data/order_updates.csv
position.service.url	Position service URL	http://localhost:8081
max.events.per.second	Maximum event rate	50
Position Maintaining Service

Example application.yml:

server:
  port: 8081

spring:
  application:
    name: position-maintaining-service
```
---
## 11. Prerequisites

Before running the application, make sure the following are installed:

Java 21
Maven
Git

Verify Java:

java -version

Verify Maven:

mvn -version
---

## 12. Project Structure
```
take-home-assessment/
|
+-- order-update-service/
|   |
|   +-- src/
|   |   +-- main/
|   |   |   +-- java/
|   |   |   +-- resources/
|   |   |
|   |   +-- test/
|   |
|   +-- data/
|   |   +-- order_updates.csv
|   |
|   +-- pom.xml
|
+-- position-maintaining-service/
    |
    +-- src/
    |   +-- main/
    |   |   +-- java/
    |   |   +-- resources/
    |   |
    |   +-- test/
    |
    +-- pom.xml
```
---
## 13. Running the Services

Both services run as separate processes.

Step 1: Start Position Maintaining Service

Open a terminal inside:
```
position-maintaining-service

Run:

mvn spring-boot:run

The service starts on:
http://localhost:8081
```
---
Step 2: Start Order Update Service

Open another terminal inside:
```
order-update-service

Run:

mvn spring-boot:run

The service starts on:

http://localhost:8080
```
---
## 14. Process CSV

After both services are running, call:
```
POST http://localhost:8080/orders/process

Using curl:

curl -X POST http://localhost:8080/orders/process

Expected response:

Order processing started

The Order Update Service will then:

Read CSV
   ↓
Validate row
   ↓
Check duplicate event ID
   ↓
Send valid event
   ↓
Position Maintaining Service
```
---
## 15. Get Current Positions

Use:
```
GET http://localhost:8081/position

Using curl:

curl http://localhost:8081/position

Example response:

{
  "ICICIBANK": -2250,
  "BHARTIARTL": -750,
  "TATASTEEL": -1750,
  "TCS": -3750,
  "HINDUNILVR": -2750,
  "LT": -4250,
  "KOTAKBANK": -1250,
  "HDFCBANK": 3000,
  "AXISBANK": 2000,
  "TATAMOTORS": 2500,
  "SBIN": 5000,
  "MARUTI": -4750,
  "ITC": 3500,
  "BAJFINANCE": 500,
  "INFY": 1500,
  "SUNPHARMA": 4000,
  "ASIANPAINT": -250,
  "RELIANCE": 4500,
  "NTPC": -3250,
  "ADANIENT": 1000
}
```
Negative and zero positions are valid.

Symbols are retained even when their position becomes zero.
---
## 16. Automated Testing

Automated tests are implemented using JUnit and Spring Boot Test.

The test suite covers the required scenarios from the assessment.
```
Covered test cases
Valid BUY event
Valid SELL event
BUY position calculation
SELL position calculation
Multiple symbols
Negative positions
Zero net positions
Duplicate event_id
Invalid transaction type
Zero quantity
Negative quantity
Non-integer quantity
Blank quantity
Blank event ID
Blank symbol
Continuing after an invalid CSV row
GET /position response
```
---

## 17. Running Tests
Order Update Service

From the Order Update Service directory:
```
mvn test
Position Maintaining Service

From the Position Maintaining Service directory:

mvn test

Expected result:

BUILD SUCCESS
```

All tests should pass before submitting the project.
---

## 18. Concurrency

The Position Maintaining Service may receive events while the /position endpoint is being accessed.

The in-memory position state is protected using synchronized operations so that position updates and reads remain consistent during concurrent access.

---

## 19. Error Handling

Malformed and invalid CSV rows do not stop the entire processing operation.

Example log:

Rejected event evt-0005:
transaction_type must be BUY or SELL

Malformed quantity:

Rejected malformed CSV row:
evt-0006,INFY,BUY,abc
Reason: quantity must be an integer

If communication with the Position Maintaining Service fails, the Order Update Service logs the error.

Example:

Error processing CSV row:
I/O error while communicating with Position Maintaining Service

---

## 20. Delivery Semantics

The application uses direct HTTP communication.

The implementation does not provide durable delivery or exactly-once guarantees across service restarts.

If the Position Maintaining Service is unavailable while an event is being sent, the event is not persisted in a durable queue.

This is an intentional trade-off because durable delivery and recovery after a complete process restart are explicitly out of scope for the assessment.

--- 

## 21. Persistence

No database is used.

The following data is maintained in memory:

Positions
Processed event IDs
```
Example:

RELIANCE -> 4500
TCS      -> -3750
SBIN     -> 5000
```
Restarting the Position Maintaining Service clears the in-memory state.

---

21. Technology Stack
    
Java 21
Spring Boot 3.5.7
Maven
Spring Web
Spring Validation
Lombok
JUnit 5
Mockito
MockMvc
HTTP / RestClient

---

## 24. Dependencies

Both services use Maven for dependency management.

The main dependencies include:

Spring Boot Web
Spring Boot Validation
Spring Boot Test
Lombok

No external database or message broker is required.

---

## 25. Sample End-to-End Flow

Input:
```
event_id,symbol,transaction_type,quantity
evt-0001,RELIANCE,BUY,90
evt-0002,TCS,SELL,75
evt-0003,RELIANCE,SELL,40
```
Processing:
```
RELIANCE = 0 + 90
TCS      = 0 - 75
RELIANCE = 90 - 40
```
Final positions:
```
{
  "RELIANCE": 50,
  "TCS": -75
}
```
---

## 26. Design Decisions

The solution intentionally follows a simple architecture because the assessment prioritizes correctness, readability, testing, and clear reasoning.

Persistence is explicitly out of scope.

HTTP is sufficient for the required communication and avoids unnecessary infrastructure.

Only two services are required and there is no requirement for a centralized gateway.

In-memory state

The assessment explicitly allows in-memory position tracking and duplicate event tracking.

---

## 27. Known Limitations
Position state is lost when the Position Maintaining Service restarts.
Duplicate event tracking is also lost after restart.
There is no persistent event queue.
There is no automatic retry mechanism for failed HTTP delivery.
There is no exactly-once guarantee across process restarts.
The application is intended for the assessment scope rather than production deployment.
The CSV parser assumes the provided simple CSV structure and does not implement full RFC-compliant CSV parsing.

---

## 28. AI-Assisted Development

AI-assisted tools were used during development for guidance, debugging assistance, code review, and documentation support.

All submitted code and design decisions were reviewed and understood by the author.

---

## 29. Conclusion

This project provides a simple two-service implementation for processing order updates and maintaining net trading positions.

The implementation focuses on:
```
Correct order processing
Input validation
Duplicate event handling
Incremental CSV processing
HTTP-based service communication
Configurable throttling
Thread-safe in-memory state
Automated testing
Clear documentation
Easy local execution
```
The solution intentionally avoids unnecessary infrastructure while satisfying the functional and technical requirements of the assessment.


### Bas ek important kaam karna

README paste karne ke baad ye command dono projects me chala:

```bash
mvn test
