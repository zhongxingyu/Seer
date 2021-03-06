 package edu.cmu.sphinx.frontend.util;
 
 import edu.cmu.sphinx.frontend.DataBlocker;
 import edu.cmu.sphinx.frontend.DataProcessor;
 import edu.cmu.sphinx.frontend.FrontEnd;
 import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
 
 /**
  * Some little helper methods to ease the handling of frontend-processor chains.
  *
  * @author Holger Brandl
  */
 public class FrontEndUtils {
 
 
     /** Returns a the next <code>DataProcessor</code> of type <code>predecClass</code> which preceeds <code>dp</code> */
     public static DataProcessor getDataSource(DataProcessor dp, Class<? extends DataProcessor> predecClass) {
         while (!predecClass.isInstance(dp.getPredecessor())) {
             dp = dp.getPredecessor();
 
             if (dp == null)
                 return null;
 
             if (dp instanceof FrontEnd)
                 dp = ((FrontEnd) dp).getLastDataProcessor();
         }
 
 
         return dp;
     }
 
 
     /**
      * Applies several heuristics in order to determine the shift-size of the fronted a given <code>DataProcessor</code>
      * belongs to.
      * <p/>
      * <p/>
      * The shift-size is searched using the following procedure <ol> <li> Try to determine the window shift-size used by
      * the <code>RaisedCosineWindower</code> which precedes the given <code>DataProcessor</code>. <li> If a data-blocker
      * is found within the precding processor chain but no <code>RaisedCosineWindower</code> 0 is returned. </ol>
      * <p/>
      * <p/>
      * If both approaches fail, an <code>AssertionError</code> becomes thrown.
      *
      * @param dataProc The <code>DataProcessor</code> which predecessors are searched backwards to some hints about the
      *                 used window shift size.
      * @return The found window shift size
      * @throws RuntimeException If both approaches fail, an <code>AssertionError</code> becomes thrown.
      */
     public static float getWindowShiftMs(DataProcessor dataProc) {
         DataProcessor dp = dataProc;
 
         while (!(dp instanceof RaisedCosineWindower)) {
             dp = dp.getPredecessor();
 
             if (dp == null) {
                 break;
             }
 
             if (dp instanceof FrontEnd) {
                 dp = ((FrontEnd) dp).getLastDataProcessor();
             }
         }
 
         if (dp != null) {
             return ((RaisedCosineWindower) dp).getWindowShiftInMs();
         }
 
         dp = dataProc;
         while (!(dp instanceof DataBlocker)) {
             dp = dp.getPredecessor();
 
             if (dp == null) {
                 break;
             }
 
             if (dp instanceof FrontEnd) {
                 dp = ((FrontEnd) dp).getLastDataProcessor();
             }
         }
 
        if (dp != null && dp instanceof DataBlocker) {
            return (float) ((DataBlocker) dp).getBlockSizeMs();
         }
 
         throw new RuntimeException("Can not dermine the current shift-size of the given feature frontend.");
     }
 }
