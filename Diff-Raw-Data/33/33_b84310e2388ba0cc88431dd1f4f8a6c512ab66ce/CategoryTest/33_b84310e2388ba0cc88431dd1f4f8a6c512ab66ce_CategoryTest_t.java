 package lightframework.data.mysql;
 
 import java.util.*;
import java.util.regex.Matcher;
import org.hamcrest.Description;
import org.junit.*;
 
 /**
  *
  * @author Tom Deng
  */
 public class CategoryTest {
 
    private final static String url = "jdbc:mysql://localhost/test?user=root&password=ddd";
    private CategoryDAO dao;
 
     public void run() {
         CategoryDAO category = new CategoryDAO(null);
         category.deleteAll();
         List<CategoryEntity> categories = category.select();
         for (int i = 0; i < 10; i++) {
            CategoryEntity dto = new CategoryEntity(i, "test" + i, "test1" + i);
             int id = category.insertWithId(dto);
         }
     }

    @Before
    public void startup() {
        this.dao = new CategoryDAO(null);
    }

    @After
    public void tearDown() {
        this.dao  = null;
    }

    @Test
    public void add() {
        CategoryEntity entity = new CategoryEntity(1, "test1", "test1");
        int id = this.dao.insertWithId(entity);
        Assert.assertThat(id,null);
    }
    
    @Test
    public void delete(){
        
    }
 }
