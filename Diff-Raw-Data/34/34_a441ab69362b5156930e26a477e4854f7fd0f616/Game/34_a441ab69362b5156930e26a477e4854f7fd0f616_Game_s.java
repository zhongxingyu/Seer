 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 
 import java.beans.XMLDecoder;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.TimeUnit;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 public class Game implements Runnable {
 
 	public static Feld[][] spielfeld;
 	//public Player one, two;
 	private final List<Player> players = new ArrayList<Player>();
 	static Lock lock1 = new ReentrantLock();
 	private LWJGL_Font lucida;
 	
 	public Game(int spielerzahl) {
 		//Spielfeld laden
 		try {
 			initialfeld("default.xml");
 		} catch (FileNotFoundException e) {
 			// Level nicht gefunden
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 		//
 		
 		if(spielerzahl==1) {
 			players.add(new Player("One",1,1));
 		}
 		if(spielerzahl==2) {
 			players.add(new Player("One",1,1));
 			players.add(new Player("Two",13,11));
 		}	
 		//Ausgangsfeld mit zufaelligen Variablen
 		int x = 1;
 		int y = 1;
 		do {
 			x = (int) ((Math.random()*100)%13)+1;
 			y = (int) ((Math.random()*100)%11)+1;
 		}
 		while(spielfeld[x][y] instanceof Steinfeld);
 //		System.out.println(x+", "+y);
 		spielfeld[x][y]=new Exitfeld();
 		GameTime.init();
 	}
 	
 	public void pollInput(){
 		lock1.lock();
 		
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 				
 			if (spielfeld[p.getx()][p.gety()] instanceof Explosionsfeld) {
				p.lives = p.lives-1;
 				try {
 						Main.t1.sleep(1000);
 					} catch (InterruptedException e) {
 					e.printStackTrace();
 					}
 //				lucida.setScale(0.33f);
 //				lucida.print(10, 5, "Spieler "+i+" hat noch "+p.lives+" Leben.");
 				}
 //			System.out.println(p.lives); //Ueberpruefung der Leben
 			if(spielfeld[p.getx()][p.gety()] instanceof Exitfeld){
 				Main.m.gameover.setText("Spieler "+(i+1)+" hat gewonnen!");
 				p.die();
 				}
 			
 			if(!p.isAlive()) { continue; }
 					 
 			if (Keyboard.isKeyDown(p.getKeyRight())) {
 				if (spielfeld[p.getx()+1][p.gety()] instanceof Leerfeld | 
 					spielfeld[p.getx()+1][p.gety()] instanceof Exitfeld |
 					spielfeld[p.getx()+1][p.gety()] instanceof Explosionsfeld){
 				
 					 p.move(1,0);
 				}
 			}
 			if (Keyboard.isKeyDown(p.getKeyLeft())) {
 				if (spielfeld[p.getx()-1][p.gety()] instanceof Leerfeld | 
 					spielfeld[p.getx()-1][p.gety()] instanceof Exitfeld |
 					spielfeld[p.getx()-1][p.gety()] instanceof Explosionsfeld){
 				
 					p.move(-1,0);
 				}
 			}
 			if (Keyboard.isKeyDown(p.getKeyDown())) {
 				if (spielfeld[p.getx()][p.gety()+1] instanceof Leerfeld | 
 					spielfeld[p.getx()][p.gety()+1] instanceof Exitfeld |
 					spielfeld[p.getx()][p.gety()+1] instanceof Explosionsfeld){
 				 
 					p.move(0,1);
 				}
 			}
 			if (Keyboard.isKeyDown(p.getKeyUp())) {
 				if (spielfeld[p.getx()][p.gety()-1] instanceof Leerfeld | 
 					spielfeld[p.getx()][p.gety()-1] instanceof Exitfeld |
 					spielfeld[p.getx()][p.gety()-1] instanceof Explosionsfeld){
 					p.move(0,-1);
 				}
 			}
 			if (Keyboard.isKeyDown(p.getKeyBomb()) ) {
				if((Bombe.getBombs() < p.getBombs()) && (!p.isPress_bomb())) {
					new Bombe(p.getx(),p.gety()).start();
 					spielfeld[p.getx()][p.gety()]= new Bombenfeld();
 				}
 				p.setPress_bomb(true);
 			} else {
 				p.setPress_bomb(false);
 			}
 		}
 		
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
 			for(int i = 0; i < 4; i++){
 				Main.m.hauptButtons[i].setVisible(false);
 				if(i!=2){
 					Main.m.spielButtons[i].setVisible(true);
 				}
 				Main.m.gameover.setVisible(true);
 			}
 			Main.m.setVisible(true);
 			Keyboard.destroy();
 			try {
 				Keyboard.create();
 			} catch (LWJGLException e){
 				e.printStackTrace();
 			}
 			Main.t1.suspend();
 		}
 			lock1.unlock();
 	}
 	
     public void run() {
     	
     	Renderer.initDisplay(800,600,60);
     	Renderer.initGL();
     	Renderer.setClearColor(1.0f, 1.0f, 1.0f, 1.0f); //white
     	
 		try {
 			lucida = new LWJGL_Font("lucida_console2.png");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.exit(0);
 		}
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 			
 			p.loadSprite("player.png");
     		p.setBombs(Main.m.getBombs());
     		
     		if(i == 0) {
     			p.setKeys(Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT, Keyboard.KEY_UP, Keyboard.KEY_DOWN, Keyboard.KEY_SPACE);
     		} else {
     			p.setKeys(Keyboard.KEY_A, Keyboard.KEY_D, Keyboard.KEY_W, Keyboard.KEY_S, Keyboard.KEY_F);
     		}
 		}
     	
     	Menue.conti = true;
     	
        while (!Display.isCloseRequested() && (playerAlive(players.get(0))) && (playerAlive(players.get(1))) && (Menue.conti == true)) {
         	// conti bezeichnet den Unterschied zwischen Spiel- und Hauptmenu
         	Renderer.clearGL();
         	
 			pollInput();
 		    
 		    for(int x = 0; x < spielfeld.length; x++) {
 		    	for(int y = 0; y < spielfeld[0].length; y++) {
 		    		spielfeld[x][y].draw(x, y);
 		    	}
 		    }
 		    
 		    lucida.setScale(0.33f);
 			for(int i = 0; i < players.size(); i++) {
 				Player p = players.get(i);
 				if(p.isAlive()) p.draw();
 				lucida.print((int)(p.getx()*128*0.33f),(int)(p.gety()*128*0.33f) - 6, p.getName());
 			}
 			
 			lucida.setScale(0.50f);
 			if(players.size()==1) {
 				lucida.print(5, 5, "Player 1 is " + (players.get(0).isAlive()?"alive":"dead"));
 			}
 			else {
				lucida.print(5, 5, "Player 1 is " + (players.get(0).isAlive()?"alive":"dead") + ", Player 2 is " + (players.get(1).isAlive()?"alive":"dead")  + " (" + players.size() + ")");
 			}
 			
 		    GameTime.update();
 		    Renderer.sync();
 		}   
         try {
 			TimeUnit.SECONDS.sleep(1);
 		} catch (InterruptedException e) {
 			
 			e.printStackTrace();
 		}    
         for(int i = 0; i < 4; i++){
 			Main.m.hauptButtons[i].setVisible(true);
 			Main.m.gameover.setVisible(true);
 		}
 		Main.m.setVisible(true);
 		Keyboard.destroy(); //Keyboard wird zerstört...
 		try {
 			Keyboard.create(); // ...und hier wieder neu erstellt.
 		} catch (LWJGLException e){
 			e.printStackTrace();
 		}
 		Renderer.destroy();
     }
     
     public static void destroy()
     {
     	Renderer.destroy();
     }
     
 /*    public boolean playersAlive() {
     	
 		if(players.size() <= 0) return false;
 		
 		for(int i = 0; i < players.size(); i++) {
 			Player p = players.get(i);
 			if(p.isAlive() && p.getLives()!=0) {return true;}
 			else{Main.m.gameover.setText("Spieler "+(i+1)+" ist tot!");}
 		}
     	return false;
     } */
     /**
      * Sterbebedingungen fuer Einzel- und Mehrspieler
      * @param p: Uebergabe des zu ueberpruefenden Spielers
      * @return: true, falls betreffender Spieler lebt; false, falls betreffender Spieler tot
      */
    public boolean playerAlive(Player p){
    	
    	if(p.isAlive() && p.getLives()!=0){
    		return true;
    		}
    	else{
    		Main.m.gameover.setText("Spieler "+p.getName()+" ist tot!");
    		return false;
    		}
     }
 
 /**
  * Laed das gewuenschte Level
  * @param name: Uebergabe des Levelnamens
  * @throws FileNotFoundException
  * @throws XMLStreamException
  */
 	protected void initialfeld(String name) throws FileNotFoundException, XMLStreamException{
 		InputStream in = new FileInputStream(name);
 		XMLInputFactory factory = XMLInputFactory.newInstance();
 		XMLStreamReader parser = factory.createXMLStreamReader(in);	
 		
 		spielfeld=new Feld[15][13];
 		int y = -2;		//2mal Start_Element bevor die Attribute kommen, somit y=0 beim ersten Attribut
 		
 		while( parser.hasNext() ) {
 		    int event = parser.next();
 		    if (event==XMLStreamConstants.START_ELEMENT) y++;
 		    switch (event) {
 		        case XMLStreamConstants.END_DOCUMENT:
 		            parser.close();
 		            break;
 		        case XMLStreamConstants.START_ELEMENT:
 		            for( int x = 0; x < parser.getAttributeCount(); x++ ){
 		            	String test = parser.getAttributeValue(x);
 		            	
 		            		if (test.equals("Steinfeld")){spielfeld[x][y]= new Steinfeld();}
 		            		else if (test.equals("Mauerfeld")){spielfeld[x][y]= new Mauerfeld();}
 		            		else if (test.equals("Leerfeld")){spielfeld[x][y]= new Leerfeld();};
 		            		
 		            		/*switch(parser.getAttributeValue(x)){
 		            			case "Steinfeld": spielfeld[x][y]= new Steinfeld(); break;
 		            			case "Mauerfeld": spielfeld[x][y]= new Mauerfeld(); break;
 		            			case "Leerfeld": spielfeld[x][y]= new Leerfeld(); break;
 		            			default: break;
 		            		}*/
 		            }
 		            break;
 		        
 		       
 		        default:
 		            break;
 		    }
 		}
 		
 		
 		
 		
 		
 	}
 	
 	
 }//end class
