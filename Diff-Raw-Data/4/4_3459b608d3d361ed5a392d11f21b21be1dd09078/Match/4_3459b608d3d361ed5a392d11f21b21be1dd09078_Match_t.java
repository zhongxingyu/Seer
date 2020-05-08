 package uk.ac.gla.dcs.tp3.w.league;
 
 /**
  * This class has many members. The class holds the information, and result of,
  * a match between two teams. This is an element of the fixture list which is
  * used by both the UI and algorithm.
  * 
  * @author Team W
  * @version 1.0
  */
 public class Match {
 
 	private Team homeTeam;
 	private Team awayTeam;
 	private int homeScore;
 	private int awayScore;
 	private DateTime date;
 	private boolean played;
 
 	/**
 	 * No parameter constructor. Sets all instance variables to null, -1, or
 	 * false.
 	 */
 	public Match() {
 		this(null, null, -1, -1, null, false);
 	}
 
 	/**
 	 * A typical constructor to create a match object.
 	 * 
 	 * @param h
 	 *            The home team
 	 * @param a
 	 *            The away team
 	 * @param hs
 	 *            The home team's score
 	 * @param as The away team's score
 	 * @param d
 	 *            The date and time the match is played
 	 * @param b
 	 *            A boolean to show if the match has been played or not.
 	 */
 	public Match(Team h, Team a, int hs, int as, DateTime d, boolean b) {
 		homeTeam = h;
 		awayTeam = a;
 		homeScore = hs;
 		awayScore = as;
 		date = d;
 		played = b;
 	}
 
 	/**
 	 * Get the home team
 	 * 
 	 * @return Team object for home team
 	 */
 	public Team getHomeTeam() {
 		return homeTeam;
 	}
 
 	/**
 	 * Set the home team
 	 * 
 	 * @param homeTeam
 	 *            Team object for home team
 	 */
 	public void setHomeTeam(Team homeTeam) {
 		this.homeTeam = homeTeam;
 	}
 
 	/**
 	 * Get the away team
 	 * 
 	 * @return Team object for away team
 	 */
 	public Team getAwayTeam() {
 		return awayTeam;
 	}
 
 	/**
 	 * Set the away team
 	 * 
 	 * @param awayTeam
 	 *            Team object for away team
 	 */
 	public void setAwayTeam(Team awayTeam) {
 		this.awayTeam = awayTeam;
 	}
 
 	/**
 	 * Get the home team's score
 	 * 
 	 * @return int for home team's score
 	 */
 	public int getHomeScore() {
 		return homeScore;
 	}
 
 	/**
 	 * Set the home team's score
 	 * 
 	 * @param homeScore
 	 *            int for home team's score
 	 */
 	public void setHomeScore(int homeScore) {
 		this.homeScore = homeScore;
 	}
 
 	/**
 	 * Get the away team's score
 	 * 
 	 * @return int for away team's score
 	 */
 	public int getAwayScore() {
 		return awayScore;
 	}
 
 	/**
 	 * Set the away team's score
 	 * 
 	 * @param awayScore
 	 *            int for away team's score
 	 */
 	public void setAwayScore(int awayScore) {
 		this.awayScore = awayScore;
 	}
 
 	/**
 	 * Get the date the match will play on
 	 * 
 	 * @return Date for when the match will be played
 	 */
 	public DateTime getDateTime() {
 		return date;
 	}
 
 	/**
 	 * Set the date the match will play on
 	 * 
 	 * @param date
 	 *            Date for when the match will be played
 	 */
 	public void setDate(DateTime date) {
 		this.date = date;
 	}
 
 	/**
 	 * Check to see if the match has already been played
 	 * 
 	 * @return boolean representing if the match has been played or not
 	 */
 	public boolean isPlayed(Date currentDate) {
 		return !currentDate.before(date);
 	}
 
 	/**
 	 * Return a boolean representing the played status of the match.
 	 * 
 	 * @return true if match played, false otherwise
 	 */
 	public boolean isPlayed() {
 		return played;
 	}
 
 	/**
 	 * Set whether or not the match has been played
 	 * 
 	 * @param played
 	 *            boolean representing if the match has been played or not
 	 */
 	public void setPlayed(boolean played) {
 		this.played = played;
 	}
 
 	/**
 	 * Works out which team won the match and returns a reference to the winning
 	 * team. If the game has not been played yet, null is returned.
 	 * 
 	 * @return A reference to the team with the higher score.
 	 */
 	public Team getWinner() {
 		if (played)
 			return (homeScore > awayScore) ? homeTeam : awayTeam;
 		else
 			return null;
 	}
 
 	/**
 	 * Print out the home and away team names and the date of the match.
 	 */
 	public String toString() {
 		return String.format("%s vs. %s on %s", homeTeam, awayTeam, date);
 	}
 
 	/**
 	 * If the match has just been played, find out which team won. Add a point
 	 * for the winning team and increment the total number of games played by
 	 * both teams then remove the reference to the upcoming match arrays.
 	 */
 	public void playMatch() {
 		// Only execute this method once. Also ensure scores have been set.
 		if (played || homeScore == -1 || awayScore == -1)
 			return;
 		played = true;
 		// t is the winner of the match, s is the loser of the match.
 		Team t = getWinner();
 		Team s = ((t == homeTeam) ? awayTeam : homeTeam);
 		// t gets a point, s does not. Increment number of games played and
 		// remove associated match for both teams.
 		t.setPoints(t.getPoints() + 1);
 		t.setGamesPlayed(t.getGamesPlayed() + 1);
 		t.removeUpcomingMatch(this);
 		s.setGamesPlayed(s.getGamesPlayed() + 1);
 		s.removeUpcomingMatch(this);
 	}
 
 	/**
 	 * Unplay the match. If the team hasn't been played yet or has no valid
 	 * score, nothing is done. Otherwise the winning and losing teams are found
 	 * and points and games played are decremented. The matches are also added
 	 * to each team's upcoming match array for future playing.
 	 */
 	public void unplayMatch() {
 		// Only execute this method once. Also ensure scores have been set.
 		if (!played || homeScore == -1 || awayScore == -1)
 			return;
 		// t is the winner of the match, s is the loser of the match.
 		Team t = getWinner();
 		Team s = ((t == homeTeam) ? awayTeam : homeTeam);
 		// t gets a point, s does not. Decrement number of games played and
 		// add associated match for both teams.
 		t.setPoints(t.getPoints() - 1);
 		t.setGamesPlayed(t.getGamesPlayed() - 1);
 		t.addUpcomingMatch(this);
 		s.setGamesPlayed(s.getGamesPlayed() - 1);
 		s.addUpcomingMatch(this);
		//finally, set false. Setting earlier causes null pointer exception
		played = false;
 	}
 
 }
