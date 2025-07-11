/*
 * DEVS Streaming Framework Store Java Copyright (C) 2025 simlytics.cloud LLC and
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

import devs.Port;
import devs.msg.PortValue;

public class ImmutablePort<T> extends Port<T> {

  /**
   * Constructs a Port instance with a specified identifier and associated data type.
   *
   * @param portIdentifier the unique identifier of the port
   * @param clazz          the class type representing the data type associated with the port
   */
  public ImmutablePort(String portIdentifier, Class<T> clazz) {
    super(portIdentifier, clazz);
  }

  @Override
  public PortValue<T> createPortValue(T value) {
    if (value instanceof Mutable mutable) {
      return (PortValue<T>) new PortValue<>(mutable.toImmutable(), getPortIdentifier());
    } else {
      return super.createPortValue(value);
    }
  }

  @Override
  public T getValue(PortValue<?> portValue) {
    if (portValue.getValue() instanceof Immutable immutable) {
      return (T) immutable.toMutable();
    } else {
      return super.getValue(portValue);
    }
  }
}
