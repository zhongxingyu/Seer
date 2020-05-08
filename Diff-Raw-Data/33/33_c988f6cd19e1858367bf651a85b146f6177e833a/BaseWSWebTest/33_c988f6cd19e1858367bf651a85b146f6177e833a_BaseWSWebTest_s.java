 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010, 2011 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: BaseWSWebTest.java,v 1.4 2011/09/24 04:46:48 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.test.ws;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 
 import javax.servlet.DispatcherType;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.emf.texo.model.ModelObject;
 import org.eclipse.emf.texo.model.ModelResolver;
 import org.eclipse.emf.texo.resolver.DefaultObjectResolver;
 import org.eclipse.emf.texo.server.model.response.ErrorType;
 import org.eclipse.emf.texo.server.store.CurrentEntityManagerRequestFilter;
 import org.eclipse.emf.texo.server.store.EntityManagerProvider;
 import org.eclipse.emf.texo.server.test.BaseTest;
 import org.eclipse.emf.texo.server.web.JSONRestWebServiceServlet;
 import org.eclipse.emf.texo.server.web.XMLRestWebServiceServlet;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 import org.eclipse.emf.texo.utils.ModelUtils;
 import org.eclipse.emf.texo.xml.ModelXMLLoader;
 import org.eclipse.emf.texo.xml.ModelXMLSaver;
 import org.eclipse.emf.texo.xml.XMLWebServiceObjectResolver;
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.io.ByteArrayBuffer;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 
 /**
  * Base test case for the server environment which also starts and stops jetty and registers the WS servlets for
  * subclasses to use.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.4 $
  */
 public abstract class BaseWSWebTest extends BaseTest {
 
   private Server server;
 
   private static final String CHARACTER_ENCODING = "UTF-8"; //$NON-NLS-1$
   private static final int PORT = 8080;
   private static final String CONTEXTNAME = "texo"; //$NON-NLS-1$
   protected static final String XMLWS = "xmlws"; //$NON-NLS-1$
   protected static final String JSONWS = "jsonws"; //$NON-NLS-1$
 
   private HttpClient httpClient;
 
   public BaseWSWebTest(String name) {
     super(name);
   }
 
   protected void startClient() throws Exception {
     if (httpClient != null) {
       stopClient();
     }
     httpClient = new HttpClient();
     httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
     httpClient.start();
   }
 
   protected void stopClient() throws Exception {
     if (httpClient != null) {
       httpClient.stop();
       httpClient = null;
     }
   }
 
   @Before
   @Override
   public void setUp() throws Exception {
     super.setUp();
 
     DefaultObjectResolver.setServerUri(getBaseURL());
 
     server = new Server(PORT);
 
     ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
     context.setContextPath("/" + CONTEXTNAME); //$NON-NLS-1$
     server.setHandler(context);
 
     EntityManagerProvider.getInstance().setUseCurrentEntityManagerPattern(true);
 
     final XMLRestWebServiceServlet xmlWebServiceServlet = new XMLRestWebServiceServlet();
     context.addServlet(new ServletHolder(xmlWebServiceServlet), "/" + XMLWS + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
 
     final JSONRestWebServiceServlet jsonRestWebServiceServlet = new JSONRestWebServiceServlet();
     context.addServlet(new ServletHolder(jsonRestWebServiceServlet), "/" + JSONWS + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
 
     final TestEntityManagerCleanUpServlet testEMServlet = new TestEntityManagerCleanUpServlet();
     context.addServlet(new ServletHolder(testEMServlet), "/testEM"); //$NON-NLS-1$ 
 
     final EnumSet<DispatcherType> all = EnumSet.of(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD,
         DispatcherType.INCLUDE, DispatcherType.REQUEST);
 
     context.addFilter(TestEntityManagerRequestFilter.class, "/*", all); //$NON-NLS-1$
     context.addFilter(CurrentEntityManagerRequestFilter.class, "/*", all); //$NON-NLS-1$
 
     server.start();
   }
 
   @Override
   @After
   public void tearDown() throws Exception {
     super.tearDown();
     if (httpClient != null) {
       stopClient();
     }
     server.stop();
     server.destroy();
     server = null;
   }
 
   protected ContentExchange doRequest(String wsPartOfUrl, String method, String content) throws Exception {
     if (httpClient == null) {
       startClient();
     }
 
     ContentExchange contentExchange = new ContentExchange();
     contentExchange.setURL(getURL() + "/" + encodeWsPart(wsPartOfUrl)); //$NON-NLS-1$ 
     contentExchange.setMethod(method);
 
     System.err.println(">>>>>>>>>>>>>>>>>>>>>");
     System.err.println(getURL() + "/" + wsPartOfUrl);
     System.err.println(method);
     System.err.println("<<<<<<<<<<<<<<<<<<<<<");
 
     if (content != null) {
       contentExchange.setRequestContent(new ByteArrayBuffer(content.getBytes(CHARACTER_ENCODING)));
     }
 
     httpClient.send(contentExchange);
     contentExchange.waitForDone();
     return contentExchange;
   }
 
   private String encodeWsPart(String wsPart) throws Exception {
     int qIndex = wsPart.indexOf("?"); //$NON-NLS-1$
     if (qIndex == -1) {
      return URLEncoder.encode(wsPart, "UTF-8"); //$NON-NLS-1$
     }
 
    return URLEncoder.encode(wsPart.substring(0, qIndex), "UTF-8") + wsPart.substring(qIndex); //$NON-NLS-1$
   }
 
   protected void doDeleteRequest(String wsPart, int expectedResponse) {
     try {
       final ContentExchange contentExchange = doRequest(wsPart, HttpMethods.DELETE, null);
       System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
       System.err.println(contentExchange.getResponseContent());
       Assert.assertEquals(expectedResponse, contentExchange.getResponseStatus());
     } catch (final Exception e) {
       throw new IllegalStateException(e);
     }
   }
 
   protected String doContentRequest(String wsPart, String content, int expectedResponse, String expectedContent,
       String method) {
     try {
 
       final ContentExchange contentExchange = doRequest(wsPart, method, content);
 
       System.err.println(content);
 
       dumpError(expectedResponse, contentExchange);
 
       Assert.assertEquals(expectedResponse, contentExchange.getResponseStatus());
 
       if (expectedResponse == 500) {
         // no content available anyway
         return ""; //$NON-NLS-1$
       }
       final String retContent = contentExchange.getResponseContent();
       System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
       System.err.println(retContent);
 
       if (expectedContent != null && retContent.indexOf(expectedContent) == -1) {
         System.err.println(retContent);
         Assert.fail();
       }
       return retContent;
     } catch (final Exception e) {
       throw new IllegalStateException(e);
     }
   }
 
   protected String getTagValue(String content, String tag) {
     final int index1 = content.indexOf("<" + tag + ">") + ("<" + tag + ">").length(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
     if (index1 == -1) {
       return ""; //$NON-NLS-1$
     }
     final int index2 = content.indexOf("</" + tag + ">"); //$NON-NLS-1$ //$NON-NLS-2$
     if (index2 == -1) {
       return ""; //$NON-NLS-1$
     }
     return content.substring(index1, index2);
   }
 
   protected String getOneObject(Object object) {
     return doGetRequest(getObjectUrlPart(object), null, 200);
   }
 
   protected void checkExists(Object object, boolean exists) {
     doGetRequest(getObjectUrlPart(object), null, exists ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
   }
 
   protected String getObjectUrlPart(Object object) {
     final Identifiable identifiable = (Identifiable) object;
     final ModelObject<?> valueModelObject = ModelResolver.getInstance().getModelObject(object);
     return ModelUtils.getQualifiedNameFromEClass(valueModelObject.eClass()) + "/" + identifiable.getDb_Id(); //$NON-NLS-1$
   }
 
   protected void getValidateOneObject(Object object) {
     final Identifiable identifiable = (Identifiable) object;
     final String content = getOneObject(object);
     final List<Object> result = deserialize(content);
     Assert.assertEquals(1, result.size());
     final Identifiable identifiable2 = (Identifiable) result.get(0);
     Assert.assertNotSame(identifiable, identifiable2);
     Assert.assertEquals(identifiable.getDb_Id(), identifiable2.getDb_Id());
     Assert.assertSame(identifiable.getClass(), identifiable2.getClass());
   }
 
   protected List<Object> deserialize(String content) {
     final ModelXMLLoader xmlLoader = new ModelXMLLoader();
     xmlLoader.getEMFModelConverter().setUriResolver(new XMLWebServiceObjectResolver());
     xmlLoader.setReader(new StringReader(content));
     final List<Object> result = xmlLoader.read();
     return result;
   }
 
   protected String serialize(Object object) {
     final ModelXMLSaver xmlSaver = new ModelXMLSaver();
     xmlSaver.setObjects(Collections.singletonList(object));
     final StringWriter sw = new StringWriter();
     xmlSaver.setWriter(sw);
     xmlSaver.getModelEMFConverter().getObjectResolver().setUseWebServiceUriFormat(true);
     xmlSaver.write();
     return sw.toString();
   }
 
   protected String doGetRequest(String wsPart, String testContent, int responseCode) {
     try {
       final ContentExchange contentExchange = doRequest(wsPart, HttpMethods.GET, null);
       dumpError(responseCode, contentExchange);
       final String content = contentExchange.getResponseContent();
       System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
 
       System.err.println(content);
 
       Assert.assertEquals(responseCode, contentExchange.getResponseStatus());
       if (testContent != null && content.indexOf(testContent) == -1) {
         System.err.println(content);
         Assert.fail();
       }
       return content;
     } catch (final Exception e) {
       throw new IllegalStateException("Exception when executing ws: " + wsPart, e); //$NON-NLS-1$
     }
   }
 
   private void dumpError(int expectedResponseCode, ContentExchange contentExchange) throws Exception {
     if (expectedResponseCode != contentExchange.getResponseStatus()
         && contentExchange.getResponseStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
       final String content = contentExchange.getResponseContent();
       if (content.contains("response:error")) { //$NON-NLS-1$
         final ErrorType errorType = (ErrorType) deserialize(content).get(0);
         System.err.println(errorType.getMessage());
         System.err.println(errorType.getStackTrace());
       }
     }
   }
 
   /**
    * @return the base url of the webservice
    */
   protected String getBaseURL() {
     return "http://localhost:" + PORT + "/" + CONTEXTNAME; //$NON-NLS-1$//$NON-NLS-2$
   }
 
   protected abstract String getURL();
 }
