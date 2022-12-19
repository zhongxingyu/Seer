package add.features.detector.repairpatterns;

import org.junit.Assert;
import org.junit.Test;

import add.entities.RepairPatterns;
import add.main.Config;
import add.utils.TestUtils;

public class ConditionalBlockDetectorTest {

    @Test
    public void lang45() {
        Config config = TestUtils.setupConfig("Lang 45");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
    }

    @Test
    public void closure5() {
        Config config = TestUtils.setupConfig("Closure 5");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRetAdd") > 0);
    }

    @Test
    public void math48() {
        Config config = TestUtils.setupConfig("Math 48");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockExcAdd") > 0);
    }

    @Test
    public void math50() {
        Config config = TestUtils.setupConfig("Math 50");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRem") > 0);
    }

    @Test
    public void chart4() {
        Config config = TestUtils.setupConfig("Chart 4");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") == 0);
    }

    @Test
    public void chart14() {
        Config config = TestUtils.setupConfig("Chart 14");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") == 0);
    }

    @Test
    public void closure61() {
        Config config = TestUtils.setupConfig("Closure 61");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRetAdd") > 0);
    }

    @Test
    public void closure19() {
        Config config = TestUtils.setupConfig("Closure 19");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
    }

    @Test
    public void closure60() {
        Config config = TestUtils.setupConfig("Closure 60");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRetAdd") > 0);
    }

    @Test
    public void closure3() {
        Config config = TestUtils.setupConfig("Closure 3");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRetAdd") > 0);
    }

    @Test
    public void closure66() {
        Config config = TestUtils.setupConfig("Closure 66");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
    }

    @Test
    public void lang49() {
        Config config = TestUtils.setupConfig("Lang 49");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockOthersAdd") > 0);
    }

    @Test
    public void closure11() {
        Config config = TestUtils.setupConfig("Closure 11");

        RepairPatternDetector detector = new RepairPatternDetector(config);
        RepairPatterns repairPatterns = detector.analyze();

        Assert.assertTrue(repairPatterns.getFeatureCounter("condBlockRem") == 0);
    }

}
