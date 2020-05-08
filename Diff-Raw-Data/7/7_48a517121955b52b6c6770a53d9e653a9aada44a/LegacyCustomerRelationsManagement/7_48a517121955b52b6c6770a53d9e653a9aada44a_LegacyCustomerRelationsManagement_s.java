 package at.ac.tuwien.infosys.aic11.legacy;
 
 import java.math.BigDecimal;
 import java.util.Hashtable;
 import java.util.Map;
 
 import at.ac.tuwien.infosys.aic11.dto.Address;
 import at.ac.tuwien.infosys.aic11.dto.BankTransfer;
 import at.ac.tuwien.infosys.aic11.dto.Cheque;
 import at.ac.tuwien.infosys.aic11.dto.Customer;
 import at.ac.tuwien.infosys.aic11.dto.Ratings;
 
 /**
  * I assume that we don't need to create new customers, just use existing ones
  * @author stefan
  *
  */
 public class LegacyCustomerRelationsManagement {
 	
     	private static LegacyCustomerRelationsManagement instance;
     
 	private Map<Long, Customer> customers = new Hashtable<Long, Customer>();
 	
 	public static LegacyCustomerRelationsManagement instance() {
 	    if (instance == null)
 		instance = new LegacyCustomerRelationsManagement();
 	    return instance;
 	}
 	
 	private LegacyCustomerRelationsManagement()
 	{		
 		customers.put(1L, new Customer(1, "Stefan", "A.", "Koegl", new BigDecimal(10000), new Address("1", "Street", "City", "5", "1", "1234"), new Cheque("Stefan Kögl")));
 		customers.put(2L, new Customer(2, "Stefan", "B.", "Derkits", new BigDecimal(10000), new Address("1", "Street", "City", "5", "1", "1234"), new BankTransfer("MyBank", "12345", "ABCA234234")));
 		customers.put(3L, new Customer(3, "Felix",  "C.", "Winter", new BigDecimal(10000), new Address("1", "Street", "City", "5", "1", "1234"), new Cheque("Felix Winter")));
 		customers.put(4L, new Customer(4, "Manfred", "M.", "Mustermann", new BigDecimal(10000), new Address("1", "Street", "City", "5", "1", "1234"), new BankTransfer("Some Bank", "asdfasdf", "123123asdfasdf")));
 		customers.put(5L, new Customer(5, "Erika", "E.", "Musterfrau", new BigDecimal(10000), new Address("1", "Street", "City", "5", "1", "1234"), new Cheque("Erika Musterfrau")));
 	}
 	
 	/**
 	 * Tries to find an exact match in any part of the customers' name. 
 	 * The first match (or null) is returned
 	 * @param name
 	 * @return
 	 */
 	public Customer getCustomerByName(String name)
 	throws LegacyException
 	{
 		for(Customer customer : customers.values())
 		{
 			String fullname = customer.getFirstName() + " " + customer.getMiddleName() + " " + customer.getLastName();
 			
 			if(fullname.contains(name))
 			{
 				return customer;
 			}
 		}
 		
 		throw new LegacyException("Customer not found (getCustomerByName)");
 	}
 	
 	public Customer getCustomerByID(long customerId)
 	throws LegacyException
 	{
 	    if (customers.containsKey(customerId))
 		return customers.get(customerId);
 	    else
 		throw new LegacyException("Customer not found (getCustomerByID)");
 	}
 	
 	public synchronized void sendEmail(Customer customer)
 	throws LegacyException
 	{
 		if (!customers.containsKey(customer.getCustomerId()))
 		{
 			throw new LegacyException("customer " + customer.getCustomerId() + " does not exist");
 		}
 		
 		// send mail! 
 	}
 	
 	public Ratings getRating(long customerId)
 	throws LegacyException
 	{
 		// chose rating based on customer-Id
 	    	if (!customers.containsKey(customerId))
 	    	    throw new LegacyException("Customer not found (getRating");
 		Ratings[] ratings = Ratings.values();
 		int numRatings = ratings.length;
 		int rating = (int)customerId % numRatings;
 		return ratings[rating];
 	}
 }
