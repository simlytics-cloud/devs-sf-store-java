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


import devs.OutputCouplingHandler;
import devs.msg.PortValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The StoreCouplingHandler class is an implementation of the OutputCouplingHandler responsible for
 * managing the coupling of port values between models in a simulation. It acts as a mediator to
 * route data between various components, such as Clerks, Stores, and the Customer Generator.
 */
public class StoreCouplingHandler extends OutputCouplingHandler {


  /**
   * A constructor for the StoreCouplingHandler class, which extends OutputCouplingHandler. This
   * initializes the handler with no specific source, target, or port mappings. It provides default
   * empty configuration using Optional.empty() parameters.
   */
  public StoreCouplingHandler() {
    super(Optional.empty(), Optional.empty(), Optional.empty());
  }

  /**
   * Handles the routing of port values between components in a simulation based on the sender's
   * identity. It determines the appropriate mapping of port values and adds them to the receiver
   * map or output list.
   *
   * @param sender      The identifier of the sender component producing the port value.
   * @param portValue   The port value being handled and routed.
   * @param receiverMap A map of receiver component identifiers to a list of port values they
   *                    receive.
   * @param outputList  A list of port values to be routed to output components.
   */
  @Override
  public void handlePortValue(String sender, PortValue<?> portValue,
      Map<String, List<PortValue<?>>> receiverMap,
      List<PortValue<?>> outputList) {
    // Send messages from the clerk to the StoreObserver
    if (sender.startsWith("clerk")) {
      Customer customer = ClerkModel.clerkOutputPort.getValue(portValue);
      PortValue<Customer> inputPortValue = StoreObserver.observerInputPort.createPortValue(
          customer);
      addInputPortValue(inputPortValue, StoreObserver.modelIdentifier, receiverMap);
      // Send messages from the CustomerGenerator to clerk1
    } else if (sender.equals(CustomerGenerator.modelIdentifier)) {
      Customer customer = CustomerGenerator.generatorOutputPort.getValue(portValue);
      PortValue<Customer> inputPortValue = ClerkModel.clerkInputPort.createPortValue(customer);
      addInputPortValue(inputPortValue, "clerk1", receiverMap);
    }
  }
}
