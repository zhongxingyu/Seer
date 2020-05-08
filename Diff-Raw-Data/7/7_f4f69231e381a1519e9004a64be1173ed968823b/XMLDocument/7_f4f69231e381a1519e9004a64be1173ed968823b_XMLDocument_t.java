 package org.jboss.jbossesb.eclipse.plugin.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * <h3>Class represents a XML Document</h3>
  * <ul>
  * <li><i>providers</i> - list of JBossESB providers</li>
  * <li><i>services</i> - list of JBossESB services</li>
  * <li><i>jbossesb</i> - XMLElement of the corresponding element in configuration file (jbossesb)</li>
  * <li><i>globals</i> - XMLElement of the corresponding element in configuration file (globals)</li>
  * </ul>
  * 
  * @author Tomas Sedmik, tomas.sedmik@gmail.com
  * @since 2012-11-04
  */
 public class XMLDocument {
 
 	private XMLElement jbossesb;
 	
 	private static Logger log = Logger.getLogger(XMLDocument.class.getName());
 	private PropertyChangeSupport listeners;
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener){
 		listeners.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener){
 		listeners.removePropertyChangeListener(listener);
 	}
 	
 	public XMLDocument() {
 		listeners = new PropertyChangeSupport(this);
 	}
 	
 	/**
 	 * Remove given element from the document.
 	 * 
 	 * @param element Element to remove.
 	 */
 	public void removeElement(XMLElement element) {
 		
 		String address = element.getAddress();
 		XMLElement temp = jbossesb;
 		
 		// find parent of the given element
 		while (address.split("/").length != temp.getAddress().split("/").length + 1) {
 			for (XMLElement child : temp.getChildren()) {
 				if (address.startsWith(child.getAddress())) {
 					temp = child;
 				}
 			}
 		}
 		
 		// remove the given element
 		if (temp.removeChild(element)) {
 			log.log(Level.INFO, "Deletion was successful!");
 			// FIXME repair removing objects => remove right object (some problem with observer?) 
 			listeners.firePropertyChange("remove", element, null);
 		} else {
 			log.log(Level.SEVERE, "Object not found! Deletion cannot be perform.");
 		}
 	}
 
 	public void addProvider(XMLElement provider) {
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/providers")) {
 				child.addChildElement(provider);
 			}
 		}
 		listeners.firePropertyChange("add", null, provider);
 	}
 	
 	public void addService(XMLElement service) {
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/services")) {
 				child.addChildElement(service);
 			}
 		}
 		listeners.firePropertyChange("add", null, service);
 	}
 	
 	public List<XMLElement> getProviders() {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/providers")) {
 				return child.getChildren();
 			}
 		}
 		
 		return null;
 	}
 	
 	public void setProviders(List<XMLElement> providers) {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/providers")) {
 				child.setChildren(providers);
 			}
 		}
 	}
 	
 	public List<XMLElement> getServices() {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/services")) {
 				return child.getChildren();
 			}
 		}
 		
 		return null;
 	}
 	
 	public void setServices(List<XMLElement> services) {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/services")) {
 				child.setChildren(services);
 			}
 		}
 	}
 	
 	public XMLElement getJbossesb() {
 		return jbossesb;
 	}
 	
 	public void setJbossesb(XMLElement jbossesb) {
 		this.jbossesb = jbossesb;
 	}
 	
 	public XMLElement getGlobals() {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (XMLElement child : children) {
 			if (child.getAddress().equals("/jbossesb/globals")) {
 				return child;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void setGlobals(XMLElement globals) {
 		
 		List<XMLElement> children = jbossesb.getChildren();
 		for (int i = 0; i < children.size(); i++) {
 			if (children.get(i).getAddress().equals("/jbossesb/globals")) {
 				children.set(i, globals);
 			}
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((jbossesb == null) ? 0 : jbossesb.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		XMLDocument other = (XMLDocument) obj;		
 		if (jbossesb == null) {
 			if (other.jbossesb != null)
 				return false;
 		} else if (!jbossesb.equals(other.jbossesb))
 			return false;
 		return true;
 	}
 	
 }
