 
 package com.gratex.perconik.services.tag;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlType;
 
 
 /**
  * <p>Java class for SearchTagProfileResponse complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="SearchTagProfileResponse">
  *   &lt;complexContent>
  *     &lt;extension base="{http://www.gratex.com/PerConIk/TagAdm/ITagProfileWcfSvc}SearchResponse">
  *       &lt;sequence>
  *         &lt;element name="ResultSet" type="{http://www.gratex.com/PerConIk/TagAdm/ITagProfileWcfSvc}ArrayOfTagProfileSearchResItemDto" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/extension>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchTagProfileResponse2", propOrder = {
     "resultSet"
 })
 public class SearchTagProfileResponse2
     extends SearchResponse
 {
 
     @XmlElement(name = "ResultSet")
     protected ArrayOfTagProfileSearchResItemDto resultSet;
 
     /**
      * Gets the value of the resultSet property.
      * 
      * @return
      *     possible object is
      *     {@link ArrayOfTagProfileSearchResItemDto }
      *     
      */
     public ArrayOfTagProfileSearchResItemDto getResultSet() {
         return resultSet;
     }
 
     /**
      * Sets the value of the resultSet property.
      * 
      * @param value
      *     allowed object is
      *     {@link ArrayOfTagProfileSearchResItemDto }
      *     
      */
     public void setResultSet(ArrayOfTagProfileSearchResItemDto value) {
         this.resultSet = value;
     }
 
 }
