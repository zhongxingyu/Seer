 import java.awt.*;
 import java.util.*;
 
 public class Ship {
 	
 	private int x,y;
 	private int size;
 	
 	public final int NORTH = 0;
 	public final int SOUTH = 180;
 	public final int EAST = 270;
 	public final int WEST = 90;
 	
 	private ArrayList<Turret> guns;
 	
 	private int color;
 	
 	public Ship(int x, int y,int size,int color){
 		this.x=x;
 		this.y=y;
 		this.size = size;
 		this.color=color;
 		guns = new ArrayList<Turret>();
 		initArray();
 	}
 	
 	private void initArray(){
 		guns.add(new Turret(x-((size*3)+(size/3)),y,270,0,size,WEST));
 		guns.add(new Turret(x-((size*6)-(size/3)),y,270,0,size,WEST));
 		guns.add(new Turret(x+((size*2)-(size/3)),y,270,0,size,WEST));
 		guns.add(new Turret(x+((size*3)+(size/3)),y,270,0,size,WEST));
 	}
 	
 	public void addX(int x){
 		this.x+=x;
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).addX(x);
 		}
 	}
 	public void addY(int y){
 		this.y+=y;
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).addY(y);
 		}
 	}
 	public void addSize(int s){
 		this.size+=s;
 	}
 	public int getX(){
 		return x;
 	}
 	public int getY(){
 		return y;
 	}
 	public void giveXandY(int x, int y){
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).giveXandY(x,y);
 		}
 	}
 	
 	public void drawShip(Graphics g){
 		base(g,size);
 		drawGuns(g);
 	}
 	
 	private void drawGuns(Graphics g){
 		for(int index = 0; index<guns.size(); index++){
 			guns.get(index).drawGun(g);
 		}
 	}
 	
 	private void base(Graphics g, int size)
 	{	
 		//x and y are the center of the ship
 		
 		//Bow
 		g.setColor(new Color(255,211,155));
 		g.setColor(Color.gray.brighter());
 		g.fillArc(x-(size*8),y-(size-(size/6)),(size*8)+(size/3),(size*2)-(size/3),90,180);
 		g.setColor(Color.black);
 		g.drawArc(x-(size*8),y-(size-(size/6)),(size*8)+(size/3),(size*2)-(size/3),90,180);
 		
 		//Stern
 		g.setColor(new Color(255,211,155));
 		g.setColor(Color.gray.brighter());
 		g.fillArc(x+((size*2)+(size/6)),y-(size-(size/6)),(size*3)+(size/3),(size*2)-(size/3),270,180);
 		g.setColor(Color.black);
 		g.drawArc(x+((size*2)+(size/6)),y-(size-(size/6)),(size*3)+(size/3),(size*2)-(size/3),270,180);
 		
 		//Deck
 		g.setColor(new Color(255,211,155));
 		g.setColor(Color.black);
 		g.drawRect(x-((size*4)-(size/6)),y-(size-(size/6)),(size*8)-(size/3),(size*2)-(size/3));
 		g.setColor(Color.gray.brighter());
 		g.fillRect(x-((size*4)-(size/6)),y-((size-(size/6))-1),(size*8)-(size/3)+1,(size*2)-(size/3)-1);
 		
 		//Command Center
 		g.setColor(getColor().darker().darker());
 		g.fillRect(x-((size*2)-(size/3)),y-((size/2)-(size/size)),((size*2)+(size/3)),size);
 		g.setColor(getColor().darker());
 		g.fillRect(x-(size+(size/6)),y-((size/2)+(size/10)),size/6,size+(size/3));
 		g.fillRect(x-(size+(size/3)),y-(size/3),size/6,(size-(size/3))+(size/size));
 		g.fillRect(x-(size/2),y-((size/6)+(size/10)+(size/size)),size,size-(size/3));
 		g.setColor(getColor());
 		g.fillArc(x-(size-(size/6)),y-(size/3),size-(size/3),size-(size/3),90,180);
 		g.setColor(getColor().darker());
 		int xarray[] = {x-(size/2),x-(size-(size/6)),x-(size/2),x-(size/2)};
 		int yarray[] = {y-((size/3)-(size/size)),y+(size/size),y+(size/3),y-((size/3)-(size/size))};
 		g.fillPolygon(xarray,yarray,4);
 	}
 	
 	private Color getColor(){
 		switch(color){
 			case 1: return Color.green;
 			case 2: return Color.red;
 			default: return null;
 		}
 	}
 }
