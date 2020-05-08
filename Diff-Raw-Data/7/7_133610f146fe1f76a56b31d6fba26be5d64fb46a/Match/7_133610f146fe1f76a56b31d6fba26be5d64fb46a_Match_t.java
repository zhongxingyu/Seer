 package uk.ac.gla.dcs.tp3.w.league;
 
 import java.util.Date;
 
 public class Match {
 
 	private Team homeTeam;
 	private Team awayTeam;
 	private int homeScore;
 	private int awayScore;
 	private Date date;
 	private boolean played;
 
 	public Team getHomeTeam() {
 		return homeTeam;
 	}
 
 	public void setHomeTeam(Team homeTeam) {
 		this.homeTeam = homeTeam;
 	}
 
 	public Team getAwayTeam() {
 		return awayTeam;
 	}
 
 	public void setAwayTeam(Team awayTeam) {
 		this.awayTeam = awayTeam;
 	}
 
 	public int getHomeScore() {
 		return homeScore;
 	}
 
 	public void setHomeScore(int homeScore) {
 		this.homeScore = homeScore;
 	}
 
 	public int getAwayScore() {
 		return awayScore;
 	}
 
 	public void setAwayScore(int awayScore) {
 		this.awayScore = awayScore;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public boolean isPlayed() {
 		return played;
 	}
 
 	public void setPlayed(boolean played) {
 		this.played = played;
 	}
 
 	public Team getWinner() {
 		// TODO
		return null;
 	}
 	
 	public void updatePointsAndPlayGame() {
 		// TODO
 	}
 
 }
