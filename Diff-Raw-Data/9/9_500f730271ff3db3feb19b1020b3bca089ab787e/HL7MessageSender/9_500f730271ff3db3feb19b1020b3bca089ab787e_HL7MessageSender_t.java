 package com.janpix.hl7dto.hl7.v3.messages;
 
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import com.janpix.hl7dto.hl7.v3.datatypes.enums.CommunicationFunctionType;
 
 
 
 public class HL7MessageSender {
 	@XmlElement(required = true)
     public Device device;
 	@XmlAttribute(name = "typeCode", required = true)
    public CommunicationFunctionType typeCode = CommunicationFunctionType.SND;
 }
