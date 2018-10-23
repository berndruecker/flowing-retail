# Flowing Retail / REST / Go and Zeebe


This sample (micro-)service can retrieve payments and therefor needs to be called via REST. It requires an upstream REST service to charge credit cards.

![REST callstack](../docs/resilience-patterns/situation.png)

This simple call-chain is used to showcase stateful retry.

# How-to run

First you have to startup the stripe fake server, as this handles the credit card payments.

```
go run ../stripe-fake/main.go 
```

Now you can run the payment service itself, but you have to choose the version you like, e.g. V5

```
go run v5/main.go
```

Now this version of the payment service are available:

* http://localhost:8100/payment

You now can issue a PUT with an empty body:

```
curl -X PUT \
 -d '{"customerId": "42", "creditCardNo": "you-whish-9999", "amount": "100"}' \
 -i http://localhost:8100/payment
```