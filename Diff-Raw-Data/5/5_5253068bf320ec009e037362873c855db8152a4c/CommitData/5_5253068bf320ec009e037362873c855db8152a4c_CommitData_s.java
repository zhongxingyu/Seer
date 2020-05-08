 package org.hackystat.dailyprojectdata.resource.commit;
 
 import java.util.List;
 
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 
 /**
  * The class wrapping SensorData instance, which provides easy access to the
  * commit specific properties.
  * @author aito
  * 
  */
 public class CommitData {
   /** The wrapped data instance. */
   private final SensorData data;
 
   /**
    * Constructs this object with the specified SensorData instance.
    * @param data the specified data instance.
    */
   public CommitData(SensorData data) {
     this.data = data;
   }
 
   /**
    * Returns the owner of the wrapped data instance.
    * @return the data owner.
    */
   public String getOwner() {
     return this.data.getOwner();
   }
 
   /**
    * Returns the total lines added stored in this data instance.
    * @return the total lines added.
    */
   public int getLinesAdded() {
     return Integer.valueOf(this.getCommitProperty("linesAdded").getValue());
   }
 
   /**
    * Returns the total lines deleted stored in this data instance.
    * @return the total lines deleted.
    */
   public int getLinesDeleted() {
     return Integer.valueOf(this.getCommitProperty("linesDeleted").getValue());
   }
 
   /**
    * Returns the total lines changed stored in this data instance.
    * @return the total lines changed.
    */
   public int getLinesChanged() {
     return Integer.valueOf(this.getCommitProperty("totalLines").getValue());
   }
 
   /**
    * Returns true if the specified object equals this object.
    * @param object the object to test.
    * @return true if equal, false if not.
    */
   public boolean equals(Object object) {
     if (this == object) {
       return true;
     }
     if (!(object instanceof CommitData)) {
       return false;
     }
 
     CommitData otherData = (CommitData) object;
     return this.data.equals(otherData.data);
   }
 
   /**
    * Returns the hashcode of this object.
    * @return the hashcode.
    */
   public int hashCode() {
     int result = 17;
     result = 37 * result + this.data.hashCode();
     return result;
   }
 
   /**
    * Returns the Property instance with the specified property name. If no
    * property exists, false is returned.
    * @param propertyName the property name to search for.
    * @return the property with the specified name or null.
    */
   public Property getCommitProperty(String propertyName) {
     List<Property> propertyList = this.data.getProperties().getProperty();
     for (Property property : propertyList) {
       if (propertyName.equals(property.getKey())) {
         return property;
       }
     }
     return null;
   }
 
   /**
    * Returns the string representation of this data object, which is useful for
    * debugging purposes.
    * @return the string representation.
    */
   public String toString() {
     return "Owner=" + this.getOwner() + ", LinesAdded=" + this.getLinesAdded()
         + ", LinesDeleted=" + this.getLinesDeleted() + ", LinesChanged="
         + this.getLinesChanged();
   }
 }
