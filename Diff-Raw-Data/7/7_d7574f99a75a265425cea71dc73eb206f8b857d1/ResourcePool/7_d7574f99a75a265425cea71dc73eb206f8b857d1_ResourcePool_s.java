// Copyright (C) 2003, 2004, 2005 Philip Aston
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
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.communication;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import net.grinder.util.ListenerSupport;
 import net.grinder.util.thread.UncheckedInterruptedException;
 
 
 /**
  * Class that manages a pool of resources.
  *
  * <p>Each resource in the pool is wrapped in a wrapper that keeps
  * track of whether it is currently in use. Clients access resources
  * through {@link Reservation}s.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class ResourcePool {
   private static final int PURGE_FREQUENCY = 1000;
 
   private final Object m_reservableListMutex = new Object();
   private final Object m_reservableMutex = new Object();
   private List m_reservables = new ArrayList();
   private int m_lastReservable = 0;
   private int m_nextPurge = 0;
 
   private final ListenerSupport m_listeners = new ListenerSupport();
 
   /**
    * Constructor.
    */
   public ResourcePool() {
     m_reservables.add(new Sentinel());
   }
 
   /**
    * Adds a resource to the pool.
    *
    * @param resource The resource to add.
    * @return Allows the client to notify the resource pool if the
    * resource has been closed.
    */
   public Closeable add(final Resource resource) {
     final ResourceWrapper resourceWrapper = new ResourceWrapper(resource);
 
     synchronized (m_reservableListMutex) {
       m_reservables.add(resourceWrapper);
     }
 
     m_listeners.apply(
       new ListenerSupport.Informer() {
         public void inform(Object listener) {
           ((Listener)listener).resourceAdded(resource);
         }
       });
 
     return resourceWrapper;
   }
 
   /**
    * Returns a resource, reserved for exclusive use by the caller.
    *
    * <p>Resources are handed out to callers in order. A Sentinel is
    * returned once every cycle; if no resources are free the Sentinel
    * is always returned.</p>
    *
    * @return The resource. It is up to the caller to free or close the
    * resource.
    */
   public Reservation reserveNext() {
     synchronized (m_reservableListMutex) {
       purgeZombieResources();
 
       while (true) {
         if (++m_lastReservable >= m_reservables.size()) {
           m_lastReservable = 0;
         }
 
         final Reservable reservable =
           (Reservable)m_reservables.get(m_lastReservable);
 
         if (reservable.reserve()) {
           return reservable;
         }
       }
     }
   }
 
   /**
    * Returns a list of all the current resources. Blocks until all
    * Reservations can be reserved. The Sentinel is not included in the
    * list.
    *
    * @return The resources. It is up to the caller to free or close
    * each resource.
    */
   public List reserveAll() {
 
     final List result;
     final List reserveList;
 
     synchronized (m_reservableListMutex) {
       purgeZombieResources();
 
       result = new ArrayList(m_reservables.size());
       reserveList = new ArrayList(m_reservables);
     }
 
     while (reserveList.size() > 0) {
       // Iterate backwards so remove is cheap.
       final ListIterator iterator =
         reserveList.listIterator(reserveList.size());
 
       while (iterator.hasPrevious()) {
         final Reservable reservable = (Reservable)iterator.previous();
 
         if (reservable.isSentinel()) {
           iterator.remove();
         }
         else if (reservable.reserve()) {
           result.add(reservable);
           iterator.remove();
         }
         else if (reservable.isClosed()) {
           iterator.remove();
         }
       }
 
       if (reserveList.size() > 0) {
         // Block until more resources are freed.
         synchronized (m_reservableMutex) {
           try {
            m_reservableMutex.wait();
           }
           catch (InterruptedException e) {
             throw new UncheckedInterruptedException(e);
           }
         }
       }
     }
 
     return result;
   }
 
   /**
    * Close all the resources.
    */
   public void close() {
     synchronized (m_reservableListMutex) {
       final Iterator iterator = m_reservables.iterator();
 
       while (iterator.hasNext()) {
         final Reservable reservable = (Reservable)iterator.next();
         reservable.close();
       }
     }
   }
 
   /**
    * Count the active resources.
    *
    * @return The number of active resources.
    */
   public int countActive() {
     int result = 0;
 
     synchronized (m_reservableListMutex) {
       final Iterator iterator = m_reservables.iterator();
 
       while (iterator.hasNext()) {
         final Reservable reservable = (Reservable)iterator.next();
 
         if (!reservable.isClosed() && !reservable.isSentinel()) {
           ++result;
         }
       }
     }
 
     return result;
   }
 
   private void purgeZombieResources() {
     synchronized (m_reservableListMutex) {
       if (++m_nextPurge > PURGE_FREQUENCY) {
         m_nextPurge = 0;
 
         final List newReservables = new ArrayList(m_reservables.size());
 
         final Iterator iterator = m_reservables.iterator();
 
         while (iterator.hasNext()) {
           final Reservable reservable = (Reservable)iterator.next();
 
           if (!reservable.isClosed()) {
             newReservables.add(reservable);
           }
         }
 
         m_reservables = newReservables;
         m_lastReservable = 0;
       }
     }
   }
 
   /**
    * Public interface to a resource.
    */
   public interface Resource {
     void close();
   }
 
   /**
    * Listener interface.
    */
   public interface Listener {
 
     void resourceAdded(Resource resource);
 
     void resourceClosed(Resource resource);
   }
 
   /**
    * Add a new listener.
    *
    * @param listener The listener.
    */
   public void addListener(Listener listener) {
     m_listeners.add(listener);
   }
 
   /**
    * Something that can be closed.
    */
   public interface Closeable {
 
     void close();
 
     boolean isClosed();
   }
 
   /**
    * Public interface to a resource reservation.
    */
   public interface Reservation extends Closeable {
 
     boolean isSentinel();
 
     Resource getResource();
 
     void free();
   }
 
   private interface Reservable extends Reservation {
     boolean reserve();
   }
 
   private static final class Sentinel implements Reservable {
 
     public boolean isSentinel() {
       return true;
     }
 
     public boolean reserve() {
       return true;
     }
 
     public Resource getResource() {
       return null;
     }
 
     public void free() {
     }
 
     public void close() {
     }
 
     public boolean isClosed() {
       return false;
     }
   }
 
   private final class ResourceWrapper implements Reservable {
 
     private final Resource m_resource;
     private boolean m_busy = false;
     private boolean m_closed;
 
     public ResourceWrapper(Resource resource) {
       m_resource = resource;
     }
 
     public boolean isSentinel() {
       return false;
     }
 
     public boolean reserve() {
 
       synchronized (this) {
         if (m_busy || m_closed) {
           return false;
         }
 
         m_busy = true;
       }
 
       return true;
     }
 
     public void free() {
 
       final boolean stateChanged;
 
       synchronized (this) {
         stateChanged = m_busy;
         m_busy = false;
       }
 
       if (stateChanged) {
         synchronized (m_reservableMutex) {
           m_reservableMutex.notifyAll();
         }
       }
     }
 
     public Resource getResource() {
       return m_resource;
     }
 
     public void close() {
 
       final boolean stateChanged;
 
       synchronized (this) {
         stateChanged = !m_closed;
 
         if (stateChanged) {
           // Update state before closing resource to prevent possible
           // recursion.
           m_busy = false;
           m_closed = true;
 
           m_resource.close();
         }
       }
 
       if (stateChanged) {
         synchronized (m_reservableMutex) {
           m_reservableMutex.notifyAll();
         }
 
         m_listeners.apply(
           new ListenerSupport.Informer() {
             public void inform(Object listener) {
               ((Listener)listener).resourceClosed(m_resource);
             }
           });
       }
     }
 
     public synchronized boolean isClosed() {
       return m_closed;
     }
   }
 }
