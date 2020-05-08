 package helloworld;
 /**
  *
  * @author WASD
  */
 public class HelloWorld {
 
     public static void main(String[] args) {
 
         System.out.println("Hello World!");
         System.out.println("kuk");
         System.out.println("kukar");
		
        NewClass x = new NewClass(5, "Hello World!");
		for(int i = 0; i < x.getX(); i++) {
			System.out.println(i + " of " + x.getX() + ": " + x.getName());
		}
     }
 
 }
