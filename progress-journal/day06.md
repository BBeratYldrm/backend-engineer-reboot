# Day 06 — April 10, 2026

## What I learned

### [2.4] Spring Data & JPA
- Dirty Checking — auto UPDATE when @Transactional is present, no save() needed
- readOnly=true — read-only, Dirty Checking disabled
- N+1 problem — lazy loading trap, fixed with JOIN FETCH
- Projection — interface and DTO projection, fetch only what you need
- Specification — dynamic query, optional filter combinations
- Pagination — Pageable, deep pagination trap, cursor based solution

### Security Fundamentals
- Cookie — HttpOnly, Secure, SameSite, Expires
- Session vs Cookie — key on browser, data on server
- XSS — script injection, protected by HttpOnly
- CSRF — token protection, why REST APIs don't need it
- Session vs JWT — stateful vs stateless, when to use which
- JWT — structure, Bearer token, signature verification
- Access Token + Refresh Token — JWT weakness and solution
- OAuth2 — Authorization Code Flow, connected to Rakuten SSO
- Spring Security — SecurityFilterChain, @PreAuthorize

## How I feel
Long day. Brain is very full.
Security finally clicked — especially OAuth2 and JWT.
Saw my own Rakuten code and understood what it was doing.

## Tomorrow
- Security quiz
- Algorithm practice
- Week review — Feynman style