 package ch9k.chat;
 
 import ch9k.core.ChatApplication;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * @author Jens Panneel
  */
 public class ContactListTest {
     private ContactList contactList;
 
     @Before
     public void setUp() {
         contactList = new ContactList();
     }
 
     /**
      * Test of getContacts method, of class ContactList.
      */
     @Test
     public void testGetContacts() throws UnknownHostException {
         // look out, this is a sorted list ;)
         Contact[] contacts = new Contact[] {
             new Contact("jaspervdj", InetAddress.getByName("4chan.org"), false),
             new Contact("Javache", InetAddress.getByName("ugent.be"), false),
             new Contact("JPanneel", InetAddress.getByName("google.be"), false)
         };
 
         for(Contact contact : contacts) {
             contactList.addContact(contact);
         }
 
         assertArrayEquals(contacts, contactList.getContacts().toArray());
     }
 
     /**
      * Test of addContact method, of class ContactList.
      */
     @Test
     public void testAddContact() throws UnknownHostException  {
         Contact contact = new Contact("JPanneel", InetAddress.getByName("google.be"), false);
         
         contactList.addContact(contact);
         assertEquals(1, contactList.getContacts().size());
         
         contactList.addContact(contact);
         assertEquals(1, contactList.getContacts().size());
     }
 
     /**
      * Test of removeContact method, of class ContactList.
      */
     @Test
     public void testRemoveContact() throws UnknownHostException {
         Contact contact1 = new Contact("JPanneel", InetAddress.getByName("google.be"), false);
         Contact contact2 = new Contact("JPanneel", InetAddress.getByName("ugent.be"), false);
         
         assertEquals(0, contactList.getContacts().size());
         contactList.addContact(contact1);
         contactList.removeContact(contact2);
         assertEquals(1, contactList.getContacts().size());
         contactList.removeContact(contact1);
         assertEquals(0, contactList.getContacts().size());
     }
 
     /**
      * Test of removeContact method, of class ContactList.
      */
     @Test
     public void testGetContact() throws UnknownHostException {
         Contact contact1 = new Contact("JPanneel", InetAddress.getByName("google.be"), false);
        Contact contact2 = new Contact("Javache", InetAddress.getByName("ugent.be"), false);
 
         contactList.addContact(contact1);
         contactList.addContact(contact2);
 
         assertEquals(contact1, contactList.getContact(contact1.getIp(),contact1.getUsername()));
        assertNotSame(contact1, contactList.getContact(contact2.getIp(),contact2.getUsername()));
         assertNull(contactList.getContact(contact2.getIp(),contact1.getUsername()));
         assertNull(contactList.getContact(contact1.getIp(),contact2.getUsername()));
     }
 }
