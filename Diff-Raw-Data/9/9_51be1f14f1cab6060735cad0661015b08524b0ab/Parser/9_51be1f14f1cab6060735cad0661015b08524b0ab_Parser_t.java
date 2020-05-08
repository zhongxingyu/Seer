 import java.io.IOException;
 import java.util.Stack;
 
 public class Parser
 {
 	private Stack<String> expandedNodes = new Stack<String>();
 	private int thisLevel;
 	private int previousLevel;
 	
 	public Parser(FileRead fr, FileWrite fw) throws IOException
 	{
 		String check = "";
 		String previousLine = fr.read();
 		String previousValues[] = previousLine.split("\\s+",3);
 		previousLevel = Integer.parseInt(previousValues[0]);
 		String thisLine = fr.read();
 		
 		while (!check.equals("EOF"))
 		{	
 			if(thisLine.equals("EOF"))
 			{
 				thisLine = "0 FINA Finish off";
 				check = "EOF";
 			}
 			String thisValues[] = thisLine.split("\\s+",3);
 			thisLevel = Integer.parseInt(thisValues[0]);
 			
 			if(thisLevel > previousLevel)
 			{
 				
 				if(expandedNodes.empty() && previousValues[1].startsWith("@") && previousValues[1].endsWith("@"))
 				{
 					expandedNodes.push(previousValues[2].toLowerCase());
 					fw.writeIndiTags(previousValues);
 				}
 				else
 				{
 					if(expandedNodes.empty())
 					{
 						if (previousValues.length == 3)
 						{
 							fw.writeTags(previousValues);
 						}
 						else
 						{
 							fw.writeNoValueTag(previousValues);
 						}
 					}
 					else
 					{
 						expandedNodes.push(previousValues[1].toLowerCase());
						if (previousValues.length == 3)
						{
							fw.writeValueTags(previousValues);
						}
						else
						{
							fw.writeOpenTag(previousValues);
						}
 					}
 					
 				}
 				
 			}
 			else
 			{
 				if(thisLevel == previousLevel)
 				{
 					if (previousValues.length == 3)
 					{
 						fw.writeTags(previousValues);
 					}
 					else
 					{
 						fw.writeNoValueTag(previousValues);
 					}
 				}
 				else
 				{
 					if (previousValues.length == 3)
 					{
 						fw.writeTags(previousValues);
 					}
 					else
 					{
 						fw.writeNoValueTag(previousValues);
 					}
 					
 					int count = previousLevel-thisLevel;
 					
 					for(int i=0; i<count; i++)
 					{
 						String temp = expandedNodes.pop();
 						fw.writeCloseTags(temp, expandedNodes.size()+2);
 					}
 					
 				}
 			}
            
 			previousLevel = thisLevel;
 			previousValues = thisValues;
 			if (!(check.equals("EOF")))
 			thisLine = fr.read();
 			
 		}
 		
 		fw.close();
 		
 	}
 }
