 package de.bioinformatikmuenchen.pg4.alignment.io;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.util.CollectionUtil;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 /**
  *
  * @author koehleru
  */
 public class ALIAlignmentOutputFormatter extends AbstractAlignmentOutputFormatter {
 
     private DecimalFormat numberFormat = new DecimalFormat();
 
     public ALIAlignmentOutputFormatter() {
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
        numberFormat.setMinimumFractionDigits(5);
        numberFormat.setMaximumFractionDigits(5);
         numberFormat.setDecimalFormatSymbols(dfs);
     }
 
     @Override
     public String format(AlignmentResult result) {
         check(result);
         //
         // NOTE: This formatter currently only prints the first alignment, even if there are more
         //
         StringBuilder ret = new StringBuilder();
         ret.append(">").append(result.getQuerySequenceId()).append(" ").append(result.getTargetSequenceId()).append(" ").append(numberFormat.format(result.getScore())).append("\n");
         SequencePairAlignment alignment = result.getFirstAlignment();
         ret.append(result.getQuerySequenceId()).append(": ").append(alignment.getQueryAlignment()).append("\n");
         ret.append(result.getTargetSequenceId()).append(": ").append(alignment.getTargetAlignment());
         return ret.toString();
     }
 }
