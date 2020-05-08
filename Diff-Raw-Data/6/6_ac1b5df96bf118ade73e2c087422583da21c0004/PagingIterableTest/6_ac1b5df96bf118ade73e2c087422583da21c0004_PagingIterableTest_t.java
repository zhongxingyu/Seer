 package com.liorh.paging;
 
 import com.google.common.collect.Lists;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created by IntelliJ IDEA.
  * User: liorharel
  * Date: 4/24/12
  * Time: 10:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PagingIterableTest {
 
     private Mockery context;
     private PagesProvider pagesProvider;
 
     @Before
     public void setUp(){
        context = new Mockery();
        pagesProvider = context.mock(PagesProvider.class);
     }
 
     @After
     public void tearDown() {
        context.assertIsSatisfied();
     }
     @Test
     public void testNoPagesIterator(){
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator());
         }});
         Iterable<String> iterable = new PagingIterable<String>(pagesProvider);
        assertItarableEquals(new String[]{}, iterable);
     }
     @Test
     public void testOnePageIterator(){
 
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator("one", "two"));
             oneOf(pagesProvider).fetchPage(2); will(returnIterator());
 
         }});
 
         Iterable<String> iterable = new PagingIterable<String>(pagesProvider);
         assertItarableEquals(new String[] {"one", "two"}, iterable);
     }
     @Test
     public void testTwoPagesIterator(){
 
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator("one", "two"));
             oneOf(pagesProvider).fetchPage(2); will(returnIterator("three"));
             oneOf(pagesProvider).fetchPage(3); will(returnIterator());
 
         }});
 
         Iterable<String> iterable = new PagingIterable<String>(pagesProvider);
         assertItarableEquals(new String[] {"one", "two", "three"}, iterable);
     }
 
     @Test
     public void testFixedPageSizeIteratorOfOnePage(){
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator("one", "two"));
         }});
 
         Iterable<String> iterable = new FixedPagingIterable<String>(pagesProvider,3);
         assertItarableEquals(new String[] {"one", "two"}, iterable);
     }
 
     @Test
     public void testFixedPageSizeIteratorOfTwoPages(){
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator("one", "two"));
             oneOf(pagesProvider).fetchPage(2); will(returnIterator("three"));
         }});
 
         Iterable<String> iterable = new FixedPagingIterable<String>(pagesProvider,2);
         assertItarableEquals(new String[] {"one", "two", "three"}, iterable);
     }
 
     @Test
     public void testFixedPageSizeIteratorOfOneExactPage(){
         context.checking(new Expectations(){{
             oneOf(pagesProvider).fetchPage(1); will(returnIterator("one", "two"));
             oneOf(pagesProvider).fetchPage(2); will(returnIterator());
         }});
 
         Iterable<String> iterable = new FixedPagingIterable<String>(pagesProvider,2);
         assertItarableEquals(new String[] {"one", "two"}, iterable);
     }
 
     private void assertItarableEquals(String[] expected, Iterable<String> iterable) {
         List<String> results = Lists.newArrayList(iterable);
         assertEquals(Arrays.asList(expected), results);
     }
 
 }
