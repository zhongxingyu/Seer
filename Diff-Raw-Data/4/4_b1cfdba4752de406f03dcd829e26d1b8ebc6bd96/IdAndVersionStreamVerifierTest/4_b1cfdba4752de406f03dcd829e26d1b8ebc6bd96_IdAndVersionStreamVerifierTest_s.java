 package com.aconex.scrutineer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class IdAndVersionStreamVerifierTest {
 
     @Mock
     private IdAndVersionStream primaryStream;
     @Mock
     private IdAndVersionStream secondayStream;
 
     @Before
     public void setup() {
         initMocks(this);
     }
 
     @Test
     public void shouldOpenBothStreams() {
         IdAndVersionStreamVerifier idAndVersionStreamVerifier = new IdAndVersionStreamVerifier();
         idAndVersionStreamVerifier.verify(primaryStream, secondayStream);
         verify(primaryStream).open();
         verify(secondayStream).open();
     }
 
     @Test
     public void shouldCloseBothStreams() {
         IdAndVersionStreamVerifier idAndVersionStreamVerifier = new IdAndVersionStreamVerifier();
         idAndVersionStreamVerifier.verify(primaryStream, secondayStream);
        verify(primaryStream).open();
        verify(secondayStream).open();
     }
 }
