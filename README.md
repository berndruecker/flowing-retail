# Flowing Retail

This sample application demonstrates a simple order fulfillment system decomposed into multiple independant components (e.g. microservices).

There is code for multiple implementation alternatives to allow a broad audience to undestand the code or to compare alternatives. The table below lists these alternatives.

The example respects learnings from **Domain Driven Design (DDD)**, Event Driven Architecture (EDA) and **Microservices (µS)** and should give you a very hands-on access to certain topics.

**Note:** The code was written in order to be explained. Hence I favored simplified code or copy & paste over production-ready code with generic solutions. **Don't consider the coding style best practice! It serves the purpose to have easily explainable code**.

## Architecture and implemenation alternatives

The most fundamental choice is to select the **communication mechanism**:

* **[Apache Kafka](kafka/)** as event bus (could be easily changed to messaging, e.g. RabbitMQ): [](docs/architecture.png)
* **[REST](rest/)** communication between Services
  * This example also shows how to do **stateful resilience patterns** like **stateful retries** leveraging a workflow engine
* **[Zeebe](zeebe/)** broker doing work distribution

Having chosen that your probably can choose the **workflow engine**:

* **Camunda BPM 7**
* **Zeebe** (if you are interessted why Zeebe is listed in the communication mechanism as well as workflow engine please look into the [Zeebe example readme](zeebe/))

and the **programming language**:

* **Java**
* **Go**

## Storyline

Flowing retail simulates a very easy order fulfillment system. The business logic is separated into the following services (shown as [context map](https://www.infoq.com/articles/ddd-contextmapping)):

![Microservices](docs/context-map.png)

Some services are long running in nature, as e.g. the payment service ask customers to update expired credit cards. Hence a workflow engine is used to persist and control these long running interactions.

An important thought is, that this state machine (or workflow engine in this case) is a library used **within** one service. If different services need a workflow engine they potentally run multiple engines. This way it is an autonomous team decision if they want to use some framework and which one:

![Events and Commands](docs/workflow-in-service.png)


## Links and background reading

* Introduction blog post by Bernd Rücker: https://blog.bernd-ruecker.com/flowing-retail-demonstrating-aspects-of-microservices-events-and-their-flow-with-concrete-source-7f3abdd40e53
* InfoQ-Writeup "Events, Flows and Long-Running Services: A Modern Approach to Workflow Automation": https://www.infoq.com/articles/events-workflow-automation
