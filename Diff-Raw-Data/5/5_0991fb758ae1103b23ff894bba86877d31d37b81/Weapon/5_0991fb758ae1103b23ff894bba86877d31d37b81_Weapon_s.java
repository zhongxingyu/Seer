 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import javax.imageio.ImageIO;
 
 
 public class Weapon {
 	public boolean longRange; //Long range or not
 	public boolean equipped;
 	public boolean draw;	//Whether or not the mouse is hovering over it
 	public int x, y; 	//Position on map, -1,-1 if not on map)
 	public int mid;		//Model # (1.png etc)
 	public int wid;
 	public String name;
 	public int damage, dur, range, rarity;	//dur: # of swings before it breaks; rate: # of shots per 5 seconds
	public int rate;
 	public int ammo;
 //	private long lastUsed;
 	public Image model;
 	private int recLen = 68;
 	
 	public Weapon(){	this.wid = -1;	} //clear other variables?
 	
 	/**
 	 * For inventory
 	 * 
 	 * @param weap
 	 */
 	public Weapon(int weap){
 		if (weap > 0 && weap != 10 && weap != 15) {
 			try{
 				RandomAccessFile raf = new RandomAccessFile("items/weps.bin", "rw");
 				this.readRaf(raf, weap); //TODO: make fist a weapon? or have it as a default if no weapon assigned?
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			this.ammo = 35;
 		}
 		else if (weap == 0 || weap == 10 || weap == 15) {
 			this.wid = weap;
 			this.ammo = 35;
 			if (weap == 0)
 				this.name = "Basic Sword";
 			else if (weap == 10)
 				this.name = "Handgun";
 			else if (weap == 15)
 				this.name = "Teleportation Ability";
 			try {
 				this.model = ImageIO.read(new File("items/" + this.wid + ".png"));
 			}
 			catch (IOException e) {	e.printStackTrace();	}
 		}
 	}
 	
 	/**
 	 * For NPC
 	 * 
 	 * @param raf
 	 * @param x
 	 * @param y
 	 */
 	public Weapon(RandomAccessFile raf, int x, int y){
 		try{
 			//Old random weapon generation
 			/*if ((int)(Math.random()*1+1) == 1){
 				this.x = x;
 				this.y = y;
 				this.range = -1;
 				this.slot = 0;
 				this.draw = false;
 				int numRecs = (int)(raf.length()/recLen);
 				int recAt = (int)(Math.random()*(numRecs)-1);
 				this.readRaf(raf, recAt);
 				Entity.wIndex++;
 			}*/
 			int numRecs = (int)(raf.length()/recLen);
 			Weapon[] temp = new Weapon[numRecs];
 			for (int i = 0; i < numRecs; i++){
 				temp[i] = new Weapon();
 				temp[i].readRaf(raf, i);
 			}
 			boolean found = false;
 			int num = (int)(Math.random()*100+1);
 			int full = 0;
 			for (int i = 0; i < numRecs && !found; i++){
 				if (num < (full+temp[i].getRarity())){
 					found = true;
 					this.x = x;
 					this.y = y;
 					this.readRaf(raf, i);
 					Entity.wIndex++;
 				}else
 					full += temp[i].getRarity();
 			}
 			if (!found)
 				System.out.println("No weapon found;  drop a random item");
 		}catch(Exception e){	
 			e.printStackTrace();
 		}
 	}
 	
 	public void readRaf(RandomAccessFile raf, int recordNumber) throws IOException{
     	if (raf.length() >= (recordNumber * recLen)){
     		raf.seek(recordNumber * recLen);
     		this.wid = recordNumber+1;
 	    	this.name = readString(raf);
 	    	this.mid = raf.readInt();
 	    	int minDam = raf.readInt();
 	    	int maxDam = raf.readInt();
 	    	this.damage = (int)((Math.random()*(maxDam-minDam))+minDam);
	    	this.rate = raf.readInt();
 	    	this.range = raf.readInt();
 	    	this.dur = raf.readInt();
 	    	this.rarity = raf.readInt();
 	    	this.ammo = 35;
 	    //	System.out.println(minDam + "-" + maxDam);
 	    	//this.dur = raf.readInt();
 	    	//this.mid = raf.readInt();
 	    	model = ImageIO.read(new File("items/" + this.wid + ".png"));
     	}
     //	System.out.println(this.name);
     }
 	
 	public void equip(){
 		this.x = -1;
 		this.y = -1;
 		this.equipped = true;
 	}
 	
 	public void drop(int x, int y){
 		if (this.equipped){
 			this.x = x;
 			this.y = y;
 			this.equipped = false;
 		}
 	}
 	
 	public void drawWeapon(Graphics g, Display m, int x, int y, boolean small){
 		if (small) {
 			//if not an ability
 				g.drawImage(this.model, x, y, x+25, y+25, 0, 0, 50, 50, m);
 			//else if ability
 				//g.drawImage(Display.scroll, x, y, x+25, y+25, 0, 0, 50, 50, m);
 		}
 		else
 			g.drawImage(this.model, x, y, m);
 	}
 	
 	public int getDamage(){	return this.damage;	}
 	public int getDur(){	return this.dur;	}
 	public float getRate(){	return this.rate;	}
 	public int getRarity(){	return this.rarity;	}
 	
 	public void takeDur(){	this.dur--;	}
 	
 	private String readString(RandomAccessFile raf) throws IOException{
     	String s = "";
     	for (int i = 0; i < 20; i++)
     		s = s + raf.readChar();
     	return s.trim();
     }
 }
