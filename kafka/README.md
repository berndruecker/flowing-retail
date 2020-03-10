# Flowing Retail / Apache Kafka

This folder contains services that connect to Apache Kafka as means of communication between the services.

![Microservices](../docs/kafka-services.png)

The nice thing about this architecture is, that Kafka is the only common denominator. For every service you can freely decide for

* **programming language** and
* **workflow engine**.

You find the variations in the sub folders. Note that I do not yet have implemented all possible combinations. So you might want to start with:

* [Java and Camunda BPM 7](java/)
* [Java and Zeebe](java/)


## Concrete technologies/frameworks:

### Java

* Java 8
* Spring Boot 2.1.x
* Spring Cloud Streams

And of course
* Apache Kafka
* Camunda or Zeebe

## Communication of services

The services have to collaborate in order to implement the overall business capability of order fulfillment. This example focues on:

* *Asynchronous* communication via Apache Kafka
* *Event-driven* wherever appropriate
* Sending *Commands* in cases you want somebody to do something, which involves that events need to be transformed into events from the component responsible for, which in our case is the Order service:

![Events and Commands](../docs/event-command-transformation.png)
