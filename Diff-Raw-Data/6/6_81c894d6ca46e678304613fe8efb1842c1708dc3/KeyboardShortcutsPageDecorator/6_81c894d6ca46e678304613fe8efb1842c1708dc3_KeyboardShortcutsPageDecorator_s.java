 /*
  * The MIT License
  * 
  * Copyright (c) 2012, Jesse Farinacci
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
 
 package org.jenkins.ci.plugins.keyboard_shortcuts;
 
 import hudson.Extension;
 import hudson.model.Item;
 import hudson.model.PageDecorator;
 import hudson.model.TopLevelItem;
 import hudson.model.Job;
 import hudson.model.PermalinkProjectAction.Permalink;
 import hudson.model.User;
 import hudson.model.View;
 
 import java.util.TreeMap;
 
 import jenkins.model.Jenkins;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  * The <a
  * href="http://wiki.jenkins-ci.org/display/JENKINS/Keyboard+Shortcuts+Plugin">
  * Keyboard Shortcuts Plugin</a> provides keyboard shortcuts to quickly navigate
  * and manage <a href="http://jenkins-ci.org">Jenkins</a>.
  * 
  * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
  */
 @Extension
 public final class KeyboardShortcutsPageDecorator extends PageDecorator {
     public static String getAllNodesAsJson() {
         return NodeUtils.getAllNodesAsJsonArray().toString();
     }
 
     public static String getAllPermalinksAsJson() {
         final TopLevelItem topLevelItem = JobUtils.getJob();
 
         if (topLevelItem != null) {
             for (final Job<?, ?> job : topLevelItem.getAllJobs()) {
                 final JSONArray permalinks = new JSONArray();
 
                 int idx = 0;
                 for (final Permalink permalink : job.getPermalinks()) {
                     if (permalink.resolve(job) != null) {
                         final TreeMap<String, String> map = new TreeMap<String, String>();
                         map.put("idx", "ks_selector_" + Integer.toString(idx++));
                         map.put("url", permalink.getId());
                         map.put("name", permalink.getDisplayName());
                         permalinks.add(JSONObject.fromObject(map));
                     }
                 }
 
                 return permalinks.toString();
             }
         }
 
         return "undefined";
     }
 
     public static String getAllViewJobNamesAsJson() {
         final View view = ViewUtils.getView();
 
         if (view != null) {
             final JSONArray viewJobNames = new JSONArray();
 
             for (final TopLevelItem topLevelItem : view.getItems()) {
                 viewJobNames.add(topLevelItem.getDisplayName());
             }
 
             return viewJobNames.toString();
         }
 
         return "undefined";
     }
 
     public static String getAllViewsAsJson() {
         return ViewUtils.getAllViewsAsJsonArray().toString();
     }
 
     public static String getAllJobsAsJson() {
         return JobUtils.getAllJobsAsJsonArray().toString();
     }
 
     public static String getBaseJobUrl() {
         final Item job = JobUtils.getJob();
 
         if (job != null) {
             return job.getUrl();
         }
 
         return "undefined";
     }
 
     public static String getBaseUrl() {
         return Jenkins.getInstance().getRootUrlFromRequest();
     }
 
     public static String getBaseViewUrl() {
         final View view = ViewUtils.getView();
         if (view != null) {
             final String viewUrl = view.getUrl();
             if (StringUtils.isEmpty(viewUrl)) {
                 return "/";
             }
 
             return viewUrl;
         }
 
         return "undefined";
     }
 
     public static boolean isDisabled() {
         return isDisabled(User.current());
     }
 
     public static boolean isDisabled(
             final KeyboardShortcutsUserProperty property) {
         if (property == null) {
             return KeyboardShortcutsUserProperty.DEFAULT_DISABLED;
         }
 
         return property.isDisabled();
     }
 
     public static boolean isDisabled(final User user) {
         if (user == null) {
             return KeyboardShortcutsUserProperty.DEFAULT_DISABLED;
         }
 
         return isDisabled(user.getProperty(KeyboardShortcutsUserProperty.class));
     }
 
     public static boolean isJobPage() {
         return JobUtils.getJob() != null;
     }
 
     public static boolean isViewPage() {
         return !isJobPage() && ViewUtils.getView() != null;
     }
 
     @DataBoundConstructor
     public KeyboardShortcutsPageDecorator() {
         super();
     }
 
     @Override
     public String getDisplayName() {
         return Messages.Keyboard_Shortcuts_Plugin_DisplayName();
     }
 }
