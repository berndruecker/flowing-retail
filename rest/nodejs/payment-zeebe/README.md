# Node.js Express version of flowing-retail/rest

This example demonstrates stateful resilience patterns in a REST environment. A payment (micro-)service can retrieve payments if called via REST. It requires an upstream REST service to charge credit cards.

![REST callstack](../../../docs/resilience-patterns/situation.png)

This simple call-chain is great to demonstrate various important resilience patterns.

**See [introduction](../../) for the storyline / patterns behind this demo.**

# Concrete technologies/frameworks in this folder:

* Node.js
* Express
* TypeScript
* [Brakes](https://github.com/awolden/brakes)
* Zeebe on [Camunda Cloud](https://camunda.io)

## Install

```
pnpm i
```

## Run

With Camunda Cloud: 

* Copy the environment variables from the [Camunda Cloud Console](https://camunda.io) for your client connection.
* Set them in your environment.
* Follow the instructions in [stripe-fake/README.md](../stripe-fake/README.md) to start the upstream credit card charging service.
* Start this microservice: 

```
npm run start
```

Now the different versions of the payment service are available:

* http://localhost:8100/api/payment/v1
* ...
* http://localhost:8100/api/payment/v6

You can now issue a `PUT` with an empty body:

```
curl \
-H "Content-Type: application/json" \
-X PUT \
-d '{}' \
http://localhost:8100/api/payment/v1
```