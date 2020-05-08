 package dialog;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import csci306.Board;
 import csci306.Card;
 import csci306.HumanPlayer;
 
 public class ClueGame extends JFrame {
 	Board board;
 	
 	JTextField rollDiceField;
 	JTextField guessField;
 	JTextField guessResultField;
 	
 	
 	public ClueGame() {
 		setTitle("Clue Game");
 		setSize(800, 700);
 		
 		createMenuBar();
 		createBoard();
 		createControls();
 		
 	}
 	
 	public void createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		
 		JMenuItem showNotes = new JMenuItem("Show Notes");
 		showNotes.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JDialog showNotesDialog = new NotesDialog(board);
 				showNotesDialog.setVisible(true);
 			}
 			
 		});
 		
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 				
 			}
 			
 		});
 		
 		file.add(showNotes);
 		file.add(exit);
 		menuBar.add(file);
 		setJMenuBar(menuBar);
 	}
 	
 	public void createBoard() {
 		board = new Board();
 		add(board, BorderLayout.CENTER);
 
 		//board.loadPlayers(); Dont do this. The constructor automatically loads and deals the cards.
 		//board.loadCards();
 		//board.deal();
 	}
 	
 	public void createControls() {
 		//add human hand to left of board
 		JPanel myCardControl = new JPanel();
 		myCardControl.setPreferredSize(new Dimension(100, 300));
 		myCardControl.setLayout(new BoxLayout(myCardControl, BoxLayout.Y_AXIS));
 		
 		myCardControl.add(new JLabel("My Cards"));
 		myCardControl.add(createCardDisplay("People", board.getHumanPlayer().getCards(), Card.CardType.PERSON));
 		myCardControl.add(createCardDisplay("Weapons", board.getHumanPlayer().getCards(), Card.CardType.WEAPON));
 		myCardControl.add(createCardDisplay("Rooms", board.getHumanPlayer().getCards(), Card.CardType.ROOM));
 		add(myCardControl, BorderLayout.EAST);
 		
 		JPanel controls = new JPanel();
 		JButton nextTurn = new JButton("Next turn");
 		JButton makeAccusation = new JButton("Make Accusation");
 		//controls.setLayout(new GridLayout(0, 4));
 		
 		JPanel dieRollPanel = new JPanel();
 		JPanel guessPanel = new JPanel();
 		JPanel guessResult = new JPanel();
 		
 		final JTextField rollDiceField = new JTextField(Integer.toString(board.dieRoll));
 		rollDiceField.setEditable(false);
 		
 		guessField = new JTextField();
 		guessField.setEditable(false);
 		
 		guessResultField = new JTextField();
 		guessResultField.setEditable(false);
 		
 		dieRollPanel.setBorder(new TitledBorder(new EtchedBorder(), "Die"));
 		guessPanel.setBorder(new TitledBorder(new EtchedBorder(), "Guess"));
 		guessResult.setBorder(new TitledBorder(new EtchedBorder(), "Guess Result"));
 		
 		dieRollPanel.add(new JLabel("Roll"));
 		dieRollPanel.add(rollDiceField);
 		
 		guessPanel.add(new JLabel("Guess"));
 		guessPanel.add(guessField);
 		
 		guessResult.add(new JLabel("Result"));
 		guessResult.add(guessResultField);
 		
 		final JTextField whoseTurn = new JTextField(board.getWhoseTurn().getName());
 		whoseTurn.setEditable(false);
 		controls.add(new JLabel("Whose turn?"));
 		controls.add(whoseTurn);
 		controls.add(nextTurn);
 		controls.add(makeAccusation);
 		controls.add(dieRollPanel);
 		controls.add(guessPanel);
 		controls.add(guessResult);
 		
 		
 		nextTurn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				board.nextTurn();
 				whoseTurn.setText(board.getWhoseTurn().getName());
 				rollDiceField.setText(Integer.toString(board.dieRoll));
 			}
 			
 		});
 		makeAccusation.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(board.getWhoseTurn() instanceof csci306.HumanPlayer){ // only create this dialog when it's the human player's turn
 					AccusationDialog accDiag = new AccusationDialog(board);
 					accDiag.setVisible(true);
 				} else {
 					String error = "Sorry, but it must be your turn to make a accusation";
 					JOptionPane.showMessageDialog(null, error);
 				}
 			}
			
 		});
 		
 		add(controls, BorderLayout.SOUTH);
 	}
 	
 	public JPanel createCardDisplay(String name, List<Card> cards, Card.CardType cType) {
 		JPanel ret = new JPanel();
 		ret.setBorder(new TitledBorder(new EtchedBorder(), name));
 		
 		for (Card c : cards) {
 			if (c.getType() == cType) {
 				ret.add(new JLabel(c.getName()));
 			}
 		}
 		
 		return ret;
 	}
 }
