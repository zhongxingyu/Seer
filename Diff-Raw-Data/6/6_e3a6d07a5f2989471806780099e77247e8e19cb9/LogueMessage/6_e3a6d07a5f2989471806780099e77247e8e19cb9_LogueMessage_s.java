 package ecologylab.services.logging;
 
 import ecologylab.xml.ElementState;
 import ecologylab.xml.XmlTools;
 
 /**
 * Message to extend for Prologue and Epilogue.
 * Enables writing application-specific headers and footers in the log file.
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
