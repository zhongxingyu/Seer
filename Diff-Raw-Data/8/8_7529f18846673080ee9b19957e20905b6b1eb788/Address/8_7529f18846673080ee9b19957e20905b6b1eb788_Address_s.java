 
 
 
 public class Address
 {
 	private String street1;
 	private String street2;
 	private String city;
 	private String state;
 	private String zip;
 	private String country;
 	
 	
 	public Address(String s1, String s2, String inputCity, String inputState, String cntry, int z)
 	{
 		street1 = s1;
 		street2 = s2;
 		city = inputCity;
 		state = inputState;
 		zip = z;
 		country = cntry;
 	}
 	
 	public Address(String s1, String c, String st, String cn, int z)
 	{
 		street1 = s1;
 		city = c;
 		state = st;
 		country = cn;
 		zip = z;
 	}
 	
 	public Address(Address input)
 	{
 	
 	
 	}
 	
 	public String toString()
 	{
 	
 	
 	}
 	
 	public void changeStreet1(String input)
 	{
 	
 	
 	}
 	
 	public void changeStreet2(String input)
 	{
 	
 	
 	}
 	
	public void State(String input)
 	{
 	
 	}
 	
	public void Zip(int input)
 	{
 	
 	
 	}
 	
	public void Country(String input)
 	{
 	
 	
 	}
 	
 	public String getStreet1()
 	{
 		return Street1;
 	}
 	
 	public String getStreet2()
 	{
 		return Street2;
 	}
 	
 	public String getCity()
 	{
 		return City;
 	}
 	
 	public String getState()
 	{
 		return State;
 	}
 	
 	public String getCountry()
 	{
 		return Country;
 	}
 	
 	public int getZip()
 	{
 		return Zip;
 	}
 	
 }
 	
