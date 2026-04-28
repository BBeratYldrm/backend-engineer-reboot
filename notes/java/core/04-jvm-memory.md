# [1.3] JVM & Memory

## Three Memory Areas

HEAP:
→ All objects live here (new User(), new ArrayList())
→ Managed by Garbage Collector
→ Large area — typically GBs
→ OutOfMemoryError when full and GC can't free space

STACK:
→ Method calls and local variables
→ Each thread has its own stack
→ New frame added per method call, removed when method returns
→ StackOverflowError when too deep (infinite recursion)

METASPACE (Java 8+, replaced PermGen):
→ Class definitions and metadata
→ Static variables
→ Method bytecode
→ Grows dynamically — rarely causes OOM

## Stack vs Heap

Stack:
int x = 5;          // stack — primitive, local variable
String s = "hello"; // s reference on stack, "hello" on heap

Heap:
new User()          // object on heap
new ArrayList<>()   // object on heap

## StackOverflowError vs OutOfMemoryError

StackOverflowError:
→ Too many nested method calls
→ Usually infinite recursion (missing base case)
→ Stack full

OutOfMemoryError:
→ Heap full
→ Too many objects, GC can't free enough
→ Usually a memory leak

## JVM Memory Model — Happens-Before

CPUs and compilers can reorder instructions.
Without happens-before, threads may see stale values.

volatile boolean ready;
→ Guarantees visibility — always read from main memory, not CPU cache
→ Establishes happens-before relationship

Use volatile/synchronized/concurrent classes
when sharing data between threads.

## GC Types

G1 GC (default Java 9+):
→ General purpose, good for most apps
→ Splits heap into regions
→ Predictable pause times

ZGC (Java 15+):
→ Low-latency — pause < 1ms
→ Large heaps (multi-GB)
→ Fintech, trading systems → ZGC preferred

Stop-the-world:
→ App pauses while GC runs
→ Modern GC minimizes this
→ ZGC: nearly zero pause

## GC Tuning Basics

-Xms → initial heap size
-Xmx → max heap size
-Xms == -Xmx → avoid resizing overhead (production best practice)

Allocation pressure:
Creating many short-lived objects → GC works harder
Solution: object reuse, avoid unnecessary object creation

## Escape Analysis

JVM optimization — if object doesn't "escape" the method,
JVM may allocate it on stack instead of heap.
→ No GC needed → faster

public int sum(int a, int b) {
Point p = new Point(a, b); // may go to stack, not heap
return p.x + p.y;
}

Rakuten connection:
Tirebringin under load → many objects created per reservation
GC pauses visible in Gatling p95 response times
Heap sizing: -Xms and -Xmx should be equal in production

Real world:
Revolut → ZGC for low-latency financial operations
Netflix → G1 GC with tuned heap for streaming
Uber → monitors GC pause time as key metric

Interview tip:
"The JVM has three main memory areas: Heap for objects,
Stack for method frames and local variables, Metaspace for class definitions.
OutOfMemoryError means heap is full — usually a memory leak.
StackOverflowError means too many nested calls — usually infinite recursion."

The question I ask myself:
"Is this OutOfMemoryError or StackOverflowError?"
OutOfMemory → check for memory leaks, large object retention
StackOverflow → check for infinite recursion, missing base case

