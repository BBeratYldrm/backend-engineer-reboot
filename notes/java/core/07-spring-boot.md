# [2.2] Spring Boot

## Auto-Configuration — How It Works

Before Spring Boot: every bean defined manually — DataSource, TransactionManager,
DispatcherServlet, all by hand in XML or Java config.

Spring Boot principle: "Look at the classpath, configure what makes sense."

spring-boot-autoconfigure.jar contains hundreds of @Configuration classes.
Each is guarded by @ConditionalOn... annotations.

@ConditionalOnClass      → only if this class is on the classpath
@ConditionalOnMissingBean → only if this bean is not already defined
@ConditionalOnProperty   → only if this property is set

Example:
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
public class DataSourceAutoConfiguration {
@Bean
public DataSource dataSource() {
return new HikariDataSource(...);
}
}

HikariCP on classpath → DataSource auto-created.
You define your own DataSource bean → auto-config backs off.

Convention over configuration:
Say nothing → Spring picks a sensible default.
Say something → Spring uses what you said.

Debugging auto-configuration:
java -jar app.jar --debug

Or in application.yml:
logging.level.org.springframework.boot.autoconfigure=DEBUG

Output shows:
DataSourceAutoConfiguration   → MATCHED (HikariCP on classpath)
MongoAutoConfiguration        → DID NOT MATCH (MongoClient not on classpath)

A bean is not being created and you don't know why → this is the first thing to check.

## Profiles — Dev / Test / Prod Separation

File structure:
application.yml          → shared config, always loaded
application-dev.yml      → dev-specific
application-test.yml     → test-specific
application-prod.yml     → prod-specific

application.yml:
spring:
application:
name: tirebringin
server:
port: 8080

application-dev.yml:
spring:
datasource:
url: jdbc:mysql://localhost:3306/tirebringin_dev
username: root
password: root
jpa:
show-sql: true
logging:
level:
root: DEBUG

application-prod.yml:
spring:
datasource:
url: jdbc:mysql://${DB_HOST}:3306/tirebringin
username: ${DB_USER}
password: ${DB_PASSWORD}
logging:
level:
root: WARN

Prod values come from environment variables — never hardcoded in config files.

Activating a profile:
java -jar app.jar --spring.profiles.active=prod

Environment variable:
SPRING_PROFILES_ACTIVE=prod

Kubernetes (Rakuten pattern):
env:
- name: SPRING_PROFILES_ACTIVE
  value: prod

Profile-specific beans:
@Configuration
@Profile("dev")
public class MockEmailConfig {
@Bean
public EmailService emailService() {
return new MockEmailService(); // no real emails in dev
}
}

@Configuration
@Profile("prod")
public class RealEmailConfig {
@Bean
public EmailService emailService() {
return new SmtpEmailService();
}
}

## Secret Management

Secrets — DB passwords, API keys, credentials — never go into config files.

Level 1 — Environment Variables (minimum acceptable):
spring:
datasource:
password: ${DB_PASSWORD}
Value comes from OS or container runtime. No plain text in files.

Level 2 — Kubernetes Secrets:
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
name: db-secret
data:
password: cGFzc3dvcmQ=  # base64 encoded

# deployment.yaml
env:
- name: DB_PASSWORD
  valueFrom:
  secretKeyRef:
  name: db-secret
  key: password

Kubernetes Secret → injected as environment variable → Spring reads via ${DB_PASSWORD}.
Jenkins pipeline maps secrets at build/deploy time — never stored in source code.

Level 3 — Vault / AWS Secrets Manager (enterprise):
Centralized secret store. Secrets are rotatable, access-controlled, audit-logged.
Spring Cloud Vault integrates directly — secrets pulled at startup.

## Config Precedence

Same property defined in multiple places — Spring uses this priority order:

1. Command line args          → highest priority
   --server.port=9090
2. Environment variables
   SERVER_PORT=9090
3. application-{profile}.yml
4. application.yml            → lowest priority

Override prod config without touching files:
Set an environment variable → takes precedence over application-prod.yml.
Used heavily in Kubernetes deployments — config files stay clean.

## Actuator — Observability

Production-ready endpoints to inspect application internals.

dependency: spring-boot-starter-actuator

application.yml:
management:
endpoints:
web:
exposure:
include: health, info, metrics, env
endpoint:
health:
show-details: always

Key endpoints:
/actuator/health   → application health (DB, disk, custom checks)
/actuator/metrics  → JVM, HTTP request, custom metrics
/actuator/env      → active config values
/actuator/beans    → all Spring beans in context
/actuator/mappings → all HTTP endpoints

Health Groups — critical for Kubernetes:
management:
endpoint:
health:
group:
liveness:
include: livenessState
readiness:
include: readinessState, db, redis

/actuator/health/liveness  → Kubernetes liveness probe
/actuator/health/readiness → Kubernetes readiness probe

Liveness:
Is the application alive? Did it crash or deadlock?
Fails → Kubernetes restarts the pod.
Should be simple — if the JVM is running and not deadlocked, this passes.

Readiness:
Is the application ready to receive traffic?
Checks: DB connection alive, cache warmed up, external dependencies reachable.
Fails → Kubernetes stops routing traffic to this pod, does not restart it.
Pod stays running but receives no requests until readiness passes again.

Rakuten connection:
Kubernetes deployment had readiness probe configured.
Pod did not receive reservation traffic until readiness passed —
DB connection pool initialized, ActiveMQ connection established.

Actuator security:
Never expose actuator on the public port in production.

Option 1 — separate management port:
management:
server:
port: 8081
Port 8081 stays behind internal network, not exposed via load balancer.

Option 2 — Spring Security on actuator endpoints:
Restrict /actuator/** to admin role only.

## Starter Structure

spring-boot-starter-* dependencies are curated dependency sets.
Compatible versions, tested together, no version conflicts.

spring-boot-starter-web      → Spring MVC + Tomcat + Jackson
spring-boot-starter-data-jpa → Spring Data + Hibernate + HikariCP
spring-boot-starter-security → Spring Security
spring-boot-starter-test     → JUnit + Mockito + AssertJ + Spring Test

Add the starter → Spring Boot picks the right versions.
Override a version explicitly only when you have a reason.

## Twelve-Factor App Alignment

Spring Boot naturally follows Twelve-Factor App principles:

Config in environment → profiles + environment variables
Logs as event streams → structured logging to stdout, collected by platform
Port binding         → embedded Tomcat, no external server needed
Dev/prod parity      → profiles make environments consistent

Interview checklist:
→ How does auto-configuration work? → @ConditionalOn* annotations, classpath scanning
→ How do you separate dev and prod config? → profiles + environment variables
→ Where do secrets go? → environment variables, Kubernetes Secrets, Vault — never in files
→ What is the difference between liveness and readiness? → crash detection vs traffic readiness
→ How do you debug why a bean is not created? → --debug flag, auto-config report
→ What does a Spring Boot starter do? → curated, compatible dependency set