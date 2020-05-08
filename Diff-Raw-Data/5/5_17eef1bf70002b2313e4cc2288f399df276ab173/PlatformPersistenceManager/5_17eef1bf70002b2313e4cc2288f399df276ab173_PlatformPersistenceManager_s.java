 package org.bh.platform;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOProject;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.platform.i18n.BHTranslator;
 
 /**
  * Platform Persistence
  * 
  * <p>
  * Handling open and save functions
  * 
  * @author Michael Löckelt
  * @version 0.1, 26.12.2009
  * 
  */
 public class PlatformPersistenceManager {
 
 	/**
 	 * Path to File
 	 */
 	File path;
 
 	/**
 	 * reference to repomanager
 	 */
 	ProjectRepositoryManager projectRepositoryManager;
 
 	/**
 	 * reference to bh main frame
 	 */
 	BHMainFrame bhmf;
 
 	private static final Logger log = Logger
 			.getLogger(PlatformPersistenceManager.class);
 
 	public static SaveActionListener sAL = new SaveActionListener();
 
 	public PlatformPersistenceManager(BHMainFrame bhmf,
 			ProjectRepositoryManager projectRepositoryManager) {
 		this.bhmf = bhmf;
 		this.projectRepositoryManager = projectRepositoryManager;
 	}
 
 	public ArrayList<DTOProject> openFile(File path) {
 		if (!path.toString().endsWith(".bh"))
 			path = new File(path.toString() + ".bh");
 		else
 			this.path = path;
 		
 		ArrayList<DTOProject> returnRepository = new ArrayList<DTOProject>();
 
 		try {
 			FileInputStream fileLoader = new FileInputStream(path);
 			ObjectInputStream objectLoader = new ObjectInputStream(fileLoader);
 
 			Object whileObj = null;
 
 			while ((whileObj = objectLoader.readObject()) != null) {
 				if (whileObj instanceof DTOProject)
 					returnRepository.add((DTOProject) whileObj);
 			}
 
 		} catch (FileNotFoundException e) {
 			log.error("File " + path + " not found!");
 		} catch (EOFException e) {
 
 			// replace ProjectRepository
 			projectRepositoryManager.replaceProjectList(returnRepository);
 
 			// Save path to preferences
 			PlatformController.preferences.put("path", path.toString());
 
 			// Set isChanged
 			ProjectRepositoryManager.setChanged(false);
 			
 			// Refresh title
 			bhmf.resetTitle();
 
 			log.debug("file " + path.toString()
 					+ " successfully opened");
 
 			return returnRepository;
 		} catch (IOException e) {
 			log.error("IOException while opening file");
 		} catch (Exception e) {
 			log.error("Exception while opening file");
 		}
 
 		return null;
 	}
 
 	public void prepareSaveFile (boolean forcedSaveAs) {
 		if (PlatformController.preferences.get("path", "").equals("") || forcedSaveAs == true) {
 			
 			int returnVal = bhmf.getChooser().showSaveDialog(bhmf);
 			 if (returnVal == JFileChooser.APPROVE_OPTION) {
 				 log.debug("You choose to save this file: " + bhmf.getChooser().getSelectedFile().getName());
 				 this.path = bhmf.getChooser().getSelectedFile();
 			 }
 			 
 		} else {
 			File path = new File(PlatformController.preferences.get("path", ""));
 
 			this.path = path;
 		}
 		
 		this.saveFile();
 	}
 	
 	public void saveFile() {
 
 		try {
 
 			// force .bh data extension
 			if (!path.toString().endsWith(".bh")) {
 				File newPath = new File(path + ".bh");
 				this.path = newPath;
 			}
 
 			DTOProject iteratorObject;
 			FileOutputStream fileWriter = new FileOutputStream(path);
 			ObjectOutputStream objectWriter = new ObjectOutputStream(fileWriter);
 
 			Iterator<DTOProject> projectIterator = projectRepositoryManager
 					.getRepositoryList().iterator();
 
 			while (projectIterator.hasNext()) {
 				iteratorObject = projectIterator.next();
 				objectWriter.writeObject(iteratorObject);
 				log.debug("Object " + iteratorObject.toString()
 						+ "written to file " + path);
 			}
 
 			objectWriter.close();
 			fileWriter.close();
 			
 			log.debug("ProjectRepository successfully saved to " + path);
 			
 			// Set isChanged
 			ProjectRepositoryManager.setChanged(false);
 			
 			// Save path to preferences
 			PlatformController.preferences.put("path", this.path.toString());
 
 			// Refresh title
 			bhmf.resetTitle();
 
 		} catch (FileNotFoundException e) {
 			log.error("File " + path + "not found!");
 		} catch (IOException e) {
 			log.error("IO Error occured while saving" + path);
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * last edited file dialog
 	 * 
 	 * @author Thiele.Klaus (UI)
 	 * @author Loeckelt.Michael (persistence logic)
 	 */
 	public void lastEditedFile () {
 		String lastFile = PlatformController.preferences.get("path", "");
 		if (!lastFile.equals("")) {
			String test = "/Users/klaus/Documents/DHBW/05_DHBW-Semester5/Geschäftsprozessmodellierung/VisObjNet27/DEUTSCHES TUTORIAL/blah/juju/tescht.bh";
 			String title = Services.getTranslator().translate("PlastFile");
			String message = "<html>" + Services.getTranslator().translate("PlastFile", BHTranslator.LONG) + "<br /><i>(" + test + ")</i></html>";
			
 			
 			int action = JOptionPane.showConfirmDialog(bhmf, message, title, JOptionPane.YES_NO_OPTION);
 			if (action == JOptionPane.YES_OPTION) {
 				File tmpFile = new File(lastFile);
 				PlatformController.platformPersistenceManager.openFile(tmpFile);
 				
 			} else if (action == JOptionPane.NO_OPTION) {
 				PlatformController.preferences.remove("path");
 				bhmf.resetTitle();
 			}
 		}
 	}
 }
 
 class SaveActionListener implements IPlatformListener {
 
 	public SaveActionListener() {
 		Services.addPlatformListener(this);
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (PlatformEvent.Type.SAVE == e.getEventType()) {
 			PlatformController.platformPersistenceManager.prepareSaveFile(false);
 		} else if (PlatformEvent.Type.SAVEAS == e.getEventType()) {
 			PlatformController.platformPersistenceManager.prepareSaveFile(true);
 		}
 	}
 }
