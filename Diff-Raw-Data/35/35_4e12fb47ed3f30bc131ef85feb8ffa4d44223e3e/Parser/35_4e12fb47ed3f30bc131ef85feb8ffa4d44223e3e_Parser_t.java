 package quoridor;
 
 
 /**
  * 
  * A framework for scanning and tokenising user input
  * 
  * @author steve
  *
  */
 public interface Parser {
 	
 	public boolean hasNextToken();
 	public short currentToken();
	public String currentCommand();
 	public void progressToken();
 	
 	public boolean hasNextArg();
 	public String nextArg();
 	
 	public boolean scanLine();
 	
 	public short ensureCommand();
 	
 	public boolean hasRequiredArgs();
 	public boolean hasErroneousArgs();
 	
 }
