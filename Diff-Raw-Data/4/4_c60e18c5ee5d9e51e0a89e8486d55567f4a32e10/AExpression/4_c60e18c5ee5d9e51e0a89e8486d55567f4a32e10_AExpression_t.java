 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eagerlogic.cubee.client.properties;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author dipacs
  */
 public abstract class AExpression<T> implements IProperty<T>, IObservable {
     
     private LinkedList<IProperty> bindingSources = new LinkedList<IProperty>();
     private IChangeListener bindingListener = new IChangeListener() {
 
         @Override
         public void onChanged(Object sender) {
             invalidate();
         }
     };
     private LinkedList<IChangeListener> changeListeners = new LinkedList<IChangeListener>();
     
     public abstract T calculate();
     private boolean valid = false;
     private T value;
     
     public T get() {
         if (!valid) {
             this.value = calculate();
             this.valid = true;
         } 
         
         return this.value;
     }
     
     @Override
     public void addChangeListener(IChangeListener listener) {
         if (listener == null) {
             throw new NullPointerException("The listener parameter can not be null.");
         }
         
         if (hasChangeListener(listener)) {
             return;
         }
         
         changeListeners.add(listener);
         invalidate();
     }
 
     @Override
     public void removeChangeListener(IChangeListener listener) {
         Iterator<IChangeListener> iterator = changeListeners.iterator();
         while (iterator.hasNext()) {
             if (iterator.next() == listener) {
                 iterator.remove();
                 return;
             }
         }
         invalidate();
     }
 
     @Override
     public boolean hasChangeListener(IChangeListener listener) {
         for (IChangeListener l : changeListeners) {
             if (l == listener) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public Object getObjectValue() {
         return this.get();
     }
     
     protected void bind(IProperty property) {
         this.bindingSources.add(property);
         property.addChangeListener(bindingListener);
         this.invalidate();
     }
     
     @Override
     public final void invalidate() {
         this.valid = false;
        for (IChangeListener listener : changeListeners) {
           listener.onChanged(this);
         }
     }
     
 }
