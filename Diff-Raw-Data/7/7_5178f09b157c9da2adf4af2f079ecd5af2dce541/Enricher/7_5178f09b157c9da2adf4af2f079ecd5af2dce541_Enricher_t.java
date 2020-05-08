 package org.rulez.magwas.styledhtml;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import uk.ac.bolton.archimate.editor.utils.HTMLUtils;
 import uk.ac.bolton.archimate.model.IArchimateModel;
 
 public class Enricher{
 
 	private Document policy = null;
 	private Document xml = null;
 	private XPath xpath;
 	private VarResolver vars;
 	private NSResolver nss;
 	private HashMap<Element,String> associations = new HashMap<Element, String>();
 	private EventLog log;
 	private IArchimateModel model;
 	
 	
 	private Enricher(IArchimateModel themodel, Document infile, File policyfile, EventLog elog) {
 		model = themodel;
 		log = elog;
 		log.issueInfo("starting enricher", EventLog.now());
 		xml=infile;
 		xpath = XPathFactory.newInstance().newXPath(); 
 		vars = new VarResolver();
 		nss = new NSResolver();
 		xpath.setXPathVariableResolver(vars);
 		nss.put("archimate","http://www.bolton.ac.uk/archimate");
 		xpath.setNamespaceContext(nss);
 		//FIXME we use the same xpath for both the policy and the archi file: if namespace clashing occurs, they should be separated
 		if ((null != policyfile) && policyfile.exists()) {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db;
 			try {
 				db = dbf.newDocumentBuilder();
 				policy = db.parse(policyfile);
 			} catch (Exception e) {
 				Widgets.tellProblem("Problem loading policy file", e.toString());
 	        	log.printStackTrace(e);
 			}
 		}
 		log.issueInfo("enricher done", EventLog.now());
 	}
 	
 	public static void enrichXML(IArchimateModel model, Document infile, File policyfile, EventLog log) {
 		Enricher er = new Enricher(model,infile,policyfile,log);
 		er.enrichDocs();
 		er.enrichXML(infile);
 		er.associateObjects();
 		er.addDefaultSubelements();
 	}
 	
 	private void associateObjects() {
 		for(Entry<Element,String> e: associations.entrySet()) {
 			this.associateObjectClass(e.getKey(),e.getValue());
 		}
 	}
 	
 	private void enrichDocs() {
     	//enrich the documentation-like parts in the xml
     	NodeList nl = xml.getElementsByTagName("documentation");
     	for(int i=0;i<nl.getLength();i++) {
     		Element n = (Element) nl.item(i);
     		parseCharsAndLinks(n);
     	}
     	NodeList pl = xml.getElementsByTagName("purpose");
     	for(int j=0;j<pl.getLength();j++) {
     		Element k = (Element) pl.item(j);
     		parseCharsAndLinks(k);
     	}
 
 	}
     private void enrichXML(Node n) {
     	NodeList nl = n.getChildNodes();
     	int ll=nl.getLength();
     	for(int i=0;i<ll;i++) {
     		Node m = nl.item(i);
            	if (Node.ELEMENT_NODE == m.getNodeType()) {
            		Element e = (Element)m;
            	    enrichXML(e);
         		enrichElement((Element) e);    		
            	}
     	}
     }
     
     private void enrichElement(Element m){
     	// copies element node to an identical node which have nodename of the xsi:type attribute
     	String typename = m.getAttribute("xsi:type");
     	if(m.getNodeName() == "folder") {
     		typename = "archimate:Folder";
 		}
     	if("" != typename ) {
     		
     		xml.renameNode(m, namespaceForType(typename), typename);
     		m.removeAttribute("xsi:type");
     	}
     	
     	List<Element> props = getChildElementsByTagName(m,"property");
     	int l = props.size();
     	for(int i=0;i<l;i++) {
     		Element p = (Element) props.get(i);
     		if ( m != p.getParentNode()) {
     			continue;
     		}
     		String key = p.getAttribute("key");
     		String value = p.getAttribute("value");
     		//System.out.println("property("+key+")="+value);
     		if(key.equals("objectClass")) {
     			//System.out.println("creating "+value);
     			getorCreateObjectClass(m,value);
     		}
     		if(key.contains(":")) {
     			String[] k = key.split(":",2);
     			createSubElement(m,k[0],k[1], value);
     		}
     		if(key.equals("associatedObjectClass")) {
     			this.associations.put(m,value);
     		}
     	}
     }
 
     private void associateObjectClass(Element group, String objectclass) {
     	
     	String ocpath= "//*[@id=//archimate:Group[@id=$thisid]//archimate:DiagramObject/@archimateElement]";
     	String thisid=group.getAttribute("id");
     	vars.put("thisid", thisid);
     	//System.out.println("Associating objects with "+objectclass);
     	NodeList nl=null;
     	try {
 			nl=(NodeList) xpath.evaluate(ocpath, group, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			log.printStackTrace(e);
 			throw new RuntimeException("problem in association");
 		}
 		int l = nl.getLength();
     	//System.out.println(" found "+l+" objects");
 		for(int i=0;i<l;i++) {
 			//System.out.println("id="+nl.item(i).getNodeValue());
 			Element e = (Element) nl.item(i);
 			//System.out.println(" associating "+e);
 			getorCreateObjectClass(e, objectclass);
 		}
     }
     private static List<Element> getChildElementsByTagName(Element e, String name) {
     	NodeList nl = e.getChildNodes();
     	int l = nl.getLength();
     	List<Element> out = new ArrayList<Element>();
     	for(int i=0;i<l;i++) {
     		Node c = nl.item(i);
            	if (Node.ELEMENT_NODE == c.getNodeType()) {
            		if(c.getNodeName().equals(name)) {
            			out.add((Element) c);
            		}
            	}    		
     	}
     	return out;
     }
     
     
     
     private void applyPolicyForElement(Element node, Element objectclass, String ancestor) {
     	/*
     	 *    applyPolicyForElement(node,objectclass)
     	 *    - for all ancestors for the objectclass
     	 *      recursively add all properties of the ancestor:
     	 *        - for all ancestors of the objectclass
     	 *          if ancestor is not an archi class (starts with 'archimate:')
     	 *           applyPolicyForElement(node,ancestor)
     	 * 	  - for all properties in policy
     	 *     - if the property does not exist in node
     	 *       addPropertyToElement(node,property)
     	 *       if no defaults have given back anything and minOccurs != 0
     	 *         issue a warning
     	 */
      	//System.out.println("applyPolicyForElement("+node.getAttribute("parentid")+","+objectclass.getAttribute("name")+","+ancestor+")");
     	NodeList ancestors = objectclass.getElementsByTagName("ancestor");
     	int k = ancestors.getLength();
     	for(int j=0;j<k;j++) {
     		String ancestorname=((Element)ancestors.item(j)).getAttribute("class");
     		if(!ancestorname.startsWith("archimate:")) {
         		Element occ=getPolicyFor(ancestorname);
         		if(null == occ) {
         			log.issueError("no policy for ancestor "+ancestorname,"for objectClass"+objectclass.getAttribute("name"));
         			return;
         		}
     			applyPolicyForElement(node, occ,ancestorname);
     		}
     	}
     	NodeList pl = objectclass.getElementsByTagName("property");
     	int l = pl.getLength();
     	for(int i=0;i<l;i++) {
     		Element property = (Element) pl.item(i);
     		String propname = property.getAttribute("name");
     		//System.out.println(" looking at "+propname);
 			addPropertyToElement(node,property,propname,ancestor);
     	}
     }
 
     private Element defaultItem(NodeList dl, int n) {
     	int l = dl.getLength();
     	for(int i=0;i<l;i++) {
     		Element d = (Element) dl.item(i);
     		if(Integer.parseInt(d.getAttribute("order")) == n) {
     			return d;
     		}
     	}
     	return null;
     }
     
     private void addPropertyToElement(Element node, Element property, String propname, String ancestor) {
     	if(null != ancestor) {
     		/*
     		 * try to copy ../[ancestor]/[propname] here
     		 */
     		//System.out.println("  copying "+propname+" from "+ancestor);
     		String path="../"+ancestor+"/"+propname;
     		NodeList defaults;
 			try {
 				defaults = (NodeList) xpath.evaluate(path,node,XPathConstants.NODESET);
 			} catch (XPathExpressionException e) {
 				log.printStackTrace(e);
 				throw new RuntimeException("bad path"+path);
 			}
 			int k=defaults.getLength();
 			//System.out.println("  path='"+path+"' items:"+k);
 			for(int j=0;j<k;j++) {
 				String v = ((Element)defaults.item(j)).getTextContent();
 				//System.out.println("  inserting to "+node.getNodeName()+"."+node.getAttribute("parentid")+"("+propname+") from "+ancestor+":"+v);
 				Element e = xml.createElement(propname);
 				e.setTextContent(v);
 				node.appendChild(e);
 			}
 			//return;
     	}
     	NodeList pl = property.getElementsByTagName("default");
     	int i = 0;
     	Element defitem;
     	do {
     		defitem = defaultItem(pl,i);
     		//System.out.println(" defitem=" + defitem);
     		if(null != defitem) {
     			//System.out.println("trying it out");
     			tryDefaultForProperty(node,propname,defitem);
     		}
     		i++;
     	} while (null != defitem);
     	int len = node.getElementsByTagName(propname).getLength();
     	String mo = property.getAttribute("minOccurs");
       	int minOccurs;
     	if("".equals(mo)) {
     		minOccurs = 1;
     	} else {
     		minOccurs = Integer.parseInt(mo);
     	}
     	if(len<minOccurs) {
         	log.issueError(model,(Element) node.getParentNode(),"Too few ("+len+"<"+minOccurs+") occurence of "+propname+ " in "+node.getTagName(),helpForProperty(property));    		
     	}
     	String Mo = property.getAttribute("maxOccurs");
     	if(!"".equals(Mo)) {
         	int maxOccurs = Integer.parseInt(Mo);
         	if(maxOccurs<len) {
             	log.issueError(model,(Element) node.getParentNode(),"Too much ("+len+">"+maxOccurs+") occurence of "+propname+ " in "+node.getTagName(),helpForProperty(property));        		
         	}
     	}
     }
     
     private String helpForProperty(Element property) {
     	try {
 			String propdesc = xpath.evaluate("./description", property);
 			NodeList defaults = (NodeList) xpath.evaluate("./default/description",property,XPathConstants.NODESET);
 			int l = defaults.getLength();
 			for(int i=0;i<l;i++) {
 				Element d = (Element) defaults.item(i);
 				propdesc += "\n"+d.getTextContent()+" ("+((Element)d.getParentNode()).getAttribute("select")+")";
 			}
 			return propdesc;
 		} catch (XPathExpressionException e) {
 			log.printStackTrace(e);
 			throw new RuntimeException("problem with xpath");
 		}
     }
     
      private boolean tryDefaultForProperty(Element node, String propname, Element defitem) {
     	int definedproperties = node.getElementsByTagName(propname).getLength();
     	if((definedproperties>0) && (!"true".equals(defitem.getAttribute("always")))) {
     		//System.out.println("already have this");
     		return true;
     	}
     	String path = defitem.getAttribute("select");
     	boolean multi = false;
     	if("true".equals(defitem.getAttribute("multi"))) {
     		multi = true;
     	}
 		vars.put("id", node.getAttribute("parentid"));
 		
 		//System.out.println("propname="+propname);
 		//System.out.println("id="+vars.get("id"));
 		//System.out.println("select="+path+", multi="+multi);
 		try {
 			if(multi) {
 				NodeList result = (NodeList) xpath.evaluate(path, node, XPathConstants.NODESET);
 				int l=result.getLength();
 				if(l==0) {
 					//System.out.println("empty nodelist");
 					return false;
 				}
 				for(int i=0;i<l;i++) {
 					//System.out.println("inserting "+propname+" using path "+path);
 					Node n = result.item(i);
 					String val = n.getTextContent();
 					if(null == val) {
 						//System.out.println("empty nodevalue");
 						return false;
 					}
 					//System.out.println(" value="+val);
 		    		Element prop = xml.createElement(propname);
 		    		node.appendChild(prop);
 			    	prop.setTextContent(val);
 				}
 				return true;				
 			} else {
 				String result = (String) xpath.evaluate(path, node, XPathConstants.STRING);
 				//System.out.println("result="+result);
 				if("".equals(result)) {
 					return false;
 				}
 				Element prop = xml.createElement(propname);
 				node.appendChild(prop);
 				prop.setTextContent(result);
 				return true;
 			}
 		} catch (XPathExpressionException e) {
 			// issue warning, with error message compiled from policy
 			log.printStackTrace(e);
 		}
 		return false;
     }
     
     private void addDefaultSubelements () {
     	/*
     	 * This is how we add default sub elements
     	 * 
     	 * for all objectclasses in policy 
     	 *  - for all nodes belonging to that objectclass
     	 *    applyPolicyForElement(node,objectclass)
     	 *    - for all ancestors for the objectclass
     	 *      recursively add all properties of the ancestor:
     	 *        - for all ancestors of the objectclass
     	 *          if ancestor is not an archi class (starts with 'archimate:')
     	 *           applyPolicyForElement(node,ancestor)
     	 * 	  - for all properties in policy
     	 *     - if the property does not exist in node
     	 *       addPropertyToElement(node,property)
     	 *       - for the defaults in order
     	 *         tryDefaultForProperty(node,propertyname,default)
     	 *         - use the xpath in the 'select' attribute
     	 *           - if the xpath gives back something
     	 *             then we add a subelement for the property
     	 *             and go for the next property
     	 *       if no defaults have given back anything and minOccurs != 0
     	 *         issue a warning
     	 *  
     	 */
     	
 		if(null == policy) {
 			log.issueWarning("no policy",null);
 			return;
 		}
     	NodeList ol = policy.getElementsByTagName("objectClass");
 		if(null == ol) {
 			Widgets.tellProblem("policy problem","no objectClass");
 			return;
 		}
     	int l = ol.getLength();
     	for(int i=0;i<l;i++) {
     		Element objectclass = (Element) ol.item(i);
     		NodeList nl = xml.getElementsByTagName(objectclass.getAttribute("name"));
     		int k = nl.getLength();
     		for(int j=0;j<k;j++) {
     			Element node = (Element) nl.item(j);
     			applyPolicyForElement(node,objectclass,null);
     		}
     		
     	}
     }
     
     private Element getorCreateObjectClass(Element m, String ocname) {
     	/*
     	 * m: the archi object we are at
     	 * value: the name of the objectClass
     	 */
     	Element e = getOrCreateElement(m,ocname);
     	if("".equals(e.getAttribute("parentid"))) {
     		//we are creating it
     		e.setAttribute("parentid", m.getAttribute("id"));
     		/*
     		 * check in the policy whether the element type of m is accepted as ancestor of ocname
     		 */
     		String elementtype=m.getNodeName();
     		if ((!checkAncestry(ocname,elementtype)) && (null != policy)) {
             	log.issueError(model,m,"objectClass "+ocname+" should not be related to "+elementtype,"No such ancestor defined in policy for the objectClass");
     		}
     	}
 		return e;
     }
 
     private Element getPolicyFor(String classname) {
 		NodeList nl;
 		if (null == policy) {
 			return null;
 		}
 		try {
 			nl = (NodeList) xpath.evaluate("//objectClass", policy, XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			log.printStackTrace(e);
 			throw new RuntimeException("problem evaluating xpath");
 		}
 
     	int l = nl.getLength();
     	for(int i=0;i<l;i++) {
     		Element c = (Element) nl.item(i);
     		String ocname=c.getAttribute("name");
     		if(ocname.equals(classname)) {
     			return c;
     		}
     	}
     	//System.out.println("null getpolicyfor "+classname);
        	return null;
     }
     
     private boolean checkAncestry(String ocname, String elementtype) {
     	/*
     	 * See whether elementtype is an ancestor of ocname
     	 */
     	//System.out.println("checkAncestry("+ocname+","+elementtype+")");
     	Element policy = this.getPolicyFor(ocname);
     	if(null == policy) {
     		return false;
     	}
     	List<Element> el = getChildElementsByTagName(policy,"ancestor");
     	for(Element e: el) {
     		String ancestorname=e.getAttribute("class");
     		//System.out.println("checking "+ocname+" against "+ancestorname);
     		if(ancestorname.equals(elementtype)) {
     			return true;
     		}
     		if(checkAncestry(ancestorname,elementtype)){
     			return true;
     		}
     	}
     	return false;
     }
 
     private void createSubElement(Element m, String el, String propname, String value) {
     	Element obj = getorCreateObjectClass(m,el);
     	Element prop = getOrCreateElement(obj,propname);
     	prop.setTextContent(value);
     }
     private Element getOrCreateElement(Element m, String value) {
     	List<Element> nl = getChildElementsByTagName(m,value);
     	if(0 == nl.size()) {
     		Element e = xml.createElement(value);
     		m.appendChild(e);
     		return e;
     	}
     	if(1 == nl.size()) {
     		return nl.get(0);
         }
     	Widgets.tellProblem("property problem", "objectClass name '"+value+"' is reserved");
     	return null;
     }
     
     private static String namespaceForType(String tname) {
     	//FIXME use the policy to figure this out
     	String xmlns = tname.split(":")[0];
     	if(xmlns.equals("archimate")) {
     		return "http://www.bolton.ac.uk/archimate";
     	}
     	return "http://namespaces.local/"+xmlns;
     }
     private static void parseCharsAndLinks(Element n) {
         // Escape chars
     	Document d = n.getOwnerDocument();
     	String s = n.getTextContent();
     	n.setTextContent("");
         
         String[] ss = s.split("(\r\n|\r|\n)");
         
         for(String sss : ss) {
         	parseLinks(sss,n);
         	n.appendChild(d.createElement("br"));
         }
         
     }
     
     private static void parseLinks(String s, Node parent) {
        	Matcher matcher = HTMLUtils.HTML_LINK_PATTERN.matcher(s);
     	Document d = parent.getOwnerDocument();
     	
         int lastend=0;
         while(matcher.find(lastend)) {
             String group = matcher.group();
             String text = s.substring(lastend,matcher.start());
 
             Node txt = d.createTextNode(text);
             lastend=matcher.end();
             parent.appendChild(txt);
             Element a = d.createElement("a");
             a.setAttribute("href", group);
             Node sub = d.createTextNode(group);
             a.appendChild(sub);
             parent.appendChild(a);
         }
         if(lastend < s.length()){
         	Node txt = d.createTextNode(s.substring(lastend));
         	parent.appendChild(txt);
         }
     }
 }
 
