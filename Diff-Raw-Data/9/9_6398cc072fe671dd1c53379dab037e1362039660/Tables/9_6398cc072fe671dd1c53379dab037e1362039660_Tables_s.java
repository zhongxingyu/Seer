 package util;
 
 public class Tables {
 
 	public static String PRODUCTS = "PRODUCTS";
 	public static String PULLOUTS = "PULLOUTS";
 
 	public static String ACCOUNT_RECEIVABLES = "ACCOUNT RECEIVABLES";
 	public static String AR_PAYMENTS = "AR PAYMENTS";
 
 	public static String CASH_ADVANCE = "CASH ADVANCE";
 	public static String CA_PAYMENTS = "CA PAYMENTS";
 
 	public static String CUSTOMERS = "CUSTOMERS";
 	public static String ACCOUNTS = "ACCOUNTS";
 	public static String EMPLOYEES = "EMPLOYEES";
 
 	public static String DELIVERIES = "DELIVERIES";
 	public static String SUPPLIERS = "SUPPLIERS";
 
 	public static String EXPENSES = "EXPENSES";
 	public static String SALARY = "SALARY";
 
 	public static String SALES = "SALES";
 
 	public static String SALES_GRAPH = "SALES_GRAPH";
 
 	public static String DISCOUNTS = "DISCOUNTS";
 
 	public static String DEPOSITS = "DEPOSITS";
 	public static String BANK = "BANK";
 	public static String BANK_EDIT = "BANK_EDIT";
 
 	public static String INVENTORY_SHEET = "INVENTORY SHEET";
 
 	public static String LOGS = "LOGS";
 
 	public static String[] productFormLabel = { "Name*", "Description", "Available?*", "kilos per sack*", "Quantity (in sack)*", "On-display qty (in kg)*",
 			"Price (per sack)*", "Price (per kg)*", "Category*", "Alert using: *"};
 
	public static String[] customerFormLabel = { "Last Name*", "First Name*", "Middle Name", "Address", "Contact Number" };
 
 	public static String[] accountFormLabel = { "Employee*", "Account Type*", "Username*", "Password*" };
 
 	public static String[] employeeFormLabel = { "Last Name*", "First Name*", "Middle Name", "Address", "Contact No.", "Designation*",
 			"Employment Status*", "Starting Date", "Salary*", "Remarks", "End Date" };
 
 	public static String[] supplierFormLabel = { "Name*", "Address", "TIN", "Contact No.", "Remarks" };
 
 	public static String[] cashAdvanceFormLabel = { "Date*", "Issued By*", "Issued For*", "Amount*", "Balance*" };
 
 	public static String[] discountFormLabel = { "Date", "Issued By", "Product", "Customer", "Amount" };
 
 	public static String[] depositFormLabel = { "Date*", "Bank*", "Bank Account*", "Depositor", "Amount*", "Issued by", "Remarks" };
 
 	public static String[] ARPaymentFormLabel = { "AR ID", "Date", "Issued By", "Customer Representative", "Amount" };
 
 	public static String[] CAPaymentFormLabel = { "CA ID", "Date", "Issued By", "Employee Representative", "Amount" };
 
 	public static String[] bankFormLabel = { "Name*", "Address", "Contact Number" };
 
 	/*
 	 * import java.awt.*;
 	 * 
 	 * public class GetMouse { public static void main(String[] args) { while
 	 * (true) { PointerInfo a = MouseInfo.getPointerInfo(); Point b =
 	 * a.getLocation(); int x = (int) b.getX(); int y = (int) b.getY();
 	 * System.out.println(x + ":" + y); try { Thread.sleep(1000); } catch
 	 * (InterruptedException e) { e.printStackTrace(); } } } }
 	 */
 }
