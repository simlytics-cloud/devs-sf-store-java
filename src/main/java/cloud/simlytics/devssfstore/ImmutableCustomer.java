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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
public class ImmutableCustomer implements Immutable<Customer> {
  double twait;
  double tenter;
  double tleave;

  @Builder(toBuilder = true)
  @JsonCreator
  public ImmutableCustomer(
      @JsonProperty("twait") double twait,
      @JsonProperty("tenter") double tenter,
      @JsonProperty("tleave") double tleave) {
    this.twait = twait;
    this.tenter = tenter;
    this.tleave = tleave;
  }
}
