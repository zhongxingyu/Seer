 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.workqueue;
 
 import java.util.Collection;
 import java.util.Queue;
 import java.util.Set;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ForwardingQueue;
 
 /**
  * Decorator to add {@link Set} semantic to {@link Queue}s.
  * <p>
  *   The later added element wins. Older elements will be removed.
  * </p>
  *
  * @since 1.0
  * @author Willi Schoenborn
  * @param <E> generic element type
  */
 public final class SetQueue<E> extends ForwardingQueue<E> {
 
     private final Queue<E> queue;
     
     public SetQueue(Queue<E> queue) {
         this.queue = Preconditions.checkNotNull(queue, "Queue");
     }
 
     @Override
     protected Queue<E> delegate() {
         return queue;
     }
 
     @Override
     public boolean offer(E element) {
        // should be atomic
         remove(element);
         return super.offer(element);
     }
 
     @Override
     public boolean add(E element) {
        // should be atomic
         remove(element);
         return super.add(element);
     }
 
     @Override
     public boolean addAll(Collection<? extends E> collection) {
        // should be atomic
         removeAll(collection);
         return super.addAll(collection);
     }
     
 }
