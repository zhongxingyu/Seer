 package entanglement.utils;
 
 import java.io.*;
 
 public class Config {
 	
 	
 	public static void debug(String message) {
 		if(DEBUG)
 			System.out.println(message);
 	}
 	
 	public static Config getInstance() {
 		if(instance == null)
 			instance = new Config();
 		return instance;
 	}
 	
 	// should validate the configuration file
 	public boolean load(Reader fileReader) {
 		String oContent = ""; 
 		try 
 		{
 			String str;
 			BufferedReader bR = new BufferedReader(fileReader);
 			while ((str = bR.readLine()) != null)
 				oContent += str + ":";
 			bR.close();
 		}
 		catch (IOException e) 
 		{
 			return false;
 		}
 		confLines = oContent.substring(0, oContent.length()-2).split(":");
 		loadTilesConf();
 		return true;
 	}
 	
 	private void loadTilesConf(){
 		int count = tileTypesCount();
 		String[] tileConf;
 		for(int i = 0;i < count;i++)
 		{
 			tileConf = confLines[3 + i].split(" ");
 			tilesConf[i] = new int[tileConf.length];
 			for (int j = 0;j < tileConf.length;j++)
 				tilesConf[i][j] = Integer.parseInt(tileConf[j]);
 		}
 	}
 	
 	public int numberOfSides(){
 		return Integer.parseInt(confLines[0]);
 	}
 	
 	public int opeingsPerSide(){
 		return Integer.parseInt(confLines[1]);
 	}
 	
 	public int tileTypesCount(){
 		return Integer.parseInt(confLines[2]);
 	}
 	
 	public int[] tileConf(int conf){
 		return tilesConf[conf];
 	}
 	
 	public int boardWidth(){
 		return Integer.parseInt(confLines[3 + tileTypesCount()]);
 	}
 	
 	public int boardHeight(){
 		return Integer.parseInt(confLines[4 + tileTypesCount()]);
 	}
 	
 	public int startLocation(){
 		return Integer.parseInt(confLines[5 + tileTypesCount()]);
 	}
 	
 	public int playersCount(){
 		return Integer.parseInt(confLines[6 + tileTypesCount()]);
 	}
 	
 	
 	public final static boolean DEBUG = true;
 	
 	private String[] confLines 		= null;
 	private int[][] tilesConf 		= null;
 	private static Config instance 	= null;
 }
