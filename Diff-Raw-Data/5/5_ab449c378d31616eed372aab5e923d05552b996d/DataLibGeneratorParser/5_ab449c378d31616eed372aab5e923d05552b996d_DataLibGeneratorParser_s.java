 package fr.eyal.datalib.generator;
 
 import java.math.BigInteger;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import org.eclipse.emf.common.util.EList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import fr.eyal.lib.datalib.genmodel.android.datalib.DataLibProject;
 import fr.eyal.lib.datalib.genmodel.android.datalib.DatalibFactory;
 import fr.eyal.lib.datalib.genmodel.android.datalib.DatalibPackage;
 import fr.eyal.lib.datalib.genmodel.android.datalib.WebService;
 import fr.eyal.lib.datalib.genmodel.android.datalib.content.DataLibOption;
 import fr.eyal.lib.datalib.genmodel.android.datalib.content.HttpMethod;
 import fr.eyal.lib.datalib.genmodel.android.datalib.content.ParameterType;
 import fr.eyal.lib.datalib.genmodel.android.datalib.content.ParseType;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.BusinessObject;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.BusinessObjectDAO;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.Field;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.FieldBusinessObject;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.ModelFactory;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.ModelPackage;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.Parameter;
 import fr.eyal.lib.datalib.genmodel.android.datalib.model.ResponseBusinessObject;
 
 
 public class DataLibGeneratorParser extends DefaultHandler {
 
 	private static final String TAG = DataLibGeneratorParser.class.getSimpleName();
 
 	public static final int UNKNOWN = -1;
 
 	//Content tags
 	private static final int PROJECT = 0;
 	private static final int WEBSERVICE = 1;
 	private static final int CONFIG = 2;
 	private static final int HTTP_METHOD = 3;
 	private static final int PARSE_TYPE = 4;
 	private static final int OPTIONS = 5;
 	private static final int PARAMETERS = 6;
 	private static final int CONTENT = 7;
 	private static final int OPTIONS_OPTION = 8;
 
 	private int mState = UNKNOWN;
 
 	private final StringBuilder mBuilder = new StringBuilder();
 	private DataLibProject project;
 	private WebService webservice;
 
 	String name = DataLibConfig.DEFAUT_PROJECT_NAME;
 	String packageName = DataLibConfig.DEFAUT_PROJECT_PACKAGE;
 	String authority = DataLibConfig.DEFAUT_PROJECT_AUTHORITY;
 	String databaseName = DataLibConfig.DEFAUT_PROJECT_DATABASE_NAME;
 	String databaseVersion = DataLibConfig.DEFAUT_PROJECT_DATABASE_VERSION;
 
 	DatalibFactory factory;
 	ModelFactory modelFactory;
 
 	BusinessObject parent; //the current parent of the node
 	BusinessObject bo; //the current business object where to include a new node
 
 	Field field = null; //the current field
 	Field fieldXML = null; //the current xmlField
 	boolean isFillingField = false; //tells whether we are filling a Field or not
 	Field tempField;
 	
 //	ArrayList<String> javaNameList = new ArrayList<String>(); //the list of the member variables contained inside the current BusinessObject
 	Stack<String> javaTagStack = new Stack<>(); //stack of names for the current BusinessObject
 	
 	ArrayList<String> prefixList = new ArrayList<String>();
 	String prefixName = ""; //the prefix to paste for the current node's name
 	String responseTagName; //the first content node's tag name
 	boolean isResponseField = false; //tells if we are on the ResponseBusinessObject level and the content does not still has a contentField
 
 	Attributes mCurrentAttributes;
 	
 	boolean cached = DataLibConfig.DEFAUT_WEBSERVICE_CACHED;
 	int contentLevel = 0;
 	long parseId = 0;
 
 	public DataLibGeneratorParser() {
 		//initialize the model
 		DatalibPackage.eINSTANCE.eClass();
 		ModelPackage.eINSTANCE.eClass();
 		//retrieve the default factory singleton
 		factory = DatalibFactory.eINSTANCE;
 		modelFactory = ModelFactory.eINSTANCE;
 
 		System.out.println("Creation of the model...");
 
 	}
 
 
 	@Override
 	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
 
 		//        String attString = "";
 		//        for (int i = 0; i < attributes.getLength(); i++) {
 		//            attString += " - " + attributes.getQName(i);
 		//        }
 
 		for (int i = 0; i < contentLevel; i++) {
 			System.out.print("-");
 		}
 		System.out.println(qName);
 
 		mBuilder.setLength(0);
 
 		mCurrentAttributes = attributes;
 		
 		switch (mState) {
 
 		case UNKNOWN:
 			if (qName.equals(DataLibLabels.XML_PROJECT)) {
 				mState = PROJECT;
 
 				name = attributes.getValue(DataLibLabels.XML_NAME);
 				packageName = attributes.getValue(DataLibLabels.XML_PACKAGE);
 				authority = attributes.getValue(DataLibLabels.XML_AUTHORITY);
 				databaseName = attributes.getValue(DataLibLabels.XML_DATABASE_NAME);
 				databaseVersion = attributes.getValue(DataLibLabels.XML_DATABASE_VERSION);
 
 				if(name == null) name = DataLibConfig.DEFAUT_PROJECT_NAME;
 				if(packageName == null) packageName = DataLibConfig.DEFAUT_PROJECT_PACKAGE;
 				if(authority == null) authority = DataLibConfig.DEFAUT_PROJECT_AUTHORITY;
 				if(databaseName == null) databaseName = DataLibConfig.DEFAUT_PROJECT_DATABASE_NAME;
 				if(databaseVersion == null) databaseVersion = DataLibConfig.DEFAUT_PROJECT_DATABASE_VERSION;
 
 				project = DataLibGenerator.createProject(factory, authority, databaseName, databaseVersion, name, packageName);
 
 			}
 			break;
 
 		case PROJECT:
 			if (qName.equals(DataLibLabels.XML_WEBSERVICE)) {
 				mState = WEBSERVICE;
 
 				//we clear the webservice values
 				cached = DataLibConfig.DEFAUT_WEBSERVICE_CACHED;
 				contentLevel = 0;
 //				parseId = 0;
 
 				HttpMethod httpMethod = getHttpMethod(attributes.getValue(DataLibLabels.XML_HTTP_METHOD));
 
 				ParseType parseType = getParseType(attributes.getValue(DataLibLabels.XML_PARSE_TYPE));
 
 				String url = attributes.getValue(DataLibLabels.XML_URL);
 				if(url == null) throw new SAXException("'"+DataLibLabels.XML_URL+"' attributes is mandatory for each "+DataLibLabels.XML_WEBSERVICE+" tag. ou must provide the URL reference of the webservice");
 				
 				name = attributes.getValue(DataLibLabels.XML_NAME);
 				
 				webservice = factory.createWebService();
 				webservice.setCached(true);
 				webservice.setMethod(httpMethod);
 				webservice.setPackage(packageName);
 				webservice.setParseType(parseType);
 				webservice.setUrl(url);
 				webservice.setName(name);
 
 				project.getWebservices().add(webservice);
 				
 
 				ResponseBusinessObject response;
 				
 				String javaTag = getJavaTag(name);
 				String javaName = name;
 
 				//we create the response depending on the cache status
 				if(cached){
 					response = modelFactory.createResponseBusinessObjectDAO();
 					DataLibGenerator.fillBusinessObjectDAO((BusinessObjectDAO) response, name, packageName, new BigInteger(""+parseId), qName, javaName, javaTag, null, null, null, null, project);
 				} else {
 					response = modelFactory.createResponseBusinessObject();
 					DataLibGenerator.fillBusinessObject(response, name, packageName, new BigInteger(""+parseId), qName, javaName, javaTag, null, null, null, null);
 				}
 				parseId++; //we increment the parseId counter
 
 				//we add the content response to the webservice
 				webservice.setContentResponse((ResponseBusinessObject) response);
 
 				parent = response; //we set the response as the node's parent
 				bo = response; //we set the response as the current business object to fill
 
 			}
 			break;
 
 		case WEBSERVICE:
 		case CONTENT:
 
 			if (qName.equals(DataLibLabels.XML_CONFIG)) {
 				mState = CONFIG;   
 
 			} else { //if we are on a content tag
 				mState = CONTENT;
 
 				//we get the name
 				String name = attributes.getValue(DataLibLabels.XML_NAME);
 				if(name == null) name = qName;
 
 				javaTagStack.push(qName);
 				String javaTag = getJavaTag(qName);
 
 				//we check if the node is multiple. If it is we create a new child for the parent
 				boolean multiple = Boolean.parseBoolean(attributes.getValue(DataLibLabels.XML_MULTIPLE));
 
 				//if we are in the content response (first node of the content)
 				if(contentLevel == 0){
 
 					//we tell that we are on the response level
 					isResponseField = true;
 					
 					String javaName = getJavaName(name);
 					
 					//we manage the attributes linking
 					processBOAttributes(attributes, bo, null);
 					
 					prefixList.clear();
 					prefixName = name; // we declare the prefix name
 					responseTagName = qName; //we set the response tag name
 
 					//we create and add the new XML node
 					if(multiple)
 						tempField = DataLibGenerator.createFieldBusinessObject(modelFactory, name, null, "", "", qName, javaName, javaTag, new BigInteger(""+parseId), null, parent, null, bo, null);
 					else
 						tempField = DataLibGenerator.createField(modelFactory, name, null, "", "", qName, javaName, javaTag, new BigInteger(""+parseId), null, parent, null);
 						
 					parseId++;
 					
 					((ResponseBusinessObject)bo).getXmlContentFields().add(tempField);
 					fieldXML = tempField;
 
 					
 					
 				} else { //if we are on the rest of the content
 
 					//We tells that we are out on the response level
 					isResponseField = false;
 					
 					//if the tag is multiple, we add a new content level 
 					if(multiple){
 
 						String javaName = getJavaName(name);
 
 						//we create the BusinessObject depending on the cache status
 						if(cached){
 							bo = modelFactory.createBusinessObjectDAO();
 							DataLibGenerator.fillBusinessObjectDAO((BusinessObjectDAO) bo, name, packageName, new BigInteger(""+parseId), qName, javaName, javaTag, null, null, null,null, project);
 						} else {
 							bo = modelFactory.createBusinessObject();
 							DataLibGenerator.fillBusinessObject(bo, name, packageName, new BigInteger(""+parseId), qName, javaName, javaTag, null, null, null, null);
 						}
 						
 						//we create and add the new XML node
 						FieldBusinessObject f = null;
 						f = DataLibGenerator.createFieldBusinessObject(modelFactory, name, null, "", "", qName, javaName, javaTag, new BigInteger(""+parseId), null, parent, null, bo, null);
 						//if we are on the first level after the ResponseBusinessObject
 //						if(fieldXML == null){
 //							((ResponseBusinessObject)parent).getXmlContentFields().add(f);
 //							f.setXmlParent(parent.getRelatedField());
 //						} else {
 							f.setXmlParent(fieldXML);
 							fieldXML.getXmlContentFields().add(f);
 //						}
 						bo.setRelatedField(f);
 						fieldXML = f;
 						
 						parseId++; //we increment the parseId counter
 
 						//we manage the attributes linking
 						processBOAttributes(attributes, bo, f);
 
 						//we add the BusinessObject to the parent
 						parent.getChilds().add(bo);
 
 						//we set the parent of the new node and set the current parent as the new node
 						bo.setParent(parent);
 						parent = bo;
 
 						prefixList.clear();
 						prefixName = name; // we declare the prefix name
 
 					} else { //we are in a node that has to be include inside the parent's content fields
 
 						//if the previous content field is just a container (that means that it does not contain data, just nodes)
						if(isFillingField)
 							bo.getContentFields().remove(field); //we remove it from the current business object
 						else
 							isFillingField = true;
 
 						String javaName = getJavaName(name);
 
 						ParameterType type = getParameterType(attributes.getValue(DataLibLabels.XML_TYPE));
 						String defaultValue = attributes.getValue(DataLibLabels.XML_DEFAULT);
 
 						//we create the current field
 						field = DataLibGenerator.createField(modelFactory, name, type, "", defaultValue, qName, javaName, javaTag, new BigInteger(""+parseId), null, bo, null);
 
 						//we create and add the new XML node
 						Field f = null;
 						f = DataLibGenerator.createField(modelFactory, name, type, "", defaultValue, qName, javaName, javaTag, new BigInteger(""+parseId), null, bo, null);
 						f.setXmlParent(fieldXML);
 						f.setRelatedField(field);
 
 						fieldXML.getXmlContentFields().add(f);
 						fieldXML = f;
 
 						field.setRelatedField(f);
 
 						parseId++; //we increment the parseId counter
 
 						//we update the prefix name
 						prefixList.add(name);
 						prefixName = prefixName + name;
 
 						//we manage the attributes linking
 						processFieldAttributes(attributes, f);
 						
 						bo.getContentFields().add(field);
 					}
 
 				}
 				contentLevel++;
 			}
 
 			break;
 
 		case CONFIG:
 
 			if (qName.equals(DataLibLabels.XML_HTTP_METHOD)) {
 				mState = HTTP_METHOD;
 
 			} else if (qName.equals(DataLibLabels.XML_PARSE_TYPE)) {
 				mState = PARSE_TYPE;
 
 			} else if (qName.equals(DataLibLabels.XML_OPTIONS)) {
 				mState = OPTIONS;
 
 			} else if (qName.equals(DataLibLabels.XML_PARAMETERS)) {
 				mState = PARAMETERS;
 
 			}
 			break;
 
 		case OPTIONS:
 			mState = OPTIONS_OPTION;
 
 			if (qName.equals(DataLibLabels.XML_OPTION_CONSERVE_COOKIE)) {
 				webservice.getOptions().add(DataLibOption.CONSERVE_COOKIE);
 
 			} else if (qName.equals(DataLibLabels.XML_OPTION_DATABASE_CACHE_DISABLED)) {
 				webservice.getOptions().add(DataLibOption.DATA_BASE_CACHE_DISABLED);
 				webservice.setCached(false); //we update the webservice cached value
 				cached = false;
 
 			} else if (qName.equals(DataLibLabels.XML_OPTION_HELPER_CACHE_DISABLED)) {
 				webservice.getOptions().add(DataLibOption.HELPER_CACHE_DISABLED);
 
 			} else if (qName.equals(DataLibLabels.XML_OPTION_HOST_VERIFIER_DISABLED)) {
 				webservice.getOptions().add(DataLibOption.HOST_VERIFIER_DISABLED);
 
 			} else if (qName.equals(DataLibLabels.XML_OPTION_SEND_WITH_PARCELABLE)) {
 				webservice.getOptions().add(DataLibOption.SEND_WITH_PARCELABLE);
 			}
 			break;
 
 		case PARAMETERS:
 			String description = attributes.getValue(DataLibLabels.XML_DESCRIPTION);
 			ParameterType type = getParameterType(attributes.getValue(DataLibLabels.XML_TYPE));
 
 			//we eventually add the url parameter
 			int urlIndex = -1;
 			String urlParam = attributes.getValue(DataLibLabels.XML_URL_PARAMETERS);
 			if(urlParam != null){
 				urlIndex = Integer.parseInt(urlParam);
 
 				EList<Parameter> urlParamsList = webservice.getUrlParameters();
 				//we scope the urlIndex and set it as the maximum acceptable value for the actual array
 				int index = (urlIndex+1 > urlParamsList.size()) ? urlParamsList.size() : urlIndex;
 				
 				webservice.getUrlParameters().add(index, DataLibGenerator.createParameter(modelFactory, qName, type, description, urlIndex));
 			}
 			
 			webservice.getParameters().add(DataLibGenerator.createParameter(modelFactory, qName, type, description, urlIndex));
 			
 		default:
 			// do nothing
 			break;
 		}
 
 	}
 
 
 	@Override
 	public void characters(final char[] ch, final int start, final int length) throws SAXException {
 		mBuilder.append(ch, start, length);
 	}
 
 	@Override
 	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
 
 		switch (mState) {
 
 		case PROJECT:
 			if (qName.equals(DataLibLabels.XML_PROJECT)) {
 				mState = UNKNOWN;
 			}
 			break;
 		case WEBSERVICE:
 			if (qName.equals(DataLibLabels.XML_WEBSERVICE)) {
 				mState = PROJECT;
 			}
 			break;
 
 		case CONFIG:
 			if (qName.equals(DataLibLabels.XML_CONFIG)) {
 				mState = WEBSERVICE;
 				
 				//we check the URL format
 				EList<Parameter> params =  webservice.getUrlParameters();
 				if(params.size() > 0){
 					
 					Parameter lastParams = params.get(params.size()-1);
 					String[] array = new String[lastParams.getUrlParameter()+1];
 					try{
 						for (Parameter parameter : params) {
 							array[parameter.getUrlParameter()] = parameter.getName();
 						}
 					} catch (IndexOutOfBoundsException e) {
 						throw new IllegalArgumentException("The URL parameters are not well numbered. Check that is correct and add the corresponding parameters into the <"+DataLibLabels.XML_PARAMETERS+"> content's. Use "+DataLibLabels.XML_URL_PARAMETERS+" attribute to define the parameter as a URL parameter specifying its ID");
 					}
 					String test = MessageFormat.format(webservice.getUrl(), (Object[])array);
 					System.out.println(test);
 					
 				} else {
 					System.out.println(webservice.getUrl());
 				}
 			}
 			break;
 
 		case HTTP_METHOD:
 			if (qName.equals(DataLibLabels.XML_HTTP_METHOD)) {
 				mState = CONFIG;
 				webservice.setMethod(getHttpMethod(mBuilder.toString()));
 			}
 			break;
 
 		case PARSE_TYPE:
 			if (qName.equals(DataLibLabels.XML_PARSE_TYPE)) {
 				mState = CONFIG;
 				webservice.setParseType(getParseType(mBuilder.toString()));
 			}
 			break;
 
 		case OPTIONS:
 			if (qName.equals(DataLibLabels.XML_OPTIONS)) {
 				mState = CONFIG;
 			}
 			break;
 
 		case OPTIONS_OPTION:
 			mState = OPTIONS;
 			break;
 
 		case PARAMETERS:
 			if (qName.equals(DataLibLabels.XML_PARAMETERS)) {
 				mState = CONFIG;
 			}
 			break;
 
 		case CONTENT:
 			//if we finish to read the content
 			if (qName.equals(responseTagName)) {
 				mState = WEBSERVICE;
 				contentLevel--;
 				
 				String content = mBuilder.toString();
 				content = content.replaceAll("\\s+", "");
 				
 				if(isResponseField && !content.equals("")){
 					
 					//we get the name
 					String name = mCurrentAttributes.getValue(DataLibLabels.XML_NAME);
 					if(name == null) name = qName;
 
 					String javaTag = getJavaTag(qName);
 					String javaName = getJavaName(name);
 					
 					ParameterType type = getParameterType(mBuilder.toString());
 
 					String defaultValue = mCurrentAttributes.getValue(DataLibLabels.XML_DEFAULT);
 					
 					//we create the current field
 					field = DataLibGenerator.createField(modelFactory, name, type, "", defaultValue, qName, javaName, javaTag, new BigInteger(""+parseId), null, bo, null);
 										
 //					//we update the prefix name
 //					prefixList.add(name);
 //					prefixName = prefixName + name;
 					
 //					//we manage the attributes linking
 //					processFieldAttributes(mCurrentAttributes, f);
 					
 					bo.getContentFields().add(field);
 
 					tempField.setRelatedField(field);
 				}
 				
 				
 				
 				
 				//if we are reading a field
 			} else if(isFillingField) {
 				
 				ParameterType type = getParameterType(mBuilder.toString());
 				field.setType(type);
 				fieldXML.setType(type);
 
 				field = null;
 				isFillingField = false;
 				
 				fieldXML = fieldXML.getXmlParent();
 				contentLevel--;
 				
 				//we pop the last name
 				prefixList.remove(prefixList.size()-1);
 
 				//if we are reading a business object
 			} else {
 				contentLevel--;
 				bo = bo.getParent();
 				parent = parent.getParent();
 				fieldXML = fieldXML.getXmlParent();
 			}
 			
 			javaTagStack.pop();
 			
 			break;			
 
 
 		default:
 			// do nothing
 			break;
 		}
 		
 		mBuilder.setLength(0);
 
 	}
 
 
 	private HttpMethod getHttpMethod(String value) {
 
 		if(value == null)
 			return DataLibConfig.DEFAUT_WEBSERVICE_HTTP_METHOD;    		
 
 		if(value.equalsIgnoreCase(HttpMethod.GET.getName()))
 			return HttpMethod.GET;
 		if(value.equalsIgnoreCase(HttpMethod.DELETE.getName()))
 			return HttpMethod.DELETE;
 		if(value.equalsIgnoreCase(HttpMethod.HEAD.getName()))
 			return HttpMethod.HEAD;
 		if(value.equalsIgnoreCase(HttpMethod.POST.getName()))
 			return HttpMethod.POST;
 		if(value.equalsIgnoreCase(HttpMethod.PUT.getName()))
 			return HttpMethod.PUT;
 		if(value.equalsIgnoreCase(HttpMethod.SOAP.getName()))
 			return HttpMethod.SOAP;
 
 		return DataLibConfig.DEFAUT_WEBSERVICE_HTTP_METHOD;
 	}
 
 
 	private ParseType getParseType(String value) {
 
 		if(value == null)
 			return DataLibConfig.DEFAUT_WEBSERVICE_PARSE_TYPE;    		
 
 		if(value.equalsIgnoreCase(ParseType.SAX.getName()))
 			return ParseType.SAX;
 		if(value.equalsIgnoreCase(ParseType.JSON.getName()))
 			return ParseType.JSON;
 		if(value.equalsIgnoreCase(ParseType.IMAGE.getName()))
 			return ParseType.IMAGE;
 		if(value.equalsIgnoreCase(ParseType.CUSTOM.getName()))
 			return ParseType.CUSTOM;
 
 		return DataLibConfig.DEFAUT_WEBSERVICE_PARSE_TYPE;
 	}
 
 	private ParameterType getParameterType(String value) {
 
 		if(value == null)
 			return DataLibConfig.DEFAUT_WEBSERVICE_PARAMETER_TYPE;    		
 
 		if(value.equalsIgnoreCase(ParameterType.BOOLEAN.getName()))
 			return ParameterType.BOOLEAN;
 		if(value.equalsIgnoreCase(ParameterType.FLOAT.getName()))
 			return ParameterType.FLOAT;
 		if(value.equalsIgnoreCase(ParameterType.INT.getName()) ||
 		   value.equalsIgnoreCase("integer"))
 			return ParameterType.INT;
 		if(value.equalsIgnoreCase(ParameterType.STRING.getName()))
 			return ParameterType.STRING;
 
 		return DataLibConfig.DEFAUT_WEBSERVICE_PARAMETER_TYPE;
 	}
 
 	
 	private String getPrefixName(){
 		StringBuilder prefix = new StringBuilder();
 		for (int i = 0; i < prefixList.size(); i++) {
 			String item = prefixList.get(i);
 			if(item.length() > 0){
 				prefix.append(Character.toUpperCase(item.charAt(0)));
 				prefix.append(item.substring(1));
 			} else
 				prefix.append(item);
 		}
 		return prefix.toString();
 	}
 
 	public DataLibProject getParseResult(){
 		return project;
 	}
 
 
 	private void processBOAttributes(final Attributes attributes, BusinessObject businessObject, Field field) {
 		
 		for (int i = 0; i < attributes.getLength(); i++) {
 			
 			if(!attributes.getQName(i).startsWith(DataLibLabels.XML_PREFIX)){
 				ParameterType typeP = getParameterType(attributes.getValue(i));
 
 				String xmlName = attributes.getQName(i);
 				String upperName = Character.toUpperCase(xmlName.charAt(0))+xmlName.substring(1);
 				String javaName = getPrefixName()+upperName;
 				String javaTag = getJavaTag(xmlName);
 				String name = getPrefixName()+upperName;
 
 				businessObject.getAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 				if(field != null)
 					field.getXmlAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 				else
 					((ResponseBusinessObject) businessObject).getXmlAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 			}
 		}
 	}
 
 	private void processFieldAttributes(final Attributes attributes, Field f) {
 		
 		for (int i = 0; i < attributes.getLength(); i++) {
 			
 			if(!attributes.getQName(i).startsWith(DataLibLabels.XML_PREFIX)){
 				ParameterType typeP = getParameterType(attributes.getValue(i));
 				String xmlName = attributes.getQName(i);
 				String upperName = Character.toUpperCase(xmlName.charAt(0))+xmlName.substring(1);
 				String javaName = getPrefixName()+upperName;
 				String javaTag = getJavaTag(xmlName);
 				String name = getPrefixName()+upperName;
 				bo.getAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 				field.getXmlAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 				f.getXmlAttributes().add(DataLibGenerator.createField(modelFactory, name, typeP, "", "", xmlName, javaName, javaTag, new BigInteger("-1"), null, bo, null));
 			}
 		}
 	}
 
 	private String getJavaName(String name) {
 		
 //		javaName = null;
 //		while(javaName == null){
 //			if(javaNameList.contains(name));			
 //		}
 		
 		return name;
 	}
 
 	private String getJavaTag(String name) {
 		
 		StringBuilder b = new StringBuilder();
 
 		int size = javaTagStack.size();
 		for (int i = 0; i < size; i++) {
 			b.append(javaTagStack.get(i).toUpperCase());
 			if(i<=size-2)
 			b.append("_");
 		}		
 		
 		return b.toString();
 	}
 	
 	
 }
 
 
