 package de.enwida.web.dao.implementation;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.enwida.web.dao.interfaces.AbstractBaseDao;
 import de.enwida.web.dao.interfaces.IDataAvailibilityDao;
 import de.enwida.web.model.DataAvailibility;
 
 @Repository
 @TransactionConfiguration(transactionManager = "jpaTransactionManager", defaultRollback = true)
 @Transactional(rollbackFor = Exception.class)
 public class DataAvailibilityDaoImpl extends AbstractBaseDao<DataAvailibility> implements IDataAvailibilityDao, RowMapper<DataAvailibility> {
 
 	public boolean isAvailableByExample(DataAvailibility dataAvailibility) {
		final String selectQuery = "SELECT COUNT(*) FROM availability WHERE product > ? AND product <= ? AND timefrom <= ? AND timeto >= ? AND tablename SIMILAR TO ?;";
 		
 		final Object[] params = new Object[] {
 			dataAvailibility.getProduct(),
 			Integer.parseInt(Integer.toString(dataAvailibility.getProduct()).replace('0', '9')),
 			new java.sql.Timestamp(dataAvailibility.getTimeFrom().getTime()),
 			new java.sql.Timestamp(dataAvailibility.getTimeTo().getTime()),
 			"%" + dataAvailibility.getTableName() + "%"
 		};
 		
 		final int count = jdbcTemplate.queryForInt(selectQuery, params);
 		return count > 0 ? true : false;
 	}
 
 	public List<DataAvailibility> getListByExample(DataAvailibility dataAvailibility) {
        final String selectQuery = "SELECT * FROM availability WHERE product > ? AND product <= ? AND tso = ? AND tablename SIMILAR TO ?;";
         
         final Object[] params = new Object[] {
         		dataAvailibility.getProduct(),
         		Integer.parseInt(Integer.toString(dataAvailibility.getProduct()).replace('0', '9')),
         		dataAvailibility.getTso(),
         		"%" + dataAvailibility.getTableName() + "%"
         };
 
         return jdbcTemplate.query(selectQuery, params, this);
 	}
 
 	@Override
 	public DataAvailibility mapRow(ResultSet rs, int rowNum) throws SQLException {
 		final DataAvailibility result = new DataAvailibility();
 		
 		result.setTso(rs.getInt("tso"));
 		result.setProduct(rs.getInt("product"));
 		result.setNrows(rs.getInt("nrows"));
 		result.setTableName(rs.getString("tablename"));
 		result.setTimeFrom(rs.getDate("timefrom"));
 		result.setTimeTo(rs.getDate("timeto"));
 		
 		return result;
 	}
 
 }
