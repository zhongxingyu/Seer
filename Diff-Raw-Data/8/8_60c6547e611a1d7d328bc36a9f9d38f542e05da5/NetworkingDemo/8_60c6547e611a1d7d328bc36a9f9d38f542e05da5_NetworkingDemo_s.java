 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.demo;
 import cnt.game.*;
 import cnt.network.*;
 import cnt.*;
 
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 /**
  * Networking demo class
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class NetworkingDemo
 {
     /**
      * Non-constructor
      */
     private NetworkingDemo()
     {
 	assert false : "You may not create instances of this class [NetworkingDemo].";
     }
     
     
     
     /**
      * This is the main entry point of the demo
      * 
      * @param  args  Startup arguments, unused
      * 
      * @throws  Exception  On total failure
      */
     public static void main(final String... args) throws Exception
     {
 	final int PORT = 9999;
 	final ServerSocket server = new ServerSocket(PORT);
 	
 	
 	final Thread thread0 = new Thread()
 	        {
 		    /**
 		     * {@inheritDoc}
 		     */
 		    @Override
 		    public void run()
 		    {
 			try
 			{
 			    final Socket sock = server.accept();
 			    final InputStream in = sock.getInputStream();
 			    final OutputStream out = sock.getOutputStream();
 			    
 			    final BlackboardNetworking network0 = new BlackboardNetworking(new GameNetworking(new ObjectNetworking(in, out)));
 			    Blackboard.broadcastMessage(new Blackboard.LocalPlayer(new Player("Mattias", Color.RED.getRGB())));
 			    
 			    System.out.println("network0: sending message: ChatMessage");
 			    Blackboard.broadcastMessage(new Blackboard.ChatMessage(new Player("Mattias", Color.RED.getRGB()), "sending a message"));
 			    
 			    System.out.println("network0: sending message: SystemMessage");
			    Blackboard.broadcastMessage(new Blackboard.SystemMessage(null, "system message"));
 			    
 			    System.out.println("network0: sending message: UserMessage");
			    Blackboard.broadcastMessage(new Blackboard.UserMessage("local user chat message"));
 			    
 			    System.out.println("network0: sending message: MatrixPatch");
 			    Blackboard.broadcastMessage(new Blackboard.MatrixPatch(new boolean[2][2], new Block[2][2], 0, 0));
 			}
 			catch (final Throwable err)
 			{
 			    err.printStackTrace(System.err);
 			}
 		    }
 	        };
 
 	final Thread thread1 = new Thread()
 	        {
 		    /**
 		     * {@inheritDoc}
 		     */
 		    @Override
 		    public void run()
 		    {
 			try
 			{
 			    final Socket sock = new Socket("127.0.0.1", PORT);
 			    final InputStream in = sock.getInputStream();
 			    final OutputStream out = sock.getOutputStream();
 			    
 			    final BlackboardNetworking network1 = new BlackboardNetworking(new GameNetworking(new ObjectNetworking(in, out)))
 				    {
 					/**
 					 * {@inheritDoc}
 					 */
 					@Override
 					protected void broadcastMessage(final Blackboard.BlackboardMessage message) throws IOException, ClassNotFoundException
 					{
 					    System.out.println("network1: received blackboard message: " + message.getClass().toString());
 					}
 				    };
 			    
 			    Blackboard.unregisterObserver(network1);
 			    
 			    
			    for (int i = 0; i < 4; i++)
 				network1.receiveAndBroadcast();
 			}
 			catch (final Throwable err)
 			{
 			    err.printStackTrace(System.err);
 			}
 		    }
 	        };
 	
 	thread0.start();
 	Thread.sleep(200);
 	thread1.start();
     }
     
 }
 
