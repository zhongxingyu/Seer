 /*
  * DPP - Serious Distributed Pair Programming
  * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
  * (c) Riad Djemili - 2006
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 1, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package de.fu_berlin.inf.dpp.project;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.ui.progress.IProgressConstants;
 import org.jivesoftware.smack.Connection;
 import org.joda.time.DateTime;
 import org.picocontainer.annotations.Inject;
 
 import de.fu_berlin.inf.dpp.FileList;
 import de.fu_berlin.inf.dpp.Saros;
 import de.fu_berlin.inf.dpp.SarosContext;
 import de.fu_berlin.inf.dpp.User;
 import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
 import de.fu_berlin.inf.dpp.annotations.Component;
 import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
 import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
 import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
 import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
 import de.fu_berlin.inf.dpp.invitation.OutgoingSessionNegotiation;
 import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
 import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
 import de.fu_berlin.inf.dpp.net.ConnectionState;
 import de.fu_berlin.inf.dpp.net.IConnectionListener;
 import de.fu_berlin.inf.dpp.net.JID;
 import de.fu_berlin.inf.dpp.net.SarosNet;
 import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
 import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
 import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
 import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
 import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
 import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
 import de.fu_berlin.inf.dpp.project.internal.SarosSession;
 import de.fu_berlin.inf.dpp.ui.ImageManager;
 import de.fu_berlin.inf.dpp.ui.SarosUI;
 import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
 import de.fu_berlin.inf.dpp.ui.views.SarosView;
 import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;
 
 /**
  * The SessionManager is responsible for initiating new Saros sessions and for
  * reacting to invitations. The user can be only part of one session at most.
  * 
  * @author rdjemili
  */
 @Component(module = "core")
 public class SarosSessionManager implements ISarosSessionManager {
 
     private static final Logger log = Logger
         .getLogger(SarosSessionManager.class.getName());
 
     private static final Random SESSION_ID_GENERATOR = new Random();
 
     private static final long LOCK_TIMEOUT = 10000L;
 
     private static final long NEGOTIATION_PROCESS_TIMEOUT = 10000L;
 
     @Inject
     private SarosSessionObservable sarosSessionObservable;
 
     @Inject
     private XMPPTransmitter transmitter;
 
     @Inject
     private SessionIDObservable sessionID;
 
     @Inject
     private PreferenceUtils preferenceUtils;
 
     @Inject
     private SarosContext sarosContext;
 
     @Inject
     private SarosUI sarosUI;
 
     @Inject
     private InvitationProcessObservable currentSessionNegotiations;
 
     @Inject
     private ProjectNegotiationObservable currentProjectNeogtiations;
 
     private SarosNet sarosNet;
 
     private final List<ISarosSessionListener> sarosSessionListeners = new CopyOnWriteArrayList<ISarosSessionListener>();
 
     private final Lock startStopSessionLock = new ReentrantLock();
 
     private volatile boolean sessionStartup = false;
 
     private final IConnectionListener listener = new IConnectionListener() {
         @Override
         public void connectionStateChanged(Connection connection,
             ConnectionState state) {
 
             if (state == ConnectionState.DISCONNECTING) {
                 stopSarosSession();
             }
         }
     };
 
     public SarosSessionManager(SarosNet sarosNet) {
         this.sarosNet = sarosNet;
         this.sarosNet.addListener(listener);
     }
 
     /**
      * @JTourBusStop 3, Invitation Process:
      * 
      *               This class manages the current Saros session.
      * 
      *               Saros makes a distinction between a session and a shared
      *               project. A session is an on-line collaboration between
      *               users which allows users to carry out activities. The main
      *               activity is to share projects. Hence, before you share a
      *               project, a session has to be started and all users added to
      *               it.
      * 
      *               (At the moment, this separation is invisible to the user.
      *               He/she must share a project in order to start a session.)
      * 
      */
     @Override
     public void startSession(
         final Map<IProject, List<IResource>> projectResourcesMapping) {
 
         try {
             if (!startStopSessionLock.tryLock(LOCK_TIMEOUT,
                 TimeUnit.MILLISECONDS)) {
                 log.warn("could not start a new session because another operation still tries to start or stop a session");
                 return;
             }
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             return;
         }
 
         try {
             if (sarosSessionObservable.getValue() != null) {
                 log.warn("could not start a new session because a session has already been started");
                 return;
             }
 
             sessionStartup = true;
 
             sessionID.setValue(String.valueOf(SESSION_ID_GENERATOR
                 .nextInt(Integer.MAX_VALUE)));
 
             final SarosSession sarosSession = new SarosSession(
                 preferenceUtils.getFavoriteColorID(), new DateTime(),
                 sarosContext);
 
             sarosSessionObservable.setValue(sarosSession);
 
             sessionStarting(sarosSession);
             sarosSession.start();
             sessionStarted(sarosSession);
 
             for (Entry<IProject, List<IResource>> mapEntry : projectResourcesMapping
                 .entrySet()) {
 
                 IProject project = mapEntry.getKey();
                 List<IResource> resourcesList = mapEntry.getValue();
 
                 if (!project.isOpen()) {
                     try {
                         project.open(null);
                     } catch (CoreException e) {
                         log.debug("an error occur while opening project: "
                             + project.getName(), e);
                         continue;
                     }
                 }
 
                 String projectID = String.valueOf(SESSION_ID_GENERATOR
                     .nextInt(Integer.MAX_VALUE));
 
                 sarosSession.addSharedResources(project, projectID,
                     resourcesList);
 
                 projectAdded(projectID);
             }
 
             log.info("session started");
         } finally {
             sessionStartup = false;
             startStopSessionLock.unlock();
         }
     }
 
     /**
      * {@inheritDoc}
      */
 
     // FIXME offer a startSession method for the client and host !
     @Override
     public ISarosSession joinSession(JID host, int colorID,
         DateTime sessionStart, JID inviter, int inviterColorID) {
 
         assert getSarosSession() == null;
 
         SarosSession sarosSession = new SarosSession(host, colorID,
             sessionStart, sarosContext, inviter, inviterColorID);
 
         sarosSessionObservable.setValue(sarosSession);
 
         log.info("joined uninitialized Saros session");
 
         return sarosSession;
     }
 
     /**
      * @nonSWT
      */
     @Override
     public void stopSarosSession() {
 
         assert !SWTUtils.isSWT() : "stopSarosSession must not be called from SWT";
 
         try {
             if (!startStopSessionLock.tryLock(LOCK_TIMEOUT,
                 TimeUnit.MILLISECONDS)) {
                 log.warn("could not stop the current session because another operation still tries to start or stop a session");
                 return;
             }
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             return;
         }
 
         try {
 
             SarosSession sarosSession = (SarosSession) sarosSessionObservable
                 .getValue();
 
             if (sarosSession == null) {
                 sessionID.setValue(SessionIDObservable.NOT_IN_SESSION);
                 return;
             }
 
             if (sessionStartup)
                 throw new IllegalStateException(
                     "cannot stop the session from the same thread context that is currently about to start the session: "
                         + Thread.currentThread().getName());
 
             log.debug("terminating all running negotiation processes");
 
             if (!terminateNegotiationProcesses())
                 log.warn("there are still running negotiation processes");
 
             sessionEnding(sarosSession);
 
             // FIXME move to session
             transmitter.sendLeaveMessage(sarosSession);
 
             log.debug("Leave message sent.");
 
             try {
                 sarosSession.stop();
             } catch (RuntimeException e) {
                 log.error("Error stopping project: ", e);
             }
 
             sarosSessionObservable.setValue(null);
 
             sessionEnded(sarosSession);
 
             sessionID.setValue(SessionIDObservable.NOT_IN_SESSION);
 
             log.info("session stopped");
         } finally {
             startStopSessionLock.unlock();
         }
     }
 
     /**
      * This method and the sarosSessionObservable are dangerous to use. The
      * session might be in the process of being destroyed while you call this
      * method. The caller needs to save the returned value to a local variable
      * and do a null check. For new code you should consider being scoped by the
      * SarosSession and get the SarosSession in the constructor.
      * 
      * @deprecated Error prone method, which produces NPE if not handled
      *             correctly. Will soon get removed.
      */
     @Override
     @Deprecated
     public ISarosSession getSarosSession() {
         return sarosSessionObservable.getValue();
     }
 
     @Override
     public void invitationReceived(JID from, String sessionID,
         String invitationID, DateTime sessionStart, VersionInfo versionInfo,
         String description) {
 
         if (!startStopSessionLock.tryLock()) {
             log.warn("could not accept invitation because the current session is about to stop");
             return;
         }
 
         IncomingSessionNegotiation process;
 
         try {
             /*
              * Side effect ! Setting the sessionID will reject further
              * invitation requests
              */
 
             this.sessionID.setValue(sessionID);
 
             // side effect in InvitationProcessObservable
             process = new IncomingSessionNegotiation(this, from, versionInfo,
                 sessionStart, invitationID, description, sarosContext);
         } finally {
             startStopSessionLock.unlock();
         }
 
         process.acknowledgeInvitation();
         sarosUI.showIncomingInvitationUI(process, true);
     }
 
     /**
      * This method is called when a new project was added to the session
      * 
      * @param from
      *            The one who added the project.
      * @param projectInfos
      *            what projects where added ({@link FileList}, projectName etc.)
      *            see: {@link ProjectExchangeInfo}
      * @param processID
      *            ID of the exchanging process
      */
     @Override
     public void incomingProjectReceived(JID from,
         List<ProjectExchangeInfo> projectInfos, String processID) {
 
         if (!startStopSessionLock.tryLock()) {
             log.warn("could not accept project negotation because the current session is about to stop");
             return;
         }
 
         IncomingProjectNegotiation process;
 
         try {
             // side effect in ProjectNegotiationObservable
             process = new IncomingProjectNegotiation(getSarosSession(), from,
                 processID, projectInfos, sarosContext);
         } finally {
             startStopSessionLock.unlock();
         }
 
         sarosUI.showIncomingProjectUI(process);
     }
 
     @Override
     public void invite(JID toInvite, String description) {
         ISarosSession sarosSession = sarosSessionObservable.getValue();
 
         if (!startStopSessionLock.tryLock()) {
             log.warn("could not start an invitation because the current session is about to stop");
             return;
         }
 
         OutgoingSessionNegotiation result;
 
         try {
             // side effect in InvitationProcessObservable
             result = new OutgoingSessionNegotiation(toInvite, sarosSession,
                 description, sarosContext);
         } finally {
             startStopSessionLock.unlock();
         }
 
         OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
             result);
 
         outgoingInvitationJob.setPriority(Job.SHORT);
         outgoingInvitationJob.schedule();
     }
 
     @Override
     public void invite(Collection<JID> jidsToInvite, String description) {
         for (JID jid : jidsToInvite)
             invite(jid, description);
     }
 
     /**
      * 
      * OutgoingInvitationJob wraps the instance of
      * {@link OutgoingSessionNegotiation} and cares about handling the
      * exceptions like local or remote cancellation.
      * 
      * It notifies the user about the progress using the Eclipse Jobs API and
      * interrupts the process if the session closes.
      * 
      */
     private class OutgoingInvitationJob extends Job {
 
         private OutgoingSessionNegotiation process;
         private String peer;
 
         public OutgoingInvitationJob(OutgoingSessionNegotiation process) {
             super(MessageFormat.format(
                 Messages.SarosSessionManager_inviting_user,
                 User.getHumanReadableName(sarosNet, process.getPeer())));
             this.process = process;
             this.peer = process.getPeer().getBase();
 
             setUser(true);
             setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
             setProperty(IProgressConstants.ICON_PROPERTY,
                 ImageManager
                     .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
         }
 
         @Override
         protected IStatus run(IProgressMonitor monitor) {
             try {
                 InvitationProcess.Status status = process.start(monitor);
 
                 switch (status) {
                 case CANCEL:
                     return Status.CANCEL_STATUS;
                 case ERROR:
                     return new Status(IStatus.ERROR, Saros.SAROS,
                         process.getErrorMessage());
                 case OK:
                     break;
                 case REMOTE_CANCEL:
                     SarosView
                         .showNotification(
                             Messages.SarosSessionManager_canceled_invitation,
                             MessageFormat
                                 .format(
                                     Messages.SarosSessionManager_canceled_invitation_text,
                                     peer));
 
                     return new Status(
                         IStatus.CANCEL,
                         Saros.SAROS,
                         MessageFormat
                             .format(
                                 Messages.SarosSessionManager_canceled_invitation_text,
                                 peer));
 
                 case REMOTE_ERROR:
                     SarosView
                         .showNotification(
                             Messages.SarosSessionManager_error_during_invitation,
                             MessageFormat
                                 .format(
                                     Messages.SarosSessionManager_error_during_invitation_text,
                                     peer, process.getErrorMessage()));
 
                     return new Status(
                         IStatus.ERROR,
                         Saros.SAROS,
                         MessageFormat
                             .format(
                                 Messages.SarosSessionManager_error_during_invitation_text,
                                 peer, process.getErrorMessage()));
                 }
             } catch (Exception e) {
                 log.error("This exception is not expected here: ", e);
                 return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);
 
             }
 
             startSharingProjects(process.getPeer());
 
             return Status.OK_STATUS;
         }
     }
 
     /**
      * Adds project resources to an existing session.
      * 
      * @param projectResourcesMapping
      * 
      */
     @Override
     public void addResourcesToSession(
         Map<IProject, List<IResource>> projectResourcesMapping) {
 
         ISarosSession session = getSarosSession();
 
         if (session == null) {
             log.warn("could not add resources because there is no active session");
             return;
         }
 
         /*
          * TODO: there are race conditions, USER A restricts USER B to read-only
          * while this code is executed
          */
 
         if (!session.hasWriteAccess()) {
             log.error("current local user has not enough privileges to add resources to the current session");
             return;
         }
 
         List<IProject> projectsToShare = new ArrayList<IProject>();
 
         for (Entry<IProject, List<IResource>> mapEntry : projectResourcesMapping
             .entrySet()) {
             IProject project = mapEntry.getKey();
             List<IResource> resourcesList = mapEntry.getValue();
 
             if (!project.isOpen()) {
                 try {
                     project.open(null);
                 } catch (CoreException e1) {
                     log.debug("An error occur while opening project", e1);
                     continue;
                 }
             }
 
             // side effect: non shared projects are always partial -.-
             if (!session.isCompletelyShared(project)) {
                String projectID = String.valueOf(SESSION_ID_GENERATOR
                    .nextInt(Integer.MAX_VALUE));
                 session.addSharedResources(project, projectID, resourcesList);
                 projectAdded(projectID);
                 projectsToShare.add(project);
             }
         }
 
         if (projectsToShare.isEmpty()) {
             log.warn("skipping project negotitation because no new projects were added to the current session");
             return;
         }
 
         if (!startStopSessionLock.tryLock()) {
             log.warn("could not start a project negotiation because the current session is about to stop");
             return;
         }
 
         try {
             for (User user : session.getRemoteUsers()) {
 
                 // side effect in ProjectNegotiationObservable
                 OutgoingProjectNegotiation out = new OutgoingProjectNegotiation(
                     user.getJID(), session, projectsToShare, sarosContext);
 
                 OutgoingProjectJob job = new OutgoingProjectJob(out);
                 job.setPriority(Job.SHORT);
                 job.schedule();
             }
         } finally {
             startStopSessionLock.unlock();
         }
     }
 
     @Override
     public void startSharingProjects(JID user) {
 
         ISarosSession session = getSarosSession();
 
         if (session == null) {
             /*
              * as this currently only called by the OutgoingSessionNegotiation
              * job just silently return
              */
             log.error("cannot share projects when no session is running");
             return;
         }
 
         /*
          * this can trigger a ConcurrentModification exception as the
          * SarosProjectMapper is completely broken
          */
         List<IProject> currentSharedProjects = new ArrayList<IProject>(
             session.getProjects());
 
         if (currentSharedProjects.isEmpty())
             return;
 
         if (!startStopSessionLock.tryLock()) {
             log.warn("could not start a project negotiation because the current session is about to stop");
             return;
         }
 
         OutgoingProjectNegotiation out;
         try {
             // side effect in ProjectNegotiationObservable
             out = new OutgoingProjectNegotiation(user, session,
                 currentSharedProjects, sarosContext);
         } finally {
             startStopSessionLock.unlock();
         }
         OutgoingProjectJob job = new OutgoingProjectJob(out);
         job.setPriority(Job.SHORT);
         job.schedule();
 
     }
 
     private class OutgoingProjectJob extends Job {
 
         private OutgoingProjectNegotiation process;
         private String peer;
 
         public OutgoingProjectJob(
             OutgoingProjectNegotiation outgoingProjectNegotiation) {
             super(Messages.SarosSessionManager_sharing_project);
             process = outgoingProjectNegotiation;
             peer = process.getPeer().getBase();
 
             setUser(true);
             setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
             setProperty(IProgressConstants.ICON_PROPERTY,
                 ImageManager.getImageDescriptor("/icons/invites.png"));
         }
 
         @Override
         protected IStatus run(IProgressMonitor monitor) {
             try {
                 ProjectNegotiation.Status status = process.start(monitor);
                 String peerName = User.getHumanReadableName(sarosNet, new JID(
                     peer));
 
                 String message;
 
                 switch (status) {
                 case CANCEL:
                     return Status.CANCEL_STATUS;
                 case ERROR:
                     return new Status(IStatus.ERROR, Saros.SAROS,
                         process.getErrorMessage());
                 case OK:
                     break;
                 case REMOTE_CANCEL:
                     message = MessageFormat
                         .format(
                             Messages.SarosSessionManager_project_sharing_cancelled_text,
                             peerName);
 
                     return new Status(IStatus.ERROR, Saros.SAROS, message);
 
                 case REMOTE_ERROR:
                     message = MessageFormat
                         .format(
                             Messages.SarosSessionManager_sharing_project_cancelled_remotely,
                             peerName, process.getErrorMessage());
                     SarosView
                         .showNotification(
                             Messages.SarosSessionManager_sharing_project_cancelled_remotely_text,
                             message);
 
                     return new Status(IStatus.ERROR, Saros.SAROS, message);
                 }
             } catch (Exception e) {
                 log.error("This exception is not expected here: ", e);
                 return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);
 
             }
 
             return Status.OK_STATUS;
         }
     }
 
     @Override
     public void addSarosSessionListener(ISarosSessionListener listener) {
         sarosSessionListeners.add(listener);
     }
 
     @Override
     public void removeSarosSessionListener(ISarosSessionListener listener) {
         sarosSessionListeners.remove(listener);
     }
 
     @Override
     public void preIncomingInvitationCompleted(IProgressMonitor monitor) {
         try {
             for (ISarosSessionListener sarosSessionListener : sarosSessionListeners) {
                 sarosSessionListener.preIncomingInvitationCompleted(monitor);
             }
         } catch (RuntimeException e) {
             log.error("Internal error in notifying listener"
                 + " of an incoming invitation: ", e);
         }
     }
 
     @Override
     public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
         User user) {
         try {
             for (ISarosSessionListener sarosSessionListener : sarosSessionListeners) {
                 sarosSessionListener.postOutgoingInvitationCompleted(monitor,
                     user);
             }
         } catch (RuntimeException e) {
             log.error("Internal error in notifying listener"
                 + " of an outgoing invitation: ", e);
         }
     }
 
     @Override
     public void sessionStarting(ISarosSession sarosSession) {
         try {
             for (ISarosSessionListener sarosSessionListener : sarosSessionListeners) {
                 sarosSessionListener.sessionStarting(sarosSession);
             }
         } catch (RuntimeException e) {
             log.error("error in notifying listener of session starting: ", e);
         }
     }
 
     @Override
     public void sessionStarted(ISarosSession sarosSession) {
         for (ISarosSessionListener sarosSessionListener : sarosSessionListeners) {
             try {
                 sarosSessionListener.sessionStarted(sarosSession);
             } catch (RuntimeException e) {
                 log.error("error in notifying listener of session start: ", e);
             }
         }
     }
 
     private void sessionEnding(ISarosSession sarosSession) {
         for (ISarosSessionListener saroSessionListener : sarosSessionListeners) {
             try {
                 saroSessionListener.sessionEnding(sarosSession);
             } catch (RuntimeException e) {
                 log.error("error in notifying listener of session ending: ", e);
             }
         }
     }
 
     private void sessionEnded(ISarosSession sarosSession) {
         for (ISarosSessionListener listener : sarosSessionListeners) {
             try {
                 listener.sessionEnded(sarosSession);
             } catch (RuntimeException e) {
                 log.error("error in notifying listener of session end: ", e);
             }
         }
     }
 
     @Override
     public void projectAdded(String projectID) {
         for (ISarosSessionListener listener : sarosSessionListeners) {
             try {
                 listener.projectAdded(projectID);
             } catch (RuntimeException e) {
                 log.error("error in notifying listener of an added project: ",
                     e);
             }
         }
     }
 
     private boolean terminateNegotiationProcesses() {
 
         for (InvitationProcess process : currentSessionNegotiations
             .getProcesses()) {
             process.localCancel(null, CancelOption.NOTIFY_PEER);
         }
 
         for (ProjectNegotiation process : currentProjectNeogtiations
             .getProcesses().values())
             process.localCancel(null, CancelOption.NOTIFY_PEER);
 
         log.trace("waiting for all invitation and project negotiation processes to terminate");
 
         long startTime = System.currentTimeMillis();
 
         boolean terminated = false;
 
         while (System.currentTimeMillis() - startTime < NEGOTIATION_PROCESS_TIMEOUT) {
             if (currentSessionNegotiations.getProcesses().isEmpty()
                 && currentProjectNeogtiations.getProcesses().isEmpty()) {
                 terminated = true;
                 break;
             }
 
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 break;
             }
         }
 
         return terminated;
     }
 }
