 import java.io.*;
 
 public class ParsingRules {
 
     static public void main(String argv[]) {
        	try {
	    System.out.println("Parsing (" + argv[0] + ")...");

 	    parser p = new parser(new lexer(new FileReader(argv[0])));
 	    p.parse();
 	    
	    /*lexer l = new lexer(new FileReader(argv[0]));
 	    java_cup.runtime.Symbol s = l.next_token();
 
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
 	    s = l.next_token();
 	    System.out.println(s.toString());
	    System.out.println(sym.SET_HOST);*/
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
     
 }
