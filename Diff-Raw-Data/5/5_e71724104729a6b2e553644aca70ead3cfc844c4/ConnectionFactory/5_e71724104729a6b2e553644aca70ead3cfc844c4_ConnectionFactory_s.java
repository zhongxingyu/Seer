 package com.wp.carlos4web.cdi.dao.connection;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.apache.log4j.Logger;
 
 public class ConnectionFactory{
 
 	@Inject
 	private Logger logger;
 	
	private Connection connection;

 	@Produces
 	public Connection getConnection(){
 		logger.info("Configurando uma conexão com o banco de dados...");
 		
 		try {
			return DriverManager.getConnection("jdbc:mysql://localhost/gp", "root", "123");
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
