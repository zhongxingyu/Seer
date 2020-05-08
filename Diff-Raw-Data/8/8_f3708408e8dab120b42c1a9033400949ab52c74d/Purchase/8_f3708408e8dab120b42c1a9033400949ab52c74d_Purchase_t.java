 /*****************************************************************************************
  * 
  * Purchase.java
  *  
  * Aaron Averill
  * John Lloyd
  * Matthew Marum
  * Rob Parsons
  * 
  * CSC 540
  * Fall 2012
  * 
  * Purchase.java processes purchases for books from a vendor.
  * 
  */
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 
 
 public class Purchase extends AbstractCommandHandler {
 
 	private static String TABLE = "Purchase";
 
 	/*
 	 * Contruct a handler for staff objects.
 	 */
 	public Purchase(Connection connection) { 
 		super(connection);
 	}
 
 
 
 	/**
 	 * Execute the command to create a purchase record.
 	 * 
 	 * @param bookId
 	 *   The Id of the book record
 	 * @param vendorId
 	 *   The Id of the vendor record
 	 * @param staffId
 	 *   The Id of the staff record
 	 * @param qty
 	 *   The quantity of the purchase
 	 * @param price
 	 *   The wholesale price of the book 
 	 */
 	public void execAdd(		
 			@Param("bookId") String bookId, 
 			@Param("vendorId") String vendorId, 
 			@Param("staffId") String staffId,
 			@Param("quantity") String qty,
 			@Param("price") String price) throws ValidationException, SQLException {
 
 		Double priceValue = null;
 		Integer qtyValue = null;
 
 		// validate input parameters
 		try {
 			// check book ID numeric and in database
 			checkBookId(bookId);
 			// check vendor ID numeric
 			checkVendorId(vendorId);
 			// check staff ID numeric
 			checkStaffId(staffId);
 			// check quantity numeric and > 0
 			qtyValue = checkQty(qty);
 			// check price numeric and >= 0
 			priceValue = checkPrice(price);
 
 
 		} catch (ValidationException ex) {
 			System.out.println("Validation Error: " + ex.getMessage());
 			return;
 		}
 
 		// set purchase date to today
 		Date todaysDate = new java.util.Date();
 		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
 		String date = formatter.format(todaysDate);
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("bookId", bookId);
 		params.put("vendorId", vendorId);
 		params.put("staffId", staffId);
 		params.put("quantity", qtyValue);
 		params.put("wholesalePrice", priceValue);
 		params.put("orderDate", date);
 		params.put("status", "ordered");
 
 		int newID = insertRow(TABLE, "id", 1001, params);
 
 		System.out.println("Inserted Purchase record with ID " + newID + " into Database"); 
 
 	} // execAdd
 
 
 	/**
 	 * List all purchases in the system, ordered by the purchase id.
 	 */
 	public void execAll() throws SQLException {
 
 		// Select all rows in the staff table and sort by ID
 		String sql = "SELECT * FROM " + TABLE + " ORDER BY id";
 
 		Statement statement = createStatement();
 		int cnt = displayPurchase(statement.executeQuery(sql));
 
 		System.out.println(cnt+" Row(s) Returned");
 
 	} // execAll
 
 	/**
 	 * Delete the specified purchase record
 	 *
 	 * @param id
 	 *   The purchase id. Must be convertable to an integer.
 	 */
 	public void execDelete(@Param("staff id") String id) throws SQLException {
 
 		try {
 			checkPurId(id);
 		} catch (ValidationException e) {
 			System.out.println("Validation Error: " + e.getMessage());
 			return;
 		}
 		
 		int count = deleteRow(TABLE, Integer.parseInt(id));
 
 		System.out.println("Deleted "+ count + " Staff with ID " + id + " from Database"); 
 
 	} // exec delete
 
 	/**
 	 * Display the properties of a specific purchase record.
 	 *
 	 * @param id
 	 *   The purchase id. Must be convertable to an integer.
 	 */
 	public void execList(@Param("staff id") String id) throws SQLException {
 
 		try {
 			checkPurId(id);
 		} catch (ValidationException e) {
 			System.out.println("Validation Error: " + e.getMessage());
 			return; 
 		}
 		
 		// Select row in the Staff table with ID
 		String sql = "SELECT * FROM " + TABLE + " WHERE id = "+ Integer.parseInt(id);
 
 		Statement statement = createStatement();
 		int cnt = displayPurchase(statement.executeQuery(sql));
 
 		System.out.println(cnt+" Row(s) Returned");
 
 	}
 
 	/**
 	 * Execute the command to update a purchase record.
 	 * 
 	 * @param id
 	 *   The id of the purchase record we are updating
 	 * @param bookId
 	 *   The Id of the book record
 	 * @param vendorId
 	 *   The Id of the vendor record
 	 * @param staffId
 	 *   The Id of the staff record
 	 * @param qty
 	 *   The quantity of the purchase
 	 * @param price
 	 *   The wholesale price of the book 
 	 * @param orderDate
 	 *   The date the order was placed
 	 * @param status
 	 *   The status of the order (ordered, received, shipped)
 	 */
 	// TODO this needs testing!
 	public void execUpdate(	 
 			@Param("id") String purId,
 			@Param("bookId") String bookId, 
 			@Param("vendorId") String vendorId, 
 			@Param("staffId") String staffId,
 			@Param("quantity") String qty,
 			@Param("price") String price,
 			@Param("orderDate") String orderDate,
 			@Param("status") String status) throws ValidationException, SQLException {
 
 		Double priceValue = null;
 		Integer qtyValue = null;
 		Integer purIDValue = null;
 
 		// validate input parameters
 		try {
 			// check the purchase record id is numeric and in database
 			purIDValue = checkPurId(purId);
 			// check book ID numeric and in database
 			checkBookId(bookId);
 			// check vendor ID numeric
 			checkVendorId(vendorId);
 			// check staff ID numeric
 			checkStaffId(staffId);
 			// check quantity numeric and > 0
 			qtyValue = checkQty(qty);
 			// check price numeric and >= 0
 			priceValue = checkPrice(price);
 			// check valid format date
 			// TODO uses date of birth routine but not DoB
 			ValidationHelpers.checkDateOfBirth(orderDate);
 			// check status ordered, shipped, received
 			status = checkStatus(status);
 
 		} catch (ValidationException ex) {
 			System.out.println("Validation Error: " + ex.getMessage());
 			return;
 		}
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", purId);
 		params.put("bookId", bookId);
 		params.put("vendorId", vendorId);
 		params.put("staffId", staffId);
 		params.put("quantity", qtyValue);
 		params.put("wholesalePrice", priceValue);
 		params.put("orderDate", orderDate);
 		params.put("status", status);
 
 
 	    updateRow(TABLE, "id", purIDValue, params);
 
 		System.out.println("Update Purchase record with ID " + purId); 
 
 	} // execUpdate
 
 	// check the status passed from user is valid
 	private String checkStatus(String status) throws ValidationException {
 
 		Map<String, String> valid = new TreeMap<String, String>();
 		valid.put("O", "ordered");
 		valid.put("R", "received");
 		valid.put("S", "shipped");
 
 		return validateCode(status, "Status", valid);
 
 	} // checkStatus
 
 	/**
 	 * Check the Quantity. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable integer greater than zero.
 	 *
 	 * @param quantity
 	 *   The quantity of books purchased.
 	 */
 	private int checkQty(String qty) throws ValidationException {
 		try {
 			int qtyValue = Integer.parseInt(qty);
 			if (qtyValue <= 0) {
 				throw new ValidationException("Quantity must be greater than zero");
 			}
 			return qtyValue;
 		} catch (Exception e) {
 			throw new ValidationException("Quantity must be a number");
 		}
 	} // checkQty
 
 	/**
 	 * Check the Price. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable double greater than equal to zero.
 	 *
 	 * @param quantity
 	 *   The quantity of books purchased.
 	 */
 	private double checkPrice(String price) throws ValidationException {
 		try {
 			double priceValue = Double.parseDouble(price);
 			if (priceValue <= 0) {
 				throw new ValidationException("Price must be greater than or equal zero");
 			}
 			return priceValue;
 		} catch (Exception e) {
 			throw new ValidationException("Price must be a number");
 		} 
 	} // checkPrice
 
 	/**
 	 * Check the Purchase Id. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable integer greater than equal to zero and
	 * exist in the purchase table.
 	 *
 	 * @param id
 	 *   The id of the purchase record
 	 */
 	private int checkPurId(String purId) throws ValidationException {
 		int idValue;
 		
 		try {
 			idValue = Integer.parseInt(purId);
 			if (idValue <= 0) {
 				throw new ValidationException("Purchase Id must be a positive integer: "+purId);
 			}
 		} catch (Exception e) {
 			throw new ValidationException("Purchase Id must be a number: "+purId); 
 		}
 		try {
 			// Book ID must be in book table
 			String sql = "SELECT id FROM Purchase Where id='"+purId+"'";
 
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			ResultSet result = statement.executeQuery(sql);
 
 			if (result.next()) {
 				int id = result.getInt("id");
 				if (id != Integer.parseInt(purId)) {
 					throw new ValidationException("Purchase Id must be in database: "+ purId);
 				}
 			} else {
 				throw new ValidationException("Purchase Id must be in database: "+ purId);
 			}
 
 			return idValue;
 
 		} catch (Exception e) {
 			throw new ValidationException("Exception Validating Purchase Id: " + e.getMessage());
 		}
 
 	} // checkPurId
 	
 	/**
 	 * Check the BookId. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable integer greater than equal to zero and
 	 * exist in the books table.
 	 *
 	 * @param bookId
 	 *   The id of the record for the book purchased from vendor
 	 */
 	private void checkBookId(String bookId) throws ValidationException {
 		try {
 			int bookIdValue = Integer.parseInt(bookId);
 			if (bookIdValue <= 0) {
 				throw new ValidationException("Book Id must be a positive integer: "+bookId);
 			}
 		} catch (Exception e) {
 			throw new ValidationException("Book Id must be a number: "+bookId); 
 		}
 		try {
 			// Book ID must be in book table
 			String sql = "SELECT id FROM Book Where id='"+bookId+"'";
 
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			ResultSet result = statement.executeQuery(sql);
 
 			if (result.next()) {
 				int id = result.getInt("id");
 				if (id != Integer.parseInt(bookId)) {
 					throw new ValidationException("Book Id must be in database: "+ bookId);
 				}
 			} else {
 				throw new ValidationException("Book Id must be in database: "+ bookId);
 			}
 
 			return;
 
 		} catch (Exception e) {
 			throw new ValidationException("Exception Validating Book Id: " + e.getMessage());
 		}
 
 	} // checkBookId
 
 	/**
 	 * Check the VendorId. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable integer greater than equal to zero and
	 * exist in the vendor table.
 	 *
 	 * @param vendorId
 	 *   The id of the record for the vendor from which a book purchased
 	 */
 	private void checkVendorId(String vendorId) throws ValidationException {
 		try {
 			int vendorIdValue = Integer.parseInt(vendorId);
 			if (vendorIdValue <= 0) {
 				throw new ValidationException("Vendor Id must be a positive integer: "+vendorId);
 			}
 		} catch (Exception e) {
 			throw new ValidationException("Vendor Id must be a number: "+vendorId);
 		}
 		try {
 			// Book ID must be in book table
 			String sql = "SELECT id FROM Vendor Where id='"+vendorId+"'";
 
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			ResultSet result = statement.executeQuery(sql);
 
 			if (result.next()) {
 				int id = result.getInt("id");
 				if (id != Integer.parseInt(vendorId)) {
 					throw new ValidationException("Vendor Id must be in database: "+ vendorId);
 				}
 			} else {
 				throw new ValidationException("Vendor Id must be in database: "+ vendorId);
 			}
 
 			return;
 
 		} catch (Exception e) {
 			throw new ValidationException("Exception Validating Vendor Id: " + e.getMessage());
 		}
 
 	} // checkVendorId
 
 	/**
 	 * Check the StaffId. Throw a ValidationException if there is an error.
 	 *
 	 * Must be a parseable integer greater than equal to zero and
	 * exist in the staff table.
 	 *
 	 * @param staffId
 	 *   The id of the record for the staff member that purchased the book from the vendor
 	 */
 	private void checkStaffId(String staffId) throws ValidationException {
 		try {
 			int vendorIdValue = Integer.parseInt(staffId);
 			if (vendorIdValue <= 0) {
 				throw new ValidationException("Staff Id must be a positive integer: "+staffId);
 			}
 		} catch (Exception e) {
 			throw new ValidationException("Staff Id must be a number: "+staffId);
 		}
 		try {
 			// Book ID must be in book table
 			String sql = "SELECT id FROM Staff Where id='"+staffId+"'";
 
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			ResultSet result = statement.executeQuery(sql);
 
 			if (result.next()) {
 				int id = result.getInt("id");
 				if (id != Integer.parseInt(staffId)) {
 					throw new ValidationException("Staff Id must be in database: "+ staffId);
 				}
 			} else {
 				throw new ValidationException("Staff Id must be in database: "+ staffId);
 			}
 
 			return;
 
 		} catch (Exception e) {
 			throw new ValidationException("Exception Validating Vendor Id: " + e.getMessage());
 		}
 
 	} // checkStaffId
 
 	/**
 	 * Display the purchase record from the result set and return the total count.
 	 */
 	private int displayPurchase(ResultSet result) throws SQLException {
 
 		int cnt = 0;
 		// loop through the result set printing attributes
 		while (result.next()) {
 			cnt++;
 			int purId = result.getInt("id");
 			Date date = result.getDate("orderDate");
 			int bookId = result.getInt("bookId");
 			int vendorId = result.getInt("vendorId");
 			int staffId = result.getInt("staffId");
 			int qty = result.getInt("quantity");
 			String status = result.getString("status");
 			int price = result.getInt("wholesalePrice");
 
 			System.out.println(cnt+"\tID: "+purId+"\tDate: "+date+"\tBook ID: "+bookId+"\tVendor ID: "+vendorId+"\tStaff ID: "+staffId+"\tQty: "+qty+"\tStatus: "+status+"\tWholesale Price: "+price);
 		}
 
 		return cnt;
 
 	}
 
 }
