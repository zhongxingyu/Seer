 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import java.util.Scanner;
 import java.awt.EventQueue;
 import java.io.File;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 /*  
 28 july 2013
 Authors: Alex Southwell
 Purpose: import/export/edit xml files for Legacy of Barubash
 */
  public class XmlParser
  {
 		static final int			DIALOGUE			= 0;
 		static final int			TRIGGER				= 1;
 		static final int			PATH				= 2;
 		static final int			DISPLAY				= 3;
 		static final int			CUTSCENE			= 4;
 		static final int			SETFLAG				= 5;
 		static final int			SHOP				= 6;
 		static final int			LOAD				= 7;
 		static final int			BUTTON				= 8;
 		static final int			CHEST				= 9;
 		static final int			SPEAK				= 10;
 		private static final int	CLEARCONTROLLED		= 11;
 		private static final int	SETPARAM			= 12;
 		private static final int	RESETCHAPTER		= 13;
 		static final int			LOOT				= 15;
 		static final int			MOBSNOTACTIVE		= 16;
 		static final int			SETQUADLAYER		= 17;
 		static final int			DROPLOOT			= 20;
 		
 		static final int			SETPLAYERSTATE		= 61;
 		
 		static final int			BUTTONSTACK			= 88;
 		static final int			DIALOGUEUPDATEPOP	= 80;
 		static final int			FOLLOW				= 81;
 		static final int			CLEARTRIGGERS		= 82;
 		static final int			UNFOLLOW			= 83;
 		static final int			PORTRAIT			= 70;
 		static final int			PORTRAITNAME		= 71;
 		static final int			CRYSTALPORT			= 73;
 		private static final int	ADDTRIGGER			= 84;
 		
 		static final int			COMMAND				= -1;					// executes a static predefined command eg close dialogue
 																				
 		static final int			CONDRUN				= -2;					// checks key operation value, if met perform action. Continues to process instructions;
 		static final int			CONDBRANCHRUN		= -5;					// checks key operation value, if met performs first action. else second action, continues to process instructions
 		static final int			CONDBRANCHJUMP		= -6;					// checks key operation value, if met performs first action. else second action, only if first met
 		static final int			CONDJUMP			= -3;					// checks key operation value, if met performs actions, only continues if not met.
 		static final int			CHECKPREREQ			= -4;					// checks agains a prerequesit
 		static final int			TAKEITEM			= -7;					// Removes item from inventory eg quest item
 																				
 		static final int			NULL				= -10;
 		
 		static final int			cCLOSE				= -1;
 		static final int			cMAINMENU			= -2;
 		static final int			cTELEPORT			= 1;
 		static final int			cQUEST				= 2;
 		static final int			cQUESTSTART			= 6;
 		static final int			cSYSMSG				= 3;
 		static final int			cTUTE				= 4;
 		static final int			cNOTIFY				= 5;
 		static final int			cCLEARSYSMSG		= 36;
 		static final int			cADDITIONAL			= 37;
 		static final int			cXP					= 38;
 		static final int			cCOMPLETEQUEST		= 39;
 		private static final int	cTRAP				= 40;
 		private static final int	cCLEARPOINTERS		= 50;
 		private static final int	cSETPOINTER			= 51;
 		private static final int	cSETPOINTERMOBS		= 52;
 		static final int			cMOVE				= 53;
 		static final int			cSWAPFG				= 54;
 		static final int			cUNDIE				= 55;
 		static final int			cPOP				= 56;
 		
 		static final String			sDIALOGUE			= "d_";
 		static final String			sTRIGGER			= "";
 		static final String			sPATH				= "p_";
 		static final String			sDISPLAY			= "cd_";
 		static final String			sCUTSCENE			= "cs_";
 		static final String			sSETFLAG			= "sf_";
 		static final String			sSHOP				= "s_";
 		static final String			sLOAD				= "l_";
 		static final String			sBUTTON				= "b_";
 		static final String			sCOMMAND			= "c_";
 	
         // Creates trigger objects
 		static trigger[] currentTrigger;
 		static additional[] currentAdditional;
 		static EditorUi currentUi;
 		
 	public static void main(String argv[])
 	{
 		
 		
 		EventQueue.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 			currentUi = new EditorUi();
 			importTriggerXml();
 			importAdditionalXml();
 			//displayAllAdditionals();
 			//editTriggerItems();
 			//searchTriggerName();
 			//exportXml();
 			populateAdditionalList();
 			populateTriggerList();
 			}
 		});
 		
 
 
 	}
 	
 	public static void populateTriggerList()
 	{
 		for (int i = 0; i<currentTrigger.length; i++)
 		{
 			System.out.println("Pop: " + i);
 			currentUi.addTriggerListItem(currentTrigger[i].getTriggerName());
 			}
 	}
 	
 	public static void populateTItemList(String triggerName)
 	{
 		for (int i = 0; i<currentTrigger.length; i++)
 		{
 			if (currentTrigger[i].getTriggerName().equals(triggerName))
 			{
 				String currentArray[] = currentTrigger[i].getArrayItems();
 				for (int j = 0; j<currentArray.length; j++)	
 				{
 					currentUi.addTItemListItem(currentArray[j]);
 				}
 			}
 		}
 		
 	}	
 	public static void populateAdditionalList()
 	{
 		for (int i = 0; i<currentAdditional.length; i++)
 		{
 			currentUi.addAdditionalListItem(currentAdditional[i].getAdditionalName());
 			}
 	}
 	
 	public static void populateAItemList(String AdditionalName)
 	{
		for (int i = 0; currentAdditional[i]!=null; i++)
 		{
 			if (currentAdditional[i].getAdditionalName().equals(AdditionalName))
 			{
 				String currentArray[] = currentAdditional[i].getArrayItems();
				for (int j = 0; currentArray[j]!=null; j++)	
 				{
 					currentUi.addAItemListItem(currentArray[j]);
 				}
 			}
 		}
 		
 	}
 	
 	
     public static void importTriggerXml()
     {
     	 int triggerCounter = 0;
          try {
         	 // Prepares the file for access
              File fXmlFile = new File("triggers.xml");
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
              Document doc = dBuilder.parse(fXmlFile);
           
              // optional, but recommended
              // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
              doc.getDocumentElement().normalize();
              
              // Selects which nodelist
              NodeList nList = doc.getElementsByTagName("string-array");
                        // Creates trigger objects
              currentTrigger = new trigger[nList.getLength()];
              
              // Loops all of the lists
              for (int temp = 0; temp < nList.getLength(); temp++) 
              {
             	 // Creates an array to be filled with items
                  String[] arrayItems = new String[20];
                  
                  // Selects which node 
                  Node nNode = nList.item(temp);
                  
                  // Checks for correct node type
                  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
                  {          
                 	 // Selects the element from the node
                 	 Element eElement = (Element) nNode;                
                      
                      // Loops all of the items in the node
                      for (int i=0; i<eElement.getElementsByTagName("item").getLength();i++)
                      {
                          // Stores item in the array
                     	 arrayItems[i]=eElement.getElementsByTagName("item").item(i).getTextContent();
                      }
                      
                      // Creates a new object with the Trigger name and array of items
                      currentTrigger[temp] = new trigger(eElement.getAttribute("name"),arrayItems);
                      
                      triggerCounter++;
                  }
  
              }
             // currentUi.updateTriggersLabel(Integer.toString(triggerCounter));
          } catch (Exception e) 
          {
              e.printStackTrace();
          }
     }
     
     public static void importAdditionalXml()
     {
     	 int additionalCounter = 0;
          try {
         	 // Prepares the file for access
              File fXmlFile = new File("additionals.xml");
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
              Document doc = dBuilder.parse(fXmlFile);
           
              // optional, but recommended
              // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
              doc.getDocumentElement().normalize();
              
              // Displays the node name
              System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
              
              // Selects which nodelist
              NodeList nList = doc.getElementsByTagName("string-array");
           
              System.out.println("----------------------------");
              
              // Creates additional objects
              currentAdditional = new additional[nList.getLength()];
              
              // Loops all of the lists
              for (int temp = 0; temp < nList.getLength(); temp++) 
              {                 
                  // Selects which node 
                  Node nNode = nList.item(temp);
                  
                  // Checks for correct node type
                  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
                  {          
                 	 // Selects the element from the node
                 	 Element eElement = (Element) nNode;                
                      
                 	 // Creates an array to be filled with items
                      String[] arrayItems = new String[eElement.getElementsByTagName("item").getLength()];
                 	 
                      // Loops all of the items in the node
                      for (int i=0; i<eElement.getElementsByTagName("item").getLength();i++)
                      {
                          // Stores item in the array
                     	 arrayItems[i]=eElement.getElementsByTagName("item").item(i).getTextContent();
                      }
                      
                      // Creates a new object with the Trigger name and array of items
                      currentAdditional[temp] = new additional(eElement.getAttribute("name"),arrayItems);
                      
                      additionalCounter++;
                  }
  
              }
              System.out.println("Additional imported: "+ additionalCounter);
          } catch (Exception e) 
          {
              e.printStackTrace();
          }
     }
     
     public static void displayAllAdditionals()
     {
     	for (int i = 0; i<currentAdditional.length;i++)
     	{
     		String newAdditionalName = currentAdditional[i].getAdditionalName();
     		System.out.println(newAdditionalName);
     		String newArray[] = currentAdditional[i].getArrayItems();
     		for (int j =0; j< newArray.length; j++)
     		{
     			System.out.println(newArray[j]);
     		}
     	}
     }
 
     public static void searchAdditionalName()
     {
     	// addnpc,imagetype,x,y,trigger,npctype,triggerfrobs?,name,gotox,gotoy,triggerEndMotion,ai,direction,animation
 		
     }
     
     public static void exportXml()
     {
     	try
     	{// Creates a file for writing
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
         Document doc = docBuilder.newDocument();
         Element rootElement = doc.createElement("resources");
         doc.appendChild(rootElement);
                 
         // Loops the objects into the file
 	        for (int i = 0; i< currentTrigger.length; i++)
 	        {
 	            Element stringArray = doc.createElement("string-array");
 	            rootElement.appendChild(stringArray);
 	            stringArray.setAttribute("name", currentTrigger[i].getTriggerName());
 	            
 	    		String newArray[] = currentTrigger[i].getArrayItems();
 	    		
 	    		for (int j =0 ; newArray[j]!=null; j++)
 	    		{
 	    			Element item = doc.createElement("item");
 	    			item.appendChild(doc.createTextNode(newArray[j]));
 	    			rootElement.appendChild(item);
 	    		}
 	    		
 	        }	
 	    		// write the content into xml file
 	    		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 	    		Transformer transformer = transformerFactory.newTransformer();
 	    		DOMSource source = new DOMSource(doc);
 	    		StreamResult result = new StreamResult(new File("newtriggers.xml"));
 	     
 	    		// Output to console for testing
 	    		// StreamResult result = new StreamResult(System.out);
 	     
 	    		transformer.transform(source, result);
 	     
 	    		System.out.println("File saved!");
 	        
 	    	} catch (ParserConfigurationException pce) {
 	    		pce.printStackTrace();
 	    	} catch (TransformerException tfe) {
 	    		tfe.printStackTrace();
 	    	}
     }
 
     public static void editTriggerItems()
     {
         Scanner in = new Scanner(System.in);
         
         System.out.print("Enter trigger number: ");
         int requestedTrigger = in.nextInt();
         
         System.out.print("Enter item number: ");
         int requestedItem = in.nextInt();
         
         in.nextLine();
         
         String currentArray[] = currentTrigger[requestedTrigger].getArrayItems();
         
         System.out.println("TriggerName: " + currentTrigger[requestedTrigger].getTriggerName() + " | ItemNumber: " + requestedItem);
         System.out.println(currentArray[requestedItem]);
         
         System.out.print("Enter new item value: ");
         String editedItem = in.nextLine();
                
         System.out.println("New value: ");
         System.out.println(editedItem);
         
         System.out.print("Confirm (y/n): ");
         String confirmItem = in.nextLine();
         
         if (confirmItem.equals("y") || confirmItem.equals("Y"))
         {
         	currentArray[requestedItem]= editedItem;
         	currentTrigger[requestedTrigger].setArrayItems(currentArray);
         	String a[] = currentTrigger[requestedTrigger].getArrayItems();
         	System.out.println(a[requestedItem]);
         }
     }
 
     public static void searchTriggerName()
     {
     	//Menu
         Scanner in = new Scanner(System.in);
         
         System.out.print("Enter trigger name: ");
         String requestedTrigger = in.nextLine();
         
         for (int i = 0; i < currentTrigger.length; i++ )
         {
        	 if (requestedTrigger.equals(currentTrigger[i].getTriggerName()))
        			 {
 	                     String aItems[] = currentTrigger[i].getArrayItems();
 	                     System.out.println("Trigger Name: " +currentTrigger[i].getTriggerName());
 	                     
 	                     
 	                     for (int t =0; aItems[t]!= null; t++ )
 	                     {
 	                        System.out.println("##############");
 	                    	 String[] itemContents =aItems[t].split("=");
 	                         int identifier = Integer.parseInt(itemContents[0]);
 	                 		if (itemContents.length < 2 && identifier <= 10)
 	                 		{}
 	                 		else
 	                 		{		                 		
 	                 			switch (identifier)
 	                 			{
 	                 				case CLEARCONTROLLED:
 	                 					break;
 	                 				case SETPLAYERSTATE:
 	                 					break;
 	                 				case DIALOGUE:
 	                 					System.out.println("DIALOGUE #0");
 			                 			System.out.println("");
 	                 					String [] params = itemContents[1].split("\\|");
 	                 					System.out.println("Id: " + params[0]);
 	                 					if (params.length >= 2)
 	                 						System.out.println("Defaultselection: " + params[1]);
 	                 					
 	                 					if (params.length >= 3)
 	                 						System.out.println("Timeout: " + params[2]);
 	                 					
 	                 					if (params.length >= 4)
 	                 						System.out.println("Imageid1: " + params[3]);
 	                 					
 	                 					if (params.length >= 5)
 	                 						System.out.println("Imageid2: " + params[4]);
 	                 					break;
 	                 				case PORTRAIT:
 	                 					System.out.println("Portait #70");
 			                 			System.out.println("");
 	                 					System.out.println("Draw Portait: "+ itemContents[1]);
 	                 					break;
 	                 				case PORTRAITNAME:
 	                 					System.out.println("Portait Name #71");
 			                 			System.out.println("");
 	                 					System.out.println("Portait Name: "+ itemContents[1]);
 	                 					break;
 	                 				case CRYSTALPORT:
 	                 					break;
 	                 				case TRIGGER:
 	                 					break;
 	                 				case BUTTON:
 	                 					System.out.println("Button #8");
 			                 			System.out.println("");
 	                 					String[] buttons = itemContents[1].split("\\|");
 	                 					for (int f =0 ; f < buttons.length; f++)
 	                 					{
 	                 						System.out.println("Button: " + buttons [f]);
 	                 					}
 	                 					break;
 	                 				case BUTTONSTACK:
 	                 					break;
 	                 				case FOLLOW:
 	                 					break;
 	                 				case UNFOLLOW:
 	                 					break;
 	                 				case CLEARTRIGGERS:
 	                 					System.out.println("Clear Triggers #82");
 			                 			System.out.println("");
 	                 					System.out.println("Clear Triggers! " + itemContents[1]);
 	                 					break;
 	                 				case ADDTRIGGER:
 	                 					break;
 	                 				case DIALOGUEUPDATEPOP:
 	                 					break;
 	                 				case LOAD:
 	                 					break;
 	                 				case SETQUADLAYER:
 	                 					break;
 	                 				case COMMAND:
 	                 					break;
 	                 				case CHECKPREREQ:
 	                 					break;
 	                 				case CHEST:
 	                 					break;
 	                 				case MOBSNOTACTIVE:
 	                 					break;
 	                 				case CONDBRANCHRUN:
 	                 					break;
 	                 				case CONDBRANCHJUMP:
 	                 					break;
 	                 				case CONDJUMP:
 	                 					break;
 	                 				case CONDRUN:
 	                 					break;
 	                 				case SPEAK:
 	                 					break;
 	                 				case SETPARAM:
 	                 					break;
 	                 				case RESETCHAPTER:
 	                 					break;
 	                 				case LOOT:
 	                 					break;
 	                 				case DROPLOOT:
 	                 					break;
 	                 				default:
 	                 		}
 	                 			System.out.println("");
 	                 			System.out.println("Trigger|Item: " + i + "|" + t );
 	                 			System.out.println("");
 	                     }
        			 }
        		}
         }
     }
  
  }
      
