 package org.nnsoft.commons.meiyo.classvisitor;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 public abstract class AbstractVisitorConfiguration implements VisitorConfiguration, AnnotationHandlerBinder {
 
     private AnnotationHandlerBinder wrapped;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final void configure(final AnnotationHandlerBinder binder) {
         this.wrapped = binder;
        this.configure();
     }
 
     public abstract void configure();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public AnnotatedHandlerBuilder<Class> handleType() {
         return this.wrapped.handleType();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public AnnotatedHandlerBuilder<Constructor> handleConstructor() {
         return this.wrapped.handleConstructor();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public AnnotatedHandlerBuilder<Field> handleField() {
         return this.wrapped.handleField();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public AnnotatedHandlerBuilder<Method> handleMethod() {
         return this.wrapped.handleMethod();
     }
 
 }
