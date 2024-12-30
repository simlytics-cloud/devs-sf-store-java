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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


/**
 * Represents an abstract definition of a customer with attributes for waiting time, entering time,
 * and leaving time.
 * <p>
 * This class is designed to be immutable and serialized/deserialized using JSON. Specific
 * implementations can extend this class and provide concrete functionality or additional
 * attributes.
 */
@Value.Immutable
@JsonSerialize(as = Customer.class)
@JsonDeserialize(as = Customer.class)
public abstract class AbstractCustomer {

  /**
   * Retrieves the waiting time (Twait) associated with the customer.
   *
   * @return the waiting time of the customer as a double.
   */
  abstract double getTwait();

  /**
   * Retrieves the entering time (Tenter) associated with the customer.
   *
   * @return the entering time of the customer as a double.
   */
  abstract double getTenter();

  /**
   * Retrieves the leaving time (Tleave) associated with the customer.
   *
   * @return the leaving time of the customer as a double.
   */
  abstract double getTleave();
}
