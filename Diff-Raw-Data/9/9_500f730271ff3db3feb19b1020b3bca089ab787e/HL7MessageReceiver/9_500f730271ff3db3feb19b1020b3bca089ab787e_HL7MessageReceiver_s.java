 package com.janpix.hl7dto.hl7.v3.messages;
 
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 
 import com.janpix.hl7dto.hl7.v3.datatypes.enums.*;
 
 
 public class HL7MessageReceiver {
 	@XmlElement(required = true)
     public Device device;
 	@XmlAttribute(name = "typeCode", required = true)
    public CommunicationFunctionType typeCode;
 }
