 /**
  * Qontacts Mobile Application
  * Qontacts is a mobile application that updates the address book contacts
  * to the new Qatari numbering scheme.
  * 
  * Copyright (C) 2010  Abdulrahman Saleh Alotaiba
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation version 3 of the License.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mawqey.qontacts.models;
 
 
 import i18n.QontactsResource;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.pim.PIM;
 import javax.microedition.pim.PIMException;
 
 import net.rim.blackberry.api.pdap.BlackBerryContact;
 import net.rim.blackberry.api.pdap.BlackBerryContactList;
 import net.rim.blackberry.api.pdap.BlackBerryPIM;
 
 import com.mawqey.qontacts.main.QontactsApplication;
 import com.mawqey.qontacts.screens.helpers.ContactsListData;
 
 public class ContactsModel {
 	
 	private BlackBerryContactList contactList;
 	public Hashtable attrToLabel;
 	
 	public ContactsModel() {
 		
 	}
 	
 	public boolean openContactsDB() {
 		if (this.contactList == null) {
 			try {
 				BlackBerryPIM bbPIM = (BlackBerryPIM) BlackBerryPIM.getInstance();
 				contactList = (BlackBerryContactList) bbPIM.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
 			} catch (PIMException e) {
 				System.out.println("Error: PIM issue " + e.getMessage());
 				return false;
 			} catch (SecurityException s) {
 	        	System.out.println("Error: Security issue " + s.getMessage());
 	        	return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean closeContactsDB() {
 		if (this.contactList != null) {
 	        try {
 	        	contactList.close();
 	        } catch (PIMException e) {
 	        	System.out.println("Error: PIM issue " + e.getMessage());
 	        	return false;
 	        } catch (SecurityException s) {
 	        	System.out.println("Error: Security issue " + s.getMessage());
 	        	return false;
 	        }
 		}
 		return true;
 	}
 	
 	public Vector getContactsItems() {
 		Vector contactItems = new Vector();
 		try {
 			contactItems = getUpdateableContacts(this.contactList.items());
 		} catch (PIMException e) {
 			System.out.println("Error: PIM issue " + e.getMessage());
 		} catch (SecurityException s) {
         	System.out.println("Error: Security issue " + s.getMessage());
         }
 		return contactItems;
 	}
 	
 	private boolean checkContact(BlackBerryContact contact) {
 		Hashtable updateableContactNumbers = updateableContactNumbers(contact);
 		if (updateableContactNumbers.size() > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	private Vector getUpdateableContacts(Enumeration contactEnum) {
 		Vector _items = new Vector();
 		
 		if (contactEnum == null) {
 			return _items;
 		}
 		
 		while (contactEnum.hasMoreElements()) {
 			BlackBerryContact _contact = (BlackBerryContact)contactEnum.nextElement();
 			if (checkContact(_contact)) {
 				_items.addElement(new ContactsListData(_contact, true));
 			}
 		}
 		
 		return _items;
 	}
 	
 	public Hashtable updateableContactNumbers(BlackBerryContact contact) {
 		Vector _originalNumbers = new Vector();
 		Vector _updatedNumbers = new Vector();
 		
 		Hashtable _retNumbers = new Hashtable();
 		
 		int telCount = contact.countValues(BlackBerryContact.TEL);
 		
 		if ( telCount > 0 ) {
 			for (int i = 0; i < telCount; i++) {
 				int _telAttributeInt = contact.getAttributes(BlackBerryContact.TEL, i);
 				Integer _telAttribute = new Integer(_telAttributeInt);
 				String _telNumberOriginal = contact.getString(BlackBerryContact.TEL, i);
 				String _telNumberUpdated = updateableNumber(_telNumberOriginal);
 				
 				Hashtable _tempOriginalNumberDetails = new Hashtable(3);
 				_tempOriginalNumberDetails.put("Attribute", _telAttribute);
 				_tempOriginalNumberDetails.put("Number", _telNumberOriginal);
 				_tempOriginalNumberDetails.put("Index", new Integer(i));
 				
 				_originalNumbers.addElement(_tempOriginalNumberDetails);
 				
 				if ((_telNumberUpdated != null) && (_telNumberUpdated.length() > 0)) {
 					Hashtable _tempUpdatedNumberDetails = new Hashtable(3);
 					_tempUpdatedNumberDetails.put("Attribute", _telAttribute);
 					_tempUpdatedNumberDetails.put("Number", _telNumberUpdated);
 					_tempUpdatedNumberDetails.put("Index", new Integer(i));
 					
 					_updatedNumbers.addElement(_tempUpdatedNumberDetails);
 				}
 			}
 			
 			if ( _updatedNumbers.size() > 0 ) {
 				_retNumbers.put("Original Numbers", _originalNumbers);
 				_retNumbers.put("Updated Numbers", _updatedNumbers);
 			}
 		}
 		
 		return _retNumbers;
 	}
 	
 	private String updateableNumber(String number) {
 		if ((number != null) && (number.length() > 0)) {
 			String _retNumber = "";
 			String _tempPrefix = "";
			String _tempNumber = number.trim();
 			
 			if (number.startsWith("+974")) {
 				_tempPrefix = "+974";
				_tempNumber = number.substring(4).trim();
 			} else if (number.startsWith("00974")) {
 				_tempPrefix = "00974";
				_tempNumber = number.substring(5).trim();
 			}
 			
             if ((_tempNumber.length() == 7) && (
             									_tempNumber.startsWith("3") || 
             									_tempNumber.startsWith("4") || 
             									_tempNumber.startsWith("5") || 
             									_tempNumber.startsWith("6") || 
             									_tempNumber.startsWith("7")
             								   ))
             {
             	_retNumber = _tempPrefix + _tempNumber.charAt(0) + _tempNumber;
             }
 			return _retNumber;
 		}
 		return null;
 	}
 	
 	public void initAtrributesLabels() {
 		this.attrToLabel = new Hashtable();
 		
 		//Standard J2ME MIDP and BlackBerry contacts constants -- try to say that 5 times :), hehe you sound silly!
 		int[] supportedAttributes = this.contactList.getSupportedAttributes(BlackBerryContact.TEL);
 		for (int i = 0; i < supportedAttributes.length; i++) {
 			String label = this.contactList.getAttributeLabel(supportedAttributes[i]);
 			this.attrToLabel.put(new Integer(supportedAttributes[i]), label);
 		}
 		
 		//Overriding "Home Fax" on BB OS > 5.0 to display "Fax" instead, due to missing API from documentation (RIM Laziness?)
 		this.attrToLabel.put(new Integer(BlackBerryContact.ATTR_FAX), QontactsApplication.res.getString(QontactsResource.ATTR_FAX));
 	}
 	
 	public String getAtrributeLabel(Integer attr) {
 		String _retString = "Unknown";
 		
 		if (this.attrToLabel.containsKey(attr)) {
 			_retString = (String) this.attrToLabel.get(attr);
 		}
 		
 		return _retString;
 	}
 	
 	public boolean convertContactNumbers(BlackBerryContact contact) {
 		Hashtable contactNumbers = updateableContactNumbers(contact);
 		Vector updatedNumbers = (Vector) contactNumbers.get("Updated Numbers");
 		if (convertContactNumbers(contact, updatedNumbers)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean convertContactNumbers(BlackBerryContact contact, Vector numbers) {
 		Enumeration updatedNumbers = numbers.elements();
 		
 		while (updatedNumbers.hasMoreElements()) {
 			Hashtable numberItem = (Hashtable) updatedNumbers.nextElement();
 			String number = (String) numberItem.get("Number");
 			Integer attr = (Integer) numberItem.get("Attribute");
 			Integer index = (Integer) numberItem.get("Index");
 			contact.setString(BlackBerryContact.TEL, index.intValue(), attr.intValue(), number);
 		}
 
 		try {
             contact.commit();
         } catch (PIMException e) {
         	System.out.println("Error: PIM issue " + e.getMessage());
         	return false;
         } catch (SecurityException s) {
         	System.out.println("Error: Security issue " + s.getMessage());
         	return false;
         }
         return true;
 	}
 }
