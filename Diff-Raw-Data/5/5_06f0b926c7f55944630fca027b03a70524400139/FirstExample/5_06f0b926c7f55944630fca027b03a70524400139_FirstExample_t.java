 public class FirstExample {
 
     public void sayHello(String name){
 
 	System.out.printf("hello %s\n", name);
     }
 
    public static void main(String[] args){

	FirstExample firstExample = new FirstExample();
	firstExample.sayHello(args[0]);
    }
 }
