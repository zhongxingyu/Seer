 package server;
 
 import client.GsonConverter;
 import client.Packager;
 import game.*;
 
 public class GameManager {
 	
 	private ServerControl controller;
 	private Game game;
 	
 	protected GameManager(ServerControl controller) {
 		
 		this.controller = controller;
 		this.game = new Game();
 		
 		// Add the default functions (aka. Paths) to the game
 		
 		Function ping = new Function("ping", game, "Takes the Java System.currentTimeMillis() minus the time given in arg[0] and returns it") {
 			@Override
 			public Object run(Object args) {
 				double time = (Double) ((Object[]) args)[0];
 				return new String[] {"pong", "" + (System.currentTimeMillis() - time)};
 			}
 		};
 		
 		Function getPaths = new Function("getPaths", game, "Returns all of the valid paths that the server handles") {
 			@Override
 			public Object run(Object args) {
 				return getPaths();
 			}
 		};
		
		Function help = new Function("help", game, "Returns the descrption of the specified path in arg[0]") {
			@Override
			public Object run(Object args){
				System.out.println("the /help function is running!");
				String requestedPath = (String) Packager.getArgs((Object[]) args);
				System.out.println("reqested Path: " + requestedPath);
				String desc = (game.findFunction(requestedPath)).desc;
				System.out.println("description: " + desc);
				return desc;
			}
		};
 	}
 	
 	/**
 	 * Receives the path and calls the correct function 
 	 * from the game if the path is correct
 	 * 
 	 * @param path The path used to find the function
 	 * 
 	 * @param args The gson equivalent of the packaged arguments
 	 * 
 	 * @return String Returns the packaged Object[] as a gson String
 	 */
 	public String callFunction(String path, String args) {
 		
 		Object[] packRecieve = Packager.toStandardForm(path, GsonConverter.gsonToObjectArray(args));
 		
 		Object returnVal = game.runFunction(Packager.getPath(packRecieve), Packager.getArgs(packRecieve));
 		
 		Object[] packSend = Packager.toStandardForm(Packager.getPath(packRecieve), game.findFunction(Packager.getPath(packRecieve)).desc, returnVal, null);
 		
 		return GsonConverter.objectArrayToGson(packSend);
 	}
 	
 	/**
 	 * Returns the game's paths as a gson String
 	 * 
 	 * @return String
 	 */
 	public String getGsonPaths() {
 		
 		return GsonConverter.stringArrayToGson(getPaths());
 	}
 	
 	/**
 	 * Finds all the paths available in the game's function
 	 * 
 	 * @return String[] Returns the paths found
 	 */
 	public String[] getPaths() {
 		
 		String[] paths = new String[game.functions.size()];
 		
 		for(int i = 0; i < game.functions.size(); i++) {
 			paths[i] = "/" + game.functions.get(i).name;
 		}
 		
 		return paths;
 	}
 	
 	/**
 	 * Starts the game
 	 */
 	public void startGame() {
 		
 		game.startGame();
 	}
 }
