 import java.util.Scanner;
 
 public class LOGOIO {
 	private Scanner scanner;
 	public LOGOIO() {
 		scanner = new Scanner(System.in);
 	}
 	
 	public void out(String str) {
		//System.out.println(str);
		LOGOPP.prev.append(str+"\n");
 	}
 
 	public void debug(String str) {
 		System.out.println(str);
 	}
 
 	public void err(String str) {
		//System.err.println(str);
		LOGOPP.prev.append(str+"\n");
 	}
 	
 	public String in() {
 		return scanner.nextLine();
 	}

	
 }
