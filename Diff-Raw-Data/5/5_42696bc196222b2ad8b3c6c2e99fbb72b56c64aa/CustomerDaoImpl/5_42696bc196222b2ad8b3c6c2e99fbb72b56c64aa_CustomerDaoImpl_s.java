 package trey.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 
 import trey.model.Customer;
 
 public class CustomerDaoImpl extends JdbcDaoSupport implements CustomerDao {
 
	public Customer getCustomerById(long id) {
 		try {
 			return getJdbcTemplate().queryForObject("select * from customer", new CustomerRowMapper());
 		} catch (IncorrectResultSizeDataAccessException e) {
 			return null;
 		}
 	}
 
 	public void updateName(long custId, String first, String last) {
 		getJdbcTemplate().update("update customer set first_name = ?, last_name = ? where customer_id = ?", first,
 				last, custId);
 	}
 
 	public void updateAddress(long custId, String zip) {
 		getJdbcTemplate().update("update customer set zip = ? where customer_id = ?", zip, custId);
 	}
 
 	private static class CustomerRowMapper implements RowMapper<Customer> {
 		public Customer mapRow(ResultSet res, int rowNum) throws SQLException {
 			Customer cust = new Customer();
			cust.setCustomerId(res.getLong("id"));
 			cust.setFirstName(res.getString("first_name"));
 			cust.setLastName(res.getString("last_name"));
 			cust.setZip(res.getString("zip"));
 			return cust;
 		}
 	}
 
 }
