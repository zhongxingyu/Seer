 package edu.uci.lighthouse.model;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import edu.uci.lighthouse.model.LighthouseRelationship.TYPE;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.LHEntityDAO;
 import edu.uci.lighthouse.model.jpa.LHRelationshipDAO;
 import edu.uci.lighthouse.model.util.LHStringUtil;
 import edu.uci.lighthouse.model.util.UtilModifiers;
 
 /**
  * This is a utility class for building model objects. Its purpose is to
  * assist in constructing models for test cases, but it is useful any time a
  * model needs to be constructed from code.
  * 
  * All the methods are static.
  * 
  */
 public class LighthouseModelManager {
 	
 	private static Logger logger = Logger.getLogger(LighthouseModelManager.class);
 	
 	protected LighthouseModel model;
 
 	public LighthouseModelManager(LighthouseModel model) {
 		super();
 		this.model = model;
 	}
 
 	private LighthouseEntity addEntity(LighthouseEntity newEntity){
 		LighthouseEntity entity = model.getEntity(newEntity.getFullyQualifiedName());
 		if (entity!=null) {
 			return entity;
 		} else {
 			model.addEntity(newEntity);
 			return newEntity;	
 		}
 	}
 	
 	private LighthouseRelationship addRelationship(LighthouseRelationship newRelationship){
 		LighthouseRelationship relationship = model.getRelationship(newRelationship);
 		if (relationship == null) {
 			relationship = newRelationship;
 		}
 		handleEntitiesNotInsideClass(relationship);		
 		model.addRelationship(relationship);
 		return newRelationship;
 	}
 	
 	/** Need this method for add the Entities: ExternalClass and Modifiers in the LHBaseFile
 	 * because they have not TYPE.INSIDE relationship */
 	private void handleEntitiesNotInsideClass(
 			LighthouseRelationship relationship) {
 		LighthouseEntity fromEntity = relationship.getFromEntity();
 		LighthouseEntity toEntity = relationship.getToEntity();
 		if (fromEntity instanceof LighthouseExternalClass) {
 			addEntity(fromEntity);
 		}
 		if (toEntity instanceof LighthouseExternalClass) {
 			addEntity(toEntity);
 		}
 		if (relationship.getType()==TYPE.MODIFIED_BY) {
 			addEntity(toEntity);
 		}
 	}
 	
 	/** Add <code>event</code> in the LighthouseModel, however do not add the event in the database*/
 	public void addEvent(LighthouseEvent event) {
 		Object artifact = addArtifact(event.getArtifact());
 		event.setArtifact(artifact);
 		model.addEvent(event);
 	}
 	
 	/**
 	 * @param artifact
 	 * 		{@link LighthouseEntity}
 	 * 		OR
 	 * 		{@link LighthouseRelationship}
 	 * */
 	public Object addArtifact(Object artifact) {
 		if (artifact instanceof LighthouseEntity) {
 			LighthouseEntity entity = addEntity((LighthouseEntity) artifact);
 			return entity;
 		} else if (artifact instanceof LighthouseRelationship) {
 			LighthouseRelationship relationship = addRelationship((LighthouseRelationship) artifact);
 			return relationship;
 		}
 		return null;
 	}
 	
 	public LighthouseEntity getEntity(String fqn) {
 		return model.getEntity(fqn);
 	}
 	
 	/** 
 	 * Used to import a new project in the database
 	 * 
 	 * After parsing a new Project we end up having a list of entities and a list of relationship.
 	 * Then we need to populate the LighthouseModel with events based in those lists.
 	 * This method is responsible to create such events, and add then in the model.
 	 * 
 	 * */
 	public Collection<LighthouseEvent> createEventsAndSaveInLhModel(LighthouseAuthor author, Collection<LighthouseEntity> listEntities, Collection<LighthouseRelationship> listLighthouseRelationships) {
 		Collection<LighthouseEvent> listEvents = new LinkedList<LighthouseEvent>();
 		for (LighthouseEntity entity : listEntities) {
 			LighthouseEvent event = new LighthouseEvent(LighthouseEvent.TYPE.ADD,author,entity);
 			event.setCommitted(true);
 			event.setCommittedTime(new Date(0));
 			event.setTimestamp(new Date(0));
 			addArtifact(entity);
 			listEvents.add(event);
 			logger.debug("Add entity in model: " + entity);
 		}
 		for (LighthouseRelationship rel : listLighthouseRelationships) {
 			LighthouseEvent event = new LighthouseEvent(LighthouseEvent.TYPE.ADD,author,rel);
 			event.setCommitted(true);
 			event.setCommittedTime(new Date(0));
 			event.setTimestamp(new Date(0));
 			addArtifact(rel);
 			listEvents.add(event);
 			logger.debug("Add relationship in model: " + rel);
 		}
 		return listEvents;
 	}
 
 	/**
 	 * Remove Artifacts that are inside of the list of classes,
 	 * and also remove the Events related to those Artifacts
 	 * */
 	public void removeArtifactsAndEvents(Collection<String> listClazzFqn) {
 		Collection<LighthouseEntity> listEntity = LighthouseModelUtil.getEntitiesInsideClasses(model, listClazzFqn);
 		Collection<LighthouseRelationship> listRel = LighthouseModelUtil.getRelationships(model, listEntity);
 		LinkedHashSet<LighthouseEvent> listEvents = LighthouseModelUtil.getEventsByListEntityAndRel(model, listEntity, listRel);
 		for (LighthouseEvent event : listEvents) {
 			model.removeEvent(event);
 		}
 		for (LighthouseRelationship rel : listRel) {
 			model.removeRelationship(rel);
 		}
 		for (LighthouseEntity entity : listEntity) {
 			model.removeEntity(entity);
 		}
 	}
 
	public void removeCommittedEventsAndArtifacts(Collection<String> listClazzFqn, Date eventTime) {
 		Collection<LighthouseEntity> listEntity = LighthouseModelUtil.getEntitiesInsideClasses(model, listClazzFqn);
 		Collection<LighthouseRelationship> listRel = LighthouseModelUtil.getRelationships(model, listEntity);
 		LinkedHashSet<LighthouseEvent> listEvents = LighthouseModelUtil.getEventsByListEntityAndRel(model, listEntity, listRel);
 		for (LighthouseEvent event : listEvents) {
 			if (event.isCommitted()) {
 				// If I am allowed to commit, it is supposed that I am working on the last version,
 				// so I can remove every committed event, because all of them will be in the past
				if (event.getType()==LighthouseEvent.TYPE.REMOVE) {
					model.removeEventAndArtifact(event);
				} else {
					model.removeEvent(event);
				}
 			}
 		}
 	}
 	
 	
 	//TODO: Put those methods in another class (think later about that)
 
 	
 	public LighthouseEntity getEntityFromDatabase(String fqn) throws JPAException {
 		LighthouseEntity entity = getEntity(fqn);
 		if (entity==null) {
 			try {
 				entity = new LHEntityDAO().get(LHStringUtil.getMD5Hash(fqn));
 			} catch (Exception e) {
 				logger.error(e,e);
 			}
 		}
 		return entity;
 	}
 	
 	public LinkedHashSet<LighthouseEntity> selectEntitiesInsideClass(String fqnClazz) throws JPAException {
 		return selectEntitiesInsideClass(new LinkedHashSet<LighthouseEntity>(),fqnClazz);
 	}
 	
 	/**
 	 * Going to the database to return the entities inside a class
 	 * Recursive method
 	 * @param listEntitiesInside should be a new LinkedHashSet()
 	 * @throws JPAException 
 	 * */ 
 	private LinkedHashSet<LighthouseEntity> selectEntitiesInsideClass(LinkedHashSet<LighthouseEntity> listEntitiesInside, String fqnClazz) throws JPAException {
 		LighthouseClass clazz = new LighthouseClass(fqnClazz);
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("relType", LighthouseRelationship.TYPE.INSIDE);
 		parameters.put("toEntity", clazz);
 		List<LighthouseEntity> subListEntitiesInside = new LHRelationshipDAO().executeNamedQueryGetFromEntityFqn("LighthouseRelationship.findFromEntityByTypeAndToEntity", parameters);
 		for (LighthouseEntity entity : subListEntitiesInside) {
 			if (entity instanceof LighthouseClass || entity instanceof LighthouseInterface) { // That is a Inner class
 				listEntitiesInside.addAll(selectEntitiesInsideClass(listEntitiesInside, entity.getFullyQualifiedName()));
 			}
 		}
 		listEntitiesInside.addAll(subListEntitiesInside);
 		LighthouseEntity entityClazz = getEntityFromDatabase(fqnClazz);
 		if (entityClazz instanceof LighthouseClass || entityClazz instanceof LighthouseInterface) {
 			listEntitiesInside.add(entityClazz);
 		}
 		return listEntitiesInside;
 	}
 	
 	
 	
 	/*  PUT THOSE METHODS BELLOW IN THE LighthouseModelUtil.java */
 	
 	//TODO: Put methods MyClass, isStatic and other to another class.
 	
 	/**
 	 * Returns the associate class or <code>null</code> otherwise.
 	 * 
 	 * @param model
 	 * @param e
 	 * @return
 	 */
 	public LighthouseClass getMyClass(LighthouseEntity e){
 		String classFqn = null;
 		if (e instanceof LighthouseClass){
 			classFqn = e.getFullyQualifiedName();
 		} else if (e instanceof LighthouseField){
 			classFqn = e.getFullyQualifiedName().replaceAll("(\\.\\w+)\\z", "");
 		} else if (e instanceof LighthouseMethod){
 			classFqn = e.getFullyQualifiedName().replaceAll("\\.[\\<\\w]*\\w+[\\>\\w]*\\(.*\\)","");
 		}
 		LighthouseEntity c = model.getEntity(classFqn);
 		if (c instanceof LighthouseClass){
 			return (LighthouseClass)c;
 		}
 		return null;
 	}
 	
 	public boolean isStatic(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isStatic(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean isFinal(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isFinal(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean isSynchronized(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isSynchronized(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isPublic(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isPublic(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean isProtected(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isProtected(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean isPrivate(LighthouseEntity e){
 		Collection<LighthouseRelationship> list =  model.getRelationshipsFrom(e);
 		for (LighthouseRelationship r : list) {
 			if (r.getToEntity() instanceof LighthouseModifier){
 				if (UtilModifiers.isPrivate(r.getToEntity().getFullyQualifiedName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 }
