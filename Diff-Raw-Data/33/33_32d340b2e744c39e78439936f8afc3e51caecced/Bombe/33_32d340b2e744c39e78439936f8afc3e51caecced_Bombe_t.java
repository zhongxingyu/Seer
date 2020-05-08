 package bomberman;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 /**
  * Erzeugt die Bombe. Spieler 1 legt diese mit dem Buchstaben "F", Spieler 2 mit der Taste "K".<br>
  * <br>
  * Beinhaltet das Bomben legen, die Grafiken f&uuml;r Bombe & Explosion, s&auml;mtliche Timer, sowie<br>
  * die Vorraussetzungen f&uuml;r das explodieren der Bombe.
  */
 public class Bombe extends AbstractAction {
 	private static final long serialVersionUID = -7038533653091561580L;
 	private BomberMan player1;
 	private BomberMan player2;
 	private JLayeredPane s;
 	private JLabel bomb = new JLabel(new ImageIcon("src/gfx/bomb/bomb.gif"));
 	private JLabel boom1 = new JLabel(new ImageIcon("src/gfx/bomb/explode.gif"));
 	private JLabel boom2 = new JLabel(new ImageIcon("src/gfx/bomb/explode.gif"));
 	private JLabel boom3 = new JLabel(new ImageIcon("src/gfx/bomb/explode.gif"));
 	private JLabel boom4 = new JLabel(new ImageIcon("src/gfx/bomb/explode.gif"));
 	private JLabel boom5 = new JLabel(new ImageIcon("src/gfx/bomb/explode.gif"));
 	private int x, y, width = 40, height = 40;
 	private Spielfeld spielfeld;
 	private JLabel walls[][];
 	private Timer LayTimer = new Timer(1650, new LayBomb());
 	private Timer ExploTimer = new Timer(2425, new ExploBomb());
 	private Timer LayTimer2 = new Timer(0, new LayBomb());
	private Timer ExploTimer2 = new Timer(775, new ExploBomb());
 	private boolean kette;
 	private boolean bombIsLayed;
 
 	public Bombe(BomberMan player1, BomberMan player2, JLayeredPane s,
 			Spielfeld spielfeld) {
 		this.player1 = player1;
 		this.player2 = player2;
 		this.s = s;
 		this.spielfeld = spielfeld;
 		this.walls = spielfeld.getWalls();
 	}
 
 	/**
 	 * @return width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @return height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * @return x
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * @return y
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * @return bomb
 	 */
 	public JLabel getBomb() {
 		return bomb;
 	}
 
 	/**
 	 * &Uuml;berpr&uuml;ft, ob sich die Spieler bewegen und wo diese sich befinden.<br>
 	 * Die Bombe wird bei Aktivierung an die Position des jeweiligen Spieler gesetzt.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
		if (!LayTimer.isRunning() && !ExploTimer.isRunning()) {
 			x = player1.getX();
 			y = player1.getY();
 			bomb.setBounds(x, y, width, height);
 			s.add(bomb, 2);
 			bombIsLayed = true;
 
 			LayTimer.start();
 		}
 		ExploTimer.start();
 	}
 
 	// Anfang der Explosion
 	class LayBomb implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 
 			s.remove(bomb);
 			bombIsLayed = false;
 			s.repaint();
 			Explosion();
 			LayTimer.stop();
 			LayTimer2.stop();
 
 		}
 	}
 
 	// Ende der Explosion
 	class ExploBomb implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			s.remove(boom1);
 			s.remove(boom2);
 			s.remove(boom3);
 			s.remove(boom4);
 			s.remove(boom5);
 			s.repaint();
 			ExploTimer.stop();
 			ExploTimer2.stop();
 		}
 	}
 
 	/**
 	 * Die Methode &uuml;berpr&uuml;ft, ob sich mehrere Bomben in einer Reihe befinden.<br>
 	 * ist dies der Fall, wird Kettenreaktion ausgel%ouml;. Ist dies nicht der Fall, greift ExploTimer2<br>
 	 * und die Bombe detoniert ganz normal.
 	 */
 	public void Kettenreaktion() {
 		kette = true;
 		LayTimer.stop();
 		ExploTimer.stop();
 
 		LayTimer2.start();
 		ExploTimer2.start();
 		kette = false;
 	}
 
 	/**
 	 * @return bombIsLayed
 	 */
 	public boolean bombActive() {
 		return bombIsLayed;
 	}
 
 	/**
 	 * Explosion der Bombe.<br>
 	 * Ein Feld um die Bombe ist als Radius gesetzt. befindet sich in diesem Radius ein zerst&ouml;rbarer Stein,<br>
 	 * so wird dieser entfernt und die Animation der Explosion angezeigt. Das selbe gilt f&uuml;r Spieler. Bei<br>
 	 * Explosion in einem Spieler erscheint eine Meldung mit "Spieler x hat das Spiel gewonnen".
 	 */
 	public void Explosion() {
 		boom1.setBounds(x, y, width, height);
 		s.add(boom1, 0);
 
 		// Pruefe ein Feld weiter rechts
 		if (walls[(x / 40) + 1][(y / 40)].getName().equals("walkable")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 		} else if (walls[(x / 40) + 1][(y / 40)].getName().equals("destroyable")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 			walls[(x / 40) + 1][(y / 40)].setIcon(null);
 			walls[(x / 40) + 1][(y / 40)].setName("walkable");
 		} else if (walls[(x / 40) + 1][(y / 40)].getName().equals("hidden")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 			walls[(x / 40) + 1][(y / 40)].setIcon(new ImageIcon("src/gfx/door/door.png"));
 			walls[(x / 40) + 1][(y / 40)].setName("exit");
 		}
 
 		// Pruefe Felder auf walls
 		// Pruefe ein Feld weiter links
 		if (walls[(x / 40) - 1][(y / 40)].getName().equals("walkable")) {
 			boom3.setBounds(x - 40, y, width, height);
 			s.add(boom3, 1);
 		} else if (walls[(x / 40) - 1][(y / 40)].getName().equals("destroyable")) {
 			boom3.setBounds(x - 40, y, width, height);
 			s.add(boom3, 1);
 			walls[(x / 40) - 1][(y / 40)].setIcon(null);
 			walls[(x / 40) - 1][(y / 40)].setName("walkable");
 		} else if (walls[(x / 40) - 1][(y / 40)].getName().equals("hidden")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 			walls[(x / 40) + 1][(y / 40)].setIcon(new ImageIcon("src/gfx/door/door.png"));
 			walls[(x / 40) + 1][(y / 40)].setName("exit");
 		}
 
 		// Pruefe ein Feld weiter oben
 		if (walls[(x / 40)][(y / 40) + 1].getName().equals("walkable")) {
 			boom4.setBounds(x, y + 40, width, height);
 			s.add(boom4, 1);
 		} else if (walls[(x / 40)][(y / 40) + 1].getName().equals("destroyable")) {
 			boom4.setBounds(x, y + 40, width, height);
 			s.add(boom4, 1);
 			walls[x / 40][(y / 40) + 1].setIcon(null);
 			walls[x / 40][(y / 40) + 1].setName("walkable");
 		} else if (walls[(x / 40)][(y / 40) + 1].getName().equals("hidden")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 			walls[(x / 40) + 1][(y / 40)].setIcon(new ImageIcon("src/gfx/door/door.png"));
 			walls[(x / 40) + 1][(y / 40)].setName("exit");
 		}
 
 		// Pruefe ein Feld weiter unten
 		if (walls[(x / 40)][(y / 40) - 1].getName().equals("walkable")) {
 			boom5.setBounds(x, y - 40, width, height);
 			s.add(boom5, 1);
 		} else if (walls[(x / 40)][(y / 40) - 1].getName().equals("destroyable")) {
 			boom5.setBounds(x, y - 40, width, height);
 			s.add(boom5, 1);
 			walls[x / 40][(y / 40) - 1].setIcon(null);
 			walls[x / 40][(y / 40) - 1].setName("walkable");
 		} else if (walls[(x / 40)][(y / 40) - 1].getName().equals("hidden")) {
 			boom2.setBounds(x + 40, y, width, height);
 			s.add(boom2, 1);
 			walls[(x / 40) + 1][(y / 40)].setIcon(new ImageIcon("src/gfx/door/door.png"));
 			walls[(x / 40) + 1][(y / 40)].setName("exit");
 		}
 
 		// Pruefe Felder auf Spieler
 		// Aktuelles Feld
 		if (x == player1.getX() && y == player1.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player2.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		} else if (x == player2.getX() && y == player2.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player1.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		}
 
 		// Feld weiter rechts
 		if (x + 40 == player1.getX() && y == player1.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player2.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		} else if (x + 40 == player2.getX() && y == player2.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player1.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		}
 
 		// Feld weiter links
 		if (x - 40 == player1.getX() && y == player1.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player2.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		} else if (x - 40 == player2.getX() && y == player2.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player1.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		}
 
 		// Feld weiter oben
 		if (x == player1.getX() && y + 40 == player1.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player2.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		} else if (x == player2.getX() && y + 40 == player2.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player1.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		}
 
 		// Feld weiter unten
 		if (x == player1.getX() && y - 40 == player1.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player2.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		} else if (x == player2.getX() && y - 40 == player2.getY()) {
 			spielfeld.unbindAllControls();
 			JOptionPane.showMessageDialog(null,"Spieler " + player1.getPlayerID() + " hat das Spiel gewonnen", "Spielende", 1);
 		}
 
 		// Pruefe auf Bomben
 		// Wenn Spieler 1 Bombe legt ...
 		if (player1.getPlayerID() == 1) {
 
 			// Nur ausf�hren, wenn nicht durch eine Kettenreaktion ausgeloest
 			// und Bombe 2 liegt
 			if (kette == false && spielfeld.getBomb2().bombActive() == true) {
 
 				// ... Pruefe auf Bombe 2
 				// Aktuelle Position
 				if (x == spielfeld.getBomb2().getX()
 						&& y == spielfeld.getBomb2().getY()) {
 					spielfeld.getBomb2().Kettenreaktion();
 				}
 
 				// Ein Feld nach Rechts
 				if (x + 40 == spielfeld.getBomb2().getX()
 						&& y == spielfeld.getBomb2().getY()) {
 					spielfeld.getBomb2().Kettenreaktion();
 				}
 
 				// Ein Feld nach Links
 				if (x - 40 == spielfeld.getBomb2().getX()
 						&& y == spielfeld.getBomb2().getY()) {
 					spielfeld.getBomb2().Kettenreaktion();
 				}
 
 				// Ein Feld nach Oben
 				if (x == spielfeld.getBomb2().getX()
 						&& y + 40 == spielfeld.getBomb2().getY()) {
 					spielfeld.getBomb2().Kettenreaktion();
 				}
 				// Ein Feld nach Unten
 				if (x == spielfeld.getBomb2().getX()
 						&& y - 40 == spielfeld.getBomb2().getY()) {
 					spielfeld.getBomb2().Kettenreaktion();
 				}
 			}
 		}
 
 		// Wenn Spieler 2 Bombe legt ...
 		if (player1.getPlayerID() == 2) {
 
 			// Nur ausf�hren, wenn nicht durch eine Kettenreaktion ausgeloest
 			// und Bombe 2 liegt
 			if (kette == false && spielfeld.getBomb1().bombActive() == true) {
 
 				// ... Pruefe auf Bombe 2
 				// Aktuelle Position
 				if (x == spielfeld.getBomb1().getX()
 						&& y == spielfeld.getBomb1().getY()) {
 					spielfeld.getBomb1().Kettenreaktion();
 				}
 
 				// Ein Feld nach Rechts
 				if (x + 40 == spielfeld.getBomb1().getX()
 						&& y == spielfeld.getBomb1().getY()) {
 					spielfeld.getBomb1().Kettenreaktion();
 				}
 
 				// Ein Feld nach Links
 				if (x - 40 == spielfeld.getBomb1().getX()
 						&& y == spielfeld.getBomb1().getY()) {
 					spielfeld.getBomb1().Kettenreaktion();
 				}
 
 				// Ein Feld nach Oben
 				if (x == spielfeld.getBomb1().getX()
 						&& y + 40 == spielfeld.getBomb1().getY()) {
 					spielfeld.getBomb1().Kettenreaktion();
 				}
 				// Ein Feld nach Unten
 				if (x == spielfeld.getBomb1().getX()
 						&& y - 40 == spielfeld.getBomb1().getY()) {
 					spielfeld.getBomb1().Kettenreaktion();
 				}
 			}
 		}
 	}
 }
