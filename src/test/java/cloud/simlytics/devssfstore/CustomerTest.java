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

import com.fasterxml.jackson.databind.ObjectMapper;
import devs.msg.Bag;
import devs.msg.DevsMessage;
import devs.msg.ExecuteTransition;
import devs.msg.PortValue;
import devs.msg.time.LongSimTime;
import devs.utils.DevsObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A test class for verifying the serialization and deserialization of Customer objects and
 * interactions with the related model components. This test ensures proper functionality of the
 * object mapping mechanisms and the creation and handling of port values in the simulation
 * framework.
 * <p>
 * The class makes use of the ObjectMapper utility for JSON operations and tests the correctness of
 * Customer serialization, deserialization, and integration within ExecuteTransition messages.
 */
public class CustomerTest {

  ObjectMapper objectMapper = DevsObjectMapper.buildObjectMapper();

  /**
   * Tests the serialization and deserialization of a Customer object within the context of a
   * simulation framework, ensuring the proper creation and handling of PortValue and
   * ExecuteTransition components.
   * <p>
   * The test verifies that: - A Customer object can be serialized into its JSON representation. - A
   * JSON representation of an ExecuteTransition containing a Customer PortValue is correctly
   * deserialized back into its original object form. - The integration and correctness of
   * interactions between PortValue, ExecuteTransition, and the underlying Customer instance are
   * validated.
   * <p>
   * This method makes use of the ObjectMapper to perform JSON serialization and deserialization,
   * and asserts type correctness and structure during the transformation process.
   *
   * @throws IOException if an I/O error occurs during JSON operations.
   */
  @Test
  @DisplayName("Serialize and deserialize customer port value")
  void serializeDeserializeCustomerPortValue() throws IOException {
    Customer customer1 = Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build();

    String customer1Json = objectMapper.writeValueAsString(customer1);
    System.out.println(customer1Json);

    PortValue<Customer> pv = ClerkModel.clerkInputPort.createPortValue(customer1);
    Bag inputBag = Bag.builder().addPortValueList(pv).build();
    ExecuteTransition<?> executeTransition = ExecuteTransition.builder()
        .time(LongSimTime.builder().t(0L).build())
        .modelInputsOption(inputBag)
        .build();

    String executeTransitionJson2 = objectMapper.writeValueAsString(executeTransition);
    System.out.println(executeTransitionJson2);

    DevsMessage devsMessage = objectMapper.readValue(executeTransitionJson2, DevsMessage.class);
    assert devsMessage instanceof ExecuteTransition;
    ExecuteTransition<LongSimTime> executeTransitionDes = (ExecuteTransition<LongSimTime>)
        devsMessage;
    PortValue<?> pvDes = executeTransitionDes.getModelInputsOption().get().getPortValueList()
        .get(0);
    assert pvDes.getValue() instanceof Customer;

  }

}
