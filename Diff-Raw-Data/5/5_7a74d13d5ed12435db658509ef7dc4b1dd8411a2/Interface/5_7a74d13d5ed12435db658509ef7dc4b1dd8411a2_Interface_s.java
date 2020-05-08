 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 
 
 /**
  * 
  * erstellt das Menu
  *
  */
 public class Interface extends JFrame implements ActionListener, KeyListener{
 
 	public static JButton starten;
 	public static JButton ende;
 	public static int reihe;
 	public static int spalte;
 
 	private int counter;
 	private int counter2;
 	private int counter3;
 	private int returner;
 	private static boolean spielGestartet;
 
 	public static final String MAUERIMG = "Images\\mauer.jpg";
 	public static final String BODENIMG = "Images\\boden.jpg";
 	public static final String CHECKPOINTIMG = "Images\\checkpoint.jpg";
 	public static final String EMPTYHEARTIMG = "Images\\emptyHeart.jpg";
 	public static final String EMPTYMANAIMG = "Images\\emptyMana.jpg";
 	public static final String FALLEIMG = "Images\\falle.jpg";
 	public static final String FULLHEARTIMG = "Images\\fullHeart.jpg";
 	public static final String FULLMANAIMG = "Images\\fullMana.jpg";
 	public static final String GAMEOVERIMG = "Images\\gameover.jpg";
 	public static final String GEWONNENIMG = "Images\\gewonnen.jpg";
 	public static final String HALFHEARTIMG = "Images\\halfHeart.jpg";
 	public static final String HALFMANAIMG = "Images\\halfMana.jpg";
 	public static final String MOBIMG = "Images\\mob.jpg";
 	public static final String SIEGIMG = "Images\\sieg.jpg";
 	public static final String SPIELFIGURIMG = "Images\\spielfigur.jpg";
 	public static final String STARTIMG = "Images\\start.jpg";
 	public static final String ZIELIMG = "Images\\ziel.jpg";
 	public static final String FIGURGELB = "Images\\figurGelb.jpg";
 	public static final String FIGURROT = "Images\\figurRot.jpg";
 	public static final String FIGURBLAU = "Images\\figurBlau.jpg";
 	public static final String FIGURGELBSCHILD = "Images\\figurGelbSchild.jpg";
 	public static final String FIGURROTSCHILD = "Images\\figurRotSchild.jpg";
 	public static final String FIGURBLAUSCHILD = "Images\\figurBlauSchild.jpg";
 	public static final String FARBEGELBIMG = "Images\\farbeGelb.jpg";
 	public static final String FARBEROTIMG = "Images\\farbeRot.jpg";
 	public static final String FARBEBLAUIMG = "Images\\farbeBlau.jpg";
 	public static final String STORYTELLERIMG = "Images\\storyteller.jpg";
 	public static final String MUENZENIMG = "Images\\muenze.jpg";
 	public static final String SHOP1IMG = "Images\\shop1.jpg";
 	public static final String SHOP2IMG = "Images\\shop2.jpg";
 	public static final String SHOP3IMG = "Images\\shop3.jpg";
 	public static final String HPTRANKIMG = "Images\\hpTrank.jpg";
 	public static final String MANATRANKIMG = "Images\\manaTrank.jpg";
 	public static final String BOSS1IMG = "Images\\boss1.jpg";
 	public static final String BOSS2IMG = "Images\\boss2.jpg";
 	public static final String BOSS3IMG = "Images\\boss3.jpg";
 	
 	public static final int RECHTS = 0;
 	public static final int UNTEN = 1;
 	public static final int LINKS = 2;
 	public static final int OBEN= 3;
 
 	public static final int BODEN = 0;
 	public static final int MAUER = 1;
 	public static final int START = 2;
 	public static final int ZIEL = 3;
 	public static final int FALLE = 4;
 	public static final int MOB = 5;
 	public static final int FIGUR = 6;
 	public static final int SIEG = 7;
 	public static final int CHECKPOINT = 8;
 	public static final int STORYTELLER = 9;
 	public static final int MUENZEN = 15;
 	public static final int SHOP1 = 18;
 	public static final int SHOP2 = 19;
 	public static final int SHOP3 = 20;
 	public static final int FARBEGELB = 12;
 	public static final int FARBEBLAU = 10;
 	public static final int FARBEROT = 11;
 	public static final int GELB = 0;
 	public static final int BLAU = 1;
 	public static final int ROT = 2;
 	public static final int HPTRANK = 16;
 	public static final int MANATRANK= 17;
 	public static final int HPTRANKSHOP = 21;
 	public static final int MANATRANKSHOP= 22;
 	public static final int BOSS1= 23;
 	public static final int BOSS2= 24;
 	public static final int BOSS3= 25;
 		
 	private static final int BREITE = 990;
 	private static final int HOEHE = 660;
 	
 	private Spieler[] player = new Spieler[2];
 	private Gegner[] gegner = new Gegner[20];
 	private Boss3 boss3;
 	
 	private int playerZahl;
 	private static int gegnerZahl=0;
 	
 	private static int level;
 	private static int raum;
 	
 	private int checkStartZiel;
 	
 	
 	private int[] gegnerAttack = new int[3];
 	private int[] playerAttack = new int[3];
 	
 	private static int aktiveCheckpoint=0;
 	private static int[] checkpointArray = new int[4];
 	
 	public static  boolean toCheckpoint;
 	
 
 	
 	
 	/**
 	 * Methode oeffnet das Menue
 	 * 
 	 */
 	
 	
 	public static void main(String[] args){
 		StdDraw.setCanvasSize(BREITE, HOEHE);
 		starten = new JButton("Spiel starten"); //neuer Button
 		starten.setBounds(260,0,160,40); //legt Groesse und Position fest
 		starten.addActionListener(StdDraw.frame); //damit was passiert, wenn man Buttons drueckt
 		starten.setFocusable(false);
 		StdDraw.frame.add(starten); //wird der Oberflaeche hinzugefuegt
 		
 		// Button "Beenden"
 		ende = new JButton("Beenden"); //folgendes analog zu starten-Button (mit geaenderten Koordinaten)
 		ende.setBounds(450,0,160,40);
 		ende.addActionListener(StdDraw.frame);
 		StdDraw.frame.add(ende);
 		
 		StdDraw.frame.addKeyListener(StdDraw.frame);
 		
 		
 	}
 	
 	public Interface(){
 	
 		}
 	
 	public static int getLevel(){
 		return level;
 	}
 	public static int getRaum(){
 		return raum;
 	}
 	
 	public void actionPerformed(ActionEvent event) {
 		
 		//wenn der Button 'starten' gedrueckt wird, soll sich Fenster mit Spielfeld oeffnen
 		if (event.getSource().equals(starten)){
 			StdDraw.clear();
 			Spielfeld.initSpielfeld();
 			spielGestartet=true;
 			raum=0;
 			level=0;
 			aktiveCheckpoint=0;
 			player[0]= new Spieler(0);
 			levelDarstellen(); // stellt das aktuelle/erste level dar
 			Aktion.reachedCheckpoint=false;
 			
 		}
 		
 		//wenn der Button 'schliessen' gedrueckt wird, soll sich das Menuefenster schliessen
 		if(event.getSource().equals(ende)){
 			System.exit(0);
 		}
 
 	/**
 	 * Methode kyeTyped: KeyEvent,
 	 * jedoch nicht genutzt
 	 * 	
 	 */
 	}
 	public void keyTyped(KeyEvent k){
 		
 	}
 
 	/**
 	 * Methode keyPressed: KeyEvent:
 	 * Tasten werden mit Bewegungsfunktionen innerhalb des Programms belegt
 	 * 
 	 */
 	public void keyPressed(KeyEvent k){
 		toCheckpoint=false;
 		if(spielGestartet == true){
 			StdDraw.show(0);
 				if (k.getKeyCode() == KeyEvent.VK_RIGHT){
 					checkStartZiel = player[0].bewegen(RECHTS);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_DOWN){
 					checkStartZiel = player[0].bewegen(UNTEN);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_LEFT){
 					checkStartZiel = player[0].bewegen(LINKS);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_UP){
 					checkStartZiel = player[0].bewegen(OBEN);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_SPACE){
 					player[0].schildZauber();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_W){
 					playerAttack(OBEN);
 					alleGegnerBewegen();
 					
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_A){
 					playerAttack(LINKS);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_S){
 					playerAttack(UNTEN);
 					alleGegnerBewegen();
 				}
 				else if (k.getKeyCode() == KeyEvent.VK_D){
 					playerAttack(RECHTS);
 					alleGegnerBewegen();
 				}
 				
 				
 				
 				if (checkStartZiel == ZIEL){
 					gegnerZahl=0;
 					nextRoom();
 					levelDarstellen();
 					player[0].moveTo(Spielfeld.getStartpunkt(level,raum)[0], Spielfeld.getStartpunkt(level,raum)[1]);
 				}
 				else if(checkStartZiel == START){
 					gegnerZahl=0;
 					previousRoom();
 					levelDarstellen();
 					player[0].moveTo(Spielfeld.getZielpunkt(level,raum)[0], Spielfeld.getZielpunkt(level,raum)[1]);
 				}
 				
 				if (toCheckpoint==true){
 					gegnerZahl=0;
 					levelDarstellen();
 					player[0].moveTo(checkpointArray[2], checkpointArray[3]);
 				}
 					
 				
 			StdDraw.show();
 		}
 	}
 	public void alleGegnerBewegen(){
 		for (counter=0;counter<gegnerZahl;counter++){
 			if (spielGestartet==true){
 				gegnerAktion(counter);
 			}
 		}
 	}
 	
 	
 	public void gegnerAktion(int id){
 		gegnerAttack=gegner[id].bewegen();
 		if (gegnerAttack[0]==1){
 			if (player[ getPlayerID(gegnerAttack[1],gegnerAttack[2]) ].schadenBekommen( gegner[id].getSchaden() ) ==true){
 				toCheckpoint=true;
 			}
 		}
 	}
 	public void playerAttack(int richtung){
 		playerAttack=player[0].playerAttack(richtung);
 		if (playerAttack[0]==MOB){
 			gegner[ getGegnerID(playerAttack[1],playerAttack[2]) ].schadenBekommen( player[0].getSchaden() );
 		}
 		else if (playerAttack[0]==BOSS3){
 			boss3.schadenBekommen( player[0].getSchaden() );
 			if (player[0].schadenBekommen(boss3.getSchaden())==true){
 				toCheckpoint=true;
 			}
 		}
 	}
 	
 	/**
 	 * Methode keyReleased : KeyEvent, 
 	 * jedoch nicht genutzt
 	 * 
 	 */
 	public void keyReleased(KeyEvent k){
 		
 	}
 
 	
 	
 	
 	
 /**
  * Darstellung des Spielfelds (Grafische Ausgabe)
  * 
  */
 	public void levelDarstellen() {
 		gegnerZahl=0;
 		//stellt StdDraw auf eine besser handhabbare skala um
 		StdDraw.setXscale(0.0,900);
 		StdDraw.setYscale(0,600);
 		StdDraw.show(0);
 		for (spalte=0;spalte<20;spalte++) {
 			for(reihe=0;reihe<13;reihe++) {
 				// stellt an allen orten das dem wert entsprechende bild dar
 				if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==BODEN
 						|(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==13) //13 und 14 sind Hilfselemente um an bestimmte Punkte zurueck zu kehren
 						|(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==14)){ //13: X steht in level.txt fuer die Stelle neben dem Ziel
 																					//14: Y steht in level.txt fuer die Stelle neben dem Checkpoint
 					StdDraw.picture(20+40*spalte,20+40*reihe, BODENIMG); 
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==MAUER){
 					StdDraw.picture(20+40*spalte,20+40*reihe, MAUERIMG);
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)== START){
 					StdDraw.picture(20+40*spalte,20+40*reihe, STARTIMG);
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==ZIEL){
 					StdDraw.picture(20+40*spalte,20+40*reihe, ZIELIMG);
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==FALLE){
 					StdDraw.picture(20+40*spalte,20+40*reihe, FALLEIMG);
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==MOB){
 					StdDraw.picture(20+40*spalte,20+40*reihe, MOBIMG);
 					gegner[gegnerZahl] = new Gegner(10, spalte, reihe, OBEN, 0, 2, gegnerZahl,MOB);
 					gegnerZahl++;
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==FIGUR){
 					//StdDraw.picture(20+40*spalte,20+40*reihe, SPIELFIGURIMG);
 					player[0].setXY(spalte,reihe);
 				}
 				else if (Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==SIEG){
 
 					StdDraw.picture(20+40*spalte,20+40*reihe, SIEGIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==CHECKPOINT){
 					StdDraw.picture(20+40*spalte,20+40*reihe, CHECKPOINTIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==FARBEBLAU){
 					StdDraw.picture(20+40*spalte,20+40*reihe, FARBEBLAUIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==FARBEGELB){
 					StdDraw.picture(20+40*spalte,20+40*reihe, FARBEGELBIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==FARBEROT){
 					StdDraw.picture(20+40*spalte,20+40*reihe, FARBEROTIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==STORYTELLER){
 					StdDraw.picture(20+40*spalte,20+40*reihe, STORYTELLERIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==MUENZEN){
 					StdDraw.picture(20+40*spalte,20+40*reihe, MUENZENIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==SHOP1){
 					StdDraw.picture(20+40*spalte,20+40*reihe, SHOP1IMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==SHOP2){
 					StdDraw.picture(20+40*spalte,20+40*reihe, SHOP2IMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==SHOP3){
 					StdDraw.picture(20+40*spalte,20+40*reihe, SHOP3IMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==HPTRANK){
 					StdDraw.picture(20+40*spalte,20+40*reihe, HPTRANKIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==MANATRANK){
 					StdDraw.picture(20+40*spalte,20+40*reihe, MANATRANKIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==HPTRANKSHOP){
 					StdDraw.picture(20+40*spalte,20+40*reihe, HPTRANKIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==MANATRANKSHOP){
 					StdDraw.picture(20+40*spalte,20+40*reihe, MANATRANKIMG);
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==BOSS1){
 					StdDraw.picture(20+40*spalte,20+40*reihe, BOSS1IMG);
 					gegner[gegnerZahl] = new Gegner(20, spalte, reihe, LINKS, 0, 4, gegnerZahl,BOSS1);
 					gegnerZahl++;
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==BOSS2){
 					StdDraw.picture(20+40*spalte,20+40*reihe, BOSS2IMG);
 					gegner[gegnerZahl] = new Gegner(25, spalte, reihe, RECHTS, 0, 4, gegnerZahl,BOSS2);
 					gegnerZahl++;
 				}
 				else if(Spielfeld.wertLesenBeiXY(level,raum,spalte,reihe)==BOSS3){
 					StdDraw.picture(20+40*spalte,20+40*reihe, BOSS3IMG);
 					boss3 = new Boss3(spalte,reihe);
 				}
 
 			}
 			
 		}
 				
 
 	
 		player[0].display();
 		StdDraw.show();
 	}
 	
 	
 	public static void nextRoom(){
 		if (raum<2){
 			raum++;
 		}
 		else {
 			level++;
 			raum=0;
 			aktiveCheckpoint=level*2;	
 		}
 	}
 	public static void previousRoom(){
 		if (raum>0){
 			raum--;
 		}
 	}
 
 	public static void nextCheckpoint(){
 		aktiveCheckpoint++;
 	}
 	public static void toCheckpoint(){
 		gegnerZahl=0;
 		checkpointArray=Spielfeld.getCheckpoint(aktiveCheckpoint);
 		level=checkpointArray[0];
 		raum=checkpointArray[1];
 	}
 	
 	
 	
 		/**
 		 * 
 		 * blendet 'Game Over' ein und setzt spielGestartet zurueck auf 0
 		 * 
 		 */
 		public static void gameOver(){
 			StdDraw.picture(400,300, GAMEOVERIMG);
 			spielGestartet=false;			
 		}
 		
 		
 		
 		/**
 		 * 
 		 * blendet 'Gewonnen' ein und setzt spielGestartet zurueck auf 0
 		 * 
 		 */
 		public static void sieg(){
 			StdDraw.picture(400,300, GEWONNENIMG);
 			spielGestartet=false;			
 		}
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		/**
 		 * Storyteller erzaehlt seine Geschichte
 		 * 
 		 */
 		public static void storyteller(){
 			
 			if ((level==0)&(raum==0)){ //Storyteller am Anfang (Level 1, Raum 1)
 				StdDraw.text(400, 560, "Frher war dies ein friedliches Pltzchen, aber dann kamen die Trolle und legten Fallen aus... ");
 				StdDraw.text(400, 540, "Und das nur, um ihr Essen besser wrzen zu knnen!");
 									
 			}
 			else if ((level==0)&(raum==1)){ //Storyteller vor dem Checkpoint (Level 1, Raum 2)
 				StdDraw.text(400, 570, "Da bist du ja schon! Pass auf, dort unten steht die beste Erfindung, seit es die fiesen Trolle hier runter geschafft haben.");
 				StdDraw.text(400, 550, "Diese rote Flagge da ist ein CHECKPOINT! Wenn du sie berhrst und im weiteren Verlauf deines Abenteuers stirbst,");
 				StdDraw.text(400, 530, "wirst du hier wiederbelebt werden. Und, habe ich zu viel versprochen? Die Idee ist ja wohl genial!");
 				
 			}
 			else if ((level==1)&(raum==0)){ //Storyteller vor dem Checkpoint (Level 2, Raum 1)
 				StdDraw.text(400, 560, "Das hier ist der einzige Shop hier unten. Decke dich gut ein, denn du wirst nicht so schnell wieder die Mglichkeit finden,");
 				StdDraw.text(400, 540, "deine Gesundheit und dein Mana zu regenerieren! Jeder Trank kostet genau eine Mnze!");
 				
 			}
 			else if ((level==2)&(raum==2)){ //Storyteller im Endraum (Level 3, Raum 3)
 				StdDraw.text(400, 560, "Fast hast du es geschafft!!! Berhre diese Flagge dort und du kannst beruhigt nach Hause zurckkehren");
 				StdDraw.text(400, 540, "und dich als Sieger feiern lassen. Aber lass dich nicht auf den letzten Metern von den Trollen erwischen!");
 				
 			}		
 			
 		
 		}
 		public void gegnerSchlag(int gegnerID, int zielX, int zielY){
 			player[getPlayerID(zielX,zielY)].schadenBekommen(gegner[gegnerID].getSchaden());
 		}
 		public void playerSchlag(int playerID, int zielX, int zielY){
 			gegner[getGegnerID(zielX,zielY)].schadenBekommen(player[playerID].getSchaden());
 		}
 		
 		public int getPlayerID(int x,int y){
 			for (counter2=0;counter2<playerZahl; counter2++ ){
 				if ((player[counter2].getX() == x)
					|(player[counter2].getY() == y)){
 					returner = counter2;
 				}
 			}
 			return returner;
 		}
 		public int getGegnerID(int x,int y){
 			for (counter3=0; counter3<gegnerZahl; counter3++ ){
 				if ((gegner[counter3].getX() == x)
					|(gegner[counter3].getY() == y)){
 					returner= counter3;
 				}
 			}
 			return returner;
 		}
 		public void displayGegner(int x, int y, int richtung){
 			StdDraw.picture(20+40*x,20+40*y, MOBIMG,((-richtung)*90));
 	}
 }
