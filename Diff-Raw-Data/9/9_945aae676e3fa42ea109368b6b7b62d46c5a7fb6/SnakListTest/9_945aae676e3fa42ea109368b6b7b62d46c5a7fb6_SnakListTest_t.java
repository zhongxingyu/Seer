 package test.wikibase.dataModel.snak;
 
 import org.junit.Test;
import org.junit.Assert;
 import wikibase.dataModel.entity.PropertyId;
 import wikibase.dataModel.snak.PropertyNoValueSnak;
 import wikibase.dataModel.snak.Snak;
 import wikibase.dataModel.snak.SnakList;
 
 public class SnakListTest {
     @Test
     public void assertCanConstruct() throws Exception {
         SnakList snaks = new SnakList();
     }
 
     @Test
     public void assertCanAddSnaks() throws Exception {
         SnakList snaks = new SnakList();
 
         Snak firstSnak = new PropertyNoValueSnak(new PropertyId("p42"));
         Snak secondSnak = new PropertyNoValueSnak(new PropertyId("p43"));
 
         snaks.add(firstSnak);
         snaks.add(secondSnak);
 
         Assert.assertEquals(snaks.size(), 2);
         Assert.assertEquals(snaks.get(0), firstSnak);
         Assert.assertEquals(snaks.get(1), secondSnak);
     }
 }
