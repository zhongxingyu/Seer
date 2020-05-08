 package org.bioinfo.infrared.ws.server.rest;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.zip.ZipOutputStream;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.commons.Config;
 import org.bioinfo.commons.log.Logger;
 import org.bioinfo.commons.utils.ListUtils;
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.infrared.common.DBConnector;
 import org.bioinfo.infrared.core.common.Feature;
 import org.bioinfo.infrared.core.common.FeatureList;
 import org.bioinfo.infrared.core.variation.TranscriptConsequenceType;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
 @Path("/{version}")
 @Produces("text/plain")
 public class GenericRestWSServer implements IWSServer {
 
 	protected DBConnector infraredDBConnector;
 	protected Config config;
 
 	// application parameters
 	protected String version;
 	protected String species;
 	protected UriInfo uriInfo;
 
 	// common parameters
 	protected String resultSeparator;
 	protected String querySeparator;
 
 	// output format file type: null or txt or text, xml, excel
 	protected String fileFormat;
 	
 	// file name without extension which server will give back when file format is !null
 	private String filename;
 
 	// output content format: txt or text, json, jsonp, xml, das
 	protected String contentFormat;
 
 	// in file output produces a zip file, in text outputs generates a gzipped output
 	protected String outputCompress;
 
 	// only in text format
 	protected String outputRowNames;
 	protected String outputHeader;
 
 	protected String user;
 	protected String password;
 
 	protected Type listType;
 
 	private MediaType mediaType;
 	private Gson gson; 
 	private GsonBuilder gsonBuilder;
 	protected Logger logger;
 
 
 	private static final String NEW_LINE = "newline";
 	private static final String TAB = "tab";
 
 	public GenericRestWSServer(@PathParam("version") String version) {
 		this.version = version;
 	}
 
 	public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
 		if(version != null && species != null) {
 			init(version, species, uriInfo);
 		}
 	}
 	
 	//This method are for nice printing help URL
 	protected List<String> getPathsNicePrint(){
 		return new ArrayList<String>();
 	}
 	
 	protected List<String> getExamplesNicePrint(){
 		return new ArrayList<String>();
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see org.bioinfo.infrared.ws.server.rest.IWSServer#stats()
 	 */
 	@Override
 	public String stats() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.bioinfo.infrared.ws.server.rest.IWSServer#isValidSpecies()
 	 */
 	@Override
 	public boolean isValidSpecies() {
 		return true;
 	}
 
 	@GET
 	@Path("/help")
 	public String help() {
 		StringBuilder br = new StringBuilder();
 		br.append(this.uriInfo.getPath().replace("help", ""));
 		br.append(System.getProperty("line.separator"));
 		br.append(System.getProperty("line.separator"));
 		br.append("Path:\n");
 		if (getPathsNicePrint().size()==0){
 			br.append("\tNo information avalaible\n");
 		}
 		else{
 			for (String path : getPathsNicePrint()) {
 				br.append("\t"+path+"\n");
 			}
 		}
 		br.append(System.getProperty("line.separator"));
 		br.append("Examples:\n");
 		if (getExamplesNicePrint().size()==0){
 			br.append("\tNo examples avalaible\n");
 		}
 		else{
 			for (String path : getExamplesNicePrint()) {
 				br.append("\t"+path+"\n");
 			}
 		}
 		br.append("");
 		return br.toString();
 	}
 
 	@GET
 	@Path("/species")
 	public String getSpecies() {
 		StringBuilder stringBuilder = new StringBuilder();
 		stringBuilder.append("#short").append("\t").append("common").append("\t").append("scientific").append("\n");
 		stringBuilder.append("hsa").append("\t").append("human").append("\t").append("Homo sapiens").append("\n");
 		stringBuilder.append("mus").append("\t").append("mouse").append("\t").append("Mus musculus").append("\n");
 		stringBuilder.append("rno").append("\t").append("rat").append("\t").append("Rattus norvegicus");
 		return stringBuilder.toString();
 	}
 
 	protected void init(String version, String species, UriInfo uriInfo) throws VersionException, IOException {
 		this.version = version;
 		this.species = species;
 		this.uriInfo = uriInfo;
 
 
 		// load properties file
 		ResourceBundle databaseConfig = ResourceBundle.getBundle("org.bioinfo.infrared.ws.application");
 		config = new Config(databaseConfig);
 
 		mediaType = MediaType.valueOf("text/plain");
 		gson = new GsonBuilder().serializeNulls().create();
 		gsonBuilder = new GsonBuilder();
 		logger = new Logger();
 		logger.setLevel(Logger.DEBUG_LEVEL);
 
 		// this code MUST be run before the checking 
 		parseCommonQueryParameters(uriInfo.getQueryParameters());
 
 		// checking all parameters are OK
 		if(isValidSpecies() && checkVersion(version) && uriInfo != null) {
 			// connect to database
 			connect();
 		}
 	}
 
 	private void parseCommonQueryParameters(MultivaluedMap<String, String> multivaluedMap) {
 		// default result separator is '//'
 		// resultSeparator = (multivaluedMap.get("result_separator") != null) ? multivaluedMap.get("result_separator").get(0) : "//";
 		if(multivaluedMap.get("result_separator") != null) {
 			if(multivaluedMap.get("result_separator").get(0).equalsIgnoreCase(NEW_LINE)) {
 				resultSeparator = "\n";				
 			}else {
 				if(multivaluedMap.get("result_separator").get(0).equalsIgnoreCase(TAB)) {
 					resultSeparator = "\t";	
 				}else {
 					resultSeparator = multivaluedMap.get("result_separator").get(0);
 				}
 			}
 		}else {
 			resultSeparator = "//";
 		}
 
 		// default query separator is '\n'
 		// querySeparator = (multivaluedMap.get("query_separator") != null) ? multivaluedMap.get("query_separator").get(0) : "\n";
 		if(multivaluedMap.get("query_separator") != null) {
 			if(multivaluedMap.get("query_separator").get(0).equalsIgnoreCase(NEW_LINE)) {
 				querySeparator = "\n";				
 			}else {
 				if(multivaluedMap.get("query_separator").get(0).equalsIgnoreCase(TAB)) {
 					querySeparator = "\t";	
 				}else {
 					querySeparator = multivaluedMap.get("query_separator").get(0);
 				}
 			}
 		}else {
 			querySeparator = "\n";
 		}
 
 		fileFormat = (multivaluedMap.get("fileformat") != null) ? multivaluedMap.get("fileformat").get(0) : "";
 		contentFormat = (multivaluedMap.get("contentformat") != null) ? multivaluedMap.get("contentformat").get(0) : "txt";
 		
 		filename = (multivaluedMap.get("filename") != null) ? multivaluedMap.get("filename").get(0) : "result";
 		
 		
 		outputRowNames = (multivaluedMap.get("outputrownames") != null) ? multivaluedMap.get("outputrownames").get(0) : "false";
 		outputHeader = (multivaluedMap.get("outputheader") != null) ? multivaluedMap.get("outputheader").get(0) : "false";
 		outputCompress = (multivaluedMap.get("outputcompress") != null) ? multivaluedMap.get("outputcompress").get(0) : "false";
 		user = (multivaluedMap.get("user") != null) ? multivaluedMap.get("user").get(0) : "anonymous";
 		password = (multivaluedMap.get("password") != null) ? multivaluedMap.get("password").get(0) : "";
 	}
 
 	private boolean checkVersion(String version) throws VersionException {
 		if(StringUtils.toList(config.getProperty("PUBLIC.VERSION"), ",").contains(version)) {
 			return true;
 		}else {
 			if(StringUtils.toList(config.getProperty("PRIVATE.VERSION"), ",").contains(version)) {
 				if(user != null && user.equals(config.getProperty("PRIVATE.VERSION.USER")) && password != null && password.equals(config.getProperty("PRIVATE.VERSION.PASSWORD"))) {
 					return true;
 				}else {
 					throw new VersionException("No user or password valid");
 				}		
 			}else {
 				throw new VersionException("Version '"+version+"' not valid");
 			}
 		}
 	}
 
 	private void connect() throws IOException {
 		logger.debug("in connect");
 		infraredDBConnector = new DBConnector(species, config.getProperty("INFRARED.HOST"), config.getProperty("INFRARED.PORT", "3306"), config.getProperty("INFRARED."+species.toUpperCase()+".DATABASE"), config.getProperty("INFRARED.USER"), config.getProperty("INFRARED.PASSWORD"));
 	}
 
 	protected void disconnect() throws SQLException {
 		if(infraredDBConnector != null) {
 			infraredDBConnector.disconnect();
 		}
 	}
 
 
 	protected <E> Response generateResponseFromListList(List<List<E>> features, Type listType) throws IOException {
 		return null;
 	}
 
 	//protected <E extends Feature> Response generateResponseFromFeature(String queryString, E feature, Type type) throws IOException {
 	protected <E extends Feature> Response generateResponseFromFeature(  E feature, Type type) throws IOException {
 		String queryString = "";
 		
 		logger.debug("[generateResponseFromFeature]");
 		String response = "";
 		if (contentFormat != null) {
 			if(contentFormat.equalsIgnoreCase("txt") || contentFormat.equalsIgnoreCase("text") || contentFormat.equalsIgnoreCase("jsontext")) {
 				response = createStringResultFromFeature(queryString, feature);
 
 				if(contentFormat.equalsIgnoreCase("jsontext")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJsonText(response);
 				}else {
 					mediaType = MediaType.valueOf("text/plain");
 				}
 			}
 			
 			if((contentFormat.equalsIgnoreCase("json") || contentFormat.equalsIgnoreCase("jsonp"))) {
 				mediaType =  MediaType.valueOf("application/json");
 				if(feature != null) {
 					if(type != null) {
 						logger.debug("\tCreating JSON object");
 						response = gson.toJson(feature, listType);
 					}else {
 						logger.error("[GenericRestWSServer] GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 
 				if(contentFormat.equals("jsonp")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJson(response);
 				}
 			}
 
 			if(contentFormat.equalsIgnoreCase("xml") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = new StringBuilder().append(feature.toString()).append(resultSeparator).toString().trim();
 			}
 
 			if(contentFormat.equalsIgnoreCase("das") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = new StringBuilder().append(feature.toString()).append(resultSeparator).toString().trim();
 			}
 		}
 //		if(outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
 //			response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" " , "");
 //		}
 
 		return createResponse(response);
 	}
 	
 	protected <E extends Feature> Response generateResponseFromList(String queryString, List<E> features, Type listType) throws IOException {
 		String response = "";
 		if (contentFormat != null) {
 			if(contentFormat.equalsIgnoreCase("txt") || contentFormat.equalsIgnoreCase("text") || contentFormat.equalsIgnoreCase("jsontext")) {
 				response = createStringResultFromList(queryString, features);
 
 				if(contentFormat.equalsIgnoreCase("jsontext")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJsonText(response);
 				}else {
 					mediaType = MediaType.valueOf("text/plain");
 				}
 			}
 
 			if((contentFormat.equalsIgnoreCase("json") || contentFormat.equalsIgnoreCase("jsonp"))) {
 				mediaType =  MediaType.valueOf("application/json");
 				if(features != null && features.size() > 0) {
 					if(listType != null) {
 						logger.debug("\tCreating JSON object");
 						response = gson.toJson(features, listType);
 					}else {
 						logger.error("[GenericRestWSServer] GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 
 				if(contentFormat.equals("jsonp")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJson(response);
 				}
 			}
 
 			if(contentFormat.equalsIgnoreCase("xml") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = ListUtils.toString(features, resultSeparator);
 			}
 
 			if(contentFormat.equalsIgnoreCase("das") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = ListUtils.toString(features, resultSeparator);
 			}
 		}
 //		if(outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
 //			response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" " , "");
 //		}
 
 		return createResponse(response);
 	}
 	
 	
 	
 	protected <E extends Feature> Response generateResponseFromFeatureList(FeatureList<E> features, Type listType) throws IOException {
 		return generateResponseFromFeatureList("", features, listType);
 	}
 		
 	protected <E extends Feature> Response generateResponseFromFeatureList(String queryString, FeatureList<E> features, Type listType) throws IOException {
 		String response = "";
 		if (contentFormat != null) {
 			if(contentFormat.equalsIgnoreCase("txt") || contentFormat.equalsIgnoreCase("text") || contentFormat.equalsIgnoreCase("jsontext")) {
 				response = createStringResultFromFeatureList(queryString, features);
 
 				if(contentFormat.equalsIgnoreCase("jsontext")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJsonText(response);
 				}else {
 					mediaType = MediaType.valueOf("text/plain");
 				}
 			}
 
 			if((contentFormat.equalsIgnoreCase("json") || contentFormat.equalsIgnoreCase("jsonp"))) {
 				mediaType =  MediaType.valueOf("application/json");
 				if(features != null && features.size() > 0) {
 					if(listType != null) {
 						logger.debug("\tCreating JSON object");
 						response = gson.toJson(features, listType);
 					}else {
 						logger.error("[GenericRestWSServer] GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 
 				if(contentFormat.equals("jsonp")) {
 					mediaType =  MediaType.valueOf("text/javascript");
 					response = convertToJson(response);
 				}
 			}
 
 			if(contentFormat.equalsIgnoreCase("xml") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = ListUtils.toString(features, resultSeparator);
 			}
 
 			if(contentFormat.equalsIgnoreCase("das") ) {
 				mediaType =  MediaType.valueOf("text/xml");
 				response = ListUtils.toString(features, resultSeparator);
 			}
 		}
 		return createResponse(response);
 	}
 
 	protected <E extends Feature> Response generateResponseFromListFeatureList(String queryString, List<FeatureList<E>> features, Type listType) throws IOException {
 		String response = "";
 		if (contentFormat != null) {
 			if(contentFormat.equalsIgnoreCase("txt") || contentFormat.equalsIgnoreCase("text") || contentFormat.equalsIgnoreCase("jsontext")) {
 				response = createStringResultFromListFeatureList(queryString, features);
 				if(contentFormat.equalsIgnoreCase("jsontext")) {
 					mediaType = MediaType.valueOf("text/javascript");
 					response = convertToJsonText(response);
 				}else {
 					mediaType = MediaType.TEXT_PLAIN_TYPE;
 				}
 			}
 
 			if((contentFormat.equalsIgnoreCase("json") || contentFormat.equalsIgnoreCase("jsonp"))) {
 				mediaType = MediaType.APPLICATION_JSON_TYPE;
 				if(features != null && features.size() > 0) {
 					if(listType != null) {
 						response = gson.toJson(features, listType);
 					}else {
 						logger.error("[GenericRestWSServer] GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 
 				if(contentFormat.equals("jsonp")) {
 					mediaType = MediaType.valueOf("text/javascript");
 					response = convertToJson(response);
 				}
 			}
 
 			if(contentFormat.equalsIgnoreCase("xml") ) {
 				mediaType = MediaType.TEXT_XML_TYPE;
 				response = ListUtils.toString(features, resultSeparator);
 			}
 
 			if(contentFormat.equalsIgnoreCase("das") ) {
 				mediaType = MediaType.TEXT_XML_TYPE;
 				response = ListUtils.toString(features, resultSeparator);
 			}
 		}
 
 		return createResponse(response);
 	}
 
 	private Response createResponse(String response) throws IOException {
 		//Logs
 		logger.debug("Query Params------------ ");
 		logger.debug("\t\t - FileFormat: " + fileFormat);
 		logger.debug("\t\t - ContentFormat: " + contentFormat);
 		logger.debug("\t\t - Compress: " + outputCompress);
 		logger.debug("\t\t -------------------------------");
 		logger.debug("\t\t - Inferred media type: " + mediaType.toString());
 		
 		if (response.length()>99){
 			logger.debug("\t\t -Response: " + response.substring(0,100) + ".....");
 		}
 		else{
 			logger.debug("\t\t -Response: " + response);
 		}
 		//End of logs
 		
 		if(fileFormat == null || fileFormat.equalsIgnoreCase("")) {
 			if(outputCompress != null && outputCompress.equalsIgnoreCase("true") && !contentFormat.equalsIgnoreCase("jsonp")&& !contentFormat.equalsIgnoreCase("jsontext")) {
 				response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" " , "");
 			}
 		}else {
 			
 			mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
 			logger.debug("\t\t - Creating byte stream ");
 			
 			if(outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
 				
 				
 				OutputStream bos = new ByteArrayOutputStream();
 				bos.write(response.getBytes());
 				
 				ZipOutputStream zipstream = new ZipOutputStream(bos);
 				zipstream.setLevel(9);
 		
 				logger.debug("\t\t - zipping.... ");
 				logger.debug("\t\tFinal media Type: " + mediaType.toString());
 				
 				return Response.ok(zipstream, mediaType).header("content-disposition","attachment; filename = "+ filename + ".zip").build();
 
 					
 			}else {
 				if(fileFormat.equalsIgnoreCase("xml")) {
 					//mediaType =  MediaType.valueOf("application/xml");	
 				}
 
 				if(fileFormat.equalsIgnoreCase("excel")) {
 					//mediaType =  MediaType.valueOf("application/vnd.ms-excel");
 				}
 				if(fileFormat.equalsIgnoreCase("txt") || fileFormat.equalsIgnoreCase("text")) {
 					logger.debug("\t\t - text File ");
 					
 					byte[] streamResponse = response.getBytes();
 					return Response.ok(streamResponse, mediaType).header("content-disposition","attachment; filename = "+ filename + ".txt").build();
 				}
 				
 			}
 		}
 		logger.debug("");
 		logger.debug("\t\tFinal media Type: " + mediaType.toString());
 		logger.debug(" ------------ ");
 		return Response.ok(response, mediaType).build();
 	}
 
 	
 //	private byte[] zipStringToBytes( String input  ) throws IOException
 //	  {
 //	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
 //	    BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
 //	    bufos.write( input.getBytes() );
 //	    bufos.close();
 //	    byte[] retval= bos.toByteArray();
 //	    logger.debug("\t\t - bytes " + input.getBytes());
 //	    logger.debug("\t\t - zipped " + retval);
 //	    bos.close();
 //	    return retval;
 //	  }
 	
 	private <E extends Feature> String createStringResultFromFeature(String queryString, E feature) throws IOException {
 		StringBuilder stringBuilder = new StringBuilder();
 		if(outputRowNames != null && outputRowNames.equalsIgnoreCase("true")) 
 		{
 				if(feature != null) {
 					stringBuilder.append(queryString).append("\t").append(feature.toString()).append(querySeparator);
 				}else {
 					stringBuilder.append(queryString).append("\t").append("not found").append(querySeparator);
 				}
 		}
 		else{
 			stringBuilder.append(feature.toString()).append(querySeparator);
 		}
 		return stringBuilder.toString().trim();
 	}
 	
 	
 	private <E extends Feature> String createStringResultFromList(String queryString, List<E> features) throws IOException {
 		if(outputRowNames != null && outputRowNames.equalsIgnoreCase("true")) {
 			StringBuilder stringBuilder = new StringBuilder();
 			String[] ids = queryString.split(",");
 			if(ids.length != features.size()) {
 				throw new IOException("IDs length and features size do not match");
 			}
 			for(int i=0; i<features.size(); i++) {
 				if(features.get(i) != null) {
 					stringBuilder.append(ids[i]).append("\t").append(features.get(i).toString()).append(querySeparator);
 				}else {
 					stringBuilder.append(ids[i]).append("\t").append("not found").append(querySeparator);
 				}
 			}
 			return stringBuilder.toString().trim();
 		}else {
 			return ListUtils.toString(features, querySeparator);
 		}
 	}
 	
 	private <E extends Feature> String createStringResultFromFeatureList(String queryString, FeatureList<E> features) throws IOException {
 		if(outputRowNames != null && outputRowNames.equalsIgnoreCase("true")) {
 			StringBuilder stringBuilder = new StringBuilder();
 			String[] ids = queryString.split(",");
 			if(ids.length != features.size()) {
 				throw new IOException("IDs length and features size do not match");
 			}
 			for(int i=0; i<features.size(); i++) {
 				if(features.get(i) != null) {
 					stringBuilder.append(ids[i]).append("\t").append(features.get(i).toString()).append(querySeparator);
 				}else {
 					stringBuilder.append(ids[i]).append("\t").append("not found").append(querySeparator);
 				}
 			}
 			return stringBuilder.toString().trim();
 		}else {
 			return ListUtils.toString(features, querySeparator);
 		}
 	}
 
 	private <E extends Feature> String createStringResultFromListFeatureList(String queryString, List<FeatureList<E>> features) throws IOException {
 		StringBuilder stringBuilder = new StringBuilder();
 		String[] ids = queryString.split(",");
 		if(ids == null || features == null || ids.length != features.size()) {
 			throw new IOException("IDs length and features size do not match");
 		}
 
 		if(outputRowNames != null && outputRowNames.equalsIgnoreCase("true")) {
 			for(int i=0; i<features.size(); i++) {
 				if(features.get(i) != null) {
 					stringBuilder.append(ids[i]).append("\t").append(ListUtils.toString(features.get(i), resultSeparator)).append(querySeparator);
 				}else {
 					stringBuilder.append(ids[i]).append(querySeparator);
 				}
 			}
 			return stringBuilder.toString().trim();
 		}else {
 			for(int i=0; i<features.size(); i++) {
 				if(features.get(i) != null) {
 					stringBuilder.append(ListUtils.toString(features.get(i), resultSeparator)).append(querySeparator);
 				}else {
 					stringBuilder.append("not found").append(querySeparator);
 				}
 			}
 			return stringBuilder.toString().trim();
 		}
 	}
 
 
 	protected Response generateErrorResponse(String errorMessage) {
 		return Response.ok("An error occurred: "+errorMessage, MediaType.valueOf("text/plain")).build();
 	}
 
 
 	private String convertToJsonText(String response) {
 		String jsonpQueryParam = (uriInfo.getQueryParameters().get("callbackParam") != null) ? uriInfo.getQueryParameters().get("callbackParam").get(0) : "callbackParam";
 		response = "var " + jsonpQueryParam+ " = \"" + response +"\"";
 		return response;
 	}
 
 	private String convertToJson(String response) {
 		String jsonpQueryParam = (uriInfo.getQueryParameters().get("callbackParam") != null) ? uriInfo.getQueryParameters().get("callbackParam").get(0) : "callbackParam";	
 		response = "var " + jsonpQueryParam+ " = (" + response +")";
 		return response;
 	}
 
 	
 	
 	
 	@Deprecated
 	protected <E extends Feature> String createResultString(List<String> ids, FeatureList<E> features) {
 		if(contentFormat.equals("txt")) {
 			StringBuilder result = new StringBuilder();
 			for(int i=0; i<ids.size(); i++) {
 				if(features.get(i) != null) {
 					result.append(ids.get(i)).append(":\t").append(features.get(i).toString()).append(querySeparator);
 				}else {
 					result.append(ids.get(i)).append(":\t").append("not found").append(querySeparator);
 				}
 			}
 			return result.toString().trim();	
 		}else {
 			if(contentFormat.equals("json")) {
 				return new Gson().toJson(features);
 			}
 		}
 		return "output format '"+contentFormat+"' not valid";
 	}
 
 	@Deprecated
 	protected <E extends Feature> String createResultString(List<String> ids, List<FeatureList<E>> features) {
 		if(contentFormat.equals("txt")) {
 			StringBuilder result = new StringBuilder();
 			for(int i=0; i<ids.size(); i++) {
 				if(features.get(i) != null && features.get(i).size() > 0) {
 					for(E feature: features.get(i)) {
 						if(feature != null) {
 							result.append(ids.get(i)).append(":\t").append(feature.toString()).append(querySeparator);
 						}else {
 							result.append(ids.get(i)).append(":\t").append("not found").append(querySeparator);
 						}
 					}
 				}else {
 					result.append(ids.get(i)).append(":\t").append("not found").append(querySeparator);
 				}
 			}
 			return result.toString().trim();
 		}
 		return "output format '"+contentFormat+"' not valid";
 	}
 
 	
 	
 /*	@Deprecated
 	protected <E extends Feature> Response generateResponseFromFeatureList(FeatureList<E> features, Type listType) throws IOException {
 		mediaType = MediaType.valueOf("text/plain");
 		String response = new String();
 		Gson gson = new GsonBuilder().serializeNulls().create();
 
 		if (outputFormat != null)
 		{
 			if(outputFormat.equals("txt")||outputFormat.equals("jsontext")) {
 				mediaType = MediaType.valueOf("text/plain");
 				response = ListUtils.toString(features, resultSeparator);
 			}
 
 			if((outputFormat.equals("json")||outputFormat.equals("jsonp"))) {
 				mediaType =  MediaType.valueOf("application/json");
 
 				if(features != null && features.size() > 0) {
 					if(listType != null) {
 						logger.info("   Creating JSON object");
 						response = gson.toJson(features, listType);
 					}else 
 					{
 						logger.error("[GenericRestWSServer] GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 			}
 
 			if(outputFormat.equals("jsonp")) {
 				mediaType =  MediaType.valueOf("text/javascript");
 				response = convertToJson(response);
 			}
 
 
 			if(outputFormat.equals("jsontext")) {
 				mediaType =  MediaType.valueOf("text/javascript");
 				response = convertToJsonText(response);
 			}
 
 
 			if(outputFormat.equals("xml")) {
 				mediaType =  MediaType.valueOf("text/xml");
 			}
 		}
 
 		if(outputCompress!= null) {
 			mediaType =  MediaType.valueOf("application/zip");
 			String zippedResponse = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" " , "");
 
 			logger.info("[GenericRestWSServer] entity.length(): "+response.length());
 			logger.info("[GenericRestWSServer] zipEntity.length(): "+zippedResponse.length());
 
 			return Response.ok(zippedResponse, mediaType).build();
 		}
 		else 
 		{
 			return Response.ok(response, mediaType).build();
 		}
 	}*/
 	
 	@Deprecated
 	protected Response generateResponse(String entity, String outputFormat, boolean compress) throws IOException {
 		MediaType mediaType = MediaType.valueOf("text/plain");
 		if(outputFormat != null && outputFormat.equals("json")) {
 			mediaType =  MediaType.valueOf("application/json");
 			Gson gson = new Gson();
 			System.out.println("Entro: "+entity);
 			entity = gson.toJson(entity);
 			System.out.println(entity);
 		}
 		if(outputFormat != null && outputFormat.equals("xml")) {
 			mediaType =  MediaType.valueOf("text/xml");
 		}
 		if(outputCompress!= null) {
 			mediaType =  MediaType.valueOf("application/zip");
 			return Response.ok(StringUtils.zipToBytes(entity), mediaType).build();
 		}else {
 			return Response.ok(entity, mediaType).build();
 		}
 	}
 
 	@Deprecated
 	protected <E extends Object> Response generateResponse(List<E> entityList, String outputFormat, boolean compress) throws IOException {
 		MediaType mediaType = MediaType.valueOf("text/plain");
 		String entity = "";
 		String zipEntity = "";
 		if(outputFormat != null && outputFormat.equals("json")) {
 			mediaType =  MediaType.valueOf("application/json");
 			Gson gson = new Gson();
 			System.out.println("Entro: "+entityList);
 			entity = gson.toJson(entityList, this.listType);
 			System.out.println(entity);
 			zipEntity = Arrays.toString(StringUtils.gzipToBytes(entity)).replace(" " , "");
 		}
 		if(outputFormat != null && outputFormat.equals("xml")) {
 			mediaType =  MediaType.valueOf("text/xml");
 		}
 		if(compress) {
 			mediaType =  MediaType.valueOf("application/zip");
 			return Response.ok(zipEntity, mediaType).build();
 		}else {
 			return Response.ok(entity, mediaType).build();
 		}
 	}
 
 	@Deprecated
 	protected String createResultStringByTranscriptConsequenceType(List<String> ids, List<List<TranscriptConsequenceType>> features) {
 		if(contentFormat.equals("txt")) {
 			StringBuilder result = new StringBuilder();
 			for(int i=0; i<ids.size(); i++) {
 				if(features.get(i) != null && features.get(i).size() > 0) {
 					for(TranscriptConsequenceType feature: features.get(i)) {
 						if(feature != null) {
 							result.append(ids.get(i)).append(":\t").append(feature.toString()).append(querySeparator);
 						}else {
 							result.append(ids.get(i)).append(":\t").append("not found").append(querySeparator);
 						}
 					}
 				}else {
 					result.append(ids.get(i)).append(":\t").append("not found").append(querySeparator);
 				}
 			}
 			return result.toString().trim();
 		}else {
 			if(contentFormat.equals("json")) {
 				return new Gson().toJson(features);
 			}
 		}
 		return "output format '"+contentFormat+"' not valid";
 	}
 
 	@Deprecated
 	protected <E extends Feature> Response generateResponseFromListFeatureList(List<FeatureList<E>> features, Type listType) throws IOException {
 		MediaType mediaType = MediaType.valueOf("text/plain");
 		String response = new String();
 		Gson gson = new GsonBuilder().serializeNulls().create();
 		
 		logger.info("DEPRECATED:: generateResponseFromListFeatureList");
 		
 		if(contentFormat != null)
 		{
 			if(contentFormat.equals("txt")||contentFormat.equals("jsontext")) {
 				mediaType = MediaType.valueOf("text/plain");
 				// cada ID en una line, cada valor en la misma linea separado por '//'
 				//			entity = ListUtils.toString(features, querySeparator);
 
 				StringBuilder result= new StringBuilder();
 				for(FeatureList<E> featureList: features) {
 					if(featureList != null) {
 						result.append(ListUtils.toString(featureList, resultSeparator));
 					}else {
 						result.append("null");					
 					}
 					result.append(querySeparator);
 				}
 				response = result.toString().trim();
 			}
 
 			if(contentFormat.equals("json")||contentFormat.equals("jsonp")) {
 				mediaType =  MediaType.valueOf("application/json");
 				if(features != null && features.size() > 0) {
 
 					if(listType != null) {
 						logger.info("Creating JSON object");
 						response = gson.toJson(features, listType);
 
 					}else {
 						System.err.println("GenericRestWSServer: TypeToken from Gson equals null");
 					}
 				}
 			}
 
 			if(contentFormat.equals("jsonp")) {
 				mediaType =  MediaType.valueOf("text/javascript");
 				response = convertToJson(response);
 			}
 
 			if(contentFormat.equals("jsontext")) {
 				mediaType =  MediaType.valueOf("text/javascript");
 				response = convertToJsonText(response);
 			}
 
 			if(contentFormat.equals("xml")) {
 				mediaType =  MediaType.valueOf("text/xml");
 			}
 		}
 
 		if(outputCompress != null) {
 			mediaType =  MediaType.valueOf("application/zip");
 			String zippedResponse = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" " , "");
 
 			logger.info("[GenericRestWSServer] entity.length(): "+response.length());
 			logger.info("[GenericRestWSServer] zipEntity.length(): "+zippedResponse.length());
 
 			return Response.ok(zippedResponse, mediaType).build();
 		}
 		else 
 		{
 			return Response.ok(response, mediaType).build();
 		}
 	}
 
 	@Deprecated
 	protected <E> Response generateResponseFromList(List<E> features) throws IOException {//, Type listType
 		
 		logger.info("DEPRECATED:: generateResponseFromList");
 		MediaType mediaType = MediaType.valueOf("text/plain");
 		String entity = "";
 		String zipEntity = "";
 		Gson gson = new GsonBuilder().serializeNulls().create();
 		//Gson gson = new Gson();
 		if(contentFormat != null && contentFormat.equals("txt")) {
 			mediaType = MediaType.valueOf("text/plain");
 			entity = ListUtils.toString(features, querySeparator);
 
 		}
 		if(contentFormat != null && contentFormat.equals("json")) {
 			mediaType =  MediaType.valueOf("application/json");
 			if(features != null && features.size() > 0 /*&& features.get(0) != null*/) {
 
 				if(listType != null && features != null ) {
 					System.out.println("Entro3");
 					System.out.println("Creating JSON object...");
 					entity = gson.toJson(features);//, listType
 					System.err.println("done!");
 					System.err.println("Entity json: "+entity);
 					zipEntity = Arrays.toString(StringUtils.gzipToBytes(entity)).replace(" " , "");
 					System.err.println("zipEntryBytes: "+StringUtils.gzipToBytes(entity));
 					System.err.println("zipEntry: "+zipEntity);
 					System.out.println("entity.length(): "+entity.length());
 					System.out.println("zipEntity.length(): "+zipEntity.length());
 
 				}else {
 					System.err.println("GenericRestWSServer: TypeToken from Gson equals null");
 				}
 			}
 		}
 
 		if(contentFormat != null && contentFormat.equals("xml")) {
 			mediaType =  MediaType.valueOf("text/xml");
 		}
 		if(outputCompress!=null) {
 			mediaType =  MediaType.valueOf("application/zip");
 			return Response.ok(zipEntity, mediaType).build();
 		}else {
 			System.out.println("No zipEntity: "+zipEntity);
 			return Response.ok(entity, mediaType).build();
 		}
 	}
 
 	@Deprecated
 	protected <E extends Feature> Response generateResponseFromListFeatureList(List<FeatureList<E>> features) throws IOException {
 		
 		logger.info("DEPRECATED:: generateResponseFromListFeatureList");
 		
 		MediaType mediaType = MediaType.valueOf("text/plain");
 		String entity = null;
 		String zipEntity = "";
 		//Gson gson = new GsonBuilder().serializeNulls().create();
 		Gson gson = new Gson();
 
 		if(contentFormat != null && contentFormat.equals("txt")) {
 			mediaType = MediaType.valueOf("text/plain");
 			// cada ID en una line, cada valor en la misma linea separado por '//'
 			//			entity = ListUtils.toString(features, querySeparator);
 
 			StringBuilder result= new StringBuilder();
 			for(FeatureList<E> featureList: features) {
 				if(featureList != null) {
 					result.append(ListUtils.toString(featureList, resultSeparator));
 				}else {
 					result.append("null");					
 				}
 				result.append(querySeparator);
 			}
 			entity = result.toString().trim();
 
 		}
 		if(contentFormat != null && contentFormat.equals("json")) {
 			mediaType =  MediaType.valueOf("application/json");
 			System.out.println("features: " +features);
 			if(features != null && features.size() > 0 /*&& features.get(0) != null && features.get(0).get(0) != null*/) {
 				System.err.println("FeatureList Object Class: "+features.get(0).getClass());
 				if(listType != null) {
 					System.out.println("Creating JSON object...");
 					entity = gson.toJson(features, listType);
 					System.err.println("done!");
 					System.err.println("Entity json: "+entity);
 					zipEntity = Arrays.toString(StringUtils.gzipToBytes(entity)).replace(" " , "");
 					System.err.println("zipEntryBytes: "+StringUtils.gzipToBytes(entity));
 					System.err.println("zipEntry: "+zipEntity);
 					System.out.println("entity.length(): "+entity.length());
 					System.out.println("zipEntity.length(): "+zipEntity.length());
 
 				}else {
 					System.err.println("GenericRestWSServer: TypeToken from Gson equals null");
 				}
 			}
 		}
 		if(contentFormat != null && contentFormat.equals("xml")) {
 			mediaType =  MediaType.valueOf("text/xml");
 		}
 		if(outputCompress!=null) {
 			mediaType =  MediaType.valueOf("application/zip");
 			//			return Response.ok(StringUtils.zipToBytes(entity), mediaType).build();
 			return Response.ok(zipEntity, mediaType).build();
 		}else {
 			System.out.println("No zipEntity: "+zipEntity);
 			return Response.ok(entity, mediaType).build();
 		}
 	}
 
 	@Deprecated
 	protected Response generateErrorMessage(String errorMessage) {
 		return Response.ok("An error occurred: "+errorMessage, MediaType.valueOf("text/plain")).build();
 	}
 
 
 }
