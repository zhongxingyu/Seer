 package ru.spbau.bioinf.mgra;
 
 import org.jdom.Element;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class Gene {
 
     private String id;
     private Direction direction;
     private List<End> ends = new LinkedList<End>();
 
     public Gene(String id, Direction direction) {
         this.id = id;
         this.direction = direction;
     }
 
     public String getId() {
         return id;
     }
 
     public void addEnd(End end) {
         ends.add(end);
     }
 
     public void clearEnds() {
         ends.clear();
     }
 
     public Element toXml() {
         Element gene = new Element("gene");
         XmlUtil.addElement(gene, "id", id);
         XmlUtil.addElement(gene, "direction", direction.toString());
         for (End end : ends) {
             gene.addContent(end.toXml());
         }
 
         return gene;
     }
 }
