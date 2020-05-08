 package uk.ac.gla.dcs.tp3.w.parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.Scanner;
 
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 /**
  * @author James & Kris
  * 
  */
 public class Parser {
 
 	public static void main(String[] args) {
 		// ArrayList<League> leagueEachDay = new ArrayList<League>(); //array of
 		// days in season each with seperate state
 		League theLeague = new League();
 
 		boolean postponedCheck = false;
 		String thisLine = "", date = "";
 		Date d = new Date();
 		Scanner fileScanner, lineScanner;
 		int score1 = 0, score2 = 0;
		File source = new File(System.getProperty("user.dir")
				+ "/src/uk/ac/gla/dcs/tp3/w/parser/baseballSource.txt");
 
 		try {
 			fileScanner = new Scanner(source); // scan over file object
 			thisLine = fileScanner.nextLine();
 			// leagueEachDay.add(new League()); //date emulation (adding a day
 			// and manipulating it's state below)
 			while (fileScanner.hasNextLine() || thisLine.isEmpty()) {
 
 				/** Setting the date */
 				if (thisLine.isEmpty()) {
 					date = fileScanner.nextLine();
 					lineScanner = new Scanner(date);
 					d.setDate(lineScanner.nextInt()); // setting day
 					String m = lineScanner.next();
 					d.setMonth(getMonth(m) - 1); // month converted to a number
 													// and stored
 					d.setYear(lineScanner.nextInt()); // year
 				}
 
 				/** Checking to see if we're on the same day */
 				// if()
 
 				thisLine = fileScanner.nextLine(); // get each line one at a
 													// time(within a day)
 				if (thisLine.isEmpty()) {
 					continue;
 				}
 				lineScanner = new Scanner(thisLine); // start new scanner to
 														// scan particular line
 
 				/** parsing out information from a particular line */
 				String time = "", team1Name = "", team2Name = "", scoreOne = "", scoreTwo = "";
 				time = lineScanner.next(); // store time
 				boolean hyphenCheck = false, atScore2 = false;
 				for (int i = 6; i < thisLine.length(); i++) { // loop through
 																// characters in
 																// the line
 																// starting at
 																// first team
 																// name
 					char c = thisLine.charAt(i);
 
 					// time here
 
 					// check & add char to team 1's name
 					if (c != '-' && hyphenCheck == false) {
 						team1Name += c;
 					}
 					// check and add char to team 2's name if past hyphen in
 					// source
 					if (hyphenCheck
 							&& (c != '0' && c != '1' && c != '2' && c != '3'
 									&& c != '4' && c != '5' && c != '6'
 									&& c != '7' && c != '8' && c != '9')
 							&& c != '\t' && c != ':' && c != '$') {
 						team2Name += c;
 					}
 					// lets it know we're past the hyphen
 					if (c == '-') {
 						hyphenCheck = true;
 						i++;
 					}
 					// moved onto score of both teams
 					if (c == '0' || c == '1' || c == '2' || c == '3'
 							|| c == '4' || c == '5' || c == '6' || c == '7'
 							|| c == '8' || c == '9' || c == ':' || c == '$') {
 						/** if it hits a colon, working on team 2's score */
 						if (c == ':') {
 							atScore2 = true;
 							continue;
 						} else if (c == '$') {
 							postponedCheck = true;
 						}
 
 						/** setting score's for teams 1 and 2 */
 						if (atScore2) {
 							scoreTwo += c;
 						} else {
 							scoreOne += c;
 						}
 					}
 				}
 
 				Team t1 = new Team(removeSpaces(team1Name)); // creating 2
 																// temporary
 																// team objects
 																// to store
 																// parsed info
 				Team t2 = new Team(removeSpaces(team2Name)); // handle null
 																// pointer error
 
 				// convert score to integers if the match was not postponed
 				if (!postponedCheck) {
 					score1 = Integer.parseInt(scoreOne);
 					score2 = Integer.parseInt(scoreTwo);
 
 					/**
 					 * allocating points to the team which won the match +
 					 * another if statement surrounding this to check if we're
 					 * before the date of this match occurring with the emulated
 					 * date, if so don't calculate scores
 					 * */
 					if (score1 > score2) {
 						for (Team t : theLeague.getTeams()) {
 							if (t.equals(t1)) {
 								t.setPoints(t.getPoints() + 1);
 								break;
 							}
 						}
 					} else {
 						for (Team t : theLeague.getTeams()) {
 							if (t.equals(t2)) {
 								t.setPoints(t.getPoints() + 1);
 								break;
 							}
 						}
 					}
 				}
 				postponedCheck = false;
 
 				/** Checking if team exists in league or if not, add it */
 				if (!theLeague.isMember(t1)) {
 					theLeague.addTeam(new Team(removeSpaces(team1Name)));
 				}
 				if (!theLeague.isMember(t2)) {
 					theLeague.addTeam(new Team(removeSpaces(team2Name)));
 				}
 
 				/** Getting the Team objects out of main storage */
 				Team temp1 = null, temp2 = null;
 				for (Team t : theLeague.getTeams()) {
 					if (t.equals(t1)) {
 						temp1 = t;
 					}
 				}
 				for (Team t : theLeague.getTeams()) {
 					if (t.equals(t2)) {
 						temp2 = t;
 					}
 				}
 
 				/** and then creating the match object with them + scores */
 				Match match = new Match(temp1, temp2, score1, score2, d, false);
 				theLeague.addFixture(match);
 
 				/** add the match to the upcoming matches of both teams */
 				for (Team t : theLeague.getTeams()) {
 					if (t.equals(t1)) {
 						t.addUpcomingMatch(match);
 						break;
 					}
 				}
 				for (Team t : theLeague.getTeams()) {
 					if (t.equals(t2)) {
 						t.addUpcomingMatch(match);
 						break;
 					}
 				}
 				System.out.println(theLeague);
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Wrong filename");
 		}
 	}
 
 	/** used to remove any leading/trailing spaces randomly in the file */
 	public static String removeSpaces(String name) {
 		String fullName = "";
 		String[] team = name.split("\\s+");
 		for (String x : team) {
 			fullName = fullName + " " + x;
 		}
 		return fullName;
 	}
 
 	/** Method to convert from string to int */
 	public static int getMonth(String month) {
 		month = month.toLowerCase();
 		if (month.equals("jan")) {
 			return 1;
 		} else if (month.equals("feb")) {
 			return 2;
 		} else if (month.equals("mar")) {
 			return 3;
 		} else if (month.equals("apr")) {
 			return 4;
 		} else if (month.equals("may")) {
 			return 5;
 		} else if (month.equals("jun")) {
 			return 6;
 		} else if (month.equals("jul")) {
 			return 7;
 		} else if (month.equals("aug")) {
 			return 8;
 		} else if (month.equals("sep")) {
 			return 9;
 		} else if (month.equals("oct")) {
 			return 10;
 		} else if (month.equals("nov")) {
 			return 11;
 		} else {
 			return 12;
 		}
 	}
 }
