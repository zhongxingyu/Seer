 
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class API_InterfaceTest {
 
 	/**
 	 * initialize tests
 	 */
 	@Test
 	public void initializetestnotReturningNull() {
 		API_Interface api = new API();
 		assertNotNull(api.initalize());
 	}
 	@Test
 	public void initializetestnotReturningFalse() {
 		API_Interface api = new API();
 		assertSame(api.initalize(), true);
 	}
 	/**
 	 * authenticate_user negative tests
 	 */
 	@Test
 	public void authenticate_userTestArg1Null() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user(null, "abcd"), false);
 	}
 	@Test
 	public void authenticate_userTestArg1Empty() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user("", "abcd"), false);
 	}
 	@Test
 	public void authenticate_userTestArg2Null() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user("abcd", null), false);
 	}
 	@Test
 	public void authenticate_userTestArg2Empty() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user("abcd",""), false);
 	}
 	@Test
 	public void authenticate_userTestArgbothNull() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user(null, null), false);
 	}
 	@Test
 	public void authenticate_userTestArgbothEmpty() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user("",""), false);
 	}
 	@Test
 	public void authenticate_userTestArgNullEmpty() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user(null, ""), false);
 	}
 	@Test
 	public void authenticate_userTestArgEmptyNull() {
 		API_Interface api = new API();
 		assertSame(api.authenticate_user("", null), false);
 	}
 	/**
 	 * loadToRobot negative tests
 	 */
 	@Test
 	public void loadToRobotTestArgNull() {
 		API_Interface api = new API();
 		assertSame(api.loadToRobot(null), "Error");
 	}
 	@Test
 	public void loadToRobotTestArgEmpty() {
 		API_Interface api = new API();
 		assertSame(api.loadToRobot(""), "Error");
 	}
 	/**
 	 * loadToSimulator negative tests
 	 */
 	@Test
 	public void loadToSimulatorTestArgNull() {
 		API_Interface api = new API();
 		assertSame(api.loadToSimulator(null), "Error");
 	}
 	@Test
 	public void loadToSimulatorTestArgEmpty() {
 		API_Interface api = new API();
 		assertSame(api.loadToSimulator(""), "Error");
 	}
 	/**
 	 * translateLoadToRobot negative tests
 	 */
 	@Test
 	public void translateLoadToRobotTestArgNull() {
 		API_Interface api = new API();
 		assertSame(api.translateLoadToRobot(null), "Error");
 	}
 	@Test
	public void loadToSimulatorTestArgEmpty() {
 		API_Interface api = new API();
 		assertSame(api.translateLoadToRobot(""), "Error");
 	}
 }
