 /*
  * The MIT License
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., Alan Harder
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.nodenamecolumn;
 
 import hudson.Extension;
 import hudson.XmlFile;
 import hudson.matrix.MatrixProject;
 import hudson.maven.MavenModuleSet;
 import hudson.model.Descriptor;
 import hudson.model.Job;
 import hudson.model.FreeStyleProject;
 import hudson.views.ListViewColumn;
 
 import java.io.IOException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * View column that shows the node name by parsing the configuration.
  *
  * @author Kalpana Nagireddy
  */
 public class NodeNameColumn extends ListViewColumn {
 
     public String getNodeName(Job job) {
         String name="";
         XmlFile configXmlFile = job.getConfigFile();
         try{
             Object obj =  configXmlFile.read();
             name = getName(obj, job.getClass().getSimpleName());
         }catch(IOException ex){
            //Exception in reading config file
         }
         return name;
     }
     public enum JobTypeEnum{
        FreeStyleProject, MavenModuleSet, MatrixProject;
     }
     @Extension
     public static final Descriptor<ListViewColumn> DESCRIPTOR = new DescriptorImpl();
 
     public Descriptor<ListViewColumn> getDescriptor() {
         return DESCRIPTOR;
     }
 
     private String getName(Object obj, String type){
         String name = null;
         final JobTypeEnum jobType;
         try {
             jobType = JobTypeEnum.valueOf(type);
         } catch(IllegalArgumentException e) {
             throw new RuntimeException("String has no matching NumeralEnum value");
         }
        switch (jobType){
            case FreeStyleProject:
                name = ((FreeStyleProject)obj).getAssignedLabel().getName();
                break;
            case MavenModuleSet:
                name = ((MavenModuleSet)obj).getAssignedLabel().getName();
                break;
            case MatrixProject:
                name = ((MatrixProject)obj).getAssignedLabel().getName();
                break;
            default:
                name = "N/A";
 
         }
         return name;
     }
 
     private static class DescriptorImpl extends Descriptor<ListViewColumn> {
         @Override
         public ListViewColumn newInstance(StaplerRequest req,
                                           JSONObject formData) throws FormException {
             return new NodeNameColumn();
         }
 
         @Override
         public String getDisplayName() {
             return Messages.NodeNameColumn_DisplayName();
         }
     }
 }
