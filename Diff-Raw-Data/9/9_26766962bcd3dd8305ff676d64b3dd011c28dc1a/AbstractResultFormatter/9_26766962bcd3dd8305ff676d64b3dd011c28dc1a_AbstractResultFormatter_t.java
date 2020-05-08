 /*
 * $Id: AbstractResultFormatter.java,v 1.6 2004-05-27 14:33:41 o_rossmueller Exp $
  *
  * 2002 Oliver Rossmueller
  *
  */
 package org.junitee.anttask;
 
 
 import java.io.*;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NamedNodeMap;
 
 
 /**
 * @version $Revision: 1.6 $
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
  */
 public abstract class AbstractResultFormatter implements JUnitEEResultFormatter {
 
 
   private OutputStream out;
   private File outfile;
   private boolean filterTrace;
   private String extension;
 
 
   public boolean isFilterTrace() {
     return filterTrace;
   }
 
 
   public void setFilterTrace(boolean filterTrace) {
     this.filterTrace = filterTrace;
   }
 
 
   public void setOut(OutputStream out) {
     this.out = out;
   }
 
 
   public void setOutfile(File file) {
     outfile = file;
   }
 
 
   public void setExtension(String extension) {
     this.extension = extension;
   }
 
 
   public OutputStream getOutput(String testName) throws FileNotFoundException {
     if (out != null) {
       return out;
     }
     String fileName = outfile.getAbsolutePath() + testName + extension;
    out = new FileOutputStream(fileName);
    return out;
   }
 
 
   public void flush() throws IOException {
     out.flush();
     if (out != null && out != System.out && out != System.err) {
       try {
         out.close();
       } catch (IOException e) { /* ignore */
       }
     }
   }
 
 
   protected String getTestName(Node testSuiteNode) {
     NamedNodeMap attributes = testSuiteNode.getAttributes();
     String name = attributes.getNamedItem("name").getNodeValue();
     String pkg = attributes.getNamedItem("package").getNodeValue();
 
     if (pkg != null && pkg.length() != 0) {
       return pkg + "." + name;
     } else {
       return name;
     }
   }
 
 
 }
