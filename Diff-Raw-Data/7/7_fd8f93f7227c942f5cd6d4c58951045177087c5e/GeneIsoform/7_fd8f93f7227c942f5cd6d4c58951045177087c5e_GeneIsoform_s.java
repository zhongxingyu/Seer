 package model;
 
 import java.util.List;
 import java.util.Map;
 
 public class GeneIsoform extends GffFeature {
    protected Gene gene;
    protected List<Exon> exons;
    
    public Gene getGene() {
       return gene;
    }
 
    public void setGene(Gene gene) {
       this.gene = gene;
    }
 
    public List<Exon> getExons() {
       return exons;
    }
 
    public GeneIsoform(String chromosome, String source, String feature, int start, int stop,
          String score, boolean reverse, String frame, Map<String, String> attributes) {
       super(chromosome, source, feature, start, stop, score, reverse, frame, attributes);
       
       if (reverse)
          this.start -= 3;
       else
          this.stop += 3;
    }
 
    public String getGeneId() {
       return attributes.get("gene_id");
    }
    
    public String getIsoformName() {
       return attributes.get("transcript_id");
    }
    
    /**
     * Gets the Sequence that this Isoform's coding regions consist of.
     */
    public Sequence getSequence() {
       Sequence s = new Sequence();
       for (Exon e : exons) {
         System.out.println(gene);
          Sequence exonSequence = gene.getSequence().slice(e.getStart(), e.getStop());
          s = s.concat(exonSequence);
       }
       return s;
    }
    
    public void setExons(List<Exon> exons) { this.exons = exons; }
    
    public int numExons() {
       return exons.size();
    }
    
    public int numIntrons() {
       return exons.size() - 1;
    }
 
    public int exonSize() {
       int size = 0;
       for (Exon e : exons)
          size += e.size();
       return size;
    }
    
    public int intronSize() {
       int size = 0;
       for (int i = 0; i < exons.size()-1; ++i)
          size += exons.get(i+1).getStart() - exons.get(i).getStop();
       return size;
    }
 }
