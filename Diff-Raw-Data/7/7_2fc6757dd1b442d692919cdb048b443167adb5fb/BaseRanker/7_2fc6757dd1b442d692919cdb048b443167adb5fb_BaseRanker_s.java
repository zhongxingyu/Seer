 public abstract class BaseRanker {
     
     // Instance variables
     public User user;
     public RandomItems randomItems;
 
     // The found preferences
     public ArrayList<Item> preferences;
     
     public BaseRanker {
         user = new User();
         randomItems = new RandomItems();
         preferences = new ArrayList<Item>();
     }
     
     public void printPreferences() {
         for (Item sortedItem : preferences) {
             System.out.println(sortedItem);
         }
     }
     
 }
