 package net.buhacoff.netvote.model;
 
 import java.util.List;
 
 /**
  *
  * @author jbuhacoff
  */
 public class Issue {
     public String issueId;
     public String title;
     public String description;
     public List<String> choices;
	public boolean anonymous; //is this an anonymous (general election) issue?  (this should be copied to each ballot submitted for this issue)
 }
