/*
 * DEVS Streaming Framework Store Java Copyright (C) 2024 simlytics.cloud LLC and
 * DEVS Streaming Framework Store Java contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloud.simlytics.devssfstore;

import cloud.simlytics.devssfstore.StoreApp.StoreAppMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.PDevsSimulator;
import devs.RootCoordinator;
import devs.msg.DevsMessage;
import devs.msg.InitSim;
import devs.msg.time.DoubleSimTime;
import devs.proxy.KafkaDevsStreamProxy;
import devs.proxy.KafkaReceiver;
import devs.utils.ConfigUtils;
import devs.utils.KafkaUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.Terminated;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.actor.typed.javadsl.ReceiveBuilder;


/**
 * The StoreApp class represents a simulation application for a store environment. It handles the
 * initialization and coordination of various simulation components, such as customer generators,
 * clerks, and store observers, using Pekko's actor system. This simulation follows the
 * <a href="https://web.ornl.gov/~nutarojj/adevs/docs/manual/node4.html">
 * example in the Adevs documentation.</a>
 * <p>
 * StoreApp extends AbstractBehavior and defines behaviors to handle specific simulation messages
 * and termination signals.
 * <p>
 * Key responsibilities of this class include:
 * <p>
 * - Managing Kafka topics for communication between components. - Setting up simulation components
 * such as customer generators, clerks, and observers. - Coordinating the interactions between these
 * components to simulate the store operations. - Handling life-cycle events such as application
 * start and termination.
 * <p>
 * It uses Pekko typed actors to facilitate message-driven communication and defines the
 * simulation's main entry point in the `main` method.
 */
public class StoreApp extends AbstractBehavior<StoreAppMessage> {

  private static final String clerkInputTopic = "clerk1";
  private static final String storeCoordinatorInputTopic = "storeCoordinator";

  /**
   * Configuration for connecting to a Kafka cluster. This static private field holds the necessary
   * configuration properties required for establishing and managing communication with a Kafka
   * cluster instance.
   * <p>
   * This configuration is utilized to define Kafka cluster-specific settings, such as bootstrap
   * servers, serializers, and other producer/consumer settings, enabling the application to
   * interact seamlessly with Kafka topics for message-driven operations.
   * <p>
   * This variable is an integral part of the application's messaging and data processing setup,
   * facilitating the coordination of inputs and outputs across various Kafka topics.
   */
  private static Config kafkaClusterConfig;
  /**
   * Configuration for a Kafka consumer used within the application.
   * <p>
   * This static field stores settings required to configure the Kafka consumer, such as brokers,
   * group ID, key-value deserialization, and other properties needed for managing communication
   * with a Kafka cluster. It enables the application to consume messages from Kafka topics
   * according to defined parameters.
   * <p>
   * The configuration is expected to align with the Kafka client's consumer API, ensuring proper
   * connection and message processing.
   * <p>
   * Usage of this field may entail initializing the consumer, subscribing to relevant topics, and
   * specifying runtime behavior for message polling and handling.
   */
  private static Config kafkaConsumerConfig;

  /**
   * This flag indicates whether the StoreApp should create a local ClerkModel to consume customers
   * from the clerkInputTopic.  If it is set to false, a remote DEVS Streaming Framework model will
   * handle the processing of customers.  It can be modified by changing the store-app.run-local <a
   * href="https://github.com/lightbend/config"> HOCON configuration property.</a>
   */
  private static boolean runLocal = false;

  /**
   * Marker interface representing messages used within the actor system of the StoreApp.
   * <p>
   * The `StoreAppMessage` interface provides a common type for defining all messages that are
   * utilized in communication between actors in the StoreApp system. By implementing this
   * interface, application-specific message types can ensure consistency and type-safety across the
   * actor-based workflow.
   * <p>
   * This interface serves as a foundation for the actor communication model in the StoreApp and
   * facilitates the definition of behaviors and message handlers for various components within the
   * actor system.
   */
  public interface StoreAppMessage {

  }


  /**
   * A message class used to signal the initiation of the StoreApp actor system.
   * <p>
   * StoreStart is a marker message that triggers the startup sequence or initialization processes
   * within the StoreApp. It is primarily used in conjunction with the message handling capabilities
   * of the actor framework leveraged by StoreApp.
   * <p>
   * This class implements the {@code StoreApp.StoreAppMessage} interface, which acts as a common
   * type for all messages exchanged within the StoreApp actor system. By adhering to this
   * interface, StoreStart ensures consistency and type safety in the communication structure
   * employed by the application.
   * <p>
   * Typical usage involves sending an instance of StoreStart to the root or coordinator actor of
   * the system, initiating the defined behaviors needed to activate various components within the
   * StoreApp.
   */
  public static class StoreStart implements StoreApp.StoreAppMessage {

  }

