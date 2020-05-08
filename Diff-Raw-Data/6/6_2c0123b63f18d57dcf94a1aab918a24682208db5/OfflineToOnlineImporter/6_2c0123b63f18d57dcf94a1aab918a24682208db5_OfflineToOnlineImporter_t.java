 package org.iucn.sis.server.extensions.offline;
 
 import java.io.Writer;
 import java.sql.BatchUpdateException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.NoteIO;
 import org.iucn.sis.server.api.io.ReferenceIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
 import org.iucn.sis.server.api.locking.FileLocker;
 import org.iucn.sis.server.api.locking.HibernateLockRepository;
 import org.iucn.sis.server.api.locking.LockException;
 import org.iucn.sis.server.api.persistance.FieldDAO;
 import org.iucn.sis.server.api.persistance.PrimitiveFieldDAO;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.WorkingSetCriteria;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.utils.RegionConflictException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Relationship;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.models.interfaces.ForeignObject;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.util.DynamicWriter;
 import com.solertium.util.events.ComplexListener;
 
 public class OfflineToOnlineImporter extends DynamicWriter implements Runnable {
 	
 	public enum OfflineImportMode {
 		RESYNC, REMOVE
 	}
 
 	private final Map<Integer, Integer> offlineNotesMap, offlineReferencesMap;
 	private final Properties targetProperties;
 	private final String username;
 	
 	private SISPersistentManager onlineTargetManager;
 	private OfflineImportMode mode;
 
 	protected Session offline;
 	protected Session online;
 	protected User loggedInUser;
 	
 	private Writer out;
 	private String lineBreakRule;
 	private Set<Integer> importedAssessments;
 
 	public OfflineToOnlineImporter(String username, Properties properties) {
 		super();
 		this.targetProperties = properties;
 		this.username = username;
 		this.mode = OfflineImportMode.RESYNC;
 		this.offlineNotesMap = new HashMap<Integer, Integer>();
 		this.offlineReferencesMap = new HashMap<Integer, Integer>();
 	}
 	
 	@Override
 	public void setOutputStream(Writer writer, String lineBreakRule) {
 		super.setOutputStream(writer, lineBreakRule);
 		this.out = writer;
 		this.lineBreakRule = lineBreakRule;
 	}
 	
 	public void setMode(OfflineImportMode mode) {
 		this.mode = mode;
 	}
 
 	public final void run() {
 		Date start = Calendar.getInstance().getTime();
 
 		write("Export started at %s", start);
 		
 		importedAssessments = new HashSet<Integer>();
 
 		try {
			if (username == null || "".equals(username))
				throw new NullPointerException("No username supplied. Stopping.");
			
 			// The offline database.
 			offline = SISPersistentManager.instance().openSession();
 	
 			// Live DB will NEVER be from scratch...
 			onlineTargetManager = SISPersistentManager.newInstance("sis_target", targetProperties, false);
 			online = onlineTargetManager.openSession();
 			
 			loggedInUser = new UserIO(offline).getUserFromUsername(username);
			if (loggedInUser == null)
				throw new NullPointerException("No user found with the username \"" + username + "\". Stopping.");
 
 			execute();
 		} catch (Throwable e) {
 			Debug.println(e);
 			write("Failed unexpectedly: %s\n%s", e.getMessage(), toString(e));
 			
 			if (e.getCause() instanceof BatchUpdateException) {
 				Debug.println("Full error: {0}", 
 					toString(((BatchUpdateException)e.getCause()).getNextException()));
 			}
 		} finally {
 			Date end = Calendar.getInstance().getTime();
 
 			long time = end.getTime() - start.getTime();
 			int secs = (int) (time / 1000);
 			int mins = (int) (secs / 60);
 
 			write("Export completed at %s in %s minutes, %s seconds.", end, mins, secs);
 			write("<br/><a href=\"../manager\">Click here to return to the Offline Manager.</a>");
 
 			close();
 		}
 	}
 	
 	protected String toString(Throwable e) {
 		final StringBuilder out = new StringBuilder();
 		out.append(e.toString() + "\r\n");
 		for (StackTraceElement el : e.getStackTrace())
 			out.append(el.toString() + "\r\n");
 		int count = 0;
 		Throwable t = e;
 		while (count++ < 10 && (t = t.getCause()) != null) {
 			out.append(t.toString() + "\r\n");
 			for (StackTraceElement el : t.getStackTrace())
 				out.append(el.toString() + "\r\n");
 		}
 		return out.toString();
 	}
 
 	@Override
 	public void close() {
 		super.close();
 		offline.close();
 		online.close();
 		onlineTargetManager.shutdown();
 	}
 
 	protected void execute() throws PersistentException {
 		// Update Foreign Object metadata first 
 		pushMetadataToOnline();
 		
 		// Update Assessments created Offline
 		AssessmentIO assessmentIO = new AssessmentIO(offline);
 		Assessment[] offlineAssessments = assessmentIO.getOfflineCreatedAssessments();
 		if (offlineAssessments.length > 0) {
 			write("Writing Offline Assessments");
 			insertOfflineAssessments(offlineAssessments);
 		} else
 			write("Detected no assessments created offline");
 		
 		offline.clear();
 		
 		//Merge the assessment data
 		for (WorkingSet ws : SISPersistentManager.instance().listObjects(WorkingSet.class, offline)) {
 			write("Synchronizing data for working set %s (#%s)", ws.getName(), ws.getId());
 			
 			if (mergeAssessments(ws)) {
 				if (OfflineImportMode.RESYNC.equals(mode)) {
 					write("-- Creating Database Backup --");
 					OfflineBackupWorker.backup();
 					
 					write("-- Re-syncing online data to offline --");
 					OfflineWorkingSetReExporter exporter = 
 						new OfflineWorkingSetReExporter(ws.getId(), username, onlineTargetManager);
 					exporter.setOutputStream(out, lineBreakRule);
 					exporter.run();
 					write("-- Database synchronized successfully --");
 				}
 			}
 		}
 
 		write("Sync data to Online Complete.");
 		
 		if (OfflineImportMode.REMOVE.equals(mode)) {
 			//TODO: Disconnect database, backup, delete.
 			write("Database image backed up to " + OfflineBackupWorker.backup());
 			
 			SISPersistentManager.instance().shutdown();
 			
 			OfflineBackupWorker.delete();
 		}
 	}
 	
 	private WorkingSet createOnlineWorkingSet(WorkingSet source) {
 		source.toXML();
 		
 		online.beginTransaction();
 		
 		User user = new UserIO(online).getUserFromUsername(source.getCreatorUsername());
 		
 		WorkingSet target = new WorkingSet();
 		if (source.getAssessmentTypes() != null)
 			for (AssessmentType type : source.getAssessmentTypes())
 				target.getAssessmentTypes().add((AssessmentType)online.load(AssessmentType.class, type.getId()));
 		if (source.getRegion() != null)
 			for (Region region : source.getRegion())
 				target.getRegion().add((Region)online.load(Region.class, region.getId()));
 		if (source.getRelationship() != null)
 			target.setRelationship((Relationship)online.load(Relationship.class, source.getRelationship().getId()));
 		
 		target.setIsMostRecentPublished(source.getIsMostRecentPublished());
 		target.setCreatedDate(source.getCreatedDate());
 		target.setCreator(user);
 		target.setDescription(source.getDescription());
 		target.setName(source.getName());
 		target.setNotes(source.getNotes());
 		target.getUsers().add(user);
 		for (Taxon taxon : source.getTaxon())
 			target.getTaxon().add((Taxon)online.load(Taxon.class, taxon.getId()));
 		
 		online.save(target);
 		
 		online.getTransaction().commit();
 		
 		return target;
 	}
 	
 	private boolean mergeAssessments(final WorkingSet offlineWorkingSet) throws PersistentException {
 		final AssessmentIO assessmentIO = new AssessmentIO(offline);
 		
 		WorkingSet workingSet = getOnlineWorkingSet(offlineWorkingSet);
 		if (workingSet == null) {
 			workingSet = createOnlineWorkingSet(offlineWorkingSet);
 		}
 		else {
 			Hibernate.initialize(workingSet.getUsers());
 			Hibernate.initialize(workingSet.getAssessmentTypes());
 			// workingSet.toXML();
 		}
 
 		final AssessmentFilterHelper helper = new AssessmentFilterHelper(online, workingSet.getFilter());
 		final int size = workingSet.getTaxon().size();
 		int count = 0;
 		write("Beginning import for %s taxa", size);
 		for (Taxon taxon : workingSet.getTaxon()) {
 			Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
 			write("Copying %s eligible assessments for %s, (%s/%s)", assessments.size(), taxon.getFullName(), ++count,
 					size);
 
 			for (Assessment targetAsm : assessments) {
 				if (importedAssessments.contains(targetAsm.getId()))
 					continue;
 				
 				// Fetch Assessments
 				offline.clear();
 				Assessment sourceAsm = assessmentIO.getAssessment(targetAsm.getId());
 				if (sourceAsm == null)
 					write(" - Could not find offline version of assesment #%s", targetAsm.getId());
 				else {
 					try {
 						syncAssessment(sourceAsm, targetAsm);
 						write(" + Write successful!");
 						importedAssessments.add(targetAsm.getId());
 					} catch (ResourceException e) {
 						write(" - Write failed.");
 						if (online.getTransaction() != null) {
 							try {
 								online.getTransaction().rollback();
 							} catch (Exception f) {
 								Debug.println(f);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	private WorkingSet getOnlineWorkingSet(WorkingSet offlineWorkingSet) {
 		WorkingSetCriteria criteria = new WorkingSetCriteria(online);
 		criteria.id.eq(offlineWorkingSet.getId());
 		criteria.name.eq(offlineWorkingSet.getName());
 		criteria.createCreatorCriteria().username.eq(offlineWorkingSet.getCreatorUsername());
 		
 		try {
 			return criteria.uniqueWorkingSet();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	private void pushMetadataToOnline() throws PersistentException {
 		UserIO userIO = new UserIO(offline);
 		User[] offlineUsers = userIO.getOfflineCreatedUsers();
 		if (offlineUsers.length > 0) {
 			write("Writing %s Offline Users", offlineUsers.length);
 			insertOfflineMetadata(offlineUsers, null);
 		} else
 			write("Detected no users created offline");
 		
 		offline.clear();
 
 		// Update References created Offline
 		ReferenceIO referenceIO = new ReferenceIO(offline);
 		Reference[] offlineReferences = referenceIO.getOfflineCreatedReferences();
 		if (offlineReferences.length > 0) {
 			write("Writing %s Offline References", offlineReferences.length);
 			insertOfflineMetadata(offlineReferences, offlineReferencesMap);
 		} else
 			write("Detected no references created offline");
 		
 		offline.clear();
 		
 		// Update Notes created Offline
 		NoteIO notesIO = new NoteIO(offline);
 		Notes[] offlineNotes = notesIO.getOfflineCreatedNotes();
 		if (offlineNotes.length > 0) {
 			write("Writing %s Offline Notes", offlineNotes.length);
 			insertOfflineMetadata(offlineNotes, offlineNotesMap);
 		}
 		else
 			write("Detected no notes created offline");
 		
 		offline.clear();
 	}
 
 	private void insertOfflineAssessments(Assessment[] offlineAssessments) throws PersistentException {
 		if (offlineAssessments.length > 0) {
 			online.clear();
 			
 			final AssessmentIO io = new AssessmentIO(online);
 			for (Assessment assessment : offlineAssessments) {
 				online.beginTransaction();
 				
 				assessment.toXML();
 				
 				write("  Writing new assessment #%s", assessment.getId());
 				
 				Assessment newAssessment = assessment.getOfflineCopy();
 				newAssessment.setOfflineStatus(false);
 
 				final AssessmentIOWriteResult result;
 				try {
 					result = io.saveNewAssessment(newAssessment, loggedInUser);
 				} catch (RegionConflictException e) {
 					write(" - Failed to save new assessment: regions conflict.");
 					online.getTransaction().rollback();
 					continue;
 				}
 				
 				if (result.status.isSuccess())
 					write(" + Write successful.");
 				else
 					write(" - Write failed.");
 				
 				online.getTransaction().commit();
 			}
 		}		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void insertOfflineMetadata(ForeignObject[] objects, Map<Integer, Integer> storage) {
 		if (objects.length > 0) {
 			for (ForeignObject<ForeignObject> object : objects) {
 				online.clear();
 				online.beginTransaction();
 				ForeignObject data = object.getOfflineCopy();
 				data.setOfflineStatus(false);
 				online.save(data);
 				online.flush();
 				online.getTransaction().commit();
 				
 				if (storage != null) {
 					storage.put(object.getId(), data.getId());
 					write(" + Added %s %s online with ID %s", object.getClass().getSimpleName(), object.getId(), data.getId());
 				}
 			}
 		}
 	}
 	
 	private void write(String template, Object... args) {
 		write(String.format(template, args));
 	}
 
 	private synchronized void syncAssessment(Assessment sourceAsm, Assessment targetAsm) throws PersistentException,
 			ResourceException {
 		online.beginTransaction();
 		
 		/*
 		 * Initialize everything this way because it's lazy & recursive.
 		 */
 		sourceAsm.toXML();
 
 		final Map<Integer, Reference> targetRefs = new HashMap<Integer, Reference>();
 		for (Reference reference : targetAsm.getReference())
 			targetRefs.put(reference.getId(), reference);
 
 		for (Reference sourceRef : sourceAsm.getReference()) {
 			if (sourceRef.getId() == 0)
 				continue;
 
 			Reference targetRef = targetRefs.remove(sourceRef.getId());
 			if (targetRef == null) {
 				Reference ref = SISPersistentManager.instance().
 					getObject(offline, Reference.class, sourceRef.getId());
 				if (ref != null)
 					targetAsm.getReference().add(ref);
 			}
 		}
 
 		targetAsm.getReference().removeAll(targetRefs.values());
 		targetAsm.toXML();
 
 		final OfflineAssessmentPersistence saver = new OfflineAssessmentPersistence(online, targetAsm);
 		saver.setOfflineNotes(offlineNotesMap);
 		saver.setOfflineReferences(offlineReferencesMap);
 		saver.setAllowAdd(true);
 		saver.setAllowDelete(true);
 		saver.setAllowManageNotes(true);
 		saver.setAllowManageReferences(true);
 		saver.setDeleteFieldListener(new ComplexListener<Field>() {
 			public void handleEvent(Field field) {
 				try {
 					FieldDAO.deleteAndDissociate(field, online);
 				} catch (PersistentException e) {
 					Debug.println(e);
 				}
 			}
 		});
 		saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField<?>>() {
 			public void handleEvent(PrimitiveField<?> field) {
 				try {
 					PrimitiveFieldDAO.deleteAndDissociate(field, online);
 				} catch (PersistentException e) {
 					Debug.println(e);
 				}
 			}
 		});
 
 		try {
 			saver.sink(sourceAsm);
 		} catch (PersistentException e) {
 			Debug.println(e);
 			write(" - Error merging changes, could not save: %s", e.getMessage());
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		Hibernate.initialize(targetAsm.getEdit());
 
 		// This may or may not need to happen for hibernate reasons...
 		targetAsm.toXML();
 
 		AssessmentIO assessmentIO = new AssessmentIO(online);
 		if (!assessmentIO.allowedToCreateNewAssessment(targetAsm)) {
 			write(" - Another assessment in this region already exists, could not save");
 			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, new RegionConflictException());
 		}
 		
 		offline.clear();
 
 		AssessmentIOWriteResult result = 
 			assessmentIO.writeAssessment(targetAsm, loggedInUser, "Sync'd from Offline",
 				true);
 
 		online.flush();
 
 		if (!result.status.isSuccess()) {
 			write(" - Error saving merged changes, could not save.");
 			throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
 		}
 		
 		online.getTransaction().commit();
 
 		if (result.edit == null)
 			Debug.println("Error: No edit associated with this change. Not backing up changes.");
 		else
 			saver.saveChanges(targetAsm, result.edit, onlineTargetManager);
 
 		/*
 		 * After successful merge, unlock online assessment.
 		 */
 		try {
 			// Create objects
 			HibernateLockRepository repository = new HibernateLockRepository(onlineTargetManager);
 			FileLocker locker = new FileLocker(repository);
 
 			// Unlock Live Assessment
 			Status status = locker.persistentEagerRelease(sourceAsm.getId(), loggedInUser);
 			if (status.isSuccess())
 				write(" + Assessment unlocked.");
 			else if (Status.CLIENT_ERROR_NOT_FOUND.equals(status))
 				write(" - No lock found for this assessment.");
 			else if (Status.CLIENT_ERROR_FORBIDDEN.equals(status))
 				write(" - Can not unlock this assessment, not yours.");
 		} catch (LockException e) {
 			write(" - Unable to unlock assessment %s: %s", sourceAsm.getId(), e.getMessage());
 		}
 	}
 
 }
