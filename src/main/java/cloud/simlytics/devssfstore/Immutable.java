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
import java.lang.reflect.Modifier;

public interface Immutable<M extends Mutable<?>> extends MutableImmutable {
  default M toMutable() {
    try {
      // 1. Figure out the target mutable class
      String immutableClassName = this.getClass().getName();
      String mutableClassName = immutableClassName.replace("Immutable", "");

      Class<?> mutableClass = Class.forName(mutableClassName);
      Object mutableInstance = mutableClass.getDeclaredConstructor().newInstance();

      // 2. Copy fields using reflection
      for (Field field : this.getClass().getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;

        field.setAccessible(true);
        Object value = field.get(this);

        Object transformedValue = MutabilityUtil.toMutable(value);

        try {
          Field mutableField = mutableClass.getDeclaredField(field.getName());
          mutableField.setAccessible(true);
          mutableField.set(mutableInstance, transformedValue);
        } catch (NoSuchFieldException ignored) {
          // skip unknown fields
        }
      }

      return (M) mutableInstance;

    } catch (Exception e) {
      throw new RuntimeException("Failed to convert to mutable: " + this.getClass().getName(), e);
    }
  }
}
