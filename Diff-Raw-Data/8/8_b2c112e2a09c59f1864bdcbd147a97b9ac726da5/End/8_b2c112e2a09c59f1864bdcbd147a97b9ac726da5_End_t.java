 package ru.spbau.bioinf.mgra;
 
 import org.jdom.Element;
 
 public class End {
 
     private String id;
     private int color;
 
     private EndType type;
 
     public End(int color, String s) {
         this.color = color;
         if ("oo".equals(s)) {
             id = "";
             type = EndType.OO;
         } else {
             int lastChar = s.length() - 1;
             id = s.substring(0, lastChar);
             type = EndType.getType(s.charAt(lastChar));
         }
     }
 
     public String getId() {
         return id;
     }
 
    public int getColor() {
        return color;
    }

    public EndType getType() {
        return type;
    }

     public Element toXml() {
         Element end = new Element("end");
         XmlUtil.addElement(end, "id", id);
         XmlUtil.addElement(end, "type", type.toString());
         XmlUtil.addElement(end, "color", color);
         return end;
     }
 }
