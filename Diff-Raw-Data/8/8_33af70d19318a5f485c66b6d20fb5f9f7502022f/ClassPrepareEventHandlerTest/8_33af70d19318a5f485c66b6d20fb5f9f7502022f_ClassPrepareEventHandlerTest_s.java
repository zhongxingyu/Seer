 package events;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import mockjdi.MockClassPrepareEvent;
 import mockjdi.MockField;
 import mockjdi.MockReferenceType;
 import mockjdi.MockVirtualMachine;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sun.jdi.Field;
 import com.sun.jdi.event.ClassPrepareEvent;
 
 public class ClassPrepareEventHandlerTest {	
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testClassPrepareEventHandler() {
		ClassPrepareEventHandler classPrepareEventHandler = new ClassPrepareEventHandler(new MockVirtualMachine(), new MockLogger());
 	}
 
 	@Test
 	public void testHandle() {
 		List<Field> allFields = new LinkedList<Field>();
 		allFields.add(new MockField());
 		allFields.add(new MockField());
 		ClassPrepareEvent classPrepareEvent = new MockClassPrepareEvent(new MockReferenceType("type", allFields));
 		
		ClassPrepareEventHandler classPrepareEventHandler = new ClassPrepareEventHandler(new MockVirtualMachine(), new MockLogger());
 		classPrepareEventHandler.handle(classPrepareEvent);
 	}
 
 }
