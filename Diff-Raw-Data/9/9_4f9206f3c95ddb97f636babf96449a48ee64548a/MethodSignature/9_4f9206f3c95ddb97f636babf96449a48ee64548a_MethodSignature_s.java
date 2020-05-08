 /*
  * Copyright 2010 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.raffael.util.i18n;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public final class MethodSignature {
 
     private final Class<?> returnType;
     private final String name;
     private final List<Argument> arguments;
 
     private final int selectorCount;
 
     private ResourceIndicator indicator;
 
 
     public MethodSignature(Method method) {
         returnType = method.getReturnType();
         name = method.getName();
         Class<?>[] paramTypes = method.getParameterTypes();
         Annotation[][] paramAnnotations = method.getParameterAnnotations();
         Argument[] args = new Argument[paramTypes.length];
         int selectorCount = 0;
         for ( int i = 0; i < paramTypes.length; i++ ) {
             boolean selector = false;
             for ( Annotation a : paramAnnotations[i] ) {
                 if ( a instanceof Selector ) {
                     selectorCount++;
                     selector = true;
                 }
             }
             args[i] = new Argument(paramTypes[i], selector);
         }
         arguments = Collections.unmodifiableList(Arrays.asList(args));
         this.selectorCount = selectorCount;
     }
 
     @Override
     public String toString() {
         StringBuilder buf = new StringBuilder();
         buf.append('<').append(returnType.getName()).append(' ').append(name).append('(');
         boolean first = true;
         for ( Argument arg : arguments ) {
             if ( first ) {
                 first = false;
             }
             else {
                 buf.append(',');
             }
             if ( arg.isSelector() ) {
                 buf.append("@Selector ");
             }
             buf.append(arg.getType().getName());
         }
        buf.append(')');
         return buf.toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if ( this == o ) {
             return true;
         }
         if ( o == null || getClass() != o.getClass() ) {
             return false;
         }
         MethodSignature that = (MethodSignature)o;
         if ( !arguments.equals(that.arguments) ) {
             return false;
         }
         if ( !name.equals(that.name) ) {
             return false;
         }
         return returnType.equals(that.returnType);
     }
 
     @Override
     public int hashCode() {
         int result = returnType.hashCode();
         result = 31 * result + name.hashCode();
         result = 31 * result + arguments.hashCode();
         return result;
     }
 
     public Class<?> getReturnType() {
         return returnType;
     }
 
     public String getName() {
         return name;
     }
 
     public List<Argument> getArguments() {
         return arguments;
     }
 
     public int getSelectorCount() {
         return selectorCount;
     }
 
     public int getParameterCount() {
         return arguments.size() - selectorCount;
     }
 
     public ResourceIndicator getIndicator() {
         if ( indicator == null ) {
             Class<?>[] paramTypes = new Class<?>[arguments.size()];
             for ( int i = 0; i < paramTypes.length; i++ ) {
                 paramTypes[i] = arguments.get(i).getType();
             }
             indicator = new ResourceIndicator(name, paramTypes);
         }
         return indicator;
     }
 
     public void checkCompatibility(MethodSignature other) {
         if ( !returnType.equals(other.returnType) ) {
             throw new I18NException("Incompatible method return types: " + this + " <> " + other);
         }
         if ( !arguments.equals(other.arguments) ) {
             throw new I18NException("Incompatible method parameters: " + this + " <> " + other);
         }
     }
 
     public static final class Argument {
         private final Class<?> type;
         private final boolean selector;
         private Argument(Class<?> type, boolean selector) {
             this.type = type;
             this.selector = selector;
         }
         @Override
         public String toString() {
             return (selector ? "@Selector " : "" + type);
         }
         @Override
         public boolean equals(Object o) {
             if ( this == o ) {
                 return true;
             }
             if ( o == null || getClass() != o.getClass() ) {
                 return false;
             }
             Argument argument = (Argument)o;
             if ( selector != argument.selector ) {
                 return false;
             }
             return type.equals(argument.type);
         }
         @Override
         public int hashCode() {
             int result = type.hashCode();
             result = 31 * result + (selector ? 1 : 0);
             return result;
         }
         public Class<?> getType() {
             return type;
         }
         public boolean isSelector() {
             return selector;
         }
     }
 
 }
