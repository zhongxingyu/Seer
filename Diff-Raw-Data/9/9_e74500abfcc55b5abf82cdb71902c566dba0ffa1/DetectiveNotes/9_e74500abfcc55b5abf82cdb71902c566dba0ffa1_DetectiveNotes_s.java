 package control_GUI;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class DetectiveNotes extends JFrame {
 	
 	private JCheckBox 
 	missScarlet, colonelMustard, mrGreen, mrsWhite, mrsPeacock, professorPlum,
 	Room1, Room2, Room3, Room4, Room5, Room6, Room7, Room8, Room9,
 	weapon1, weapon2, weapon3, weapon4, weapon5, weapon6;
 	
 	public DetectiveNotes(){
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle("Clue GUI");
		setSize(400, 400);
 		
 		addPeople();
 		
 	}
 	
 
 	
 	public void addPeople() {
 		final JPanel topPanel = new JPanel();
 		GridLayout experimentLayout = new GridLayout(3,1);
 		topPanel.setLayout(experimentLayout);
 		
 		JPanel peoplePanel = new JPanel();
 		missScarlet = new JCheckBox("Miss Scarlet");
 		colonelMustard = new JCheckBox("Colonel Mustard");
 		mrGreen = new JCheckBox("Mr. Green");
 		mrsWhite = new JCheckBox("Mrs. White");
 		mrsPeacock = new JCheckBox("Mrs. Peacock");
 		professorPlum = new JCheckBox("Professor Plum");
 		peoplePanel .add(missScarlet);
 		peoplePanel .add(colonelMustard);
 		peoplePanel .add(mrGreen);
 		peoplePanel .	add(mrsWhite);
 		peoplePanel .add(mrsPeacock);
 		peoplePanel. add(professorPlum);
 		peoplePanel.setLayout(new GridLayout(0, 2));
 		peoplePanel.setBorder(new TitledBorder (new EtchedBorder(), "People"));
 	
 	
 
 	JPanel roomPanel = new JPanel();
 	Room1 = new JCheckBox("Kitchen");
 	Room2 = new JCheckBox("Dining Room");
 	Room3 = new JCheckBox("Lounge");
 	Room4 = new JCheckBox("Ball Room");
 	Room5 = new JCheckBox("Conservatory");
 	Room6 = new JCheckBox("Hall");
 	Room7 = new JCheckBox("Library");
 	Room8 = new JCheckBox("Billiard Room");
 	Room9 = new JCheckBox("Study");
 	roomPanel.add(Room1);
 	roomPanel.add(Room2);
 	roomPanel.add(Room3);
 	roomPanel.add(Room4);
 	roomPanel.add(Room5);
 	roomPanel.add(Room6);
 	roomPanel.add(Room7);
 	roomPanel.add(Room8);
 	roomPanel.add(Room9);
 	roomPanel.setLayout(new GridLayout(0, 2));
 	roomPanel.setBorder(new TitledBorder (new EtchedBorder(), "Rooms"));
 	
 	JPanel weaponPanel = new JPanel();
 	weapon1 = new JCheckBox("Candelstick");
 	weapon2 = new JCheckBox("Knife");
 	weapon3 = new JCheckBox("Lead Pipe");
 	weapon4 = new JCheckBox("Revolver");
 	weapon5 = new JCheckBox("Rope");
 	weapon6 = new JCheckBox("Wrench");
 	weaponPanel.add(weapon1);
 	weaponPanel.add(weapon2);
 	weaponPanel.add(weapon3);
 	weaponPanel.add(weapon4);
 	weaponPanel.add(weapon5);
 	weaponPanel.add(weapon6);
 	weaponPanel.setLayout(new GridLayout(0, 2));
 	weaponPanel.setBorder(new TitledBorder(new EtchedBorder(), "Weapons"));
 	
 	JPanel personGuess = new JPanel();
 	JComboBox<String> people = new JComboBox<String>();
 	people.addItem("Miss. Scarlet");
 	people.addItem("Colonel Mustard");
 	people.addItem("Mr. Green");
 	people.addItem("Mrs. White");
 	people.addItem("Mrs. Peacock");
 	people.addItem("Professor Plum");
 	personGuess.add(people);
 	personGuess.setBorder(new TitledBorder(new EtchedBorder(), "Person Guess"));
 	
 	JPanel roomGuess = new JPanel();
 	JComboBox<String> rooms = new JComboBox<String>();
 	rooms.addItem("Kitchen");
 	rooms.addItem("Dining Room");
 	rooms.addItem("Lounge");
 	rooms.addItem("Ballroom");
 	rooms.addItem("Conservatory");
 	rooms.addItem("Hall");
 	rooms.addItem("Study");
 	rooms.addItem("Library");
 	rooms.addItem("Billiard Room");
 	roomGuess.add(rooms);
 	roomGuess.setBorder(new TitledBorder(new EtchedBorder(), "Room Guess"));
 
 	JPanel weaponGuess = new JPanel();
 	JComboBox<String> weapons = new JComboBox<String>();
 	weapons.addItem("Candelstick");
 	weapons.addItem("Knife");
 	weapons.addItem("Revolver");
 	weapons.addItem("Rope");
 	weapons.addItem("Wrench");
 	weapons.addItem("Lead Pipe");
 	weaponGuess.add(weapons);
 	weaponGuess.setBorder(new TitledBorder(new EtchedBorder(), "Weapon Guess"));
 	
 	
 	topPanel.add(peoplePanel);
 	topPanel.add(personGuess);
 	topPanel.add(roomPanel);
 	topPanel.add(roomGuess);
 	topPanel.add(weaponPanel);
 	topPanel.add(weaponGuess);
 	
 	
 	
 	add(topPanel);
 	}
 	
 
 	
 	public static void main(String[] args) {
 		DetectiveNotes controler = new DetectiveNotes() ;
 		controler.setVisible(true);
 
 	}
 	
 	
 
 }
