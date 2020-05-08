 package game;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.List;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Vector;
 import td.Chance;
 
 public class GameState
 {
 	List<Country> countries;
 
 	int numberOfPlayers;
 
 	public GameState(List<Country> countries)
 	{
 		this.countries = countries;
 
 		this.numberOfPlayers = getPlayers().size();
 	}
 
 	public GameState(GameState other)
 	{
 		countries = new Vector<Country>();
 
 		numberOfPlayers = other.numberOfPlayers;
 
 		// Copy all the countries
 		for (Country country : other.countries)
 			countries.add(new Country(country));
 
 		// Update neighbours to direct to the countries in the current state
 		for (Country country : countries)
 			for (int i = 0; i < country.neighbours.size(); ++i)
 				country.neighbours.set(i, getCountry(country.neighbours.get(i)));
 	}
 
 	/**
 	 * Make a move, and generate the resulting state. If expected is true, it will
 	 * calculate the most likely outcome. Otherwise, it will actually play the game
 	 * with dice and everything.
 	 */
 	public GameState apply(Move move, Boolean expected)
 	{
 		// Clone the current gamestate.
 		GameState state = new GameState(this);
 
 		// If this is a non-move move, there is nothing to add :)
 		if (move.isEndOfTurn())
 			return state;
 
         int attackingEyes;
         int defendingEyes;
 
         // If we calculate the expected value, don't add the element of chance.
         if (expected)
         {
             attackingEyes = move.attackingCountry.getDice();
 		    defendingEyes = move.defendingCountry.getDice();
         }
         // Otherwise, let's roll those dice.
         else
         {
 		    attackingEyes = rollDice(move.attackingCountry.getDice());
 		    defendingEyes = rollDice(move.defendingCountry.getDice());
         }
 
 		// Attacker wins
 		if (attackingEyes > defendingEyes)
 		{
 			// Take the country!
 			state.getCountry(move.defendingCountry).setPlayer(move.attackingCountry.getPlayer());
 
 			// Reset attacking country dice to 1 (as the army has moved to the defending country)
 			state.getCountry(move.attackingCountry).setDice(1);
 
 			// Assign remaining dice to country
 			state.getCountry(move.defendingCountry).setDice(remainingDice(attackingEyes - defendingEyes));
 		}
 
 		// It's a draw
 		else if (attackingEyes == defendingEyes)
 		{
 			state.getCountry(move.attackingCountry).setDice(1);
 
 			state.getCountry(move.defendingCountry).setDice(1);
 		}
 
 		// Attacker loses
 		else
 		{
 			state.getCountry(move.attackingCountry).setDice(1);
 
 			state.getCountry(move.defendingCountry).setDice(remainingDice(defendingEyes - attackingEyes));
 		}
 
 		return state;
 	}
 
     public GameState expectedState(Move move, int i)
     {
         // Clone the current gamestate.
 		GameState state = new GameState(this);
         int attackingEyes = move.attackingCountry.getDice();
 	    int defendingEyes = move.defendingCountry.getDice();
     
         // Attacker wins
         if (i == 1)
         {
             // Take the country!
 			state.getCountry(move.defendingCountry).setPlayer(move.attackingCountry.getPlayer());
 
 			// Reset attacking country dice to 1 (as the army has moved to the defending country)
 			state.getCountry(move.attackingCountry).setDice(1);
 
 			// Assign remaining dice to country
			state.getCountry(move.defendingCountry).setDice(remainingDice(Chance.diceRemainingAttacker(attackingEyes, defendingEyes)));
         }
         
         // It's a draw
 		else if (i == 2)
 		{
 			state.getCountry(move.attackingCountry).setDice(1);
 
 			state.getCountry(move.defendingCountry).setDice(1);
 		}   
 
         // Attacker loses
 		else if (i == 3)
 		{
 			state.getCountry(move.attackingCountry).setDice(1);
 
			state.getCountry(move.defendingCountry).setDice(remainingDice(Chance.diceRemainingDefender(attackingEyes, defendingEyes)));
 		}   
       
         return state;
     }
 
 	public List<Move> generatePossibleMoves(Player player)
 	{
 		List<Move> moves = new Vector<Move>();
 
 		// The end-of-turn move
 		moves.add(new Move());
 
 		for (Country country : countries)
 		{
 			// Is this a country of the player who's turn it is?
 			if (!country.getPlayer().equals(player))
 				continue;
 			
 			// Does the country have enough dice to attack?
 			if (country.getDice() <= 1)
 				continue;
 			
 			List<Country> enemyNeighbours = country.enemyNeighbours();
 
 			// Does the country have any enemy neighbours?
 			if (enemyNeighbours.size() == 0)
 				continue;
 			
 			// If yes, then add a move for each enemy neighbour it can attack.
 			for (Country enemyCountry : enemyNeighbours)
 				moves.add(new Move(country, enemyCountry));
 		}
 
 		return moves;
 	}
 
 	public Country getCountry(Country country)
 	{
 		return countries.get(countries.indexOf(country));
 	}
 
 	public List<Country> getCountries()
 	{
 		return countries;
 	}
 
 	public List<Country> getCountries(Player player)
 	{
 		List<Country> playerCountries = new Vector<Country>();
 
 		for (Country country : countries)
 			if (country.getPlayer().equals(player))
 				playerCountries.add(country);
 
 		return playerCountries;
 	}
 
 	public Set<Player> getPlayers()
 	{
 		Set<Player> players = new HashSet<Player>();
 
 		for (Country country : countries)
 			players.add(country.getPlayer());
 
 		return players;
 	}
 
 	public int getNumberOfPlayers()
 	{
 		return numberOfPlayers;
 	}
 
 	public String toString()
 	{
 		String out = "[GameState countries:\n";
 
 		for (Country country : countries)
 			out += "\t" + country + "\n";
 
 		out += "]";
 
 		return out;
 	}
 
 	public boolean isWonBy(Player player)
 	{
 		Set<Player> players = getPlayers();
 
 		return players.size() == 1 && players.contains(player);
 	}
 
 	public boolean isFinished()
 	{
 		return getPlayers().size() == 1;
 	}
 
 	private int rollDice(int dice)
 	{
 		Random random = new Random();
 
 		int eyes = 0;
 
 		for (int i = 0; i < dice; ++i)
 			eyes += 1 + random.nextInt(6);
 
 		return eyes;
 	}
 
 	private int remainingDice(int eyes)
 	{
 		return (int) Math.max(1,Math.round(eyes / 6.0));
 	}
 }
