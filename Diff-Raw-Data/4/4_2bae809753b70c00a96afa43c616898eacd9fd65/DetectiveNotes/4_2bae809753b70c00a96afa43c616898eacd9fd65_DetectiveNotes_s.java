 package game;
 import java.awt.GridLayout;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 @SuppressWarnings("serial")
 public class DetectiveNotes extends JFrame {
 	public DetectiveNotes(){
 		this.setLayout(new GridLayout(0,2));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle("Detective Notes");
 		setSize(450, 650);
 		JPanel People=new JPanel();
 		People.setBorder( new TitledBorder(new EtchedBorder(),"People"));
 		String[] peopleList={ "Unsure","Ms. Scarlet","Colonel Mustard","Mr. Green","Ms. White",
 				"Professor Plum","Mrs. Peacock"};
 		JComboBox PeopleCombo=new JComboBox(peopleList);
 		PeopleCombo.setBorder( new TitledBorder(new EtchedBorder(),"People Guess"));
 		JPanel Weapons=new JPanel();
 		Weapons.setBorder( new TitledBorder(new EtchedBorder(),"Weapons"));
 		String[] weaponsList={"Unsure","Candlestick","Knife","Lead Pipe","Gun","Rope","Wrench"};
 		JComboBox WeaponsCombo=new JComboBox(weaponsList);
 		WeaponsCombo.setBorder( new TitledBorder(new EtchedBorder(),"Weapons Guess"));
 		JPanel Rooms=new JPanel();
		People.setBorder( new TitledBorder(new EtchedBorder(),"Rooms"));
 		String[] roomList={"Unsure","Kitchen","Dining Room","Lounge","Ballroom","Conservatory","Hall","Study",
 				"Library","Billiard Room"};
 		JComboBox RoomsCombo=new JComboBox(roomList);
 		RoomsCombo.setBorder( new TitledBorder(new EtchedBorder(),"Rooms Guess"));
 
 		//Just to cut down on the clutter
 		//creates the Checkboxex
 		addPeople(People);
 		//adds them
 		add(People);
 		add(PeopleCombo);
 		//and so on...
 		addWeapons(Weapons);
 		add(Weapons);
 		add(WeaponsCombo);
 		addRooms(Rooms);
 		add(Rooms);
 		add(RoomsCombo);
 	}
 	
 	private void addRooms(JPanel rooms) {
 		rooms.setLayout(new GridLayout(0,2));
 		JCheckBox kitchen =new JCheckBox("Kitchen");
 		JCheckBox dining=new JCheckBox("Dining Room");
 		JCheckBox lounge=new JCheckBox("Lounge");
 		JCheckBox ballroom=new JCheckBox("Ballroom");
 		JCheckBox conservatory=new JCheckBox("Conservatory");
 		JCheckBox hall=new JCheckBox("Hall");
 		JCheckBox study=new JCheckBox("Study");
 		JCheckBox library=new JCheckBox("Library");
 		JCheckBox billiard=new JCheckBox("Billiard Room");
 		rooms.add(kitchen);
 		rooms.add(dining);
 		rooms.add(lounge);
 		rooms.add(ballroom);
 		rooms.add(conservatory);
 		rooms.add(hall);
 		rooms.add(study);
 		rooms.add(library);
 		rooms.add(billiard);
 
 		
 	}
 	private void addWeapons(JPanel weapons) {
 		weapons.setLayout(new GridLayout(0,2));
 		JCheckBox candlestick=new JCheckBox("CandleStick");
 		JCheckBox knife=new JCheckBox("Knife");
 		JCheckBox leadpipe=new JCheckBox("Lead Pipe");
 		JCheckBox gun=new JCheckBox("Gun");
 		JCheckBox rope=new JCheckBox("Rope");
 		JCheckBox wrench=new JCheckBox("Wrench");
 		weapons.add(candlestick);
 		weapons.add(knife);
 		weapons.add(leadpipe);
 		weapons.add(gun);
 		weapons.add(rope);
 		weapons.add(wrench);
 		
 	}
 	private void addPeople(JPanel people){
 		people.setLayout(new GridLayout(0,2));
 		JCheckBox scarlet=new JCheckBox("Ms. Scarlet");
 		JCheckBox mustard=new JCheckBox("Colonel Mustard");
 		JCheckBox green=new JCheckBox("Mr. Green");
 		JCheckBox white=new JCheckBox("Ms. White");
 		JCheckBox plum=new JCheckBox("Professor Plum");
 		JCheckBox peacock=new JCheckBox("Mrs. Peacock");
 		people.add(scarlet);
 		people.add(mustard);
 		people.add(green);
 		people.add(white);
 		people.add(peacock);
 		people.add(plum);
 	}
 	
 
 
 }
