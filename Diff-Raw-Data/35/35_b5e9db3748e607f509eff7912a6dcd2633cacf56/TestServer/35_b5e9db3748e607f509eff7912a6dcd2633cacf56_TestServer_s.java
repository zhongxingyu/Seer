 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.tests.util;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.server.CapoServer;
 
 
 
 /**
  * @author jeremiah
  * This class will start up the server, and wait until it is ready.
  * If you want to start up the test server to test the client, use the ExternalTestServer class.
  *
  */
 public class TestServer
 {
 	private static Thread serverThread = null;
 	private static CapoServer capoServer;
 	
 	public static CapoServer start(final String... args) throws Exception
 	{
 		
 		if (serverThread != null)
 		{
 			System.err.println("found an existing server" + serverThread);
 			System.exit(0);
 		}
		serverThread  = new Thread("ServerThread")
 		{
 			@Override
 			public void run()
 			{
 
 				try
 				{					
 					//CapoServer.main(new String[]{});
 					CapoServer capoServer = new CapoServer();					
 					capoServer.start(args);
 				}
 				catch (Exception e)
 				{					
 					e.printStackTrace();
 				}
 
 			}
 		};
 		serverThread.start();
 		while(CapoApplication.getApplication() == null || CapoApplication.getApplication().isReady() == false)
 		{            
 			Thread.sleep(1000);
 		}
 		capoServer = (CapoServer) CapoApplication.getApplication();
 		capoServer.setExceptionList(new CopyOnWriteArrayList<Exception>());
 		return capoServer;
 	}
 	
 	
 	public static void shutdown() throws Exception
 	{
 		if (capoServer != null)
 		{
 			capoServer.shutdown();
 		}
 		if (serverThread != null)
 		{
 			while (serverThread.isAlive())
 			{
 				CapoApplication.logger.log(Level.INFO, "waiting for server thread to die");
 				Thread.sleep(500);
 			}			
 		}
 	}
 	
 	public static CopyOnWriteArrayList<Exception> getExceptionList()
 	{
 		return capoServer.getExceptionList();
 	}
 	
 	public static void cleanup() throws Exception
 	{
 		shutdown();
 		serverThread = null;
 		capoServer = null;
 	}
 	
 	public static CapoServer getServerInstance()
 	{
 	    return capoServer;
 	}
 }
