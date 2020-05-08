 package tables;
 
 import java.sql.ResultSet;
 import database.Database;
 
 public class Customer extends Database {
 	private long id;
 	private int controller;
 	private String ssNbrr;
 	private String forname;
 	private String lastname;
 	private String phoneNbr;
 	private String address;
 	private String password;
 	private String smartParkID;
 	private String registered;
 	private long balance;
 
 	// == Settings for the Table ========================
 
 	private static String dbName = "test";
 	private String tblName = "Customer";
 	private static String[] columns = { "cont", "ssNbr", "Forname", "Lastname",
 			"Address", "PhoneNbr", "Password", "SmartParkID",
 			"RegistrationDate", "Balance" };
 
 	private String[] columnTypes = { "INT", "TEXT", "TEXT", "TEXT", "TEXT",
 			"TEXT", "TEXT", "TEXT", "TEXT", "REAL" };
 
 	boolean[] notNull = { true, true, true, true, true, true, true, true, true,
 			false };
 
 	// --------------------------------------------------
 
 	public static final String cont = "cont";
 	public static final String ssNbr = "ssNbr";
 	public static final String Forname = "Forname";
 	public static final String Lastname = "Lastname";
 	public static final String Address = "Address";
 	public static final String PhoneNbr = "PhoneNbr";
 	public static final String Password = "Password";
 	public static final String SmartParkID = "SmartParkID";
 	public static final String RegistrationDate = "RegistrationDate";
 	public static final String Balance = "Balance";
 
 	// private ResultSet result;
 
 	/*
 	 * Avail columns in the customer table
 	 */
 	public enum CustomerColumns {
 		ID, cont, ssNbr, Forname, Lastname, Address, PhoneNbr, Password, SmartparkID, Registered, Balance
 	}
 
 	/**
 	 * Constructor for Customer
 	 * 
 	 * @param id
 	 *            Long SocialSecurityNumber
 	 * @param forename
 	 *            String name
 	 * @param lastname
 	 *            String name
 	 */
 	/**
 	 * Empty Constructor to read customer data from database
 	 */
 	public Customer() {
 		super(dbName);
 
 	}
 
 	public Customer(String ssNbr) {
 		this();
 		selectCustomer(ssNbr, 1, false);
 	}
 
 	/**
 	 * Constructor to read and write customer data to the database
 	 * 
 	 * @param ssNbr
 	 * @param forename
 	 * @param lastname
 	 * @param adress
 	 * @param phoneNbr
 	 */
 	public Customer(int cont, String ssNbr, String forename, String lastname,
 			String address, String phoneNbr, String password,
 			String smartParkID, String registered) {
 		this();
 		this.controller = cont;
 		this.ssNbrr = ssNbr;
 		this.forname = forename;
 		this.lastname = lastname;
 		this.address = address;
 		this.phoneNbr = phoneNbr;
 		this.password = password;
 		this.smartParkID = smartParkID;
 		this.registered = registered;
 
 		/*
 		 * Find customer and update DB * IF table does not exist create new
 		 * table * IF customer does not exist create new entry
 		 */
 
 	}
 
 	/**
 	 * Before invoking this method, set the table name and columns and column
 	 * types and whether or not hey can be NULL. This method will then create a
 	 * table with this information or return an error message.
 	 * 
 	 * @return
 	 */
 	public String createCustomerTable() {
 		String error = createTable(tblName, columns, columnTypes, notNull);
 		if (error.length() == 0) {
 			System.out.println(tblName + " table successfully created in "
 					+ dbName);
 		}
 		return error;
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
 	 * 
 	 * @param columnData
 	 */
 	public void insertCustomerData(String[] columnData) {
 
 		insertIntoTable(tblName, columns, columnTypes, columnData);
 		String s = "";
 		for (String str : columnData) {
 			s += str + " ";
 		}
 		System.out.println(s);
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
	 * Dont forget to put ' around a string you search for. Example: 'Thomas'
 	 * 
 	 * @param searchString
 	 * @param columnNr
 	 */
 	public void selectCustomer(String searchString, int columnNr,
 			boolean rangeSelection) {
 
 		ResultSet result = selectDataFromTable(tblName, columns, searchString,
 				columnNr, rangeSelection);
 
 		try {
 			while (getResult().next()) {
 				this.id = result.getInt("ID");
 				this.controller = result.getInt("cont");
 				this.ssNbrr = result.getString("ssNbr");
 				this.forname = result.getString("Forname");
 				this.lastname = result.getString("Lastname");
 				this.address = result.getString("Address");
 				this.phoneNbr = result.getString("PhoneNbr");
 				this.password = result.getString("Password");
 				this.smartParkID = result.getString("SmartParkID");
 				this.registered = result.getString("RegistrationDate");
 				this.balance = result.getLong("Balance");
 				System.out.println(this.toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try {
 			getResult().close();
 			getStatement().close();
 			closeConnection();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
 	 * Update Customer data in Customer Table if exists. This method first finds
 	 * a searchValue in the searchCol you specify and then changes the value in
 	 * the whatCol you have specified to whatValue.
 	 * 
 	 * @param searchCol
 	 *            What Column are you searching for?
 	 * @param searchValue
 	 *            What value should that column contain?
 	 * @param whatCol
 	 *            What Column do you want to change?
 	 * @param whatValue
 	 *            What value should that column be?
 	 */
 	public void updateCustomerTable(String searchColumn, String searchValue,
 			String whatColumn, String whatValue) {
 		updateTableData(tblName, searchColumn, searchValue, whatColumn,
 				whatValue);
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
 	 * Set Row
 	 * 
 	 * @param id
 	 */
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
 	 * Get Row
 	 * 
 	 * @return
 	 */
 	public long getId() {
 		return id;
 	}
 
 	// -----------------------------------------------------------------
 	public boolean isController() {
 		return controller == 1;
 	}
 
 	// -----------------------------------------------------------------
 	public void setCont(int controller) {
 		this.controller = controller;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get SocialSecurityNumber
 	 * 
 	 * @return
 	 */
 	public void setSsNbr(String ssNbr) {
 		this.ssNbrr = ssNbr;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set SocialSecurityNumber
 	 * 
 	 * @return
 	 */
 	public String getSsNbr() {
 		return ssNbrr;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get Forname (String)
 	 * 
 	 * @return
 	 */
 	public String getForname() {
 		return forname;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set Forname
 	 * 
 	 * @param forname
 	 *            String forname
 	 */
 	public void setForename(String forname) {
 		this.forname = forname;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get Lastname (String)
 	 * 
 	 * @return
 	 */
 	public String getLastname() {
 		return lastname;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set Lastname
 	 * 
 	 * @param lastname
 	 *            String lastname
 	 */
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set Password
 	 * 
 	 * @param password
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get Password
 	 * 
 	 * @return
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set SmartParkID
 	 * 
 	 * @param smartParkID
 	 */
 	public void setSmartParkID(String smartParkID) {
 		this.smartParkID = smartParkID;
 	}
 
 	// -----------------------------------------------------------------
 
 	/**
 	 * Get SmartParkID
 	 * 
 	 * @return
 	 */
 	public String getSmartParkID() {
 		return smartParkID;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set Register date
 	 * 
 	 * @param registered
 	 */
 	public void setRegistered(String registered) {
 		this.registered = registered;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get Register date
 	 * 
 	 * @return
 	 */
 	public String getRegistered() {
 		return registered;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Get Balance
 	 * 
 	 * @return
 	 */
 	public long getBalance() {
 		return balance;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Set Balance
 	 * 
 	 * @param balance
 	 */
 	public void setBalance(long balance) {
 		this.balance = balance;
 	}
 
 	// -----------------------------------------------------------------
 	/**
 	 * Tostring method. Create a string with all the current data in the object.
 	 */
 	public String toString() {
 		/* @formatter:off */
 		String string = "ID: " + this.id + " controller: " + this.controller
 				+ " ssNbr: " + this.ssNbrr + " Name: " + this.forname
 				+ " Lastname: " + this.lastname + " Address: " + this.address
 				+ " PhoneNbr: " + this.phoneNbr + " Password: " + this.password
 				+ " SmartParkID: " + this.smartParkID + " Registered: "
 				+ this.registered + " Balance: " + this.balance;
 		/* @formatter:on */
 		return string;
 	}
 
 	// -----------------------------------------------------------------
 	public static void main(String[] args) {
 
 		Customer c = new Customer();
 		// c.createCustomerTable();
 		// String[] data = {"1", "910611", "Artur","Olech", "Snödroppsgatan3",
 		// "0762361910", "artur" ,
 		// "001First", "Today", "150"};
 		// c.insertCustomerData(data);
 		// String[] data2 = {"0", "820620", "Saeed","Ghasemi", "Folketspark",
 		// "0762361910", "saeed",
 		// "002First", "Today", "100"};
 		// c.insertCustomerData(data2);
 		// String[] data3 = {"0","666", "Truls","jobbarinte", "trelleborg",
 		// "9999999 999999", "truls",
 		// "003Third", "Never", "-500"};
 		// c.insertCustomerData(data3);
 		//
 		// System.out.println("\n");
 		// System.out.println("looking for all");
 		// c.selectCustomer(null, 0, false);
 		//
 		// System.out.println("\n");
 		// System.out.println("Select: 100-200 in balance");
 		// c.selectCustomer("100:200", 9, true);
 		//
 		// System.out.println("\n");
 		// System.out.println("Update Truls....");
 		// c.updateCustomerTable(Balance, "-500", Balance, "-900");
 		// System.out.println("\n");
 		// System.out.println("looking for all");
		 c.selectCustomer(ssN, 1, false);
 
 		// for (String s : args) {
 		// switch (s) {
 		// case "CreateTable":
 		// Customer c = new Customer();
 		// c.CreateCustomerTable();
 		// c.InsertCustomerData(new Customer(1, "910611", "Artur",
 		// "Olech", "Snödroppsgatan3", "0762361910", "artur",
 		// "001First", "Today"));
 		// c.InsertCustomerData(new Customer(0, "820620", "Saeed",
 		// "Ghasemi", "Hyllie", "0763150074", "saeed",
 		// "002Second", "Tomorrow"));
 		// c.InsertCustomerData(new Customer(0, "na", "Truls",
 		// "Haraldsson", "Trelleborg", "some number", "truls",
 		// "003Third", "Never"));
 		//
 		// break;
 		// case "Print":
 		// System.out
 		// .println("Printing all customer Tables in Database\n");
 		// c = new Customer();
 		// c.selectCustomer(null);
 		// break;
 		//
 		// default:
 		// System.out
 		// .println("Usage:\nCreateTable: To Create 3 customer default inserts\nPrint: to print all the created tables");
 		// break;
 		// }
 		// }
 
 	}
 }
