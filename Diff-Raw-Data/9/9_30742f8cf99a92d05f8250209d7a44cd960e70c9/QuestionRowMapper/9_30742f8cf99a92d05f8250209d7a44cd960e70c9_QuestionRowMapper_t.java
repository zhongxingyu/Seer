 package com.forum.repository;
 
 import com.forum.domain.Question;
 
 import com.forum.domain.User;
 import org.springframework.jdbc.core.RowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class QuestionRowMapper implements RowMapper {
 
     @Override
     public Question mapRow(ResultSet resultSet, int i) throws SQLException {
 
 
         User user = new User(
                 resultSet.getInt("ID"),
                 resultSet.getString("PASSWORD"),
                 resultSet.getString("NAME"),
                 resultSet.getString("EMAIL_ADDRESS"),
                 resultSet.getString("PHONE_NUMBER"),
                 resultSet.getString("COUNTRY"),
                 resultSet.getString("GENDER"),
                 resultSet.getInt("AGE"));
 
        Question question = new Question(resultSet.getInt("QUESTION_ID"),
                 resultSet.getString("TITLE"),
                 resultSet.getString("DESCRIPTION"),
                 user,
                 resultSet.getTimestamp("CREATED_AT"));
         return question;
     }
 }
