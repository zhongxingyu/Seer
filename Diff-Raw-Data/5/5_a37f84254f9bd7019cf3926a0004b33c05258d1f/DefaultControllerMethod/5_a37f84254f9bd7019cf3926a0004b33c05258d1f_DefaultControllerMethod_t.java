 /***
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package br.com.caelum.vraptor4.controller;
 
 import static com.google.common.base.Objects.equal;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import br.com.caelum.vraptor4.util.Stringnifier;
 
 public class DefaultControllerMethod implements ControllerMethod {
 
 	private final BeanClass controller;
 	private final Method method;
 
 	public DefaultControllerMethod(BeanClass controller, Method method) {
 		this.controller = controller;
 		this.method = method;
 	}
 
 	public static ControllerMethod instanceFor(Class<?> type, Method method) {
 		return new DefaultControllerMethod(new DefaultBeanClass(type), method);
 	}
 
 	public Method getMethod() {
 		return method;
 	}
 
 	public BeanClass getController() {
 		return controller;
 	}
 
 	public boolean containsAnnotation(Class<? extends Annotation> annotation) {
 		return method.isAnnotationPresent(annotation);
 	}
 	
 	@Override
 	public Annotation[] getAnnotations(){
 		return method.getAnnotations();
 	}
 
 	@Override
 	public String toString() {
		return "[DefaultControllerMethod: " + Stringnifier.simpleNameFor(method) + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((method == null) ? 0 : method.hashCode());
 		result = prime * result + ((controller == null) ? 0 : controller.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		DefaultControllerMethod other = (DefaultControllerMethod) obj;
 		return equal(method, other.method) && equal(controller, other.controller);
 	}
 
 }
