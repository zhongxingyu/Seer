 package iogame;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import exceptions.GameIOException;
 import game.Game;
 
 public class IOSerializer {
 
 	/**
 	 * Creates a new game from a saved game file
 	 * 
 	 * @param f
 	 * @return Game
 	 * @throws InvalidLoadedFileException
 	 */
 	public static Game load(File f) throws GameIOException {
 		ObjectInputStream file = null;
 		try {
 			file = new ObjectInputStream(new BufferedInputStream(
 					new FileInputStream(f)));
 			return (Game) file.readObject();
 
 		} catch (Exception e) {
 			throw new GameIOException();
 		} finally {
 			try {
 				if (file != null) {
 					file.close();
					System.out.println("Archivo cargado");
 
 				}
 			} catch (IOException e) {
 				throw new GameIOException();
 			}
 		}
 	}
 
 	/**
 	 * Saves the game in the specified file
 	 * 
 	 * @param f
 	 *            The file to save the game into
 	 * @throws IOException
 	 */
 	public static void save(Game game, File f) throws GameIOException {
 		ObjectOutputStream file = null;
 		try {
 			file = new ObjectOutputStream(new BufferedOutputStream(
 					new FileOutputStream(f)));
 			file.writeObject(game);
 
 		} catch (Exception e) {
			System.out.println(e.getClass());

 			throw new GameIOException();
 		} finally {
 			try {
 				if (file != null) {
 					file.close();
					System.out.println("Archivo guardado en " + f.getName());
 				}
 			} catch (IOException e) {
 				throw new GameIOException();
 			}
 		}
 
 	}
 }
