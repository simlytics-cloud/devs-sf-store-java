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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;


public interface Mutable<I extends Immutable<?>> extends MutableImmutable {


  @SuppressWarnings("unchecked")
  default I toImmutable() {
    
    try {
      // 1. Resolve Immutable class name by convention
      String mutableClassName = this.getClass().getName();
      String immutableClassName =
          mutableClassName.substring(0, mutableClassName.lastIndexOf('.') + 1)
              + "Immutable" + mutableClassName.substring(mutableClassName.lastIndexOf('.') + 1);

      Class<?> immutableClass = Class.forName(immutableClassName);

      // 2. Collect constructor arguments by field order
      Field[] fields = getAllFields(this.getClass());

      Constructor<?> ctor = Arrays.stream(immutableClass.getDeclaredConstructors())
          .filter(constructor -> constructor.getParameterCount() == fields.length)
          .findFirst()
          .orElseThrow(() -> new RuntimeException(
              "No matching constructor found for " + immutableClassName));

      Object[] args = Arrays.stream(ctor.getParameters())
          .map(parameter -> {
            String paramName = parameter.getName();
            Class<?> paramType = parameter.getType();
            return Arrays.stream(fields)
                .filter(
                    field -> field.getName().equals(paramName) && field.getType().equals(paramType))
                .findFirst()
                .map(field -> {
                  try {
                    field.setAccessible(true);
                    return MutabilityUtil.toImmutable(field.get(this));
                  } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                  }
                })
                .orElseThrow(
                    () -> new RuntimeException("No matching field for parameter: " + paramName));
          })
          .toArray();

      return (I) ctor.newInstance(args);

    } catch (Exception e) {
      throw new RuntimeException("Failed to convert to immutable: " + this.getClass().getName(), e);
    }
  }
}
