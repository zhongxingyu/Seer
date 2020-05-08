 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class Game extends JPanel implements ActionListener {
 	private static final long serialVersionUID = -36131189042163368L;
 
 	private Timer timer;
 	private boolean[][] cells;
 	private int CELL_SIZE = 1, FPS = 60, width, height, xSize, ySize;
 
 	public Game() {
 		setDoubleBuffered(true);
 		setBackground(Color.black);
 
 		timer = new Timer(1000 / FPS, this);
 		timer.start();
 	}
 
 	private void checkDimensions() {
 		// Figure out the drawing space.
 		Dimension size = getSize();
 		Insets insets = getInsets();
		int width = size.width - insets.left - insets.right, height = size.height - insets.top - insets.bottom, xSize = (int) Math
				.ceil(width / CELL_SIZE), ySize = (int) Math.ceil(height / CELL_SIZE);
 
 		if (width != this.width || height != this.height) {
 			this.width = width;
 			this.height = height;
 			this.xSize = xSize;
 			this.ySize = ySize;
 			cells = new boolean[xSize][ySize];
 			seedCells();
 		}
 	}
 
 	private void seedCells() {
 		Random r = new Random();
 		for (int x = 0; x < xSize; x++) {
 			for (int y = 0; y < ySize; y++) {
 				boolean alive = r.nextBoolean() && r.nextBoolean() && r.nextBoolean();
 				cells[x][y] = alive;
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		repaint();
 	}
 
 	public void paint(Graphics g) {
 		checkDimensions();
 
 		g.setColor(Color.black);
 		g.fillRect(0, 0, width, height);
 		paintCells(g);
 
 		buildNextGeneration();
 
 		Toolkit.getDefaultToolkit().sync();
 		g.dispose();
 	}
 
 	private void paintCells(Graphics g) {
 		g.setColor(Color.white);
 		for (int x = 0; x < xSize; x++) {
 			for (int y = 0; y < ySize; y++) {
 				boolean alive = cells[x][y];
 				if (alive) {
 					g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
 				}
 			}
 		}
 	}
 
 	private void buildNextGeneration() {
 		for (int x = 0; x < xSize; x++) {
 			for (int y = 0; y < ySize; y++) {
 				cells[x][y] = getNextState(x, y, cells[x][y]);
 			}
 		}
 	}
 
 	private boolean getNextState(int x, int y, boolean alive) {
 		int count = 0;
 		boolean xm1 = x > 0, xp1 = x + 1 < xSize, ym1 = y > 0, yp1 = y + 1 < ySize;
 
 		if (xm1) {
 			if (ym1 && cells[x - 1][y - 1]) {
 				count++;
 			}
 			if (cells[x - 1][y]) {
 				count++;
 			}
 			if (yp1 && cells[x - 1][y + 1]) {
 				count++;
 			}
 		}
 		if (xp1) {
 			if (ym1 && cells[x + 1][y - 1]) {
 				count++;
 			}
 			if (cells[x + 1][y]) {
 				count++;
 			}
 			if (yp1 && cells[x + 1][y + 1]) {
 				count++;
 			}
 		}
 		if (ym1 && cells[x][y - 1]) {
 			count++;
 		}
 		if (yp1 && cells[x][y + 1]) {
 			count++;
 		}
 
 		return (alive && (count == 2 || count == 3)) || (!alive && count == 3);
 	}
 }
