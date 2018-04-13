# .NET version of flowing-retail/rest

This example demonstrates stateful resilience patterns in a REST environment. A payment (micro-)service can retrieve payments if called via REST. It requires an upstream REST service to charge credit cards.

![REST callstack](../../../docs/resilience-patterns/docs/situation.png)

This simple call-chain is great to demonstrate various important resilience patterns.

**See [introduction](../../README.md) for the storyline / patterns behind**

# Concrete technologies/frameworks in this folder:

* .NET Core 2.0
* C#
* Polly 5.8.x
* Camunda 7.x

# How-to run

This example uses this [Camunda Client Sample for C#](https://github.com/berndruecker/camunda-dot-net-showcase/tree/master/CamundaClient) to avoid low level [Camunda REST API](https://docs.camunda.org/manual/latest/reference/rest/) handling. 

The first step is to run Camunda itself. You have two options:

* Donwload, unzip and run, see: https://camunda.com/download/
* Docker: 
```
docker run -d -p 8080:8080 camunda/camunda-bpm-platform:latest
```
See also [Use Camunda without touching Java and get an easy-to-use REST-based orchestration and workflow engine](https://blog.bernd-ruecker.com/use-camunda-without-touching-java-and-get-an-easy-to-use-rest-based-orchestration-and-workflow-7bdf25ac198e) for more details on this.


In order to run the flowing-retail/rest example:

* Create a new solution in Visual Studio
* Add the [Camunda Client Sample for C#](https://github.com/berndruecker/camunda-dot-net-showcase/tree/master/CamundaClient)
* Add the [FlowingRetailPayment](FlowingRetailPayment.csproj) project (this folder)
* Fix the dependency of [FlowingRetailPayment](FlowingRetailPayment.csproj) to [Camunda Client Sample for C#](https://github.com/berndruecker/camunda-dot-net-showcase/tree/master/CamundaClient)
* Add the [StripeFake](../stripe-fake/StripeFake.csproj) project 
* [Configure your solution for *multiple start projects*](https://msdn.microsoft.com/en-us/library/ms165413.aspx)
* Make sure you run the projects via the .NET CLI, see [launchSettings.json commandName usage](https://stackoverflow.com/questions/44645775/launchsettings-json-commandname-usage)

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

For Camunda there is an enterprise edition available with [additional features in Cockpit](https://camunda.com/products/cockpit/#/features) (the monitoring tool). It is quite handy to use this when playing around with the example. You can easily switch to use enterprise edition:

* Download and get a trial license if you don't have a license yet: https://camunda.com/download/enterprise/

Note that you do not need the enterprise edition to run the examples, the community edition will also do fine. But because of less features you do not see historical workflow instances - and that means you do not see that much in Camunda Cockpit if everything runs smoothly.
