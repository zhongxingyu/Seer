 package nu.staldal.lsp.framework;
 
 import junit.framework.*;
 
 
 public class TestFrameworkStuff extends TestCase
 {
     public TestFrameworkStuff(String name)
     {
         super(name);
     }
 
     private DispatcherServlet dispatcherServlet;
     
     @Override
     public void setUp()
     {
         dispatcherServlet = new DispatcherServlet();
     }
     
     public void testFixServiceName() throws Exception
     {
        assertEquals("foobar", dispatcherServlet.dispatchService(new HttpServletRequestMock("foobar")));
        assertEquals("foobar", dispatcherServlet.dispatchService(new HttpServletRequestMock("/foobar")));
        assertEquals("foobar", dispatcherServlet.dispatchService(new HttpServletRequestMock("foobar.s")));
        assertEquals("foobar", dispatcherServlet.dispatchService(new HttpServletRequestMock("/foobar.s")));
        assertEquals("foobar", dispatcherServlet.dispatchService(new HttpServletRequestMock("foobar.sss")));
         
        assertEquals("foo/bar", dispatcherServlet.dispatchService(new HttpServletRequestMock("foo/bar")));
        assertEquals("foo/bar", dispatcherServlet.dispatchService(new HttpServletRequestMock("/foo/bar")));
        assertEquals("foo/bar", dispatcherServlet.dispatchService(new HttpServletRequestMock("foo/bar.s")));
        assertEquals("foo/bar", dispatcherServlet.dispatchService(new HttpServletRequestMock("/foo/bar.s")));
         
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock("")));
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock("/")));
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock("/.")));
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock("/.s")));
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock("/.sss")));
        assertEquals("", dispatcherServlet.dispatchService(new HttpServletRequestMock(null)));
 	}
 	
 }
