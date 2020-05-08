 package com.miicard.consumers.service.v1.claims.impl;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import com.miicard.consumers.service.v1.claims.api.EmailAddress;
 import com.miicard.consumers.service.v1.claims.api.Identity;
 import com.miicard.consumers.service.v1.claims.api.MiiUserProfile;
 import com.miicard.consumers.service.v1.claims.api.PhoneNumber;
 import com.miicard.consumers.service.v1.claims.api.PostalAddress;
 
 /**
  * <p>Java class for MiiUserProfile complex type.
  * 
  * <p>The following schema fragment specifies the expected content contained within this class.
  * 
  * <pre>
  * &lt;complexType name="MiiUserProfile">
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element name="CardImageUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="DateOfBirth" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
  *         &lt;element name="EmailAddresses" type="{http://schemas.datacontract.org/2004/07/miiCard.STS.Model.Api}ArrayOfEmailAddress" minOccurs="0"/>
  *         &lt;element name="FirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="HasPublicProfile" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
  *         &lt;element name="Identities" type="{http://schemas.datacontract.org/2004/07/miiCard.STS.Model.Api}ArrayOfIdentity" minOccurs="0"/>
  *         &lt;element name="IdentityAssured" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
  *         &lt;element name="LastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="LastVerified" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
  *         &lt;element name="MiddleName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="PhoneNumbers" type="{http://schemas.datacontract.org/2004/07/miiCard.STS.Model.Api}ArrayOfPhoneNumber" minOccurs="0"/>
  *         &lt;element name="PostalAddresses" type="{http://schemas.datacontract.org/2004/07/miiCard.STS.Model.Api}ArrayOfPostalAddress" minOccurs="0"/>
  *         &lt;element name="PreviousFirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="PreviousLastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="PreviousMiddleName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="ProfileShortUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="ProfileUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="PublicProfile" type="{http://schemas.datacontract.org/2004/07/miiCard.STS.Model.Api}MiiUserProfile" minOccurs="0"/>
  *         &lt;element name="Salutation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *         &lt;element name="Username" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "MiiUserProfile", propOrder = {
     "cardImageUrl",
     "dateOfBirth",
     "emailAddresses",
     "firstName",
     "hasPublicProfile",
     "identities",
     "identityAssured",
     "lastName",
     "lastVerified",
     "middleName",
     "phoneNumbers",
     "postalAddresses",
     "previousFirstName",
     "previousLastName",
     "previousMiddleName",
     "profileShortUrl",
     "profileUrl",
     "publicProfile",
     "salutation",
     "username"
 })
 public class MiiUserProfileImpl implements MiiUserProfile {
 
     @XmlElement(name = "CardImageUrl", nillable = true)
     protected String cardImageUrl;
     
     @XmlElement(name = "DateOfBirth", nillable = true)
     @XmlSchemaType(name = "dateTime")
     protected XMLGregorianCalendar dateOfBirth;
     
     @XmlElement(name = "EmailAddresses", nillable = true)
     protected ArrayOfEmailAddress emailAddresses;
     
     @XmlElement(name = "FirstName", nillable = true)
     protected String firstName;
     
     @XmlElement(name = "HasPublicProfile")
     protected Boolean hasPublicProfile;
     
     @XmlElement(name = "Identities", nillable = true)
     protected ArrayOfIdentity identities;
     
     @XmlElement(name = "IdentityAssured")
     protected Boolean identityAssured;
     
     @XmlElement(name = "LastName", nillable = true)
     protected String lastName;
     
     @XmlElement(name = "LastVerified")
     @XmlSchemaType(name = "dateTime")
     protected XMLGregorianCalendar lastVerified;
     
     @XmlElement(name = "MiddleName", nillable = true)
     protected String middleName;
     
     @XmlElement(name = "PhoneNumbers", nillable = true)
     protected ArrayOfPhoneNumber phoneNumbers;
     
     @XmlElement(name = "PostalAddresses", nillable = true)
     protected ArrayOfPostalAddress postalAddresses;
     
     @XmlElement(name = "PreviousFirstName", nillable = true)
     protected String previousFirstName;
     
     @XmlElement(name = "PreviousLastName", nillable = true)
     protected String previousLastName;
     
     @XmlElement(name = "PreviousMiddleName", nillable = true)
     protected String previousMiddleName;
     
     @XmlElement(name = "ProfileShortUrl", nillable = true)
     protected String profileShortUrl;
     
     @XmlElement(name = "ProfileUrl", nillable = true)
     protected String profileUrl;
     
     @XmlElement(name = "PublicProfile", nillable = true)
     protected MiiUserProfileImpl publicProfile;
     
     @XmlElement(name = "Salutation", nillable = true)
     protected String salutation;
     
     @XmlElement(name = "Username", nillable = true)
     protected String username;
 
     /**
      * Gets the value of the cardImageUrl property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getCardImageUrl() {
         return cardImageUrl;
     }
 
     /**
      * Sets the value of the cardImageUrl property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setCardImageUrl(
     		final String value) {
         
     	this.cardImageUrl = value;
     }
 
     /**
      * Gets the value of the emailAddresses property.
      * 
      * @return possible object is
      * {@link ArrayOfEmailAddress }
      *     
      */
     public final ArrayOfEmailAddress getArrayOfEmailAddress() {
         return emailAddresses;
     }
 
     /**
      * Sets the value of the emailAddresses property.
      * 
      * @param value allowed object is
      * {@link ArrayOfEmailAddress }
      *     
      */
     public final void setArrayOfEmailAddress(
     		final ArrayOfEmailAddress value) {
         
     	this.emailAddresses = value;
     }
 
     /**
      * Gets the value of the firstName property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getFirstName() {
         return firstName;
     }
 
     /**
      * Sets the value of the firstName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setFirstName(
     		final String value) {
         
     	this.firstName = value;
     }
 
     /**
      * Gets the value of the hasPublicProfile property.
      * 
      * @return possible object is
      * {@link Boolean }
      *     
      */
     public final Boolean hasPublicProfile() {
         return hasPublicProfile;
     }
 
     /**
      * Sets the value of the hasPublicProfile property.
      * 
      * @param value allowed object is
      * {@link Boolean }
      *     
      */
     public final void setHasPublicProfile(
     		final Boolean value) {
         
     	this.hasPublicProfile = value;
     }
 
     /**
      * Gets the value of the identities property.
      * 
      * @return possible object is
      * {@link ArrayOfIdentity }
      *     
      */
     public final ArrayOfIdentity getArrayOfIdentity() {
         return identities;
     }
 
     /**
      * Sets the value of the identities property.
      * 
      * @param value allowed object is
      * {@link ArrayOfIdentity }
      *     
      */
     public final void setArrayOfIdentity(
     		final ArrayOfIdentity value) {
         
     	this.identities = value;
     }
 
     /**
      * Gets the value of the identityAssured property.
      * 
      * @return possible object is
      * {@link Boolean }
      *     
      */
     public final Boolean isIdentityAssured() {
         return identityAssured;
     }
 
     /**
      * Sets the value of the identityAssured property.
      * 
      * @param value allowed object is
      * {@link Boolean }
      *     
      */
     public final void setIdentityAssured(
     		final Boolean value) {
         
     	this.identityAssured = value;
     }
 
     /**
      * Gets the value of the lastName property.
      * 
      * @return possible object is 
      * {@link String }
      *     
      */
     public final String getLastName() {
         return lastName;
     }
 
     /**
      * Sets the value of the lastName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setLastName(
     		final String value) {
         
     	this.lastName = value;
     }
 
     /**
      * Gets the value of the lastVerified property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final Date getLastVerified() {
         return lastVerified.toGregorianCalendar().getTime();
     }
 
     /**
      * Sets the value of the lastVerified property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setLastVerified(
     		final Date value) {
     	GregorianCalendar g = new GregorianCalendar();
     	g.setTime(value);
     	
     	try 
     	{
 			this.lastVerified = DatatypeFactory.newInstance().newXMLGregorianCalendar(g);
 		} 
     	catch (DatatypeConfigurationException e) 
     	{
 		}
     }
     
     /**
      * Gets the value of the dateOfBirth property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final Date getDateOfBirth() {
		if (dateOfBirth != null)
		{
        	return dateOfBirth.toGregorianCalendar().getTime();
		}
		else
		{
			return null;
		}
     }
 
     /**
      * Sets the value of the dateOfBirth property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setDateOfBirth(
     		final Date value) {
     	GregorianCalendar g = new GregorianCalendar();
     	g.setTime(value);
     	
     	try 
     	{
 			this.dateOfBirth = DatatypeFactory.newInstance().newXMLGregorianCalendar(g);
 		} 
     	catch (DatatypeConfigurationException e) 
     	{
 		}
     }
 
     /**
      * Gets the value of the middleName property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getMiddleName() {
         return middleName;
     }
 
     /**
      * Sets the value of the middleName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setMiddleName(
     		final String value) {
         
     	this.middleName = value;
     }
 
     /**
      * Gets the value of the phoneNumbers property.
      * 
      * @return possible object is
      * {@link ArrayOfPhoneNumber }
      *     
      */
     public final ArrayOfPhoneNumber getArrayOfPhoneNumber() {
         return phoneNumbers;
     }
 
     /**
      * Sets the value of the phoneNumbers property.
      * 
      * @param value allowed object is
      * {@link ArrayOfPhoneNumber }
      *     
      */
     public final void setArrayOfPhoneNumber(
     		final ArrayOfPhoneNumber value) {
         
     	this.phoneNumbers = value;
     }
 
     /**
      * Gets the value of the postalAddresses property.
      * 
      * @return possible object is
      * {@link ArrayOfPostalAddress }
      *     
      */
     public final ArrayOfPostalAddress getArrayOfPostalAddress() {
         return postalAddresses;
     }
 
     /**
      * Sets the value of the postalAddresses property.
      * 
      * @param value allowed object is
      * {@link ArrayOfPostalAddress }
      *     
      */
     public final void setArrayOfPostalAddress(
     		final ArrayOfPostalAddress value) {
         
     	this.postalAddresses = value;
     }
 
     /**
      * Gets the value of the previousFirstName property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getPreviousFirstName() {
         return previousFirstName;
     }
 
     /**
      * Sets the value of the previousFirstName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setPreviousFirstName(
     		final String value) {
         
     	this.previousFirstName = value;
     }
 
     /**
      * Gets the value of the previousLastName property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getPreviousLastName() {
         return previousLastName;
     }
 
     /**
      * Sets the value of the previousLastName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setPreviousLastName(
     		final String value) {
         
     	this.previousLastName = value;
     }
 
     /**
      * Gets the value of the previousMiddleName property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getPreviousMiddleName() {
         return previousMiddleName;
     }
 
     /**
      * Sets the value of the previousMiddleName property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setPreviousMiddleName(
     		final String value) {
         
     	this.previousMiddleName = value;
     }
 
     /**
      * Gets the value of the profileShortUrl property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getProfileShortUrl() {
         return profileShortUrl;
     }
 
     /**
      * Sets the value of the profileShortUrl property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setProfileShortUrl(
     		final String value) {
         
     	this.profileShortUrl = value;
     }
 
     /**
      * Gets the value of the profileUrl property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getProfileUrl() {
         return profileUrl;
     }
 
     /**
      * Sets the value of the profileUrl property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setProfileUrl(
     		final String value) {
         
     	this.profileUrl = value;
     }
 
     /**
      * Gets the value of the publicProfile property.
      * 
      * @return possible object is
      * {@link MiiUserProfileImpl }
      *     
      */
     public final MiiUserProfileImpl getPublicProfile() {
         return publicProfile;
     }
 
     /**
      * Sets the value of the publicProfile property.
      * 
      * @param value allowed object is
      * {@link MiiUserProfileImpl }
      *     
      */
     public final void setPublicProfile(
     		final MiiUserProfileImpl value) {
         
     	this.publicProfile = value;
     }
 
     /**
      * Gets the value of the salutation property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getSalutation() {
         return salutation;
     }
 
     /**
      * Sets the value of the salutation property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setSalutation(
     		final String value) {
         
     	this.salutation = value;
     }
 
     /**
      * Gets the value of the username property.
      * 
      * @return possible object is
      * {@link String }
      *     
      */
     public final String getUsername() {
         return username;
     }
 
     /**
      * Sets the value of the username property.
      * 
      * @param value allowed object is
      * {@link String }
      *     
      */
     public final void setUsername(
     		final String value) {
         
     	this.username = value;
     }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<? extends EmailAddress> getEmailAddresses() {
 		
 		if (this.getArrayOfEmailAddress() != null 
 				&& this.getArrayOfEmailAddress().getEmailAddress() != null) {
 			
 			return this.getArrayOfEmailAddress().getEmailAddress();
 		}
 		
 		return new ArrayList<EmailAddress>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<? extends Identity> getIdentities() {
 		
 		if(this.getArrayOfIdentity() != null 
 				&& this.getArrayOfIdentity().getIdentity() != null) {
 			
 			return this.getArrayOfIdentity().getIdentity();
 		}
 		
 		return new ArrayList<Identity>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<? extends PhoneNumber> getPhoneNumbers() {
 		
 		if (this.getArrayOfPhoneNumber() != null 
 				&& this.getArrayOfPhoneNumber().getPhoneNumber() != null) {
 			
 			return this.getArrayOfPhoneNumber().getPhoneNumber();
 		}
 		
 		return new ArrayList<PhoneNumber>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<? extends PostalAddress> getPostalAddresses() {
 		
 		if(this.getArrayOfPostalAddress() != null 
 				&& this.getArrayOfPostalAddress().getPostalAddress() != null) {
 			
 			return this.getArrayOfPostalAddress().getPostalAddress();
 		}
 		
 		return new ArrayList<PostalAddress>();
 	}
 }
