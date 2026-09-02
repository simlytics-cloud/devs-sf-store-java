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

import cloud.simlytics.devssfstore.StoreApp.ModelStructure;
import devs.PDEVSModel;
import devs.Port;
import devs.iso.PortValue;
import devs.iso.time.DoubleSimTime;
import devs.msg.state.TimeState;
import java.util.Collections;
import java.util.List;

/**
 * StoreObserver is a specialized PDEVS model used to monitor the behavior of customers within a
 * simulation. It observes and records customer departure times and their respective waiting times.
 * This model performs state transitions and manages incoming data related to customer actions.
 */
public class StoreObserver extends PDEVSModel<DoubleSimTime, TimeState<DoubleSimTime>> {


  /**
   * Represents an input port for receiving `Customer` objects in the `StoreObserver` model. This
   * port is used to handle incoming data related to customer activities, such as their departure
   * times and waiting periods, within the simulation.
   */
  public static Port<Customer> observerInputPort = new Port<>("INPUT", Customer.class);

  /**
   * Constructs a StoreObserver model used to monitor customer behaviors. This model observes
   * customer departure times and waiting times in a simulation.
   *
   * @param modelState The initial state of the model, representing a `Void` state in this context.
   */
  public StoreObserver(Void modelState) {
    super(new TimeState<>(DoubleSimTime.create(0.0)), ModelStructure.storeObserver);
  }

  @Override
  public void internalStateTransitionFunction() {
    throw new IllegalStateException("Internal state transition not supported for StoreObserver");
  }

  /**
   * Executes the external state transition function for the StoreObserver model. This method
   * processes input data received through the input port, determining the departure time and wait
   * duration of customers.
   *
   * @param elapsedTime The simulation time at which this external event occurs.
   * @param inputs        The collection of port values containing data input for the current
   *                      event.
   */
  @Override
  public void externalStateTransitionFunction(DoubleSimTime elapsedTime, List<PortValue<?>> inputs) {
    DoubleSimTime currentTime = modelState.getCurrentTime().plus(elapsedTime);
    modelState.setCurrentTime(currentTime);
    for (PortValue<?> pv : inputs) {
      Customer customer = observerInputPort.getValue(pv);
      System.out.println("Customer leaving at " + currentTime.getT()
          + " after a wait of " + customer.getTwait());
    }
  }

  @Override
  public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {
    throw new IllegalStateException("Confluent state transition not supported for StoreObserver");
  }

  /**
   * Calculates the time until the next internal state transition for the StoreObserver model. This
   * method computes the time difference between the current simulation time and a predetermined
   * value.
   *
   * @return A DoubleSimTime object representing the time until the next internal event.
   */
  @Override
  public DoubleSimTime timeAdvanceFunction() {
    return DoubleSimTime.builder().t(Double.MAX_VALUE).build();
  }

  /**
   * Generates the output for the StoreObserver model during the simulation. This function creates
   * and returns an empty Bag object, indicating no output is produced.
   *
   * @return A Bag object containing the output data, which is empty in this implementation.
   */
  @Override
  public List<PortValue<?>> outputFunction() {
    return Collections.emptyList();
  }
}
