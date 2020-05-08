 package org.grouplens.ratingvalue;
 
 import org.grouplens.lenskit.cursors.AbstractCursor;
 import org.grouplens.lenskit.cursors.Cursor;
 import org.grouplens.lenskit.cursors.Cursors;
 import org.grouplens.lenskit.cursors.LongCursor;
 import org.grouplens.lenskit.data.Event;
 import org.grouplens.lenskit.data.UserHistory;
 import org.grouplens.lenskit.data.dao.*;
 import org.grouplens.lenskit.data.event.Rating;
 import org.grouplens.lenskit.data.event.SimpleRating;
 import org.grouplens.lenskit.data.pref.PreferenceDomain;
 import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.WillCloseWhenClosed;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  */
 public class RescaledRatingDao implements DataAccessObject {
     protected static final Logger logger = LoggerFactory.getLogger(RescaledRatingDao.class);
 
     private final PreferenceDomain originalDomain;
     private final PreferenceDomain newDomain;
     private final PreferenceDomainQuantizer quantizer;
     private final EventCollectionDAO delegate;
     private final DataAccessObject dao;
     private double[] thresholds;
 
 
     public RescaledRatingDao(PreferenceDomain originalDomain, PreferenceDomain newDomain, DataAccessObject dao, double [] thresholds) {
         this.originalDomain = originalDomain;
         this.newDomain = newDomain;
         this.thresholds = thresholds;
         this.quantizer = new PreferenceDomainQuantizer(newDomain);
 
         List<Event> transformed = new ArrayList<Event>();
         for (Event event : dao.getEvents()) {
             if (event instanceof Rating) {
                 event = rescale((Rating) event);
             }
             transformed.add(event);
         }
         this.delegate = new EventCollectionDAO(transformed);
         this.dao = dao;
     }
     
     public Rating rescale(Rating rating) {
         if (rating.getPreference() == null) {
             return rating;
         }
         double r1 = rating.getPreference().getValue();
         double r2 = newDomain.getMaximum();
         for (int i = 0; i < thresholds.length; i++) {
            if (r1 < thresholds[i]) {
                 r2 = quantizer.getValue(i);
             }
         }
         // normalized will be in [0,1]
 //        double normalized = (rating.getPreference().getValue() - originalDomain.getMinimum())
 //                        / (originalDomain.getMaximum() - originalDomain.getMinimum());
 //        double rescaled = normalized * (newDomain.getMaximum() - newDomain.getMinimum()) + newDomain.getMinimum();
 //
 //        double binned = Utils.binRating(newDomain, rescaled);
         return new SimpleRating(rating.getId(), rating.getUserId(), rating.getItemId(), r2, rating.getTimestamp());
     }
 
     public DataAccessObject getDao() {
         return this.dao;
     }
 
     @Override
     public LongCursor getUsers() {
         return delegate.getUsers();
     }
 
     @Override
     public <E extends Event> Cursor<E> getUserEvents(long user, Class<E> type) {
         return delegate.getUserEvents(user, type);
     }
 
     @Override
     public Cursor<UserHistory<Event>> getUserHistories() {
         return delegate.getUserHistories();
     }
 
     @Override
     public <E extends Event> Cursor<UserHistory<E>> getUserHistories(Class<E> type) {
         return delegate.getUserHistories(type);
     }
 
     @Override
     public <E extends Event> Cursor<E> getItemEvents(long item, Class<E> type) {
         return delegate.getItemEvents(item, type);
     }
 
     @Override
     public Cursor<Event> getEvents() {
         return delegate.getEvents();
     }
 
     @Override
     public void close() {
         delegate.close();
     }
 
     @Override
     public Cursor<? extends Event> getEvents(SortOrder order) {
         return delegate.getEvents(order);
     }
 
     @Override
     public <E extends Event> Cursor<E> getEvents(Class<E> type) {
         return delegate.getEvents(type);
     }
 
     @Override
     public <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) {
         return delegate.getEvents(type, order);
     }
 
     @Override
     public Cursor<? extends Event> getUserEvents(long userId) {
         return delegate.getUserEvents(userId);
     }
 
     @Override
     public UserHistory<Event> getUserHistory(long user) {
         return delegate.getUserHistory(user);
     }
 
     @Override
     public <E extends Event> UserHistory<E> getUserHistory(long user, Class<E> type) {
         return delegate.getUserHistory(user, type);
     }
 
     @Override
     public Cursor<? extends Event> getItemEvents(long itemId) {
         return delegate.getItemEvents(itemId);
     }
 
     @Override
     public LongCursor getItems() {
         return delegate.getItems();
     }
 
     @Override
     public int getItemCount() {
         return delegate.getItemCount();
     }
 
     @Override
     public int getUserCount() {
         return delegate.getUserCount();
     }
     
     public static class Factory implements DAOFactory {
         private final PreferenceDomain originalDomain;
         private final PreferenceDomain newDomain;
         private final DAOFactory daoFactory;
         private double[] thresholds;
 
         public Factory(PreferenceDomain originalDomain, PreferenceDomain newDomain, DAOFactory daoFactory, double thresholds []) {
             this.originalDomain = originalDomain;
             this.newDomain = newDomain;
             this.daoFactory = daoFactory;
             this.thresholds = thresholds;
         }
 
         @Override
         public DataAccessObject create() {
             return new RescaledRatingDao(originalDomain, newDomain, daoFactory.create(), thresholds);
         }
 
         @Override
         public DataAccessObject snapshot() {
             return create();
         }
     }
 }
