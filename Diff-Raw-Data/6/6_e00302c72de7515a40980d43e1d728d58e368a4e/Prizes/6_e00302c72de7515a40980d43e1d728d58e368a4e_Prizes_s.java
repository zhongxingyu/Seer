 package org.titanomachia.mclogcmdexec.command;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.titanomachia.mclogcmdexec.ApplicationContext;
 
 public class Prizes extends Command {
 	@Override
 	public void execute() {
 		Integer points = ApplicationContext.getValue("quiz.points." + getUser());
 
 		Map<String, Prize> prizes = getPrizes(points, "list".equals(getArgs().trim()));
 		
 		if (getArgs().trim().length() == 0) {
 			showAvailablePrizes(points, prizes);
 			return;
 		}
 		else if ("list".equals(getArgs().trim())) {
 			showAllPrizes(prizes);
 			return;
 		}
 		
 		if (null == points || 0 == points) {
 			CommandUtils.writeToConsole("You have no points", getUser());
 			return;
 		}
 		
     	int argIndex = 0, nextIndex = getArgs().indexOf(" ");
     	if (nextIndex < 0) {
     		nextIndex = getArgs().length();
     	}
     	String prizeId = getArgs().substring(argIndex, nextIndex);
     	if (!prizes.containsKey(prizeId.toUpperCase())) {
     		CommandUtils.writeToConsole("\"" + prizeId + "\" is not available", getUser());
     		return;
     	}
     	
     	Prize prize = prizes.get(prizeId.toUpperCase());
     	
     	Integer count = 1;
     	
     	argIndex = nextIndex;
     	if (argIndex < getArgs().length()) {
 	    	nextIndex = getArgs().indexOf(" ", argIndex + 1);
 	    	if (nextIndex < 0) {
 	    		nextIndex = getArgs().length();
 	    	}
 			String countStr = getArgs().substring(argIndex + 1, nextIndex);
 			try {
 				count = Integer.valueOf(countStr);
 			}
 			catch (NumberFormatException e) {
 				CommandUtils.writeToConsole("\"" + countStr + "\" is not a valid value for count", getUser());
 				return;
 			}
 	    	
 	    	if (count * prize.cost > points) {
 	    		CommandUtils.writeToConsole("You can't get that many " + prize.name, getUser());
 	    		return;
 	    	}
     	}
     	
     	for (int i = 0; i < count; i++) {
 			try {
 				Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", "echo \"give " + getUser() + " " + prize.typeId + " " + prize.count + "\" > console.pipe"}).waitFor();
 			}
 			catch ( InterruptedException e ) {
 			    e.printStackTrace();
 			}
 			catch ( IOException e ) {
 			    e.printStackTrace();
 			}
 			
 			points -= prize.cost;
 
 			ApplicationContext.setValue("quiz.points." + getUser(), points);
     	}
     	
     	ApplicationContext.setValue("quiz.streak." + getUser(), new ArrayList<Problem<?>>());
     	
     	CommandUtils.writeToConsole("You now have " + points + " point" + (points == 1 ? "" : "s"), getUser());
 	}
 
	private Map<String, Prize> getPrizes(Integer points, boolean all) {
 		Map<String, Prize> prizes = new HashMap<String, Prize>();
 		try {
 			Properties prizeProperties = new Properties();
 			prizeProperties.load( new FileReader("prizes.properties") );
 			for (Object key : prizeProperties.keySet()) {
 				String id = String.valueOf(key).toUpperCase();
 				
 				String value = (String)prizeProperties.get(key);
 				String name = value.substring(0, value.indexOf("("));
 				Integer count = Integer.valueOf(value.substring(value.indexOf("(") + 1, value.indexOf(")")));
 				Integer typeId = Integer.valueOf(value.substring(value.indexOf("[") + 1, value.indexOf("]")));
 				Integer cost = Integer.valueOf(value.substring(value.indexOf("=") + 1));
 				if (all || points >= cost) {
 					prizes.put(id, new Prize(count, name, typeId, cost));
 				}
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println("Failed to load prizes " + e);
 			CommandUtils.writeToConsole("Sorry, prizes not available", getUser());
 			return prizes;
 		} catch (IOException e) {
 			System.err.println("Failed to load prizes " + e);
 			CommandUtils.writeToConsole("Sorry, prizes not available", getUser());
 			return prizes;
 		}
 		return prizes;
 	}
     
     private void showAvailablePrizes(Integer points, Map<String, Prize> prizes) {
     	CommandUtils.writeToConsole("You have " + points + " point" + (points == 1 ? "" : "s") + ", prizes available:", getUser());
     	
     	if (prizes.keySet().size() > 0) {
 	    	List<String> ids = new ArrayList<String>(prizes.keySet());
 	    	Collections.sort(ids);
 	    	
 	    	for (String id : ids) {
 	    		Prize prize = prizes.get(id);
 	    		writePrizeToConsole(id, prize);
 	    	}
 	    	
 	    	CommandUtils.writeToConsole("Remember, purchasing ends your streak", getUser());
     	}
     	else {
     		CommandUtils.writeToConsole("Sorry, you need more points", getUser());
     	}
 	}
     
     private void showAllPrizes(Map<String, Prize> prizes) {
     	CommandUtils.writeToConsole("Prize list:", getUser());
     	
     	if (prizes.keySet().size() > 0) {
 	    	List<String> ids = new ArrayList<String>(prizes.keySet());
 	    	Collections.sort(ids);
 	    	
 	    	for (String id : ids) {
 	    		Prize prize = prizes.get(id);
 	    		writePrizeToConsole(id, prize);
 	    	}
 	    	
 	    	CommandUtils.writeToConsole("Remember, purchasing ends your streak", getUser());
     	}
 	}
 
 	private void writePrizeToConsole(String id, Prize prize) {
 		CommandUtils.writeToConsole(StringUtils.rightPad(id, 6) + "|" + StringUtils.leftPad(String.valueOf(prize.count), 4) + " " + StringUtils.rightPad(prize.name, 15) + prize.cost, getUser());
 	}
 
 //	private void showUsage() {
 //    	CommandUtils.writeToConsole("Usage: Prizes {prizeId {count}}", getUser());
 //    	CommandUtils.writeToConsole("Prizes by itself will list available prize Ids", getUser());
 //    	CommandUtils.writeToConsole("e.g. Prizes 11 2", getUser());
 //    }
 	
 	static class Prize {
 		String name;
 		Integer typeId;
 		Integer count;
 		Integer cost;
 		
 		Prize(Integer count, String name, Integer typeId, Integer cost) {
 			this.name = name;
 			this.typeId = typeId;
 			this.count = count;
 			this.cost = cost;
 		}
 	}
 }
