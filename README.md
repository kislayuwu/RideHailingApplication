# Ride Hailing Application

A scalable, low-latency, multi-tenant ride-hailing backend system inspired by Uber/Ola.  
The system supports real-time driver location updates, async driver–rider matching, trip lifecycle management, dynamic surge pricing, payments, notifications, and monitoring.

---

## Features

- Real-time driver location tracking (Redis Geo)  
- Asynchronous driver–rider matching with offer expiry  
- Strong consistency in driver assignment  
- Trip lifecycle management  
- Dynamic surge pricing based on demand/supply  
- Secure payment processing with pessimistic locking  
- WebSocket-based real-time notifications  
- JWT-based authentication & authorization  
- Performance monitoring using New Relic  

---

## High Level Architecture

### Core Components
- Ride Service  
- Driver Service  
- Matching Engine (Async)  
- Trip Service  
- Payment Service  
- Notification Service (WebSocket extendable design for email, sms etc)  
- Redis (Caching + Geo + Locks)  
- MySQL (Transactional DB)  
- New Relic (Monitoring)  

### Key Design Goals
- Driver–rider matching under 1s p95  
- No double driver allocation  
- Stateless APIs  
- Region-local writes  
- Horizontal scalability  

---

## Database Schema

### User (Authentication)
| Column  | Description                 |
|--------|-----------------------------|
| id     | Primary Key                 |
| email  | Unique, indexed             |
| password | Encrypted                 |
| role   | RIDER / DRIVER / ADMIN      |

---

### Driver
| Column   | Description                |
|---------|----------------------------|
| id      | Primary Key                |
| name    | Driver name (indexed)      |
| city    | Operating city             |
| lat     | Current latitude           |
| lng     | Current longitude          |
| assigned | Driver availability       |

---

### Ride
| Column          | Description               |
|-----------------|---------------------------|
| id              | Primary Key              |
| city            | Ride city                |
| rider_name      | Rider name               |
| pickup_lat      | Pickup latitude          |
| pickup_lng      | Pickup longitude         |
| destination_lat | Destination latitude     |
| destination_lng | Destination longitude    |
| status          | CREATED / ASSIGNED / CANCELLED |
| created_at      | Creation time            |

---

### Trip
| Column          | Description               |
|-----------------|---------------------------|
| id              | Primary Key               |
| city            | Trip city                 |
| rider_name      | Rider name                |
| driver_name     | Driver name               |
| pickup_lat      | Pickup latitude           |
| pickup_lng      | Pickup longitude          |
| destination_lat | Destination latitude      |
| destination_lng | Destination longitude     |
| start_time      | Trip start                |
| end_time        | Trip end                  |
| fare            | Final fare                |
| status          | STARTED / ENDED           |

---

### Payment
| Column       | Description                 |
|--------------|-----------------------------|
| id           | Primary Key                 |
| trip_id      | FK → Trip                   |
| amount       | Fare amount                 |
| psp_reference| PSP transaction ID          |
| status       | SUCCESS / FAILED            |
| created_at   | Payment time                |

---

## Redis Design

### Keys Used
| Key                       | Description                           |
|---------------------------|---------------------------------------|
| driver:geo:available:{city} | Geo index of available drivers      |
| ride:candidates:{rideId}  | List of top 5 driver candidates      |
| ride:candidateindex:{rideId} | Index pointer for next candidate   |
| driver:lock:{driverId}    | Prevents multiple simultaneous offers |
| ride:assigneddriver:{rideId} | Tracks assigned driver for ride   |

---

## Ride Flow

### 1️⃣ Create Ride
**Endpoint:** `POST /v1/rides`  

**Flow:**
1. Rider creates a ride  
2. Ride stored with status REQUESTED  
3. Driver matching triggered asynchronously  

---

### 2️⃣ Driver Matching (Async)
- Redis GEO search for drivers within 5km  
- Top 5 drivers pushed into `ride:candidates:{rideId}`  
- Offers sent sequentially  
- Each offer expires in 15 seconds  
- `driver:lock:{driverId}` prevents double offers  
- On Redis key expiry → next driver is offered  

---

### 3️⃣ Driver Accepts Ride
**Endpoint:** `POST /v1/drivers/{driverId}/accept`  

**Consistency Guarantees:**
- Check `ride:assigneddriver:{rideId}` before assignment  
- Prevents double driver allocation  
- Trip created after successful acceptance  

---

### 4️⃣ Trip End & Fare Calculation
**Endpoint:** `POST /v1/trips/{id}/end`  

**Fare Calculation:**
- Base fare + distance  
- Dynamic surge pricing based on:  
  - Available drivers: `driver:geo:available:{city}`  
  - Active rides: `ride:assigneddriver:*`  

---

### 5️⃣ Payment Processing
**Endpoint:** `POST /v1/payments`  

- Supports multiple payment methods (mock PSP)  
- Uses pessimistic DB locking  
- Ensures exactly-once payment processing  

---

## Notifications
- WebSocket-based real-time notifications  
- Events supported:  
  - Ride offer  
  - Driver assignment  
  - Trip end  
  - Payment status  

**Extensible Design:**  
- Future support for Email / SMS / Push notifications  

---

## Security
- JWT-based authentication  
- `/login` → Access Token (Header)  
- Refresh Token stored in HTTP-only cookie  
- `/refresh-token` issues new access token  
- All APIs are authenticated and role-protected  

---

## Concurrency & Consistency
- Redis locks for driver offers  
- Atomic driver assignment  
- DB transactions for ride → trip transition  
- Pessimistic locking for payments  
- Idempotent APIs  

---

## Monitoring & Performance

**New Relic Integration:**
- API latency (p95 / p99)  
- Slow DB queries  
- Slow response time alerts  

**Optimizations Applied:**
- DB indexing on frequently queried columns  
- Redis caching for hot paths  
- Async driver matching  
- Reduced DB round-trips  

---

## Testing


---

**Author:** Kislay Shekhar  
**Email:** Shekharkislay07@gmail.com
