 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import com.icbat.game.tradesong.assetReferences.TextureAssets;
 
 import java.io.IOException;
 import java.util.HashSet;
 
 /**
  * Set of all spawnable/existing items for quick reference. Get returns a copy of the Item called.
  * */
 public class ItemPrototypes {
    public static final int SPRITE_DIMENSION = 34;
     private HashSet<Item> prototypes = new HashSet<Item>();
 
     /**
      * Reads in the data from the appropriate asset file and creates items.
      * */
     public ItemPrototypes () {
         XmlReader reader = new XmlReader();
         XmlReader.Element parentElement;
         try {
             parentElement = reader.parse(Gdx.files.internal("items.xml"));
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         Array<XmlReader.Element> items = parentElement.getChildrenByName("item");
 
         for (XmlReader.Element item : items) {
             prototypes.add(parseItemFromXml(item));
         }
     }
 
     private Item parseItemFromXml(XmlReader.Element itemXml) {
         String name = itemXml.get("name", "");
         String description = itemXml.get("description", "");
 
         Texture itemSpritesheet = Tradesong.getTexture(TextureAssets.ITEMS);
         Integer spriteX = itemXml.getInt("spriteX", 0);
         Integer spriteY = itemXml.getInt("spriteY", 0);
        Gdx.app.debug("found X as", spriteX*SPRITE_DIMENSION + "");
        Gdx.app.debug("found Y as", spriteY*SPRITE_DIMENSION + "");
         TextureRegion icon = new TextureRegion(itemSpritesheet, spriteX * SPRITE_DIMENSION, spriteY * SPRITE_DIMENSION, SPRITE_DIMENSION, SPRITE_DIMENSION);
 
         Integer basePrice = itemXml.getInt("basePrice", 0);
 
         return new Item(name, description, icon, basePrice);
     }
 
     /**
      * Returns a copy of the item, or null if the name can't be found
      * */
     public Item get(String itemName) {
         for (Item item : prototypes) {
             if (item.getName().equalsIgnoreCase(itemName)) {
                 return new Item(item);
             }
         }
         return null;
     }
 }
