 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.geobatch.destination;
 
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Transaction;
 import org.geotools.factory.Hints;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.jdbc.JDBCDataStore;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it"
  *
  */
 public class RoadArc extends IngestionObject {
 	
 	private final static Logger LOGGER = LoggerFactory.getLogger(RoadArc.class);
 		
 	private static Pattern typeNameParts  = Pattern.compile("^([A-Z]{2})_([A-Z]{1})_([A-Za-z]+)_([0-9]{8})$");
 	
 	private int partner;
 	private String codicePartner;
 	private String date;
 	
 	public static Properties aggregation = new Properties();
 	public static Properties bersaglio = new Properties();
 		
 	private String gridTypeName = "siig_geo_grid";
 	
 	private String geoTypeName = "siig_geo_ln_arco_X";	
 	private String geoId = "id_geo_arco";
 	
 	private String byVehicleTypeName = "siig_r_tipovei_geoarcoX";
 	private String dissestoTypeName = "siig_r_arco_X_dissesto";
 	private String tipobersTypeName = "siig_r_arco_X_scen_tipobers";
 	private String sostanzaTypeName = "siig_t_sostanza";
 	private String sostanzaArcoTypeName = "siig_r_arco_X_sostanza";
 	
 	
 	private static Map attributeMappings = null;		
 	
 	static {	
 		// load mappings from resources				
 		attributeMappings = (Map) readResourceFromXML("/roadarcs.xml");	
 		
 		InputStream aggregationStream = null;
                 InputStream bersaglioStream = null;
 		try {
 		        aggregationStream = RoadArc.class.getResourceAsStream("/aggregation.properties");
 		        bersaglioStream = RoadArc.class.getResourceAsStream("/bersaglio.properties");
 			aggregation.load(aggregationStream);
 			bersaglio.load(bersaglioStream);
 		} catch (IOException e) {
 			LOGGER.error("Unable to load configuration: "+e.getMessage(), e);
 		} finally{
 		    try {
                         if(bersaglioStream != null){
 		            bersaglioStream.close();
                         }
                     } catch (IOException e) {
                         LOGGER.error(e.getMessage(), e);
                     }
 		    try {
 		        if(aggregationStream != null){
                             aggregationStream.close();
 		        }
                     } catch (IOException e) {
                         LOGGER.error(e.getMessage(), e);
                     }
 		}
 	}
 	
 	/**
 	 * Initializes a VectorTarget handler for the given input feature.
 	 * 
 	 * @param inputTypeName
 	 */
 	public RoadArc(String inputTypeName, ProgressListenerForwarder listenerForwarder) {
 		super(inputTypeName, listenerForwarder);		
 	}
 	
 	/**
 	 * Parse input feature typeName and extract useful information from it. 
 	 */
 	protected boolean parseTypeName(String typeName) {
 		Matcher m = typeNameParts.matcher(typeName);
 		if(m.matches()) {
 			// partner alphanumerical abbreviation (from siig_t_partner)
 			codicePartner = m.group(1);
 			// partner numerical id (from siig_t_partner)
 			partner = Integer.parseInt(partners.get(codicePartner).toString());
 			// target macro type (bu or bnu)			
 			// file date identifier
 			date = m.group(4);			
 			
 			// TODO: add other validity checks
 			
 			return true;
 		}
 		return false;
 	}
 		
 	/**
 	 * @param geoTypeName2
 	 * @param aggregationLevel
 	 * @return
 	 */
 	private String getTypeName(String typeName, int aggregationLevel) {		
 		return typeName.replace("X", aggregationLevel+"");
 	}
 
 	/**
 	 * Imports the arcs feature from the original Feature to the SIIG
 	 * arcs tables (in staging).
 	 * 
 	 * @param datastoreParams
 	 * @param crs
 	 * @throws IOException
 	 */
 	public void removeZeros(Map<String, Serializable> datastoreParams,
 			CoordinateReferenceSystem crs, int aggregationLevel, boolean onGrid, boolean dropInput) throws IOException {
 		reset();
 		
 		double kinc = 1;
 		if(isValid()) {				
 			JDBCDataStore dataStore = null;					
 			
 			crs = checkCrs(crs);			
 			
 			int process = -1;
 			int trace = -1;
 			
 			int errors = 0;
 			
 			
 			
 			String processPhase = "C";
 			
 			try {												
 				dataStore = connectToDataStore(datastoreParams);
 				
 				Ingestion.Process importData = getProcessData(dataStore);
 				process = importData.getId();
 				trace = importData.getMaxTrace();
 				errors = importData.getMaxError();
 				
 				// setup input reader								
 				createInputReader(dataStore, null, onGrid ? gridTypeName : null);
 								
 				
 				// setup geo output object
 				String geoName = getTypeName(geoTypeName, aggregationLevel);
 				OutputObject geoObject = new OutputObject(dataStore, null, geoName, geoId);	
 				
 				// now we aggregate on 3rd aggregation level, waiting for 
 				String aggregationAttribute = aggregation.getProperty("3");
 				// get unique aggregation values		
 				Set<Integer> aggregationValues = getAggregationValues(aggregationAttribute);
 				
 				for(int aggregationValue : aggregationValues) {						
 					setInputFilter(filterFactory.equals(
 						filterFactory.property(aggregationAttribute),
 						filterFactory.literal(aggregationValue)
 					));
 					//int arcs = getImportCount();
 					Long incidenti = (Long)getSumOnInput("INCIDENT", new Long(0));
 					if(incidenti != 0) {
 						Long lunghezzaTotale = (Long)getSumOnInput("LUNGHEZZA", new Long(0));
 						
 						Double weightedSum = 0.0;		
 						int n = 0;
 						int m = 0;
 						
 						SimpleFeature inputFeature;
 						try {
 							while( (inputFeature = readInput()) != null) {
 								Integer nrIncidenti = (Integer)inputFeature.getAttribute("INCIDENT");
 								Integer lunghezza = (Integer)inputFeature.getAttribute("LUNGHEZZA");
 								if(nrIncidenti == 0) {
 									n += lunghezza;
 								} else {
 									m += lunghezza;
 								}
 								weightedSum += (double)(nrIncidenti * lunghezza);
 							}
 							
 							
 						} finally {
 							closeInputReader();
 						}	
 						
 						Double avg = weightedSum / lunghezzaTotale;
 						
 						
 						Double inc = kinc * avg;
 						Double dec = inc * n / m;
 						
 						try {
 							while( (inputFeature = readInput()) != null) {
 								Integer nrIncidenti = (Integer)inputFeature.getAttribute("INCIDENT");
 								
 								double newIncidenti = (double)nrIncidenti;
 								if(newIncidenti == 0) {
 									newIncidenti += inc;
 								} else {
 									newIncidenti -= dec;
 								}
 								
 								updateIncidentalita(geoObject, inputFeature, newIncidenti);
 							}
 						} finally {
 							closeInputReader();
 						}	
 						
 						
 					}
 					
 					
 				}
 				
 			} catch (IOException e) {
 				errors++;	
 				Ingestion.logError(dataStore, trace, errors, "Error importing data", getError(e), 0);				
 				throw e;
 			} finally {
 				if(dropInput) {
 					dropInputFeature(datastoreParams);
 				}
 				
 				if(process != -1) {
 					// close current process phase
 					Ingestion.closeProcessPhase(dataStore, process, processPhase);
 				}
 				
 				if(dataStore != null) {
 					dataStore.dispose();
 				}				
 			}
 		}
 	}
 	
 	/**
 	 * @param geoObject
 	 * @param inputFeature
 	 * @param newIncidenti
 	 * @throws IOException 
 	 */
 	private void updateIncidentalita(OutputObject geoObject,
 			SimpleFeature inputFeature, double newIncidenti) throws IOException {
 		Filter updateFilter = filterFactory.and(filterFactory.equals(
 			filterFactory.property("fk_partner"), filterFactory.literal(partner)
 		),filterFactory.equals(
 			filterFactory.property("id_tematico_shape"), filterFactory.literal(getMapping(inputFeature, attributeMappings, "id_tematico_shape")))
 		);
 		geoObject.getWriter().modifyFeatures(geoObject.getSchema().getDescriptor("nr_incidenti_elab").getName(), newIncidenti, updateFilter);
 	}
 
 	/**
 	 * Imports the arcs feature from the original Feature to the SIIG
 	 * arcs tables (in staging).
 	 * 
 	 * @param datastoreParams
 	 * @param crs
 	 * @throws IOException
 	 */
 	public void importArcs(Map<String, Serializable> datastoreParams,
 			CoordinateReferenceSystem crs, int aggregationLevel, boolean onGrid, boolean dropInput) throws IOException {
 		reset();
 		if(isValid()) {				
 			JDBCDataStore dataStore = null;		
 			
 			
 			crs = checkCrs(crs);			
 			
 			int process = -1;
 			int trace = -1;
 			
 			int errors = 0;
 			
 			String processPhase = "A";
 			
 			try {												
 				dataStore = connectToDataStore(datastoreParams);				
 				// create a new process record for the ingestion
 				if(aggregationLevel == 1) {
 					process = createProcess(dataStore);
 					// write log for the imported file
 					trace = logFile(dataStore,  process, NO_TARGET,
 							partner, codicePartner, date, false);
 				} else {
 					Ingestion.Process importData = getProcessData(dataStore);
 					process = importData.getId();
 					trace = importData.getMaxTrace();
 					errors = importData.getMaxError();
 				}																										
 				
 				// setup input reader								
 				createInputReader(dataStore, null, onGrid ? gridTypeName : null);						
 				
 				Transaction transaction = new DefaultTransaction();
 				
 				// setup geo output object
 				String geoName = getTypeName(geoTypeName, aggregationLevel);
 				OutputObject geoObject = new OutputObject(dataStore, transaction, geoName, geoId);				
 				
 				// setup vehicle output object
 				String vehicleName = getTypeName(byVehicleTypeName, aggregationLevel);
 				OutputObject vehicleObject = new OutputObject(dataStore, transaction, vehicleName, "");
 								
 				// setup dissesto output object
 				String dissestoName = getTypeName(dissestoTypeName, aggregationLevel);
 				OutputObject dissestoObject = new OutputObject(dataStore, transaction, dissestoName, "");
 				
 	                        // setup CFF output object
                                 String tipobersName = getTypeName(tipobersTypeName, aggregationLevel);
                                 OutputObject tipobersObject = new OutputObject(dataStore, transaction, tipobersName, "");
                                 
                                 // setup  sostanza output object
                                 String tiposostName = getTypeName(sostanzaArcoTypeName, aggregationLevel);
                                 OutputObject tiposostObject = new OutputObject(dataStore, transaction, tiposostName, "");
                                 
 				OutputObject[] outputObjects = new OutputObject[] {vehicleObject,
 						dissestoObject, geoObject, tipobersObject, tiposostObject};
 				
 				BigDecimal maxId = null;
 				
 				try {
 					// remove previous data for the given partner
 					Filter removeFilter = filterFactory.equals(
 						filterFactory.property("fk_partner"), filterFactory.literal(partner)
 					);
 					removeObjects(outputObjects, removeFilter);
 					
 					maxId = (BigDecimal)getOutputId(geoObject);
 					
 					transaction.commit();	
 				} catch (IOException e) {
 					errors++;	
 					Ingestion.logError(dataStore, trace, errors, "Error removing old data", getError(e), 0);					
 					transaction.rollback();					
 					throw e;
 				} finally {
 					transaction.close();
 				}
 									
 				// calculates total objects to import				
 				int total = getImportCount();				
 				
 				
 				
 				if(onGrid) {
 					processPhase = "C";
 					errors = aggregateArcsOnGrid(trace, dataStore, outputObjects, maxId.intValue(), total, errors, geoName, aggregationLevel);
 				} else if(aggregationLevel == 1) {
 					errors = importWithoutAggregation(trace, dataStore, outputObjects, maxId.intValue(), total, errors, geoName);
 				} else {
 					processPhase = "B";
 					errors = aggregateArcs(trace, dataStore, outputObjects, maxId.intValue(), total, errors, geoName, aggregationLevel);
 				}
 				Ingestion.updateLogFile(dataStore, trace, total, errors, processPhase);
 			} catch (IOException e) {
 				errors++;	
 				Ingestion.logError(dataStore, trace, errors, "Error importing data", getError(e), 0);				
 				throw e;
 			} finally {
 				if(dropInput) {
 					dropInputFeature(datastoreParams);
 				}
 				
 				if(process != -1) {
 					// close current process phase
 					Ingestion.closeProcessPhase(dataStore, process, processPhase);
 				}
 				
 				if(dataStore != null) {
 					dataStore.dispose();
 				}				
 			}
 		}
 	}
 
 
 
 	/**
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects
 	 * @param intValue
 	 * @param total
 	 * @param errors
 	 * @param geoName
 	 * @param aggregationLevel
 	 * @return
 	 * @throws IOException 
 	 */
 	private int aggregateArcsOnGrid(int trace, JDBCDataStore dataStore,
 			OutputObject[] outputObjects, int startId, int total, int errors,
 			String outputName, int aggregationLevel) throws IOException {
 		try {
 			String inputGeometryName = getInputGeometryName(dataStore);
 			
 			SimpleFeature gridFeature = null;			
 			while( (gridFeature = readInput()) != null) {
 				
 				int id = nextId(startId);							
 				int idTematico = ((BigDecimal)gridFeature.getAttribute("gid")).intValue();
 				
 				Geometry cell = (Geometry)gridFeature.getDefaultGeometry();
 				
 				FeatureSource<SimpleFeatureType, SimpleFeature> reader = createInputReader(
 						dataStore, null, null);				
 				
 				FeatureIterator<SimpleFeature> iterator = reader.getFeatures(filterFactory.intersects(
 						filterFactory.property(inputGeometryName), 
 						filterFactory.literal(cell)
 				)).features();
 				
 				try  {
 					errors = aggregateStep(trace, dataStore, outputObjects, total,
 							errors, outputName, id, idTematico, iterator, cell);
 				} finally {
 					iterator.close();
 				}
 				
 																				
 			}
 			importFinished(total, "Data imported in "+ outputName);
 			
 		} finally {
 			closeInputReader();
 		}				
 																				
 		
 		return errors;
 	}
 
 	/**
 	 * @throws IOException 
 	 * 
 	 */
 	private int aggregateArcs(int trace, JDBCDataStore dataStore,
 			OutputObject[] outputObjects, int startId, int total, int errors,
 			String outputName, int aggregationLevel) throws IOException {
 		String aggregationAttribute = aggregation.getProperty(aggregationLevel + "");
 		// get unique aggregation values		
 		Set<Integer> aggregationValues = getAggregationValues(aggregationAttribute);		
 		
 		for(int aggregationValue : aggregationValues) {						
 			
 			int id = nextId(startId);			
 			int idTematico = aggregationValue;						
 			
 			setInputFilter(filterFactory.equals(
 				filterFactory.property(aggregationAttribute),
 				filterFactory.literal(aggregationValue)
 			));
 			try {
 				errors = aggregateStep(trace, dataStore, outputObjects, total,
 						errors, outputName, id, idTematico, null, null);
 			} finally {
 				closeInputReader();
 			}	
 			
 		}
 		importFinished(total, "Data imported in "+ outputName);
 		return errors;		
 	}
 
 	/**
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects
 	 * @param total
 	 * @param errors
 	 * @param outputName
 	 * @param id
 	 * @param idTematico
 	 * @return
 	 * @throws IOException
 	 */
 	private int aggregateStep(int trace, JDBCDataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors,
 			String outputName, int id, int idTematico, FeatureIterator<SimpleFeature> iterator, Geometry aggregateGeo) throws IOException {
 		
 		SimpleFeature inputFeature;
 		Geometry geo = null;
 		int lunghezza = 0;
 		int incidenti = 0;
 		int corsie = 0;
 		int[] tgm = new int[] {0, 0};
 		int[] velocita = new int[] {0, 0};
 		Set<Integer> pterr = new HashSet<Integer>();
 		
 		while( (inputFeature = readInput(iterator)) != null) {	
 			try {
 				if(aggregateGeo == null) {
 					// geo				
 					if(geo == null) {
 						geo = (Geometry)inputFeature.getDefaultGeometry();
 					} else {
 						geo = geo.union((Geometry)inputFeature.getDefaultGeometry());
 					}
 				} else {
 					geo = aggregateGeo;
 				}
 				int currentLunghezza = ((Number)getMapping(inputFeature, attributeMappings, "lunghezza")).intValue(); 
 				lunghezza +=  currentLunghezza;
 				incidenti += ((Number)getMapping(inputFeature, attributeMappings, "nr_incidenti")).intValue();
 				corsie += ((Number)getMapping(inputFeature, attributeMappings, "nr_corsie")).intValue() * currentLunghezza;
 				
 				// by vehicle
 				int[] tgms = extractMultipleValues(inputFeature, "TGM");
 				int[] velocitas = extractMultipleValues(inputFeature, "VELOCITA");
 				
 				for(int i=0; i<tgms.length; i++) {
 					tgm[i] += tgms[i] * currentLunghezza;
 					velocita[i] += velocitas[i] * currentLunghezza;
 				}
 				
 				// dissesto
 				String[] pterrs = inputFeature.getAttribute("PTERR") == null ? null : inputFeature.getAttribute("PTERR").toString().split("\\|");					
 				
 				for(int j=0; j < pterrs.length; j++) {
 					try {
 						int dissesto = Integer.parseInt(pterrs[j]);
 						pterr.add(dissesto);									
 					} catch(NumberFormatException e) {
 						
 					}
 				}
 			} catch(Exception e) {						
 				errors++;
 				Ingestion.logError(dataStore, trace, errors,
 						"Error writing output feature", getError(e),
 						idTematico);
 			}
 		}
 		if(geo != null) {
 			Transaction rowTransaction = new DefaultTransaction();
 			setTransaction(outputObjects, rowTransaction);			
 			
 			try {							
 				addAggregateGeoFeature(outputObjects[2], id, idTematico, geo,
 						lunghezza, corsie, incidenti, inputFeature);						
 				addAggregateVehicleFeature(outputObjects[0], id, lunghezza,
 						tgm, velocita, inputFeature);
 				addAggregateDissestoFeature(outputObjects[1], id, lunghezza,
 						pterr, inputFeature);
 				
 				rowTransaction.commit();
 				
 				updateImportProgress(total, "Importing data in " + outputName);
 			} catch(Exception e) {						
 				errors++;
 				rollbackId();
 				rowTransaction.rollback();
 				Ingestion.logError(dataStore, trace, errors,
 						"Error writing output feature", getError(e),
 						idTematico);
 			} finally {	
 				rowTransaction.close();							
 			}
 		} else {
 			rollbackId();
 		}
 		return errors;
 	}
 
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param lunghezza
 	 * @param pterr
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateDissestoFeature(OutputObject outputObject, int id,
 			int lunghezza, Set<Integer> pterr, SimpleFeature inputFeature) throws IOException {
 		SimpleFeatureBuilder dissestoFeatureBuilder = outputObject.getBuilder();
 		
 		for(int dissesto : pterr) {			
 			// compiles the attributes from target and read feature data, using mappings
 			// to match input attributes with output ones
 			for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					dissestoFeatureBuilder.add(id);
 				} else if(attr.getLocalName().equals("id_dissesto")) {
 					dissestoFeatureBuilder.add(dissesto);
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					dissestoFeatureBuilder.add(partner+"");
 				} else {
 					dissestoFeatureBuilder.add(null);
 				}
 			}
 			String featureid = dissesto + "." + id;
 			SimpleFeature feature = dissestoFeatureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			outputObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}
 	}
 
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param idTematico
 	 * @param tgm
 	 * @param velocita
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateVehicleFeature(OutputObject outputObject, int id,
 			int lunghezza, int[] tgm, int[] velocita,
 			SimpleFeature inputFeature) throws IOException {
 		SimpleFeatureBuilder byvehicleFeatureBuilder = outputObject.getBuilder();
 
 		for(int type = 0; type <= 1;type++) {
 			for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					byvehicleFeatureBuilder.add(id);
 				} else if(attr.getLocalName().equals("densita_veicolare")) {
 					if(lunghezza == 0) {
 						byvehicleFeatureBuilder.add(0);
 					} else {
 						byvehicleFeatureBuilder.add(tgm[type] / lunghezza);
 					}
 				} else if(attr.getLocalName().equals("id_tipo_veicolo")) {
 					byvehicleFeatureBuilder.add(type + 1);
 				} else if(attr.getLocalName().equals("velocita_media")) {
 					if(lunghezza == 0) {
 						byvehicleFeatureBuilder.add(0);
 					} else {
 						byvehicleFeatureBuilder.add(velocita[type] / lunghezza);
 					}
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					byvehicleFeatureBuilder.add(partner+"");
 				} else {
 					byvehicleFeatureBuilder.add(null);
 				}
 			}
 			String featureid = (type + 1) + "." + id;
 			SimpleFeature feature = byvehicleFeatureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			outputObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}
 	}
 
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param geo
 	 * @param lunghezza
 	 * @param corsie
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateGeoFeature(OutputObject outputObject, int id, int idTematico,
 			Geometry geo, int lunghezza, int corsie, int incidenti, SimpleFeature inputFeature) throws IOException {
 		SimpleFeatureBuilder geoFeatureBuilder = outputObject.getBuilder();
 		for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 			if(attr.getLocalName().equals(geoId)) {
 				geoFeatureBuilder.add(id);
 			} else if(attr.getLocalName().equals("fk_partner")) {
 				geoFeatureBuilder.add(partner+"");
 			} else if(attr.getLocalName().equals("id_tematico_shape")) {
 				geoFeatureBuilder.add(idTematico+"");
 			} else if(attr.getLocalName().equals("geometria")) {
 				geoFeatureBuilder.add(geo);
 			} else if(attr.getLocalName().equals("lunghezza")) {
 				geoFeatureBuilder.add(lunghezza);
 			} else if(attr.getLocalName().equals("nr_incidenti")) {
 				geoFeatureBuilder.add(incidenti);
 			} else if(attr.getLocalName().equals("nr_corsie")) {
 				if(lunghezza == 0) {
 					geoFeatureBuilder.add(0);
 				} else {
 					geoFeatureBuilder.add(corsie / lunghezza);
 				}
 			} else {
 				geoFeatureBuilder.add(null);
 			}
 		}
 		SimpleFeature geoFeature = geoFeatureBuilder.buildFeature(null);		
 		outputObject.getWriter().addFeatures(DataUtilities
 				.collection(geoFeature));
 	}
 
 	/**
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects
 	 * @param total
 	 * @param errors
 	 * @param outputName
 	 * @param inputFeature
 	 * @param id
 	 * @param idTematico
 	 * @return
 	 * @throws IOException
 	 */
 	private int writeOutputObjects(int trace, JDBCDataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors,
 			String outputName, SimpleFeature inputFeature, int id,
 			int idTematico) throws IOException {
 		Transaction rowTransaction = new DefaultTransaction();
 		setTransaction(outputObjects, rowTransaction);
 		
 		try {							
 			addGeoFeature(outputObjects[2], id, inputFeature);						
 			addVehicleFeature(outputObjects[0], id, inputFeature);
 			addDissestoFeature(outputObjects[1], id, inputFeature);
 			addCFFFeature(outputObjects[3], id, inputFeature);
 			addSostanzaFeature(outputObjects[4], id, inputFeature, dataStore);
 			
 			rowTransaction.commit();
 			
 			updateImportProgress(total, "Importing data in " + outputName);
 		} catch(Exception e) {						
 			errors++;
 			rollbackId();
 			rowTransaction.rollback();
 			Ingestion.logError(dataStore, trace, errors,
 					"Error writing output feature", getError(e),
 					idTematico);
 		} finally {				
 			rowTransaction.close();							
 		}
 		return errors;
 	}
 	
 
 	/**
 	 * @throws IOException 
 	 * 
 	 */
 	private int importWithoutAggregation(int trace, JDBCDataStore dataStore,
 			OutputObject[] outputObjects, int startId, int total, int errors,
 			String outputName) throws IOException {
 		try {
 			SimpleFeature inputFeature = null;
 			while( (inputFeature = readInput()) != null) {
 								
 				int id = nextId(startId);							
 				int idTematico = getIdTematico(inputFeature, attributeMappings);
 				
 				errors = writeOutputObjects(trace, dataStore, outputObjects,
 						total, errors, outputName, inputFeature, id, idTematico);						
 			}
 			importFinished(total, "Data imported in "+ outputName);
 			
 		} finally {
 			closeInputReader();
 		}				
 																				
 		
 		return errors;
 	}
 
 	/**
 	 * Adds arc - vehicletype data feature.
 	 * 
 	 */
 	private void addVehicleFeature(OutputObject vehicleObject,
 			int id, SimpleFeature inputFeature)
 			throws IOException {
 		
 		SimpleFeatureBuilder featureBuilder = vehicleObject.getBuilder();
 		
 		int[] tgm = extractMultipleValues(inputFeature, "TGM");
 		int[] velocita = extractMultipleValues(inputFeature, "VELOCITA");
 		
 		
 		for(int type = 0; type <= 1; type++) {
 			featureBuilder.reset();
 			// compiles the attributes from target and read feature data, using mappings
 			// to match input attributes with output ones
 			for(AttributeDescriptor attr : vehicleObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					featureBuilder.add(id);
 				} else if(attr.getLocalName().equals("densita_veicolare")) {
 					featureBuilder.add(tgm[type]);
 				} else if(attr.getLocalName().equals("id_tipo_veicolo")) {
 					featureBuilder.add(type + 1);
 				} else if(attr.getLocalName().equals("velocita_media")) {
 					featureBuilder.add(velocita[type]);
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					featureBuilder.add(partner+"");
 				} else if(attr.getLocalName().equals("flg_velocita")) {
                                    featureBuilder.add(inputFeature.getAttribute(""+attributeMappings.get("flg_veloc")));
 				} else if(attr.getLocalName().equals("flg_densita_veicolare")) {
                                    featureBuilder.add(null);
 				} else {
 					featureBuilder.add(null);
 				}
 			}
 			String featureid = (type + 1) + "." + id;
 			SimpleFeature feature = featureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			vehicleObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}		
 	}
 
 	/**
 	 * @param inputFeature
 	 * @return
 	 */
 	private int[] extractMultipleValues(SimpleFeature inputFeature, String attributeName) {
 		String[] svalues = inputFeature.getAttribute(attributeName).toString().split("\\|");				
 		int[] values = new int[] {0,0};		
 		
 		for(int count=0; count < svalues.length; count++) {
 			try {
 				values[count] = Integer.parseInt(svalues[count]);
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 		return values;
 	}
 	
 	/**
 	 * Adds arc - dissesto data feature.
 	 * 
 	 */
 	private void addDissestoFeature(OutputObject dissestoObject,
 			int id, SimpleFeature inputFeature) throws IOException {
 		
 		SimpleFeatureBuilder featureBuilder = dissestoObject.getBuilder();
 		
 		String[] pterrs = inputFeature.getAttribute("PTERR") == null ? null : inputFeature.getAttribute("PTERR").toString().split("\\|");					
 		
 		for(int count=0; count < pterrs.length; count++) {
 			try {
 				int dissesto = Integer.parseInt(pterrs[count]);
 				featureBuilder.reset();
 				// compiles the attributes from target and read feature data, using mappings
 				// to match input attributes with output ones
 				for(AttributeDescriptor attr : dissestoObject.getSchema().getAttributeDescriptors()) {
 					if(attr.getLocalName().equals(geoId)) {
 						featureBuilder.add(id);
 					} else if(attr.getLocalName().equals("id_dissesto")) {
 						featureBuilder.add(dissesto);
 					} else if(attr.getLocalName().equals("fk_partner")) {
 						featureBuilder.add(partner+"");
 					} else {
 						featureBuilder.add(null);
 					}
 				}
 				String featureid = id + "." + dissesto;
 				SimpleFeature feature = featureBuilder.buildFeature(featureid);
 				feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 				
 				dissestoObject.getWriter().addFeatures(DataUtilities
 						.collection(feature));
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 				
 	}
 	
 	private void addCFFFeature(OutputObject cffObject,
                 int id, SimpleFeature inputFeature) throws IOException {
 	    
 	    SimpleFeatureBuilder featureBuilder = cffObject.getBuilder();
 	    Object cffAttribute = inputFeature.getAttribute("CFF");	    
             String[] cffAttributeSplitted =  cffAttribute == null ? null : cffAttribute.toString().split("\\|");                                       
             
             for(int count=0; count < cffAttributeSplitted.length; count++) {
                     try {
                             String el = cffAttributeSplitted[count].replace(",", ".");
                             double cffElement = Double.parseDouble(el);
                             featureBuilder.reset();
                             // compiles the attributes from target and read feature data, using mappings
                             // to match input attributes with output ones
                             for(AttributeDescriptor attr : cffObject.getSchema().getAttributeDescriptors()) {
                                     if(attr.getLocalName().equals(geoId)) {
                                             featureBuilder.add(id);
                                     } else if(attr.getLocalName().equals("cff")) {
                                             featureBuilder.add(cffElement);
                                     } else if(attr.getLocalName().equals("id_bersaglio")) {
                                             featureBuilder.add(bersaglio.getProperty(Integer.toString(count)));
                                     } else if(attr.getLocalName().equals("fk_partner")) {
                                             featureBuilder.add(partner+"");
                                     } else {
                                             featureBuilder.add(null);
                                     }
                             }
 //                            String fid2 = el.replaceAll("\\,", "");
 //                            fid2 = el.replaceAll("\\.", "");
                             String featureid = id + "." + count;
                             SimpleFeature feature = featureBuilder.buildFeature(featureid);
                             feature.getUserData().put(Hints.USE_PROVIDED_FID, true);                        
                             
                             cffObject.getWriter().addFeatures(DataUtilities
                                             .collection(feature));
                     } catch(NumberFormatException e) {
                             
                     }
             }
 	}
 	
         private void addSostanzaFeature(OutputObject sostanzaObject, int id, SimpleFeature inputFeature, JDBCDataStore datastore)
                 throws IOException {
     
             SimpleFeatureBuilder featureBuilder = sostanzaObject.getBuilder();
     
             Transaction transaction = new DefaultTransaction();
             OutputObject tipobersObject = new OutputObject(datastore, transaction, sostanzaTypeName,
                     "");
             FeatureCollection<SimpleFeatureType, SimpleFeature> bersaglioCollection = tipobersObject
                     .getReader().getFeatures();
             FeatureIterator iter = bersaglioCollection.features();
             try{
                 while (iter.hasNext()) {
                     SimpleFeature sf = (SimpleFeature) iter.next();
                     BigDecimal bd = (BigDecimal)sf.getAttribute("id_sostanza");
                     String id_sostanza = bd.toString();
                     for (AttributeDescriptor attr : sostanzaObject.getSchema().getAttributeDescriptors()) {
                         if (attr.getLocalName().equals(geoId)) {
                             featureBuilder.add(id);
                         } else if (attr.getLocalName().equals("id_sostanza")) {
                             featureBuilder.add(id_sostanza);
                         } else if (attr.getLocalName().equals("padr")) {
                             featureBuilder.add(.5f);
                         } else if(attr.getLocalName().equals("fk_partner")) {
                             featureBuilder.add(partner+"");
                         } else {
                             featureBuilder.add(null);
                         }
                     }
                     String featureid = id + "." + id_sostanza;
                     SimpleFeature feature = featureBuilder.buildFeature(featureid);
                     feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
     
                     sostanzaObject.getWriter().addFeatures(DataUtilities.collection(feature));
                 }
             }finally{
                 if(iter != null){
                     iter.close();
                 }
                 if(transaction != null){
                     transaction.close();
                 }
             }
         }
 	
 	
 	
 	/**
 	 * @param e
 	 * @return
 	 */
 	private String getError(Exception e) {		
 		// TODO: human readble error
 		Throwable t = e;
 		while(t.getCause() != null) {
 			t=t.getCause();
 		}
 		
 		return t.getMessage().substring(0,Math.min(t.getMessage().length(), 1000));
 	}
 	
 	/**
 	 * Adds a new geo arc feature.
 	 * 
 	 * @param geoSchema
 	 * @param geoFeatureBuilder
 	 * @param geoFeatureWriter
 	 * @param inputFeature
 	 * @param id
 	 * @throws IOException
 	 */
 	private void addGeoFeature(OutputObject geoObject,
 			int id,  SimpleFeature inputFeature) throws IOException {				
 		SimpleFeatureBuilder geoFeatureBuilder = geoObject.getBuilder();
 		// compiles the attributes from target and read feature data
 		for(AttributeDescriptor attr : geoObject.getSchema().getAttributeDescriptors()) {
 			if(attr.getLocalName().equals(geoId)) {
 				geoFeatureBuilder.add(id);
 			} else if(attr.getLocalName().equals("fk_partner")) {
 				geoFeatureBuilder.add(partner+"");
 			} else if(attr.getLocalName().equals("geometria")) {
 				geoFeatureBuilder.add(inputFeature.getDefaultGeometry());
 			} else if(attributeMappings.containsKey(attr.getLocalName())) {
 				geoFeatureBuilder.add(getMapping(inputFeature,attributeMappings, attr.getLocalName()));
 			} else {
 				geoFeatureBuilder.add(null);
 			}
 						
 		}
 		
 		SimpleFeature geoFeature = geoFeatureBuilder.buildFeature(null);		
 		geoObject.getWriter().addFeatures(DataUtilities
 				.collection(geoFeature));
 	}
 	
 	 
 	
 }
