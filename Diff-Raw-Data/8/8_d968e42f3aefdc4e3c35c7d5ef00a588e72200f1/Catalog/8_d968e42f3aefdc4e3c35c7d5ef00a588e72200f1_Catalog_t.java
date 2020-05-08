 /*
 * $Id: Catalog.java,v 1.20 2007/05/22 22:18:08 vpapad Exp $
  *  
  */
 
 package niagara.connection_server;
 import java.util.*;
 import java.io.*;
 
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 
 import org.apache.xml.serialize.XMLSerializer;
 
 import niagara.logical.DoubleDomain;
 import niagara.logical.FileScanSpec;
 import niagara.logical.IntDomain;
 import niagara.logical.NodeDomain;
 import niagara.logical.Variable;
 import niagara.ndom.*;
 import niagara.utils.*;
 
 import niagara.optimizer.Plan;
 import niagara.optimizer.colombia.Attrs;
 import niagara.optimizer.colombia.ICatalog;
 import niagara.optimizer.colombia.LogicalProperty;
 import niagara.optimizer.rules.ConstructedRule;
 import niagara.optimizer.rules.CustomRule;
 import niagara.optimizer.rules.SimpleRule;
 import niagara.query_engine.Instrumentable;
 
 public class Catalog implements ICatalog {
     // This primitive Catalog maintains a mapping from URNs to URLs,
     // or location of servers that know to resolve the URNs
     
     /** Configuration parameters */
     private HashMap configParams;
 
     /** All the known resource names */
     private HashSet resourceNames;
     /** Resources to local filenames */
     private Hashtable urn2local;
     /** Resources to servers that can resolve them */
     private Hashtable urn2resolvers;
     /** Resources to URLs */
     private Hashtable urn2urls;
 
     /** Colombia cost model parameters */
     private HashMap parameters;
 
     /** Cache for double parameters lookups */
     private HashMap doubleParams;
     /** Cache for int parameters lookups */
     private HashMap intParams;
 
     /** Prepared plans */
     private int preparedPlanID = 0;
     private HashMap<String, Plan> preparedPlans = new HashMap<String, Plan>();
     private Stack<String> orderedPlanIDs = new Stack<String>();
     /** Registry for instrumented operators */
     private HashMap<String, HashMap<String, Instrumentable>> planIDs2operators = new HashMap<String, HashMap<String, Instrumentable>>();
     private HashMap<String, ServerQueryInfo> runningPlans = new HashMap<String, ServerQueryInfo>();
     
     // Operator name <-> class mapping
     private HashMap name2class;
     private HashMap class2name;
 
     private HashMap rulesets;
 
     /** The catalog file name */
     private String filename;
 
     /** The catalog DOM document */
     private Document document;
 
     // Logical operator -> Array of Physical operator classes
     private HashMap logical2physical;
 
     // Resource name -> its logical properties
     private HashMap logicalProperties;
 
     /** Registered streams */
     private HashMap<String, FileScanSpec> registeredStreams;
     private HashMap<String, HashMap<String, Variable>> streamSchemas;
     
     public String getConfigParam(String name) {
         return (String) configParams.get(name);
     }
 
     public int getIntConfigParam(String name) {
         return Integer.parseInt((String) configParams.get(name));
     }
 
     public boolean getBooleanConfigParam(String name) {
 	return Boolean.parseBoolean((String) configParams.get(name));
     }
 
     public void addResolver(String urn, String resolver) {
         if (!urn2resolvers.containsKey(urn)) {
             urn2resolvers.put(urn, new Vector());
         }
         Vector resolvers = (Vector) urn2resolvers.get(urn);
         resolvers.add(resolver);
         resourceNames.add(urn);
     }
 
     /** Get an appropriate file name to store data for the given URN. 
      * (Should only be called from the data manager.) */
     public String getNewFileName(String urn) {
         return urn.hashCode() + "." + System.currentTimeMillis();
     }
 
     /** Register the fact that we have stored the parsed contents of 
      * <code>urn</code> locally. (Should only be called from the data 
      * manager.) */
     public void addLocal(String urn, String filename) {
         urn2local.put(urn, filename);
         resourceNames.add(urn);
     }
     
     public void addURL(String urn, String url) {
         if (!urn2urls.containsKey(urn)) {
             urn2urls.put(urn, new Vector());
         }
         Vector urls = (Vector) urn2urls.get(urn);
         urls.add(url);
         resourceNames.add(urn);
     }
 
     public boolean isLocallyResolvable(String urn) {
         return urn2local.containsKey(urn) || urn2urls.containsKey(urn);
     }
 
     public Vector getURL(String urn) {
         return (Vector) urn2urls.get(urn);
     }
 
     public String getFile(String urn) {
         return (String) urn2local.get(urn);
     }
 
     public Vector getResolvers(String urn) {
         return (Vector) urn2resolvers.get(urn);
     }
 
     public Hashtable getResolvers(Vector urns) {
         Hashtable resolver2urns = new Hashtable();
         for (int i = 0; i < urns.size(); i++) {
             String urn = (String) urns.get(i);
             Vector resolvers = (Vector) urn2resolvers.get(urn);
             if (resolvers == null)
                 continue;
             for (int j = 0; j < resolvers.size(); j++) {
                 String resolver = (String) resolvers.get(j);
                 if (!resolver2urns.containsKey(resolver)) {
                     resolver2urns.put(resolver, new Vector());
                 }
                 Vector resolvable = (Vector) resolver2urns.get(resolver);
                 resolvable.add(urn);
             }
         }
         return resolver2urns;
     }
 
     public Catalog(String filename) {
         configParams = new HashMap();
         resourceNames = new HashSet();
         urn2local = new Hashtable();
         urn2urls = new Hashtable();
         urn2resolvers = new Hashtable();
         parameters = new HashMap();
         name2class = new HashMap();
         class2name = new HashMap();
         rulesets = new HashMap();
         logical2physical = new HashMap();
         logicalProperties = new HashMap();
         registeredStreams = new HashMap<String, FileScanSpec>();
         streamSchemas = new HashMap<String, HashMap<String, Variable>>();
         
         
         this.filename = filename;
 
         // parse the catalog file
         niagara.ndom.DOMParser parser = DOMFactory.newParser();
 
         InputSource is = null;
         try {
             is = new InputSource(new FileInputStream(filename));
             parser.parse(is);
 
             // Get document
             document = parser.getDocument();
 
             // Get root element
             Element root = document.getDocumentElement();
 
             System.err.println("Parsing catalog...");
             loadConfiguration(root);
             loadOperators(root);
             loadResources(root);
             loadParameters(root, "costmodel");
             loadParameters(root, "config");
             loadRules(root);
             loadRegisteredStreams(root);
         } catch (FileNotFoundException e) {
             throw new ConfigurationError(
                 "Catalog file not found: " + e.getMessage());
         } catch (org.xml.sax.SAXException se) {
             throw new PEException(
                 "Error parsing catalog file " + se.getMessage());
         } catch (IOException ioe) {
             throw new PEException(
                 "Error reading catalog file " + ioe.getMessage());
         }
     }
 
     void loadConfiguration(Element root) {
         NodeList nl = root.getElementsByTagName("configuration");
         if (nl.getLength() != 1)
             confError("The catalog must contain exactly one <configuration> element");
         Element conf= (Element) nl.item(0);
         nl = conf.getElementsByTagName("param");
         for (int i = 0; i < nl.getLength(); i++) {
             Element param = (Element) nl.item(i);
             configParams.put(param.getAttribute("name"), param.getAttribute("value"));
         } 
     }
 
     void loadOperators(Element root) {
         NodeList nl = root.getElementsByTagName("operators");
         if (nl.getLength() != 1)
             confError("The catalog must contain exactly one <operators> element");
         Node cm = nl.item(0);
 
         nl = cm.getChildNodes();
 
         // Populate the operator hashmaps
         for (int i = 0; i < nl.getLength(); i++) {
             Node n = nl.item(i);
             if (n.getNodeType() != Node.ELEMENT_NODE)
                 continue;
             Element e = (Element) n;
             String name = mustHaveAttribute(e, "name");
             String className = mustHaveAttribute(e, "class");
             Class opClass = null;
             try {
                 opClass = Class.forName(className);
             } catch (ClassNotFoundException cnfe) {
                 confError("Could not find class: " + className);
             }
             name2class.put(name, opClass);
             class2name.put(opClass, name);
 
             // Physical ops with no logical counterpart
             if (e.getTagName().equals("physical"))
                 continue;
 
             ArrayList al = new ArrayList();
             NodeList physical = e.getChildNodes();
             for (int j = 0; j < physical.getLength(); j++) {
                 Node m = physical.item(j);
                 if (m.getNodeType() != Node.ELEMENT_NODE)
                     continue;
                 Element elt = (Element) m;
                 String physName = mustHaveAttribute(elt, "name");
                 String physicalClassName = mustHaveAttribute(elt, "class");
                 try {
                     Class c = Class.forName(physicalClassName);
                     al.add(c);
                     class2name.put(c, physName);
                     name2class.put(physName, c);
                 } catch (ClassNotFoundException exc) {
                     confError("Could not find class: " + physicalClassName);
                 }
             }
             Class[] implementations = new Class[al.size()];
             for (int k = 0; k < al.size(); k++)
                 implementations[k] = (Class) al.get(k);
             logical2physical.put(className, implementations);
         }
         NiagraServer.info("Loaded " + name2class.size() + " query operators");
     }
 
     void loadResources(Element root) {
         NodeList resources = root.getElementsByTagName("resource");
         for (int i = 0; i < resources.getLength(); i++) {
             Element resource = (Element) resources.item(i);
             String urn = resource.getAttribute("name");
             resourceNames.add(urn);
 
             NodeList urls = resource.getElementsByTagName("url");
             for (int j = 0; j < urls.getLength(); j++) {
                 Element url = (Element) urls.item(j);
                 String location = url.getAttribute("location");
                 if (!urn2urls.containsKey(urn)) {
                     urn2urls.put(urn, new Vector());
                 }
                 Vector v = (Vector) urn2urls.get(urn);
                 v.add(location);
             }
 
             NodeList resolvers = resource.getElementsByTagName("resolver");
             for (int j = 0; j < resolvers.getLength(); j++) {
                 Element resolver = (Element) resolvers.item(j);
                 String location = resolver.getAttribute("location");
                 if (!urn2resolvers.containsKey(urn)) {
                     urn2resolvers.put(urn, new Vector());
                 }
                 Vector v = (Vector) urn2resolvers.get(urn);
                 v.add(location);
             }
 
             NodeList files = resource.getElementsByTagName("file");
             int len = files.getLength();
             if (len > 1)
                 confError("Resource " + urn + " has more than one local file");
 
             if (len == 1) {
                 Element file = (Element) files.item(0);
                 String location = file.getAttribute("location");
                 urn2local.put(urn, location);
             }
 
             boolean resolvable =
                 (files.getLength() > 0 || urls.getLength() > 0);
             int size;
             if (len > 0)
                 size = 1;
             // XXX vpapad: here we must grab the statistics from the file
             else
                 size = urls.getLength();
             LogicalProperty lp =
                 new LogicalProperty(
                     size,
                     new Attrs(new Variable(urn, NodeDomain.getDOMNode())),
                     resolvable);
             logicalProperties.put(urn, lp);
         }
     }
 
     public void loadRules(Element root) {
         NodeList rs = root.getElementsByTagName("ruleset");
         if (rs.getLength() < 1) {
             confError("The catalog must contain at least one <ruleset> element");
         }
         for (int j = 0; j < rs.getLength(); j++) {
             Element ruleList = (Element) rs.item(j);
             String name = ruleList.getAttribute("name");
             ArrayList rules = new ArrayList();
             NodeList nl = ruleList.getChildNodes();
             for (int i = 0; i < nl.getLength(); i++) {
                 Node n = nl.item(i);
                 if (n.getNodeType() != Node.ELEMENT_NODE)
                     continue;
                 Element e = (Element) n;
                 if (!e.getTagName().equals("rule"))
                     confError("The rules list must contain only <rule> elements");
                 String typeName = e.getAttribute("type");
                 if (typeName.equals("simple"))
                     rules.add(SimpleRule.fromXML(e, this));
                 else if (typeName.equals("custom")) {
                     rules.add(CustomRule.fromXML(e, this));
                 } else if (typeName.equals("constructed")) {
                     rules.add(ConstructedRule.fromXML(e, this));
                 }
             }
             rulesets.put(name, rules);
             NiagraServer.info(
                 "Loaded " + rules.size() + " optimizer rules for the " + name);
         }
     }
 
     public void loadRegisteredStreams(Element root) {
         NodeList nl = root.getElementsByTagName("streams");
         if (nl.getLength() == 0)
             return;
         if (nl.getLength() > 1)
             confError("The catalog must contain at most one <streams> element");
         Node cm = nl.item(0);
         nl = cm.getChildNodes();
         
         for (int i = 0; i < nl.getLength(); i++) {
             Node n = nl.item(i);
             if (n.getNodeType() != Node.ELEMENT_NODE)
                 continue;
             Element e = (Element) n;
             String streamName = mustHaveAttribute(e, "name");
             String filename = mustHaveAttribute(e, "filename");
             boolean isStream = 
                 mustHaveAttribute(e, "isStream").equalsIgnoreCase("yes");
 	    int delay = Integer.parseInt(mustHaveAttribute(e, "delay"));
             registeredStreams.put(streamName, 
                     new FileScanSpec(filename, isStream, delay));
             NodeList vars = e.getChildNodes();
             streamSchemas.put(streamName, new HashMap<String, Variable>());
             for (int j = 0; j < vars.getLength(); j++) {
                 Node v = vars.item(j);
                 if (v.getNodeType() != Node.ELEMENT_NODE)
                     continue;
                 Element ev = (Element) v;
                 Variable var = null;
                 String name = ev.getAttribute("name");
                 String type = ev.getAttribute("type");
                 if (type.equals("int"))
                     var = new Variable(name, IntDomain.getDomain());
                 else if (type.equals("double"))
                     var = new Variable(name, DoubleDomain.getDomain());
                 else
                     confError("Unknown attribute type: '" + type + "'");
                 streamSchemas.get(streamName).put(name, var);
             }
         }
         NiagraServer.info(
                 "Registered " + registeredStreams.size() + " streams");
     }
     
     public static void confError(String msg) {
         throw new ConfigurationError(msg);
     }
 
     /**
      * Find the logical properties for a resource
      */
     public LogicalProperty getLogProp(String resourceName) {
         if (logicalProperties.containsKey(resourceName))
             return (LogicalProperty) logicalProperties.get(resourceName);
         // If we don't know anything about the resource,
         // treat URLs as local resources, unknown URNs as remote
         float card;
         boolean local;
 
         if (resourceName.startsWith("http:")) {
             card = 1;
             local = true;
         } else {
             card = 0;
             local = false;
         }
         return new LogicalProperty(
             card,
             new Attrs(new Variable(resourceName, NodeDomain.getDOMNode())),
             local);
     }
 
     void loadParameters(Element root, String category) {
         NodeList nl = root.getElementsByTagName(category);
         if (nl.getLength() != 1) {
             throw new PEException(
                 "The catalog must contain exactly one <"
                     + category
                     + "> element");
         }
         Node cm = nl.item(0);
 
         nl = cm.getChildNodes();
 
         // Populate the cost model HashMap
         for (int i = 0; i < nl.getLength(); i++) {
             Node n = nl.item(i);
             if (n.getNodeType() != Node.ELEMENT_NODE)
                 continue;
             Element e = (Element) n;
             parameters.put(e.getTagName(), e.getAttribute("value"));
             intParams = new HashMap();
             doubleParams = new HashMap();
         }
     }
 
     public String getOperatorName(Class operatorClass) {
         return (String) class2name.get(operatorClass);
     }
 
     public Class getOperatorClass(String operatorName) {
         return (Class) name2class.get(operatorName);
     }
 
     public Class[] getPhysical(String logicalClass) {
         return (Class[]) logical2physical.get(logicalClass);
     }
 
     public ArrayList getRules(String ruleSetName) {
         return (ArrayList) rulesets.get(ruleSetName);
     }
 
     public String getCostModelParameter(String param) {
         return (String) parameters.get(param);
     }
 
     public int getInt(String param) {
         Integer val;
         if (intParams.containsKey(param)) {
             val = (Integer) intParams.get(param);
             return val.intValue();
         } else {
             String s = (String) parameters.get(param);
             val = Integer.valueOf(s);
             intParams.put(param, val);
             return val.intValue();
         }
     }
 
     public double getDouble(String param) {
         Double val;
         if (doubleParams.containsKey(param)) {
             val = (Double) doubleParams.get(param);
         } else {
             String s = (String) parameters.get(param);
             val = Double.valueOf(s);
             doubleParams.put(param, val);
         }
         return val.doubleValue();
     }
 
     private String mustHaveAttribute(Element e, String attrName) {
         String value = e.getAttribute(attrName);
         if (value.length() == 0)
             confError("Missing attribute: " + attrName);
         return value;
     }
 
     public static void cerr(String msg) {
         System.err.println(msg);
     }
 
     public void shutdown() {
         Element root = document.getDocumentElement();
         NodeList resourcesList = root.getElementsByTagName("resources");
         Element oldResources = (Element) resourcesList.item(0);
         Element resources = document.createElement("resources");
 
         Iterator keys = resourceNames.iterator();
         while (keys.hasNext()) {
             String urn = (String) keys.next();
             Element r = document.createElement("resource");
             r.setAttribute("name", urn);
             r.appendChild(document.createTextNode("\n"));
             if (urn2local.containsKey(urn)) {
                 Element l = document.createElement("file");
                 l.setAttribute("location", (String) urn2local.get(urn));
                 r.appendChild(l);
                 r.appendChild(document.createTextNode("\n"));
             }
             if (urn2urls.containsKey(urn)) {
                 Vector v = (Vector) urn2urls.get(urn);
                 for (int i = 0; i < v.size(); i++) {
                     Element l = document.createElement("url");
                     l.setAttribute("location", (String) v.get(i));
                     r.appendChild(l);
                     r.appendChild(document.createTextNode("\n"));
                 }
             }
             if (urn2resolvers.containsKey(urn)) {
                 Vector v = (Vector) urn2resolvers.get(urn);
                 for (int i = 0; i < v.size(); i++) {
                     Element l = document.createElement("url");
                     l.setAttribute("location", (String) v.get(i));
                     r.appendChild(l);
                     r.appendChild(document.createTextNode("\n"));
                 }
             }
             resources.appendChild(document.createTextNode("\n"));
             resources.appendChild(r);
         }
         resources.appendChild(document.createTextNode("\n"));
         root.replaceChild(resources, oldResources);
 
        // Save resources
 	if (getBooleanConfigParam("update catalog file on shutdown")) {
 	    try {
 		// XXX vpapad: yeah, I know, referencing apache directly sucks
 		XMLSerializer serializer = new XMLSerializer (new FileWriter(filename), null);
 		serializer.asDOMSerializer();
 		serializer.serialize(document);
 	    } catch (FileNotFoundException e) {
 		cerr("Catalog file does not exist.");
 	    } catch (IOException e) {
 		cerr("Problems encountered while saving catalog file" + e.getMessage());
 	    }
 	}
     }
 
     // for testing
     public void dumpStats() {
         cerr("Locally resolvable URNs:");
         Enumeration e = urn2urls.keys();
         while (e.hasMoreElements()) {
             String urn = (String) e.nextElement();
             cerr("\t" + urn + ":");
             Vector urls = (Vector) urn2urls.get(urn);
             for (int i = 0; i < urls.size(); i++) {
                 cerr("\t\t" + urls.get(i));
             }
         }
         Vector allResolvableURNs = new Vector();
         e = urn2resolvers.keys();
         while (e.hasMoreElements()) {
             String urn = (String) e.nextElement();
             allResolvableURNs.add(urn);
         }
 
         Hashtable resolver2urns = getResolvers(allResolvableURNs);
 
         cerr("Resolvers for URNs:");
         Enumeration resolvers = resolver2urns.keys();
         while (resolvers.hasMoreElements()) {
             String resolver = (String) resolvers.nextElement();
             cerr(resolver + ": ");
             Vector resolvables = (Vector) resolver2urns.get(resolver);
             for (int i = 0; i < resolvables.size(); i++) {
                 cerr("\t" + resolvables.get(i));
             }
         }
     }
 
     public void dumpCostModel() {
         cerr("Cost model parameters: ");
         Iterator keys = parameters.keySet().iterator();
         while (keys.hasNext()) {
             String k = (String) keys.next();
             cerr(k + " = " + parameters.get(k));
         }
     }
 
     public String storePreparedPlan(Plan plan) 
     throws InvalidPlanException {
     	synchronized (preparedPlans) {
     		preparedPlanID++;
     		String planID = plan.getPlanID();
     		if(preparedPlans.containsKey(planID))
     			throw new InvalidPlanException("PlanID Already Exists");
     		if (planID == null || planID.length() == 0)
     			planID = String.valueOf(preparedPlanID);
     		preparedPlans.put(planID, plan);
     		orderedPlanIDs.push(planID);
     		return planID;
     	}
     }
     
     public Instrumentable getOperator(String planID, String operatorName) {
     	synchronized (preparedPlans) {
     		HashMap<String, Instrumentable> hm = planIDs2operators.get(planID);
     		if (hm == null) {
     			if (planID.equals("*")) {
     				if (orderedPlanIDs.isEmpty())
     					return null;
     				if (orderedPlanIDs.size() > 1)
     					NiagraServer.warning("Querying for planid=* when multiple plans are prepared");
     				return getOperator(orderedPlanIDs.peek(), operatorName);
     			}
     			return null;
     		}
     		return hm.get(operatorName);
     	}
     }
     
     public String instrumentPlan(String planID) {
         synchronized (preparedPlans) {
             assert preparedPlans.containsKey(planID);
             Plan plan = preparedPlans.get(planID);
             HashMap<String, Instrumentable> operatorRegistry = new HashMap<String, Instrumentable>();
             String planAsXML = plan.planToXML(planID, operatorRegistry);
             planIDs2operators.put(planID, operatorRegistry);
             return planAsXML;
         }
     }
     
     public Plan getPreparedPlan(String planID) {
         // XXX vpapad: Remember that prepared plans cannot 
         // currently be executed more than once
         synchronized (preparedPlans) {
             Plan plan = preparedPlans.get(planID);
            if (plan == null && planID.equals("*") && !orderedPlanIDs.empty()) {
             		return getPreparedPlan(orderedPlanIDs.peek());
             }
             return plan;
         }
     }
     
     public ServerQueryInfo getQueryInfo(String planID) {
     	synchronized(preparedPlans) {
     		return runningPlans.get(planID);
     	}
     }
 
     public void registerQueryInfo(String planID, ServerQueryInfo sqi) {
     	synchronized(preparedPlans) {
     		runningPlans.put(planID, sqi);
     	}
     }
     
     public void removePreparedPlan(String planID) {
         synchronized (preparedPlans) {
             preparedPlans.remove(planID);
             orderedPlanIDs.remove(planID);
             planIDs2operators.remove(planID);
             runningPlans.remove(planID);
         }
     }
     
     public boolean isActive(String planID) {
         synchronized(preparedPlans) {
         	boolean active = planIDs2operators.containsKey(planID); 
         	if (!active && planID.equals("*"))
        		return (!orderedPlanIDs.isEmpty()) && isActive(orderedPlanIDs.peek());
             return active; 
         }
     }
     
     public FileScanSpec getRegisteredStream(String streamName) {
         return registeredStreams.get(streamName);
     }
     
     public Variable getStreamAttribute(String streamName, String attrName) {
         return streamSchemas.get(streamName).get(attrName);
     }
 }
