 package cluedo.main;
 
 import cluedo.cards.*;
 import cluedo.cards.Character;
 
 /**
  * Represents a human player in the game. Should contain their current location,
  * as well as their own cards.
  * 
  * TODO: Put an interface in for this I guess.
  * 
  * @author Andrew
  * 
  */
 public class Player {
 	private Location myPosition;
 
 	/* Cards that player has. Assumes can only hold one of each for now. */
 	private CharacterI myChar;
 	private RoomI myRoomCard;
 	private WeaponI myWeap;
 
 	public Player(Location start, CharacterI characterI, RoomI roomI,
 			WeaponI weaponI) {
 		myPosition = start;
 		myChar = characterI;
 		myRoomCard = roomI;
 		myWeap = weaponI;
 	}
 
 	public CharacterI getMyChar() {
 		return myChar;
 	}
 
 	public void setMyChar(Character myChar) {
 		this.myChar = myChar;
 	}
 
 	public RoomI getMyRoomCard() {
 		return myRoomCard;
 	}
 
 	public void setMyRoomCard(Room myRoomCard) {
 		this.myRoomCard = myRoomCard;
 	}
 
 	public WeaponI getMyWeap() {
 		return myWeap;
 	}
 
 	public void setMyWeap(Weapon myWeap) {
 		this.myWeap = myWeap;
 	}
 
 	public void updatePosition(Location newPos) {
 		myPosition = newPos;
 	}
 
 	public Location getLocation() {
 		return myPosition;
 	}
 
 	public static Location startLocation(CharacterI characterI) {
 		// TODO: Can we use enums here? Would be prettier
 		String charName = characterI.getCard();
 		if (charName.equals("Kasandra Scarlett")) {
 			return new Location(28, 18);
 		} else if (charName.equals("Jack Mustard")) {
 			return new Location(28,7);
 		} else if (charName.equals("Diane White")) {
 			return new Location(19,0);
 		} else if (charName.equals("Jacob Green")) {
 			return new Location(9,0);
		} else if (charName.equals("Elanor Peacock")) {
 			return new Location(0,6);
 		} else if (charName.equals("Victor Plum")) {
 			return new Location(0,20);
 		}
 		throw new IllegalArgumentException("Invalid Char name");
 	}
 }
