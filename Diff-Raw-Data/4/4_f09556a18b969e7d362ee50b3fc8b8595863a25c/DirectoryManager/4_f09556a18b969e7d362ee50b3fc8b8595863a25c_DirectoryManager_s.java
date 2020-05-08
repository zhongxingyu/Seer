 /*
  * Copyright (C) 2013 CampusUB1 Development Team
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.dev.campus.directory;
 
 import java.io.IOException;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.directory.Contact.ContactType;
 import com.unboundid.ldap.sdk.Control;
 import com.unboundid.ldap.sdk.Filter;
 import com.unboundid.ldap.sdk.LDAPConnection;
 import com.unboundid.ldap.sdk.LDAPConnectionPool;
 import com.unboundid.ldap.sdk.LDAPException;
 import com.unboundid.ldap.sdk.SearchRequest;
 import com.unboundid.ldap.sdk.SearchResult;
 import com.unboundid.ldap.sdk.SearchResultEntry;
 import com.unboundid.ldap.sdk.SearchScope;
 import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
 
 public class DirectoryManager {
 	
 	private final int NUM_CONNECTIONS = 10;
 	private final int MAX_PAGE_SIZE = 10;
 	private final int UB1_LDAP_PORT = 389;
 	
 	private final String UB1_BASE_DN = "ou=people,dc=u-bordeaux1,dc=fr";
 	private final String UB1_LDAP_HOST = "carnet.u-bordeaux1.fr";
 	
 	private final String ATTR_MAIL = "mail";
 	private final String ATTR_TEL = "telephoneNumber";
 	private final String ATTR_NAME = "givenName";
 	private final String ATTR_SURNAME = "sn";
 	private final String[] LDAPSearchAttributes = {ATTR_MAIL, ATTR_TEL, ATTR_NAME, ATTR_SURNAME};
 
 	private LDAPConnection LDAP;
 	private LDAPConnectionPool mConnectionPool;
 
 	private List<Contact> mLabriContacts;
 	
 	public List<Contact> searchContact(String firstName, String lastName) throws LDAPException, IOException {
 		ArrayList<Contact> searchResult = new ArrayList<Contact>();
 		if (CampusUB1App.persistence.isSubscribedUB1()) 
 			searchResult.addAll(searchUB1(firstName, lastName));
 
 		if (CampusUB1App.persistence.isSubscribedLabri()) {
			if (mLabriContacts == null)
 				parseLabriDirectory();
 			searchResult.addAll(filterLabriResults(firstName, lastName));
 		}
 		return searchResult;
 	}
 
 	public List<Contact> searchUB1(String firstName, String lastName) throws LDAPException {
 		if (LDAP == null || mConnectionPool == null) {
 			LDAP = new LDAPConnection(UB1_LDAP_HOST, UB1_LDAP_PORT);
 			mConnectionPool = new LDAPConnectionPool(LDAP, NUM_CONNECTIONS);
 		}
 		
 		ArrayList<Contact> contacts = new ArrayList<Contact>();
 		Filter f = Filter.create("(&(" + ATTR_NAME + "=" + firstName + "*)("
 									   + ATTR_SURNAME + "=" + lastName + "*))");
 		
 		SearchRequest searchRequest = new SearchRequest(UB1_BASE_DN, SearchScope.SUB, f, LDAPSearchAttributes);
 		searchRequest.setControls(new Control[] { new SimplePagedResultsControl(MAX_PAGE_SIZE, null)});
 		SearchResult searchResult = mConnectionPool.search(searchRequest);
 		int entryCount = searchResult.getEntryCount();
 		
 		// Create Contact objects with the entries that are returned.
 		for (int i = 0; i < entryCount; i++) {
 			SearchResultEntry entry = searchResult.getSearchEntries().get(i);
 			Contact contact = new Contact();
 			
 			if ((entry.getAttributeValue(ATTR_MAIL) != null) && !entry.getAttributeValue(ATTR_MAIL).equals(""))
 				contact.setEmail(entry.getAttributeValue(ATTR_MAIL));
 			if ((entry.getAttributeValue(ATTR_TEL) != null) && !entry.getAttributeValue(ATTR_TEL).equals("Non renseigne"))
 				contact.setTel(entry.getAttributeValue(ATTR_TEL));
 			contact.setFirstName(entry.getAttributeValue(ATTR_NAME));
 			contact.setLastName(entry.getAttributeValue(ATTR_SURNAME));
 			contact.setType(ContactType.UB1_CONTACT);
 			contacts.add(contact);
 		}
 
 		return contacts;
 	}
 
 
 	public List<Contact> filterLabriResults(String firstName, String lastName){
 		ArrayList<Contact> matchingContacts = new ArrayList<Contact>();
 		if (firstName == null)
 			firstName = "";
 		if (lastName == null)
 			lastName = "";
 		firstName = reformatString(firstName);
 		lastName = reformatString(lastName);
 
 		for (Contact c : mLabriContacts) {
 			if (reformatString(c.getFirstName()).contains(firstName)
 			 && reformatString(c.getLastName()).contains(lastName)) {
 				matchingContacts.add(c);
 			}
 		}
 		return matchingContacts;
 	}
 
 	public void parseLabriDirectory() throws IOException {
 		ArrayList<Contact> allContacts = new ArrayList<Contact>();
 		Document doc;
 
 		try {
 			doc = Jsoup.connect("http://www.labri.fr/index.php?n=Annuaires.Noms&initiale=tout").get();
 		} catch (Exception e) {
 			//Either java.net.SocketTimeoutException or org.jsoup.HttpStatusException
 			CampusUB1App.LogD("Failed to retrieve LaBRI contacts");
 			mLabriContacts = allContacts;
 			return;
 		}
 
 		Elements tables = doc.select("table[border=1][cellpadding=4][cellspacing=0][width=100%]");
 		Element table = tables.first();
 
 		int i = 1;
 		String buffer;
 		Contact contact = new Contact();
 
 		Elements tds = table.getElementsByTag("td");
 		for (Element td : tds) {
 			buffer = td.text();
 
 			if (i > 8) {
 				if (i % 8 == 1) { // Name
 					String name = buffer;
 					int offset = name.lastIndexOf (" "); // Split first name/last name with last space
 					if (offset != -1) {
 						String lastName = name.substring(0, offset);
 						String firstName = name.substring(offset+1, name.length());
 						contact.setFirstName(firstName);
 						contact.setLastName(lastName);
 						contact.setType(ContactType.LABRI_CONTACT);
 					}
 				}
 				else if(i % 8 == 2) { // Email
 					String email = td.getElementsByTag("a").attr("href");
 					email = email.substring(7); // remove: "mailto:"
 					if (!email.equals(""))
 						contact.setEmail(email);
 				}
 				else if (i % 8 == 3) { // Telephone
 					String tel = buffer;
 					if (!tel.equals("+33 (0)5 40 00")) { // Default value: "+33 (0)5 40 00 "
 						tel = tel.replaceAll("\\(0\\)", "");
 						contact.setTel(tel);
 					}
 				}
 				else if (i % 8 == 7) { // Website
 					contact.setWebsite(td.getElementsByTag("a").attr("href"));
 				}
 				else if (i % 8 == 0 && i > 0) {
 					allContacts.add(contact);
 					contact = new Contact();
 				}
 			}
 			i++;
 		}
 
 		mLabriContacts = allContacts;
 	}
 
 	public String reformatString(String str) {
 		//strip accents
 		str = Normalizer.normalize(str, Normalizer.Form.NFD);
 		str = str.replaceAll("[^\\p{ASCII}]", "");
 		return str.toLowerCase();
 	}
 }
