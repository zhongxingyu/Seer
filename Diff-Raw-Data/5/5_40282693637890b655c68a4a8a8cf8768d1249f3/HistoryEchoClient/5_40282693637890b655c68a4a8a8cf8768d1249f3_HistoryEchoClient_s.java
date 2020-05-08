 package ecologylab.tutorials;
 import java.io.IOException;
 import java.util.Scanner;
 
 import ecologylab.xml.TranslationScope;
 import ecologylab.collections.Scope;
 import ecologylab.services.distributed.client.*;
 import ecologylab.services.distributed.exception.MessageTooLargeException;
 import ecologylab.services.messages.DefaultServicesTranslations;
 
 /** 
  * HistoryEchoServer: A sample server implemented via OODSS.
  * Intended to be used as a tutorial application.
  */
 public class HistoryEchoClient
 {
 	public static void main(String[] args) throws IOException
 	{
 		NIOClient client;
 		String serverAddress;
 		int portNumber;
 		
 		Scanner scan = new Scanner(System.in);
 		scan.useDelimiter(":");
 		
 		/*
 		 * Input server address and port number
 		 */
 		System.out.print("Please enter [server:port] : ");
 		serverAddress = scan.next();
		scan.reset();		
 		portNumber = Integer.parseInt(scan.next().substring(1));;
 		
 		System.out.println("Connecting to " + serverAddress + " on port# " + portNumber);
 		
 		Class[] historyEchoClasses = { HistoryEchoRequest.class,
 												 HistoryEchoResponse.class };
 		
 		/*
 		 *  Get base translations with static accessor
 		 */
 		TranslationScope baseServices = DefaultServicesTranslations.get();
 		
 		/*
 		 * compose translations, to create the histEchoTrans
 		 * space inheriting the base translations
 		 */
 		TranslationScope histEchoTranslations = 
 			TranslationScope.get("histEchoTrans",
 										historyEchoClasses,
 										baseServices);	
 		
 		Scope clientScope = new Scope();
 		
 		client = new NIOClient(serverAddress, portNumber,
 												 histEchoTranslations,clientScope);
 		client.connect();
 		
		scan.reset();
 		
 		System.out.println("Please enter some messages: ");
 		
 		while(true)
 		{
 			String input = scan.nextLine();
 			
 			if(input.trim().toLowerCase().equals("exit"))
 				break;
 			
 			HistoryEchoRequest echoRequest = new HistoryEchoRequest(input);
 			try
 			{
 				client.sendMessage(echoRequest);
 			}
 			catch (MessageTooLargeException e)
 			{
 				System.err.println("The message you sent was too large!");
 				e.printStackTrace();
 			}
 		}
 		
 		client.disconnect();
 	}
 }
