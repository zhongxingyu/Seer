 package kc.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 public class PhotoliciousUtils 
 {
	public void saveDefaultPrinter(String defaultPrinter) {
 		 try {
 		        Properties props = new Properties();
 		        props.setProperty("defaultPrinter", defaultPrinter);
 		        File f = new File("../printer.properties");
 		        OutputStream out = new FileOutputStream( f );
 		        props.store(out, "Default Properties Saved");
 		    }
 		    catch (Exception e ) {
 		        e.printStackTrace();
 		    }
 	}
 	
 	public static String readDefaultPrinter()
 	{
 		String defaultPrinter = null;
 		try{
 			File f = new File("../printer.properties");
 			InputStream in = new FileInputStream(f);
 			Properties p = new Properties();
 			p.load(in);
 			defaultPrinter = p.getProperty("defaultPrinter");
 		}
 		catch (Exception e) {
 			 e.printStackTrace();
 		}
 		return defaultPrinter;
 	}
 	
 	
 	public static File[] filterJPEGImagesFromFolder (File[] folder)
 	{
 		List<File> files = new ArrayList<File>();
 		File[] listOfJPEGImages = null;
 		int i=0;
 		try{
 			for(File file:folder)
 			{
 				if(file.getName().endsWith(".jpg")||file.getName().endsWith(".JPG")
 						||file.getName().endsWith(".JPEG")||file.getName().endsWith(".jpeg"))
 				{
 					files.add(file);
 				}
 			}
 			listOfJPEGImages = new File[files.size()];
 			for(Object object:files)
 			{
 				listOfJPEGImages[i] = (File)object;
 				i++;
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 			
 		return listOfJPEGImages;
 	}
 	
 	
 	public static File[] filterImagesFromFolder (File[] folder)
 	{
 		List<File> files = new ArrayList<File>();
 		File[] listOfImages = null;
 		int i=0;
 		try{
 			for(File file:folder)
 			{
 				if(file.getName().endsWith(".jpg")||file.getName().endsWith(".jpeg")||file.getName().endsWith(".png")
 						|| file.getName().endsWith(".JPG")||file.getName().endsWith(".JPEG") || file.getName().endsWith(".PNG"))
 				{
 					files.add(file);
 				}
 			}
 			listOfImages = new File[files.size()];
 			for(Object object:files)
 			{
 				listOfImages[i] = (File)object;
 				i++;
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return listOfImages;
 	}
 	
 	public static List<String> nameOfFiles(File[] files)
 	{
 		List<String> name = new ArrayList<String>();
 		try{
 			for(File file:files)
 			{
 				name.add(file.getName());
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return name;
 	}
 	
 	public static void saveOutputFolder(String outputFolder) {
 		 try {
 		        Properties props = new Properties();
 		        props.setProperty("output", outputFolder);
 		        File f = new File("../outputFolder.properties");
 		        OutputStream out = new FileOutputStream( f );
 		        props.store(out, "Default Properties Saved");
 		    }
 		    catch (Exception e ) {
 		        e.printStackTrace();
 		    }
 	}
 	
 	
 	public static String readOutputFolder()
 	{
 		String outputFolder = null;
 		try{
 			File f = new File("../outputFolder.properties");
 			InputStream in = new FileInputStream(f);
 			Properties p = new Properties();
 			p.load(in);
 			outputFolder = p.getProperty("output");
 		}
 		catch (Exception e) {
 			 e.printStackTrace();
 		}
 		return outputFolder;
 	}
 	
 	public static void main(String args[]) {
 		//File file = new File("C:\\Users\\HOME\\Pictures\\Google Talk Received Images");
 		new PhotoliciousUtils().saveOutputFolder("C:\\Users\\HOME\\Pictures\\Google Talk Received Images");
 	}
 }
