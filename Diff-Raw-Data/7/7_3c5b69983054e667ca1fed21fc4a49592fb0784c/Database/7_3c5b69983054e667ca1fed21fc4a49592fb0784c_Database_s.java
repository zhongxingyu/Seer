 package HTTPServer;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 public class Database {
 
   private static final Database instance = new Database();
   public Hashtable<Integer, Object> table = new Hashtable<Integer, Object>();
 
   private Database(){}
 
   public static Hashtable table(){
     return instance.table;
   }
 
   public static int add(Object object) {
     int index = getMaxId();
     instance.table.put(index, object);
     return index;
   }
 
   public static int[] ids() {
     int[] indexes = new int[instance.table.size()];
     int i = 0;
     for(Enumeration<Integer> e = instance.table.keys(); e.hasMoreElements(); ){
       indexes[i] = e.nextElement();
       i++;
     }
     return indexes;
   }
 
   private static int getMaxId() {
     int maxIndex = 0;
     for(int i = 0; i < ids().length; i++){
      if(ids()[i] >= maxIndex)
         maxIndex = i + 1;
     }
     return maxIndex;
   }
 }
