 package pl.edu.agh.to1.dice.view;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import pl.edu.agh.to1.dice.logic.DiceRoll;
 import pl.edu.agh.to1.dice.logic.Player;
 import pl.edu.agh.to1.dice.logic.ScoreCategory;
 
 public class SwingIOHandler implements IOHandler {
 
 	public void showWinner(List<Player> winner, String finalTable) {
 		scoreField.setText(finalTable);
 		mainFrame.repaint();
 	}
 
 	public void showTable(String table) {
 		scoreField.setText(table);
 		mainFrame.pack();
 		mainFrame.repaint();
 	}
 
 	public void showPoints(int i) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void newTurn(int turnNr, String string) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public ScoreCategory getScoreCategory() {
 		System.out.println("Chose " + buttonCategoryGroup.getSelection());
 		return ScoreCategory.getCategoryMap().get(buttonCategoryGroup.getSelection());
 	}
 
 	public DiceRoll rollDice(int diceCount) {
 		DiceRoll dr = new DiceRoll(diceCount);
 		for(int i=0; i<diceCount; ++i) {
 			diceCheckBoxes[i].setText(""+dr.getDiceValue(i));
 		}
 		System.out.println("Rolled " + dr);
 		mainFrame.repaint();
 		return dr;
 	}
 
 	public ScoreCategory chooseScoreCategoryAgain() {
 		return getScoreCategory();
 	}
 
 	public DiceRoll rerollDice(DiceRoll roll, int times) {
 		for(int i=0; i<5; ++i) {
 			diceCheckBoxes[i].setText(""+roll.getDiceValue(i));
 		}
 		synchronized(waitForClick) {
 			while(!diceSaved) {
 				try {
 					waitForClick.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			diceSaved = false;
 		}
 		return roll;
 	}
 
 	public void init() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				createAndShow();
 			}
 		});
 	}
 	
 	private boolean diceSaved = false;
 	private Object waitForClick = new Object();
 	private JFrame mainFrame = new JFrame();
	private JTextField scoreField = new JTextField();
 	private JPanel dicePanel = new JPanel();
 	private JPanel scoreCatPanel = new JPanel();
 	private JCheckBox diceCheckBoxes[] = new JCheckBox[5];
 	ButtonGroup buttonCategoryGroup = new ButtonGroup();
 	public void createAndShow() {
 		mainFrame = new JFrame("DICE GAME");
 		mainFrame.setSize(600, 400);
 		BorderLayout mainPanelLayout = new BorderLayout();
 		mainFrame.setLayout(mainPanelLayout);
 		Container pane = mainFrame.getContentPane();
 		pane.add(scoreField, BorderLayout.LINE_START);
 		scoreField.setEditable(false);
 		
 		dicePanel.setLayout(new FlowLayout());
 		for(int i=0; i<5; i++) {
 			diceCheckBoxes[i] = new JCheckBox();
 			diceCheckBoxes[i].setText("");
 			dicePanel.add(diceCheckBoxes[i]);
 		}
 		JButton diceSelectedButton = new JButton("Go!");
 		diceSelectedButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent actionEvent) {
 				diceSaved = true;
 				waitForClick.notify();
 			}
 			
 		});
 		dicePanel.add( new JButton("Go!") );
 		
 		scoreCatPanel.setLayout(new BoxLayout(scoreCatPanel, BoxLayout.Y_AXIS));
 		
 		for( final Entry<String, ScoreCategory> entry : ScoreCategory.getCategoryMap().entrySet() ) {
 			JRadioButton button = new JRadioButton(entry.getKey());
 			scoreCatPanel.add( button );
 			buttonCategoryGroup.add( button );
 		}
 		pane.add(dicePanel, BorderLayout.PAGE_END);
 		pane.add(scoreCatPanel, BorderLayout.LINE_END);
 		mainFrame.setVisible(true);
 	}
 }
