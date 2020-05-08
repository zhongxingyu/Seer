 /*
  * The MIT License
  *
  * Copyright (c) 2013, CloudBees, Inc.
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
 
 package jenkins.plugins.consoletail;
 
 import edu.umd.cs.findbugs.annotations.CheckForNull;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Result;
 import org.apache.commons.jelly.XMLOutput;
 
 import java.io.IOException;
 
 /**
  * @author Stephen Connolly
  */
 public class ConsoleTailProjectAction<P extends AbstractProject<P, R>, R extends AbstractBuild<P, R>>
         implements Action {
     private final P project;
 
     public ConsoleTailProjectAction(P project) {
         this.project = project;
     }
 
     public String getIconFileName() {
         return null;
     }
 
     public String getDisplayName() {
         return Messages.ConsoleTailProjectAction_DisplayName();
     }
 
     public String getUrlName() {
         return null;
     }
 
     public boolean isVisible() {
         return getBuild() != null;
     }
 
     @CheckForNull
     public R getBuild() {
         if (project == null) {
             return null;
         }
         R build = project.getLastBuild();
         while (build != null && build.isBuilding()) {
             build = build.getPreviousBuild();
         }
         if (build == null) {
             return null;
         }
         return Result.UNSTABLE.isBetterOrEqualTo(build.getResult()) ? build : null;
     }
 
     public boolean isComplete() throws IOException {
         R build = getBuild();
        return build == null || build.getLogText().length() < 4096L;
     }
 
     public void writeLogTo(XMLOutput out) throws IOException {
         R build = getBuild();
         if (build != null) {
            build.getLogText().writeHtmlTo(Math.max(0L, build.getLogText().length() - 4096L), out.asWriter());
         }
     }
 }
