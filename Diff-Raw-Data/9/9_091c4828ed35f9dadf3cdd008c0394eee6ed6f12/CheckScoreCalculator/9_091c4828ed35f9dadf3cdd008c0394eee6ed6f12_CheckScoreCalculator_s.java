 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 
 /**
  *
  * @author koehleru
  */
 public class CheckScoreCalculator {
 
     public static String[] stripStartAndEndGapsLocal(String qa, String ta) {
         int startIndex = 0;
         int endIndex = qa.length();
         if ((qa.startsWith("-") && ta.startsWith("-") || (qa.endsWith("-") && ta.endsWith("-")))) {
             throw new IllegalArgumentException("Alignment starts or ends with a gap on both parts!");
         }
         for (int i = 0; i < qa.length(); i++) {
             if (qa.charAt(i) == '-' || ta.charAt(i) == '-') {
                 startIndex++;
             } else {
                 break;
             }
         }
 
         //
         // Handle end gaps
         //
         for (int i = qa.length() - 1; i < ta.length(); i--) {
             if (ta.charAt(i) == '-' || qa.charAt(i) == '-') {
                 endIndex--;
             } else {
                 break;
             }
         }
         return new String[]{qa.substring(startIndex, endIndex), ta.substring(startIndex, endIndex)};
     }
 
     public static String[] stripStartAndEndGapsFreeshift(String qa, String ta) {
         int startIndex = 0;
         int endIndex = qa.length();
         if ((qa.startsWith("-") && ta.startsWith("-") || (qa.endsWith("-") && ta.endsWith("-")))) {
             throw new IllegalArgumentException("Alignment starts or ends with a gap on both parts!");
         }
         //
         // Handle start gaps
         //
         //We need to iterate over the alignment part that starts with a gap, if any
         if (qa.startsWith("-")) {
             for (int i = 0; i < qa.length(); i++) {
                 if (qa.charAt(i) == '-') {
                     startIndex++;
                 } else {
                     break;
                 }
             }
         } else if (ta.startsWith("-")) {
             for (int i = 0; i < qa.length(); i++) {
                 if (ta.charAt(i) == '-') {
                     startIndex++;
                 } else {
                     break;
                 }
             }
         }
         //
         // Handle end gaps
         //
         if (qa.endsWith("-")) {
             for (int i = qa.length() - 1; i > 0; i--) {
                 if (qa.charAt(i) == '-') {
                     endIndex--;
                 } else {
                     break;
                 }
             }
         } else if (ta.endsWith("-")) {
             for (int i = ta.length() - 1; i > 0; i--) {
                 if (ta.charAt(i) == '-') {
                     endIndex--;
                 } else {
                     break;
                 }
             }
         }
         return new String[]{qa.substring(startIndex, endIndex), ta.substring(startIndex, endIndex)};
     }
 
     public static double getOverallGapCost(String alignmentPart, IGapCost gapCost) {
         double scoreSum = 0.0;
         int currentGapLength = 0;
         for (int i = 0; i < alignmentPart.length(); i++) {
             if (alignmentPart.charAt(i) == '-') {
                 currentGapLength++;
             } else {
                 if (currentGapLength > 0) {
                     scoreSum += gapCost.getGapCost(currentGapLength);
                     currentGapLength = 0;
                 }
             }
         }
         //Process the last gap, if any
         if (currentGapLength > 0) {
             scoreSum += gapCost.getGapCost(currentGapLength);
         }
         return scoreSum;
     }
 
     public static double calculateCheckScoreNonAffine(AlignmentMode mode, SequencePairAlignment alignment, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         assert alignment.getQueryAlignment().length() == alignment.getTargetAlignment().length();
         String qa = alignment.getQueryAlignment();
         String ta = alignment.getTargetAlignment();
         double score = 0;
         //Remove gaps at the beginning/end for local
         if (mode == AlignmentMode.FREESHIFT) {
             String[] vals = stripStartAndEndGapsFreeshift(qa, ta);
             qa = vals[0];
             ta = vals[1];
         } else if (mode == AlignmentMode.LOCAL) {
             String[] vals = stripStartAndEndGapsLocal(qa, ta);
             qa = vals[0];
             ta = vals[1];
         }
         //Iterate over the characters
        for (int i = 0; i < alignment.getQueryAlignment().length(); i++) {
             if (qa.charAt(i) == '-' || ta.charAt(i) == '-') {
                 score += gapCost.getGapCost(1);
             } else { //Match or mismatch
                 score += distanceMatrix.distance(qa.charAt(i), ta.charAt(i));
             }
         }
         return score;
     }
 
     public static double calculateCheckScoreAffine(AlignmentMode mode, SequencePairAlignment alignment, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         if (alignment.getQueryAlignment().length() != alignment.getTargetAlignment().length()) {
             throw new IllegalArgumentException("Alignment parts have different lengths");
         }
         String qa = alignment.getQueryAlignment();
         String ta = alignment.getTargetAlignment();
         double score = 0;
         //Remove gaps at the beginning/end for local
         if (mode == AlignmentMode.FREESHIFT) {
             String[] vals = stripStartAndEndGapsFreeshift(qa, ta);
             qa = vals[0];
             ta = vals[1];
         } else if (mode == AlignmentMode.LOCAL) {
             String[] vals = stripStartAndEndGapsLocal(qa, ta);
             qa = vals[0];
             ta = vals[1];
         }
         //Calculate the substitution-only score!
         for (int i = 0; i < qa.length(); i++) {
             //Ignore gaps
             if (qa.charAt(i) == '-' || ta.charAt(i) == '-') {
                 continue;
             } else { //Match or mismatch
                 score += distanceMatrix.distance(qa.charAt(i), ta.charAt(i));
             }
         }
         //Return the sum 
         return score + getOverallGapCost(qa, gapCost) + getOverallGapCost(ta, gapCost);
     }
 }
