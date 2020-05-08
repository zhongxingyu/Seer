 package nmd.rss.collector.gae.feed;
 
 import nmd.rss.collector.feed.FeedHeader;
 import nmd.rss.collector.feed.FeedItem;
 import nmd.rss.collector.updater.FeedHeadersRepository;
 import nmd.rss.collector.updater.FeedItemsRepository;
 import nmd.rss.collector.updater.FeedService;
 import nmd.rss.collector.updater.FeedServiceException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.UUID;
 
 import static nmd.rss.collector.feed.TimestampComparator.TIMESTAMP_COMPARATOR;
 import static nmd.rss.collector.util.Assert.assertNotNull;
 import static nmd.rss.collector.util.TransactionTools.rollbackIfActive;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 02.05.13
  */
 public class FeedServiceImpl implements FeedService {
 
     private final EntityManager entityManager;
     private final FeedItemsRepository feedItemsRepository;
     private final FeedHeadersRepository feedHeadersRepository;
 
     public FeedServiceImpl(final EntityManager entityManager, final FeedItemsRepository feedItemsRepository, final FeedHeadersRepository feedHeadersRepository) {
         assertNotNull(entityManager);
         this.entityManager = entityManager;
 
         assertNotNull(feedItemsRepository);
         this.feedItemsRepository = feedItemsRepository;
 
         assertNotNull(feedHeadersRepository);
         this.feedHeadersRepository = feedHeadersRepository;
     }
 
     @Override
     public FeedHeader loadHeader(final UUID feedId) throws FeedServiceException {
         assertNotNull(feedId);
 
         EntityTransaction transaction = null;
 
         try {
             transaction = this.entityManager.getTransaction();
             transaction.begin();
 
             final FeedHeader result = this.feedHeadersRepository.loadHeader(feedId);
 
             transaction.commit();
 
             return result;
         } catch (Exception exception) {
             throw new FeedServiceException(exception);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     @Override
     public List<FeedItem> loadItems(final UUID feedId) throws FeedServiceException {
         assertNotNull(feedId);
 
         EntityTransaction transaction = null;
 
         try {
             transaction = this.entityManager.getTransaction();
             transaction.begin();
 
             final List<FeedItem> result = this.feedItemsRepository.loadItems(feedId);
 
             transaction.commit();
 
             return result;
         } catch (Exception exception) {
             throw new FeedServiceException(exception);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     @Override
     public void updateItems(final UUID feedId, final List<FeedItem> removed, final List<FeedItem> retained, final List<FeedItem> added) throws FeedServiceException {
         assertNotNull(feedId);
         assertNotNull(removed);
         assertNotNull(retained);
         assertNotNull(added);
 
         EntityTransaction transaction = null;
 
         try {
             final List<FeedItem> feedItems = new ArrayList<>();
             feedItems.addAll(retained);
             feedItems.addAll(added);
 
             Collections.sort(feedItems, TIMESTAMP_COMPARATOR);
 
             transaction = this.entityManager.getTransaction();
             transaction.begin();
 
             this.feedItemsRepository.updateItems(feedId, feedItems);
 
             transaction.commit();
         } catch (Exception exception) {
             throw new FeedServiceException(exception);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     @Override
     public void clearAll() throws FeedServiceException {
         EntityTransaction transaction = null;
 
         try {
             transaction = this.entityManager.getTransaction();
             transaction.begin();
 
            final List<FeedItems> victims = this.feedItemsRepository.loadAllEntities();
 
             transaction.commit();
 
            for (final FeedItems victim : victims) {
                 transaction = this.entityManager.getTransaction();
                 transaction.begin();
 
                 this.feedItemsRepository.removeEntity(victim);
 
                 transaction.commit();
             }
         } catch (Exception exception) {
             throw new FeedServiceException(exception);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
 }
