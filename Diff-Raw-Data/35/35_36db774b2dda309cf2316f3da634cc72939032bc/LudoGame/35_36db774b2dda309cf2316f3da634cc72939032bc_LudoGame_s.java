 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * This is the main runner class for the Ludo Game. It currently prints the
  * board background image, and adds the pawns to the board.
  * 
  * @author Daniel Faulkner
  */
 public class LudoGame extends JPanel {
 
 	// Generated.
 	private static final long serialVersionUID = 4096501410402784791L;
 	// The offsets from the walls of the window
 	private final static int BOARDLEFTOFFSET = 9;
 	private final static int BOARDTOPOFFSET = 7;
 
 	// The grids on the game board are 48 pixels across
 	private final static int GRIDSIZE = 48;
 	// There are 11x11 grids (including ones the pawns can't occupy)
 	private final static int GRIDNUM = 11;
 	// This 2D array holds the top left point of each matching grid spot
 	private final static Point[][] THEGRID = new Point[GRIDNUM][GRIDNUM];
 
	public static int SLEEP = 20;
 
 	private boolean theShowMustGoOn = true;
 	private int thePlayer = 0;
 
 	private final JLabel redLabel;
 	private final JLabel blueLabel;
 	private final JLabel yellowLabel;
 	private final JLabel greenLabel;
 
 	// The array lists to keep track of the pawns in the game
 	private final ArrayList<Pawn> redPawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> bluePawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> yellowPawns = new ArrayList<Pawn>();
 	private final ArrayList<Pawn> greenPawns = new ArrayList<Pawn>();
 
 	// The swing panel for layered objects
 	private final JLayeredPane boardPane;
 
 	private final Die theDie;
 
 	private HomeField redHome;
 	private HomeField blueHome;
 	private HomeField greenHome;
 	private HomeField yellowHome;
 
 	private final ArrayList<GoalField> redGoal = new ArrayList<GoalField>();
 	private final ArrayList<GoalField> blueGoal = new ArrayList<GoalField>();
 	private final ArrayList<GoalField> yellowGoal = new ArrayList<GoalField>();
 	private final ArrayList<GoalField> greenGoal = new ArrayList<GoalField>();
 
 	private final ArrayList<Player> players = new ArrayList<Player>();
 
 	public Runnable continueAfterThreadEnd = new Runnable() {
 		@Override
 		public void run() {
 			continueGameRound();
 		}
 	};
 
 	/**
 	 * Constructor for the game board. Adds layered images and game pieces.
 	 * 
 	 * @param numberOfHumans
 	 *            The number of humans to be playing the game.
 	 */
 	private LudoGame() {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		final ImageIcon boardBackground = createImageIcon("src/board_bkg.png");
 		final ImageIcon redPawnImg = createImageIcon("src/red_disk.png");
 		final ImageIcon bluePawnImg = createImageIcon("src/blue_disk.png");
 		final ImageIcon yellowPawnImg = createImageIcon("src/yellow_disk.png");
 		final ImageIcon greenPawnImg = createImageIcon("src/green_disk.png");
 		final ImageIcon dieImg = createImageIcon("src/die_1.png");
 
 		setupTheGrid();
 
 		boardPane = new JLayeredPane();
 		boardPane.setPreferredSize(new Dimension(540, 540));
 		// boardPane.setPreferredSize(new Dimension(APPWIDTH, APPHEIGHT));
 
 		JLabel board = new JLabel(boardBackground);
 		boardPane.add(board, new Integer(0));
 		Dimension boardSize = board.getPreferredSize();
 		board.setBounds(BOARDLEFTOFFSET, BOARDTOPOFFSET, boardSize.width,
 				boardSize.height);
 
 		JPanel rightPane = new JPanel();
 		// rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
 		rightPane.setLayout(new GridBagLayout());
 
 		JPanel dieLayer = new JPanel();
 		dieLayer.setPreferredSize(new Dimension(200, 200));
 		dieLayer.setBorder(BorderFactory.createTitledBorder(
 				BorderFactory.createLineBorder(Color.black), "Die Roll"));
 		dieLayer.setLayout(new GridBagLayout());
 
 		JLabel die = new JLabel(dieImg);
 		dieLayer.add(die, new GridBagConstraints());
 		dieLayer.setBackground(new Color(188, 189, 194));
 
 		JPanel playersLayer = new JPanel();
 		playersLayer.setPreferredSize(new Dimension(200, 200));
 		playersLayer.setBorder(BorderFactory.createTitledBorder(
 				BorderFactory.createLineBorder(Color.black), "Players"));
 		redLabel = new JLabel("Red Player");
 		blueLabel = new JLabel("Blue Player");
 		yellowLabel = new JLabel("Yellow Player");
 		greenLabel = new JLabel("Green Player");
 		playersLayer.setLayout(new GridBagLayout());
 		playersLayer.setBackground(new Color(188, 189, 194));
 
 		GridBagConstraints playGrid = new GridBagConstraints();
 		playGrid.gridy = 0;
 		playersLayer.add(redLabel, playGrid);
 		playGrid.gridy = 1;
 		playersLayer.add(blueLabel, playGrid);
 		playGrid.gridy = 2;
 		playersLayer.add(yellowLabel, playGrid);
 		playGrid.gridy = 3;
 		playersLayer.add(greenLabel, playGrid);
 
 		GridBagConstraints theGrid = new GridBagConstraints();
 		theGrid.gridy = 0;
 		rightPane.add(dieLayer, theGrid);
 		theGrid.gridy = 1;
 		rightPane.add(playersLayer, theGrid);
 		rightPane.setBackground(new Color(188, 189, 194));
 
 		setupTheFields();
 
 		addPawns(redPawnImg, redPawns, redHome);
 		addPawns(bluePawnImg, bluePawns, blueHome);
 		addPawns(yellowPawnImg, yellowPawns, yellowHome);
 		addPawns(greenPawnImg, greenPawns, greenHome);
 
 		setupThePlayers();
 
 		// Get an instance of the singleton Die.
 		theDie = Die.getInstance(die);
 
 		// Finally, boardPane is added to the game frame.
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		add(boardPane);
 		add(rightPane);
 	}
 
 	/**
 	 * Start of a test method. Anything that should happen after board setup
 	 * goes here.
 	 */
 	protected void startTheGame() {
 		startGameRound();
 	}
 
 	private void startGameRound() {
 		if (theShowMustGoOn) {
 			Player pl = players.get(thePlayer);
 			System.out.println("Player " + thePlayer + " starts turn ...");
 			pl.setLabelIsTurn();
 			int roll = rollDie();
 			sleep(SLEEP);
 			pl.doMove(roll);
 		}
 	}
 
 	protected void continueGameRound() {
 		sleep(SLEEP);
 		if (theDie.lastRoll() == 6) {
 			startGameRound();
 		} else {
 			Player pl = players.get(thePlayer);
 			System.out.println("Turn done!\n");
 			if (pl.checkIfGoalFull()) {
 				System.err.println("We have a winner!!!");
 				theShowMustGoOn = false;
 			}
 			pl.setLabelNotTurn();
 			sleep(SLEEP);
 			if (theShowMustGoOn) {
 				System.out.println("Round done! Next round starting...\n");
 				thePlayer++;
 				if (thePlayer > 3) {
 					thePlayer = 0;
 				}
 				startGameRound();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return The players roll of the die.
 	 */
 	private int rollDie() {
 		int playerRoll = theDie.roll();
 		System.out.println("Roll: " + playerRoll);
 		theDie.setImage(createImageIcon("src/die_" + playerRoll + ".png"));
 		sleep(SLEEP * 2); // This simulates being held in suspense.
 		return playerRoll;
 	}
 
 	/**
 	 * 
 	 * @param milli
 	 *            The amount of time to wait (In milliseconds)
 	 */
 	private void sleep(final long milli) {
 		try {
 			Thread.sleep(milli);
 		} catch (InterruptedException ie) {
 			System.err
 					.println("Unexpected timing error. Aborting thread sleep");
 		}
 	}
 
 	/**
 	 * Sets up the 2D array that holds the 11 x 11 grid imagining of the board
 	 * and assigns each location the relevant Point of the top left corner (for
 	 * a Point p, p.x and p.y give you the x and y coordinates of that point,
 	 * respectively).
 	 */
 	private void setupTheGrid() {
 		for (int i = 0; i < GRIDNUM; i++) {
 			for (int j = 0; j < GRIDNUM; j++) {
 				THEGRID[i][j] = new Point(BOARDLEFTOFFSET + (i * GRIDSIZE),
 						BOARDTOPOFFSET + (j * GRIDSIZE));
 			}
 		}
 	}
 
 	/**
 	 * Hideous method that sets up all the Field instances and lines these up
 	 * with the appropriate spot on THEGRID, assigning a Point value to each
 	 * field and establishing links. If you don't want to cry, try to avoid
 	 * looking at this method too much. It may seem perplexing, which is because
 	 * it directly channels misery into text and wants to devour your soul. But
 	 * don't worry, it works! You can now navigate the track by starting from
 	 * any HomeField!
 	 */
 	private void setupTheFields() {
 
 		final int[] gridJ = { 10, 10, 9, 8, 7, 6, 6, 6, 6, 6, 5, 4, 4, 4, 4, 4,
 				3, 2, 1, 0, 0, 0, 1, 2, 3, 4, 4, 4, 4, 4, 5, 6, 6, 6, 6, 6, 7,
 				8, 9, 10 };
 		final int[] gridI = { 5, 4, 4, 4, 4, 4, 3, 2, 1, 0, 0, 0, 1, 2, 3, 4,
 				4, 4, 4, 4, 5, 6, 6, 6, 6, 6, 7, 8, 9, 10, 10, 10, 9, 8, 7, 6,
 				6, 6, 6, 6 };
 		Field lastField = null;
 		BasicField firstField = null;
 		for (int i = 0; i < 40; i++) {
 			BasicField theTrack = new BasicField(THEGRID[gridI[i]][gridJ[i]]);
 			if (i % 10 == 0) {
 				if (i == 0) {
 					firstField = theTrack;
 					int[] goalJ = { 9, 8, 7, 6 };
 					int[] goalI = { 5, 5, 5, 5 };
 					setupTheGoals(redGoal, goalI, goalJ, theTrack);
 				} else if (i == 10) {
 					int[] goalJ = { 5, 5, 5, 5 };
 					int[] goalI = { 1, 2, 3, 4 };
 					setupTheGoals(blueGoal, goalI, goalJ, theTrack);
 				} else if (i == 20) {
 					int[] goalJ = { 1, 2, 3, 4 };
 					int[] goalI = { 5, 5, 5, 5 };
 					setupTheGoals(yellowGoal, goalI, goalJ, theTrack);
 				} else if (i == 30) {
 					int[] goalJ = { 5, 5, 5, 5 };
 					int[] goalI = { 9, 8, 7, 6 };
 					setupTheGoals(greenGoal, goalI, goalJ, theTrack);
 				}
 			} else if ((i - 1) % 10 == 0) {
 				if (i == 1) {
 					int[] homeJ = { 8, 9, 8, 9 };
 					int[] homeI = { 1, 1, 2, 2 };
 					redHome = setupTheHome(homeI, homeJ, theTrack);
 				} else if (i == 11) {
 					int[] homeJ = { 1, 1, 2, 2 };
 					int[] homeI = { 2, 1, 2, 1 };
 					blueHome = setupTheHome(homeI, homeJ, theTrack);
 				} else if (i == 21) {
 					int[] homeJ = { 2, 1, 2, 1 };
 					int[] homeI = { 9, 9, 8, 8 };
 					yellowHome = setupTheHome(homeI, homeJ, theTrack);
 				} else if (i == 31) {
 					int[] homeJ = { 9, 9, 8, 8 };
 					int[] homeI = { 8, 9, 8, 9 };
 					greenHome = setupTheHome(homeI, homeJ, theTrack);
 				}
 			}
 			if (lastField != null) {
 				lastField.setNextField(theTrack);
 			}
 			lastField = theTrack;
 		}
 		lastField.setNextField(firstField);
 	}
 
 	/**
 	 * Set up the goal fields for each player.
 	 * 
 	 * @param theGoal
 	 * @param gridI
 	 * @param gridJ
 	 * @param linker
 	 */
 	private void setupTheGoals(final ArrayList<GoalField> theGoal,
 			final int[] gridI, final int[] gridJ, final BasicField linker) {
 		GoalField lastField = null;
 		GoalField currentField = null;
 		for (int i = 3; i >= 0; i--) {
 			currentField = new GoalField(THEGRID[gridI[i]][gridJ[i]]);
 			theGoal.add(currentField);
 			if (lastField != null) {
 				currentField.setNextGoalField(lastField);
 			}
 			lastField = currentField;
 		}
 		linker.setGoalField(currentField);
 	}
 
 	/**
 	 * 
 	 * @param gridI
 	 * @param gridJ
 	 * @param entry
 	 * @return
 	 */
 	private HomeField setupTheHome(final int[] gridI, final int[] gridJ,
 			final BasicField entry) {
 		final ArrayList<Point> points = new ArrayList<Point>();
 		for (int i = 0; i < gridI.length; i++) {
 			points.add(THEGRID[gridI[i]][gridJ[i]]);
 		}
 		HomeField hf = new HomeField(points);
 		hf.setNextField(entry);
 		return hf;
 	}
 
 	/**
 	 * Sets up all of the players with strategies and their owned pawns, etc.
 	 */
 	private void setupThePlayers() {
 
 		// Set up the GoalFields in order
 		ArrayList<ArrayList<GoalField>> goalFields = new ArrayList<ArrayList<GoalField>>(
 				Arrays.asList(redGoal, blueGoal, yellowGoal, greenGoal));
 
 		// Same deal with Pawn lists
 		ArrayList<ArrayList<Pawn>> pawns = new ArrayList<ArrayList<Pawn>>(
 				Arrays.asList(redPawns, bluePawns, yellowPawns, greenPawns));
 
 		// And homeFields
 		HomeField[] homeFields = { redHome, blueHome, yellowHome, greenHome };
 
 		// And player labels
 		JLabel[] playerLabels = { redLabel, blueLabel, yellowLabel, greenLabel };
 
 		// Player names
 		String[] names = { "Red", "Blue", "Yellow", "Green" };
 
 		// Possible Strategies
 		String[] strategies = { "Aggressive", "Defensive", "Lone Pawn",
 				"Many Pawns", "Human Player" };
 		// Player Choices of Strategies
 		@SuppressWarnings("rawtypes")
 		JComboBox[] choices = { new JComboBox<String>(strategies),
 				new JComboBox<String>(strategies),
 				new JComboBox<String>(strategies),
 				new JComboBox<String>(strategies) };
 
 		JPanel prompt = new JPanel();
 		for (int i = 0; i < 4; i++) {
 			prompt.add(new JLabel(names[i]));
 			prompt.add(choices[i]);
 		}
 		int result = JOptionPane.showConfirmDialog(null, prompt,
 				"Please designate the players", JOptionPane.OK_CANCEL_OPTION);
 		if (result == JOptionPane.OK_OPTION) {
 			// Loop through the four players.
 			for (int i = 0; i < 4; i++) {
 				Strategy someStrategy;
 				switch (choices[i].getSelectedItem().toString()) {
 				case "Aggressive":
 					someStrategy = new AggressiveStrategy();
 					break;
 				case "Lone Pawn":
 					someStrategy = new LonePawnStrategy();
 					break;
 				case "Many Pawns":
 					someStrategy = new ManyPawnsStrategy();
 					break;
 				case "Defensive":
 					someStrategy = new DefensiveStrategy();
 					break;
 				case "Human Player":
 				default:
 					someStrategy = new HumanStrategy();
 					break;
 				}
 				// Create the players.
 				players.add(new Player(someStrategy, goalFields.get(i),
 						homeFields[i], playerLabels[i], pawns.get(i), this));
 			}
 		} else {
 			System.exit(1);
 		}
 		// All the players are initialized now.
 	}
 
 	/**
 	 * Adds a player's 4 pawns to the game board in the grid spot corresponding
 	 * with the home field (once that's implemented).
 	 * 
 	 * @param imgSrc
 	 *            the pawn's image
 	 * @param pawnList
 	 *            the list to add the pawn to after putting on board
 	 * @param homeFieldX
 	 *            the home field positioning (LEFT or RIGHT)
 	 * @param homeFieldY
 	 *            the home field positioning (TOP or BOTTOM)
 	 */
 	private void addPawns(final ImageIcon imgSrc,
 			final ArrayList<Pawn> pawnList, final HomeField home) {
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 2; j++) {
 				// JLabel jl = new JLabel(imgSrc);
 				JButton jl = new JButton(imgSrc);
 				jl.setBorderPainted(false);
 				jl.setContentAreaFilled(false);
 				boardPane.add(jl, new Integer(1));
 				Dimension size = new Dimension(jl.getIcon().getIconWidth(), jl
 						.getIcon().getIconHeight());
 				jl.setBounds(0, 0, size.width, size.height);
 				Pawn p = new Pawn(jl, home);
 				pawnList.add(p);
 			}
 		}
 	}
 
 	/**
 	 * Loads the image from the specified path.
 	 * 
 	 * @param src
 	 *            the path of the image file
 	 * @return an ImageIcon (or null, if the image is not found)
 	 */
 	private ImageIcon createImageIcon(final String src) {
 		try {
 			BufferedImage bluePawn = ImageIO.read(new File(src));
 			ImageIcon icon = new ImageIcon(bluePawn);
 			return icon;
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Creates the general GUI frames and call the LudoGame constructor.
 	 */
 	private static void createGUI() {
 		JFrame frame = new JFrame("Ludo Game");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		LudoGame contentPane = new LudoGame();
 		contentPane.setOpaque(true);
 		contentPane.setBackground(new Color(188, 189, 194));
 		frame.setContentPane(contentPane);
 
 		frame.pack();
 		frame.setVisible(true);
 
 		// And the game is started!
 		contentPane.startTheGame();
 	}
 
 	/**
 	 * The entry point for the program. Kicks things off by creating the GUI.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		createGUI();
 	}
 }
