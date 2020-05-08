 package com.forum.repository;
 
 
 import com.forum.domain.Question;
 import org.junit.Test;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class QuestionRowMapperTest {
     @Test
     public void shouldMapQuestionFrommResultSet() throws SQLException {
         QuestionRowMapper questionRowMapper = new QuestionRowMapper();
         ResultSet resultSetMock = mock(ResultSet.class);
        when(resultSetMock.getInt("ID")).thenReturn(1);
         when(resultSetMock.getString("TITLE")).thenReturn("title");
         when(resultSetMock.getString("DESCRIPTION")).thenReturn("description");
 
         Question question = questionRowMapper.mapRow(resultSetMock, 1);
         assertThat(question.getId(), is(1));
         assertThat(question.getTitle(), is("title"));
         assertThat(question.getDescription(), is("description"));
     }
 
 }
