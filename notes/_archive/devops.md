# [6.1] DevOps & Deployment

## CI/CD Pipeline

Flow:
PR opened → code review → merge to main → Jenkins trigger → deploy

Pipeline stages (fail fast — if any stage fails, pipeline stops):
1. Build    → compile code, create JAR
2. Test     → unit + integration tests
3. Docker   → build image, push to Harbor registry
4. Deploy   → K8s rolling update

Fail Fast principle:
Test fails → pipeline stops → production never touched ✅
Catch problems early, not in production.

Rakuten:
Bitbucket PR → Jenkins pipeline → Harbor → K8s (One Cloud)
Manual trigger with inputs: branch name, config repo, environment

## Health Checks — Liveness & Readiness

Problem:
Pod starts but app not ready (DB connections not established)
K8s sends traffic immediately → 500 errors

Two probes:
Liveness  → "Am I alive?" → if NO → K8s restarts the pod
Readiness → "Am I ready for traffic?" → if NO → K8s waits, no traffic sent

Spring Actuator:
/actuator/health/liveness  → liveness probe
/actuator/health/readiness → readiness probe

ci-pod.yaml (Rakuten):
livenessProbe:
httpGet:
path: /actuator/health/liveness
readinessProbe:
httpGet:
path: /actuator/health/readiness

## Graceful Shutdown

Problem:
Pod killed immediately during deploy → 100 in-flight requests cut off → data loss

Solution:
1. K8s sends SIGTERM signal
2. Pod stops accepting new requests
3. Completes all in-flight requests
4. Then shuts down cleanly

Spring Boot config:
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s

## Rolling Update — Zero Downtime Deploy

K8s replaces pods one by one:
3 old pods (v1) running
→ 1 new pod (v2) starts → readiness OK → traffic shifts
→ 1 old pod (v1) graceful shutdown
→ repeat until all pods updated

Result: no downtime, traffic always served ✅

Real world:
Netflix → zero downtime, 7/24 uninterrupted streaming
Uber → thousands of services, multiple deploys per day
Rakuten → rolling update on K8s One Cloud (you did this)

The question I ask myself:
"What happens to in-flight requests during deploy?"
→ Always implement graceful shutdown
→ Always configure liveness + readiness probes
→ Always use rolling update strategy

## Docker

Problem: "Works on my machine" — different Java versions, different configs
Solution: Package app WITH its environment

Dockerfile → recipe for the image
Image → static template (the recipe)
Container → running instance (the cake)

FROM openjdk:21
COPY target/app.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

docker build → creates image
docker run   → creates container from image

Docker vs VM:
Docker: shares OS kernel, lightweight, starts in seconds
VM: full OS copy, heavy, starts in minutes

## Docker Compose (Local Development)

Run multiple services together locally:
docker-compose up → app + ActiveMQ + MySQL all start together
Simulates production environment locally

Rakuten: julie-docker compose → tirebringin + ActiveMQ + MySQL

## Harbor + Kubernetes — Production Flow

Code → docker build → docker push to Harbor → K8s pulls image → Pod runs

Harbor = private image registry (company's own Docker Hub)
Why not Docker Hub? → Company code would be public ❌

K8s deployment YAML:
image: harbor.rakuten.com/tirebringin/front:v1.2.3
K8s pulls this image from Harbor → creates pod → app runs

K8s key concepts:
Pod        → smallest unit, 1+ containers
Deployment → "run 3 pods, restart if one crashes"
Service    → load balancing, how to reach pods
Namespace  → isolation — carwash ns, autoparts ns

Rakuten:
Each project had its own namespace
ci-pod.yaml defined pod spec
kubectl apply → K8s applies the config
Jenkins pipeline → build → push to Harbor → kubectl apply