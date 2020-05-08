 package foo;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cprice
  * Date: 8/21/12
  * Time: 6:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Foo {
 
     public static void main(String[] args) {
         System.out.println("hi");
        byte[] mybytes = new byte[] { (byte) 0xc2, (byte) 0x7f };
        for (int i = 0; i < mybytes.length; i++) {
            System.out.println(mybytes[i]);
        }

     }
 }
