 package com.odea.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Service;
 
 import com.odea.domain.Usuario;
 
 //"SELECT * FROM users WHERE u_login='" + login + "' AND u_password=password('" + psswd + "')	"
 
 @Service
 public class MySqlLoginUtil extends AbstractDAO{
 	
	public Boolean logear(String login){
 		try {
			jdbcTemplate.queryForObject("SELECT * FROM users WHERE u_login=?", new RowMapper<Usuario>(){
 					@Override
 					public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
 						return new Usuario(1,"no te interesa","tampoco te interesa");
 					}
				}, login);
 				
 			return true;
 		} catch (DataAccessException e) {
 			//System.out.println("clave incorrecta");
 			return false;
 		}
 	}
 	
 	
 }
