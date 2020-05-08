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
 
 package hudson.views.tabbar;
 
 import hudson.Extension;
 import hudson.views.ViewsTabBar;
 import hudson.views.ViewsTabBarDescriptor;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
 * This plugin provides an alternate rendering of the Views bar which runs along
 * the top of all views. This plugin is useful for instances which have a very
 * large number of views and want a compact rendering.
 * 
  * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
  * @version $Id$
  * @since 1.393
  */
 public final class DropDownViewsTabBar extends ViewsTabBar
 {
   @DataBoundConstructor
   public DropDownViewsTabBar()
   {
     super();
   }
 
   @Extension
   public static class DescriptorImpl extends ViewsTabBarDescriptor
   {
     @Override
     public String getDisplayName()
     {
      return "DropDown ViewsTabBar";
     }
   }
 }
