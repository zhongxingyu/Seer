 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.models;
 
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import plantgame.utils.Constants;
 import plantgame.utils.GameItemsEnum;
 
 /**
  *
  * @author tyler
  */
 public class Store implements Runnable{
 
   //StoreItem will be an internal class to Store that holds an item type
   //and the number of that item which the store contains
   private class StoreItem{
     //numberOfItem: number of items in stock
     public int numberOfItem;
     public GameItemsEnum itemType;
     public int maxNumberOfItem;
     
     public StoreItem(GameItemsEnum e, int num){
       // num: ITEM_NUMBER_STORE_START
       numberOfItem = num;
       itemType = e;
       // maxNumberOfItem: MAX_NUMBER_OF_ITEM_IN_STORE
       maxNumberOfItem = e.getMaxNumber();
     }
   }
   
   //Private class extending TimerTask. This will be used by the addItemsToStoreTimer
   //object to add items to the store.
   private class StoreDeliver extends TimerTask{
     public StoreDeliver(){
       super();
     }
     
     @Override
     public void run(){
       for(GameItemsEnum item : GameItemsEnum.values()){
         addItemToStore(item.getName(), Constants.NUMBER_OF_ITEM_TO_ADD_TO_STORE);
         
         //DEBUG
         System.out.println("Store:StoreDeliver adding "+item.getName());
       }
     }
   }
   
   
   private static Store store = null;
   private HashMap<String, StoreItem> storeItems;
   
   //The constructor creates the storeItems hashmap which can be used to
   //look up a StoreItem object by its name
   private Store(){
     
     storeItems = new HashMap<String, StoreItem>();
     
     for (GameItemsEnum item : GameItemsEnum.values()){
       StoreItem newStoreItem = new StoreItem(item, Constants.ITEM_NUMBER_STORE_START);
       storeItems.put(item.getName(), newStoreItem);
     }
   }
   
   @Override
   public void run(){
     //DEBUG
     System.out.println("Store creating thread for deliveries.");
     
     //Create a timer object which will execute 
     Timer addItemsToStoreTimer = new Timer();
     addItemsToStoreTimer.scheduleAtFixedRate(new StoreDeliver(), Constants.STORE_FIRST_DELIVERY_DELAY, Constants.STORE_INTER_DELIVERY_DELAY);    
   }
   
   
   //Store will be a singleton since it will be shared among all players in the 
   //game
   //This will also start the store thread
   public static Store getInstance (){
     //DEBUG
     System.out.println("Store returning an instance of the store.");
     
     if (store == null){
       store = new Store();
       
       //DEBUG
       System.out.println("Store starting thread.");
       
       //Start the thread
       (new Thread(store)).start();
     }
     return store;
   }
   
   //method for adding items to store. Should be synchronized
   public synchronized void addItemToStore(String itemName, int numberToAdd){
     
     //DEBUG
     System.out.println("Store adding "+numberToAdd+" "+itemName+" to the store.");
     
     
     //First get the StoreItem
     StoreItem item = storeItems.get(itemName);
     
     //Add the numberToAdd items to the store's inventory
     item.numberOfItem = item.numberOfItem+numberToAdd;
     
     //Then determine if the store hold too many of that item
     //and if so decrease the number to the max number that the store
     //can hold
     if (item.numberOfItem > item.maxNumberOfItem){
       item.numberOfItem = item.maxNumberOfItem;
     }
     
     //DEBUG
     System.out.println("Store now has "+item.numberOfItem+" of "+item.itemType.getName());
     
   }
   
   //Method for getting the number of items in stock for a particular item
   public int getNumberOfItemInStock(String itemName){
     return storeItems.get(itemName).numberOfItem;
     //storeItems.get(itemName): return a StoreItem object
   }
   
   //method for removing items from store. Should be synchronized
   //The actual purchasing of items will be done here
   //since this method is synchronized
   public synchronized String purchaseItems(int[] selectedItems, User user){
     
     //DEBUG
     System.out.println("Store User's name is "+user.getUserName());
     
     //DEBUG
     if (user.getItems() == null){
       System.out.println("Store the User's items are null");
     }
     else{
       for (GameItemsEnum item : GameItemsEnum.values()){
         if (user.getItems().containsKey(item.getName())  ){
           System.out.println("Store The user has "+item.getName());
         }
         else{
           System.out.println("Store The user does not have "+item.getName());
         }
       }
     }
     
     int index = 0;
     HashMap<String, StoreItem> storeItemsCopy = new HashMap<String,StoreItem>(this.storeItems);
     HashMap<String, UserItem> userItemsCopy = new HashMap<String, UserItem>(user.getItems());
    double total = 0;
     StoreItem storeItem;
     UserItem userItem;
     
     //Check that there are enough items in the store
     for (GameItemsEnum item: GameItemsEnum.values()){
      total = total+(double)selectedItems[index]*this.getItemPrice(item.getName());
       
       if (this.getNumberOfItemInStock(item.getName()) < selectedItems[index]){
         return Constants.NOT_ENOUGH_ITEMS_IN_STORE;
       }
       else{
         //DEBUG
         System.out.println("Store purchasing "+item.getName());
         
         //Remove items from copy of store's items
         storeItem = storeItemsCopy.get(item.getName());
         storeItem.numberOfItem=storeItem.numberOfItem-selectedItems[index];
         //Add items to copy of user's stock
         userItem = userItemsCopy.get(item.getName());
         userItem.setNumberOfItem(userItem.getNumberOfItem()+selectedItems[index]);
       }
       index++;
     }
     
     if (total > user.getMoney()){
       return Constants.NOT_ENOUGH_MONEY;
     }
     //At this point it has been verified that the store has enough items and
     //the user has enough money for the purchase     
     
     //Subtract user's money
     user.setMoney(user.getMoney()-total);
     
     //set store's stock to the copy of the store's stock
     this.storeItems = storeItemsCopy;
     //set user's stock to the copy of the user's stock
     user.setItems(userItemsCopy);
     
     return Constants.PURCHASE_COMPLETE;
   }
   
   //method for getting price of an item
   public int getItemPrice(String itemName){
     StoreItem s = storeItems.get(itemName);
     return s.itemType.getPrice(s.numberOfItem);
   }
   
   //Convenience method for getting an item's price without having
   //to actually get a store object
   public static int getItemPriceStatic(String itemName){
     Store s = Store.getInstance();
     return s.getItemPrice(itemName);
   }
 }
