 /**
  * 
  */
 package rinde.evo4mas.fabrirecht;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import rinde.ecj.GPFunc;
 import rinde.ecj.GPFuncNode;
 import rinde.ecj.GPProgram;
 import rinde.ecj.GPProgramParser;
 import rinde.ecj.GenericFunctions.Constant;
 import rinde.evo4mas.common.TruckContext;
 import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
 import rinde.sim.problem.fabrirecht.FabriRechtParser;
 import rinde.sim.problem.fabrirecht.FabriRechtScenario;
 import rinde.sim.scenario.ConfigurationException;
 
 /**
  * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
  * 
  */
 public class SimulationTest {
 
 	protected FabriRechtScenario scenario;
 	protected GPProgram<TruckContext> dummyProgram;
 
 	@Before
 	public void setup() throws FileNotFoundException {
 
 		final String scenFile = "files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc203.scenario";
 		// "../../RinSim/problem/data/test/fabri-recht/lc101.scenario"
 
 		scenario = FabriRechtParser.fromJson(new FileReader(scenFile), 10, 4);
 		dummyProgram = new GPProgram<TruckContext>(new GPFuncNode<TruckContext>(new Constant<TruckContext>(0)));
 	}
 
 	@Test
 	public void test() throws ConfigurationException {
 		final Simulation sim = new Simulation(scenario, dummyProgram, false);
 
 		final StatisticsDTO stats = sim.start();
 
 		System.out.println("shutdown prematurely? " + !stats.simFinish);
 		System.out.println(stats);
 	}
 
 	@Test
 	public void test2() {
 		final Collection<GPFunc<TruckContext>> funcs = new FRFunctions().create();
 		final Simulation sim2 = new Simulation(scenario, GPProgramParser.parseProgramFunc("(add 0.0 dist)", funcs),
 				false);
 
 		final StatisticsDTO stats2 = sim2.start();
 
 		// sim2.problemInstance.
 		System.out.println(stats2);
		// assertTrue(stats2.simFinish);
 
 	}
 
 	@Test
 	public void testProgram1() throws ConfigurationException {
 		final Collection<GPFunc<TruckContext>> funcs = new FRFunctions().create();
 		// (add (if4 (add (add 0.0 1.0) (add 0.0 1.0)) (add ado ado) (if4 dist
 		// ado ado 1.0) (if4 1.0 ado dist dist)) (if4 dist 0.0 0.0 dist))
 		String progString = "(add (if4 (add (add 0.0 1.0) (add 0.0 1.0)) (add ado ado) (if4 dist ado ado 1.0) (if4 1.0 ado dist dist)) (if4 dist 0.0 0.0 dist))";
 
 		progString = "(if4 (if4 (add mado dist) (if4 urge 1.0 est 0.0) (mul dist dist) (div est 0.0)) (if4 (if4 dist est 1.0 ado) (pow ttl 0.0) (mul 1.0 0.0) (add ttl dist)) (div (if4 0.0 dist mido mado) (sub mido mado)) (if4 (div est 1.0) (mul mido dist) (div mido ttl) (pow urge mido)))";
 
 		progString = "(if4 (mul (mul (mul mido ado) (pow 1.0 est)) (div est (if4 (sub (div est 1.0) mado) (div (div mado mido) (pow 1.0 est)) (pow (sub (mul 0.0 dist) (pow ado mido)) (if4 mido mado ado 1.0)) (pow ttl ttl)))) (if4 (sub (div est 1.0) (mul 0.0 ttl)) (if4 mido dist mido urge) (pow (sub (mul 0.0 dist) (div est 0.0)) (pow mado 1.0)) (add (pow (div (add (if4 (if4 mado dist (if4 ttl 1.0 0.0 mido) ado) mido mido (div est 0.0)) mado) ado) (mul (mul mido ado) (mul mido ado))) (mul 1.0 (div ado est)))) (pow urge mado) (sub mido ttl))";
 
 		final GPProgram<TruckContext> prog = GPProgramParser.parseProgramFunc(progString, funcs);
 		assertEquals(progString, prog.toString());
 
 		final Simulation sim = new Simulation(scenario, prog);
 		sim.start();
 
 	}
 
 }
