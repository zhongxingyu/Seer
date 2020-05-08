 package clueGame;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class DetectivePanel extends JPanel {
 	private List<JCheckBox> peopleCB = new LinkedList<JCheckBox>();
 	private List<JCheckBox> weaponsCB = new LinkedList<JCheckBox>();
 	private List<JCheckBox> roomsCB = new LinkedList<JCheckBox>();
 	private JComboBox personGuess, weaponGuess, roomGuess;
 	private List<Card> cardDeck;
 	
 	public DetectivePanel(Board board) {
 		super();
 		
 		cardDeck = board.getCards();
 		// TODO: Change the size
 		setSize(new Dimension(800, 800));
 		setMinimumSize(new Dimension(500, 500));
 		setLayout(new GridLayout(0,2));
 		// Pull in the card names and associate with checkboxes
 		peopleCB = loadCheckBoxes(Card.CardType.PERSON);
 		weaponsCB = loadCheckBoxes(Card.CardType.WEAPON);
 		roomsCB = loadCheckBoxes(Card.CardType.ROOM);
 		// Add appropriate cards to combo boxes
 		personGuess = createCardCombo(Card.CardType.PERSON);
 		weaponGuess = createCardCombo(Card.CardType.WEAPON);
 		roomGuess = createCardCombo(Card.CardType.ROOM);
 		// Add other panels
 		add(new PeoplePanel());
 		add(new PersonGuessPanel());
 		add(new WeaponsPanel());
 		add(new WeaponGuessPanel());
 		add(new RoomsPanel());
 		add(new RoomGuessPanel());
 	}
 	
 	private LinkedList<JCheckBox> loadCheckBoxes(Card.CardType cType) {
 		LinkedList<JCheckBox> boxList = new LinkedList<JCheckBox>();
 		for (Card c : cardDeck) {
 			if(c.getType().equals(cType)) {
 				JCheckBox cBox = new JCheckBox(c.getName());
 				boxList.add(cBox);
 			}
 		}
 		return boxList;
 	}
 	
 	private JComboBox createCardCombo(Card.CardType type) {
 		JComboBox guess = new JComboBox();
 		// Step through card deck and check if the card is of the correct type.
 		// Add cardname to comboBox if it is correct type.
 		for (Card c : cardDeck) {
 			if(c.getType().equals(type)) {
 				guess.addItem(c.getName());
 			}
 		}
 		guess.addItem("Unsure");
 		return guess;
 	}
 	
 	private class PeoplePanel extends JPanel {
 		public PeoplePanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "People"));
 			setLayout(new GridLayout(0,2));
 			for (JCheckBox cb : peopleCB) {
 				add(cb);
 			}
 		}
 	}
 	
 	private class WeaponsPanel extends JPanel {
 		public WeaponsPanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "Weapons"));
 			setLayout(new GridLayout(0,2));
 			for (JCheckBox cb : weaponsCB) {
 				add(cb);
 			}
 		}
 	}
 	
 	private class RoomsPanel extends JPanel {
 		public RoomsPanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "Rooms"));
 			setLayout(new GridLayout(0,2));
 			for (JCheckBox cb : roomsCB) {
 				add(cb);
 			}
 		}
 	}
 	
 	private class PersonGuessPanel extends JPanel {
 		public PersonGuessPanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "Guess Person"));
			add(personGuess, BorderLayout.WEST);
 		}
 	}
 	
 	private class WeaponGuessPanel extends JPanel {
 		public WeaponGuessPanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "Guess Weapon"));
			add(personGuess, BorderLayout.WEST);
 		}
 	}
 	
 	private class RoomGuessPanel extends JPanel {
 		public RoomGuessPanel() {
 			setBorder(new TitledBorder (new EtchedBorder(), "Guess Room"));
			add(personGuess, BorderLayout.WEST);
 		}
 	}
 }
