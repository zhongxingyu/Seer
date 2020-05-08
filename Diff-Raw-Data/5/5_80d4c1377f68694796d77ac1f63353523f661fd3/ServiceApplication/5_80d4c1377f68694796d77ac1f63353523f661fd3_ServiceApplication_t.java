 /*
 * Claudia Project
 * http://claudia.morfeo-project.org
 *
 * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
 * S.A.Unipersonal (Telefonica I+D)
 *
 * See CREDITS file for info about members and contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License (AGPL) as 
 * published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Affero GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * If you want to use this software an plan to distribute a
 * proprietary application in any way, and you are not licensing and
 * distributing your source code under AGPL, you probably need to
 * purchase a commercial license of the product. Please contact
 * claudia-support@lists.morfeo-project.org for more information.
 */
 package com.telefonica.claudia.slm.deployment;
 
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.hibernate.annotations.CollectionOfElements;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.telefonica.claudia.slm.common.SMConfiguration;
 import com.telefonica.claudia.slm.deployment.hwItems.NICConf;
 import com.telefonica.claudia.slm.deployment.hwItems.Network;
 import com.telefonica.claudia.slm.naming.DirectoryEntry;
 import com.telefonica.claudia.slm.naming.FQN;
 import com.telefonica.claudia.slm.naming.ReservoirDirectory;
 
 @Entity
 public class ServiceApplication implements DirectoryEntry {
     
 	@Id
 	@GeneratedValue
 	public long internalId;
 	
     private String appName = null;
 	
 	@ManyToOne
     private Customer customer = null;
 	
 	@OneToOne(cascade={CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST})
     private FQN serAppFQN = null;
     
 	@OneToMany(mappedBy="serviceApplication", cascade=CascadeType.ALL)
 	private Set<VEE> vees = new HashSet<VEE>();
     
     @OneToMany(mappedBy="serviceApplication", cascade=CascadeType.ALL)
     private Set<Network> networks = new HashSet<Network>();
     
     @OneToMany(mappedBy="serviceApplication", cascade=CascadeType.ALL)
     private Set<ServiceKPI> serviceKPIs = new HashSet<ServiceKPI>();
     
     @OneToMany(mappedBy="serviceApplication", cascade=CascadeType.ALL)
     private Set<Rule> rules = new HashSet<Rule>();
     
     @CollectionOfElements
     private Set<String> hostAffinities = new HashSet<String>();
     
     @CollectionOfElements
     private Set<String> siteAffinities = new HashSet<String>();
     
     @CollectionOfElements
     private Set<String> domainAffinities = new HashSet<String>();
     
     @CollectionOfElements
     private Set<String> hostAntiAffinity = new HashSet<String>();
     
     @CollectionOfElements
     private Set<String> siteAntiAffinities = new HashSet<String>();
     
     @CollectionOfElements
     private Set<String> domainAntiAffinities = new HashSet<String>();
     
 	/**
 	 * URL of the OVF Descriptor used to deploy the service.
 	 */
 	private String xmlFile;
     
     public ServiceApplication() {}
     
     public ServiceApplication(String appName, Customer customer) {
     	if(appName == null)
     		throw new IllegalArgumentException("Application name cannot be null");
     	if(customer == null)
     		throw new IllegalArgumentException("Customer cannot be null");
         this.appName = appName;
         this.customer = customer;
     }
 
     public void setXmlFile(String xmlFile) {
     	this.xmlFile = xmlFile;
     }
     
     public String getXmlFile() {
     	return xmlFile;
     }
     
     public void addHostAffinity(String vee){
         hostAffinities.add(vee);
     }
     
     public Set<String> getHostAffinities() {
         return hostAffinities;
     }
     
     public void addSiteAffinity(String vee){
         siteAffinities.add(vee);
     }
     
     public Set<String> getSiteAffinities() {
         return siteAffinities;
     }
     
     public void addDomainAffinity(String vee){
         domainAffinities.add(vee);
     }
     
     public Set<String> getDomainAffinities() {
         return domainAffinities;
     }
     
     public void addHostAntiAffinity(String vee) {
         hostAntiAffinity.add(vee);
     }
     
     public Set<String> getHostAntiAffinities() {
         return hostAntiAffinity;
     }
     
     public void addSiteAntiAffinity(String vee) {
         siteAntiAffinities.add(vee);
     }
     
     public Set<String> getSiteAntiAffinities() {
         return siteAntiAffinities;
     }
     
     public void addDomainAntiAffinity(String vee) {
         domainAntiAffinities.add(vee);
     }
     
     public Set<String> getDomainAntiAffinities() {
         return domainAntiAffinities;
     }
     
     public String getSerAppName(){
         return appName;
     }
     
     public void setSerAppName(String name) {
     	this.appName=name;
     }
     
     public Customer getCustomer(){
         return customer;
     }
     
     public Set<VEE> getVEEs(){
         return vees;
     }
     
     public void registerVEE(VEE vee){
         
         if(vee == null)
             throw new IllegalArgumentException("Cannot register null VEE");
                 
         if(!vee.getServiceApplication().equals(this))
         	throw new IllegalArgumentException("Trying to register VEE " + vee + " on a different Service " + this);
     	
         vees.add(vee);
     }
     
     public boolean isVEERegistered(VEE vee) {
         return vees.contains(vee);
     }
     
     public void unregisterVEE(VEE vee) {
         vees.remove(vee);
     }
     
     public Set<ServiceKPI> getServiceKPIs() {
     	return serviceKPIs;
     }
     
     public void registerServiceKPI(ServiceKPI serviceKPI) {
     	
     	if(serviceKPI == null)
             throw new IllegalArgumentException("Cannot register null service KPI");
         
         if(!serviceKPI.getServiceApplication().equals(this))
             throw new IllegalArgumentException("Trying to register service KPI " + serviceKPI + " on a different Service " + this);
     	
         serviceKPIs.add(serviceKPI);
     }
     
     public boolean isServiceKPIRegistered(ServiceKPI serviceKPI) {
     	return serviceKPIs.contains(serviceKPI);
     }
     
     public void unregisterServiceKPI(ServiceKPI serviceKPI) {
     	serviceKPIs.remove(serviceKPI);
     }
     
     public Set<Network> getNetworks() {
     	Set<Network> networks = new HashSet<Network>();
     	for(VEE vee : vees) {
     		List<NICConf> nicConfs = vee.getNICsConf();
     		for(NICConf nicConf: nicConfs)
     			networks.add(nicConf.getNetwork());
     	}
     	return networks;
     }
     
     /**
      * @param netName
      * @return the Network class correspondig to that name, null if no Netwokr can be found
      */
     public Network getNetworkByName(String netName) {
 
     	for (Iterator<Network> it = networks.iterator(); it.hasNext(); ) {
     		Network net = it.next();
     		if (net.getName().equals(netName)) {
     			return net;
     		}
     	}
     	return null;
     }
     
     public void registerNetwork(Network network) {
         
         if(network == null)
             throw new IllegalArgumentException("Cannot register null network");
         
         if(!network.getServiceApplication().equals(this))
             throw new IllegalArgumentException("Trying to register network " + network + " on a different Service " + this);
     	
         networks.add(network);
     }
     
     public boolean isNetworkRegistered(Network network) {
     	return networks.contains(network);
     }
     
     public void unregisterNetwork(Network network) {
     	networks.remove(network);
     }
     
     public Set<Rule> getServiceRules() {
     	return rules;
     }
     
     public void registerServiceRule(Rule rule) {
 
     	rules.add(rule);
     }
     
     public boolean isServiceRuleRegistered(Rule rule) {
     	return rules.contains(rule);
     }
     
     public void unregisterRule(Rule rule) {
     	rules.remove(rule);
     }
     
     public FQN getFQN(){
         if(serAppFQN == null)
             serAppFQN = ReservoirDirectory.getInstance().buildFQN(this);
         return serAppFQN;
     }
     
     @Override
     public String toString() {
         return getFQN().toString();
     }
     
     @Override
     public int hashCode() {
         return getFQN().hashCode();
     }
     
     @Override
     public boolean equals(Object object) {
         
         if(object == null)
             return false;
         
         if(!(object instanceof ServiceApplication))
             return false;
         
         return ((ServiceApplication)object).getFQN().equals(getFQN());
     }
     
     public Document toXML() {
     	
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder;
         Document doc;
     	
         String organizationId = SMConfiguration.getInstance().getSiteRoot().replace(".", "_");
         
         try {
 			docBuilder = dbfac.newDocumentBuilder();
 			
 			doc = docBuilder.newDocument();
 			
 	        Element r = doc.createElement("VApp");
 	        doc.appendChild(r);
 	        
 	        r.setAttribute("name", getFQN().toString());
 	        r.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + getCustomer().getCustomerName() + "/vapp/" + getSerAppName());
 	        
 	        Element link = doc.createElement("Link");
 	        r.appendChild(link);
 	        
 	        link.setAttribute("rel", "monitor:measures");
 	        link.setAttribute("type", "application/vnc.telefonica.tcloud. measureDescriptorList+xml");
 	        link.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + getCustomer().getCustomerName() + "/vapp/" + getSerAppName() + "/monitor");
 
 	        Element children = doc.createElement("Children");
 	        r.appendChild(children);
 	        
 	    	for(VEE vee : vees) { 
 	    		
 	    		Element veeElement = doc.createElement("Vapp");
 	    		children.appendChild(veeElement);
 	    		
 	    		veeElement.setAttribute("name", vee.getFQN().toString());
 	    		veeElement.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + getCustomer().getCustomerName() + "/vapp/" + getSerAppName() + "/" + vee.getVEEName());
 	    		
 	    		Element monitorLink = doc.createElement("Link");
 	    		veeElement.appendChild(monitorLink);
 	    		
 	    		monitorLink.setAttribute("rel", "monitor:measures");
 	    		monitorLink.setAttribute("type", "application/vnc.telefonica.tcloud. measureDescriptorList+xml");
 	    		monitorLink.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + 
 	    				getCustomer().getCustomerName() + "/vapp/" + getSerAppName() + "/" + vee.getVEEName() + "/monitor");
 	    		
 	    		Element veeChildren = doc.createElement("Children");
 	    		veeElement.appendChild(veeChildren);
 	    		
 	    		SortedSet<VEEReplica> orderedVEEReplicas = new TreeSet<VEEReplica>(new VEEReplicasComparator());
 	    		orderedVEEReplicas.addAll(vee.getVEEReplicas());
 	    		
 	    		for(VEEReplica veeReplica : orderedVEEReplicas) { 
 	    			
 	    			Element veeReplicaElement = doc.createElement("VApp");
 	    			veeChildren.appendChild(veeReplicaElement);
 	    			
 	    			veeReplicaElement.setAttribute("name", veeReplica.getFQN().toString());
	    			veeReplicaElement.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + getCustomer().getCustomerName() + "/vapp/" + getSerAppName() +
 													"/" + vee.getVEEName() + "/" + veeReplica.getId());
 	    			
 	    			Element linkVeeReplica = doc.createElement("Link");
 	    			veeReplicaElement.appendChild(linkVeeReplica);
 	    			
 	    			linkVeeReplica.setAttribute("rel", "monitor:measures");
 	    			linkVeeReplica.setAttribute("type", "application/vnc.telefonica.tcloud. measureDescriptorList+xml");
	    			linkVeeReplica.setAttribute("href", "@HOSTNAME/api/org/" + organizationId + "/vdc/" + 
 	        				getCustomer().getCustomerName() + "/vapp/" + getSerAppName() + "/" + vee.getVEEName() + "/" + veeReplica.getId() + "/monitor");
 
 	    		}
 	    	}
 	    	
 	    	return doc;
 
 		} catch (ParserConfigurationException e) {
 			
 		}
 
         return null;
     }
 }
 
 class VEEReplicasComparator implements Comparator<VEEReplica> {
 
 	public int compare(VEEReplica replica1, VEEReplica replica2) {
 		return replica2.getId() - replica1.getId();
 	}
 	
 }
