 package com.randomhumans.svnindex;
 
 import java.io.IOException;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.search.Hits;
 
 import junit.framework.TestCase;
 
 public class CommitQueryTest extends TestCase
 {
 
     /*
      * Test method for 'com.randomhumans.svnindex.CommitQuery.performQuery(String)'
      */
     public void testPerformQuery() throws IOException
     {
         CommitQuery t = new CommitQuery();
         Hits h = t.performQuery("author:justinpitts");
         assertNotNull(h);
         for(int i = 0; i < h.length(); i ++)
         {
             Document d = h.doc(i);
             System.out.println(d);        
             
         }
         
     }
 
 }
