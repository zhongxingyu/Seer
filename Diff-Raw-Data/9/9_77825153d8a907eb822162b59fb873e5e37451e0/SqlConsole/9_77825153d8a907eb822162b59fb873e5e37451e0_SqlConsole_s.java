 package sqlengine_a1;
 import java.util.*;
 import sqlengine_a1.parser.*;
 
 public class SqlConsole {
    public static void main(String[] args) {
        Scanner stdin = new Scanner(System.in);
 		while (true) {
 			System.out.print("|> ");
 			String line = stdin.nextLine();
 			
 			try {
 				SqlParser sp = new SqlParser(line);
 				ASTNode astRoot = sp.parseStatement();
 				System.out.println(astRoot.toString());
 				
 				if (astRoot.type == ASTNode.Type.QUIT_STATEMENT)
 					return;
 			}
 			catch (ParseError e) {
 				System.err.println("Parse error: " + e);
 			}
        }
    }
 }
