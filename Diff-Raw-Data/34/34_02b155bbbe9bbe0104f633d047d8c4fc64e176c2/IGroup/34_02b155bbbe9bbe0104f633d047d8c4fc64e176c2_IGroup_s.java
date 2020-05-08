 package org.softwaresynthesis.mytalk.server.abook;
 
 import java.util.Set;
 
 import org.softwaresynthesis.mytalk.server.IMyTalkObject;
 
 /**
  * Rappresenta un gruppo della rubrica
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
 public interface IGroup extends IMyTalkObject 
 {
 	/**
 	 * Restituisce il nome del gruppo
 	 * 
 	 * @return	{@link String} nome del gruppo
 	 */
 	public String getName();
 	
 	/**
 	 * Imposta il nome del gruppo
 	 * 
 	 * @param 	name	{@link String} nome del
 	 * 					gruppo
 	 */
 	public void setName(String name);
 	
 	/**
 	 * Restituisce i contatti appartenenti al gruppo
 	 * 
 	 * @return	{@link Set<IAddressBookEntry>} lista dei
 	 * 			contatti
 	 */
 	public Set<IAddressBookEntry> getAddressBook();
 	
 	/**
 	 * Imposta i contatti appartenenti al gruppo
 	 * 
 	 * @param 	addressBook 	{@link Set<IAddressBookEntry>}
 	 * 							contatti appartenenti al gruppo
 	 */
 	public void setAddressBook(Set<IAddressBookEntry> addressBook);
 	
 	/**
 	 * Aggiunge un nuovo contatto al gruppo
 	 * 
 	 * @param 	entry	{@link IAddressBookEntry} contatto da aggiungere
 	 */
 	public boolean addAddressBookEntry(IAddressBookEntry entry);
 	
 	/**
 	 * Rimuove un contatto dal gruppo
 	 * 
 	 * @param 	entry	{@link IAddressBookEntry} contatto da rimuovere
 	 */
 	public boolean removeAddressBookEntry(IAddressBookEntry entry);
 }
