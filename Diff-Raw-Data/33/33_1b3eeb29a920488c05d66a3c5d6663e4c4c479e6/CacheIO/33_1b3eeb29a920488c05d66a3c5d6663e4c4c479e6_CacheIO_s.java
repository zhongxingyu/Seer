 package com.earlofmarch.reach.model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import com.earlofmarch.reach.gui.Main;
 
 public class CacheIO {
 	
 	public static void saveOptions(){
 		File f = new File(PlayerIO.ROOT + "/cache/options.opts");		
 		f.delete();
		System.out.println("Saving mp as "+Main.mpTheme);
 		PrintWriter out;
 		try {
 			out = new PrintWriter(new BufferedWriter(new FileWriter(f)));		
 			out.println(Main.failSound);
 			out.println(Main.successSound);
 			out.println(Main.timerSound);
 			out.println(Main.autoUp);
 			out.println(Main.debug);
 			out.println(Main.mpTheme);
 			out.close();	
 		} catch (FileNotFoundException e){
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void loadOptions(){
 		if(!new File(PlayerIO.ROOT + "/cache/options.opts").exists())
 			return;
 		System.out.println("Loading Options");
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(PlayerIO.ROOT + "/cache/options.opts"));
 			Main.failSound = in.readLine().equals("true")?true:false;
 			Main.successSound = in.readLine().equals("true")?true:false;
 			Main.timerSound = in.readLine().equals("true")?true:false;
 			Main.autoUp = in.readLine().equals("true")?true:false;		
 			Main.debug = in.readLine().equals("true")?true:false;	
 			Main.mpTheme = in.readLine().equals("true")?true:false;
			System.out.println("mp:"+Main.mpTheme);
 			in.close();
 		}catch(FileNotFoundException e){			
 		}catch (IOException e) {
 		}catch (NullPointerException e){
 			System.out.println("File Corrupt -- Deleting");
 			new File(PlayerIO.ROOT+"/cache/options.opt").delete();
 		}
 	}
 
 }
