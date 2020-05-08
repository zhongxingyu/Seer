 package edu.uci.lighthouse.core.controller;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.parser.LighthouseParser;
 import edu.uci.lighthouse.core.util.ModelUtility;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseInterface;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseModelUtil;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.LHEventDAO;
 import edu.uci.lighthouse.model.jpa.LHRepositoryEventDAO;
 import edu.uci.lighthouse.model.repository.LighthouseRepositoryEvent;
 import edu.uci.lighthouse.parser.ParserException;
 
 public class PushModel {
 	
 	private static Logger logger = Logger.getLogger(PushModel.class);
 
 	private LighthouseModel model;
 	
 	public PushModel(LighthouseModel model) {
 		this.model = model;
 	}
 	
 	public void updateDatabaseFromEvents(Collection<LighthouseEvent> listEvents) throws JPAException {   
 		new UpdateLighthouseModel(model).updateByEvents(listEvents);
 	    saveEventsInDatabase(listEvents,null);
 	}
 	
 	public Collection<LighthouseEvent> updateCommittedEvents(List<String> listClazzFqn, Date svnCommittedTime, LighthouseAuthor author) throws JPAException {
 		Collection<LighthouseEvent> listEvents = LighthouseModelUtil.getEventsInside(model, listClazzFqn); 
 		LinkedHashSet<LighthouseEvent> listEventsToCommitt = new LinkedHashSet<LighthouseEvent>();
 		for (LighthouseEvent event : listEvents) {
 			if (event.getAuthor().equals(author)) {
 				if (!event.isCommitted()) {
 					event.setCommitted(true);
 					event.setCommittedTime(svnCommittedTime);
 					listEventsToCommitt.add(event);
 				}
 			}
 		}
 //		FIXME remove the following line
 //		new LHEventDAO().updateCommittedEvents(listEventsToCommitt, svnCommittedTime);
 		saveEventsInDatabase(listEventsToCommitt,null);
 		return listEventsToCommitt;
 	}
 	
 	public Collection<LighthouseEvent> parseJavaFiles(Collection<IFile> javaFiles)
 			throws ParserException, JPAException {
 		if (javaFiles.size() > 0) {
 			LighthouseParser parser = new LighthouseParser();
 			parser.execute(javaFiles);
 			Collection<LighthouseEntity> listEntities = parser.getListEntities();
 			// set SVN time as newDate(1969-12-31 16:00:00)
			Map<String, Date> mapClassToSVNCommittedTime = Controller.getInstance().getWorkingCopy();
 			for (LighthouseEntity entity : listEntities) {
 				if (entity instanceof LighthouseClass 
 						|| entity instanceof LighthouseInterface) {
 					mapClassToSVNCommittedTime.put(entity.getFullyQualifiedName(), new Date(0));
 				}
 			}
 			Collection<LighthouseRelationship> listLighthouseRel = parser.getListRelationships();
 			LighthouseModelManager modelManager = new LighthouseModelManager(model);
 			Collection<LighthouseEvent> listEvents = modelManager
 					.createEventsAndSaveInLhModel(Activator.getDefault()
 							.getAuthor(), listEntities, listLighthouseRel);
 			return listEvents;
 		}
 		return null;
 	}
 
 	public void saveEventsInDatabase(final Collection<LighthouseEvent> listEvents,	IProgressMonitor monitor) throws JPAException {
 		LHEventDAO dao = new LHEventDAO();
 		dao.saveListEvents(listEvents,monitor);		
 	}
 	
 	public void saveRepositoryEvent(Map<IFile, ISVNInfo> svnFiles, 
 			LighthouseRepositoryEvent.TYPE type,
 			Date eventTime
 			) throws JPAException {
 
 		/* We make this to navigate the class names and their
 		 * properties in the same order when calling 
 		 * methods getClassesFullyQualifiedName and
 		 * getFilesRevisionNumbers
 		 */
 		IFile [] iFiles = svnFiles.keySet().toArray(new IFile[0]);
 		List<String> classNames = getClassesFullyQualifiedNameInOrder(svnFiles, iFiles); 
 		LighthouseAuthor author = Activator.getDefault().getAuthor(); 
 		List<Number> versionsAffected = getFilesRevisionNumbersInOrder(svnFiles, iFiles);
 		
 		// If there is not a version number for each class name, return
 		if (classNames.size() != versionsAffected.size())
 			return;
 		
 		Collection<LighthouseRepositoryEvent> listRepEvents = new LinkedList<LighthouseRepositoryEvent>();
 		for (int i = 0; i < classNames.size(); i++) {
 			LighthouseRepositoryEvent repEvent = new LighthouseRepositoryEvent(author,type,classNames.get(i),eventTime,versionsAffected.get(i).longValue());
 			listRepEvents.add(repEvent);
 		}
 		LHRepositoryEventDAO dao = new LHRepositoryEventDAO();
 		dao.saveRepositoryEvents(listRepEvents);
 	}
 	
 	private List<String> getClassesFullyQualifiedNameInOrder(
 			Map<IFile, ISVNInfo> svnFiles, IFile [] iFiles) {
 		LinkedList<String> result = new LinkedList<String>();
 		for (IFile iFile : iFiles) {
 			String fqn = ModelUtility.getClassFullyQualifiedName(iFile);
 			if (fqn != null) {
 				result.add(fqn);
 			}
 		}
 		return result;
 	}
 
 	private List<Number> getFilesRevisionNumbersInOrder(
 			Map<IFile, ISVNInfo> svnFiles, IFile [] iFiles) {
 		LinkedList<Number> result = new LinkedList<Number>();
 		for (IFile iFile : iFiles) {
 			Number svnRevisionNumber = svnFiles.get(iFile).getRevision().getNumber();
 			if (svnRevisionNumber != null) {
 				result.add(svnRevisionNumber);
 			}
 		}
 		return result;
 	}
 	
 }
