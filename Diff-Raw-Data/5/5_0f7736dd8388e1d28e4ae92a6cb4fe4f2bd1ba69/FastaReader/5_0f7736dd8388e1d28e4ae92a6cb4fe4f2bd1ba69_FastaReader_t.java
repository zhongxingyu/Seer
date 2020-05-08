 package de.blacksheepsoftware.gene;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import de.blacksheepsoftware.hmm.Alphabet;
 import de.blacksheepsoftware.hmm.Sequence;
 
 /**
  * @author <a href="bauerb@in.tum.de">Bernhard Bauer</a>
  *
  */
 public class FastaReader {
 
     protected static final Pattern headerPattern = Pattern.compile("^>\\s*(\\S+)\\s+(\\d+)\\s+bp\\s+(.*)");
 
     protected final BufferedReader r;
 
     protected String line;
 
     public FastaReader(BufferedReader r) throws IOException {
         this.r = r;
         line = r.readLine();
     }
 
     public boolean ready() {
         return (line != null);
     }
 
     public Sequence readSequence() throws IOException {
         if (!ready()) {
             return null;
         }
         Matcher m = headerPattern.matcher(line);
         if (!m.matches()) {
             throw new FileFormatException("Invalid sequence header");
         }
         final String identifier = m.group(1);
         final String basePairs = m.group(2);
         final String sequenceType = m.group(3);
 
         final int length = Integer.parseInt(basePairs);
 
         final Alphabet alphabet;
         if (sequenceType.endsWith("dna")) {
             alphabet = Alphabet.DNA;
         } else if (sequenceType.endsWith("ami")) {
             alphabet = Alphabet.AMINO_ACIDS;
         } else {
             throw new FileFormatException("Unknown sequence type for \""+identifier+"\" ("+sequenceType+")");
         }
 
         StringBuffer content = new StringBuffer();
        while (true) {
             line = r.readLine();
            if ((line == null) || line.startsWith(">")) {
                 break;
             }
             content.append(line);
         }
         return new Sequence(identifier, content.toString(), alphabet, length);
     }
 
     public List<Sequence> readAllSequences() throws IOException {
         List<Sequence> testSequences = new ArrayList<Sequence>();
         while (ready()) {
             testSequences.add(readSequence());
         }
         return testSequences;
     }
 
 }
