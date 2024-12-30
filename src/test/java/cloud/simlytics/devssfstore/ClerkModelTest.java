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

import devs.msg.Bag;
import devs.msg.PortValue;
import devs.msg.time.DoubleSimTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test class for verifying the behavior and correctness of the {@code ClerkModel} class. This
 * test ensures the accurate functioning of the clerk model in a PDEVS simulation environment,
 * including proper handling of time advancement, external, internal, and confluent state
 * transitions, as well as correct customer processing through the model's ports.
 * <p>
 * The test performs the following validations: - Verifies that the time advance function defaults
 * to a maximum value when no customers are being served. - Simulates the arrival of customers
 * through the input port and verifies proper state transitions. - Validates the output of customers
 * through the output port after processing. - Ensures correct functionality of confluent state
 * transitions when simultaneous events are processed. - Confirms the correct updating of customer
 * leave times and model state across transitions.
 * <p>
 * This test takes an integrated approach to validate the clerk model's behavior in a simulated
 * environment.
 */
class ClerkModelTest {

  /**
   * Tests the functionality and correctness of the {@code ClerkModel} class in a PDEVS (Parallel
   * Discrete Event System Specification) simulation environment.
   * <p>
   * This method verifies the following behaviors of the {@code ClerkModel}:
   * <p>
   * 1. Ensures that the time advance function initially returns the maximum time value when the
   * model is in its idle state. 2. Simulates the reception of customer entities through the input
   * port and validates proper external state transitions. 3. Confirms the correctness of the
   * {@code outputFunction}, ensuring that processed customers are output with accurate properties,
   * especially their leave times. 4. Verifies the confluent state transition by processing external
   * and internal events occurring at the same simulation time. 5. Validates the correct progression
   * of time advance intervals after state transitions. 6. Ensures accurate processing, including
   * updating customer-specific attributes such as wait times and leaving times.
   * <p>
   * Test assertions are used to confirm the expected outcomes for each operation, including time
   * advance intervals, output transition behavior, and model state updates.
   */
  @Test
  @DisplayName("Test clerk model")
  void testClerkModel() {

    ClerkModel clerkModel = new ClerkModel("clerkModel");
    DoubleSimTime t1 = DoubleSimTime.builder().t(1.0).build();

    // next time should be max value
    assert clerkModel.timeAdvanceFunction(t1).getT() == Double.MAX_VALUE;

    // Send first customer
    Customer customer1 = Customer.builder().twait(1.0).tenter(1.0).tleave(0.0).build();
    PortValue<Customer> pv = ClerkModel.clerkInputPort.createPortValue(customer1);
    Bag bag1 = Bag.builder().addPortValueList(pv).build();
    clerkModel.externalStateTransitionFunction(t1, bag1);

    // Next time should be 2
    DoubleSimTime t = clerkModel.timeAdvanceFunction(t1);
    assertEquals(1.0, t.getT(), 0.01);

    // Output first customer and do confluent state transition at t2
    Bag outBag2 = clerkModel.outputFunction();
    Customer outCustomer2 = ClerkModel.clerkOutputPort.getValue(outBag2.getPortValueList().get(0));
    assertEquals(2.0, outCustomer2.getTleave(), 0.01);

    Customer inCustomer2 = Customer.builder().twait(4.0).tenter(2.0).tleave(0.0).build();
    Bag inBag2 = Bag.builder().addPortValueList(
        ClerkModel.clerkInputPort.createPortValue(inCustomer2)).build();
    clerkModel.confluentStateTransitionFunction(DoubleSimTime.create(2.0), inBag2);

    // Next transition should be at t = 6
    DoubleSimTime t4 = clerkModel.timeAdvanceFunction(DoubleSimTime.create(2.0));
    assertEquals(4.0, t4.getT(), 0.01);

    Bag outBag6 = clerkModel.outputFunction();
    Customer outCustomer6 = ClerkModel.clerkOutputPort.getValue(outBag6.getPortValueList().get(0));
    assertEquals(6.0, outCustomer6.getTleave(), 0.01);
  }

}