 import java.util.*;
 import java.io.*;
 
 /**
  * 
  * @author Mudrekh Goderya, James Sanderlin
  * 
  * The Account_IO class handles input and output of the current state of the program to accounts.txt
  * 
  * @
  *
  */
 public class Account_IO 
 {
 	public Account_IO()
 	{
 		File accountFile = new File("accounts.txt");
 		
 		try
 		{
 			Scanner in = new Scanner(accountFile);
 			//Parse the accounts file
 			while(in.hasNext())
 			{
 				String name = in.nextLine();
 				//System.out.println(name);
 				int accountId = Integer.parseInt(in.nextLine());
 				//System.out.println(accountId);
 				double balance = Double.parseDouble(in.nextLine());
 				//System.out.println(balance);
 				boolean flag = (in.nextInt() == 0) ? false : true;
 				//System.out.println(flag);
 				boolean isCommercial = (in.nextInt() == 0) ? false : true;
 				in.nextLine();
 				String dateString = in.nextLine();
 				/*TODO Convert dateString into Date (or Calendar?) object */
 				Date deadline = new Date();
 				String address = "";
 				
 				//Read address until you see "PAYMENTS:"
 				String line = in.nextLine(); 
 				while(!line.equals("PAYMENTS:"))
 				{
 					address += line + "\n";
 					line = in.nextLine();
 				}
 				
 				//Read Payments until you see a blank line.
 				line = in.nextLine();
 				while(!line.equals(""))
 				{
 					/*TODO Read in and create the payment objects */
 					//System.out.println("Line " + line);
 					line = in.nextLine();
 				}
 				
 				if(isCommercial)
 				{
 					CommercialAccount a = new CommercialAccount(name, accountId, balance, flag, deadline, address);
 					System.out.println("COMMERCIAL ACCOUNT INFO:");
					System.out.println(a.clientFirstName);
					System.out.println(a.clientLastName);
 					System.out.println(a.accountID);
 					System.out.println(a.balance);
 					System.out.println(a.flag);
 					System.out.println(a.deadline);
 					System.out.println("ADDRESS: " + a.billingAddress);	
 					System.out.println("END COMMERCIAL INFO");
 				}
 				else
 				{
 					ResidentialAccount a = new ResidentialAccount(name, accountId, balance, flag, deadline, address);
 					System.out.println("RESIDENTIAL ACCOUNT INFO:");
					System.out.println(a.clientFirstName);
					System.out.println(a.clientLastName);
 					System.out.println(a.accountID);
 					System.out.println(a.balance);
 					System.out.println(a.flag);
 					System.out.println(a.deadline);
 					System.out.println("ADDRESS: " + a.billingAddress);	
 					System.out.println("END RESIDENTIAL INFO");
 				}
 			}
 		}
 		catch(FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }	
