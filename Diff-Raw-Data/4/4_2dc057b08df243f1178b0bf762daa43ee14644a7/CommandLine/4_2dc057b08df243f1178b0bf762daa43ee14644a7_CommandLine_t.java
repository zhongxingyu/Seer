 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Collections;
 
 public class CommandLine {
 	public static void main(String [] args) {
 		if(args[args.length-1].equals("down")) {
 			Arrays.sort(args,Collections.reverseOrder());
 			for(String str:args) {
				if(System.getProperty(str) != null){ 
				System.out.println(System.getProperty(str));
 				}
 			}
 		} else {
 			Arrays.sort(args);
 			for(String str:args) {
 				if(System.getProperty(str) != null){ 
 					System.out.println(System.getProperty(str));
 				}
 			}
 		}
 	}
 }
