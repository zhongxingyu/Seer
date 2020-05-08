 package org.commonjava.shelflife.store.infinispan;
 
 import static org.commonjava.shelflife.expire.ExpirationEventType.CANCEL;
 import static org.commonjava.shelflife.expire.ExpirationEventType.EXPIRE;
 import static org.commonjava.shelflife.expire.ExpirationEventType.SCHEDULE;
 import static org.commonjava.shelflife.store.infinispan.BlockKeyUtils.generateCurrentBlockKey;
 import static org.commonjava.shelflife.store.infinispan.BlockKeyUtils.generateNextBlockKey;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 
 import org.commonjava.shelflife.expire.ExpirationEvent;
 import org.commonjava.shelflife.expire.ExpirationManager;
 import org.commonjava.shelflife.expire.ExpirationManagerException;
 import org.commonjava.shelflife.expire.match.ExpirationMatcher;
 import org.commonjava.shelflife.model.Expiration;
 import org.commonjava.shelflife.model.ExpirationKey;
 import org.commonjava.shelflife.store.infinispan.inject.ShelflifeCache;
 import org.commonjava.shelflife.store.infinispan.inject.ShelflifeCaches;
 import org.commonjava.util.logging.Logger;
 import org.infinispan.Cache;
 
 @javax.enterprise.context.ApplicationScoped
 public class InfinispanExpirationManager
     implements ExpirationManager
 {
 
     public static final int NEXT_EXPIRATION_OFFSET_MINUTES = 5;
 
     private static final long NEXT_EXPIRATION_BATCH_OFFSET =
         TimeUnit.MILLISECONDS.convert( NEXT_EXPIRATION_OFFSET_MINUTES, TimeUnit.MINUTES );
 
     private static final long MIN_PURGE_PERIOD = 500;
 
     public static final String BLOCK_CACHE_NAME = "shelflife-blocks";
 
     public static final String DATA_CACHE_NAME = "shelflife-data";
 
     private final Logger logger = new Logger( getClass() );
 
     private final Timer timer = new Timer( true );
 
     private final LinkedHashMap<ExpirationKey, Expiration> currentExpirations =
         new LinkedHashMap<ExpirationKey, Expiration>();
 
     @Inject
     private Event<ExpirationEvent> eventQueue;
 
     @Inject
     @ShelflifeCache( ShelflifeCaches.BLOCKS )
     //    @Named( BLOCK_CACHE_NAME )
     private transient Cache<String, Set<ExpirationKey>> expirationBlocks;
 
     @Inject
     @ShelflifeCache( ShelflifeCaches.DATA )
     //    @Named( DATA_CACHE_NAME )
     private transient Cache<ExpirationKey, Expiration> expirationCache;
 
     @PostConstruct
     protected void init()
     {
         timer.schedule( new LoadNextExpirationsTask(), 0, NEXT_EXPIRATION_BATCH_OFFSET );
 
         timer.schedule( new PurgeExpiredTask(), 0 );
     }
 
     @PreDestroy
     protected void stopLoader()
     {
         timer.cancel();
     }
 
     @Override
     public synchronized void schedule( final Expiration expiration )
         throws ExpirationManagerException
     {
         if ( contains( expiration ) )
         {
             logger.info( "SKIPPING SCHEDULE: %s. Already scheduled!", expiration.getKey() );
             return;
         }
 
         final long expires = expiration.getExpires();
         final long expiresOffset = expires - System.currentTimeMillis();
 
         if ( expiresOffset < NEXT_EXPIRATION_BATCH_OFFSET )
         {
             synchronized ( currentExpirations )
             {
                 currentExpirations.put( expiration.getKey(), expiration );
                 currentExpirations.notifyAll();
             }
             //            timer.schedule( new ExpirationTask( expiration ), expires );
         }
 
         final String blockKey = generateNextBlockKey( expires );
         Set<ExpirationKey> keySet = expirationBlocks.get( blockKey );
         if ( keySet == null )
         {
             keySet = new HashSet<ExpirationKey>();
             expirationBlocks.put( blockKey, keySet );
         }
 
         keySet.add( expiration.getKey() );
 
         expirationCache.put( expiration.getKey(), expiration );
 
         logger.info( "\n\n[%s] SCHEDULED %s, expires: %s\nCurrent time: %s\n\n", System.currentTimeMillis(),
                      expiration.getKey(), new Date( expiration.getExpires() ), new Date() );
         eventQueue.fire( new ExpirationEvent( expiration, SCHEDULE ) );
     }
 
     @Override
     public void cancel( final Expiration expiration )
         throws ExpirationManagerException
     {
         logger.info( "\n\n[%s] ATTEMPTING CANCEL: %s\n\n", System.currentTimeMillis(), expiration.getKey() );
         logger.debug( "Is expiration still active? If not, skip cancellation! %s", expiration.isActive() );
         if ( expiration.isActive() && contains( expiration ) )
         {
             logger.debug( "doing cancel: %s", expiration );
             expiration.cancel();
 
             remove( expiration );
 
             logger.info( "\n\n[%s] CANCELED %s at: %s\n\n", System.currentTimeMillis(), expiration.getKey(), new Date() );
             eventQueue.fire( new ExpirationEvent( expiration, CANCEL ) );
         }
     }
 
     @Override
     public void cancel( final ExpirationKey key )
         throws ExpirationManagerException
     {
         final Expiration expiration = expirationCache.get( key );
         if ( expiration != null )
         {
             cancel( expiration );
         }
     }
 
     private synchronized void remove( final Expiration expiration )
     {
         final String blockKey = generateNextBlockKey( expiration.getExpires() );
         final Set<ExpirationKey> keySet = expirationBlocks.get( blockKey );
         if ( keySet != null )
         {
             keySet.remove( expiration );
 
             if ( keySet.isEmpty() )
             {
                 expirationBlocks.remove( blockKey );
             }
         }
 
         synchronized ( currentExpirations )
         {
             currentExpirations.remove( expiration.getKey() );
         }
 
         expirationCache.remove( expiration.getKey() );
     }
 
     @Override
     public void trigger( final Expiration expiration )
         throws ExpirationManagerException
     {
         logger.info( "\n\n[%s] ATTEMPTING TRIGGER: %s\n\n", System.currentTimeMillis(), expiration.getKey() );
         //        logger.debug( "Is expiration still active? If not, skip trigger! %s", expiration.isActive() );
         if ( expiration.isActive() && contains( expiration ) )
         {
             expiration.expire();
 
             remove( expiration );
 
             logger.info( "\n\n[%s] TRIGGERED %s at: %s\n\n", System.currentTimeMillis(), expiration.getKey(),
                          new Date() );
             eventQueue.fire( new ExpirationEvent( expiration, EXPIRE ) );
         }
     }
 
     @Override
     public void trigger( final ExpirationKey key )
         throws ExpirationManagerException
     {
         final Expiration expiration = expirationCache.get( key );
         if ( expiration != null )
         {
             trigger( expiration );
         }
     }
 
     @Override
     public void triggerAll()
         throws ExpirationManagerException
     {
         logger.debug( "[TRIGGER] ALL" );
         for ( final Expiration exp : all() )
         {
             trigger( exp );
         }
     }
 
     @Override
     public void triggerAll( final ExpirationMatcher matcher )
         throws ExpirationManagerException
     {
         logger.debug( "[TRIGGER] ALL" );
         for ( final Expiration exp : getMatching( matcher ) )
         {
             if ( matcher.matches( exp ) )
             {
                 trigger( exp );
             }
         }
     }
 
     @Override
     public void cancelAll()
         throws ExpirationManagerException
     {
         final Set<Expiration> all = all();
         logger.debug( "[CANCEL] ALL(%s)", all.size() );
         for ( final Expiration exp : all )
         {
             logger.info( "[%s] Canceling: %s", System.currentTimeMillis(), exp );
             cancel( exp );
         }
     }
 
     @Override
     public void cancelAll( final ExpirationMatcher matcher )
         throws ExpirationManagerException
     {
         for ( final Expiration exp : getMatching( matcher ) )
         {
             cancel( exp );
         }
     }
 
     @Override
     public void loadedFromStorage( final Collection<Expiration> expirations )
         throws ExpirationManagerException
     {
         for ( final Expiration expiration : expirations )
         {
             if ( expiration.getExpires() <= System.currentTimeMillis() )
             {
                 trigger( expiration );
             }
             else
             {
                 this.currentExpirations.put( expiration.getKey(), expiration );
             }
 
             synchronized ( currentExpirations )
             {
                 currentExpirations.notifyAll();
             }
         }
     }
 
     @Override
     public boolean contains( final Expiration expiration )
     {
         return currentExpirations.containsKey( expiration.getKey() )
             || expirationCache.containsKey( expiration.getKey() );
     }
 
     private Set<Expiration> getMatching( final ExpirationMatcher matcher )
     {
         final Set<Expiration> matching = new LinkedHashSet<Expiration>();
         for ( final Expiration exp : all() )
         {
             if ( matcher.matches( exp ) )
             {
                 matching.add( exp );
             }
         }
 
         return matching;
     }
 
     private Set<Expiration> all()
     {
         final Set<Expiration> result = new HashSet<Expiration>();
         for ( final Entry<ExpirationKey, Expiration> entry : expirationCache.entrySet() )
         {
             result.add( entry.getValue() );
         }
 
         return result;
     }
 
     public final class PurgeExpiredTask
         extends TimerTask
     {
         private final Logger logger = new Logger( getClass() );
 
         @Override
         public void run()
         {
             //            logger.info( "Purging expired entries." );
 
             synchronized ( currentExpirations )
             {
                 while ( currentExpirations.isEmpty() )
                 {
                     try
                     {
                         //                        logger.info( "Purge task waiting for expirations..." );
                         currentExpirations.wait( TimeUnit.MILLISECONDS.convert( 10, TimeUnit.SECONDS ) );
                     }
                     catch ( final InterruptedException e )
                     {
                         logger.info( "Expiration purge task interrupted." );
                         return;
                     }
                 }
             }
 
             Map<ExpirationKey, Expiration> current;
             synchronized ( currentExpirations )
             {
                 current = new HashMap<ExpirationKey, Expiration>( currentExpirations );
             }
 
             for ( final Map.Entry<ExpirationKey, Expiration> entry : new HashSet<Map.Entry<ExpirationKey, Expiration>>(
                                                                                                                         current.entrySet() ) )
             {
                 final ExpirationKey key = entry.getKey();
                 final Expiration exp = entry.getValue();
                if ( exp == null )
                {
                    continue;
                }
 
                 //                logger.info( "Handling expiration for: %s", key );
 
                 boolean cancel = false;
                 if ( !exp.isActive() )
                 {
                     //                    logger.info( "Expiration no longer active: %s", exp );
                     cancel = true;
                     return;
                 }
 
                 boolean expired = false;
                 if ( !cancel )
                 {
                     expired = exp.getExpires() <= System.currentTimeMillis();
 
                     //                    logger.info( "Checking expiration: %s vs current time: %s. Expired? %s", exp.getExpires(),
                     //                                 System.currentTimeMillis(), expired );
 
                     if ( expired )
                     {
                         try
                         {
                             //                            logger.info( "\n\n\n [%s] TRIGGERING: %s (expiration timeout: %s)\n\n\n",
                             //                                         System.currentTimeMillis(), exp, exp.getExpires() );
 
                             trigger( exp );
                         }
                         catch ( final ExpirationManagerException e )
                         {
                             logger.error( "Failed to trigger expiration: %s. Reason: %s", e, key, e.getMessage() );
 
                             cancel = true;
                         }
                     }
                 }
 
                 if ( cancel )
                 {
                     //                    logger.info( "Canceling: %s", key );
                     try
                     {
                         InfinispanExpirationManager.this.cancel( exp );
                     }
                     catch ( final ExpirationManagerException e )
                     {
                         logger.error( "Failed to cancel expiration: %s. Reason: %s", e, key, e.getMessage() );
                     }
                 }
 
                 if ( cancel || expired )
                 {
                     synchronized ( currentExpirations )
                     {
                         //                        logger.info( "Removing handled expiration: %s", key );
                         remove( exp );
                         current.remove( key );
                     }
                 }
             }
 
             if ( !current.isEmpty() )
             {
                 final List<Expiration> remaining = new ArrayList<Expiration>( current.values() );
                 //                logger.info( "Sorting %d remaining expirations", remaining.size() );
 
                 Collections.sort( remaining, new Comparator<Expiration>()
                 {
                     @Override
                     public int compare( final Expiration one, final Expiration two )
                     {
                         return Long.valueOf( one.getExpires() )
                                    .compareTo( two.getExpires() );
                     }
                 } );
 
                 boolean scheduled = false;
                 for ( final Expiration exp : remaining )
                 {
                     if ( exp.getExpires() > ( System.currentTimeMillis() + MIN_PURGE_PERIOD ) )
                     {
                         final long offset = exp.getExpires() - System.currentTimeMillis();
 
                         logger.info( "Next purge in %d seconds.",
                                      TimeUnit.SECONDS.convert( offset, TimeUnit.MILLISECONDS ) );
 
                         timer.schedule( new PurgeExpiredTask(), offset );
                         scheduled = true;
                         break;
                     }
                 }
 
                 if ( !scheduled )
                 {
                     logger.info( "Next purge in: %d seconds.",
                                  TimeUnit.SECONDS.convert( MIN_PURGE_PERIOD, TimeUnit.MILLISECONDS ) );
 
                     timer.schedule( new PurgeExpiredTask(), MIN_PURGE_PERIOD );
                 }
             }
         }
     }
 
     public final class LoadNextExpirationsTask
         extends TimerTask
     {
 
         private final Logger logger = new Logger( getClass() );
 
         @Override
         public void run()
         {
             final String key = generateCurrentBlockKey();
             logger.info( "Loading batch of expirations for: %s", key );
 
             final Set<ExpirationKey> expirationKeys = expirationBlocks.get( key );
             if ( expirationKeys != null )
             {
                 synchronized ( currentExpirations )
                 {
                     int added = 0;
                     for ( final ExpirationKey expirationKey : expirationKeys )
                     {
                         final Expiration expiration = expirationCache.get( expirationKey );
                         if ( !currentExpirations.containsKey( expiration ) )
                         {
                             currentExpirations.put( expirationKey, expiration );
                             added++;
                         }
                     }
 
                     if ( added > 0 )
                     {
                         currentExpirations.notifyAll();
                     }
                 }
             }
         }
     }
 
 }
