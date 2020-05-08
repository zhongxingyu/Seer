 package fr.minepod.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class VersionsManager {
 	@SuppressWarnings("unused")
 	public VersionsManager() throws IOException, ParseException, InterruptedException {
 		ArrayList<DownloaderThread> arrayList = new ArrayList<DownloaderThread>();
 		JSONParser jsonParser = new JSONParser();
 		String launcherDataFile = Config.launcherLocation + Config.slash + "launcher_data.json";
 
 		new Downloader(new URL(Config.launcherDataUrl), launcherDataFile, false);
 		String dataFile = fr.minepod.utils.UtilsFiles.readFile(launcherDataFile);
 		JSONObject jsonObject = (JSONObject) jsonParser.parse(dataFile);
 
 		if(jsonObject.get("skip") != null && (Boolean) jsonObject.get("skip")) {
 			new CrashReport("Le serveur de fichiers est actuellement en cours de maintenance. Retestez plus tard.", Langage.LAUNCHINGGAME.toString());
 		}
 
 		String release = (String) jsonObject.get("release");
 		String beta = (String) jsonObject.get("beta");
 		String alpha = (String) jsonObject.get("alpha");
 		String url = (String) jsonObject.get("url");
 
 		// TODO: Implement multiple versions channels
 		if(true) {
 			url = url.replace("{id}", release);
 		}
 
 		new Downloader(new URL(url), launcherDataFile, false);
 		dataFile = fr.minepod.utils.UtilsFiles.readFile(launcherDataFile);
 		jsonObject = (JSONObject) jsonParser.parse(dataFile);
 
 		JSONArray files = (JSONArray) jsonObject.get("files");
 		@SuppressWarnings("unchecked")
 		Iterator<Object> iterator = files.iterator();
 
 		while(iterator.hasNext()) {
 			JSONObject object = (JSONObject) iterator.next();
 
 			if(object.get("skip") == null || !(Boolean) object.get("skip")) {
 				String fileId = (String) object.get("id");
 				String fileVersion = (String) object.get("version");
 				String fileType = (String) object.get("type");
 				String fileAction = (String) object.get("action");
 				String filePath = fill((String) object.get("path"), fileId, fileVersion, fileType, true);
 				String fileName = fill((String) object.get("name"), fileId, fileVersion, fileType, true);
 				String fileTemp = fill((String) object.get("temp"), fileId, fileVersion, fileType, true);
 				String fileUrl = fill((String) object.get("url"), fileId, fileVersion, fileType, false);
 				String fileMd5 = (String) object.get("md5");
 				Boolean fileReplace = (Boolean) object.get("replace");
 
 				if(!new File(fileTemp).getParentFile().exists()) {
 					new File(fileTemp).getParentFile().mkdirs();
 				}
 
 				if(!new File(fileTemp).exists()) {
 					new File(fileTemp).createNewFile();
 				}
 
				if(!new File(filePath).getParentFile().exists()) {
					new File(filePath).getParentFile().mkdirs();
 				}
 
				if(!new File(filePath).exists()) {
					new File(filePath).createNewFile();
 				}
 
 				int arrayListSize = arrayList.size();
 				arrayList.add(new DownloaderThread(fileUrl, fileMd5, filePath, fileName, fileTemp, fr.minepod.utils.UtilsFiles.md5(fileTemp), fileType, fileAction));
 				arrayList.get(arrayListSize).start();
 			}
 		}
 
 		for(DownloaderThread temp : arrayList) {
 			temp.join();
 		}
 	}
 
 	public String fill(String input, String id, String version, String type, boolean replaceSlashes) {
 		input = input.replace("{launcherdir}", Config.launcherLocation);
 		input = input.replace("{minecraftdir}", Config.minecraftDir);
 		input = input.replace("{id}", id);
 		input = input.replace("{version}", version);
 		input = input.replace("{type}", type);
 
 		if(replaceSlashes) {
 			input = input.replace("/", Config.slash);
 		}
 
 		return input;
 	}
 }
