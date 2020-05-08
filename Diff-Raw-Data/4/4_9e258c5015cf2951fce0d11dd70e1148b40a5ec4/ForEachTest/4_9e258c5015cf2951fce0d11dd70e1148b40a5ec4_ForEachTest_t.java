 package uk.co.jezuk.mango;
 
 import junit.framework.*;
 
 public class ForEachTest  extends TestCase
 {
   java.util.List list;
 
   public ForEachTest(String name) { super(name); }
   public static Test suite() { return new TestSuite(ForEachTest.class); }
 
   protected void setUp()
   {
     list = new java.util.ArrayList();
     for(int i = 0; i < 10; ++i)
       list.add(new Integer(i));
   } // setUp
 
   private class Print implements UnaryFunction 
   {
    public Object fn(Object o)
     {
       System.out.println(o.toString());
      return null;
     } 
   } // Print
 
   public void test1()
   {
     Mango.forEach(list, new Print());
   } // 
 } // ForEachTest
