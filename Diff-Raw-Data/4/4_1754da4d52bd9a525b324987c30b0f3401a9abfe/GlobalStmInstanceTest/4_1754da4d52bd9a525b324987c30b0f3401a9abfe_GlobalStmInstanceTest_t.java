 package org.multiverse.api;
 
 
 import static org.hamcrest.CoreMatchers.*;
 import org.junit.Test;
 import org.multiverse.api.programmatic.ProgrammaticReferenceFactoryBuilder;
 
 import static org.junit.Assert.*;
 import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
 /**
  * @author Sai Venkat
  */
 public class GlobalStmInstanceTest {
    @Test
    public void Test(){
        
    }
 //    private Stm stm;
 //    private Stm stm1;
 //    private Stm stm2;
 //
 //    @Test
 //    public void GiveMePreConfiguredSTMInstance(){
 //        stm = getGlobalStmInstance();
 //        assertThat(stm.getTransactionFactoryBuilder(), is(instanceOf(TransactionFactoryBuilder.class)));
 //        assertThat(stm.getProgrammaticReferenceFactoryBuilder(), is(instanceOf(ProgrammaticReferenceFactoryBuilder.class)));
 //    }
 //    @Test
 //    public void GiveMeSingleInstanceOfSTM() {
 //        stm1 = getGlobalStmInstance();
 //        stm2 = getGlobalStmInstance();
 //        assertThat(stm1, is(sameInstance(stm2)));
 //    }
 }
