 package junit.sorcer.core.provider;
 
 import static org.junit.Assert.assertEquals;
 import static sorcer.eo.operator.context;
 import static sorcer.eo.operator.exert;
 import static sorcer.eo.operator.get;
 import static sorcer.eo.operator.in;
 import static sorcer.eo.operator.input;
 import static sorcer.eo.operator.job;
 import static sorcer.eo.operator.jobContext;
 import static sorcer.eo.operator.out;
 import static sorcer.eo.operator.output;
 import static sorcer.eo.operator.pipe;
 import static sorcer.eo.operator.result;
 import static sorcer.eo.operator.sig;
 import static sorcer.eo.operator.strategy;
 import static sorcer.eo.operator.task;
 import static sorcer.eo.operator.value;
 
 import java.io.IOException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.util.logging.Logger;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import sorcer.core.SorcerConstants;
 import sorcer.service.Job;
 import sorcer.service.Strategy.Access;
 import sorcer.service.Strategy.Flow;
 import sorcer.service.Strategy.Monitor;
 import sorcer.service.Strategy.Wait;
 import sorcer.service.Task;
 import sorcer.util.ProviderAccessor;
 import sorcer.util.Sorcer;
 import sorcer.util.exec.ExecUtils;
 import sorcer.util.exec.ExecUtils.CmdResult;
 //import static sorcer.vo.operator.args;
 //import static sorcer.vo.operator.outputVars;
 //import static sorcer.vo.operator.expr;
 //import static sorcer.vo.operator.expression;
 //import static sorcer.vo.operator.inputVars;
 //import static sorcer.vo.operator.var;
 //import static sorcer.vo.operator.varModel;
 //import static sorcer.vo.operator.vars;
 //import sorcer.core.dataContext.model.VarModel;
 //import sorcer.vfe.Var;
 
 /**
  * @author Mike Sobolewski
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class ArithmeticNetTest implements SorcerConstants {
 
 	private final static Logger logger = Logger
 			.getLogger(ArithmeticNetTest.class.getName());
 
 	static {
 		System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
 				+ "/configs/sorcer.policy");
 		System.setSecurityManager(new RMISecurityManager());
 		Sorcer.setCodeBaseByArtifacts(new String[] {
 				"org.sorcersoft.sorcer:sos-platform",
 				"org.sorcersoft.sorcer:ju-arithmetic-api" });
 		System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
 		System.out.println("Webster:" + Sorcer.getWebsterUrl());
 		System.out.println("Codebase:" + System.getProperty("java.rmi.server.codebase"));
 	}
 	
 	@BeforeClass
 	public static void setUpOnce() throws IOException, InterruptedException {
		CmdResult result = ExecUtils.execCommand("ant -f " + System.getenv("SORCER_HOME") 
				+ "/sos/sos-tests/ju-arithmetic/ju-arithmetic-prv/all-arithmetic-prv-boot-spawn.xml");
 		System.out.println("out: " + result.getOut());
 		System.out.println("err: " + result.getErr());
 		System.out.println("status: " + result.getExitValue());	
 		waitForServices();
 	}
 
     public static void waitForServices() throws InterruptedException {
 		boolean servicesUp = false;
 		int tries = 0; 
 		while (!servicesUp && tries < 8) {
 			Object subtractor = ProviderAccessor.getService(null, Subtractor.class);
 			if (subtractor!=null)
 				return;
 			Thread.sleep(1000);
 			tries++;
 		}
 	}
 	
 	
 	@AfterClass 
 	public static void cleanup() throws RemoteException, InterruptedException {
 		Sorcer.destroyNode(null, Adder.class);
 //		Sorcer.destroy(null, Arithmetic.class);
 	}
 	
 //	@Test
 //	public void arithmeticVars() throws Exception {
 //		Var y = var("y", expr("(x1 * x2) - (x3 + x4)", args(vars("x1", "x2", "x3", "x4")))); 
 //		Object val = value(y, entry("x1", 10.0), entry("x2", 50.0), entry("x3", 20.0), entry("x4", 80.0));
 //		//logger.info("y value: " + val);
 //		assertEquals(val, 400.0);
 //	}
 	
 //	@Test
 //	public void createJobModel() throws Exception {
 //		
 //		VarModel vm = varModel("Hello Arithmetic #1", 
 //				inputVars(var("x1", 10.0), var("x2", 50.0), var("x3", 20.0), var("x4", 80.0)),
 //				outputVars(var("t4", expression("x1 * x2", args(vars("x1", "x2")))), 
 //						var("t5", expression("x3 + x4", args(vars("x3", "x4")))),
 //						var("j1", expression("t4 - t5", args(vars("t4", "t5"))))));
 //				
 //		//logger.info("t4 value: " + value(var(vm, "t4")));
 //		assertEquals(value(var(vm, "t4")), 500.0);
 //
 //		//logger.info("t5 value: " + value(var(vm, "t5")));
 //		assertEquals(value(var(vm, "t5")), 100.0);
 //		
 //		//logger.info("j1 value: " + value(var(vm, "j1")));
 //		assertEquals(value(var(vm, "j1")), 400.0);
 //		
 //		assertEquals(value(var(put(vm, entry("x1", 20.0), entry("x2", 100.0)), "j1")), 1900.0);
 //		//logger.info("j1 value: " + value(var(vm, "j1")));
 //		//assertEquals(value(var(vm, "j1")), 1900.0);
 //	}
 	
 //	@Test
 //	public void createMogJob() throws Exception {
 //		
 //		VarModel vm = varModel("Hello Arithmetic #2", 
 //			inputVars(var("x1", 10.0), var("x2", 50.0), var("x3", 20.0), var("x4", 80.0)),
 //			outputVars(var("t4", expression("x1 * x2", args(vars("x1", "x2")))), 
 //				var("t5", task("t5", sig("add", AdderImpl.class), 
 //					cxt("add", in("arg/x3", var("x3")), in("arg/x4", var("x4")), result("result/y")))),
 //				var("j1", expression("t4 - t5", args(vars("t4", "t5"))))));
 //				
 //		//logger.info("t4 value: " + value(var(vm, "t4")));
 //		assertEquals(value(var(vm, "t4")), 500.0);
 //
 //		//logger.info("t5 value: " + value(var(vm, "t5")));
 //		assertEquals(value(var(vm, "t5")), 100.0);
 //		
 //		//logger.info("j1 value: " + value(var(vm, "j1")));
 //		assertEquals(value(var(vm, "j1")), 400.0);
 //	}
 	
 	@Test
 	public void arithmeticProviderExertTest() throws Exception {
 		
 		Task t5 = task(
 				"t5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0)));
 		
 		t5 = exert(t5);
 		logger.info("t5 dataContext: " + context(t5));
 		assertEquals("Wrong value for 100.0", value(context(t5), "result/value"), 100.0);
 	}
 	
 	@Test
 	public void arithmeticProviderGetTest() throws Exception {
 		
 		Task t5 = task(
 				"t5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), result("result/y")));
 		t5 = exert(t5);
 //		logger.info("t5 dataContext: " + dataContext(t5));
 //		logger.info("t5 value: " + get(t5));
 		assertEquals("Wrong value for 100.0", get(t5), 100.0);
 	}
 	
 	@Test
 	public void arithmeticProviderValueTest() throws Exception {
 		
 		Task t5 = task(
 				"t5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), result("result/y")));
 
 		assertEquals("Wrong value for 100.0", value(t5), 100.0);
 	}
 	
 	@Ignore
 	@Test
 	public void arithmeticMultiServiceTest() throws Exception {
 		
 		Task t5 = task(
 				"t5",
 				sig("add", Arithmetic.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), result("result/y")));
 		
 		assertEquals("Wrong value for 100.0", value(t5), 100.0);
 	}
 
 	@Test
 	public void arithmeticSpaceTaskTest() throws Exception {
 		Task t5 = task(
 				"t5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y")),
 				strategy(Access.PULL, Wait.YES));
 		
 		t5 = exert(t5);
 //		logger.info("t5 dataContext: " + dataContext(t5));
 //		logger.info("t5 value: " + get(t5, "result/y"));
 		assertEquals("Wrong value for 100.0", get(t5, "result/y"), 100.0);
 	}
 
 	@Test
 	public void exertJobPushParTest() throws Exception {
 		Job job = createJob(Flow.PAR, Access.PUSH);
 		job = exert(job);
 		//logger.info("job j1: " + job);
 		//logger.info("job j1 job dataContext: " + dataContext(job));
 		logger.info("job j1 job dataContext: " + jobContext(job));
 		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
 		assertEquals(get(job, "j1/t3/result/y"), 400.00);
 	}
 	
 	@Test
 	public void exertJobPushSeqTest() throws Exception {
 		Job job = createJob(Flow.SEQ, Access.PUSH);
 		job = exert(job);
 		//logger.info("job j1: " + job);
 		//logger.info("job j1 job dataContext: " + dataContext(job));
 		logger.info("job j1 job dataContext: " + jobContext(job));
 		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
 		assertEquals(get(job, "j1/t3/result/y"), 400.00);
 	}
 	
 	@Test
 	public void exertJobPullParTest() throws Exception {
 		Job job = createJob(Flow.PAR, Access.PULL);
 		job = exert(job);
 		//logger.info("job j1: " + job);
 		//logger.info("job j1 job dataContext: " + dataContext(job));
 		logger.info("job j1 job dataContext: " + jobContext(job));
 		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
 		assertEquals(get(job, "j1/t3/result/y"), 400.00);
 	}
 	
 	@Test
 	public void exertJobPullSeqTest() throws Exception {
 		Job job = createJob(Flow.SEQ, Access.PULL);
 		job = exert(job);
 		//logger.info("job j1: " + job);
 		//logger.info("job j1 job dataContext: " + dataContext(job));
 		logger.info("job j1 job dataContext: " + jobContext(job));
 		//logger.info("job j1 value @ j1/t3/result/y = " + get(job, "j1/t3/result/y"));
 		assertEquals(get(job, "j1/t3/result/y"), 400.00);
 	}
 	
 	// two level job composition with PULL and PAR execution
 	private Job createJob(Flow flow, Access access) throws Exception {
 		Task t3 = task("t3", sig("subtract", Subtractor.class), 
 				context("subtract", in("arg/x1", null), in("arg/x2", null),
 						out("result/y", null)));
 
 		Task t4 = task("t4", sig("multiply", Multiplier.class), 
 				context("multiply", in("arg/x1", 10.0), in("arg/x2", 50.0),
 						out("result/y", null)));
 
 		Task t5 = task("t5", sig("add", Adder.class), 
 				context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
 						out("result/y", null)));
 
 		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
 		//Job job = job("j1",
 		Job job = job("j1", //sig("service", RemoteJobber.class), 
 				//job("j2", t4, t5),
 				job("j2", t4, t5, strategy(flow, access)), 
 				t3,
 				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
 				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
 				
 		return job;
 	}
 		
 	@Test
 	public void asyncTaskTest() throws Exception {
 		//TODO get result from monitor
 		task("f5",
 				sig("add", Adder.class),
 				context("add", input("arg/x1", 20.0), input("arg/x2", 80.0),
 						output("result/y", null)), strategy(Monitor.YES, Wait.NO));
 	}
 	
 	public static void main(String[] args) throws Exception {
 		ArithmeticNetTest ant = new ArithmeticNetTest();
 		ant.arithmeticProviderExertTest();
 		ant.arithmeticProviderGetTest();
 		ant.arithmeticProviderValueTest();
 		ant.arithmeticSpaceTaskTest();
 		ant.asyncTaskTest();
 		ant.exertJobPullParTest();
 		ant.exertJobPullSeqTest();
 		ant.exertJobPushParTest();
 		ant.exertJobPushSeqTest();
 	}
 }
