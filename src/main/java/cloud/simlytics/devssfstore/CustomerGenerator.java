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
import devs.msg.time.DoubleSimTime;
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
public class CustomerGenerator extends PDEVSModel<DoubleSimTime, TreeMap<Double, List<Customer>>> {

  /**
   * Represents the unique identifier for the model.
   * <p>
   * In the context of the CustomerGenerator class, the identifier serves as a distinguishing label
   * for the customer generation model.
   */
  public static String modelIdentifier = "customerGenerator";

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
   * @param modelState a TreeMap representing the model state, where the keys are Double values and
   *                   the values are lists of Customer objects.
   */
  public CustomerGenerator(TreeMap<Double, List<Customer>> modelState) {
    super(modelState, modelIdentifier);
  }

  /**
   * Handles the internal state transition of the model by removing customers at the specified
   * simulation time. These customers were already sent as output during the output function call.
   *
   * @param doubleSimTime the simulation time at which the internal state transition occurs. It can
   *                      be used to access the specific time value for this operation.
   */
  @Override
  public void internalStateTransitionFunction(DoubleSimTime doubleSimTime) {
    // Remove the customers generated at this time.  They were sent as output during the call to
    // the output function
    modelState.remove(doubleSimTime.getT());
  }

  @Override
  public void externalStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {

  }

  @Override
  public void confluentStateTransitionFunction(DoubleSimTime doubleSimTime, Bag bag) {

  }

  /**
   * Determines the time until the next internal event occurs for the model.
   *
   * @param doubleSimTime the current simulation time, used to calculate the time advance.
   * @return the time until the model's next internal event. If the model state is empty, returns
   * DoubleSimTime representing a very large value, otherwise calculates the time difference to the
   * simulation time for the first key in the model state.
   */
  @Override
  public DoubleSimTime timeAdvanceFunction(DoubleSimTime doubleSimTime) {
    if (modelState.isEmpty()) {
      return (DoubleSimTime) DoubleSimTime.builder().t(Double.MAX_VALUE).build()
          .minus(doubleSimTime);
    } else {
      return (DoubleSimTime) DoubleSimTime.builder().t(modelState.firstKey()).build()
          .minus(doubleSimTime);
    }
  }

  /**
   * Generates the output of the model by retrieving the first entry in the model state, extracting
   * the associated customers, and creating a Bag of output port values corresponding to these
   * customers.
   *
   * @return a Bag containing the generated output port values for the customers in the first entry
   * of the model state.
   */
  @Override
  public Bag outputFunction() {
    List<Customer> customers = modelState.firstEntry().getValue();
    Bag.Builder builder = Bag.builder();
    for (Customer customer : customers) {
      builder.addPortValueList(generatorOutputPort.createPortValue(customer));
    }
    return builder.build();
  }
}
