 /*
  * URLManager - URL Indexer
  * Copyright (C) 2008-2012  Open-S Company
  *
  * This file is part of URLManager.
  *
  * URLManager is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact us by mail: open-s AT open-s DOT com
  */
 package org.opens.urlmanager.entity.dao.request;
 
 import java.util.*;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.opens.urlmanager.entity.locale.Locale;
 import org.opens.urlmanager.entity.locale.LocaleImpl;
 import org.opens.urlmanager.entity.request.Request;
 import org.opens.urlmanager.entity.request.RequestImpl;
 import org.opens.urlmanager.entity.service.webpage.WebpageDataServiceImpl;
 import org.opens.urlmanager.entity.tag.Tag;
 import org.opens.urlmanager.entity.tag.TagImpl;
 import org.opens.urlmanager.entity.webpage.Webpage;
 import org.opens.urlmanager.entity.webpage.WebpageImpl;
 import org.opens.urlmanager.persistence.AbstractDaoTestCase;
 import org.opens.urlmanager.persistence.ListContentComparator;
 
 /**
  *
  * @author bcareil
  */
 public class RequestDAOImplTest extends AbstractDaoTestCase {
 
     private RequestDAO requestDAO;
 
     public RequestDAOImplTest(String testName) {
         super(testName, "src/test/resources/dataSets/flatDataSet.xml");
         requestDAO = (RequestDAO) springBeanFactory.getBean("requestDAO");
     }
 
     public static Test suite() {
         TestSuite suite = new TestSuite(RequestDAOImplTest.class);
         return suite;
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of getEntityClass method, of class RequestDAOImpl.
      */
     public void testGetEntityClass() {
         System.out.println("getEntityClass");
         RequestDAOImpl instance = new RequestDAOImpl();
         Class expResult = RequestImpl.class;
         Class result = instance.getEntityClass();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of findAll method, of class RequestDAOImpl
      */
     public void testFindAll() {
         System.out.println("testFindAll");
         Collection<Request> result;
         Request expRequest = new RequestImpl(4L, "lol french pages");
         Set<Tag> expTag = new HashSet<Tag>(Arrays.asList(
                 (Tag) new TagImpl(1L, "lol")
                 ));
         Set<Locale> expLocale = new HashSet<Locale>(Arrays.asList(
                 (Locale) new LocaleImpl(1L, "fr", "french", "FR", "France"),
                 (Locale) new LocaleImpl(2L, "fr", "french", "CA", "Canada")
                 ));
         
         // run method
         result = requestDAO.findAll();
         // expect 5 results
         assertEquals(5, result.size());
         // look for the expected request
         Iterator<Request> it = result.iterator();
         
         while (it.hasNext()) {
             Request request = it.next();
             
             if (expRequest.equals(request)) {
                 it = null;
                 assertEquals(expLocale, request.getLocales());
                 assertEquals(expTag, request.getTags());
                 break;
             }
         }
         // ensure we found the expected request
         assertNull(
                 "The expected request of label " +
                 expRequest.getLabel() + " was not found",
                 it);
     }
 
     /**
      * Test of read method, of class RequestDAOImpl
      */
     public void testRead() {
         System.out.println("testRead");
         Request result;
         Request expRequest = new RequestImpl(4L, "lol french pages");
         Set<Tag> expTag = new HashSet<Tag>(Arrays.asList(
                 (Tag) new TagImpl(1L, "lol")
                 ));
         Set<Locale> expLocale = new HashSet<Locale>(Arrays.asList(
                 (Locale) new LocaleImpl(1L, "fr", "french", "FR", "France"),
                 (Locale) new LocaleImpl(2L, "fr", "french", "CA", "Canada")
                 ));
 
         /* nominal UC */
         // run method
         result = requestDAO.read(expRequest.getId());
         // assert we found the right request
         assertEquals(expRequest, result);
         assertEquals(expLocale, result.getLocales());
         assertEquals(expTag, result.getTags());
         
         /* error UC */
         // run method
         result = requestDAO.read(0L);
         // assert
         assertNull(result);
     }
 
     /**
      * Test of findRequestFromLabel method, of class RequestDAOImpl.
      */
     public void testFindRequestFromLabel() {
         System.out.println("findRequestFromLabel");
         Request result;
         Request expRequest = new RequestImpl(3L, "lol related");
         Set<Tag> expTag = new HashSet<Tag>(Arrays.asList(
                 (Tag) new TagImpl(1L, "lol")
                 ));
 
         System.out.println("-> existing request");
         result = requestDAO.findRequestFromLabel(expRequest.getLabel());
         // expected result : The request with the label "lol related" and the Id_Request=3
         assertNotNull(result);
         assertEquals(expRequest, result);
         assertEquals(expTag, result.getTags());
         
         System.out.println("-> invalid request");
         result = requestDAO.findRequestFromLabel("blah");
         assertNull(result);
     }
 
     /**
      * Test of findWebpageListFromRequest method, of class RequestDAOImpl.
      */
     public void testFindMatchingWebpages() {
         System.out.println("findMatchingWebpages");
         Request request;
        Collection<Locale> locales;
        Collection<Tag> tags;
         List<Webpage> result;
         List<Webpage> expResult;
 
         /*
          * Request webpages matching an exising request
          */
         System.out.println("-> from existing request");
         // fetch request
         request = requestDAO.findRequestFromLabel("lol french pages");
         // check request ID
         assertNotNull(request);
         assertEquals(Long.valueOf(4), request.getId());
         // set expected result
         expResult = Arrays.asList(
                 (Webpage) new WebpageImpl(1L, "http://lol.com/", true)
                 );
         // do request
         result = new ArrayList(requestDAO.findMatchingWebpages(request));
         // compare results
         assertEquals(0, new ListContentComparator<Webpage>().compare(
                 expResult, result,
                 new WebpageDataServiceImpl.Comparator()));
         
         System.out.println("-> from created on purpose request");
         /*
          * Request webpages matching a list of tags and locales
          */
         System.out.println("-> with locales and tags");
         // set locales and tag for a request equivalent to "lol french pages"
         locales = Arrays.asList(
                 (Locale) new LocaleImpl(1L, "fr", "french", "FR", "France"),
                 (Locale) new LocaleImpl(1L, "fr", "french", "CA", "Canada")
                 );
         tags = Arrays.asList(
                 (Tag) new TagImpl(1L, "lol")
                 );
         request.setLocales(locales);
         request.setTags(tags);
         // set expected result
         expResult = Arrays.asList(
                 (Webpage) new WebpageImpl(1L, "http://lol.com/", true)
                 );
         // fetch results
         result = new ArrayList(requestDAO.findMatchingWebpages(request));
         // compare result
         assertEquals(0, new ListContentComparator<Webpage>().compare(
                 expResult, result,
                 new WebpageDataServiceImpl.Comparator()));
 
         /*
          * Request weboages matching a list of tags
          */
         System.out.println("-> with tags only");
         // set locales and tags for a request equivalent to "lol and toto related"
         locales = new ArrayList<Locale>();
         tags = Arrays.asList(
                 (Tag) new TagImpl(1L, "lol"),
                 (Tag) new TagImpl(3L, "toto")
                 );
         request.setLocales(locales);
         request.setTags(tags);
         // set expected result
         expResult = Arrays.asList(
                 (Webpage) new WebpageImpl(3L, "http://lol.com/page.html", false)
                 );
         // fetch results
         result = new ArrayList(requestDAO.findMatchingWebpages(request));
         // compare result
         assertEquals(0, new ListContentComparator<Webpage>().compare(
                 expResult, result,
                 new WebpageDataServiceImpl.Comparator()));
         
         /*
          * Request webpages matching a list of locales
          */
         System.out.println("-> with locales only");
         // set locales and tag for a request equivalent to "french pages"
         locales = Arrays.asList((Locale) new LocaleImpl(1L, "fr", "french", "FR", "France"));
         tags = new ArrayList<Tag>();
         request.setLocales(locales);
         request.setTags(tags);
         // set expected result
         expResult = Arrays.asList(
                 (Webpage) new WebpageImpl(1L, "http://lol.com/", true),
                 (Webpage) new WebpageImpl(4L, "http://toto.com/", true)
                 );
         // fetch results
         result = new ArrayList(requestDAO.findMatchingWebpages(request));
         // compare result
         assertEquals(0, new ListContentComparator<Webpage>().compare(
                 expResult, result,
                 new WebpageDataServiceImpl.Comparator()));
         
         /*
          * Request all webpages
          */
         System.out.println("-> without anything : joker research");
         // set locales and tags for a request equivqlent to "joker"
         locales = new ArrayList<Locale>();
         tags = new ArrayList<Tag>();
         request.setLocales(locales);
         request.setTags(tags);
         // set expected result (all webpage entries)
         expResult = Arrays.asList(
                 (Webpage) new WebpageImpl(1L, "http://lol.com/", true),
                 (Webpage) new WebpageImpl(2L, "http://lol.com/app/", false),
                 (Webpage) new WebpageImpl(3L, "http://lol.com/page.html", false),
                 (Webpage) new WebpageImpl(4L, "http://toto.com/", true),
                 (Webpage) new WebpageImpl(5L, "http://foreveralone.com/", true)
                 );
         // fetch results
         result = new ArrayList(requestDAO.findMatchingWebpages(request));
         // compare result
         assertEquals(0, new ListContentComparator<Webpage>().compare(
                 expResult, result,
                 new WebpageDataServiceImpl.Comparator()));                
     }
 
     /**
      * Test of buildRequestQuery method, of class RequestDAOImpl.
      */
     public void testBuildRequestQuery() {
         System.out.println("buildRequestQuery");
         RequestDAOImpl instance = new RequestDAOImpl();
         String result;
         String expResult;
 
         System.out.println("-> with tags and locales");
         expResult = "SELECT DISTINCT w FROM " +
                 WebpageImpl.class.getName() + " w" +
                 " INNER JOIN w.locales l" +
                 " INNER JOIN w.tags t WHERE" +
                 " l IN (:locales) AND t IN (:tags)";
         result = instance.buildRequestQuery(true, true);
         assertEquals(expResult, result);
 
         System.out.println("-> with tags");
         expResult = "SELECT DISTINCT w FROM " +
                 WebpageImpl.class.getName() + " w" +
                 " LEFT JOIN FETCH w.locales" +
                 " INNER JOIN w.tags t WHERE" +
                 " t IN (:tags)";
         result = instance.buildRequestQuery(false, true);
         assertEquals(expResult, result);
 
         System.out.println("-> with locales");
         expResult = "SELECT DISTINCT w FROM " +
                 WebpageImpl.class.getName() + " w" +
                 " INNER JOIN w.locales l" +
                 " LEFT JOIN FETCH w.tags WHERE" +
                 " l IN (:locales)";
         result = instance.buildRequestQuery(true, false);
         assertEquals(expResult, result);
 
         System.out.println("-> without anything");
         expResult = "SELECT DISTINCT w FROM " +
                 WebpageImpl.class.getName() + " w" +
                 " LEFT JOIN FETCH w.locales" +
                 " LEFT JOIN FETCH w.tags";
         result = instance.buildRequestQuery(false, false);
         assertEquals(expResult, result);
     }
 }
