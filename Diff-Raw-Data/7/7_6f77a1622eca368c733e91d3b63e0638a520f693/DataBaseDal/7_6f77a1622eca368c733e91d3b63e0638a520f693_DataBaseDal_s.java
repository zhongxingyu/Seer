 package fr.esieaprojet.dao;
 
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import fr.esieaprojet.business.Adress;
 import fr.esieaprojet.business.BillingAdress;
 import fr.esieaprojet.business.Contact;
 import fr.esieaprojet.business.DeliveryAdress;
 import fr.esieaprojet.service.IContactDao;
 
 public class DataBaseDal implements IContactDao {
 
 	private HashMap<String, ArrayList<Contact>> db = new HashMap<String, ArrayList<Contact>>();
 	private ArrayList<Contact> users = new ArrayList<Contact>();
 	int count = 0;
 	private static final Pattern ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
 	//first element of arraylist in db is the user contact himself
 	
 	public DataBaseDal()
 	{
 		Contact mainUser = new Contact();
 		mainUser.setActive(true);
 		mainUser.setBirthday("22/07/1991");
 		mainUser.setMail("mail@gmail.com");
 		mainUser.setPassword("Password0");
 		mainUser.setFirstName("Jean");
 		mainUser.setLastName("Paul");
 		
 		Contact user1 = new Contact();
 		user1.setActive(true);
 		user1.setBirthday("22/07/1991");
 		user1.setMail("mail1@gmail.com");
 		user1.setPassword("Password1");
 		user1.setFirstName("Patrick");
 		user1.setLastName("Ping");
 		
 		Contact user2 = new Contact();
 		user2.setActive(true);
 		user2.setBirthday("22/07/1991");
 		user2.setMail("mail2@gmail.com");
 		user2.setPassword("Password2");
 		user2.setFirstName("Fred");
 		user2.setLastName("Duval");
 		
 		createContact(mainUser, "0");
 		createContact(user1, mainUser.getId());
 		createContact(user2, mainUser.getId());
 	}
 	
 	@Override
 	public boolean createContact(Contact contact, String userId) {
 		// TODO Auto-generated method stub
 		
 		if(userId.equals("0"))
 		{
 			ArrayList<Contact> contacts = new ArrayList<Contact>();
 			//contacts.add(contact);
 			db.put(contact.getId(), contacts);
 			users.add(contact);
 			count++;
 			System.out.println("create contact "+contact.getId()+" count = "+count);
 			
 		}
 		else
 		{
 			ArrayList<Contact> values = db.get(userId);
 			
 			for(int i=0; i<values.size(); i++)
 			{
 				if( values.get(i).getId().equals(contact.getId()) )
 					return false;
 			}
 			
 			values.add(contact);
 			
 			if(db.put(userId, values) == null)
 				return false;
 			
 			System.out.println("num = "+db.keySet().size());
 			
 		}
 		
 		return true;
 		
 	}
 
 	@Override
 	public boolean deleteContact(Contact contact, String userId) {
 		// TODO Auto-generated method stub
 		
 		ArrayList<Contact> values = db.get(userId);
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			if( values.get(i).getId().equals(contact.getId()) )
 			{
 				values.remove(i);
 				break;
 			}
 			
 			if(i == values.size()-1)
 				return false;
 		}
 		
 		
 		
 		if(db.put(userId, values) == null)
 			return false;
 		
 		return true;
 		
 	}
 
 	@Override
 	public Contact getContactByName(String firstName, String lastName, String userId) {
 		// TODO Auto-generated method stub
 		
 		ArrayList<Contact> values = db.get(userId);
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			if(values.get(i).getFirstName().equals(firstName) && values.get(i).getLastName().equals(lastName))
 				return values.get(i);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Contact getContactById(String id, String userId) {
 		// TODO Auto-generated method stub
 		
 			
 		ArrayList<Contact> values = db.get(userId);
 			
 		for(int i=0; i<values.size(); i++)
 		{
 			if(values.get(i).getId().equals(id))
 				return values.get(i);
 		}
 			
 		return null;
 		
 		
 	}
 
 	@Override
 	public Contact getContactByMail(String mail, String userId) {
 		// TODO Auto-generated method stub
 		ArrayList<Contact> values = db.get(userId);
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			if(values.get(i).getMail().equals(mail))
 				return values.get(i);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public List<Contact> getAllContactByString(String string, String userId) {
 		// TODO Auto-generated method stub
 		
		System.out.println("search contact "+userId+" count = "+count+" string = "+string);
 		
 		ArrayList<Contact> values = db.get(userId);
 		ArrayList<Contact> result = new ArrayList<Contact>();
 		boolean flag;
 		
 		if(values == null)
 			System.out.println("search contact null "+userId+" count = "+count);
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			Contact c = values.get(i);
 			ArrayList<DeliveryAdress> list1 = c.getDeliveryAdresses();
 			BillingAdress bAdr = c.getBillingAdress();
 			flag = false;
			System.out.println("last= "+c.getLastName()+" test = "+string);
			if(search(c.getFirstName(), string) || search(c.getFirstName(), string) || search(c.getBirthday(), string) 
 					|| (search(c.getActiveMessage(), string) && c.getActiveMessage().length() == string.length()) || search(c.getMail(), string)) {
 				result.add(c);
 				
 				continue;
 			}
 			
 			else if((list1 != null) && (bAdr != null) &&( search(bAdr.getCityName(), string) || search(bAdr.getPostalCode(), string) || search(bAdr.getStreetName(), string) || search(bAdr.getStreetNumber(), string)))
 			{
 				result.add(c);
 				System.out.println("reussi2");
 				continue;
 			}
 			
 			else if(list1 != null)
 			{	
 				for(int j=0; j<list1.size() && !flag; j++)
 				{
 					DeliveryAdress da = list1.get(j);
 					if(search(da.getCityName(), string) || search(da.getPostalCode(), string) || search(da.getStreetName(), string) || search(da.getStreetNumber(), string))
 					{	
 						result.add(c);
 						System.out.println("reussi3");
 						flag = true;
 					}
 				}
 			}
 		}
 		
 		if(result.isEmpty())
 			return null;
 		else
 			return result;
 	}
 	
 	/*
 	
 	@Override
 	public List<Contact> getAllContactByString(String string, String userId) {
 		// TODO Auto-generated method stub
 		
 		System.out.println("search contact "+userId+" count = "+count+" string = "+string);
 		
 		ArrayList<Contact> values = db.get(userId);
 		ArrayList<Contact> result = new ArrayList<Contact>();
 		boolean flag;
 		
 		if(values == null)
 			System.out.println("search contact null "+userId+" count = "+count);
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			Contact c = values.get(i);
 			ArrayList<DeliveryAdress> list1 = c.getDeliveryAdresses();
 			BillingAdress bAdr = c.getBillingAdress();
 			flag = false;
 			System.out.println("last= "+c.getLastName()+" test = "+string);
 			if((c.getFirstName()+" ").contains(string) || (c.getLastName()+" ").contains(string)) {
 				result.add(c);
 				
 				continue;
 			}
 			
 			else if((list1 != null) && (bAdr != null) &&((bAdr.getCityName()+" ").contains(string) || (bAdr.getPostalCode()+" ").contains(string) || (bAdr.getStreetName()+" ").contains(string) || (bAdr.getStreetNumber()+" ").contains(string)))
 			{
 				result.add(c);
 				System.out.println("reussi2");
 				continue;
 			}
 			
 			else if(list1 != null)
 			{	
 				for(int j=0; j<list1.size() && !flag; j++)
 				{
 					DeliveryAdress da = list1.get(j);
 					if((da.getCityName()+" ").contains(string) || (da.getPostalCode()+" ").contains(string) || (da.getStreetName()+" ").contains(string) || (da.getStreetNumber()+" ").contains(string))
 					{	
 						result.add(c);
 						System.out.println("reussi3");
 						flag = true;
 					}
 				}
 			}
 		}
 		
 		if(result.isEmpty())
 			return null;
 		else
 			return result;
 	}
 	*/
 	
 
 
 	@Override
 	public List<Contact> getAllContactByDate(String date, String userId) {
 		// TODO Auto-generated method stub
 		
 		ArrayList<Contact> values = db.get(userId);
 		ArrayList<Contact> result = new ArrayList<Contact>();
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			Contact c = values.get(i);
 			if(new String(c.getBirthday()).equals(date))
 				result.add(c);
 		}
 		
 		if(result.isEmpty())
 			return null;
 		else
 			return result;
 	}
 
 	@Override
 	public boolean updateContact(Contact contact, String userId) {
 		// TODO Auto-generated method stub
 		
 		
 		
 		if(userId.equals(contact.getId()))
 		{
 			System.out.println("test reussi");
 			
 			for(int i=0; i<users.size(); i++)
 			{
 				if( users.get(i).getId().equals(contact.getId()) )
 				{
 					users.remove(i);
 					users.add(contact);
 					return true;
 				}
 				
 				if(i == users.size()-1)
 					return false;
 			}
 		}
 		else
 		{
 			ArrayList<Contact> values = db.get(userId);
 			
 			for(int i=0; i<values.size(); i++)
 			{
 				if( values.get(i).getId().equals(contact.getId()) )
 				{
 					values.remove(i);
 					break;
 				}
 				
 				if(i == values.size()-1)
 					return false;
 			}
 			
 			
 			values.add(contact);
 			
 			if(db.put(userId, values) == null)
 				return false;
 		}
 		
 		return true;
 		
 	}
 
 	@Override
 	public Contact getContact(String mail, String password) {
 		// TODO Auto-generated method stub
 		
 		for(int i=0; i<users.size(); i++)
 		{
 			if( users.get(i).getMail().equals(mail) && users.get(i).getPassword().equals(password) )
 				return users.get(i);
 		}
 		
 		return null;
 		
 	}
 	
 	@Override
 	public Contact getContact(String id) {
 		// TODO Auto-generated method stub
 		
 		for(int i=0; i<users.size(); i++)
 		{
 			if( users.get(i).getId().equals(id) )
 				return users.get(i);
 		}
 		
 		return null;
 		
 	}
 
 	@Override
 	public List<Contact> getAllContact(String userId) {
 		// TODO Auto-generated method stub
 		System.out.println("get contact "+userId);
 		ArrayList<Contact> values = db.get(userId);
 		if(values == null)
 		{
 			System.out.println("null");
 		}
 		ArrayList<Contact> result = new ArrayList<Contact>();
 		
 		for(int i=0; i<values.size(); i++)
 		{
 			Contact c = values.get(i);
 				result.add(c);
 		}
 		
 		if(result.isEmpty())
 			return null;
 	
 			
 		else
 			return result;
 	}
 
 	public static boolean search(String haystack, String needle) {
 	    final String hsToCompare = removeAccents(haystack+" ").toLowerCase();
 	    final String nToCompare = removeAccents(needle).toLowerCase();
 
 	    return hsToCompare.contains(nToCompare);
 	}
 
 	public static String removeAccents(String string) {
 	    return ACCENTS_PATTERN.matcher(Normalizer.normalize(string, Normalizer.Form.NFD)).replaceAll("");
 	}
 	
 	
 }
