 import java.io.*;
 import java.util.*;
 
 public class DbConsole {
 
 	public static void main(String...args) throws Exception {
 		Database simpledb = new Database();
 		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 		
 		ParserElem cmd = null;
 		do {
			System.out.print("\nsimpledb > ");
 
 			String[] vanillaCmd = input.readLine().split("\\s+");
			if (vanillaCmd.length == 0 || vanillaCmd[0].isEmpty()) continue;
 
 			cmd = ParserElem.getElem(vanillaCmd[0]);
 			String[] cmdArgs = Arrays.copyOfRange(vanillaCmd, 1, vanillaCmd.length);
 			
 			if (cmd == null) {
 				System.out.println("INVALID COMMAND");
 				continue;
 			}
 
 			try {
 				String result = cmd.runOn(simpledb, cmdArgs);
 				if(result != null) {
 					System.out.println(result);
 				}
 			} catch (Exception e) {
 				System.out.println(e.toString());
 			}
 		} while (cmd != ParserElem.END);
 	}
 }
 
