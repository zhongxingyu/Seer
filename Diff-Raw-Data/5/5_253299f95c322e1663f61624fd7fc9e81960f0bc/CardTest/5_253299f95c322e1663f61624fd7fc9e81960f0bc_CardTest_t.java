 package normal;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 
 public class CardTest {
 
     @Test
     public void throwErrorIfCardNumberGreaterThan13OrLessThan0() {
         try {
            new Card(14, 3);
             fail("An Exception should have been thrown for number > 13");
         } catch (Card.CardGenerationError error) {
         }
         try {
            new Card(0, 1);
             fail("An Exception should have been thrown for number < 1");
         } catch (Card.CardGenerationError error) {
         }
     }
 
     @Test
     public void throwErrorIfCardSuitGreaterThan4OrLessThan0() {
         try {
             new Card(3, 5);
             fail("An Exception should have been thrown for suit > 4");
         } catch (Card.CardGenerationError error) {
         }
         try {
             new Card(6, 0);
             fail("An Exception should have been thrown for suit < 1");
         } catch (Card.CardGenerationError error) {
         }
     }
 
     @Test
     public void cardValueLessThanTen() {
         Card card = new Card(2, 2);
         int num = card.value();
         assertEquals(2, num);
     }
 
     @Test
     public void cardValueMoreThanTen() {
         Card card = new Card(12, 2);
         int num = card.value();
         assertEquals(10, num);
     }
 }
