# Java version of flowing-retail/rest

This example demonstrates stateful resilience patterns in a REST environment. A payment (micro-)service can retrieve payments if called via REST. It requires an upstream REST service to charge credit cards.

![REST callstack](docs/situation.png)

This simple call-chain is great to demonstrate various important resilience patterns.

#See [introduction](../../README.md) for the storyline / patterns behind

#Concrete technologies/frameworks in this folder:

* Java 8
* Spring Boot 1.5.x
* Hystrix
* Camunda 7.x

# How-to run

First you have to startup the stripe fake server, as this handles the credit card payments.

```
mvn -f ../stripe-fake/ exec:java
```

Now you can run the payment service itself

```
mvn exec:java
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


## Hint on using Camunda Enterprise Edition

For Camunda there is an enterprise edition available with [https://camunda.com/products/cockpit/#/features](additional features in Cockpit) (the monitoring tool). It is quite handy to use this when playing around with the example. You can easily switch to use enterprise edition:

* Get a trial license if you don't have a license yet: https://camunda.com/download/enterprise/
* Adjust Camunda version used in pom: [./pom.xml#L12](./pom.xml#L12), [./pom.xml#L50](./pom.xml#L50)

Note that you do not need the enterprise edition to run the examples, the community edition will also do fine. But because of less features you do not see historical workflow instances - and that means you do not see that much in Camunda Cockpit if everything runs smoothly.
