 package com.example.payback;
 
 import java.util.*;
 
 public class Transaction {
 
 	int lenderId, borrowerId;
	double amount;
	String comment;
 	Transaction(int lenderId, int borrowerId, double amount, String comment)
 	{
 		this.lenderId = lenderId;
 		this.borrowerId = borrowerId;
		this.amount = amount;
		this.comment = comment;
 	}
 	//Example for outside code: t.sendTransToServer();
 	void sendTransactionToServer()
 	{
 		//json + sql magic
 	}
 	
 	//Example for outside code: Transaction t = getTransFromServer(wmacfarlane@ufl.edu, arnav@ufl.edu)
 	static ArrayList<Transaction> getTransactionsFromServer(String lenderEmail, String borrowerEmail)
 	{
 		//json + sql magic
 		ArrayList<Transaction> a = new ArrayList<Transaction>();
 		//a.add(T1);
 		//a.add(T2);
 		//...
 		return a; //dummy code to silence errors
 	}
 }
