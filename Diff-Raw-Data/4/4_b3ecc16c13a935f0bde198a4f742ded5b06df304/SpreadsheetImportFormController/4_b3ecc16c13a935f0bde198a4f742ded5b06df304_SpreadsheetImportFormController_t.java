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
 package org.openmrs.module.spreadsheetimport.web.controller;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.Encounter;
 import org.openmrs.FieldType;
 import org.openmrs.Form;
 import org.openmrs.FormField;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.spreadsheetimport.DatabaseBackend;
 import org.openmrs.module.spreadsheetimport.SpreadsheetImportTemplate;
 import org.openmrs.module.spreadsheetimport.SpreadsheetImportTemplateColumn;
 import org.openmrs.module.spreadsheetimport.SpreadsheetImportTemplateColumnPrespecifiedValue;
 import org.openmrs.module.spreadsheetimport.SpreadsheetImportUtil;
 import org.openmrs.module.spreadsheetimport.UniqueImport;
 import org.openmrs.module.spreadsheetimport.service.SpreadsheetImportService;
 import org.openmrs.module.spreadsheetimport.validators.SpreadsheetImportTemplateValidator;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 /**
  * This controller backs and saves the Spreadsheet Import module settings
  */
 @Controller
 @RequestMapping("/module/spreadsheetimport/spreadsheetimport.form")
 @SessionAttributes({"template", "dataforms"})
 public class SpreadsheetImportFormController {
 	
 	/**
 	 * Logger for this class
 	 */
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	@ModelAttribute("tableColumnMap")
 	Map<String, String> populateTableColumnMap() throws Exception {
 		return DatabaseBackend.getTableColumnMap();
 	}
 	
 //	@RequestMapping(method = RequestMethod.GET)
 	@RequestMapping(value = {"/module/spreadsheetimport/spreadsheetimport.form"}, method = RequestMethod.GET)
 	public String setupForm(@RequestParam(value = "id", required = false) Integer id, ModelMap model,
 	                        HttpServletRequest request) {
 		SpreadsheetImportTemplate template = null;
 		if (id != null) {
 			template = Context.getService(SpreadsheetImportService.class).getTemplateById(id);
 		} else {
 			template = new SpreadsheetImportTemplate();
 		}				
 		model.addAttribute("template", template);		
 		
 		// Citigo addition starts
 		List<Form> dataForms = Context.getService(FormService.class).getAllForms(false);
 		model.addAttribute("dataforms", dataForms);
 		// Citigo addition ends
 		
 		return "/module/spreadsheetimport/spreadsheetimportFormColumn";
 	}
 	
 	@RequestMapping(value = {"/module/spreadsheetimport/spreadsheetimport.form"}, method = RequestMethod.POST)
 	public String processSubmit(@ModelAttribute("template") SpreadsheetImportTemplate template, BindingResult result,
 	                            HttpServletRequest request) throws Exception {
 
 		log.debug("process import, step = " + request.getParameter("step"));
 		
 		Map<String, List<String>> tableColumnListMap = DatabaseBackend.getTableColumnListMap();
 		
 		// creating import template from Form. Import data will be treated as 1 encounter
 		if (request.getParameter("createFromForm") != null) {
 			template.getColumns().clear();
 			String formId = request.getParameter("targetForm");
 			Form selectedForm = Context.getService(FormService.class).getForm(Integer.parseInt(formId)); 
 			
 			for (FormField field : selectedForm.getFormFields()) {
 				FieldType fieldType = field.getField().getFieldType();
 				
 				// ignore the field of type set
 				if (fieldType.getIsSet().booleanValue())
 					continue;
 				
 				// ignore encounter mapping
 				if ("encounter".equals(field.getField().getTableName()))
 					continue;
 				
 				boolean willAdd = true;
 				SpreadsheetImportTemplateColumn column = new SpreadsheetImportTemplateColumn();
 				column.setName(field.getField().getName());
 				column.setTemplate(template);				
 				
 				Concept concept = field.getField().getConcept();
 				SpreadsheetImportTemplateColumn dateColumn = null;
 				if (concept == null) {
 					String tableName = field.getField().getTableName();
 					String columnName = field.getField().getAttributeName();
 					// if we can't find a table name, likely it's person
 					if (tableName.startsWith("patient")) {
 						if (!tableColumnListMap.containsKey(tableName))
 							tableName = tableName.replace("patient", "person");
 						else {
 							List<String> columns = tableColumnListMap.get(tableName);
 							if (!columns.contains(columnName))
 								tableName = tableName.replace("patient", "person");
 						}
 					}
 					
 					// final check: if there is really no such table and column
 					if ( (!tableColumnListMap.containsKey(tableName)) || (!tableColumnListMap.get(tableName).contains(columnName)))
 						willAdd = false;
 					
 					column.setTableDotColumn(tableName + "." + field.getField().getAttributeName());
 				} else {
 					// getting data type of the concept
 					ConceptDatatype conceptDataType = concept.getDatatype();
 					if (conceptDataType.isNumeric())
 						column.setTableDotColumn("obs.value_numeric");
 					else if (conceptDataType.isCoded())
 						column.setTableDotColumn("obs.value_coded");
 					else if (conceptDataType.isBoolean())
 						column.setTableDotColumn("obs.value_boolean");
 					else if (conceptDataType.isDate())
 						column.setTableDotColumn("obs.value_datetime");
 					else if (conceptDataType.isText())
 						column.setTableDotColumn("obs.value_text");
 					else
 						willAdd = false; // don't know type
 					
 					if (willAdd) {
 						dateColumn = new SpreadsheetImportTemplateColumn();
 						dateColumn.setTableDotColumn("obs.obs_datetime");
						dateColumn.setName(field.getField().getName() + " datetime");
 						dateColumn.setTemplate(template);
 					}
 					
 				}
 				if (willAdd) {
 					template.getColumns().add(column);
 					if (dateColumn != null)
						template.getColumns().add(dateColumn);
 					log.debug("Adding column " + column.getData());
 				}
 			}
 			
 			SpreadsheetImportTemplateColumn encounterId = new SpreadsheetImportTemplateColumn();
 			encounterId.setName("Encounter ID");
 			encounterId.setTableDotColumn("encounter.encounter_id");
 			encounterId.setTemplate(template);
 			encounterId.setDisallowDuplicateValue(new Boolean(false));
 			template.getColumns().add(encounterId);
 
 			return "/module/spreadsheetimport/spreadsheetimportFormColumn";
 		}
 		
 		Map<UniqueImport, Set<SpreadsheetImportTemplateColumn>> rowDataTemp = template.getMapOfUniqueImportToColumnSetSortedByImportIdx();
 
 		for (UniqueImport uniqueImport : rowDataTemp.keySet()) {
 			Set<SpreadsheetImportTemplateColumn> columnSet = rowDataTemp.get(uniqueImport);
 			boolean isFirst = true;
 			for (SpreadsheetImportTemplateColumn column : columnSet) {
 
 				if (isFirst) {
 					isFirst = false;
 					// Should be same for all columns in unique import
 //					System.out.println("SpreadsheetImportUtil.importTemplate: column.getColumnPrespecifiedValues(): " + column.getColumnPrespecifiedValues().size());
 					if (column.getColumnPrespecifiedValues().size() > 0) {
 						Set<SpreadsheetImportTemplateColumnPrespecifiedValue> columnPrespecifiedValueSet = column.getColumnPrespecifiedValues();
 						for (SpreadsheetImportTemplateColumnPrespecifiedValue columnPrespecifiedValue : columnPrespecifiedValueSet) {
 //							System.out.println(columnPrespecifiedValue.getPrespecifiedValue().getTableDotColumn() + " :: " + columnPrespecifiedValue.getPrespecifiedValue().getId() + " :: " + columnPrespecifiedValue.getColumnName() + " ==> " + columnPrespecifiedValue.getPrespecifiedValue().getValue());
 						}
 					}
 				}
 			}
 		}
 
 		template.setTest("abc");
 		template.setRowDataTemp(rowDataTemp);
 		
 		
 		
 		if (request.getParameter("step").equals("columns")) {
 			
 			// Delete columns, must use iterator to update module iterator
 			if (request.getParameter("Delete Columns") != null) {
 				int i = 0;
 				Iterator<SpreadsheetImportTemplateColumn> iterator = template.getColumns().iterator();
 				while (iterator.hasNext()) {
 					iterator.next();
 					if (request.getParameter(Integer.toString(i)) != null) {
 						iterator.remove();
 					}
 					i++;
 				}
 				return "/module/spreadsheetimport/spreadsheetimportFormColumn";
 			}
 			
 			// Add column
 			if (request.getParameter("Add Column") != null) {
 				SpreadsheetImportTemplateColumn column = new SpreadsheetImportTemplateColumn();
 				column.setTemplate(template);
 				template.getColumns().add(column);
 				return "/module/spreadsheetimport/spreadsheetimportFormColumn";
 			}
 			
 		}
 
 		if (request.getParameter("step").equals("prespecifiedValues") &&
 			request.getParameter("Previous Step") != null) {
 
 //			template.clearPrespecifiedValues();
 //			template.clearColumnColumns();
 
 //			try {
 //				SpreadsheetImportUtil.resolveTemplateDependencies(template);
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //			}
 			
 			return "/module/spreadsheetimport/spreadsheetimportFormColumn";			
 
 		}
 		
 		new SpreadsheetImportTemplateValidator().validate(template, result);
 
 		if (result.hasErrors()) {
 			if (request.getParameter("step").equals("columns")) {
 				return "/module/spreadsheetimport/spreadsheetimportFormColumn";
 			} else {
 				return "/module/spreadsheetimport/spreadsheetimportFormPrespecifiedValue";				
 			}
 		}
 		
 		if (request.getParameter("step").equals("columns")) {
 							
 			template.clearPrespecifiedValues();
 			template.clearColumnColumns();
 			SpreadsheetImportUtil.resolveTemplateDependencies(template);
 			if (template.getPrespecifiedValues().size() != 0) {
 				return "/module/spreadsheetimport/spreadsheetimportFormPrespecifiedValue";
 			}
 
 		}
 		
 		Context.getService(SpreadsheetImportService.class).saveSpreadsheetImportTemplate(template);
 		return "redirect:/module/spreadsheetimport/spreadsheetimport.list";
 		
 	}
 }
