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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MutabilityUtil {

  @SuppressWarnings("unchecked")
  public static <T> T toImmutable(Object obj) {
    if (obj == null) return null;

    if (obj instanceof Immutable) {
      return (T) obj;
    } else if (obj instanceof Mutable<?>) {
      return (T) ((Mutable<?>) obj).toImmutable();
    } else if (obj instanceof Collection<?>) {
      return (T) ((Collection<?>) obj).stream()
          .map(MutabilityUtil::toImmutable)
          .collect(Collectors.toList()); // returns List<Object>
    } else {
      return (T) obj;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T toImmutableCollection(Object input) {
    if (input instanceof Collection<?>) {
      return (T) ((Collection<?>) input).stream()
          .map(MutabilityUtil::toImmutable)
          .toList();
    } else if (input instanceof Map<?, ?> map) {
      return (T) map.entrySet().stream()
          .collect(Collectors.toMap(
              e -> e.getKey(),
              e -> toImmutable(e.getValue())
          ));
    }
    return (T) toImmutable(input);
  }

  @SuppressWarnings("unchecked")
  public static <T> T toMutableCollection(Object input) {
    if (input instanceof Collection<?> collection) {
      return (T) collection.stream()
          .map(MutabilityUtil::toMutable)
          .collect(Collectors.toCollection(java.util.ArrayList::new));
    } else if (input instanceof Map<?, ?> map) {
      return (T) map.entrySet().stream()
          .collect(Collectors.toMap(
              entry -> entry.getKey(),
              entry -> toMutable(entry.getValue()),
              (k1, k2) -> k1,
              java.util.HashMap::new
          ));
    }
    return (T) toMutable(input);
  }

  @SuppressWarnings("unchecked")
  public static <T> T toMutable(Object obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof Mutable<?>) {
      return (T) obj;
    } else if (obj instanceof Immutable<?>) {
      return (T) ((Immutable<?>) obj).toMutable();
    } else if (obj instanceof Collection<?>) {
      return (T) ((Collection<?>) obj).stream()
          .map(MutabilityUtil::toMutable)
          .collect(Collectors.toCollection(java.util.ArrayList::new));
    } else if (obj instanceof Map<?, ?> map) {
      return (T) map.entrySet().stream()
          .collect(Collectors.toMap(
              entry -> entry.getKey(),
              entry -> toMutable(entry.getValue()),
              (k1, k2) -> k1,
              java.util.HashMap::new
          ));
    } else {
      return (T) obj;
    }
  }

}
