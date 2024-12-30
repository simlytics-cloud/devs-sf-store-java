# DEVS Streaming Framework Store Java

This library demonstrates the use of the [DEVS Streaming Framework Java](https://github.com/simlytics-cloud/devs-streaming) library for a distributed DEVS simulation.  DEVS Streaming Framework has implemented an interface to the Apache Kafka streaming platform.  The simple single-clerk store implemented here is the same as that described in the [ADEVS Documentation](https://web.ornl.gov/~nutarojj/adevs/docs/manual/node4.html).  The [StoreApp](src/main/java/cloud/simlytics/devssfstore/StoreApp.java) class executes the simulation.  The [CustomerGenerator](src/main/java/cloud/simlytics/devssfstore/CustomerGenerator.java) and the [StoreObserver](src/main/java/cloud/simlytics/devssfstore/StoreObserver.java) run locally in a single DEVS Coupled Model.  It also sets up a local KafkaDevsStreamProxy, the clerkProxy, to receive messages to the clerk and stream them to the "clerk1" Kafka topic.  Similarly, it sets up a KafkaReceiver, the storeCoordinatorReceiver, to receive messages from the clerk via the "storeCoordinator" Kafka topic and pass them along to the storeCoordinator coupled model.

This library has been tested with a remote clerk running in C++ using the ADEVS simulator.  However, the C++ interface to Kafka for the DEVS Streaming Framework has not yet been published, so the default behavior of this library is to run a local clerk to pull Kafka messages from the "clerk1" topic, update the state of the [ClerkModel](src/main/java/cloud/simlytics/devssfstore/ClerkModel.java) and publish output to the storeCoordinator Kafka topic.  This is all done in the setupLocalClerk() method of the StoreApp.  The local clerk requires a KafkaReceiver, the clerk1Receiver, to pull messages from the "clerk1" topic and pass them to the clerk's PDevsSimulator, the clerk1Simulator.  This simulator executes the ClerkModel according to the input messages and routes output to a KafkaDevsStreamProxy, the storeCoordinatorProxy, to be published to the "storeCoordinator" Kafka topic for consumption by the storeCoordinatorReceiver.  For testing models with a remote clerk, disable the local clerk by setting the store-app.run-local proper to false in the [HOCON configuration file](src/main/resources/reference.conf).

## Building

### Prerequisites

- An Apache Kafka cluster.  The [HOCON configuration file](src/main/resources/reference.conf) is already set up for a local version of Kafka using Docker compose.  To use this instance of Kafka, follow the [Install Kafka](kafka-ui/Install Kafka.md) directions.  Otherwise, edit the kafka-cluster and kafka-readall-consumer of the [HOCON configuration file](src/main/resources/reference.conf) to reflect the settings of your Kafka instance.  This has been tested with [Confluent Cloud](https://www.confluent.io/confluent-cloud/).
- Java (Developed and tested with Java 17)
- Apache Maven (Developed and tested with Maven 3.9.1)
- Requires internet connection to pull Maven dependencies.
- Install [DEVS Streaming Framework Java](https://github.com/simlytics-cloud/devs-streaming) into your local Maven repository.

### Steps
- From the top-level directory, type `mvn package`.  The library will build and test.  Note that the DeleteAndCreateTopicsTest is disabled because it requires a Kafka connection.  You can enable to test the ability to create and delete topics in your Kafka cluster.

## Running

- Run using the mvn exec:java command:

`mvn exec:java -Dexec.mainClass="cloud.simlytics.devssfstore.StoreApp"`