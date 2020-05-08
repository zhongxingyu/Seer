 package com.github.overengineer.gunmetal.inject;
 
import com.github.overengineer.gunmetal.*;
 import com.github.overengineer.gunmetal.key.Dependency;
 import com.github.overengineer.gunmetal.util.FieldRef;
 
 /**
  * @author rees.byars
  */
 public class DefaultFieldInjector<T> implements FieldInjector<T> {
 
     private final FieldRef fieldRef;
     private final Dependency<?> dependency;
     private transient ComponentStrategy strategy;
 
     DefaultFieldInjector(FieldRef fieldRef, Dependency<?> dependency) {
         this.fieldRef = fieldRef;
         this.dependency = dependency;
     }
 
     @Override
     public void inject(T component, Provider provider) {
         try {
             if (strategy == null) {
                 synchronized (this) {
                     if (strategy == null && fieldRef.getField().getType().isAssignableFrom(fieldRef.getField().getDeclaringClass())) {
                         strategy = provider.getStrategy(dependency, new SelectionAdvisor() {
                             @Override
                             public boolean validSelection(ComponentStrategy<?> candidateStrategy) {
                                 return candidateStrategy.getComponentType() != fieldRef.getField().getDeclaringClass();
                             }
                         });
                    } else if (strategy == null){
                         strategy = provider.getStrategy(dependency, SelectionAdvisor.NONE);
                     }
                 }
             }
             fieldRef.getField().set(component, strategy.get(provider));
         } catch (CircularReferenceException e) {
             e.setFieldRef(fieldRef);
             throw e;
         } catch (Exception e) {
             throw new InjectionException("Could not inject field [" + fieldRef.getField().getName() + "] on component of type [" + component.getClass().getName() + "].", e);
         }
     }
 
 }
