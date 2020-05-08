 package newgame;
 
 
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 
 
 public class Board extends JPanel implements ActionListener{
 
 	private static final long serialVersionUID = 1L;
 	Image image;
 	Image img;  																		//Bild fuer den Hintergrund (WEG)
 	private Character Jay;
 	private String raum="";
 	private String lr,rooms,lrs; 														//lr fuer den Namen der Raumdatei, w:wandbild , h:hintergrundsbild
 	private ArrayList<Shot> shots;														//Array fuer die Zeichnung der Schuesse
 	private ArrayList<Sword> swords;
 	private Timer timer;
 	private int BLOCK = 50;																// 50* 50 Pixel
 	private int position;
 	private int need_life;
 	private int ruban=0,xruban;
 	private int life=3, xlife;
 	private int magic=0;
 	private int k,z,posX,posY;
 	boolean ingame,mana,failed,get_sword;
 	private checkpoint check;
 	private Ghost Geist;
 	private Boss Monster;
 	private Ball ball;
 	Font smallfont = new Font("Helvetica", Font.BOLD, 17);
 
 
 
 	ImageIcon r = new ImageIcon("src/Resources/r1.png");								//fuer versch. Positionen rechts, links, oben, unten
 	ImageIcon l = new ImageIcon("src/Resources/l1.png");
 	ImageIcon t = new ImageIcon("src/Resources/Character top.png");
 	ImageIcon b = new ImageIcon("src/Resources/Character.png");
 	ImageIcon tr = new ImageIcon("src/Resources/trankk.png");							//Bilder fuer Traenke, Herzen und Geld an der rechten Seite
 	ImageIcon h1 = new ImageIcon("src/Resources/herz.png");
 	ImageIcon s = new ImageIcon("src/Resources/schatz.png");
 	ImageIcon dr = new ImageIcon("src/Resources/digright.png");							//Bilder fuer den Schlag mit dem Schwert
 	ImageIcon dl = new ImageIcon("src/Resources/digleft.png");
 	ImageIcon du = new ImageIcon("src/Resources/digup.png");
 	ImageIcon db = new ImageIcon("src/Resources/digb.png");
 	ImageIcon sw = new ImageIcon("src/Resources/sword.png");
 	ImageIcon co = new ImageIcon("src/Resources/Coin.png");
 	
 	
 	
 	
 
 	java.util.List<Movement> enemys = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> walls = new java.util.ArrayList<Movement>();				// Array fuer Waende, Gegner, Schluessel, NPC, Muenzen, Traenke, Herzen und Ladenbesitzer
 	java.util.List<Movement> keys = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> wizards = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> coins = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> shopkeepers = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> manas = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> Jays = new java.util.ArrayList<Movement>();
 	java.util.List<Movement> herzen = new java.util.ArrayList<Movement>();
 	
 			
 	Image image1 = image = r.getImage();												// fuer das aktualisieren nach Kollision
 	Image image2 = image = l.getImage();												// mit Jay.getImage() Abfrage
 	Image image3 = image = t.getImage();	
 	Image image4 = image = b.getImage();
 	Image trank = image = tr.getImage();
 	Image herz1 = image = h1.getImage();
 	Image schatz = image = s.getImage();
 	Image image5 = image = dr.getImage();
 	Image image6 = image = dl.getImage();
 	Image image7 = image = du.getImage();
 	Image image8 = image = db.getImage();
 	Image sword = image = sw.getImage();
 	Image coin = image = co.getImage();
 	
 
 	public Board() throws IOException{
 
 		lr="l1r1";																//start des Levels in Level.Raum		
 		addKeyListener(new Ap());
 		setFocusable(true);		
 		initWorld(image4);																//Status des Spielers bei Start, z.B. ohne Mana
 		ingame = true;
 		mana = false;
 	    shots = new ArrayList<Shot>();
 		swords = new ArrayList<Sword>();
 	    timer = new Timer(5, this);														//zeichnet alle  5ms das Board (Schuesse)
         timer.start();
     }
 
 	public void loeschen(boolean b){													//loescht alle Inhalte 
 		coins.clear();
 		walls.clear();
 		enemys.clear();
 		keys.clear();
 		wizards.clear();
 		manas.clear();
 		herzen.clear();
 		shopkeepers.clear();
 		if (b) raum="";
 		if (failed) {
 			if (lr==lrs){
 				raum=rooms;
 			}
 			ruban=0;
 			life=3;
 			magic=3;																	// wenn im ganzen Spiel 3 Manas verbraucht sind, soll nach Niederlage auch verbraucht bleiben
 		}
 	}
 
 	public void collision(int movx,int movy,Image image){																				// char pos mit image geaendert um statt mit  t,v Bild festzulegen man abfragt wo er guckt 
 		
 		int xx = ((Jay.getX()+movx)/BLOCK);																	 							//xx und yy sind die imaginaere Koordinaten innerhalb des Strings Variable (level).
 		int yy=(Jay.getY()+movy)/BLOCK;																									//xx und yy werden dafuer gerechnet um zu erkennen, ob an der Stelle wohin sich die Spielfigur bewegen will, kein # im variable level bzw kein Stueck Mauer im Spielfeld gibt
 		if ((raum.charAt(yy*20+xx)!='#')&&(raum.charAt(yy*20+xx)!='~')&&(raum.charAt(yy*20+xx)!='s')&&(xx>=0)||(Jay.getY()<0))		    //yy wird mal 20 multipliziert da es in jeder linie des Spielfelds 20 Bloecke gibt(also in jeder linie des strings level gibt es 20 zeichen)
 		{																							        							//Wandkollision
 			Jay.move(movx,movy);																		    							//erst wenn es kein Stueck Mauer, keinen NPC/Ladenbesitzer oder einen Ein-Ausgang gibt(entweder xx oder yy <0 ist) darf/kann sich die Spielfigur bewegen
 			if (raum.charAt(yy*20+xx)=='a'){
 				ruban= ruban+1;
 				if (raum.contains("@") )
 				{	int c =raum.lastIndexOf("@");						
 					raum=raum.substring(0,c)+' '+raum.substring(c+1);
 					raum=raum.substring(0,yy*20+xx)+'@'+raum.substring(yy*20+xx+1);
 					try {
 						restartLevel(false,Jay.getImage());
 					} catch (IOException e1) {
 
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
 		if (raum.charAt(yy*20+xx)=='*'){    														// Kollision mit dem Gegner, Neustart des Spiels
			//Game_over();
 			life=life-1;
 			if(life==0){
 				failed=true;
 				try {
 					
 					restartLevel(true,Jay.getImage());
 					//life=life-1;
 				} catch (IOException e1) {
 					
 					e1.printStackTrace();
 				}
 			}
 			else{
 				//Game_over();
 			}
 	   }
 		if (raum.charAt(yy*20+xx)=='~'){    														//startet bei Kollision den Dialog des NPC
 			Dialogue();
 		}
 		if (raum.charAt(yy*20+xx)=='s'){    														//startet bei Kollision den Dialog des Ladenbesitzers
 			DialogueShop();
 		}
 		if (raum.charAt(yy*20+xx)=='h'){															//Nimmt bei Kollision das Herz/Heiltank auf und gibt 30 Gold aus
 				if((ruban>= 30)||(xruban>=30)){
 				life = life+1;
 				xruban = xruban - 30;
 				mana = true;
 				spend_herzen();}
 				else{
 				}
 			}
 
 		
 		if (raum.charAt(yy*20+xx)=='q'){
			if (ruban >= 20){
 															//Nimmt bei Kollision den Manatrank auf und gibt 20 Gold aus
 			magic = magic + 1;
 			mana = true;
 			spend_mana();
 			xruban = xruban-20;
 		}
 			else{
 			}
 			}
 		if (raum.charAt(yy*20+xx)=='m'){    														// Bei Einsammeln des Manatranks verschwindet das Bild und er erhaelt auf der rechten Seite ein Mana
 				magic = magic  + 1;
 				mana = true;
 				spend_mana();
 
 
 		}
 		else if (raum.charAt(yy*20+xx)=='$'){														//startet die naechste Welt wenn ein Schluessel aufgenommen wurde
 			if (lr.charAt(1)=='1') lr="l2r1";
 			else if (lr.charAt(1)=='2')lr="l3r1"; 
 			else if (lr.charAt(1)=='3')lr="l3r6";
 			loeschen(true);
 			try {
 				initWorld(Jay.getImage());
 			} catch (IOException e1) {
 
 				e1.printStackTrace();
 			}
 		}
 		else if (raum.charAt(yy*20+xx)=='b'){														//speichert Status des Raumes mit Muenzen, Gegner und Position
 			xruban=xruban+ruban;															
 			//xlife=xlife+life;
 			ruban=0;
 			posX=Jay.getX();
 			posY=Jay.getY();
 			rooms=raum;
 			lrs=lr;
 		}
 	}
 
 	
 	
 	public void movemonster() {
 		
 		if (Jay.getY()<Monster.getY()){
 			Monster.move(0,-Monster_speed);
 		}
 		else if (Jay.getY()>Monster.getY()){
 			Monster.move(0,Monster_speed);
 		}
 		if (Jay.getX()>Monster.getX()){
 			Monster.move(Monster_speed,0);
 		}
 		else if (Jay.getX()<Monster.getX()){
 			Monster.move(-Monster_speed,0);
 		}
 		kollision_boss_spieler();
 	}
 		
 		public void movegeist() {
 			
 		
 		if (Jay.getY()<Geist.getY()){
 			Geist.move(0,-Geist_speed);
 		}
 		else if (Jay.getY()>Geist.getY()){
 			Geist.move(0,Geist_speed);
 		}
 		else if (Jay.getX()>Geist.getX()){
 			Geist.move(Geist_speed,0);
 		}
 		else if (Jay.getX()<Geist.getX()){
 			Geist.move(-Geist_speed,0);
 		}
 		
 	}
 	
 	private int mx,my,counter,Monster_speed,Geist_speed,schuss_speed;
 
 	
 	public void moveBall() {
 			
 		if (ball.getX()<mx){
 			ball.move(schuss_speed, 0);
 		}
 		else if (ball.getX()>mx){
 			ball.move(-schuss_speed, 0);
 		}
 		if (ball.getY()<my){
 			ball.move(0, schuss_speed);
 		}
 		else if (ball.getY()>my){
 			ball.move(0,-schuss_speed);
 		}
 		
 		kollision_ball_spieler();
 		
 		if (counter % 150==0){
 			ball.setX(Monster.getX());
 			ball.setY(Monster.getY());
 			mx=0;my=0;
 		}
 		/*if (counter % 150==0){
 			ball.setX(Geist.getX());
 			ball.setY(Geist.getY());
 			mx=0;my=0;
 		}*/
 	}
 	
 			
 	
 	private void kollision_ball_spieler() {
 		if ((Math.abs(Jay.getX()-ball.getX())<50)&&(Math.abs(Jay.getY()-ball.getY())<50)){
 			if(life==1){
 				
 				Game_over();
 				
 				failed=true;
 				try {
 					restartLevel(true,Jay.getImage());
 				} catch (IOException e1) {
 
 					e1.printStackTrace();
 				}
 			}
 			else{
 				life=life-1;
 			}
 		}
 	}
 	
 	
 	private void kollision_boss_spieler() {
 		if ((Math.abs(Jay.getX()-Monster.getX())<50)&&(Math.abs(Jay.getY()-Monster.getY())<50)){
 			if(life==1){
 				
 				Game_over();
 				
 				failed=true;
 				try {
 					restartLevel(true,Jay.getImage());
 				} catch (IOException e1) {
 
 					e1.printStackTrace();
 				}
 			}
 			else{
 				life=life-1;
 			}
 		}
 	}
 
 	public Image getImage(){																		//laedt Bilder
 		return image;
 	}
 
 	public String raumeinlesen() throws IOException{
 		String room="";
 		FileReader fr = new FileReader("src/Resources/"+lr+".txt");
 		BufferedReader br = new BufferedReader(fr);
 		String zeile = br.readLine();
 		while (zeile != null)
 		{
 			room=room+zeile+'\n';
 		    zeile = br.readLine();
 		}
 		br.close();
 		return room;
 	}
 
 	public final void initWorld(Image image) throws IOException{										// zeichnet das Level mit Inhalten
 
 		//ImageIcon ii= new ImageIcon ("src/Resources/back"+lr.charAt(1)+".png");						// Den Pfad fuers Hintergrundbild angeben.
 		//img=ii.getImage();		//Image importieren.	
 		setBackground(Color.BLACK);																		// dunkler Hintergrund fuer den Kontrast der Schuesse
 		if (raum=="") raum=raumeinlesen();
 		int x = 0;
 		int y = 0;
 		Wall wall;
 		Coin coin;
 		Enemy enemy;
 		Key key;
 		Wizard wizard;
 		Shopkeeper shopkeeper;
 		Mana mana;
 		Mana shopmana;
 		Heiltrank heiltrank;
 		
 
 		for(int i = 0; i < raum.length(); i++){																		// level variable Buchstabe fuer Buchstabe durchgehen.
 
 			char obj = raum.charAt(i);										
 
 			if(obj == '\n'){																						//y erhoeht sich um ein BLOCK wenn man ein /n im String Level findet.
 				y = y + BLOCK;
 				x = 0;
 			}else if(obj == '#'){																					// # bezeichnet ein Stueck Mauer. eine Mauer im array walls an seine Position speichern.
 				wall = new Wall(x,y, "wand"+ lr.charAt(1));
 				walls.add(wall);
 				x = x + BLOCK;
 			}
 			else if(obj == '@'){																					// Legt die Position des Charakters beim Levelstart fest
 				if (lr!="l3r4"){	
 					if (failed==false)Jay = new Character(x,y);
 					else if (failed){
 						if (lr==lrs) Jay=new Character(posX,posY);
 						else Jay = new Character(x,y);
 						failed=false;
 					}
 					if (image==image1){
 						image =	r.getImage();																		//Image vom Spieler der nach rechts laeuft
 						Jay.setImage(image);}																		// abspeichern um mit Jay.getImage abzufragen bei Kollision
 					if (image==image2){
 						image =	l.getImage();
 						Jay.setImage(image);}
 					if (image==image3){
 						image =	t.getImage();
 						Jay.setImage(image);}
 					if (image==image4){
 						image =	b.getImage();
 						Jay.setImage(image);}	
 					if (image==image5){
 						image =	dr.getImage();
 						Jay.setImage(image);}	
 					if (image==image6){
 						image =	dl.getImage();
 						Jay.setImage(image);}
 					if (image==image7){
 						image =	du.getImage();
 						Jay.setImage(image);}
 					if (image==image8){
 						image =	db.getImage();
 						Jay.setImage(image);}
 
 				x = x + BLOCK;}
 			}
 			else if(obj == ' '){																	//x erhoeht sich um einen Block(' ':Bereich wo sich der Spieler bewegen kann)
 					x = x + BLOCK;
 			}
 			else if(obj == '*'){                													// stellt den Enemy in den Levels als * dar
 					enemy = new Enemy(x,y);
 					enemys.add(enemy);
 					x = x + BLOCK;
 			}
 			else if(obj == '$'){                													// stellt den Schluessel in den Levels als $ dar
 					key = new Key(x,y);	
 					keys.add(key);
 					x = x + BLOCK;
 			}
 			else if (obj=='a'){																		//stellt die Muenzen in den Levels als ein 'a' dar
 					coin=new Coin(x,y);
 					coins.add(coin);
 					x=x+BLOCK;
 			}
 			else if(obj == '~'){																	//stellt den NPC in den Levels als ein ~ dar
 					wizard = new Wizard(x,y);
 					wizards.add(wizard);
 					x = x + BLOCK;
 			}
 			else if(obj == 's'){																	//stellt den Ladenbesitzer in den Levels als ein s dar
 					shopkeeper = new Shopkeeper(x,y);
 					shopkeepers.add(shopkeeper);
 					x = x + BLOCK;
 			}
 			else if(obj == 'b'){																	// Legt die Position des Charakters beim Tod fest
 					check = new checkpoint(x,y);
 					x=x+BLOCK;
 			}
 			else if(obj == 'h'){																	//stellt die Heiltraenke/Herzen in den Levels als ein 'h' dar
 				heiltrank = new Heiltrank(x,y);
 				herzen.add(heiltrank);
 				x=x+BLOCK;
 				
 			}
 			else if(obj == 'k'){																	// Legt die Position des Bosses beim Tod fest
 				Monster = new Boss(x,y);
 				boss_leben=10;
 				x=x+BLOCK;
 			}
 			else if(obj == 'w'){																	// Legt die Position des Bosses beim Tod fest
 				Geist = new Ghost(x,y);
 				x=x+BLOCK;
 			}
 			else if(obj == 'r'){																	
 				 ball = new Ball(x,y);
 				x=x+BLOCK;
 
 			}	
 			else if(obj == 'm'){																	// Legt die Position des Manatrankes fest
 					mana = new Mana(x,y);
 					manas.add(mana);
 					x=x+BLOCK;
 			}
 			else if(obj == 'q'){																	//Legt die Position des Manatrankes im Shop fest
 					shopmana = new Mana(x,y);
 					manas.add(shopmana);
 					x=x+BLOCK;
 					
 					
 			}
 		}
 	}
 	ArrayList<Movement> world = new ArrayList<Movement>();
 	public void buildWorld( Graphics g){
 
 		g.drawImage(img, 0, 0, null);																//Background Image zeichnen
 		ArrayList<Movement> world = new ArrayList<Movement>();
 
 		if (raum.contains("b")==true)world.add(check);																			//im levelend soll es kein Spielfigur geben
 		if (lr!="l3r6")world.add(Jay);
 		if (raum.contains("k")) world.add(Monster);
 		if (raum.contains("r")) world.add(ball);
 		if (raum.contains("w")) world.add(Geist);
 		world.addAll(keys);
 		world.addAll(wizards);
 		world.addAll(coins);
 		world.addAll(shopkeepers);
 		world.addAll(manas);
 		world.addAll(herzen);
 
 		for(int i = 0; i < world.size(); i++){														// Array world durchgehen um objekte zu zeichnen.
 
 			Movement obj = (Movement) world.get(i);
 			g.drawImage(obj.getImage(), obj.getX(), obj.getY(), this);								// g.drawImage fuer die Grafische Zeichnung
 		}
 		
 	       ArrayList<Shot> shots = getShots();
 	       		for (int j = 0; j < shots.size(); j++){
 	       			Shot m = (Shot) shots.get(j);
 	       			g.drawImage(m.getImage(), m.getX(), m.getY(), this);
                 }
 	       		
 	       
 	      ArrayList<Sword> swords = getSwords();
 	       		for (int i = 0; i < swords.size(); i++){
 	       			Sword s = (Sword) swords.get(i);
 	       			g.drawImage(s.getImage(), s.getX(), s.getY(), this);
                }
 
 	       			for (int i = 0; i < enemys.size(); i++){										// Enemy soll nur gezeichnet werden, wenne es noch nicht tot ist 
 	       				Enemy e = (Enemy) enemys.get(i);
 	       				if (e.isVisible())
 	       				g.drawImage(e.getImage(), e.getX(), e.getY(), this);
 	       			}
 
 	       			for (int i = 0; i < walls.size(); i++){											//  NICHT LOESCHEN FUER DAS TESTEN DER WAENDE 
 
 	       				Wall w = (Wall) walls.get(i);
 	       				if (w.isVisible())
 	       				g.drawImage(w.getImage(), w.getX(), w.getY(), this);
 	       			}
 
 	       			for (int i = 0; i < coins.size(); i++){											// Muenzen sollen gezeichnet werden bei Tod
 
 	       				Coin c = (Coin) coins.get(i);
 	       				if (c.isVisible())
 	       				g.drawImage(c.getImage(), c.getX(), c.getY(), this);
 	       			}
 	       			 			
 	}
     
 
 
     public void paint(Graphics g){
 	super.paint(g);
 
 	if(ingame){																					// falls Spiel nicht verloren
 		buildWorld(g);																			// zeichnet Welt mit Punktestand..
 		if (raum.contains("k")){
 			if (lr.charAt(1)=='3') Monster_speed=1;
 			else Monster_speed=1;
 			movemonster();
 		}
 		if (raum.contains("w")){
 			if (lr=="l1r1") Geist_speed=2;
 			else Geist_speed=1;
 			movegeist();
 		}
 		if (raum.contains("r")){
 				if (mx==0)mx=Jay.getX();
 				if (my==0)my=Jay.getY();
 				if (lr.charAt(1)=='3') {
 					schuss_speed=4;
 				}
 				else if (lr.charAt(1)=='2'){
 					schuss_speed=3;
 					
 				}
 				else if (lr.charAt(1)=='1'){
 					schuss_speed=2;
 					
 				}
 				counter=counter+1;
 				moveBall();
 			} 
 		int countsmoney= ruban + xruban;
 	        String s,w,l,k;
 
 	        g.setFont(smallfont);																// Geldanzeige
 	        g.setColor(new Color(98,150,255));
 	        s = "Money: " + (countsmoney);
 	        g.drawString(s,970,160);
 	    	g.drawImage(schatz,970,180,this);													// zeichnet Welt mit Punktestand..
 		
 	    	int lifebar= life;
 	    	
 			String t;
 																								// Lebensanzeige
 			t = "Leben: " + (lifebar);
 			g.drawString(t,970,40);
 			
 			if(life > 3){
 				life = 3;
 			}
 			if(life==3){																		// zeichnet 3 Herzchen fuer 3 Leben
 			g.drawImage(herz1,970,60,this);
 			g.drawImage(herz1,1020, 60, this);
 			g.drawImage(herz1,1070, 60, this);
 			}
 			if(life==2){						
 				g.drawImage(herz1,970,60,this);
 				g.drawImage(herz1,1020, 60, this);
 			}
 			need_life = 1;																		// need_life flag fuer das 2te Mana gibt volles Leben und zeichnet Herzchen
 			if(life==1){
 				g.drawImage(herz1,970,60,this);
 			}
 			need_life = 1;
 		
 			   String mes;
    		        g.setFont(smallfont);																// Manaanzeige
    		        g.setColor(new Color(98,150,255));
    		        mes = "Mana: " + (magic)+ " von 3 verbraucht";
    		        g.drawString(mes,970,280);
    		 
 	        	if(mana==true){
 	        		
 	        		if(magic==1){																	// Beim Aufnehmen des ersten Manatrankes erhaelt Diggy 100 Gold
 	        			g.drawImage(trank, 970, 300, this);
 	        			g.drawImage(coin,970,400,this);
 						xruban = + 30;
 					    w = " Du hast + 30 $ ! ";
 				        g.drawString(w,970,380);
 	        		}
 	        			if(magic==2){																// Beim Aufnehmen des zweiten Manatrankes erhaelt Diggy volles Leben
 	        				g.drawImage(trank,970,300,this);
 	        				g.drawImage(trank,1020,300, this);
 	        				g.drawImage(coin,970,400,this);
 	        				g.drawImage(herz1,1020,400, this);
 	        			    l = " Du erhaeltst volles Leben ! ";
 					        g.drawString(l,970,380);
 	        					if(need_life==1){
 	        						life = 3;
 	        					}
 	        			}
 	        	 if(magic==3){																	// beim 3 mana soll er eine Waffe kriegen ??
 	        			g.drawImage(trank,970,300,this);
 	        			g.drawImage(trank,1020, 300, this);
 	        			g.drawImage(trank,1070, 300, this);
 	        			g.drawImage(coin,970,400,this);
 	        			g.drawImage(herz1,1020,400, this);
 	        			g.drawImage(sword,1070, 400, this);
 	        			  k = " Du hast ein Schwert!";
 					        g.drawString(k,970,380);
 					        get_sword = true;
 	        			
 	        	 }
 	        	 if (magic > 3){			
 	        		 magic = 3;
 	        	 }
 	       }
 	}else{																					 	// was bei Niederlage passieren soll..
 	}    
 }
 
     public ArrayList<Shot> getShots() {																// gibt die Schuesse  wieder
 	        return shots;
 	    }
    
 
     public ArrayList<Sword> getSwords() {																
 	     return swords;
     }
 
 	private class Ap extends KeyAdapter{															// fuer rechts: holt das Bild mit Position rechts
 																									// durch die class Character bewegt sich Diggy in die entsprechende Richtung
 		public  void keyPressed(KeyEvent e){
 
 			int key = e.getKeyCode();
 
 			if(key == KeyEvent.VK_RIGHT){		
 
 				Image image1 = image = r.getImage();												//Image vom Spieler der nach rechts laeuft usw.
 				Jay.setImage(image);
 				position = 1;
 				collision(BLOCK,0, image1);
 
 			}else if(key == KeyEvent.VK_LEFT){
 
 				Image image2 = image = l.getImage();
 				Jay.setImage(image);
 				position = 2;
 				collision(-BLOCK,0, image2);
 
 			}else if(key == KeyEvent.VK_UP){
 
 				Image image3= image = t.getImage() ;
 				Jay.setImage(image);
 				position = 3;
 				collision(0,-BLOCK,image3);
 
 			}else if(key == KeyEvent.VK_DOWN){
 
 				Image image4 = image = b.getImage();
 				Jay.setImage(image);
 				position = 4;
 				collision(0,BLOCK, image4);
 
 			}else if (key == KeyEvent.VK_SPACE) {													// Taste -Space ruft die Funktion fire auf
 				fire();
 			}else if (key == KeyEvent.VK_V && get_sword==true) {														// 2 te Waffe = Schwertkampf in versch Richtungen
 				sword_play();
 			}
 
 			repaint();
 
 			if ((Jay.getY()==-BLOCK)||(Jay.getY()==0))  {	
 				if (lr.charAt(3)=='1') lr=lr.substring(0,3)+"2";									//Wenn der Spieler am Ausgang des 1. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='2') lr=lr.substring(0,3)+'3';								//Wenn der Spieler am Ausgang des 2. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='3') lr=lr.substring(0,3)+'4';								//Wenn der Spieler am Ausgang des 3. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='4') lr=lr.substring(0,3)+'5';//Wenn der Spieler am Ausgang des 4. Raums ist dann ueberwechseln
 				
 				xruban=xruban+ruban;
 				xlife=xlife+life;
 				ruban=0;
 				loeschen(true);
 				try {
 					initWorld(Jay.getImage());
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}																					//world initialisieren 
 			}
 			if (Jay.getX() ==950 ){																	//Bedingung erfuellt nur am Ausgang des 2. Raums
 				if (lr.charAt(3)=='1') lr=lr.substring(0,3)+"2";									//Wenn der Spieler am Ausgang des 1. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='2') lr=lr.substring(0,3)+'3';								//Wenn der Spieler am Ausgang des 2. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='3') lr=lr.substring(0,3)+'4';								//Wenn der Spieler am Ausgang des 3. Raums ist dann ueberwechseln
 				else if (lr.charAt(3)=='4') lr=lr.substring(0,3)+'5';								//Wenn der Spieler am Ausgang des 4. Raums ist dann ueberwechseln
 				xruban=xruban+ruban;
 				xlife=xlife+life;
 				ruban=0;
 				loeschen(true);
 				try {
 					initWorld(Jay.getImage());
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public void restartLevel(boolean test,Image image2) throws IOException {			
 		loeschen(test);
 		initWorld(Jay.getImage());
 	}
 
 	 public void fire() {
 		 	if(position==1){
 		 		shots.add(new Shot(Jay.getX() + BLOCK, Jay.getY()));								// Posistion der Schussrichtung, je in welche Richtung Diggy guckt
 		 		k = 00;}
 		 	if(position==2){																		// der Schuss soll nicht ueber Diggy gehen 
 		 		shots.add(new Shot(Jay.getX() - BLOCK, Jay.getY()));								// k als Flag
 		 		k = 01;}
 		 	if(position==3){
 		 		shots.add(new Shot(Jay.getX(), Jay.getY() - BLOCK));
 		 		k = 10;}
 		 	if(position==4){
 		 		shots.add(new Shot(Jay.getX(), Jay.getY() + BLOCK));	
 		 	    k = 11;}
 	}
 	 
 	 public void sword_play(){																		// Schwertkampf mit 4 Richtungen zum schiessen				
 	 
 	 	 	if(position==1){																		
 		 		image = dr.getImage();
 				Jay.setImage(image);
 				swords.add(new Sword(Jay.getX() + 2, Jay.getY()));								    // Posistion der Schwertrichtung, je in welche Richtung Diggy guckt
 		 		z = 00;}																			// tot nach 2 Entfernung mit Schwert
 		 	if(position==2){			
 		 		image = dl.getImage();
 				Jay.setImage(image);																	
 			 	swords.add(new Sword(Jay.getX() - 2, Jay.getY()));									// z als Flag fuer die Richtungen des Angriffs
 			 	z = 01;}
 		 	if(position==3){
 		 		image = du.getImage();
 				Jay.setImage(image);
 				swords.add(new Sword(Jay.getX(), Jay.getY() - 2));
 		 		z = 10;}
 			if(position==4){
 		 		image = db.getImage();
 				Jay.setImage(image);
 				swords.add(new Sword(Jay.getX(), Jay.getY() + 2));	
 		 	    z = 11;}
 		 	}
 
 	 @Override
 	 public void actionPerformed(ActionEvent e) {													// zeichnet die Schuesse 
 
      ArrayList<Shot> shots = getShots();
      
      for (int i = 0; i < shots.size(); i++) {
     	 Shot m = (Shot) shots.get(i);																// falss limit des Boards nicht ueberschritten
 		 																							// wird je nach Blickrichtung in die richtgige Richtungen angegriffen
     	 if(m.isVisible()){	 	
 		 					
 			if(k==00) m.move_r();
 			if(k==01) m.move_l();
 			if(k==10) m.move_u();
 			if(k==11) m.move_d();
 			
 			
 		}else shots.remove(i);
     	 	check_shot_vs_wall();																	// Kollisionabfrage mit Schuss
 			check_shot_vs_enemy();
 			check_shot_vs_coin();
 			if (raum.contains("k")){
 				check_shot_vs_boss();
 			}
 	}
 			 ArrayList<Sword> swords = getSwords()
 					 ;
 			 for (int j = 0; j < swords.size(); j++) {												// fuer den Schwertkampf mit versch Bildern und Angriffsrichtungen
 				 Sword s = (Sword) swords.get(j);
 				 
 				 		if(s.isVisible()){
 							
 				 			if(z==00) s.move_r_sword();
 				 			if(z==01) s.move_l_sword();
 				 			if(z==10) s.move_u_sword();
 				 			if(z==11) s.move_d_sword();
 							
 						}else swords.remove(j);
 				 			check_sword_vs_wall();													// Kollision mit Schwertangriff 
 				 			check_sword_vs_enemy();
 				 			check_sword_vs_coin();
 	 		}	
 			 repaint();	
 	}
 		 
 		public Rectangle getBounds(){
 			return new Rectangle(Jay.getX(),Jay.getY(),50,50);				
 		}
 
 			public void Dialogue(){																	//definiert die Methode Dialogue genauer, mit Close-Operation, Name, Layout und Position
 
 			JFrame Dialogue = new Dialogue("Weiser Zauberer");
 
 			Dialogue.setSize(600,300);
 			Dialogue.setLocationRelativeTo(null);
 			Dialogue.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 			Dialogue.setVisible(true);
 			Dialogue.setFocusable(true);
 			Dialogue.setLayout(new BorderLayout());
 			Dialogue.setLayout(null);
 			Dialogue.add(new Dialogue("Weiser Zauberer"));
 		}
 
 		public void DialogueShop(){                                  								//definiert die Methode DialogueShop genauer, mit Close-Operation, Name, Layout und Position      
 
 		JFrame DialogueShop = new DialogueShop("Ladenbesitzer");
 		DialogueShop.setSize(600,300);
 		DialogueShop.setLocationRelativeTo(null);
 		DialogueShop.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		DialogueShop.setVisible(true);
 		DialogueShop.setFocusable(true);
 		DialogueShop.setLayout(new BorderLayout());     
 		DialogueShop.setLayout(null);
 	    DialogueShop.add(new Dialogue("Ladenbesitzer"));
 	    }
 
 		public void check_shot_vs_coin() {															// schiesst nicht durch Coins
 
 			ArrayList<Shot> shots = getShots();
 
 			    for (int i = 0; i < shots.size(); i++) {
 			        Shot m = (Shot) shots.get(i);
 
 			        Rectangle r1 = m.getBounds();
 
 			        for (int j = 0; j < coins.size(); j++) {
 				        Coin c = (Coin) coins.get(j);
 				        Rectangle r2 = c.getBounds();
 
 			            if (r1.intersects(r2)) {													// Coin bleib sichtbar aber Schuss auf Coin nicht
 			                m.setVisible(false);
 			                c.setVisible(true);
 			            }
 			        }
 			    }
 			}
 		public void check_sword_vs_coin() {										
 
 			 ArrayList<Sword> swords = getSwords();
 			 
 				for (int j = 0; j < swords.size(); j++) {										
 					Sword s = (Sword) swords.get(j);
 
 			        Rectangle r1 = s.getBounds();
 
 			        for (int i = 0; i< coins.size(); i++) {
 				        Coin c = (Coin) coins.get(i);
 				        Rectangle r2 = c.getBounds();
 
 			            if (r1.intersects(r2)) {
 			                s.setVisible(false);
 			                c.setVisible(true);
 			            }
 			        }
 			    }
 			}
 		public void check_sword_vs_wall() {																
 
 			 ArrayList<Sword> swords = getSwords();
 			 
 				for (int j = 0; j < swords.size(); j++) {										
 					Sword s = (Sword) swords.get(j);
 
 			        Rectangle r1 = s.getBounds();
 
 			        for (int i = 0; i< walls.size(); i++) {
 				        Wall w = (Wall) walls.get(i);
 				        Rectangle r2 = w.getBounds();
 
 			            if (r1.intersects(r2)) {
 			                s.setVisible(false);
 			                w.setVisible(true);
 			            }
 			        }
 			    }
 			}
 
 		
 		private int boss_leben;
 		
 		public void check_shot_vs_boss() {																
 			ArrayList<Shot> shots = getShots();
 
 		    for (int i = 0; i < shots.size(); i++) {
 		        Shot m = (Shot) shots.get(i);
 		        Rectangle r1 = m.getBounds();
 		        
 		        if ((Math.abs(r1.x-Monster.getX())<50)&&(Math.abs(r1.y-Monster.getY())<50)) {													 		
 		        		boss_leben=boss_leben-1;
 		        		if (boss_leben==0){
 		        			if (lr.charAt(3)=='5')lr=lr.substring(0,3)+'6';
 		        			else if (lr.charAt(3)=='4')lr=lr.substring(0, 3)+'5';
 		        			loeschen(true);
 		        			try {
 		        				initWorld(Jay.getImage());
 		        			} catch (IOException e1) {
 
 		        				e1.printStackTrace();
 		        			}
 		        		}
 		        }
 	        }
 
 		}
 		
 		public void check_shot_vs_wall() {																// Wandkollision
 
 			ArrayList<Shot> shots = getShots();
 
 		    for (int i = 0; i < shots.size(); i++) {
 		        Shot m = (Shot) shots.get(i);
 
 		        Rectangle r1 = m.getBounds();
 
 		        for (int j = 0; j < walls.size(); j++) {
 			        Wall w = (Wall) walls.get(j);
 			        Rectangle r2 = w.getBounds();
 
 		            if (r1.intersects(r2)) {													 		//  schuss ist auf der Wand nicht sichtbar
 		                m.setVisible(false);
 		                w.setVisible(true);
 
 		             }
 		        }
 		    }
 		}
 
 		public void check_shot_vs_enemy() {								
 
 			for (int i = 0; i < shots.size(); i++) {
 		        Shot m = (Shot) shots.get(i);
 		        Rectangle r1 = m.getBounds();
 		  
 		   	      	int xx = (int) ((r1.getX())/BLOCK);																	
 	        		int yy=(int)(r1.getY())/BLOCK;
 
 	        		if (raum.charAt(yy*20+xx)=='*') {																		// ersetzt in der txt datei enemy mit ' '																							
 		        		int xxx = ((Jay.getX())/BLOCK);																	
 		        		int yyy=(Jay.getY())/BLOCK;	
 
 		        		raum=raum.substring(0,yy*20+xx)+' '+raum.substring(yy*20+xx+1);	
 		        				int c =raum.lastIndexOf("@");						
 		    					raum=raum.substring(0,c)+' '+raum.substring(c+1);
 		    					raum=raum.substring(0,yyy*20+xxx)+'@'+raum.substring(yyy*20+xxx+1);
 		    					try {
 		    						restartLevel(false, (Jay.getImage()));
 		    					} catch (IOException e1) {
 		    						e1.printStackTrace();
 		    					}
 	        		}
 		
 		        }
 			}
 		
 		public void check_sword_vs_enemy() {	
 			 ArrayList<Sword> swords = getSwords();
 			
 
 			for (int i = 0; i < swords.size(); i++) {
 		        Sword s = (Sword) swords.get(i);
 		        Rectangle r1 = s.getBounds();
 	
 		            int xx = (int) ((r1.getX())/BLOCK);																	//xx und yy sind die imaginaere Koordinaten innerhalb des Strings Variable (level).
 	        		int yy=(int)(r1.getY())/BLOCK;
 
 	        		if (raum.charAt(yy*20+xx)=='*') {																	// wenn ein Gegner angeschossen wird wird der Gegner im txt mit ' ' ersetzt	Diggy bleibt dort wo er war															
 		        		int xxx = ((Jay.getX())/BLOCK);																	//xx und yy sind die imaginaere Koordinaten innerhalb des Strings Variable (level).
 		        		int yyy=(Jay.getY())/BLOCK;	
 
 		        		raum=raum.substring(0,yy*20+xx)+' '+raum.substring(yy*20+xx+1);	
 		        				int c =raum.lastIndexOf("@");						
 		    					raum=raum.substring(0,c)+' '+raum.substring(c+1);
 		    					raum=raum.substring(0,yyy*20+xxx)+'@'+raum.substring(yyy*20+xxx+1);
 		    					try {
 		    						restartLevel(false, (Jay.getImage()));
 		    					} catch (IOException e1) {
 		    						e1.printStackTrace();
 		    					}
 	        		}
 	        		
 			}
 	}
 		public void spend_mana(){
 
 			int xx = (int) ((Jay.getX())/BLOCK);																	//xx und yy sind die imaginaere Koordinaten innerhalb des Strings Variable (level).
 	        int yy=(int)(Jay.getY())/BLOCK;
 
 	        	if (raum.charAt(yy*20+xx)=='m'||(raum.charAt(yy*20+xx)=='q')) {										// wenn mana eingesammelt wird, wird mana ' ' ersetzt	Diggy bleibt dort wo er war															
 		        		int xxx = ((Jay.getX())/BLOCK);																
 		        		int yyy=(Jay.getY())/BLOCK;	
 
 		        		raum=raum.substring(0,yy*20+xx)+' '+raum.substring(yy*20+xx+1);	
 		        				int c =raum.lastIndexOf("@");						
 		    					raum=raum.substring(0,c)+' '+raum.substring(c+1);
 		    					raum=raum.substring(0,yyy*20+xxx)+'@'+raum.substring(yyy*20+xxx+1);
 		    					try {
 		    						restartLevel(false, (Jay.getImage()));
 		    					}catch (IOException e1) {
 		    						e1.printStackTrace();
 		    					}
 	        	}
 		}
 		
 		public void spend_herzen(){
 
 			int xx = (int) ((Jay.getX())/BLOCK);											//xx und yy sind die imaginaere Koordinaten innerhalb des Strings Variable (level).
 	        int yy=(int)(Jay.getY())/BLOCK;
 
 	        	if (raum.charAt(yy*20+xx)=='h') {											// wenn herzen eingesammelt werden, werden herzen durch ' ' ersetzt, Diggy bleibt dort wo er war															
 		        		int xxx = ((Jay.getX())/BLOCK);																
 		        		int yyy=(Jay.getY())/BLOCK;	
 
 		        		raum=raum.substring(0,yy*20+xx)+' '+raum.substring(yy*20+xx+1);	
 		        				int c =raum.lastIndexOf("@");						
 		    					raum=raum.substring(0,c)+' '+raum.substring(c+1);
 		    					raum=raum.substring(0,yyy*20+xxx)+'@'+raum.substring(yyy*20+xxx+1);
 		    					try {
 		    						restartLevel(false, (Jay.getImage()));
 		    					}catch (IOException e1) {
 		    						e1.printStackTrace();
 		    					}
 	        	}
 		}
 
 public void Game_over(){
 	
 	 JFrame Game_over = new JFrame();
 
 	 Game_over.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	 Game_over.setSize(500,500);
 	 Game_over.setVisible(true);
 	 Game_over.setFocusable(true);
 	 Game_over.setLocationRelativeTo(null);   																			// Fenster in der Mitte 
 	 Game_over.add(new Game_over());
 	}
 }
 
  
