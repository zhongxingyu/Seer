 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.util.Set;
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 @SuppressWarnings("serial")
 public class Arena extends JComponent {
 	BufferedImage background;
 	Graphics2D bg_graphics;
 	ArenaObject[][] arena;
 
 	LightCycle user;
 	Set<LightCycle> players;
 
 	private int interval = 35; // Milliseconds between updates.
 	private Timer timer; // Each time timer fires we animate one step.
 
 	public Arena() {
 		setFocusable(true);
 
		players = new HashSet<LightCycle>();

 		timer = new Timer(interval, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tick();
 			}
 		});
 		timer.start();
 
 		addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_LEFT)
 					user.setDirection(LightCycle.Direction.EAST);
 				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
 					user.setDirection(LightCycle.Direction.WEST);
 				else if (e.getKeyCode() == KeyEvent.VK_UP)
 					user.setDirection(LightCycle.Direction.NORTH);
 				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
 					user.setDirection(LightCycle.Direction.SOUTH);
 				else if (e.getKeyCode() == KeyEvent.VK_R)
 					reset();
 			}
 		});
 	}
 
 	/**
 	 * Set the state of the state of the game to its initial value and prepare
 	 * the game for keyboard input.
 	 */
 	public void reset() {
 		background = null;
 		arena = new ArenaObject[getWidth()][getHeight()];
 		user = new LightCycle(Color.WHITE);
 		players.add(user);
 		user.setPosition(getWidth() / 2, getHeight() / 2);
 		requestFocusInWindow();
 	}
 
 	public Set<ArenaObject> spacesUnderLightCycle(LightCycle l) {
 		Set<ArenaObject> ans = new HashSet<ArenaObject>();
 		int start, end, x, y;
 		if (l.getDirection() == LightCycle.Direction.NORTH
 				|| l.getDirection() == LightCycle.Direction.SOUTH) {
 			start = Math.min(l.getRearYPos(), l.getFrontYPos());
 			end = Math.max(l.getRearYPos(), l.getFrontYPos());
 			x = l.getFrontXPos();
 			for (y = start; y < end; y++)
 				if (arena[x][y] != null)
 					ans.add(arena[x][y]);
 		} else {
 			start = Math.min(l.getRearXPos(), l.getFrontXPos());
 			end = Math.max(l.getRearXPos(), l.getFrontXPos());
 			y = l.getFrontYPos();
 			for (x = start; x < end; x++)
 				if (arena[x][y] != null)
 					ans.add(arena[x][y]);
 		}
 
 		return ans;
 	}
 
 	/** Update the game one timestep. */
 	void tick() {
 		for (LightCycle l : players) {
 			l.move();
 			for (ArenaObject o : spacesUnderLightCycle(l)) {
					o.actOn(l);
 			}
 			if (l.isAlive())
 				arena[l.getRearXPos()][l.getRearYPos()] = new Trail(l,
 						l.getRearXPos(), l.getRearYPos());
 			else {
 				System.out.println(l + " is dead.");
 			}
 		}
 		repaint(); // Repaint indirectly calls paintComponent.
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g); // Paint background, border
 		if (background == null) {
 			background = new BufferedImage(getWidth(), getHeight(),
 					BufferedImage.TYPE_INT_RGB);
 			bg_graphics = background.createGraphics();
 			bg_graphics.setColor(Color.BLACK);
 			bg_graphics.drawRect(0, 0, getWidth(), getHeight());
 		}
 		g.drawImage(background, 0, 0, null);
 		for (ArenaObject[] a : arena)
 			for (ArenaObject b : a)
 				if (b != null)
 					b.draw(g);
 		user.draw(g);
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(600, 600);
 	}
 }
