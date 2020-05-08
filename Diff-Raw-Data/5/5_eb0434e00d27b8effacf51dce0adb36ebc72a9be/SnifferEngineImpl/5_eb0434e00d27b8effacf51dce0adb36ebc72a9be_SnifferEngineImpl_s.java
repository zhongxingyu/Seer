 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 // Copyright (C) 2000  Phil Dawes
 // Copyright (C) 2001  Phil Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.tools.tcpsniffer;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import net.grinder.util.TerminalColour;
 
 
 /**
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @version $Revision$
  */
 public class SnifferEngineImpl implements SnifferEngine
 {
     private final SnifferFilter m_requestFilter;
     private final SnifferFilter m_responseFilter;
     private final String m_remoteHost;
     private final int m_remotePort;
     private final boolean m_useColour;
     private ServerSocket m_serverSocket = null;
 
     public SnifferEngineImpl(SnifferFilter requestFilter,
 			     SnifferFilter responseFilter,
 			     int localPort, String remoteHost, int remotePort,
 			     boolean useColour)
 	throws Exception
     {
 	this(requestFilter, responseFilter, remoteHost, remotePort, useColour);
 	
 	m_serverSocket = new ServerSocket(localPort);
     }
 
     protected SnifferEngineImpl(SnifferFilter requestFilter,
 				SnifferFilter responseFilter,
 				String remoteHost, int remotePort,
 				boolean useColour) 
     {
 	m_requestFilter = requestFilter;
 	m_responseFilter = responseFilter;
 	m_remoteHost = remoteHost;
 	m_remotePort = remotePort;
 	m_useColour = useColour;
     }
 
     protected void setServerSocket(ServerSocket serverSocket) 
     {
 	m_serverSocket = serverSocket;
     }
 
     protected String getRemoteHost()
     {
 	return m_remoteHost;
     }
 
     protected int getRemotePort()
     {
 	return m_remotePort;
     }
 
     protected Socket createRemoteSocket() throws IOException
     {
 	 return new Socket(m_remoteHost, m_remotePort);
     }
     
     public void run()
     {
 	while (true) {
 	    try {
 		final Socket localSocket = m_serverSocket.accept();
 		final Socket remoteSocket = createRemoteSocket();
 
 		new StreamThread(new ConnectionDetails("localhost",
 						       localSocket.getPort(),
 						       m_remoteHost,
 						       remoteSocket.getPort(),
 						       getIsSecure()),
 				 localSocket.getInputStream(),
 				 remoteSocket.getOutputStream(),
 				 m_requestFilter,
				 TerminalColour.RED);
 
 		new StreamThread(new ConnectionDetails(m_remoteHost,
 						       remoteSocket.getPort(),
 						       "localhost",
 						       localSocket.getPort(),
 						       getIsSecure()),
 				 remoteSocket.getInputStream(),
 				 localSocket.getOutputStream(),
 				 m_responseFilter,
				 TerminalColour.BLUE);
 	    }
 	    catch(IOException e) {
 		e.printStackTrace(System.err);
 	    }
 	}
     }
 
     protected boolean getIsSecure() 
     {
 	return false;
     }
 
     private String getColour(boolean response)
     {
 	if (!m_useColour) {
 	    return "";
 	}
 	else {
 	    return response ? TerminalColour.BLUE : TerminalColour.RED;
 	}
     }
 }
 
 class StreamThread implements Runnable
 {
     private final static int BUFFER_SIZE=65536;
 
     private final ConnectionDetails m_connectionDetails;
     private final InputStream m_in;
     private final OutputStream m_out;
     private final SnifferFilter m_filter;
     private final String m_colour;
     private final String m_resetColour;
 
     public StreamThread(ConnectionDetails connectionDetails,
 			InputStream in, OutputStream out,
 			SnifferFilter filter,
 			String colourString)
     {
 	m_connectionDetails = connectionDetails;
 	m_in = in;
 	m_out = out;
 	m_filter = filter;
 	m_colour = colourString;
 	m_resetColour = m_colour.length() > 0 ? TerminalColour.NONE : "";
 	
 	final Thread t = new Thread(this,
 				    m_connectionDetails.getDescription());
 
 	m_filter.connectionOpened(m_connectionDetails);
 
 	t.start();
     }
 	
     public void run()
     {
 	try {
 	    byte[] buffer = new byte[BUFFER_SIZE];
 
 	    while (true) {
 		int bytesRead = m_in.read(buffer, 0, BUFFER_SIZE);
 
 		if (bytesRead == -1) {
 		    break;
 		}
 
 		System.out.print(m_colour);
 		m_filter.handle(m_connectionDetails, buffer, bytesRead);
 		System.out.print(m_resetColour);
 
 		// and write in out
 		m_out.write(buffer, 0, bytesRead);
 	    }
 	}
 	catch (SocketException e) {
 	    // Be silent about SocketExceptions.
 	}
 	catch (Exception e) {
 	    e.printStackTrace(System.err);
 	}
 
 	System.out.print(m_colour);
 	m_filter.connectionClosed(m_connectionDetails);
 	System.out.print(m_resetColour);
 
 	// We're exiting, usually because the in stream has been
 	// closed. Whatever, close our streams. This will cause the
 	// paired thread to exit to.
 	try {
 	    m_out.close();
 	}
 	catch (Exception e) {
 	}
 	
 	try {
 	    m_in.close();
 	}
 	catch (Exception e) {
 	}
     }
 }
