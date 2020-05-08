 /*
  * Copyright (c) 2009, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.swing.on.steroids.presenters;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.swing.on.steroids.messagebus.Subscribtion;
 import org.swing.on.steroids.views.handlers.HandlerRegistration;
 import org.swing.on.steroids.views.View;
 
 /**
  * Base class to implement Presenters that provide common code, mostly Handler & Subscription related.
  *
  * @param <V> View type required by this Presenter.
  */
 public abstract class BasePresenter<V extends View>
         implements Presenter
 {
 
     private boolean bound = false;
     private Set<HandlerRegistration> viewHandlerRegistrations = new HashSet<HandlerRegistration>();
     private Set<Subscribtion> messageSubscribtions = new HashSet<Subscribtion>();
     protected final V view;
 
     protected BasePresenter( V view )
     {
         this.view = view;
     }
 
     @Override
     public final V view()
     {
         return view;
     }
 
     @Override
    public synchronized final void bind()
     {
         if ( !bound ) {
             onBind();
             bound = true;
         }
     }
 
     @Override
    public synchronized final void unbind()
     {
         if ( bound ) {
             onUnbind();
             for ( HandlerRegistration eachViewRegistration : viewHandlerRegistrations ) {
                 eachViewRegistration.removeHandler();
             }
             for ( Subscribtion subscribtion : messageSubscribtions ) {
                 subscribtion.unsubscribe();
             }
             bound = false;
         }
     }
 
     /**
      * Called when the Presenter has to be bound to its views and other participants. Subtypes must implement this.
      */
     protected abstract void onBind();
 
     /**
      * Called when the Presenter has to be unbound from its views and other participants. Subtypes must implement this.
      */
     protected abstract void onUnbind();
 
     /**
      * HandlerRegistration recorded with this method will automatically be removed on unbind.
      * @param viewRegistration HandlerRegistration to be recorded.
      */
     protected final void recordViewRegistration( HandlerRegistration viewRegistration )
     {
         viewHandlerRegistrations.add( viewRegistration );
     }
 
     /**
      * Subscribtion recorded with this method will automatically be unsubscribed on unbind.
      * @param subscribtion Subscribtion
      */
     protected final void recordMessageSubscribtion( Subscribtion subscribtion )
     {
         messageSubscribtions.add( subscribtion );
     }
 
 }
