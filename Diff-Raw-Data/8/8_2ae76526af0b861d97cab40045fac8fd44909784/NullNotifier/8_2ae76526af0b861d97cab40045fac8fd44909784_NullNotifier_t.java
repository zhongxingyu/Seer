 package net.sourcewalker.svnnotify.notifier;
 
 import java.util.List;
 
 import net.sourcewalker.svnnotify.data.interfaces.INotifier;
 import net.sourcewalker.svnnotify.data.interfaces.IRepository;
 import net.sourcewalker.svnnotify.data.interfaces.IRevision;
 
 /**
 * This class contains a notifier implementation which simply does nothing. It
 * is intended to be used in the early development phase or in case the
 * repository information should only be updated but not displayed to the user.
 *
  * @author Xperimental
  */
 public class NullNotifier implements INotifier {
 
     @Override
    public void reportUpdates(final IRepository repository,
            final List<IRevision> revisions) {
         // Do nothing.
     }
 
 }
