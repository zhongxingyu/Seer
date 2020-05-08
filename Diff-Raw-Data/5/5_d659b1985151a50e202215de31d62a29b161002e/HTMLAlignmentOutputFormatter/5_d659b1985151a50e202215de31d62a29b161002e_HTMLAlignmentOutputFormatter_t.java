 package de.bioinformatikmuenchen.pg4.alignment.io;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 
 /**
  *
  * @author koehleru
  */
 public class HTMLAlignmentOutputFormatter extends AbstractAlignmentOutputFormatter {
 
     public String format(AlignmentResult result) {
         check(result);
         StringBuilder builder = new StringBuilder();
         builder.append("<div>");
         for (SequencePairAlignment align : result.getAlignments()) {
            builder.append("<h5>Aligment of ").append(result.getQuerySequenceId()).append(" of ").append(result.getTargetSequenceId()).append("</h5>");
            builder.append("<h5>").append(result.getScore()).append("</h5>");
             //First line
             builder.append("<pre>");
             builder.append(align.queryAlignment);
             builder.append("</pre>");
             //Second line in red
             builder.append("<pre style=\"color: red;\">");
            align.calculateMatchLine();
             builder.append(align.matchLine);
             builder.append("</pre>");
             //Third line
             builder.append("<pre>");
             builder.append(align.targetAlignment);
             builder.append("</pre>");
 
             builder.append("</div>");
         }
         return builder.toString();
     }
 }
