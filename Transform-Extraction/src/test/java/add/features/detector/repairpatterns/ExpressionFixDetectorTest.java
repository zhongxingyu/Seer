package add.features.detector.repairpatterns;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import add.entities.PatternInstance;
import add.entities.RepairPatterns;
import add.main.Config;
import add.utils.TestUtils;

public class ExpressionFixDetectorTest {

	@Test
	public void chart1() {
		Config config = TestUtils.setupConfig("Chart 1");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("binOperatorModif") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("binOperatorModif");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("(dataset != null"));

		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("com.google.javascript.jscomp.NodeUtil.isFunctionExpression(n)"))
				.findFirst().isPresent());

		assertTrue(pi1.getFaultyLine().toString().equals(" dataset != null"));

	}

	@Test
	public void closure4() {
		Config config = TestUtils.setupConfig("Closure 4");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		// Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") > 0);
	}

	@Test
	public void closure104() {
		Config config = TestUtils.setupConfig("Closure 104");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		// Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") > 0);
	}

	@Test
	public void closure55() {
		Config config = TestUtils.setupConfig("Closure 55");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicExpand");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains(
				"(com.google.javascript.jscomp.NodeUtil.isFunctionExpression(n)) && (!(com.google.javascript.jscomp.NodeUtil.isGetOrSetKey(n.getParent())))"));

		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("com.google.javascript.jscomp.NodeUtil.isFunctionExpression(n)"))
				.findFirst().isPresent());

		assertTrue(pi1.getFaultyLine().toString()
				.equals("return com.google.javascript.jscomp.NodeUtil.isFunctionExpression(n)"));

	}

	@Test
	public void chart5() {
		Config config = TestUtils.setupConfig("Chart 5");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") == 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicReduce");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("f (index >= 0) {"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("(!(this.allowDuplicateXValues))"))
				.findFirst().isPresent());

		assertTrue(pi1.getFaultyLine().toString().contains("if ((index >= 0) && (!(this.allowDuplicateXValues))) {"));

	}

	@Test
	public void chart16() {
		Config config = TestUtils.setupConfig("Chart 16");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") == 0);
		// Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") > 0);
	}

	@Test
	public void closure6() {
		Config config = TestUtils.setupConfig("Closure 6");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") == 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);
	}

	@Test
	public void closure20() {
		Config config = TestUtils.setupConfig("Closure 20");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicExpand");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains(
				"((value != null) && ((value.getNext()) == null)) && (com.google.javascript.jscomp.NodeUtil.isImmutableValue(value))"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("if (value != null) {")).findFirst()
				.isPresent());

		assertTrue(pi1.getFaultyLine().toString().contains("if (value != null) {"));

	}

	@Test
	public void closure23() {
		Config config = TestUtils.setupConfig("Closure 23");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicReduce");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("for (int i = 0; current != null; i++) {"));

		assertTrue(
				pi1.getFaulty().stream().filter(e -> e.toString().contains("(i < intIndex)")).findFirst().isPresent());

		assertTrue(
				pi1.getFaultyLine().toString().contains("for (int i = 0; (current != null) && (i < intIndex); i++) {"));

	}

	@Test
	public void closure30() {
		Config config = TestUtils.setupConfig("Closure 30");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") > 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicReduce");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("if (n.isName()) {"));

		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("jsScope.isDeclared(n.getString(), true)")).findFirst().isPresent());

		assertTrue(pi1.getFaultyLine().toString()
				.contains("if ((n.isName()) && (jsScope.isDeclared(n.getString(), true))) {"));

	}

	@Test
	public void closure31() {
		Config config = TestUtils.setupConfig("Closure 31");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") > 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("expLogicReduce");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString()
				.contains("(options.dependencyOptions.needsManagement()) && (options.closurePass)"));

		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("(!(options.skipAllPasses))")).findFirst()
				.isPresent());

		assertTrue(pi1.getFaultyLine().toString().contains(
				// "if (((options.dependencyOptions.needsManagement()) &&
				// (!(options.skipAllPasses))) && (options.closurePass)) {"
				"((options.dependencyOptions.needsManagement()) && (!(options.skipAllPasses))) && (options.closurePass)"));
	}

	@Test
	public void closure35() {
		Config config = TestUtils.setupConfig("Closure 35");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);
	}

	@Test
	public void closure131() {
		Config config = TestUtils.setupConfig("Closure 131");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicReduce") == 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);
	}

	@Test
	public void math64() {
		Config config = TestUtils.setupConfig("Math 64"); // False positive

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		// Assert.assertTrue(repairPatterns.getFeatureCounter("expArithMod") > 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") == 0);
	}

	@Test
	public void math76() {
		Config config = TestUtils.setupConfig("Math 76");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		// Assert.assertTrue(repairPatterns.getFeatureCounter("expArithMod") > 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicMod") > 0);
	}

	@Test
	public void closure80() {
		Config config = TestUtils.setupConfig("Closure 80");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") > 0);
	}

	@Test
	public void closure19() {
		Config config = TestUtils.setupConfig("Closure 19");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") == 0);
	}

	@Test
	public void closure44() {
		Config config = TestUtils.setupConfig("Closure 44");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expLogicExpand") == 0);
	}

	@Test
	public void chart10() {
		Config config = TestUtils.setupConfig("Chart 10");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("expArithMod") == 0);
	}
}
