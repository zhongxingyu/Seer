 /*******************************************************************************
  * Copyright (c) 2010, 2011 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.model;
 
 import static org.oobium.persist.Relation.CASCADE;
 import static org.oobium.persist.Relation.DEFAULT_DEPENDENT;
 import static org.oobium.persist.Relation.DEFAULT_EMBED;
 import static org.oobium.persist.Relation.DEFAULT_EMBEDDED;
 import static org.oobium.persist.Relation.DEFAULT_HASKEY;
 import static org.oobium.persist.Relation.DEFAULT_INCLUDE;
 import static org.oobium.persist.Relation.DEFAULT_ONDELETE;
 import static org.oobium.persist.Relation.DEFAULT_ONUPDATE;
 import static org.oobium.persist.Relation.DEFAULT_OPPOSITE;
 import static org.oobium.persist.Relation.DEFAULT_READONLY;
 import static org.oobium.persist.Relation.DEFAULT_THROUGH;
 import static org.oobium.persist.Relation.DEFAULT_UNIQUE;
 import static org.oobium.persist.Relation.DEFAULT_VIRTUAL;
 import static org.oobium.persist.Relation.DELETE;
 import static org.oobium.persist.Relation.DESTROY;
 import static org.oobium.persist.Relation.NO_ACTION;
 import static org.oobium.persist.Relation.NULLIFY;
 import static org.oobium.persist.Relation.RESTRICT;
 import static org.oobium.persist.Relation.SET_DEFAULT;
 import static org.oobium.persist.Relation.SET_NULL;
 import static org.oobium.persist.Relation.UNDEFINED;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.columnName;
 import static org.oobium.utils.StringUtils.simpleName;
 import static org.oobium.utils.StringUtils.tableName;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.oobium.persist.Relation;
 
 public class ModelRelation {
 
 	public static String getDependentConstant(int dependent) {
 		switch(dependent) {
 		case UNDEFINED: return "UNDEFINED";
 		case DESTROY:   return "DESTROY";
 		case DELETE:    return "DELETE";
 		case NULLIFY:   return "NULLIFY";
 		default:
 			throw new IllegalArgumentException("unknown dependent constant: " + dependent);
 		}
 	}
 	
 	public static String getReferentialConstant(int referentialAction) {
 		switch(referentialAction) {
 		case UNDEFINED:   return "UNDEFINED";
 		case CASCADE:     return "CASCADE";
 		case NO_ACTION:   return "NO_ACTION";
 		case RESTRICT:    return "RESTRICT";
 		case SET_DEFAULT: return "SET_DEFAULT";
 		case SET_NULL:    return "SET_NULL";
 		default:
 			throw new IllegalArgumentException("unknown referential action: " + referentialAction);
 		}
 	}
 	
 	
 	private final ModelDefinition model;
 
 	private final boolean hasMany;
 	private String name;
 	private String type;
 	private String opposite;
 	private String through;
 	private boolean readOnly;
 	private boolean unique;
 	private boolean virtual;
 	private int dependent;
 	private int onDelete;
 	private int onUpdate;
 	private String embed;
 	private boolean embedded;
 	private boolean include;
 	private boolean hasKey;
 	private ModelRelation oppositeRelation;
 	
 	public ModelRelation(ModelDefinition model, String annotation, boolean hasMany) {
 		this.model = model;
 		this.hasMany = hasMany;
 
 		char[] ca = annotation.toCharArray();
 		int start = annotation.indexOf('(') + 1;
 		int end = annotation.length() - 1;
 		Map<String, String> entries = ModelUtils.getJavaEntries(ca, start, end);
 		
 		name(ModelUtils.getString(entries.get("name")));
 		type(model.getType(entries.get("type")));
 		opposite(ModelUtils.getString(entries.get("opposite")));
 		through(ModelUtils.getString(entries.get("through")));
 		readOnly(coerce(entries.get("readOnly")).from(DEFAULT_READONLY));
 		unique(coerce(entries.get("unique")).from(DEFAULT_UNIQUE));
 		virtual(coerce(entries.get("virtual")).from(DEFAULT_VIRTUAL));
 		dependent(getReferential(entries.get("dependent"), DEFAULT_DEPENDENT));
 		onDelete(getReferential(entries.get("onDelete"), DEFAULT_ONDELETE));
 		onUpdate(getReferential(entries.get("onUpdate"), DEFAULT_ONUPDATE));
 		embed(ModelUtils.getString(entries.get("embed")));
 		embedded(coerce(entries.get("embedded")).from(DEFAULT_EMBEDDED));
 		hasKey(coerce(entries.get("hasKey")).from(DEFAULT_HASKEY));
 		include(coerce(entries.get("include")).from(DEFAULT_INCLUDE));
 	}
 
 	private ModelRelation(ModelRelation original, ModelDefinition model, boolean hasMany) {
 		this.model = model;
 		this.hasMany = hasMany;
 		name(original.name);
 		type(original.type);
 		opposite(original.opposite);
 		through(original.through);
 		readOnly(original.readOnly);
 		unique(original.unique);
 		virtual(original.virtual);
 		dependent(original.dependent);
 		onDelete(original.onDelete);
 		onUpdate(original.onUpdate);
 		embed(original.embed);
 		embedded(original.embedded);
 		hasKey(original.hasKey);
 		include(original.include);
 	}
 	
 	public int dependent() {
 		return dependent;
 	}
 	
 	public ModelRelation dependent(int dependent) {
 		this.dependent = dependent;
 		return this;
 	}
 	
 	public String embed() {
 		return embed;
 	}
 	
 	public ModelRelation embed(String embed) {
 		this.embed = (embed == null) ? DEFAULT_EMBED : embed;
 		return this;
 	}
 	
 	public boolean embedded() {
 		return embedded;
 	}
 	
 	public ModelRelation embedded(boolean embedded) {
 		this.embedded = embedded;
 		return this;
 	}
 
 	public ModelRelation getCopy() {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public ModelRelation getCopy(boolean hasMany) {
 		return new ModelRelation(this, model, hasMany);
 	}
 	
 	public ModelRelation getCopy(ModelDefinition model) {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public ModelRelation getCopy(ModelDefinition model, boolean hasMany) {
 		return new ModelRelation(this, model, hasMany);
 	}
 
 	public Map<String, Object> getCustomProperties() {
 		// when updating this method, make sure to also update #hasCustomProperties()
 		Map<String, Object> props = new HashMap<String, Object>();
 		if(!opposite.equals(DEFAULT_OPPOSITE)) {
 			props.put("opposite", opposite);
 		}
 		if(!through.equals(DEFAULT_THROUGH)) {
 			props.put("through", through);
 		}
 		if(readOnly != DEFAULT_READONLY) {
 			props.put("readOnly", readOnly);
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			props.put("unique", unique);
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			props.put("virtual", virtual);
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			props.put("dependent", dependent);
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			props.put("onDelete", onDelete);
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			props.put("onUpdate", onUpdate);
 		}
 		if(!embed.equals(DEFAULT_EMBED)) {
 			props.put("embed", embed);
 		}
 		if(embedded != DEFAULT_EMBEDDED) {
 			props.put("embedded", embedded);
 		}
 		if(hasKey != DEFAULT_HASKEY) {
 			props.put("hasKey", hasKey);
 		}
 		if(include != DEFAULT_INCLUDE) {
 			props.put("include", include);
 		}
 		return props;
 	}
 	
 	public String getDependentConstant() {
 		return getDependentConstant(dependent);
 	}
 	
 	public String getOnDeleteConstant() {
		return getDependentConstant(onDelete);
 	}
 	
 	public String getOnUpdateConstant() {
		return getDependentConstant(onUpdate);
 	}
 	
 	public Map<String, Object> getProperties() {
 		Map<String, Object> props = getCustomProperties();
 		props.put("name", name);
 		props.put("type", type);
 		props.put("hasMany", hasMany);
 		return props;
 	}
 	
 	public ModelRelation getOpposite() {
 		return oppositeRelation;
 	}
 	
 	private int getReferential(String referential, int defaultValue) {
 		try {
 			return coerce(referential).from(defaultValue);
 		} catch(Exception e) {
 			String constant;
 			String type;
 			int ix = referential.lastIndexOf('.');
 			if(ix == -1) {
 				constant = referential;
 				type = model.getType(constant);
 			} else {
 				constant = referential.substring(ix+1);
 				type = model.getType(referential.substring(0, ix));
 			}
 			try {
 				Class<?> c = Class.forName(type);
 				Field f = c.getField(constant);
 				return f.getInt(c);
 			} catch(Exception e2) {
 				return defaultValue;
 			}
 		}
 	}
 	
 	public String getSimpleType() {
 		return simpleName(type);
 	}
 	
 	public ModelValidation getValidation() {
 		return model.getValidation(name);
 	}
 
 	public boolean hasCustomProperties() {
 		// when updating this method, make sure to also update #getCustomProperties()
 		if(!opposite.equals(DEFAULT_OPPOSITE)) {
 			return true;
 		}
 		if(!through.equals(DEFAULT_THROUGH)) {
 			return true;
 		}
 		if(readOnly != DEFAULT_READONLY) {
 			return true;
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			return true;
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			return true;
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			return true;
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			return true;
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			return true;
 		}
 		if(!embed.equals(DEFAULT_EMBED)) {
 			return true;
 		}
 		if(embedded != DEFAULT_EMBEDDED) {
 			return true;
 		}
 		if(hasKey != DEFAULT_HASKEY) {
 			return true;
 		}
 		if(include != DEFAULT_INCLUDE) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean hasKey() {
 		// NOTE: this is the compile-time version of ModelAdapter#hasKey(String)
 		//       make sure to apply updates to both
 		if(isThrough()) {
 			return false;
 		}
 		if(hasKey) {
 			return true;
 		}
 		if(oppositeRelation != null && oppositeRelation.hasKey) {
 			return false;
 		}
 		String column1 = columnName(tableName(model.getSimpleName()), columnName(name()));
 		String column2 = columnName(tableName(getSimpleType()), columnName(opposite()));
 		return column1.compareTo(column2) < 0; // the lower sort order contains the key
 	}
 
 	public void hasKey(boolean hasKey) {
 		this.hasKey = hasKey;
 	}
 	
 	public boolean hasMany() {
 		return hasMany;
 	}
 	
 	public boolean hasOpposite() {
 		return opposite != null && opposite.length() > 0;
 	}
 	
 	public boolean hasValidation() {
 		return model.hasValidation(name);
 	}
 	
 	public boolean include() {
 		return include;
 	}
 	
 	public ModelRelation include(boolean include) {
 		this.include = include;
 		return this;
 	}
 
 	public boolean isThrough() {
 		return !blank(through);
 	}
 	
 	public ModelDefinition model() {
 		return model;
 	}
 	
 	public String name() {
 		return name;
 	}
 	
 	public ModelRelation name(String name) {
 		if(name == null) {
 			throw new IllegalArgumentException("name cannot be null");
 		}
 		this.name = name;
 		return this;
 	}
 	
 	public int onDelete() {
 		return onDelete;
 	}
 
 	public ModelRelation onDelete(int onDelete) {
 		this.onDelete = onDelete;
 		return this;
 	}
 
 	public int onUpdate() {
 		return onUpdate;
 	}
 
 	public ModelRelation onUpdate(int onUpdate) {
 		this.onUpdate = onUpdate;
 		return this;
 	}
 
 	public String opposite() {
 		return opposite;
 	}
 	
 	public ModelRelation opposite(String opposite) {
 		this.opposite = (opposite == null) ? DEFAULT_OPPOSITE : opposite;
 		return this;
 	}
 	
 	public boolean readOnly() {
 		return readOnly;
 	}
 	
 	public ModelRelation readOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 		return this;
 	}
 	
 	void setOpposite(ModelDefinition[] models) {
 		if(hasOpposite() && oppositeRelation == null) {
 			for(ModelDefinition model : models) {
 				if(type.equals(model.getCanonicalName())) {
 					oppositeRelation = model.getRelation(opposite);
 					if(oppositeRelation != null) {
 						oppositeRelation.oppositeRelation = this;
 					}
 				}
 			}
 		}
 	}
 	
 	public String through() {
 		return through;
 	}
 	
 	public ModelRelation through(String through) {
 		this.through = (through == null) ? DEFAULT_THROUGH : through;
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append('@').append(Relation.class.getSimpleName()).append('(');
 		sb.append("name=\"").append(name).append("\"");
 		sb.append(", type=").append(getSimpleType()).append(".class");
 		if(!through.equals(DEFAULT_THROUGH)) {
 			sb.append(", through=\"").append(through).append('"');
 		} else {
 			if(!opposite.equals(DEFAULT_OPPOSITE)) {
 				sb.append(", opposite=\"").append(opposite).append('"');
 			}
 			if(embedded != DEFAULT_EMBEDDED) {
 				sb.append(", embedded=").append(embedded);
 			} else if(!embed.equals(DEFAULT_EMBED)) {
 				sb.append(", embed=\"").append(embed).append('"');
 			}
 			if(readOnly != DEFAULT_READONLY) {
 				sb.append(", readOnly=").append(readOnly);
 			}
 		}
 		if(unique != DEFAULT_UNIQUE) {
 			sb.append(", unique=").append(unique);
 		}
 		if(virtual != DEFAULT_VIRTUAL) {
 			sb.append(", virtual=").append(virtual);
 		}
 		if(dependent != DEFAULT_DEPENDENT) {
 			sb.append(", dependent=").append(getDependentConstant());
 		}
 		if(onDelete != DEFAULT_ONDELETE) {
 			sb.append(", onDelete=").append(getOnDeleteConstant());
 		}
 		if(onUpdate != DEFAULT_ONUPDATE) {
 			sb.append(", onUpdate=").append(getOnUpdateConstant());
 		}
 		if(hasKey != DEFAULT_HASKEY) {
 			sb.append(", hasKey=").append(hasKey);
 		}
 		if(include != DEFAULT_INCLUDE) {
 			sb.append(", include=").append(include);
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 	
 	public String type() {
 		return type;
 	}
 	
 	public ModelRelation type(String type) {
 		if(type == null) {
 			throw new IllegalArgumentException("type cannot be null");
 		}
 		this.type = type;
 		return this;
 	}
 	
 	public boolean unique() {
 		return unique;
 	}
 	
 	public ModelRelation unique(boolean unique) {
 		this.unique = unique;
 		return this;
 	}
 
 	public boolean virtual() {
 		return virtual;
 	}
 
 	public ModelRelation virtual(boolean virtual) {
 		this.virtual = virtual;
 		return this;
 	}
 
 	
 }
