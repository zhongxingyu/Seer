 /*
  *  Copyright 2009-2010 Mathieu ANCELIN
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
 package cx.ath.mancel01.dependencyshot.graph;
 
 import cx.ath.mancel01.dependencyshot.injection.util.InstanceProvider;
 import cx.ath.mancel01.dependencyshot.api.DSBinder;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.injection.fluent.FluentBinder;
 import cx.ath.mancel01.dependencyshot.injection.fluent.QualifiedBinding;
 import cx.ath.mancel01.dependencyshot.injection.fluent.StagingBinding;
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.inject.Provider;
 
 /**
  * A binder is a configuration class containing bindings
  * between interface classes and their implementations.
  * At least one binder with one binding is necessary to
  * inject dependencies in your code.
  * To use a binder, define your own binder class extending this one
  * and define your own configureBindings method with your personnal
  * bindings.
  *
  * The fluent api of a binder can be used this way :
  * 
  * bind() -> to()
  *	      -> named()
  *	      -> annotatedWith()
  *	      -> providedBy()
  *	      -> toInstance()
  *	      -> onStage()
  *
  * to() -> onStage()
  *
  * named() -> to()
  *         -> providedBy()
  *         -> toInstance()
  *
  * annotatedWith() -> to()
  *                 -> providedBy()
  *	               -> toInstance()
  *
  * providedBy() -> onStage()
  *
  * toInstance() -> onStage()
  *
  * onStage()
  * 
  * @author Mathieu ANCELIN
  */
 public abstract class Binder implements DSBinder,
                                         FluentBinder,
                                         QualifiedBinding,
                                         StagingBinding {
 
     /**
      * Context for named injections.
      */
     private Map<Binding<?>, Binding<?>> bindings = new HashMap<Binding<?>, Binding<?>>();
     /**
      * The injector that manage the binder instance.
      */
     private DSInjector binderInjector;
     private Class from = null;
     private Class to = null;
     private String named = null;
     private Class<? extends Annotation> annotation = null;
     private Provider provider = null;
     private Stage stage = null;
 
     private List<Object> injectLater = new ArrayList<Object>();
 
     /**
      * Constructor.
      */
     public Binder() {
     }
 
     /**
      * Abstract method to configure the binder with bindings.
      */
     @Override
     public abstract void configureBindings();
 
     /**
      * Add a binding to current bindings.
      *
      * @param <T> type
      * @param binding a binding to add.
      */
     private <T> void addBindingToBinder(Binding<T> binding) {
         if (bindings.containsKey(binding)) {
             return;
         }
         Binding<?> old = bindings.put(binding, binding);
         if (old != null) {
             throw new IllegalArgumentException(binding + " overwrites " + old);
         }
     }
 
     /**
      * Configure the last binding present in the binder (because of the fluent API).
      */
     @Override
     public final void configureLastBinding() {
         addBindingToBinder(
                 new Binding(this.annotation, this.named,
                 this.from, this.to, this.provider, this.stage));
         if (binderInjector.getStage() != null) {
             if (binderInjector.getStage().equals(this.stage)) {
                 addBindingToBinder(
                         new Binding(this.annotation, this.named,
                         this.from, this.to, this.provider, this.stage));
             }
             if (this.stage == null) {
                 addBindingToBinder(
                         new Binding(this.annotation, this.named,
                         this.from, this.to, this.provider, this.stage));
             }
         } else {
             addBindingToBinder(
                     new Binding(this.annotation, this.named,
                     this.from, this.to, this.provider, this.stage));
         }
         this.from = null;
         this.to = null;
         this.named = null;
         this.annotation = null;
         this.provider = null;
         this.stage = null;
         injectLateObjects();
     }
 
     private void injectLateObjects() {
         for (Object o : injectLater)  {
            o = binderInjector.injectInstance(o);
         }
     }
 
     /**
      * Bind a class in the injector.
      *
      * @param <T> type.
      * @param from the class to bind.
      * @return the actual binder.
      */
     public final <T> FluentBinder bind(Class<T> from) {
         if (binderInjector.getStage() != null) {
             if (binderInjector.getStage().equals(this.stage)) {
                 addBindingToBinder(
                         new Binding<T>(this.annotation, this.named,
                         this.from, this.to, this.provider, this.stage));
             }
             if (this.stage == null) {
                 addBindingToBinder(
                         new Binding<T>(this.annotation, this.named,
                         this.from, this.to, this.provider, this.stage));
             }
         } else {
             addBindingToBinder(
                     new Binding<T>(this.annotation, this.named,
                     this.from, this.to, this.provider, this.stage));
         }
         this.from = from;
         this.to = from;
         this.named = null;
         this.annotation = null;
         this.provider = null;
         this.stage = null;
         return this;
     }
 
     /**
      * The target class for a binding.
      *
      * @param <T> type.
      * @param to the targeted class for the binding.
      * @return the actual binder.
      */
     @Override
     public final <T> StagingBinding to(Class<? extends T> to) {
         this.to = to;
         return this;
     }
 
     /**
      * Specify a name qualifier for a binding.
      *
      * @param <T> type.
      * @param named the name for the qualifier.
      * @return the actual binder.
      */
     @Override
     public final <T> QualifiedBinding named(String named) {
         this.named = named;
         return this;
     }
 
     /**
      * Specify a qualfier for a binding.
      *
      * @param <T> type.
      * @param annotation the qualifier of the binding.
      * @return the actual binder.
      */
     @Override
     public final <T> QualifiedBinding annotatedWith(Class<? extends Annotation> annotation) {
         this.annotation = annotation;
         return this;
     }
 
     /**
      * Specify a provider for the actual binding.
      *
      * @param <T> type.
      * @param provider the provider for the binding.
      * @return the actual binder.
      */
     @Override
     public final <T> StagingBinding providedBy(Provider<T> provider) {
         this.provider = provider;
         return this;
     }
 
     /**
      * Specify a specific instance to bind with.
      *
      * @param <T> type.
      * @param instance specify the instance.
      * @return the actual binder.
      */
     @Override
     public final <T> StagingBinding toInstance(Object instance) {
         injectLater.add(instance);
         this.provider = new InstanceProvider(instance);
         return this;
     }
 
     /**
      * Specify the stage of the binding.
      *
      * @param stage the actual stage.
      */
     @Override
     public final void onStage(Stage stage) {
         this.stage = stage;
     }
 
     /**
      * Allow user to import multiple exisiting binder in one main binder.
      *
      * @param binders binders instance to import.
      */
     public final void importBindingsFrom(Binder... binders) {
         for(Binder binder : binders) {
             binder.setInjector(binderInjector);
             binder.configureBindings();
             binder.configureLastBinding();
             for (Binding binding : binder.getBindings().values()) {
                 this.addBindingToBinder(binding);
             }
         }
     }
 
     /**
      * @return actual bindings in this binder.
      */
     public final Map<Binding<?>, Binding<?>> getBindings() {
         return bindings;
     }
 
     /**
      * @return is the actual binder empty.
      */
     @Override
     public final boolean isEmpty() {
         return bindings.isEmpty();
     }
 
     /**
      * @param injector set the value of the actual injector.
      */
     @Override
     public final void setInjector(DSInjector injector) {
         binderInjector = injector;
     }
 
     /**
      * @return the actual injector.
      */
     public final DSInjector injector() {
         return binderInjector; 
     }
 }
