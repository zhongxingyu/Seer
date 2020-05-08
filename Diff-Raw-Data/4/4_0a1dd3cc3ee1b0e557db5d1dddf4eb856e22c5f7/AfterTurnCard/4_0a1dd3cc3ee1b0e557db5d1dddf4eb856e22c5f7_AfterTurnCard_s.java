 package cluedo.cards;
 
 import java.util.List;
 import java.util.Scanner;
 
 import cluedo.main.Game;
 import cluedo.structs.Player;
 //import cluedo.cards.*;
 
 public class AfterTurnCard extends Keepers {
 
 	private String description;
 
 	public AfterTurnCard(String s) {
 		if (!(s.equalsIgnoreCase(Keepers.Cards[0])
 				|| s.equalsIgnoreCase(Keepers.Cards[4]) || s
 					.equalsIgnoreCase(Keepers.Cards[7]))) {
 			throw new IllegalArgumentException(s + " is not an aproriate card");
 		}
 
 		description = s;
 	}
 
 	@Override
 	public String getDescription() {
 		// TODO Auto-generated method stub
 		return description;
 	}
 
 	@Override
 	public String toString() {
 		return description;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AfterTurnCard other = (AfterTurnCard) obj;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String apply(Game game, Player player) {
 
 		if (description.equals(Keepers.Cards[0])) {
 			// player on right show you card.
 			Card card = game.playerShowCard(game.getPlayerToPrevoiusPlayer()
 					.get(player));
 			System.out.println(card.toString());
 			player.getKeeperCards().remove(this);
 			return null;
 		}
 		if (description.equals(Keepers.Cards[4])) {
 			// move anyone back to their starting position
 			Character c = getCharacterInput();
 
 			// gets the above Character and moves back to their starting
 			// position
 			Player p = game.getPlayer(c.toString());
 			game.changePlayerLocation(p.getLocation(), p.getStarting());
 			player.getKeeperCards().remove(this);
 			return null;
 		}
 
 		if (description.equals(Keepers.Cards[7])) {
 			// take another turn
 
 			// TODO if player dies we need to kill them (i.e remove from turns list or whatever)
 			player.getKeeperCards().remove(this);
 			return game.takeTurn(player);
 
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Requests input for the player to move back to their start
 	 */
 	private Character getCharacterInput() {
 		System.out.println("Type the character name to move back to start");
 		Scanner sc = new Scanner(System.in);
 		String data = sc.nextLine();
 
 		try {
 			// Trim them for a little leeway on typing
 			Character c = new Character(data.trim());
 
 			return c;
 		} catch (IllegalArgumentException e) {
 			System.out.println("Not valid: " + e.getMessage());
		} finally {
			sc.close();
		}
 
 		return getCharacterInput();
 	}
 
 }
