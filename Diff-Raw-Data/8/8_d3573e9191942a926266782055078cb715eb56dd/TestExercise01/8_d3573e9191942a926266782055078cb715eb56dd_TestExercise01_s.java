 package javasnack.testng1;
 
 import static org.testng.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 public class TestExercise01 {
     @BeforeSuite
     public void beforeSuite() {
         System.out.println(Thread.currentThread() + " - beforeSuite()");
     }
     @AfterSuite
     public void afterSuite() {
         System.out.println(Thread.currentThread() + " - afterSuite()");
     }
     @BeforeTest
     public void beforeTest() {
         System.out.println(Thread.currentThread() + " - beforeTest()");
     }
     @AfterTest
     public void afterTest() {
         System.out.println(Thread.currentThread() + " - afterTest()");
     }
     @BeforeClass
     public void beforeClass() {
         System.out.println(Thread.currentThread() + " - beforeClass()");
     }
     @AfterClass
     public void afterClass() {
         System.out.println(Thread.currentThread() + " - afterClass()");
     }
     @BeforeMethod
     public void beforeMethod() {
         System.out.println(Thread.currentThread() + " - beforeMethod()");
     }
     @AfterMethod
     public void afterMethod() {
         System.out.println(Thread.currentThread() + " - afterMethod()");
     }
 
     @DataProvider(name="dp1")
     public Object[][] dp1() {
         return new Object[][] {
                 {1, 2, 3},
                 {4, 5, 9},
                 {6, 7, 13}
                 };
     }
 
     int testadd(int a, int b) {
         return a + b;
     }
 
     @Test(dataProvider = "dp1")
     public void verifyAddFunc(int a, int b, int expected) {
        assertEquals(expected, testadd(a, b));
     }
 
     List<String> mylist = Arrays.asList("123", "456");
 
     @Test
     public void listLength1() {
         mylist = Arrays.asList("abc", "def", "ghi");
         System.out.println(mylist);
        assertEquals(3, mylist.size());
     }
 
     @Test
     public void listLenngth2() {
         System.out.println(mylist);
        assertEquals(3, mylist.size());
     }
 
 
 }
