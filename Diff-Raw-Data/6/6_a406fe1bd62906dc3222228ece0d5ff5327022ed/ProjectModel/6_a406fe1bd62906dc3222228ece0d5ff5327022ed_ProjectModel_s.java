 /**
  * 
  */
 package ch.ethz.e4mooc.server.util;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.logging.Level;

import com.sun.istack.internal.logging.Logger;
 
 import ch.ethz.e4mooc.shared.ProjectModelDTO;
 
 /**
  * A ProjectModel represents an 
  * Eiffel projects and contains the files (and the
  * file names) that should be displayed on the web page.
  * 
  * The ProjectModel resides on the server. It contains a ProjectModelDTO which contains
  * the data that needs to be send to the client.
  * 
  * @author hce
  *
  */
 public class ProjectModel {
 
 	/** Logger for the Project Model */
	private final static Logger LOGGER = Logger.getLogger(ch.ethz.e4mooc.server.util.ProjectModel.class);
 	
 	/** the ProjectModelDTO that belongs to this ProjectModel */
 	private ProjectModelDTO projectModelDTO;
 	/** mapping of file names to their relative path within a project */
 	private Map<String, String> fileNameToPathMap;
 	/** the relative path and file name of the ECF file */
 	private String ecfFileNameAndRelativePath;
 	
 	/**
 	 * Constructor for a new project model.
 	 * @param projectName the name of the project (we use the name of the project folder on the server)
 	 */
 	public ProjectModel(String projectName) {
 		this.projectModelDTO = new ProjectModelDTO(projectName);
 		this.fileNameToPathMap = new HashMap<String, String>();
 		this.ecfFileNameAndRelativePath = "";
 		
 		LOGGER.log(Level.INFO, "Creating project model for: " + projectName);
 	}
 	
 	/**
 	 * Returns the name of this project, i.e. the name of the project folder on the server.
 	 * @return the name of this project.
 	 */
 	public String getProjectName() {
 		return projectModelDTO.getProjectName();
 	}
 	
 	/**
 	 * Adds a the name of a file to the project model's list of file names.
 	 * @param fileName name of a file
 	 * @param relativeFilePath the relative path of the (including the file name) where the file is stored in the project
 	 */
 	public void addFileName(String fileName, String relativeFilePath) {
 		if(fileNameToPathMap.containsKey(fileName))
 			LOGGER.log(Level.WARNING, "A file of name '" + fileName + "' already exists. Previous file will be overwritten.");
 		
 		fileNameToPathMap.put(fileName, relativeFilePath);
 		projectModelDTO.addFileName(fileName);
 	}
 	
 	/**
 	 * Adds a file to the project model.
 	 * @param fileName the file name
 	 * @param fileContent the content of the file
 	 */
 	public void addFile(String fileName, String fileContent) {
 		projectModelDTO.addFile(fileName, fileContent);
 	}
 	
 	/**
 	 * Returns the list of all file names in the project represented by
 	 * the current object.
 	 * @return all the file names in the project
 	 */
 	public LinkedList<String> getFileNames() {
 		return projectModelDTO.getFileNames();
 	}
 	
 	/**
 	 * Returns the content of a file (as stored on the server) for a given file name.
 	 * @param fileName the name of file for which the content should be returned
 	 * @return the content of the file
 	 */
 	public String getFileContent(String fileName) {
 		return projectModelDTO.getFileContent(fileName);
 	}
 
 	/**
 	 * Returns true if the project model has a file with the given file name.
 	 * @param fileName the file name to check for
 	 * @return true if a file with that name exists, otherwise false
 	 */
 	public boolean hasFileName(String fileName) {
 		return projectModelDTO.getFileNames().contains(fileName);
 	}
 
 	
 	/**
 	 * Returns the DTO of this project model.
 	 * The DTO contains only the data that is needed by the client
 	 * (which is less than what's needed by the server).
 	 * @return
 	 */
 	public ProjectModelDTO getProjectModelDTO() {
 		return projectModelDTO;
 	}
 	
 	
 	/**
 	 * Store the file name and relative path of the ECF file.
 	 * @param relativePathAndFileName
 	 */
 	public void setEcfFile(String relativePathAndFileName) {
 		if(!this.ecfFileNameAndRelativePath.isEmpty())
 			LOGGER.log(Level.WARNING, "An ECF file was already set for this project. Previous ECF file will be ignored.");
 		this.ecfFileNameAndRelativePath = relativePathAndFileName;
 		LOGGER.log(Level.INFO, "ECF file of the project: " + relativePathAndFileName);
 	}
 	
 	/**
 	 * Returns the ecf file and its relative path in the project.
 	 * @return the ecf file and its relative path in the project
 	 */
 	public String getEcfFile() {
 		return this.ecfFileNameAndRelativePath;
 	}
 }
 
