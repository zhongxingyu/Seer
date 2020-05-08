 package uk.ac.gla.dcs.tp3.w.gen;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import uk.ac.gla.dcs.tp3.w.league.DateTime;
 import uk.ac.gla.dcs.tp3.w.league.Division;
 import uk.ac.gla.dcs.tp3.w.league.Team;
 import uk.ac.gla.dcs.tp3.w.parser.Parser;
 
 public class GenerateLeague {
 	private String fileName = null;
 	private int gamesPerTeam = 180;
 	private boolean verbose = false;
 	
 	public GenerateLeague(String fileName) {
 		this.fileName = System.getProperty("user.dir") + "/" + fileName;
 	}
 	
 	public boolean generate(){
 		Parser p = new Parser();
 		p.generateStandardDivisionInfo();
 		int teamSize, gamesBetweenTeams, i, j, k;
 		int scoreA, scoreB, index = 0;
 		Random r = new Random();
 		DateTime date = new DateTime();
 		ArrayList<String> fixtures = new ArrayList<String>();
 		String fixture;
 		for(Division d: p.getDivisions().values()){
 			System.out.println("New Division: " + d.getName());
 			ArrayList<Team> teams = d.getTeams();
 			teamSize = teams.size();
 			gamesBetweenTeams = gamesPerTeam/teamSize;
 			System.out.println("Games Between Teams: " + gamesBetweenTeams);
 			for(i=0;i<teamSize;i++){
 				for(j=i+1;j<teamSize;j++){
 					for(k=0;k<gamesBetweenTeams;k++){
 						scoreA = r.nextInt(10);
 						scoreB = r.nextInt(10);
 						if (scoreA==scoreB) scoreB=(scoreB+1)%10;
 						if(!fixtures.isEmpty())index = r.nextInt(fixtures.size());
						fixture = date.formatTime() + " " + teams.get(i) + " - " + teams.get(j) + " " + scoreA +":"+scoreB+"\n";
 						if(verbose)	System.out.println(fixture);
 						fixtures.add(index, fixture);
 					}
 				}
 			}
 		}
 		i=0;
 		int totEntries = fixtures.size();
 		while(i<totEntries){
 			date.incrementDate();
			fixtures.add(i,"\n");
			fixtures.add(i+1,date.genDate()+"\n");
 			totEntries=totEntries+2;
 			i = i+10+r.nextInt(10);
 		}
 		return writeToFile(fixtures);
 	}
 	
 	private boolean writeToFile(ArrayList<String> lines){
 		FileWriter fw;
 		try{
 			fw = new FileWriter(fileName);
 		} catch (IOException IOE){
 			IOE.printStackTrace();
 			return false;
 		}
 		BufferedWriter bf = new BufferedWriter(fw);
 		try{
 			for(String line : lines){
 				bf.write(line);
 			}
 		}catch (IOException IOE){
 			IOE.printStackTrace();
 			return false;
 		}
 		try{
 			bf.close();
 		}catch (IOException IOE){
 			IOE.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public void verbose(){
 		verbose=true;
 	}
 	
 	public static void main(String[] args){
 		GenerateLeague gn = new GenerateLeague("TEST.txt");
 		gn.generate();
 	}
 
 }
