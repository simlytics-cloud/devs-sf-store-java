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

      Class<?> builderClass = Arrays.stream(immutableClass.getDeclaredClasses())
          .filter(c -> c.getSimpleName().endsWith("Builder"))
          .findFirst().get();
      Object builder = immutableClass.getMethod("builder").invoke(null);

      for (Field field : fields) {
        field.setAccessible(true);
        Object fieldValue = MutabilityUtil.toImmutable(field.get(this));
        String setterMethodName = field.getName();

        builderClass.getMethod(setterMethodName, field.getType())
            .invoke(builder, fieldValue);
      }

      return (I) builderClass.getMethod("build").invoke(builder);

    } catch (Exception e) {
      throw new RuntimeException("Failed to convert to immutable: " + this.getClass().getName(), e);
    }
  }
}
