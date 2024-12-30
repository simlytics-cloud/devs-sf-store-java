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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import devs.utils.ConfigUtils;
import devs.utils.KafkaUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * A test class to validate the deletion and creation of Kafka topics for testing purposes. This
 * class is intended to perform the following sequence:
 * <p>
 * 1. Deletes specified Kafka topics using the Kafka Admin API. 2. Re-creates the deleted topics
 * with specific configurations.
 * <p>
 * The test primarily ensures that: - The Kafka Admin client can successfully connect to a Kafka
 * cluster and perform administrative operations. - Topics can be deleted and re-created reliably
 * within tests to simulate a clean state. - Proper handling of configurations like replication
 * factor and partition count during topic creation.
 * <p>
 * Dependencies: - Connection to a Kafka cluster is required for this test to run. - Configuration
 * is retrieved from a configuration file through the Typesafe Config library. - Utility methods
 * from `KafkaUtils` are used for topic management.
 * <p>
 * This test is disabled by default unless a connection to a Kafka cluster can be established. This
 * is indicated by the `@Disabled` annotation.
 */
public class DeleteCreateTopicsTest {

  private static final String clerkInputTopic = "clerk1";
  private static final String storeCoordinatorInputTopic = "storeCoordinator";

  /**
   * Deletes and re-creates specified Kafka topics for testing purposes.
   * <p>
   * This test method performs the following steps: 1. Loads the Kafka cluster configuration using
   * the Typesafe Config library. 2. Creates an AdminClient instance to interact with the Kafka
   * cluster. 3. Deletes the specified Kafka topics using the `deleteTopics` method. 4. Waits for a
   * brief period to allow deletion operations to complete. 5. Re-creates the deleted topics with
   * specified parameters such as the number of partitions and replication factor using the
   * `createTopics` method.
   * <p>
   * Preconditions: - A connection to a functional Kafka cluster is required for this test to run. -
   * Required Kafka topic names are defined and set up in the test class. - Typesafe Config library
   * is used to load Kafka configuration files.
   * <p>
   * Postconditions: - The specified Kafka topics will be deleted and then re-created. This ensures
   * a clean state for subsequent tests or operations relying on these topics.
   * <p>
   * Note: - This test is disabled by default and can only be executed in an environment with a
   * valid Kafka connection, as indicated by the `@Disabled` annotation. - The thread sleep is used
   * to ensure the deletion process is complete before proceeding to the topic creation step, which
   * may require fine-tuning based on the Kafka cluster's performance and settings.
   *
   * @throws InterruptedException if the thread sleep is interrupted.
   * @throws ExecutionException   if an error occurs during Kafka Admin operations.
   */
  @Disabled("Requires Kafka connection")
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
