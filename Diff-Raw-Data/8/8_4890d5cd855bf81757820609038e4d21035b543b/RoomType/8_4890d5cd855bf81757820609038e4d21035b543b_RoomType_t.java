 package interiores.business.models;
 
 import interiores.business.models.catalogs.PersistentIdObject;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * This class represents a type of room, defined by a name, the type of furniture it should have,
  * and the type furniture it can't have
  * @author alvaro
  */
 @XmlRootElement
 public class RoomType
     extends PersistentIdObject
 {    
     /**
      * Set containing all the FurnitureTypes that must be in this type of room
      */
     @XmlElementWrapper
     private HashSet<String> mustHave;
     
     /**
      * Set containing all the FurnitureTypes that can't be in this type
      */
     @XmlElementWrapper
     private HashSet<String> cantHave;
     
     public RoomType() {
         this(null);
     }
     
     public RoomType(String name) {
         this(name, new ArrayList(), new ArrayList());
     }
     
     public RoomType(String name, Collection<FurnitureType> mustHave, Collection<FurnitureType> cantHave) {
         super(name);
         this.mustHave = new HashSet();
         this.cantHave = new HashSet();
         
         for(FurnitureType fType : mustHave)
             this.mustHave.add(fType.getId());
         
         for(FurnitureType fType : cantHave)
             this.cantHave.add(fType.getId());
     }
     
     public String getName() {
         return identifier;        
     }
     
     public HashSet<String> getMandatory() {
         return mustHave;
     }
     
     public void addToMandatory(FurnitureType fType) {
         mustHave.add(fType.getId());
     }
     
     public void removeFromMandatory(FurnitureType fType) {
        mustHave.remove(fType.getId());
     }
     
     public void removeFromMandatory(String fTypename) {
         cantHave.remove(fTypename);
     }
     
     public HashSet<String> getForbidden() {
         return cantHave;
     }
     
     public void addToForbidden(FurnitureType fType) {
         cantHave.add(fType.getId());
     }
     
     public void removeFromForbidden(FurnitureType fType) {
         cantHave.remove(fType.getId());
     }
     
     public void removeFromForbidden(String fTypename) {
         cantHave.remove(fTypename);
     }
     
     // should these methods go here or maybe in the controller
     public boolean isMandatory(FurnitureType ftype) {
         return mustHave.contains(ftype.getId());
     }
     
     public boolean isForbidden(FurnitureType ftype) {
         return cantHave.contains(ftype.getId());
     }
     
     @Override
     public String toString() {
         return identifier;
     }
 }
