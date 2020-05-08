 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.patuck.stegmpp.xmpp;
 
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.patuck.stegmpp.StegMPP;
 
 /**
  * The IQ class represents an in
  * @author reshad
  */
 public class IQ
 {
 	
 	private static PrintWriter pw=null;
 
 	/**
 	 * Set the PrintWriter to write to the XMPP server.
 	 * @param p the PrintWriter object.
 	 */
 	public static void setPrintWriter(PrintWriter p)
 	{
 		pw = Session.getPrintWriter();
 	}
 
 	
 	/**
 	 * Send an IQ message.
 	 * @param type the type of message.
 	 * @param to the to field.
 	 * @param data the data in the tag.
 	 */
 	public static void sendIQ(String type, String to, String data)
 	{
 		if (to == null)
 		{
 			pw.println("<iq type='" + type + "' id='" + Session.getId() + "'>" + data + "</iq>");
 		}
 		else
 		{
 			pw.println("<iq type='" + type +"' id='" + Session.getId() + "' to='" + to + "'>" + data + "</iq>");
 		}
 		pw.flush();
 		try		
 		{
 			synchronized(StegMPP.getSession())
 			{
 				StegMPP.getSession().wait();
 			}
 		}
 		catch (InterruptedException ex)
 		{
 			Logger.getLogger(IQ.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		Session.incrementID();
 	}
 	
 	
 	
 	/**
 	 * Read and process the IQ tag.
 	 * @param tag the IQ tag.
 	 */
 	public static void receiveIQ(org.jdom2.Document tag)
 	{
 		if (tag.getRootElement().getAttribute("type").getValue().equals("result"))
 		{
 			
 			synchronized(StegMPP.getSession())
 			{
 				StegMPP.getSession().notifyAll();
 			}
 		}
 		else if(tag.getRootElement().getAttribute("type").getValue().equals("get"))
 		{
 			tag.toString();
 		}
 		
 	}
 }
