 package com.example.sextoncalculator.test;
 
 import android.test.AndroidTestCase;
 
 import com.example.sextoncalculator.FoodItem;
 
 public class FoodItemTest extends AndroidTestCase {
 
 	FoodItem one, two;
 	String name = "test";
 	double price = 3.49;
 	int quantity = 4;
 	int calories = 59;
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		one = new FoodItem();
 		two = new FoodItem(name, price, quantity);
 	}
 	
 	public void testInitialize(){
 		assertTrue(one.getName() == null);
 		assertTrue(two.getName().equals("test"));
 		assertTrue(two.getPrice() == 3.49);
 		assertTrue(two.getQuantity() == 4);
 	}
 	
 	public void testSettersAndGetters (){
 		one.setCalories(20);
 		one.setName("hello");
 		one.setPrice(2.32);
 		one.setQuantity(2);
 		assertEquals(one.getCalories(), 20);
 		assertEquals(one.getName(), "hello");
 		assertEquals(one.getPrice(), 2.32);
 		assertEquals(one.getQuantity(), 2);
 	}
 	
 	public void testToString() {
 		assertEquals(two.toString(), "test $3.49 X 4");
 		two.setCalories(30);
 		assertEquals(two.toString(), "test calories: 30");
 	}
 	
 	public void testEquals(){
 		one.setName("test");
 		one.setPrice(3.49);
 		one.setQuantity(4);
 		assertTrue(two.equals(one));
 	}
 	
 	public void testCompareTo(){
 		one.setName("test");
 		one.setPrice(3.49);
 		one.setQuantity(4);
 		assertEquals(two.compareTo(one), 0);
		one.setName("Fail");
		assertTrue(two.compareTo(one) != 0);
 	}
 
 }
