 
 package axirassa.util.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.tapestry5.dom.Document;
 import org.apache.tapestry5.dom.Element;
 import org.jaxen.JaxenException;
 import org.junit.After;
 
 import axirassa.ioc.AxirassaSecurityModule;
 import axirassa.ioc.ExternalServicesMockingModule;
 import axirassa.ioc.FlowsModule;
 import axirassa.ioc.HibernateTestingModule;
 import axirassa.ioc.LoggingModule;
 import axirassa.ioc.MessagingModule;
 import axirassa.ioc.PageTestingModule;
 
 import com.formos.tapestry.testify.core.TapestryTester;
 import com.formos.tapestry.testify.junit4.TapestryTest;
 import com.formos.tapestry.xpath.TapestryXPath;
 
 public class TapestryPageTest extends TapestryTest {
 	private static final TapestryTester SHARED_TESTER = new TapestryTester("axirassa.webapp", FlowsModule.class,
 	        MessagingModule.class, LoggingModule.class, ExternalServicesMockingModule.class,
 	        HibernateTestingModule.class, AxirassaSecurityModule.class, PageTestingModule.class);
 
 
 	public TapestryPageTest() {
 		super(SHARED_TESTER);
 		setSecurityManager();
 	}
 
 
 	private void setSecurityManager() {
 		CustomSecurityManagerFactory factory = SHARED_TESTER.autobuild(CustomSecurityManagerFactory.class);
 		SecurityUtils.setSecurityManager(factory.getWebSecurityManager());
 	}
 
 
 	@After
 	public void stopSessions() {
 		HibernateCleanupService cleanupService = SHARED_TESTER.autobuild(HibernateCleanupService.class);
 		cleanupService.cleanup();
 	}
 
 
 	public void ensureNoErrors(Document page) throws JaxenException {
 		// ensure no errors
 		List<Element> errorNodes = TapestryXPath.xpath("//*[@class='t-error']").selectElements(page);
		assertEquals("tapestry error elements found", 0, errorNodes.size());
 
 		// ensure no stack trace
 		List<Element> stacktraceNodes = TapestryXPath.xpath("//*[@class='t-stack-trace']").selectElements(page);
		assertEquals("stack trace elements found", 0, stacktraceNodes.size());
 	}
 }
