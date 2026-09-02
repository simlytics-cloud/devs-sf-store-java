# DEVS Streaming Framework Store Java

This library demonstrates the use of the [DEVS Streaming Framework Java](https://github.com/simlytics-cloud/devs-streaming) library for a distributed DEVS simulation.  DEVS Streaming Framework has implemented an interface to the Apache Kafka streaming platform.  The simple single-clerk store implemented here is the same as that described in the [ADEVS Documentation](https://web.ornl.gov/~nutarojj/adevs/docs/manual/node4.html).  The [StoreApp](src/main/java/cloud/simlytics/devssfstore/StoreApp.java) class executes the simulation.  The [CustomerGenerator](src/main/java/cloud/simlytics/devssfstore/CustomerGenerator.java) and the [StoreObserver](src/main/java/cloud/simlytics/devssfstore/StoreObserver.java) run locally in a single DEVS Coupled Model.  It also sets up a local KafkaDevsStreamProxy, the clerkProxy, to receive messages to the clerk and stream them to the configured Kafka topic.  Similarly, it sets up a KafkaReceiver, the storeCoordinatorReceiver, to receive messages from the clerk via the Kafka topic and pass them along to the storeCoordinator coupled model.

The default behavior of this library is to run a local clerk to pull Kafka messages from the topic, update the state of the [ClerkModel](src/main/java/cloud/simlytics/devssfstore/ClerkModel.java) and publish output to the same topic.  This is all done in the setupLocalClerk() method of the StoreApp.  The local clerk requires a KafkaReceiver, the clerk1Receiver, to pull messages from the and pass them to the clerk's PDevsSimulator, the clerk1Simulator.  This simulator executes the ClerkModel according to the input messages and routes output to a KafkaDevsStreamProxy, the storeCoordinatorProxy, to be published to Kafka for consumption by the storeCoordinatorReceiver.  For testing models with a remote clerk, disable the local clerk by setting the store-app.run-local proper to false in the [HOCON configuration file](src/main/resources/reference.conf).  A C++ version of the local clerk is available in the [DEVS Streaming Framework C++](https://github.com/simlytics-cloud/devs-sf-cpp) project.  Follow the instructions there to build the project.

## Building

### Prerequisites

- An Apache Kafka cluster.  The [HOCON configuration file](src/main/resources/reference.conf) is already set up for a local version of Kafka using Docker compose.  To use this instance of Kafka, follow the [Install Kafka](kafka-ui/Install Kafka.md) directions.  Otherwise, edit the kafka-cluster and kafka-readall-consumer of the [HOCON configuration file](src/main/resources/reference.conf) to reflect the settings of your Kafka instance.  The expected topic is set by the "store-app.simulationId" configuration as "BusyMartSimulation".  You may change that topic as needed.  It is also important that the "runId" property matches the remote broker if you are using one, because each run create a unique consumer group tagged to that runId.  See the [devs-sf-cpp quickstart](https://github.com/simlytics-cloud/devs-sf-cpp/blob/main/quickstart.md) for more information about Kafka configuraion.  You may create the topic manually in your broker, or use the [DeleteCreateTopicsTest](./src/test/java/cloud/simlytics/devssfstore/DeleteCreateTopicsTest.java) to do it.  This has also been tested with [Confluent Cloud](https://www.confluent.io/confluent-cloud/).
- Java (Developed and tested with Java 21)
- Apache Maven (Developed and tested with Maven 3.9.1)
- Requires internet connection to pull Maven dependencies.
- Install [DEVS Streaming Framework Java](https://github.com/simlytics-cloud/devs-streaming) into your local Maven repository.

### Steps
- From the top-level directory, type `mvn package`.  The library will build and test.  Note that the DeleteAndCreateTopicsTest is disabled because it requires a Kafka connection.  You can enable to test the ability to create and delete topics in your Kafka cluster.

## Running

First, start an Apache Kafka broker.  You can use your own or see these [instructions](./kafka-ui/Install%20Kafka.md) for running locally via Docker.

This application is currently set up to run a local clerk for the store coordinator to interface with via Kafka.  However, as noted above, you may also run a C++ clerk via the [DEVS Streaming Framework C++](https://github.com/simlytics-cloud/devs-sf-cpp) project.  Set the "store-app.run-local" configuration property to false.  Ensure the remote clerk is configured for the same broker, Kafka topic, and runId as this library.  Then run the remote clerk via the kafka_clerk_simulator executable.  

Run this project using the mvn exec:java command:

`mvn exec:java -Dexec.mainClass="cloud.simlytics.devssfstore.StoreApp"`

```
The expected output will be:
Customer leaving at 2.0 after a wait of 1.0
Customer leaving at 6.0 after a wait of 4.0
Customer leaving at 10.0 after a wait of 4.0
Customer leaving at 12.0 after a wait of 2.0
Customer leaving at 22.0 after a wait of 10.0
Customer leaving at 42.0 after a wait of 20.0
Customer leaving at 44.0 after a wait of 2.0
Customer leaving at 45.0 after a wait of 1.0
```
You can view the configured Kafka topic to see the DEVS messages sent to and from the remote clerk.