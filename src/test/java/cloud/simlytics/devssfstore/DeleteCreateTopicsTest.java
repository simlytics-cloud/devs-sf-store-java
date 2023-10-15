package cloud.simlytics.devssfstore;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devs.utils.ConfigUtils;
import devs.utils.KafkaUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DeleteCreateTopicsTest {

  static private final String clerkInputTopic = "clerk1";
  static private final String storeCoordinatorInputTopic = "storeCoordinator";

  @Test
  @DisplayName("Delete and create topics")
  void deleteAndCreateTopics() throws InterruptedException, ExecutionException {
    Config config = ConfigFactory.load();
    Config kafkaClusterConfig = config.getConfig("kafka-cluster");
    Properties kafkaClusterProperties = ConfigUtils.toProperties(kafkaClusterConfig);
    AdminClient adminClient = KafkaUtils.createAdminClient(kafkaClusterProperties);
    KafkaUtils.deleteTopics(
        Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic), adminClient);
    Thread.sleep(5000);
    KafkaUtils.createTopics(Arrays.asList(clerkInputTopic, storeCoordinatorInputTopic),
        adminClient, Optional.of(1), Optional.empty());
  }
}
