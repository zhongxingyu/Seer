 package edu.cornell.ics;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.cornell.ics.entities.Author;
 import edu.cornell.ics.entities.Entry;
 import edu.cornell.ics.entities.Query;
 import edu.cornell.ics.interfaces.IEntry;
 import edu.cornell.ics.interfaces.IQuery;
 import edu.cornell.ics.utilities.providers.EntryProvider;
 import edu.cornell.ics.utilities.providers.HibernateDataProvider;
import java.util.Set;
 import org.junit.Ignore;
 
 public class AuthorTest {
 
     private static final Logger log = Logger.getLogger(AuthorTest.class);
     private static EntryProvider entryProvider;
     private static HibernateDataProvider<Author> authorProvider;
 
     // Uncomment this if you want ton run these tests.
//    @BeforeClass
     public static void setup() {
         authorProvider = new HibernateDataProvider<Author>();
         entryProvider = new EntryProvider();
     }
 
     // Uncomment this if you want ton run these tests.
//    @AfterClass
     public static void cleaup() {
         log.debug("starting teardown");
         Entry e = (Entry) entryProvider.LoadByTitle("Some Book");
         entryProvider.Delete(e);
         Entry e2 = (Entry) entryProvider.LoadByTitle("Some Book 2");
         entryProvider.Delete(e2);
         IQuery qry = new Query("from Author where authorname=:authorname");
         qry.setParameter("authorname", "Jason");
         Author a = authorProvider.Load(qry);
         authorProvider.Delete(a);
         qry.setParameter("authorname", "Karan");
         a = authorProvider.Load(qry);
         authorProvider.Delete(a);
         log.debug("ending teardown");
     }
 
     @Test
    @Ignore
     public void createAuthor() {
         Author author1 = new Author();
         author1.setAuthorName("Jason");
         authorProvider.Save(author1);
         Author author2 = new Author();
         author2.setAuthorName("Karan");
         authorProvider.Save(author2);
         Entry e = new Entry();
         e.setTitle("Some Book");
         Entry e2 = new Entry();
         e2.setTitle("Some Book 2");
         e.addAuthor(author1);
         e.addAuthor(author1);
         e.addAuthor(author2);
         e2.addAuthor(author1);
         entryProvider.Save(e);
         entryProvider.Save(e2);
     }
 
     @Test
    @Ignore
     public void loadbyAuthor() {
         IQuery qry = new Query("from Author where authorname=:authorname");
         qry.setParameter("authorname", "Jason");
         Author author = authorProvider.Load(qry);
         Assert.assertNotNull("author should not be null", author);
         Assert.assertEquals("the names should be what we expect", "Jason", author.getAuthorName());
         List<IEntry> E = entryProvider.ListByAuthor(author);
         Assert.assertNotNull("E should not be null", E);
         Assert.assertTrue("E should have stuff in it", (E.size() > 0));
         for (IEntry entry : E) {
             log.debug(entry.getTitle());
         }
     }

     @Test
    @Ignore
     public void addCoAuthors(){
         IQuery qry = new Query("from Author where authorname=:authorname");
         qry.setParameter("authorname", "Jason");
         Author author1 = authorProvider.Load(qry);
         qry.setParameter("authorname", "Karan");
         Author author2 = authorProvider.Load(qry);
         author1.addCoAuthor(author2);
         authorProvider.Update(author1);
         authorProvider.Update(author2);
         Assert.assertTrue(author1.getCoAuthors().contains(author2));
        Assert.assertTrue(author2.getCoAuthors().contains(author1));        
     }
 }
