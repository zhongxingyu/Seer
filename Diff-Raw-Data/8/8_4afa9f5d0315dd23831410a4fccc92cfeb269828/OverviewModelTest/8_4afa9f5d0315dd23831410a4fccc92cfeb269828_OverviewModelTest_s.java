 package ch.opentrainingcenter.model.training.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.transfer.ITraining;
 import static org.junit.Assert.assertEquals;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class OverviewModelTest {
 
     private OverviewModel model;
     private List<ITraining> trainings;
 
     @Before
     public void setUp() {
         trainings = new ArrayList<>();
 
     }
 
     @Test
     public void testKeinTraining() {
 
         model = new OverviewModel(trainings);
 
         assertEquals(0, model.getAnzahlTrainings());
        assertEquals(0, model.getTotaleDistanzInMeter());
         assertEquals(0, model.getTotaleZeitInSekunden());
     }
 
     @Test
     public void testEinTraining() {
         final ITraining trainingA = mock(ITraining.class);
         when(trainingA.getDauer()).thenReturn(42D);
         when(trainingA.getLaengeInMeter()).thenReturn(1042D);
 
         trainings.add(trainingA);
 
         model = new OverviewModel(trainings);
 
         assertEquals(1, model.getAnzahlTrainings());
         assertEquals(42, model.getTotaleZeitInSekunden());
        assertEquals(1042, model.getTotaleDistanzInMeter());
     }
 
     @Test
     public void testZweiTraining() {
         final ITraining trainingA = mock(ITraining.class);
         when(trainingA.getDauer()).thenReturn(42D);
         when(trainingA.getLaengeInMeter()).thenReturn(1042D);
 
         final ITraining trainingB = mock(ITraining.class);
         when(trainingB.getDauer()).thenReturn(100D);
         when(trainingB.getLaengeInMeter()).thenReturn(1000D);
 
         trainings.add(trainingA);
         trainings.add(trainingB);
 
         model = new OverviewModel(trainings);
 
         assertEquals(2, model.getAnzahlTrainings());
         assertEquals(142, model.getTotaleZeitInSekunden());
        assertEquals(2042, model.getTotaleDistanzInMeter());
     }
 }
