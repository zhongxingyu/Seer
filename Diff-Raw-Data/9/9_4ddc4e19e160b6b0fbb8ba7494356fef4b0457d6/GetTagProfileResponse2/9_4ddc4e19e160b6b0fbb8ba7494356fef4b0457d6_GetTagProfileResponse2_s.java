 
 package com.gratex.perconik.services.tag;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlType;
 
 
 /**
  * <p>Java class for GetTagProfileResponse complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="GetTagProfileResponse">
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="Profile" type="{http://www.gratex.com/PerConIk/TagAdm/ITagProfileWcfSvc}TagProfileDto" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTagProfileResponse", propOrder = {
     "profile"
 })
 public class GetTagProfileResponse2 {
 
     @XmlElement(name = "Profile")
     protected TagProfileDto profile;
 
     /**
      * Gets the value of the profile property.
      * 
      * @return
      *     possible object is
      *     {@link TagProfileDto }
      *     
      */
     public TagProfileDto getProfile() {
         return profile;
     }
 
     /**
      * Sets the value of the profile property.
      * 
      * @param value
      *     allowed object is
      *     {@link TagProfileDto }
      *     
      */
     public void setProfile(TagProfileDto value) {
         this.profile = value;
     }
 
 }
