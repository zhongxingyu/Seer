 package lightframework.data.mysql;
 
 import java.util.*;
 
 /**
  *
  * @author Tom Deng
  */
 public class CategoryTest {
 
    private final static String url = "jdbc:mysql://192.168.1.251/test?user=root&password=ddd";
 
     public void run() {
         CategoryDAO category = new CategoryDAO(null);
         category.deleteAll();
         List<CategoryEntity> categories = category.select();
         for (int i = 0; i < 10; i++) {
            CategoryEntity dto = new CategoryEntity(i,"test" + i, "test1" + i);
             int id = category.insertWithId(dto);
         }
     }
 }
