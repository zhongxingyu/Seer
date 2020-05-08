 package de.fu_berlin.inf.dpp.ui.actions;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.services.IDisposable;
 import org.picocontainer.annotations.Inject;
 
 import de.fu_berlin.inf.dpp.SarosPluginContext;
 import de.fu_berlin.inf.dpp.User;
 import de.fu_berlin.inf.dpp.annotations.Component;
 import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager.SessionListener;
 import de.fu_berlin.inf.dpp.project.ISarosSession;
 import de.fu_berlin.inf.dpp.project.SarosSessionManager;
 import de.fu_berlin.inf.dpp.ui.SarosUI;
 import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
 import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
 import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
 import de.fu_berlin.inf.dpp.util.Utils;
 
 // TODO (for Remove User Mod): add necessary imports (only for Part C)
 
 /**
  * javadoc here
  */
 @Component(module = "action")
 // it's the JFace action
 public class RemoveFromSessionAction extends Action implements IDisposable {
 
     // necessary class members go here
     // logger
     private static final Logger log = Logger
         .getLogger(RemoveFromSessionAction.class);
     // sarosUI (injectable)
     @Inject
     SarosUI sarosui;
     // sessionListener that overrides sessionStarted and sessionEnded methods
     SessionListener sessionListener;
     // selectionListener that overrides selectionChanged method
     protected ISelectionListener selectionListener = new ISelectionListener() {
         public void selectionChanged(IWorkbenchPart part, ISelection selection) {
             updateEnablement();
         }
     };
     // saros session manager (injectable)
     @Inject 
     SarosSessionManager sessionManager;
 
     public RemoveFromSessionAction() {
         // call parent class constructor with message "Remove from Session"
         super("Remove from Session");
         // initialize this component within the saros plug-in context
         SarosPluginContext.initComponent(this);
         // set appropriate tool-tip text
         setToolTipText("Add a New Buddy");
         // initialize this component again
         SarosPluginContext.initComponent(this);
         // if the current saros session is not null
         ISarosSession sarosSession = sessionManager.getSarosSession();
         if (sarosSession != null) {
             // call the sessionListener's sessionStarted method with the current
             // saros session
             sessionListener.sessionStarted(sarosSession);
             // add the sessionListener member as a saros session listener
             sessionManager.addSarosSessionListener(sessionListener);
             // add the selectionListener member as a selection listener
             SelectionUtils.getSelectionService().addSelectionListener(
                 selectionListener);
             // update the enablement of this action
             updateEnablement();
         }
 
     }
 
     @Override
     public void run() {
         // run the following code in a safe asynchronous utility command
         Utils.runSafeAsync(log, new Runnable() {
             // in the run method of the utility command, do the
             // following:
             public void run() {
                 // get the selected list of participants
                 List<User> participants = null;
                 try {
                    participants = SelectionRetrieverFactory
                        .getSelectionRetriever(User.class).getSelection();

                 } catch (NullPointerException e) {
                     setEnabled(false);
                 } catch (Exception e) {
                     if (!PlatformUI.getWorkbench().isClosing())
                         log.error("Unexcepted error while updating enablement",
                             e);
                 }
                 // remove them from the saros session
                 CollaborationUtils.removeUsersFromSarosSession(sessionManager,
                     participants);
             }
         });
 
     }
 
     protected void updateEnablement() {
         // try to do the following:
         try {
 
             // get the selected list of participants
             List<User> participants = null;
             participants = SelectionRetrieverFactory.getSelectionRetriever(
                 User.class).getSelection();
             // get the current saros session
             ISarosSession sarosSession = sessionManager.getSarosSession();
             // if the saros session is not null and this computer is the host of
             // the session
 
             if (sarosSession != null && sarosSession.isHost()) {
                 // and the selected list of participants does not include the
                 // host then enable this action
                 if (!participants.contains(sarosSession.getHost())) {
                     setEnabled(true);
                     // otherwise disable it
                 } else {
                     setEnabled(false);
                 }
             }
 
             // catch exception when saros session is null
         } catch (NullPointerException e) {
             System.out.println(e.getStackTrace());
             // catch other exceptions
         } catch (Exception e) {
             log.warn(e.getMessage());
             // disable this action
             this.setEnabled(false);
             System.out.println(e.getStackTrace());
             if (!PlatformUI.getWorkbench().isClosing()) {
                 log.error("Unexpected errors");
             }
         } finally {
             // if the platform UI's workbench is not closing, log an error about
             // unexpected errors
 
         }
 
     }
 
     // @Override
     public void dispose() {
         // remove the selectionListener
         SelectionUtils.getSelectionService().removeSelectionListener(
             selectionListener);
         // remove the sessionListener
         sessionManager.removeSarosSessionListener(sessionListener);
     }
 
 }
