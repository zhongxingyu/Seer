 package org.atlasapi.remotesite.freebase;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Equiv;
 import org.atlasapi.media.entity.Publisher;
 
 public class FreebaseBrandEquivalenceGeneratorTest extends TestCase {
     private FreebaseBrandEquivGenerator generator = new FreebaseBrandEquivGenerator();
     
     public void testShouldRetrieveWikipediaEquivForGlee() {
         Brand brand = new Brand("http://www.hulu.com/glee", "hulu:glee", Publisher.HULU);
         brand.setTitle("Glee");
         
         List<Equiv> equivs = generator.equivalent(brand);
         assertNotNull(equivs);
         assertFalse(equivs.isEmpty());
         
         for (Equiv equiv: equivs) {
             assertEquals(brand.getCanonicalUri(), equiv.left());
             assertNotSame(equiv.left(), equiv.right());
             assertTrue(equiv.right().startsWith(FreebaseBrandEquivGenerator.WIKIPEDIA) || equiv.right().startsWith(FreebaseBrandEquivGenerator.HULU) || equiv.right().startsWith(FreebaseBrandEquivGenerator.IMDB));
         }
         System.out.println(equivs);
     }
     
     public void testShouldRetrieveWikipediaEquivForCDWM() {
         Brand brand = new Brand("http://www.channel4.com/programmes/come-dine-with-me", "c4:glee", Publisher.C4);
         brand.setTitle("Come Dine With Me");
         
         List<Equiv> equivs = generator.equivalent(brand);
         assertNotNull(equivs);
         assertFalse(equivs.isEmpty());
         
         for (Equiv equiv: equivs) {
             assertEquals(brand.getCanonicalUri(), equiv.left());
            assertTrue(equiv.right().startsWith(FreebaseBrandEquivGenerator.WIKIPEDIA));
         }
         System.out.println(equivs);
     }
     
     public void testShouldRetrieveWikipediaEquivForEastenders() {
         Brand brand = new Brand("http://www.bbc.co.uk/programmes/b006m86d", "b006m86d", Publisher.BBC);
         brand.setTitle("Eastenders");
         
         List<Equiv> equivs = generator.equivalent(brand);
         assertNotNull(equivs);
         assertFalse(equivs.isEmpty());
         
         for (Equiv equiv: equivs) {
             assertEquals(brand.getCanonicalUri(), equiv.left());
         }
         System.out.println(equivs);
     }
 }
