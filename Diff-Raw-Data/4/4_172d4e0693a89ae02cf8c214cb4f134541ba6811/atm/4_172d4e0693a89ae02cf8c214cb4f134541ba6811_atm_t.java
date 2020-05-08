 //by HafizJef
 import java.io.*;
 class atm
 {
 	public static void main(String args[]) throws IOException
 	{
 		
 		String money;
 		BufferedReader getinput = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println("\n\n+Welcome to Maybank2Mu berhad, This machine can spit out :\n\n\t| 10, 20, 50 and 100 |");
 		
 		
 		int max100=10, max50=10, max20=10, max10=10, note100=0, note50=0, note20=0, note10=0;
 		
 			
 		int loop=0;
 		while (loop==0)
 		{
			note100=0; note50=0; note20=0; note10=0;
 			
 			System.out.print("\n\n+Please input amount of money that you want to withdraw(Min. RM 10) : ");
 		
 			money = getinput.readLine();
 			int amount = Integer.parseInt(money); 
 			
 			int total=amount;
 		
 			if (amount<10)
 			{
 				System.out.println("\n\nThe entered value is invalid");
 				continue; //continue to loop, use return to end;
 			}
 			else if (amount>1500)
 			{
 				System.out.println("\n\nThe maximum amount to per withdrawal is RM1500");
 				continue;
 			}
 
 
 
 			if (amount>=100)
 			{
 				note100=amount/100;
 				amount=amount%100;
 				if (note100>max100)
 				{
 					amount=(amount%100)+(100*(note100-max100));
 					note100=max100;
 					//System.out.println(amount);
 				}
 				max100=max100-note100;
 			}
 			if (amount>=50)
 			{
 				note50=amount/50;
 				amount=amount%50;
 				if (note50>max50)
 				{
 					amount=(amount%50)+(50*(note50-max50));
 					note50=max50;
 					//System.out.println(amount);
 				}
 				max50=max50-note50;
 			}
 			if (amount>=20)
 			{
 				note20=amount/20;
 				amount=amount%20;
 				if (note20>max20)
 				{
 					amount=(amount%20)+(20*(note20-max20));
 					note20=max20;
 					//System.out.println(amount);
 				}
 				max20=max20-note20;
 			}
 			if (amount>=10)
 			{
 				note10=amount/10;
 				amount=amount%10;
 				if (note10>max10)
 				{
 					amount=(amount%10)+(10*(note10-max10));
 					note10=max10;
 					//System.out.println(amount);
 					System.out.println("Sorry, our machine is out of service");
 					System.exit(0);
 				}
 				max10=max10-note10;
 			}
 		
 			if (amount!=0)
 			{
 				System.out.println("Error!, Please input amount in multiple of 10");
 				return;
 			}
 			else
 			{
 				System.out.println("\n\n\t=====================\n"+"\t| Note $100\t= "+note100+" | "+"\n\t| Note $50\t= "+note50+" | "+"\n\t| Note $20\t= "+note20+" | "+"\n\t| Note $10\t= "+note10+" | ");
 				System.out.println("\t=====================\n"+"\n\t  Total\t\t= "+total);
 
 
 				System.out.print("\n\nDo you want to make another transaction?[Y/N] : ");
 				String userMenu = getinput.readLine();
 
				if ((userMenu.equalsIgnoreCase("Y")))
 				{
 					loop=0;
 				}
 				else
 				{
 					System.out.println("Thanks for using our service");
 					loop=1;
 				}
 
 			}
 		
 		}
 		
 		
 	}
 }
