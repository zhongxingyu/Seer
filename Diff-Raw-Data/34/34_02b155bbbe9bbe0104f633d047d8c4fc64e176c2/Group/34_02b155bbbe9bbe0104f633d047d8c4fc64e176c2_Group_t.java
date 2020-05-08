 package org.softwaresynthesis.mytalk.server.abook;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Group implements IGroup 
 {
 	private Long id;
 	private String name;
	private IUserData owner;
 	private Set<IAddressBookEntry> addressBook;
 	
 	/**
 	 * Costruisce una nuova istanza priva
 	 * di valori
 	 */
 	public Group()
 	{
 		this(-1L);
 	}
 	
 	/**
 	 * Costruisce una nuova istanza assegnandogli
 	 * un identificativo
 	 * 
 	 * @param 	identifier	{@link Long} identificativo
 	 * 						associato
 	 */
 	public Group(Long identifier)
 	{
 		this.id = identifier;
 		this.addressBook = new HashSet<IAddressBookEntry>();
 	}
 	
 	@Override
 	public Long getId()
 	{
 		return this.id;
 	}
 	
 	@Override
 	public void setId(Long id)
 	{
 		this.id = id;
 	}
 
 	@Override
 	public String getName() 
 	{
 		return this.name;
 	}
 
 	@Override
 	public void setName(String name) 
 	{
 		this.name = name;
 	}
 
 	@Override
	public IUserData getOwner()
	{
		return this.owner;
	}
	
	@Override
	public void setOwner(IUserData owner)
	{
		this.owner = owner;
	}
	
	@Override
 	public Set<IAddressBookEntry> getAddressBook() 
 	{
 		return this.addressBook;
 	}
 
 	@Override
 	public void setAddressBook(Set<IAddressBookEntry> addressBook)
 	{
 		this.addressBook = addressBook;
 	}
 
 	@Override
 	public boolean addAddressBookEntry(IAddressBookEntry entry) 
 	{
 		boolean result = false;
 		entry.setGroup(this);
 		result = this.addressBook.add(entry);
 		return result;
 	}
 
 	@Override
 	public boolean removeAddressBookEntry(IAddressBookEntry entry) 
 	{
 		boolean result = this.addressBook.remove(entry);
 		return result;
 	}
 	
 	/**
 	 * Determina se due istanze rappresentano lo stesso
 	 * oggetto Group
 	 * 
 	 * @param	obj {@link Object} istanza da verificare
 	 * @return	true se le due istanze rappresentano lo
 	 * 			stesso oggetto, false altrimenti
 	 */
 	@Override
 	public boolean equals(Object obj)
 	{
 		boolean result = false;
 		Group toCompare = null;
 		String toCompareName = "";
 		if (obj instanceof Group)
 		{
 			toCompare = (Group)obj;
 			toCompareName = toCompare.getName();
 			if (this.name.equals(toCompareName))
 			{
 				result = true;
 			}
 		}
 		return result;
 	}
 }
