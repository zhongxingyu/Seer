 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist;
 
 import static org.oobium.utils.StringUtils.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 public class ModelAdapter {
 
 	private static final Map<Class<?>, ModelAdapter> adapters = new HashMap<Class<?>, ModelAdapter>();
 	
 	private static ModelAdapter createAdapter(Class<? extends Model> modelClass) {
 		ModelAdapter adapter = new ModelAdapter(modelClass);
 		adapter.init();
 		adapters.put(modelClass, adapter);
 		return adapter;
 	}
 
 	public static ModelAdapter getAdapter(Class<? extends Model> clazz) {
 		if(clazz != null) {
 			ModelAdapter adapter = adapters.get(clazz);
 			if(adapter == null) {
 				synchronized(adapters) {
 					adapter = adapters.get(clazz);
 					if(adapter == null) {
 						 // support cases where the given class is a subclass of the actual model class (mock and spy especially)
 						Class<? extends Model> modelClass = clazz;
 						while(modelClass.getAnnotation(ModelDescription.class) == null && modelClass != Model.class) {
 							modelClass = modelClass.getSuperclass().asSubclass(Model.class);
 						}
 						if(modelClass == clazz) { // the simple case
 							adapter = createAdapter(modelClass);
 						} else {
 							adapter = adapters.get(modelClass); // the actual model class may have been adapted earlier - check for it
 							if(adapter == null) {
 								adapter = createAdapter(modelClass);
 							}
 							// save for all subclasses of the model class, so we don't have to do this again... leak?
 							Class<? extends Model> c = clazz;
 							while(c != modelClass) {
 								adapters.put(c, adapter);
 								c = c.getSuperclass().asSubclass(Model.class);
 							}
 						}
 					}
 				}
 			}
 			return adapter;
 		}
 		return null;
 	}
 
 	public static ModelAdapter getAdapter(Model model) {
 		if(model != null) {
 			return getAdapter(model.getClass());
 		}
 		return null;
 	}
 
 	private Class<? extends Model> clazz;
 	
 	private Map<String, Attribute> attribute;
 	private Map<String, Relation> hasOne;
 	private Map<String, Relation> hasMany;
 	private Set<String> fields;
 	private Set<String> modelFields;
 	private Set<String> virtualFields;
 	private boolean dateStamped;
 	private boolean timeStamped;
 	private boolean deletable;
 	private boolean updatable;
 
 	
 	private ModelAdapter(Class<? extends Model> clazz) {
 		this.clazz = clazz;
 		this.hasOne = new HashMap<String,Relation>();
 		this.hasMany = new HashMap<String,Relation>();
 		this.fields = new HashSet<String>();
 		this.modelFields = new HashSet<String>();
 		this.virtualFields = new HashSet<String>();
 		this.attribute = new HashMap<String, Attribute>();
 	}
 
 	public Set<String> getAttributeFields() {
 		return attribute.keySet();
 	}
 
 	public Class<?> getClass(String field) {
 		if(attribute.containsKey(field)) {
 			return getType(attribute.get(field));
 		}
 		else if(hasOne.containsKey(field)) {
 			return hasOne.get(field).type();
 		}
 		else if(hasMany.containsKey(field)) {
 			return ModelList.class;
 		}
 		return null;
 	}
 	
 	public String[] getEmbedded(String field) {
 		if(hasOne.containsKey(field)) {
 			if(hasOne.get(field).embedded()) {
 				return null;
 			}
 			String embed = hasOne.get(field).embed();
 			if(!blank(embed)) {
 				return embed.split("\\s*,\\s*");
 			}
 		}
 		if(hasMany.containsKey(field)) {
 			if(hasMany.get(field).embedded()) {
 				return null;
 			}
 			String embed = hasMany.get(field).embed();
 			if(!blank(embed)) {
 				return embed.split("\\s*,\\s*");
 			}
 		}
 		return null;
 	}
 
 	public String[] getFields() {
 		return fields.toArray(new String[fields.size()]);
 	}
 	
 	public Set<String> getHasManyFields() {
 		return hasMany.keySet();
 	}
 	
 	/**
 	 * Get the member class of the given hasMany field. hasMany fields are always a
 	 * collection; use this method to find out of what.
 	 * @param field
 	 * @return the class of the members of the hasMany field, if it exists; null otherwise
 	 */
 	public Class<? extends Model> getHasManyMemberClass(String field) {
 		if(hasMany.containsKey(field)) {
 			Class<?> clazz = hasMany.get(field).type();
 			if(Model.class.isAssignableFrom(clazz)) {
 				return clazz.asSubclass(Model.class);
 			}
 		}
 		return null;
 	}
 	
 	public Class<? extends Model> getHasOneClass(String field) {
 		if(hasOne.containsKey(field)) {
 			Class<?> clazz = hasOne.get(field).type();
 			if(Model.class.isAssignableFrom(clazz)) {
 				return clazz.asSubclass(Model.class);
 			}
 		}
 		return null;
 	}
 	
 	public Set<String> getHasOneFields() {
 		return hasOne.keySet();
 	}
 
 	/**
 	 * The model class is the class which has the @ModelDescription annotation.
 	 * This may or may not be the class passed into getAdapter, but will be the correct
 	 * class for use in table name methods. It is always best to use the result of this
 	 * class rather than just calling getClass() on the model object.
 	 * @return the true model class (that with the @ModelDescription annotation)
 	 */
 	public Class<? extends Model> getModelClass() {
 		return this.clazz;
 	}
 	
 	public String[] getModelFields() {
 		return modelFields.toArray(new String[modelFields.size()]);
 	}
 	
 	public String getOpposite(String field) {
 		Relation relation = getRelation(field);
 		if(relation != null) {
 			String opposite = relation.opposite();
 			if(!blank(opposite)) return opposite;
 		}
 		return null;
 	}
 	
 	public PersistService getOppositePersistService(String field) {
 		PersistService p1 = Model.getPersistService(clazz);
 		PersistService p2 = Model.getPersistService(getOppositeType(field));
 		if(p1 != p2) {
 			return p2;
 		}
 		return null;
 	}
 	
 	public Class<? extends Model> getOppositeType(String field) {
 		Relation relation = getRelation(field);
 		return (relation != null) ? relation.type() : null;
 	}
 	
 	public Class<?> getRawClass(String field) {
 		if(attribute.containsKey(field)) {
 			return attribute.get(field).type();
 		} else if(hasOne.containsKey(field)) {
 			return int.class;
 		}
 		return null;
 	}
 	
 	public Relation getRelation(String field) {
 		if(hasOne.containsKey(field)) {
 			return hasOne.get(field);
 		} else if(hasMany.containsKey(field)) {
 			return hasMany.get(field);
 		}
 		return null;
 	}
 	
 	public Class<? extends Model> getRelationClass(String field) {
 		Class<?> clazz = null;
 		if(hasOne.containsKey(field)) {
 			clazz = hasOne.get(field).type();
 		} else if(hasMany.containsKey(field)) {
 			clazz = hasMany.get(field).type();
 		}
 		if(clazz != null && Model.class.isAssignableFrom(clazz)) {
 			return clazz.asSubclass(Model.class);
 		}
 		return null;
 	}
 	
 	/**
 	 * Get a set of all hasOne and hasMany fields
 	 */
 	public Set<String> getRelationFields() {
 		Set<String> rels = new LinkedHashSet<String>();
 		rels.addAll(hasOne.keySet());
 		rels.addAll(hasMany.keySet());
 		return rels;
 	}
 
 	/**
 	 * Get a set of all hasOne and hasMany relationships
 	 */
 	public Set<Relation> getRelationships() {
 		Set<Relation> rels = new LinkedHashSet<Relation>();
 		rels.addAll(hasOne.values());
 		rels.addAll(hasMany.values());
 		return rels;
 	}
 
 	public String[] getThrough(String field) {
 		Relation relation = hasOne.get(field);
 		if(relation == null) {
 			relation = hasMany.get(field);
 		}
 		if(relation != null) {
 			String through = relation.through();
 			if(!blank(through)) {
 				int ix = through.indexOf(':');
 				if(ix == -1) {
 					boolean plural = (isOneToMany(through) || isManyToMany(through));
 					return new String[] { through, varName(relation.type(), plural) };
 				} else {
 					return new String[] { through.substring(0, ix), through.substring(ix + 1) };
 				}
 			}
 		}
 		return null;
 	}
 
 	public Class<? extends Model> getThroughClass(String field) {
 		String[] through = getThrough(field);
 		if(through != null) {
 			return getRelationClass(through[0]);
 		}
 		return null;
 	}
 
 	private Class<?> getType(Attribute attr) {
 		Class<?> type = attr.type();
 		if(type == Text.class) {
 			return String.class;
 		}
 		if(type == Binary.class) {
 			return byte[].class;
 		}
 		return type;
 	}
 	
 	public boolean hasAttribute(String field) {
 		return attribute.containsKey(field);
 	}
 
 	public boolean hasField(String field) {
 		return fields.contains(field);
 	}
 
 	/**
 	 * Find out if this field holds the foreign key in a 1:1 relationship.
 	 * Only valid for 1:1 relationships - check with {@link #isOneToOne(String)}
 	 * @return true if this field holds the key, false if it is held by the opposite field.
 	 * @see #isOneToOne(String)
 	 */
 	public boolean hasKey(String field) {
		// NOTE: update this with ModelRelation#hasKey()
 		Relation relation = hasOne.get(field);
		if(relation == null) {
			return false;
		}
 		String column1 = columnName(tableName(clazz), columnName(field));
 		String column2 = columnName(tableName(relation.type()), columnName(relation.opposite()));
 		return column1.compareTo(column2) < 0; // the lower sort order contains the key
 	}
 	
 	public boolean hasMany(String field) {
 		return hasMany.containsKey(field);
 	}
 
 	public boolean hasOne(String field) {
 		return hasOne.containsKey(field);
 	}
 
 	public boolean hasOpposite(String field) {
 		Relation relation = hasMany.get(field);
 		if(relation == null) {
 			relation = hasOne.get(field);
 		}
 		if(relation != null) {
 			String opposite = relation.opposite();
 			if(opposite != null) {
 				ModelDescription md = relation.type().getAnnotation(ModelDescription.class);
 				if(md != null) {
 					for(Relation r : md.hasOne()) {
 						if(r.name().equals(opposite)) {
 							if(r.type().isAssignableFrom(this.clazz) && r.opposite().equals(field)) {
 								return true;
 							} else {
 								return false;
 							}
 						}
 					}
 					for(Relation r : md.hasMany()) {
 						if(r.name().equals(opposite)) {
 							if(r.type().isAssignableFrom(this.clazz) && r.opposite().equals(field)) {
 								return true;
 							} else {
 								return false;
 							}
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	void init() {
 		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
 		Class<?> c = this.clazz;
 		while(c != Object.class) {
 			classes.addFirst(c);
 			c = c.getSuperclass();
 		}
 		for(Class<?> clazz : classes) {
 			ModelDescription description = clazz.getAnnotation(ModelDescription.class);
 			if(description != null) {
 				for(Attribute attr : description.attrs()) {
 					attribute.put(attr.name(), attr);
 					fields.add(attr.name());
 					if(attr.virtual()) {
 						virtualFields.add(attr.name());
 					} else {
 						modelFields.add(attr.name());
 					}
 				}
 				for(Relation relation : description.hasOne()) {
 					hasOne.put(relation.name(), relation);
 					fields.add(relation.name());
 					modelFields.add(relation.name());
 				}
 				for(Relation relation : description.hasMany()) {
 					hasMany.put(relation.name(), relation);
 					fields.add(relation.name());
 				}
 				if(!dateStamped && description.datestamps()) {
 					dateStamped = true;
 					attribute.put(ModelDescription.CREATED_ON, ModelAttributes.createdOn);
 					attribute.put(ModelDescription.UPDATED_ON, ModelAttributes.updatedOn);
 					fields.add(ModelDescription.CREATED_ON);
 					fields.add(ModelDescription.UPDATED_ON);
 					modelFields.add(ModelDescription.CREATED_ON);
 					modelFields.add(ModelDescription.UPDATED_ON);
 				}
 				if(!timeStamped && description.timestamps()) {
 					timeStamped = true;
 					attribute.put(ModelDescription.CREATED_AT, ModelAttributes.createdAt);
 					attribute.put(ModelDescription.UPDATED_AT, ModelAttributes.updatedAt);
 					fields.add(ModelDescription.CREATED_AT);
 					fields.add(ModelDescription.UPDATED_AT);
 					modelFields.add(ModelDescription.CREATED_AT);
 					modelFields.add(ModelDescription.UPDATED_AT);
 				}
 				deletable = description.allowDelete();
 				updatable = description.allowUpdate();
 			}
 		}
 	}
 
 	public boolean isBoolean(String field) throws NoSuchFieldException {
 		Class<?> type = getClass(field);
 		return type == Boolean.class || type == boolean.class;
 	}
 
 	public boolean isDateStamped() {
 		return dateStamped;
 	}
 	
 	public boolean isDeletable() {
 		return deletable;
 	}
 
 	public boolean isEmbedded(String field) {
 		if(hasOne.containsKey(field)) {
 			return hasOne.get(field).embedded() || !blank(hasOne.get(field).embed());
 		}
 		if(hasMany.containsKey(field)) {
 			return hasMany.get(field).embedded() || !blank(hasMany.get(field).embed());
 		}
 		return false;
 	}
 	
 	public boolean isIncluded(String field) {
 		if(hasOne.containsKey(field)) {
 			return hasOne.get(field).include();
 		}
 		if(hasMany.containsKey(field)) {
 			return hasMany.get(field).include();
 		}
 		return false;
 	}
 	
 	public boolean isInitialized(String field) {
 		if(attribute.containsKey(field)) {
 			return attribute.get(field).init().length() > 0;
 		}
 		return false;
 	}
 
 	public boolean isJson(String field) {
 		if(attribute.containsKey(field)) {
 			return attribute.get(field).json();
 		}
 		return true;
 	}
 	
 	public boolean isManyToMany(String field) {
 		Relation relation = hasMany.get(field);
 		if(relation != null) {
 			String opposite = relation.opposite();
 			if(opposite != null) {
 				ModelDescription md = relation.type().getAnnotation(ModelDescription.class);
 				if(md != null) {
 					for(Relation r : md.hasMany()) {
 						if(r.name().equals(opposite)) {
 							if(r.type().isAssignableFrom(this.clazz) && r.opposite().equals(field)) {
 								return true;
 							} else {
 								return false;
 							}
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean isManyToNone(String field) {
 		Relation relation = hasMany.get(field);
 		if(relation != null) {
 			return blank(relation.opposite());
 		}
 		return false;
 	}
 	
 	public boolean isManyToOne(String field) {
 		return isThisOpposite(hasMany, field, true);
 	}
 	
 	public boolean isOneToMany(String field) {
 		return isThisOpposite(hasOne, field, false);
 	}
 	
 	public boolean isOneToOne(String field) {
 		return isThisOpposite(hasOne, field, true);
 	}
 	
 	public boolean isOneToNone(String field) {
 		Relation relation = hasOne.get(field);
 		if(relation != null) {
 			return blank(relation.opposite());
 		}
 		return false;
 	}
 	
 	/**
 	 * Get a list of all the fields that link back to this model
 	 * from the related model.<br>
 	 * For example, if AModel hasOne BModel, and
 	 * BModel hasOne AModel, calling this method on getAdapter(AModel).getRelationLinkBacks("bModels")
 	 * would return [ "aModels" ].
 	 */
 	public List<String> getRelationLinkBacks(String field) {
 		Relation relation = getRelation(field);
 		if(relation != null) {
 			List<String> linkbacks = new ArrayList<String>();
 			ModelAdapter adapter = getAdapter(relation.type());
 			for(Relation r : adapter.hasOne.values()) {
 				if(r.type() == clazz) {
 					linkbacks.add(r.name());
 				}
 			}
 			return linkbacks;
 		}
 		return new ArrayList<String>(0);
 	}
 	
 	public boolean isReadOnly(String field) {
 		if(attribute.containsKey(field)) {
 			return attribute.get(field).readOnly();
 		}
 		if(hasOne.containsKey(field)) {
 			return hasOne.get(field).readOnly();
 		}
 		if(hasMany.containsKey(field)) {
 			return hasMany.get(field).readOnly();
 		}
 		return ModelDescription.ID.equals(field);
 	}
 
 	private boolean isThisOpposite(Map<String, Relation> map, String field, boolean toOne) {
 		Relation relation = map.get(field);
 		if(relation != null) {
 			String opposite = relation.opposite();
 			if(opposite != null) {
 				ModelDescription md = relation.type().getAnnotation(ModelDescription.class);
 				if(md != null) {
 					for(Relation r : (toOne ? md.hasOne() : md.hasMany())) {
 						if(r.name().equals(opposite)) {
 							if(r.type().isAssignableFrom(this.clazz) && r.opposite().equals(field)) {
 								return true;
 							} else {
 								return false;
 							}
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean isThrough(String field) {
 		if(hasOne.containsKey(field)) {
 			return !blank(hasOne.get(field).through());
 		}
 		if(hasMany.containsKey(field)) {
 			return !blank(hasMany.get(field).through());
 		}
 		return false;
 	}
 	
 	public boolean isTimeStamped() {
 		return timeStamped;
 	}
 	
 	public boolean isUpdatable() {
 		return updatable;
 	}
 	
 	public boolean isVirtual(String field) {
 		return virtualFields.contains(field);
 	}
 	
 	@Override
 	public String toString() {
 		return "ClassAdapter {" + clazz.getCanonicalName() + "}";
 	}
 	
 }
