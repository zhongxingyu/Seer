 package plugins;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import com.ijg.darklight.core.Issue;
 import com.ijg.darklight.core.ScoreModule;
 import com.ijg.darklight.core.settings.ConfigParser;
 
 /**
  * Proof of concept for dynamically loading module settings from the config.json file
  * @author Isaac Grant
  * @version 1.0.0
  *
  */
 
 public class FileModule extends ScoreModule {
 
 	private HashMap<Issue, File[]> issueMap = new HashMap<Issue, File[]>();
 	
 	public FileModule() {
 		loadSettings();
 		
 		Iterator<Issue> iter = issueMap.keySet().iterator();
 		while (iter.hasNext()) {
 			issues.add(iter.next());
 		}
 	}
 	
 	@Override
 	public ArrayList<Issue> check() {
 		Iterator<Issue> iter = issueMap.keySet().iterator();
 		while (iter.hasNext()) {
 			Issue curIssue = iter.next();
 			File[] assocFiles = issueMap.get(curIssue);
 			int existingFiles = 0;
 			for (File assocFile : assocFiles) {
 				if (assocFile.exists()) {
 					existingFiles++;
 				}
 			}
 			if (existingFiles == 0) {
 				add(curIssue);
 			} else {
 				remove(curIssue);
 			}
 		}
 		
 		return issues;
 	}
 	
 	/**
 	 * Load issues from the config file
 	 */
 	@Override
 	protected void loadSettings() {
 		JSONObject fileSettings = (JSONObject) ConfigParser.getConfig().get("FileModule");
 		
 		@SuppressWarnings("unchecked")
 		Iterator<String> iter = fileSettings.keySet().iterator();
		System.out.println("FileModule has loaded the following issues:");
 		while (iter.hasNext()) {
 			String issueName = iter.next();
 			JSONArray rawIssueFiles = (JSONArray) ((JSONObject) fileSettings.get(issueName)).get("files");
 			String issueDescription = (String) ((JSONObject) fileSettings.get(issueName)).get("description");
 			File[] issueFiles = new File[rawIssueFiles.size()];
 			
			System.out.println(issueName + ": " + issueDescription + ", with the following associated files:");
 			for (int i = 0; i < issueFiles.length; ++i) {
 				issueFiles[i] = new File((String) rawIssueFiles.get(i));
				System.out.println(issueFiles[i]);
 			}
 			
 			issueMap.put(new Issue(issueName, issueDescription), issueFiles);
 		}
 	}
 
 }
