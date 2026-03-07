# System Design Interview Prep

A hands-on interview preparation toolkit combining a built-in system design knowledge base, evaluation rubric, and working Java code implementations.

## Features

- AI-powered mock system design interviews via Claude Code skill
- Covers core topics: rate limiting, caching, sharding, load balancing, message queues, and more
- Runnable Java implementations for each design problem
- Structured evaluation rubric for self-assessment

## Getting Started

### Setup

1. Run the setup script from the project root:
   ```bash
   bash src/setup.sh
   ```
   This copies `SKILL.md` to `~/.claude/skills/`, installs the Claude Code CLI, and guides you through the IntelliJ plugin setup.
2. In the Claude Code prompt, invoke:
   ```
   /sysdesigninterviewprep
   ```
3. Start the mock interview session

## Running the Code

Compile and run from the project root:

```bash
javac -d out src/systemdesignprac/RateLimiter.java src/Main.java
java -cp out Main
```

## Project Structure

```
src/
├── setup.sh
├── Main.java
└── systemdesignprac/
    └── RateLimiter.java
```

## Topics Covered

Based on *System Design Interview* by Alex Xu (Vol. 1 & 2).

| #  | Topic | Summary |
|----|-------|---------|
| 1  | Design A Rate Limiter | Token Bucket, Sliding Window Log, Fixed Window Counter; client- vs server-side; Redis-based distributed impl |
| 2  | Design Consistent Hashing | Virtual nodes on a hash ring to minimize key remapping on server add/remove |
| 3  | Design A Key-Value Store | CAP theorem, replication, quorum (W+R>N), consistent hashing, gossip protocol, Merkle tree |
| 4  | Design A Unique ID Generator In Distributed Systems | Multi-master replication, UUID, Twitter Snowflake (timestamp + datacenter + machine + seq) |
| 5  | Design A URL Shortener | Hash + collision resolution or base62 encoding; 301 vs 302 redirect; single row per short URL |
| 6  | Design A Web Crawler | BFS with URL frontier, DNS cache, politeness policy, dedup with bloom filter, HTML parser |
| 7  | Design A Notification System | Push (APNs/FCM), SMS (Twilio), email (SendGrid); fan-out workers; retry with exponential backoff |
| 8  | Design A News Feed System | Fan-out-on-write vs fan-out-on-read; hybrid for celebrities; cache feed lists with Redis |
| 9  | Design A Chat System | WebSocket for real-time; presence service; message sync with last-seen cursor; group chat fan-out |
| 10 | Design A Search Autocomplete System | Trie with top-K caching; AJAX debounce; offline trie builder; filter via bloom filter |
| 11 | Design YouTube | Chunked upload to blob store, transcoding pipeline (DAG), CDN for delivery, metadata in SQL |
| 12 | Design Google Drive | Block-level dedup, sync conflict resolution, delta sync, strong consistency for metadata |
| 13 | Proximity Service | Geohash / quadtree for location indexing; read-heavy so cache heavily; radius search |
| 14 | Nearby Friends | WebSocket pub/sub per user; Redis location store with TTL; location history in Cassandra |
| 15 | Google Maps | Graph + A* / Dijkstra for routing; map tile CDN; ETA via ML; location update stream |
| 16 | Distributed Message Queue | Producer/consumer, at-least-once delivery, offset-based consumption (Kafka model), partition & replication |
| 17 | Metrics Monitoring and Alerting System | Time-series DB (InfluxDB/Prometheus), pull vs push, downsampling, alert rule engine |
| 18 | Ad Click Event Aggregation | Lambda architecture; MapReduce aggregation; idempotent dedup; watermark for late data |
| 19 | Hotel Reservation System | Optimistic locking for inventory, idempotency key for payment, double-booking prevention |
| 20 | Distributed Email Service | SMTP relay, metadata in NoSQL, body in blob store, spam filter, soft/hard bounce handling |
| 21 | S3-like Object Storage | Data store (erasure coding) + metadata store; multipart upload; versioning; presigned URL |
| 22 | Real-time Gaming Leaderboard | Redis sorted set (ZADD/ZRANK) for top-K; score sharding for global scale |
| 23 | Payment System | PSP integration, idempotency key, exactly-once via outbox pattern, reconciliation |
| 24 | Digital Wallet | Event sourcing for balance, distributed transaction via 2PC or Saga, CQRS |
| 25 | Stock Exchange | Order matching engine (price-time priority), sequencer for ordering, low-latency ring buffer |
