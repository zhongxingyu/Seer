 package mingleplugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 
public class MingleDescriptionSetter extends Recorder {
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
   BuildListener listener) throws InterruptedException {
   	MingleRestService service = MingleRestService.get(build);
 
   	//TODO: get Actions? How many Actions are performed? In which step the changelog is processed?
    //TODO: getProjectAction(build) doesnt work even if it shoudl arcording to the jenkins documentation
   	MingleBuildAction action = (MingleBuildAction) getProjectAction(build);
   	List<Integer> cardIds = action.getCardIds();
     Iterator<Integer> myListIterator = cardIds.iterator(); 
     String newDescription = "OLD DESCRIPTION HERE?<br/>\n\n";
 
     while (myListIterator.hasNext()) {
       int id = (int) myListIterator.next();
       //add formation here to string
       newDescription += service.getCardUrl(id)+"<br/>\\n\\n";
     }
     // 	result = build.getEnvironment(listener).expand(result);.
     listener.getLogger().println("Description set: " + newDescription);
     build.setDescription(newDescription);
     //
   }
 
  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

 }
