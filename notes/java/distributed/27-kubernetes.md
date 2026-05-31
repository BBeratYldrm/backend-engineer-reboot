# [11] Kubernetes

## Keywords
pod · deployment · service · ingress · egress · hpa · liveness-probe ·
readiness-probe · statefulset · rolling-update · consumer-lag · partition-count

---

## [11.1] Core Concepts

Pod:
→ smallest deployable unit
→ 1+ containers (usually 1)
→ runs a Docker image
→ ephemeral — when it crashes, a new one starts

Deployment:
→ manages pods declaratively
→ replicas: 10 → Kubernetes guarantees 10 pods are always running
→ handles rolling updates and rollbacks

Service:
→ stable network endpoint for pods
→ load balances across pods
→ pod IPs change, service name stays fixed
→ in code: "http://websocket-service/push/user123"
→ Kubernetes DNS resolves to the correct pod automatically

Ingress:
→ routes external traffic into the cluster
→ example: api.example.com/messages → Message Service

Egress:
→ traffic from cluster to the outside world
→ example: push notifications to external services

---

## [11.2] Probes

Liveness probe:
→ "is this pod alive?"
→ fail → pod is restarted
→ Spring Boot Actuator: /actuator/health/liveness

Readiness probe:
→ "is this pod ready to receive traffic?"
→ fail → pod stays running but Service stops sending it traffic
→ Spring Boot Actuator: /actuator/health/readiness
→ Kafka consumer: readiness=false until consumer group join is complete

Spring Boot Actuator provides these endpoints automatically.
No manual implementation needed — add dependency, configure probe path.

---

## [11.3] HPA — Horizontal Pod Autoscaler

Automatically adds pods when load increases, removes when load decreases.

CPU metric → wrong for Kafka consumers (CPU may be low even when lagging)
Consumer lag metric → correct: high lag = consumers can't keep up → add pods

Max pods = partition count
→ 1 partition → 1 consumer rule
→ adding pods beyond partition count = idle pods, no benefit

---

## [11.4] StatefulSet vs Deployment

Deployment → stateless services (Message Service, Delivery Service)
pods are interchangeable

StatefulSet → stateful services (Kafka brokers, HBase RegionServers)
each pod has a unique stable identity and persistent storage

---

## [11.5] Rolling Update

maxUnavailable: 2 → at most 2 pods down during update
maxSurge: 2        → at most 2 extra pods running during update

Process:
1. Start new pod → wait for readiness probe
2. Send SIGTERM to old pod
3. preStop hook runs (commit Kafka offsets, drain connections)
4. Terminate old pod
5. Repeat

---

## [11.6] Inter-pod Communication

Pods communicate via Service names — stable DNS entries.
Pod IPs change freely. Service name never changes.
No code change needed when pods restart or scale.

Open source (Apache License). Originally developed by Google.
Managed offerings: AWS EKS, GCP GKE, Azure AKS.