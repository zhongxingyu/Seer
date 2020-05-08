 package net.versustheinter.experiment.thunk.conditional;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import net.versustheinter.experiment.thunk.Computable;
 import net.versustheinter.experiment.thunk.Thunkable;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ConditionalThunkTest {
     public Computable computable;
     public Thunkable thunk;
     
     @Before
     public void setUp() {
         initializeComputable();
         initializeThunk();
     }
     
     public void initializeThunk() {
         thunk = new ConditionalThunk(computable);
     }
     
     public void initializeComputable() {
         computable = mock(Computable.class);
     }
     
     @Test
     public void computesWhenReceivingGet() {        
         thunk.get();
         
         verify(computable).compute();
     }
     
     @Test
     public void doesNotComputeWhenReceivingSubsequentGets() {        
         thunk.get();
         thunk.get();
         
         verify(computable, times(1)).compute();
    } 
 }
