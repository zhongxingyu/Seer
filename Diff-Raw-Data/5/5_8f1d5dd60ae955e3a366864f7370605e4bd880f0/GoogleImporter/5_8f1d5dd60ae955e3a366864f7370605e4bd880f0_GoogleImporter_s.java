 package de.fhb.autobday.manager.connector.google;
 
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.contacts.Gender.Value;
 import com.google.gdata.data.contacts.*;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.util.ServiceException;
 import de.fhb.autobday.commons.GoogleBirthdayConverter;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdGroupToContactFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdGroupToContact;
 import de.fhb.autobday.exception.CanNotConvetGoogleBirthdayException;
 import de.fhb.autobday.manager.connector.AImporter;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * class to import the contact information and map the contacts to us contacts
  * write the contact information in the database
  *
  * variablename if the variable start with abd this is an Auto-B-Day model
  *
  * @author Tino Reuschel
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  */
 public class GoogleImporter extends AImporter {
 
 	private final static Logger LOGGER = Logger.getLogger(GoogleImporter.class.getName());
 	protected boolean connectionEtablished;
 	protected AbdAccount accdata;
 	protected ContactsService myService;
 
 	public GoogleImporter() {
 		connectionEtablished = false;
 		accdata = null;
 		myService = null;
 	}
 
 	@Override
 	/**
 	 * (non-Javadoc)
 	 * @see de.fhb.autobday.manager.connector.AImporter#getConnection(de.fhb.autobday.data.AbdAccount)
 	 * create connection to google contact api
 	 */
 	public void getConnection(AbdAccount data) {
 		
 		LOGGER.info("getConnection");
 		LOGGER.log(Level.INFO, "data :{0}", data.getId());
 		
 		connectionEtablished = false;
 		accdata = data;
 
 		// testausgabe
 		System.out.println("Username: " + accdata.getUsername());
 		System.out.println("Passwort: " + accdata.getPasswort());
 		System.err.println("WARNING: User credentials not be used by connector!");
 
 		//connect to google
 		try {
 			myService = new ContactsService("BDayReminder");
 			myService.setUserCredentials("fhbtestacc@googlemail.com", "TestGoogle123");
 
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 		connectionEtablished = true;
 	}
 	
 	@Override
 	public void importContacts() {
 		
 		LOGGER.info("importContacts");
 		
 		String groupid,groupname;
 		//TODO Template einbinden
 		String grouptemplate="Hier soll das Template rein";
 		Boolean active=true;
 		AbdGroup abdgroupEntry;
 		AbdContact abdcontact;
 		AbdContact abdcontacthelp;
 		AbdGroupFacade abdGroupFacade = new AbdGroupFacade();
 		AbdContactFacade abdContactFacade = new AbdContactFacade();
 		
 		List<ContactGroupEntry> groups;
 		List<AbdGroup> abdgroups;
 		List<ContactEntry> contacts;
 		List<GroupMembershipInfo> groupMembershipInfo;
 		
 		// if we have a connection and a valid accounddata then import the contacts and groups
 		// else throw an exception
 		if (connectionEtablished && accdata != null) {
 			
 				// get all information
 				groups = getAllGroups();
 				abdgroups = new ArrayList<AbdGroup>(accdata.getAbdGroupCollection());
 				contacts = getAllContacts();
 				
 				
 				
 				for (ContactGroupEntry groupentry : groups) {
 					groupid = groupentry.getId();
 					// if the group dont exist, create the group in the database
 					if (!existGroup(abdgroups,groupid)){
 						abdgroupEntry = new AbdGroup(groupid);
 						groupname=groupentry.getTitle().getPlainText();
 						abdgroupEntry.setName(groupname);
 						abdgroupEntry.setTemplate(grouptemplate);
 						abdgroupEntry.setActive(active);
 						abdGroupFacade.create(abdgroupEntry);
 					}
 				}
 				
 				for (ContactEntry contactEntry : contacts) {
 					abdcontact = mapGContactToContact(contactEntry);
 					//look in the database if the contact exist
 					abdcontacthelp=abdContactFacade.find(abdcontact.getId());
 					if (abdcontacthelp ==  null){
 						if ((abdcontact.getBday()!= null)&&(!abdcontact.getMail().equals(""))) {
 							abdContactFacade.create(abdcontact);
 						}
 					} else {
 						// check if data has been modify
 						if (!abdcontact.equals(abdcontacthelp)){
 							abdContactFacade.edit(abdcontact);
 						}
 					}
 					groupMembershipInfo=contactEntry.getGroupMembershipInfos();
 					updateGroupMembership(contactEntry.getId(),groupMembershipInfo);
 				}
 		} else {
 			//TODO Exception ersetzen
 			throw new UnsupportedOperationException("Please Connect the service first.");
 		}
 	}
 	
 	/**
 	 * get all groups from google from the connected account
 	 * 
	 * if don´t get information from google return null else a list of Google ContactGroupEntrys
 	 * 
 	 */
 	protected List<ContactGroupEntry> getAllGroups() {
 	
 		LOGGER.info("getAllGroups");
 		
 		URL feedUrl;
 		try {
 			//url to get all groups
 			feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full");
 			ContactGroupFeed resultFeed = myService.getFeed(feedUrl, ContactGroupFeed.class);	
 			if (resultFeed == null){
 				return null;
 			}
 			return resultFeed.getEntries();
 		
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return null;
 	}
 
 	/**
 	 * get all contacts from the connected acoount
 	 * 
	 * if don´t get information from google return null else a list of Google ContactEntrys
 	 * 
 	 */
 	protected List<ContactEntry> getAllContacts() {
 
 		LOGGER.info("getAllContacts");
 
 		URL feedUrl;
 		try {
 			feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
 			ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);
 			if (resultFeed == null){
 				return null;
 			}
 			return resultFeed.getEntries();
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return null;
 	}
 
 	/**
 	 * @return the connectionEtablished
 	 */
 	public boolean isConnectionEtablished() {
 		return connectionEtablished;
 	}
 
 	/**
 	 * @param connectionEtablished the connectionEtablished to set
 	 */
 	public void setConnectionEtablished(boolean connectionEtablished) {
 		this.connectionEtablished = connectionEtablished;
 	}
 
 	/**
 	 * @return the accdata
 	 */
 	public AbdAccount getAccdata() {
 		return accdata;
 	}
 
 	/**
 	 * @param accdata the accdata to set
 	 */
 	public void setAccdata(AbdAccount accdata) {
 		this.accdata = accdata;
 	}
 
 	/**
 	 * @return the myService
 	 */
 	public ContactsService getMyService() {
 		return myService;
 	}
 
 	/**
 	 * @param myService the myService to set
 	 */
 	public void setMyService(ContactsService myService) {
 		this.myService = myService;
 	}
 
 	/**
 	 * methode to map a google contact to a auto-b-day contact
 	 * 
 	 * @param ContactEntry contactEntry
 	 * 
 	 * @return AbdContact
 	 * 
 	 */
 	protected AbdContact mapGContactToContact(ContactEntry contactEntry){
 		
 		LOGGER.info("mapGContacttoContact");
 		LOGGER.log(Level.INFO, "contactEntry :{0}", contactEntry.getId());
 		
 		AbdContact contact;
 		String firstname;
 		String name;
 		Date birthday;
 		String mailadress;
 		String id;
 
 		contact = new AbdContact();
 		id = contactEntry.getId();
 		contact.setId(id);
 		firstname = getGContactFirstname(contactEntry);
 		contact.setFirstname(firstname);
 		name = getGContactFamilyname(contactEntry);
 		contact.setName(name);
 		birthday = getGContactBirthday(contactEntry);
 		if (birthday != null) {
 			contact.setBday(birthday);
 		} 
 		if (!contactEntry.getEmailAddresses().isEmpty()) {
 			mailadress = getGContactFirstMailAdress(contactEntry);
 			contact.setMail(mailadress);
 		}
 		if (contactEntry.getGender().getValue() == Value.FEMALE) {
 			contact.setSex('w');
 		} else {
 			contact.setSex('m');
 		}
 		return contact;
 	}
 
 	/**
 	 * check if the Group exist in the database
 	 * and return the group
 	 * 
 	 * @param List<AbdGroup> griups
 	 * @param String group
 	 * 
 	 * @return AbdGroup
 	 */
 	protected Boolean existGroup(List<AbdGroup> groups, String group){
 		
 		LOGGER.info("existGroup");
 		LOGGER.log(Level.INFO, "group :{0}", group);
 		
 		for (AbdGroup abdgroup : groups) {
 			if (abdgroup.getId().equals(group)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * update the membership of a contact
 	 * 
 	 * @param String contactid
 	 * @param List<GroupMembershipInfo> groupMembership 
 	 */
 	protected void updateGroupMembership(String id, List<GroupMembershipInfo> groupMemberships){
 		AbdGroupToContactFacade abdGroupToContactFacade = new AbdGroupToContactFacade();
 		AbdGroupToContact abdGroupToContactEntity;
 		AbdContactFacade abdContactFacade = new AbdContactFacade();
 		AbdGroupFacade abdGroupFacade = new AbdGroupFacade();
 		List<AbdGroupToContact> abdGroupMemberships = new ArrayList<AbdGroupToContact> (abdGroupToContactFacade.findGroupByContact(id));
 		int i=0;
 		
 		// check if the membership exist and remove the membership out of the list
 		while (i < groupMemberships.size() ) {
 			if(diffMembership(groupMemberships.get(i).getHref(), abdGroupMemberships)){
 				groupMemberships.remove(i);
 			} else {
 				i=i+1;
 			}
 		}		
 		// delete all unused memberships
 		if (!abdGroupMemberships.isEmpty()){
 			for (AbdGroupToContact abdGroupToContact : abdGroupMemberships) {
 				abdGroupToContactFacade.remove(abdGroupToContact);
 			}
 		}
 		//create new memberships
 		if (!groupMemberships.isEmpty()){
 			for (GroupMembershipInfo groupMembershipInfo : groupMemberships) {
 				abdGroupToContactEntity = new AbdGroupToContact();
 				abdGroupToContactEntity.setAbdContact(abdContactFacade.find(id));
 				abdGroupToContactEntity.setAbdGroup(abdGroupFacade.find(groupMembershipInfo.getHref()));
 				abdGroupToContactEntity.setActive(true);
 				abdGroupToContactFacade.create(abdGroupToContactEntity);
 			}
 		}
 	}
 	
 	/**
 	 * if the membership exist remove the membership out of the list of the exist memberships
 	 * 
 	 * return a boolean if the membership exist
 	 * 
 	 * @param String groupid
 	 * @param List<AbdGroupToContact> abdGroupMembership
 	 * 
 	 * @return boolean
 	 */
 	protected boolean diffMembership(String groupid, List<AbdGroupToContact> abdGroupMembership){
 		AbdGroup abdGroup;
 		for (int i = 0; i < abdGroupMembership.size(); i++) {
 			abdGroup = abdGroupMembership.get(i).getAbdGroup();
 			if(abdGroup.getId().equals(groupid)){
 				abdGroupMembership.remove(i);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * methode that return the firstname of a given contact
 	 * 
 	 * @param ContactEntry contactEntry
 	 * 
 	 * @return String
 	 */
 	protected String getGContactFirstname(ContactEntry contactEntry){
 		String firstname;
 		firstname=contactEntry.getName().getGivenName().getValue();
 		return firstname;
 		
 	}
 	
 	/**
 	 * methode that return the familyname of a given Contact
 	 * 
 	 * @param ContactEntry contactEntry
 	 * 
 	 * @return String
 	 */
 	protected String getGContactFamilyname(ContactEntry contactEntry){
 		String familyname;
 		familyname=contactEntry.getName().getFamilyName().getValue();
 		return familyname;
 		
 	}
 	
 	/**
 	 * methode that return the birthday of a given Contact
 	 * 
 	 * @param ContactEntry contactEntry
 	 * 
 	 * @ return Date
 	 */
 	protected Date getGContactBirthday(ContactEntry contactEntry){
 		String gContactBirthday = contactEntry.getBirthday().getValue();
 		try {
 		return GoogleBirthdayConverter.convertBirthday(gContactBirthday);
 		} catch (CanNotConvetGoogleBirthdayException e){
 			return null;
 		}
 	}
 	
 	/**
 	 * Methode that return a mailadress of a given Contact
 	 * 
 	 * @param ContactEntry contactEntry
 	 * 
 	 * @return String
 	 */
 	protected String getGContactFirstMailAdress(ContactEntry contactEntry){
 		List<Email> mailadresses;
 		String mailadress;
 		mailadresses = contactEntry.getEmailAddresses();
 		if (mailadresses.size()>0){
 			mailadress = mailadresses.get(0).getAddress();
 			return mailadress;
 		}
 		return "";
 	}
 }
