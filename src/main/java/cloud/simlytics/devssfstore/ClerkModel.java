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

import devs.PDEVSModel;
import devs.Port;
import devs.msg.Bag;
import devs.msg.PortValue;
import devs.msg.time.DoubleSimTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a clerk model in a PDEVS (Parallel Discrete Event System) simulation. This model
 * handles the processing of customers arriving and leaving the system, managing their states, and
 * transitioning between internal and external events.
 * <p>
 * The ClerkModel uses two ports: - clerkInputPort: To receive arriving customers. -
 * clerkOutputPort: To send departing customers.
 * <p>
 * The model maintains a state consisting of a list of customers currently at the clerk. Customers
 * are processed in a sequential order based on their arrival time.
 */
public class ClerkModel extends PDEVSModel<DoubleSimTime, List<Customer>> {

  /**
   * Represents the input port of the ClerkModel for receiving arriving customers. This port serves
   * as the entry point for customer entities entering the simulation model. Customers passed
   * through this port are queued and processed in a sequential order.
   * <p>
   * Port Details: - Name: "arrive" - Type: Customer
   * <p>
   * Usage: - External components send Customer instances to this port to indicate their arrival at
   * the clerk. - The model's external state transition processes customers arriving through this
   * port.
   */
  public static Port<Customer> clerkInputPort = new Port<>("arrive", Customer.class);

  /**
   * Represents the output port for the ClerkModel, used to send Customer entities when they are
   * done being processed. This port is associated with the "depart" event and is configured to
   * handle Customer objects.
   */
  public static Port<Customer> clerkOutputPort = new Port<>("depart", Customer.class);

  /**
   * Creates a new instance of the ClerkModel with the specified model identifier.
   *
   * @param modelIdentifier the unique identifier of the ClerkModel
   */
  public ClerkModel(String modelIdentifier) {
    super(new ArrayList<>(), modelIdentifier);
  }

  /**
   * Handles the internal state transition of the model based on the current simulation time. This
   * function is called when an internal event occurs within the model.
   * <p>
   * This method: 1. Removes the first customer from the model state, as they are leaving the system
   * at this time. 2. If there are still customers in the model state, it initiates the serving of
   * the next customer.
   *
   * @param doubleSimTime the current simulation time used to process the internal state
   *                      transition.
   */
  @Override
  public void internalStateTransitionFunction(DoubleSimTime doubleSimTime) {
    modelState.remove(0);  // remove customer that exits at this time
    if (!modelState.isEmpty()) {
      serveNextCustomer(doubleSimTime);
    }
  }

  /**
   * Serves the next customer in the model state by updating their leave time and ensuring the state
   * is properly updated to reflect this change.
   *
   * @param doubleSimTime the current simulation time used to calculate the customer's leave time
   */
  private void serveNextCustomer(DoubleSimTime doubleSimTime) {
    // Start serving next customer
    Customer nextCustomer = modelState.remove(0);
    nextCustomer = nextCustomer.withTleave(doubleSimTime.getT() + nextCustomer.getTwait());
    modelState.add(0, nextCustomer);
  }

  /**
   * Handles the external state transition of the model when external events occur. This function
   * processes input from external sources and updates the model state accordingly.
   *
   * @param doubleSimTime the current simulation time used to process the external state
   *                      transition.
   * @param bag           the collection of input port values received, containing data from
   *                      external sources.
   */
  @Override
  public void externalStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    for (PortValue<?> pv : bag.getPortValueList()) {
      Customer customer = clerkInputPort.getValue(pv);
      modelState.add(customer);
      if (modelState.size() == 1) { // If this is the first customer, start serving
        serveNextCustomer(doubleSimTime);
      }
    }
  }

  /**
   * Handles the confluent state transition of the model when both internal and external events
   * occur at the same simulation time.
   * <p>
   * This method combines the execution of the internal state transition function and the external
   * state transition function to address simultaneous events.
   *
   * @param doubleSimTime the current simulation time used to process the confluent state
   *                      transition.
   * @param bag           the collection of input port values received, containing data from
   *                      external sources.
   */
  @Override
  public void confluentStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {
    internalStateTransitionFunction(doubleSimTime);
    externalStateTransitionFunction(doubleSimTime, bag);
  }

  /**
   * Calculates the time until the next internal event within the simulation model. This function
   * determines the time-advance based on the current model state and simulation time. If the model
   * state is empty, it returns a large time value indicating no immediate events; otherwise, it
   * calculates the time based on the next customer's leave time.
   *
   * @param doubleSimTime the current simulation time used as a reference for the time-advance
   *                      calculation
   * @return the remaining simulation time as a {@code DoubleSimTime} object until the next internal
   * event occurs
   */
  @Override
  public DoubleSimTime timeAdvanceFunction(DoubleSimTime doubleSimTime) {
    if (modelState.isEmpty()) {
      return (DoubleSimTime) DoubleSimTime.builder().t(Double.MAX_VALUE).build()
          .minus(doubleSimTime);
    } else {
      double timeLeave = modelState.get(0).getTleave();
      return (DoubleSimTime) DoubleSimTime.builder().t(timeLeave).build().minus(doubleSimTime);
    }
  }

  /**
   * Generates the output of the model by providing the necessary data encapsulated in a
   * {@code Bag}. This method retrieves the first customer in the model's state and constructs the
   * output for the given simulation time.
   *
   * @return a {@code Bag} containing the output data, including the details of the exiting customer
   * forwarded through the output port
   */
  @Override
  public Bag outputFunction() {
    Customer exitingCustomer = modelState.get(0);
    return Bag.builder().addPortValueList(clerkOutputPort.createPortValue(exitingCustomer)).build();
  }
}
