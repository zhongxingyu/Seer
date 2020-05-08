 package swampbox.filemgmt;
 
 import java.net.*;
 import java.io.*;
 import java.util.ArrayList;
 
 public class GetModList {
 	public static void main(String[] args) throws Exception{
 		String modIndex = "mods.txt";
 		String coremodIndex = "coremods.txt";
 		String configIndex = "config.txt";
 
 		String[][] mods = getURLs(modIndex,"mods");
 		String[][] coremods = getURLs(coremodIndex,"coremods");
 		String[][] configs = getURLs(configIndex,"config");
 		
 		//patchFiles(mods,"mods");
 		//patchFiles(coremods,"coremods");
 		patchFiles(configs,"config");
 		
 	}
 	
 	public static String[][] getURLs(String list, String subdir) throws Exception{
 		URL packDir = new URL("https://googledrive.com/host/0B4J4lMoc2heLSVdnOGV6SWRiZ00/pack/");
 		URL listURL = new URL(packDir + list);
 		ArrayList<String> listURLs = new ArrayList<String>();
 		ArrayList<String> listNames = new ArrayList<String>();
 		BufferedReader in = new BufferedReader(new InputStreamReader(listURL.openStream()));
 		
 		String inputLine;
 		while((inputLine = in.readLine()) != null){
 			listURLs.add(packDir + subdir + "/" + inputLine);
 			listNames.add(inputLine);
 		}
 		in.close();
 		
 		String[] listArray = new String[listURLs.size()];
 		listURLs.toArray(listArray);
 		
 		String[] listNameArray = new String[listNames.size()];
 		listNames.toArray(listNameArray);
 		
 		String [][] arrayUrlAndName = new String[listArray.length][2];
 		
 		for(int i = 0; i < listArray.length; i++){
 			arrayUrlAndName[i][0] = listArray[i];
 			arrayUrlAndName[i][1] = listNameArray[i];
 		}
 
 		return arrayUrlAndName;
 	}
 	
 	public static void patchFiles(String[][] fileList, String subdir) throws Exception{
 		byte[] buffer = new byte[1024];
 		int bytesRead;
 		
 		String installLoc = "C:/Users/Jeff/Documents/test/";
 		
		File dirBuilder = new File(installLoc + subdir);
		dirBuilder.mkdirs();
 		
 		for(int i = 0; i < fileList.length; i++){
 			URL path = new URL(fileList[i][0]);
 			BufferedInputStream inputStream = null;
 			BufferedOutputStream outputStream = null;
 			URLConnection connection = path.openConnection();
 			inputStream = new BufferedInputStream(connection.getInputStream());
 			File f = new File(installLoc + subdir + "/" + fileList[i][1]);
 			if(f.exists()){
 				System.out.println("(" + (i+1) + "/" + fileList.length + ") Found: " + fileList[i][1] + ", skipping...");
 				continue;
 			}
 			outputStream = new BufferedOutputStream(new FileOutputStream(f));
 			while((bytesRead = inputStream.read(buffer)) != -1){
 				outputStream.write(buffer, 0, bytesRead);
 			}
 			inputStream.close();
 			outputStream.close();
 			System.out.println("(" + (i+1) + "/" + fileList.length + ") Downloaded: " + fileList[i][1]);
 		}
 		System.out.println("\nDownload Complete: " + subdir + "\n");
 	}
 }
