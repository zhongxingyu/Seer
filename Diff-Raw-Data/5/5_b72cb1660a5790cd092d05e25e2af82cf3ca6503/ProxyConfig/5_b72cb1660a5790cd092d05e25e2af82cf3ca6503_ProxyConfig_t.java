 //
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.6
 // Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen.
 // Generado el: PM.06.05 a las 06:16:21 PM CEST 
 //
 
 
 package es.eucm.eadventure.tracking.pub.config;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 
 /**
  * <p>Clase Java para anonymous complex type.
  * 
  * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
  * 
  * <pre>
  * &lt;complexType>
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="host-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}string"/>
  *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder = {
     "hostName",
     "port",
     "protocol"
 })
 @XmlRootElement(name = "proxy-config")
 public class ProxyConfig {
 
     @XmlElement(name = "host-name", required = true)
     protected String hostName;
     @XmlElement(required = true)
     protected String port;
     @XmlElement(defaultValue = "http")
     protected String protocol;
 
     /**
      * Obtiene el valor de la propiedad hostName.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getHostName() {
         return hostName;
     }
 
     /**
      * Define el valor de la propiedad hostName.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setHostName(String value) {
         this.hostName = value;
     }
 
     /**
      * Obtiene el valor de la propiedad port.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getPort() {
         return port;
     }
 
     /**
      * Define el valor de la propiedad port.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setPort(String value) {
         this.port = value;
     }
 
     /**
      * Obtiene el valor de la propiedad protocol.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getProtocol() {
         return protocol;
     }
 
     /**
      * Define el valor de la propiedad protocol.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setProtocol(String value) {
         this.protocol = value;
     }
 
 }
