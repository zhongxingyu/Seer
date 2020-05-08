 /*
  * Copyright (c) 2008-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package iudex.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.htmap.UniMap;
 
 /**
  * A prioritized queue of ready and sleeping HostQueues. The ready
  * hosts queue is prioritized by the top priority order in each ready
  * host. The sleeping hosts queue is prioritized by least next visit
  * time.
  */
 public class VisitQueue implements VisitCounter
 {
     public int defaultMinHostDelay()
     {
         return _defaultMinHostDelay;
     }
 
     public void setDefaultMinHostDelay( int defaultMinHostDelay )
     {
         _defaultMinHostDelay = defaultMinHostDelay;
     }
 
     public int defaultMaxAccessPerHost()
     {
         return _defaultMaxAccessPerHost;
     }
 
     public void setDefaultMaxAccessPerHost( int defaultMaxAccessPerHost )
     {
         _defaultMaxAccessPerHost = defaultMaxAccessPerHost;
     }
 
     public synchronized void configureHost( String host,
                                             int minHostDelay,
                                             int maxAccessCount )
     {
         configureHost( host, null, minHostDelay, maxAccessCount );
     }
 
     public synchronized void configureHost( String host,
                                             String type,
                                             int minHostDelay,
                                             int maxAccessCount )
     {
         DomainKey key = configKey( host, type );
         HostQueue hq = _hosts.get( key );
         if( hq != null ) {
             throw new IllegalStateException(
                 "configureHost on non empty VisitQueue or " +
                 hq.key() +
                 " already configured." );
         }
 
         _hosts.put( key, new HostQueue( key, minHostDelay, maxAccessCount ) );
     }
 
     /**
      * Create a new VisitQueue with the same defaults and host configuration.
      * This is intended to support configuring a VisitQueue template and
      * cloning repeatedly for use.
      * @throws IllegalStateException if this VisitQueue has orders already.
      */
     @Override
     public VisitQueue clone()
     {
         if( _orderCount > 0 ) {
             throw new IllegalStateException(
                 "VisitQueue can't be cloned with orders" );
         }
 
         VisitQueue newQ = new VisitQueue();
         newQ._defaultMinHostDelay     = _defaultMinHostDelay;
         newQ._defaultMaxAccessPerHost = _defaultMaxAccessPerHost;
 
         //Very important to deep copy the host queues
         for( HostQueue hq : _hosts.values() ) {
             newQ._hosts.put( hq.key(), hq.clone() );
         }
 
         return newQ;
     }
 
     public synchronized void addAll( List<UniMap> orders )
     {
         for( UniMap order : orders ) {
             privAdd( order );
         }
         notifyAll();
     }
 
     public synchronized void add( UniMap order )
     {
         privAdd( order );
         notifyAll();
     }
 
     /**
      * Return the total number of visit orders across all hosts.
      */
     public synchronized int orderCount()
     {
         return _orderCount;
     }
 
     /**
      * Return the total of acquired, and as not yet released orders.
      */
     public synchronized int acquiredCount()
     {
         return _acquiredCount;
     }
 
     /**
      * Return the total number of unique hosts for which there is at
      * least one visit order.
      */
     public synchronized int hostCount()
     {
         return _hostCount;
     }
 
     /**
      * Returns the highest priority of the available visit orders. May block
      * up to maxWait milliseconds. Caller must call the
      * {@link #release(UniMap, UniMap)}  when done processing this order.
      * @return UniMap visit order or null if maxWait was  exceeded
      */
     public synchronized UniMap acquire( long maxWait )
         throws InterruptedException
     {
         UniMap job = null;
         HostQueue hq = take( maxWait );
         if( hq != null ) {
             _log.debug( "Take: {}", hq.key() );
 
             if( ! hq.isAvailable() ) {
                 throw new IllegalStateException( "Unavailable host take!");
             }
 
             job = hq.remove();
 
             untakeImpl( hq );
             ++_acquiredCount;
         }
         return job;
     }
 
     @Override
     public synchronized void release( UniMap acquired, UniMap newOrder )
     {
         if( newOrder != null ) privAdd( newOrder );
         --_orderCount;
         --_acquiredCount;
 
         if( acquired == null ) {
             throw new NullPointerException( "Null release!" );
         }
 
         DomainKey key = orderKey( acquired );
         HostQueue queue = _hosts.get( key );
 
         if( queue == null ) {
             throw new IllegalStateException( "Host order key [" +
                                              key + "] not found" );
         }
 
         _log.debug( "Release: {} {}", queue.key(), queue.size() );
 
         if( queue.release() && ( queue.size() > 0 ) ) addSleep( queue );
 
         checkRemove( queue );
     }
 
     protected DomainKey orderKey( UniMap order )
     {
         final String domain = order.get( ContentKeys.URL ).domain();
 
         List<DomainKey> typedKeys = _typedDomainKeys.get( domain );
         if( typedKeys != null ) {
             DomainKey key = new DomainKey( domain,
                                            order.get( ContentKeys.TYPE ) );
             for( DomainKey old : typedKeys ) {
                 if( old.equals( key ) ) return old;
             }
         }
 
         return new DomainKey( domain, null );
     }
 
     protected DomainKey configKey( String host, String type )
     {
         host = Domains.normalize( host.trim() );
         String domain = Domains.registrationLevelDomain( host );
         if( domain == null ) domain = host;
 
         if( type != null ) type = type.intern();
 
         DomainKey key = new DomainKey( domain, type );
 
         if( type != null ) {
             List<DomainKey> keys = _typedDomainKeys.get( domain );
             if( keys == null ) {
                 keys = new ArrayList<DomainKey>();
                 _typedDomainKeys.put( domain, keys );
             }
             for( DomainKey old : keys ) {
                 if( old.equals( key ) ) return old;
             }
             keys.add( key );
         }
 
         return key;
     }
 
     synchronized String dump()
     {
         StringBuilder out = new StringBuilder(4096);
         long now = System.currentTimeMillis();
 
         out.append( String.format(
             "VisitQueue@%x Dump, orders %d, acq %d, hosts %d ::\n",
             System.identityHashCode( this ),
             orderCount(),
             acquiredCount(),
             hostCount() ) );
 
         for( HostQueue hq : _hosts.values() ) {
 
             boolean isReady = _readyHosts.contains( hq );
             boolean isSleep = _sleepHosts.contains( hq );
 
             out.append( String.format(
                 "%20s size %4d, acq %1d, delay %3dms, next %3dms, %c %c\n",
                 hq.key(),
                 hq.size(),
                 hq.accessCount(),
                 hq.minHostDelay(),
                 hq.nextVisit() - now,
                 ( isReady ? 'R' : ' ' ),
                 ( isSleep ? 'S' : ' ' ) ) );
          }
 
         return out.toString();
     }
 
     /**
      * Take the next ready/highest priority host queue. May block up
      * to maxWait for the next ready queue.
      * @param maxWait maximum wait in milliseconds
      * @return HostQueue or null if maxWait exceeded
      */
     private synchronized HostQueue take( long maxWait )
         throws InterruptedException
     {
         long now = System.currentTimeMillis();
         HostQueue ready = null;
         while( ( ( ready = _readyHosts.poll() ) == null ) &&
                ( maxWait > 0 ) ) {
             HostQueue next = null;
             while( ( next = _sleepHosts.peek() ) != null ) {
                 if( ( now - next.nextVisit() ) >= 0 ) {
                     addReady( _sleepHosts.remove() );
                 }
                 else break;
             }
             if( _readyHosts.isEmpty() ) {
 
                 long delay = maxWait;
                 if( next != null ) {
                     delay = Math.min( next.nextVisit() - now + 1, maxWait );
                 }
                 wait( delay );
                 long nextNow = System.currentTimeMillis();
                 maxWait -= nextNow - now;
                 now = nextNow;
             }
         }
         if( ready != null ) ready.setLastTake( now );
         return ready;
     }
 
     private void checkRemove( HostQueue queue )
     {
         if( ( queue.accessCount() == 0 ) && ( queue.size() == 0 ) ) {
             --_hostCount;
             if( ( queue.key().type() == null ) &&
                 ( queue.minHostDelay() == _defaultMinHostDelay ) &&
                 ( queue.maxAccessCount() == _defaultMaxAccessPerHost ) ) {
                 _hosts.remove( queue.key() );
             }
         }
     }
 
     private void untakeImpl( HostQueue queue )
     {
         if( queue.isAvailable() && ( queue.size() > 0 ) ) {
             addSleep( queue );
         }
     }
 
     private void privAdd( UniMap order )
     {
         DomainKey key = orderKey( order );
 
         HostQueue queue = _hosts.get( key );
         if( queue == null ) {
             queue = new HostQueue( key,
                                    _defaultMinHostDelay,
                                    _defaultMaxAccessPerHost );
             _hosts.put( key, queue );
         }
 
         queue.add( order );
 
         if( ( queue.size() == 1 ) && ( queue.isAvailable() ) ) {
             addReady( queue );
         }
         if( ( queue.size() == 1 ) && ( queue.accessCount() == 0 ) ) {
             ++_hostCount;
         }
 
         ++_orderCount;
     }
 
     private void addReady( HostQueue queue )
     {
         if( _log.isDebugEnabled() ) {
             _log.debug( "addReady: {} {}", queue.key(), queue.size() );
             checkAdd( queue );
         }
 
         if( ! queue.isAvailable() ) {
             throw new IllegalStateException( "Unavailable addReady!");
         }
 
         _readyHosts.add( queue );
     }
 
     private void addSleep( HostQueue queue )
     {
         if( _log.isDebugEnabled() ) {
             _log.debug( "addSleep: {} {}", queue.key(), queue.size() );
             checkAdd( queue );
         }
 
         if( ! queue.isAvailable() ) {
             throw new IllegalStateException( "Unavailable addSleep!");
         }
 
         _sleepHosts.add( queue );
         notifyAll();
     }
 
     private void checkAdd( HostQueue queue )
         throws IllegalStateException
     {
         if( _readyHosts.contains( queue ) ) {
             throw new IllegalStateException( "Already ready!" );
         }
         if( _sleepHosts.contains( queue ) ) {
             throw new IllegalStateException( "Already sleeping!" );
         }
         if( queue.size() == 0 ) {
             throw new IllegalStateException( "Adding empty queue!" );
         }
     }
 
     private int _defaultMinHostDelay     = 500; //ms
     private int _defaultMaxAccessPerHost =   1;
 
     private int _orderCount = 0;
     private int _acquiredCount = 0;
     private int _hostCount = 0;
 
     private final Map<DomainKey, HostQueue> _hosts      =
         new HashMap<DomainKey, HostQueue>( 2048 );
 
    private final Map<String,List<DomainKey>> _typedDomainKeys =
         new HashMap<String,List<DomainKey>>( 16 );
 
     private PriorityQueue<HostQueue>     _readyHosts =
         new PriorityQueue<HostQueue>( 1024, new HostQueue.TopOrderComparator());
 
     private PriorityQueue<HostQueue>     _sleepHosts =
         new PriorityQueue<HostQueue>( 128, new HostQueue.NextVisitComparator());
 
     private Logger _log = LoggerFactory.getLogger( getClass() );
 }
