 /*
  * The MIT License
  * 
  * Copyright (c) 2011, Jesse Farinacci
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
 
 package org.jenkins.ci.plugins.column.console;
 
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.views.ListViewColumnDescriptor;
 import hudson.views.ListViewColumn;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * A {@link ListViewColumn} which adds a fast-path icon to the
  * <code>lastBuild</code> console output.
  * 
  * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
  * @since 1.0
  */
 public final class LastBuildColumn extends ListViewColumn {
     /**
      * The plugin descriptor.
      */
     private static final class ConsoleColumnDescriptor extends
             ListViewColumnDescriptor {
         @Override
         public String getDisplayName() {
             return Messages.ConsoleColumn_Last_DisplayName();
         }
 
         @Override
         public ListViewColumn newInstance(final StaplerRequest request,
                 final JSONObject formData) throws FormException {
             return new LastBuildColumn();
         }
 
         @Override
         public boolean shownByDefault() {
             return false;
         }
     }
 
     /**
      * The plugin descriptor.
      */
     @Extension
     public static final Descriptor<ListViewColumn> DESCRIPTOR = new ConsoleColumnDescriptor();
 
     @Override
     public Descriptor<ListViewColumn> getDescriptor() {
         return DESCRIPTOR;
     }
 }
