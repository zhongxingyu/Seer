 package edu.toronto.bb.core.phred;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.List;
 
 import edu.toronto.bb.common.Constants;
 
 public class PhredWriter {
 
     OutputStream os;
     Collection<PhredScoreSequence> sequences;
     private int scoresPerLine = 60;
     byte[] lineSep = System.getProperty("line.separator").getBytes();
     int lineLength = (Constants.DIGITS_IN_SCORE + Constants.SCORE_DELIMITER.length()) * scoresPerLine;
 
     public PhredWriter(OutputStream os, Collection<PhredScoreSequence> sequences) {
         this.os = os;
         this.sequences = sequences;
     }
 
     public void setScorePerLine(int scorePerLine) {
         this.scoresPerLine = scorePerLine; 
     }
 
     /**
      * Allow to overwrite the system's line separator
      * @param newLineSeparator
      */
     public void setLineSeparator(String newLineSeparator) {
         this.lineSep = newLineSeparator.getBytes();
     }
 
     /**
      * Write the sequence to the OutputStream os
      * @throws IOException 
      */
     public void process() throws IOException {				
         for (PhredScoreSequence sequence : sequences) {			
             buildHeader(sequence);
             buildSequenceContents(sequence);            
         }
     }
 
     private void buildHeader(PhredScoreSequence sequence) throws IOException {        
         os.write('>');
         os.write(sequence.getAccession().getID().getBytes());
         os.write(lineSep);
     }
 
     private void buildSequenceContents(PhredScoreSequence sequence) throws IOException {        
         String scores = sequence.getScores();
         int seqSize = scores.length();
         int charsWrittenSoFar = 0;
         StringBuilder sb = new StringBuilder(lineLength);
         
         for (int i = 0; i < seqSize; i++) {            
             charsWrittenSoFar++;
            if (shouldWriteLineSep(charsWrittenSoFar)) {
                 os.write(sb.toString().trim().getBytes());
                 os.write(lineSep);
                 sb = new StringBuilder(lineLength);
             } else {
                 sb.append(scores.charAt(i));
             }
         }
         // last line in sequence
         os.write(sb.toString().getBytes());
         os.write(lineSep);  
     }
    
    private boolean shouldWriteLineSep(int charsWrittenSoFar) {
        return (charsWrittenSoFar) % (lineLength) == 0; 
     }
     
 }
