 package org.karmaexchange.dao;
 
 import lombok.EqualsAndHashCode;
 import lombok.ToString;
 
 import com.google.appengine.api.datastore.GeoPt;
 import com.googlecode.objectify.annotation.Embed;
 
 @Embed
@ToString
 @EqualsAndHashCode
 public class GeoPtWrapper {
 
   /// Though the AppEngine GeoPt doesn't provide any value now. It may provide value in the future
   // once full text search is integrated with the datastore. And we can't use GeoPt directly
   // because it doesn't have setters and getters for JAXB conversion.
   private GeoPt geoPt;
 
   public static GeoPtWrapper create(GeoPt geoPt) {
     GeoPtWrapper wrapper = new GeoPtWrapper();
     wrapper.geoPt = geoPt;
     return wrapper;
   }
 
  public float getLatitude() {
    return (geoPt == null) ? 0f : geoPt.getLatitude();
  }

  public float getLongitude() {
    return (geoPt == null) ? 0f : geoPt.getLongitude();
  }

   public void setLatitude(float latitude) {
    geoPt = new GeoPt(latitude, getLongitude());
   }
 
   public void setLongitude(float longitude) {
    geoPt = new GeoPt(getLatitude(), longitude);
   }
 }
