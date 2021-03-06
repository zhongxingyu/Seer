 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.jackrabbit.oak.plugins.observation;
 
 import static com.google.common.collect.Lists.newLinkedList;
 import static com.google.common.collect.Sets.newLinkedHashSet;
 import static org.apache.jackrabbit.oak.api.Type.NAMES;
 import static org.apache.jackrabbit.oak.api.Type.STRING;
 import static org.apache.jackrabbit.oak.core.AbstractTree.OAK_CHILD_ORDER;
 import static org.apache.jackrabbit.oak.plugins.memory.EmptyNodeState.MISSING_NODE;
 import static org.apache.jackrabbit.oak.spi.state.MoveDetector.SOURCE_PATH;
 
 import java.util.LinkedList;
 import java.util.Set;
 
 import javax.annotation.Nonnull;
 import javax.swing.event.ChangeListener;
 
 import org.apache.jackrabbit.oak.api.PropertyState;
 import org.apache.jackrabbit.oak.plugins.observation.handler.ChangeHandler;
 import org.apache.jackrabbit.oak.spi.state.NodeState;
 import org.apache.jackrabbit.oak.spi.state.NodeStateDiff;
 
 /**
  * Continuation-based content diff implementation that generates
  * {@link ChangeHandler} callbacks by recursing down a content diff
  * in a way that guarantees that only a finite number of callbacks
  * will be made during a {@link #generate()} method call, regardless
  * of how large or complex the content diff is.
  * <p>
  * A simple usage pattern would look like this:
  * <pre>
  * EventGenerator generator = new EventGenerator(before, after, handler);
  * while (generator.isDone()) {
  *     generator.generate();
  * }
  * </pre>
  */
 public class EventGenerator {
 
     /**
      * Maximum number of content changes to process during the
      * execution of a single diff continuation.
      */
     private static final int MAX_CHANGES_PER_CONTINUATION = 10000;
 
     /**
      * Maximum number of continuations queued for future processing.
      * Once this limit has been reached, we'll start pushing for the
      * processing of property-only diffs, which will automatically
      * help reduce the backlog.
      */
     private static final int MAX_QUEUED_CONTINUATIONS = 1000;
 
     private static final String[] STRING_ARRAY = new String[0];
 
     private final LinkedList<Continuation> continuations = newLinkedList();
 
     /**
      * Creates a new generator instance for processing the given changes.
      */
     public EventGenerator(
             @Nonnull NodeState before, @Nonnull NodeState after,
             @Nonnull ChangeHandler handler) {
         continuations.add(new Continuation(handler, before, after, 0));
     }
 
     /**
      * Checks whether there are no more content changes to be processed.
      */
     public boolean isDone() {
         return continuations.isEmpty();
     }
 
     /**
      * Generates a finite number of {@link ChangeListener} callbacks based
      * on the content changes that have yet to be processed. Further processing
      * (even if no callbacks were made) may be postponed to a future
      * {@link #generate()} call, until the {@link #isDone()} method finally
      * return {@code true}.
      */
     public void generate() {
         if (!continuations.isEmpty()) {
             Continuation c = continuations.removeFirst();
             c.after.compareAgainstBaseState(c.before, c);
         }
     }
 
     private class Continuation implements NodeStateDiff {
 
         /**
          * Filtered handler of detected content changes.
          */
         private final ChangeHandler handler;
 
         /**
          * Before state, possibly non-existent.
          */
         private final NodeState before;
 
         /**
          * After state, possibly non-existent.
          */
         private final NodeState after;
 
         /**
          * Number of initial changes to skip.
          */
         private final int skip;
 
         /**
          * Number of changes seen so far.
          */
         private int counter = 0;
 
         private Continuation(
                 ChangeHandler handler, NodeState before, NodeState after,
                 int skip) {
             this.handler = handler;
             this.before = before;
             this.after = after;
             this.skip = skip;
         }
 
         //-------------------------------------------------< NodeStateDiff >--
 
         @Override
         public boolean propertyAdded(PropertyState after) {
             if (beforeEvent()) {
                 handler.propertyAdded(after);
                 return afterEvent();
             } else {
                 return true;
             }
         }
 
         @Override
         public boolean propertyChanged(
                 PropertyState before, PropertyState after) {
             if (beforeEvent()) {
                 // check for reordering of child nodes
                 if (OAK_CHILD_ORDER.equals(before.getName())) {
                     Set<String> beforeSet =
                             newLinkedHashSet(before.getValue(NAMES));
                    Set<String> afterSet =
                            newLinkedHashSet(after.getValue(NAMES));
                     afterSet.retainAll(beforeSet);
                     beforeSet.retainAll(afterSet);
                     String[] beforeNames = beforeSet.toArray(STRING_ARRAY);
                     String[] afterNames = afterSet.toArray(STRING_ARRAY);
 
                     // Selection sort beforeNames into afterNames,
                     // recording the swaps as we go
                     for (int a = 0; a < afterNames.length; a++) {
                         String name = afterNames[a];
                         for (int b = a + 1; b < beforeNames.length; b++) {
                             if (name.equals(beforeNames[b])) {
                                 beforeNames[b] = beforeNames[a];
                                 beforeNames[a] = name;
                                 handler.nodeReordered(
                                         beforeNames[a + 1], name,
                                         this.after.getChildNode(name));
                             }
                         }
                     }
                 }
 
                 handler.propertyChanged(before, after);
                 return afterEvent();
             } else {
                 return true;
             }
         }
 
         @Override
         public boolean propertyDeleted(PropertyState before) {
             if (beforeEvent()) {
                 handler.propertyDeleted(before);
                 return afterEvent();
             } else {
                 return true;
             }
         }
 
         @Override
         public boolean childNodeAdded(String name, NodeState after) {
             if (beforeEvent()) {
                 PropertyState sourceProperty = after.getProperty(SOURCE_PATH);
                 if (sourceProperty != null) {
                     String sourcePath = sourceProperty.getValue(STRING);
                     handler.nodeMoved(sourcePath, name, after);
                 }
 
                 handler.nodeAdded(name, after);
                 return addChildDiff(name, MISSING_NODE, after);
             } else {
                 return true;
             }
         }
 
         @Override
         public boolean childNodeChanged(
                 String name, NodeState before, NodeState after) {
             if (beforeEvent()) {
                 return addChildDiff(name, before, after);
             } else {
                 return true;
             }
         }
 
         @Override
         public boolean childNodeDeleted(String name, NodeState before) {
             if (beforeEvent()) {
                 handler.nodeDeleted(name, before);
                 return addChildDiff(name, before, MISSING_NODE);
             } else {
                 return true;
             }
         }
 
         //-------------------------------------------------------< private >--
 
         /**
          * Schedules a continuation for processing changes within the given
          * child node, if changes within that subtree should be processed.
          */
         private boolean addChildDiff(
                 String name, NodeState before, NodeState after) {
             ChangeHandler h = handler.getChildHandler(name, before, after);
             if (h != null) {
                 continuations.add(new Continuation(h, before, after, 0));
             }
 
             if (continuations.size() > MAX_QUEUED_CONTINUATIONS) {
                 // Postpone further processing of the current continuation.
                 // Even though this increases the size of the queue beyond
                 // the limit, doing so ultimately forces property-only
                 // diffs to the beginning of the queue, and thus helps
                 // automatically clean up the backlog.
                 continuations.add(new Continuation(
                         handler, this.before, this.after, counter));
                 return false;
             } else {
                 return afterEvent();
             }
         }
 
         /**
          * Increases the event counter and checks whether the event should
          * be processed, i.e. whether the initial skip count has been reached.
          */
         private boolean beforeEvent() {
             return ++counter > skip;
         }
 
         /**
          * Checks whether enough events have already been processed in this
          * continuation. If that is the case, we postpone further processing
          * to a new continuation that will first skip all the initial events
          * we've already seen. Otherwise we let the current diff continue.
          */
         private boolean afterEvent() {
             if (counter >= skip + MAX_CHANGES_PER_CONTINUATION) {
                 continuations.add(
                         new Continuation(handler, before, after, counter));
                 return false;
             } else {
                 return true;
             }
         }
 
     }
 
 }
