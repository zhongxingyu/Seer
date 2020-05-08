 package fr.webmarket.backend.test.model;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import fr.webmarket.backend.features.search.ISearchCriterion;
 import fr.webmarket.backend.features.search.ItemSearchEngine;
 import fr.webmarket.backend.features.search.SearchAccuracy;
 import fr.webmarket.backend.features.search.SearchStrategy;
 import fr.webmarket.backend.features.search.criteria.ItemBrandCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemDescriptionCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemNameCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemPriceCriterion;
 import fr.webmarket.backend.features.search.criteria.ItemTagCriterion;
 import fr.webmarket.backend.model.Item;
 import fr.webmarket.backend.model.ItemsCatalog;
 import fr.webmarket.backend.model.ItemTag;
 
 public class ItemSearchingTest {
 
 	ItemsCatalog catalog;
 	ItemSearchEngine engine;
 
 	private Item item1;
 	private Item item6;
 	private Item item2;
 	private Item item3;
 	private Item item4;
 	private Item item5;
 
 	private ItemTag electroMenager;
 	private ItemTag aspirateur;
 	private ItemTag laveVaisselle;
 	private ItemTag lecteurSalon;
 	private ItemTag hifi;
 	private ItemTag baladeur;
 
 	@Before
 	public void init() {
 
 		catalog = new ItemsCatalog();
 		engine = new ItemSearchEngine();
 
		electroMenager = new ItemTag("Electromnager");
 		aspirateur = new ItemTag("Aspirateur");
 		laveVaisselle = new ItemTag("Lave-Vaisselle");
 		lecteurSalon = new ItemTag("Lecteur Salon");
 		hifi = new ItemTag("HiFi");
 		baladeur = new ItemTag("Baladeur");
 
 		item1 = new Item("Lave-Vaisselle PV123", "Miele", "", 250);
 		item1.getTags().add(electroMenager);
 		item1.getTags().add(laveVaisselle);
 
 		item2 = new Item("Lave-Vaisselle", "Wirpool", "", 190);
 		item2.getTags().add(electroMenager);
 		item2.getTags().add(laveVaisselle);
 
 		item3 = new Item("iPod", "Apple", "Pouet", 340);
 		item3.getTags().add(hifi);
 		item3.getTags().add(baladeur);
 
 		item4 = new Item("Lecteur Blu-Ray", "Samsung", "", 190);
 		item4.getTags().add(hifi);
 		item4.getTags().add(lecteurSalon);
 
 		item5 = new Item("Aspirateur SupraPlus", "Dyson", "", 450);
 		item5.getTags().add(aspirateur);
 		item5.getTags().add(electroMenager);
 
 		item6 = new Item("Lave-Vaisselle PV567", "Miele", "", 280);
 		item6.getTags().add(electroMenager);
 		item6.getTags().add(laveVaisselle);
 	}
 
 	@Test
 	public void testSimpleSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 300));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle), SearchStrategy.ALL_CRITERIA));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertTrue(result.contains(item1));
 		assertTrue(result.contains(item2));
 	}
 
 	@Test
 	public void testUncompatibleCriteriaSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 300));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle, hifi), SearchStrategy.ALL_CRITERIA));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertTrue(result.size() == 0);
 	}
 
 	@Test
 	public void testPriceAndTagsCriteriaSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 		catalog.addItem(item3);
 		catalog.addItem(item4);
 		catalog.addItem(item5);
 		catalog.addItem(item6);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 300));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle, hifi), SearchStrategy.ONE_OF_CRITERION));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertTrue(result.contains(item1));
 		assertTrue(result.contains(item2));
 		assertFalse(result.contains(item3));
 		assertTrue(result.contains(item4));
 		assertFalse(result.contains(item5));
 		assertTrue(result.contains(item6));
 	}
 
 	@Test
 	public void testBrandCriteriaSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 		catalog.addItem(item3);
 		catalog.addItem(item4);
 		catalog.addItem(item5);
 		catalog.addItem(item6);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 500));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle, hifi), SearchStrategy.ONE_OF_CRITERION));
 		criteria.add(new ItemBrandCriterion("Apple", SearchAccuracy.FLEXIBLE));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertFalse(result.contains(item1));
 		assertFalse(result.contains(item2));
 		assertTrue(result.contains(item3));
 		assertFalse(result.contains(item4));
 		assertFalse(result.contains(item5));
 		assertFalse(result.contains(item6));
 	}
 
 	@Test
 	public void testDescriptionCriteriaSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 		catalog.addItem(item3);
 		catalog.addItem(item4);
 		catalog.addItem(item5);
 		catalog.addItem(item6);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 500));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle, hifi), SearchStrategy.ONE_OF_CRITERION));
 		criteria.add(new ItemDescriptionCriterion("Pouet",
 				SearchAccuracy.FLEXIBLE));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertFalse(result.contains(item1));
 		assertFalse(result.contains(item2));
 		assertTrue(result.contains(item3));
 		assertFalse(result.contains(item4));
 		assertFalse(result.contains(item5));
 		assertFalse(result.contains(item6));
 	}
 
 	@Test
 	public void testNameCriteriaSearching() {
 
 		// Load data
 		catalog.addItem(item1);
 		catalog.addItem(item2);
 		catalog.addItem(item3);
 		catalog.addItem(item4);
 		catalog.addItem(item5);
 		catalog.addItem(item6);
 
 		// Load all criteria
 		List<ISearchCriterion> criteria = Lists.newArrayList();
 		criteria.add(new ItemPriceCriterion(190, 500));
 		criteria.add(new ItemTagCriterion(Sets.newHashSet(electroMenager,
 				laveVaisselle, hifi), SearchStrategy.ONE_OF_CRITERION));
 		criteria.add(new ItemNameCriterion("Aspirateur SupraPlus",
 				SearchAccuracy.FLEXIBLE));
 
 		// Launch searching
 		List<Item> result = engine.searchFor(catalog, criteria,
 				SearchStrategy.ALL_CRITERIA);
 
 		// Check result
 		assertFalse(result.contains(item1));
 		assertFalse(result.contains(item2));
 		assertFalse(result.contains(item3));
 		assertFalse(result.contains(item4));
 		assertTrue(result.contains(item5));
 		assertFalse(result.contains(item6));
 	}
 }
