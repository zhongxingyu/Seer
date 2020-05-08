 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.InputStream;
 import java.util.Scanner;
 import org.junit.*;
 import static org.junit.Assert.*;
 import fic.*;
 
 /**
  *
  * @author shivanth
  */
 public class Test1 {
     
     public Test1() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     /*@Test
      public void test1() {
          String S = "3 1 2 3 4 5 6 7 8 9";
          ByteArrayInputStream b= new ByteArrayInputStream(S.getBytes());
          System.setIn(b);
          FIC.main(null);
          
          
      }
     * 
     */
      @Test
      public void test2() {
          float a[][]={{1,0,0},
          {0,1,0},
          {0,0,1}};
         String S = "3 1 2 3 4 5 6 7 8 9";
          ByteArrayInputStream b= new ByteArrayInputStream(S.getBytes());
          System.setIn(b);
          FIC.main(null);
      
          
         FIC.get_score(a);
          
      }
 }
