 package net.sourcewalker.svnnotify.notifier;
 
 import java.util.List;
 
 import net.sourcewalker.svnnotify.data.interfaces.INotifier;
 import net.sourcewalker.svnnotify.data.interfaces.IRepository;
 import net.sourcewalker.svnnotify.data.interfaces.IRevision;
 
 /**
  * @author Xperimental
  */
 public class NullNotifier implements INotifier {
 
     @Override
    public void reportUpdates(IRepository repository, List<IRevision> revisions) {
         // Do nothing.
     }
 
 }
