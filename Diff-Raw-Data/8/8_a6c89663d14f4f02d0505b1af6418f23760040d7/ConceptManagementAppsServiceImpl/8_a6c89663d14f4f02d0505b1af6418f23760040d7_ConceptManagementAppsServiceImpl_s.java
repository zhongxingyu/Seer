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
  * Copyright (C) OpenMRS, LLC. All Rights Reserved.
  */
 package org.openmrs.module.conceptmanagementapps.api.impl;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptReferenceTerm;
 import org.openmrs.ConceptSource;
 import org.openmrs.api.APIException;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
 import org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO;
 import org.openmrs.ui.framework.page.FileDownload;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.multipart.MultipartFile;
 import org.supercsv.cellprocessor.Optional;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.io.CsvMapReader;
 import org.supercsv.io.CsvMapWriter;
 import org.supercsv.io.ICsvMapReader;
 import org.supercsv.io.ICsvMapWriter;
 import org.supercsv.prefs.CsvPreference;
 
 /**
  * It is a default implementation of {@link ConceptManagementAppsService}.
  */
 public class ConceptManagementAppsServiceImpl extends BaseOpenmrsService implements ConceptManagementAppsService {
 	
 	protected Log log = LogFactory.getLog(getClass());
 	
 	private ConceptManagementAppsDAO dao;
 	
 	/**
 	 * @param dao the dao to set
 	 */
 	public void setDao(ConceptManagementAppsDAO dao) {
 		this.dao = dao;
 	}
 	
 	/**
 	 * @return the dao
 	 */
 	public ConceptManagementAppsDAO getDao() {
 		return dao;
 	}
 	
 	@Transactional(readOnly = true)
 	public List<Concept> getUnmappedConcepts(ConceptSource conceptSource, List<ConceptClass> classesToInclude) {
 		
 		return this.dao.getUnmappedConcepts(conceptSource, classesToInclude);
 	}
 	
 	@Transactional
 	public FileDownload uploadSpreadsheet(MultipartFile spreadsheetFile) throws APIException {
 		
 		List<String> fileLines = new ArrayList<String>();
 		ICsvMapReader mapReader = null;
 		FileDownload fileShowingErrors = null;
 		boolean hasErrors = false;
 		String errorReason = null;
 		
 		try {
 			ConceptService cs = Context.getConceptService();
 			
 			// load CSV File
 			mapReader = new CsvMapReader(new InputStreamReader(spreadsheetFile.getInputStream()),
 			        CsvPreference.STANDARD_PREFERENCE);
 			
 			// the header columns are used as the keys to the Map
 			final String[] header = mapReader.getHeader(true);
 			final CellProcessor[] processors = getSpreadsheetProcessors();
 			
 			// Prepare Map for mapping between content and header CSV
 			String delimiter = ",";
 			fileLines.add("errors - delete this column to resubmit" + delimiter + "map type" + delimiter + "source name"
 			        + delimiter + "source code" + delimiter + "concept Id" + delimiter + "concept uuid" + delimiter
 			        + "preferred name" + delimiter + "description" + delimiter + "class" + delimiter + "datatype"
 			        + delimiter + "all existing mappings");
 			
 			for (Map<String, Object> mapList = mapReader.read(header, processors); mapList != null; mapList = mapReader
 			        .read(header, processors)) {
 				
 				errorReason = " ";
 				errorReason = getInitialErrorsBeforeTryingToSaveConcept(mapList, cs);
 				
 				String line = mapReader.getUntokenizedRow();
 				fileLines.add(errorReason + "," + line);
 				
 				if (StringUtils.isNotEmpty(errorReason) && StringUtils.isNotBlank(errorReason)) {
 					hasErrors = true;
 				}
 			}
 			fileShowingErrors = writeToFile(fileLines);
 			
 		}
 		
 		catch (APIException e) {
 			e.printStackTrace();
 			throw new APIException("error on row " + mapReader.getRowNumber() + "," + mapReader.getUntokenizedRow() + e);
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		finally {
 			
 			if (mapReader != null) {
 				
 				try {
 					mapReader.close();
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		//if there are no errors then go ahead and pass through again and save
 		if (!hasErrors) {
 			
 			setMapAndSaveConcept(spreadsheetFile);
 			return null;
 		}
 
 			return fileShowingErrors;
 		
 		
 	}
 	
 	@Transactional(readOnly = true)
 	public ICsvMapWriter writeFileWithMissingConceptMappings(List<Concept> conceptList, PrintWriter spreadsheetWriter,
 	                                                         String mapTypeDefaultValue, String conceptSourceName)
 	    throws Exception {
 		Locale locale = Context.getLocale();
 		
 		final String[] header = new String[] { "map type", "source name", "source code", "concept Id", "concept uuid",
 		        "preferred name", "description", "class", "datatype", "all existing mappings" };
 		
 		final Map<String, Object> conceptsMissingMappings = new HashMap<String, Object>();
 		
 		ICsvMapWriter mapWriter = null;
 		
 		try {
 			
 			mapWriter = new CsvMapWriter(spreadsheetWriter, CsvPreference.STANDARD_PREFERENCE);
 			
 			final CellProcessor[] downloadProcessors = getSpreadsheetProcessors();
 			
 			// write the header
 			mapWriter.writeHeader(header);
 			String mapTypeValue;
 			if (StringUtils.isNotEmpty(mapTypeDefaultValue) && StringUtils.isNotBlank(mapTypeDefaultValue)) {
 				mapTypeValue = mapTypeDefaultValue;
 				
 			} else {
 				mapTypeValue = " ";
 			}
 			for (Concept concept : conceptList) {
 				
 				conceptsMissingMappings.put(header[0], mapTypeValue);
 				conceptsMissingMappings.put(header[1], conceptSourceName);
 				conceptsMissingMappings.put(header[2], " ");
 				conceptsMissingMappings.put(header[3], concept.getConceptId());
 				conceptsMissingMappings.put(header[4], concept.getUuid());
 				
 				if (concept.getPreferredName(locale) != null) {
 					conceptsMissingMappings.put(header[5], concept.getPreferredName(locale));
 				} else {
 					conceptsMissingMappings.put(header[5], " ");
 				}
 				
 				if (concept.getDescription(locale) != null) {
 					ConceptDescription cd = concept.getDescription(locale);
 					conceptsMissingMappings.put(header[6], cd.getDescription());
 				} else {
 					conceptsMissingMappings.put(header[6], " ");
 				}
 				
 				conceptsMissingMappings.put(header[7], concept.getConceptClass().getName());
 				conceptsMissingMappings.put(header[8], concept.getDatatype().getName());
 				
 				String mappingsName = "  ";
 				for (ConceptMap cm : concept.getConceptMappings()) {
 					if (cm.getConceptMapType() != null) {
 						mappingsName += cm.getConceptMapType().getName() + " ";
 					}
 					if (cm.getConceptReferenceTerm() != null && cm.getConceptReferenceTerm().getConceptSource() != null) {
 						mappingsName += cm.getConceptReferenceTerm().getConceptSource().getName() + "\n";
 					}
 				}
 				
 				//strip new line off of last entry
 				mappingsName = mappingsName.substring(0, mappingsName.length() - 1);
 				conceptsMissingMappings.put(header[9], mappingsName);
 				
 				// write the conceptsMissingMappings maps
 				mapWriter.write(conceptsMissingMappings, header, downloadProcessors);
 			}
 		}
 		finally {
			if (spreadsheetWriter != null) {
				spreadsheetWriter.close();
			}
 			if (mapWriter != null) {
 				mapWriter.close();
 			}
 		}
 		
 		return mapWriter;
 		
 	}
 	
 	private FileDownload writeToFile(List<String> lines) {
 		String linesShowingIfThereAreErrors = "";
 		for (String aline : lines) {
 			linesShowingIfThereAreErrors += aline + "\n";
 		}
 		String theDate = new SimpleDateFormat("dMy_Hm").format(new Date());
 		String contentType = "text/csv;charset=UTF-8";
 		String errorFilename = "conceptsMissingMappingsErrors" + theDate + ".csv";
 		return new FileDownload(errorFilename, contentType, linesShowingIfThereAreErrors.getBytes());
 	}
 	
 	/**
 	 * Sets up the processors used for the spreadsheet to download.
 	 * 
 	 * @return the cell processors
 	 */
 	private static CellProcessor[] getSpreadsheetProcessors() {
 		
 		final CellProcessor[] processors = new CellProcessor[] { new Optional(), // map type
 		        new Optional(), // source name
 		        new Optional(), // source code
 		        new Optional(), // concept id
 		        new Optional(), // concept uuid
 		        new Optional(), // preferred name
 		        new Optional(), // description
 		        new Optional(), // class
 		        new Optional(), // datatype
 		        new Optional() // all existing mappings
 		};
 		
 		return processors;
 	}
 	
 	private String getInitialErrorsBeforeTryingToSaveConcept(Map<String, Object> mapList, ConceptService cs) {
 		String errorString = "";
 		
 		if (isMapTypeNull(mapList)) {
 			errorString = " " + errorString
 			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.maptype.error") + " ";
 		}
 		
 		if (isSourceNameNull(mapList)) {
 			errorString = " " + errorString
 			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.sourcename.error") + " ";
 			
 		}
 		
 		if (isSourceCodeNull(mapList)) {
 			errorString = " " + errorString
 			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.sourcecode.error") + " ";
 			
 		}
 		
 		if (!isSourceNameNull(mapList)
 		        && !isSourceCodeNull(mapList)
 		        && cs.getConceptReferenceTermByCode((String) mapList.get("source code"),
 		            cs.getConceptSourceByName((String) mapList.get("source name"))) == null) {
 			errorString = " "
 			        + errorString
 			        + Context.getMessageSourceService().getMessage(
 			            "conceptmanagementapps.file.getConceptReferenceTermByCode.error") + " ";
 			
 		}
 		if (!isSourceNameNull(mapList) && cs.getConceptSourceByName((String) mapList.get("source name")) == null) {
 			
 			errorString = " "
 			        + errorString
 			        + Context.getMessageSourceService()
 			                .getMessage("conceptmanagementapps.file.getConceptSourceByName.error") + " ";
 		}
 		
 		if (!isMapTypeNull(mapList) && cs.getConceptMapTypeByName(((String) mapList.get("map type"))) == null) {
 			errorString = " "
 			        + errorString
 			        + Context.getMessageSourceService().getMessage(
 			            "conceptmanagementapps.file.getConceptMapTypeByName.error") + " ";
 			
 		}
 		
 		if (cs.getConcept(((String) mapList.get("concept Id"))) == null) {
 			errorString = " " + errorString
 			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.getConcept.error") + " ";
 			
 		}
 		
 		return errorString;
 	}
 	
 	private void setMapAndSaveConcept(MultipartFile spreadsheetFile) {
 		
 		ICsvMapReader mapReader = null;
 		try {
 			ConceptService cs = Context.getConceptService();
 			
 			// load CSV File
 			mapReader = new CsvMapReader(new InputStreamReader(spreadsheetFile.getInputStream()),
 			        CsvPreference.STANDARD_PREFERENCE);
 			
 			// the header columns are used as the keys to the Map
 			final String[] header = mapReader.getHeader(true);
 			final CellProcessor[] processors = getSpreadsheetProcessors();
 			
 			// Prepare Map for mapping between content and header CSV
 			for (Map<String, Object> mapList = mapReader.read(header, processors); mapList != null; mapList = mapReader
 			        .read(header, processors)) {
 				
 				ConceptMap conceptMap = new ConceptMap();
 				Collection<ConceptMap> conceptMappings;
 				
 				ConceptReferenceTerm refTerm = cs.getConceptReferenceTermByCode((String) mapList.get("source code"),
 				    cs.getConceptSourceByName((String) mapList.get("source name")));
 				
 				conceptMap.setConceptReferenceTerm(refTerm);
 				
 				conceptMap.setConceptMapType(cs.getConceptMapTypeByName((String) mapList.get("map type")));
 				
 				if (cs.getConcept(((String) mapList.get("concept Id"))) != null) {
 					
 					Concept concept = cs.getConcept(((String) mapList.get("concept Id")));
 					
 					//see if concept has mappings we need to add to
 					if (concept.getConceptMappings() == null) {
 						
 						List<ConceptMap> conceptMappingsList = new ArrayList<ConceptMap>();
 						conceptMappingsList.add(conceptMap);
 						concept.setConceptMappings(conceptMappingsList);
 						
 					} else {
 						
 						conceptMappings = concept.getConceptMappings();
 						conceptMappings.add(conceptMap);
 						concept.setConceptMappings(conceptMappings);
 						
 					}
 					
 					cs.saveConcept(concept);
 					
 				}
 			}
 		}
 		
 		catch (APIException e) {
 			e.printStackTrace();
 			throw new APIException("error on row " + mapReader.getRowNumber() + "," + mapReader.getUntokenizedRow() + e);
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		finally {
 			
 			if (mapReader != null) {
 				
 				try {
 					mapReader.close();
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * @return boolean for if sourcName is null or empty
 	 */
 	private boolean isSourceNameNull(Map<String, Object> mapList) {
 		String sourceName = "";
 		if (mapList.get("source name") == null) {
 			return true;
 		} else {
 			sourceName = (String) mapList.get("source name");
 		}
 		if (StringUtils.isNotEmpty(sourceName) && StringUtils.isNotBlank(sourceName)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * @return boolean for if sourceCode is null or empty
 	 */
 	private boolean isSourceCodeNull(Map<String, Object> mapList) {
 		String sourceCode = "";
 		if (mapList.get("source code") == null) {
 			return true;
 		} else {
 			sourceCode = (String) mapList.get("source code");
 		}
 		if (StringUtils.isNotEmpty(sourceCode) && StringUtils.isNotBlank(sourceCode)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * @return boolean for if mapType is null or empty
 	 */
 	private boolean isMapTypeNull(Map<String, Object> mapList) {
 		String mapType = "";
 		if (mapList.get("map type") == null) {
 			return true;
 		} else {
 			mapType = (String) mapList.get("map type");
 		}
 		if (StringUtils.isNotEmpty(mapType) && StringUtils.isNotBlank(mapType)) {
 			return false;
 		}
 		return true;
 	}
 	
 }
