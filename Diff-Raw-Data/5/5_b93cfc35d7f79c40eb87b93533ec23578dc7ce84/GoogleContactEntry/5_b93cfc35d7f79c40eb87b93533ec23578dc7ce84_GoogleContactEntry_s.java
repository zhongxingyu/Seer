 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.google.contact.wrappers;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.gdata.client.Service;
 import com.google.gdata.data.Category;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.Person;
 import com.google.gdata.data.TextConstruct;
 import com.google.gdata.data.contacts.BillingInformation;
 import com.google.gdata.data.contacts.Birthday;
 import com.google.gdata.data.contacts.CalendarLink;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.Event;
 import com.google.gdata.data.contacts.Gender;
 import com.google.gdata.data.contacts.Hobby;
 import com.google.gdata.data.contacts.Language;
 import com.google.gdata.data.contacts.Occupation;
 import com.google.gdata.data.contacts.Relation;
 import com.google.gdata.data.contacts.Subject;
 import com.google.gdata.data.contacts.UserDefinedField;
 import com.google.gdata.data.contacts.Website;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.data.extensions.ExtendedProperty;
 import com.google.gdata.data.extensions.FamilyName;
 import com.google.gdata.data.extensions.FullName;
 import com.google.gdata.data.extensions.GivenName;
 import com.google.gdata.data.extensions.Name;
 import com.google.gdata.data.extensions.Organization;
 import com.google.gdata.data.extensions.PhoneNumber;
 import com.google.gdata.data.extensions.PostalAddress;
 import com.google.gdata.data.extensions.StructuredPostalAddress;
 
 public class GoogleContactEntry extends GoogleContactBaseEntity<ContactEntry> {
 	
 	public GoogleContactEntry() {
 		super(new ContactEntry());
 	}
 	
 	public GoogleContactEntry(ContactEntry contactEntry) {
 		super(contactEntry != null ? contactEntry : new ContactEntry());
 	}
 	
 	public void setFamilyName(String familyName) {
 		Name name = getWrappedName();
 		
 		FamilyName fName = name.getFamilyName();
 		if (fName == null) {
 			fName = new FamilyName();
 			fName.setImmutable(false);
 		}
 		
 		fName.setValue(familyName);
 	}
 	
 	public String getFamilyName() {
 		return wrapped.getName() != null && wrapped.getName().getFamilyName() != null ? wrapped.getName().getFamilyName().getValue() : null;
 	}
 	
 	public void setGivenName(String givenName) {
 		Name name = getWrappedName();
 		
 		GivenName gName = name.getGivenName();
 		if (gName == null) {
 			gName = new GivenName();
 			gName.setImmutable(false);
 		}
 		
 		gName.setValue(givenName);
 	}
 	
 	public String getGivenName() {
 		return wrapped.getName() != null && wrapped.getName().getGivenName() != null ? wrapped.getName().getGivenName().getValue() : null;
 	}
 	
 	public void setFullName(String fullName) {
 		Name name = getWrappedName();
 		
 		FullName fName = name.getFullName();
 		if (fName == null) {
 			fName = new FullName();
 			fName.setImmutable(false);
 		}
 		
 		fName.setValue(fullName);
 	}
 		
 	// Only getter
 	public String getContactPhotoLink() {
 		return wrapped.getContactPhotoLink() != null ? wrapped.getContactPhotoLink().getHref() : null;
 	}
 	
 	public void setEmailAddresses(List<Email> emails) {
 		wrapped.setEmailAddresses(emails);
 	}
 	
 	public List<Email> getEmailAddresses() {
 		return wrapped.getEmailAddresses();
 	}
 	
 	/**
 	 * Because three different properties use the name inside the wrapper entity and if it does not exist it creates a new one, we need to prevent
 	 * concurrent problems isolating the retrieve/create in a synch method 
 	 * 
 	 * @return the existent/new Name assigned to the wrapped
 	 */
 	synchronized private Name getWrappedName() {
 		Name name = wrapped.getName();
 		if (name == null) {
 			name = new Name();
 			wrapped.setName(name);
 		}
 				
 		return name;
 	}
 	
 	public void setOrganizations(List<Organization> organizations) {
 		wrapped.setOrganizations(organizations);
 	}
 	
 	public List<Organization> getOrganizations() {
 		return wrapped.getOrganizations();
 	}
 	
 	public void setPostalAddresses(List<PostalAddress> postalAddresses) {
 		wrapped.setPostalAddresses(postalAddresses);
 	}
 	
 	public List<PostalAddress> getPostalAddresses() {
 		return wrapped.getPostalAddresses();
 	}
 	
 	// Only getter
 	public List<Person> getAuthors() {
 		return wrapped.getAuthors();
 	}
 	
 	public void setBillingInformation(BillingInformation billingInformation) {
 		wrapped.setBillingInformation(billingInformation);
 	}
 	
 	public BillingInformation getBillingInformation() {
 		return wrapped.getBillingInformation();
 	}
 	
 	public Birthday getBirthday() {
 		return wrapped.getBirthday();
 	}
 	
 	public void setBirthday(Birthday birthday) {
 		wrapped.setBirthday(birthday);
 	}
 
 	public void setCalendarLinks(List<CalendarLink> calendarLinks) {
 		if (calendarLinks != null) {
 			for (CalendarLink cl : calendarLinks) {
 				if (cl != null) wrapped.addCalendarLink(cl);
 			}
 		}
 	}
 	
 	public List<CalendarLink> getCalendarLinks() {
 		return wrapped.getCalendarLinks();
 	}
 	
 	// Only getter
 	public Set<Category> getCategories() {
 		return wrapped.getCategories();
 	}
 	
 	public List<Person> getContributors() {
 		return wrapped.getContributors();		
 	}
 	
 	public void setEvents(List<Event> events) {
 		if (events != null) {
 			for (Event e : events) {
 				if (e != null) wrapped.addEvent(e);
 			}
 		}
 	}
 	
 	public List<Event> getEvents() {
 		return wrapped.getEvents();
 		
 	}
 	
 	public void setExtendedProperties(List<ExtendedProperty> extendedProperties) {
 		if (extendedProperties != null) {
 			for (ExtendedProperty ep : extendedProperties) {
 				if (ep != null) wrapped.addExtendedProperty(ep);
 			}
 		}
 	}
 	
 	public List<ExtendedProperty> getExtendedProperties() {
 		return wrapped.getExtendedProperties();
 	}
 	
 	public void setGender(Gender gender) {
 		wrapped.setGender(gender);
 	}
 	
 	public Gender getGender() {
 		return wrapped.getGender();
 	}
 	
 	public void setHobbies(List<Hobby> hobbies) {
 		wrapped.setHobbies(hobbies);
 	}
 	
 	public List<Hobby> getHobbies() {
 		return wrapped.getHobbies();
 	}
 	
 	public void setLanguages(List<Language> languages) {
 		if (languages != null) {
 			for (Language l : languages) {
 				if (l != null) wrapped.addLanguage(l);
 			}
 		}
 	}
 	
 	public List<Language> getLanguages() {
 		return wrapped.getLanguages();
 		
 	}
 	
 	public void setLinks(List<Link> links) {
 		if (links != null) {
 			for (Link l : links) {
 				if (l != null) wrapped.addLink(l);
 			}
 		}
 	}
 	
 	public List<Link> getLinks() {
 		return wrapped.getLinks();
 	}
 	
 	public void setOccupation(Occupation occupation) {
 		wrapped.setOccupation(occupation);
 	}
 	
 	public Occupation getOccupation() {
 		return wrapped.getOccupation();		
 	}
 	
 	public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
 		wrapped.setPhoneNumbers(phoneNumbers);
 	}
 	
 	public List<PhoneNumber> getPhoneNumbers() {
 		return wrapped.getPhoneNumbers();
 	}
 	
 	public void setPublished(DateTime published) {
 		wrapped.setPublished(published);
 	}
 	
 	public DateTime getPublished() {
 		return wrapped.getPublished();
 	}
 	
 	public void setRelations(List<Relation> relations) {
 		if (relations != null) {
 			for (Relation r : relations) {
 				if (r != null) wrapped.addRelation(r);
 			}
 		}
 	}
 	
 	public List<Relation> getRelations() {
 		return wrapped.getRelations();
 	}
 	
 	public void setRights(TextConstruct rights) {
 		wrapped.setRights(rights);
 	}
 	
 	public TextConstruct getRights() {
 		return wrapped.getRights();
 	}
 	
 	public void setService(Service service) {
 		wrapped.setService(service);
 	}
 	
 	public Service getService() {
 		return wrapped.getService();
 	}
 	
 	public void setStructuredPostalAddresses(List<StructuredPostalAddress> structuredPostalAddresses) {
 		if (structuredPostalAddresses != null) {
 			for (StructuredPostalAddress spa : structuredPostalAddresses) {
 				if (spa != null) wrapped.addStructuredPostalAddress(spa);
 			}
 		}
 	}
 	
 	public List<StructuredPostalAddress> getStructuredPostalAddresses() {
 		return wrapped.getStructuredPostalAddresses();		
 	}
 	
 	public void setSubject(Subject subject) {
 		wrapped.setSubject(subject);
 	}
 	
 	public Subject getSubject() {
 		return wrapped.getSubject();		
 	}
 	
 	public void setSummary(TextConstruct summary) {
 		wrapped.setSummary(summary);
 	}
 	
 	public TextConstruct getSummary() {
 		return wrapped.getSummary();
 	}
 	
 	public void setUserDefinedFields(List<UserDefinedField> userDefinedFields) {
 		if (userDefinedFields != null) {
 			for (UserDefinedField udf : userDefinedFields) {
 				if (udf != null) wrapped.addUserDefinedField(udf);
 			}
 		}
 	}
 	
 	public List<UserDefinedField> getUserDefinedFields() {
 		return wrapped.getUserDefinedFields();		
 	}
 	
 	public String getVersionId() {
 		return wrapped.getVersionId();
 	}
 	
 	public void setWebsites(List<Website> websites) {
 		wrapped.setWebsites(websites);
 	}
 	
 	public List<Website> getWebsites() {
 		return wrapped.getWebsites();
 	}
 }
