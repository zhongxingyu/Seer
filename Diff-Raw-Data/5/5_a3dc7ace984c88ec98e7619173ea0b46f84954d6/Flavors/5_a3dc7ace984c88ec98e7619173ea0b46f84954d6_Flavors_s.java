 public class Flavors {
 	private int favorite; //Initial global variables
 	private static String[] flavors={"Mint Chocolate Chip", "Chocolate", "Vanilla", "Strawberry",
 		"Cookies N' Cream", "Strawberry"}; //Array that lists all the flavors
 	private int cost;
 	private boolean nuts;
 	private boolean syrup;
 	private String message = "You bought: ";
 	public Flavors(int flavors) //Constructer that requires instance data
 		{
 		favorite=flavors;
 		if (flavors>=1 && flavors<=5) //Loop checks if the query is valid
 		{
 			System.out.println("Your favorite ice cream flavor is " +Flavors.flavors[favorite]);
 			cost=3;
 		}
 		else
 			{
 				System.out.println("We don't offer "+favorite);
 			}
 		
 
 		}
 	public static void ListFlavors()  //Displays a list for the user to choose from
 	{
 	    for (int i=1; i<flavors.length; i++)
 	    {
 	      System.out.println(i+": "+flavors[i]);
 	    } 
 	}
 	public String getFlavor() //Getter
 	{
 		return flavors[favorite];
 		
 	}
 	public void setFlavor(int newflavor) //Setter
 	{
 		favorite=newflavor;
 	}
 	public void AddNuts(boolean nuts) //Method 1
 	{
 		if (nuts==true)
 		{
 		cost+=1;
 		this.nuts=nuts;
 		}
 	}
 	public void AddSyrup(boolean syrup) //Method 2
 	{
 		if (syrup==true)
 		{
 		cost+=1;
 		this.syrup=syrup;
 		}
 		}
 	public String Receipt(boolean receipt) //Method 3
 	{
 		return "Thank you your cost is $" + cost +".\n Have a good day!";
 	}
 	public void setSyrupAndNuts(boolean nuts, boolean syrup) //Setter for nuts and syrup booleans
 	{
 		this.nuts=nuts;
 		this.syrup=syrup;
 	}
 	public String getSyrupAndNuts() //Getter for nuts and syrup booleans
 	{
 		String nuts_syrup="You have: ";
 		if (nuts==true)
 				nuts_syrup+="Nuts. ";
 		if (syrup==true)
 				nuts_syrup+="Syrup.";
 		else if(nuts==false && syrup==false)
 			nuts_syrup="You have no syrup or nuts";
 		return nuts_syrup;
 		
 	}
 	public String toString()
 	{
 		if (nuts==true)
 		{
			message += "nuts, ";
 		}
 	    if (syrup==true)
 	    {
			message += "syrup, ";
 	    }
 	    message+="ice cream.";
 	    return message;
 	}
 	}
