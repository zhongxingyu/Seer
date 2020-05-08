 package anchovy.io;
 
 import java.io.FileNotFoundException;
 import java.util.StringTokenizer;
 
 import anchovy.GameEngine;
 import anchovy.InfoPacket;
 import anchovy.Pair;
 import anchovy.Components.*;
 import anchovy.Pair.Label;
 /**
  * This class is responsible for parsing text commands supplied by the user and then executing a fitting gameEngine method
  * or interacting with a certain component
  * Valid commands are: save; save as; show saves; load; valveName open/close
  * pumpName RPM/rpm number;
  * Control Rods name set/SET number;
  * componentName repair
  * 
  */
 public class Parser {
 	private GameEngine engine;
 
 	public Parser(GameEngine Engine)
 	{
 		engine = Engine;
 	}
 
 	/**
 	 * Using the two input string determines what command should be executed by the gameEngine or 
 	 * one of the components
 	 * @command command Command that will applied to a component
 	 * @param componentName part of the input supplied by the user
 	 * @return returns a message to the user that gets written into the output of the console
 	 */
 	public String parseCommand(String componentName, String command)
 		{
 		String result = new String();
 		//Check for for engine commands first
 		if (componentName.contains("save as")) {
 			return engine.saveGameState(engine.getAllComponentInfo(), command);
 		} else if (componentName.equals("save")) {
 			return engine.save(engine.getAllComponentInfo());
 			
 		} else if (componentName.contains("load")) 
 		{
 			
 			try {
 				engine.readfile(command);
				engine.updateInterfaceComponents(engine.getAllComponentInfo());
				return "Loaded game Successfully";
 			} catch (FileNotFoundException e) {
 				return "File not found.";
 			}
			
 
 		} else if (componentName.equals("show saves")) {
 			return engine.findAvailableSaves();
 		} else if (componentName.equals("new game")){
 			try {
 				engine.readfile("newgame");
 				engine.updateInterfaceComponents(engine.getAllComponentInfo());
 				return getGameIntroString(command);
 			} catch (FileNotFoundException e) {
 				return "File not found.";
 			}
 			
 		} else if (componentName.equals("exit") | componentName.equals("quit")){
 			System.exit(0);
 		}
 		
 		//then check for components
 		try {
 			InfoPacket i = new InfoPacket();
 			Component component = null;
 			
 			//Used for components that also need a numerical parameter (pumps and control rods for now)
 			if (componentName.contains("Pump") || componentName.contains("rods")) 
 			{
 				String alterName = componentName.substring(0, componentName.lastIndexOf(' '));
 				component = engine.getPowerPlantComponent(alterName);
 				if (componentName.contains("RPM") || componentName.contains("rpm")) 
 				{
 					i.namedValues.add(new Pair<Double>(Label.RPMs, Double.parseDouble(command)));
 				} else if(componentName.contains("set") || componentName.contains("SET")) 
 				{
 					i.namedValues.add(new Pair<Double>(Label.coRL, Double.parseDouble(command)));
 				}
 			} 
 			else
 			{
 				component = engine.getPowerPlantComponent(componentName);
 				
 				if (component.getName().contains("Valve")) 
 				{
 					// i.namedValues.add(new Pair<String>(Pair.Label.cNme,
 					// component.getName()));
 					if (command.equals("open")) {
 						i.namedValues.add(new Pair<Boolean>(Pair.Label.psit, true));
 					} else if (command.equals("close")) 
 					{
 						i.namedValues
 								.add(new Pair<Boolean>(Pair.Label.psit, false));
 					}
 				} 
 	
 				else if (command.equals("repair")) 
 				{
 					engine.repair(component);
 				}
 			}
 			
 			try {
 				component.takeInfo(i);
 			} catch (Exception e) {
 				return e.getMessage();
 			}
 		}
 		catch(Exception e)
 		{
 			result = "Component " + componentName + " does not exist";
 		}
 
 		engine.updateInterfaceComponents(engine.getAllComponentInfo());
 		return result;
 	}
 	
 	/**
 	 * Checks for bad input and splits the string in two tokens, then calls parseCommand.
 	 * @param in an input string (usually coming from the UI)
 	 * @return a message for the user
 	 * @throws FileNotFoundException
 	 */
 	public String parse(String text)
 	{
 		if(text.length() != 0)
 		{
 			String lowerCase= text;
 			if(lowerCase.equals("save") || lowerCase.equals("exit") || lowerCase.equals("quit"))
 			{
 				return parseCommand(lowerCase,lowerCase);
 			}
 			//String lowerCase = text.toLowerCase(); //Convert string to lower case to avoid case mismatches
 			
 			if(lowerCase.contains(" "))
 			{
 				if(lowerCase.equals("show saves") || lowerCase.equals("save as") || lowerCase.equals("new game"))
 					return parseCommand(lowerCase, lowerCase);
 				String s= lowerCase.substring(0, lowerCase.lastIndexOf(' '));
 				String i= lowerCase.substring(lowerCase.lastIndexOf(' ') + 1, lowerCase.length());
 
 				
 
 			return parseCommand(s,i);
 			}
 			
 			else return "Invalid command";
 		}
 
 		else return "";
 	}
 
 	/**
 	 * Method containing the background story of the game hardcoded into it. Brings the story and the user together.
 	 * @param userName The name of the user.
 	 * @return The story with the users name in it.
 	 */
 	public String getGameIntroString(String userName){
 		return "Fire Blanket " + 
 				"On the 8th April 2013, students at the University of York prepared to demonstrate their second year software projects. " +
 				"A challenging task, several teams had prepared video game simulations of nuclear power plants for their professor's approval. " +
 				"Some projects were simple stand-alone affairs powered only by the department's computers. Others utilised the internet to provide " +
 				"automatic updates. One such system would change the world forever. \n" +
 				"Precisely one week from the initial deployment of the SEPR solutions, disaster shook the world. " +
 				"Propagating from one server to the next using obscure net technologies, an unidentified program began initiating the update protocols of the world's major nuclear power plants. " +
 				"Inexplicably replacing SCADA systems with the student's simulations, the world's power plants began melting down in quick succession. \n"+
 				"The ensuing blanket of fire engulfed the world, reducing civilisation to smouldering ash. \n"+
 				"But the human race proved more resilient than could have been hoped, and pockets of life survived. " +
 				"One such region, spared of destruction by the recent decommission of its own power plant was none other than the city that spawned its downfall. " +
 				"York had survived, and with it, it's university. \n"+
 				"In the weeks that followed, urgent discussion took place across what was left of the city. " +
 				"Unified by an impromptu council of community leaders and prominent academics, the course was set for humanity's fate. " +
 				"The survivors would require food, shelter, infrastructure and means of self-protection, and for this they would require electricity. \n"+
 				"Enlisting the might of the nearby garrison of Northern Command, " +
 				"the council lead its citizens on a journey to re-commission its fatefully dormant nuclear facility.  \n"+
 				"Months passed, and the decision to mobilise Northern Command proved to be a crucial one. " +
 				"The first signs of mutation arose in the form of rodents emerging from the irradiated ground. Whilst of little danger to humans, " +
 				"these rodents would herald the coming of a much greater threat to humanity. \n"+
 				"With dwindling supplies, and rumours of larger mutating creatures converging towards the sounds of construction, " +
 				"the military remnants began a fevered fortification of the power plant. Enmeshed within electric fences and weapons systems, " +
 				"humanity's final revival would likely be carried out against the shadow of constant siege. " +
 				"And to some, lingering memories of the destructive potential of nuclear power outweighed the protection that it afforded... \n"+
 				"Six months have passed since the blanket of fire descended and the newly established outpost of Town has been born. " +
 				"The nuclear facility for which the town was created has been successfully commissioned by a team of surviving engineers lead by Professor Richard Paige, " +
 				"and in which you were involved. " +
 				"Following the untimely digestion of a senior operator by a stray mutated goose, " +
 				"you have been assigned a more prominent role in the functioning of the plant. " +
 				"Your presence in the control room has been requested by the Professor to explain your new responsibilities... \n"+
 				"\n"+
 				"*knocks*\n"+
 				"Richard: Come in! Hey, I recognise you from SEPR. Your name is...?\n"+
 				userName +"\n" +
 				"Richard: " + userName + ". That's right; I thought it was something like that. " +
 						"So I've heard that you've been promoted to replace our previous senior operator.  " +
 						"Terrible thing that happened to him, I can't help wonder how a mutated goose made it past our defences. " +
 						"Well never mind, the job's yours now anyway, congratulations.\n"+
 				userName +": So what are my new responsibilities?\n"+
 				"Richard: Well, you have to manage the power plant's operation." +
 				" All of the more experienced engineers have been asked to help design additional infrastructure for the town, " +
 				"so you're the most qualified person left to make decisions about the facility's operation. " +
 				"That said you'll have a team of people who will implement the changes you request in the command terminal and someone else to distribute the power that you generate to the city. \n"+
 				"Richard: As you know, Town's population and industry is growing at quite a substantial rate so you're going to have your work cut out keeping up with demand. " +
 				"Additionally, the military will require prioritised use of the power to ensure that the city's defences remain operable.\n"+
 				userName + ": I see.\n"+
 				"Richard: The Lieutenant here will explain Town's structure and defensive capabilities in more detail.\n"+
 				"Lieutenant:	Yes. The power generated by this facility is directed into three different zones. " +
 				"The first, and from a survival perspective the least important, is that of the city itself. Should you be unable to generate enough power, " +
 				"we'll cut off civilian homes and factories first. Next is the electric fence around the perimeter of the city, " +
 				"this is our first line of defence against the mutants. As more mutants gather, the fence will require more energy to repel them." +
 				" If the fence falls, then the city will be invariably overrun. Finally, and our highest priority is this facility's own defence systems. " +
 				"Over the last half year, we've built and supplied a fortified garrison around the power plant and this will be our final line of defence. If power goes out here, then we'll lose our Helicopters, SUVs, Weapons Systems, Emergency Power Grid and ultimately our lives.\n"+
 				"*Gulp*\n"+
 				userName + ": No pressure then...\n"+
 				"Richard: As I said, distribution of power will managed for you, but it's worthwhile considering the potential consequences of not outputting enough power from the system. " +
 				"Of course, if you try to generate too much power, the plant could overheat and we'll end up just as evaporated as every other region in the country.\n"+
 				userName + ": ...\n"+
 				"Richard: So, the power plant is in your hands! We don't have time to train you, but you can refer to your instruction manual if you forget the terminal's commands. " +
 				"Try not to blow everything up!\n"+
 				"*Richard and the Lieutenant leave the control room leaving you stood alone. " +
 				"Walking up to the facility's command system, you hope to redeem your past nuclear failings, " +
 				"and swear never to copy strange code from the internet again*\n";
 	}
 }
