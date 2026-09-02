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
import devs.experimentalframe.Generator;
import devs.iso.PortValue;
import devs.iso.time.DoubleSimTime;
import devs.msg.state.ScheduleState;
import devs.utils.Schedule;
import devs.utils.Schedule.ScheduledEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * The CustomerGenerator class is a PDEVS (Parallel Discrete Event System Specification) model
 * responsible for generating customers at specified times.
 * <p>
 * This class utilizes TreeMap data structures to manage the model's state, where keys represent
 * time points and values correspond to lists of customers to be generated.
 * <p>
 * Key responsibilities of this class include: - Managing customer generation based on pre-defined
 * time intervals stored in the model state. - Removing processed customers during internal state
 * transitions. - Generating an output containing customers scheduled for the current time. -
 * Calculating the time advance based on the next customer generation event.
 * <p>
 * The model defines one static output port, `generatorOutputPort`, for transmitting generated
 * customers.
 */
public class CustomerGenerator extends Generator<DoubleSimTime> {

  /**
   * Represents the unique identifier for the model.
   * <p>
   * In the context of the CustomerGenerator class, the identifier serves as a distinguishing label
   * for the customer generation model.
   */

  /**
   * A static output port used to transmit generated customers from the CustomerGenerator model.
   * <p>
   * This port, named "OUTPUT", is associated with the Customer class and is utilized during the
   * output phase of the CustomerGenerator model. Generated customers, as per the model's state, are
   * sent through this port at specified simulation times.
   * <p>
   * The primary purpose of `generatorOutputPort` is to facilitate communication of customer data to
   * other connected models within the simulation environment.
   */
  public static Port<Customer> generatorOutputPort = new Port<>("OUTPUT", Customer.class);

  /**
   * Constructs a CustomerGenerator with the given model state.
   *
   * @param customerSchedule the schedule of customer arrivals
   */
  public CustomerGenerator(Schedule<DoubleSimTime> customerSchedule, String modelIdentifier) {
    super(modelIdentifier, new ScheduleState<>(DoubleSimTime.create(0.0), customerSchedule));
  }

  @Override
  public void handleScheduledEvents(List<Object> events) {
    // There are no internal events for this model
  }

  @Override
  public void externalStateTransitionFunction(DoubleSimTime doubleSimTime, List<PortValue<?>> inputs) {

  }

  @Override
  public void confluentStateTransitionFunction(List<PortValue<?>> inputs) {

  }

  /**
   * Determines the time until the next internal event occurs for the model.
   *
   * @return the time until the model's next internal event. If the model state is empty, returns
   * DoubleSimTime representing a very large value, otherwise calculates the time difference to the
   * simulation time for the first key in the model state.
   */
  @Override
  public DoubleSimTime timeAdvanceFunction() {
    if (modelState.getSchedule().isEmpty()) {
      return DoubleSimTime.builder().t(Double.MAX_VALUE).build();
    } else {
      return modelState.getSchedule().getFirstEventTime()
          .minus(modelState.getCurrentTime());
    }
  }


}
