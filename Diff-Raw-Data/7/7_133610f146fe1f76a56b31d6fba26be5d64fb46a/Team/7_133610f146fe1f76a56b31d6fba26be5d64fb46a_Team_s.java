 package uk.ac.gla.dcs.tp3.w.league;
 
 public class Team {
 	private String name;
 	private int points;
 	private int gamesPlayed;
 	private boolean eliminated;
 	private Match[] upcomingMatches;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getPoints() {
 		return points;
 	}
 
 	public void setPoints(int points) {
 		this.points = points;
 	}
 
 	public int getGamesPlayed() {
 		return gamesPlayed;
 	}
 
 	public void setGamesPlayed(int gamesPlayed) {
 		this.gamesPlayed = gamesPlayed;
 	}
 
 	public boolean isEliminated() {
 		return eliminated;
 	}
 
 	public void setEliminated(boolean eliminated) {
 		this.eliminated = eliminated;
 	}
 
 	public Match[] getUpcomingMatches() {
 		return upcomingMatches;
 	}
 
 	public void setUpcomingMatches(Match[] upcomingMatches) {
 		this.upcomingMatches = upcomingMatches;
 	}
 	
 	public void addUpcomingMatch(Match m) {
 		// TODO
 	}
 	
 	public Match removeUpcomingMatch() {
 		// TODO
 	}
 
 }
