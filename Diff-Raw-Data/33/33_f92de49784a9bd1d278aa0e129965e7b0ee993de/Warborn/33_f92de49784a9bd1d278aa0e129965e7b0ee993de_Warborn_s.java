 package warborn.model;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Random;
 
 import warborn.SupportClasses.MapData;
 import warborn.model.spells.Spell;
 
 
 public class Warborn extends Observable{
 
 	private ArrayList<Player> players;
 	private Territory[] territories;
 	private Move move;
 	private Battle battle;
 	private int selectedMap = 0, currentPlayer = 0, selectedTerritory = -1, nbrOfReinforcements = 0;
 	private int state = -1, phase = 0, startPhases;
 	private Dimension dimension;
 	private CardDeck deck;
 	private Spell selectedSpell;
 	private ArrayList<Spell> activeSpells;
 
 
 	public Warborn (){
 		this.deck = new CardDeck();
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		dimension = kit.getScreenSize();
 		this.players = new ArrayList<Player>();
 		this.activeSpells = new ArrayList<Spell>();
 		
 		//Going to make random players instead
 		addPlayer("Player 1", Color.blue, 0, 0);
 		addPlayer("Player 2", Color.red, 0, 0);
 		/*try {
 			this.territories = TerritoryFactory.getTerritories("Gothenburg");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		for(int i = 0; i<territories.length; i++){
 			territories[i].setNbrOfUnits(4);
 		}
 		*/
 		this.battle = new Battle(this);
 		this.move = new Move(this);
 		
 	}
 
 
 	//Getters
 
 	public Player[] getPlayers(){
 		return (Player[])players.toArray();
 	}
 	
 	public Player getPlayer(int index){
 		return players.get(index);
 	}
 	
 	public int getNumberOfPlayers(){
 		return players.size();
 	}
 
 	public Territory[] getTerritories(){
 		return territories;
 	}
 	
 	public int getMapIndex(){
 		return selectedMap;
 	}
 
 	/**
 	 * Retruns the current state of the model
 	 * @return 0 - if currently the program state is in <code>Menu</code><br>
 	 * 1 - if currently the program state is in <code>Unit Placement</code><br>
 	 * 2 - if currently the program state is in <code>Attack</code><br>
 	 * 3 - if currently the program state is in <code>Move</code>
 	 */
 	public int getState(){
 		return state;
 	}
 
 	/**
 	 * Retruns the current phase of the model
 	 * @return 0 - if currently the program phase is <code>normal</code><br>
 	 * 1 - if currently the program state is <code>action</code>
 	 */
 	public int getPhase(){
 		return phase;
 	}
 
 
 	public Player getCurrentPlayer(){
 		return players.get(currentPlayer);
 	}
 
 	public int getSelectedTerritoryIndex(){
 		return selectedTerritory;
 	}
 
 	public Move getMove(){
 		return move;
 	}
 
 	public Battle getBattle(){
 		return battle;
 	}
 
 	public int getWidth(){
 		return dimension.width;
 	}
 
 	public int getHeight(){
 		return dimension.height;
 	}
 
 	public Territory getTerritory(int i){
 		return territories[i];
 	}
 	
 	public Territory getSelectedTerritory(){
 		return territories[getSelectedTerritoryIndex()];
 	}
 	
 	public int getNbrOfReinforcements(){
 		return nbrOfReinforcements;
 	}
 
 
 	//Setters:
 
 	public void setPlayers(String[] names, Color[] colors, int[] race, int[] background){
 		players.clear();
 		for(int i = 0; i < names.length; i++){
 			players.add(new Player(names[i], i, colors[i], race[i], background[i]));
 		}
 		changed();
 	}
 
 
 	public void setSelectedMap(int id){
 		this.selectedMap = id;
 		changed();
 	}
 
 	public void setSelectedTerritory(int id){
 		System.out.println(selectedTerritory + ": " + id);
 		if (selectedTerritory != id){
			
 			if(selectedSpell != null){
 				selectedTerritory = id;
 				invokeSpell(selectedSpell);
 			}else if(state == 1 && players.get(currentPlayer) == territories[id].getOwner() && nbrOfReinforcements > 0){
 				territories[id].incrementUnit();
 				nbrOfReinforcements--;
 			}else if(state != 1){
 				if(selectedTerritory == -1 && players.get(currentPlayer) != territories[id].getOwner()){
 					selectedTerritory = id;
 					
 				}else if((selectedTerritory == -1 || territories[selectedTerritory].getOwner() != players.get(currentPlayer)) &&
 											players.get(currentPlayer) == territories[id].getOwner()){
 					battle.add(territories[id]);
 					move.add(territories[id]);
 					selectedTerritory = id;
 					
 				}else if(state == 2 && attackCompatible(territories[selectedTerritory], territories[id])){
 					battle.add(territories[id]);
 					selectedTerritory = -1;
 					nextPhase();
 					
 				}else if(state == 3 && moveCompatible(territories[selectedTerritory], territories[id])){
 					move.add(territories[id]);
 					selectedTerritory = -1;
 					nextPhase();
 				}
			}else if(state == 0 && players.get(currentPlayer) != territories[id].getOwner()){
				territories[id].incrementUnit();
				this.currentPlayer = (++this.currentPlayer)%players.size();
				startPhases--;
				if (startPhases<0){
					nextState();
				}
 			}
 		}else{
 			selectedTerritory = -1;
 			battle.resetTerritories();
 			move.resetTerritories();
 		}
 		changed();
 	}
 	
 	public void setSelectedSpell(Spell spell){
 		selectedSpell = spell;
 		if(spell.isInstant()){
 			invokeSpell(spell);
 		}
 	}
 
 	//End Setters
 
 	public void addPlayer(String name, Color color, int race, int background){
 		players.add(new Player(name, players.size(), color, race, background));
 		changed();
 	}
 
 	public void nextState(){
 		this.state++;
 		if(this.state > 3){
 			this.state = 1;
 			if(players.get(currentPlayer).hasConquered()){
 				players.get(currentPlayer).addCard(deck.drawCard());
 			}
 			this.currentPlayer = (++this.currentPlayer)%players.size();
 			players.get(currentPlayer).conquered(false);
 		}
 		if(this.state == 1){
 			nbrOfReinforcements = Math.max(players.get(currentPlayer).getNbrOfTerritories()/3, 3);
 			for(Territory terry : territories){
 				if(terry.getOwner() == players.get(currentPlayer)){
 					terry.setProtected(false);
 				}
 			}
 			for(int i = 0; i < activeSpells.size(); i++){
 				activeSpells.get(i).tick(this);
 				if(activeSpells.get(i).getTimer() <= 0){
 					activeSpells.remove(i);
 				}
 			}
 		}else if(this.state == 0){
			System.out.println("First Next State");
 			startPhases--;
 		}
 		changed();
 	}
 
 	public void nextPhase(){
 		this.phase = (this.phase+1)%2;
 		changed();
 	}
 
 	public void removePlayer(){
 		removePlayer(players.size()-1);
 		changed();
 	}
 
 	public void removePlayer(int id){
 		players.remove(id);
 		changed();
 	}
 
 	public void startGame(){
 		if(this.state != -1){
 			return;
 		}
 		
 		try {
 			String[] mapName = MapData.getMapNames();
 			territories = TerritoryFactory.getTerritories(mapName[selectedMap]);
 		} catch (IOException e) {
 			System.out.println("Selected Map does not exist!");
 		}
 		Random rand = new Random();
 		int sum = 0;
 		int player = 0;
 		while(sum < territories.length){
 			int i = rand.nextInt(territories.length);
 			if(territories[i].getOwner() == null){
 				players.get(player).addTerritory(territories[i]);
 				territories[i].setNbrOfUnits(1);
 				sum++;
 				player = (++player)%players.size();
 			}
 		}
 		this.startPhases = 10*this.getNumberOfPlayers();
 		this.phase = 0;
 		nextState();
 	}
 
 	public boolean attackCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() == t2.getOwner().getID() || t1.getNbrOfUnits() < 2 || t2.isProtected()){
 			return false;
 		}
 		return true;
 	}
 
 	public boolean moveCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() != t2.getOwner().getID() || (t1.getNbrOfUnits() < 2 && t2.getNbrOfUnits() < 2)){
 			return false;
 		}
 		return true;
 	}
 	
 	public void exchangeSouls(){
 		int[] nbrOfCards = new int[CardDeck.getMaxValue()];
 		for (int i=0; i<players.get(currentPlayer).getNumberOfCards(); i++){
 			nbrOfCards[players.get(currentPlayer).getCards()[i].getIndex()]++;
 		}
 		if (nbrOfCards[0]!=0 && nbrOfCards[1]!=0 && nbrOfCards[2]!=0){
 			players.get(currentPlayer).changeMana(10);
 			players.get(currentPlayer).removeCard(0);
 			players.get(currentPlayer).removeCard(1);
 			players.get(currentPlayer).removeCard(2);
 		}else if (nbrOfCards[2]>2){
 			players.get(currentPlayer).changeMana(8);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(2);
 			}
 		}else if (nbrOfCards[1]>2){
 			players.get(currentPlayer).changeMana(6);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(1);
 			}
 		}else{
 			players.get(currentPlayer).changeMana(4);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(0);
 			}
 		}
 		this.changed();
 	}
 	
 	public void invokeSpell(Spell spell){
 		if(spell.validTarget(this) && players.get(currentPlayer).getMana() >= spell.getManaCost()){
 			spell.invoke(this);
 			if(spell.getTimer() > 0){
 				activeSpells.add(spell);
 			}
 		}
 		selectedTerritory = -1;
 		selectedSpell = null;
 		changed();
 	}
 
 
 	protected void changed() {
 		setChanged();
 		notifyObservers();
 	}
 
 }
