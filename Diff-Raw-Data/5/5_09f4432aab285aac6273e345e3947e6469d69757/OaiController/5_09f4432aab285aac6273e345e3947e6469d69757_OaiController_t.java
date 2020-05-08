 package au.gov.nsw.records.search.web;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import au.gov.nsw.records.search.bean.OaiMetadataFormat;
 import au.gov.nsw.records.search.bean.OaiSetSpec;
 import au.gov.nsw.records.search.model.Agency;
 import au.gov.nsw.records.search.model.Item;
 import au.gov.nsw.records.search.model.Serie;
 import au.gov.nsw.records.search.service.DateHelper;
 
 @RequestMapping("/oai/**")
 @Controller
 public class OaiController {
 	  
 	private final Map<String, List<String>> FORMATS = new HashMap<String, List<String>>();
 	private final Map<String, List<String>> SCHEMAS = new HashMap<String, List<String>>();
 	private final List<OaiSetSpec> SETSPECS = new ArrayList<OaiSetSpec>();
 	
 	  public OaiController(){
 	  	
 	  	FORMATS.put("series", new ArrayList<String>(Arrays.asList("mods","rdf_zotero", "rif")));
 	  	FORMATS.put("items", new ArrayList<String>(Arrays.asList("mods","rdf_zotero")));
 	  	FORMATS.put("agencies", new ArrayList<String>(Arrays.asList("oai_dc","eac_cpf")));
 	  	
 	  
 	  	SCHEMAS.put("mods", new ArrayList<String>(Arrays.asList("Library of Congress Metadata Object Description Schema", "http://www.loc.gov/standards/mods/", 
 	  			"http://www.loc.gov/mods/v3", "http://www.loc.gov/standards/mods/mods.xsd")));
 	  	SCHEMAS.put("oai_dc", new ArrayList<String>(Arrays.asList("Dublin Core without qualification", "http://www.openarchives.org/OAI/openarchivesprotocol.html",
 	     	  "http://www.openarchives.org/OAI/2.0/oai_dc/", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd")));
 	  	SCHEMAS.put("eac_cpf", new ArrayList<String>(Arrays.asList("Encoded Archival Context - Corporate Bodies, Persons, and Families",
 	     	  "http://eac.staatsbibliothek-berlin.de/", "urn:isbn:1-931666-33-4", "http://eac.staatsbibliothek-berlin.de/schema/cpf.xsd")));
 	  	SCHEMAS.put("rif", new ArrayList<String>(Arrays.asList("Registry Information Format - Collections and Services", "http://www.ands.org.au/resource/rif-cs.html",
 	        "http://ands.org.au/standards/rif-cs/registryObjects", "http://services.ands.org.au/documentation/rifcs/1.3/schema/registryObjects.xsd")));
 	  	
 	  	SETSPECS.add(new OaiSetSpec("functions", "Function descriptions", "A function is a major area of responsibility, authority or jurisdiction assigned to or assumed by an organisation. Functions derive from mandates usually given in legislation. Functions can be permissive or prescriptive. They constitute the principal themes of business of any organisation."));
 	  	SETSPECS.add(new OaiSetSpec("activities", "Activity descriptions", "An activity is a part of a function. Activities provide more specific functional context for record series than can be providedbe provided by a function."));
 	  	SETSPECS.add(new OaiSetSpec("agencies", "Agency descriptions", "An agency is an administrative or business unit which has responsibility for carrying out some designated activity."));
 	  	SETSPECS.add(new OaiSetSpec("persons", "Person descriptions", "A person is an individual who creates records, usually in an official capacity, but whose records have not been maintained in the records of the associated agency."));
 	  	SETSPECS.add(new OaiSetSpec("organisations", "Organisation descriptions", "An organisation is a whole government, municipal council, incorporated company, church or other body that is generally regarded as independent and autonomous in the performance of its normal functions."));
 	  	SETSPECS.add(new OaiSetSpec("ministries", "Ministry descriptions", "A ministry is the body of ministers who hold warrants from the Head of State as members of the Executive Council. A ministry comprises a number of portfolios. A ministry is often named for the Premier who led it. Coalition ministries are often named after both leaders."));
 	  	SETSPECS.add(new OaiSetSpec("portfolios", "Portfolio descriptions", "A portfolio is the responsibility, or combination of responsibilities, assigned to a particular minister. Portfolios administer agencies."));
 	  	SETSPECS.add(new OaiSetSpec("series", "Series descriptions", "A record series is a group of (one or more) record items accumulated by an agency or person which have a common identity and system of control, and are generally in the same format."));
 	  	SETSPECS.add(new OaiSetSpec("items", "Item descriptions", "A record item is an individual unit within a record series, and the smallest entity. A record item may be in any format: (for example) a file, card, volume, plan or drawing, photograph or videotape. Some record items (such as files) may contain multiple individual documents but these are not normally listed as individual entities. In order to fully understand the significance of a record item it is vital to know what record series it forms part of. There is usually no way to determine the context or content, or format of a record item without learning about the record series."));
 	  }
 
     @RequestMapping(method = RequestMethod.POST, value = "{id}")
     public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
     }
 
     private String getRecord(String identifier, String metadataPrefix, Model uiModel) throws OaiException{
     	
     	if (identifier==null || metadataPrefix==null || identifier.isEmpty() || metadataPrefix.isEmpty()){
     		throw new OaiException("badArgument#" + "missing required argument " + (identifier==null? "identifier" : "metadataPrefix"));
     	}
     	
     	if (!isFormatAvailable(identifier, metadataPrefix)){
     		throw new OaiException("cannotDisseminateFormat#" + "The value of the metadataPrefix argument is not supported by the item identified by the value of the identifier argument");
     	}
     	
   		uiModel.addAttribute("format", metadataPrefix);
   		if (!populateEntity(identifier, uiModel)){
   			throw new OaiException("idDoesNotExist#" + "Bad identifier:" + identifier);
   		}
     	
     	uiModel.addAttribute("view", "oai/getrecord");
 			return "oai/getrecord";
     }
     
     private String identify(String identifier, String metadataPrefix, Model uiModel) throws OaiException{
     	
     	if (identifier!=null || metadataPrefix!=null){
     		throw new OaiException("badArgument#" + "The request includes illegal arguments");
     	}
     	
     	uiModel.addAttribute("view", "oai/identify");
     	return "oai/identify";
     }
     
     private String listIdentifiers(String identifier, String metadataPrefix, String from, String until, String className, String resumptionToken, Model uiModel) throws OaiException{
     	
     	if (metadataPrefix==null){
     		throw new OaiException("badArgument#" + "The request includes illegal arguments or missing required arguments");
     	}
     	ResumptionToken token = null;
     	if (resumptionToken!=null){
     		token = ResumptionToken.parse(resumptionToken);
     	}else{
     		if (!isValidFormat(metadataPrefix)){
     			throw new OaiException("cannotDisseminateFormat#" + "Repository does not support format: " + metadataPrefix);
     		}
     		if (className!=null){
     			if (!isValidSpec(className)){
     				throw new OaiException("badArgument#" + "Set does not exist: " + className);
     			}
     			if (!isValidFormat(className, metadataPrefix)){
     				throw new OaiException("cannotDisseminateFormat#" + className + " set does not support format: " + metadataPrefix);
     			}
     		}
     		
     		token = new ResumptionToken(metadataPrefix, className, from, until, 1);
     	}
     	
     	if (!populateEntitySearch(token ,uiModel)){
     		throw new OaiException("noRecordsMatch#" + "No records returned for these parameters");
     	}
     		
     	uiModel.addAttribute("resumptionToken", token.nextToken().toString());
     	uiModel.addAttribute("page", token.page);
     	uiModel.addAttribute("view", "oai/listidentifiers");
     	return "oai/listidentifiers";
     }
     
     private String listMetadataFormats(String identifier, String metadataPrefix, Model uiModel) throws OaiException{
     	
     	if (metadataPrefix!=null){
     		throw new OaiException("badArgument#" + "The request includes illegal arguments or missing required arguments");
     	}
     	
     	if (identifier!=null){
     		String type = getEntityType(identifier);
     		if (type!=null && !type.isEmpty()){
     			List<OaiMetadataFormat> formats = new ArrayList<OaiMetadataFormat>();
     			for (String fmt: FORMATS.get(type)){
     				if (SCHEMAS.get(fmt)!=null){
     					formats.add(new OaiMetadataFormat(fmt, SCHEMAS.get(fmt).get(3), SCHEMAS.get(fmt).get(2)));
     				}
     			}
     			if (formats.isEmpty()){
     				throw new OaiException("noMetadataFormats#" + "There are no metadata formats for " + identifier);
     			}
     			uiModel.addAttribute("formats", formats);	
     		}else{
     			throw new OaiException("idDoesNotExist#" + "Bad identifier:" + identifier);
     		}
     	}else{
     		List<OaiMetadataFormat> formats = new ArrayList<OaiMetadataFormat>();
     		for (String key:SCHEMAS.keySet()){
     			formats.add(new OaiMetadataFormat(key, SCHEMAS.get(key).get(3), SCHEMAS.get(key).get(2)));
     		}
     		uiModel.addAttribute("formats", formats);	
     	}
     	uiModel.addAttribute("view", "oai/listmetadataformats");
     	return "oai/listmetadataformats";
     }
     
     private String listRecords(String identifier, String metadataPrefix, String from, String until, String className, String resumptionToken, Model uiModel) throws OaiException{
     	
     	ResumptionToken token = null;
     	if (resumptionToken!=null){
     		token = ResumptionToken.parse(resumptionToken);
    		
    		metadataPrefix = resumptionToken.substring(0,resumptionToken.indexOf(":"));
     	}else{
     		
     		if (metadataPrefix==null){
       		throw new OaiException("badArgument#" + "The request includes illegal arguments or missing required arguments");
       	}
     		
     		if (!isValidFormat(metadataPrefix)){
     			throw new OaiException("cannotDisseminateFormat#" + "Repository does not support format: " + metadataPrefix);
     		}
     		if (className!=null){
     			if (!isValidSpec(className)){
     				throw new OaiException("badArgument#" + "Set does not exist: " + className);
     			}
     			if (!isValidFormat(className, metadataPrefix)){
     				throw new OaiException("cannotDisseminateFormat#" + className + " set does not support format: " + metadataPrefix);
     			}
     		}
     		token = new ResumptionToken(metadataPrefix, className, from, until, 1);
     	}
     	
     	if (!populateEntitySearch(token ,uiModel)){
     		throw new OaiException("noRecordsMatch#" + "No records returned for these parameters");
     	}
     		
     	uiModel.addAttribute("resumptionToken", token.nextToken().toString());
     	uiModel.addAttribute("page", token.page);
     	
    	String view = String.format("oai/listrecords_%s_%s", uiModel.asMap().get("type"), metadataPrefix);
     	uiModel.addAttribute("view", view);
     	return "view";
     }
     
     private String listSets(String identifier, String metadataPrefix, Model uiModel) throws OaiException{
     	
     	uiModel.addAttribute("sets", SETSPECS);
     	uiModel.addAttribute("view", "oai/listsets");
     	return "oai/listsets";
     }
     
     @RequestMapping(produces="application/xml")
     public String index(@RequestParam(value = "verb", required = false) String verb, Model uiModel,
     		@RequestParam(value = "metadataPrefix", required = false) String metadataPrefix,
     		@RequestParam(value = "identifier", required = false) String identifier,
     		@RequestParam(value = "from", required = false) String from,
     		@RequestParam(value = "until", required = false) String until,
     		@RequestParam(value = "set", required = false) String set, 
     		@RequestParam(value = "resumptionToken", required = false) String resumptionToken) {
     	
     	uiModel.addAttribute("verb", verb);
     	uiModel.addAttribute("metadataPrefix", metadataPrefix);
     	uiModel.addAttribute("identifier", identifier);
     	uiModel.addAttribute("from", from);
     	uiModel.addAttribute("until", until);
     	uiModel.addAttribute("set", set);
     	uiModel.addAttribute("resumptionToken", resumptionToken);
     
     	String responseAttributes = "";
     	if (until!=null) responseAttributes += String.format(" until=\"%s\"",until);
     	if (from!=null) responseAttributes += String.format(" from=\"%s\"",from);
     	if (set!=null) responseAttributes += String.format(" set=\"%s\"",set);
     	if (metadataPrefix!=null) responseAttributes += String.format(" metadataPrefix=\"%s\"",metadataPrefix);
     	if (resumptionToken!=null) responseAttributes += String.format(" resumptionToken=\"%s\"",resumptionToken);
     	if (verb!=null) responseAttributes += String.format(" verb=\"%s\"",verb);
     	
     	uiModel.addAttribute("responseXML", "<request" + responseAttributes + ">");
     	uiModel.addAttribute("_responseXML", "</request>");
     	
     	String returnView = "oai/error";
     	try{
     		
 	    	if (verb!=null){
 	    		if (verb.equalsIgnoreCase("GetRecord")){
 	    			return getRecord(identifier, metadataPrefix, uiModel);
 	    		}else if (verb.equalsIgnoreCase("Identify")){
 	    			return identify(identifier, metadataPrefix, uiModel);
 	    		}else if (verb.equalsIgnoreCase("ListIdentifiers")){
 	    			return listIdentifiers(identifier, metadataPrefix, from, until, set, resumptionToken, uiModel);
 	    		}else if (verb.equalsIgnoreCase("ListMetadataFormats")){
 	    			return listMetadataFormats(identifier, metadataPrefix, uiModel);
 	    		}else if (verb.equalsIgnoreCase("ListRecords")){
 	    			return listRecords(identifier, metadataPrefix, from, until, set, resumptionToken, uiModel);
 	    		}else if (verb.equalsIgnoreCase("ListSets")){
 	    			return listSets(identifier, metadataPrefix, uiModel);
 	    		}else{
 	    			throw new OaiException("badVerb#Expecting parameter verb=GetRecord|Identify|ListIdentifiers|ListMetadataFormats|ListRecords|ListSets");
 	    		}
 	    	}else{
 	  			throw new OaiException("badVerb#Expecting parameter verb=GetRecord|Identify|ListIdentifiers|ListMetadataFormats|ListRecords|ListSets");
 	    	}
     	}catch(OaiException e){
     		//e.printStackTrace();
     		uiModel.addAttribute("errorCode", e.getMessage().substring(0, e.getMessage().indexOf("#")));
   			uiModel.addAttribute("errorContent", e.getMessage().substring(e.getMessage().indexOf("#")+1));
   			returnView = "oai/error";
     	}catch(Exception e){
     		e.printStackTrace();
     		uiModel.addAttribute("errorCode","badVerb");
     		uiModel.addAttribute("errorContent", "internal server error");
   			returnView = "oai/error";
     	}
     	uiModel.addAttribute("view", returnView);
     	return returnView;
     }
     
     private boolean isFormatAvailable(String identifier, String metadataPrefix){
     	String entityAndId = identifier.substring(identifier.lastIndexOf(":") + 1);
     	String entityId = entityAndId.substring(entityAndId.indexOf("/") + 1);
     	String entityName = entityAndId.replace("/"+entityId, "");
     	
     	List<String> supportFormats = FORMATS.get(entityName);
     	if (supportFormats!=null && supportFormats.contains(metadataPrefix)){
     		return true;
     	}
     	return false;
     }
     
     private boolean isValidFormat(String spec, String format){
     	if (FORMATS.containsKey(spec) && SCHEMAS.get(spec)!=null && SCHEMAS.get(spec).contains(format)){
     		return true;
     	}
     	return false;
     }
     private boolean isValidFormat(String format){
     	if (SCHEMAS.containsKey(format)){
     		return true;
     	}
     	return false;
     }
     
     private boolean isValidSpec(String spec){
     	if (FORMATS.containsKey(spec)){
     		return true;
     	}
     	return false;
     }
     
     private boolean populateEntitySearch(ResumptionToken token, Model uiModel){
     	String entityName = token.getSet();
     	int pageSize = 100;
     	// auto populate entity from the known format if it does not present
     	if (entityName==null || entityName.isEmpty()){
     		for (String key:FORMATS.keySet()){
     			for (String storedFormat:FORMATS.get(key)){
     				if (storedFormat.equalsIgnoreCase(token.getFormat())){
     					entityName = key;
     					break;
     				}
     			}
     		}
     	}
     	uiModel.addAttribute("type", entityName);
     	if (entityName.equals("series")){
     		List<Serie> s = Serie.findSeriesFromLastAmendmentDate(token.fromDate, token.untilDate, token.getPage(), pageSize);
     		if (!s.isEmpty()){
     			uiModel.addAttribute("entities", s);
     			uiModel.addAttribute("size", Serie.countSeriesFromLastAmendmentDate(token.fromDate, token.untilDate));
     		}else{
     			return false;
     		}
     	}else if (entityName.equals("items")){
     		List<Item> i = Item.findItemFromLastAmendmentDate(token.fromDate, token.untilDate, token.getPage(), pageSize);
     		if (!i.isEmpty()){
     			uiModel.addAttribute("entities", i);
     			uiModel.addAttribute("size", Item.countItemFromLastAmendmentDate(token.fromDate, token.untilDate));
     		}else{
     			return false;
     		}
     	}else if (entityName.equals("agencies")){
     		List<Agency> a =Agency.findAgencyFromLastAmendmentDate(token.fromDate, token.untilDate, token.getPage(), pageSize);
     		if (!a.isEmpty()){
     			uiModel.addAttribute("entities", a);
     			uiModel.addAttribute("size", Agency.countAgencyFromLastAmendmentDate(token.fromDate, token.untilDate));
     		}else{
     			return false;
     		}
     	}else{
     		return false;
     	}
     	
     	return true;
     }
     
     private boolean populateEntity(String entity, Model uiModel){
     	//ex. oai:api.records.nsw.gov.au:series/1
     	String entityAndId = entity.substring(entity.lastIndexOf(":") + 1);
     	String entityId = entityAndId.substring(entityAndId.indexOf("/") + 1);
     	String entityName = entityAndId.replace("/"+entityId, "");
     	uiModel.addAttribute("type", entityName);
     	if (entityName.equals("series")){
     		Serie s = Serie.findSerie(Integer.valueOf(entityId));
     		if (s!=null){
     			uiModel.addAttribute("entity", s);
     		}else{
     			return false;
     		}
     	}else if (entityName.equals("items")){
     		Item i = Item.findItem(Integer.valueOf(entityId));
     		if (i!=null){
     			uiModel.addAttribute("entity", i);
     		}else{
     			return false;
     		}
     	}else if (entityName.equals("agencies")){
     		Agency a =Agency.findAgency(Integer.valueOf(entityId));
     		if (a!=null){
     			uiModel.addAttribute("entity", a);
     		}else{
     			return false;
     		}
     	}else{
     		return false;
     	}
     	
     	return true;
     }
     
     private String getEntityType(String identifier){
     	String entityAndId = identifier.substring(identifier.lastIndexOf(":") + 1);
     	String entityId = entityAndId.substring(entityAndId.indexOf("/") + 1);
     	String entityName = entityAndId.replace("/"+entityId, "");
     	return entityName;
     }
     @SuppressWarnings("serial")
 		public class OaiException extends Exception{
     	protected OaiException(String error){
     		super(error);
     	}
     }
     
     private static class ResumptionToken{
     	public String getFrom() {
 				return nullToEmpty(from);
 			}
 
 			public String getUntil() {
 				return nullToEmpty(until);
 			}
 
 			public String getSet() {
 				return nullToEmpty(set);
 			}
 
 			public String getFormat() {
 				return format;
 			}
 
 			public int getPage() {
 				return page;
 			}
 			private String from="";
     	private String until="";
     	private String set="";
     	private String format="";
     	private Date fromDate;
     	private Date untilDate;
     	private int page = 1;
     	
     	public ResumptionToken(String format, String set, String from, String until, int page) throws OaiException {
 				super();
 				try {
 					if (from!=null && !from.isEmpty()){
 						fromDate = DateHelper.parseDate(from);
 					}
 					if (until!=null && !until.isEmpty()){
 						untilDate = DateHelper.parseDate(until);
 					}
 				} catch (ParseException e) {
 					throw (new OaiController()).new OaiException("badResumptionToken#" + "Token invalid:");
 				}
 				this.from = from==null? "":from;
 				this.until = until==null? "":until;
 				this.set = set==null? "":set;
 				this.format = format==null? "":format;
 				this.page = page;
 			
 			}
 
 			public static ResumptionToken parse(String token) throws OaiException{
 				ArrayList<String> elements = new ArrayList<String>(Arrays.asList(token.split(":")));
 				if (elements.size()!=5){
 					throw (new OaiController()).new OaiException("badResumptionToken#" + "Token invalid:" + token);
 				}
     		return new ResumptionToken(elements.get(0), elements.get(1), elements.get(2), elements.get(3), Integer.valueOf(elements.get(4)));
     	}
 			
 			public ResumptionToken nextToken() throws OaiException{
 				return new ResumptionToken(format, getSet(), getFrom(), getUntil(), page+1);
 			}
     	@Override
     	public String toString(){
     		return String.format("%s:%s:%s:%s:%s", format, set, from, until, page);
     	}
     	
     	private String nullToEmpty(String str){
     		if (str==null || str.equals("null")){
     			return "";
     		}
     		return str;
     	}
     }
 }
