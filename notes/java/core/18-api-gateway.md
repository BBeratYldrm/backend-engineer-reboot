# [4.5] API Gateway

## Keywords
api-gateway · reverse-proxy · routing · rate-limiting · ssl-termination ·
authentication · load-balancing · single-entry-point · cross-cutting-concerns ·
bff · kong · aws-api-gateway · nginx

---

## What Is API Gateway

Single entry point for all client requests.
Every request passes through it before reaching any service.

Without API Gateway:
Client → directly calls Order Service
Client → directly calls Payment Service
Client → directly calls Shop Service
→ Each service handles auth, rate limiting, logging separately
→ Code duplication across all services
→ Inconsistency risk — one service updated, others not

With API Gateway:
Client → API Gateway → Order Service
→ Payment Service
→ Shop Service
→ Cross-cutting concerns handled once, centrally

Rakuten connection:
Apache + Load Balancer + GSLB combination served as API Gateway.
URLs registered in Apache config → routing.
IP blocking in Apache → security.
GSLB → global traffic management.
Modern systems replace this combination with a dedicated API Gateway.

---

## What API Gateway Does

Authentication:
→ Validate JWT token once at the gateway
→ Services receive only verified requests
→ No auth code duplication across services
→ One security bug to fix, not ten

Rate Limiting:
→ Limit requests per IP, per user, per API key
→ Protects all downstream services
→ DDoS protection at the entry point

Routing:
→ /api/orders → Order Service
→ /api/payments → Payment Service
→ /api/shops → Shop Service
→ Rules configured centrally, not in each service

Load Balancing:
→ Multiple instances of each service
→ Gateway distributes traffic across instances
→ Health check aware — does not route to unhealthy instances

SSL Termination:
→ HTTPS handled at the gateway
→ Internal service-to-service communication can be HTTP
→ Certificate management in one place

Logging and Monitoring:
→ Every request logged at the gateway
→ Full picture of traffic without touching individual services
→ Latency, error rate, request count — all visible

Request/Response Transformation:
→ Add headers, remove headers
→ Transform request format before forwarding
→ Aggregate responses from multiple services

---

## API Gateway vs Reverse Proxy

Reverse Proxy (Nginx, Apache):
→ Forwards requests to backend servers
→ Basic routing, SSL termination, load balancing
→ Configuration-based, less dynamic

API Gateway (Kong, AWS API Gateway):
→ Everything reverse proxy does, plus:
→ Authentication, authorization
→ Rate limiting per user/API key
→ Analytics and monitoring
→ Plugin ecosystem
→ Developer portal

Rakuten's Apache was a reverse proxy.
Modern API Gateways add the intelligence layer on top.

---

## BFF — Backend For Frontend

A pattern built on top of API Gateway concept.
Different clients have different needs:

Mobile app    → needs small payloads, battery-friendly
Web browser   → needs rich data, full features
Third-party   → needs public API, rate-limited

One BFF per client type:
Mobile BFF  → optimized for mobile
Web BFF     → optimized for web
Public BFF  → external API with strict rate limiting

Each BFF aggregates calls to multiple backend services
and shapes the response for its specific client.

Netflix uses this — mobile API returns different data than web API.

Trade-offs:
+ Each client gets exactly what it needs
+ Backend services stay clean and generic
- More services to maintain
- Code duplication across BFFs if not careful

---

## Popular API Gateway Tools

Kong:
→ Open source, plugin-based
→ Runs on Nginx
→ Rich plugin ecosystem (auth, rate limit, logging)

AWS API Gateway:
→ Fully managed, AWS ecosystem
→ Integrates with Lambda, ECS, EKS
→ Pay per request

Spring Cloud Gateway:
→ Java/Spring ecosystem
→ Code-based routing configuration
→ Good for Java-heavy teams

Nginx / Apache:
→ Reverse proxy, basic routing
→ Manual configuration
→ Used in many traditional setups (like Rakuten)

---

## Trade-offs

+ Single entry point — security, monitoring, rate limiting in one place
+ Services stay clean — no cross-cutting concern code
+ Easy to add new policies — change gateway config, not every service
+ Client isolation — internal architecture hidden from clients

- Single point of failure — gateway goes down, everything goes down
  → Solution: run multiple gateway instances behind a load balancer
- Added latency — every request goes through one more hop
  → Usually negligible (< 5ms) but exists
- Bottleneck risk — all traffic through one place → must scale horizontally
- Complexity — one more system to maintain and monitor

---

## Interview Checklist
→ What is API Gateway? → single entry point, handles cross-cutting concerns centrally
→ Why not auth in every service? → code duplication, inconsistency risk, maintenance burden
→ API Gateway vs Reverse Proxy? → reverse proxy is basic forwarding, gateway adds auth/rate limit/analytics
→ BFF pattern? → separate gateway per client type, each optimized for its client
→ API Gateway single point of failure? → run multiple instances behind load balancer
→ Rakuten connection? → Apache + LB + GSLB was the API Gateway combination