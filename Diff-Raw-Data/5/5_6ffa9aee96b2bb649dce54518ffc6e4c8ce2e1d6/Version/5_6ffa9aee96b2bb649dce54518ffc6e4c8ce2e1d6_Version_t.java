 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import play.db.jpa.Model;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Version.
  */
 @Entity
 public class Version extends Model{
 	
     /** The platform. */
     @ManyToOne
     public Platform platform;
 
     /** The version. */
    public Double version;
 
     /**
         * Instantiates a new version.
         *
         * @param platform the platform
         * @param version the version
         */
    public Version(Platform platform, Double version) {
         this.platform = platform;
         this.version = version;
     }
         
     public String toString() {
         return version.toString();
     }
 }
