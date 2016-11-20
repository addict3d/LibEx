/**
 * Copyright 2012 John Butler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language erning permissions and
 *  limitations under the License.
 */
package org.libex.annotation;

import java.lang.annotation.Documented;

/**
 * This annotation denotes a method that is overridden the framework (i.e.
 * Spring).
 * 
 * @author John Butler
 */
@Documented
public @interface FrameworkOverridden {
	String value() default "";
}
