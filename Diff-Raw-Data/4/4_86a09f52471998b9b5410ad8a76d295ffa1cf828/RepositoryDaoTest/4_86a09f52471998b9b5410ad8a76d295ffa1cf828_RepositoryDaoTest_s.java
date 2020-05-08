 package com.github.scorekeeper.persistence.dao;
 
 import javax.annotation.Resource;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 
 import com.github.scorekeeper.persistence.entity.Player;
 
 @ContextConfiguration("/persistence-context.xml")
 public class RepositoryDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
 
 	@Resource
 	private PlayerRepository playerRepository;
 
 	@Test
 	public void createPlayer() {
 		Player player = new Player();
 		player.setName("X. Ample");
 
 		player = playerRepository.save(player);
 
 		Assert.assertNotNull(player.getId());
 	}
 
 }
