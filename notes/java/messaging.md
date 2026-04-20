# [3.4] Messaging — ActiveMQ & Kafka

## Why Message Queue?

Problem: reservation saved → need to send email
Direct approach: send email inside the transaction → slow, risky

Solution: Message Queue
Producer → puts message in queue → returns immediately
Consumer → picks up message → sends email async

Three reasons to use MQ:
1. Performance → user doesn't wait, system returns instantly
2. Transactional independence → DB commit and email are separate
   Email cannot be rolled back — must be outside @Transactional
3. Durability → if server is down, messages wait in queue
   When server comes back → processes them in order

## Producer → Queue → Consumer

Producer: the service that puts message in queue
(tirebringin-front → ReserveResultService)

Queue: ActiveMQ — just a postman, doesn't read the message
message format at Rakuten: "1:reservationNum"
1 = mail type, reservationNum = reservation ID

Consumer: the service that reads and processes
(batch service → reads queue → builds and sends email)

ActiveMQ does NOT send emails — it only transports messages.
The consumer does the actual work.

## High Availability — Failover

spring.activemq.broker-url:
failover:(tcp://server1:61616, tcp://server2:61616)

If server1 goes down → automatically switch to server2
If both down → messages wait → processed when connection restored