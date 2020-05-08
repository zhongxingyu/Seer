 package socio.observer;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import socio.Config;
 import socio.model.Promotion;
 import socio.semantic.SemanticCore;
 import socio.semantic.Semantics;
 
 public class ObserverTest {
 
 	private Semantics semantics;
 	private SemanticCore core;
 
 	@Before
 	public void setUp() throws Exception {
 		Config.testmode();
 
 		semantics = new Semantics();
 		core = SemanticCore.getInstance();
 		core.clear();
 	}
 
 	@Test
 	public void test() {
 
 		Observer observer = new Observer() {
 
 			public Boolean wasNotified = false;
 
			@SuppressWarnings("unchecked")
 			@Override
 			public void update(Observable o, Object arg) {
 				if (o instanceof SemanticCore) {
 					wasNotified = true;
 					System.out.println(arg);
 
					List<Promotion> promotions = (List<Promotion>) arg;
 					System.out.println(promotions.get(0).getResource());
 					assertEquals("https://www.fbi.h-da.de/", promotions.get(0).getResource());
 				}
 			}
 
 			@Override
 			public String toString() {
 				return wasNotified.toString();
 			}
 		};
 
 		core.addObserver(observer);
 
 		core.persistStatements(semantics.constructDemoMessageModel(), true);
 		assertEquals(observer.toString(), "true");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		core.deleteObservers();
 	}
 }
