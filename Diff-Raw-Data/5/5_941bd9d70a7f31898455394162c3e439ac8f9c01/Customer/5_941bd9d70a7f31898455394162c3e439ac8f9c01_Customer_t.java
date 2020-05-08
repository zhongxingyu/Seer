 package refactoring;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 class Customer
 {
 	private String _name;
 	private Vector _rentals = new Vector();
 
 	public Customer(String name)
 	{
 		_name = name;
 	};
 
 	public void addRental(Rental arg)
 	{
 		_rentals.addElement(arg);
 	}
 
 	public String statement()
 	{
 		double totalAmount = 0;
 		int frequentRenterPoints = 0;
 		Enumeration rentals = _rentals.elements();
 		String result = "   " + getName() + "\n";
 		while (rentals.hasMoreElements())
 		{
 			double thisAmount = 0;
 			Rental each = (Rental) rentals.nextElement();
 			thisAmount = amountFor(each);
 			
 			//     
 			frequentRenterPoints++;
 			//       
 			if ((each.getMovie().getPriceCode() == Movie.NEW_RELEASE)
 					&& each.getDaysRented() > 1)
 				frequentRenterPoints++;
 			//     
 			result += "\t" + each.getMovie().getTitle() + "\t"
 					+ String.valueOf(thisAmount) + "\n";
 			totalAmount += thisAmount;
 		}
 		//   
 		result += "   "
 				+ String.valueOf(totalAmount) + "\n";
 		result += "  " + String.valueOf(frequentRenterPoints)
 				+ "   ";
 		return result;
 	}
 
	private double amountFor(Rental each)
 	{
		double thisAmount = 0;
 		switch (each.getMovie().getPriceCode())
 		{
 			case Movie.REGULAR:
 				thisAmount += 2;
 				if (each.getDaysRented() > 2)
 					thisAmount += (each.getDaysRented() - 2) * 1.5;
 				break;
 			case Movie.NEW_RELEASE:
 				thisAmount += each.getDaysRented() * 3;
 				break;
 			case Movie.CHILDRENS:
 				thisAmount += 1.5;
 				if (each.getDaysRented() > 3)
 					thisAmount += (each.getDaysRented() - 3) * 1.5;
 				break;
 		}
 		return thisAmount;
 	}
 
 	public String getName()
 	{
 		return _name;
 	}
 }
