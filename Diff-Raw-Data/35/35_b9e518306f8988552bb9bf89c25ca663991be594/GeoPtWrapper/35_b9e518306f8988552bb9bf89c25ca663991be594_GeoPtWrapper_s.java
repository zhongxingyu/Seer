 package org.karmaexchange.dao;
 
 import lombok.EqualsAndHashCode;
import lombok.Getter;
 import lombok.ToString;
 
 import com.google.appengine.api.datastore.GeoPt;
 import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Ignore;
 
 @Embed
@ToString(exclude={"latitude", "longitude"})
 @EqualsAndHashCode
 public class GeoPtWrapper {
 
   /// Though the AppEngine GeoPt doesn't provide any value now. It may provide value in the future
   // once full text search is integrated with the datastore. And we can't use GeoPt directly
   // because it doesn't have setters and getters for JAXB conversion.
   private GeoPt geoPt;
 
  @Ignore
  @Getter
  private float latitude;

  @Ignore
  @Getter
  private float longitude;

   public static GeoPtWrapper create(GeoPt geoPt) {
     GeoPtWrapper wrapper = new GeoPtWrapper();
     wrapper.geoPt = geoPt;
    wrapper.latitude = geoPt.getLatitude();
    wrapper.longitude = geoPt.getLongitude();
     return wrapper;
   }
 
   public void setLatitude(float latitude) {
    geoPt = new GeoPt(latitude, longitude);
    this.latitude = latitude;
   }
 
   public void setLongitude(float longitude) {
    geoPt = new GeoPt(latitude, longitude);
    this.longitude = longitude;
   }
 }
