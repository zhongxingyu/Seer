 package net.sourceforge.pmd.renderers;
 
 import net.sourceforge.pmd.Report;
 import net.sourceforge.pmd.RuleViolation;
 
 import java.util.Iterator;
 
 public class TextRenderer implements Renderer {
 
     protected String EOL = System.getProperty("line.separator", "\n");
 
     public String render(Report report) {
         StringBuffer buf = new StringBuffer();
         for (Iterator i = report.iterator(); i.hasNext();) {
             RuleViolation rv = (RuleViolation) i.next();
             buf.append(EOL + rv.getFilename());
             buf.append("\t" + Integer.toString(rv.getLine()));
             buf.append("\t" + rv.getDescription());
         }
         for (Iterator i = report.errors(); i.hasNext();) {
            Report.ProcessingError error = (Report.ProcessingError) i.next();
            buf.append(EOL + error.getFile());
             buf.append("\t-");
            buf.append("\t" + error.getMsg());
         }
         return buf.toString();
     }
 }
