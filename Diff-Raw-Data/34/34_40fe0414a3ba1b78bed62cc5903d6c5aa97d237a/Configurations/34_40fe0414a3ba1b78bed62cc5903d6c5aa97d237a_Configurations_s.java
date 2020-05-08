 package config;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Configurations {
 	private static boolean loaded=false;
 	private static Map<String,String> configs = new HashMap<String,String>();
 	private static String[] defaults={"Username:Default User1","Should Log:True"};
 	
 	private static void load(){
 		try{
			File f = new File("configs.txt");
 			if(!f.exists()){
 				initFile(f);
 				}
 			
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line="";
 			while((line = br.readLine())!=null){
 				String[] temp=line.split(":");
 				if(temp.length==1)
 					configs.put(temp[0],null);
 				else if(temp.length>1)
 					configs.put(temp[0], temp[1]);
 				
 			}
 			loaded=true;
 			br.close();
 			
 		}
 		catch(Exception ex){
 			ex.printStackTrace();
 		}
 	}
 	
 	public static void initFile(File f){
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			for(String s : defaults)
 				bw.write(s+"\r\n");
 			bw.flush();
 			bw.close();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static String getAttribute(String attribute){
 		if(!loaded)
 			load();
 		if(!configs.containsKey(attribute))
 			return null;
 		return configs.get(attribute);
 	}
 	
 	public static void setAttribute(String attribute, String newConfigeration){
 			configs.put(attribute, newConfigeration);
 			saveFile();
 		
 	}
 	
 	private static void saveFile(){
 		try {
 			File f = new File("configs");
 			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
 			for(String s : configs.keySet())
				bw.write(s+":"+configs.get(s));
 			
 			bw.flush();
 			bw.close();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 
 
 }
