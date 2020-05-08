 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.users.groups;
 
 import org.apache.log4j.Logger;
 
 import org.openide.nodes.Node;
 import org.openide.util.HelpCtx;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CookieAction;
 import org.openide.windows.WindowManager;
 
 import java.util.Arrays;
 
 import javax.swing.JOptionPane;
 
 import de.cismet.cids.abf.domainserver.project.DomainserverContext;
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;
 import de.cismet.cids.abf.domainserver.project.users.UserContextCookie;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  1.7
  */
 public final class RemoveGroupMembershipAction extends CookieAction {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(RemoveGroupMembershipAction.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getName() {
         return NbBundle.getMessage(
                 RemoveGroupMembershipAction.class,
                 "RemoveGroupMembershipAction.getName().returnvalue"); // NOI18N
     }
 
     @Override
     protected String iconResource() {
         return DomainserverProject.IMAGE_FOLDER + "remove_user_from_group.png"; // NOI18N
     }
 
     @Override
     public HelpCtx getHelpCtx() {
         return HelpCtx.DEFAULT_HELP;
     }
 
     @Override
     protected boolean asynchronous() {
         return false;
     }
 
     @Override
     protected int mode() {
         return MODE_ALL;
     }
 
     @Override
     protected Class[] cookieClasses() {
         return new Class[] {
                 DomainserverContext.class,
                 UserContextCookie.class
             };
     }
 
     @Override
     protected void performAction(final Node[] nodes) {
         final int answer = JOptionPane.showConfirmDialog(
                 WindowManager.getDefault().getMainWindow(),
                 NbBundle.getMessage(
                     RemoveGroupMembershipAction.class,
                     "RemoveGroupMembershipAction.performAction(Node[]).JOptionPane.message"), // NOI18N
                 NbBundle.getMessage(
                     RemoveGroupMembershipAction.class,
                     "RemoveGroupMembershipAction.performAction(Node[]).JOptionPane.title"), // NOI18N
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE);
         if (answer == JOptionPane.YES_OPTION) {
             for (final Node n : nodes) {
                 final DomainserverProject project = n.getCookie(DomainserverContext.class).getDomainserverProject();
                 final Backend backend = project.getCidsDataObjectBackend();
                 final UserGroupNode ugn = n.getParentNode().getLookup().lookup(UserGroupNode.class);
                 final User usr = n.getCookie(UserContextCookie.class).getUser();
                 if (ugn != null) {
                     final UserGroup ug = ugn.getUserGroup();
                     ug.getUsers().remove(usr);
                    usr.getUserGroups().remove(ug);
                     try {
                         backend.store(ug);
                        backend.store(usr);
                     } catch (final RuntimeException e) {
                         LOG.error("could not store usergroup '" + ug                        // NOI18N
                                     + "' and hence user '" + usr                            // NOI18N
                                     + "' was not removed",                                  // NOI18N
                             e);
                        ErrorUtils.showErrorMessage("Could not store usergroup or user", "Store error", e);
                     }
                     project.getLookup().lookup(UserManagement.class).refreshGroups(Arrays.asList(ug));
                 } else {
                     LOG.warn("the usergroup the user '"                                     // NOI18N
                                 + usr + "' was supposed to be in a group which could not"   // NOI18N
                                 + " be found in lookup, nothing is done");                  // NOI18N
                 }
             }
         }
     }
 
     @Override
     protected boolean enable(final Node[] nodes) {
         if (!super.enable(nodes)) {
             return false;
         }
 
         for (final Node node : nodes) {
             final UserContextCookie ucc = node.getCookie(UserContextCookie.class);
             final DomainserverContext context = node.getCookie(DomainserverContext.class);
 
             if ((ucc == null) || (context == null)) {
                 return false;
             }
         }
 
         return true;
     }
 }
