 package edu.umich.sbolt.world;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.umich.sbolt.InputLinkHandler;
 
 import abolt.lcmtypes.categorized_data_t;
 import abolt.lcmtypes.category_t;
 import abolt.lcmtypes.object_data_t;
 import sml.Identifier;
 import sml.IntElement;
 import sml.StringElement;
 import sml.WMElement;
 
 /**
  * A single Object in the world, can be created using either a sensable or an object_data_t
  * 
  * @author mininger
  * 
  */
 public class WorldObject implements IInputLinkElement
 {
     public static String getSensableId(String sensable){
         // Matches against id=ID followed by a comma, whitespace, or end of string
         // Assumes ID consists of numbers
         sensable = sensable.toLowerCase();
         Pattern p = Pattern.compile("id=(\\p{Digit})+(,|\\s|\\Z)");
         Matcher m = p.matcher(sensable);
         if(!m.find()){
             return null;
         }
         // m.group() returns a string like "id=ID,"
         // we trim, then split to get the actual ID
         String[] id = m.group().trim().split("(id=)|,");
         if(id.length < 2){
             return null;
         }
         //Note that the first element will be the empty string, we want the second
         return id[1];
     }
     
     
     // Root identifier for the object
     protected Identifier objectId;
     
     // Name of the object (may be null if not named)
     protected String name;
     protected StringElement nameWME;
     
     // Id of the object 
     protected Integer id;
     protected IntElement idWME;
     
     // Pose of the object
     protected Pose pose;
     
     // Bounding box of the object
     protected BBox bbox;
     
     protected Map<String, VisualProperty> categories;
     
     protected Map<String, Property> properties;
 
      // svs added
     protected boolean isNew;
     protected boolean hasChanged;
     
     public WorldObject(String sensable){
         initMembers();
         newSensableString(sensable);
     }
     
     public WorldObject(object_data_t object){
         initMembers();
         newObjectData(object);
     }
     
     private void initMembers(){
         objectId = null;
         idWME = null;
         nameWME = null;
         
         name = null;
         id = -1;
         pose = new Pose();
         bbox = new BBox();
         properties = new HashMap<String, Property>();
         categories = new HashMap<String, VisualProperty>();
         isNew = true;
     }
     
     // Accessors
     public String getName(){
         return name;
     }
     
     public Integer getId(){
         return id;
     }
     
     public Pose getPose(){
         return pose;
     }
     
     public BBox getBBox(){
     	return bbox;
     }
     
     public synchronized String getValue(String attribute){
         return properties.get(attribute).getValue();
     }
     
 
     // Mutators
 
     @Override
     public synchronized void updateInputLink(Identifier parentIdentifier)
     {
         if(objectId == null){
             objectId = parentIdentifier.CreateIdWME("object");
             idWME = objectId.CreateIntWME("id", id);
         } 
         if(name != null){
         	if(nameWME == null){
         		nameWME = objectId.CreateStringWME("name", name);
         	}
         	if(!nameWME.GetValueAsString().equals(name)){
         		nameWME.Update(name);
         	}
         }
         
         for(VisualProperty category : categories.values()){
         	category.updateInputLink(objectId);
         }
         
         pose.updateInputLink(objectId);
         bbox.updateInputLink(objectId);
         
         for(Property prop : properties.values()){
         	prop.updateInputLink(objectId);
         }
     }
 
     @Override
     public synchronized void destroy()
     {
         if(objectId != null){
         	for(VisualProperty category : categories.values()){
         		category.destroy();
         	}
         	pose.destroy();
         	bbox.destroy();
         	idWME.DestroyWME();
         	idWME = null;
        	if(nameWME != null){
            	nameWME.DestroyWME();
            	nameWME = null;
        	}
             objectId.DestroyWME();
             objectId = null;
         }
     }
     
     public synchronized void newSensableString(String sensable){
         sensable = sensable.toLowerCase();
         String[] keyValPairs = sensable.split(",");
         
         for (String keyValPair : keyValPairs)
         {
             String[] keyVal = keyValPair.split("=");
             if (keyVal.length < 2)
             {
                 // Note a valid key-val pair, must be "key=value"
                 continue;
             }
 
             if (keyVal[0].equals("id"))
             {
                 id = Integer.parseInt(keyVal[1]);
             } else if(keyVal[0].equals("pose")){
                 if (!pose.equals(keyVal[1]))
                 {
                     hasChanged = true;
                 }
                 
                 pose.updateWithString(keyVal[1]);
                 continue;
             } else if(keyVal[0].equals("name")){
             	name = keyVal[1].toLowerCase();
             } else if(keyVal[0].equals("bbox")){
             	bbox.updateWithString(keyVal[1]);
             	continue;
             } else {
             	categorized_data_t category = new categorized_data_t();
             	String categoryName = keyVal[0].toLowerCase();
             	category.cat = new category_t();
             	Integer catType = VisualProperty.getCategoryType(categoryName);
             	if(catType == null){
             		updateProperty(keyVal[0], keyVal[1]);
             		continue;
             	}
             	category.cat.cat = catType;
             	category.len = 1;
             	category.label = new String[1];
             	category.label[0] = keyVal[1].toLowerCase();
             	category.confidence = new double[1];
             	category.confidence[0] = 1;
             	if(categories.containsKey(categoryName)){
             		categories.get(categoryName).updateCategoryInfo(category);
             	} else {
             		categories.put(categoryName, new VisualProperty(category));
             	}
             }        
         }
     }
     
     public void updateProperty(String name, String value){
     	name = name.toLowerCase();
     	value = value.toLowerCase();
     	if(properties.containsKey(name)){
     		properties.get(name).update(value);
     	} else {
     		properties.put(name, new Property("state", name, value));
     	}
     }
 
     public synchronized void newObjectData(object_data_t objectData){        
         id = objectData.id;
         
         //used for svs
         if (!pose.equals(objectData.pos))
            hasChanged = true;
         pose.updateWithArray(objectData.pos);
         bbox.updateWithArray(objectData.bbox);
         
         for(categorized_data_t category : objectData.cat_dat){
         	String categoryName = VisualProperty.getCategoryName(category.cat.cat);
         	if(categories.containsKey(categoryName)){
         		categories.get(categoryName).updateCategoryInfo(category);
         	} else {
         		categories.put(categoryName, new VisualProperty(category));
         	}
         }
     }
 }
