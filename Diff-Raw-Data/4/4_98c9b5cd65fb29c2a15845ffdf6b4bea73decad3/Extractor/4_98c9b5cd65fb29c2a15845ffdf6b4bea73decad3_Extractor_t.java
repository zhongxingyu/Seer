 package cz.opendata.linked.geocoder.google;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.openrdf.model.Graph;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.QueryEvaluationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.geocoder.Geocoder;
 import com.google.code.geocoder.GeocoderRequestBuilder;
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.GeocoderStatus;
 
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
 import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
 import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
 import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
 import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
 import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
import cz.cuni.mff.xrg.odcs.rdf.help.MyTupleQueryResultIf;
 import cz.cuni.mff.xrg.odcs.rdf.impl.MyTupleQueryResult;
 import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
 
 import org.apache.commons.io.*;
 
 @AsExtractor
 public class Extractor 
 extends ConfigurableBase<ExtractorConfig> 
 implements DPU, ConfigDialogProvider<ExtractorConfig> {
 
 	/**
 	 * DPU's configuration.
 	 */
 
 	private Logger logger = LoggerFactory.getLogger(DPU.class);
 	final Geocoder geocoder = new Geocoder();
 	private int geocodes = 0;
 	private int cacheHits = 0;
 	
 	@InputDataUnit(name = "Schema.org addresses")
 	public RDFDataUnit sAddresses;
 
 	@OutputDataUnit(name = "Geocoordinates")
 	public RDFDataUnit outGeo;	
 	
 	public Extractor() {
 		super(ExtractorConfig.class);
 	}
 
 	@Override
 	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
 		return new ExtractorDialog();
 	}
 
 	private int countTodaysCacheFiles(DPUContext ctx)
 	{
 		int count = 0;
 
 		// Directory path here
 		File currentFile;
 		File folder = ctx.getGlobalDirectory();
 		if (!folder.isDirectory()) return 0;
 
 		File[] listOfFiles = folder.listFiles(); 
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
 		for (int i = 0; i < listOfFiles.length; i++) 
 		{
 			if (listOfFiles[i].isFile()) 
 			{
 				currentFile = listOfFiles[i];
 
 				Date now = new Date();
 				Date modified = null;
 				try {
 					modified = sdf.parse(sdf.format(currentFile.lastModified()));
 				} catch (ParseException e) {
 					logger.error(e.getLocalizedMessage());
 				}
 				long diff = (now.getTime() - modified.getTime()) / 1000;
 				//System.out.println("Date modified: " + sdf.format(currentFile.lastModified()) + " which is " + diff + " seconds ago.");
 
 				if (diff < (config.hoursToCheck * 60 * 60)) count++;
 			}
 		}
 		logger.info("Total of " + count + " positions cached in last " + config.hoursToCheck + " hours. " + (config.limit - count) + " remaining.");
 		return count;
 	}
 	
 	public void execute(DPUContext ctx) throws DPUException
 	{
 		java.util.Date date = new java.util.Date();
 		long start = date.getTime();
 
 		URI dcsource = outGeo.createURI("http://purl.org/dc/terms/source");
 		URI googleURI = outGeo.createURI("https://developers.google.com/maps/documentation/javascript/geocoding");
 		URI geoURI = outGeo.createURI("http://schema.org/geo");
 		URI geocoordsURI = outGeo.createURI("http://schema.org/GeoCoordinates");
 		URI postalAddressURI = sAddresses.createURI("http://schema.org/PostalAddress");
 		URI streetAddressURI = sAddresses.createURI("http://schema.org/streetAddress");
 		URI addressRegionURI = sAddresses.createURI("http://schema.org/addressRegion");
 		URI addressLocalityURI = sAddresses.createURI("http://schema.org/addressLocality");
 		URI addressCountryURI = sAddresses.createURI("http://schema.org/addressCountry");
 		URI postalCodeURI = sAddresses.createURI("http://schema.org/postalCode");
 		//URI xsdDouble = outGeo.createURI("http://www.w3.org/2001/XMLSchema#double");
 		//URI xsdDecimal = outGeo.createURI("http://www.w3.org/2001/XMLSchema#decimal");
 		URI longURI = outGeo.createURI("http://schema.org/longitude");
 		URI latURI = outGeo.createURI("http://schema.org/latitude");
 		String countQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress . "
 				+  "}"; 
 		/*String notGCcountQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT (COUNT (*) as ?count) "
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress . "
 					+ "FILTER NOT EXISTS {?address s:geo ?geo}"
 				+  "}";*/ 
 		String sOrgConstructQuery = "PREFIX s: <http://schema.org/> "
 				+ "CONSTRUCT {?address ?p ?o}"
 				+ "WHERE "
 				+ "{"
 					+ "?address a s:PostalAddress ;"
 					+ "			?p ?o . "
 //					+ "FILTER NOT EXISTS {?address s:geo ?geo}"
 				+  "}"; 
 		/*String sOrgQuery = "PREFIX s: <http://schema.org/> "
 				+ "SELECT DISTINCT * "
 				+ "WHERE "
 				+ "{"
 					+ "{?address a s:PostalAddress . } "
 					+ "UNION { ?address s:streetAddress ?street . } "
 					+ "UNION { ?address s:addressRegion ?region . } "
 					+ "UNION { ?address s:addressLocality ?locality . } "
 					+ "UNION { ?address s:postalCode ?postal . } "
 					+ "UNION { ?address s:addressCountry ?country . } "
 				+ " }";*/
 
 		logger.debug("Geocoder init");
 		
 		int total = 0;
 		int ngc = 0;
 		int count = 0;
 		int failed = 0;
 		try {
			MyTupleQueryResultIf countres = sAddresses.executeSelectQueryAsTuples(countQuery);
 			//MyTupleQueryResult countnotGC = sAddresses.executeSelectQueryAsTuples(notGCcountQuery);
 			total = Integer.parseInt(countres.next().getValue("count").stringValue());
 			//ngc = Integer.parseInt(countnotGC.next().getValue("count").stringValue());
 			ctx.sendMessage(MessageType.INFO, "Found " + total + " addresses"/* + ngc + " not geocoded yet."*/);
 		} catch (InvalidQueryException e1) {
 			logger.error(e1.getLocalizedMessage());
 		} catch (NumberFormatException e) {
 			logger.error(e.getLocalizedMessage());
 		} catch (QueryEvaluationException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
 		try {
 			
 			//Schema.org addresses
 			logger.debug("Executing Schema.org query: " + sOrgConstructQuery);
 			//MyTupleQueryResult res = sAddresses.executeSelectQueryAsTuples(sOrgQuery);
 			Graph resGraph = sAddresses.executeConstructQuery(sOrgConstructQuery);
 			
 			logger.debug("Starting geocoding.");
 			
 			URI[] propURIs = new URI [] {streetAddressURI, addressLocalityURI, addressRegionURI, postalCodeURI, addressCountryURI};
 			Iterator<Statement> it = resGraph.match(null, RDF.TYPE, postalAddressURI);
 			
 			int cachedToday = countTodaysCacheFiles(ctx);
 			int toCache = (config.limit - cachedToday);
 
 			long lastDownload = 0;
 			while (it.hasNext() && !ctx.canceled() && geocodes <= toCache)
 			{
 				count++;
 				Resource currentAddressURI = it.next().getSubject();
 				StringBuilder addressToGeoCode = new StringBuilder();
 				
 				for (URI currentPropURI : propURIs)
 				{
 					Iterator<Statement> it1 = resGraph.match(currentAddressURI, currentPropURI, null); 
 					
 					if (it1.hasNext())
 					{
 						Value currentValue = it1.next().getObject();
 						if (currentValue != null)
 						{
 							//logger.trace("Currently " + currentBinding);
 							String currentValueString = currentValue.stringValue();
 							//logger.trace("Value " + currentValueString);
 							addressToGeoCode.append(currentValueString);
 							addressToGeoCode.append(" ");
 						}
 					}
 				}				
 				
 				String address = addressToGeoCode.toString();
 				logger.debug("Address to geocode (" + count + "/" + total + "): " + address);
 								
 				//CACHE
 				String file = address.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-");
 				File hPath = ctx.getGlobalDirectory();
 				File hFile = new File(hPath, file);
 				GeocoderRequest geocoderRequest = null;
 				GeocodeResponse geocoderResponse = null;
 				
 				if (!hFile.exists())
 				{
 					long curTS = date.getTime();
 					if (lastDownload + config.interval > curTS)
 					{
 						logger.debug("Sleeping: " + (lastDownload + config.interval - curTS));
 						try {
 							Thread.sleep(lastDownload + config.interval - curTS);
 						} catch (InterruptedException e) {
 							logger.info("Interrupted while sleeping");
 						}
 					}
 					
 					geocoderRequest = new GeocoderRequestBuilder().setAddress(address).setLanguage("en").getGeocoderRequest();
 					geocoderResponse = geocoder.geocode(geocoderRequest);
 					lastDownload = date.getTime();
 					
 					geocodes++;
 					GeocoderStatus s = geocoderResponse.getStatus();
 					if (s == GeocoderStatus.OK) {
 						logger.debug("Googled (" + geocodes + "): " + address);
 						
 						try {
 							BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
 							fw.append(geocoderResponse.toString());
 							fw.close();
 						} catch (Exception e) {
 							logger.error(e.getLocalizedMessage());
 						}
 						//CACHED
 					}
 					else if (s == GeocoderStatus.ZERO_RESULTS) {
 						logger.warn("Zero results for: " + address);
 						continue;
 					}
 					else {
 						logger.error("Status: " + geocoderResponse.getStatus() + " " + address);
 						break;
 					}
 				}
 				else {
 					cacheHits++;
 					logger.debug("From cache (" + cacheHits + "): " + address);
 				}
 				
 				//READ FROM FILE - NOW IT EXISTS
 				String cachedFile = null;
 				try {
 					cachedFile = FileUtils.readFileToString(hFile);
 				} catch (IOException e) {
 					logger.error(e.getLocalizedMessage());
 				}
 				
 				int indexOfLocation = cachedFile.indexOf("location=LatLng") + 16;
 				String location = cachedFile.substring(indexOfLocation, cachedFile.indexOf("}", indexOfLocation)); 
 				
 				BigDecimal latitude = new BigDecimal(location.substring(location.indexOf("lat=")+4, location.indexOf(",")));
 				BigDecimal longitude = new BigDecimal(location.substring(location.indexOf("lng=")+4));
 				
 				logger.debug("Located: " + address + " Latitude: " + latitude + " Longitude: " + longitude);
 				
 				String uri = currentAddressURI.stringValue();
 				URI addressURI = outGeo.createURI(uri);
 				URI coordURI = outGeo.createURI(uri+"/geocoordinates/google");
 				
 				outGeo.addTriple(addressURI, geoURI , coordURI);
 				outGeo.addTriple(coordURI, RDF.TYPE, geocoordsURI);
 				outGeo.addTriple(coordURI, longURI, outGeo.createLiteral(longitude.toString()/*, xsdDecimal*/));
 				outGeo.addTriple(coordURI, latURI, outGeo.createLiteral(latitude.toString()/*, xsdDecimal*/));
 				outGeo.addTriple(coordURI, dcsource, googleURI);
 			}
 			if (ctx.canceled()) logger.info("Cancelled");
 			
 		} catch (InvalidQueryException e) {
 			logger.error(e.getLocalizedMessage());
 		}
 
        	logger.info("Geocoding done.");
 
 		java.util.Date date2 = new java.util.Date();
 		long end = date2.getTime();
 
 		ctx.sendMessage(MessageType.INFO, "Geocoded " + count + ": Googled: "+ geocodes +" From cache: " + cacheHits + " in " + (end-start) + "ms, failed attempts: " + failed);
 
 	}
 
 	@Override
 	public void cleanUp() {	}
 
 }
