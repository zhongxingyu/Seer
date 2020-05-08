 /**
  * 
  */
 package com.github.ribesg.javachat.client;
 
 import static com.github.ribesg.javachat.common.Constants.*;
 
 import java.util.Scanner;
 
 
 /**
  * @author Ribesg
  * 
  */
 public class MainClient {
 
 	private Client client;
 	/**
 	 * @param args
 	 *            Not used for now
 	 */
 	public static void main(final String[] args) {
 		new MainClient();
 	}
 
 	public MainClient() {
 		client = new Client(SERVER_ADDRESS, SERVER_PORT);
 		Scanner scan = new Scanner(System.in);
 		String input = "";
 		while (!input.equalsIgnoreCase("exit")) {
 			input = scan.nextLine();
 			try {
 				client.sendMessage(input, System.currentTimeMillis());
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(42);
 			}
 		}
 		scan.close();
 	}
 
 }
