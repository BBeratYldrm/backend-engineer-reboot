# Day 80 — July 6, 2026

## What I did today

Revisited SLI/SLO/SLA from scratch — SLI as the measured metric, SLO as the
internal target, SLA as the contractual version. Reinforced error budget
concept and PromQL basics (rate() over a counter metric for error rate).

Started implementation-speed practice for an upcoming fintech-style coding
assessment (CodeSignal-format: CSV input/output, decimal precision, custom
classes instead of raw lists):
- BigDecimal fundamentals — String constructor vs double constructor pitfall,
  multiply/divide with explicit scale and RoundingMode, setScale for output
  formatting
- Practiced a small end-to-end example: monthly interest calculation applied
  to a balance
- Walked through an Account Balance Summary problem — custom record for
  transactions, HashMap vs TreeMap for key ordering, malformed line handling
  via split() length check + try/catch around parsing
- Clarified the distinction between ordering data structures (TreeMap for
  key-sorted output) vs queue structures (Deque/BlockingQueue for
  processing order) — these solve different problems and shouldn't be
  conflated

Completed a skills-matching response form for a new contract role
application (staffing agency) — filled out technology matrix based on real
production experience, flagged gaps between production experience and
system-design-level theoretical knowledge (BigQuery/GCS/Dataproc, MongoDB)
honestly rather than overstating. Negotiated hourly rate for the contract
and reached agreement before submitting.

Conducted research on a target company's technical assessment process and
question patterns (coding challenge format, common pitfalls: decimal
precision, over-engineering, resume-credibility checks) to prepare for a
potential 1-week intensive prep sprint.

Weighing two engineering tracks at the same target company — decision
pending after a recruiter call tomorrow.

## Next

- Continue implementation-speed practice this evening — small, timed,
  CSV/custom-class/BigDecimal problems using only already-known tools
  (HashMap, loops, String parsing)
- SD-08 remaining topic — reversed timestamp / HBase row key
- Algorithm: Two Pointer — 392. Is Subsequence (resume when ready)
- Mock interview when interview date confirmed
- Decide between the two engineering tracks after tomorrow's call