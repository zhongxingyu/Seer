 /* 
  * Copyright © 2011 Karl J. Ots <kjots@kjots.org>
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
 package org.kjots.lib.gwt.event.message.shared;
 
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HasHandlers;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * Message Event.
  * <p>
  * Created: 5th December 2011.
  *
  * @param <T> The type of the message.
  * @author <a href="mailto:kjots@kjots.org">Karl J. Ots &lt;kjots@kjots.org&gt;</a>
  * @since 1.0
  */
 public class MessageEvent<T> extends GwtEvent<MessageHandler<T>> {
   /** The message type. */
   private final MessageType<T> messageType;
   
   /** The message. */
   private final T message;
   
   /**
    * Fire a message event for a message of the given type from the given
    * source.
    *
    * @param <T> The type of the message.
    * @param source The source of the message event.
    * @param messageType The message type.
    * @param message The message.
    */
   public static <T> void fire(HasHandlers source, MessageType<T> messageType, T message) {
     source.fireEvent(new MessageEvent<T>(messageType, message));
   }
   
   /**
    * Fire a message event for a message of the given type on the given event
    * bus.
    *
    * @param <T> The type of the message.
   * @param source The event bus.
    * @param messageType The message type.
    * @param message The message.
    */
   public static <T> void fire(EventBus eventBus, MessageType<T> messageType, T message) {
     eventBus.fireEvent(new MessageEvent<T>(messageType, message));
   }
   
   /**
    * Fire a message event for a message of the given type on the given event
    * bus from the given source.
    *
    * @param <T> The type of the message.
   * @param source The event bus.
    * @param messageType The message type.
    * @param message The message.
    * @param source The source.
    */
   public static <T> void fire(EventBus eventBus, MessageType<T> messageType, T message, Object source) {
     eventBus.fireEventFromSource(new MessageEvent<T>(messageType, message), source);
   }
   
   /**
    * Retrieve the type of the event.
    *
    * @return The type of the event.
    */
   @Override
   public Type<MessageHandler<T>> getAssociatedType() {
     return this.messageType;
   }
   
   /**
    * Retrieve the message type.
    *
    * @return The message type.
    */
   public MessageType<T> getMessageType() {
     return this.messageType;
   }
 
   /**
    * Retrieve the message.
    *
    * @return The message.
    */
   public T getMessage() {
     return this.message;
   }
 
   /**
    * Create a string representation of this object.
    *
    * @return The string representation of this object.
    */
   @Override
   public String toString() {
     return "MessageEvent: messageType=" + this.messageType;
   }
 
   /**
    * Dispatch the event for the given handler.
    *
    * @param handler The handler.
    */
   @Override
   protected void dispatch(MessageHandler<T> handler) {
     handler.onMessage(this.messageType, this.message);
   }
   
   /**
    * Construct a new Message Event.
    * <p>
    * This constructor is declared <code>private</code> to prevent external
    * instantiation.
    *
    * @param messageType The message type.
    * @param message The message
    */
   private MessageEvent(MessageType<T> messageType, T message) {
     this.messageType = messageType;
     this.message = message;
   }
 }
