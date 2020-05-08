 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.services.config.impl;
 
 import gov.nasa.arc.mct.services.config.impl.properties.ArrayProperty;
 import gov.nasa.arc.mct.services.config.impl.properties.SimpleProperty;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import org.osgi.framework.Constants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @XmlRootElement(name="service")
 @XmlType(propOrder={"simpleProps", "arrayProps"})
 @XmlAccessorType(XmlAccessType.PROPERTY)
 class Service {
 	// Default access so unit tests can mock this.
 	static Logger log = LoggerFactory.getLogger(Service.class);
 
 	// This is package private instead of private so that unit tests can
 	// see it.
 	private String serviceID;
 
 	// This is package private instead of private so that unit tests can
 	// see it.
     private List<SimpleProperty> simpleProps = new ArrayList<SimpleProperty>();
 
 	// This is package private instead of private so that unit tests can
 	// see it.
     private List<ArrayProperty> arrayProps = new ArrayList<ArrayProperty>();
 
 	@XmlTransient
    public Dictionary<String, ?> getProperties() {
     	Dictionary<String, Object> dict = new Hashtable<String, Object>();
     	
     	if (serviceID != null) {
     		dict.put(Constants.SERVICE_PID, serviceID);
     	}
     	
     	for (SimpleProperty p : simpleProps) {
 			try {
 				dict.put(p.getName(), p.getConvertedValue());
 			} catch (NumberFormatException e) {
 				dict.put(p.getName(), 0);
 				log.error("Conversion for property '{}' with value '{}' caused NumberFormatException. Setting this property to zero.", p.getName() ,p.getValue());
 			}
 		}
     	for (ArrayProperty p : arrayProps) {
     		dict.put(p.getName(), p.asArray());
     	}
     	
     	return dict;
     }
 
     @XmlAttribute(name="pid")
 	public String getServiceID() {
 		return serviceID;
 	}
 
 	public void setServiceID(String newServiceID) {
 		serviceID = newServiceID;
 	}
 
 	@XmlElement(name="property")
 	public List<SimpleProperty> getSimpleProps() {
 		return simpleProps;
 	}
 
 	public void setSimpleProps(List<SimpleProperty> simpleProps) {
 		this.simpleProps = simpleProps;
 	}
 
 	@XmlElement(name="array")
 	public List<ArrayProperty> getArrayProps() {
 		return arrayProps;
 	}
 
 	public void setArrayProps(List<ArrayProperty> arrayProps) {
 		this.arrayProps = arrayProps;
 	}
 
 }
