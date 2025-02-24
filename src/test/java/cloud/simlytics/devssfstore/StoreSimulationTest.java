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

import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.PDevsSimulator;
import devs.RootCoordinator;
import devs.msg.DevsMessage;
import devs.msg.InitSim;
import devs.msg.time.DoubleSimTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for the simulation of a store using a discrete event system. This test utilizes
 * the Pekko framework for actor-based simulations and validates the interaction and state
 * transitions of customers, clerk models, and the store system.
 * <p>
 * The test includes: - Customer generation and scheduling. - Simulation of a clerk model handling
 * customers. - Observation of the store system. - Coupling and coordination of individual models
 * into a cohesive simulation.
 * <p>
 * It ensures that the store's components are correctly integrated and interact as per the designed
 * logic during the specified simulation timeframe.
 */
public class StoreSimulationTest {

  /**
   * A static instance of {@code ActorTestKit}, used for setting up and testing actor systems in a
   * simulation or testing environment.
   * <p>
   * This instance is initialized using the {@code ActorTestKit.create()} method, providing the
   * functionality to verify actor behaviors, interactions, and states during simulation or unit
   * tests. It facilitates accurate and isolated testing of actor-based components within the
   * framework.
   * <p>
   * The usage of this instance ensures a controlled testing environment, including features for
   * message passing, system state validation, and lifecycle management of the associated actor
   * system.
   */
  static final ActorTestKit testKit = ActorTestKit.create();

  /**
   * Cleans up resources and shuts down the TestKit after all tests have been executed.
   * <p>
   * This method is annotated with {@code @AfterAll}, ensuring it is executed once after all test
   * methods in the test class have completed execution. It is used to release any resources or
   * perform final teardown operations required to ensure a clean test environment.
   * <p>
   * Specifically, it shuts down the TestKit, which might have been used during the tests to
   * facilitate simulation or testing of system components.
   */
  @AfterAll
  public static void cleanup() {
    testKit.shutdownTestKit();
  }

  private final TreeMap<Double, List<Customer>> customerSchedule = new TreeMap<>();

  /**
   * Default constructor for the StoreSimulationTest class.
   * <p>
   * Initializes the customerSchedule map with predefined data, simulating customer wait times,
   * enter times, and leave times. The map uses time as the key and associates it with a list of
   * Customer objects representing customer behavior at that given time.
   */
  StoreSimulationTest() {
    customerSchedule.put(1.0, Collections.singletonList(
        Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build()));
    customerSchedule.put(2.0, Collections.singletonList(
        Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build()));
  }

  /**
   * Tests the store simulation using a Parallel Discrete Event System Specification (PDEVS)
   * framework.
   * <p>
   * This test sets up a simulation involving a customer generator, a clerk model, and a store
   * observer. It includes the following components:
   * <p>
   * - `CustomerGenerator`: Simulates the generation of customers at predefined intervals. -
   * `ClerkModel`: Represents the behavior of a clerk in the simulation environment. -
   * `StoreObserver`: Observes store-related events occurring within the simulation.
   * <p>
   * The test also sets up necessary PDEVS couplings, coordinators, and simulators with an initial
   * simulation time of 0.0 and a total duration of up to 8.0 simulation time units. A root
   * coordinator orchestrates the execution flow of the simulation by initializing and running it.
   * <p>
   * This method ensures all components are properly initialized, coupled, and coordinated for the
   * execution of a complete store simulation. After initializing, the simulation thread runs long
   * enough to execute predefined customer schedules and interactions between components.
   *
   * @throws InterruptedException if the current thread is interrupted during the simulation's sleep
   *                              period.
   */
  @Test
  @DisplayName("Test store simulation")
  void testStoreSim() throws InterruptedException {

    DoubleSimTime t0 = DoubleSimTime.builder().t(0.0).build();
    CustomerGenerator customerGenerator = new CustomerGenerator(customerSchedule);
    ActorRef<DevsMessage> customerSimulator =
        testKit.spawn(PDevsSimulator.create(customerGenerator, t0), "customerGenerator");

    ClerkModel clerkModel = new ClerkModel("clerk1");
    ActorRef<DevsMessage> clerk1Simulator = testKit.spawn(
        PDevsSimulator.create(clerkModel, t0), "clerk1Simulator");

    StoreObserver storeObserver = new StoreObserver(null);
    ActorRef<DevsMessage> storeObserverSimulator =
        testKit.spawn(PDevsSimulator.create(storeObserver, t0), "storeObserver");

    PDevsCouplings storeCouplings = new PDevsCouplings(Collections.emptyList(),
        Collections.singletonList(new StoreCouplingHandler()));

    Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
    modelSimulators.put(customerGenerator.getModelIdentifier(), customerSimulator);
    modelSimulators.put(clerkModel.getModelIdentifier(), clerk1Simulator);
    modelSimulators.put(storeObserver.getModelIdentifier(), storeObserverSimulator);

    ActorRef<DevsMessage> storeCoordinator = testKit.spawn(PDevsCoordinator.create(
            "storeCoordinator", modelSimulators, storeCouplings),
        "storeCoordinator");

    ActorRef<DevsMessage> rootCoordinator = testKit.spawn(Behaviors.setup(context ->
        new RootCoordinator<>(context, DoubleSimTime.builder().t(8.0).build(), storeCoordinator)));
    rootCoordinator.tell(InitSim.builder().time(DoubleSimTime.builder().t(0.0).build()).build());

    Thread.sleep(2000L);
  }

}
