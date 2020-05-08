 package components;
 
 import gui.GUI;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Ellipse2D;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.Timer;
 
 import div.Sheep;
 
 public class MySheepButton extends JButton implements MouseListener {
 	private JButton jb;
 	private MyBorder border;
 	private int diameter;
 	private Sheep sheep;
 	private Color color = Color.WHITE;
 	private GUI gui;
 	private boolean attackSheep;
 
 	private int buttonCount = 0;
 
 	private Timer timer = new Timer(1000, new TimerListener());
 	private Timer dTimer = new Timer(100, new DrawingTimer());
 
 	public MySheepButton(JButton jb, Sheep sheep, int x, int y, int diameter,
 			GUI gui) {
 		this.jb = jb;
 		this.setVisible(true);
 		this.diameter = diameter;
 		this.sheep = sheep;
 		this.gui = gui;
 
 		this.setBounds(x - diameter / 2, y - diameter / 2, diameter, diameter);
 		this.setOpaque(false);
 		this.setContentAreaFilled(false);
 
 		this.border = new MyBorder(80);
 		border.setColor(Color.BLACK);
 		this.setBorder(border);
 
 		this.addMouseListener(this);
 		this.addActionListener(new TimerListener());
 	}
 
 	/**
 	 * Metode for aa returnere sauen knappen tilhorer
 	 * 
 	 * @return sheep Sheep
 	 */
 
 	public Sheep getSheep() {
 		return sheep;
 	}
 
 	/**
 	 * Metode for aa endre storrelse paa knappen
 	 * 
 	 * @param dim
 	 *            pixels
 	 */
 
 	public void changeSize(int dim) {
 		this.diameter = dim;
 		this.setSize(dim, dim);
 		repaint();
 	}
 
 	/**
 	 * Metode for aa endre fargen til knappen
 	 * 
 	 * @param color
 	 *            fargen til knappen
 	 */
 
 	public void setColor(Color color) {
 		this.color = color;
 		// this.border.setColor(color);
 		repaint();
 	}
 
 	/**
 	 * Metode for aa sette knappen til rod og starte angreps animasjon
 	 * 
 	 * @param bool
 	 *            true for aa sette knapp til angrep
 	 */
 
 	public void attackSheep(boolean bool) {
 		attackSheep = bool;
 		if (bool) {
 			setColor(Color.RED);
 			dTimer.start();
 		} else {
 			setColor(Color.WHITE);
 			dTimer.stop();
 		}
 	}
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, diameter, diameter);
 		g2d.setColor(color);
 		g2d.fill(circle);
 
 		if (attackSheep) {
 			if (border.getColor().equals(Color.BLACK)) {
 				border.setColor(Color.YELLOW);
 			} else {
 				border.setColor(Color.BLACK);
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		this.gui.setLwEditSheep(this.sheep);
 		this.gui.setListSelection(this.sheep);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
		this.color = Color.BLUE;
		repaint();

 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		if (!color.equals(Color.RED)) {
 			this.color = Color.WHITE;
 		}
 		repaint();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		timer.start();
 		if (buttonCount == 1) {
 			gui.setLwEditSheep(this.sheep);
 			buttonCount = 0;
 		}
 		buttonCount++;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Klasse som haandterer dobbel tastetrykk p saue
 	 * 
 	 * @author andreas
 	 * 
 	 */
 
 	private class TimerListener implements ActionListener {
 		int i = 0;
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (i > 1) {
 				buttonCount = 0;
 				i = 0;
 				timer.stop();
 			}
 			i++;
 		}
 	}
 
 	/**
 	 * Klasse som tegner en paa nytt knapp hver gang den blir tilkalt.
 	 * 
 	 * @author andreas
 	 * 
 	 */
 
 	private class DrawingTimer implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			repaint();
 		}
 
 	}
 
 	public String toString() {
 		int id = sheep.getId();
 
 		String lat = String.format("%.6g%n",
 				Double.parseDouble(sheep.getLocation().getLatitude()));
 		String lon = String.format("%.6g%n",
 				Double.parseDouble(sheep.getLocation().getLongitude()));
 
 		return id + " - " + lat + "," + lon;
 	}
 }
