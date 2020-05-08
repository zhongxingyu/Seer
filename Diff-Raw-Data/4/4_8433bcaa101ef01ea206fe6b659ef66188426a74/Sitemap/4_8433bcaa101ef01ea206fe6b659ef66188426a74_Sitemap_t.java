 package uk.co.arjones.ant.task;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.util.FileUtils;
 
 import com.redfin.sitemapgenerator.WebSitemapGenerator;
 
 import java.util.Vector;
 import java.io.File;
 
 public class Sitemap extends Task {
     
     private Vector filesets = new Vector();
     private File destdir;
     private String url;
     
     /**
      * Receives a nested fileset from the ant task
      * @param fileset The nested fileset to recieve.
      */
     public void addFileSet(FileSet fileset) {
         if (!filesets.contains(fileset)) {
             filesets.add(fileset);
         }
     }
     
     /**
    * Recieves the url attribute from the ant task
    * @param url
     */
     public void setUrl(String url) {
         this.url = url;
     }
     
     /**
      * Receives the destdir attribute from the ant task.
      * @param destdir
      */
     public void setDestdir (File destdir) {
         this.destdir = destdir;
     }
     
     /**
      * Executes the task
      */
      public void execute() throws BuildException {
         if(this.url == null){
             throw new BuildException("You must specify the url");
         }
         
         if(this.destdir == null){
             throw new BuildException("You must specify the destdir");
         }
         
         if (filesets.size() == 0) {
             throw new BuildException("You must specify one or more fileset child elements");
         }
         
         try {
             WebSitemapGenerator wsg = new WebSitemapGenerator(this.url, this.destdir);
             
             // Loop through fileset
             for (int i = 0; i < filesets.size(); i++) {
             
                 // Get current fileset
                 FileSet fs = (FileSet)filesets.elementAt(i);
                 DirectoryScanner ds = fs.getDirectoryScanner(getProject());
 
                 // Get base directory from fileset
                 File dir = ds.getBasedir();
 
                 // Get included files from fileset
                 String[] srcs = ds.getIncludedFiles();
 
                 // Loop through files
                 for (int j = 0; j < srcs.length; j++) {
 
                     // Make file object from base directory and filename
                     File temp = new File(dir,srcs[j]);
                     System.out.println(temp.getName());
                     wsg.addUrl(this.url+temp.getName());
                 }
                 wsg.write();
             }
         } catch(Exception e) {
             throw new BuildException(e);
         }            
     }
 }
