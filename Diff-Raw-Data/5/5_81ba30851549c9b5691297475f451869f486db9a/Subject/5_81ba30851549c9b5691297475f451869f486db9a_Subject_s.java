 package cz.edu.x3m.net.objects;
 
 import com.gargoylesoftware.htmlunit.html.DomElement;
 
 /**
  *
  * @author Jan
  */
 public class Subject {
 
     public static final String SEPERATOR = "#";
     public final String acronym;
     public final String title;
     public final int count;
     private DomElement tr;
 
     public Subject(DomElement acronym, DomElement title, DomElement nothing) {
         this.acronym = acronym.getElementsByTagName("span").get(0).asText();
         this.title = title.asText();
         this.tr = (DomElement) acronym.getParentNode();
 
         String name = tr.getNextElementSibling().getId();
         String nextName;
         DomElement nextTr = tr;
         int tmpCount = 0;
 
         do {
             nextTr = nextTr.getNextElementSibling();
             nextName = nextTr.getId();
             if (nextName.equals(name)) {
                 tmpCount++;
             } else {
                 break;
             }
         } while (true);
         count = tmpCount / 2;
     }
 
     public Subject(String line) {
         String[] items = line.split(SEPERATOR);
         acronym = items[0];
         title = items[1];
         count = Integer.parseInt(items[2]);
     }
 
     @Override
     public String toString() {
        return String.format("%s%s%s%s%d", acronym, SEPERATOR, title, SEPERATOR, count);
     }
 
     public String asOutput() {
         return String.format("%s%s%s%s%d", acronym, SEPERATOR, title, SEPERATOR, count);
     }
 
     public boolean isSameSubjectAs(Subject another) {
        return title.equals(another.title);
     }
 }
