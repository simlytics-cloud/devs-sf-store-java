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
import java.util.Collections;
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
      return (T) toImmutableCollection(obj);// returns List<Object>
    }else if (obj instanceof Map<?, ?>) {
      return (T) toImmutableMap(obj);// returns List<Object>
    } else {
      return (T) obj;
    }
  }


  @SuppressWarnings("unchecked")
  public static <T> T toImmutableCollection(Object input) {
    if (input instanceof Collection<?> collection) {
      Collection<?> immutableCollection;

      if (collection instanceof java.util.ArrayList) {
        immutableCollection = com.google.common.collect.ImmutableList.copyOf(
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .toList()
        );
      } else if (collection instanceof java.util.LinkedHashSet) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
      } else if (collection instanceof java.util.HashSet) {
        immutableCollection = com.google.common.collect.ImmutableSet.copyOf(
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .toList()
        );
      } else if (collection instanceof java.util.TreeSet) {
        immutableCollection = com.google.common.collect.ImmutableSortedSet.copyOf(
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .toList()
        );
      } else if (collection instanceof java.util.Stack) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.Stack::new));
      } else if (collection instanceof java.util.Vector) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.Vector::new));
      } else if (collection instanceof java.util.LinkedList) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.LinkedList::new));
      } else if (collection instanceof java.util.PriorityQueue) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.PriorityQueue::new));
      } else if (collection instanceof java.util.ArrayDeque) {
        immutableCollection =
            collection.stream()
                .map(MutabilityUtil::toImmutable)
                .collect(Collectors.toCollection(java.util.ArrayDeque::new));
      } else {
        immutableCollection = collection.stream()
            .map(MutabilityUtil::toImmutable)
            .toList();
      }
      return (T) immutableCollection;
    }
    return (T) toImmutable(input);
  }

  @SuppressWarnings("unchecked")
  public static <T> T toImmutableMap(Object input) {
    if (input instanceof Map<?, ?> map) {
      Map<?, ?> immutableMap;

      if (map instanceof java.util.LinkedHashMap) {
        immutableMap = map.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toImmutable(e.getValue()),
                (k1, k2) -> k1,
                java.util.LinkedHashMap::new
            ));
      } else if (map instanceof java.util.TreeMap) {
        immutableMap = com.google.common.collect.ImmutableSortedMap.copyOf(
            map.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> toImmutable(e.getValue())
                ))
        );
      } else {
        immutableMap = com.google.common.collect.ImmutableMap.copyOf(
            map.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> toImmutable(e.getValue())
                ))
        );
      }
      return (T) immutableMap;
    }
    return (T) toImmutable(input);
  }


  @SuppressWarnings("unchecked")
  public static <T> T toMutableCollection(Object input) {

    if (input instanceof Collection<?> collection) {
      if (collection instanceof java.util.ArrayList) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.ArrayList::new));
      } else if (collection instanceof com.google.common.collect.ImmutableList) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.ArrayList::new));
      } else if (collection instanceof java.util.LinkedHashSet) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
      } else if (collection instanceof java.util.HashSet) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.HashSet::new));
      } else if (collection instanceof com.google.common.collect.ImmutableSortedSet) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.TreeSet::new));
      } else if (collection instanceof com.google.common.collect.ImmutableSet) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.HashSet::new));
      } else if (collection instanceof java.util.TreeSet) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.TreeSet::new));
      } else if (collection instanceof java.util.LinkedList) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.LinkedList::new));
      } else if (collection instanceof java.util.Stack) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.Stack::new));
      } else if (collection instanceof java.util.Vector) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.Vector::new));
      } else if (collection instanceof java.util.PriorityQueue) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.PriorityQueue::new));
      } else if (collection instanceof java.util.ArrayDeque) {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.ArrayDeque::new));
      } else {
        return (T) collection.stream()
            .map(MutabilityUtil::toMutable)
            .collect(Collectors.toCollection(java.util.ArrayList::new));
      }
    }
    return (T) toMutable(input);
  }


  @SuppressWarnings("unchecked")
  public static <T> T toMutableMap(Object input) {
    if (input instanceof Map<?, ?> map) {
      if (map instanceof java.util.LinkedHashMap) {
        return (T) map.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toMutable(e.getValue()),
                (k1, k2) -> k1,
                java.util.LinkedHashMap::new
            ));
      } else if (map instanceof java.util.TreeMap) {
        return (T) map.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toMutable(e.getValue()),
                (k1, k2) -> k1,
                java.util.TreeMap::new
            ));
      } else {
        return (T) map.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> toMutable(e.getValue()),
                (k1, k2) -> k1,
                java.util.HashMap::new
            ));
      }
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
      return toMutableCollection(obj);
    } else if (obj instanceof Map<?, ?>) {
      return toMutableMap(obj);
    } else {
      return (T) obj;
    }
  }

}
