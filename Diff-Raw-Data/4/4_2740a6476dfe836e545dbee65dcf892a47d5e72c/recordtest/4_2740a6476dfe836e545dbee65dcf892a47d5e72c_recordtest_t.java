 import java.util.*;
 import com.sleepycat.db.*;
 
 public class recordtest {
   public static void test(int i) {
     String title;
     int id;
     String[] artists = new String[Record.getNumArtists(i)];
     String[] user = new String[Record.getNumUsers(i)];
     int[] rating = new int[user.length];
     id = Record.getID(i);
     title = Record.getTitle(i);
     artists = Record.getArtists(i);
     user = Record.getUsers(i);
     for (int j=0; j<user.length; j++) {
       rating[j] = Record.getRating(i, user[j]);
     }
     System.out.print(id + " " + title + " ");
     for (int k=0; k<artists.length; k++) {
       System.out.print(artists[k] + " ");
     }
     for (int m=0; m<user.length; m++) {
       System.out.print(user[m] + " " + rating[m] + " ");
     }
     System.out.println("");
   }
 /*
   public static void testDB() {
     try {
       Cursor tcur = recordDB.title_DB.openCursor(null, null);
       Cursor acur = recordDB.artists_DB.openCursor(null, null);
       Cursor ucur = recordDB.userrat_DB.openCursor(null, null);
       OperationStatus opStat;
       DatabaseEntry key, data;
       key = new DatabaseEntry();
       data = new DatabaseEntry();
       
       for (int i=0; i<Record.recordSize(); i++) {
         String id = Record.getID(i).toString();
         key.setData(id.getBytes());
         key.setSize(id.length());
         while (tcur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.println(new String(key.getData()) + " " + new String(data.getData()) + " ");
           data = new DatabaseEntry();
         }
         while (acur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.print(new String(data.getData()) + " ");
           data = new DatabaseEntry();
         }
         while (ucur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.print(new String(data.getData()) + " ");
           data = new DatabaseEntry();
           }
         //System.out.print("\n");
         key = new DatabaseEntry();
       }
     } catch (Exception e) {
       e.getMessage();
     }
   }
 */
   public static void testDB() {
     try {
       Cursor acur = recordDB.artists_DB.openCursor(null, null);
       Cursor ucur = recordDB.userrat_DB.openCursor(null, null);
       DatabaseEntry key, data;
       key = new DatabaseEntry();
       data = new DatabaseEntry();
       for (int i=0; i<Record.recordSize(); i++) {
         String id = Record.getID(i).toString();
         key.setData(id.getBytes());
         key.setSize(id.length());
         if (recordDB.title_DB.get(null, key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.println(new String(key.getData()) + " " + new String(data.getData()));
           data = new DatabaseEntry();
         }
        //acur.getFirst(key, data, LockMode.DEFAULT);
        //System.out.println(new String(data.getData
         while (acur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.println(new String(data.getData()));
           data = new DatabaseEntry();
         }
         while (ucur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
           System.out.println(new String(data.getData()));
           data = new DatabaseEntry();
         }
       }
       acur.close();
       ucur.close();
       recordDB.title_DB.close();
       recordDB.artists_DB.close();
       recordDB.userrat_DB.close();
     } catch (Exception e) {
       e.getMessage();
     }
   }
 
 }
