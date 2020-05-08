 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jdom.Element;
 import ru.spbau.bioinf.tagfinder.util.XmlUtil;
 
 public class Protein {
     private int proteinId;
     private String sequence;
     private String simplifiedAcids = null;
     private String name;
 
     public Protein(int proteinId, String sequence, String name) {
         this.proteinId = proteinId;
         this.sequence = sequence;
         this.name = name;
     }
 
     public Element toXml() {
         Element protein = new Element("protein");
         XmlUtil.addElement(protein, "protein-id", proteinId);
         XmlUtil.addElement(protein, "protein-name", name);
         XmlUtil.addElement(protein, "protein-sequence", sequence);
         return protein;
     }
 
     public int getProteinId() {
         return proteinId;
     }
 
     public String getSequence() {
         return sequence;
     }
 
     public String getName() {
         return name;
     }
 
     public String getSimplifiedAcids() {
         if (simplifiedAcids == null) {
             simplifiedAcids = sequence.replaceAll("L", "I").replaceAll("Z", "Q").replaceAll("B", "E").replaceAll("X", "I");
         }
         return simplifiedAcids;
     }
 
     private double[] yends = null;
 
     public double[] getYEnds() {
         if (yends != null) {
             return yends;
         }
         List<Double> ans = new ArrayList<Double>();
         double m = 0;
         String s = getSimplifiedAcids();
         for (int cur = s.length() - 1; cur >= 0; cur--) {
             Acid acid = Acid.getAcid(s.charAt(cur));
             if (acid != null) {
                 m += acid.getMass();
                 ans.add(m);
             } else {
                 break;
             }
         }
         yends = new double[ans.size()];
         for (int i = 0; i < yends.length; i++) {
             yends[i] = ans.get(i);
         }
 
         return yends;
     }
 
     private double[] bends = null;
 
     public double[] getBEnds() {
         if (bends != null) {
             return bends;
         }
         List<Double> ans = new ArrayList<Double>();
         double m = 0;
         String s = getSimplifiedAcids();
         for (int cur = 1; cur < s.length(); cur++) {
             Acid acid = Acid.getAcid(s.charAt(cur));
             if (acid != null) {
                 m += acid.getMass();
                 ans.add(m);
             } else {
                 break;
             }
         }
         bends = new double[ans.size()];
         for (int i = 0; i < bends.length; i++) {
             bends[i] = ans.get(i);
         }
 
         return bends;
     }
 }
