 package model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 
 /**
  * A Feature from a GFF file (gene isoform, exon, etc).
  * 
  * @author Mitchell Rosen
  * @version 20-Apr-2013
  */
 @AllArgsConstructor
 public class GffFeature {
    @Getter protected String chromosome; // chromosome/scaffold
    @Getter protected String source; // name of program that generated this feature
    @Getter protected String feature;
    @Getter protected int start; // inclusive
    @Getter protected int stop; // exclusive
    @Getter protected String score;
    @Getter protected boolean reverse; // Relative to |sequence|
    @Getter protected String frame;
    @Getter protected Map<String, String> attributes;
 
    public static GffFeature fromLine(String line) {
       if (line == null)
          return null;
       
       String[] tokens = line.split("\\s+");
 
       String chromosome = tokens[0];
       String source = tokens[1];
       String feature = tokens[2];
       int start = Integer.parseInt(tokens[3]);
       int stop = Integer.parseInt(tokens[4]);
       String score = tokens[5];
       boolean reverse = tokens[6].equals("-");
       String frame = tokens[7];
 
       Map<String, String> attributes = new HashMap<String, String>();
       for (int i = 8; i < tokens.length; i += 2)
          attributes.put(tokens[i], tokens[i + 1]);
 
       switch (feature) {
       case "mRNA":
          return new GeneIsoform(chromosome, source, feature, start, stop, score, reverse, frame,
                attributes);
       case "CDS":
          return new Exon(chromosome, source, feature, start, stop, score, reverse, frame,
                attributes);
       default:
          return new GffFeature(chromosome, source, feature, start, stop, score, reverse, frame,
                attributes);
       }
    }
    
    public int size() {
      return stop - start;
    }
    
    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append(String.format("Chromosome: %s\n", chromosome));
       sb.append(String.format("Source: %s\n", source));
       sb.append(String.format("Feature: %s\n", feature));
       sb.append(String.format("Start: %d\n", start));
       sb.append(String.format("Stop: %d\n", stop));
       sb.append(String.format("Score: %s\n", score));
       sb.append(String.format("Reverse: %s\n", reverse));
       sb.append(String.format("Frame: %s\n", frame));
       
       sb.append("Attributes: {\n");
       for (Map.Entry<String, String> entry : attributes.entrySet())
          sb.append(String.format("   %s: %s\n", entry.getKey(), entry.getValue()));
       sb.append("}\n");
       return sb.toString();
    }
 }
