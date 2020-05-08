 import java.util.ArrayList;
 import java.util.List;
 import java.awt.Color;
 import javax.swing.ImageIcon;
 
 /**
  * 
  * @author Team 7
  * Represent a player in the MULE game
  *
  */
 public class Player extends Actor implements Comparable {
 	
 	private Race race;
 	private String color;
 	private String name;
 	private int score;
 	private int money;
 	private List<Property> properties;
 	private int food;
 	private ImageIcon image; // later, make this an array to hold all animation frames
 
 	public Player() {
 		properties = new ArrayList<Property>();
 	}
 	
 	/**
 	 * @return the player's score
 	 */
 	public int getScore()
 	{
 		return score;
 	}
 	
 
 	public int getFood() {
 	    return food;
 	}
 	
 	public void setFood(int food) {
 	    this.food = food;
 	}
 	
 
 	/**
 	 * @param p the property to be added to the players owned property
 	 */
 	public void addProperty(Property p)
 	{
 		properties.add(p);
 		p.setOwned(true);
 	}
 	
 	
 	/**
 	 * @param score The player's score to be changed to
 	 */
 	public void setScore(int score)
 	{
 		this.score = score;
 	}
 	
 	/**
 	 * @return the player's money
 	 */
 	public int getMoney()
 	{
 		return money;
 	}
 	
 	/**
 	 * @param score The player's money to be changed to
 	 */
 	public void setMoney(int money)
 	{
 		this.money = money;
 	}
 	
 	/**
 	 * @param amount how much you want to change the money be
 	 */
 	public void changeMoney(int amount) {
 		this.money = money + amount;
 	}
 	
 	/**
 	 * @return the name of the player
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * @param name The name of the player
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * @return the player's race
 	 */
 	public Race getRace() {
 		return race;
 	}
 	
 	/**
 	 * @param race The player's race
 	 */
 	public void setRace(Race race) {
 		this.money = race.getStartingMoney();
 		this.race = race;
 	}
 	
 	/**
 	 * Precondition: (race != null) && (color != null)
 	 * Postcondition: 
 	 * sets the player's image icon based on race and color
 	 */
 	public void setImage() {
 		ImageIcon playerIcon;
 		if(race==Race.Human){
 			if(color.equals("Orange")){
 				playerIcon = new ImageIcon("humanOrange.png");
 			}
 			else if(color.equals("Blue")){
 				playerIcon = new ImageIcon("humanBlue.png");
 			}
 			else if(color.equals("Yellow")){
 				playerIcon = new ImageIcon("humanYellow.png");
 			}
 			else{
 				playerIcon = new ImageIcon("humanRed.png");
 			}
 		}
 		else if(race==Race.Bonzoid){
 			if(color.equals("Orange")){
 				playerIcon = new ImageIcon("bonzoidOrange.png");
 			}
 			else if(color.equals("Blue")){
 				playerIcon = new ImageIcon("bonzoidBlue.png");
 			}
 			else if(color.equals("Yellow")){
 				playerIcon = new ImageIcon("bonzoidYellow.png");
 			}
 			else{
 				playerIcon = new ImageIcon("bonzoidRed.png");
 			}
 		}
 		else if(race==Race.Ugaite){
 			if(color.equals("Orange")){
 				playerIcon = new ImageIcon("ugaiteOrange.png");
 			}
 			else if(color.equals("Blue")){
 				playerIcon = new ImageIcon("ugaiteBlue.png");
 			}
 			else if(color.equals("Yellow")){
 				playerIcon = new ImageIcon("ugaiteYellow.png");
 			}
 			else{
 				playerIcon = new ImageIcon("ugaiteRed.png");
 			}
 		}
 		else if(race==Race.Buzzite){
 			if(color.equals("Orange")){
 				playerIcon = new ImageIcon("buzziteOrange.png");
 			}
 			else if(color.equals("Blue")){
 				playerIcon = new ImageIcon("buzziteBlue.png");
 			}
 			else if(color.equals("Yellow")){
 				playerIcon = new ImageIcon("buzziteYellow.png");
 			}
 			else{
 				playerIcon = new ImageIcon("buzziteRed.png");
 			}
 		}
 		else{
 			if(color.equals("Orange")){
 				playerIcon = new ImageIcon("flapperOrange.png");
 			}
 			else if(color.equals("Blue")){
 				playerIcon = new ImageIcon("flapperBlue.png");
 			}
 			else if(color.equals("Yellow")){
 				playerIcon = new ImageIcon("flapperYellow.png");
 			}
 			else{
 				playerIcon = new ImageIcon("flapperRed.png");
 			}
		}
		image = playerIcon;
 	}
 	
 	/**
 	 * @return The color of the player
 	 */
 	public String getColor() {
 		return color;
 	}
 	
 	public Color getColorRepresentation() {
 	    if (color.equals("Red"))
 	        return Color.RED;
 	    else if (color.equals("Orange"))
 	        return Color.ORANGE;
 	    else if (color.equals("Blue"))
 	        return Color.BLUE;
 	    else 
 	        return Color.YELLOW;
 	}
 	
 	/**
 	 * @return the image icon that represents the player
 	 */
 	public ImageIcon getImage() {
 		return image;
 	}
 	
 	/**
 	 * @param color The color of the player
 	 */
 	public void setColor(String color) {
 		this.color = color;
 	}
 	
 	public int compareTo(Object o) {
 	    return this.getScore() - ((Player)o).getScore();
 	}
 	
 }
