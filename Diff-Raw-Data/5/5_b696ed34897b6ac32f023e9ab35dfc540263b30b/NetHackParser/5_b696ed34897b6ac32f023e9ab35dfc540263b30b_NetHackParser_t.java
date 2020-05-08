 package annahack.nethackparser;
 
 import annahack.telnetconnection.*;
 import java.util.Queue;
 import java.io.IOException;
 
 public class NetHackParser
 {
 	private TelnetInterface com;
 	private Queue<String> messageBuf;
 	private Queue<String> itemsBuf;
 	
 	public NetHackParser(TelnetInterface com)
 	{
 		this.com=com;
 		messageBuf=new java.util.LinkedList<String>();
 	}
 	
 	/**
 	 * @return
 	 * 0 if there were no messages,
 	 * 1 if there were messages
 	 * 2 if there were messages and space needs to be sent (--More--)
 	 * 3 if the last message is a prompt
 	 */
 	public byte checkMessages() throws IOException
 	{
 		String line;
 		line=new String(com.peekLine(0));
 		
 		if (line.equals("                        "))
 		{
 			//Nothing
 			return 0;
 		}else{
 			//Something
 			int search=line.indexOf("--More--");
 			if (search!=-1)
 			{
 				//Messages and --More--
 				String[] msgs=line.substring(0, search).split("  ");
 				for (int i=0; i<msgs.length; i++)
 				{
 					messageBuf.add(msgs[i].trim());
 				}
 				return 2;
 			}else{
 				//No --More-- on first line
 				
 				if (thingsThatAreHere(0))
 					return 2;
 				
 				if (line.matches("There is an? ([a-z ]+) here."))
 				{
 					messageBuf.add(line);
 					
 					if (thingsThatAreHere(2))
 						return 2;
 					
 					System.err.println("Unable to figure out line:\n"+line);
 					throw new UnparseableBullshitException(line);
 				}else{
 					
 				}
 				
 			}
 		}
 		
 		return 0;
 	}
 	
	/**
 	 * Checks for a "things that are here" starting on the given line.
 	 * Line should be 0 or 2.
 	 * Returns true if things are here.
	 **/
 	private boolean thingsThatAreHere(int linenum) throws IOException
 	{
 		String line=new String(com.peekLine(linenum));
 		
 		int search=line.indexOf("Things that are here");
 		
 		if (search==-1)
 			return false;	//Clearly, things are not here
 		
 		//Item list
 		for (int i=1;
 		(line=new String(com.peekLine(i), search, 80-search).trim()).
 			indexOf("--More--")==-1; i++)
 		{
 			itemsBuf.add(line);
 		}
 		
 		return true;
 	}
 	
 }
