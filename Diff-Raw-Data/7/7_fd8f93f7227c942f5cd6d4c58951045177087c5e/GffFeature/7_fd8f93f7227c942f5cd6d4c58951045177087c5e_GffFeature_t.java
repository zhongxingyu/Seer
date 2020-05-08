 package model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A Feature from a GFF file (gene isoform, exon, etc).
  * 
  * @author Mitchell Rosen
  * @version 20-Apr-2013
  */
 public class GffFeature {
    protected String chromosome; // chromosome/scaffold
 
    protected String source; // name of program that generated this feature
    protected String feature;
    protected int start; // inclusive
    protected int stop; // exclusive
    protected String score;
    protected boolean reverse; // Relative to |sequence|
    protected String frame;
    protected Map<String, String> attributes;
 
    public String getChromosome() {
       return chromosome;
    }
 
    public void setChromosome(String chromosome) {
       this.chromosome = chromosome;
    }
 
    public String getSource() {
       return source;
    }
 
    public void setSource(String source) {
       this.source = source;
    }
 
    public String getFeature() {
       return feature;
    }
 
    public void setFeature(String feature) {
       this.feature = feature;
    }
 
    public int getStart() {
       return start;
    }
 
    public void setStart(int start) {
       this.start = start;
    }
 
    public int getStop() {
       return stop;
    }
 
    public void setStop(int stop) {
       this.stop = stop;
    }
 
    public String getScore() {
       return score;
    }
 
    public void setScore(String score) {
       this.score = score;
    }
 
    public boolean isReverse() {
       return reverse;
    }
 
    public void setReverse(boolean reverse) {
       this.reverse = reverse;
    }
 
    public String getFrame() {
       return frame;
    }
 
    public void setFrame(String frame) {
       this.frame = frame;
    }
 
    public Map<String, String> getAttributes() {
       return attributes;
    }
 
    public void setAttributes(Map<String, String> attributes) {
       this.attributes = attributes;
    }
 
    public GffFeature(String chromosome2, String source2, String feature2,
          int start2, int stop2, String score2, boolean reverse2, String frame2,
          Map<String, String> attributes2) {
       chromosome = chromosome2;
       source = source2;
       feature = feature2;
       start = start2;
      stop = stop2;
       score = score2;
       reverse = reverse2;
       frame = frame2;
       attributes = attributes2;
    }
 
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
 
       if (feature.equals("mRNA")) {
          return new GeneIsoform(chromosome, source, feature, start, stop,
                score, reverse, frame, attributes);
       } else if (feature.equals("CDS")) {
          return new Exon(chromosome, source, feature, start, stop, score,
                reverse, frame, attributes);
       } else {
          return new GffFeature(chromosome, source, feature, start, stop, score,
                reverse, frame, attributes);
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
          sb.append(String.format("   %s: %s\n", entry.getKey(),
                entry.getValue()));
       sb.append("}\n");
       return sb.toString();
    }
 }
