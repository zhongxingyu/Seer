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
 package de.escidoc.core.test.om.item;
 
 import de.escidoc.core.common.exceptions.remote.application.notfound.FileNotFoundException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.st.StagingFileClient;
 import de.escidoc.core.test.common.resources.PropertiesProvider;
 import de.escidoc.core.test.st.StagingFileTestBase;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 /**
  * Test the mock implementation of the item resource.
  * 
  * @author MSC
  * 
  */
 @RunWith(value = Parameterized.class)
 public class ItemContentURLTest extends ItemTestBase {
 
     private String theItemId = null;
 
     private String theItemXml = null;
 
     private Document theItemDoc = null;
 
     private final String testUploadFile = "UploadTest.zip";
 
     private final String testUploadFileMimeType = "application/zip";
 
     private StagingFileClient sfc;
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public ItemContentURLTest(final int transport) {
         super(transport);
     }
 
     /**
      * Clean up after servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Override
     @After
     public void tearDown() throws Exception {
 
         super.tearDown();
         try {
             delete(this.theItemId);
         }
         catch (Exception e) {
             // do nothing
         }
     }
 
     /**
      * Successfully create an item with a component with content from staging
      * referred by local staging URL. (/st/...)
      * 
      * @throws Exception
      */
     @Test
     public void testCreateStagingURL_1() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         // content to staging
         String url = createStagingFile(false);
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
 
         this.theItemId = getObjidValue(this.theItemXml);
     }
 
     /**
      * Successfully create an item with a component with content from staging
      * referred by full qualified staging URL. (http://<host>:<port>/st/...)
      * 
      * @throws Exception
      */
     @Test
     public void testCreateStagingURL_2() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         // content to staging
         String url = createStagingFile(true);
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
 
         this.theItemId = getObjidValue(this.theItemXml);
     }
 
     /**
      * Successfully update content from staging referred by local staging URL.
      * (/st/...)
      * 
      * @throws Exception
      */
     @Test
     public void testUpdateStagingURL_1() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         this.theItemId = getObjidValue(this.theItemDoc);
 
         // content to staging
         String url = createStagingFile(false);
 
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemXml = update(this.theItemId, this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
         selectSingleNodeAsserted(this.theItemDoc,
             "/item[properties/version/number = '2']");
     }
 
     /**
      * Successfully update content from staging referred by full qualified
      * staging URL. (http://<host>:<port>/st/...)
      * 
      * @throws Exception
      */
     @Test
     public void testUpdateStagingURL_2() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         this.theItemId = getObjidValue(this.theItemDoc);
 
         // content to staging
         String url = createStagingFile(true);
 
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemXml = update(this.theItemId, this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
         selectSingleNodeAsserted(this.theItemDoc,
             "/item[properties/version/number = '2']");
     }
 
     /**
      * Successfully create a component with content from staging referred by
      * local staging URL (/st/...) in an existing item.
      * 
      * @throws Exception
      */
     @Test
     public void testCreateStagingURL_3() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         this.theItemId = getObjidValue(this.theItemDoc);
 
         // content to staging
         String url = createStagingFile(false);
 
         if (getTransport() == Constants.TRANSPORT_REST) {
             this.theItemDoc =
                 (Document) substitute(this.theItemDoc,
                     "/item/components/component/@href", "");
         }
         else {
             this.theItemDoc =
                 (Document) substitute(this.theItemDoc,
                     "/item/components/component/@objid", "");
         }
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component/content/@href", url);
 
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemXml = update(this.theItemId, this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
         selectSingleNodeAsserted(this.theItemDoc,
             "/item[properties/version/number = '2']");
     }
 
     /**
      * Successfully create a component with content from staging referred by
      * full qualified staging URL (http://<host>:<port>/st/...) in an existing
      * item.
      * 
      * @throws Exception
      */
     @Test
     public void testCreateStagingURL_4() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         this.theItemId = getObjidValue(this.theItemDoc);
 
         // content to staging
         String url = createStagingFile(true);
 
         if (getTransport() == Constants.TRANSPORT_REST) {
             this.theItemDoc =
                 (Document) substitute(this.theItemDoc,
                     "/item/components/component/@href", "");
         }
         else {
             this.theItemDoc =
                 (Document) substitute(this.theItemDoc,
                     "/item/components/component/@objid", "");
         }
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component/content/@href", url);
 
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemXml = update(this.theItemId, this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         assertXmlValidItem(this.theItemXml);
         selectSingleNodeAsserted(this.theItemDoc,
             "/item[properties/version/number = '2']");
     }
 
     /**
      * 
      * @param withXmlBase
      * @return
      * @throws Exception
      */
     private String createStagingFile(boolean withXmlBase) throws Exception {
 
         // download file from test data service to local tempfile
         File f =
             downloadTempFile(new URL(
                 properties.getProperty(PropertiesProvider.TESTDATA_URL) + "/"
                     + testUploadFile));
 
         InputStream fileInputStream = new FileInputStream(f);
 
         HttpResponse httpRes = null;
         try {
             if (sfc == null) {
                 sfc = new StagingFileClient(Constants.TRANSPORT_REST);
             }
             httpRes =
                 (HttpResponse) sfc.create(fileInputStream,
                     testUploadFileMimeType, testUploadFile);
         }
         catch (Exception e) {
             EscidocRestSoapTestBase.failException(e);
         }
         assertNotNull("No HTTPMethod. ", httpRes);
         assertHttpStatusOfMethod("Create failed", httpRes);
         final String stagingFileXml =
             EntityUtils.toString(httpRes.getEntity(), HTTP.UTF_8);
 
         String url = "";
         if (withXmlBase) {
             url =
                 selectSingleNode(getDocument(stagingFileXml), "//@base")
                     .getNodeValue();
         }
         url +=
             selectSingleNode(getDocument(stagingFileXml), "//@href")
                 .getNodeValue();
         return url;
     }
 
     /**
      * Download file an save as temp.
      * 
      * @param url
      *            URL to file
      * @return File handler for temporary file.
      */
    private File downloadTempFile(final URL url) {
 
         java.io.BufferedInputStream in =
             new java.io.BufferedInputStream(url.openStream());
 
         File temp = File.createTempFile("escidoc-core-testfile", ".tmp");
         temp.deleteOnExit();
 
        FileOutputStream fos = new java.io.FileOutputStream(temp);
         BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
 
         byte[] data = new byte[1024];
         int x = 0;
         while ((x = in.read(data, 0, 1024)) >= 0) {
             bout.write(data, 0, x);
         }
 
         bout.close();
         in.close();
 
         return temp;
     }
 
     /**
      * Decline create an item with a component with content referred by Fedora
      * URL.
      * 
      * @throws Exception
      */
     @Test
     public void testCreateFedoraURL_1() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         // content to staging
         String fedoraUrl =
             this.properties.getProperty(PropertiesProvider.FEDORA_URL,
                 "http://localhost:8082/fedora");
         String url = fedoraUrl + "/get/escidoc:ex6/content";
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, false);
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, true);
         try {
             this.theItemXml = create(this.theItemXml);
             this.theItemId = getObjidValue(this.theItemXml);
             fail("No exception: Create with Fedora URL.");
         }
         catch (Exception e) {
             // Class ec = InvalidContentException.class;
             Class ec = FileNotFoundException.class;
             assertExceptionType(ec, e);
         }
     }
 
     /**
      * Decline update an item with a component with content referred by Fedora
      * URL.
      * 
      * @throws Exception
      */
     @Test
     public void testUpdateFedoraURL_1() throws Exception {
         this.theItemDoc =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         deleteElement(this.theItemDoc,
             "/item/components/component[not(content/@href)]");
         this.theItemXml = toString(this.theItemDoc, true);
         this.theItemXml = create(this.theItemXml);
         this.theItemDoc = getDocument(this.theItemXml);
         this.theItemId = getObjidValue(this.theItemDoc);
 
         // content to staging
         String fedoraUrl =
             this.properties.getProperty(PropertiesProvider.FEDORA_URL,
                 "http://localhost:8082/fedora");
         String url = fedoraUrl + "/get/escidoc:ex6/content";
 
         this.theItemDoc =
             (Document) substitute(this.theItemDoc,
                 "/item/components/component[1]/content/@href", url);
         this.theItemXml = toString(this.theItemDoc, false);
         try {
             this.theItemXml = update(this.theItemId, this.theItemXml);
             fail("No exception: Update with Fedora URL.");
         }
         catch (Exception e) {
             // Class ec = InvalidContentException.class;
             Class ec = FileNotFoundException.class;
             assertExceptionType(ec, e);
         }
     }
 
 }
