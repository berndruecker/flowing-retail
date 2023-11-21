# Flowing Retail / REST

This folder contains services that connect via REST. Currently, this is reduced to showcasing resilience patterns.

A good background read is this InfoWorld article [3 common pitfalls of microservices integration—and how to avoid them](https://www.infoworld.com/article/3254777/application-development/3-common-pitfalls-of-microservices-integrationand-how-to-avoid-them.html)

# Sample service demonstrating stateful resilience patterns in a REST environment

This sample REST (micro-)service effects payments in response to a PUT call. It requires an upstream REST service that charges credit cards.

![REST callstack](../docs/resilience-patterns/situation.png)

This simple call-chain is perfect for demonstrating important resilience patterns.

The following technologies are used:

* Java 17
* Spring Boot 3.1.x
* Resilience4j 2.1
* Camunda 8.x

# How-to run

First you have to startup the "[Stripe Fake Server](java/stripe-fake)", as this handles the credit card payments:

```
mvn -f java/stripe-fake/ exec:java
```

Now you can run the [Payment Service](java/payment/) itself:

```
mvn -f java/payment/ exec:java
```

Now the different versions of the payment service are available:

* http://localhost:8100/api/payment/v1
* ...
* http://localhost:8100/api/payment/v6

You now can issue a PUT with an empty body:

```
curl \
-H "Content-Type: application/json" \
-X PUT \
-d '{}' \
http://localhost:8100/api/payment/v1
```


## Using Camunda 

All examples from version 3 and above use Camunda as a workflow engine, so you need a Camunda instance to run those. The easiest way to play around is to get a test account in the [Camunda SaaS offering](https://camunda.io/), [create a cluster and API credentials](https://docs.camunda.io/docs/next/guides/orchestrate-microservices/#create-a-cluster) and add them to the [application.properties](java/payment/src/main/resources/application.properties#L3-L6).









# Storyline

See **Fail fast is not enough**: https://blog.bernd-ruecker.com/fail-fast-is-not-enough-84645d6864d3

Let's assume a scenario where the upstream credit card service still responds, but its very slow. With no resilience pattern in place, this is the worst thing that can happen - as now the payment service will call the credit card service and block until it gets a response. As this take a long time, all threads from the payment service are held hostage, and the payment service will eventually time out for its clients. Tiny failures somewhere in your system might blow up your whole system:

![V1](../docs/resilience-patterns/v1.png)

* Java: [PaymentRestHacksControllerV1.java](java/payment/src/main/java/io/flowing/retail/payment/rest/PaymentRestHacksControllerV1.java)

## Fail fast

A simple mitigation is to apply a **fail fast** pattern like [**circuit breaker**](https://martinfowler.com/bliki/CircuitBreaker.html). In this example I use [Resilience4J](https://resilience4j.readme.io/). If a service responds too slowly, the circuit breaker interrupts and the payment service gets a failure right away. This way you make sure the overall system is still responding, even if functionality degrades (meaning: we cannot charge credit cards).

![V2](../docs/resilience-patterns/v2.png)

* Java: [PaymentRestHacksControllerV2.java](java/payment/src/main/java/io/flowing/retail/payment/rest/PaymentRestHacksControllerV2.java#L24)

There is also an extended version adding some stateless retrying to the mix also using Resilience4J. This can be usefuly to mitigate problems with a flaky network:

* Java: [PaymentRestHacksControllerV2.java](java/payment/src/main/java/io/flowing/retail/payment/rest/PaymentRestHacksControllerV2b.java#L27)



## Fail fast is not enough

Failing fast is good, but it is not enough. Frequently, a retry after the credit card service has been fixed resolves the situation (if the service was in a hard failure mode) - or a retry may discover that the earlier attempt succeeded, but took an abnormal amount of time (if the service is in a degraded performance mode). This retry needs to be stateful to not only retry right away but again in a couple of minutes, hours or even days. Keeping this stateful retry local to the payment service reduces overall architectural complexity.

![V3](../docs/resilience-patterns/v3.png)

In the example, I use the [Camunda workflow engine](http://camunda.com/) to do the stateful retry reliably.

* Java: [PaymentRestHacksControllerV3.java](java/payment/src/main/java/io/flowing/retail/payment/rest/PaymentRestHacksControllerV3.java)

## Keep synchronous responses

The processing just got asynchronous, which is often not wanted. In this scenario you could very well return a synchronous response whenever the credit card service is available, but switch to asynchronicity when it is not.

![V4](../docs/resilience-patterns/v4.png)

HTTP supports this via return codes: `200 OK` means "_all OK_", `202 ACCEPTED` means "_I'll call you back later_".

![Sync vs. async](../docs/resilience-patterns/syncAsync.png)

* Java: [PaymentRestHacksControllerV4.java](java/payment/src/main/java/io/flowing/retail/payment/rest/PaymentRestHacksControllerV4.java)


## Asynchronous work distribution without messaging

An alternative to synchronously calling an upstream service is to communicate asynchronously. The default would be messaging.

This example shows a successful approach taken by many customers: using the workflow engine as work distribution, behaving like a queue. This  leverages the [External Tasks](https://docs.camunda.org/manual/latest/user-guide/process-engine/external-tasks/) pattern.

![Microservices](../docs/resilience-patterns/v5.png)

* Java
    * Workflow model: [payment5.bpmn](java/payment/src/main/resources/payment5.bpmn) (hint: use the free [Camunda Modeler](https://camunda.com/download/modeler/) to show this model graphically)
    * Worker Node.js: [index.js](java/payment/node-customer-credit-worker/index.js)
    * Worker in Java (alternative): [CustomerCreditWorker.java](java/payment/src/main/java/io/flowing/retail/payment/worker/CustomerCreditWorker.java)


## Business transactions using compensation

The last part of the example adds compensation to the game. In distributed systems, ACID transactions are not applicable (or at least do not scale well). Using compensation is the alternative - meaning that you reliably undo already executed work if something later on fails.

![Microservices](../docs/resilience-patterns/v6.png)

See [payment6.bpmn / Java](java/payment/src/main/resources/payment6.bpmn) and [PaymentV6.bpmn / C#](csharp/payment/Models/PaymentV6.bpmn) for the workflow

