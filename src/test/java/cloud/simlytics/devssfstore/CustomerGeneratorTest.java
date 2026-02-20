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

import static org.junit.jupiter.api.Assertions.assertEquals;

import devs.iso.PortValue;
import devs.iso.time.DoubleSimTime;
import devs.utils.Schedule;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for the CustomerGenerator component.
 * <p>
 * This test ensures that the CustomerGenerator behaves as expected when generating customers based
 * on a predefined schedule stored in a TreeMap.
 */
@DisplayName("Test Customer Generator")
class CustomerGeneratorTest {

  private final Schedule<DoubleSimTime> customerSchedule = new Schedule<>();

  CustomerGeneratorTest() {

    customerSchedule.scheduleOutput(DoubleSimTime.create(1.0), CustomerGenerator.generatorOutputPort,
        Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build());
    customerSchedule.scheduleOutput(DoubleSimTime.create(2.0), CustomerGenerator.generatorOutputPort,
        Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build());
  }


  /**
   * Verifies the correct functionality of customer generation using a predefined schedule in the
   * {@code CustomerGenerator} model.
   * <p>
   * The test is designed to ensure that: 1. Customers are generated at the expected simulation
   * times as defined by the schedule. 2. The {@code timeAdvanceFunction} correctly calculates the
   * time to the next event. 3. Generated customers have accurate and expected properties, such as
   * wait times and enter times.
   * <p>
   * Test Procedure: - Initialize a {@code CustomerGenerator} instance with a predefined schedule. -
   * Use {@code timeAdvanceFunction} to verify the time until the next event. - Invoke the
   * {@code outputFunction} to retrieve and verify the properties of generated customers. - Execute
   * the {@code internalStateTransitionFunction} to confirm the removal of processed customers. -
   * Repeat the above steps to validate sequential customer generation and state updates.
   */
  @Test
  @DisplayName("Test generation of customers from a table")
  void testCustomerGeneration() {
    CustomerGenerator customerGenerator = new CustomerGenerator(customerSchedule, StoreApp.ModelStructure.customerGenerator);
    DoubleSimTime t0 = DoubleSimTime.builder().t(0.0).build();

    // First customer out should be at t = 1;
    DoubleSimTime t1 = customerGenerator.timeAdvanceFunction();
    assert t1.getT() == 1;

    // Get first customer
    List<PortValue<?>> output1 = customerGenerator.outputFunction();
    Customer customer1 = CustomerGenerator.generatorOutputPort
        .getValue(output1.get(0));
    assertEquals(1.0, customer1.getTenter(), 0.01);
    assertEquals(1.0, customer1.getTwait(), 0.01);

    // Execute internal transition at t = 1, removing customer 1 from the list
    customerGenerator.internalStateTransitionFunction();

    // Next customer should exit at t = 2
    DoubleSimTime t2 = customerGenerator.timeAdvanceFunction();
    assert t2.getT() == 1;

    // Get first customer
    List<PortValue<?>> output2 = customerGenerator.outputFunction();
    Customer customer2 = CustomerGenerator.generatorOutputPort
        .getValue(output2.get(0));
    assertEquals(2.0, customer2.getTenter(), 0.01);
    assertEquals(4.0, customer2.getTwait(), 0.01);
  }

}