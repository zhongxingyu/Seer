 package test.model;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;

 import model.ProductQuantity;
 import model.Unit;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ProductQuantityTest {
 	// Test fixtures
 	private ProductQuantity fluid;
 	private ProductQuantity count;
 
 	@Before
 	public void setUp() throws Exception {
 		fluid = new ProductQuantity(3.2f, Unit.FLUID_OUNCES);
 		count = new ProductQuantity(1, Unit.COUNT);
 
 		// test invariants
 		assertTrue(fluid.getQuantity() >= 0);
 		assertTrue(fluid.getUnits() != null);
 		assertTrue(count.getQuantity() >= 0);
 		assertTrue(count.getUnits() != null);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		// test invariants
 		assertTrue(fluid.getQuantity() >= 0);
 		assertTrue(fluid.getUnits() != null);
 		assertTrue(count.getQuantity() >= 0);
 		assertTrue(count.getUnits() != null);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidAddCount() {
 		fluid.add(new ProductQuantity(1.0f, Unit.COUNT));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidAddToWeight() {
 		ProductQuantity weight = new ProductQuantity(2.0f, Unit.KILOGRAMS);
 		weight.add(new ProductQuantity(3.0f, Unit.FLUID_OUNCES));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidAddVolume() {
 		count.add(new ProductQuantity(1.0f, Unit.FLUID_OUNCES));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidAddWeight() {
 		fluid.add(new ProductQuantity(1.0f, Unit.KILOGRAMS));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidSubtractCount() {
 		fluid.subtract(new ProductQuantity(1.0f, Unit.COUNT));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidSubtractToWeight() {
 		ProductQuantity weight = new ProductQuantity(2.0f, Unit.KILOGRAMS);
 		weight.subtract(new ProductQuantity(3.0f, Unit.FLUID_OUNCES));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInvalidSubtractVolume() {
 		count.subtract(new ProductQuantity(1.0f, Unit.FLUID_OUNCES));
 	}
 
 	@Test
 	public void testIsValidProductQuantity() {
 		assertTrue(ProductQuantity.isValidProductQuantity(3.2f, Unit.FLUID_OUNCES));
 		assertTrue(ProductQuantity.isValidProductQuantity(0f, Unit.FLUID_OUNCES));
 		assertFalse(ProductQuantity.isValidProductQuantity(-1.4f, Unit.FLUID_OUNCES));
 		assertTrue(ProductQuantity.isValidProductQuantity(1, Unit.COUNT));
 		assertTrue(ProductQuantity.isValidProductQuantity(3, Unit.COUNT));
 		assertFalse(ProductQuantity.isValidProductQuantity(3.1f, Unit.COUNT));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testNegativeInvalidSubtract() {
 		ProductQuantity weight = new ProductQuantity(2.0f, Unit.KILOGRAMS);
 		weight.subtract(new ProductQuantity(3.0f, Unit.KILOGRAMS));
 	}
 
 	@Test
 	public void testProductQuantity() {
 		assertTrue(fluid.getQuantity() == 3.2f);
 		assertTrue(fluid.getUnits() == Unit.FLUID_OUNCES);
 		assertTrue(ProductQuantity.isValidProductQuantity(fluid.getQuantity(),
 				fluid.getUnits()));
 
 		assertTrue(count.getQuantity() == 1);
 		assertTrue(count.getUnits().equals(Unit.COUNT));
 		assertTrue(ProductQuantity.isValidProductQuantity(count.getQuantity(),
 				count.getUnits()));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSetInvalidQuantity() {
 		count.setQuantity(1.2f);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSetNegativeQuantity() {
 		count.setQuantity(-1f);
 	}
 
 	@Test
 	public void testSetValidQuantity() {
 		fluid.setQuantity(0f);
 		assertTrue(fluid.getQuantity() == 0f);
 
 		fluid.setQuantity(200.423f);
 		assertTrue(fluid.getQuantity() == 200.423f);
 
 		count.setQuantity(1);
 		assertTrue(count.getQuantity() == 1);
 		count.setQuantity(200);
 		assertTrue(count.getQuantity() == 200);
 	}
 
 	@Test
 	public void testToString() {
		assertTrue(fluid.toString().equals("3.2 Fluid_ounces"));
 		assertTrue(count.toString().equals("1 Count"));
 	}
 
 	@Test
 	public void testValidAddCount() {
 		count.add(new ProductQuantity(4.0f, Unit.COUNT));
 		assertTrue(count.getQuantity() == 5.0f);
 		assertTrue(count.getUnits().equals(Unit.COUNT));
 	}
 
 	@Test
 	public void testValidAddVolume() {
 		fluid.add(new ProductQuantity(1.0f, Unit.GALLONS));
 		assertTrue(fluid.getQuantity() == 131.2f);
 		assertTrue(fluid.getUnits().equals(Unit.FLUID_OUNCES));
 
 		fluid.add(new ProductQuantity(3.3f, Unit.LITERS));
 		assertTrue(fluid.getQuantity() == 242.7862f);
 		assertTrue(fluid.getUnits().equals(Unit.FLUID_OUNCES));
 
 		fluid.add(new ProductQuantity(2.0f, Unit.PINTS));
 		assertTrue(fluid.getQuantity() == 274.7862f);
 		assertTrue(fluid.getUnits().equals(Unit.FLUID_OUNCES));
 
 		fluid.add(new ProductQuantity(3.0f, Unit.QUARTS));
 		assertTrue(fluid.getQuantity() == 370.7862f);
 		assertTrue(fluid.getUnits().equals(Unit.FLUID_OUNCES));
 	}
 
 	@Test
 	public void testValidAddWeight() {
 		ProductQuantity kilos = new ProductQuantity(2.1f, Unit.KILOGRAMS);
 		kilos.add(new ProductQuantity(1.1f, Unit.GRAMS));
 		assert (kilos.getQuantity() == 2.1011f);
 		assert (kilos.getUnits().equals(Unit.KILOGRAMS));
 
 		kilos.add(new ProductQuantity(2.3f, Unit.POUNDS));
 		assertTrue(kilos.getQuantity() == 3.1443615f);
 		assertTrue(kilos.getUnits().equals(Unit.KILOGRAMS));
 
 		kilos.add(new ProductQuantity(12.2f, Unit.OUNCES));
 		assertTrue(kilos.getQuantity() == 3.4902253f);
 		assertTrue(kilos.getUnits().equals(Unit.KILOGRAMS));
 	}
 
 	@Test
 	public void testValidSubtractVolume() {
 		ProductQuantity gallons = new ProductQuantity(3.0f, Unit.GALLONS);
 		gallons.subtract(fluid);
 		assertTrue(gallons.getQuantity() == 2.975f);
 		assertTrue(gallons.getUnits().equals(Unit.GALLONS));
 
 		gallons.subtract(new ProductQuantity(0.5f, Unit.LITERS));
 		assertTrue(gallons.getQuantity() == 2.8429139f);
 		assertTrue(gallons.getUnits().equals(Unit.GALLONS));
 
 		gallons.subtract(new ProductQuantity(0.5f, Unit.PINTS));
 		assertTrue(gallons.getQuantity() == 2.7804139f);
 		assertTrue(gallons.getUnits().equals(Unit.GALLONS));
 
 		gallons.subtract(new ProductQuantity(0.5f, Unit.QUARTS));
 		assertTrue(gallons.getQuantity() == 2.6554139f);
 		assertTrue(gallons.getUnits().equals(Unit.GALLONS));
 	}
 
 	@Test
 	public void testValidSubtractWeight() {
 		ProductQuantity kilos = new ProductQuantity(3.0f, Unit.KILOGRAMS);
 
 		kilos.subtract(new ProductQuantity(0.5f, Unit.POUNDS));
 		assertTrue(kilos.getQuantity() == 2.773204f);
 		assertTrue(kilos.getUnits().equals(Unit.KILOGRAMS));
 
 		kilos.subtract(new ProductQuantity(100f, Unit.GRAMS));
 		assertTrue(kilos.getQuantity() == 2.6732042f);
 		assertTrue(kilos.getUnits().equals(Unit.KILOGRAMS));
 
 		kilos.subtract(new ProductQuantity(20f, Unit.OUNCES));
 		assertTrue(kilos.getQuantity() == 2.106214f);
 		assertTrue(kilos.getUnits().equals(Unit.KILOGRAMS));
 	}
 
 }
