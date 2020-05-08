 /**
  * Copyright (c) 2004, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.sim.util;
 
 import avrora.sim.Simulator;
 
 /**
  * The <code>DeltaQueue</code> class implements an amortized constant time
  * delta-queue for processing of scheduled events. Events are put into the queue
  * that will fire at a given number of cycles in the future. An internal delta
  * list is maintained where each link in the list represents a set of triggers
  * to be fired some number of clock cycles after the previous link.
  * <p/>
  * Each delta between links is maintained to be non-zero. Thus, to insert a
  * trigger X cycles in the future, at most X nodes will be skipped over. Therefore
  * over X time steps, this cost is amortized to be constant.
  * <p/>
  * For each clock cycle, only the first node in the list must be checked, leading
  * to constant time work per clock cycle.
  * <p/>
  * This class allows the clock to be advanced multiple ticks at a time.
  * <p/>
  * Also, since this class is used heavily in the simulator, its performance is
  * important and maintains an internal cache of objects. Thus, it does not create
  * garbage over its execution and never uses more space than is required to store
  * the maximum encountered simultaneous events. It does not use standard libraries,
  * casts, virtual dispatch, etc.
  */
 public class DeltaQueue {
 
     /**
      * The <code>TriggerLink</code> class represents a link in the list of triggers
      * for a given <code>Link</code> in the delta queue chain.
      */
     static final class TriggerLink {
         Simulator.Event trigger;
         TriggerLink next;
 
         /**
          * The constructor for the <code>TriggerLink</code> class simply initializes
          * the internal references to the trigger and the next link in the chain
          * based on the parameters passed.
          * @param t a reference the trigger
          * @param n the next link in the chain
          */
         TriggerLink(Simulator.Event t, TriggerLink n) {
             trigger = t;
             next = n;
         }
 
     }
 
     /**
      * The <code>Link</code> class represents a link in the list of delta queue
      * items that are being stored. It contains a list of triggers that share
      * the same delta.
      */
     final class Link {
         TriggerLink triggers;
 
         Link next;
         long delta;
 
         Link(Simulator.Event t, long d) {
             triggers = newList(t, null);
             delta = d;
         }
 
         void add(Simulator.Event t) {
             triggers = newList(t, triggers);
         }
 
         void remove(Simulator.Event t) {
             TriggerLink prev = null;
             TriggerLink pos = triggers;
             while (pos != null) {
                 TriggerLink next = pos.next;
 
                 if (pos.trigger == t) {
                     if (prev == null)
                     // remove the whole thing.
                         triggers = pos.next;
                     else
                     // remove the "pos" link
                         prev.next = pos.next;
 
                     free(pos);
                 } else {
                     prev = pos;
                 }
                 pos = next;
             }
         }
 
         void fire() {
             for (TriggerLink pos = triggers; pos != null; pos = pos.next) {
                 pos.trigger.fire();
             }
         }
     }
 
     /**
      * The <code>head</code> field stores a reference to the head of the delta queue,
      * which represents the event that is nearest in the future.
      */
     protected Link head;
 
     /**
      * The <code>freeLinks</code> field stores a reference to any free links that
      * have become unused during the processing of events. A free list is used to
      * prevent garbage from accumulating.
      */
     protected Link freeLinks;
 
     /**
      * The <code>freeTriggerLinks</code> field stores a reference to any free trigger
      * links that have become unused during the processing of events. A free list
      * is used to prevent garbage from accumulating.
      */
     protected TriggerLink freeTriggerLinks;
 
     /**
      * The <code>count</code> field stores the total number of cycles that this
      * queue has been advanced, i.e. the sum of all <code>advance()</code> calls.
      */
     protected long count;
 
     /**
      * The <code>add</code> method adds a trigger to be executed in the future.
      *
      * @param t      the trigger to fire
      * @param cycles the number of clock cycles in the future
      */
     public void add(Simulator.Event t, long cycles) {
         // degenerate case, nothing in the queue.
         if (head == null) {
             head = newLink(t, cycles, null);
             return;
         }
 
         // search for first link that is "after" this cycle delta
         Link prev = null;
         Link pos = head;
         while (pos != null && cycles > pos.delta) {
             cycles -= pos.delta;
             prev = pos;
             pos = pos.next;
         }
 
         if (pos == null) {
             // end of the head
             addAfter(prev, t, cycles, null);
         } else if (cycles == pos.delta) {
             // exactly matched the delta of some other event
             pos.add(t);
         } else {
             // insert a new link in the chain
             addAfter(prev, t, cycles, pos);
         }
     }
 
     private void addAfter(Link prev, Simulator.Event t, long cycles, Link next) {
         if (prev != null)
             prev.next = newLink(t, cycles, next);
         else
             head = newLink(t, cycles, next);
     }
 
     /**
      * The <code>remove</code> method removes all occurrences of the specified
      * trigger within the delta queue.
      *
      * @param e the trigger to remove
      */
     public void remove(Simulator.Event e) {
         if (head == null) return;
 
         // search for first link that is "after" this cycle delta
         Link prev = null;
         Link pos = head;
         while (pos != null) {
             Link next = pos.next;
             pos.remove(e);
 
             if (pos.triggers == null) {
                 // the link became empty because of removing this trigger
                 if (prev == null)
                     head = pos.next;
                 else
                     prev.next = pos.next;
 

                // fixes up the delta of the next item in the queue
                if(pos.next != null) {
                    pos.next.delta += pos.delta;
                }

                 free(pos);
             } else {
                 prev = pos;
             }
             pos = next;
         }
     }
 
     /**
      * The <code>advance</code> method advances timesteps through the queue by the
      * specified number of clock cycles, processing any triggers.
      *
      * @param cycles the number of clock cycles to advance
      */
     public void advance(long cycles) {
         count += cycles;
 
         while (head != null && cycles >= 0) {
 
             Link pos = head;
             Link next = pos.next;
 
             long left = cycles - pos.delta;
             pos.delta = -left;
 
             // if haven't arrived yet, break
             if (pos.delta > 0) break;
 
             // chop off head
             head = next;
 
             // fire all events at head
             pos.fire();
 
             // free the head
             free(pos);
 
             // consume the cycles
             cycles = left;
         }
     }
 
     /**
      * The <code>getHeadDelta()</code> method gets the number of clock cycles until
      * the first event will fire.
      * @return the number of clock cycles until the first event will fire
      */
     public long getHeadDelta() {
         if (head != null) return head.delta;
         return -1;
     }
 
     /**
      * The <code>getCount()</code> gets the total cumulative count of all the
      * <code>advance()</code> calls on this delta queue.
      * @return the total number of cycles this queue has been advanced
      */
     public long getCount() {
         return count;
     }
 
     private void free(Link l) {
         l.next = freeLinks;
         freeLinks = l;
 
         freeTriggerLinks = l.triggers;
         l.triggers = null;
     }
 
     private void free(TriggerLink l) {
         l.trigger = null;
         l.next = freeTriggerLinks;
         freeTriggerLinks = l;
     }
 
     private Link newLink(Simulator.Event t, long cycles, Link next) {
         Link l;
         if (freeLinks == null)
         // if none in the free list, allocate one
             l = new Link(t, cycles);
         else {
             // grab one from the free list
             l = freeLinks;
             freeLinks = freeLinks.next;
             l.delta = cycles;
             l.add(t);
         }
 
         // adjust delta in the next link in the chain
         if (next != null) {
             next.delta -= cycles;
         }
 
         l.next = next;
         return l;
     }
 
     private TriggerLink newList(Simulator.Event t, TriggerLink next) {
         TriggerLink l;
 
         if (freeTriggerLinks == null) {
             // no free links, so allocate one
             l = new TriggerLink(t, next);
         } else {
             // grab the first link off the free chain
             l = freeTriggerLinks;
             freeTriggerLinks = freeTriggerLinks.next;
             l.next = next;
             l.trigger = t;
         }
 
         return l;
     }
 }
