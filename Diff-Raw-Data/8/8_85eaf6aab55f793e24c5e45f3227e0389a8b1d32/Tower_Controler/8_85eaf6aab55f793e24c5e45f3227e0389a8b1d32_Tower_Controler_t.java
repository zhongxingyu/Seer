 
 import java.util.ArrayList;
 import java.awt.Graphics;
 public class Tower_Controler {
 	static Graphics g;
 	// current list of towers
 	//static ArrayList<Towers> towers = new ArrayList();
 	// what has the towers continue or stop at end of level/game
 	static boolean live = false;
 
 	// when a new tower is placed this sets the position and creates the tower
 	public static void recieve_position(int x , int y) {
 		// do x and y conversion here
 		/////////////////////////////
 		
 		/////////////////////////////
 		// create new tower when called
 		//System.out.println((x/32) * 32  + " " +(y/32) *32);
		Map[][] m = Data.getMap();
		//System.out.println(m[x/32][y/32].getCharString());
		if(m[x/32][y/32].getCharString().equals("GP")){
 		Towers tower = new Towers((x/32)*32, (y/32) *32, g);
 		Data.towers.add(tower);
		m[x/32][y/32].setCharString("B");
		}

 	}
 	
 	public static void update(){
 
 		for(int i = 0; i < Data.towers.size(); i++){
 			Data.towers.get(i).paint();
 			try{
 			//Thread.sleep(2000);
 			//System.out.println(i);
 			}
 			catch(Exception e){
 			}
 		}
 	}
 }
