 import java.io.*;
 
 import javax.swing.JFileChooser;
 
 public class DataSaver
 {
 	private static JFileChooser fileChooser = new JFileChooser();
 	private static File file;
 	
 	//private static final String FILE = "pvp.jpg"; //it stands for "java programmed game"!!;
 	
 	public static void save(Game gamma)
 	{
 		// open the file chooser dialog, return if cancel is clicked
 		if(fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
 		{
 			return;
 		}
 		
 		file				= fileChooser.getSelectedFile();
 		
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		try
 		{
 			fos = new FileOutputStream(file.getPath());
 			oos = new ObjectOutputStream(fos);
 			oos.writeObject(gamma);
 			oos.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static Game load()
 	{
 		// open the file chooser dialog, return if cancel is clicked
 		if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
 		{
 			return null;
 		}
 		
 		file				= fileChooser.getSelectedFile();
 		
 		FileInputStream fos = null;
 		ObjectInputStream ois = null;
 		Game ret = null;
 		
 		try
 		{
 			fos = new FileInputStream(file.getPath());
 			ois = new ObjectInputStream(fos);
 			try
 			{
 				ret = (Game) ois.readObject();
 			}
 			catch(InvalidClassException e)
 			{
 				ParkViewProtector.error("Sorry, that save file is not compatible with " +
 						"this version of Park View Protector.");
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			ois.close();
 		}
 		catch(StreamCorruptedException e)
 		{
 			ParkViewProtector.error("That save file appears to be corrupt. Please ensure " +
 					"that it is a Park View Protector Java Programmed Game file.");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 }
