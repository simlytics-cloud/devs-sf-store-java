package cloud.simlytics.devssfstore;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import com.fasterxml.jackson.databind.ObjectMapper;
import devs.PDevsCoordinator;
import devs.PDevsCouplings;
import devs.PDevsSimulator;
import devs.RootCoordinator;
import devs.msg.DevsMessage;
import devs.msg.InitSim;
import devs.msg.time.DoubleSimTime;
import devs.utils.DevsObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StoreSimulationTest {

  static final ActorTestKit testKit = ActorTestKit.create();
  static ObjectMapper objectMapper = DevsObjectMapper.buildObjectMapper();;

  @AfterAll
  public static void cleanup() {
    testKit.shutdownTestKit();
  }

  private final TreeMap<Double, List<Customer>> customerSchedule = new TreeMap<>();

  StoreSimulationTest() {
    customerSchedule.put(1.0, Collections.singletonList(
        Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build()));
    customerSchedule.put(2.0, Collections.singletonList(
        Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build()));
  }

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
        "storeCoordinator", "root", modelSimulators, storeCouplings),
        "storeCoordinator");

    ActorRef<DevsMessage> rootCoordinator = testKit.spawn(Behaviors.setup(context ->
        new RootCoordinator<>(context, DoubleSimTime.builder().t(8.0).build(), storeCoordinator)));
    rootCoordinator.tell(InitSim.builder().time(DoubleSimTime.builder().t(0.0).build()).build());

    Thread.sleep(2000L);
  }

}
