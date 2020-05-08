 /**
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.importer;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 import org.supercsv.io.CsvMapReader;
 import org.supercsv.io.ICsvMapReader;
 import org.supercsv.prefs.CsvPreference;
 
 /**
  * @author Jean Kahigiso M.
  *
  */
 public class NormalizedDataImporter extends Importer {
 	
	private static final Integer NUMBER_OF_LINES_TO_IMPORT = 200;
 	private static final Log log = LogFactory.getLog(NormalizedDataImporter.class);
 	
 	private LocationService locationService;
 	private DataService dataService;
 	private PlatformTransactionManager transactionManager;
 	private SessionFactory sessionFactory;
 	
 	private ImporterErrorManager manager;
 	private RawDataElement rawDataElement;
 	private Period period;
 	
 	private TransactionTemplate transactionTemplate;
 	
 	private TransactionTemplate getTransactionTemplate() {
 		if (transactionTemplate == null) {
 			transactionTemplate = new TransactionTemplate(transactionManager);
 			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
 		}
 		return transactionTemplate;
 	}
 	
 	public NormalizedDataImporter(LocationService locationService,
 			ValueService valueService, DataService dataService, 
 			SessionFactory sessionFactory, PlatformTransactionManager transactionManager,
 			ImporterErrorManager manager, RawDataElement rawDataElement,
 			Period period) {
 		super(valueService);
 		this.locationService = locationService;
 		this.dataService = dataService;
 		this.sessionFactory = sessionFactory;
 		this.transactionManager = transactionManager;
 		this.manager = manager;
 		this.rawDataElement = rawDataElement;
 		this.period = period;
 	}
 
 	
 	/**
 	 * Imports numberLinesToImport lines and then returns. Returns true when file has been read entirely.
 	 * 
 	 * @param fileName
 	 * @param reader
 	 * @param numberOfLinesToImport
 	 * @param sanitizer
 	 * @param readFileAsMap
 	 * @param headers
 	 * @param positions
 	 * @throws IOException
 	 */
 	private boolean importData(String fileName, Reader reader, Integer numberOfLinesToImport, ImportSanitizer sanitizer, ICsvMapReader readFileAsMap, String[] headers, Map<String, Integer> positions) throws IOException {
 
 		DataLocation dataLocation=null;
 		RawDataElementValue rawDataElementValue= null;	
 		String code=null;
 		
 		Map<String,Object> positionsValueMap = new HashMap<String, Object>();
 		Map<String,String> values = readFileAsMap.read(headers);
 		
 		int importedLines = 0;
 		while (values != null && importedLines < numberOfLinesToImport) {
 			if (log.isInfoEnabled()) log.info("starting import of line with values: "+values);
 			
 			sanitizer.setLineNumber(readFileAsMap.getLineNumber());
 			sanitizer.setNumberOfErrorInRows(0);
 			
 			if(log.isDebugEnabled()) log.debug("current facility code: "+values.get(CODE_HEADER));
 			
 			if (values.get(CODE_HEADER)!=null && !values.get(CODE_HEADER).equals(code)) {
 				// either we are reading the first line and there is no current location
 				// or we change location
 				
 				// first we save the data of the preceding lines
 				if (dataLocation != null) {
 					// we merge and save the current data
 					saveAndMergeIfNotNull(rawDataElementValue, positionsValueMap, positions, code, sanitizer);
 					
 					// clear the value map since we are reading a line for a new location
 					positionsValueMap.clear();
 				}
 				
 				// second we get the new location
 				// 1 update the current code
 				code = values.get(CODE_HEADER);
 				// 2 update and save the position	
 				if (positions.get(code) == null) positions.put(code, 0);
 				// 3 update the location
 				dataLocation = locationService.findCalculationLocationByCode(code, DataLocation.class);
 				// if the location is not found, we add an error
 				if (dataLocation == null) {
 					manager.getErrors().add(new ImporterError(fileName,readFileAsMap.getLineNumber(),CODE_HEADER,"import.error.message.unknown.location"));
 				} 
 				else {
 					// get the value associated to the new location
 					rawDataElementValue = valueService.getDataElementValue(rawDataElement, dataLocation, period);
 					if(rawDataElementValue == null) {
 						rawDataElementValue = new RawDataElementValue(rawDataElement, dataLocation, period, new Value(""));
 					}
 				}
 			}
 			
 			if (dataLocation == null) {
 				manager.incrementNumberOfUnsavedRows();
 			}
 			else {
 				// read values from line and put into valueMap
 				for (String header : headers){
 					if (!header.equals(CODE_HEADER)){
 						positionsValueMap.put("[" + positions.get(code) + "]."+ header, values.get(header));
 					}		
 				}
 				// increment number of lines read for this location
 				positions.put(code, positions.get(code) + 1);
 			
 				if (sanitizer.getNumberOfErrorInRows()>0) manager.incrementNumberOfRowsSavedWithError(1);
 				manager.incrementNumberOfSavedRows();
 			}
 			if (log.isInfoEnabled()) log.info("finished importing line");
 			
 			// we increment the number of imported lines to stop the while loop when it reaches numberOfLinesToImport
 			importedLines++;
 			
 			// read new line
 			if (importedLines < numberOfLinesToImport) values = readFileAsMap.read(headers);
 		}
 		
 		saveAndMergeIfNotNull(rawDataElementValue, positionsValueMap, positions, code, sanitizer);
 		return values == null;
 	}
 	
 	private void saveAndMergeIfNotNull(RawDataElementValue rawDataElementValue, Map<String,Object> positionsValueMap, Map<String, Integer> positions, String code, ImportSanitizer sanitizer) {
 		if (rawDataElementValue != null) {
 			positionsValueMap.put("", getLineNumberString(positions.get(code)-1));
 
 			if (log.isDebugEnabled()) log.debug("merging with data from map of header and data "+ positionsValueMap);
 			if (log.isTraceEnabled()) log.trace("value before merge" + rawDataElementValue.getValue());
 			rawDataElementValue.setValue(
 				rawDataElement.getType().mergeValueFromMap(rawDataElementValue.getValue(), positionsValueMap, "", new HashSet<String>(), sanitizer)
 			);
 			if (log.isTraceEnabled()) log.trace("value after merge " + rawDataElementValue.getValue());
 			
 			valueService.save(rawDataElementValue);
			sessionFactory.getCurrentSession().evict(rawDataElementValue);
 			if (log.isTraceEnabled()) log.trace("saved rawDataElement: "+ rawDataElementValue.getValue());
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.chai.kevin.importer.Importer#importData(java.lang.String, java.io.Reader)
 	 */
 	@Override
 	public void importData(final String fileName, final Reader reader) throws IOException {
 		if (log.isDebugEnabled()) log.debug("importData(FileName:"+fileName+"Reader:" + reader + ")");
 		
 		manager.setCurrentFileName(fileName);
 		
 		final ICsvMapReader readFileAsMap = new CsvMapReader(reader, CsvPreference.EXCEL_PREFERENCE);
 		final String[] headers = readFileAsMap.getCSVHeader(true);
 		
 		Map<String,Type> types = new HashMap<String,Type>();
 		
 		for (String header : headers)  { 
 			try {
 				if(!header.equals(CODE_HEADER))
 					types.put("[_]."+header,rawDataElement.getType().getType("[_]."+header));
 			} catch(IllegalArgumentException e){
 				if(log.isWarnEnabled()) log.warn("Column type not found for header"+header, e);
 				manager.getErrors().add(new ImporterError(fileName,readFileAsMap.getLineNumber(),header,"import.error.message.unknowm.column.type"));
 			}
 		}
 				
 		final ImportSanitizer sanitizer = new ImportSanitizer(fileName,manager.getErrors(), types, dataService);
 		final Map<String,Integer> positions = new HashMap<String,Integer>();
 		
 		boolean readEntirely = false;
 		
 		while (!readEntirely) {
 			readEntirely = (Boolean)getTransactionTemplate().execute(new TransactionCallback() {
 				@Override
 				public Object doInTransaction(TransactionStatus arg0) {
 					sessionFactory.getCurrentSession().refresh(rawDataElement);
 					sessionFactory.getCurrentSession().refresh(period);
 					
 					try {
 						return importData(fileName, reader, NUMBER_OF_LINES_TO_IMPORT, sanitizer, readFileAsMap, headers, positions);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						return true;
 					}
 				}
 			});
 			sessionFactory.getCurrentSession().clear();
 		}
 		
 	}
 
 }
