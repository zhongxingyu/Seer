 
 public class Number
 {
 	int number;
 	
 	public Number(int num)
 	{
 		number = num;
 	}
	public int getNumber()
	{
		return number;
	}
 	@Override
 	public boolean equals(Object obj)
 	{
 		boolean equality = false;
 		if(obj.getClass().equals(this.getClass()))
 		{
 			if(this.number == ((Number)obj).number)
 			{
 				equality = true;
 			}
 		}
 		else
 		{
 			equality = false;
 		}
 		return equality;
 		
 	}
 	public int hashCode(Object obj)
 	{
 		return ((Number)obj).number % 71;
 		/*
 		int hashedIndex;
 		return hashedIndex;
 		*/
 	}
 	
 	public int hashCode1(Object obj)
 	{	
 		int  last4digits;
 		if(((Number)obj).number >= 4160000)
 		{
 			last4digits = ((Number)obj).number - 4160000;
 			last4digits = last4digits % 71;
 			
 			
 		}
 		else if(((Number)obj).number >= 5740000)
 		{
 			last4digits = ((Number)obj).number - 5740000;
 			last4digits = last4digits % 71;
 		}
 		else
 		{
 			System.out.println("Invalid area code");
 			last4digits = -1;
 		}
 		return last4digits;	
 	}
	public String toString()
	{
		String stringNum =  String.valueOf(getNumber());
		return stringNum;
		
	}
 }
