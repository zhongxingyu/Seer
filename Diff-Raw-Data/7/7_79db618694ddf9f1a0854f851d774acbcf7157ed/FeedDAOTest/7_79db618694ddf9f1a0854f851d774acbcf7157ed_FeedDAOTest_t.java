 package com.mycompany.dao;
 
 import com.mycompany.dao.impl.CompanyDAOImpl;
 import com.mycompany.dao.impl.FeedDAOImpl;
 import com.mycompany.entity.Company;
 import com.mycompany.entity.Feed;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.util.Collection;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 3/11/12
  * Time: 18:48
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(MockitoJUnitRunner.class)
 public class FeedDAOTest {
 
     private FeedDAO fdao = new FeedDAOImpl();
     private CompanyDAO cdao = new CompanyDAOImpl();
 
     @Before
     public void setUp() {
         /* Clean database of any previous content */
     }
 
    @Ignore("note ready yet")
     @Test
     public void addFeedToCompanyOk()
             throws DuplicatedCompanyException, DuplicatedFeedException, CompanyNotFoundException {
         /* Insert "ACME" company in database */
         Company c = new Company();
         c.setName("ACME");
         cdao.create(c);
 
         /* Create feed "news" */
         Feed f = new Feed();
         f.setId(1);
         f.setName("news");
         f.setCompany(c);
 
         /* Insert the feed in database */
         fdao.create(f);
 
         /* Now, read the company from the database */
         c = cdao.read("ACME");
 
         /* ... and check that the Feed is there */
         Collection<Feed> feeds = c.getFeeds();
         assertTrue(feeds.contains(f));
     }
 
    @Ignore("note ready yet")
     @Test
     public void duplicatedFeedSameCompany()
             throws DuplicatedCompanyException, DuplicatedFeedException, CompanyNotFoundException {
         /* Insert "ACME" company in database */
         Company c = new Company();
         c.setName("ACME");
         cdao.create(c);
 
         /* Create feed "news" */
         Feed f = new Feed();
         f.setId(1);
         f.setName("news");
         f.setCompany(c);
 
         /* Insert the feed in database */
         fdao.create(f);
 
         /* Try to associate again the same company fails*/
         boolean thrown = false;
         try {
             fdao.create(f);
         }
         catch (DuplicatedFeedException e) {
             thrown = true;
         }
         assertTrue(thrown);
     }
 
    @Ignore("note ready yet")
     @Test
     public void duplicatedFeedDifferentCompany()
             throws DuplicatedCompanyException, DuplicatedFeedException, CompanyNotFoundException {
         /* Insert two companies */
         Company c1 = new Company();
         Company c2 = new Company();
         c1.setName("ACME");
         c2.setName("Weyland Yutani");
         cdao.create(c1);
         cdao.create(c2);
 
         /* Create feed "news" */
         Feed f = new Feed();
         f.setId(1);
         f.setName("news");
         f.setCompany(c1);
 
         /* Insert the feed in database */
         fdao.create(f);
 
         /* Try to associate again the same company fails*/
         f.setCompany(c2);
         boolean thrown = false;
         try {
             fdao.create(f);
         }
         catch (DuplicatedFeedException e) {
             thrown = true;
         }
         assertTrue(thrown);
     }
 
 }
