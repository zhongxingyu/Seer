 package com.wadpam.rnr.service;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Rating;
 import com.wadpam.open.analytics.google.GoogleAnalyticsTracker;
 import com.wadpam.open.transaction.Idempotent;
 import com.wadpam.rnr.dao.*;
 import com.wadpam.rnr.domain.*;
 import com.wadpam.open.exceptions.NotFoundException;
 import net.sf.mardao.core.CursorPage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.Serializable;
 import java.util.*;
 import java.util.concurrent.Future;
 
 /**
  *
  * @author os
  */
 public class RnrService {
     static final Logger LOG = LoggerFactory.getLogger(RnrService.class);
 
     public static final int ERR_BASE_FEEDBACK = 10000;
 
     public static final int ERR_BASE_RNR = 20000;
     public static final int ERR_BASE_COMMENTS = ERR_BASE_RNR + 1000;
     public static final int ERR_BASE_FAVORITES = ERR_BASE_RNR + 2000;
     public static final int ERR_BASE_LIKE = ERR_BASE_RNR + 3000;
     public static final int ERR_BASE_RATE = ERR_BASE_RNR + 4000;
     public static final int ERR_BASE_THUMBS = ERR_BASE_RNR + 5000;
 
     public static final int ERR_BASE_PRODUCT_NOT_FOUND = ERR_BASE_RNR + 1;
 
     // Analytics
     private static final String RNR_CATEGORY = "RnR";
 
     // Max number of random users that liked a product that will be saved
     private static final int MAX_RANDOM_USERS = 100;
 
     // Properties
     private DAppSettingsDao appSettingsDao;
     private DProductDao productDao;
     private DRatingDao ratingDao;
     private DLikeDao likeDao;
     private DThumbsDao thumbsDao;
     private DCommentDao commentDao;
     private DFavoritesDao favoritesDao;
 
     private boolean tracking = true;
 
     // Decide the sort order in nearby searches
     public enum SortOrder {DISTANCE, TOP_RATED, MOST_LIKED, MOST_THUMBS_UP}
 
     // enum to hold thumbs up and down
     public enum Thumbs {
         UP(1), DOWN(-1);
 
         private int value;
 
         private Thumbs(int v) {
             value = v;
         }
 
         public int getValue() {
             return value;
         }
     }
 
 
     // Init
     public void init() {
         // Do nothing
     }
 
 
     /* Like related methods */
 
     // Like a product
     @Idempotent
     @Transactional
     public DLike addLike(String domain, String productId, String username, Float latitude, Float longitude,
                          GoogleAnalyticsTracker tracker) {
         LOG.debug("Add new like to product:{}", productId);
 
         // Send of all datastore queries to start with, will run in parallel
         Future settingsFuture = appSettingsDao.findByPrimaryKeyForFuture(domain);
         Future productFuture = productDao.findByPrimaryKeyForFuture(productId);
 
         DLike dLike = null;
         if (null != username) {
             // blocking
             dLike = likeDao.findByProductIdUsername(productId, username);
         }
 
         // Can users only Like once?
         DAppSettings settings = appSettingsDao.getDomain(settingsFuture); // block
         boolean onlyLikeOncePerUser = (null != settings) ? settings.getOnlyLikeOncePerUser() : true;
 
         // Create new?
         Future likeFuture = null;
         if (null == dLike || !onlyLikeOncePerUser) {
             dLike = new DLike();
             dLike.setProductId(productId);
             dLike.setUsername(username);
             // Store
             likeFuture = likeDao.persistForFuture(dLike); // non-blocking
         }  else {
             LOG.debug("User:{} already liked this product", username);
             // Do not increase the like count, just return the existing like
             return dLike;
         }
 
         // Update total number of likes
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null == dProduct) {
             // First time this product is handled, create new
             dProduct = new DProduct();
             dProduct.setProductId(productId);
             dProduct.setLikeCount(1L);
         } else {
             // Update existing
             dProduct.setLikeCount(dProduct.getLikeCount() + 1);
         }
 
         // Update the product location if any is provided
         if (null != latitude && null != longitude) {
             final GeoPt location = new GeoPt(latitude, longitude);
             dProduct.setLocation(location);
         }
 
         // Update random users that liked if user name is provided
         if (null != username) {
             ArrayList<String> randomLikedUsers  = null;
 
             if (null == dProduct.getLikeRandomUsernames()) {
                 // First time this product is liked
                 randomLikedUsers = new ArrayList<String>(1);
             } else {
                 randomLikedUsers = new ArrayList<String>(dProduct.getLikeRandomUsernames());
             }
 
             // Remove the first one if more the 100
             if (randomLikedUsers.size() > MAX_RANDOM_USERS) {
                 randomLikedUsers.remove(1);
             }
 
             // Add new user
             randomLikedUsers.add(username);
 
             // Shuffle and set
             Collections.shuffle(randomLikedUsers);
             dProduct.setLikeRandomUsernames(randomLikedUsers);
         }
 
         // Persist and index the location
         productDao.persistForFuture(dProduct);
 
         // Call to generate a datastrore ConcurrentModificationException in order to test the transaction retry mechanism
         // Uncomment this to run tests
         //throw(new ConcurrentModificationException());
 
         // Track the event
         if (isTracking() && null != tracker) {
             try {
                 tracker.trackEvent(RNR_CATEGORY, "like", productId, 1);
             } catch (Exception doNothing) {
                 // Make sure this never generates and exception that cause the transaction to fail
                 LOG.warn("Sending like event to analytics failed:{}", doNothing);
             }
         }
 
         // If we have a future get the result to get hold of the like id
         if (null != likeFuture) {
             dLike.setId(likeDao.getSimpleKey(likeFuture));
         }
 
         return dLike;
     }
 
     // Get a like with a specific id
     public DLike getLike(long id) {
         LOG.debug("Get like with id:{}", id);
         return likeDao.findByPrimaryKey(id);
     }
 
 
     // Delete a like with a specific id
     @Idempotent
     @Transactional
     public DLike deleteLike(long id) {
         LOG.debug("Delete like with id:{}", id);
 
        DLike dLike = likeDao.findByPrimaryKey(null, id);  // block
         if (null == dLike)
            return null;
 
         // Get product, non blocking already here
        Future productFuture = productDao.findByPrimaryKeyForFuture(null,dLike.getProductId());
        
         // Update the product
         DProduct dProduct = productDao.getDomain(productFuture);  // block
         if (null != dProduct) {
             dProduct.setLikeCount(dProduct.getLikeCount() - 1);
             if (null!= dProduct.getLikeRandomUsernames()) {
                 dProduct.getLikeRandomUsernames().remove(dLike.getUsername());
             }
             Future futureObject= productDao.persistForFuture(dProduct);
             // Delete the like
             likeDao.delete(dLike); // block
             productDao.getDomain(futureObject);//block
             
         } else
             // Should not happen, log error
         {
             LOG.error("Like exist but not the product:{}", dLike.getProductId());
             likeDao.delete(dLike); // block
         }
 
         return dLike;
     }
 
     // Get all likes for a specific user
     public Iterable<DLike> getMyLikes(String username) {
         LOG.debug("Get all likes for user:{}", username);
         return likeDao.queryByUsername(username);
     }
 
 
     /* Thumbs related methods */
 
 
     // Add a thumbs up or down
     @Idempotent
     @Transactional
     public DThumbs addThumbs(String domain, String productId, String username, Float latitude, Float longitude,
                              Thumbs value, GoogleAnalyticsTracker tracker) {
         LOG.debug("Add new thumbs:{} to product:{}", value, productId);
 
         // Send of all datastore queries to start with, will run in parallel
         Future settingsFuture = appSettingsDao.findByPrimaryKeyForFuture(domain);
         Future productFuture = productDao.findByPrimaryKeyForFuture(productId);
 
         DThumbs dThumbs = null;
         if (null != username) {
             dThumbs  = thumbsDao.findByProductIdUsername(productId, username); // blocking
         }
 
         // Specified users can only thumb once
         DAppSettings settings = appSettingsDao.getDomain(settingsFuture);
         boolean onlyThumbOncePerUser = (null != settings) ? settings.getOnlyThumbOncePerUser() : true;
 
         // Remember the existing value
         Thumbs existing = null;
 
         // Create new?
         if (null == dThumbs || !onlyThumbOncePerUser) {
             dThumbs = new DThumbs();
             dThumbs.setProductId(productId);
             dThumbs.setUsername(username);
         } else {
             existing = (dThumbs.getValue()) > 0 ? Thumbs.UP : Thumbs.DOWN;
         }
 
         // Always update the value
         dThumbs.setValue((long)value.getValue());
 
         // Store
         Future thumbdFuture = thumbsDao.persistForFuture(dThumbs);
 
         // Update total number of thumbs
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null == dProduct) {
             // First time this product is handled, create new
             dProduct = new DProduct();
             dProduct.setProductId(productId);
             if (value == Thumbs.UP)
                 dProduct.setThumbsUp(1L);
             else
                 dProduct.setThumbsDown(1L);
         } else {
             if (null == existing) {
                 // User has not thumbed before
                 if (value == Thumbs.UP)
                     dProduct.setThumbsUp(dProduct.getThumbsUp() + 1);
                 else
                     dProduct.setThumbsDown(dProduct.getThumbsDown() + 1);
             } else {
                 // User has thumbed before
                 if (existing != value) {
                     // User has not thumbed before
                     if (value == Thumbs.UP) {
                         dProduct.setThumbsUp(dProduct.getThumbsUp() + 1);
                         dProduct.setThumbsDown(dProduct.getThumbsDown() - 1);
                     }
                     else {
                         dProduct.setThumbsDown(dProduct.getThumbsDown() + 1);
                         dProduct.setThumbsUp(dProduct.getThumbsUp() - 1);
                     }
                 }
             }
         }
 
         // Update the product location if any is provided
         if (null != latitude && null != longitude) {
             final GeoPt location = new GeoPt(latitude, longitude);
             dProduct.setLocation(location);
         }
 
         // Persist and index the location
         productDao.persistForFuture(dProduct);
 
         // Track the event
         if (isTracking() && null != tracker) {
             try {
                 tracker.trackEvent(RNR_CATEGORY, value == Thumbs.UP ? "thumbsUp" : "thumbsDown", productId, 1);
             } catch (Exception doNothing) {
                 // Make sure this never generates and exception that cause the transaction to fail
                 LOG.warn("Sending thumbs event to analytics failed:{}", doNothing);
             }
         }
 
         if (null != thumbdFuture) {
             dThumbs.setId(thumbsDao.getSimpleKey(thumbdFuture));
         }
 
         return dThumbs;
     }
 
     // Delete a specific thumbs
     @Idempotent
     @Transactional
     public DThumbs deleteThumbs(long id) {
         LOG.debug("Delete thumb with id:{}", id);
 
         DThumbs dThumbs = thumbsDao.findByPrimaryKey(id);
         if (null == dThumbs)
             return null;
 
         // Get product, non blocking already here
         Future productFuture = productDao.findByPrimaryKeyForFuture(dThumbs.getProductId());
 
         // Update the product
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null != dProduct) {
 
             // Figure out what value to decrease
             if (dThumbs.getValue() > 0)
                 dProduct.setThumbsUp(dProduct.getThumbsUp() - 1);
             else
                 dProduct.setThumbsDown(dProduct.getThumbsDown() - 1);
 
             productDao.persistForFuture(dProduct);
         } else
             // Should not happen, log error
             LOG.error("Thumb exist but not the product:{}", dThumbs.getProductId());
 
         // Delete
         thumbsDao.delete(dThumbs);
 
         return dThumbs;
     }
 
     // Get a specific thumbs
     public DThumbs getThumbs(long id) {
         LOG.debug("Get thumb with id:{}", id);
         return thumbsDao.findByPrimaryKey(id);
     }
 
     // Get my thumbs for user
     public Iterable<DThumbs> getMyThumbs(String username) {
         LOG.debug("Get all thumbs for user:{}", username);
         return thumbsDao.queryByUsername(username);
     }
 
 
     /* Rating related methods */
 
     // Rate a product
     @Idempotent
     @Transactional
     public DRating addRating(String domain, String productId, String username,
                              Float latitude, Float longitude, int rating, String comment,
                              GoogleAnalyticsTracker tracker) {
         LOG.debug("Add new rating to product:{}", productId);
 
         // Send of all datastore queries to start with, will run in parallel
         Future settingsFuture = appSettingsDao.findByPrimaryKeyForFuture(domain);
         Future productFuture = productDao.findByPrimaryKeyForFuture(productId);
 
         DRating dRating = null;
         if (null != username) {
             dRating = ratingDao.findByProductIdUsername(productId, username);
         }
 
         // specified users can only rate once
         DAppSettings settings = appSettingsDao.getDomain(settingsFuture);
         boolean onlyRateOncePerUser = (null != settings) ? settings.getOnlyRateOncePerUser() : true;
 
         // create new?
         int existing = -1;
         if (null == dRating || !onlyRateOncePerUser) {
             dRating = new DRating();
             dRating.setProductId(productId);
             dRating.setUsername(username);
         }
         else {
             // store existing rating to subtract below
             existing = dRating.getRating().getRating();
         }
 
         // store the rating  (always update the rating and review comment)
         dRating.setRating(new Rating(rating));
         dRating.setComment(comment);
         Future ratingFuture = ratingDao.persistForFuture(dRating);
 
         // update product info
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null == dProduct) {
             dProduct = new DProduct();
             dProduct.setProductId(productId);
             dProduct.setRatingCount(1L);
             dProduct.setRatingSum((long)rating);
             dProduct.setRatingAverage(new Rating(rating));
         }
         else {
             // result exists, and existing rating for user
             if (-1 < existing) {
                 dProduct.setRatingSum(dProduct.getRatingSum() - existing + rating);
                 dProduct.setRatingAverage(new Rating((int)(dProduct.getRatingSum() / dProduct.getRatingCount())));
                 // no need to update ratingCount!
             }
             else {
                 // result exists, no existing rating for user
                 dProduct.setRatingSum(dProduct.getRatingSum() + rating);
                 dProduct.setRatingCount(dProduct.getRatingCount() + 1);
                 dProduct.setRatingAverage(new Rating((int)(dProduct.getRatingSum() / dProduct.getRatingCount())));
             }
         }
 
         // Update product location if provided in the request
         if (null != latitude && null != longitude) {
             final GeoPt location = new GeoPt(latitude, longitude);
             dProduct.setLocation(location);
         }
 
         // Persist and index location
         productDao.persistForFuture(dProduct);
 
         // Track the event
         if (isTracking() && null != tracker) {
             try {
                 tracker.trackEvent(RNR_CATEGORY, "rate", productId, rating);
             } catch (Exception doNothing) {
                 // Make sure this never generates and exception that cause the transaction to fail
                 LOG.warn("Sending ratings event to analytics failed:{}", doNothing);
             }
         }
 
         if (null != ratingFuture) {
             dRating.setId(ratingDao.getSimpleKey(ratingFuture));
         }
 
         return dRating;
     }
 
     // Get a rating with a specific id
     public DRating getRating(long id) {
         LOG.debug("Get rating with id:{}", id);
         return ratingDao.findByPrimaryKey(id);
     }
 
     // Delete a ratings with a specific id
     @Idempotent
     @Transactional
     public DRating deleteRating(long id) {
         LOG.debug("Delete rating with id:{}",  id);
 
         DRating dRating = ratingDao.findByPrimaryKey(id);
         if (null == dRating)
             return null;
 
         // Get product, non-blocking
         Future productFuture = productDao.findByPrimaryKeyForFuture(dRating.getProductId());
 
         // Update the product
         DProduct dProduct = productDao.getDomain(productFuture); // blocking
         if (null != dProduct) {
             dProduct.setRatingSum(dProduct.getRatingSum() - dRating.getRating().getRating());
             dProduct.setRatingCount(dProduct.getRatingCount() - 1);
 
             // Guard against deleting the last rating to avoid / by zero
             if (0 != dProduct.getRatingCount()) {
                 dProduct.setRatingAverage(new Rating((int) (dProduct.getRatingSum() / dProduct.getRatingCount())));
             }
             else {
                 dProduct.setRatingAverage(null);
             }
 
             productDao.persistForFuture(dProduct);
         } else
             // Should not happen, log error
         {
             LOG.error("Rating exist but not the product:{}",  dRating.getProductId());
         }
 
         // Delete the rating
         ratingDao.delete(dRating);
 
         return dRating;
     }
 
     // Get all ratings done by a specific user
     public Iterable<DRating> getMyRatings(String username) {
         LOG.debug("Get all ratings for user:{}",  username);
         return ratingDao.queryByUsername(username);
     }
 
     // Create a histogram
     public Map<Long, Long> getHistogramForProduct(String productId, int interval) {
         LOG.debug("Create histogram for product:{}", productId);
 
         Iterator<DRating> dRatingIterator = ratingDao.queryByProductId(productId).iterator();
 
         // Build histogram
         Map<Long, Long> histogram = new HashMap<Long, Long>();
         while (dRatingIterator.hasNext() ) {
             DRating dRating = dRatingIterator.next();
 
             // Find the interval
             long rounded = Math.round((float)dRating.getRating().getRating() / interval) * interval;
 
             Long currentFrequency = histogram.get(rounded);
             if (null == currentFrequency)
                 // No previous value
                 histogram.put(rounded, 1L);
             else
                 // Update frequency
                 histogram.put(rounded, currentFrequency + 1);
         }
 
         return histogram;
     }
 
     /* Comment related methods */
 
 
     // Add a comment to a product
     @Idempotent
     @Transactional
     public DComment addComment(String productId, String username, Float latitude, Float longitude,
                                String comment, GoogleAnalyticsTracker tracker) {
         LOG.debug("Add new comment to product:(}",  productId);
 
         // Get product already here, non-blocking
         Future productFuture = productDao.findByPrimaryKeyForFuture(productId);
 
         // Create new comment. Do not check if user have commented before
         DComment dComment = new DComment();
         dComment.setProductId(productId);
         dComment.setUsername(username);
         dComment.setComment(comment);
         Future commentFuture = commentDao.persistForFuture(dComment);
 
         // update product info
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null == dProduct) {
             dProduct = new DProduct();
             dProduct.setProductId(productId);
             dProduct.setCommentCount(1L);
         }
         else
             dProduct.setCommentCount(dProduct.getCommentCount() + 1);
 
         // Update product location if provided in the request
         if (null != latitude && null != longitude) {
             final GeoPt location = new GeoPt(latitude, longitude);
             dProduct.setLocation(location);
         }
 
         // Persist and index on location
         productDao.persistForFuture(dProduct);
 
         // Track the event
         if (isTracking() && null != tracker) {
             try {
                 tracker.trackEvent(RNR_CATEGORY, "comment", productId, 1);
             } catch (Exception doNothing) {
                 // Make sure this never generates and exception that cause the transaction to fail
                 LOG.warn("Sending comment event to analytics failed:{}", doNothing);
             }
         }
 
         if (null != commentFuture) {
             // Get the id
             dComment.setId(commentDao.getSimpleKey(commentFuture));
         }
 
         return dComment;
     }
 
     // Get a comment with a specific id
     public DComment getComment(long id) {
         LOG.debug("Get comment with id:{}", id);
         return commentDao.findByPrimaryKey(id);
     }
 
     // Delete a comment with a specific id
     @Idempotent
     @Transactional
     public DComment deleteComment(long id) {
         LOG.debug("Delete comment with id:{}", id);
 
         DComment dComment = commentDao.findByPrimaryKey(id);
         if (null == dComment)
             return null;
 
         // Get product already here, non-blocking
         Future productFuture = productDao.findByPrimaryKeyForFuture(dComment.getProductId());
 
         // Delete the comment
         commentDao.delete(dComment);
 
         // Update the product
         DProduct dProduct = productDao.getDomain(productFuture);
         if (null != dProduct) {
             dProduct.setCommentCount(dProduct.getCommentCount() - 1);
             productDao.persistForFuture(dProduct);
         } else
             // Should not happen, log error
             LOG.error("Comment exist but not the product:{}", dComment.getProductId());
 
         return dComment;
     }
 
     // Get all comments done by a specific user
     public Iterable<DComment> getMyComments(String username) {
         LOG.debug("Get all comments for user:{}", username);
         return commentDao.queryByUsername(username);
     }
 
 
     /* Favorite related methods */
 
     // Add new favorite product
     @Idempotent
     @Transactional
     public DFavorites addFavorite(String productId, String username, GoogleAnalyticsTracker tracker) {
         LOG.debug("Add product:{} as favorites for user:{}", productId, username);
 
         DFavorites dFavorites = favoritesDao.findByPrimaryKey(username);
         if (null == dFavorites) {
             // User does not have any existing favorites
             dFavorites = new DFavorites();
             dFavorites.setUsername(username);
             dFavorites.setProductIds(Arrays.asList(productId));
         } else {
             //productIds can be null, when removing favorite, we just set productIds to empty
             if (null == dFavorites.getProductIds()) {
                 dFavorites.setProductIds(Arrays.asList(productId));
             } else {
                    // Update existing list of favorites if it is not already a favorite
                 if (dFavorites.getProductIds().contains(productId) == false) {
                     dFavorites.getProductIds().add(productId);
                 }
             }
         }
         // Store
         favoritesDao.persistForFuture(dFavorites);
 
         // Track the event
         if (isTracking() && null != tracker) {
             try {
                 tracker.trackEvent(RNR_CATEGORY, "favorite", productId, 1);
             } catch (Exception doNothing) {
                 // Make sure this never generates and exception that cause the transaction to fail
                 LOG.warn("Sending favorite event to analytics failed:{}", doNothing);
             }
         }
 
         return dFavorites;
     }
 
     // Delete a product from favorites
     @Idempotent
     @Transactional
     public DFavorites deleteFavorite(String productId, String username) {
         LOG.debug("Delete product:{} from favorites for user:{}", productId, username);
 
         DFavorites dFavorites = favoritesDao.findByPrimaryKey(username);
 
         // If the favorite is not found return 404
         if (null == dFavorites || dFavorites.getProductIds().remove(productId) == false)
             return null;
 
         // Store
         favoritesDao.persistForFuture(dFavorites);
 
         return dFavorites;
     }
 
     // Get all user favorites
     public DFavorites getFavorites(String username) {
         LOG.debug("Get favorites for user:{}", username);
         return favoritesDao.findByPrimaryKey(username);
     }
 
 
     /* Product related methods */
 
     // Get a specific product
     public DProduct getProduct(String productId, String username) {
         LOG.debug("Get product:{}",  productId);
 
         // Non-blocking get product info
         final Future futureProduct = productDao.findByPrimaryKeyForFuture(productId);
 
         // If user name is provided check if this person have liked this product
         Long likeKey = null;
         if (null != username) {
             // Blocking
             likeKey = likeDao.findKeyByProductIdUsername(productId, username);
         }
 
         DProduct dProduct = productDao.getDomain(futureProduct);
         LOG.info("Future:{}", dProduct);
         if (null == dProduct)
             throw new NotFoundException(ERR_BASE_PRODUCT_NOT_FOUND,
                     String.format("Product with id:%s not found", productId));
 
         // Insert the user like if available
         if (null != username) {
             if (null != likeKey) {
                 dProduct.setLikedByUser(true);
             } else {
                 dProduct.setLikedByUser(false);
             }
         }
 
         return dProduct;
     }
 
     // Get a list of products
     public Iterable<DProduct> getProducts(String[] ids, String username) {
         LOG.debug("Get a list of products:{}", ids);
 
         Iterable<DProduct> productIterable = productDao.queryByPrimaryKeys(null, Arrays.asList(ids));
 
         // If user name is provided check if this person have liked this product
         if (null != username) {
             // Get all for list of product ids
             Iterable<DLike> likeIterable = likeDao.findByProductIdsUsername(Arrays.asList(ids), username);
 
             // Build a hash map for quick lookup
             HashMap<String, Long> likeMap = new HashMap<String, Long>();
             for (DLike dLlike : likeIterable) {
                 likeMap.put(dLlike.getProductId(), dLlike.getId());
             }
 
             // Check each product against the hash map of like keys
             for (DProduct dProduct : productIterable) {
                 if (likeMap.containsKey(dProduct.getProductId())) {
                     dProduct.setLikedByUser(true);
                 } else {
                     dProduct.setLikedByUser(false);
                 }
             }
         }
 
         return productIterable;
     }
 
 
     // Mark the products the user has liked
     private void markLikedProducts(Collection<DProduct> dProducts, String username) {
         if (null == username) {
             return;
         }
 
         // Collect all product Ids
         Collection<String> productIds = new ArrayList<String>();
         for (DProduct dProduct : dProducts) {
             productIds.add(dProduct.getProductId());
         }
 
         // Get all for list of product ids
         Iterable<DLike> likeIterable = likeDao.findByProductIdsUsername(productIds, username);
 
         // Build a hash map for quick lookup
         HashMap<String, Long> likeMap = new HashMap<String, Long>();
         for (DLike dLlike : likeIterable) {
             likeMap.put(dLlike.getProductId(), dLlike.getId());
         }
 
         // Check each product against the hash map of like keys
         for (DProduct dProduct : dProducts) {
             if (likeMap.containsKey(dProduct.getProductId())) {
                 dProduct.setLikedByUser(true);
             }
         }
     }
 
     // Get all products
     public CursorPage<DProduct, String> getProductPage(int pageSize, String cursor) {
         LOG.debug("Get product page with cursor:{} page size:{}", cursor, pageSize);
         return productDao.queryPage(pageSize, cursor);
     }
 
     // Find nearby products with different sort order
     public CursorPage<DProduct, String> findNearbyProducts(int pageSize, String cursor, Float latitude, Float longitude,
                                                            int radius, SortOrder sort) {
         LOG.debug("Find nearby products");
         return productDao.searchForNearby(pageSize, cursor, latitude, longitude, radius, sort);
     }
 
     // Get the most liked products
     public CursorPage<DProduct, String> getMostLikedProducts(int limit, Serializable cursor) {
         LOG.debug("Get most liked products with limit:{}", limit);
         return productDao.queryMostLiked(limit, cursor);
     }
 
     // Get most thumbs up products
     public CursorPage<DProduct, String> getMostThumbsUpProducts(int limit, Serializable cursor) {
         LOG.debug("Get most thumbs up products with limit:{}", limit);
         return productDao.queryMostThumbsUp(limit, cursor);
     }
 
     // Get most thumbs down products
     public CursorPage<DProduct, String> getMostThumbsDownProducts(int limit, Serializable cursor) {
         LOG.debug("Get most thumbs down products with limit:{}", limit);
         return productDao.queryMostThumbsDown(limit, cursor);
     }
 
     // Get the most rated products
     public CursorPage<DProduct, String> getMostRatedProducts(int limit, Serializable cursor) {
         LOG.debug("Get most rated products with limit:{}", limit);
         return productDao.queryMostRated(limit, cursor);
     }
 
     // Get the top rated products
     public CursorPage<DProduct, String> getTopRatedProducts(int limit, Serializable cursor) {
         LOG.debug("Get top rated products with limit:{}", limit);
         return productDao.queryTopRated(limit, cursor);
     }
 
     // Get most commented products
     public CursorPage<DProduct, String> getMostCommentedProducts(int limit, Serializable cursor) {
         LOG.debug("Get most commented products with limit:{}", limit);
         return productDao.queryMostCommented(limit, cursor);
     }
 
      // Get all likes for a product
     public CursorPage<DLike, Long> getAllLikesForProduct(String productId, int limit, Serializable cursor) {
         LOG.debug("Get all likes for product:{}", productId);
         return likeDao.queryPageByProductId(productId, limit, cursor);
     }
 
     // Get all thumbs for a product
     public CursorPage<DThumbs, Long> getAllThumbsForProduct(String productId, int limit, Serializable cursor) {
         LOG.debug("Get all thumbs for product:{}", productId);
         return thumbsDao.queryPageByProductId(productId, limit, cursor);
     }
 
     // Get all ratings for a product
     public CursorPage<DRating, Long> getAllRatingsForProduct(String productId, int limit, Serializable cursor) {
         LOG.debug("Get all ratings for product:{}", productId);
         return ratingDao.queryPageByProductId(productId, limit, cursor);
     }
 
     // Get all comments for a product
     public CursorPage<DComment, Long> getAllCommentsForProduct(String productId, int limit, Serializable cursor) {
         LOG.debug("Get all comments for product:{}", productId);
         return commentDao.queryPageByProductId(productId, limit, cursor);
     }
 
     // Get all products a user have liked
     public Iterable<DProduct> getProductsLikedByUser(String username) {
         LOG.debug("Get all products liked by user:{}", username);
 
         final Iterator<DLike> dLikeIterator = likeDao.queryByUsername(username).iterator();
 
         // Collect all unique product ids and get their products
         Set<String> productIds = new HashSet<String>();
         while (dLikeIterator.hasNext())
             productIds.add(dLikeIterator.next().getProductId());
 
         return productDao.queryByPrimaryKeys(null, productIds);
     }
 
     // Get all products a user have thumbed
     public Iterable<DProduct> getProductsThumbedByUser(String username) {
         LOG.debug("Get all products thumbed by user:{}", username);
 
         final Iterator<DThumbs> dThumbsIterator = thumbsDao.queryByUsername(username).iterator();
 
         // Collect all unique product ids and get their products
         Set<String> productIds = new HashSet<String>();
         while (dThumbsIterator.hasNext())
             productIds.add(dThumbsIterator.next().getProductId());
 
         return productDao.queryByPrimaryKeys(null, productIds);
     }
 
     // Get all products a user have rated
     public Iterable<DProduct> getProductsRatedByUser(String username) {
         LOG.debug("Get all products rated by user:{}", username);
 
         final Iterator<DRating> dRatingIterator = ratingDao.queryByUsername(username).iterator();
 
         // Collect all unique product ids and get their products
         Set<String> productIds = new HashSet<String>();
         while (dRatingIterator.hasNext())
             productIds.add(dRatingIterator.next().getProductId());
 
         return productDao.queryByPrimaryKeys(null, productIds);
     }
 
     // Get all products a user have commented
     public Iterable<DProduct> getProductsCommentedByUser(String username) {
         LOG.debug("Get all products commented by user:{}", username);
 
         final Iterator<DComment> dCommentIterator = commentDao.queryByUsername(username).iterator();
 
         // Collect all unique product ids and get their products
         Set<String> productIds = new HashSet<String>();
         while (dCommentIterator.hasNext())
             productIds.add(dCommentIterator.next().getProductId());
 
         return productDao.queryByPrimaryKeys(null, productIds);
     }
 
 
     // Get all users favorite products
     public Iterable<DProduct> geUserFavoriteProducts(String username) {
         LOG.debug("Get favorite products for user:{}", username);
 
         final DFavorites dFavorites = favoritesDao.findByPrimaryKey(username);
 
         return productDao.queryByPrimaryKeys(null, dFavorites.getProductIds());
     }
 
 
     // Setters and getters
     public void setRatingDao(DRatingDao ratingDao) {
         this.ratingDao = ratingDao;
     }
 
     public void setProductDao(DProductDao productDao) {
         this.productDao = productDao;
     }
 
     public void setLikeDao(DLikeDao likeDao) {
         this.likeDao = likeDao;
     }
 
     public void setThumbsDao(DThumbsDao thumbsDao) {
         this.thumbsDao = thumbsDao;
     }
 
     public void setCommentDao(DCommentDao commentDao) {
         this.commentDao = commentDao;
     }
 
     public void setFavoritesDao(DFavoritesDao favoritesDao) {
         this.favoritesDao = favoritesDao;
     }
 
     public void setAppSettingsDao(DAppSettingsDao appSettingsDao) {
         this.appSettingsDao = appSettingsDao;
     }
 
     public boolean isTracking() {
         return tracking;
     }
 
     public void setTracking(boolean tracking) {
         this.tracking = tracking;
     }
 
 }
