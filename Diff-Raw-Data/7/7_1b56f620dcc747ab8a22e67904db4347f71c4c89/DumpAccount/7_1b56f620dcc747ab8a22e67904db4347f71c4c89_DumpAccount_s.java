 package com.freshbooks.test;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
import java.util.Scanner;
 
 import com.freshbooks.ApiConnection;
 import com.freshbooks.ApiException;
 import com.freshbooks.model.Category;
 import com.freshbooks.model.Client;
 import com.freshbooks.model.Expense;
 import com.freshbooks.model.Invoice;
 import com.freshbooks.model.Item;
 import com.freshbooks.model.Payment;
 
 public class DumpAccount {
 
     /**
      * @param args
      * @throws ParseException 
      */
     public static void main(String[] args) throws ParseException {
        url = args[0];
        key = args[1];
     	
     	/*
     	String url = "";
     	String key = "";
     	  	
     	
     	if(args.length == 0) {
         	Scanner kb = new Scanner(System.in);
             System.out.println("Usage: dump URL API-KEY");
             System.out.println("Accepting arguments at runtime:");
             System.out.print("Enter the API URL: ");
             url = kb.nextLine();
             System.out.print("Enter the API Key: ");
             key = kb.nextLine();
             //return;
         } else {
         	url = args[0];
         	key = args[1];
         } */
     	
         try {
             ApiConnection con = new ApiConnection(new URL(url), key, "FreshBooks Java API Client Test");
             con.setDebug(true);
             try {
                 for(Client client : con.listClients(null, null, null)) {
                     System.out.println("Found client "+client.getFirstName()+" "+client.getLastName()+" at "+client.getOrganization());
                     con.getClient(client.getId());
                 }
                 for(Invoice invoice : con.listInvoices(null, null, null, null, null)) {
                     System.out.println("Found invoice "+invoice.getId()+" with amount "+invoice.getAmount());
                     con.getInvoice(invoice.getId());
                 }
                 for(Payment payment : con.listPayments(null, null, null, null)) {
                     System.out.println("Found payment "+payment.getId()+" on invoice "+payment.getInvoiceId()+" with amount "+payment.getAmount());
                 }
                 HashMap<Long,String> categoryNames = new HashMap<Long, String>();
                 for(Category category : con.listCategories()) {
                     System.out.println("Found category "+category.getId()+": "+category.getName());
                     categoryNames.put(category.getId(), category.getName());
                 }
                 for(Expense expense : con.listExpenses(null, new Date(109,0,1), null, null, null, null)) {
                     expense = con.getExpense(expense.getId());
                     System.out.println("Found expense "+expense.getId()+" with amount "+expense.getAmount()+" and category "+categoryNames.get(expense.getCategoryId()));
                 }
                 for(Item item : con.listItems(25)) {
                     System.out.println("Found item "+item.getId()+" with name "+item.getName()+" and description "+item.getDescription());
                 }
             } catch(Error e) {
                 if(e.getCause() instanceof ApiException) {
                     throw (ApiException)e.getCause();
                 }
                 if(e.getCause() instanceof IOException) {
                     throw (IOException)e.getCause();
                 }
             }
             
             
         } catch (MalformedURLException e) {
             System.err.println(url+" is not a valid URL ("+e.getLocalizedMessage()+")");
         } catch (ApiException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
 
 }
