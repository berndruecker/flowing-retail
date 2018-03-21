# Flowing Retail / Zeebe

This folder contains services that leverage the horizontally scalable workflow engine Zeebe for work distribution and as means of communication.

One important aspect on this design is, that Zeebe is handled as central middleware and messaging systems like Apache Kafka or RabbitMQ are not used. This might feel quite unusal for you, but we do know of quite some projects going into this direction to save some pains with messaging systems.

![Alternatives](../docs/zeebe-broker-alternatives.png)

Note that the workflow model **(1)** keeps to be owned by the Order Service - and is defined there, even if it gets deployed onto the Broker for execution.

Upsides:

* Less code involved
* No need to operate an own messaging system or event business
* Operations tooling from the workflow engine can be directly used to operate the tool with a lot of context information

Downsides:

* Dependency to Zeebe **(2)** in a lot of components (the places where you had a Kafka dependency before)

Other differences
* Data mapping might be in the workflow model instead of the WorkItemHandler

So both are valid choices:

* Local broker within service (left hand side of the picture above)
* Broker used for work distribution (right hand side of the picture above)
