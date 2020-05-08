 package de.bioinformatikmuenchen.pg4.alignment.io;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 /**
  *
  * @author koehleru
  */
 public class ScoreOnlyAlignmentOutputFormatter extends AbstractAlignmentOutputFormatter {
 
     private DecimalFormat numberFormat = new DecimalFormat();
 
     public ScoreOnlyAlignmentOutputFormatter() {
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
        numberFormat.setGroupingUsed(false);
         numberFormat.setMinimumFractionDigits(4);
         numberFormat.setMinimumFractionDigits(4);
        numberFormat.setDecimalSeparatorAlwaysShown(false);
         numberFormat.setDecimalFormatSymbols(dfs);
     }
 
     @Override
     public String format(AlignmentResult result) {
         check(result);
        if(Math.abs(result.getScore()) < 0.0000000001) {
            result.setScore(0.0);
        }
         return result.getQuerySequenceId() + " " + result.getTargetSequenceId() + " " + numberFormat.format(result.getScore());
     }
 }
