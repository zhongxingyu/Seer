 // Copyright (C) 2011 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.synchronisation;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.grinder.communication.CommunicationException;
 import net.grinder.synchronisation.messages.BarrierIdentity;
 import net.grinder.util.ListenerSupport;
 
 
 /**
  * Common barrier group implementation.
  *
  * @author Philip Aston
  */
 public abstract class AbstractBarrierGroups implements BarrierGroups  {
 
   // Guarded by self.
   private final Map<String, BarrierGroupImplementation> m_groups =
     new HashMap<String, BarrierGroupImplementation>();
 
   /**
    * {@inheritDoc}
    */
   public final BarrierGroup getGroup(String name) {
     synchronized (m_groups) {
       final BarrierGroup existing = m_groups.get(name);
 
       if (existing != null) {
         return existing;
       }
 
       final BarrierGroupImplementation newInstance = createBarrierGroup(name);
       m_groups.put(name, newInstance);
 
       return newInstance;
     }
   }
 
   /**
    * Calls {@link BarrierGroup#cancelAll()} on all of our barrier groups.
    *
    * @throws CommunicationException If a network problem occurred.
    */
   public final void cancelAll() throws CommunicationException {
     synchronized (m_groups) {
      final Set<BarrierGroupImplementation> clonedGroups =
        new HashSet<BarrierGroupImplementation>(m_groups.values());

      for (BarrierGroupImplementation group : clonedGroups) {
         group.cancelAll();
       }
     }
   }
 
   /**
    * Provide subclasses a way to access to an existing group without creating
    * one if it doesn't exist.
    *
    * @param name
    *          Barrier name.
    * @return The barrier group, or {@code null} if none exists.
    */
   protected final BarrierGroupImplementation getExistingGroup(String name) {
     synchronized (m_groups) {
       return m_groups.get(name);
     }
   }
 
   private void removeBarrierGroup(BarrierGroup barrierGroup) {
     synchronized (m_groups) {
       m_groups.remove(barrierGroup.getName());
     }
   }
 
   /**
    * Factory method through which subclasses provide an appropriate barrier
    * group implementation.
    *
    * @param name
    *          Barrier name.
    * @return A new barrier group instance.
    */
   protected abstract BarrierGroupImplementation createBarrierGroup(String name);
 
   /**
    * {@inheritDoc}
    */
   @Override public String toString() {
     synchronized (m_groups) {
       return getClass().getSimpleName() + "[" + m_groups + "]";
     }
   }
 
   /**
    * Basic {@link BarrierGroup} implementation.
    */
   protected class BarrierGroupImplementation implements BarrierGroup {
 
     private final String m_name;
 
     private final ListenerSupport<Listener> m_listeners =
       new ListenerSupport<Listener>();
 
 
     // Guarded by this. Negative <=> the group is invalid.
     private long m_barriers = 0;
 
     // Guarded by this.
     private final Set<BarrierIdentity> m_waiters =
       new HashSet<BarrierIdentity>();
 
     /**
      * Constructor.
      *
      * @param name Barrier group name.
      */
     public BarrierGroupImplementation(String name) {
       m_name = name;
     }
 
     private void checkValid() {
       if (m_barriers < 0) {
         throw new IllegalStateException("BarrierGroup is invalid");
       }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getName() {
       return m_name;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addListener(Listener listener) {
       m_listeners.add(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeListener(Listener listener) {
       m_listeners.remove(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addBarrier() throws CommunicationException {
       synchronized (this) {
         checkValid();
 
         ++m_barriers;
       }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeBarriers(long n) throws CommunicationException {
       synchronized (this) {
         checkValid();
 
         if (n > m_barriers - m_waiters.size()) {
           throw new IllegalStateException(
             "Can't remove " + n + " barriers from " +
             m_barriers + " barriers, " + m_waiters.size() + " waiters");
         }
 
         m_barriers -= n;
 
         if (m_barriers == 0) {
           removeBarrierGroup(this);
           m_barriers = -1;
         }
       }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addWaiter(BarrierIdentity barrierIdentity)
       throws CommunicationException {
 
       synchronized (this) {
         checkValid();
 
         if (m_barriers == 0) {
           throw new IllegalStateException("Can't add waiter, no barriers");
         }
 
         assert m_waiters.size() < m_barriers;
 
         m_waiters.add(barrierIdentity);
       }
     }
 
     /**
      * {@inheritDoc}
      */
     public void cancelWaiter(BarrierIdentity barrierIdentity)
       throws CommunicationException {
 
       synchronized (this) {
         m_waiters.remove(barrierIdentity);
       }
     }
 
     /**
      * {@inheritDoc}
      */
     public void cancelAll() throws CommunicationException {
       synchronized (this) {
         final Set<BarrierIdentity> clonedWaiters =
           new HashSet<BarrierIdentity>(m_waiters);
 
         for (BarrierIdentity barrierIdentity : clonedWaiters) {
           cancelWaiter(barrierIdentity);
         }
 
         removeBarriers(m_barriers);
       }
     }
 
     /**
      * Call the {@link Listener#awaken()} method for all of the listeners.
      */
     protected final void fireAwaken() {
       m_listeners.apply(new ListenerSupport.Informer<Listener>() {
         public void inform(Listener listener) { listener.awaken(); }
       });
     }
 
     /**
      * Check whether the barrier group should awaken.
      *
      * @return {@code true} if and only if the group should awaken.
      */
     protected final boolean checkConditionLocal() {
       synchronized (this) {
         if (m_barriers > 0 && m_barriers == m_waiters.size()) {
 
           m_waiters.clear();
 
           // The caller will notify the listeners after releasing the lock to
           // minimise the length of time it is held. Otherwise the distributed
           // nature of the communication might delay subsequent operations
           // significantly.
           //
           // This does not cause a race from the perspective of an individual
           // waiting thread, since it cannot proceed until its barrier is woken
           // or cancelled, and once cancelled a barrier cannot be re-used.
             return true;
         }
 
         return false;
       }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override public String toString() {
       synchronized (this) {
         return getClass().getSimpleName() +
           "[" + m_barriers + ", " +  m_waiters + "]";
       }
     }
   }
 }
