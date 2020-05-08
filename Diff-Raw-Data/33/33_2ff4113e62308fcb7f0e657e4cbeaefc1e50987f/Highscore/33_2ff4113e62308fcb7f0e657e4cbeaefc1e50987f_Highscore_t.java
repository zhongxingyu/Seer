 package logic;
 
 /**
  * @author nanne
  * 
  */
 public class Highscore {
	private String	spelerNaam = "";
	private String	score = "";
	private String	tijdOver = "";
 
 	/**
 	 * @return the score
 	 */
 	public String getScore() {
 		return score;
 	}
 
 	/**
 	 * @return the spelerNaam
 	 */
 	public String getSpelerNaam() {
 		return spelerNaam;
 	}
 
 	/**
 	 * @return the tijdOver
 	 */
 	public String getTijdOver() {
 		return tijdOver;
 	}
 
 	/**
 	 * @param score
 	 *            the score to set
 	 */
 	public void setScore(String score) {
 		this.score = score;
 	}
 
 	/**
 	 * @param spelerNaam
 	 *            the spelerNaam to set
 	 */
 	public void setSpelerNaam(String spelerNaam) {
 		this.spelerNaam = spelerNaam;
 	}
 
 	/**
 	 * @param tijdOver
 	 *            the tijdOver to set
 	 */
 	public void setTijdOver(String tijdOver) {
 		this.tijdOver = tijdOver;
 	}
 }
