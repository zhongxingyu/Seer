 package uk.co.acuminous.julez.test;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 
import test.JdbcTestUtils;

 public class TestEventSchema {
 
     public static void ddl() {
         JdbcTemplate jdbcTemplate = new JdbcTemplate(JdbcTestUtils.getDataSource());
         jdbcTemplate.execute(
             "CREATE TABLE event (" + 
             "id VARCHAR(36), " + 
             "timestamp VARCHAR(255), " + 
             "type VARCHAR(255), " + 
             "PRIMARY KEY (id)" + 
             ")");
     } 
     
 }
