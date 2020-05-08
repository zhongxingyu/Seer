 package org.nchelp.meteor.message.index;
 
 import org.nchelp.meteor.util.XMLParser;
 import org.w3c.dom.Document;
 
 /**
  *   Add messages to the MeteorIndexResponse.  This class allows
  *   the Index Provider to return descriptive messages that will
  *   be logged or returned to the user.
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Sep 4, 2002
  */
 public class IndexMessage {
 
 	/**
 	 * String containing the message
 	 */
 	private String message;
 	
 	/**
 	 * Level of this message: I = Informational
 	 * W = Warning, E = Error
 	 */
 	private String level;
 
 	/**
 	 * Method IndexMessage.
 	 * @param message String with the message to return
 	 * @param level String with the error level, I = Informational
 	 * W = Warning, E = Error
 	 */
 	public IndexMessage(String message, String level) {
 		this.message = message;
 		this.level = level;
 	}
 
 	/**
 	 * Method to marshall the Document from XML to this object
 	 * @param doc Document containing a Message
 	 */
 	public IndexMessage(Document doc) {
 		this.message = XMLParser.getNodeValue(doc, "RsMsg");
		this.level = XMLParser.getNodeValue(doc, "RsMsgLevel");
 	}
 
 	/**
 	 * Method getMessage.
 	 * @return String
 	 */
 	public String getMessage() {
 		return this.message;
 	}
 
 	/**
 	 * Get the current level of this Index Provider
 	 * @return String
 	 */
 	public String getLevel() {
 		return this.level;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "<Message><RsMsg>"
			+ message
 			+ "</RsMsg>"
 			+ "<RsMsgLevel>"
 			+ level
 			+ "</RsMsgLevel></Message>";
 	}
 }
