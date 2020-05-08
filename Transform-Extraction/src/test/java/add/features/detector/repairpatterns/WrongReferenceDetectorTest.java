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

public class WrongReferenceDetectorTest {

	@Test
	public void closure30() {
		Config config = TestUtils.setupConfig("Closure 30");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrongMethodRef");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("traverseRoots(externs, root)"));

		assertTrue(
				pi1.getFaulty().stream().filter(e -> e.toString().contains("traverse(root)")).findFirst().isPresent());
		assertTrue(pi1.getFaultyLine().toString().contains("(new NodeTraversal(compiler, this)).traverse(root)"));
	}

	@Test
	public void closure37() {
		Config config = TestUtils.setupConfig("Closure 37");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);
	}

	@Ignore
	@Test
	public void closure109() {
		Config config = TestUtils.setupConfig("Closure 109");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);
	}

	@Test
	public void lang26() {
		Config config = TestUtils.setupConfig("Lang 26");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);
	}

	@Test
	public void math9() {
		Config config = TestUtils.setupConfig("Math 9");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);
	}

	@Test
	public void math58() {
		Config config = TestUtils.setupConfig("Math 58");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrongMethodRef");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

	}

	@Test
	public void closure3() {
		Config config = TestUtils.setupConfig("Closure 3");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongMethodRef") == 0);
	}

	@Test
	public void chart8() {
		Config config = TestUtils.setupConfig("Chart 8");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongVarRef") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrongVarRef");
		System.out.println(insts);
		assertTrue(insts.size() > 0);
	}

	@Ignore
	@Test
	public void math33() {
		Config config = TestUtils.setupConfig("Math 33");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongVarRef") > 0);
	}

	@Test
	public void math64() {
		Config config = TestUtils.setupConfig("Math 64");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongVarRef") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrongVarRef");
		System.out.println(insts);
		assertTrue(insts.size() > 0);
	}

	@Test
	public void chart10() {
		Config config = TestUtils.setupConfig("Chart 10");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrongVarRef") == 0);
	}
}
