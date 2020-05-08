 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.contrib.databaseexporter.transform;
 
 import org.openmrs.contrib.databaseexporter.ExportContext;
 import org.openmrs.contrib.databaseexporter.TableRow;
 
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This replaces the address_hierarchy table with contents based on the configured addresses file
  */
 public class HaitiAddressHierarchyTransform extends StructuredAddressTransform {
 
	private List<String> hierarchyLevels = Arrays.asList("country","state_province","city_village","address3","address2","address1");
 
 	//***** CONSTRUCTORS *****
 
 	public HaitiAddressHierarchyTransform() {}
 
 	//***** INSTANCE METHODS *****
 
 	@Override
 	public boolean canTransform(String tableName, ExportContext context) {
 		return tableName.equals("address_hierarchy_entry");
 	}
 
 	// Remove all existing rows from the address_hierarchy_entry table
 	public boolean transformRow(TableRow row, ExportContext context) {
 		if (row.getTableName().equals("address_hierarchy_entry")) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public List<TableRow> postProcess(String tableName, ExportContext context) {
 		List<TableRow> rows = new ArrayList<TableRow>();
 		if (tableName.equals("address_hierarchy_entry")) {
 			int entryNum = 0;
 			Map<String, Integer> recordedEntries = new HashMap<String, Integer>();
 			for (int i=0; i<getReplacements().size(); i++) {
 				StringBuilder fullEntry = new StringBuilder();
 				Map<String, String> replacement = getReplacements().get(i);
 				for (int level=1; level<=getHierarchyLevels().size(); level++) {
 					Integer parentId = recordedEntries.get(fullEntry.toString());
 					String entryValue = replacement.get(getHierarchyLevels().get(level-1));
 					fullEntry.append("|"+entryValue);
 					if (!recordedEntries.containsKey(fullEntry.toString())) {
 						entryNum++;
 						recordedEntries.put(fullEntry.toString(), entryNum);
 						TableRow row = new TableRow("address_hierarchy_entry");
 						row.addColumnValue("address_hierarchy_entry_id", Types.INTEGER, entryNum);
 						row.addColumnValue("name", Types.VARCHAR, entryValue);
 						row.addColumnValue("level_id", Types.INTEGER, level);
 						row.addColumnValue("parent_id", Types.INTEGER, parentId);
 						row.addColumnValue("user_generated_id", Types.VARCHAR, Integer.toString(entryNum));
 						row.addColumnValue("latitude", Types.DOUBLE, null);
 						row.addColumnValue("longitude", Types.DOUBLE, null);
 						row.addColumnValue("elevation", Types.DOUBLE, null);
 						rows.add(row);
 					}
 				}
 			}
 		}
 		return rows;
 	}
 
 	//***** PROPERTY ACCESS *****
 
 	public List<String> getHierarchyLevels() {
 		if (hierarchyLevels == null) {
 			hierarchyLevels = new ArrayList<String>();
 		}
 		return hierarchyLevels;
 	}
 
 	public void setHierarchyLevels(List<String> hierarchyLevels) {
 		this.hierarchyLevels = hierarchyLevels;
 	}
 }
