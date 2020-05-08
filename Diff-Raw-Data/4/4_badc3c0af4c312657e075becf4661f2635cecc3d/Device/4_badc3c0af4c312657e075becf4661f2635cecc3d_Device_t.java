 package com.janpix.hl7dto.hl7.v3.messages;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlType;
 
 import com.janpix.hl7dto.hl7.v3.datatypes.II;
 import com.janpix.hl7dto.hl7.v3.datatypes.TEL;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name="Device")
 public class Device {
 	@XmlAttribute(name = "determinerCode", required = true)
    public String determinerCode = "INSTANCE";
	@XmlAttribute(name = "classCode")
	public String classCode = "DEV";
 	@XmlElement(required = true)
     public List<II> id;
     public List<TEL> telecom;
     
     public Device() {
     	id = new ArrayList<II>();
     	telecom = new ArrayList<TEL>();
     }
 }
