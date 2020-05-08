 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.injection;
 
 import cx.ath.mancel01.dependencyshot.api.InjectionPoint;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Member;
 import java.lang.reflect.Type;
 import java.util.Set;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class InjectionPointImpl implements InjectionPoint {
 
     private Type type;
 
     private Set<Annotation> annotations;
 
     private Member member;
 
     private Class injectedClass;
 
     public InjectionPointImpl(Type type, Set<Annotation> annotations, Member member, Class injectedClass) {
         this.type = type;
         this.annotations = annotations;
         this.member = member;
         this.injectedClass = injectedClass;
     }
 
     @Override
     public final Type getType() {
         return this.type;
     }
 
     @Override
     public final Set<Annotation> getAnnotations() {
         return this.annotations;
     }
 
     @Override
     public final Member getMember() {
         return this.member;
     }
 
     @Override
     public final Class getBeanClass() {
         return this.member.getDeclaringClass();
     }
 
    public final Class getInjectedClass() {
         return injectedClass;
     }
 
    public final void setInjectedClass(Class injectedClass) {
         this.injectedClass = injectedClass;
     }
 }
