package add.features.detector.repairpatterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import add.entities.PatternInstance;
import add.entities.RepairPatterns;
import add.main.Config;
import add.utils.TestUtils;

public class WrapsWithDetectorTest {

	@Test
	public void chart18() {
		Config config = TestUtils.setupConfig("Chart 18");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsIf") > 0);

		List<PatternInstance> listWrap = repairPatterns.getPatternInstances().get("wrapsIf");
		System.out.println(listWrap);
		assertTrue(listWrap.size() > 0);

		PatternInstance pi1 = listWrap.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("index >= 0"));
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("rowData.removeValue(columnKey)"))
				.findFirst().isPresent());

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("rebuildIndex()"));
		// assertEquals(2, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("if (index < (this.keys.size()))"))
				.findFirst().isPresent());

	}

	@Ignore
	@Test
	public void lang31() {
		Config config = TestUtils.setupConfig("Lang 31");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsIfElse") > 0);

		List<PatternInstance> inst = repairPatterns.getPatternInstances().get("wrapsIfElse");
		System.out.println(inst);
		assertTrue(inst.size() > 0);

		PatternInstance pi1 = inst.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("i < csLastIndex"));
		assertTrue(pi1.getFaulty().size() == 1);
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("return true")).findFirst().isPresent());

	}

	@Test
	public void closure2() {
		Config config = TestUtils.setupConfig("Closure 2");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsIfElse") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("implicitProto == null"));
		assertTrue(pi1.getFaulty().size() == 1);
		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("currentPropertyNames = implicitProto.getOwnPropertyNames()"))
				.findFirst().isPresent());
	}

	@Test
	public void lang33() {
		Config config = TestUtils.setupConfig("Lang 33");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsIfElse") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("(array[i]) == null"));
		assertTrue(pi1.getFaulty().size() == 1);
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("array[i].getClass()")).findFirst()
				.isPresent());
	}

	@Test
	public void closure111() {
		Config config = TestUtils.setupConfig("Closure 111");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsIfElse") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("topType.isAllType()"));
		assertTrue(pi1.getFaulty().size() == 1);
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("topType")).findFirst().isPresent());

	}

	@Test
	public void chart21() {
		Config config = TestUtils.setupConfig("Chart 21");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsElse") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		// assertTrue(pi1.getNodeAffectedOp().toString().contains("topType.isAllType()"));
		assertEquals(6, pi1.getFaulty().size());
		assertTrue(
				pi1.getFaulty().stream().filter(e -> e.toString().contains("double minval =")).findFirst().isPresent());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("item.getMinOutlier()")).findFirst()
				.isPresent());

	}

	@Test
	public void lang17() {
		Config config = TestUtils.setupConfig("Lang 17");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapIfElse") > 0);

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString()
				.contains("pos += java.lang.Character.charCount(java.lang.Character.codePointAt(input, pos))"));
		// assertEquals(2, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("if (pos < (len - 2))")).findFirst()
				.isPresent());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("pos++")).findFirst().isPresent());

	}

	@Test
	public void math46() {
		Config config = TestUtils.setupConfig("Math 46");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapIfElse") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapIfElse");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("NaN"));
		// assertEquals(2, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("isZero")).findFirst().isPresent());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("INF")).findFirst().isPresent());
	}

	@Test
	public void time18() {
		Config config = TestUtils.setupConfig("Time 18");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsTryCatch") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsTryCatch");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("try"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("instant = iGregorianChronology"))
				.findFirst().isPresent());

	}

	@Test
	public void closure83() {
		Config config = TestUtils.setupConfig("Closure 83");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsTryCatch") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsTryCatch");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("try"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("params.getParameter(0)")).findFirst()
				.isPresent());
	}

	@Test
	public void math60() {
		Config config = TestUtils.setupConfig("Math 60");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapTryCatch") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapTryCatch");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("return 0.5 * "));
		// assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("if (x <")).findFirst().isPresent());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("return 1")).findFirst().isPresent());

	}

	@Test
	public void lang13() {
		Config config = TestUtils.setupConfig("Lang 13");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapTryCatch") == 0);
		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsTryCatch") == 1);

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsTryCatch") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsTryCatch");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("try"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("return java.lang.Class.forName(name, false,")).findFirst()
				.isPresent());
	}

	@Test
	public void chart10() {
		Config config = TestUtils.setupConfig("Chart 10");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("ImageMapUtilities.htmlEscape(toolTipText)"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("toolTipText")).findFirst().isPresent());
	}

	@Test
	public void chart12() {
		Config config = TestUtils.setupConfig("Chart 12");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("setDataset(dataset)"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("this.dataset = dataset")).findFirst()
				.isPresent());
	}

	@Test
	public void math105() {
		Config config = TestUtils.setupConfig("Math 105");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString()
				.contains("Math.max(0.0, ((sumYY) - (((sumXY) * (sumXY)) / (sumXX))))"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("((sumYY) - (((sumXY) * (sumXY)) / (sumXX)))")).findFirst()
				.isPresent());
	}

	@Test
	public void time8() {
		Config config = TestUtils.setupConfig("Time 8");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("Math.abs(minutesOffset)"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("inutesOffset")).findFirst().isPresent());

	}

	@Ignore
	@Test
	public void mockito14() {
		Config config = TestUtils.setupConfig("Mockito 14");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("new MockAwareVerificationMode(mock, mode)"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("mode")).findFirst().isPresent());
	}

	@Ignore
	@Test
	public void math27() {
		Config config = TestUtils.setupConfig("Math 27");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("100"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(
				pi1.getFaulty().stream().filter(e -> e.toString().contains("multiply(100)")).findFirst().isPresent());

	}

	@Test
	public void time17() {
		Config config = TestUtils.setupConfig("Time 17");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("unwrapMethod") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("unwrapMethod");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("convertUTCToLocal("));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream()
				.filter(e -> e.toString().contains("instant - (3 * (DateTimeConstants.MILLIS_PER_HOUR)))")).findFirst()
				.isPresent());
	}

	@Test
	public void closure124() {
		Config config = TestUtils.setupConfig("Closure 124");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsLoop") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsLoop");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString().contains("while (node.isGetProp())"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("node = node.getFirstChild()"))
				.findFirst().isPresent());

	}

	@Test
	public void math7() {
		Config config = TestUtils.setupConfig("Math 7");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("wrapsLoop") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("wrapsLoop");
		System.out.println(insts);
		assertTrue(insts.size() > 0);

		PatternInstance pi1 = insts.get(0);
		assertTrue(pi1.getNodeAffectedOp().toString()
				.contains("for (final org.apache.commons.math3.ode.events.EventState state : eventsStates)"));
		assertEquals(1, pi1.getFaulty().size());
		assertTrue(pi1.getFaulty().stream().filter(e -> e.toString().contains("node = node.getFirstChild()"))
				.findFirst().isPresent());
	}

}
