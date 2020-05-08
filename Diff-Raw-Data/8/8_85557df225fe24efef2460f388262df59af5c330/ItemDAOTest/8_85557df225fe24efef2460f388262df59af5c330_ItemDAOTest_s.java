 package com.tda.persistence;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tda.model.item.Item;
 import com.tda.model.item.ItemBuilder;
 import com.tda.persistence.dao.ItemDAO;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/spring-persistence.xml" })
 @Transactional
 @TransactionConfiguration(defaultRollback = true)
 public class ItemDAOTest {
 	@Autowired
 	ItemDAO itemDAO;
 
 	@Test
 	public void save() {
 		Item anItem = ItemBuilder.createItem().withName("nombre").build();
 		itemDAO.save(anItem);
 		assertEquals(anItem, itemDAO.findById(anItem.getId()));
 	}
 
 	@Test
 	public void delete() {
 		Item anItem = ItemBuilder.createItem().withName("nombre").build();
 		itemDAO.save(anItem);
 		itemDAO.delete(anItem);
 		assertNull(itemDAO.findById(anItem.getId()));
 	}
 
 	@Test
 	public void update() {
 		Item anItem = ItemBuilder.createItem().withName("nombre").build();
 		itemDAO.save(anItem);
 		anItem.setName("newName");
 		itemDAO.update(anItem);
 		assertEquals(anItem, itemDAO.findById(anItem.getId()));
 	}
 
 	@Test
 	public void findById() {
 		Item anItem = ItemBuilder.createItem().withName("nombre").build();
 		itemDAO.save(anItem);
 		assertEquals(anItem, itemDAO.findById(anItem.getId()));
 	}
 
 	@Test
 	public void findAll() {
 		int originalCount = itemDAO.count();
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1").build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2").build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nombre3").build();
 		itemDAO.save(anItem3);
 		assertEquals(originalCount + 3, itemDAO.findAll().size());
 	}
 
 	@Test
 	public void deleteById() {
 		Item anItem = ItemBuilder.createItem().withName("nombre").build();
 		itemDAO.save(anItem);
 		itemDAO.deleteById(anItem.getId());
 		assertNull(itemDAO.findById(anItem.getId()));
 	}
 
 	@Test
 	public void count() {
 		int originalCount = itemDAO.count();
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1").build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2").build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nombre3").build();
 		itemDAO.save(anItem3);
		assertEquals(3, originalCount + itemDAO.count());
 	}
 
 	@Test
 	public void findByExample() {
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1")
 				.withQuantity(5L).build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2")
 				.withQuantity(5L).build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nombre3")
 				.withQuantity(10L).build();
 		itemDAO.save(anItem3);
 
 		Item example = ItemBuilder.createItem().withQuantity(5L).build();
 		assertEquals(2, itemDAO.findByExample(example).size());
 	}
 
 	@Test
 	public void findByNameContaining() {
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1").build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2").build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nada").build();
 		itemDAO.save(anItem3);
 
 		assertEquals(itemDAO.findByNameContaining("nombre").size(), 2);
 	}
 
 	@Test
 	public void findByDescriptionContaining() {
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1")
 				.withDescription("desc").build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2")
 				.withDescription("nada").build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nada")
 				.withDescription("anda").build();
 		itemDAO.save(anItem3);
 
 		assertEquals(itemDAO.findByDescriptionContaining("nada").size(), 1);
 	}
 
 	@Test
 	public void findByQuantityRange() {
 		Item anItem1 = ItemBuilder.createItem().withName("nombre1")
 				.withQuantity(5L).build();
 		itemDAO.save(anItem1);
 		Item anItem2 = ItemBuilder.createItem().withName("nombre2")
 				.withQuantity(10L).build();
 		itemDAO.save(anItem2);
 		Item anItem3 = ItemBuilder.createItem().withName("nombre3")
 				.withQuantity(15L).build();
 		itemDAO.save(anItem3);
 
 		assertEquals(2, itemDAO.findByQuantityRange(7L, 20L).size());
 	}
 }
