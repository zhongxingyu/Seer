 /**
  * @author Michael Zhou (Dominator008), Siyang Chen
  */
 package io;
 
 import io.annotations.Modifiable;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import leveleditor.DecoratorSelector;
 import leveleditor.VoogaUtilities;
 import core.characters.GameElement;
 import core.physicsengine.physicsplugin.PhysicsAttributes;
 import core.tiles.Tile;
 import core.tiles.TileDecorator;
 
 public class SpriteWrapper implements Cloneable, Serializable {
 
     private static final long serialVersionUID = 8194097148022115858L;
 
     public static enum SpriteGroupIdentifier {
 	PLAYER, CHARACTER, SETTING, ITEM
     }
 
     private String myName;
     private SpriteGroupIdentifier myGroup;
    // private Class<?> myClass;
     private String myImagesrc;
     private Map<SpriteAttribute, Serializable> myPhysicsAttributeMap;
     private Map<SpriteAttribute, Serializable> myGamePlayAttributeMapForMap;
     private Map<SpriteAttribute, Serializable> myGamePlayAttributeMapForIndividual;
     private transient Field myDummyAttributeMapForMap;
     private transient Set<Field> myDummyIndividualFields;
     private GameElement myGameElement;
 
    /* public SpriteWrapper(String name, SpriteGroupIdentifier group,
 	    Class<?> clazz, Map<SpriteAttribute, Serializable> attributes,
 	    String imgsrc) {
 	System.out.println(attributes);
 	myName = name;
 	myGroup = group;
 	myClass = clazz;
 	myImagesrc = imgsrc;
 	myGameElement = new GameElement();
 	myPhysicsAttributeMap = new HashMap<SpriteAttribute, Serializable>(
 		attributes);
 	buildAttributeMap(clazz, null);
 	reconstruct();
     }*/
 
     public SpriteWrapper(String name, SpriteGroupIdentifier group, String imgsrc,
 	    GameElement sprite) {
 	myName = name;
 	myGroup = group;
 	//myClass = sprite.getClass();
 	myImagesrc = imgsrc;
 	try {
 	    sprite.getClass().getConstructor(PhysicsAttributes.class);
 		myGameElement = sprite.getClass().getConstructor(PhysicsAttributes.class)
 	    	.newInstance(sprite.getPhysicsAttribute());
 	
 	} catch (InstantiationException e) {
 	    e.printStackTrace();
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	} catch (NoSuchMethodException e) {
 		Tile t = (TileDecorator) sprite;
 		Set<Class<?>> decorators = new HashSet<Class<?>>();
 		while(t.getClass().equals(Tile.class)) {
 		    decorators.add(t.getClass());
 		    t = t.removeDecorator();
 		}
 		myGameElement = 
 			DecoratorSelector.getDecoratedSprite(decorators, Tile.class, 
 				sprite.getPhysicsAttribute());
 		
 	} catch (SecurityException e) {
 	    e.printStackTrace();
 	} catch (InvocationTargetException e) {
 	    e.printStackTrace();
 	} 
 	Map<SpriteAttribute, Serializable> realmap
 	= SpriteAttribute.convertkeyToAttribute(sprite.getPhysicsAttribute().getPhysicsAttrMap(),
 		"Physics");
 	myPhysicsAttributeMap = new HashMap<SpriteAttribute, Serializable>(realmap);
 	myGamePlayAttributeMapForMap = new HashMap<SpriteAttribute, Serializable>();
 	myGamePlayAttributeMapForIndividual = new HashMap<SpriteAttribute, Serializable>();
 	myDummyIndividualFields = new HashSet<Field>();
 	buildMapAttributeMap();
 	buildIndividualAttributeMap();
 	reconstruct();
     }
     
     @SuppressWarnings("unchecked")
     private void buildMapAttributeMap() {
 	Class<?> curr = myGameElement.getClass();
 	while(true) {
 	    for (Field f: curr.getDeclaredFields()) {
 		f.setAccessible(true);
 		if (f.isAnnotationPresent(Modifiable.class)) {
 		    Modifiable annotation = f.getAnnotation(Modifiable.class);
 		    if (!annotation.type().equals("Map"))
 			return;
 		    Map<SpriteAttribute, Serializable> attrmap = null;
 		    myDummyAttributeMapForMap = f;
 		    try {
 			Map<String, Serializable> dummymap = 
 				(Map<String, Serializable>) f.get(myGameElement);
 			attrmap = SpriteAttribute.convertkeyToAttribute(dummymap, 
 				annotation.classification());
 		    } catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		    } catch (IllegalAccessException e) {
 			e.printStackTrace();
 		    }
 		    myGamePlayAttributeMapForMap.putAll(attrmap);
 		    return;
 		}
 	    }
 	    if (curr.equals(GameElement.class))
 		return;
 	    curr = curr.getSuperclass();
 	}
     }
     
     private void buildIndividualAttributeMap() {
 	Class<?> curr = myGameElement.getClass();
 	while (true) {
 	    checkAndAddIndividual(curr);
 	    if (curr.equals(GameElement.class))
 		return;
 	    curr = curr.getSuperclass();
 	}	
     }
     
     private void checkAndAddIndividual(Class<?> curr) {
 	    for (Field f : curr.getDeclaredFields()) {
 		f.setAccessible(true);
 		if (f.isAnnotationPresent(Modifiable.class)) {
 		    if (!f.getAnnotation(Modifiable.class).type().equals("Individual"))
 			return;
 		    String fname = f.getName();
 		    Class<?> ftype = f.getType();
 		    String fclassification = ((Modifiable) f
 			    .getAnnotation(Modifiable.class)).classification();
 		    myDummyIndividualFields.add(f);
 		    try {
 			myGamePlayAttributeMapForIndividual.put(new SpriteAttribute(
 				fname, ftype, fclassification),
 				(Serializable) f.get(myGameElement));
 		    } catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		    } catch (IllegalAccessException e) {
 			e.printStackTrace();
 		    }
 		}
 	    }
     }
 
     //TODO
     /*public void addAdditionalAttributes(Set<Class<?>> additionalclasses) {
 	for (Class<?> clazz: additionalclasses) {
 	    this.checkAndAddIndividual(clazz);
 	}
     }*/
     
     public SpriteWrapper clone() {
 	SpriteWrapper cloned = new SpriteWrapper(myName, myGroup, myImagesrc, myGameElement);
 	cloned.myGamePlayAttributeMapForMap = new HashMap<SpriteAttribute, Serializable>(
 		myGamePlayAttributeMapForMap);
 	cloned.myGamePlayAttributeMapForIndividual = new HashMap<SpriteAttribute, Serializable>(
 		myGamePlayAttributeMapForIndividual);
	cloned.myPhysicsAttributeMap 
	= new HashMap<SpriteAttribute, Serializable>(myPhysicsAttributeMap);
 	return cloned;
     }
 
     public void updateAttributeMap(SpriteAttribute sa, Serializable o) {
 	if (myGamePlayAttributeMapForMap.containsKey(sa))
 	    myGamePlayAttributeMapForMap.put(sa, o);
 	if (myGamePlayAttributeMapForIndividual.containsKey(sa))
 	    myGamePlayAttributeMapForIndividual.put(sa, o);
 	if (myPhysicsAttributeMap.containsKey(sa))
 	    myPhysicsAttributeMap.put(sa, o);
     }
 
     /*public Class<?> getSpriteClass() {
 	return myClass;
     }*/
 
     public String getName() {
 	return myName;
     }
 
     public String getImageSrc() {
 	return myImagesrc;
     }
 
     public SpriteGroupIdentifier getGroup() {
 	return myGroup;
     }
 
     public GameElement getGameElement() {
 	return myGameElement;
     }
 
     public Map<SpriteAttribute, Serializable> getAttributeMapForMap() {
 	return Collections.unmodifiableMap(myGamePlayAttributeMapForMap);
     }
     
     public Map<SpriteAttribute, Serializable> getAttributeMapForIndividual() {
 	return Collections.unmodifiableMap(myGamePlayAttributeMapForIndividual);
     }
 
     public Map<SpriteAttribute, Serializable> getPhysicsAttributeMap() {
 	return Collections.unmodifiableMap(myPhysicsAttributeMap);
     }
     
     public Map<SpriteAttribute, Serializable> getMergedAttributeMap() {
 	Map<SpriteAttribute, Serializable> mergedmap = new HashMap<SpriteAttribute, Serializable>();
 	mergedmap.putAll(myPhysicsAttributeMap);
 	mergedmap.putAll(myGamePlayAttributeMapForMap);
 	mergedmap.putAll(myGamePlayAttributeMapForIndividual);
 	return Collections.unmodifiableMap(mergedmap);
     }
     
     private void reconstruct() {
 	BufferedImage[] dummyimages = new BufferedImage[1];
 	dummyimages[0] = VoogaUtilities.getImage(myImagesrc);
 	myGameElement.setImages(dummyimages);
     }
     
     private void readObject(ObjectInputStream stream) 
 	    throws ClassNotFoundException, IOException {
 	stream.defaultReadObject();
 	reconstruct();
     }
 
     public void saveAttributes() {
 	Map<String, Serializable> dummymapphysics = 
 		SpriteAttribute.convertkeyToString(myPhysicsAttributeMap);
 	myGameElement.setPhysicsAttribute(new PhysicsAttributes(dummymapphysics));
 	try {
 	    for (SpriteAttribute sa: myGamePlayAttributeMapForIndividual.keySet())
 		for (Field f: myDummyIndividualFields)
 		    if (f.getName().equals(sa.getName()))
 			f.set(myGameElement, myGamePlayAttributeMapForIndividual.get(sa));
 	    //System.out.println(myGamePlayAttributeMapForMap);
 	    if (!myGamePlayAttributeMapForMap.isEmpty()) {
 		Map<String, Serializable> dummymapgameplay = 
 			SpriteAttribute.convertkeyToString(myGamePlayAttributeMapForMap);
 		myDummyAttributeMapForMap.set(myGameElement, dummymapgameplay);
 	    }
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	}
     }
     
     public void save(String path) {
 	VoogaUtilities.serialize(this, new File(path));
     }
     
     public static SpriteWrapper load(File file) {
 	return (SpriteWrapper) VoogaUtilities.deserialize(file);
     }
     
 }
