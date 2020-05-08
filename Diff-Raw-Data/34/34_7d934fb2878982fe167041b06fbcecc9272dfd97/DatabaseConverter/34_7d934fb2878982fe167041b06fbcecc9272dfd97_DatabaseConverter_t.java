 package com.example.wecharades.presenter;
 
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Invitation;
 import com.example.wecharades.model.Player;
 import com.example.wecharades.model.Turn;
 import com.parse.ParseObject;
 
 public class DatabaseConverter {
 
 	//A protected method to parse a ParseObject to a game
 	protected static Game parseGame(Database db, ParseObject game) throws DatabaseException{
 		if(game.getClassName().equals("Game")){
 			//Save player locally to avoid multiple fetches. //TODO have a local list of players??
 			Player p1 = db.getPlayerById(game.getString("player1"));
 			Player p2 = db.getPlayerById(game.getString("player2"));
 			Player current = (game.getString("currentPlayer").equals(p1.getParseId())) ? p1 : p2 ;
 			return new Game(
 					game.getObjectId(),
 					db.getPlayerById(game.getString("player1")),
 					db.getPlayerById(game.getString("player2")),
 					current,
 					game.getInt("turn"), 
 					game.getBoolean("finished"), 
 					game.getUpdatedAt());
 		} else{
 			return null;
 		}
 	}
 
 	//A method to parse ParseObject turns into turns
 	protected static Turn parseTurn(Database db, ParseObject turn) throws DatabaseException{
 		if(turn.getClassName().equals("Turn")){
 			return new Turn(
					turn.getParseObject("game").getObjectId(),
 					turn.getInt("turn"), 
 					turn.getInt("state"), 
 					turn.getString("word"), 
 					turn.getString("videoLink"), 
 					db.getPlayerById(turn.getString("recPlayer")),
 					turn.getInt("recPlayerScore"),
 					db.getPlayerById(turn.getString("ansPlayer")), 
 					turn.getInt("ansPlayerScore") 
 					);
 		} else{
 			return null;
 		}
 	}
 
 	//A method to parse ParseObject players to players
 	protected static Player parsePlayer(ParseObject player){
 		if(player != null && player.getClassName().equals("_User")) {
 			return new Player(
 					player.getObjectId(), 
 					player.getString("naturalUsername"), 
 					player.getInt("globalScore"));
 		} else{
 			return null;
 		}
 	}
 
 	protected static Invitation parseInvitation(Database db, ParseObject invitation) throws DatabaseException{
 		if(invitation.getClassName().equals("Invite")){
 			return new Invitation(db.getPlayerById(invitation.getString("inviter")), db.getPlayerById(invitation.getString("invitee")), invitation.getCreatedAt());
 		} else{
 			return null;
 		}
 	}
 
 }
