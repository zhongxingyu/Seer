 /*
  * The MIT License
  *
  * Copyright (c) 2011-2012, CloudBees, Inc., Stephen Connolly.
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
 package jenkins.plugins.deployedoncolumn;
 
 import hudson.Extension;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixProject;
 import hudson.maven.MavenModule;
 import hudson.maven.MavenModuleSet;
 import hudson.model.AbstractProject;
 import hudson.model.Item;
 import hudson.model.Run;
 import hudson.views.ListViewColumn;
 import hudson.views.ListViewColumnDescriptor;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author stephenc
  * @since 01/10/2012 16:15
  */
 public class DeployedOnColumn extends ListViewColumn {
     @DataBoundConstructor
     public DeployedOnColumn() {
     }
 
     @SuppressWarnings("unused") // stapler
     public List<DeployedOnAction> getActions(Item item) {
         return DescriptorImpl.getActions(item);
     }
 
     @Extension(ordinal = -1000)
     public static class DescriptorImpl extends ListViewColumnDescriptor {
 
         @Override
         public String getDisplayName() {
             return Messages.DeployedOnColumn_DisplayName();
         }
 
         public static List<DeployedOnAction> getActions(Item item) {
             if (!(item instanceof AbstractProject)) {
                 return Collections.emptyList();
             }
             AbstractProject project = (AbstractProject) item;
             Run lastSuccessful = project.getLastSuccessfulBuild();
             if (lastSuccessful == null) {
                 // quickest answer
                 return Collections.emptyList();
             }
             if (item instanceof MavenModuleSet) {
                 List<DeployedOnAction> result = new ArrayList<DeployedOnAction>();
                 MavenModuleSet maven = (MavenModuleSet) item;
                 for (MavenModule m : maven.getModules()) {
                     result.addAll(getActions(m));
                 }
                 return result;
             } else if (item instanceof MatrixProject) {
                 List<DeployedOnAction> result = new ArrayList<DeployedOnAction>();
                 MatrixProject matrix = (MatrixProject) item;
                 for (MatrixConfiguration m : matrix.getActiveConfigurations()) {
                     result.addAll(getActions(m));
                 }
                 return result;
             }
             return getActions(lastSuccessful);
         }
 
         public static List<DeployedOnAction> getActions(Run run) {
             return run.getActions(DeployedOnAction.class);
         }
 
     }
 }
