# gRPC vs REST

## Keywords
grpc · rest · protobuf · http2 · binary · streaming ·
microservice · internal-api · public-api

---

## What Is gRPC

Google's high-performance RPC framework.
Alternative to REST for service-to-service communication.

REST:
→ HTTP/1.1, JSON, text-based
→ Human-readable, browser-friendly
→ Universal client support

gRPC:
→ HTTP/2, Protobuf (binary), compact
→ Much faster, lower latency
→ Requires gRPC client — not browser-friendly

---

## When to Use Which

REST:
→ Public API (mobile, browser, third-party)
→ Simple CRUD operations
→ Team unfamiliar with gRPC

gRPC:
→ Internal microservice communication
→ High-performance, low-latency needed
→ Streaming data (real-time updates)
→ Polyglot services (Java talks to Go talks to Python)

---

## Key Difference — One Sentence

REST sends JSON over HTTP/1.1.
gRPC sends binary Protobuf over HTTP/2 — faster, smaller, stricter.

---

## Interview Answer

"gRPC is a high-performance RPC framework using Protobuf over HTTP/2.
I'd use it for internal microservice communication where latency matters.
For public APIs, REST is still preferred because browser support is universal."

---

## Trade-offs

+ Much faster than REST (binary vs text)
+ Streaming support built-in
+ Strong typing via Protobuf schema
- Not human-readable — harder to debug
- Browser support limited (needs gRPC-Web proxy)
- Steeper learning curve than REST