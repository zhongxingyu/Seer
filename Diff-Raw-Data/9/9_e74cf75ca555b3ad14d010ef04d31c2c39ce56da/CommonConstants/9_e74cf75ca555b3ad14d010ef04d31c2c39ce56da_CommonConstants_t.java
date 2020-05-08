 package com.fnz.common;
 
 public class CommonConstants
 {
 	public static final String DRIVERNAME = "org.sqlite.JDBC";
 	
 	public static final String sJdbc = "jdbc:sqlite";
 	
 	public static final String sTempDb = "inventory.db";
 	
 	//public static final String DB_LOCATION="C:\\Inventory\\";
 	
 	public static final String DB_LOCATION="Data\\";
 	
 	public static final Integer TIMEOUT = 30;
 	
 	public static final String CATEGORY_ID = "C_";
 	
 	public static final String ITEM_ID = "I_";
 	
 	public static final String TYPE_ID = "T_";
 	
 	public static final int ZERO = 0;
 	
 	public static final int CASE_SIZE = 12;
 	
 	public static final String STATUS_LOW_ON_STOCK = "Cancelled";
 	
 	public static final String STATUS_AVAILABLE = "Available";
 	
 	public static final String STATUS_UNAVAILABLE= "Unavailable";
 	
 	
 	public static final String CATEGORY_PREMIUM_SCOTCH = "Premium Scotch";
 	public static final String CATEGORY_REGULAR_SCOTCH = "Regular Scotch";
 	public static final String CATEGORY_PREMIUM_WHISKY= "Premium Whisky";
 	public static final String CATEGORY_REGULAR_WHISKY= "Regular Whisky"; 
 	public static final String CATEGORY_PREMIUM_VODKA= "Premium Vodka";
 	public static final String CATEGORY_REGULAR_VODKA= "Regular Vodka";
 	public static final String CATEGORY_BRANDY= "Brandy";
 	public static final String CATEGORY_GIN= "Gin";
 	public static final String CATEGORY_PREMIUM_RUM= "White Rum & Premixes";
  	public static final String CATEGORY_REGULAR_RUM= "Regular Rum";
  	public static final String CATEGORY_WINE= "Wine";
 	public static final String CATEGORY_BEER= "Beer";
 	public static final String CATEGORY_BEVRAGES= "Beverages";	
 	
 
 	
 	
 	
 	
 	
 	
 
 	
 	public static final String COMMA= ",";
 	
 	public static final String NIP= "NIP";
 	public static final String PINT= "PINT";
 	public static final String QUAD= "QUAD";
 	public static final String UPDATE_MSG= "Updated Successfully";
 	public static final String EMPTY_MSG = "*Mandatory field can't be empty";
 	public static final String ITEMADD_MSG = "Item added successfully";
 	public static final String CAT_MSG = "Category added successfully";
 	public static final String COMBO_MSG = "*Please select atleast one value";
 	public static final String DEL_MSG = "Deleted Successfully";
 	public static final String EDIT_MSG = "Edited Successfully";
 	public static final String ALREADY_EXISTS = "Already Exists !";
 	public static final String WRONG_DATE = "*Wrong Date format. Please enter in dd/MM/yyyy";
 	public static final String STAR_MSG = "* Mandatory Fields";
 	public static final String QTY_MSG1 = "# Quantity in bottles";
 	public static final String QTY_MSG2 = " Quantity in cartons";
 	public static final String SELECT_ITEM_MSG = "*Please Select atleast one item";
 	public static final String SELECT_CATEGORY = "*Please Select atleast one category";
 	public static final String SELECT_TYPE = "*Please Select atleast one type";
 	public static final String EDIT_TYPE_MSG = "Type Name Edited";
 	public static final String LOW_STOCK_MSG = " is low in Stock";
 	public static final String QTY_LOW = " Low in Stock";
 	public static final String DATE_COMPARE = "Start date can't be greater than End date";
 
	public static final String PURCHASE_INVOICE_ERROR = "InvoiceId & Date already exists. Incase of incorrect entry, please delete from history";
 }
