 package org.netvogue.ecommerce.persistence.mongo.converters;
 
 import org.netvogue.ecommerce.domain.model.Address;
 import org.netvogue.ecommerce.domain.model.Order;
 import org.netvogue.ecommerce.domain.model.OrderLineItem;
 import org.netvogue.ecommerce.domain.model.OrderReview;
 import org.netvogue.ecommerce.domain.model.OrderStatus;
 import org.netvogue.ecommerce.domain.model.OrderTracking;
 import org.netvogue.ecommerce.domain.model.PaymentMethod;
 import org.netvogue.ecommerce.domain.model.Style;
 import org.netvogue.ecommerce.domain.model.StyleSize;
 import org.netvogue.ecommerce.persistence.UserDao;
 import org.springframework.core.convert.converter.Converter;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class OrderReadConverter implements Converter<DBObject, Order> {
 
   private UserDao userDao;
 
   public Order convert(final DBObject source) {
 
     Order order = new Order();
     order.setId(source.get("_id").toString());
     order.setOrderCompletionDate((Date) source.get("orderCompletionDate"));
     order.setOrdereCreatedDate((Date) source.get("orderedCreatedDate"));
     order.setOrderTotal((Long) source.get("orderTotal"));
     order.setResolutionComments((String) source.get("resolutionComments"));
     order.setPaymentMethod(PaymentMethod.valueOf((String) source.get("paymentMethod")));
     order.setBrand(userDao.getUser((String) source.get("brand")));
     order.setCreatedBy(userDao.getUser((String) source.get("createdBy")));
 
     BasicDBList originalLineItemsList = (BasicDBList) source.get("originallLineItems");
 
     Set<OrderLineItem> orderLineItems = new HashSet<OrderLineItem>();
 
     for (Object obj : originalLineItemsList) {
       BasicDBObject dbObj = (BasicDBObject) obj;
       OrderLineItem lineItem = new OrderLineItem();
       lineItem.setLineItemId((String) dbObj.get("lineItemId"));
       lineItem.setLineItemPrice((Long) dbObj.get("lineItemPrice"));
       lineItem.setQuantity((Integer) dbObj.get("quantity"));
       lineItem.setStylePrice((Long) dbObj.get("stylePrice"));
       lineItem.setStyleSize(StyleSize.valueOf((String) dbObj.get("styleSize")));
       BasicDBObject dbStyle = (BasicDBObject) dbObj.get("style");
       Style style = new StyleReadConverter().convert(dbStyle);
       lineItem.setStyle(style);
       orderLineItems.add(lineItem);
     }
 
     order.setOriginalLineItems(orderLineItems);
 
    source.get("finalizedLineItemsAfterReview");
 
     Set<OrderLineItem> finalLineItems = new HashSet<OrderLineItem>();
 
    for (Object obj : originalLineItemsList) {
       BasicDBObject dbObj = (BasicDBObject) obj;
       OrderLineItem lineItem = new OrderLineItem();
       lineItem.setLineItemId((String) dbObj.get("lineItemId"));
       lineItem.setLineItemPrice((Long) dbObj.get("lineItemPrice"));
       lineItem.setQuantity((Integer) dbObj.get("quantity"));
       lineItem.setStylePrice((Long) dbObj.get("stylePrice"));
       lineItem.setStyleSize(StyleSize.valueOf((String) dbObj.get("styleSize")));
       BasicDBObject dbStyle = (BasicDBObject) dbObj.get("style");
       Style style = new StyleReadConverter().convert(dbStyle);
       lineItem.setStyle(style);
       finalLineItems.add(lineItem);
     }
 
     order.setFinalizedLineItemsAfterReview(finalLineItems);
 
     order.setShippingAddress(toAddress((DBObject) source.get("shippingAddress")));
     order.setBillingAddress(toAddress((DBObject) source.get("billinggAddress")));
     order.setBrandAddress(toAddress((DBObject) source.get("brandAddress")));
 
     OrderTracking tracking = new OrderTracking();
     BasicDBObject dbtracking = (BasicDBObject) source.get("orderTracking");
     tracking.setCompany(dbtracking.getString("company"));
     tracking.setOrderTrackingId(dbtracking.getString("trackingId"));
     tracking.setStatus(OrderStatus.valueOf(dbtracking.getString("status")));
 
     order.setOrderTracking(tracking);
 
     BasicDBList dbReviews = (BasicDBList) source.get("reviews");
 
     List<OrderReview> reviewsList = new ArrayList<OrderReview>();
 
     for (Object obj : dbReviews) {
 
       BasicDBObject dbReview = (BasicDBObject) obj;
       OrderReview review = new OrderReview();
       review.setComments(dbReview.getString("comments"));
       review.setReviewedBy(userDao.getUser(dbReview.getString("reviewedBy")));
       review.setReviewedDate((Date) dbReview.get("reviewedBy"));
       Set<OrderLineItem> lineItems = new HashSet<OrderLineItem>();
       BasicDBList dbReviewLineItems = (BasicDBList) dbReview.get("lineItems");
 
       for (Object lineItemObj : dbReviewLineItems) {
         DBObject dbLineItem = (DBObject) lineItemObj;
         OrderLineItem lineItem = new OrderLineItem();
         lineItem.setLineItemId((String) dbLineItem.get("lineItemId"));
         lineItem.setLineItemPrice((Long) dbLineItem.get("lineItemPrice"));
         lineItem.setQuantity((Integer) dbLineItem.get("quantity"));
         lineItem.setStylePrice((Long) dbLineItem.get("stylePrice"));
         lineItem.setStyleSize(StyleSize.valueOf((String) dbLineItem.get("styleSize")));
         BasicDBObject dbStyle = (BasicDBObject) dbLineItem.get("style");
         Style style = new StyleReadConverter().convert(dbStyle);
         lineItem.setStyle(style);
         lineItems.add(lineItem);
       }
 
       review.setLineItems(lineItems);
       reviewsList.add(review);
     }
 
     order.setReviews(reviewsList);
     return order;
   }
 
   public UserDao getUserDao() {
     return userDao;
   }
 
   public void setUserDao(final UserDao userDao) {
     this.userDao = userDao;
   }
 
   private Address toAddress(final DBObject dbObject) {
     Address address = new Address();
     address.setFirstName((String) dbObject.get("firstName"));
     address.setLastName((String) dbObject.get("lastName"));
     address.setEmailId((String) dbObject.get("emailId"));
     address.setAddress((String) dbObject.get("address"));
     address.setCity((String) dbObject.get("city"));
     address.setState((String) dbObject.get("state"));
     address.setZipCode((Integer) dbObject.get("zipCode"));
     address.setContactNumber((Long) dbObject.get("contactNumber"));
     return address;
   }
 
 }
