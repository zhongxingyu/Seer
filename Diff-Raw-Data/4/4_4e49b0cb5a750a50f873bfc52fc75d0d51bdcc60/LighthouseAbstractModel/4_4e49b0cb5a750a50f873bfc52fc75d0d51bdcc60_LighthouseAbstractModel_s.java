 package edu.uci.lighthouse.model;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uci.lighthouse.model.LighthouseRelationship.TYPE;
 
 /**
  * It consist of a list of entities and a list of relationships
  * */
 public abstract class LighthouseAbstractModel {
 
 	private HashMap<String, LighthouseEntity> entities = new HashMap<String, LighthouseEntity>();
 
 	/** Indexed by ToEntity. */
 	private HashMap<String, LinkedHashSet<LighthouseRelationship>> relationshipsTo = new HashMap<String, LinkedHashSet<LighthouseRelationship>>();
 
 	/** Indexed by FromEntity. */
 	private HashMap<String, LinkedHashSet<LighthouseRelationship>> relationshipsFrom = new HashMap<String, LinkedHashSet<LighthouseRelationship>>();
 	
 	private LinkedHashSet<String> packageNames = new LinkedHashSet<String>();
 	
 	private LinkedHashSet<String> projectNames = new LinkedHashSet<String>();
 	
 	private LinkedHashSet<LighthouseClass> classes = new LinkedHashSet<LighthouseClass>();
 
 	/**
 	 * Only {@link LighthouseModelManager} is allowed to call this method
 	 * */
 	final synchronized void addEntity(LighthouseEntity entity) {
 		/* We are interested just in classes and interfaces that belongs our project. External classes are not included. */
 		if (entity instanceof LighthouseClass || entity instanceof LighthouseInterface) {
 			packageNames.add(entity.getPackageName());
 			projectNames.add(entity.getProjectName());
 			if (entity instanceof LighthouseClass){
 				classes.add((LighthouseClass)entity);
 			}
 		}
 		entities.put(entity.getFullyQualifiedName(), entity);
 	}
 
 	/**
 	 * Only {@link LighthouseModelManager} is allowed to call this method
 	 * */
 	final synchronized void removeEntity(LighthouseEntity entity) {
 		/* Is not necessary remove project and package names since the list is populated everytime Lighthouse runs.*/
 		classes.remove(entity);
 		entities.remove(entity.getFullyQualifiedName());
 	}
 	
 	public Collection<String> getPackageNames(){
 		return packageNames;
 	}
 	
 	/**
 	 * Only {@link LighthouseModelManager} is allowed to call this method
 	 * */
 	final synchronized void addRelationship(LighthouseRelationship rel) {
 		LinkedHashSet<LighthouseRelationship> listFrom = relationshipsFrom
 				.get(rel.getFromEntity().getFullyQualifiedName());
 		LinkedHashSet<LighthouseRelationship> listTo = relationshipsTo.get(rel
 				.getToEntity().getFullyQualifiedName());
 		if (listFrom == null) {
 			listFrom = new LinkedHashSet<LighthouseRelationship>();
 			relationshipsFrom.put(rel.getFromEntity().getFullyQualifiedName(),
 					listFrom);
 		}
 		listFrom.add(rel);
 		if (listTo == null) {
 			listTo = new LinkedHashSet<LighthouseRelationship>();
 			relationshipsTo.put(rel.getToEntity().getFullyQualifiedName(),
 					listTo);
 		}
 		listTo.add(rel);
 	}
 
 	/**
 	 * Only {@link LighthouseModelManager} is allowed to call this method
 	 * */
 	final synchronized void removeRelationship(LighthouseRelationship rel) {
 		Collection<LighthouseRelationship> listTo = relationshipsTo.get(rel
 				.getToEntity().getFullyQualifiedName());
 		if (listTo != null) {
 			listTo.remove(rel);
 		}
 		Collection<LighthouseRelationship> listFrom = relationshipsFrom.get(rel
 				.getFromEntity().getFullyQualifiedName());
 		if (listFrom != null) {
 			listFrom.remove(rel);
 		}
 	}
 
 	/**
 	 * Verify if a entity exist in the model
 	 * */
 	public boolean containsEntity(String fqn) {
 		return (this.getEntity(fqn) != null);
 	}
 
 	public LighthouseEntity getEntity(String fqn) {
 		return entities.get(fqn);
 	}
 
 	public Collection<LighthouseEntity> getMethodsAndAttributesFromClass(
 			LighthouseClass c) {
 		LinkedList<LighthouseEntity> result = new LinkedList<LighthouseEntity>();
 		Collection<LighthouseRelationship> list = relationshipsTo.get(c
 				.getFullyQualifiedName());
 		if (list != null) {
 			for (LighthouseRelationship r : list) {
 				if (((r.getFromEntity() instanceof LighthouseField) || r
 						.getFromEntity() instanceof LighthouseMethod)
 						&& (r.getType() == LighthouseRelationship.TYPE.INSIDE)) {
 					result.add(r.getFromEntity());
 				}
 			}
 		}
 		return result;
 	}
 
 	public Collection<LighthouseClass> getAllClasses() {
 //		LinkedList<LighthouseClass> list = new LinkedList<LighthouseClass>();
 //		for (LighthouseEntity e : entities.values()) {
 //			if (e instanceof LighthouseClass) {
 //				list.add((LighthouseClass) e);
 //			}
 //		}
 //		return list;
 		return classes;
 	}
 
 	public Collection<LighthouseEntity> getEntities() {
 		return entities.values();
 	}
 
 	public boolean containsRelationship(LighthouseRelationship relationship) {
 		Collection<LighthouseRelationship> listOfAllRelationships = getRelationships();
 		return listOfAllRelationships.contains(relationship);
 	}
 
 	/**
 	 * Verify if the <code>relationship</code> exist in the model
 	 * 
 	 * @return the relationship instance from the model
 	 * */
 	public LighthouseRelationship getRelationship(
 			LighthouseRelationship relationship) {
 		Collection<LighthouseRelationship> list = getRelationshipsFrom(relationship
 				.getFromEntity());
 		if (list != null) {
 			for (LighthouseRelationship lighthouseRelationship : list) {
 				if (lighthouseRelationship.equals(relationship)) {
 					return lighthouseRelationship;
 				}
 			}
 		}
 		return null;
 	}
 
 	public Collection<LighthouseRelationship> getRelationships() {
 		Collection<LighthouseRelationship> result = new LinkedList<LighthouseRelationship>();
 		for (LinkedHashSet<LighthouseRelationship> list : relationshipsTo
 				.values()) {
 			result.addAll(list);
 		}
 		return result;
 	}
 
 	public Collection<LighthouseRelationship> getRelationshipsFrom(
 			LighthouseEntity entity) {
 		LinkedHashSet<LighthouseRelationship> list = relationshipsFrom
 				.get(entity.getFullyQualifiedName());
 		return list != null ? list : new LinkedList<LighthouseRelationship>();
 	}
 
 	public Collection<LighthouseRelationship> getRelationshipsTo(
 			LighthouseEntity entity) {
 		LinkedHashSet<LighthouseRelationship> list = relationshipsTo.get(entity
 				.getFullyQualifiedName());
 		return list != null ? list : new LinkedList<LighthouseRelationship>();
 	}
 
 	public Collection<LighthouseRelationship> getRelationshipsTo(
 			LighthouseEntity entity, TYPE type) {
 		List<LighthouseRelationship> result = new LinkedList<LighthouseRelationship>();
 		for (LighthouseRelationship rel : getRelationshipsTo(entity)) {
 			if (rel.getType() == type) {
 				result.add(rel);
 			}
 		}
 		return result;
 	}
 
 }
