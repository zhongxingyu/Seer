 /**
  * 
  */
 package ch.ethz.e4mooc.server.util;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import ch.ethz.e4mooc.shared.ProjectModelDTO;
 
 /**
  * This class represents state information of the server.
  * In particular, it holds the information about the available Eiffel projects.
  * 
  * The class can be accessed statically as it implements a singleton.
  * 
  * @author hce
  *
  */
 public class ServerState {
 
 	/** The singleton object */
 	private static ServerState serverState;
 	private final String SEP = System.getProperty("file.separator"); 
 	
 	/** Maps a project name to a project model, e.g. AutoTest, to the corresponding ToolModel object */
 	private ConcurrentHashMap<String, ProjectModel> projectModelMap;
 	/** A list that contains all the names of the projects available. */
 	private Set<String> projectNames;
 	/** Maps a temporary project folder (stored in E4MOOC_TMP) to a time stamp when it was last used by the client */
 	private ConcurrentHashMap<String, Long> tmpFoldersMap;
 	/** Maps a project name to the name of .ecf file that is used for compilation */
 	private ConcurrentHashMap<String, String> ecfFileNameMap;
 	
 	/** This lock will be used by the CleanUpListener to prevent all servlet threads from writing/reading tmp-project-folders */
 	Lock deleteLock;
 	
 	
 	/**
 	 * Private constructor as object creation is done through singleton pattern.
 	 */
 	private ServerState() {
 		projectModelMap = new ConcurrentHashMap<String, ProjectModel>();
 		// we get a concurrent set based on the concurrent map
 		projectNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
 		tmpFoldersMap = new ConcurrentHashMap<String, Long>();
 		ecfFileNameMap = new ConcurrentHashMap<String, String>();
 	}
 	
 	/**
 	 * Returns the singleton object of the ServerState.
 	 * @return the singleton of the ServerState
 	 */
 	public static ServerState getState() {
 		if(serverState != null)
 			return serverState;
 
 		serverState = new ServerState();
 		return serverState;
 
 	}
 	
 	
 	/**
 	 * Returns a project model for a given (valid) project name. Otherwise returns null.
 	 * @param projectName name of the project
 	 * @return a project model
 	 */
 	public ProjectModel getProjectModel(String projectName) {
 		return projectModelMap.get(projectName);
 	}
 	
 	
 	/**
 	 * Adds a ProjectModel object to the server-state.
 	 * @param pm a ProjectModel object
 	 */
 	public void addProjectModel(ProjectModel pm) {
 		
 		// add the PorjectModel to the map
 		projectModelMap.put(pm.getProjectName(), pm);
 		// and add the project's name to the list of names
 		projectNames.add(pm.getProjectName());
 	}
 	
 	
 	/**
 	 * Removes the project with the given projectName from the server state
 	 * (if it exists, otherwise does nothing).
 	 * @param projectName the name of the project
 	 */
 	public void removeProjectModel(String projectName) {
 		if(projectModelMap.keySet().contains(projectName))
 			projectModelMap.remove(projectName);
 		
 		if(projectNames.contains(projectName))
 			projectNames.remove(projectName);
 	}
 	
 	
 	/**
 	 * Adds a collection of ProjectModel objects to the server-state.
 	 * @param pms a collection of ProjectModel objects
 	 */
 	public void addToolModels(Collection<ProjectModel> pms) {
 		for(ProjectModel pm: pms)
 			addProjectModel(pm);
 	}
 	
 	
 	/**
 	 * Returns a list with the names of all projects.
 	 * @return a list with the names of all projects
 	 */
 	public Set<String> getProjectNames() {
 		return projectNames;
 	}
 	
 	/**
 	 * Returns the name of the ecf file for the given project.
 	 * Returns an empty string if there's no ecf file name known for the given project name.
 	 * @param projectName the project name
 	 * @return name of the ecf file (without the ending ".ecf" and without any path)
 	 */
 	public String getEcfFileNameWithoutAnyPath(String projectName) {
 		
 		if(ecfFileNameMap.contains(projectName))
 			return ecfFileNameMap.get(projectName);
 		else {
 			String ecfFile = projectModelMap.get(projectName).getEcfFile();
 			ecfFile = ecfFile.substring(0, ecfFile.length() - 4);
			String [] s = ecfFile.split(SEP);
 			ecfFile = s[s.length -1];
			// cache the result
 			ecfFileNameMap.put(projectName, ecfFile);
 			
 			return ecfFile;
 		}
 	}
 	
 	
 	/**
 	 * Adds an entry for a tmp-project-folder to the server state.
 	 * The server state will add a time stamp for when this entry was created.
 	 * If the server state already knows about this tmp-folder, the time stamp
 	 * will be updated.
 	 * @param tmpFolderPath the path of the temporary folder
 	 */
 	public void setTmpFolderAndTimeStamp(String tmpFolderPath) {
 		tmpFoldersMap.put(tmpFolderPath, System.currentTimeMillis());
 	}
 	
 	/**
 	 * Removes an entry of a tmp-project-folder from the server state.
 	 * This method should be called once a tmp-folder was deleted.
 	 * @param tmpFolderPath
 	 */
 	public void removeTmpFolderAndTimeStamp(String tmpFolderPath) {
 		if(tmpFoldersMap.keySet().contains(tmpFolderPath))
 			tmpFoldersMap.remove(tmpFolderPath);
 	}
 	
 	/**
 	 * Returns a set of all the tmp-project-folders older than 'milliseconds'.
 	 * @param the amount of milliseconds that must have past since a tmp folder was added
 	 * @return set containing the tmp-project-folders older than 'milliSeconds'
 	 */
 	public Set<String> getSetOfAllTmpFoldersOlderThan(Long milliSeconds) {
 		
 		Set<String> result = new HashSet<String>();
 		Long currentTime = System.currentTimeMillis();
 		
 		// store all folders in list
 		for(String key: tmpFoldersMap.keySet()) {
 			if(currentTime - tmpFoldersMap.get(key) > milliSeconds)
 				result.add(key);
 		}
 		
 		// remove the entries from the map
 		for(String folderName: result) {
 			tmpFoldersMap.remove(folderName);
 		}
 		
 		return result;
 	}
 }
