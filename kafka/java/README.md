# Flowing Retail / Apache Kafka / Java

This folder contains services written in Java that connect to Apache Kafka as means of communication between the services.

Tech stack:

* Java 8
* Spring Boot 2.6.x
* Apache Kafka (and Spring Kafka)
* Camunda Zeebe 8.x (and Spring Zeebe)

![Microservices](../../docs/kafka-services.png)

# Run the application

You can either

* Docker Compose with pre-built images from Docker Hub (simplest)
* Build (Maven) and start manually (including Zookeeper, Kafka)


## Hint on using Camunda License

The core components of Camunda are source available and free to use, but the operations tool Camunda Operate is only free for non-production use.


## Docker Compose with pre-build Docker images

* Download [docker-compose-kafka-java-order-camunda.yml](../../runner/docker-compose/docker-compose-kafka-java-orchestrated.yml) or clone this repo and goto [docker-compose/](../../runner/docker-compose/)
* Goto directory where you downloaded this file

```
cd docker-compose
```

* Start using docker compose:

```
docker-compose -f docker-compose-kafka-java-orchestrated.yml up
```

* After everything has started up you are ready to visit the overview page [http://localhost:8099](http://localhost:8089)
* You can place an order via [http://localhost:8091](http://localhost:8091)
* You can inspect processes via Camunda Operate on [http://localhost:8081](http://localhost:8081)
* You can inspect all events going on via [http://localhost:8095](http://localhost:8095)

If you like you can connect to Kafka from your local Docker host machine too. 

Note that there are a couple of other docker-compose files available too, e.g. to play around with the choreography.



## Manual start (Kafka, mvn exec:java)

* Download or clone the source code
* Run a full maven build

```
cd kafka/java
mvn install
```

* Install and start Kafka on the standard port
* Create topic *"flowing-retail"*

```
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic flowing-retail
```

* You can check & query all topics by:

```
kafka-topics.sh --list --zookeeper localhost:2181
```

* Startup your Zeebe broker as described in [Install Zeebe](https://docs.camunda.io/docs/self-managed/platform-deployment/). As an alternative you could also use [Camunda Platform 8 SaaS](https://camunda.com/get-started/)

* Start the different microservices components by Spring Boot one by one, e.g.

```
mvn -f checkout exec:java
mvn -f order-zeebe exec:java
...
```

Here you could easily switch to use order-zeebe instead.

You can also import the projects into your favorite IDE and start the following class yourself:

```
checkout/io.flowing.retail.java.CheckoutApplication
...
```

* Now you can place an order via [http://localhost:8091](http://localhost:8091)
* You can inspect processes via Camunda Operate on [http://localhost:8081](http://localhost:8081)
* You can inspect all events going on via [http://localhost:8095](http://localhost:8095)
