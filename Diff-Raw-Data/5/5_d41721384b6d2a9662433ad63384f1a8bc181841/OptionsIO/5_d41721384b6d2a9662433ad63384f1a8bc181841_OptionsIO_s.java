 package se.chalmers.kangaroo.io;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.Scanner;
 
 /**
  * This is a class that will save your custom keys to a file. You will of course
  * be able to load them as well.
  * 
  * @author alburgh
  * @modifiedby simonal
  * 
  */
 public class OptionsIO {
 	private static OptionsIO instance;
 	private static int[] customKeys = new int[4];
 	private static final String KEYS_FILE_NAME = "resources/customkeys.txt";
 	private static final String SOUND_FILE_NAME = "resources/soundoptions.txt";
 
 	/* Private constructor, so only one instance will be created. */
 	private OptionsIO() {
 	}
 
 	/**
 	 * Returns the only instance of this class.
 	 * 
 	 * @return
 	 */
 	public static synchronized OptionsIO getInstance() {
 		if (instance == null)
 			instance = new OptionsIO();
 		return instance;
 	}
 
 	/**
 	 * Returns the current keys set in the file.
 	 * 
 	 * @return the key codes.
 	 */
 	public int[] getKeys() {
 		loadKeys();
 		return customKeys;
 	}
 
 	/**
 	 * Use the KeyEvent.keycodes to set your keys. You can then use getKeys to
 	 * return them.
 	 * 
 	 * @param key
 	 *            , the array of key codes.
 	 */
 	public void setKeys(int[] key) {
 		try {
 			Writer w = new FileWriter(KEYS_FILE_NAME);
 			for (int i = 0; i < key.length; i++) {
 				w.write(key[i] + "\n");
 			}
 			w.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("There is no such file");
 		} catch (IOException e) {
 			System.out.println("Something went wrong with the io writing");
 		}
 	}
 
 	/* Used to make sure that the latest version of the keys are returned. */
 	private static void loadKeys() {
 		try {
 			InputStream in = new FileInputStream(KEYS_FILE_NAME);
 			Scanner sc = new Scanner(in);
 			int i = 0;
 			while (sc.hasNext()) {
 				customKeys[i] = Integer.parseInt(sc.next());
 				i++;
 			}
 			sc.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("No such file exists");
 		} catch (IOException i) {
 			System.out.println("Problem closing the file");
 		}
 	}
 
 	/**
 	 * A method for setting the backgorund and soundeffets volumes.
 	 * 
 	 * @param bg
 	 *            , the background volume
 	 * @param sfx
 	 *            , the sound effects volume
 	 */
 	public void saveVolume(double bg, double sfx) {
 		try {
 			Writer w = new FileWriter(SOUND_FILE_NAME);
 			w.write(bg + "\n");
 			w.write(sfx + "\n");
 		} catch (FileNotFoundException e) {
 			System.out.println("There is no such file");
 		} catch (IOException e) {
 			System.out.println("Something went wrong with the io writing");
 		}
 	}
 
 	/**
 	 * A method that will return the background volume Will return -1 if the
 	 * value cannot be read.
 	 * 
 	 * @return a value between 0 and 1
 	 */
 	public double getBgVolume() {
 		double bg = -1;
 		try {
			InputStream in = new FileInputStream(KEYS_FILE_NAME);
 			Scanner sc = new Scanner(in);
 			bg = Double.parseDouble(sc.next());
 			sc.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("No such file exists");
 		} catch (IOException i) {
 			System.out.println("Problem closing the file");
 		}
 		return bg;
 	}
 
 	/**
 	 * A method that will return the sound effects volume Will return -1 if the
 	 * value cannot be read.
 	 * 
 	 * @return a value between 0 and 1
 	 */
 	public double getSfxVolume() {
 		double sfx = -1;
 		try {
			InputStream in = new FileInputStream(KEYS_FILE_NAME);
 			Scanner sc = new Scanner(in);
 			sc.next();
 			sfx = Double.parseDouble(sc.next());
 			sc.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("No such file exists");
 		} catch (IOException i) {
 			System.out.println("Problem closing the file");
 		}
 		return sfx;
 	}
 
 }
