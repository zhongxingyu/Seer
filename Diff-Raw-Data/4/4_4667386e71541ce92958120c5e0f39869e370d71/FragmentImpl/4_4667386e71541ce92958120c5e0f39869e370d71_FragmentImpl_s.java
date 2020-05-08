 package org.xbrlapi.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.LabelResource;
 import org.xbrlapi.Locator;
 import org.xbrlapi.ReferenceResource;
 import org.xbrlapi.SimpleLink;
 import org.xbrlapi.builder.Builder;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Implements the functionality that is common to all types of XBRL fragments.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class FragmentImpl extends XMLImpl implements Fragment {
 	
     /**
      * 
      */
     private static final long serialVersionUID = 935011241451397867L;
 
     /**
      * @see Fragment#getAncestorOrSelf(String)
      */
     public Fragment getAncestorOrSelf(String type) throws XBRLException {
     	if (getType().equals(type)) return this;
     	Fragment parent = this.getParent();
     	if (parent == null) throw new XBRLException("No ancestor (or self) fragments match the given type: " + type);
         return parent.getAncestorOrSelf(type);
     }
     
     /**
      * @see Fragment#getChildren(String)
      */
     public <F extends Fragment> List<F> getChildren(String type) throws XBRLException {
         String query = "#roots#[@parentIndex='" + getIndex() + "' and @type='org.xbrlapi.impl." + type + "Impl']";
         if (type.indexOf(".") > -1) {
             query = "#roots#[@parentIndex='" + getIndex() + "' and @type='" + type + "']";
         }
         
     	List<F> fragments = getStore().<F>queryForXMLResources(query);
     	return fragments;
     }
     
     /**
      * @see Fragment#getChildren(Class)
      */
     public <F extends Fragment> List<F> getChildren(Class<?> requiredClass) throws XBRLException {
         String query = "#roots#[@parentIndex='" + getIndex() + "' and @type='" + requiredClass.getName() + "']";        
         List<F> fragments = getStore().<F>queryForXMLResources(query);
         return fragments;
     }    
     
     /**
      * @see Fragment#getChildrenIndices(String)
      */
     public Set<String> getChildrenIndices(String type) throws XBRLException {
         String query = "#roots#[@parentIndex='" + getIndex() + "' and @type='" + type + "']";
         return getStore().queryForIndices(query);
     }    
     
     /**
      * @see Fragment#getSimpleLinks()
      */
     public List<SimpleLink> getSimpleLinks() throws XBRLException {
     	return this.getStore().<SimpleLink>getChildFragments(SimpleLinkImpl.class,this.getIndex());
     }
     
     /**
      * @see Fragment#getAllChildren()
      */
     public List<Fragment> getAllChildren() throws XBRLException {
     	String xpath = "#roots#[@parentIndex='" + getIndex() + "']";
     	List<Fragment> fragments = getStore().<Fragment>queryForXMLResources(xpath);
     	return fragments;
     }
     
     /**
      * @see Fragment#getAllChildrenIndices()
      */
     public Set<String> getAllChildrenIndices() throws XBRLException {
         String xpath = "#roots#[@parentIndex='" + getIndex() + "']";
         return getStore().queryForIndices(xpath);
     }    
     
     /**
      * @see Fragment#getChild(String, int)
      */
     public Fragment getChild(String type, int index) throws XBRLException {
     	List<Fragment> children = getChildren(type);
     	if (children == null) return null;
     	if (index >= children.size()) throw new XBRLException("The index is too high.");
     	if (index < 0) throw new XBRLException("The index is too low.");
     	return children.get(index);   
     }
 
     /**
      * @see Fragment#getDataRootElement()
      */
     public Element getDataRootElement() throws XBRLException {
         Element dataContainer;
         
     	if (getBuilder() != null) {
     		dataContainer = getBuilder().getData();
     	} else {
     	    dataContainer = (Element) getMetadataRootElement().getElementsByTagNameNS(Constants.XBRLAPINamespace.toString(),Constants.FragmentDataContainerElementName).item(0);
     	}
 
     	NodeList children = dataContainer.getChildNodes();
     	for (int i=0; i< children.getLength(); i++) 
     		if (children.item(i).getNodeType() == Node.ELEMENT_NODE) return (Element) children.item(i);
     	throw new XBRLException("The data element of the fragment could not be found.");
     }
     
     /**
      * @see Fragment#isNewFragment()
      */
     public boolean isNewFragment() throws XBRLException {
     	if (getBuilder() == null) return false;
     	return getBuilder().isNewFragment();
     }
 
     /**
      * @see Fragment#getURI()
      */
     public URI getURI() throws XBRLException {
         try {
             return new URI(this.getMetaAttribute("uri"));
         } catch (URISyntaxException e) {
             throw new XBRLException(this.getMetaAttribute("uri") + " has an invalid URI syntax.");
         }
     }
 
     /**
      * @see Fragment#setURI(URI)
      */
     public void setURI(URI uri) throws XBRLException {
         this.setMetaAttribute("uri",uri.toString());
     }
     
     /**
      * @see Fragment#getReferencingLocators()
      */
     public List<Locator> getReferencingLocators() throws XBRLException {
     	
     	// Construct the Query
     	String predicate = "*/link:loc and @targetDocumentURI='"+ getURI() +"' and (";
     	NodeList xptrs = this.getMetadataRootElement().getElementsByTagNameNS(Constants.XBRLAPINamespace.toString(),"xptr");
     	for (int i=0; i<xptrs.getLength(); i++) {
     		String value = ((Element) xptrs.item(i)).getAttribute("value").trim();
     		predicate += "@targetPointerValue='" + value +"'";
     		if (i == (xptrs.getLength()-1)) 
     			predicate += ")";
     		else
     			predicate += " or ";
     	}
     	String query = "#roots#[" + predicate + "]";
     	
     	return getStore().<Locator>queryForXMLResources(query);
     }
 
     /**
      * @see Fragment#getLabels()
      */
     public List<LabelResource> getLabels() throws XBRLException {
         return getStore().getLabels(getIndex());
     }    
 
     /**
      * @see Fragment#getLabelsWithResourceRole(URI)
      */
     public List<LabelResource> getLabelsWithResourceRole(URI role) throws XBRLException {
         return getStore().getLabels(getIndex(),role);
     }
 
     /**
      * @see Fragment#getLabels(List, List)
      */
     public List<LabelResource> getLabels(List<String> languages, List<URI> resourceRoles) throws XBRLException {
 
             if (languages == null || resourceRoles == null) {
                 throw new XBRLException("Null parameters are not allowed.");
             }
 
             List<LabelResource> result;
             for (String language: languages) {
                 for (URI role: resourceRoles) {
                     result = this.getLabelsWithLanguageAndResourceRole(language,role);
                     if (! result.isEmpty()) return result;
                 }
             }
             result = this.getLabels();
             if (! result.isEmpty()) return result;
             return new Vector<LabelResource>();
 
     }
     
     /**
      * @see Fragment#getLabels(List, List, List)
      */
     public List<LabelResource> getLabels(List<String> languages, List<URI> labelRoles, List<URI> linkRoles) throws XBRLException {
 
         if (languages == null || labelRoles == null || linkRoles == null) {
             throw new XBRLException("Null parameters are not allowed.");
         }
 
         List<LabelResource> result;
         for (URI linkRole: linkRoles) {
             for (URI labelRole: labelRoles) {
                 for (String language: languages) {
                     result = this.getLabelsWithLanguageAndResourceRoleAndLinkRole(language,labelRole, linkRole);
                     if (! result.isEmpty()) return result;
                 }
             }
         }
 
         result = this.getLabels();
         if (! result.isEmpty()) return result;
         
         return new Vector<LabelResource>();
 
     }    
 
     /**
      * @see Fragment#getReferencesWithResourceRole(String)
      */
     public List<ReferenceResource> getReferencesWithResourceRole(String role) throws XBRLException {
         return getStore().getReferences(getIndex(),role);
     }
 
     /**
      * @see Fragment#getLabelsWithLanguage(String)
      */
     public List<LabelResource> getLabelsWithLanguage(String language) throws XBRLException {
         return getStore().getLabels(getIndex(),language);
     }
 
     /**
      * @see Fragment#getLabelsWithLanguageAndResourceRole(String, URI)
      */
     public List<LabelResource> getLabelsWithLanguageAndResourceRole(String language, URI role) throws XBRLException {
         return getStore().getLabels(getIndex(),role, language);
     }
     
     /**
      * @see Fragment#getLabelsWithLanguageAndResourceRole(String, URI)
      */
     public List<LabelResource> getLabelsWithLanguageAndResourceRoleAndLinkRole(String language, URI resourceRole, URI linkRole) throws XBRLException {
         return getStore().getLabels(getIndex(),linkRole, resourceRole, language);
     }    
 
     /**
      * @see Fragment#getReferences()
      */
     public List<ReferenceResource> getReferences() throws XBRLException {
         List<ReferenceResource> references = getStore().getTargets(getIndex(),null,Constants.ReferenceArcrole);
         List<ReferenceResource> genericReferences = getStore().getTargets(getIndex(),null,Constants.GenericReferenceArcrole);
         references.addAll(genericReferences);
         return references;
     }
     
     /**
      * @see Fragment#getReferencesWithLanguage(String)
      */
     public List<ReferenceResource> getReferencesWithLanguage(String language) throws XBRLException {
         List<ReferenceResource> references = new Vector<ReferenceResource>();
         List<ReferenceResource> candidates = this.getReferences();
         for (ReferenceResource reference: candidates) {
             String l = reference.getLanguage();
             if (l == null) if (l == null) references.add(reference);
             else if (l.equals(language)) references.add(reference);
         }
         return references;
     }
     
     /**
      * @see Fragment#getReferencesWithLanguageAndRole(String, URI)
      */
     public List<ReferenceResource> getReferencesWithLanguageAndRole(String language, URI role) throws XBRLException {
         List<ReferenceResource> references = new Vector<ReferenceResource>();
         List<ReferenceResource> candidates = this.getReferences();
         for (ReferenceResource reference: candidates) {
             String l = reference.getLanguage();
             URI r = reference.getResourceRole();
             if (l != null && r != null) {
                 if (l.equals(language) && r.equals(role)) references.add(reference);
             } else if (l != null) {
                 if (l.equals(language) && role == null) references.add(reference);
             } else if (r != null) {
                 if (r.equals(role) && language == null) references.add(reference);
             } else {
                 if (role == null && language == null) references.add(reference);
             }
         }
         return references;
     }
     
     /**
      * @see Fragment#getReferencesWithLanguageAndResourceRoleAndLinkRole(String, URI, URI)
      */
     public List<ReferenceResource> getReferencesWithLanguageAndResourceRoleAndLinkRole(String language, URI resourceRole, URI linkRole) throws XBRLException {
         List<ReferenceResource> references = new Vector<ReferenceResource>();
         List<ReferenceResource> candidates = this.getReferences();
         for (ReferenceResource reference: candidates) {
             if (linkRole == null || reference.getExtendedLink().getLinkRole().equals(linkRole)) {
                 String l = reference.getLanguage();
                 URI r = reference.getResourceRole();
                 if (l != null && r != null) {
                     if (l.equals(language) && r.equals(resourceRole)) references.add(reference);
                 } else if (l != null) {
                     if (l.equals(language) && resourceRole == null) references.add(reference);
                 } else if (r != null) {
                     if (r.equals(resourceRole) && language == null) references.add(reference);
                 } else {
                     if (resourceRole == null && language == null) references.add(reference);
                 }
             }
         }
         return references;
     }    
 
     /**
      * @see Fragment#getParentIndex()
      */
     public String getParentIndex() throws XBRLException {
     	return getMetaAttribute("parentIndex");
     }
     
     /**
      * @see Fragment#isChild()
      */
     public boolean isChild() throws XBRLException {
         return (! this.getParentIndex().equals(""));
     }
     
     /**
      * @see Fragment#isAncestorOf(Fragment)
      */
     public boolean isAncestorOf(Fragment descendant) throws XBRLException {
         if (! descendant.isChild()) return false;
         if (this.getIndex().equals(descendant.getParentIndex())) return true;
         return this.isAncestorOf(descendant.getParent());
     }
     
     /**
      * @see Fragment#isRoot()
      */
     public boolean isRoot() throws XBRLException {
         return (getParentIndex().equals(""));
     }
     
     /**
      * @see Fragment#setParentIndex(String)
      */
     public void setParentIndex(String index) throws XBRLException {
         setMetaAttribute("parentIndex",index);
     }
 
     /**
      * @see Fragment#getXPath()
      */
     public String getXPath() throws XBRLException {
 
     	String value = getMetaAttribute("sequenceToParentElement");
     	if (value.equals("")) return ".";
     	
     	String[] sequence = value.split(" ");
     	StringBuffer xpath = new StringBuffer(".");
     	for (int i=0; i<sequence.length; i++) {
     		xpath.append("/*[" + sequence[i] + "]");
     	}    	
     	return xpath.toString();
     }
 
     /**
      * Determines the sibling order of 
      * @param current The element that we are determining
      * the sibling order for.  
      * @return the order of the element, counting sibling 
      * elements from the left in document order.
      */
     private int getSiblingOrder(Element current) {
         
         Node previous = current.getPreviousSibling();
         
         while (previous != null) {
             if (previous.getNodeType() != Node.ELEMENT_NODE) 
                 previous = previous.getPreviousSibling();
             else break;
         }
 
         if (previous == null) return 1;
         return getSiblingOrder((Element) previous) + 1;
     }
     
     /**
      * @see Fragment#setSequenceToParentElement(Fragment)
      */
     public void setSequenceToParentElement(Fragment parent) throws XBRLException {
 
         Builder parentBuilder = parent.getBuilder();
         if (parentBuilder == null) throw new XBRLException("This method is not usable after the fragment has been built.");
         Element current = parentBuilder.getInsertionPoint();
         Element next = (Element) current.getParentNode();
         Stack<Integer> values = new Stack<Integer>();
 
         while (! next.getNamespaceURI().equals(Constants.XBRLAPINamespace.toString())) {
             values.push(new Integer(getSiblingOrder(current)));
             current = next;
             next = (Element) next.getParentNode();
         }
 
         StringBuffer value = new StringBuffer("");
     	while (! values.empty()) {
     	    Integer v = values.pop();
 			if (value.length() == 0) {
 				value.append(v.toString());
 			} else {
 				value.append(" " + v.toString()); 
 			}
 		}
 
     	String result = value.toString();
     	if (! result.equals(""))
     	setMetaAttribute("sequenceToParentElement",result);
     	
     }
     
     /**
      * @see Fragment#getSequenceToParentElement()
      */
     public String[] getSequenceToParentElement() throws XBRLException {
     	return this.getSequenceToParentElementAsString().split(" ");
     }
     
     /**
      * @see Fragment#getSequenceToParentElementAsString()
      */
     public String getSequenceToParentElementAsString() throws XBRLException {
     	String value = this.getMetaAttribute("sequenceToParentElement");
     	if (value == null) return "";
     	return value;
     }    
 
     /**
      * @see Fragment#appendID(String)
      * TODO Eliminate the ID metadata element given the existence of the xptr elements.
      */
     public void appendID(String id) throws XBRLException {
     	HashMap<String,String> attributes = new HashMap<String,String>();
     	attributes.put("id",id);
 		appendMetadataElement("ID",attributes);
     }
 
     /**
      * @see Fragment#appendElementSchemeXPointer(String)
      */
     public void appendElementSchemeXPointer(String expression) throws XBRLException {
     	HashMap<String,String> attributes = new HashMap<String,String>();
     	attributes.put("value",expression);
 		appendMetadataElement("xptr",attributes);
     }
 
     /**
      * @see Fragment#getNamespace()
      */
     public URI getNamespace() throws XBRLException {
     	if (getDataRootElement() == null) {
     		throw new XBRLException("The XML fragment root node is null.");
     	}
 	    return URI.create(getDataRootElement().getNamespaceURI());
     }
     
     /**
      * @see Fragment#getLocalname()
      */
     public String getLocalname() throws XBRLException {
     	return getDataRootElement().getLocalName();
     }
     
     /**
      * @see Fragment#getPrefixFromQName(String)
      */
     public String getPrefixFromQName(String qname) {
     	// Get the required namespace prefix from the QName
     	String prefix = "";
 		int delimiterIndex = qname.indexOf(':');
     	if (delimiterIndex > 0) {
     		prefix = qname.substring(0,delimiterIndex);
     	}
     	return prefix;
     }
 
     /**
      * Algorithm is as follows:
      * <ol>
      *  <li>If the node is an attribute, redo with the parent node.</li>
      *  <li>If the node is a XBRLAPI metadata element redo with right node in parent fragment.</li>
      *  <li>If the node is an element in the fragment data:</li>
      *   <ol>
      *    <li>Generate namespace declaration attribute name - 'xmlns:...' to search for.</li>
      *    <li>Try to match the QName prefix to the element's prefix to see if the element namespace is appropriate to return.</li>
      *    <li>Try to find the attribute doing the namespace declaration on the element and use that.</li>
      *    <li>If that fails, redo the search using the parent node.</li>
      *   </ol>
      * </ol>
      * 
      * @see Fragment#getNamespaceFromQName(String, Node)
      */
     public URI getNamespaceFromQName(String qname, Node node) throws XBRLException {
         
         if (this.getPrefixFromQName(qname).equals("xml")) return Constants.XMLNamespace;
         
         // If we have an attribute - go straight to working with the parent element.
         if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
             Node parent = node.getParentNode();
             if (parent == null) throw new XBRLException("The attribute has no parent element so the namespace for " + qname + " cannot be determined.");
             return getNamespaceFromQName(qname, parent);
         }
         
         if (node.getNodeType() == Node.ELEMENT_NODE) {
             Element element = (Element) node;
 
             // Go to parent fragment if we are looking at the container element for the fragment data.
             String elementNamespace = element.getNamespaceURI();
             if (elementNamespace != null) {
                 if (elementNamespace.equals(Constants.XBRLAPINamespace.toString())) {
                     if (this.isRoot()) throw new XBRLException("No namespace is defined for QName " + qname);
                     Fragment parent = getParent();
                     if (parent == null) throw new XBRLException("A parent fragment is missing from the data store preventing QName resolution for " + qname);
                     Element parentElement = getParentElement(parent.getDataRootElement());
                     return parent.getNamespaceFromQName(qname, parentElement);
                 }
             }
             
             // Try to exploit a known namespace mapping for the element or one of its attributes
             String prefix = getPrefixFromQName(qname);
             
             if ((node.getPrefix() == null) && (prefix.equals(""))) {
                 try {
                     return new URI(node.getNamespaceURI());
                 } catch (URISyntaxException e) {
                     throw new XBRLException("The namespace URI " + node.getNamespaceURI() + " has invalid syntax.",e);
                 }
             }
 
             if ((node.getPrefix() != null) && (node.getPrefix().equals(prefix))) {
                 try {
                     return new URI(node.getNamespaceURI());
                 } catch (URISyntaxException e) {
                     throw new XBRLException("The namespace URI " + node.getNamespaceURI() + " has invalid syntax.",e);
                 }
             }
             NamedNodeMap attrs = node.getAttributes();
             for (int i=0; i<attrs.getLength(); i++) {
                 Node attr = attrs.item(i);
 
                 if ((attr.getPrefix() == null) && (prefix.equals(""))) {
                     try {
                         return new URI(attr.getNamespaceURI());
                     } catch (URISyntaxException e) {
                         throw new XBRLException("The namespace URI " + attr.getNamespaceURI() + " has invalid syntax.",e);
                     }
                 }
                 
                 if ((attr.getPrefix() != null) && (attr.getPrefix().equals(prefix))) {
                     try {
                         return new URI(attr.getNamespaceURI());
                     } catch (URISyntaxException e) {
                         throw new XBRLException("The namespace URI " + attr.getNamespaceURI() + " has invalid syntax.",e);
                     }
                 }
             }
             
             // Create NS prefix declaration for the QName being sought.
             String declaration = "xmlns";
             if (!prefix.equals("")) {
                 declaration = declaration + ":" + prefix;
             }
             
             // Check for a namespace declaration on the current element
             if (element.hasAttribute(declaration)) {
                 String ns = element.getAttribute(declaration);
                 if (ns.equals("")) {
                     return null;// The namespace prefix has been undefined by the declaration.
                 }
                 try {
                     return new URI(ns);
                 } catch (URISyntaxException e) {
                     throw new XBRLException("The namespace URI " + ns + " has invalid syntax.",e);
                 }
             }
             
             return getNamespaceFromQName(qname, element.getParentNode());
             
         }
             
         throw new XBRLException("An element or attribute node is expected.");
 
     }
 
     /**
      * Algorithm is as follows:
      * <ol>
      *  <li>If the node is an attribute, redo with the parent node.</li>
      *  <li>If the node is a XBRLAPI metadata element redo with right node in parent fragment.</li>
      *  <li>If the node is an element in the fragment data:</li>
      *   <ol>
      *    <li>Try to find the XML lang attribute and use its value.</li>
      *    <li>If that fails, redo the search using the parent node.</li>
      *   </ol>
      * </ol>
      * 
      * @see Fragment#getLanguage(Node)
      */
     public String getLanguage(Node node) throws XBRLException {
         
         // If we have an attribute - go straight to working with the parent element.
         if (node.getNodeType() != Node.ELEMENT_NODE) {
             Node parentNode = node.getParentNode();
             if (parentNode == null) return null;
             return getLanguage(parentNode);
         }
         
         Element element = (Element) node;
 
         // Go to parent fragment if we are looking at the container element for the fragment data.
         String elementNamespace = element.getNamespaceURI();
         if (elementNamespace != null) {
             if (elementNamespace.equals(Constants.XBRLAPINamespace.toString())) {
                 if (this.isRoot()) return null;
                 Fragment parent = getParent();
                 if (parent == null) throw new XBRLException("A parent fragment is missing from the data store preventing language code identification.");
                 Element parentElement = getParentElement(parent.getDataRootElement());
                 return parent.getLanguage(parentElement);
             }
         }
         
         // Check for a language code on the current element
        if (element.hasAttribute("xml:lang")) {
            return element.getAttribute("xml:lang");
         }
         
         // Check for a language code on the parent element
         return getLanguage(element.getParentNode());
 
     }
     
     /**
      * @see Fragment#getLanguage()
      * @see Fragment#getLanguage(Node)
      */
     public String getLanguage() throws XBRLException {
         return this.getLanguage(this.getDataRootElement());
     }
 
     /**
      * @see Fragment#getParent()
      */
     public Fragment getParent() throws XBRLException {
         if (! this.isChild()) return null;
     	String parentIndex = this.getParentIndex();
     	return getStore().getXMLResource(parentIndex);
     }
     
     /**
      * This method operates as follows:
      * <ol>
      *  <li>If the sequence to the parent element indicates that the 
      *  data root element is the parent - use that.</li>
      *  <li>Loop through the sequence to the parent element.</li>
      * </ol>
      * @see Fragment#getParentElement(Element)
      */
     public Element getParentElement(Element parentDataRootElement) throws XBRLException {
 
     	String[] sequence = getSequenceToParentElement();
     	
     	// If there is no data about the sequence then just return the given parent data root element
     	if (sequence[0].equals("")) {
     	    return parentDataRootElement;
     	}
 
     	// Traverse the parent data DOM to find the parent element of this fragment's root element.
     	Element current = parentDataRootElement;
     	for (String value: sequence) {
             int elementOrder = (new Integer(value)).intValue();
             int elementsFound = 0;
             NodeList children = current.getChildNodes();
             int j = 0;
             // While there are still children to consider and we have not found 
             while ((elementsFound < elementOrder) && (j < children.getLength())) {
                 Node child = children.item(j);
                 if (child.getNodeType() == Node.ELEMENT_NODE) {
                     elementsFound++;
                     if (elementsFound == elementOrder) current = (Element) child;
                 }
                 j++;
             }
             if ((j==children.getLength()) && (elementsFound < elementOrder)) {
                 throw new XBRLException("The sequence to the parent element is incorrect.");
             }
     	}
     	return current;
 
     }
 
     /**
      * @see Fragment#getLocalnameFromQName(String)
      */
     public String getLocalnameFromQName(String qname) {
     	String localname = qname;
 		int delimiterIndex = qname.indexOf(':');
     	if (delimiterIndex > 0) {
     		localname = qname.substring(delimiterIndex+1,qname.length());
     	}
     	return localname;
     }
 
     /**
      * @see Fragment#getXPointerExpression()
      */
     public String getXPointerExpression() throws XBRLException {
         String expression = this.getIDXPointerExpression();
         if (expression != null) {
             return "#" + expression;
         }
         expression = this.getElementSchemeXPointerExpression();
         return "#" + "element(" + expression + ")";
     }
 
     /**
      * @see Fragment#getElementSchemeXPointerExpression()
      */
     public String getElementSchemeXPointerExpression() throws XBRLException {
         NodeList nodes = getMetadataRootElement().getElementsByTagNameNS(Constants.XBRLAPINamespace.toString(),"xptr");
         if (nodes.getLength() == 0) throw new XBRLException("The fragment is missing its element scheme XPointer expression.");
         Element xptrElement = (Element) nodes.item(0);
         if (xptrElement.hasAttribute("value")) return xptrElement.getAttribute("value");
         throw new XBRLException("An element-scheme XPointer expression is corrupted for this fragment.");        
     }
 
     /**
      * @see Fragment#getIDXPointerExpression()
      */
     public String getIDXPointerExpression() throws XBRLException {
         NodeList nodes = getMetadataRootElement().getElementsByTagNameNS(Constants.XBRLAPINamespace.toString(),"ID");
         if (nodes.getLength() == 0) return null;
         Element idElement = (Element) nodes.item(0);
         if (idElement.hasAttribute("id")) return idElement.getAttribute("id");
         throw new XBRLException("The shorthand ID for this fragment is missing.");
     }
 
 }
