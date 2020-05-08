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
 package org.oobium.build.gen.migration;
 
 import static org.oobium.persist.Relation.*;
 import static org.oobium.persist.migrate.defs.Column.DATESTAMPS;
 import static org.oobium.persist.migrate.defs.Column.TIMESTAMPS;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.columnName;
 import static org.oobium.utils.StringUtils.tableName;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.build.model.ModelAttribute;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.model.ModelRelation;
 import org.oobium.build.util.SourceFile;
 import org.oobium.persist.Relation;
 import org.oobium.persist.migrate.defs.Column;
 import org.oobium.persist.migrate.defs.Index;
 import org.oobium.persist.migrate.defs.columns.ForeignKey;
 
 
 public class ModelTable {
 
 	private final SourceFile sf;
 	
 	public String name;
 	public List<Column> columns;
 	public List<Index> indexes;
 	public List<ForeignKey> foreignKeys;
 
 	public ModelTable(SourceFile sf, ModelDefinition model, ModelDefinition[] models) {
 		this.sf = sf;
 		
 		name = tableName(model.getSimpleType());
 		columns = new ArrayList<Column>();
 		foreignKeys = new ArrayList<ForeignKey>();
 		indexes = new ArrayList<Index>();
 
 		boolean datestamps = model.datestamps;
 		boolean timestamps = model.timestamps;
 		for(ModelAttribute attribute : model.attributes.values()) {
 			if(datestamps) {
 				if(attribute.name.equals("createdOn") || attribute.name.equals("updatedOn")) {
 					continue;
 				}
 			}
 			if(timestamps) {
 				if(attribute.name.equals("createdAt") || attribute.name.equals("updatedAt")) {
 					continue;
 				}
 			}
 			addAttribute(attribute);
 		}
 		if(datestamps) {
 			columns.add(new Column(null, DATESTAMPS));
 		}
 		if(timestamps) {
 			columns.add(new Column(null, TIMESTAMPS));
 		}
 		
 		for(ModelRelation relation : model.relations.values()) {
 			if(!relation.hasMany && !relation.isThrough()) {
 				addRelation(relation);
 			}
 		}
 
 		for(String index : model.getIndexes()) {
 			addIndex(index);
 		}
 	}
 	
 	private void addAttribute(ModelAttribute attribute) {
 		String name = columnName(attribute.name);
 		String type = attribute.type;
 		
 		Map<String, Object> options = new LinkedHashMap<String, Object>();
 		if(attribute.isPrimitive()) {
 			options.put("required", true);
 		}
 		if("BigDecimal".equals(attribute.type)) {
 			options.put("precision", attribute.precision);
 			options.put("scale", attribute.scale);
 		}
 		if(attribute.unique) {
 			options.put("unique", true);
 		}
 		if(!blank(attribute.check)) {
 			options.put("check", attribute.check);
 		}
 		if(attribute.isPrimitive()) {
 			options.put("primitive", true);
 		}
 
 		columns.add(new Column(type, name, options.isEmpty() ? null : options));
 		
 		if(attribute.indexed) {
 			indexes.add(new Index(columnName(attribute.name), attribute.unique));
 		}
 	}
 
 	private void addIndex(String index) {
 		boolean unique = index.startsWith("Unique-");
 		if(unique) {
 			index = index.substring(7);
 		}
 		String[] fields = index.split(",");
 		String[] columns = new String[fields.length];
 		for(int i = 0; i < fields.length; i++) {
 			columns[i] = columnName(fields[i]);
 		}
 		indexes.add(new Index(columns, unique));
 	}
 
	private String getReferentialAction(int action) {
 		switch(action) {
 		case CASCADE:		return "CASCADE";
 		case NO_ACTION:		return "NO ACTION";
 		case RESTRICT:		return "RESTRICT";
 		case SET_DEFAULT:	return "SET DEFAULT";
 		case SET_NULL:		return "SET NULL";
 		default:			return null;
 		}
 	}
 	
 	private void addRelation(ModelRelation relation) {
 		// add the column
 		String name = columnName(relation.name);
 		String type = Integer.class.getCanonicalName();
 		Map<String, Object> options = new LinkedHashMap<String, Object>();
 		if(relation.required) {
 			options.put("required", true);
 		}
 		columns.add(new Column(type, name, options.isEmpty() ? null : options));
 
 		// add the foreign key
 		String column = columnName(relation.name);
 		String reference = tableName(relation.getSimpleType());
 		options = new LinkedHashMap<String, Object>();
		String onDelete = getReferentialAction(relation.onDelete);
 		if(onDelete != null) {
 			sf.staticImports.add(Relation.class.getCanonicalName() + "." + onDelete);
 			options.put("onDelete", onDelete);
 		}
		String onUpdate = getReferentialAction(relation.onUpdate);
 		if(onUpdate != null) {
 			sf.staticImports.add(Relation.class.getCanonicalName() + "." + onUpdate);
 			options.put("onUpdate", onUpdate);
 		}
 		foreignKeys.add(new ForeignKey(column, reference, options.isEmpty() ? null : options));
 
 		// add the index
 		indexes.add(new Index(column, relation.isUnique()));
 	}
 
 	public boolean hasForeignKey() {
 		return !foreignKeys.isEmpty();
 	}
 	
 	public boolean hasIndex() {
 		return !indexes.isEmpty();
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + " {" + name + "}";
 	}
 	
 }
