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
 import com.unboundid.ldap.sdk.LDAPException;
 import com.unboundid.ldap.sdk.SearchRequest;
 import com.unboundid.ldap.sdk.SearchResult;
 import com.unboundid.ldap.sdk.SearchResultEntry;
 import com.unboundid.ldap.sdk.SearchScope;
 import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
 
 public class DirectoryManager {
 
 	private final String UB1_BASE_DN = "ou=people,dc=u-bordeaux1,dc=fr";
 	private final String UB1_LDAP_HOST = "carnet.u-bordeaux1.fr";
 	private final int UB1_LDAP_PORT = 389;
 
 	private LDAPConnection LDAP;
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
 		ArrayList<Contact> contacts = new ArrayList<Contact>();
 		LDAP = new LDAPConnection(UB1_LDAP_HOST, UB1_LDAP_PORT);
 		Filter f = Filter.create("(&(givenName=" + firstName + "*)(sn=" + lastName + "*))");
 		String[] attributes = {"mail", "telephoneNumber", "givenName", "sn"};
 
 		SearchRequest searchRequest = new SearchRequest(UB1_BASE_DN, SearchScope.SUB, f, attributes);
 
 		searchRequest.setControls(new Control[] { new SimplePagedResultsControl(10, null)});
 		SearchResult searchResult = LDAP.search(searchRequest);
 		int entryCount = searchResult.getEntryCount();
 		// Do something with the entries that are returned.
 		if (entryCount > 0) {
 			for (int contact_nb = 0; contact_nb < entryCount; contact_nb++) {
 				SearchResultEntry entry = searchResult.getSearchEntries().get(contact_nb);
 				Contact contact = new Contact();
				if (!(entry.getAttributeValue("mail") == null) && !entry.getAttributeValue("mail").equals(""))
 					contact.setEmail(entry.getAttributeValue("mail"));
				if (!(entry.getAttributeValue("telephoneNumber") == null) && !entry.getAttributeValue("telephoneNumber").equals("Non renseigne"))
 					contact.setTel(entry.getAttributeValue("telephoneNumber"));
 				contact.setFirstName(entry.getAttributeValue("givenName"));
 				contact.setLastName(entry.getAttributeValue("sn"));
 				contact.setType(ContactType.UB1_CONTACT);
 				contacts.add(contact);
 			}
 		}
 
 		return contacts;
 	}
 
 
 	public List<Contact> filterLabriResults(String firstName, String lastName){
 		ArrayList<Contact> matchingContacts = new ArrayList<Contact>();
 		if (firstName == null)
 			firstName = "";
 		if (lastName == null)
 			lastName = "";
 		firstName = removeAccents(firstName).toLowerCase();
 		lastName = removeAccents(lastName).toLowerCase();
 
 		for (Contact c : mLabriContacts) {
 			if (removeAccents(c.getFirstName()).toLowerCase().contains(firstName)
 					&& removeAccents(c.getLastName()).toLowerCase().contains(lastName)) {
 				matchingContacts.add(c);
 			}
 		}
 		return matchingContacts;
 	}
 
 	public void parseLabriDirectory() throws IOException {
 		ArrayList<Contact> allContacts = new ArrayList<Contact>();
 		Document doc = Jsoup.connect("http://www.labri.fr/index.php?n=Annuaires.Noms&initiale=tout").get();
 
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
 				else if (i % 8 == 3) { // Telephone, Default : "+33 (0)5 40 00 "
 					String tel = buffer;
 					if (!tel.equals("+33 (0)5 40 00")) {
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
 
 	public String removeAccents(String str) {
 		str = Normalizer.normalize(str, Normalizer.Form.NFD);
 		str = str.replaceAll("[^\\p{ASCII}]", "");
 		return str;
 	}
 
 	public String capitalize(String str) {
 		str = str.toLowerCase();
 		boolean charReplaced = false;
 		for (int k = 0; k < str.length(); k++) {
 			char currentChar = str.charAt(k);
 			if (currentChar < 97 || currentChar > 122) // detecting new word, currentChar not in [a-z]
 				charReplaced = false;
 			if (charReplaced == false && (currentChar > 96 && currentChar < 123)) { // currentChar in [a-z]
 				str = str.substring(0, k) + str.substring(k, k+1).toUpperCase() + str.substring(k+1); // capitalize currentChar in string
 				charReplaced = true;
 			}
 		}
 		return str;
 	}
 }
