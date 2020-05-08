 package monopoly;
 
 import java.util.ArrayList;
 
 public class Player extends Participant {
 
 	private String name;
 	private int currentMoney;
 	private int position;
 	private boolean isInJail = false;
 	private int numberGetOutOfJailCards = 0;
 
 	/***
 	 * Erstellt eine Instanz der Klasse Player mit dem angegebenen Namen
 	 * 
 	 * @param name
 	 */
 	public Player(String name) {
 		setName(name);
 		// TODO: Startgelder aus einer Config lesen!
 		this.currentMoney = 15000;
 	}
 
 	/***
 	 * Gibt den Namen des Spielers zur�ck
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/***
 	 * Benennt den Spieler mit dem angegebenen Namen um
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/***
 	 * Gibt das aktuelle Guthaben des Spielers zur�ck
 	 */
 	public int getCurrentMoney() {
 		return currentMoney;
 	}
 
 	/***
 	 * Transferiert eine bestimmte Menge Geld an einen anderen Teilnehmer
 	 * 
 	 * @param player
 	 *            Der Spieler welcher Geld bekommen soll
 	 * @param amount
 	 *            Die Menge Geld, welche der Spieler bekommt
 	 */
 	public void transferMoneyToParticipant(Participant player, int amount)
 			throws Exception {
 		if (currentMoney >= amount) {
 			player.addMoney(amount);
 			currentMoney -= amount;
 		} else {
 			// TODO: Eigene Exceptions (z.B. OutOfMoneyException)
 			throw new Exception(
 					"Nicht gen�gend Geld f�r die Transaktion vorhanden!");
 		}
 	}
 
 	/***
 	 * F�gt eine bestimmte Menge Geld zum Guthaben des Spielers hinzu
 	 * 
 	 * @param amount
 	 *            Die Menge Geld, welche hinzugef�gt wird
 	 */
 	public void addMoney(int amount) {
 		currentMoney += amount;
 	}
 
 	/***
 	 * Zieht eine bestimmte Menge Geld vom Guthaben des Spielers ab
 	 * 
 	 * @param amount
 	 *            Die Menge Geld, welche abgezogen wird
 	 */
 	public void removeMoney(int amount) throws Exception {
 		if (amount > 0) {
 			addMoney(amount);
 		} else {
 			// TODO: Eigene Exceptions
 			throw new Exception("Man kann keinen negativen Betrag abziehen!");
 		}
 	}
 
 	/***
 	 * Gibt das aktuelle Field-Objekt, auf welchem sich der Spieler befindet
 	 * zur�ck
 	 * 
 	 * @return
 	 */
 	public Field getCurrentField() {
 		return Monopoly.getGameBoard().getAllFields().get(this.position);
 	}
 
 	/***
 	 * Bewegt den aktuellen Spieler auf ein bestimmtes Feld
 	 * 
 	 * @param field
 	 */
 	public void move(Field field) throws Exception {
 		this.position = field.property.getPosition();
 		field.onEnter();
 	}
 
 	/***
 	 * Bewegt den aktuellen Spieler um den angegebenen Wert weiter
 	 * 
 	 * @param value
 	 */
 	public void move(int value) throws Exception {
 		int position = this.position + value
 				% Monopoly.getGameBoard().getNumberOfFields();
 		this.move(Monopoly.getGameBoard().getField(position));
 	}
 
 	public void moveTo(String name) throws Exception {
 		this.move(Monopoly.getGameBoard().getField(name));
 	}
 
 	public int getPosition() {
 		return this.position;
 	}
 
 	public ArrayList<PropertyField> getOwnedFields() {
 		ArrayList<PropertyField> ownedFields = new ArrayList<PropertyField>();
 		ArrayList<Field> allFields = Monopoly.getGameBoard().getAllFields();
 
 		for (Field field : allFields) {
 			if (field.getClass().getName() == "PropertyField") {
 				PropertyField propertyField = (PropertyField) field;
 				if (propertyField.getOwner() == this)
 					ownedFields.add(propertyField);
 			}
 		}
 
 		return ownedFields;
 	}
 
 	public void goToJail() {
 		this.isInJail = true;
 	}
 
 	public void getOutOfJail() {
 		this.isInJail = false;
 	}
 
 	public boolean isInJail() {
 		return this.isInJail;
 	}
 
 	public void addGetOutOfJailCard() {
 		numberGetOutOfJailCards++;
 	}
 
 	public boolean hasGetOutOfJailCard() {
 		return numberGetOutOfJailCards > 0;
 	}
 
 	public void removeGetOutOfJailCard() {
 		if (numberGetOutOfJailCards > 0)
 			numberGetOutOfJailCards--;
 	}
 
 	// TODO
 	private boolean USE_GET_OUT_OF_JAIL_CARD() {
 		return true;
 	}
 
 	public void makeMove() throws Exception {
 		boolean move = true;
 		if (isInJail()) {
 			if (hasGetOutOfJailCard() && USE_GET_OUT_OF_JAIL_CARD()) {
 				getOutOfJail();
 				removeGetOutOfJailCard();
 			} else
 				move = false;
 		}
 		if (move) {
 			int moveAmount = Monopoly.getGameBoard().getDice().roll();
 			move(moveAmount);
 		}
 	}
 
 	public void buyField() throws Exception {
 		PropertyField currentField = (PropertyField) Monopoly.getGameBoard()
 				.getField(this.position);
 		this.removeMoney(currentField.property.getPrice());
 		currentField.owner = this;
 	}
 
 	public void buyHouse() throws Exception {
 		PropertyField currentField = (PropertyField) Monopoly.getGameBoard()
 				.getField(this.position);
 		this.removeMoney(currentField.property.getHousePrice());
 		currentField.addHouse();
 	}
 
 	public void buyHotel() throws Exception {
 		PropertyField currentField = (PropertyField) Monopoly.getGameBoard()
 				.getField(this.position);
 		this.removeMoney(currentField.property.getHousePrice());
 		currentField.addHouse();
 	}
 
 }
