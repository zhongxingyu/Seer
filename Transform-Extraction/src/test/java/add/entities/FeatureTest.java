package add.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeatureTest {

    @Test
    public void testGetFeatureNames() {
        RepairActions actions = new RepairActions();
        assertEquals(50, actions.getFeatureNames().size());
    }

    @Test
    public void testIncrementFeatureCounter() {
        RepairActions actions = new RepairActions();
        actions.incrementFeatureCounter("assignExpChange");
        assertEquals(1, actions.getFeatureCounter("assignExpChange"));
    }

    @Test
    public void testSetFeatureCounter() {
        Metrics metrics = new Metrics();
        metrics.setFeatureCounter("nbFiles", 1);
        assertEquals(1, metrics.getFeatureCounter("nbFiles"));
    }

}