  /**
   * The entry point for the StoreApp application. This method initializes and configures the
   * necessary components for the application to run, including Kafka topics, Kafka clients, and the
   * actor system. It also starts the StoreApp actor system and sends the initial {@link StoreStart}
   * message.
   *
   * @param args command-line arguments passed to the application, typically not used in this
   *             context.
   * @throws ExecutionException   if an error occurs during the asynchronous execution of Kafka
   *                              operations.
   * @throws InterruptedException if the thread is interrupted while executing Kafka operations or
   *                              initializing the application.
   */
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    Config config = ConfigFactory.load();
    runLocal = config.getBoolean("store-app.run-local");
    kafkaClusterConfig = config.getConfig("kafka-cluster");
    kafkaConsumerConfig = config.getConfig("kafka-readall-consumer");
    Properties kafkaClusterProperties = ConfigUtils.toProperties(kafkaClusterConfig);
    AdminClient adminClient = KafkaUtils.createAdminClient(kafkaClusterProperties);
    KafkaUtils.deleteTopics(
        Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic), adminClient);
    Thread.sleep(5000);

    // Note that the Kafka Topics created must have only 1 partition in order to guaranty messages
    //   are consumed in the same order they are published.
    KafkaUtils.createTopics(Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic),
        adminClient, Optional.of(1), Optional.empty());

    org.apache.pekko.actor.typed.ActorSystem<StoreAppMessage> system =
        org.apache.pekko.actor.typed.ActorSystem.create(StoreApp.create(), "StoreApp");
    system.tell(new StoreStart());
  }

  /**
   * Constructs an instance of StoreApp, extending AbstractBehavior, and initializing it with the
   * given ActorContext. This constructor sets up the base behavior for handling store-specific
   * messages in an actor-based system.
   *
   * @param context the ActorContext for setting up the StoreApp behavior, which serves as the
   *                interaction context for messages and signals in the actor system.
   */
  public StoreApp(ActorContext<StoreAppMessage> context) {
    super(context);
  }

  /**
   * Factory method to create a new instance of {@link Behavior} for the {@link StoreApp} actor
   * system. This method sets up the behavior for handling messages specific to the StoreApp.
   *
   * @return A {@link Behavior} instance configured to manage {@link StoreApp.StoreAppMessage}
   * interactions within the actor system.
   */
  public static Behavior<StoreApp.StoreAppMessage> create() {
    return Behaviors.setup(StoreApp::new);
  }

  /**
   * Creates and defines the message handling behavior for the StoreApp actor system.
   * <p>
   * This method uses a `ReceiveBuilder` to specify the reactions to particular message types or
   * signals. It initializes the StoreApp actor's behavior to handle `StoreStart` messages and also
   * processes the termination signal (`Terminated`). Each specific behavior is delegated to the
   * corresponding handler method.
   *
   * @return A `Receive` object that defines the message handling behavior for StoreApp.
   */
  @Override
  public Receive<StoreAppMessage> createReceive() {
    ReceiveBuilder<StoreAppMessage> storeAppReceiveBuilder = newReceiveBuilder();
    storeAppReceiveBuilder.onMessage(StoreStart.class, this::onStart);
    storeAppReceiveBuilder.onSignal(Terminated.class, this::onTerminated);
    return storeAppReceiveBuilder.build();
  }

  /**
   * Sets up a local clerk simulator and its corresponding components for the store actor system.
   * <p>
   * This method initializes the following components: - A ClerkModel instance representing the
   * behavior of the clerk. - A PDevsSimulator actor linked to the ClerkModel to manage its
   * simulation lifecycle. - A KafkaDevsStreamProxy actor used to interact with the store
   * coordinator. - A KafkaReceiver actor that connects the clerk simulator to the messaging system
   * for receiving inputs.
   * <p>
   * The method establishes the communication links between these components to enable the
   * simulation of clerk-related activities within the store. It uses Kafka topics and
   * configurations provided in the surrounding context or actor system.
   * <p>
   * Internally, the following steps are performed: 1. A `ClerkModel` is instantiated to represent
   * the clerk's behavior. 2. A PDEVS simulator is created to manage the simulation of the
   * `ClerkModel`. 3. A coordinator proxy actor is spawned using a KafkaDevsStreamProxy to bridge
   * interactions between the local clerk and a store-level coordinator. 4. A KafkaReceiver actor is
   * instantiated to receive input messages for the clerk simulator from Kafka, connecting the
   * clerk's processing logic to the external messaging system.
   * <p>
   * This method is invoked as part of the initialization of the {@code StoreApp} actor class,
   * preparing the local clerk simulation for participation in the wider actor-based system.
   */
  protected void setUpLocalClerk() {
    ClerkModel clerkModel = new ClerkModel("clerk1");

    ActorRef<DevsMessage> clerk1Simulator = getContext().spawn(
        PDevsSimulator.create(clerkModel, DoubleSimTime.create(0.0)), "clerk1Simulator");

    ActorRef<DevsMessage> coordinatorProxy =
        getContext().spawn(
            KafkaDevsStreamProxy.create("storeCoordinator", storeCoordinatorInputTopic,
                kafkaClusterConfig), "storeCoordinatorProxy");

    ActorRef<DevsMessage> clerk1Receiver = getContext().spawn(
        KafkaReceiver.create(clerk1Simulator, coordinatorProxy, kafkaConsumerConfig,
            clerkInputTopic), "clerk1Receiver");
  }

  /**
   * Handles the startup process for the StoreApp actor system by initializing and setting up
   * various components, simulators, and coordinators required for the store simulation.
   * <p>
   * The method configures the following: - Customer schedule and generators. - Kafka-based
   * interaction layers (clerk and store coordinator). - Observers and simulators for the store
   * system. - Root coordinator and couplings for managing the simulation's hierarchical structure.
   * <p>
   * If the system is configured to run locally, additional setup is performed for the local clerk
   * simulator.
   *
   * @param start A {@link StoreStart} message that signals the initiation of the StoreApp actor
   *              system.
   * @return A {@link Behavior} of type {@link StoreAppMessage} indicating the actor's next behavior
   * after processing the startup message.
   */
  protected Behavior<StoreAppMessage> onStart(StoreStart start) {
    TreeMap<Double, List<Customer>> customerSchedule = new TreeMap<>();
    customerSchedule.put(1.0, Collections.singletonList(
        Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build()));
    customerSchedule.put(2.0, Collections.singletonList(
        Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build()));
    customerSchedule.put(3.0, Collections.singletonList(
        Customer.builder().twait(4.0).tenter(3.0).tleave(0.0).build()));
    customerSchedule.put(5.0, Collections.singletonList(
        Customer.builder().twait(2.0).tenter(5.0).tleave(0.0).build()));
    customerSchedule.put(7.0, Collections.singletonList(
        Customer.builder().twait(10.0).tenter(7.0).tleave(0.0).build()));
    customerSchedule.put(8.0, Collections.singletonList(
        Customer.builder().twait(20.0).tenter(8.0).tleave(0.0).build()));
    customerSchedule.put(10.0, Collections.singletonList(
        Customer.builder().twait(2.0).tenter(10.0).tleave(0.0).build()));
    customerSchedule.put(11.0, Collections.singletonList(
        Customer.builder().twait(1.0).tenter(11.0).tleave(0.0).build()));
    DoubleSimTime t0 = DoubleSimTime.builder().t(0.0).build();
    CustomerGenerator customerGenerator = new CustomerGenerator(customerSchedule);
    ActorRef<DevsMessage> customerSimulator =
        getContext().spawn(PDevsSimulator.create(customerGenerator, t0), "customerGenerator");

    ActorRef<DevsMessage> clerkProxy = getContext().spawn(
        KafkaDevsStreamProxy.create("clerk1", clerkInputTopic,
            kafkaClusterConfig), "clerkProxy");

    StoreObserver storeObserver = new StoreObserver(null);
    ActorRef<DevsMessage> storeObserverSimulator =
        getContext().spawn(PDevsSimulator.create(storeObserver, t0), "storeObserver");

    PDevsCouplings storeCouplings = new PDevsCouplings(Collections.emptyList(),
        Collections.singletonList(new StoreCouplingHandler()));

    Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
    modelSimulators.put(customerGenerator.getModelIdentifier(), customerSimulator);
    modelSimulators.put("clerk1", clerkProxy);
    modelSimulators.put(storeObserver.getModelIdentifier(), storeObserverSimulator);

    ActorRef<DevsMessage> storeCoordinator = getContext().spawn(PDevsCoordinator.create(
            "storeCoordinator", "root", modelSimulators, storeCouplings),
        "storeCoordinator");

    if (runLocal) {
      setUpLocalClerk();
    }

    ActorRef<DevsMessage> rootCoordinator = getContext().spawn(RootCoordinator.create(
        DoubleSimTime.builder().t(50.0).build(), storeCoordinator), "rootCoordinator");

    ActorRef<DevsMessage> storeCoordinatorReceiver = getContext().spawn(
        KafkaReceiver.create(storeCoordinator, rootCoordinator, kafkaConsumerConfig,
            storeCoordinatorInputTopic), "storeCoordinatorReceiver");

    getContext().watch(rootCoordinator);
    rootCoordinator.tell(InitSim.builder().time(DoubleSimTime.builder().t(0.0).build()).build());
    return Behaviors.same();
  }

  /**
   * Handles the termination signal of an actor within the StoreApp actor system.
   * <p>
   * This method is invoked when the RootCoordinator actor is terminated. It stops the StoreApp
   * actor, which in turn causes the ActorSystem to shut down.
   *
   * @param signal the {@link Terminated} signal representing the termination of a monitored actor.
   * @return A {@link Behavior} of type {@link StoreAppMessage} that transitions the actor to a
   * stopped state.
   */
  protected Behavior<StoreAppMessage> onTerminated(Terminated signal) throws InterruptedException {
    return Behaviors.stopped();
  }
}
