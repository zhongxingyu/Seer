 package inputOutput;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 
 import model.GameModel;
 import model.geometrical.Position;
 import model.items.Item;
 import model.items.SupplyFactory;
 import model.items.weapons.Projectile;
 import model.items.weapons.Weapon;
 import model.items.weapons.WeaponFactory;
 import model.save.SavePath;
 import model.sprites.Enemy;
 import model.sprites.EnemyFactory;
 import model.sprites.Player;
 import model.sprites.Sprite;
 import model.world.World;
 import model.world.WorldBuilder;
 
 /**
  * Class which can save, load and create game models.
  * 
  * @author Martin Calleberg
  *
  */
 public class GameIO {
 	
 	/*Added between each value in a save string*/
 	private static final String DATA_DIVIDER = "#";
 	
 	/*All the identifiers for the different kinds of objects which can be saved*/
 	private static final char ENEMY = 'e';
 	private static final char PLAYER = 'p';
 	private static final char WEAPON = 'w';
 	private static final char PROJECTILE = 'P';
 	private static final char ITEM = 'i';
 	
 	/*The seed used when loading/creating a new model*/
 	private static long seed = Calendar.getInstance().getTimeInMillis();
 	
 	public static final String NEW_NAME = "newName";
 	
 	/**
 	 * Gives a fresh new game.
 	 * @return a new game.
 	 */
 	public static GameModel newGame() {
 		GameModel model = initBase(seed);
 		
 		Position pos = null;
 		do{
 			pos = new Position((int)(Math.random() * model.getWorld().getWidth()) +0.5f, 
 					(int)(Math.random() * model.getWorld().getHeight()) +0.5f);
 		}while(!model.getWorld().canMove(pos, new Position(pos.getX() + 1, pos.getY() + 1)));
 		
 		Player player = new Player(0, 0);
 		player.setCenter(pos);
 		player.switchWeapon(0);
 		player.pickUpWeapon(WeaponFactory.createWeapon(WeaponFactory.Type.PISTOL, WeaponFactory.Level.NORMAL));
 		player.switchWeapon(2);
 		player.pickUpWeapon(WeaponFactory.createWeapon(WeaponFactory.Type.BAT, WeaponFactory.Level.NORMAL));
 		player.switchWeapon(0);
 		model.setPlayer(player);
 
 		return model;
 	}
 	
 	/**
 	 * Does the basic setup for a new game.
 	 * @param seed the seed to use when creating the world.
 	 * @return a basic game model.
 	 */
 	private static GameModel initBase(long seed) {
 		WorldBuilder wb = new WorldBuilder(seed);
 		World w = new World(wb.getNewWorld(200, 200));
 		GameModel model = new GameModel(w);
 		model.setSpawns(wb.getSpawnPoints());
 		
 		return model;
 	}
 	
 	/**
 	 * Loads a saved game from the specified path.
 	 * @param path the path to the file.
 	 * @return a restored game model.
 	 */
 	public static GameModel loadGame(String path) {
 		try {
 			BufferedReader	reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "ISO-8859-1"));
 			
 			//Basic setup
 			String line;
 			seed = Long.parseLong(reader.readLine());
 			GameModel model = initBase(seed);
 			model.addScore(Integer.parseInt(reader.readLine()));
 			model.setGameTime(Long.parseLong(reader.readLine()));
 			
 			while ((line = reader.readLine()) != null) {
 				//Extracts the identifier
 				char id = line.charAt(0);
 				line = line.substring(2);
 				String[] data = line.split(DATA_DIVIDER);
 				
 				switch(id) {
 				case ENEMY:
 					Enemy e = EnemyFactory.createRestoredEnemy(data);
 					model.getWorld().addSprite(e);
 					
 					if ((line = reader.readLine()) != null){
 						if (line!=null){
 							line = line.substring(2);
 							data = line.split(DATA_DIVIDER);
 							e.setWeapon(WeaponFactory.loadWeapon(data));
 						}
 					}
 					break;
 				case PLAYER:
 					Player p = new Player(); 
 					model.setPlayer(p);
 					
 					for(int i = 0; i < p.getWeapons().length && ((line = reader.readLine()) != null); i++) {
 						if (line != null){
 							line = line.substring(2);
 							String[] data2 = line.split(DATA_DIVIDER);
 							p.switchWeapon(i);
 							p.pickUpWeapon(WeaponFactory.loadWeapon(data2));
 						}
 					}
 					p.restore(data);
 
 					break;
 				case PROJECTILE:
 					Projectile projectile = new Projectile();
 					projectile.restore(data);
 					model.getWorld().addProjectile(projectile);
 					break;
 				case ITEM:
 					model.getWorld().addItem(SupplyFactory.createRestoredSupply(data));
 					break;
 				case WEAPON:
 					Weapon w = WeaponFactory.loadWeapon(data);
 					model.getWorld().addItem(w);
 					break;
 				}
 			}
 
 			reader.close();
 			
 			return model;
 		} catch (IOException exc) {
 			System.out.println("Could not find " + path);
 			return newGame();
 		}
 	}
 
 	/**
 	 * Saves the specified model.
 	 * @param model the model to save.
 	 * @param path the path to the file to save to.
 	 */
 	public static void saveGame(GameModel model, String path) {
 		if(path.equals(NEW_NAME)) {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy mm dd - hh.mm.ss"); 
 			path = SavePath.getSaveFolder() + dt.format(Calendar.getInstance().getTime()) + ".save";
 		}else{
 			path = SavePath.getSaveFolder() + path;
 		}
 		
 		File f  = new File(path);
 		if(!f.exists()) {
 			try {
 				f.createNewFile();
 			} catch (IOException e) {
 				System.out.println("Could not create a new file!");
 				return;
 			}
 		}
 		try {
 			//Open the stream
 			FileOutputStream fos = new FileOutputStream(f);
 			OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1");
 
 			//Write data needed everytime
 			osw.write(seed + "\r\n");
 			osw.write(model.getScore() + "\r\n");
 			osw.write(model.getGameTime() + "\r\n");
 
 			//Writes all the sprites
 			for(Sprite s : model.getWorld().getSprites()) {
 				if(s instanceof Player) {
 					osw.write(convertToString(s.getData(), PLAYER) + "\r\n");
 					for(Weapon w : ((Player)s).getWeapons()) {
 						osw.write(convertToString(w.getData(), WEAPON) + "\r\n");
 					}
 				}else{
 					osw.write(convertToString(s.getData(), ENEMY) + "\r\n");
 					osw.write(convertToString(s.getActiveWeapon().getData(), WEAPON) + "\r\n");
 				}
 			}
 
 			//Writes all the projectiles
 			for(Projectile p : model.getWorld().getProjectiles()) {
 				osw.write(convertToString(p.getData(), PROJECTILE) + "\r\n");
 			}
 
 			//Writes all the items
 			for(Item i : model.getWorld().getItems()) {
 				if(i instanceof Weapon) {
 					osw.write(convertToString(i.getData(), WEAPON) + "\r\n");
 				}else{
 					osw.write(convertToString(i.getData(), ITEM) + "\r\n");
 				}
 			}
 
 			//Close all the streams 
 			osw.close();
 			fos.close();
 
 		} catch (IOException e) {
 			System.out.println("Could not save file!");
 		}
 	}
 
 	/**
 	 * Converts the specified array of data to a string which can be saved and restored later.
 	 * @param data the data to save.
 	 * @param identifier the identifier which indicates what the data is for.
 	 * @return a string which can be saved.
 	 */
 	private static String convertToString(String[] data, char identifier) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(identifier + "" + DATA_DIVIDER);
 		for(int i = 0; i < data.length; i++) {
 			sb.append(data[i]);
 			//No divider on last data value
 			if(i < data.length - 1) {
 				sb.append(DATA_DIVIDER);
 			}
 		}
 		return sb.toString();
 	}
 	
 }
