 package game.interactables;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 import game.GameObject;
 import game.gameplayStates.GamePlayState;
 import game.gameplayStates.VirtualRealityHome;
 import game.player.Player;
 
 public class TableToHack extends GameObject implements Interactable {
 	private int m_stage = 0;
 
 	private Boolean m_door = false, m_bed = false, m_tv = false;
 	public TableToHack(String name, int xLoc, int yLoc) 
 			throws SlickException, FileNotFoundException, UnsupportedEncodingException {
 		super(name);
 		m_x = xLoc;
 		m_y = yLoc;
 		// stage 1
 		setSprite(new Image("assets/gameObjects/table.png"));
 		PrintWriter writer = new PrintWriter("brain.txt", "UTF-8");
 		writer.print("HackCode:");
 		writer.close();	
 		File l = new File("leonardo.txt");
 		l.delete();
 		File j = new File("virtualREALity.JSON");
 		j.delete();
 	}
 
 	@Override
 	public Interactable fireAction(GamePlayState state, Player p) {
 		if (m_stage == 0) {
 			if (queryAnswer1().compareTo("") == 0) { // default
 				state.displayDialogue(new String[] {"You hope your computer can give you some answers...",
 							"\"404\"",
 							"You wonder if you can use your computer to hack the virtual machine chairs.",
 							"\"Dig deeper in to virtual world. Dig deeper into YOUR virtual world.\"",
 							"\"You must think outside the box: even beyond the box in which this world exists\"",
 							"\"Find the brain that is the clue. Clean our minds and keep it true.\"",
 							"Curious curious curious. You ponder deeply at what your computer is trying to tell you.",
 							"Finally, a strange message pops up on the screen: \"male donkey + female horse = ?\""});
 			}
 			else if (queryAnswer1().compareTo("") != 0 && 
					 queryAnswer1().contains("mule") != true) { // wrong answer but something was written
 				state.displayDialogue(new String[] {"Your computer is spazzing out..",
 						"Looks like you've accessed the system but didn't get it quite right.."});
 			}
 			else if (queryAnswer1().contains("mule")) { // right answer
 				m_stage++;
 				File l = new File("leonardo.txt");
 				l.delete();
 				state.displayDialogue(new String[] {"The screen turns white and you hear a slight buzzing",
 						"You wonder to yourself if that even did anything.",
 						"More text appears on the screen: \"Like a new born pheonix or bathtub foam\"",
 						"\"make from nothingness the fibonacci poem\"",
 						"\"Starting at nothing to no greater than 10,\"",
 						"\"Ignore all spaces to hack me again\"",
 						"\"And like Davinci, dear fibonacci shares\"",
 						"\"A commonality - the answer. Name it with care.\""});
 			}
 		}
 		else if (m_stage == 1) {
 			if (queryAnswer2().compareTo("404") == 0) {
 				state.displayDialogue(new String[] {"\"Like a new born pheonix or bathtub foam\"",
 						"\"make from nothingness the fibonacci poem\"",
 						"\"Starting at nothing to no greater than 10,\"",
 						"\"Ignore all spaces to hack me again\"",
 						"\"And like Davinci, dear fibonacci shares\"",
 						"\"A commonality - the answer. Name it with care.\""});
 			}else if (queryAnswer2().compareTo("0112358") == 0) {
 				m_stage++;
 				state.displayDialogue(new String[] {"The screen turns white again and the buzzing continues",
 						"The whole room suddenly starts to shake a litte...",
 						".. but just barely enough to notice. You realize that you must be breaking something "+
 						"about the virtual reality space with these puzzles",
 						"Wait...",
 						"...",
 						"Looks like another file appeared",
 						"VirtalRealityState.JSON",
 						"Maybe you should be careful with this one",
 						"\"\""});
 				this.writeJSON("hungry", "unhappy", "minimal", "complacent", "uncomfortable", "locked");
 				try{
 					Sound buzz = new Sound("assets/sounds/smallBuzz.wav");
 					buzz.play();
 				}catch(SlickException e){
 					System.out.println("ERROR: trouble loading small buzz");
 				}
 				state.shakeCamera(2000);	
 			}else{
 				state.displayDialogue(new String[] {"The computer spits out some text",
 						"File leonardo.txt found: password failed. Re-reading poem",
 						"\"Like a new born phoenix or bathtub foam\"",
 						"\"make from nothingness the fibonacci poem\"",
 						"\"Starting at nothing to no greater than 10,\"",
 						"\"Ignore all spaces to hack me again\"",
 						"\"And like Davinci, dear fibonacci shares\"",
 						"\"A commonality - the answer. Name it with care.\""});
 			}
 		}
 		else if (m_stage == 2) {
 			String st = queryAnswer3();
 			if (st.compareTo("404")==0){
 				state.displayDialogue(new String[] {
 						
 				});
 			} else if (st.compareTo("corrupted")==0){
 				state.displayDialogue(new String[] {
 						"ERROR: Data irreperably corrupted. Restoring original state",
 				});
 				writeJSON("hungry", "unhappy", "minimal", "complacent", "uncomfortable", "locked");
 			} else{
 				parseAnswer3(state, st);
 				finish(state);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * called when finish with puzzles
 	 * @param state
 	 */
 	public void finish(GamePlayState state) {
 		((VirtualRealityHome)state).completePuzzles();
 	}
 	
 	//this method figures out what dialogs to play based on the mood put into the JSON
 	public void parseAnswer3(GamePlayState state, String st){
 		String[] results = st.split("\n");
 		System.out.println(st);
 		ArrayList<String> compResponse = new ArrayList<String>();
 		compResponse.add("Compiling...");
 		compResponse.add("Updating...");
 		if(results[results.length-1].equals("error")){
 			compResponse.add("The computer spits out a message. It seems that some of the paramaters you added were not recognized by the system, and have been replaced by the defaults");
 		}
 		if(results[2]!="."){
 			String line2 = "As the computer flashes you catch your refleciton in the monitor. ";
 			if(results[2].equals("sexy")){
 				line2+= "Damn, you are looking fine! You're looking more delicious than a t-bone steak, grilled to perfection";
 			}else{
 				line2+= "You are not looking your best. Plus you've got a stench like rotting meat. It's a wonder the flies haven't started to settle";
 			}
 			compResponse.add(line2);
 		}
 		if(results[0]!="."){
 			String line3 = "Come to think of it, when's the last time you ate?";
 			if(results[0].equals("full")){
 				line3 += " You guess it doesn't matter anyway. That stomach of yours is feeling pretty full.";
 			}else{
 				line3+= " Your stomach rumbles as you begin to fantasize about peeling the flesh off a papaya and devouring it piece by piece";
 			}
 			compResponse.add(line3);
 		}
 		if(results[1]!="."){
 			String line4 = "A sudden burst of feeling washes over you. ";
 			if(results[1].equals("happy")){
 				line4 += "You're euphoric! You just wanna take off your shirt and roll around the room. That's how good you feel right now";
 			}else{
 				line4 += "This is a tragedy. This virtual world, so close to the real thing, and yet so far. You wonder if you'll ever love again.";
 			}
 			compResponse.add(line4);
 		}
 		if(results[3]!="."){
 			String line5 = "";
 			if(results[3].equals("driven")){
 				line5 += "Get it together! You've got work to do. You could fix poverty, change the world, end wars. Or maybe get out of this room. Yeah, that's a good start";
 			}else{
 				line5 += "Whatever. Might as well just watch some tv or something";
 			}
 			compResponse.add(line5);
 		}
 		String[] finalRes = new String[compResponse.size()];
 		compResponse.toArray(finalRes);
 		state.displayDialogue(finalRes);
 	}
 	/**
 	 * call when done with puzzles
 	 * @param state
 	 */
 	
 	public String queryAnswer3() {
 		String ret = "";
 		FileReader fileReader;
 		try {
 			fileReader = new FileReader("virtualREALity.JSON");
 			BufferedReader br = new BufferedReader(fileReader);
 			String json = "";
 			String s = br.readLine();
 			while(s!=null){
 				json +=s;
 				s = br.readLine();
 			}
 			fileReader.close();
 			JSONParser parser = new JSONParser();
 			JSONObject obj = (JSONObject) parser.parse(json);
 			JSONObject player = (JSONObject) obj.get("player");
 			JSONObject room = (JSONObject) obj.get("room");
 			if(player==null||room==null){
 				return "corrupted";
 			}
 			boolean error = false;
 			String responseString = "";
 			String bed = ((String) room.get("bed"));
 			if(bed!=null){
 				bed = bed.toLowerCase();
 			}
 			String door = ((String) room.get("door"));
 			if(door!=null){
 				door = door.toLowerCase();
 			}
 			String hunger = ((String) player.get("hunger"));
 			if(hunger!=null){
 				hunger=hunger.toLowerCase();
 			}
 			String happiness = ((String) player.get("happiness"));
 			if(happiness!=null){
 				happiness = happiness.toLowerCase();
 			}
 			String sexAppeal = ((String) player.get("sex appeal"));
 			if(sexAppeal!=null){
 				sexAppeal = sexAppeal.toLowerCase();
 			}
 			String drive = ((String) player.get("drive"));
 			if(drive!=null){
 				drive = drive.toLowerCase();
 			}
 			if(bed==null||drive==null||door==null||hunger==null||happiness==null||sexAppeal==null||drive==null){
 				return "corrupted";
 			}
 			if(bed.compareTo("soft")==0||bed.compareTo("comfortable")==0||bed.compareTo("tempurpedic")==0||bed.compareTo("luxurious")==0||bed.compareTo("comfy")==0||bed.compareTo("cozy")==0){
 				m_bed = true;
 			}else{
 				if(bed.compareTo("prison bed")==0||bed.compareTo("rock hard")==0||bed.compareTo("hard")==0||bed.compareTo("uncomfortable")==0){
 					m_bed = false;
 				}else{
 					error = true;
 					bed = "uncomfortable";
 				}
 			}
 			if(door.compareTo("unlocked")*door.compareTo("open")==0){
 				m_door = true;
 			}else{
 				if(door.compareTo("locked")==0){
 					m_door = false;
 				}else{
 					error = true;
 					door = "locked";
 				}
 			}
 
 			if(hunger.compareTo("full")*hunger.compareTo("satisfied")*hunger.compareTo("stuffed")*hunger.compareTo("satiated")*hunger.compareTo("low")==0){
 				responseString += "full\n";
 			}else{
 				if(hunger.compareTo("hungry")*hunger.compareTo("starving")*hunger.compareTo("unsatisfied")*hunger.compareTo("empty-stomached")==0){
 					responseString += "hungry\n";
 				}else{
 					error = true;
 					responseString +=".\n";
 					hunger = "hungry";
 				}
 			}
 			if(happiness.compareTo("happy")*happiness.compareTo("joyful")*happiness.compareTo("satisfied")*happiness.compareTo("mirthful")*happiness.compareTo("blessed")==0){
 				responseString += "happy\n";
 			}else{
 				if(happiness.compareTo("glum")*happiness.compareTo("sad")*happiness.compareTo("despondent")*happiness.compareTo("depressed")*happiness.compareTo("unhappy")==0){
 					responseString += "sad\n";
 				}else{
 					error = true;
 					responseString += ".\n";
 					happiness = "unhappy";
 				}
 			}
 			if(sexAppeal.compareTo("sexy")*sexAppeal.compareTo("hot")*sexAppeal.compareTo("swaggy")*sexAppeal.compareTo("bangin")*sexAppeal.compareTo("studly")==0){
 				responseString += "sexy\n";
 			}else{
 				if(sexAppeal.compareTo("none")*sexAppeal.compareTo("unsexy")*sexAppeal.compareTo("swagless")*sexAppeal.compareTo("minimal")*sexAppeal.compareTo("lame")*sexAppeal.compareTo("nonexistant")==0){
 					responseString += "unsexy\n";
 				}else{
 					error = true;
 					responseString += ".\n";
 					sexAppeal = "minimal";
 				}
 			}
 			if(drive.compareTo("driven")*drive.compareTo("ambitious")*drive.compareTo("strong")*drive.compareTo("unstoppable")*drive.compareTo("motivated")==0){
 				responseString += "driven\n";
 			}else{
 				if(drive.compareTo("unambitious")*drive.compareTo("complacent")*drive.compareTo("lazy")*drive.compareTo("unmotivated")==0){
 					responseString += "complacent\n";
 				}else{
 					error = true;
 					responseString += ".\n";
 					drive = "complacent";
 				}
 			}
 			if(error){
 				responseString += "error\n";
 				writeJSON(hunger, happiness, sexAppeal, drive, bed, door);
 			}
 			return responseString;
  		}catch (FileNotFoundException e){
 			return "404";
 		}catch (IOException e) {
 			System.out.println("Query 3 IOException");
 		} catch (ParseException e) {
 			
 			return "corrupted";
 		}
 		
 		return ret;
 	}
 	public String queryAnswer2() {
 		String ret = "";
 		FileReader fileReader;
 		try {
 			fileReader = new FileReader("leonardo.txt");
 			BufferedReader bufferedReader = new BufferedReader(fileReader);
 			StringBuilder string = new StringBuilder();
 			String line = null;
 			while((line = bufferedReader.readLine()) != null) 
 				string.append(line+"");
 			bufferedReader.close();
 			ret = string.toString();
 			System.out.println("read: " + ret + " | size: " + ret.length());
 		} catch (FileNotFoundException e) {
 			return "404";
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	public String queryAnswer1() {
 		String ret = "";
 		FileReader fileReader;
 		try {
 			fileReader = new FileReader("brain.txt");
 			BufferedReader bufferedReader = new BufferedReader(fileReader);
 			StringBuilder string = new StringBuilder();
 			String line = null;
 			while((line = bufferedReader.readLine()) != null) 
 				string.append(line+"");
 			bufferedReader.close();
 			ret = string.toString().substring(9);
 			System.out.println("read: " + ret + " | size: " + ret.length());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	@Override
 	public Types getType() {
 		return Types.TABLE_TO_HACK;
 	}
 	private void writeJSON(String hunger, String happiness, String sexAppeal, String drive, String bed, String door){
 		try {
 			PrintWriter writer = new PrintWriter("virtualREALity.JSON", "UTF-8");
 			writer.print(""+
 			"{\n"+
 			"\t\"player\": {\n"+
 			"\t\t\"hunger\":\""+hunger+"\",\n"+
 			"\t\t\"happiness\":\""+happiness+"\",\n"+
 			"\t\t\"sex appeal\":\""+sexAppeal+"\",\n"+
 			"\t\t\"drive\":\""+drive+"\",\n"+
 			"\t},\n"+
 			"\t\"room\": {\n"+
 			"\t\t\"bed\":\""+bed+"\",\n"+
 			"\t\t\"door\":\""+door+"\",\n"+
 			"\t}\n"+
 			"}");
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public boolean getDoor(){
 		return m_door;
 	}
 	public boolean getBed(){
 		return m_bed;
 	}
 	public boolean getTV(){
 		return m_tv;
 	}
 }
