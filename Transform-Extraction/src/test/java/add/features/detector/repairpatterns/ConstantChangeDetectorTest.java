package add.features.detector.repairpatterns;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import add.entities.PatternInstance;
import add.entities.RepairPatterns;
import add.main.Config;
import add.utils.TestUtils;

public class ConstantChangeDetectorTest {

	@Test
	public void closure14() {
		Config config = TestUtils.setupConfig("Closure 14");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("constChange");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("Branch.ON_EX"));

		assertTrue(
				pi1.getFaulty().stream().filter(e -> e.toString().contains("Branch.UNCOND")).findFirst().isPresent());
		assertTrue(pi1.getFaultyLine().toString().contains("cfa.createEdge(fromNode, Branch.UNCOND, finallyNode)"));

	}

	@Test
	@Ignore
	public void closure40() {
		Config config = TestUtils.setupConfig("Closure 40");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();
		// PROBLEMS WITH THE GT
		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("constChange");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("true"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("false")).findFirst().isPresent());
		assertTrue(pi1.getFaultyLine().toString().contains("JsName name = getName(ns.name, false);"));

	}

	@Test
	@Ignore
	public void math15() {
		Config config = TestUtils.setupConfig("Math 15");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") == 0);
	}

	@Test
	public void math60() {
		Config config = TestUtils.setupConfig("Math 60");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") == 0);
	}

	@Test
	@Ignore
	public void time8() {
		Config config = TestUtils.setupConfig("Time 8");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("constChange");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("-59"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("0")).findFirst().isPresent());
		assertTrue(pi1.getFaultyLine().toString().contains("if (minutesOffset < 0 || minutesOffset > 59) "));
	}

	@Test
	public void time10() {
		Config config = TestUtils.setupConfig("Time 10");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("constChange") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("constChange");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("START_1972"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("0L")).findFirst().isPresent());
		assertTrue(pi1.getFaultyLine().toString()
				.contains("int[] values = chrono.get(zeroInstance, chrono.set(start, 0L), chrono.set(end, 0L))"));

	}
}
