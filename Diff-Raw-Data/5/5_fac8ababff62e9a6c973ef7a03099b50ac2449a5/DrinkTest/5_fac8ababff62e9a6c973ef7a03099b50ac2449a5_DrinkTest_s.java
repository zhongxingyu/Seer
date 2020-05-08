 package edu.ucsb.cs56.S13.lab04.bronhuston;
 import static org.junit.Assert.assertTrue;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 
 /** Test class for Drink
 
  * @author Bronwyn Perry-Huston
  * @version 2013/05/1 for lab04, cs56, s13
  * @see Drink
 
 */
 
 public class DrinkTest {
 
 
     private Drink coffee;
 
     @Before
     public void initDrink() {
 	this.coffee = new Drink("Latte","Medium",2);
     }
 
    /** Test case for Drink No Arguement Constructor
         @see Drink
     */
     @Test
 	public void test_NoArgConstructor() {
 
 	Drink s = new Drink();
 	assertEquals("water",s.getDrinkName());
 	assertEquals("medium",s.getSize());
     assertEquals(0, s.getCost());
 
     }
 
    /** Test case for Drink Three Arguement Constructor
         @see Drink
     */
     @Test
 	public void test_ThreeArgConstructor() {
 
 	Drink s = new Drink("soda","large",5);
 	assertEquals("soda",s.getDrinkName());
 	assertEquals("large",s.getSize());
     assertEquals(5, s.getCost());
 
     }
 
 
     /** Test case for Drink.getDrinkName()
         @see Drink
     */
     @Test
 	public void test_getDrinkName() {
 	    assertEquals("Latte",coffee.getDrinkName());
     }
 
     /** Test case for Drink.getSize()
         @see Drink
     */
     @Test
 	public void test_getSize() {
 	    assertEquals("Medium",coffee.getSize());
     }
    
     /** Test case for Drink.getCost()
         @see Drink
     */
     @Test
 	public void test_getCost() {
 	    assertEquals(2,coffee.getCost());
     }
 
     /** Test case for Drink.setDrinkName()
         @see Drink
     */
     @Test
 	public void test_setDrinkName() {
         coffee.setDrinkName("Mocha");
 	    assertEquals("Mocha",coffee.getDrinkName());
     }
 
     /** Test case for Drink.setSize()
         @see Drink
     */
     @Test
 	public void test_setSize() {
         coffee.setSize("Small");
 	    assertEquals("Small",coffee.getSize());
     }
    
     /** Test case for Drink.setCost()
         @see Drink
     */
     @Test
 	public void test_setCost() {
         coffee.setCost(3);
 	    assertEquals(3,coffee.getCost());
     }
 
 
     /** Test case for Drink.toString()
         @see Drink
     */    
     @Test
     public void test_toString(){
         assertEquals("Medium Latte, $2",coffee.toString());
     }
 
     /** Test case for Drink.equals()
         @see Drink
     */
     @Test
     public void test_equals()
     {
         Drink s = new Drink("Latte","Medium",2);
         assertTrue(coffee.equals(s));
     }
 
 }
