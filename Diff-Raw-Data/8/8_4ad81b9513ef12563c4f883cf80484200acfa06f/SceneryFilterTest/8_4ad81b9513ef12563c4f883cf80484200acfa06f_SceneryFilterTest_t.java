 package br.com.devx.scenery.web;
 
 import br.com.devx.scenery.web.chanchito.ChanchitoTemplateHandler;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 public class SceneryFilterTest extends HttpUnitTestCase {
     private TargetApp m_app;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         AppsConfig config = AppsConfig.getInstance();
         config.reset();
         m_app = config.getTargetApp();
         m_app.setPath("../../test/webapp");
         m_app.addTemplateHandler(new ChanchitoTemplateHandler());
     }
 
 
     public void testIndexHtml() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/" );
         WebResponse response = m_client.getResponse(request);
         assertTrue(response.getText().trim().contains("Hello, index.html!"));
     }
 
     /**
      * Se o cenario estah mapeado, usa-o
      */
     public void testVelocity() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/velocity.do" );
         WebResponse response = m_client.getResponse(request);
         assertEquals("Hello, Zeh maneh", response.getText().trim());
     }
 
     /**
      * Freemarker
      */
     public void testFreemarker() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/freemarker.do" );
         WebResponse response = m_client.getResponse( request );
         assertEquals("Hello, Zeh maneh", response.getText().trim());
     }
 
     public void testFreemarkerInclude() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/freemarker.do?test=include" );
         WebResponse response = m_client.getResponse( request );
         assertEquals("Hello, Zeh maneh", response.getText().trim());
     }
 
     public void testCustomTemplate() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/custom-template.do" );
         WebResponse response = m_client.getResponse(request);
         assertEquals("direct: Hello, Mr. Custom Template", response.getText().trim());
     }
 
     public void testCustomTemplateEx() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/custom-template-ex.do" );
         WebResponse response = m_client.getResponse(request);
         String text = response.getText();
         assertTrue(text.trim().contains("<html>"));
         assertTrue(text.trim().contains("Mr. Custom Template"));
     }
 
     /**
      * senao, aciona o outro site
      */
     public void testRedirect() throws IOException, SAXException {
        m_app.setUrl("http://gandralf.github.com");
 
        WebRequest request   = new GetMethodWebRequest( "http://localhost/scenery-manager" );
         WebResponse response = m_client.getResponse( request );
 
        assertTrue(response.getText().toLowerCase().contains("scenery manager"));
     }
 
     /**
      * senao, aciona o outro site
      */
     public void testError() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/velocity.do?error=true" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("<pre>     \"I'll forget a comma here\"</pre>"));
     }
 
     public void testVelocityMacro() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/velocityx.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("<td bgcolor=\"blue\">hello</td>"));
     }
 
     public void testVelocityParse() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/velocityx.do?test=parse" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("<td bgcolor=\"blue\">hello</td>"));
     }
 
     public void testVelocityTools() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/tool.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("He didn't say, \\\"Stop!\\\""));
         assertTrue(text.contains("&quot;bread&quot; &amp; &quot;butter&quot;"));
         assertTrue(text.contains("hello+here+%26+there"));
     }
 
     public void testWebInfClasses() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/classloader.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("Hello, Me!, your id is 1"));
     }
 
     public void testWebInfLib() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/classloader.do?from=lib" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("Hello, Me!, your id is 1"));
     }
 
     public void testSitemesh() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/sitemesh.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue("Didn't load hello.ftl (body)", text.contains("Hello, Zeh maneh!"));
         assertTrue("Didn't load main.flt decorator", text.contains("<title>Hello, sitemesh!</title>"));
         assertTrue("Didn't load main.flt decorator", text.contains("Welcome to the world"));
     }
 
     public void testVMSitemesh() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/vmsitemesh.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue("Didn't load hello.vm (body)", text.contains("Hello, Zeh maneh!"));
         assertTrue("Didn't load main.vm decorator", text.contains("<title>Hello, sitemesh!</title>"));
         assertTrue("Didn't load main.vm decorator", text.contains("Welcome to the world"));
         assertTrue("Didn't load hello.vm (body)", text.contains("This is velocity!"));
     }
 
     public void testHeadBody() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/vmsitemesh-head-body.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue("Didn't load hello.vm (body)", text.contains("Hello, Zeh maneh!"));
         assertTrue("Didn't load main.vm decorator", text.contains("<title>Hello, sitemesh!</title>"));
         assertTrue("Didn't load main.vm decorator", text.contains("Welcome to the world"));
         
         assertEquals("Double html", -1, text.indexOf("<html>", text.indexOf("<html>") + 1));
         assertEquals("Double head", -1, text.indexOf("<head>", text.indexOf("<head>") + 1));
         assertEquals("Double body", -1, text.indexOf("<body>", text.indexOf("<body>") + 1));
     }
 
     public void testSitemeshPrint() throws IOException, SAXException {
         WebRequest request   = new GetMethodWebRequest( "http://localhost/sitemesh-print.do" );
         WebResponse response = m_client.getResponse( request );
         String text = response.getText();
         assertTrue(text.contains("Hello, Zeh maneh!"));
         assertTrue("Didn't load main.flt decorator", text.contains("<title>Hello, sitemesh!</title>"));
         assertFalse("Didn't load main.flt decorator", text.contains("Welcome to the world"));
     }
 }
