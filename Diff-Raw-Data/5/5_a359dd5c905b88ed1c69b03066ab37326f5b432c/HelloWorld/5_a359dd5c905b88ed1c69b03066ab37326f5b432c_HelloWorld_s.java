 public class HelloWorld {
 
  public static void print_message(final String name) {
     System.out.println("Hello, " + name + "!");
   }
 
   public static void main(final String[] args) {
    print_message(args[0]);
   }
 }
