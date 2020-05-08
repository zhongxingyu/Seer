 package de.deepamehta.plugins.foldercanvas;
 
 import de.deepamehta.plugins.foldercanvas.model.FolderCanvas;
 
import de.deepamehta.core.model.ClientContext;
 import de.deepamehta.core.service.Plugin;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class FolderCanvasPlugin extends Plugin {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // ************************
     // *** Overriding Hooks ***
     // ************************
 
 
 
     @Override
     public JSONObject executeCommandHook(String command, Map params, ClientContext clientContext) {
         if (command.equals("deepamehta3-foldercanvas.synchronize")) {
             long topicmapId = -1;
             try {
                 topicmapId = (Integer) params.get("topicmap_id");
                 FolderCanvas folderCanvas = new FolderCanvas(topicmapId, dms);
                 FolderCanvas.SyncStats stats = folderCanvas.synchronize();
                 //
                 JSONObject result = new JSONObject();
                 result.put("status", "success");
                 result.put("files_added", stats.filesAdded);
                 result.put("folders_added", stats.foldersAdded);
                 result.put("files_removed", stats.filesRemoved);
                 result.put("folders_removed", stats.foldersRemoved);
                 return result;
             } catch (Throwable e) {
                 throw new RuntimeException("Error while synchronizing folder canvas " + topicmapId, e);
             }
         }
         return null;
     }
 }
