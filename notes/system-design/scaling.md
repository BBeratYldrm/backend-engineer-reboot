# Scaling Basics

## Vertical Scaling (Scale Up)
- Increase power of a single server (CPU, RAM)
- Simple but limited
- Single point of failure

## Horizontal Scaling (Scale Out)
- Add more servers
- Better scalability
- Requires load balancing

## Load Balancer
- Distributes incoming traffic across servers
- Prevents overload on a single server
- Improves availability

## Stateless System
- No session stored in server
- Each request is independent
- Easier to scale horizontally

## Basic Architecture

Client
↓
Load Balancer
↓
Server 1
Server 2
Server 3
↓
Database