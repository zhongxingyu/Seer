 package net.sf.jautl.rng.variates;
 
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 import net.sf.jautl.rng.interfaces.IDoublesSource;
 
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 
 public class TestBoolean {
 	private IDoublesSource ids;
 	
 	@Before
 	public void setUp() {
 		ids = mock(IDoublesSource.class);
 	}
 	
 	@Test
 	public void test() {
 		when(ids.nextDouble()).
             thenReturn(0.0).
             thenReturn(0.299).
             thenReturn(0.301).
             thenReturn(1.0);
 		
 		VariateBoolean vb = new VariateBoolean(ids);
 		vb.setProbability(0.3);
 		assert(vb.draw() == true);
 		assert(vb.draw() == true);
 		assert(vb.draw() == false);
 		assert(vb.draw() == false);
 	}
 	
 	@After
 	public void tearDown() {
 		ids = null;
 	}
 }
