 package uw.cse403.minion.test;
 
 import uw.cse403.minion.Combat;
 import junit.framework.TestCase;
 
 /**
  * Whitebox test of backend Combat object
  * 
  * @author loki
  *
  */
 public class CombatTest extends TestCase {
 
 	/**
 	 * Test constructor
 	 * Only tests for non-crash 
 	 */
 	public void testCombat() {
 		Combat c = new Combat();
 		assertTrue(c != null);
 	}
 
 	/**
 	 * Test get base hp
 	 * Depends on constructor
 	 */
 	public void testGetBaseHP() {
 		Combat c = new Combat();
 		assertEquals(0, c.getBaseHP());
 	}
 
 	/**
 	 * Test set base hp
 	 * Depends on constructor and getBaseHP
 	 */
 	public void testSetBaseHP() {
 		Combat c = new Combat();
 		assertEquals(0, c.getBaseHP());
 		
 		int newBase = 8;
 		c.setBaseHP(newBase);
 		assertEquals(newBase, c.getBaseHP());
 		
 		newBase = 100000;
 		c.setBaseHP(newBase);
 		assertEquals(newBase, c.getBaseHP());
 	}
 
 	/**
 	 * Test get damage reduction
 	 * Depends on constructor
 	 */
 	public void testGetDamageReduction() {
 		Combat c = new Combat();
 		assertEquals(0, c.getDamageReduction());
 	}
 
 	/**
 	 * Test set damage reduction
 	 * Depends on constructor and getDamageReduction
 	 */
 	public void testSetDamageReduction() {
 		Combat c = new Combat();
 		assertEquals(0, c.getDamageReduction());
 		
 		int newDR = 1;
 		c.setDamageReduction(newDR);
 		assertEquals(newDR, c.getDamageReduction());
 		
 		newDR = 13;
 		c.setDamageReduction(newDR);
 		assertEquals(newDR, c.getDamageReduction());
 	}
 
 	/**
 	 * Test get lethal damage
 	 * Depends on constructor
 	 */
 	public void testGetLethalDamage() {
 		Combat c = new Combat();
 		assertEquals(0, c.getLethalDamage());
 	}
 
 	/**
 	 * Test set lethal damage
 	 * Depends on constructor and get lethal damage
 	 */
 	public void testSetLethalDamage() {
 		Combat c = new Combat();
 		assertEquals(0, c.getLethalDamage());
 		
 		int newDamage = 5;
 		c.setLethalDamage(newDamage);
 		assertEquals(newDamage, c.getLethalDamage());
 		
 		newDamage = 17;
 		c.setLethalDamage(newDamage);
 		assertEquals(newDamage, c.getLethalDamage());
 	}
 
 	/**
 	 * Test get bludgeoning damage
 	 * Depends on constructor
 	 */
 	public void testGetBludgeoningDamage() {
 		Combat c = new Combat();
 		assertEquals(0, c.getBludgeoningDamage());
 	}
 
 	/**
 	 * Test get bludgeoning damage
 	 * Depends on constructor and getBludgeoningDamage
 	 */
 	public void testSetBludgeoningDamage() {
 		Combat c = new Combat();
 		assertEquals(0, c.getBludgeoningDamage());
 		
 		int newDamage = 5;
 		c.setBludgeoningDamage(newDamage);
 		assertEquals(newDamage, c.getBludgeoningDamage());
 		
 		newDamage = 17;
 		c.setBludgeoningDamage(newDamage);
 		assertEquals(newDamage, c.getBludgeoningDamage());
 	}
 
 	/**
 	 * Test getting a single armor modifier
 	 * Depends on constructor
 	 */
 	public void testGetArmorModifier() {
 		Combat c = new Combat();
 		assertEquals(0, c.getArmorModifier("sheild"));
 	}
 
 	/**
 	 * Test setting a single armor modifier
 	 * Depends on constructor and getArmorModifier
 	 */
 	public void testAddArmorModifier() {
 		Combat c = new Combat();
 		assertEquals(0, c.getArmorModifier("sheild"));
 	
 		c.addArmorModifier("sheild", 2);
 		assertEquals(2, c.getArmorModifier("sheild"));
 	}
 	
 	/**
 	 * Test setting a single armor modifier
 	 * Depends on constructor, getArmorModifier and addArmorModifier
 	 */
 	public void testRemoveArmorModifier() {
 		Combat c = new Combat();
 		assertEquals(0, c.getArmorModifier("sheild"));
 		
 		c.removeArmorModifier("sheild");
 		assertEquals(0, c.getArmorModifier("sheild"));
 	
 		c.addArmorModifier("sheild", 2);
 		assertEquals(2, c.getArmorModifier("sheild"));
 		
 		c.removeArmorModifier("sheild");
 		assertEquals(0, c.getArmorModifier("sheild"));
 	}
 
 	/**
 	 * Test get base speed
 	 * Depends on constructor
 	 */
 	public void testGetSpeed() {
 		Combat c = new Combat();
 		assertEquals(0, c.getSpeed());
 	}
 
 	/**
 	 * Test set base speed
 	 * Depends on constructor and getSpeed
 	 */
 	public void testSetSpeed() {
 		Combat c = new Combat();
 		assertEquals(0, c.getSpeed());
 	
		c.setSpeed(30);
		assertEquals(30, c.getSpeed());
 	}
 
 	/**
 	 * Test get initiative modifiers
 	 * Depends on constructor
 	 */
 	public void testGetInitModifiers() {
 		Combat c = new Combat();
 		assertEquals(0, c.getInitModifier());
 	}
 
 	/**
 	 * Test set initiative modifiers
 	 * Depends on constructor and setInitModifiers
 	 */
 	public void testSetInitModifiers() {
 		Combat c = new Combat();
 		assertEquals(0, c.getInitModifier());
 	
 		c.setInitModifiers(2);
 		assertEquals(2, c.getInitModifier());
 		
 		c.setInitModifiers(1);
 		assertEquals(1, c.getInitModifier());
 	}
 
 	/**
 	 * Test get base attack bonus
 	 * Depends on constructor
 	 */
 	public void testGetbAb() {
 		Combat c = new Combat();
 		assertEquals(0, c.getbAb());
 	}
 
 	/**
 	 * Test set base attack bonus
 	 * Depends on constructor and getbAb
 	 */
 	public void testSetbAb() {
 		Combat c = new Combat();
 		assertEquals(0, c.getbAb());
 	}
 
 	/**
 	 * Test get total armor class
 	 * Depends on constructor, addArmorModifiers
 	 */
 	public void testGetArmorTotal() {
 		//TODO: Consider Dexterity
 		Combat c = new Combat();
 		assertEquals(10, c.getArmorTotal());
 		
 		c.addArmorModifier("armor", 4);
 		assertEquals(14, c.getArmorTotal());
 
 		c.addArmorModifier("size", -1);
 		assertEquals(13, c.getArmorTotal());
 	}
 
 	public void testWriteToDB() {
 		//TODO: Test database
 	}
 
 }
