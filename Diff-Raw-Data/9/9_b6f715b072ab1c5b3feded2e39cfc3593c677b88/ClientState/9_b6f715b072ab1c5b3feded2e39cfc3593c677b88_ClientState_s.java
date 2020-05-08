 /**
  * 
  */
 package ch.ethz.e4mooc.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ch.ethz.e4mooc.shared.ProjectModelDTO;
 
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.Window;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * This class holds client state.
  * 
  * @author hce
  *
  */
 public class ClientState {
 
 	/** the name of the current project */
 	private String currentProjectName;
 	
 	/** the project model of the current project */
 	private ProjectModelDTO projectModel;
 	/** maps tab indexes to file names */
 	private Map<Integer, String> tabIndexToFileNameMap;
 	/** the event bus used throughout the application */
 	private EventBus eventBus;
 	/** the text that is currently shown in the editor */
 	private String inputFile;
 	
 	/** a map that stores the content of files as they were modified by the user */
 	private Map<String, String> userStorage;
 	
 	/**
 	 * 
 	 * @param eventBus the eventBus used throughout the application
 	 */
 	public ClientState(EventBus eventBus) {
 		this.eventBus = eventBus;
 		tabIndexToFileNameMap = new HashMap<Integer, String>();
 		userStorage = new HashMap<String, String>();
 		bind();
 	}
 	
 	
 	private void bind() {
 		
 		// on closing the browser window
 		Window.addCloseHandler(new CloseHandler<Window>() {
 			
 			@Override
 			public void onClose(CloseEvent<Window> event) {
 				// before the browser window is closed, store the latest changes of the currently visible tab
 				//storeYourCodeToLocalStorage();
 			}
 		});		
 	}
 	
 	
 	/**
 	 * Stores a project model in the ClientState.
 	 * @param pm the project model
 	 */
 	public void storeProjectModel(ProjectModelDTO pm) {
 		// store the project model
 		this.projectModel = pm;
 		
 		// counter for the tab index
 		int counter = 0;
 		
 		// map the files of the project to tab indexes
 		for(String fileName: pm.getFileNames()) {
 			tabIndexToFileNameMap.put(counter, fileName);
 			counter++;
			
			// store the files in the user storage
			// userStorage.put(fileName, pm.getFileContent(fileName));
 		}
 	}
 
 	/**
 	 * Returns the name of the project.
 	 * @return the project name
 	 */
 	public String getProjectName() {
 		return projectModel.getProjectName();
 	}
 	
 	/**
 	 * Returns a list of all the file names in the project.
 	 * @return list of file names
 	 */
 	public List<String> getProjectFileNames() {
 		return projectModel.getFileNames();
 	}
 	
 
 	/**
 	 * Returns the content of a file based on the file name.
 	 * @param fileName the file name
 	 * @return the content of the file
 	 */
 	public String getContentOfFile(String fileName) {
 		
 		String result = "";
 	
 		if(userStorage.containsKey(fileName))
 			result = userStorage.get(fileName);
 		else
 			result = projectModel.getFileContent(fileName);
 		
 		return result;
 	}
 	
 	
 	/**
 	 * Returns the content of a file based on the tab index used by the editor.
 	 * @param tabIndex the tab index
 	 * @return the content of the file that belongs to the tab index
 	 */
 	public String getContentOfFile(int tabIndex) {
 				
 		String result = "";
 		
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		
 		if(userStorage.containsKey(fileName)) {
 			// get the version from the local storage
 			result = userStorage.get(fileName);
 		} else {
 			result = projectModel.getFileContent(fileName);
 		}
 		return result;
 	}
 	
 	
 	/**
 	 * Returns a map with all the files. Files include the changes by users.
 	 * @return map of all files
 	 */
 	public HashMap<String, String> getContentOfAllFiles() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		for(String fileName: this.getProjectFileNames()) {
 			result.put(fileName, this.getContentOfFile(fileName));
 		}
 		return result;
 	}
 
 	
 	/**
 	 * Stores the given text into session storage, if that's supported by the browser.
 	 * @param tabIndex the index of the tab that should be stored (is used as port of the KEY)
 	 * @param text the text to store
 	 */
 	public void storeTextFromUser(int tabIndex, String text) {
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		userStorage.put(fileName, text);
 	}
 
 	
 	/**
 	 * Removes the user version for the given tab index from the storage.
 	 * @param tabIndex the tab index of the file for which to delete the entry
 	 */
 	public void deleteFromStorage(int tabIndex) {	
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		
 		if(userStorage.containsKey(fileName))
 			userStorage.remove(fileName);	
 	}
	
 }
