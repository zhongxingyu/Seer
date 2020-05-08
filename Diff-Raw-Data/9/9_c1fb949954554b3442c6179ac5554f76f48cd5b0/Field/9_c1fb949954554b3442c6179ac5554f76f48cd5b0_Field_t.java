 package Monopoly;
 
 public class Field {
 	int id = 1;
 	String name = "IT-Factory";
 	int value = 10000;
 	int property = 0;
 	int category = 1;
 	Player owner;
 	int x, y;
 	
 	public Field(int x, int y, String name, int value,int category) {
 		this.x = x;
 		this.y = y;
 		this.name = name;
 		this.value = value;
 		this.category = category;
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public void setPositions(int x, int y){
 		this.x = x;
 		this.y = y;
 	}
 	
 	public boolean owned() {
 		if(owner != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void setOwner(Player p) {
 		if(owner == null & name != "Start") {
 			owner = p;
 		}
 	}
 	
 	public void setValue(int i)	{
 		value = i;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public int getRent()	{
		int rent;
		if(property>0) {
			rent = (value / 10) * property;
		} else {
			rent = value / 10;
		}
 		return rent;
 	}
 	
 	public Player getOwner() {
 		return owner; 
 	}
 	public int getValue() { 
 		return value;
 	}
 	
 	public int getPrice() {
 		return value; 
 	}
 	
 	public int getHousePrice() {
 		int price;
 		price = value / 5;
 		return price;
 	}
 
 }
