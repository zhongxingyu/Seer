 package monopolyCode;
 
 import java.util.ArrayList;
 
 public class player {
 
 	private Integer money;
 	private Integer currentLocation;
 	private String playerName;
 	private ArrayList<building> landProperty;
 	private ArrayList<Integer> treasureProperty;
 	private Integer netAsset;
 	private Integer penaltyTurn;
 	private Boolean status;
 	private Boolean hasRolled;
 	private Boolean hasSetName;
 	private Boolean showBuildings = false;
 	public player(String pName, Boolean state) {
 		money = 2000;
 		currentLocation = 0;
 		playerName = pName;
 		landProperty = new ArrayList<building>();
 		treasureProperty = new ArrayList<Integer>();
 		netAsset = money;
 		penaltyTurn = 0;
 		status = state;
 		hasRolled = false;
 		hasSetName = false;
 	}
 	
 
 	public Integer returnMoney() {
 		return money;
 	}
 
 	public Integer returnLocation() {
 		return currentLocation;
 	}
 
 	public String returnName() {
 		return playerName;
 	}
 
 	public Integer returnAsset() {
 		return netAsset;
 	}
 	
 	public Boolean checkTurn(){
 		return status;
 	}
 	
 	public void incrementLocation(Integer diceroll)
 	{
 		if(diceroll + this.currentLocation < 39)
 		{
			money = money + 200;
 			this.currentLocation = this.currentLocation + diceroll;
 		}
 		else
 		{
 			for(int i = 0; i < diceroll; i++)
 			{
 				if(this.currentLocation == 39)
 				{
					
 					this.currentLocation = 0;
 				}
 				else
 				{
 					this.currentLocation = this.currentLocation + 1;
 				}
 			}
 		}
 	}
 	
 	public void switchMoney(player x)
 	{
 		Integer temp = x.returnMoney();
 		x.money = money;
 		money = temp;
 	}
 
 	public void addMoney(Integer temp){
 		money = money - temp;
 	}
 	
 	public void recieveMoney(Integer temp){
 		money = money + temp;
 	}
 	
 	public Boolean checkBroke(){
 		if(money <= 0)
 			return true;
 		else
 			return false;
 	}
 	
 	public void gameover(){
 		penaltyTurn = 99999;
 	}
 	
 	public Boolean pOwned(Integer location) {
 
 		if (landProperty.contains(location)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public Boolean tOwned(Integer number){
 		if(treasureProperty.contains(number)){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public void OopsJail(){
 		penaltyTurn = 3;
 		setLocation(10);
 	}
 	
 	public void setLocation(Integer place)
 	{
 		currentLocation = place;
 	}
 	
 	public Boolean isJail(){
 		if(penaltyTurn > 0){
 			penaltyTurn--;
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public void set_status(boolean Status)
 	{
 		status = Status;
 	}
 	
 	public void set_hasRolled(Boolean roll)
 	{
 		hasRolled = roll;
 	}
 	
 	public boolean hasRolled()
 	{
 		return hasRolled;
 	}
 	
 	public void addBuilding(building Building)
 	{
 		landProperty.add(Building);
 	}
 	
 	public void setName(String newName)
 	{
 		playerName = newName;
 		hasSetName = true;
 	}
 	
 	public boolean returnSetName()
 	{
 		return hasSetName;
 	}
 	
 	public ArrayList<building> returnBuildingList()
 	{
 		return landProperty;
 	}
 	
 	public void setShowBuildings(boolean state)
 	{
 		showBuildings = state;
 	}
 	
 	public Boolean returnShowBuildings()
 	{
 		return showBuildings;
 	}
 	
 
 }
