 /**
  * 
  */
 package sirius.utils.retriever.interfaces;
 
 import java.util.ArrayList;
 
 import org.kohsuke.github.GHIssue;
 import org.kohsuke.github.GHMilestone;
 
 /**
  * @author Myk Kolisnyk
  * 
  */
 public interface IStoryFormatter {
    String GetFooter(ArrayList<GHIssue> issues);
 
    String GetHeader(ArrayList<GHIssue> issues);
 
    String GetIssue(GHIssue issue);
 
    String GetLabels(GHIssue issue);
 
    String GetMilestone(GHMilestone milestone);
 
    void Out(String text);
 }
