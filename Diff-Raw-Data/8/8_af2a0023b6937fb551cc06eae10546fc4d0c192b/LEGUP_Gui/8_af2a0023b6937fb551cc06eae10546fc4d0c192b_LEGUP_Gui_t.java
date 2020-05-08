 package edu.rpi.phil.legup.newgui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.AbstractButton;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
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

 public class LEGUP_Gui extends JFrame implements ActionListener, InternalFrameListener, TreeSelectionListener, ILegupGui
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
 	public static final int ALLOW_HINTS = 1,
 									ALLOW_DEFAPP = 2,
 									ALLOW_FULLAI = 4,
 									ALLOW_JUST = 8,
 									REQ_STEP_JUST = 16,
 									IMD_FEEDBACK = 32,
 									INTERN_RO = 64,
 									AUTO_JUST = 128;
 	public static boolean profFlag(int flag)
 	{
 		return !((PROF_FLAGS[CONFIG_INDEX] & flag) == 0);
 	}
 
 	private static final String[] PROFILES = {"No Assistance", "Rigorous Proof", "Casual Proof", "Assisted Proof", "Guided Proof", "Training-Wheels Proof", "No Restrictions"};
 	private static final int[] PROF_FLAGS =  {0,
 															ALLOW_JUST | REQ_STEP_JUST,
 															ALLOW_JUST,
 															ALLOW_HINTS | ALLOW_JUST | AUTO_JUST,
 															ALLOW_HINTS | ALLOW_JUST | REQ_STEP_JUST,
 															ALLOW_HINTS | ALLOW_DEFAPP | ALLOW_JUST | IMD_FEEDBACK | INTERN_RO,
 															ALLOW_HINTS | ALLOW_DEFAPP | ALLOW_FULLAI | ALLOW_JUST};
 
 	private static final int BOARD = 0;
 	private static final int JUSTIFICATION = 1;
 	private static final int TREE = 2;
 	private static final int TUTOR = 3;
 
 	private static final int TOOLBAR_NEW = 0;
 	private static final int TOOLBAR_OPEN = 1;
 	private static final int TOOLBAR_SAVE = 2;
 
 	private static final int TOOLBAR_UNDO = 3;
 	private static final int TOOLBAR_REDO = 4;
 
 	private static final int TOOLBAR_CHECK = 5;
 	private static final int TOOLBAR_TUTOR = 6;
 	
 	/*private static final int TOOLBAR_BOARD = 5;
 	private static final int TOOLBAR_JUSTIFICATION = 6;
 	private static final int TOOLBAR_TREE = 7;*/
 	
 
 	PickGameDialog pgd = null;
 	Legup legupMain = null;
 
 	private JDesktopPane mdiPane = new JDesktopPane();
 
 	private JMenuBar bar = new JMenuBar();
 	private JMenu file = new JMenu("File");
 	private JMenu proof = new JMenu("Proof");
 	private JMenu view = new JMenu("View");
 	//added by Jacob
 	private JMenu AI = new JMenu("AI");
 	private JMenuItem Run = new JMenuItem("Run AI to completion");
 	private JMenuItem Step = new JMenuItem("Run AI one Step");
 	private JMenuItem Test = new JMenuItem("Test AI!");
 	private edu.rpi.phil.legup.AI myAI = new edu.rpi.phil.legup.AI();
 	//end additions
 	private JMenuItem newPuzzle = new JMenuItem("New Puzzle");
 	private JMenuItem genPuzzle = new JMenuItem("Puzzle Generators");
 	private JMenuItem openProof = new JMenuItem("Open LEGUP Proof");
 	private JMenuItem saveProof = new JMenuItem("Save LEGUP Proof");
 	private JMenuItem exit = new JMenuItem("Exit");
 	private final FileDialog fileChooser;
 
 	final static String[] toolBarNames =
 	{
 		"New",
 		"Open",
 		"Save",
 		"Undo",
 		"Redo",
 		"Check",
 		"Tutor"
 /*		"Board",
 		"Justification",
 		"Tree",
 		"Tutor"*/
 	};
 
 	AbstractButton[] toolBarButtons =
 	{
 		new JButton(new ImageIcon("images/" + toolBarNames[0] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[1] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[2] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[3] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[4] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[5] + ".png")),
 		new JButton(new ImageIcon("images/" + toolBarNames[6] + ".png"))
 		/*new JToggleButton(new ImageIcon("images/" + toolBarNames[5] + ".png")),
 		new JToggleButton(new ImageIcon("images/" + toolBarNames[6] + ".png")),
 		new JToggleButton(new ImageIcon("images/" + toolBarNames[7] + ".png")),
 		new JToggleButton(new ImageIcon("images/" + toolBarNames[8] + ".png"))*/
 	};
 
 	final static int[] toolbarSeperatorBefore =
 	{
 		3, 5
 	};
 
 	private JToolBar toolBar;
 
 	private final static String[] types =
 	{
 			"Board",
 			"Justification",
 			"Tree",
 			"Tutor",
 	};
 
 	private JCheckBoxMenuItem[] viewItem =
 	{
 			new JCheckBoxMenuItem(types[0],false),
 			new JCheckBoxMenuItem(types[1],false),
 			new JCheckBoxMenuItem(types[2],false),
 			new JCheckBoxMenuItem(types[3],false),
 	};
 
 	private JustificationFrame justificationFrame;
 
 	private JInternalFrame[] frames =
 	{
 			new JInternalFrame(types[0]),
 			null, // justification is initialized in construtor
 			null, // tree is initialized in constructor
 			null // tutor is initialized in constructor
 	};
 
 	private JCheckBoxMenuItem allowDefault =
 		new JCheckBoxMenuItem("Allow Default Rule Applications",false);
 	private JMenu proofMode = new JMenu("Proof Mode");
 	private JCheckBoxMenuItem[] proofModeItems = new JCheckBoxMenuItem[PROF_FLAGS.length];
 
 	// Modified for access - Daniel P
 	private BoardPanel boardPanel = null;
 	public void repaintBoard()
 	{
 		if (boardPanel != null) boardPanel.boardDataChanged(null);
 	}
 
 
 	public LEGUP_Gui(Legup legupMain)
 	{
 		this.setLayout(new BorderLayout());
 		this.add(mdiPane, BorderLayout.CENTER);
 		this.legupMain = legupMain;
 		legupMain.getSelections().addTreeSelectionListener(this);
 		setTitle("LEGUP");
 
 		setupMenu();
 		setupToolBar();
 		setupFrames();
 		pack();
 
 		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation(100,50);
 		setSize(370,getHeight());
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		setVisible(true);
 
 		fileChooser = new FileDialog(this);
 	}
 
 	private void setupToolBar()
 	{
 		toolBar = new JToolBar();
 		toolBar.setFloatable( false );
 		toolBar.setRollover( true );
 
 		for (int x = 0; x < toolBarButtons.length; ++x)
 		{
 			for (int y = 0; y < toolbarSeperatorBefore.length; ++y)
 			{
 				if (x == toolbarSeperatorBefore[y])
 				{
 					toolBar.addSeparator(new Dimension(30,24));
 				}
 			}
 
 			toolBar.add(toolBarButtons[x]);
 			toolBarButtons[x].addActionListener(this);
 			toolBarButtons[x].setToolTipText(toolBarNames[x]);
 		}
 
 		this.add(toolBar, BorderLayout.NORTH);
 	}
 
 	private void setupFrames()
 	{
 		// initialize
 		justificationFrame = new JustificationFrame(types[1],this);
 		frames[JUSTIFICATION] = justificationFrame;
 
 		frames[TREE] = new TreeFrame(types[TREE],legupMain,this);
 		frames[TUTOR] = new TutorFrame();
 
 		boardPanel = new BoardPanel(this);
 
 		// gui
 		//frames[BOARD].setLayout(new BorderLayout());
 		//frames[BOARD].add(boardPanel,BorderLayout.CENTER);
 		frames[BOARD].add(boardPanel);
 
 		//boardPanel.setLayout(new FlowLayout());
 
 //		 window listeners
 		for (int x = 0; x < frames.length; ++x)
 		{
 			frames[x].addInternalFrameListener(this);
 			frames[x].setResizable(true);
 			frames[x].setClosable(true);
 			frames[x].setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
 			mdiPane.add(frames[x]);
 		}
 		//mdiPane.setSize(frames[JUSTIFICATION].getWidth(), frames[TREE].getHeight());
 	}
 
 	private void setupMenu()
 	{
 		bar.add(file);
 		bar.add(proof);
 		bar.add(view);
 		bar.add(AI);
 
 		file.add(newPuzzle);
 		file.add(genPuzzle);
 		file.addSeparator();
 		file.add(openProof);
 		file.add(saveProof);
 		file.addSeparator();
 		file.add(exit);
 
 		proof.add(allowDefault);
 		proof.add(proofMode);
 		for (int i = 0; i < PROF_FLAGS.length; i++)
 		{
 			proofModeItems[i] = new JCheckBoxMenuItem(PROFILES[i], i == CONFIG_INDEX);
 			proofModeItems[i].addActionListener(this);
 			proofMode.add(proofModeItems[i]);
 		}
 
 		AI.add(Step);
 		AI.add(Run);
 		AI.add(Test);
 		for (int x = 0; x < viewItem.length; ++x)
 		{
 			view.add(viewItem[x]);
 			viewItem[x].addActionListener(this);
 		}
 
 		allowDefault.addActionListener(this);
 		newPuzzle.addActionListener(this);
 		genPuzzle.addActionListener(this);
 		openProof.addActionListener(this);
 		saveProof.addActionListener(this);
 		exit.addActionListener(this);
 
 		Step.addActionListener(this);
 		Run.addActionListener(this);
 		Test.addActionListener(this);
 
 		setJMenuBar(bar);
 	}
 
 	private void selectNewPuzzle()
 	{
 		pgd.setVisible(true);
 
 		if (pgd.okPressed)
 		{
 			legupMain.loadBoardFile(pgd.getPuzzle());
 
 			PuzzleModule pm = legupMain.getPuzzleModule();
 
 			if (pm != null)
 			{
 				justificationFrame.setJustifications(pm);
 
 				// AI setup
 				myAI.setBoard(pm);
 			}
 
 			// show them all
 			showFrame(JUSTIFICATION);
 			showFrame(BOARD);
 			showFrame(TREE);
 			showFrame(TUTOR);
 		}
 	}
 
 	private void openProof()
 	{
 		fileChooser.setMode(FileDialog.LOAD);
 		fileChooser.setTitle("Select Proof");
 		fileChooser.setVisible(true);
 		String filename = fileChooser.getFile();
 
 		if (filename != null) // user didn't pressed cancel
 		{
 			filename = fileChooser.getDirectory() + filename;
 
 			if (!filename.toLowerCase().endsWith(".proof"))
 	    		filename = filename + ".proof";
 
 			Legup.getInstance().loadProofFile(filename);
 		}
 
 	}
 
 	private void saveProof()
 	{
 		BoardState root = legupMain.getInitialBoardState();
 		if (root != null)
 		{
 			fileChooser.setMode(FileDialog.SAVE);
 			fileChooser.setTitle("Select Proof");
 			fileChooser.setVisible(true);
 			String filename = fileChooser.getFile();
 
 			if (filename != null) // user didn't pressed cancel
 			{
 				filename = fileChooser.getDirectory() + filename;
 
 				if (!filename.toLowerCase().endsWith(".proof"))
 		    		filename = filename + ".proof";
 
 			    SaveableProof.saveProof(root, filename);
 			}
 
 		}
 		else
 			JOptionPane.showMessageDialog(null, "You do not have a puzzle open!");
 	}
 
 	private void checkProof()
 	{
 		BoardState root = legupMain.getInitialBoardState();
 		root.evalDelayStatus();
 		repaintAll();
 
 		if (root != null)
 		{
 			PuzzleModule pm = legupMain.getPuzzleModule();
 
 			if (pm.checkProof(root))
 				JOptionPane.showMessageDialog(null, "Your proof is correct.");
 			else
 				JOptionPane.showMessageDialog(null, "Your proof is INCORRECT.");
 		}
 		else
 			JOptionPane.showMessageDialog(null, "You do not have a puzzle open!");
 	}
 
 
 
 	/*private int buttonIndexToFrameIndex(int buttonIndex)
 	{
 		return buttonIndex - TOOLBAR_BOARD;
 	}
 
 	private int frameIndexToButtonIndex(int frameIndex)
 	{
 		return TOOLBAR_BOARD + frameIndex;
 	}*/
 
 	/**
 	 * Show the JFrame cooresponding to the ID (like BOARD or TREE)
 	 * @param ID the frame type to show, static member of LEGUP_Gui
 	 */
 	private void showFrame(int ID)
 	{
 		if (ID == BOARD)
 		{ // location is under menuBar
 			boardPanel.initSize();
 			frames[ID].setLocation(0,frames[JUSTIFICATION].getHeight());
 		}
 		else if (ID == JUSTIFICATION)
 		{
 			frames[ID].setLocation(0,0);
 		}
 		else if (ID == TREE)
 		{
 			frames[ID].setLocation(frames[BOARD].getX() + frames[BOARD].getWidth(),frames[BOARD].getY());
 		}
 		else if (ID == TUTOR)
 		{
 			frames[ID].setLocation(frames[JUSTIFICATION].getX() + frames[JUSTIFICATION].getWidth(),0);
 		}
 
 		frames[ID].pack();
 		frames[ID].setVisible(true);
 
 	}
 
 	private void setFrameState(int index, boolean show)
 	{
 		setFrameViewersState(index,show);
 
 		if (show)
 			showFrame(index);
 		else
 			frames[index].setVisible(false);
 	}
 
 	/**
 	 * Set the state of the frame viewers (menu / toggle button)
 	 * @param frameIndex the frame index from the frames array
 	 * @param newState the state we're setting it too
 	 */
 	private void setFrameViewersState(int frameIndex,boolean newState)
 	{
 		//JToggleButton b = (JToggleButton)toolBarButtons[frameIndexToButtonIndex(frameIndex)];
 		//b.setSelected(newState);
 
 		viewItem[frameIndex].setState(newState);
 	}
 
 	private void repaintAll()
 	{
 		for (int x = 0; x < frames.length; ++x)
 		{
 			frames[x].repaint();
 		}
 	}
 
 
 	/*
 	 * ILegupGui interface methods
 	 * @see edu.rpi.phil.legup.ILegupGui
 	 */
 
 	public void showStatus(String status)
 	{
 		justificationFrame.setStatus(false,status);
 	}
 
 	public void errorEncountered(String error)
 	{
 		JOptionPane.showMessageDialog(null,error);
 	}
 
 	public void promptPuzzle()
 	{
 		pgd = new PickGameDialog(this,legupMain,true);
 		selectNewPuzzle();
 
 		mdiPane.setPreferredSize(new Dimension(frames[JUSTIFICATION].getWidth() + frames[TUTOR].getWidth(), frames[TREE].getHeight() + frames[JUSTIFICATION].getHeight()));
 		pack();
 	}
 
 	public void reloadGui()
 	{
 		justificationFrame.setJustifications(Legup.getInstance().getPuzzleModule());
 
 		// AI setup
 		myAI.setBoard(Legup.getInstance().getPuzzleModule());
 
 		// show them all
 		showFrame(JUSTIFICATION);
 		showFrame(BOARD);
 		showFrame(TREE);
 		showFrame(TUTOR);
 	}
 
 
 	/*
 	 * Events
 	 */
 
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource() == newPuzzle || e.getSource() == toolBarButtons[TOOLBAR_NEW])
 		{
 			selectNewPuzzle();
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
 
 				justificationFrame.setJustifications(module);
 
 				// AI setup
 				myAI.setBoard(module);
 
 				// show them all
 				showFrame(JUSTIFICATION);
 				showFrame(BOARD);
 				showFrame(TREE);
 				showFrame(TUTOR);
 			}
 		}
 		else if (e.getSource() == exit)
 		{
 			System.exit(0);
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_UNDO])
 		{
 			System.out.println("Undo!");
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_REDO])
 		{
 			System.out.println("Redo!");
 		}
 		else if (e.getSource() == toolBarButtons[TOOLBAR_CHECK])
 		{
 			checkProof();
 		}
		else if (e.getSource() == toolBarButtons[TOOLBAR_TUTOR])
		{
			myAI.setBoard(Legup.getInstance().getPuzzleModule());
			((TutorFrame)frames[TUTOR]).tutorPrintln(myAI.findRuleApplication(Legup.getInstance().getSelections().getFirstSelection().getState()));
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
 			/*for (int x = TOOLBAR_BOARD; x <= TOOLBAR_TUTOR; ++x)
 			{
 				if (e.getSource() == toolBarButtons[x])
 				{
 					JToggleButton b = (JToggleButton)e.getSource();
 					int index = buttonIndexToFrameIndex(x);
 
 					setFrameState(index,b.isSelected());
 				}
 			}*/
 			for (int x = 0; x < viewItem.length;++x)
 			{
 				if (e.getSource() == viewItem[x]) setFrameState(x,viewItem[x].isSelected());
 			}
 			for (int x = 0; x < PROF_FLAGS.length; ++x)
 			{
 				if (e.getSource() == proofModeItems[x]) processConfig(x);
 			}
 		}
 	}
 
 	public void internalFrameClosing(InternalFrameEvent e)
 	{
 		for (int x = 0; x < frames.length; ++x)
 		{
 			if (e.getSource() == frames[x])
 			{
 				setFrameViewersState(x,false);
 			}
 		}
 	}
 
 	public void internalFrameOpened(InternalFrameEvent e)
 	{
 		for (int x = 0; x < frames.length; ++x)
 		{
 			if (e.getSource() == frames[x])
 			{
 				setFrameViewersState(x,true);
 			}
 		}
 	}
 
 	public void internalFrameClosed(InternalFrameEvent e) { }
 	public void internalFrameIconified(InternalFrameEvent e) { }
 	public void internalFrameDeiconified(InternalFrameEvent e) { }
 	public void internalFrameActivated(InternalFrameEvent e) { }//e.getInternalFrame().requestFocusInWindow(); }
 	public void internalFrameDeactivated(InternalFrameEvent e) { }
 
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
 
 		justificationFrame.setStatus(true, "Proof mode "+PROFILES[index]+" has been activated");
 	}
 
 }
