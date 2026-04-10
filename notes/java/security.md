# Security Fundamentals

## Cookie Properties
HttpOnly → JavaScript cannot access cookie (XSS protection)
Secure   → Cookie only sent over HTTPS (Man-in-the-middle protection)
SameSite → Cookie only sent from same site (CSRF protection)
Expires  → When cookie is deleted
No expires = session cookie (deleted when browser closes)
With expires = persistent cookie (stays for days/weeks)

## Session vs Cookie
Cookie: small text stored in browser — just a key (sessionId=abc123)
Session: actual data stored on server (Redis/Memory)

Browser only knows the key.
Server looks up the key → finds the data.
User cannot see or modify their own session data.

## XSS — Cross Site Scripting
Attacker injects malicious script into a page.
Script steals cookies → account hijacked.
Fix: HttpOnly → JavaScript cannot read cookies.

## CSRF — Cross Site Request Forgery
Attacker tricks browser into making unwanted requests.
Browser automatically sends cookies → server thinks it's the user.

Fix — CSRF Token:
Server generates random token per form.
Token included as hidden field.
Server validates token on submit.
Attacker cannot know the token → request rejected.

In Spring + Thymeleaf:
th:action automatically includes CSRF token.
Spring Security validates it automatically.

REST APIs don't need CSRF:
JWT sent in Authorization header (not cookie).
Browsers don't auto-send headers → no CSRF risk.

## Session vs JWT

Session → Stateful:
+ Server stores everything (Redis)
+ Instant logout — just delete session
- Hard to scale horizontally
- Redis dependency

JWT → Stateless:
+ Server stores nothing
+ Easy to scale — any server can verify
+ Mobile/microservices friendly
- Logout doesn't work instantly (wait for expiry)
- Solution: short expiry + refresh token

When to use:
Session → Monolith, server-side rendering (Thymeleaf), instant logout required
JWT    → REST API, microservices, mobile apps

## JWT Weakness & Solution

Weakness:
Stolen JWT cannot be invalidated — no server-side record.
Token stays valid until expiry.
Logout doesn't work instantly.

Solution — Refresh Token in DB/Redis:
Access token  → short lived (15 min), stateless, not stored
Refresh token → long lived, stored in DB or Redis

On logout:
→ Delete refresh token from DB/Redis
→ No new access token can be obtained
→ Existing access token expires in 15 min

Trade-off:
Not fully stateless anymore — but only refresh token is stored.
Access token validation still stateless → performance maintained.

## JWT Structure
eyJhbGc.eyJzdWI.signature

Header:    algorithm (HS256)
Payload:   data (userId, role, expiry) — Base64 encoded, NOT encrypted
Signature: Header + Payload + Secret → tamper detection

Bearer token:
Authorization: Bearer eyJhbGc...
"Bearer" = trust whoever carries this token

## Access Token + Refresh Token
Access Token:
→ Short lived (15 minutes)
→ Sent with every request
→ If stolen → expires soon

Refresh Token:
→ Long lived (7-30 days)
→ Only used to get new access token
→ Stored in HttpOnly cookie (JS cannot access)
→ Stored on server → can be revoked (logout works)

## OAuth2
Problem: "Login with Google" without giving password to third party.

Flow:
User → App → "Go to Google"
User → Google → Login + Approve
Google → App → Token
App → Google API → Get user info

Real usage at Rakuten:
Rakuten SSO (omni) — users login with Rakuten account
App receives token → uses it to identify user
Config: client-id, client-secret, auth endpoint, token endpoint

## Spring Security Config
@EnableWebSecurity
SecurityFilterChain:
→ .permitAll() — public endpoints (login, register)
→ .hasRole("ADMIN") — role based access
→ .authenticated() — any logged in user
→ SessionCreationPolicy.STATELESS — for JWT (no session)
→ addFilterBefore(jwtFilter) — validate JWT on every request

## Method Level Security
@PreAuthorize("hasRole('ADMIN')")
→ Only ADMIN can access this method

@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
→ ADMIN or the user themselves