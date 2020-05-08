 package org.xbrlapi.data;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.xbrlapi.Arc;
 import org.xbrlapi.ArcEnd;
 import org.xbrlapi.ArcroleType;
 import org.xbrlapi.Concept;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.Fact;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.Instance;
 import org.xbrlapi.Item;
 import org.xbrlapi.Resource;
 import org.xbrlapi.RoleType;
 import org.xbrlapi.Tuple;
 import org.xbrlapi.impl.FragmentListImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Abstract implementation of the XBRL data store.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public abstract class XBRLStoreImpl extends BaseStoreImpl implements XBRLStore {
 	
 	public XBRLStoreImpl() {
 		super();
 	}
     
     /**
      * @return a list of all of the root-level facts in the data store (those facts
      * that are children of the root element of an XBRL instance).  Returns an empty list 
      * if no facts are found.
      * @throws XBRLException
      */
     public FragmentList<Fact> getFacts() throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragments("Instance");
     	return getFactsFromInstances(instances);
     }
     
     /**
      * This method is provided as a helper method for the getFact methods.
      * @param instances The list of instance fragments to extract facts from.
      * @return The list of facts in the instances.
      * @throws XBRLException
      */
     private FragmentList<Fact> getFactsFromInstances(FragmentList<Instance> instances) throws XBRLException {
     	FragmentList<Fact> facts = new FragmentListImpl<Fact>();
     	for (Instance instance: instances) {
     		facts.addAll(instance.getFacts());
     	}
     	return facts;
     }
     
     /**
      * Helper method for common code in the getItem methods.
      * @param instances The instances to retrieve items for.
      * @return a list of root items in the instances.
      * @throws XBRLException
      */
     private FragmentList<Item> getItemsFromInstances(FragmentList<Instance> instances) throws XBRLException {
     	FragmentList<Fact> facts = getFactsFromInstances(instances);
     	FragmentList<Item> items = new FragmentListImpl<Item>();
     	for (Fact fact: facts) {
     		if (! fact.getType().equals("org.xbrlapi.org.impl.TupleImpl"))
     			items.addFragment((Item) fact);
     	}
     	return items;
     }
     
     /**
      * Helper method for common code in the getTuple methods.
      * @param instances The instances to retrieve tuples for.
      * @return a list of root tuples in the instances.
      * @throws XBRLException
      */
     private FragmentList<Tuple> getTuplesFromInstances(FragmentList<Instance> instances) throws XBRLException {
     	FragmentList<Fact> facts = getFactsFromInstances(instances);
     	FragmentList<Tuple> tuples = new FragmentListImpl<Tuple>();
     	for (Fact fact: facts) {
     		if (fact.getType().equals("org.xbrlapi.org.impl.TupleImpl"))
     			tuples.addFragment((Tuple) fact);
     	}
     	return tuples;
     }    
     
     /**
      * @return a list of all of the root-level items in the data store(those items
      * that are children of the root element of an XBRL instance).
      * TODO eliminate the redundant retrieval of tuples from the getItems methods.
      * @throws XBRLException
      */
     public FragmentList<Item> getItems() throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragments("Instance");
     	return getItemsFromInstances(instances);
     }
     
     /**
      * @return a list of all of the tuples in the data store.
      * @throws XBRLException
      */
     public FragmentList<Tuple> getTuples() throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragments("Instance");
     	return this.getTuplesFromInstances(instances);
     }
 
     /**
      * @param url The URL of the document to get the facts from.
      * @return a list of all of the root-level facts in the specified document.
      * @throws XBRLException
      */
     public FragmentList<Fact> getFacts(URL url) throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragmentsFromDocument(url,"Instance");
     	return this.getFactsFromInstances(instances);
     }
     
     /**
      * @param url The URL of the document to get the items from.
      * @return a list of all of the root-level items in the data store.
      * @throws XBRLException
      */
     public FragmentList<Item> getItems(URL url) throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragmentsFromDocument(url,"Instance");
     	return this.getItemsFromInstances(instances);
     }
     
     /**
      * @param url The URL of the document to get the facts from.
      * @return a list of all of the root-level tuples in the specified document.
      * @throws XBRLException
      */
     public FragmentList<Tuple> getTuples(URL url) throws XBRLException {
     	FragmentList<Instance> instances = this.<Instance>getFragmentsFromDocument(url,"Instance");
     	return this.getTuplesFromInstances(instances);
     }
 
     /**
      * Implementation strategy is:<br/>
      * 1. Get all extended link elements matching network requirements.<br/>
      * 2. Get all arcs defining relationships in the network.<br/>
      * 3. Get all resources at the source of the arcs.<br/>
      * 4. Return only those source resources that that are not target resources also.<br/>
      * 
      * @param linkNamespace The namespace of the link element.
      * @param linkName The name of the link element.
      * @param linkRole the role on the extended links that contain the network arcs.
      * @param arcNamespace The namespace of the arc element.
      * @param arcName The name of the arc element.
      * @param arcRole the arcrole on the arcs describing the network.
      * @return The list of fragments for each of the resources that is identified as a root
      * of the specified network (noting that a root resource is defined as a resource that is
      * at the source of one or more relationships in the network and that is not at the target 
      * of any relationships in the network).
      * @throws XBRLException
      */
     public FragmentList<Fragment> getNetworkRoots(String linkNamespace, String linkName, String linkRole, String arcNamespace, String arcName, String arcRole) throws XBRLException {
     	
     	// Get the links that contain the network declaring arcs.
    	String linkQuery = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@type='org.xbrlapi.impl.ExtendedLinkImpl' and "+ Constants.XBRLAPIPrefix+ ":" + "data/*[namespace-uri()='" + linkNamespace + "' and local-name()='" + linkName + "' and @xlink:role='" + linkRole + "']";
     	System.out.println(linkQuery);
     	FragmentList<ExtendedLink> links = this.<ExtendedLink>query(linkQuery);
     	
     	// Get the arcs that declare the relationships in the network.
     	// For each arc map the ids of the fragments at their sources and targets.
     	HashMap<String,String> sourceIds = new HashMap<String,String>();
     	HashMap<String,String> targetIds = new HashMap<String,String>();
     	for (int i=0; i<links.getLength(); i++) {
     		ExtendedLink link = links.getFragment(i);
     		FragmentList<Arc> arcs = link.getArcs();
     		for (Arc arc: arcs) {
     			if (arc.getNamespaceURI().equals(arcNamespace))
     				if (arc.getLocalname().equals(arcName))
     					if (arc.getArcrole().equals(arcRole)) {
     			    		FragmentList<ArcEnd> sources = arc.getSourceFragments();
     						FragmentList<ArcEnd> targets = arc.getTargetFragments();
     						for (int k=0; k<sources.getLength(); k++) {
     							sourceIds.put(sources.getFragment(k).getFragmentIndex(),"");
     						}
     						for (int k=0; k<sources.getLength(); k++) {
     							targetIds.put(targets.getFragment(k).getFragmentIndex(),"");
     						}
     					}
     		}
     	}
     	
     	// Get the root resources in the network
     	FragmentList<Fragment> roots = new FragmentListImpl<Fragment>();
     	Set<String> ids = sourceIds.keySet();
     	Iterator<String> iterator = sourceIds.keySet().iterator();
     	while (iterator.hasNext()) {
     		String id = iterator.next();
     		if (! targetIds.containsKey(id)) {
     			roots.addFragment(this.getFragment(id));
     		}
     	}
     	return roots;
     }
 
     /**
      * This implementation is not as strict as the XBRL 2.1 specification
      * requires but it is generally faster and delivers sensible results.
      * It will only fail if people use the same link role and arc role but
      * rely on arc or link element differences to distinguish networks.<br/><br/>
      * 
      * Implementation strategy is:<br/>
      * 1. Get all extended link elements with the given link role.<br/>
      * 2. Get all arcs with the given arc role.<br/>
      * 3. Get all resources at the source of the arcs.<br/>
      * 4. Return only those source resources that that are not target resources also.<br/>
      * 
      * @param linkRole the role on the extended links that contain the network arcs.
      * @param arcRole the arcrole on the arcs describing the network.
      * @return The list of fragments for each of the resources that is identified as a root
      * of the specified network (noting that a root resource is defined as a resource that is
      * at the source of one or more relationships in the network and that is not at the target 
      * of any relationships in the network).
      * @throws XBRLException
      */
     public FragmentList<Fragment> getNetworkRoots(String linkRole, String arcRole) throws XBRLException {
     	
     	// Get the links that contain the network declaring arcs.
    	String linkQuery = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@type='org.xbrlapi.impl.ExtendedLinkImpl']/"+ Constants.XBRLAPIPrefix+ ":" + "data/*[@xlink:role='" + linkRole + "']";
     	FragmentList<ExtendedLink> links = this.<ExtendedLink>query(linkQuery);
     	
     	// Get the arcs that declare the relationships in the network.
     	// For each arc map the ids of the fragments at their sources and targets.
     	HashMap<String,String> sourceIds = new HashMap<String,String>();
     	HashMap<String,String> targetIds = new HashMap<String,String>();
     	for (int i=0; i<links.getLength(); i++) {
     		ExtendedLink link = links.getFragment(i);
     		FragmentList<Arc> arcs = link.getArcs();
     		for (Arc arc: arcs) {
 				if (arc.getArcrole().equals(arcRole)) {
 		    		FragmentList<ArcEnd> sources = arc.getSourceFragments();
 					FragmentList<ArcEnd> targets = arc.getTargetFragments();
 					for (int k=0; k<sources.getLength(); k++) {
 						sourceIds.put(sources.getFragment(k).getFragmentIndex(),"");
 					}
 					for (int k=0; k<sources.getLength(); k++) {
 						targetIds.put(targets.getFragment(k).getFragmentIndex(),"");
 					}
 				}
     		}
     	}
     	
     	// Get the root resources in the network
     	FragmentList<Fragment> roots = new FragmentListImpl<Fragment>();
     	Set<String> ids = sourceIds.keySet();
     	Iterator<String> iterator = sourceIds.keySet().iterator();
     	while (iterator.hasNext()) {
     		String id = iterator.next();
     		if (! targetIds.containsKey(id)) {
     			roots.addFragment(this.getFragment(id));
     		}
     	}
     	return roots;
     }    
     
     
     /**
      * @param namespace The namespace for the concept.
      * @param name The local name for the concept.
      * @return the concept fragment for the specified namespace and name.
      * @throws XBRLException if more than one matching concept is found in the data store
      * or if no matching concepts are found in the data store.
      */
     public Concept getConcept(String namespace, String name) throws XBRLException {
     	
     	// TODO Make sure that non-concept element declarations are handled.
     	
     	FragmentList<Concept> concepts = this.<Concept>query("/"+ Constants.XBRLAPIPrefix+ ":" + "fragment/"+ Constants.XBRLAPIPrefix+ ":" + "data/xsd:element[@name='" + name + "']");
     	FragmentList<Concept> matches = new FragmentListImpl<Concept>();
     	for (Concept concept: concepts) {
     		if (concept.getTargetNamespaceURI().equals(namespace)) {
     			matches.addFragment(concept);
     		}
     	}
     	
     	if (matches.getLength() == 0) 
     		throw new XBRLException("No matching concepts were found for " + namespace + ":" + name + ".");
     	
     	if (matches.getLength() > 1) 
     		throw new XBRLException(new Integer(matches.getLength()) + "matching concepts were found for " + namespace + ":" + name + ".");
     	
     	return matches.getFragment(0);
     }
 
     /**
      * @see org.xbrlapi.data.XBRLStore#getLinkRoles()
      */
     public HashMap<String,String> getLinkRoles() throws XBRLException {
     	HashMap<String,String> roles = new HashMap<String,String>();
     	FragmentList<RoleType> types = this.getRoleTypes();
     	for (RoleType type: types) {
     		String role = type.getCustomURI();
     		String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@type='org.xbrlapi.impl.ExtendedLinkImpl' and "+ Constants.XBRLAPIPrefix+ ":" + "data/*/@xlink:role='" + role + "']";
         	FragmentList<ExtendedLink> links = this.<ExtendedLink>query(query);
     		if (links.getLength() > 0) {
     			roles.put(role,"");
     		}
     	}
     	return roles;
     }
     
 
     
     /**
      * @see org.xbrlapi.data.XBRLStore#getArcRoles()
      */
     public HashMap<String,String> getArcRoles() throws XBRLException {
     	// TODO Simplify getArcRoles method of the XBRLStore to eliminate need to get all arcs in the data store.
     	HashMap<String,String> roles = new HashMap<String,String>();
     	FragmentList<ArcroleType> types = this.getArcroleTypes();
     	for (ArcroleType type: types) {
     		String role = type.getCustomURI();
     		String query = "/"+ Constants.XBRLAPIPrefix + ":" + "fragment["+ Constants.XBRLAPIPrefix+ ":" + "data/*[@xlink:type='arc' and @xlink:arcrole='" + role + "']]";
         	FragmentList<Arc> arcs = this.<Arc>query(query);
     		if (arcs.getLength() > 0) {
     			roles.put(role,"");
     		}
     	}
 
     	return roles;
     }
     
     /**
      * @see org.xbrlapi.data.XBRLStore#getLinkRoles(String)
      */
     public HashMap<String,String> getLinkRoles(String arcrole) throws XBRLException {
     	HashMap<String,String> roles = new HashMap<String,String>();
     	HashMap<String,Fragment> links = new HashMap<String,Fragment>();
 		String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment["+ Constants.XBRLAPIPrefix+ ":" + "data/*[@xlink:type='arc' and @xlink:arcrole='" + arcrole + "']]";
     	FragmentList<Arc> arcs = this.<Arc>query(query);
     	for (Arc arc: arcs) {
     		if (! links.containsKey(arc.getParentIndex())) {
     			ExtendedLink link = arc.getExtendedLink();
     			links.put(link.getFragmentIndex(),link);
     		}
     	}
     	
     	for (Fragment l: links.values()) {
     		ExtendedLink link = (ExtendedLink) l;
     		if (! roles.containsKey(link.getLinkRole())) {
     			roles.put(link.getLinkRole(),"");
     		}
     	}
     	
     	return roles;
     	
     }
     
     /**
      * @return a list of roleType fragments
      * @throws XBRLException
      */
     public FragmentList<RoleType> getRoleTypes() throws XBRLException {
     	return this.<RoleType>getFragments("RoleType");
     }
     
     /**
      * @see org.xbrlapi.data.XBRLStore#getRoleTypes(String)
      */
     public FragmentList<RoleType> getRoleTypes(String uri) throws XBRLException {
     	String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment["+ Constants.XBRLAPIPrefix+ ":" + "data/link:roleType/@roleURI='" + uri + "']";
     	return this.<RoleType>query(query);
     }    
     
     /**
      * @return a list of ArcroleType fragments
      * @throws XBRLException
      */
     public FragmentList<ArcroleType> getArcroleTypes() throws XBRLException {
     	return this.<ArcroleType>getFragments("ArcroleType");
     }
     
     /**
      * @return a list of arcroleType fragments that define a given arcrole.
      * @throws XBRLException
      */
     public FragmentList<ArcroleType> getArcroleTypes(String uri) throws XBRLException {
     	String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment["+ Constants.XBRLAPIPrefix+ ":" + "data/link:arcroleType/@arcroleURI='" + uri + "']";
     	return this.<ArcroleType>query(query);
     }
     
     /**
      * @see org.xbrlapi.data.XBRLStore#getResourceRoles()
      */
     public HashMap<String,String> getResourceRoles() throws XBRLException {
     	HashMap<String,String> roles = new HashMap<String,String>();
     	FragmentList<Resource> resources = this.<Resource>query("/"+ Constants.XBRLAPIPrefix+ ":" + "fragment["+ Constants.XBRLAPIPrefix+ ":" + "data/*/@xlink:type='resource']");
     	for (Resource resource: resources) {
     		String role = resource.getResourceRole();
     		if (! roles.containsKey(role)) roles.put(role,"");
     	}
     	return roles;
     }    
         
 }
