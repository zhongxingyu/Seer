package add.features.detector.repairpatterns;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import add.entities.PatternInstance;
import add.entities.RepairPatterns;
import add.main.Config;
import add.utils.TestUtils;

public class AssignmentDetectorTest {

	@Test
	public void Closure110() {
		Config config = TestUtils.setupConfig("Closure 110");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("addassignment") == 0);

		// Assert.assertTrue(repairPatterns.getFeatureCounter("addassignment") > 0);

		// List<PatternInstance> insts =
		// repairPatterns.getPatternInstances().get("addassignment");
		// System.out.println(insts);
		// assertTrue(insts.size() > 0);

	}

	@Test
	public void Chart21() {
		Config config = TestUtils.setupConfig("Chart 21");

		RepairPatternDetector detector = new RepairPatternDetector(config);
		RepairPatterns repairPatterns = detector.analyze();

		Assert.assertTrue(repairPatterns.getFeatureCounter("addassignment") > 0);

		List<PatternInstance> insts = repairPatterns.getPatternInstances().get("addassignment");
		System.out.println(insts);
		assertTrue(insts.size() > 0);
	}

}
