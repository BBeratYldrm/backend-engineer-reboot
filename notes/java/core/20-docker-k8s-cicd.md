# [6.1] Docker, Kubernetes, CI/CD

## Keywords
docker · container · image · kubernetes · pod · deployment · service ·
ingress · hpa · rolling-update · liveness · readiness · ci · cd ·
jenkins · pipeline · harbor · artifact · migration · flyway · rollback ·
multi-stage-build · distroless · configmap · secret

---

## Docker

Container system — packages application + dependencies into one unit.
Run anywhere — no "works on my machine" problem.

Image: blueprint, read-only. Built once, run anywhere.
Container: running instance of an image.
Image : Container = Class : Object

VM vs Container:
VM        → separate OS kernel per VM, heavy (GBs), minutes to start
Container → shares host OS kernel, lightweight (MBs), seconds to start

Multi-stage build — smaller, safer production images:
Stage 1 (build): Maven + compiler + tests — builds the jar
Stage 2 (runtime): JRE only + jar — no build tools in production

FROM maven:3.9 AS builder
COPY . .
RUN mvn package

FROM eclipse-temurin:21-jre
COPY --from=builder target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

Distroless: minimal base image, no shell, no OS tools.
Smaller attack surface — Google distroless images common in production.

Trade-offs:
+ Platform independent, immutable, fast startup, resource efficient
- Stateful apps complex, container escape security risks

---

## Kubernetes

Manages Docker containers at scale.
"Run 3 instances, restart if one crashes, distribute traffic."

Core concepts:

Pod: smallest unit, one or more containers running together.
Each service instance = one pod.

Deployment: defines how many pods, which image, update strategy.
Pod crashes → Deployment automatically starts a new one.

Service: stable address for pods. Pods come and go, IPs change.
Service IP stays fixed → load balances across pods.

ConfigMap: non-sensitive config values, environment variables.
Secret:    sensitive data — passwords, API keys, tokens.
Rakuten: DB credentials in Secret, app config in ConfigMap.

Ingress: entry point for external traffic. URL routing, SSL termination.
Rakuten: new service → add URL to Ingress config.

HPA (Horizontal Pod Autoscaler):
CPU hits 80% → automatically add pods
Load drops → automatically remove pods
Rakuten: fired during peak reservation hours.

Liveness vs Readiness probes:
Liveness:  "is the app alive?" → fail → Kubernetes restarts pod
Readiness: "is the app ready for traffic?" → fail → Kubernetes stops routing to pod

Spring Boot Actuator:
/actuator/health/liveness  → liveness probe
/actuator/health/readiness → readiness probe

Rolling Update — zero downtime deploy:
v1 v1 v1
v2 v1 v1  ← new pod starts, gets traffic
v2 v2 v1
v2 v2 v2  ← all updated, no downtime

Rollback:
kubectl rollout undo deployment/app
→ instantly back to previous image

Trade-offs:
+ Self-healing, auto-scaling, zero-downtime deploy, platform independent
- Complex, steep learning curve, overkill for small systems

---

## CI/CD

CI — Continuous Integration:
Every commit → automated test + build.
"Is this code safe to integrate?"

CD — Continuous Delivery:
Every successful build → ready to deploy.
"Deploy with one click when ready."

Continuous Deployment:
Automatic prod deploy — no human approval.
Netflix, Amazon use this.

Rakuten pipeline (Jenkins + Bitbucket + Harbor + Kubernetes):

Code pushed to Bitbucket
↓
Jenkins triggered (webhook)
↓
Stage 1: Test
→ Unit tests (JUnit)
→ Coverage check (Jacoco)
↓
Stage 2: Build
→ Maven build
→ Docker image built
↓
Stage 3: Push
→ Image pushed to Harbor (internal registry)
→ Tagged with version + commit hash (app:1.2.3-abc123f)
↓
Stage 4: Deploy
→ kubectl apply → Kubernetes rolling update
→ Secrets and ConfigMaps injected
↓
Stage 5: Verify
→ Readiness probe passes
→ Health check OK

Artifact versioning:
app:1.2.3-abc123f  (version + commit hash)
→ "which code is in prod?" always answerable
→ rollback = deploy old image tag

DB Migration — Flyway / Liquibase:
Schema changes version-controlled as SQL files.
Migration runs before deploy — schema ready before new code starts.
Never modify existing migration files — add new ones only.

Trade-offs:
+ Early bug detection — every commit tested
+ Repeatable deploys — no human error
+ Fast release cycles — dozens of deploys per day possible
+ Easy rollback — old image always available
- Pipeline complexity grows over time
- Wrong tests → false confidence
- Automatic prod deploy risky without feature flags

## Interview Checklist
→ Docker vs VM? → shared kernel vs separate OS, MB vs GB, seconds vs minutes
→ Multi-stage build? → build tools in stage 1, only runtime in final image
→ Pod vs Deployment? → pod is running instance, deployment manages pod lifecycle
→ Liveness vs Readiness? → crash detection vs traffic readiness
→ HPA? → CPU/memory based automatic pod scaling
→ CI vs CD? → integration testing vs deployment automation
→ Why artifact versioning? → traceability and easy rollback
→ DB migration in pipeline? → Flyway runs before deploy, schema always ahead of code
→ Rolling update? → zero downtime, gradual replacement of old pods