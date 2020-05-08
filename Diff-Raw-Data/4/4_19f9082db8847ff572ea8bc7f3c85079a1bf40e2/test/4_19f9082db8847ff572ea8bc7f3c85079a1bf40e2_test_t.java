 import java.io.*;
 public class test{
 	public static void main(String[] args)throws IOException{
		RunCode run = new RunCode("foo", "class foo{ public satic void main(String[] Args){System.out.println(\"foobar\");}}");
		run.comprun();
 	}
 }
