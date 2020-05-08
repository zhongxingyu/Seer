 package data;
 
 import data.me.json.*;
 
 
 
 /**
  * The contestant class will be used to create a person who will be competing in
  * the actual game of survivor, and can be chosen by people from the User class.
  * 
  * @author Kevin Brightwell, Jonathon Demelo, Graem Littleton, Justin McDonald,
  * 			Ramesh Raj 
  */
 
 public abstract class Contestant implements Person {
 
 	// player information
 	protected String firstName, lastName, tribe, picture;
 	protected int castDate = -1; // week that player was cast off
 	protected String cID;
 	
 	protected final static String KEY_FIRST_NAME = "first";
 	protected final static String KEY_LAST_NAME	= "last";
 	protected final static String KEY_ID	= "id";
 	protected final static String KEY_PICTURE = "picture";
 	protected final static String KEY_TRIBE = "tribe";
 	protected final static String KEY_DATE = "date";
 
 	/**
 	 * Constructor method for type contestant sets player info
 	 * @param _id ID tag
 	 * @param first
 	 *            first name
 	 * @param last
 	 *            last name
 	 * @param _tribe
 	 *            contestant's tribe
 	 */
 	public Contestant(String _id, String first, String last, String _tribe) 
 			throws InvalidFieldException {
 		setID(_id);
 		setFirstName(first);
 		setLastName(last);
 		setTribe(_tribe);
 	}
 	
 	public Contestant() {
 		castDate = -1;
 	}
 
 	// ------------------ ACCESSOR METHODS -----------------//
 
 	
 	/**
 	 * getFirstName returns the contestant's first name
 	 * 
 	 * @return this.firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * getLastName returns the contestant's lats name
 	 * 
 	 * @return this.lastName
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 
 	/**
 	 * getPicture returns the contestant's picture information
 	 * 
 	 * @return this.picture
 	 */
 	public String getPicture() {
 		return picture;
 	}
 
 	/**
 	 * isCastOff returns the contestant's activity status
 	 * 
 	 * @return this.castOff
 	 */
 	public boolean isCastOff() {
 		return castDate > -1;
 	}
 
 	// ----------------- MUTATOR METHODS -----------------//
 
 	/**
 	 * castOff indicates that a contestant has been removed from the show
 	 */
 	public void castOff() {
 		castDate = GameData.getCurrentGame().getCurrentWeek();
 	}
 
 	/**
 	 * setPicture sets a contestant's picture
 	 * 
 	 * @param pic
 	 */
 	public void setPicture(String pic) {
 		picture = pic;
 	}
 
 	/**
 	 * setTribe sets the contestant's current tribe
 	 * 
 	 * @param name
 	 *            name of new tribe
 	 * @throws InvalidFieldException If tribe is not used.
 	 */
 	public void setTribe(String name) throws InvalidFieldException {
 		String[] s = GameData.getCurrentGame().getTribeNames();
 		
		if (!name.equals(s[0]) && !name.equals(s[1])) {
 			throw new InvalidFieldException("Invalid Tribe.");
 		}
 		
 		tribe = name;
 	}
 
 	/**
 	 * Sets the contestant's first name.
 	 * @param name	New first name of the contestant, must be alphabetic, 
 	 * 		between 1 and 20 chars.
 	 * @throws InvalidFieldException 
 	 */
 	public void setFirstName(String name) throws InvalidFieldException {
 		
 		if (!DataUtils.checkString(name,REGEX_FIRST_NAME))
 			throw new InvalidFieldException("Invalid First Name");
 		
 		firstName = name;
 		
 	}
 
 	/**
 	 * Sets the contestant's last name.
 	 * @param name New last name of the contestant
 	 */
 	public void setLastName(String name) throws InvalidFieldException {
 		if (!DataUtils.checkString(name,REGEX_LAST_NAME))
 			throw new InvalidFieldException("Invalid Last Name");
 		
 		lastName = name;
 	}
 
 	/**
 	 * Returns the tribe of the Contestant
 	 * @return Tribe name of the contestant.
 	 */
 	public String getTribe() {
 		return tribe;
 	}
 
 	/**
 	 * Returns the date the contestant was cast off on. 
 	 * @return Date cast off, -1 if still active.
 	 */
 	public int getCastDate() {
 		return castDate;
 	}
 
 	/**
 	 * Returns the contestant's unique ID tag
 	 * @return assigned ID tag
 	 */
 	public String getID() {
 		return cID;
 	}
 	
 	/**
 	 * Sets the user ID to the passed ID, checks that is is of the correct 
 	 * format
 	 * @param newID the new User ID, must adhere to correct syntax.
 	 * @throws InvalidFieldException 
 	 */
 	public void setID(String newID) throws InvalidFieldException {
 		newID = newID.toLowerCase();
 		if (!DataUtils.checkString(newID,REGEX_CONTEST_ID))
 			throw new InvalidFieldException("Invalid contestant ID");
 		cID = newID;
 	}
 	
 	public void setCastDate(int date) {
 		castDate = date;
 	}
 	
 	public void setCastDate(Integer date) {
 		setCastDate(date.intValue());
 	}
 	
 	public void update(Contestant c) throws InvalidFieldException {
 		if (c.getFirstName() != null) {
 			setFirstName(c.getFirstName());
 		}
 		
 		if (c.getLastName() != null) {
 			setLastName(c.getLastName());
 		}
 		
 		if (c.getID() != null) {
 			setID(c.getID());
 		}
 		
 		if (c.getPicture() != null) {
 			setPicture(c.getPicture());
 		}
 		
 		if (c.getTribe() != null) {
 			setTribe(c.getTribe());
 		}
 		
 		if (c.isCastOff()) {
 			castDate = c.getCastDate();
 		}
 	}
 	
 	/**
 	 * toString returns a string of the contestant's information in JSON format.
 	 */
 	public String toString() {
 		return new String("Contestant<FN: " + "\"" + firstName + "\"" + ", LN: " + "\"" + lastName + "\"" + 
 				", Tribe: " + "\"" + tribe + "\"" + ", ID: " + "\"" + cID + "\">");
 	}
 	
 	
 	// TODO: DOC THESE THREE
 	public abstract JSONObject toJSONObject() throws JSONException; 
 	
 	public abstract void fromJSONString(String json) throws JSONException;
 	
 	public abstract void fromJSONObject(JSONObject o);
 	
 	public String toJSONString() throws JSONException {
 		return toJSONObject().toString();
 	}
 
 	
 }
