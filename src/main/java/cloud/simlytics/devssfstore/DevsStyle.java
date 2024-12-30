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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.immutables.value.Value;

/**
 * Annotation to customize the style and naming conventions for immutable objects in a project. This
 * annotation integrates with tools or libraries that generate immutable objects based on the
 * annotated types.
 * <p>
 * Use this annotation on abstract class definitions or packages, and apply the provided
 * configuration for type prefixes, suffixes, and builder patterns across the annotated elements.
 * <p>
 * Features: - Expects abstract class names to begin with "Abstract*". - Immutable implementation
 * names are produced without the "Abstract" prefix (e.g., AbstractPerson becomes Person). - Enables
 * a staged builder pattern for constructed objects.
 * <p>
 * Targets: - Can be applied to entire packages or individual types.
 * <p>
 * Annotation properties and configuration are defined according to the requirements of the applied
 * library.
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Value.Style(typeAbstract = "Abstract*", typeImmutable = "*", stagedBuilder = true)
public @interface DevsStyle {

}


