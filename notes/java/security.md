# Security — What I Actually Understand Now

## Cookie
A small text stored in the browser. Just a key, nothing more.
Three properties matter:

HttpOnly — JavaScript can't touch it.
Even if someone injects a script into my page,
they can't steal the cookie. (XSS protection)

Secure — Only travels over HTTPS.
Even on public WiFi, no one can intercept it.
(Man-in-the-middle protection)

SameSite — Only sent from the same site.
A malicious form on another site can't trigger
requests using my cookie. (CSRF protection)

Expires — When does it die?
No expiry = gone when browser closes (session cookie)
With expiry = stays for days/weeks (like Instagram keeping me logged in)

---

## Session vs Cookie — What's the difference?

Cookie is just a key. The real data lives on the server.

Browser holds:   sessionId=abc123
Server holds:    abc123 → { userId:1, name:"Berat", cart:[...] }

I only know my key. I can't see or change what's behind it.
Server controls everything.

---

## XSS — Cross Site Scripting
Attacker sneaks a malicious script into a page I visit.
Script tries to steal my cookie.
Fix: HttpOnly. Script can't read HttpOnly cookies. Done.

---

## CSRF — Cross Site Request Forgery
I'm logged into my bank. Cookie is sitting in my browser.
Attacker sends me a link. I click it.
That page submits a hidden form to my bank — with my cookie attached.
Bank thinks it's me.

Fix: CSRF token.
Server puts a secret random token in every form.
Attacker doesn't know the token → request rejected.

In Spring + Thymeleaf:
th:action adds the token automatically. I don't even think about it.

REST APIs don't need this:
JWT goes in the Authorization header — not a cookie.
Browser doesn't send headers automatically.
Attacker can't trigger it. No CSRF risk.

---

## Session vs JWT

Session (stateful):
Server remembers everything. Redis holds the data.
Logout works instantly — just delete the session.
Hard to scale. Redis goes down = everyone logged out.

JWT (stateless):
Server remembers nothing. The token carries everything.
Any server can verify it — just check the signature.
Scales easily. But logout is tricky.

When I'd choose Session:
Thymeleaf app, monolith, need instant logout.
(That's what Rakuten tirebringin used — Spring Session + Redis)

When I'd choose JWT:
REST API, microservices, mobile app.

---

## JWT — How It Works

Three parts, separated by dots:
eyJhbGc.eyJzdWI.signature

Header    → which algorithm (HS256)
Payload   → the data (userId, role, expiry) — Base64 encoded, NOT encrypted
Signature → Header + Payload + Secret key → proves nobody tampered with it

Every request:
Authorization: Bearer eyJhbGc...
"Bearer" just means "trust whoever carries this"

Server receives it:
1. Verify signature with secret key
2. Read payload
3. Done — no DB call needed

---

## JWT Weakness

Token gets stolen. Server has no record of it.
Token stays valid until it expires.
Can't invalidate it instantly.

Fix — two tokens:
Access token  → short lived (15 min), stateless, not stored anywhere
Refresh token → long lived (7-30 days), stored in DB or Redis

On logout:
Delete refresh token from DB/Redis.
No new access token can be obtained.
Existing access token dies in 15 minutes.

Trade-off: not fully stateless anymore.
But only the refresh token is stored — access token stays fast.

---

## OAuth2 — Login Without Sharing Password

Problem: I want to "Login with Google" but I'm not giving my
Google password to a random app.

How it works:
1. I click "Login with Rakuten" on the app
2. App redirects me to Rakuten's login page
3. I log in on Rakuten's site (not the app)
4. Rakuten asks: "This app wants to access your info. OK?"
5. I approve
6. Rakuten gives the app a temporary code
7. App exchanges that code for tokens
8. App uses access token to get my info from Rakuten API

I never gave my Rakuten password to the app. Rakuten handled auth.
The app just got a token saying "this user is who they say they are."

Real usage:
In tirebringin, this was the Omni SSO flow.
AuthLoginController → redirects to Rakuten login →
comes back with code → exchanges for token → gets user info.

---

## Spring Security — Quick Reference

SecurityFilterChain controls who can access what:
→ permitAll()    — anyone (login page, public pages)
→ hasRole()      — only specific roles
→ authenticated() — any logged-in user

@PreAuthorize on methods:
→ @PreAuthorize("hasRole('ADMIN')") — method-level protection
→ @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
— admin OR the user themselves