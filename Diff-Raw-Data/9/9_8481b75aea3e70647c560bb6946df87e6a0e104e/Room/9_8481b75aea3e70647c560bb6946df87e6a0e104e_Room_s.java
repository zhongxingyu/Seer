 package interiores.business.models;
 
 import interiores.core.business.BusinessException;
 import interiores.utils.Dimension;
 import java.util.Collection;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Represents a room.
  * A room belongs to a certain type and is of a determined size
  * @author hector
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class Room
 {
     private static final int RESOLUTION = 5;
     
     @XmlElement
     private RoomType type;
     
     @XmlElement
     private Dimension size;
     
     @XmlElement
     private RoomDesign design;
     
     public Room() {
         
     }
     
     /**
      * Creates a new room
      * @param type The type of room this one belongs to
      * @param size The size of this room
      * @throws BusinessException 
      */
     public Room(RoomType type, Dimension size)
     {
         size.width -= size.width % RESOLUTION;
         size.depth -= size.depth % RESOLUTION;
         
         if(! type.isSizeValid(size))
             throw new BusinessException("The room you are trying to create is not between the permitted "
                     + "dimension range. Width[" + type.getWidthRange() + "], Depth[" + type.getDepthRange()
                     + "]");
         
         this.type = type;
         this.size = size;
        design = null;
     }
     
     /**
      * Creates a new room
      * @param type The type of room this one belongs to
      * @param width The width of this room
      * @param depth The depth of this room
      * @throws BusinessException 
      */
     public Room(RoomType type, int width, int depth)
             throws BusinessException
     {
         this(type, new Dimension(width, depth));
     }
     
     public static int getResolution() {
         return RESOLUTION;
     }
     
     /**
      * Gets the type of the room
      * @return RoomType of this room
      */
     public RoomType getType() {
         return type;
     }
     
     public String getTypeName() {
         return type.getName();
     }
     
     public Collection<String> getMandatoryFurniture() {
         return type.getMandatory();
     }
     
     public Collection<String> getForbiddenFurniture() {
         return type.getForbidden();
     }
     
     public boolean isMandatory(String typeName) {
         return type.isMandatory(typeName);
     }
     
     public boolean isForbidden(String typeName) {
         return type.isForbidden(typeName);
     }
     
     /**
      * Gets the dimension or size of the room
      * @return Dimension object representing the size of this room
      */
     public Dimension getDimension() {
         return size;
     }
     
     public int getWidth() {
         return size.width;
     }
     
     public int getDepth() {
         return size.depth;
     }
     
     public RoomDesign getDesign() {
         return design;
     }
     
     public void setDesign(RoomDesign design) {
         this.design = design;
     }
 }
