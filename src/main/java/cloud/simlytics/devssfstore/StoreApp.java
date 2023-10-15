package cloud.simlytics.devssfstore;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
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


public class StoreApp extends AbstractBehavior<StoreAppMessage>
{

    static private final String clerkInputTopic = "clerk1";
    static private final String storeCoordinatorInputTopic = "storeCoordinator";

    static private Config kafkaClusterConfig;
    static private Config kafkaConsumerConfig;

    static private boolean runLocal = false;

    public interface StoreAppMessage{}
    public static class StoreStart implements StoreApp.StoreAppMessage {}
    public static void main( String[] args ) throws ExecutionException, InterruptedException {
        Config config = ConfigFactory.load();
        kafkaClusterConfig = config.getConfig("kafka-cluster");
        kafkaConsumerConfig = config.getConfig("kafka-readall-consumer");
        Properties kafkaClusterProperties = ConfigUtils.toProperties(kafkaClusterConfig);
        AdminClient adminClient = KafkaUtils.createAdminClient(kafkaClusterProperties);
//        KafkaUtils.deleteTopics(
//            Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic), adminClient);
//        Thread.sleep(5000);
//        KafkaUtils.createTopics(Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic),
//            adminClient, Optional.of(1), Optional.empty());


        akka.actor.typed.ActorSystem<StoreAppMessage> system =
            akka.actor.typed.ActorSystem.create(StoreApp.create(), "StoreApp");
        system.tell(new StoreStart());
    }

    public StoreApp(ActorContext<StoreAppMessage> context) {
        super(context);
    }

    public static Behavior<StoreApp.StoreAppMessage> create() {
        return Behaviors.setup(StoreApp::new);
    }

    @Override
    public Receive<StoreAppMessage> createReceive() {
        ReceiveBuilder<StoreAppMessage> genStoreAppReceiveBuilder = newReceiveBuilder();
        genStoreAppReceiveBuilder.onMessage(StoreStart.class, this::onStart);
        return genStoreAppReceiveBuilder.build();
    }

    protected Behavior<StoreAppMessage> onStart(StoreStart start) {
        TreeMap<Double, List<Customer>> customerSchedule = new TreeMap<>();
        customerSchedule.put(1.0, Collections.singletonList(
            Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build()));
        customerSchedule.put(2.0, Collections.singletonList(
            Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build()));
        DoubleSimTime t0 = DoubleSimTime.builder().t(0.0).build();
        CustomerGenerator customerGenerator = new CustomerGenerator(customerSchedule);
        ActorRef<DevsMessage> customerSimulator =
            getContext().spawn(PDevsSimulator.create(customerGenerator, t0), "customerGenerator");

        ClerkModel clerkModel = new ClerkModel("clerk1");
        ActorRef<DevsMessage> clerk1Simulator = getContext().spawn(
            PDevsSimulator.create(clerkModel, t0), "clerk1Simulator");

        ActorRef<DevsMessage> clerkProxy = getContext().spawn(
            KafkaDevsStreamProxy.create("clerkProxy", clerkInputTopic,
                kafkaClusterConfig), "clerkProxy");

        StoreObserver storeObserver = new StoreObserver(null);
        ActorRef<DevsMessage> storeObserverSimulator =
            getContext().spawn(PDevsSimulator.create(storeObserver, t0), "storeObserver");

        PDevsCouplings storeCouplings = new PDevsCouplings(Collections.emptyList(),
            Collections.singletonList(new StoreCouplingHandler()));

        Map<String, ActorRef<DevsMessage>> modelSimulators = new HashMap<>();
        modelSimulators.put(customerGenerator.getModelIdentifier(), customerSimulator);
        modelSimulators.put(clerkModel.getModelIdentifier(), clerkProxy);
        modelSimulators.put(storeObserver.getModelIdentifier(), storeObserverSimulator);

        ActorRef<DevsMessage> storeCoordinator = getContext().spawn(PDevsCoordinator.create(
                "storeCoordinator", "root", modelSimulators, storeCouplings),
            "storeCoordinator");

        ActorRef<DevsMessage> storeCoordinatorReceiver = getContext().spawn(
            KafkaReceiver.create(storeCoordinator, clerk1Simulator, kafkaConsumerConfig, storeCoordinatorInputTopic),
            "coordinatorReceiver");

        if (runLocal) {
            ActorRef<DevsMessage> clerk1Receiver = getContext().spawn(
                KafkaReceiver.create(clerk1Simulator, storeCoordinator, kafkaConsumerConfig,
                    clerkInputTopic), "clerk1Receiver");
        }


        ActorRef<DevsMessage> rootCoordinator = getContext().spawn(RootCoordinator.create(
            DoubleSimTime.builder().t(8.0).build(), storeCoordinator), "rootCoordinator");
        rootCoordinator.tell(InitSim.builder().time(DoubleSimTime.builder().t(0.0).build()).build());
        return Behaviors.same();
    }
}
