# Flowing Retail Kubernetes Helm Charts

# UNDER DEVELOPMENT - DON#T USE THIS UNTIL YOU KNOW WHAT YOU ARE DOING

* Install kubectl

* On GKE create a cluster - Kafka runs 3 nodes and we need some microservices. Memory is more important as CPU for this example :

```
gcloud container clusters create fr-demo --num-nodes 4 --machine-type=n1-standard-2


n1-highmem-2
```

This makes it easy to tear it down later on
```
gcloud container clusters delete fr-demo
```

```
helm init
```

* Make sure RBAC is working, see https://github.com/helm/helm/issues/3055:

```
kubectl create serviceaccount --namespace kube-system tiller
kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
kubectl patch deploy --namespace kube-system tiller-deploy -p "{\"spec\":{\"template\":{\"spec\":{\"serviceAccount\":\"tiller\"}}}}"
```

Run Kafka, see https://docs.confluent.io/current/installation/installing_cp/cp-helm-charts/docs/index.html

Prepare
```
helm repo add confluentinc https://raw.githubusercontent.com/confluentinc/cp-helm-charts/master
helm repo update
```

```
helm install --set cp-schema-registry.enabled=false,cp-kafka-rest.enabled=false,cp-kafka-connect.enabled=false,cp-ksql-server.enabled=false confluentinc/cp-helm-charts --name confluent
```
// ,cp-zookeeper.servers=1,cp-kafka.brokers=1
"[2018-10-02 14:47:58,870] ERROR [KafkaApi-0] Number of alive brokers '1' does not meet the required replication factor '3' for the offsets topic (configured via 'offsets.topic.replication.factor'). This error can be ignored if the cluster is starting up and not all brokers are up yet. (kafka.server.KafkaApis)

```
kafka-java-base> helm install . --name fr-base
kafka-java-choreography>helm install . --name fr-choreography
```

```
zeebe> helm install . --name zeebe
```
kafka-java-choreography-track-zeebe>helm install . --name fr-choreography-track



NOTES:
## ------------------------------------------------------
## Zookeeper
## ------------------------------------------------------
Connection string for Confluent Kafka:
  dunking-jellyfish-cp-zookeeper-0.dunking-jellyfish-cp-zookeeper-headless:2181,dunking-jellyfish-cp-zookeeper-1.dunking-jellyfish-cp-zookeeper-headless:2181,...

To connect from a client pod:

1. Deploy a zookeeper client pod with configuration:

    apiVersion: v1
    kind: Pod
    metadata:
      name: zookeeper-client
      namespace: default
    spec:
      containers:
      - name: zookeeper-client
        image: confluentinc/cp-zookeeper:5.0.0
        command:
          - sh
          - -c
          - "exec tail -f /dev/null"

2. Log into the Pod

  kubectl exec -it zookeeper-client -- /bin/bash

3. Use zookeeper-shell to connect in the zookeeper-client Pod:

  zookeeper-shell dunking-jellyfish-cp-zookeeper:2181

4. Explore with zookeeper commands, for example:

  # Gives the list of active brokers
  ls /brokers/ids

  # Gives the list of topics
  ls /brokers/topics

  # Gives more detailed information of the broker id '0'
  get /brokers/ids/0## ------------------------------------------------------
## Kafka
## ------------------------------------------------------
To connect from a client pod:

1. Deploy a kafka client pod with configuration:

    apiVersion: v1
    kind: Pod
    metadata:
      name: kafka-client
      namespace: default
    spec:
      containers:
      - name: kafka-client
        image: confluentinc/cp-kafka:5.0.0
        command:
          - sh
          - -c
          - "exec tail -f /dev/null"

2. Log into the Pod

  kubectl exec -it kafka-client -- /bin/bash

3. Explore with kafka commands:

  # Create the topic
  kafka-topics --zookeeper dunking-jellyfish-cp-zookeeper-headless:2181 --topic dunking-jellyfish-topic --create --partitions 1 --replication-factor 1 --if-not-exists

  # Create a message
  MESSAGE="`date -u`"

  # Produce a test message to the topic
  echo "$MESSAGE" | kafka-console-producer --broker-list dunking-jellyfish-cp-kafka-headless:9092 --topic dunking-jellyfish-topic

  # Consume a test message from the topic
  kafka-console-consumer --bootstrap-server dunking-jellyfish-cp-kafka-headless:9092 --topic dunking-jellyfish-topic --from-beginning --timeout-ms 2000 --max-messages 1 | grep "$MESSAGE"






















