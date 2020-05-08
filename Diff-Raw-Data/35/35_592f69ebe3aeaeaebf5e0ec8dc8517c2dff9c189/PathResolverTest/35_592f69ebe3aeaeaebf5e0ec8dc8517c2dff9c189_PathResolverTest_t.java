 package org.hors.impl.resolver;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.same;
 import static org.mockito.Mockito.when;
 
 import java.util.Set;
 
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.Extension;
 import javax.inject.Inject;
 
 import org.hors.impl.resolver.cdi.ControllerBeanExtension;
 import org.hors.resolver.ResourceResolver;
 import org.hors.servlet.WebRequest;
 import org.jboss.arquillian.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.collect.Iterables;
 
 
@RunWith(MockitoJUnitRunner.class)
 public class PathResolverTest {
 

	@Mock
	ResourceProducer resource;
 	
	@Mock
	WebRequest request;
 	
 	
 	@Test
 	public void testResolvePath() throws Exception {
 		when(request.getPath()).thenReturn(TestBean.FOO_BAR);
		when(resource.apply(Matchers.<ResourceDescriptionVisitor>any(), Matchers.<VisitContext>any())).thenReturn(new TestBean("TestBean"));
		BeanResourceResolver resolver = new BeanResourceResolver(resource);
 		Object resolved = resolver.resolve(request);
 		assertNotNull(resolved);
 		assertTrue(resolved instanceof TestBean);
		assertEquals("TestBean", ((TestBean)resolved).getName());
 	}
 }
