 package edu.uci.lighthouse.core.controller;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.osgi.framework.BundleContext;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.listeners.IJavaFileStatusListener;
 import edu.uci.lighthouse.core.listeners.IPluginListener;
 import edu.uci.lighthouse.core.listeners.ISVNEventListener;
 import edu.uci.lighthouse.core.parser.IParserAction;
 import edu.uci.lighthouse.core.parser.LighthouseParser;
 import edu.uci.lighthouse.core.preferences.DatabasePreferences;
 import edu.uci.lighthouse.core.util.UserDialog;
 import edu.uci.lighthouse.core.widgets.StatusWidget;
 import edu.uci.lighthouse.model.BuildLHBaseFile;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseFile;
 import edu.uci.lighthouse.model.LighthouseFileManager;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
 import edu.uci.lighthouse.model.io.IPersistence;
 import edu.uci.lighthouse.model.io.LighthouseModelXMLPersistence;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.JPAUtility;
 import edu.uci.lighthouse.model.repository.LighthouseRepositoryEvent;
 import edu.uci.lighthouse.parser.ParserException;
 
 public class Controller implements ISVNEventListener, IJavaFileStatusListener,
 		IPluginListener, Runnable, IPropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(Controller.class);
 	private static HashMap<String, Date> mapClassToSVNCommittedTime = new HashMap<String, Date>();
 	private HashMap<String, LighthouseFile> classBaseVersion = new HashMap<String, LighthouseFile>();
 	private Set<IFile> classWithErrors = new LinkedHashSet<IFile>();
 	private Collection<IFile> ignorefilesJustUpdated = new LinkedHashSet<IFile>();
 	private Date lastDBAccess = null;
 	private boolean threadRunning;
 	private boolean threadSuspended;
 	private final int threadTimeout = 5000;
 
 	public static Map<String, Date> getWorkingCopy(){
 		return mapClassToSVNCommittedTime;
 	}
 	
 	@Override
 	public void start(BundleContext context) throws Exception {
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
 		loadPreferences();
 		loadModel();
 		(new Thread(this)).start();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		threadSuspended = false;
 		threadRunning = false;
 		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
 		savePreferences();
 		saveModel();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadPreferences() {
 		String prefix = Activator.PLUGIN_ID;
 		IPreferenceStore prefStore = Activator.getDefault()
 				.getPreferenceStore();
 
 		lastDBAccess = new Date(prefStore.getLong(prefix + "lastDBAccess"));
 		logger.debug("loading lastDBAccess=" + lastDBAccess);
 
 		try {
 			final String filename = "lighthouseWorkingCopy.version";
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
 					filename));
 			mapClassToSVNCommittedTime = (HashMap<String, Date>) ois
 					.readObject();
 			logger.debug("loading " + filename);
 		} catch (Exception e) {
			logger.error(e);
 		}
 	}
 
 	public void savePreferences() {
 		String prefix = Activator.PLUGIN_ID;
 		IPreferenceStore prefStore = Activator.getDefault()
 				.getPreferenceStore();
 
 		prefStore.setValue(prefix + "lastDBAccess", lastDBAccess.getTime());
 		logger.debug("saving lastDBAccess=" + lastDBAccess);
 
 		try {
 			final String filename = "lighthouseWorkingCopy.version";
 			ObjectOutputStream oos = new ObjectOutputStream(
 					new FileOutputStream(filename));
 			oos.writeObject(mapClassToSVNCommittedTime);
 			logger.debug("saving " + filename);
 		} catch (Exception e) {
			logger.error(e);
 		}
 	}
 
 	private void loadModel() {
 		IPersistence mos = new LighthouseModelXMLPersistence(LighthouseModel
 				.getInstance());
 		try {
 			mos.load();
 		} catch (Exception e) {
 			logger.error(e,e);
 		}
 
 		if (LighthouseModel.getInstance().isEmpty()
 				&& mapClassToSVNCommittedTime.size() > 0) {
 			PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 			try {
 				pullModel.executeQueryCheckout(mapClassToSVNCommittedTime);
 			} catch (JPAException e) {
 				logger.error(e,e);
 				UserDialog.openError(e.getMessage());
 			}
 		}
 		// Show the events in the UI.
 		LighthouseModel.getInstance().fireModelChanged();
 	}
 	
 	private void saveModel(){
 		try {
 			IPersistence mos = new LighthouseModelXMLPersistence(LighthouseModel.getInstance());
 			mos.save();
 		} catch (IOException e) {
 			logger.error(e,e);
 		}
 	}
 	
 	@Override
 	public void run() {
 		threadRunning = true;
 		while (threadRunning) {
 			// Sleep for the time defined by thread timeout
 			try {
 				try {
 					refreshModelBasedOnLastDBAccess();
 					Thread.sleep(threadTimeout);
 				} catch (Exception e) {
 					// Database exceptions are RuntimeExceptions
 					StatusWidget.getInstance().setStatus(Status.CANCEL_STATUS);
 					threadSuspended = true;
 					synchronized(this) {
 	                    while (threadSuspended) {
 	                        wait();
 	                    }
 	                }
 				} /*catch (JPAException e) {
 					logger.error(e,e);
 				}*/
 			} catch (InterruptedException e) {
 				logger.error(e,e);
 			}
 		}
 	};
 	
 	/**
 	 * Refresh the LighthouseModel with new events from database and fire this
 	 * changes to the UI.
 	 * @throws JPAException 
 	 */
 	public synchronized void refreshModelBasedOnLastDBAccess() throws JPAException {
 		LighthouseAuthor author = Activator.getDefault().getAuthor();
 		/*
 		 * If the map's size == 0, it means that it is the first time that user
 		 * is running Lighthouse. Then, the LighthouseModel will be updated only
 		 * if the user execute a checkout first.
 		 */
 		if (mapClassToSVNCommittedTime.size() != 0) {
 			PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 			List<LighthouseEvent> events = pullModel
 					.getNewEventsFromDB(lastDBAccess,author);
 			fireModificationsToUI(events);
 			//logger.debug("timeout: " + lastDBAccess);
 			if (events.size()!=0) {
 				logger.debug("timeout[ " + lastDBAccess + " ] brought: [" + events.size() + "] events");
 			}
 			lastDBAccess = getTimestamp();
 		}
 	}
 
 	@Override
 	public void open(final IFile iFile, boolean hasErrors) {
 
 		final String classFqn = getClassFullyQualifiedName(iFile);
 			Thread task = new Thread() {
 				@Override
 				public void run() {
 			//Removed the thread because the unopened files. When you try to guarantee the lifecycle (open,change,close) the base version can not
 					try {
 						LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 						classBaseVersion.put(classFqn, lhBaseFile);
 					} catch (JPAException e) {
 						logger.error(e,e);
 						UserDialog.openError(e.getMessage());
 					}
 				}
 			};
 			task.start();
 	}
 
 	public LighthouseFile getBaseVersionFromDB(String classFullyQualifiedName) throws JPAException {
 		LighthouseFile result = null;
 		Date revisionTime = mapClassToSVNCommittedTime.get(classFullyQualifiedName);
 		if (revisionTime != null) {
 			result = BuildLHBaseFile.execute(LighthouseModel.getInstance(),
 					classFullyQualifiedName, revisionTime, Activator.getDefault().getAuthor());
 		}
 		return result;
 	}
 
 	@Override
 	public void change(final IFile iFile, boolean hasErrors) {
 		// verify if any of the classWithErrors still have errors
 		// than parse the files that does not have errors anymore
 		// and remove the classes from the list classWithErrors
 		
 		if (!hasErrors) {
 			
 			if(ignorefilesJustUpdated.contains(iFile)) {
 				// event change was invoked without a merge problem
 				ignorefilesJustUpdated.remove(iFile);
 				// Update base version - the delta does not matter
 				// it is not working because we are getting the old IFile not the new one
 //				generateDeltaFromBaseVersion(Collections.singleton(iFile),true);
 				
 				// I will suppose that the revisionTime was setted in the update event
 				String classFqn =getClassFullyQualifiedName(iFile);
 				try {
 					LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 					classBaseVersion.put(classFqn, lhBaseFile);
 				} catch (JPAException e) {
 					logger.error(e,e);
 					UserDialog.openError(e.getMessage());
 				}
 				
 				
 			} else {
 				// TODO: Think about DB operations in a thread
 				
 				try {
 					Collection<LighthouseEvent> deltaEvents = generateDeltaFromBaseVersion(Collections
 							.singleton(iFile));
 					logger.debug("Event change generated delta events: " + deltaEvents.size());
 					PushModel pushModel = new PushModel(LighthouseModel
 							.getInstance());
 					pushModel.updateModelFromEvents(deltaEvents);
 					//FIXME: This implies if some error happen, the UI is not going to be updated. Check with Nilmax to see if this the behavior that we want.
 					fireModificationsToUI(deltaEvents);
 				} catch (Exception e) {
 					logger.error(e,e);
 					UserDialog.openError(e.getMessage());
 				}
 			
 			}
 			
 		} else { // if file with error
 			// FIXME: we are calling this if block in the algorithm for both error/no erros. A better approach will put them outside the main if. Check this later.
 			if (ignorefilesJustUpdated.contains(iFile)) {
 				// it is a merge conflict, next time that event change will be invoked 
 				// we are going to have just two situation:
 				// 1. file still with error
 				// 2. merge problems resolved
 				ignorefilesJustUpdated.remove(iFile);
 			}
 		}
 	}
 	
 	private Collection<LighthouseEvent> generateDeltaFromBaseVersion(Collection<IFile> files) {
 		return generateDeltaFromBaseVersion(files,false);
 	}
 	
 	private Collection<LighthouseEvent> generateDeltaFromBaseVersion(Collection<IFile> files, boolean force) {
 		final List<LighthouseEvent> result = new LinkedList<LighthouseEvent>();
 
 		// Use iterator to be able to remove the IFile in the loop
 		for (IFile file : files) {
 			final String classFqn = getClassFullyQualifiedName(file);
 			final LighthouseFile lhBaseFile = classBaseVersion.get(classFqn);
 			if (force || lhBaseFile != null) {
 				try {
 					final LighthouseParser parser = new LighthouseParser();
 					parser.executeInAJob(Collections.singleton(file),
 							new IParserAction() {
 								@Override
 								public void doAction() throws ParserException {
 									LighthouseFile currentLhFile = new LighthouseFile();
 									new LighthouseFileManager(currentLhFile)
 											.populateLHFile(parser
 													.getListEntities(), parser
 													.getListRelationships());
 									try {
 										LighthouseDelta delta = new LighthouseDelta(
 													Activator.getDefault().getAuthor(),
 													lhBaseFile, currentLhFile);
 										classBaseVersion.put(classFqn,
 												currentLhFile);
 										result.addAll(delta.getEvents());
 									} catch (JPAException e) {
 										throw new ParserException(e);
 									}
 								}
 							});
 				} catch (ParserException e) {
 					logger.error(e,e);
 					UserDialog.openError(e.getMessage());
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void close(IFile iFile, boolean hasErrors) {
 		final String classFqn = getClassFullyQualifiedName(iFile);
 		if (hasErrors) {
 			classWithErrors.add(iFile);
 		} else {
 			classBaseVersion.remove(classFqn);
 		}
 	}
 	
 	@Override
 	public void checkout(Map<IFile, ISVNInfo> svnFiles) {
 		// Ignore these files inside the change event, once we don't want to generate deltas for other people changes. 
 //		ignoredFiles.addAll(svnFiles.keySet());
 		
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 		PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 		try {
 		Collection<LighthouseEvent> events = pullModel.executeQueryCheckout(workingCopy);
 		LighthouseModel.getInstance().fireModelChanged();
 		logger.info("Number of events fetched after checkout = " + events.size());
 		
 		// Insert the CHECKOUT event in the database
 	
 			new PushModel(LighthouseModel.getInstance()).saveRepositoryEvent(
 					svnFiles, LighthouseRepositoryEvent.TYPE.CHECKOUT,
 					new Date());
 		} catch (Exception e) {
 			logger.error(e,e);
 			UserDialog.openError(e.getMessage());
 		}
 	}
 	
 	@Override
 	public void update(Map<IFile, ISVNInfo> svnFiles) {
 		// Ignore in the change event these files, once we don't want to generate deltas for other people changes. 
 		ignorefilesJustUpdated.addAll(svnFiles.keySet());
 
 		// setar a lista de arquivos que foram dados updates
 		// o metodo change vai ser chamado, dai eu nao quero gerar delta
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 		
 		LighthouseModel model = LighthouseModel.getInstance();
 		LighthouseModelManager modelManager = new LighthouseModelManager(model); 
 		
 		modelManager.removeArtifactsAndEvents(workingCopy.keySet());
 		
 		checkout(svnFiles);
 
 		// Insert the UPDATE event in the database
 		// Insert the CHECKOUT event in the database
 		try {
 			new PushModel(LighthouseModel.getInstance())
 					.saveRepositoryEvent(svnFiles,
 							LighthouseRepositoryEvent.TYPE.UPDATE, new Date());
 		} catch (Exception e) {
 			logger.error(e,e);
 		}
 	}
 	
 	@Override
 	public void commit(Map<IFile, ISVNInfo> svnFiles) {
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 		try {
 			PushModel pushModel = new PushModel(LighthouseModel.getInstance());
 
 			// assuming that there is just one committed time
 			ISVNInfo[] svnInfo = svnFiles.values().toArray(new ISVNInfo[0]);
 //			Date svnCommittedTime = svnInfo[0].getLastChangedDate();
 			//FIXME: Synchronization problem
 			Date svnCommittedTime = new Date();
 			
 			Collection<LighthouseEvent> listEvents = pushModel.updateCommittedEvents( 
 														getClassesFullyQualifiedName(svnFiles), 
 														svnCommittedTime,
 														Activator.getDefault().getAuthor());
 
 			LighthouseModelManager modelManager = new LighthouseModelManager(LighthouseModel.getInstance()); 
 			modelManager.removeCommittedEvents(workingCopy.keySet(),svnCommittedTime);
 			
 			fireModificationsToUI(listEvents);
 			logger.debug("Committed ["+ listEvents.size() + "] events " + "with time: " + svnCommittedTime);
 			
 			// Insert the CHECKIN event in the database
 			pushModel.saveRepositoryEvent(svnFiles,
 					LighthouseRepositoryEvent.TYPE.CHECKIN, new Date());
 		} catch (Exception e) {
 			logger.error(e,e);
 			UserDialog.openError(e.getMessage());
 		}
 	}
 
 	private HashMap<String, Date> getWorkingCopy(Map<IFile, ISVNInfo> svnFiles) {
 		HashMap<String, Date> result = new HashMap<String, Date>();
 		for (Entry<IFile, ISVNInfo> entry : svnFiles.entrySet()) {
 			String fqn = getClassFullyQualifiedName(entry.getKey());
 			if (fqn != null) {
 				ISVNInfo svnInfo = entry.getValue();
 				result.put(fqn, svnInfo.getLastChangedDate());
 			}
 		}
 		return result;
 	}
 
 	private List<String> getClassesFullyQualifiedName(
 			Map<IFile, ISVNInfo> svnFiles) {
 		LinkedList<String> result = new LinkedList<String>();
 		for (IFile iFile : svnFiles.keySet()) {
 			String fqn = getClassFullyQualifiedName(iFile);
 			if (fqn != null) {
 				result.add(fqn);
 			}
 		}
 		return result;
 	}
 
 	public static String getClassFullyQualifiedName(IFile iFile) {
 		String result = null;
 		try {
 			/*
 			 * When the Java file is out of sync with eclipse, get the fully
 			 * qualified name from ICompilationUnit doesn't work. So we decide
 			 * to do this manually, reading the file from the file system and
 			 * parsing it.
 			 */
 			BufferedReader d = new BufferedReader(new InputStreamReader(
 					new FileInputStream(iFile.getLocation().toOSString())));
 			while (d.ready()) {
 				String line = d.readLine();
 				if (line.contains("package")) {
 					String[] tokens = line.split("package\\s+|;");
 					for (String token : tokens) {
 						if (token.matches("[\\w\\.]+")) {
 							result = token;
 							break;
 						}
 					}
 					break;
 				}
 			}
 			/*
 			 * Java files should have at least one class with the same name of
 			 * the file
 			 */
 			String fileNameWithoutExtension = iFile.getName().replaceAll(
 					".java", "");
 			result += "." + fileNameWithoutExtension;
 			/* Append the project Name to the fqn */
 			result = iFile.getProject().getName() + "." + result;
 		} catch (Exception e) {
 			logger.error(e,e);
 		}
 		
 		return result;
 	}
 	private void fireModificationsToUI(Collection<LighthouseEvent> events) {
 		// We need hashmap to avoid repaint the UI multiple times
 		HashMap<LighthouseClass, LighthouseEvent.TYPE> mapClassEvent = new HashMap<LighthouseClass, LighthouseEvent.TYPE>();
 		HashMap<LighthouseRelationship, LighthouseEvent.TYPE> mapRelationshipEvent = new HashMap<LighthouseRelationship, LighthouseEvent.TYPE>();
 		LighthouseModel model = LighthouseModel.getInstance();
 		for (LighthouseEvent event : events) {
 			Object artifact = event.getArtifact();
 			// TODO: comment more this method. It is confusing.
 			// ADD creates a new class node in the view and populates it
 			// MODIFY just re-populates the class node
 			if (artifact instanceof LighthouseClass) {
 				LighthouseClass klass = (LighthouseClass) artifact;
 				mapClassEvent.put(klass, event.getType());
 			} else if (artifact instanceof LighthouseEntity) {
 				LighthouseModelManager manager = new LighthouseModelManager(
 						model);
 				LighthouseClass klass = manager
 						.getMyClass((LighthouseEntity) artifact);
 				if (klass != null) {
 					// Never overwrite the ADD event
 					if (!mapClassEvent.containsKey(klass)) {
 						// Just refresh the class node
 						mapClassEvent.put(klass, LighthouseEvent.TYPE.MODIFY);
 					}
 				}
 			} else if (artifact instanceof LighthouseRelationship) {
 				LighthouseRelationship relationship = (LighthouseRelationship) artifact;
 				mapRelationshipEvent.put(relationship, event.getType());
 			}
 		}
 		// Fire class changes to the UI
 		for (Entry<LighthouseClass, TYPE> entry : mapClassEvent.entrySet()) {
 			model.fireClassChanged(entry.getKey(), entry.getValue());
 		}
 		// Fire relationship changes to the UI
 		for (Entry<LighthouseRelationship, TYPE> entry : mapRelationshipEvent
 				.entrySet()) {
 			model.fireRelationshipChanged(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Override
 	public void remove(IFile iFile, boolean hasErrors) {
 		// FIXME: Gambis pra pegar FQN pelo caminho do arquivo. Melhorar depois
 		// usando o source folder do projeto
 		String srcFolder = "/src/";
 		String projectName = iFile.getProject().getName();
 		int index = iFile.getFullPath().toOSString().indexOf(projectName);
 		String classFqn = iFile.getFullPath().toOSString().substring(
 				index + projectName.length() + srcFolder.length()).replaceAll(
 				"/", ".").replaceAll(".java", "");
 		classFqn = projectName + "." + classFqn;
 
 		try {
 			LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 			if (lhBaseFile != null) {
 
 				LighthouseDelta delta = new LighthouseDelta(Activator
 						.getDefault().getAuthor(), lhBaseFile, null);
 
 				PushModel pushModel = new PushModel(LighthouseModel
 						.getInstance());
 				pushModel.updateModelFromEvents(delta.getEvents());
 				fireModificationsToUI(delta.getEvents());
 			}
 		} catch (Exception e) {
 			logger.error(e,e);
 			UserDialog.openError(e.getMessage());
 		}
 	}
 	
 	/** The method add happens when a class is added in the workspace or when is checkout a project in a empty workspace. */
 	@Override
 	public void add(IFile iFile, boolean hasErrors) {
 		final String classFqn = getClassFullyQualifiedName(iFile);
 		
 		// It is a new class created by the user. We are assuming that the user creates a class that doesn't contain errors.
 		if (!mapClassToSVNCommittedTime.containsKey(classFqn)) {
 			/* We have to put the classFqn in this map in order to show the event in the UI.*/
 			mapClassToSVNCommittedTime.put(classFqn, new Date(0));
 			// TODO: Think about DB operations in a thread
 			try {
 				Collection<LighthouseEvent> deltaEvents = generateDeltaFromBaseVersion(Collections.singleton(iFile),true);
 				PushModel pushModel = new PushModel(
 						LighthouseModel.getInstance());
 				pushModel.updateModelFromEvents(deltaEvents);
 				fireModificationsToUI(deltaEvents);
 			} catch (Exception e) {
 				// TODO: Try to throw up this exception
 				logger.error(e,e);
 				UserDialog.openError(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		try{
 			//FIXME: Check this line
 			JPAUtility.initializeEntityManagerFactory(DatabasePreferences.getDatabaseSettings());
 			if (threadSuspended){
 				synchronized(this) {
 					StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 					threadSuspended = false;
 					notify();
 				}
 			}
 		} catch (JPAException e){
 			logger.error(e,e);
 			StatusWidget.getInstance().setStatus(Status.CANCEL_STATUS);
 		}
 	}
 
 	private synchronized Date getTimestamp(){
 		return new Date();
 	}
 
 }
