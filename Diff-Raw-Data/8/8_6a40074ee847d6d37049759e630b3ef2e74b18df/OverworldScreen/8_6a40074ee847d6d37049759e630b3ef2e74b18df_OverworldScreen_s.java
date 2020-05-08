 package yuuki.ui.screen;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import yuuki.ui.WorldViewer;
 import yuuki.util.Grid;
 import yuuki.world.Locatable;
 import yuuki.world.Tile;
 import yuuki.world.WalkGraph;
 
 /**
  * The screen displayed when at the overworld.
  */
 @SuppressWarnings("serial")
 public class OverworldScreen extends Screen<OverworldScreenListener> {
 	
 	/**
 	 * The height of the world viewer, in tiles.
 	 */
 	public static final int VIEWER_HEIGHT = 10;
 	
 	/**
 	 * The width of the world viewer, in tiles.
 	 */
	public static final int VIEWER_WIDTH = 10;
 	
 	/**
 	 * Listens for clicks on this OverworldScreen's buttons.
 	 */
 	private ActionListener hitListener = new ActionListener() {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Component c = (Component) e.getSource();
 			if (c == startButton) {
 				fireStartClicked();
 			} else if (c == moveSouthWestButton || c == moveSouthButton ||
 					c == moveSouthEastButton || c == moveWestButton ||
 					c == moveNullButton || c == moveEastButton ||
 					c == moveNorthWestButton || c == moveNorthButton ||
 					c == moveNorthEastButton) {
 				if (walkGraph != null) {
 					fireMoveButtonClicked(c);
 				}
 			}
 		}
 		
 	};
 	
 	/**
 	 * The button that moves the character east.
 	 */
 	private JButton moveEastButton;
 	
 	/**
 	 * The objects listening for movement events by the player.
 	 */
 	private Set<OverworldMovementListener> movementListeners;
 	
 	/**
 	 * The button that moves the character north.
 	 */
 	private JButton moveNorthButton;
 	
 	/**
 	 * The button that moves the character north-east.
 	 */
 	private JButton moveNorthEastButton;
 	
 	/**
 	 * The button that moves the character north-west.
 	 */
 	private JButton moveNorthWestButton;
 	
 	/**
 	 * The button that makes the character pass one turn.
 	 */
 	private JButton moveNullButton;
 	
 	/**
 	 * The button that moves the character south.
 	 */
 	private JButton moveSouthButton;
 	
 	/**
 	 * The button that moves the character south-east.
 	 */
 	private JButton moveSouthEastButton;
 	
 	/**
 	 * The button that moves the character south-west.
 	 */
 	private JButton moveSouthWestButton;
 	
 	/**
 	 * The button that moves the character west.
 	 */
 	private JButton moveWestButton;
 	
 	/**
 	 * Listens for directional keypad pushes.
 	 */
 	private KeyListener numpadListener = new KeyAdapter() {
 		@Override
 		public void keyPressed(KeyEvent e) {
 			switch (e.getKeyCode()) {
 				case KeyEvent.VK_NUMPAD1:
 					moveSouthWestButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD2:
 					moveSouthButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD3:
 					moveSouthEastButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD4:
 					moveWestButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD5:
 					moveNullButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD6:
 					moveEastButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD7:
 					moveNorthWestButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD8:
 					moveNorthButton.doClick();
 					break;
 					
 				case KeyEvent.VK_NUMPAD9:
 					moveNorthEastButton.doClick();
 					break;
 			}
 		}
 		
 	};
 	
 	/**
 	 * The button that advances to the battle screen.
 	 */
 	private JButton startButton;
 	
 	/**
 	 * Used to calculate where the user wishes to go when a button is clicked.
 	 */
 	private WalkGraph walkGraph;
 	
 	/**
 	 * Displays the world.
 	 */
 	private WorldViewer worldViewer;
 	
 	/**
 	 * Creates a new OverworldScreen. The child components are created and
 	 * added to the screen.
 	 * 
 	 * @param width The width of the screen.
 	 * @param height The height of the screen.
 	 */
 	public OverworldScreen(int width, int height) {
 		super(width, height);
 		movementListeners = new HashSet<OverworldMovementListener>();
 		KeyListener enterListener = new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					fireStartClicked();
 				}
 			}
 		};
 		addKeyListener(numpadListener);
 		setLayout(new FlowLayout());
 		worldViewer = new WorldViewer(VIEWER_WIDTH, VIEWER_HEIGHT);
 		startButton = new JButton("Start");
 		startButton.addActionListener(hitListener);
 		startButton.addKeyListener(enterListener);
 		startButton.setFocusable(false);
 		createMovementButtons();
 		addElements();
 	}
 	
 	/**
 	 * Adds a listener to the list of movement listeners.
 	 * 
 	 * @param l The listener to add.
 	 */
 	public void addMovementListener(OverworldMovementListener l) {
 		movementListeners.add(l);
 	}
 	
 	/**
 	 * Adds a series of Locatable objects to the world viewer.
 	 * 
 	 * @param ls The Locatables to add.
 	 */
 	public void addWorldLocatables(ArrayList<Locatable> ls) {
 		for (Locatable l : ls) {
 			worldViewer.addLocatable(l);
 		}
 	}
 	
 	/**
 	 * Clears all of the world Locatables out of the world viewer.
 	 */
 	public void clearWorldLocatables() {
 		worldViewer.clearLocatables();
 	}
 	
 	/**
 	 * Creates a movement button and sets standard movement button properties
 	 * on it.
 	 * 
 	 * @param label The text to show on the button.
 	 * 
 	 * @return The movement button.
 	 */
 	public JButton createMoveButton(String label) {
 		JButton button = new JButton(label);
 		button.setFocusable(false);
 		button.addActionListener(hitListener);
 		return button;
 	}
 	
 	/**
 	 * Removes a listener from the list of movement listeners.
 	 * 
 	 * @param l The listener to remove.
 	 */
 	public void removeMovementListener(OverworldMovementListener l) {
 		movementListeners.remove(l);
 	}
 	
 	/**
 	 * Sets the initial focus of this screen to the start button.
 	 */
 	@Override
 	public void setInitialProperties() {
 		this.requestFocus();
 	}
 	
 	/**
 	 * Sets the WalkGraph used to calculate where the user wants to walk to.
 	 * 
 	 * @param graph The WalkGraph to use.
 	 */
 	public void setWalkGraph(WalkGraph graph) {
 		this.walkGraph = graph;
 		setMoveButtonActivations();
 	}
 	
 	/**
 	 * Changes the world viewer's view of the world.
 	 * 
 	 * @param view The grid with the view to show.
 	 */
 	public void setWorldView(Grid<Tile> view) {
 		worldViewer.setView(view);
 	}
 	
 	/**
 	 * Updates the world view to show a new center.
 	 * 
 	 * @param center The coordinates of the new center to show.
 	 */
 	public void updateWorldView(Point center) {
 		worldViewer.updateDisplay(center);
 	}
 	
 	/**
 	 * Creates element containers and adds them to this Screen.
 	 */
 	private void addElements() {
 		Box moveBox = createMovementBox();
 		Box vertBox = Box.createVerticalBox();
 		Box fieldBox = Box.createHorizontalBox();
 		fieldBox.add(new JLabel("The overworld!"));
 		fieldBox.add(Box.createHorizontalStrut(2));
 		fieldBox.add(new JLabel("Hit the button to start a battle ==>"));
 		fieldBox.add(Box.createHorizontalStrut(2));
 		fieldBox.add(startButton);
 		vertBox.add(worldViewer);
 		vertBox.add(fieldBox);
 		vertBox.add(Box.createVerticalStrut(10));
 		vertBox.add(moveBox);
 		add(vertBox);
 	}
 	
 	/**
 	 * Creates the box containing the movement buttons.
 	 * 
 	 * @return The box containing the movement buttons.
 	 */
 	private Box createMovementBox() {
 		Dimension d = new Dimension(170, 100);
 		Box moveBox = Box.createVerticalBox();
 		moveBox.setPreferredSize(d);
 		moveBox.setMaximumSize(d);
 		Box row1 = Box.createHorizontalBox();
 		Box row2 = Box.createHorizontalBox();
 		Box row3 = Box.createHorizontalBox();
 		row1.add(moveNorthWestButton);
 		row1.add(Box.createHorizontalGlue());
 		row1.add(moveNorthButton);
 		row1.add(Box.createHorizontalGlue());
 		row1.add(moveNorthEastButton);
 		row2.add(moveWestButton);
 		row2.add(Box.createHorizontalGlue());
 		row2.add(moveNullButton);
 		row2.add(Box.createHorizontalGlue());
 		row2.add(moveEastButton);
 		row3.add(moveSouthWestButton);
 		row3.add(Box.createHorizontalGlue());
 		row3.add(moveSouthButton);
 		row3.add(Box.createHorizontalGlue());
 		row3.add(moveSouthEastButton);
 		moveBox.add(row1);
 		moveBox.add(Box.createVerticalGlue());
 		moveBox.add(row2);
 		moveBox.add(Box.createVerticalGlue());
 		moveBox.add(row3);
 		return moveBox;
 	}
 	
 	/**
 	 * Constructs the movement buttons.
 	 */
 	private void createMovementButtons() {
 		moveNorthButton = createMoveButton("\u2191"); // arrow char
 		moveWestButton = createMoveButton("\u2190"); // arrow char
 		moveEastButton = createMoveButton("\u2192"); // arrow char
 		moveSouthButton = createMoveButton("\u2193"); // arrow char
 		moveNorthEastButton = createMoveButton("\u2197"); // arrow char
 		moveNorthWestButton = createMoveButton("\u2196"); // arrow char
 		moveSouthEastButton = createMoveButton("\u2198"); // arrow char
 		moveSouthWestButton = createMoveButton("\u2199"); // arrow char
 		moveNullButton = createMoveButton("\u25CF"); // dot char
 	}
 	
 	/**
 	 * Gets the requested movement and calls the movementButtonPressed() method
 	 * on all movement listeners.
 	 * 
 	 * @param c The button that was pressed.
 	 */
 	private void fireMoveButtonClicked(Component c) {
 		Point p = getMovementPoint(c);
 		if (p != null) { // if the user pressed a valid direction
 			int size = movementListeners.size();
 			OverworldMovementListener[] ls;
 			ls = new OverworldMovementListener[size];
 			movementListeners.toArray(ls);
 			for (OverworldMovementListener l : ls) {
 				l.movementButtonClicked(p);
 			}
 		}
 	}
 	
 	/**
 	 * Calls the startBattleClicked() method on all listeners.
 	 */
 	private void fireStartClicked() {
 		for (OverworldScreenListener l: getElementListeners()) {
 			l.startBattleClicked();
 		}
 	}
 	
 	/**
 	 * Gets the point that the player is to be moved to based on which movement
 	 * button was clicked.
 	 * 
 	 * @param c The movement button that was clicked.
 	 * 
 	 * @return The point that the player is to be moved to.
 	 */
 	private Point getMovementPoint(Component c) {
 		Point movePoint = null;
 		if (c == moveSouthWestButton) {
 			movePoint = walkGraph.getSouthWest();
 		} else if (c == moveSouthButton) {
 			movePoint = walkGraph.getSouth();
 		} else if (c == moveSouthEastButton) {
 			movePoint = walkGraph.getSouthEast();
 		} else if (c == moveWestButton) {
 			movePoint = walkGraph.getWest();
 		} else if (c == moveNullButton) {
 			movePoint = walkGraph.getPosition();
 		} else if (c == moveEastButton) {
 			movePoint = walkGraph.getEast();
 		} else if (c == moveNorthWestButton) {
 			movePoint = walkGraph.getNorthWest();
 		} else if (c == moveNorthButton) {
 			movePoint = walkGraph.getNorth();
 		} else if (c == moveNorthEastButton) {
 			movePoint = walkGraph.getNorthEast();
 		}
 		return movePoint;
 	}
 	
 	/**
 	 * Enables or disables the movement buttons based on whether they represent
 	 * a valid move.
 	 */
 	private void setMoveButtonActivations() {
 		boolean sw, so, se, we, ea, nw, no, ne;
 		sw = so = se = we = ea = nw = no = ne = true;
 		if (walkGraph != null) {
 			sw = (walkGraph.getSouthWest() != null);
 			so = (walkGraph.getSouth() != null);
 			se = (walkGraph.getSouthEast() != null);
 			we = (walkGraph.getWest() != null);
 			ea = (walkGraph.getEast() != null);
 			nw = (walkGraph.getNorthWest() != null);
 			no = (walkGraph.getNorth() != null);
 			ne = (walkGraph.getNorthEast() != null);
 		}
 		moveSouthWestButton.setEnabled(sw);
 		moveSouthButton.setEnabled(so);
 		moveSouthEastButton.setEnabled(se);
 		moveWestButton.setEnabled(we);
 		moveEastButton.setEnabled(ea);
 		moveNorthWestButton.setEnabled(nw);
 		moveNorthButton.setEnabled(no);
 		moveNorthEastButton.setEnabled(ne);
 	}
 	
 }
