package com.ruben;

public class Transactions
 {
 	public static void main (String[] args)
 	{
 		Account acct1 = new Account ("Don Strawsburg", 10001, 102.56);
 		Account acct2 = new Account ("Petr Ruben", 10002, 40.00);
 		Account acct3 = new Account ("Jessica Strawsburg", 10003, 759.32);
 		
 		acct1.deposit (25.85);
 		
 		double rubenBalance = acct2.deposit (500.00);
 		System.out.println ("Ruben balance after deposit: " + rubenBalance);
 		
 		System.out.println ("Ruben balance after withdrawal: " + acct2.withdraw (430.75, 1.50));
 		
 		acct1.addInterest();
 		acct2.addInterest();
 		acct3.addInterest();
 		
 		System.out.println ();
 		System.out.println (acct1);
 		System.out.println (acct2);
 		System.out.println (acct3);
 		
 	}
}
