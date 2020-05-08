 package com.daylenyang.tab;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.Serializable;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 public class DataImport implements Serializable {
 
 	
 	private static final long serialVersionUID = -411120953669125050L;
 	static String delimiter = "\r\n|[\n\r\u2028\u2029\u0085]";
 	Map<String, School> schools = new HashMap<String, School>();
 
 	private static String[] splitAndSanitizeLine(String line) {
 		// replace apostrophe
 		line = line.replace('', '\'');
 		// split by comma
 		String[] csv = line.split(",");
 		// get rid of spaces
 		for (int i = 0; i < csv.length; i++) {
 			csv[i] = csv[i].trim();
 		}
 		return csv;
 	}
 
 	public void parseTeams(String fileName, List<Team> teams,
 			Map<Student, List<Double>> speakerPoints)
 			throws FileNotFoundException {
 		Scanner fScan = new Scanner(new File(fileName), "UTF-8");
 		fScan.useDelimiter(delimiter);
 
 		while (fScan.hasNextLine()) {
 			String[] csv = splitAndSanitizeLine(fScan.nextLine());
 
 			School school = null;
 			if (schools.containsKey(csv[0]))
 				school = schools.get(csv[0]);
 			else {
 				school = new School(csv[0]);
 				schools.put(csv[0], school);
 			}
 
 			Student s1 = new Student(csv[1]);
 			Student s2 = new Student(csv[2]);
 			speakerPoints.put(s1, new ArrayList<Double>());
 			speakerPoints.put(s2, new ArrayList<Double>());
 
			Team t = new Team(school, s1, s2);
 
 			teams.add(t);
 
 		}
 
 	}
 
 	public ArrayList<Judge> parseJudges(String fileName)
 			throws FileNotFoundException {
 		Scanner fScan = new Scanner(new File(fileName), "UTF-8");
 		fScan.useDelimiter(delimiter);
 		ArrayList<Judge> judges = new ArrayList<Judge>();
 
 		while (fScan.hasNextLine()) {
 			String[] csv = splitAndSanitizeLine(fScan.nextLine());
 
 			if (csv.length > 1) {
 				// judge has school
 				School school = null;
 				if (schools.containsKey(csv[1]))
 					school = schools.get(csv[1]);
 				else {
 					school = new School(csv[1]);
 					schools.put(csv[1], school);
 				}
 
 				Judge j = new Judge(school, csv[0]);
 				judges.add(j);
 			} else {
 				// just the judge
 				Judge j = new Judge(NullSchool.getInstance(), csv[0]);
 				judges.add(j);
 			}
 		}
 
 		return judges;
 
 	}
 
 	public ArrayList<Room> parseRooms(String fileName)
 			throws FileNotFoundException {
 		Scanner fScan = new Scanner(new File(fileName), "UTF-8");
 		fScan.useDelimiter(delimiter);
 		ArrayList<Room> rooms = new ArrayList<Room>();
 
 		while (fScan.hasNextLine()) {
 			rooms.add(new Room(fScan.nextLine()));
 		}

 		return rooms;
 	}
 
 	public static void parsePrelimRoundResults(String fileName, Round r)
 			throws FileNotFoundException {
 		// Before we do anything, we know the result of the bye round
 		for (Pair p : r.getPairs()) {
 			if (p.getAffTeam() instanceof NullTeam) {
 				p.setBallots(0, 2);
 			} else if (p.getNegTeam() instanceof NullTeam) {
 				p.setBallots(2, 0);
 			}
 		}
 
 		Scanner fScan = new Scanner(new File(fileName), "UTF-8");
 		fScan.useDelimiter(delimiter);
 
 		while (fScan.hasNextLine()) {
 			String[] csv = splitAndSanitizeLine(fScan.nextLine());
			System.out.println("Imported: " + Arrays.toString(csv));
 
 			for (Pair p : r.getPairs()) {
 				if (csv[0].equals(p.getRoom().toString())) {
 
 					// Create variables
 					int affTeamBallots = Integer.parseInt(csv[1]);
 					int negTeamBallots = Integer.parseInt(csv[2]);
 
 					p.setBallots(affTeamBallots, negTeamBallots);
 
 				}
 			}
 
 		}
 	}
 
 	public static void parseSpeakerPoints(String fileName, Tournament t,
 			int roundIndex) throws FileNotFoundException {
 		Scanner fScan = new Scanner(new File(fileName), "UTF-8");
 		fScan.useDelimiter(delimiter);

 		while (fScan.hasNextLine()) {
 			String[] csv = splitAndSanitizeLine(fScan.nextLine());
 			int cellToLookAt = roundIndex + 1;
 			double points = Double.parseDouble(csv[cellToLookAt]);
			System.out.println("Imported: " + csv[0] + "," + points);
 
 			t.enterSpeakerPointsForStudent(csv[0], points, roundIndex);
 		}
 	}
 
 }
