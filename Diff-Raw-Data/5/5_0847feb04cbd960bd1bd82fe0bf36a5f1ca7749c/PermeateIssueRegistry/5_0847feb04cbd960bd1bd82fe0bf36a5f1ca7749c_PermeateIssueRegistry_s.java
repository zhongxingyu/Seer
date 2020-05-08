 package com.github.sophiedankel.permeate;
 
 import com.github.sophiedankel.permeate.rules.XmlPermissionsDetector;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.android.tools.lint.client.api.IssueRegistry;
 import com.android.tools.lint.detector.api.Issue;
 
 public class PermeateIssueRegistry extends IssueRegistry {
     public PermeateIssueRegistry() {
     }
 
     @Override
     public List<Issue> getIssues() {
         return Arrays.asList(
                XmlPermissionsDetector.ISSUE
         );
     }
 
 }
