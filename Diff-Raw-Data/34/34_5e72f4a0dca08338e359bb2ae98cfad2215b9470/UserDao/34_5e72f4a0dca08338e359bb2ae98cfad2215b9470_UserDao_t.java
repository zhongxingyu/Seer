 package org.antbear.tododont.backend.security.dao;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.authority.AuthorityUtils;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 @Transactional
 @Repository
 public class UserDao {
 
     @Autowired
     private JdbcDaoImpl userDetailsService;
 
     public boolean exists(final String email) {
         final int count = this.userDetailsService.getJdbcTemplate().queryForInt(
                 "SELECT COUNT(*) FROM users WHERE email = ?", email);
         return count > 0;
     }
 
     public void createActiveUser(final String email, final String password, final String registrationToken) {
         createUser(email, password, true, registrationToken);
     }
 
     public void createInactiveUser(final String email, final String password, final String registrationToken) {
         createUser(email, password, false, registrationToken);
     }
 
     public void createUser(final String email, final String password, final boolean enabled, final String registrationToken) {
         this.userDetailsService.getJdbcTemplate().update(
                 "INSERT INTO users (email,password,enabled,registrationtoken) VALUES (?,?,?,?)",
                 email, password, enabled, registrationToken);
 
         this.userDetailsService.getJdbcTemplate().update(
                 "INSERT INTO authorities (email, authority) VALUES (?, ?)",
                 email, "ROLE_USER");
     }
 
     public void saveChangePasswordToken(final String email, final String passwordChangeToken) {
         this.userDetailsService.getJdbcTemplate().update(
                 "UPDATE users SET passwordchangetoken = ? WHERE email = ?",
                 passwordChangeToken, email);
     }
 
     public String getChangePasswordToken(final String email) {
         return this.userDetailsService.getJdbcTemplate().queryForObject(
                 "SELECT passwordchangetoken FROM users WHERE email = ?", String.class, email);
     }
 
     public String findRegistrationTokenByUser(final String email) {
         final String sql = "SELECT registrationtoken FROM users WHERE email = ? AND enabled = 0 AND registrationtoken IS NOT NULL";
         final String token = this.userDetailsService.getJdbcTemplate().queryForObject(sql, String.class, email);
         return token;
     }
 
     public boolean getActiveStateByUser(final String email) {
         final int enabled = this.userDetailsService.getJdbcTemplate().queryForInt("SELECT enabled FROM users WHERE email = ?", email);
         return enabled == 0 ? false : true;
     }
 
     public void activateUser(final String email) {
         this.userDetailsService.getJdbcTemplate().update("UPDATE users SET enabled = 1, registrationtoken = NULL WHERE email = ?", email);
     }
 
     public UserDetails findUser(final String email) {
         return userDetailsService.loadUserByUsername(email);
     }
 
     public void delete(final String email) {
         this.userDetailsService.getJdbcTemplate().update("DELETE FROM users WHERE email = ?", email);
     }
 
     public void updatePassword(final String email, final String password) {
        this.userDetailsService.getJdbcTemplate().update(
                "UPDATE users SET password = ?, PASSWORDCHANGETOKEN = NULL WHERE email = ?", password, email);
     }
 
     public List<UserDetails> findAllWithoutAuthorities() {
         return this.userDetailsService.getJdbcTemplate().query("SELECT email,password,enabled FROM users", new RowMapper<UserDetails>() {
             @Override
             public UserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                 String email = rs.getString(1);
                 String password = rs.getString(2);
                 boolean enabled = rs.getBoolean(3);
                 return new User(email, password, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES);
             }
         });
     }
 }
