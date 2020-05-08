 package com.gsi.telecom.database;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.gsi.telecom.dao.CustomerDao;
 import com.gsi.telecom.data.Customer;
 import com.gsi.telecom.data.Product;
 
 public class CustomerDaoDerby extends ConnectionDaoDerby implements CustomerDao {
 
 	@Override
 	public void add(Customer customer) throws SQLException {
 		// We need to get the connection
 		Connection conn = getConnection();
 		// To create the string with the query
 		String orden = "INSERT INTO Customer (id, name, lastname, address) VALUES ("
 				+ customer.getId()
 				+ ", '"
 				+ customer.getName()
 				+ "', '"
 				+ customer.getLastname()
 				+ "', '"
 				+ customer.getAddress()
 				+ "')";
 		// Execute it
 		conn.createStatement().executeUpdate(orden);
 		// And close the connection
 		conn.close();
 	}
 
 	@Override
 	public void delete(Customer customer) throws SQLException {
 		Connection conn = getConnection();
 		try {
 			String orden = "delete from Customer where id=" + customer.getId();
 			conn.createStatement().executeUpdate(orden);
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This is a wrapper to the select function
 	 * 
 	 * @param id
	 * @return the Customer
 	 * @throws SQLException
 	 */
 	public Customer find(Integer id) throws SQLException {
 		return select(id);
 	}
 
 	/**
 	 * This returns a list with the products that customerA has ordered
 	 * 
 	 * @param customerA
 	 *            - the customer
 	 * @return - a <code>List&lt;Product&gt;</code> with the products
 	 * @throws SQLException
 	 */
 	public List<Product> getProductsForCustomer(Customer customerA)
 			throws SQLException {
 		List<Product> list = new ArrayList<Product>();
 		// Let's acquire the connection
 		Connection conn = getConnection();
 		ResultSet rs = null;
 		ResultSet rs2 = null;
 		try {
 			// Get the products customer has
 			String orden = "select id_product from Customer_Product where id_customer="
 					+ customerA.getId();
 			rs = conn.createStatement().executeQuery(orden);
 			if (rs != null) {
 				// For each product, get the actual concrete product (not just
 				// the id)
 				while (rs.next()) {
 					String orden2 = "SELECT * from Product p where id="
 							+ rs.getInt("id_product");
 
 					rs2 = conn.createStatement().executeQuery(orden2);
 					while (rs2.next()) {
 						// Create the product
 						Integer id = rs2.getInt("id");
 						String name = rs2.getString("name");
 						Float price = rs2.getFloat("price");
 						Boolean valid = rs2.getBoolean("valid");
 						String description = rs2.getString("description");
 						Product prod = new Product();
 						prod.setId(id);
 						prod.setName(name);
 						prod.setPrice(price);
 						prod.setValid(valid);
 						prod.setDescription(description);
 						// And add it to the list
 						list.add(prod);
 					}
 				}
 			}
 			// Close the connection
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// And return the list
 		return list;
 	}
 
 	@Override
 	public Customer select(Integer id) throws SQLException {
 		Connection conn = getConnection();
 		Customer cust = null;
 		ResultSet rs = null;
 		try {
 			String orden = "select * from Customer where id=" + id;
 			rs = conn.createStatement().executeQuery(orden);
 			// We need to iterate to get the first (and unique) result
 			rs.next();
 			Integer idd = rs.getInt("id");
 			String name = rs.getString("name");
 			String lastname = rs.getString("lastname");
 			String address = rs.getString("address");
 			// Create the new Customer with the data populated from the DB
 			cust = new Customer();
 			cust.setId(idd);
 			cust.setName(name);
 			cust.setLastname(lastname);
 			cust.setAddress(address);
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// Return the found Customer
 		return cust;
 	}
 
 	@Override
 	public Customer update(Customer customer) throws SQLException {
 		Connection conn = getConnection();
 		// We suppose that the customer IS on the database, otherwise, this will
 		// fail
 		try {
 			String orden = "UPDATE Customer set name='" + customer.getName()
 					+ "', " + "lastname='" + customer.getLastname() + "', "
 					+ "address='" + customer.getAddress() + "' where id="
 					+ customer.getId();
 			conn.createStatement().executeUpdate(orden);
 			conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return customer;
 	}
 
 }
