 package player.millitta.Generate;
 
 import player.millitta.Constants;
 
 public class Generator implements Constants, algds.Constants {
 
     private Generator() {
     }
 
     static public AbstractGenerator get(long board) {
         if( (board & (1L << BIT_PHASE)) != 0 && (board & (1L << (BIT_PHASE+1))) != 0) { // Flugphase
            return new GeneratorFlyingPhase(board);
         } else if ((board & (1L << BIT_PHASE)) != 0) { // Setzphase
             return new GeneratorPlacingPhase(board);
         } else { // Zugphase
            return new GeneratorMovingPhase(board);
         }
     }
 }
