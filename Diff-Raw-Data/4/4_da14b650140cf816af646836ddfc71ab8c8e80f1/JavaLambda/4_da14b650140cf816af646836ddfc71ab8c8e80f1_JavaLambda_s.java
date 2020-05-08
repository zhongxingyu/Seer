 package zoo.mb.java8;
 
 public class JavaLambda {
 
 	// http://www.javaworld.com/javaworld/jw-07-2013/130725-love-and-hate-for-java-8.html?page=3
 	// /usr/lib/jvm/java-8-oracle/bin
 	public static void main(String[] args) {
		Runnable runnable2 = () -> { System.out.println("Running from Lambda"); };	
		runnable2.run();
 	}
 }
