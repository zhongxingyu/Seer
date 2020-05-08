 package ecologylab.services.logging;
 
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XmlTools;
 
 /**
 * Base class for SendPrologue and SendEpilogue.
 * Probably should not be used for anything else.
 * Enables passing of the logName, in order to write custom log XML element
 * open and close tags.
  *
  * @author andruid
  */
 public class LogueMessage extends LogRequestMessage
 {
 
 	public String logName;
 
 	/**
 	 * Constructor for building from the Logging class.
 	 * @param logging
 	 */
 	public LogueMessage(Logging logging)
 	{
 		logName			= XmlTools.getXmlTagName(logging.getClass(), "State", false);
 	}
 	/*
 	 * Constructor for automatic translation;
 	 */
 	public LogueMessage()
 	{
 		super();
 	}
 
 	public String logName() {
 		return (logName != null) ? logName : "logging";
 	}
 
 }
