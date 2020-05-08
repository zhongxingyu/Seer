 package de.game.bomberman;
 
 import java.util.*;
 import org.newdawn.slick.*;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 /**
  * ## Das ist die Schnittstelle aller Klassen. Die Klasse RandomMap ruft hier von allen Klassen Konstruktoren und Methoden auf.
  * Hier wird das Spiel "zusammengesetzt". Sowohl die Spieler, die Bomben und die Mauern, als auch die Explosionen werden hier erstellt.
  * Hinzukommt noch Exit und Ende. Anders als bei der StaticMap wird hier bei jedem Start eine zufaellig erstellte Map generiert.
  * 
  * Unser Spielname wird hier geschrieben. Grafiken werden gezeichnet, sodass die Karte in einem Fenster mit den Spielern, den Mauern,
  * den Explosionen, des Ausgangs "Exit" gezeichnet wird. Das Menue wird auch hier erstellt. Auch die Musik wird hier geladen.
  * # Hier findet die Abfrage ab, wenn keine Spieler mehr vorhanden sind --> SpielEnde. Die Explosionsgroesse und zeit wird hier gesetzt.
  * Hier wird staendig Abgefragt, wo der Spieler sich nach Tastatureingaben befindet. 
  * # Die Kettenreaktion und Zerstoerung durch die Explosion wird hier abgefragt. Zudem ist diese Klasse fuer den Neustart verantwortlich.
  */
 public class RandomMap extends BasicGameState {
   
   // Karte
   protected TiledMap karte;
   // SpielObjekt: Spieler
   protected ArrayList<SpielObjekt> player = new ArrayList<SpielObjekt>();
   // SpielObjekt: Bombe
   protected ArrayList<SpielObjekt> bomben = new ArrayList<SpielObjekt>();
   // SpielObjekt: Mauer
   protected ArrayList<SpielObjekt> Mauer = new ArrayList<SpielObjekt>();
   // SpielObjekt: Explosion
   protected ArrayList<SpielObjekt> explosion = new ArrayList<SpielObjekt>();
   // Variablen: Exit und Ende
   protected Exit exit;
   protected SpielEnde ende;
   
   int stateID = 3;
   
   protected boolean debug = false;
   
 //KONSTRUKTOR:
   
  public RandomMap(int stateID) {
    this.stateID = stateID;
  }
  
  public int getID() {
      return stateID;
  }
   
   // RENDER BLOCK: Grafiken werden gezeichnet
   
   @Override
   /*
    * @see org.newdawn.slick.Game#render(org.newdawn.slick.GameContainer,
    * org.newdawn.slick.Graphics)
    */
   public void render(GameContainer container, StateBasedGame sb, Graphics g) throws SlickException {
     
     
     // Hoehe und Breite der Karte
     container.setVSync(true);
     float scaleX = (float) container.getWidth()
         / (float) (karte.getWidth() * karte.getTileWidth());
     float scaleY = (float) container.getHeight()
         / (float) (karte.getHeight() * karte.getTileHeight());
     g.scale(scaleX, scaleY); // bekommt die Werte
     karte.render(0, 0);
 
     // Bomben werden gezeichnet
     for (SpielObjekt bomb : bomben) {
       bomb.draw(g);
     }
     // Exit wird gezeichnet
     exit.draw(g);
     // Spieler wird gezeichnet
     for (SpielObjekt pl : player) {
       pl.draw(g);
     }
     // Explosion wird gezeichnet
     for (SpielObjekt exp : explosion) {
       exp.draw(g);
     }
     // Wand  wird gezeichnet
     for (SpielObjekt bl : Mauer) {
       bl.draw(g);
     }
     // Menue, Ende wird gezeichnet
       ende.draw(g);
   }
   
   // INIT BLOCK: Initialisierung der Daten
   
   @Override
   /*
    * @see org.newdawn.slick.BasicGame#init(org.newdawn.slick.GameContainer)
    */
   public void init(GameContainer container, StateBasedGame sb) throws SlickException {
     
     // Hier wird die Musik
     // geladen...
     Music music = new Music("res/Music/test.ogg");
     // ... und im Loop abgespielt
     music.loop();
     
     // Initialisierung der Karte
     initMap("res/testmap2.tmx");
     // Spieler 1
     player.add(0, new Player(32, 32, 1));
     // Spieler 2
     player.add(1, new Player(544, 32, 2));
     ((Player) player.get(0)).setKeys(Input.KEY_LEFT, Input.KEY_RIGHT,
         Input.KEY_UP, Input.KEY_DOWN, Input.KEY_SPACE);
     ((Player) player.get(1)).setKeys(Input.KEY_A, Input.KEY_D, Input.KEY_W,
         Input.KEY_S, Input.KEY_LCONTROL);
     // Exit wird erstellt und positioniert bei (x, y)
     exit = new Exit(544, 416);
     // Ende
     ende = new SpielEnde(container.getHeight(), container.getWidth());
   }
   
   // UPDATE BLOCK: Daten werden hier nachgeguckt und stetig geupdated
   
   @Override
   /*
    * @see org.newdawn.slick.BasicGame#update(org.newdawn.slick.GameContainer,
    * int)
    */
   public void update(GameContainer container, StateBasedGame sb, int arg1) throws SlickException {
     
     
     // falls keine Spieler mehr vorhanden sind: Spielende
     if (player.isEmpty()) {
       ende.setGameOver(true);
     }
     // Abfrage: weiterspielen oder beenden
     if (ende.isGameOver()) {
       // beenden
       if (container.getInput().isKeyPressed(Input.KEY_N)) {
         sb.enterState(0);  
       }
       // weiterspielen
       if (container.getInput().isKeyPressed(Input.KEY_Y)) {
         restartGame(container,sb);
       }
 
     } else {
       for (int i = 0; i < bomben.size(); i++) {
         Bombe bomb = (Bombe) bomben.get(i);
         bomb.update(arg1); // Bomben-Update
         // Kettenreaktion der Bombe + Entfernung der Bombe nach Explosion
         if (bomb.isExplode()) {
           buildExplodeArray(bomb);
           bomben.remove(bomb);
         }
       }
       // Groee der Explosion + Update 
       for (int i = 0; i < explosion.size(); i++) {
         Explosion expl = (Explosion) explosion.get(i);
         expl.update(arg1);
         // Explosion trifft Spieler + entferne Spieler nach Explosion
         ArrayList<SpielObjekt> deadPl = expl.pruefeKollsion(player);
         for (SpielObjekt dPL : deadPl) {
           player.remove(dPL);
         }
         if (expl.getCounter() <= 0) {
           explosion.remove(i);
         }
       }
       //Update der Explosion
       for (SpielObjekt expl : explosion) {
         expl.update(arg1);
       }
       // Update des Spielers
       for (int i = 0; i < player.size(); i++) {
         Player pl = (Player) player.get(i);
         pl.update(arg1, Mauer);
         
         // Steuerung des Spielers
         // Eingabe der Steuerung: Links gehen
         if (container.getInput().isKeyDown(pl.getLeft())) {
           pl.setXtendency(false);
           if ((pl.getX() % 32) == 0) {
             pl.move(-1, 0, Mauer);
           }
         }
         
         // Eingabe der Steuerung: Rechts gehen
         if (container.getInput().isKeyDown(pl.getRight())) {
           pl.setXtendency(true);
           if ((pl.getX() % 32) == 0) {
             pl.move(+1, 0, Mauer);
           }
         }
         // Eingabe der Steuerung: nach oben gehen
         if (container.getInput().isKeyDown(pl.getUp())) {
           pl.setYtendency(false);
           if ((pl.getY() % 32) == 0) {
             pl.move(0, -1, Mauer);
           }
         }
         // Eingabe der Steuerung: nach unten gehen
         if (container.getInput().isKeyDown(pl.getDown())) {
           pl.setYtendency(true);
           if ((pl.getY() % 32) == 0) {
             pl.move(0, +1, Mauer);
           }
         }
         
         // Eingabe der Steuerung: Bombe legen
         if (container.getInput().isKeyPressed(pl.getBomb())) {
           float BombX;
           float BombY;
           // Koordinaten runden der Bombe
           BombX = (float) (Math.round(pl.getX() / 32.) * 32.);
           BombY = (float) (Math.round(pl.getY() / 32.) * 32.);
           Bombe tmpBomb = new Bombe((int) BombX, (int) BombY);
           if (tmpBomb.pruefeKollsion(bomben).isEmpty()) {
             bomben.add(tmpBomb);
             // Sound der Bombe laden
             Sound fx = new Sound("res/sfx/sfxtest.wav");
             fx.play();
           }
         }
         // Ende des Spiels durch: Esc druecken
         if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)
             // ..oder durch Spieler auf Exit-Feld
             || exit.pruefeKollsion(pl)) {
           ende.setGameOver(true);
         }
       }
     }
   }
   
   /**
    * @param spObj Spielobjekt: baut die Explosion zu einem SpielObjekt Bombe
    * ## Diese Explosion ist dann spaeter im Spiel die Moeglichkeit die zerstoerbaren Bloecke und den Gegner auszuschaltern
    * und zu entfernen.
    * @throws SlickException
    */
   private void buildExplodeArray(SpielObjekt spObj) throws SlickException {
     Bombe bomb = (Bombe) spObj;
     Explosion expll, explr;
     ArrayList<SpielObjekt> kollision = new ArrayList<SpielObjekt>();
     boolean xr = true;
     boolean xl = true;
     boolean yr = true;
     boolean yl = true;
     
     explosion.add(new Explosion(bomb.getX(), bomb.getY()));
     // Pruefe Mauer Kollision
     for (int x = 0; x <= bomb.getExplodeRadius(); x++) {
       for (int y = 0; y <= bomb.getExplodeRadius(); y++) {
         if ((-x < 0 && y == 0 && xl == true)
             || (x == 0 && -y < 0 && yl == true)) {
           expll = new Explosion(bomb.getX() - x * 32, bomb.getY() - y * 32);
           
           // Entfernt das Objekt nach der Explosion
           kollision.clear();
           kollision = expll.pruefeKollsion(Mauer);
           kollision.addAll(expll.pruefeKollsion(bomben));
           if (kollision.isEmpty()) {
             explosion.add(expll);
           } else {
             for (SpielObjekt koll : kollision) {
               if (koll instanceof Block) {
                 if (((Block) koll).isZerstoerbar()) {
                   explosion.add(expll);
                   // entfernt die Mauer nach Kollision
                   Mauer.remove(koll);
                 }
               } else if (koll instanceof Bombe) {
                 ((Bombe) koll).setExplode(true);
               }
               
               if (-x < 0 && y == 0)
                 xl = false;
               if (-y < 0 && x == 0)
                 yl = false;
               
             }
           }
         }
         // Explosionskollision mit Bombe und Mauer: Kettenreaktion + Zerstoerung
         if ((x > 0 && y == 0 && xr == true) || (x == 0 && y > 0 && yr == true)) {
           explr = new Explosion(bomb.getX() + x * 32, bomb.getY() + y * 32);
           kollision.clear();
           kollision = explr.pruefeKollsion(Mauer);
           kollision.addAll(explr.pruefeKollsion(bomben));
           if (kollision.isEmpty()) {
             explosion.add(explr);
           } else {
             for (SpielObjekt koll : kollision) {
               if (koll instanceof Block) {
                 if (((Block) koll).isZerstoerbar()) {
                   explosion.add(explr);
                   Mauer.remove(koll);
                 }
               } else if (koll instanceof Bombe) {
                 ((Bombe) koll).setExplode(true);
               }
               
               if (x > 0 && y == 0)
                 xr = false;
               if (y > 0 && x == 0)
                 yr = false;
               
             }
           }
           
         }
       }
     }    
   }
   
   /**
    * @param container
    * ## In dieser Methode wird die Moeglichkeit geschrieben das Spiel
    * von neu zu starten. Dabei wird alles kurzzeitig auf null gesetzt, dh geloescht und wieder
    * in der anderen Methode neu gerendert.
    * @throws SlickException
    */
   private void restartGame(GameContainer container, StateBasedGame sb) throws SlickException {
     player.clear();
     bomben.clear();
     Mauer.clear();
     ende = null;
     exit = null;
     karte = null;
     init(container,sb);
   }
   
   /**
    * @param ref Referenz 
    * @throws SlickException
    */
   public void initMap(String ref) throws SlickException {
     
     karte = new TiledMap(ref, "res");
     int wallcounter = 0;
    for (int x = 0; x < karte.getWidth(); x++) {
      for (int y = 0; y < karte.getHeight(); y++) {
         final int tileID = karte.getTileId(x, y, 0);
         switch (tileID) {
           case 2:
             double R = Math.random();
             if(R<0.5 && wallcounter<30){ // hier z.B. ist 15 = Maximale Anzahl an Zerstoerbaren Mauern. Math.random macht die Zufaelligkeit
             Mauer.add(new Block(x * 32, y * 32, true));
             wallcounter++;}
             break;
           case 17:
             Mauer.add(new Block(x * 32, y * 32, false));
             break;
           default:
             break;
         }
       }
     }
   }
 }
