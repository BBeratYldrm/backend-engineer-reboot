# [2.5] Spring Security

## Authentication vs Authorization

Authentication (AuthN): proving who you are.
"Who are you?" → login, JWT verification, API key check.

Authorization (AuthZ): what you are allowed to do.
"Can you access this?" → comes after authentication.

User logged in → AuthN passed
User tries to access admin page → AuthZ failed → 403 Forbidden

401 → not authenticated (prove who you are first)
403 → authenticated but not authorized (I know who you are, you cannot do this)

## Security Filter Chain

Spring Security is a chain of filters. Every request passes through.

Request
→ BearerTokenAuthenticationFilter  (JWT validation)
→ ExceptionTranslationFilter        (catches auth errors)
→ FilterSecurityInterceptor         (authorization check)
→ Controller

Configuration:
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

## JWT — JSON Web Token

Three parts separated by dots:
HEADER.PAYLOAD.SIGNATURE

Header: algorithm info
{ "alg": "HS256", "typ": "JWT" }

Payload: claims
{
"sub": "user123",
"roles": ["USER", "ADMIN"],
"iat": 1714000000,
"exp": 1714003600
}

Signature: header + payload signed with secret key
→ verifies the token has not been tampered with

Signed but NOT encrypted.
Payload is base64 decoded by anyone.
Never put sensitive data in JWT — no passwords, no card numbers.

Every request:
Authorization: Bearer <access_token>

## Access Token vs Refresh Token

Access Token:
→ Short-lived: 15 minutes to 1 hour
→ Sent on every API request in Authorization header
→ If stolen: damage is limited — expires soon

Refresh Token:
→ Long-lived: 7 to 30 days
→ Used only to obtain a new access token
→ Stored in DB — can be revoked
→ Stored in HttpOnly cookie — JS cannot access → XSS protection

Token renewal flow:
1. Access token expires
2. Client sends refresh token to /auth/refresh
3. Server checks refresh token in DB (valid? revoked?)
4. Valid → new access token returned
5. Invalid → 401 → user must log in again

Rotation:
Refresh token used → new one issued, old one invalidated.
Stolen refresh token used once → system detects it → invalidated.

## JWT Revocation Problem

JWT is stateless — it cannot be revoked by default.
User logs out → access token still valid until expiry.

This is a JWT trade-off. Solutions:

1. Short access token lifetime (simplest):
   15 minutes → stolen token causes limited damage.
   No extra infrastructure needed.

2. Redis blacklist:
   // On logout:
   redisTemplate.opsForValue().set(
   "blacklist:" + token,
   "revoked",
   Duration.ofMinutes(15)
   );

   // On every request:
   if (redisTemplate.hasKey("blacklist:" + token)) {
   throw new TokenRevokedException();
   }

   Trade-off: revocation possible but every request hits Redis → added latency.

3. Short access token + refresh token revocation:
   Access token: 15 minutes → no need to revoke.
   Refresh token: stored in DB → revocable.
   Logout → refresh token deleted → no new access token can be issued.
   User fully logged out within 15 minutes.

## OAuth2 — Authorization Code Flow

OAuth2: authorization protocol — not authentication, but granting permission.
OpenID Connect (OIDC): identity layer on top of OAuth2.

Actors:
Resource Owner    → the user
Client            → your application
Authorization Server → Google, GitHub, Keycloak
Resource Server   → your API

Flow:
1. User clicks "Login with Google"

2. Client redirects to Authorization Server:
   https://accounts.google.com/oauth/authorize
   ?client_id=xxx
   &redirect_uri=https://app.com/callback
   &response_type=code
   &scope=openid email profile
   &state=random123  ← CSRF protection

3. User logs in at Google, grants permission

4. Authorization Server redirects back:
   https://app.com/callback?code=AUTH_CODE&state=random123

5. Client exchanges code for token (backend to backend, secret channel):
   POST /oauth/token
   { code: AUTH_CODE, client_secret: xxx }

6. Authorization Server returns tokens:
   { access_token, refresh_token, id_token }

7. Client calls Resource Server:
   Authorization: Bearer <access_token>

Why two steps — why not use the auth code directly?
Auth code appears in the URL → browser history, server logs, referrer headers.
Token exchange happens backend to backend → never visible in URL.
client_secret sent only in this backend call → never exposed to frontend.

state parameter → CSRF protection.
Sent in step 2, verified in step 4.
Different value returned → CSRF attack → reject request.

## PKCE — Proof Key for Code Exchange

For mobile apps and SPAs that cannot safely store client_secret.
(client_secret in an APK → anyone who decompiles it finds the secret)

Flow:
1. Client generates random string → code_verifier
2. code_challenge = SHA256(code_verifier)
3. Authorization request includes code_challenge (visible in URL, safe)
4. Auth code returned
5. Token request includes code_verifier (sent to backend)
6. Server: SHA256(code_verifier) == code_challenge? → match → token issued

No client_secret needed.
Attacker who intercepts the auth code cannot exchange it
without knowing the code_verifier — which only exists on the original client.
Mathematical proof instead of a shared secret.

## Client Credentials Flow — Service to Service

No user involved. Two services communicating securely.

Service A → Authorization Server:
POST /oauth/token
{ grant_type: client_credentials, client_id, client_secret }

Authorization Server → Service A:
{ access_token }

Service A → Service B:
Authorization: Bearer <access_token>

Used for internal service-to-service calls.
No user session, no refresh token — just machine credentials.

## Method-Level Security

Authorization at method level, not just endpoint level.

@Configuration
@EnableMethodSecurity
public class SecurityConfig { }

@Service
public class OrderService {

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(Long id) { ... }

    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    public Order getOrder(Long userId, Long orderId) { ... }
    // user can only access their own orders

    @PostAuthorize("returnObject.userId == authentication.principal.id")
    public Order findOrder(Long orderId) { ... }
    // method runs, then checks returned object belongs to caller
}

@PreAuthorize  → check before method runs
@PostAuthorize → check after method runs, on the returned object

Policy lives in the service — controller stays clean.

## Multi-Tenant Authentication

Multiple customers (tenants) on the same system, data fully isolated.

Tenant resolution — identifying which tenant:
Subdomain: tenant1.app.com
Header: X-Tenant-ID: tenant1
JWT claim: { "tenantId": "tenant1", "sub": "user123" }

Data isolation strategies:
Row-level:    same table, tenant_id column on every row
WHERE tenant_id = :currentTenant on every query
Most common — simple, scales well
Schema-level: separate DB schema per tenant
More isolated, more complex migrations
DB-level:     separate DB instance per tenant
Maximum isolation, maximum cost

Row-level implementation:
Current tenant stored in ThreadLocal at request start.
Hibernate @Filter or custom interceptor adds tenant_id condition automatically.
Every query filtered — tenant cannot see another tenant's data.

## Interview Checklist
→ AuthN vs AuthZ? → who are you vs what can you do
→ JWT structure? → header.payload.signature, signed not encrypted
→ Access vs refresh token? → short-lived API token vs long-lived renewal token
→ JWT revocation problem? → stateless by nature, solutions: short TTL, Redis blacklist, refresh revocation
→ Why two steps in OAuth2? → auth code in URL is unsafe, token exchange in backend secret channel
→ What is PKCE? → mathematical proof replacing client_secret for mobile/SPA
→ Client credentials flow? → service to service, no user involved
→ @PreAuthorize vs @PostAuthorize? → before method vs after method on return value
→ Multi-tenant data isolation? → row-level most common, schema/DB for stricter isolation