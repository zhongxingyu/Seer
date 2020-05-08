 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import gameObjects.Item;
 import gameObjects.Recipe;
 import gameObjects.Workshop;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This is similar to ItemPrototypes, but for a workshop and its recipes
  * */
 public class WorkshopListing {
     List<Workshop> workshops = new ArrayList<Workshop>();
 
     public WorkshopListing() {
         List<FileHandle> workshopFiles = readWorkshopListing();
 
         for (FileHandle file : workshopFiles) {
             workshops.add(readWorkshopFile(file));
         }
 
         Gdx.app.log("workshops created", workshops.toString());
     }
 
     private List<FileHandle> readWorkshopListing() {
         List<FileHandle> shops = new ArrayList<FileHandle>();
         XmlReader reader = new XmlReader();
         XmlReader.Element parentElement;
 
         try {
         parentElement = reader.parse(Gdx.files.internal("workshops.xml"));
         } catch (IOException e) {
             Gdx.app.error("workshopListing", "Error reading listing of workshops in workshops.xml", e);
             return null;
         }
 
 
         Array<XmlReader.Element> shopListXml = parentElement.getChildrenByName("workshopFile");
 
         for (XmlReader.Element shopXml : shopListXml) {
             shops.add(Gdx.files.internal(shopXml.getText()));
         }
 
         return shops;
     }
 
     private Workshop readWorkshopFile(FileHandle fileHandle) {
         XmlReader reader = new XmlReader();
         XmlReader.Element parentElement;
 
         try {
             parentElement = reader.parse(fileHandle);
         } catch (IOException e) {
             Gdx.app.error("workshopListing", "Error reading workshop:" + fileHandle.name(), e);
             return null;
         }
 
         List<Recipe> recipesForThisWorkshop = new ArrayList<Recipe>();
 
         String workshopName = parentElement.get("name");
         Array<XmlReader.Element> recipeListXml = parentElement.getChildByName("recipes").getChildrenByName("recipe");
 
         for (XmlReader.Element recipeXml : recipeListXml) {
             recipesForThisWorkshop.add(parseRecipe(recipeXml));
         }
 
         return new Workshop(workshopName, recipesForThisWorkshop);
     }
 
     private Recipe parseRecipe(XmlReader.Element recipeXml) {
 
         String outputName = recipeXml.get("output");
         Item output = Tradesong.itemPrototypes.get(outputName);
 
         List<Item> ingredients = new ArrayList<Item>();
        Array<XmlReader.Element> ingredientListXml = recipeXml.getChildrenByName("ingredients");
 
         for (XmlReader.Element ingredientXml : ingredientListXml) {
             ingredients.add(parseIngredient(ingredientXml));
         }
 
 
         Integer craftTime = 2; // TODO make this default but also try to read it.
         return new Recipe(output, ingredients, craftTime);
     }
 
     private Item parseIngredient(XmlReader.Element ingredientXml) {
        String ingredientName = ingredientXml.get("ingredient");
         return Tradesong.itemPrototypes.get(ingredientName);
     }
 
     public Workshop getWorkshop(String name) {
         for (Workshop workshop : workshops) {
             if (workshop.getName().equalsIgnoreCase(name)) {
                 return workshop;
             }
         }
         return null;
     }
 }
