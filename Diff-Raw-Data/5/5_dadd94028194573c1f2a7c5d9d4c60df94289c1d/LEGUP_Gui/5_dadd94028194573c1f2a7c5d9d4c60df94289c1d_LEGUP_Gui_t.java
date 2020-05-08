 package edu.rpi.phil.legup.newgui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 
 import javax.swing.AbstractButton;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDesktopPane;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 
 import javax.swing.SwingConstants;
 
 import javax.swing.JTextArea;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.PuzzleModule;
 import edu.rpi.phil.legup.PuzzleGeneration;
 import edu.rpi.phil.legup.Selection;
 import edu.rpi.phil.legup.saveable.SaveableProof;
 import edu.rpi.phil.legup.ILegupGui;
 
 //import edu.rpi.phil.legup.newgui.TreeFrame;
 import javax.swing.plaf.ToolBarUI;
 import javax.swing.plaf.basic.BasicToolBarUI;
 import java.awt.Color;
 import java.awt.Point;
 import java.io.IOException;
 
 import javax.swing.BorderFactory; 
 //import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 public class LEGUP_Gui extends JFrame implements ActionListener, TreeSelectionListener, ILegupGui, WindowListener
 {
 	private static final long serialVersionUID = -2304281047341398965L;
 
 	/**
 	 *	Daniel Ploch - Added 09/25/2009
 	 * Integrated variables for different Proof Modes in LEGUP
 	 *	The PROOF_CONFIG environment variable stores the settings as bitwise flags
 	 *
 	 *	AllOW_JUST:		Allows the user to use the Justification Panel to verify answers.
 	 *	ALLOW_HINTS:	Allow the user to query the tutor for hints (no "Oops - I gave you the answer" step)
 	 *	ALLOW_DEFAPP:	Allow the user to use default-applications (have the AI auto-infer parts of the solution)
 	 *	ALLOW_FULLAI:	Gives user full access to the AI menu, including use of the AI solving algorithm (includes "Oops" tutor step).
 	 *	REQ_STEP_JUST:	Requires the user to justify (correct not necessary) the latest transition before making a new one (safety/training device)
 	 *	IMD_FEEDBACK:	Shows green and red arrows in Proof-Tree for correct/incorrect justifications in real-time.
 	 *	INTERN_RO:		Internal nodes (in the Proof-Tree) are Read-Only, only leaf nodes can be modified.  Ideal safety feature
 	 *	AUTO_JUST:		AI automatically justifies moves as you make them.
 	 */
 	private static int CONFIG_INDEX = 0;
 	public static final int ALLOW_HINTS = 1;
 	public static final int ALLOW_DEFAPP = 2;
 	public static final int ALLOW_FULLAI = 4;
 	public static final int ALLOW_JUST = 8;
 	public static final int REQ_STEP_JUST = 16;
 	public static final int IMD_FEEDBACK = 32;
 	public static final int INTERN_RO = 64;
 	public static final int AUTO_JUST = 128;
 
 	public static boolean profFlag( int flag ){
 		return !((PROF_FLAGS[CONFIG_INDEX] & flag) == 0);
 	}
 
 	private static final String[] PROFILES = {
 		"No Assistance",
 		"Rigorous Proof",
 		"Casual Proof",
 		"Assisted Proof",
 		"Guided Proof",
 		"Training-Wheels Proof",
 		"No Restrictions" };
 	private static final int[] PROF_FLAGS = {
 		0,
 		ALLOW_JUST | REQ_STEP_JUST,
 		ALLOW_JUST,
 		ALLOW_HINTS | ALLOW_JUST | AUTO_JUST,
 		ALLOW_HINTS | ALLOW_JUST | REQ_STEP_JUST,
 		ALLOW_HINTS | ALLOW_DEFAPP | ALLOW_JUST | IMD_FEEDBACK | INTERN_RO,
 		ALLOW_HINTS | ALLOW_DEFAPP | ALLOW_FULLAI | ALLOW_JUST };
 
 	PickGameDialog pgd = null;
 	Legup legupMain = null;
 	private final FileDialog fileChooser;
 
 	private edu.rpi.phil.legup.AI myAI = new edu.rpi.phil.legup.AI();
 
 	/*** TOOLBAR CONSTANTS ***/
 	private static final int TOOLBAR_NEW = 0;
 	private static final int TOOLBAR_OPEN = 1;
 	private static final int TOOLBAR_SAVE = 2;
 	/* --- */
 	private static final int TOOLBAR_UNDO = 3;
 	private static final int TOOLBAR_REDO = 4;
 	/* --- */
 	private static final int TOOLBAR_CONSOLE = 5;
 	private static final int TOOLBAR_HINT = 6;
 	private static final int TOOLBAR_CHECK = 7;
 	/* --- */
 	private static final int TOOLBAR_ZOOMIN = 8;
 	private static final int TOOLBAR_ZOOMOUT = 9;
 	private static final int TOOLBAR_ZOOMRESET = 10;
 	private static final int TOOLBAR_ZOOMFIT = 11;
 
 	final static String[] toolBarNames =
 	{
 		"New",
 		"Open",
 		"Save",
 		"Undo",
 		"Redo",
 		"Console",
 		"Hint",
 		"Check",
 		"Zoom In",
 		"Zoom Out",
 		"Normal Zoom",
 		"Best Fit"
 	};
 
 	AbstractButton[] toolBarButtons =
 	{
 		new JButton(toolBarNames[0], new ImageIcon("images/" + toolBarNames[0] + ".png")),
 		new JButton(toolBarNames[1], new ImageIcon("images/" + toolBarNames[1] + ".png")),
 		new JButton(toolBarNames[2], new ImageIcon("images/" + toolBarNames[2] + ".png")),
 		new JButton(toolBarNames[3], new ImageIcon("images/" + toolBarNames[3] + ".png")),
 		new JButton(toolBarNames[4], new ImageIcon("images/" + toolBarNames[4] + ".png")),
 		new JButton(toolBarNames[5], new ImageIcon("images/" + toolBarNames[5] + ".png")),
 		new JButton(toolBarNames[6], new ImageIcon("images/" + toolBarNames[6] + ".png")),
 		new JButton(toolBarNames[7], new ImageIcon("images/" + toolBarNames[7] + ".png")),
 		new JButton(/*toolBarNames[8],*/ new ImageIcon("images/" + toolBarNames[8] + ".png")),
 		new JButton(/*toolBarNames[9],*/ new ImageIcon("images/" + toolBarNames[9] + ".png")),
 		new JButton(/*toolBarNames[10],*/ new ImageIcon("images/" + toolBarNames[10] + ".png")),
 		new JButton(/*toolBarNames[11],*/ new ImageIcon("images/" + toolBarNames[11] + ".png"))
 	};
 
 	final static int[] toolbarSeperatorBefore =
 	{
 		3, 5, 8
 	};
 
 	public void repaintBoard()
 	{
 		if (((Board)test.getRightComponent()) != null) ((Board)test.getRightComponent()).boardDataChanged(null);
 	}
 
 	public LEGUP_Gui(Legup legupMain)
 	{
 		this.legupMain = legupMain;
 		legupMain.getSelections().addTreeSelectionListener(this);
 		
 		setTitle("LEGUP");
 		setLayout( new BorderLayout() );
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		setupMenu();
 		setupToolBar();
 		setupContent();
 		pack();
 
 		setVisible(true);
 
 		fileChooser = new FileDialog(this);
 	}
 
 	// menubar related fields
 	private JMenuBar bar = new JMenuBar();
 	private JMenu file = new JMenu("File");
 		private JMenuItem newPuzzle = new JMenuItem("New Puzzle");
 		private JMenuItem genPuzzle = new JMenuItem("Puzzle Generators");
 		private JMenuItem openProof = new JMenuItem("Open LEGUP Proof");
 		private JMenuItem saveProof = new JMenuItem("Save LEGUP Proof");
 		private JMenuItem exit = new JMenuItem("Exit");
 	private JMenu edit = new JMenu("Edit");
 		private JMenuItem undo = new JMenuItem("Undo");
 		private JMenuItem redo = new JMenuItem("Redo");
 	private JMenu view = new JMenu("View");
 	private JMenu proof = new JMenu("Proof");
 		private JCheckBoxMenuItem allowDefault = new JCheckBoxMenuItem("Allow Default Rule Applications",false);
 		private JMenu proofMode = new JMenu("Proof Mode");
 			private JCheckBoxMenuItem[] proofModeItems = new JCheckBoxMenuItem[PROF_FLAGS.length];
 	private JMenu AI = new JMenu("AI");
 		private JMenuItem Run = new JMenuItem("Run AI to completion");
 		private JMenuItem Step = new JMenuItem("Run AI one Step");
 		private JMenuItem Test = new JMenuItem("Test AI!");
 		private JMenuItem hint = new JMenuItem("Hint");
 	private JMenu help = new JMenu("Help");
 	// contains all the code to setup the menubar
 	private void setupMenu(){
 		bar.add(file);
 			file.add(newPuzzle);
 				newPuzzle.addActionListener(this);
 				newPuzzle.setAccelerator(KeyStroke.getKeyStroke('N',2));
 			file.add(genPuzzle);
 				genPuzzle.addActionListener(this);
 			file.addSeparator();
 			file.add(openProof);
 				openProof.addActionListener(this);
 				openProof.setAccelerator(KeyStroke.getKeyStroke('O',2));
 			file.add(saveProof);
 				saveProof.addActionListener(this);
 				saveProof.setAccelerator(KeyStroke.getKeyStroke('S',2));
 			file.addSeparator();
 			file.add(exit);
 				exit.addActionListener(this);
 
 		bar.add(edit);
 			edit.add(undo);
 				undo.addActionListener(this);
 				undo.setAccelerator(KeyStroke.getKeyStroke('Z',2));
 			edit.add(redo);
 				redo.addActionListener(this);
 				redo.setAccelerator(KeyStroke.getKeyStroke('Y',2));
 
 		bar.add(view);
 
 		bar.add(proof);
 			proof.add(allowDefault);
 				allowDefault.addActionListener(this);
 			proof.add(proofMode);
 				for (int i = 0; i < PROF_FLAGS.length; i++)
 				{
 					proofModeItems[i] = new JCheckBoxMenuItem(PROFILES[i], i == CONFIG_INDEX);
 					proofModeItems[i].addActionListener(this);
 					proofMode.add(proofModeItems[i]);
 				}
 
 		bar.add(AI);
 			AI.add(Step);
 				Step.addActionListener(this);
 				Step.setAccelerator(KeyStroke.getKeyStroke("F9"));
 			AI.add(Run);
 				Run.addActionListener(this);
 				Run.setAccelerator(KeyStroke.getKeyStroke("F10"));
 			AI.add(Test);
 				Test.addActionListener(this);
 			AI.add(hint);
 				hint.addActionListener(this);
 				hint.setAccelerator(KeyStroke.getKeyStroke('H',0));
 
 		bar.add(help);
 
 		setJMenuBar(bar);
 		this.addWindowListener(this);
 		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		System.out.println("listener initialized");
 	}
 
 	// toolbar related fields
 	private JToolBar toolBar;
 	// contains all the code to setup the toolbar
 	private void setupToolBar(){
 		toolBar = new JToolBar();
 		toolBar.setFloatable( false );
 		toolBar.setRollover( true );
 
 		for (int x = 0; x < toolBarButtons.length; ++x){
 
 			for (int y = 0; y < toolbarSeperatorBefore.length; ++y){
 				if (x == toolbarSeperatorBefore[y]){
 					toolBar.addSeparator();
 				}
 			}
 
 			toolBar.add(toolBarButtons[x]);
 			toolBarButtons[x].addActionListener(this);
 			toolBarButtons[x].setToolTipText(toolBarNames[x]);
 			// TODO text under icons
 			toolBarButtons[x].setVerticalTextPosition( SwingConstants.BOTTOM );
 			toolBarButtons[x].setHorizontalTextPosition( SwingConstants.CENTER );
 		}
 
 		// TODO disable buttons
 		toolBarButtons[TOOLBAR_SAVE].setEnabled(false);
 		toolBarButtons[TOOLBAR_UNDO].setEnabled(false);
 		toolBarButtons[TOOLBAR_REDO].setEnabled(false);
 		toolBarButtons[TOOLBAR_HINT].setEnabled(false);
 		toolBarButtons[TOOLBAR_CHECK].setEnabled(false);
 
 		add( toolBar, BorderLayout.NORTH );
 	}
 
 
 	// TODO
 	private JustificationFrame justificationFrame;
 	private Tree tree;
 	private Console console;
 	private Board board;
 	private JSplitPane test, test2;
 	// contains all the code to setup the main content
 	private void setupContent(){
 		
 		JPanel consoleBox = new JPanel( new BorderLayout() );
 		JPanel treeBox = new JPanel( new BorderLayout() );
 		JPanel ruleBox = new JPanel( new BorderLayout() );
 		
 		// TODO Console
 		console = new Console();
 		//consoleBox.add( console, BorderLayout.SOUTH );
 		
 		// TODO experimental floating toolbar
 		/**/
 		//((BasicToolBarUI) console.getUI()).setFloatingLocation(500,500);
 		//((BasicToolBarUI) console.getUI()).setFloating(true, new Point(500,500));
 		/**/
 		
 		// TODO
 		tree = new Tree( this );
 		//treeBox.add( tree, BorderLayout.SOUTH );
 		
 		justificationFrame = new JustificationFrame( this );
 		//ruleBox.add( justificationFrame, BorderLayout.WEST );
 		
 		board = new Board( this );
 		board.setPreferredSize( new Dimension( 600, 400 ) );
 		
 		JPanel boardPanel = new JPanel( new BorderLayout() );
 		//boardPanel.add( board );
 		//split pane fun :)
 		test = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, justificationFrame, board);
 		test2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, test, tree);
 		test.setPreferredSize(new Dimension(600, 400));
 		test2.setPreferredSize(new Dimension(600, 600));
 		//boardPanel.add(test);
 		boardPanel.add(test2);
 		//no more fun :(
 		TitledBorder title = BorderFactory.createTitledBorder("Board");
 		title.setTitleJustification(TitledBorder.CENTER);
 		boardPanel.setBorder(title);
 		
 		
 		ruleBox.add( boardPanel );
 		treeBox.add( ruleBox );
 		consoleBox.add( treeBox );
 		add( consoleBox );
 		
 		//JLabel placeholder = new JLabel( "Nothing." );
 		//placeholder.setPreferredSize( new Dimension( 600, 400 ) );
 		//add( placeholder );
 		
 	}
 
 	private void openProof()
 	{
 		if(Legup.getInstance().getInitialBoardState() != null){if(!noquit("opening a new proof?"))return;}
 		fileChooser.setMode(FileDialog.LOAD);
 		fileChooser.setTitle("Select Proof");
 		fileChooser.setVisible(true);
 		String filename = fileChooser.getFile();
 
 		if (filename != null) // user didn't pressed cancel
 		{
 			filename = fileChooser.getDirectory() + filename;
 			if (!filename.toLowerCase().endsWith(".proof"))
 				filename += ".proof";
 			Legup.getInstance().loadProofFile(filename);
 		}
 
 	}
 
 	public void saveProof()
 	{
 		BoardState root = legupMain.getInitialBoardState();
 		if (root == null){return;}
 		fileChooser.setMode(FileDialog.SAVE);
 		fileChooser.setTitle("Select Proof");
 		fileChooser.setVisible(true);
 		String filename = fileChooser.getFile();
 
 		if (filename != null) // user didn't pressed cancel
 		{
 			filename = fileChooser.getDirectory() + filename;
 
 			if (!filename.toLowerCase().endsWith(".proof"))
 				filename = filename + ".proof";
 
 			try {
 				SaveableProof.saveProof(root, filename);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void checkProof()
 	{
 		BoardState root = legupMain.getInitialBoardState();
 		root.evalDelayStatus();
 		repaintAll();
 
 		PuzzleModule pm = legupMain.getPuzzleModule();
 		if (pm.checkProof(root))
 			JOptionPane.showMessageDialog(null, "Your proof is correct.");
 		else
 			JOptionPane.showMessageDialog(null, "Your proof is INCORRECT.");
 	}
 
 	private void showAll() {
 		((Board)test.getRightComponent()).initSize();
 		// TODO disable buttons
 		toolBarButtons[TOOLBAR_SAVE].setEnabled(true);
 		toolBarButtons[TOOLBAR_UNDO].setEnabled(true);
 		toolBarButtons[TOOLBAR_REDO].setEnabled(true);
 		toolBarButtons[TOOLBAR_HINT].setEnabled(true);
 		toolBarButtons[TOOLBAR_CHECK].setEnabled(true);
 		///
 		this.pack();
 	}
 
 	private void repaintAll(){
 		((Board)test.getRightComponent()).repaint();
 		((JustificationFrame)test.getLeftComponent()).repaint();
 		tree.repaint();
 	}
 
 
 	/*
 	 * ILegupGui interface methods
 	 * @see edu.rpi.phil.legup.ILegupGui
 	 */
 
 	public void showStatus(String status)
 	{
 		((JustificationFrame)test.getLeftComponent()).setStatus(false,status);
 		// TODO console
 		console.println( "Status: " + status );
 	}
 
 	public void errorEncountered(String error)
 	{
 		JOptionPane.showMessageDialog(null,error);
 	}
 
 	public void promptPuzzle(){
 		if(Legup.getInstance().getInitialBoardState() != null){if(!noquit("opening a new puzzle?"))return;}
 		JFileChooser newPuzzle = new JFileChooser("boards");
 		FileNameExtensionFilter filetype = new FileNameExtensionFilter( "LEGUP Puzzles", "xml" );
 		newPuzzle.setFileFilter( filetype );
 		if( newPuzzle.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ){
 			legupMain.loadBoardFile( newPuzzle.getSelectedFile().getAbsolutePath() );
 			PuzzleModule pm = legupMain.getPuzzleModule();
 			if( pm != null ){
 				((JustificationFrame)test.getLeftComponent()).setJustifications(pm);
 				// AI setup
 				myAI.setBoard(pm);
 			}
 			// show them all
 			showAll();
 		}
 	}
 
 	public void reloadGui()
 	{
 		((JustificationFrame)test.getLeftComponent()).setJustifications(Legup.getInstance().getPuzzleModule());
 
 		// AI setup
 		myAI.setBoard(Legup.getInstance().getPuzzleModule());
 
 		// show them all
 		showAll();
 	}
 
 
 	/*
 	 * Events
 	 */
 
 	//ask to save current proof
 	public boolean noquit(String instr)
 	{
 		String quest = "Would you like to save your proof before ";
 		quest += instr;
 		LEGUP_Gui curgui = Legup.getInstance().getGui();
 		System.out.println("Attempting to save good sirs...");
 		Object[] options = {"Save Proof", "Do Not Save Proof", "Cancel"};
 		int n = JOptionPane.showOptionDialog(bar, quest, "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 		switch(n)
 		{
 		case JOptionPane.YES_OPTION:
 				curgui.saveProof();
 				return true;
 		case JOptionPane.NO_OPTION:
 				return true;
 		case JOptionPane.CANCEL_OPTION:
 				return false;
 		case JOptionPane.CLOSED_OPTION:
 				//pick an option!
 				noquit(instr);
 				return true;
 		default:
 			return true;
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource() == newPuzzle || e.getSource() == toolBarButtons[TOOLBAR_NEW])
 		{
 			promptPuzzle();
 		}
 		else if (e.getSource() == openProof || e.getSource() == toolBarButtons[TOOLBAR_OPEN])
 		{
 			openProof();
 		}
 		else if (e.getSource() == saveProof || e.getSource() == toolBarButtons[TOOLBAR_SAVE])
 		{
 			saveProof();
 		}
 		else if (e.getSource() == genPuzzle)
 		{
 			PuzzleGeneratorDialog pgd = new PuzzleGeneratorDialog(this);
 			pgd.setVisible(true);
 
 			if (pgd.getChoice() == PuzzleGeneratorDialog.PUZZLE_CHOSEN)
 			{
 				PuzzleModule module = PuzzleGeneration.getModule(pgd.puzzleChosen());
 				BoardState puzzle = PuzzleGeneration.makePuzzle(pgd.puzzleChosen(), pgd.difficultyChosen(), this);
 				legupMain.initializeGeneratedPuzzle(module, puzzle);
 
 				((JustificationFrame)test.getLeftComponent()).setJustifications(module);
 
 				// AI setup
 				myAI.setBoard(module);
 
 				// show them all
 				showAll();
 			}
 		}
 		else if (e.getSource() == exit)
 		{
 			System.exit(0);
 		}
 		else if (e.getSource() == undo || e.getSource() == toolBarButtons[TOOLBAR_UNDO])
 		{
 			System.out.println("Undo!");
 		}
 		else if (e.getSource() == redo || e.getSource() == toolBarButtons[TOOLBAR_REDO])
 		{
 			System.out.println("Redo!");
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_CONSOLE])
 		{
 			console.setVisible(!console.isVisible());
 			pack();
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_CHECK])
 		{
 			checkProof();
 		}
 		else if (e.getSource() == hint || e.getSource() == toolBarButtons[TOOLBAR_HINT])
 		{
 			// Is this really necessary? It's already being set when the puzzle loads...
 			//myAI.setBoard(Legup.getInstance().getPuzzleModule());
 			// TODO console
 			console.println("Tutor: " + myAI.findRuleApplication(Legup.getInstance().getSelections().getFirstSelection().getState()) );
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_ZOOMIN]){
 			// TODO - kueblc
 			/*/ DEBUG - Not actual actions!
 			((BasicToolBarUI) justificationFrame.getUI()).setFloatingLocation(500,500);
 			((BasicToolBarUI) justificationFrame.getUI()).setFloating(true, new Point(500,500));*/
 			((Board)test.getRightComponent()).zoomIn();
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_ZOOMOUT]){
 			((Board)test.getRightComponent()).zoomOut();
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_ZOOMRESET]){
 			((Board)test.getRightComponent()).zoomTo(1.0);
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_ZOOMFIT]){
 			((Board)test.getRightComponent()).zoomFit();
 		}
 		else if (e.getSource() == allowDefault)
 		{
 			//Change default applications on, nothing, checks menu checked state everywhere
 		}
 		else if (e.getSource() == Step)
 		{
 			if (myAI.loaded()) {
 				BoardState current = legupMain.getSelections().getFirstSelection().getState();
 				myAI.step(current);
 			}
 		}
 		else if (e.getSource() == Run)
 		{
 			if (myAI.loaded()) {
 				BoardState current = legupMain.getSelections().getFirstSelection().getState();
 				myAI.stepToCompletion(current);
 			}
 		}
 		else if (e.getSource() == Test)
 		{
 			if (myAI.loaded()) {
 				//PuzzleModule current = legupMain.getPuzzleModule();
 				//myAI.test(current);
 				BoardState current = legupMain.getSelections().getFirstSelection().getState();
 				myAI.findRuleApplication(current);
 			}
 		}
 		else
 		{
 			for (int x = 0; x < PROF_FLAGS.length; ++x)
 			{
 				if (e.getSource() == proofModeItems[x]) processConfig(x);
 			}
 		}
 	}
 
 	public void treeSelectionChanged(ArrayList <Selection> s)
 	{
 		repaintAll();
 	}
 
  	public void processConfig(int index)
 	{
 		proofModeItems[CONFIG_INDEX].setState(false);
 		CONFIG_INDEX = index;
 		proofModeItems[CONFIG_INDEX].setState(true);
 
 		int flags = PROF_FLAGS[index];
 		if (!profFlag(ALLOW_DEFAPP)) allowDefault.setState(false);
 		allowDefault.setEnabled(profFlag(ALLOW_DEFAPP));
 
 		AI.setEnabled(profFlag(ALLOW_FULLAI));
 
 		((JustificationFrame)test.getLeftComponent()).setStatus(true, "Proof mode "+PROFILES[index]+" has been activated");
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		// TODO Auto-generated method stub
 		if(Legup.getInstance().getInitialBoardState() != null)
 		{
 			if(noquit("exiting LEGUP?"))
				this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		}
 		else
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 	}
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
