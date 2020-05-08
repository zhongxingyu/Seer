 package com.nelsonjrodrigues.twitter.repositories;
 
 import java.util.UUID;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 
 import com.nelsonjrodrigues.twitter.data.model.ApiToken;
 
 public class ApiTokenRepositoryImplTest extends AbstractRepositoryTest {
 
 	@Autowired
 	private ApiTokenRepository repository;
 
 	@Test
	public void testAddUser() {
 		ApiToken token = new ApiToken();
 
 		repository.save(token);
 
 		int rowsInTable = countRowsInTable("ApiTokens");
 
		Assert.assertEquals(1, rowsInTable);
 
 		ApiToken retrievedToken = simpleJdbcTemplate.queryForObject("select * from ApiTokens where id = ?", new BeanPropertyRowMapper<>(
 				ApiToken.class), token.getId());
 
 		Assert.assertEquals(token, retrievedToken);
 	}
 
 	@Test
	public void testRetrieveUser() {
 		String id = UUID.randomUUID().toString();
 
 		simpleJdbcTemplate.update("insert into ApiTokens values (?)", id);
 
 		ApiToken retrievedToken = repository.load(id);
 
 		Assert.assertNotNull(retrievedToken);
 		Assert.assertEquals(id, retrievedToken.getId());
 	}
 
 }
