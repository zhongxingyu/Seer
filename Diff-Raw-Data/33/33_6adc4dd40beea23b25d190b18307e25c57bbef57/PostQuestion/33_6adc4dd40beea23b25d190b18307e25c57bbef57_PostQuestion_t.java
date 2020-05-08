 package com.forum.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @Repository
 public class PostQuestion {
     @Autowired
     private JdbcTemplate jdbcTemplate;
     @Autowired
     private DataSource dataSource;
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public void insert(String tag, String question, String userName) {
         QuestionValidation validation = new QuestionValidation();
         question = validation.insertApostrophe(question);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         String dateformat = dateFormat.format(date);
        jdbcTemplate.execute("insert into Questions(question,post_date,user_name,tag) values('" + question + "','" + dateformat + "','" + userName + "','" + tag + "')");
     }
 }
