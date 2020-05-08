 package spielfigur;
 import game.Game;
 import game.Tutorial;
 
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import spielfeld.Spielfeld;
 import spielfeld.Spielflaeche;
 import bombe.BombType;
 import bombe.Bombe;
 import bombe.NormalBomb;
 
 public class Spielfigur {
 
 	// Initialisierung von Variabeln
 
 	public int xPosition; // aktuelle Position auf der "x-Achse" der
 							// Spielfläche
 	public int yPosition; // aktuelle Position auf der "y-Achse" der
 							// Spielfläche
 	public int dimension;
 	protected int width;
 	protected int height;
 	protected String pic;
 	private int bombPlanted = 3;
 	private int playerNumber;
 	private int newx;
 	private int newy;
 	private int radius = 3;
 	boolean wechsler = true;
 	boolean zfbombe = true;
 	final Lock lock = new ReentrantLock();
 
 	private BombType bomb = new NormalBomb();
 	/**
 	 * erzeugt eine Spielfigur Bomberman an die angegebene Stelle
 	 * 
 	 * @param xPosition
 	 *            definiert X-Position bei Start der Figur
 	 * @param yPosition
 	 *            definiert Y-Position bei Start der Figur
 	 * @param dimension
 	 *            definiert Dimension bei Start
 	 * @param player
 	 *            Gibt an welcher Spieler das ist.
 	 */
 	public Spielfigur(int xPosition, int yPosition, int dimension, int player) {
 		this.yPosition = yPosition;
 		this.xPosition = xPosition;
 		this.dimension = dimension;
 		this.playerNumber = player;
 	}
 
 	// get und set methoden
 
 	public int getPlayerNumber() {
 		return playerNumber;
 	}
 
 	public void setPlayerNumber(int playerNumber) {
 		this.playerNumber = playerNumber;
 	}
 
 	public int getxPosition() {
 		return xPosition;
 	}
 
 	public void setxPosition(int xPosition) {
 		this.xPosition = xPosition;
 	}
 
 	public int getyPosition() {
 		return yPosition;
 	}
 
 	public synchronized int getBombPlanted() {
 
 		return bombPlanted;
 
 	}
 
 	public void setyPosition(int yPosition) {
 		this.yPosition = yPosition;
 	}
 
 	public BombType getBombType() {
 		return bomb;
 	}
 
 	public void setBombType(BombType bomb) {
 		lock.lock();
 		this.bomb = bomb;
 		lock.unlock();
 	}
 
 	public synchronized void setBombPlanted(int b) {
 		bombPlanted = b;
 	}
 
 	/**
 	 * ueberprueft ob Bomberman an angegebener x/y Stelle ist.
 	 */
 	public boolean istPos(int x, int y) {
 		if ((x == xPosition && y == yPosition))
 			return true;
 		else
 			return false;
 	}
 	// Bombe
 	/**
 	 * Methode mit der Bomberman schließlich Bomben legen kann. An der Position
 	 * des Spielers wird ein Objekt des Typs Bombe erzeugt.
 	 */
 	public void bombeLegen() {
 		if (bombPlanted > 0) {
 			new Bombe(xPosition, yPosition, width, height, bomb, playerNumber,
 					radius).start();
 			Spielflaeche.play.fill(xPosition, yPosition, 4, Spielfeld.Bombe);
 			bombPlanted -= 1;
 
 		}
 	}
 	/**
 	 * Die Methode move bzw. move2 regeln die Bewegungen der Figuren "man" und
 	 * "man2. Benutzt werden diese Methoden vom gamekeylistener. Die Bewegung
 	 * wird durch Abfragen , der in der jeweiligen Bewegungsrichtung vorhandenen
 	 * Objekte realisiert. Es wird abgefragt, was sich auf dem jeweiligen Feld
 	 * befindet und falls Bewegung logisch erscheint, wird die jeweilige Figur
 	 * auf dieses Feld bewegt. (Änderung der Variablen xPosition und yPosition
 	 * des jeweiligen Spielfigurenobjektes.
 	 */
 	public void move(int x, int y) {
 
 		/*
 		 * Das Spiel wird neu gestartet wenn Bomberman und Ausgang sich auf den
 		 * selben Koordinaten befinden.
 		 */
 
 		if (Spielflaeche.play.getObj(xPosition + x, yPosition + y, 1) == Spielfeld.Ausgang
 				&& Spielflaeche.play.getObj(xPosition + x, yPosition + y, 2) != Spielfeld.Kiste
 				&& Tutorial.tutorialMode == false) {
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 			Game.restartGame(1);
 
 		}
 		if (Spielflaeche.play.getObj(xPosition + x, yPosition + y, 1) == Spielfeld.Ausgang
 				&& Spielflaeche.play.getObj(xPosition + x, yPosition + y, 2) != Spielfeld.Kiste) {
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 			Tutorial.restartTut();
 
 		}
 		/*
 		 * wenn das angepeilte Feld eine Explosion ist, dann wird die Figur
 		 * dorthin bewegt. Allerdings stirbt diese dann --> Spieler2 hat
 		 * gewonnen --> Spiel startet neu
 		 */
 
 		else if (Spielflaeche.play.equalsExplosion(xPosition + x,
 				yPosition + y, 3)
 				|| (Spielflaeche.play.equalsExplosion(xPosition, yPosition, 3))) {
 			Spielflaeche.play.fill(xPosition, yPosition, 3, Spielfeld.Gras);
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 			if (Tutorial.tutorialMode == false) {
 				Game.restartGame(2);
 			}
 			// Label soll erstellt werden // Tot - wanna restart?
 
 		}
 
 		/*
 		 * Es wird festgestellt, ob das angepeilte Feld bereits mit einem
 		 * Objekt, das unpassierbarist, belegt ist. Wenn es passierbar ist geht
 		 * die Abfrage weiter.
 		 */
 		else if (Spielflaeche.play.getObj(xPosition + x, yPosition + y, 2) == null
 				&& Spielflaeche.play.getObj(xPosition + x, yPosition + y, 4) == null
 				&& Spielflaeche.play.equalsMauer(xPosition + x, yPosition + y) == false) {
 			/*
 			 * Es wird abgefragt ob eine Bombe gelegt wurde. Wenn eine Bombe
 			 * gelegt wurde, dann wird auf den Variablen xPostítion und
 			 * yPosition des Spielfigurenobjektes eine Bombe gezeichnet.
 			 */
 			Spielflaeche.play.fill(xPosition, yPosition, 3, Spielfeld.Gras);
 			/*
 			 * if (Spielflaeche.bman.bombeLiegt) { Spielflaeche.play
 			 * .fill(xPosition, yPosition, 4, Spielfeld.Bombe); bombeLiegt =
 			 * false; // Noch eine Bedingung ( Wenn Explo --> Bman // auf explo
 			 * // sieht verkohlt aus
 			 * 
 			 * }
 			 */
 
 			/*
 			 * Bewegung der Figur
 			 */
 
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 
 			if (Spielflaeche.play.getObj(Spielflaeche.bman.xPosition,
 					Spielflaeche.bman.yPosition, 1) == Spielfeld.Wechsler
 					&& wechsler == true) {
 				Spielflaeche.play.fill(Spielflaeche.bman.xPosition,
 						Spielflaeche.bman.yPosition, 1, null);
 				newx = Spielflaeche.bman.xPosition;
 				newy = Spielflaeche.bman.yPosition;
 				Spielflaeche.bman.xPosition = Spielflaeche.bman2.xPosition;
 				Spielflaeche.bman.yPosition = Spielflaeche.bman2.yPosition;
 				Spielflaeche.bman2.xPosition = newx;
 				Spielflaeche.bman2.yPosition = newy;
 
 				/**
 				 * Hier kommen die Item Effekte rein, muss auchnoch in move2
 				 * ergänzt werden
 				 * 
 				 * 
 				 */
 			} else if (Spielflaeche.play.getObj(Spielflaeche.bman.xPosition,
 					Spielflaeche.bman.yPosition, 1) == Spielfeld.DummyItem
 					&& zfbombe == true) {
 				Spielflaeche.play.fill(Spielflaeche.bman.xPosition,
 						Spielflaeche.bman.yPosition, 1, null);
 
 				int zahl = (int) (Math.random() * 20) + 1;
 				int zahl2 = (int) (Math.random() * 20) + 1;
 				Spielflaeche.play.fill(Spielflaeche.bman.xPosition,
 						Spielflaeche.bman.yPosition, 1, null);
 				if (Spielflaeche.play.getObj(zahl, zahl2, 2) == null) {
 					new Bombe(zahl, zahl2, width, height, bomb, 3, 3).start();
 					Spielflaeche.play.fill(zahl, zahl2, 4, Spielfeld.Bombe);
 				}
 
 				// Spielflaeche.bman.radius++;
 			}
 
 		}
 
 	}// move
 
 	/**
 	 * 
 	 * Diese Methode funktioniert analog zu move ausser ,dass hierbei nicht das
 	 * Objekt"man" angesprochen wird sondern "man2"
 	 */
 	public void move2(int x, int y) {
 
 		if (Spielflaeche.play.getObj(xPosition + x, yPosition + y, 1) == Spielfeld.Ausgang
 				&& Spielflaeche.play.getObj(xPosition + x, yPosition + y, 2) != Spielfeld.Kiste) {
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 			Game.restartGame(2);
 
 		}
 		/*
 		 * Checkt ob Spieler 2 in eine Explo rennt
 		 */
 
 		else if (Spielflaeche.play.equalsExplosion(xPosition + x,
 				yPosition + y, 3)) {
 			Spielflaeche.play.fill(xPosition, yPosition, 3, Spielfeld.Gras);
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 			System.out.println("Player1 siegt");
 			Game.restartGame(1);
 		}
 
 		// Label soll erstellt werden // Tot - wanna restart?
 
 		/*
 		 * 
 		 * Checkt ob das Feld auf das Player 2 rennen soll leer ist
 		 */
 		else if (Spielflaeche.play.getObj(xPosition + x, yPosition + y, 2) == null
 				&& Spielflaeche.play.getObj(xPosition + x, yPosition + y, 4) == null
 				&& Spielflaeche.play.equalsMauer(xPosition + x, yPosition + y) == false) {
 
 			Spielflaeche.play.fill(xPosition, yPosition, 3, Spielfeld.Gras);
 
 			// Spielflaeche.play
 			// .fill(xPosition, yPosition, 4, Spielfeld.Bombe);
 
 			xPosition = xPosition + x;
 			yPosition = yPosition + y;
 
 			if (Spielflaeche.play.getObj(Spielflaeche.bman2.xPosition,
 					Spielflaeche.bman2.yPosition, 1) == Spielfeld.Wechsler
 					&& wechsler == true) {
 				Spielflaeche.play.fill(Spielflaeche.bman2.xPosition,
 						Spielflaeche.bman2.yPosition, 1, null);

				newx = Spielflaeche.bman.xPosition;
 				newy = Spielflaeche.bman.yPosition;
 				Spielflaeche.bman.xPosition = Spielflaeche.bman2.xPosition;
 				Spielflaeche.bman.yPosition = Spielflaeche.bman2.yPosition;
 				Spielflaeche.bman2.xPosition = newx;
 				Spielflaeche.bman2.yPosition = newy;

 				int zahl = (int) Math.random() * 20;
 				System.out.println(zahl);
 
 				/**
 				 * Hier kommen die Item Effekte rein, muss auchnoch in move2
 				 * ergänzt werden
 				 * 
 				 * 
 				 */
 			} else if (Spielflaeche.play.getObj(Spielflaeche.bman2.xPosition,
 					Spielflaeche.bman2.yPosition, 1) == Spielfeld.DummyItem
 					&& zfbombe == true) {
 
 				int zahl = (int) (Math.random() * 20) + 1;
 				int zahl2 = (int) (Math.random() * 20) + 1;
 				Spielflaeche.play.fill(Spielflaeche.bman2.xPosition,
 						Spielflaeche.bman2.yPosition, 1, null);
 				if (Spielflaeche.play.getObj(zahl, zahl2, 2) == null) {
 
 					new Bombe(zahl, zahl2, width, height, bomb, 3, 3).start();
 					Spielflaeche.play.fill(zahl, zahl2, 4, Spielfeld.Bombe);
 				}
 
 				// Spielflaeche.bman2.radius++;
 
 			}
 
 		}
 	}// move2
 
 	public int getRadius() {
 		return radius;
 	}
 
 	public void setRadius(int radius) {
 		this.radius = radius;
 	}
 
 	public boolean isWechsler() {
 		return wechsler;
 	}
 
 	public void setWechsler(boolean wechsler) {
 		this.wechsler = wechsler;
 	}
 
 	public boolean isZfbombe() {
 		return zfbombe;
 	}
 
 	public void setZfbombe(boolean zfbombe) {
 		this.zfbombe = zfbombe;
 
 	}
 
 }
