 package org.coder36.webdemo.service.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
import org.coder36.webdemo.advice.Logit;
 import org.coder36.webdemo.service.DatabaseService;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jndi.JndiObjectFactoryBean;
 import org.springframework.stereotype.Service;
 
 @Service( "databaseService" )
@Logit( "here")
 public class DatabaseServiceImpl implements DatabaseService {
 
 	@Override
 	public List<Object[]> getData(String jndi, String sql) {
 		try {
 			JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
 			bean.setJndiName( jndi );
 			bean.afterPropertiesSet();
 			DataSource ds = (DataSource) bean.getObject();
 			JdbcTemplate jdbcTemplate = new JdbcTemplate();
 			jdbcTemplate.setDataSource( ds  );
 			return jdbcTemplate.query( sql, new SimpleRowMapper() );
 		}
 		catch( NamingException n ) {
 			throw new RuntimeException( n );
 		}
 	}
 
 	
 	private class SimpleRowMapper implements RowMapper<Object[]> {
 
 		@Override
 		public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
 			
 			List<Object> l = new ArrayList<Object>();
 			for ( int i=1; i <= rs.getMetaData().getColumnCount(); i++ ) {
 				l.add( rs.getObject(i) );
 			}			
 			return l.toArray();
 		}		
 	}
 	
 }
