 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.sb;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.ClientBase;
 import de.escidoc.core.test.common.client.servlet.HttpHelper;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import java.util.HashMap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the implementation of the search resource.
  *
  * @author Michael Hoppe
  */
 @RunWith(value = Parameterized.class)
 public class OaipmhSearchTest extends SearchTestBase {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(OaipmhSearchTest.class);
 
     private static String[] itemIds = null;
 
     private static String[] containerIds = null;
 
     private static int methodCounter = 0;
 
     private static final String INDEX_NAME = "escidocoaipmh_all";
 
     private static String startTime = "";
 
     /**
      * @param transport The transport identifier.
      */
     public OaipmhSearchTest(final int transport) {
         super(transport);
         item = new ItemHelper(transport);
         container = new ContainerHelper(transport);
     }
 
     /**
      * Set up servlet test.
      *
      * @throws Exception If anything fails.
      */
     @Before
     public void initialize() throws Exception {
         if (methodCounter == 0) {
             prepare();
         }
     }
 
     /**
      * Clean up after servlet test.
      *
      * @throws Exception If anything fails.
      */
     @After
     public void deinitialize() throws Exception {
         methodCounter++;
         if (methodCounter == getTestAnnotationsCount()) {
             methodCounter = 0;
             deprepare();
         }
     }
 
     /**
      * insert item(s) into system for the tests.
      *
      * @throws Exception If anything fails.
      */
     private void prepare() throws Exception {
         LOGGER.info("starting OAIPMHSearchTest at "
             + new DateTime(System.currentTimeMillis() + (60 * 60 * 1000), DateTimeZone.UTC).toString());
         // create empty indices/////////////////////////////////////////////////
         String urlParameters =
             "?operation=updateIndex" + "&action=createEmpty" + "&repositoryName=escidocrepository" + "&INDEX_NAME=";
         String httpUrl =
             HttpHelper.createUrl(de.escidoc.core.test.common.client.servlet.Constants.PROTOCOL,
                 de.escidoc.core.test.common.client.servlet.Constants.HOST_PORT,
                 de.escidoc.core.test.common.client.servlet.Constants.FEDORAGSEARCH_BASE_URI + urlParameters);
         HttpHelper.executeHttpRequest(de.escidoc.core.test.common.client.servlet.Constants.HTTP_METHOD_GET, httpUrl,
             null, null, null);
         // /////////////////////////////////////////////////////////////////////
 
         startTime = new DateTime(System.currentTimeMillis(), DateTimeZone.UTC).toString();
         // Create Container/////////////////////////////////////////////////////
         try {
             containerIds = new String[Constants.NUM_OAIPMH_CONTAINERS];
             for (int i = 0; i < Constants.NUM_OAIPMH_CONTAINERS; i++) {
                 String xmlData =
                     EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_CONTAINER_SEARCH_PATH,
                         "escidoc_search_container" + i + "_" + getTransport(false) + ".xml");
                 String xml = container.create(xmlData);
                 String lastModDate = getLastModificationDate(xml);
                 containerIds[i] = getId(xml);
 
                 // submit container
                 container.submit(containerIds[i], "<param last-modification-date=\"" + lastModDate + "\" />");
 
                 // assign pids
                 String pidParam = getContainerPidParam(containerIds[i]);
                 container.assignObjectPid(containerIds[i], pidParam);
                 pidParam = getContainerPidParam(containerIds[i]);
                 container.assignVersionPid(containerIds[i] + ":1", pidParam);
 
                 // release container
                 xml = container.retrieve(containerIds[i]);
                 lastModDate = getLastModificationDate(xml);
                 container.release(containerIds[i], "<param last-modification-date=\"" + lastModDate + "\" />");
 
             }
             Thread.sleep(30000);
         }
         catch (final Exception e) {
             LOGGER.error("", e);
         }
         // /////////////////////////////////////////////////////////////////////
 
         try {
             itemIds = new String[Constants.NUM_OAIPMH_ITEMS];
             for (int i = 0; i < Constants.NUM_OAIPMH_ITEMS; i++) {
                 // Create Item submit and release it //////////////////////////
                 String xmlData =
                     EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_SEARCH_PATH, "escidoc_search_item" + i
                         + "_" + getTransport(false) + ".xml");
                 String xml = container.createItem(containerIds[0], xmlData);
                 String lastModDate = getLastModificationDate(xml);
                 itemIds[i] = getId(xml);
 
                 // submit item
                 item.submit(itemIds[i], "<param last-modification-date=\"" + lastModDate + "\" />");
 
                 // assignPids
                 Document itemDoc = EscidocRestSoapTestBase.getDocument(xml);
                 String componentId = getComponentObjidValue(itemDoc, 1);
                 String pidParam = getItemPidParam(itemIds[i]);
                 item.assignContentPid(itemIds[i], componentId, pidParam);
                 pidParam = getItemPidParam(itemIds[i]);
                 item.assignObjectPid(itemIds[i], pidParam);
                 // version pid to item[0] is assigned in a later test
                 // Sorry, but it depends on configuration if a release of an
                 // Item/container is possible without versionPid. Therefore has
                 // the 'later' test to operate on it own item.
                 // if (i > 0) {
                 String versionId = itemIds[i] + ":1";
                 pidParam = getItemPidParam(versionId);
                 item.assignVersionPid(versionId, pidParam);
                 // }
 
                 // release item
                 xml = item.retrieve(itemIds[i]);
                 lastModDate = getLastModificationDate(xml);
                 item.release(itemIds[i], "<param last-modification-date=\"" + lastModDate + "\" />");
 
                 Thread.sleep(10000);
 
                 if (i % 2 == 0) {
                     xml = item.retrieve(itemIds[i]);
                     lastModDate = getLastModificationDate(xml);
                     xml = xml.replaceAll("Gollmer", "Gollmer1");
                     item.update(itemIds[i], xml);
                 }
                 // ////////////////////////////////////////////////////////////
             }
         }
         catch (final Exception e) {
             LOGGER.error("", e);
         }
         try {
             // release container with items as new members
             // triggers indexing
             String xml = container.retrieve(containerIds[0]);
             String lastModDate = getLastModificationDate(xml);
             // submit container
             container.submit(containerIds[0], "<param last-modification-date=\"" + lastModDate + "\" />");
 
             String version =
                 selectSingleNode(EscidocRestSoapTestBase.getDocument(xml), "/container/properties/version/number")
                     .getTextContent();
             // assign pids
             String pidParam = getContainerPidParam(containerIds[0]);
             container.assignVersionPid(containerIds[0] + ":" + version, pidParam);
 
             // release container
             xml = container.retrieve(containerIds[0]);
             lastModDate = getLastModificationDate(xml);
             container.release(containerIds[0], "<param last-modification-date=\"" + lastModDate + "\" />");
 
             Thread.sleep(10000);
 
             xml = container.retrieve(containerIds[0]);
             lastModDate = getLastModificationDate(xml);
             xml = xml.replaceAll("Hoppe", "Hoppe1");
             container.update(containerIds[0], xml);
         }
         catch (final Exception e) {
             LOGGER.error("", e);
         }
         waitForIndexerToAppear(itemIds[Constants.NUM_OAIPMH_ITEMS - 1], INDEX_NAME);
         Thread.sleep(60000);
     }
 
     /**
      * explain operation without parameters for existing database xyz.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHEX1() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         String response = explain(parameters, INDEX_NAME);
         assertXmlValidExplainPlan(response);
         assertEquals("srw/search/" + INDEX_NAME, getDatabase(response));
         assertEquals(Constants.OAIPMH_INDEX_FIELD_COUNT, getIndexFieldCount(response));
         assertEquals(Constants.OAIPMH_SORT_FIELD_COUNT, getSortFieldCount(response));
     }
 
     /**
      * explain operation where operation=explain for existing database xyz is explicitly given.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHEX2() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_EXPLAIN);
         String response = explain(parameters, INDEX_NAME);
         assertXmlValidExplainPlan(response);
         assertEquals("srw/search/" + INDEX_NAME, getDatabase(response));
         assertEquals(Constants.OAIPMH_INDEX_FIELD_COUNT, getIndexFieldCount(response));
         assertEquals(Constants.OAIPMH_SORT_FIELD_COUNT, getSortFieldCount(response));
     }
 
     /**
      * explain operation with operation=explain for a not existing database zzz.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHEX3() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_OPERATION, FILTER_PARAMETER_EXPLAIN);
         try {
             explain(parameters, "zzz");
             fail("No exception occurred on explain in non-existing database.");
 
         }
         catch (final Exception e) {
             // FIXME: Assert exception
         }
     }
 
     /**
      * Test searching for a single term.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR1() throws Exception {
 
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
         assertEquals("1", getFirstRecord(response));
     }
 
     /**
      * Database not existing.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR2() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         try {
             search(parameters, "escidoc_fault");
             fail("No exception occured on search in non-existing database.");
         }
         catch (final Exception e) {
             // FIXME: assert exception
         }
     }
 
     /**
      * Request parameter startRecord \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR6() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_STARTRECORD, "2");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getFirstRecord(response));
     }
 
     /**
      * Request parameter startrecord \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR7() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_STARTRECORD, "0");
         String response = null;
         try {
             response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals(FILTER_PARAMETER_STARTRECORD, getDiagnostics(response));
         }
         catch (final Exception e) {
         }
     }
 
     /**
      * Request parameter maximumRecords \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR8() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "1");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNextRecordPosition(response));
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * Request parameter maximumRecords \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR9() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "0");
         String response = null;
         try {
             response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
            assertEquals(
                "1/java.lang.IllegalArgumentException: " + "numHits must be &gt; 0; please use TotalHitCountCollector "
                    + "if you just need the total hit count", getDiagnostics(response));
         }
         catch (final Exception e) {
         }
     }
 
     /**
      * Request parameter maximumRecords \u2013 (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR10() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_MAXIMUMRECORDS, "-1");
         try {
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals(FILTER_PARAMETER_MAXIMUMRECORDS, getDiagnostics(response));
         }
         catch (final Exception e) {
         }
     }
 
     /**
      * Request parameter recordPacking \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR11() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "xml");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Request parameter recordPacking \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR12() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "string");
         String response = search(parameters, INDEX_NAME);
         assertEquals(null, getDiagnostics(response));
     }
 
     /**
      * Request parameter recordPacking \u2013 (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR13() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_RECORDPACKING, "something");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("something", getDiagnostics(response));
     }
 
     /**
      * Request parameter recordSchema \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR14() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_RECORDSCHEMA, "default");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * Request parameter recordSchema \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR15() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_RECORDSCHEMA, "none");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("none", getDiagnostics(response));
     }
 
     /**
      * Request parameter sortKeys \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR16() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.last-modification-date");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         String[] dates = response.split("<last-modification-date>");
         String savedDatepart = "0";
         for (int i = 1; i < dates.length; i++) {
             String datepart = dates[i];
             datepart = datepart.substring(0, datepart.indexOf("<"));
             if (datepart.compareTo(savedDatepart) < 0) {
                 assertTrue("wrong sortorder", false);
             }
             savedDatepart = datepart;
         }
     }
 
     /**
      * Request parameter sortKeys \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR17() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.last-modification-date,,0");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         String[] dates = response.split("<last-modification-date>");
         String savedDatepart = "ZZZZZZZ";
         for (int i = 1; i < dates.length; i++) {
             String datepart = dates[i];
             datepart = datepart.substring(0, datepart.indexOf("<"));
             if (datepart.compareTo(savedDatepart) > 0) {
                 assertTrue("wrong sortorder", false);
             }
             savedDatepart = datepart;
         }
     }
 
     /**
      * Request parameter sortKeys \u2013 (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR18() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_SORTKEYS, "sort.escidoc.sonstwas,,0");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * : Request parameter stylesheet \u2013 (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR21() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_STYLESHEET, "http://escidev5:8080/srw/searchRetrieveResponse.xsl");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * Request parameter stylesheet \u2013 (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR22() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.md-record-identifier="
             + "\"escidoc@http://escidoc.mpg.de/metadataprofile/schema/0.1/\"");
         parameters.put(FILTER_PARAMETER_STYLESHEET, "http://escidev5:8080/srw/xyz.xsl");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * Boolean Operator AND.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR23() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title all " + "\"motor getriebe\"");
         String response = search(parameters, INDEX_NAME);
         assertEquals("1", getNumberOfHits(response));
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=" + "motor"
             + " and escidoc.publication.title=getriebe");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Boolean Operator OR .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR24() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title any " + "\"motor kalibrierung\"");
         String response = search(parameters, INDEX_NAME);
         assertEquals("2", getNumberOfHits(response));
         parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=motor "
             + "or escidoc.publication.title=kalibrierung");
         response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNumberOfHits(response));
     }
 
     /**
      * Use of * as Wildcard (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR25() throws Exception {
         // Left truncation not supported
     }
 
     /**
      * Use of * as Wildcard (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR26() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=mo*r");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Use of * as Wildcard (3).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR27() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=mot*");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Use of ? as Wildcard (1).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR28() throws Exception {
         // Left truncation not supported
     }
 
     /**
      * Use of ? as Wildcard (2).
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR29() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=mo?or");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Use of ? as Wildcard (3) .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR30() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=moto?");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * Fuzzy Search.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR31() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.publication.title=/fuzzy kalirierung");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * : Umlaut as request-parameter .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR32() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, new String(
             "escidoc.publication.creator.person.complete-name=patentanw\u00e4lte".getBytes(ClientBase.DEFAULT_CHARSET),
             ClientBase.DEFAULT_CHARSET));
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("1", getNumberOfHits(response));
     }
 
     /**
      * : UTF-8 characters as request-parameter .
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR34() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, new String(
             ("escidoc.publication.source.title=\u5161\u4e5f\u5305\u56e0\u6c98"
                 + "\u6c13\u4fb7\u67f5\u82d7\u5b6b\u5b6b\u8ca1").getBytes(ClientBase.DEFAULT_CHARSET),
             ClientBase.DEFAULT_CHARSET));
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("2", getNumberOfHits(response));
     }
 
     /**
      * : Availability of userdefined indexes for items.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR35() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_ITEM_INDEX_USERDEFINED_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_ITEM_INDEX_USERDEFINED_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=item");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of userdefined indexes for container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR36() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_CONTAINER_INDEX_USERDEFINED_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_CONTAINER_INDEX_USERDEFINED_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=container");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of properties indexes for item.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR37() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_ITEM_INDEX_PROPERTIES_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_ITEM_INDEX_PROPERTIES_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=item");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of properties indexes for container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR38() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_CONTAINER_INDEX_PROPERTIES_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_CONTAINER_INDEX_PROPERTIES_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=container");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of metadata indexes for item.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR39() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_ITEM_INDEX_METADATA_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_ITEM_INDEX_METADATA_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=item");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Availability of metadata indexes for container.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR40() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         for (String indexLabel : Constants.OAIPMH_CONTAINER_INDEX_METADATA_SEARCHES.keySet()) {
             HashMap<String, String> info = Constants.OAIPMH_CONTAINER_INDEX_METADATA_SEARCHES.get(indexLabel);
             String searchString = info.get("searchString");
             String expectedHits = info.get("expectedHits");
             parameters.put(FILTER_PARAMETER_QUERY, searchString + " and escidoc.objecttype=container");
             String response = search(parameters, INDEX_NAME);
             assertXmlValidSearchResult(response);
             assertEquals("expected " + expectedHits + " for " + indexLabel + " but was " + getNumberOfHits(response),
                 expectedHits, getNumberOfHits(response));
         }
     }
 
     /**
      * : Test searching for dates.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR41() throws Exception {
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "escidoc.latest-release.date>\"" + startTime + "\"");
         String response = search(parameters, INDEX_NAME);
         assertXmlValidSearchResult(response);
         assertEquals("3", getNumberOfHits(response));
     }
 
     /**
      * : Check deleted-element in search result for all ids.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testSBOAIPMHSR42() throws Exception {
         for (int i = 0; i < itemIds.length; i++) {
             if (itemIds[i] != null && !itemIds[i].equals("")) {
                 HashMap<String, String> parameters = new HashMap<String, String>();
                 parameters.put(FILTER_PARAMETER_QUERY, "escidoc.objid=" + itemIds[i]);
                 String response = search(parameters, INDEX_NAME);
                 assertXmlValidSearchResult(response);
                 assertEquals("1", getNumberOfHits(response));
                 assertElementEquals("element has not the expected value", getDocument(response),
                     "//*[local-name()='deleted']", "false");
             }
         }
         for (int i = 0; i < containerIds.length; i++) {
             if (containerIds[i] != null && !containerIds[i].equals("")) {
                 HashMap<String, String> parameters = new HashMap<String, String>();
                 parameters.put(FILTER_PARAMETER_QUERY, "escidoc.objid=" + containerIds[i]);
                 String response = search(parameters, INDEX_NAME);
                 assertXmlValidSearchResult(response);
                 assertEquals("1", getNumberOfHits(response));
                 assertElementEquals("element has not the expected value", getDocument(response),
                     "//*[local-name()='deleted']", "false");
             }
         }
     }
 
     /**
      * withdraw items to update index.
      *
      * @throws Exception If anything fails.
      */
     private void deprepare() throws Exception {
         // Withdraw
         // Items//////////////////////////////////////////////////////////
         if (itemIds != null) {
             for (int i = 0; i < itemIds.length; i++) {
                 if (itemIds[i] != null && !itemIds[i].equals("")) {
                     String xml = item.retrieve(itemIds[i]);
                     String lastModDate = getLastModificationDate(xml);
                     item.withdraw(itemIds[i], "<param last-modification-date=\"" + lastModDate + "\">"
                         + "<withdraw-comment>" + "This is a withdraw comment." + "</withdraw-comment>" + "</param>");
                     // ////////////////////////////////////////////////////////
 
                 }
             }
             waitForIndexer();
             for (int i = 0; i < itemIds.length; i++) {
                 if (itemIds[i] != null && !itemIds[i].equals("")) {
                     // Do search. Must be 1
                     // results///////////////////////////////////////////
                     HashMap<String, String> parameters = new HashMap<String, String>();
                     parameters.put(FILTER_PARAMETER_QUERY, "escidoc.objid=" + itemIds[i]);
                     String response = search(parameters, INDEX_NAME);
                     assertEquals("1", getNumberOfHits(response));
                     assertElementEquals("element has not the expected value", getDocument(response),
                         "//*[local-name()='deleted']", "true");
                     // ////////////////////////////////////////////////////////
                 }
             }
         }
         // Withdraw
         // Container//////////////////////////////////////////////////////////
         if (containerIds != null) {
             for (int i = 0; i < containerIds.length; i++) {
                 if (containerIds[i] != null && !containerIds[i].equals("")) {
                     String xml = container.retrieve(containerIds[i]);
                     String lastModDate = getLastModificationDate(xml);
                     container.withdraw(containerIds[i], "<param last-modification-date=\"" + lastModDate + "\">"
                         + "<withdraw-comment>" + "This is a withdraw comment." + "</withdraw-comment>" + "</param>");
                     // ////////////////////////////////////////////////////////
 
                 }
             }
             waitForIndexer();
             for (int i = 0; i < containerIds.length; i++) {
                 if (containerIds[i] != null && !containerIds[i].equals("")) {
                     // Do search. Must be 1
                     // results///////////////////////////////////////////
                     HashMap<String, String> parameters = new HashMap<String, String>();
                     parameters.put(FILTER_PARAMETER_QUERY, "escidoc.objid=" + containerIds[i]);
                     String response = search(parameters, INDEX_NAME);
                     assertEquals("1", getNumberOfHits(response));
                     assertElementEquals("element has not the expected value", getDocument(response),
                         "//*[local-name()='deleted']", "true");
                     // ////////////////////////////////////////////////////////
                 }
             }
         }
 
     }
 
 }
