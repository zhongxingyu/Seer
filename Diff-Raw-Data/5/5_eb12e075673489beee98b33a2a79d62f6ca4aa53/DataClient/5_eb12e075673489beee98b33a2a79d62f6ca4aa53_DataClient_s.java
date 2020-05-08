 package com.openfridge;
 //slightly more
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Scanner;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.SystemClock;
 import android.util.Log;
 
 /**
  * A class which facilitates communication with the server, allowing the
  * retrieving and updating of fridge food items on the database server.
  * 
  * @author Tom, Shimona, Jesse
  */
 
 public class DataClient extends Observable {
     private static final int RED = Color.parseColor("#ED1C24");
     private static final int YELLOW = Color.parseColor("#FFD300");
     private static final int GREEN = Color.parseColor("#228B22");
     // ArrayLists for the data from the XML
     private Map<ExpState, List<FridgeFood>> foods = new HashMap<ExpState, List<FridgeFood>>();
     private List<ShoppingItem> shoppingList = new ArrayList<ShoppingItem>();
     // Parsing stuff
     private XMLReader xr;
     private SAXParser sp;
     private SAXParserFactory spf;
     private URL fridgeFoodURL, shoppingItemURL;
     // Update notification stuff
     private GetDataAsyncTask getDataTask;
     private long lastRunTime = 0;
     private long nextRunTime = 0;
     {
         for (ExpState key : ExpState.values()) {
             foods.put(key, new ArrayList<FridgeFood>());
         }
         /* Create the URLs we want to load xml-data from. */
         /*
          * If you check them, you'll see they are mini-xml documents generated
          * from elvin's database.
          */
         try {
             fridgeFoodURL = new URL(
                     "http://openfridge.heroku.com/fridge_foods.xml");
             shoppingItemURL = new URL(
                     "http://openfridge.heroku.com/shopping_lists.xml");
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
 
         /* Get a SAXParser from the SAXPArserFactory. */
         spf = SAXParserFactory.newInstance();
         try {
             /* Get the XMLReader of the SAXParser we created. */
             sp = spf.newSAXParser();
             xr = sp.getXMLReader();
         } catch (ParserConfigurationException e) {
         } catch (SAXException e) {
         }
     }
 
     private DataClient() {
     }
 
     private class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
         @Override
         protected Void doInBackground(Void... arg0) {
             reloadFridgeFoods();
             reloadShoppingItems();
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             getDataTask = null;
             setChanged();
             notifyObservers();
         }
     }
 
     public void reloadFoods() {
         long now = SystemClock.uptimeMillis();
         if (getDataTask == null && (now > nextRunTime || now < lastRunTime)) {
             // 2nd check is in case the clock's been reset.
             lastRunTime = SystemClock.uptimeMillis();
             nextRunTime = lastRunTime + 1000 * 5; // 5 seconds
             getDataTask = new GetDataAsyncTask();
             getDataTask.execute();
         }
     }
 
     // FridgeFood routes
     // -----------------
 
     /**
      * Post an item of food to the cloud database. If the id of the given food
      * exists already, the existing record will be updated in the database.
      * 
      * @param food
      *            The FridgeFood object with the fields to be posted to
      *            database.
      * @throws IOException
      */
 
     public int pushFridgeFood(FridgeFood food) throws IOException {
         URL url = new URL(
                 String.format(
                         "http://openfridge.heroku.com/fridge_foods/push/%d/%s/%d/%d/%d",
                         food.getUserId(),
                         URLEncoder.encode(food.getDescription(), "UTF-8"),
                         food.getExpirationYear(), food.getExpirationMonth(),
                         food.getExpirationDay()));
         
         Scanner scan = new Scanner((new DataInputStream(url.openStream())));
         
         int itemId = scan.nextInt();
         String expirationState = scan.next();
 
         addLocalFridgeFood(food, ExpState.valueOf(expirationState.trim().toUpperCase()));
         notifyObservers();
         return itemId;
     }
 
     private boolean addLocalFridgeFood(FridgeFood food, ExpState expirationState) 
     {      
         return foods.get(expirationState).add(food);
     }
 
     public void updateFridgeFood(FridgeFood food) throws IOException {
         updateLocalFridgeFood(food);
         URL url = new URL(
                 String.format(
                         "http://openfridge.heroku.com/fridge_foods/update/%d/%s/%d/%d/%d",
 
                         food.getId(),
                         URLEncoder.encode(food.getDescription(), "UTF-8"),
                         food.getExpirationYear(), food.getExpirationMonth(),
                         food.getExpirationDay()));
         url.openStream().read();
         notifyObservers();
     }
     
     private boolean updateLocalFridgeFood(FridgeFood updatedFood) {
         for (ExpState key : ExpState.values()) {
             List<FridgeFood> foodList = foods.get(key);
 
             for (FridgeFood f : foodList) {
                 if (f.getId() == updatedFood.getId()) {
                     f.setDescription(updatedFood.getDescription());
                     f.setExpirationDate(updatedFood.getExpirationDateString());
                     return true;
                 }
             }
         }
         
         return false;
     }
 
     public void removeFridgeFood(FridgeFood food, boolean eaten)
             throws MalformedURLException, IOException {
         URL url = new URL(String.format(
                 "http://openfridge.heroku.com/fridge_foods/%d/%s",
                 food.getId(), eaten ? "eat" : "throw"));
         Log.d("OpenFridge", url.toString());
         url.openStream().read();
         
         removeLocalFridgeFood(food);
     }
     
     private boolean removeLocalFridgeFood(FridgeFood food) {
         for (ExpState key : ExpState.values()) {
             List<FridgeFood> foodList = foods.get(key);
 
             int i;
             for (i = 0; i < foodList.size(); i++) {
                 FridgeFood f = foodList.get(i);
                 
                 if (f.getId() == food.getId()) break;
             }
             
             if (i < foodList.size()) {
                 foodList.remove(i);
                 Log.d("OpenFridge", String.format("Removed #%d", i));
                 return true;
             }
         }
         
         return false;
     }
 
     private void reloadFridgeFoods() {
         FridgeFoodHandler ffH = new FridgeFoodHandler();
         parse(ffH, fridgeFoodURL);
 
         // Changes the contents of the ArrayList's,
         // rather than re-assigning them.
         for (ExpState key : ExpState.values()) {
             foods.get(key).clear();
             foods.get(key).addAll(ffH.getFoods(key));
         }
     }
 
     // ShoppingItem routes
     // -----------------
     public int pushShoppingItem(ShoppingItem x) throws IOException {
         URL url = new URL(String.format(
                "http://openfridge.heroku.com/fridge_foods/push/%d/%s",
                 x.getUserId(), URLEncoder.encode(x.getDescription(), "UTF-8")));
         return (new DataInputStream(url.openStream())).readInt();
     }
 
     public void removeShoppingItem(ShoppingItem x) throws IOException {
         URL url = new URL(String.format(
                "http://openfridge.heroku.com/fridge_foods/destroy/%d",
                 x.getId()));
         url.openStream().read();
     }
 
     private void reloadShoppingItems() {
         ShoppingItemHandler siH = new ShoppingItemHandler();
         parse(siH, shoppingItemURL);
 
         shoppingList.clear();
         shoppingList.addAll(siH.getFoods());
     }
 
     // Field getters
     // -------------
 
     public List<ShoppingItem> getShoppingList() {
         return shoppingList;
     }
 
     public List<FridgeFood> getFoods(ExpState key) {
         return foods.get(key);
     }
 
     public int getExpirationListColor() {
         if (!getFoods(ExpState.EXPIRED).isEmpty()) {
             // There are expired items:
             return RED;
         } else if (!getFoods(ExpState.NEAR).isEmpty()) {
             // There are stale items:
             return YELLOW;
         }
         return GREEN;
     }
 
 	public int getShoppingListColor() {
 		if(!getShoppingList().isEmpty()){
 			return GREEN;
 		}
 		return Color.parseColor("#FFFFFF");
 	}
 
 
     // Singleton & utility stuff
     // -------------------------
     public static DataClient getInstance() {
         return DataClientHolder.client;
     }
 
     private static class DataClientHolder /* Pugh's Way */{
         public static final DataClient client = new DataClient();
     }
 
     public String getUID() {
         return "1";
     }
 
     private void parse(SAXHandler<?> h, URL url) {
         xr.setContentHandler(h);
         try {
             xr.parse(new InputSource(url.openStream()));
         } catch (IOException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         }
 
     }
 }
