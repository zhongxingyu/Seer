 package org.deegree.securityproxy.authentication.repository;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 
 /**
  * Loads {@link UserDetails} from a {@link DataSource}.
  * 
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 public class UserDetailsDao {
 
     @Autowired
     private DataSource source;
 
     private final String schemaName;
 
     private final String tableName;
 
     private final String headerColumn;
 
     private final String userNameColumn;
 
     private final String passwordColumn;
 
     public UserDetailsDao( String schemaName, String tableName, String headerColumn, String userNameColumn,
                            String passwordColumn ) {
         this.schemaName = schemaName;
         this.tableName = tableName;
         this.headerColumn = headerColumn;
         this.userNameColumn = userNameColumn;
         this.passwordColumn = passwordColumn;
     }
 
     /**
      * Verify an header value against the encapsulated data source.
      * 
      * @param headerValue
      *            never <code>null</code> or empty
      * @return the user details that match the given header value
      * @throws IllegalArgumentException
      *             on <code>null</code> or empty argument
      */
     public UserDetails loadUserDetailsFromDataSource( String headerValue )
                             throws IllegalArgumentException {
         checkParameter( headerValue );
         JdbcTemplate template = new JdbcTemplate( source );
         RowMapper<UserDetails> rowMapper = createUserDetailsRowMapper();
         String jdbcString = generateSqlQuery();
         try {
             return (UserDetails) template.queryForObject( jdbcString, new Object[] { headerValue }, rowMapper );
         } catch ( DataAccessException e ) {
             return null;
         }
     }
 
     private void checkParameter( String headerValue ) {
         if ( headerValue == null || "".equals( headerValue ) )
             throw new IllegalArgumentException( "Header value may not be null or empty!" );
     }
 
     private String generateSqlQuery() {
         StringBuilder builder = new StringBuilder();
         builder.append( "SELECT " );
         builder.append( userNameColumn ).append( "," ).append( passwordColumn );
         appendFrom( builder );
         builder.append( " WHERE " ).append( headerColumn ).append( " = ? LIMIT 1" );
         return builder.toString();
     }
 
     private void appendFrom( StringBuilder builder ) {
         builder.append( " FROM " );
         if ( schemaName != null && !"".equals( schemaName ) )
             builder.append( schemaName ).append( "." );
         builder.append( tableName );
     }
 
     private RowMapper<UserDetails> createUserDetailsRowMapper() {
         RowMapper<UserDetails> rowMapper = new RowMapper<UserDetails>() {
             public UserDetails mapRow( ResultSet rs, int rowNum )
                                     throws SQLException {
                String username = rs.getString( userNameColumn );
                String password = rs.getString( passwordColumn );
                 return new User( username, password, Collections.<GrantedAuthority> emptyList() );
             }
         };
         return rowMapper;
     }
 }
