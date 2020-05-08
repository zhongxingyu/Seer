 package controllers;
 
 import java.util.List;
 
 import models.Game;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Query;
 
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 
 public class Application extends Controller {
 
 	public static Result index() {
 
 		if (request().accepts("text/html")) {
 			return ok(index.render());
 		} else {
 			return ok(Json.toJson(Game.find.all()));
 		}
 	}
 
 	/*
 	 * Show games
 	 * 
 	 * Takes a player name
 	 * Returns all games of that player
 	 * 
 	 * GET: /game/:username
 	 */
 	public static Result show(String player) {

 		List<Game> games = Game.find.where()
 				.eq("player", player)
 				.findList();

 		if (games.size() == 0) {
 			return ok(Json.toJson(null));
 		}
 		return ok(Json.toJson(games));
 	}
 
 	/*
 	 * Create a new game
 	 * 
 	 * POST: /game
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result create() {
 		System.out.println(request());
 		JsonNode json = request().body().asJson();
 		ObjectNode result = Json.newObject();
 
 		if (json == null) {
 			System.out.println("json er null :o");
 			result.put("status", "Couldn't create game");
 			result.put("message", "Could not parse contents of request body");
 			return badRequest(result);
 		}
 
 		Game game = Json.fromJson(json, Game.class);
		System.out.println("Player: " + game.getPlayer() + " Opponent: "
				+ game.getOpponent() + " Tiles played: "
				+ game.getTilesPlayed());
 		game.save();
 		return ok(Json.toJson(game));
 	}
 
 	/*
 	 * Update an existing game
 	 * 
	 * PUT: /game/:id
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
	public static Result update(Long id) {
 		JsonNode json = request().body().asJson();
 
 		if (json == null) {
 			System.out.println("json er null :o");
 			ObjectNode result = Json.newObject();
 			result.put("status", "Could not update game.");
 			result.put("melding", "Could not parse contents of request body.");
 
 			return badRequest(result);
 		}
 
		Game game = Game.find.byId(id);
 
 		Game updatedGame = Json.fromJson(json, Game.class);
 
 		updatedGame.setId(id);
 		game.update(updatedGame);
 		game.save();
 
		System.out.println("id: " + id + " opponent: " + game.getOpponent()
 				+ " tilesPlayed: " + game.getTilesPlayed());
 
 		return ok(Json.toJson(game));
 	}
 
 	/*
 	 * Remove tile played from game.
 	 * 
 	 * PUT: /game/:id/removeTile
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result removeTile(Long id) {
 		JsonNode json = request().body().asJson();
 
 		if (json == null) {
 			System.out.println("json er null :o");
 			ObjectNode result = Json.newObject();
 			result.put("status", "Could not update game.");
 			result.put("melding", "Could not parse contents of request body.");
 
 			return badRequest(result);
 		}
 
 		Game game = Game.find.byId(id);
 
 		Game updatedGame = Json.fromJson(json, Game.class);
 		updatedGame.setId(id);
 		game.update(updatedGame);
 		game.save();
 
 		System.out.println("id: " + id + " opponent: " + game.getOpponent()
 				+ " tilesPlayed: " + game.getTilesPlayed());
 
 		return ok(Json.toJson(game));
 	}
 
 	/*
 	 * TODO: gjøre ferdig
 	 * Delete game
 	 * 
 	 * DELETE: /game/:id
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result delete(Long id) {
 
 		Game game = Game.find.byId(id);
 
 		ObjectNode result = Json.newObject();
 		if (id == null) {
 			result.put("status", "KO");
 			result.put("message", "Missing parameter [id]");
 			return badRequest(result);
 		} else if (game == null) {
 			result.put("status", "KO");
 			result.put("message", "Missing parameter [id]");
 			return badRequest(result);
 		} else {
 			game.delete();
 			result.put("status", "OK");
 			result.put("message", "Game was deleted");
 			return ok(result);
 		}
 	}
 
 	/*
 	 * Count games GET: /numberOfGames
 	 */
 	public static Result count() {
 		
 		return ok(Json.toJson(Game.find.all().size()));
 	}
 	
 	
 	/*
 	 * TODO: lag funksjon som henter gamene som er knyttet til en bestemt bruker
 	 * kanskje en funksjon som finner alle game.id som er knyttet til en bruker 
 	 * og deretter kall til den vanlige get game for hver av dem.
 	 * 
 	 */
 	public static Result getGames(String player) {
 		return ok(Json.toJson(Game.find.where().eq("player", player)));
 	}
 
 }
