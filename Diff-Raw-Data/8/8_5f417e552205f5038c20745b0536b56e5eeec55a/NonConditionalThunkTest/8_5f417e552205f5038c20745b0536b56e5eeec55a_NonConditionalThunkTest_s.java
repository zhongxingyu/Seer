 package net.versustheinter.experiment.thunk.nonconditional;
 
 import net.versustheinter.experiment.thunk.conditional.ConditionalThunkTest;
 
 public class NonConditionalThunkTest extends ConditionalThunkTest {
     public void initializeThunk() {
         thunk = new NonConditionalThunk(computable);
     }
 }
