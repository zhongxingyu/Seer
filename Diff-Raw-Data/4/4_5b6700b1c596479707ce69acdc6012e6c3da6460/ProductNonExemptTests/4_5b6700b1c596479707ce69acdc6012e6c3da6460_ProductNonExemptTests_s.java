 package salestax;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class ProductNonExemptTests {
 
      @Test(expected = java.lang.IllegalArgumentException.class)
     public void ProductNonExempt_ExceptionThrownIf_QuantityLessThanOne() throws Exception {
         ProductNonExempt zeroQuantity = new ProductNonExempt(0, false, "zero cokes", 1.00);
     }
 
     @Test(expected = java.lang.IllegalArgumentException.class)
     public void ProductNonExempt_OnEmptyName_ExceptionThrown() throws Exception {
         ProductNonExempt emptyName = new ProductNonExempt(1, false, "", 1.00);
     }
 
     @Test(expected = java.lang.IllegalArgumentException.class)
     public void ProductNonExempt_OnNegativePrice_ExceptionThrown() throws Exception {
         ProductNonExempt negativePrice = new ProductNonExempt(1, false, "negative price", -1.0);
 	}
 
     @Test
     public void ProductNonExempt_WhenNameNotEmpty_ThenReturnsCorrectName() {
         ProductNonExempt nonEmptyName = new ProductNonExempt(1, false, "non-empty name", 1.0);
         assertEquals("non-empty name", nonEmptyName.name());
     }
 
     @Test
     public void ProductNonExempt_WhenQuantityGreaterThanZero_ThenReturnsCorrectQuantity() {
         ProductNonExempt quantityGreaterThanZero = new ProductNonExempt(12345, false, "qty > 0", 1.0);
         assertEquals(12345, quantityGreaterThanZero.quantity());
     }
 
     @Test
     public void ProductNonExempt_WhenUnitPriceGreaterThanZero_ReturnsCorrectPrice() {
         ProductNonExempt unitPriceGreaterThanZero = new ProductNonExempt(999, false, "price > 0", 1.0);
         assertEquals(1.1, unitPriceGreaterThanZero.price(), 0.01);
     }
 
     @Test
     public void ProductNonExempt_WhenImportedUnitPriceGreaterThanZero_ReturnsCorrectPrice() {
        ProductNonExempt importedQuantityGreaterThanZero = new ProductNonExempt(999, true, "price > 0", 1.0);
        assertEquals(1.15, importedQuantityGreaterThanZero.price(), 0.01);
     }
 
     @Test
     public void ProductNonExempt_WhenImported_ThenIsImportedReturnsTrue() {
         ProductNonExempt importedProduct = new ProductNonExempt(1, true, "imported", 1.0);
         assertEquals(true, importedProduct.isImported());
     }
 
     @Test
     public void ProductNonExempt_WhenImported_ThenTaxFifteenPercent() {
         ProductNonExempt importedProductTax = new ProductNonExempt(999, true, "imported", 1.0);
         assertEquals(1.0*0.15, importedProductTax.tax(), 0.01);
     }
 
     @Test
     public void ProductNonExempt_WhenNotImported_ThenTaxTenPercent() {
         ProductNonExempt notImportedProductTax = new ProductNonExempt(999, false, "not imported", 1.0);
         assertEquals(1.0*0.10, notImportedProductTax.tax(), 0.01);
     }
 
     @Test
     public void ProductNonExempt_WhenTaxNotAtFiveCentIncr_ThenRound() {
         ProductNonExempt taxRoundingProduct = new ProductNonExempt(999, false, "must round", 1.1);
         assertEquals(0.15, taxRoundingProduct.tax(), .01);
     }
 }
