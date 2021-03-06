 package de.fu_berlin.inf.dpp.concurrent.management;
 
 /**
  * this manager class handles driver event and the appropriate documents.
  * Additional this class is managing exclusive lock for temporary single driver
  * actions.
  */
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IPath;
 
import de.fu_berlin.inf.dpp.Saros;
 import de.fu_berlin.inf.dpp.activities.EditorActivity;
 import de.fu_berlin.inf.dpp.activities.FileActivity;
 import de.fu_berlin.inf.dpp.activities.IActivity;
 import de.fu_berlin.inf.dpp.activities.RoleActivity;
 import de.fu_berlin.inf.dpp.concurrent.IDriverDocumentManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
 import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
 
 /**
  * 
  * @author orieger
  * 
  */
public class DriverDocumentManager implements IDriverDocumentManager,
	ISessionListener {
 
     private static Logger logger = Logger
 	    .getLogger(DriverDocumentManager.class);
 
     /* list of all active driver. */
     private final List<JID> activeDriver;
 
     /* list of documents with appropriate drivers. */
     private final HashMap<IPath, DriverDocument> documents;
 
     private static DriverDocumentManager manager;
 
     /**
      * private constructor for singleton pattern.
      */
     private DriverDocumentManager() {
 	this.activeDriver = new Vector<JID>();
 	this.documents = new HashMap<IPath, DriverDocument>();
	Saros.getDefault().getSessionManager().addSessionListener(this);
     }
 
     /**
      * get instance of this singleton object
      * 
      * @return instance of DriverDocumentManager
      */
     public static DriverDocumentManager getInstance() {
 	/* at first time, create new manager. */
 	if (DriverDocumentManager.manager == null) {
 	    DriverDocumentManager.manager = new DriverDocumentManager();
 	}
 	return DriverDocumentManager.manager;
     }
 
     /**
      * @param jid
      *            JID of the driver
      * @return true if driver exists in active driver list, false otherwise
      */
     public boolean isDriver(JID jid) {
 	if (jid != null) {
 	    return this.activeDriver.contains(jid);
 	} else {
 	    System.out.println("jid null");
 	}
 	return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.fu_berlin.inf.dpp.concurrent.IDriverDocumentManager#getDriverForDocument
      * (org.eclipse.core.runtime.IPath)
      */
     public List<JID> getDriverForDocument(IPath path) {
 	if (this.documents.containsKey(path)) {
 	    List<JID> drivers = this.documents.get(path).getActiveDriver();
 	    return drivers;
 	}
 	return new Vector<JID>();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.fu_berlin.inf.dpp.concurrent.IDriverManager#getActiveDriver()
      */
     public List<JID> getActiveDriver() {
 	return this.activeDriver;
     }
 
     public void addDriver(JID jid) {
 	// if (user.getUserRole() == UserRole.OBSERVER) {
 	// logger.error("User " + user.getJid() + " has not driver status! ");
 	// }
 	DriverDocumentManager.logger.debug("add driver for jid: " + jid);
 	if (!this.activeDriver.contains(jid)) {
 	    this.activeDriver.add(jid);
 	}
     }
 
     public void addDriverToDocument(IPath path, JID jid) {
 	addDriver(jid);
 
 	DriverDocumentManager.logger.debug("add activer driver " + jid
 		+ " to document " + path.lastSegment().toString());
 	/* add driver to new document. */
 	DriverDocument doc = this.documents.get(path);
 	if ((doc == null) || !this.documents.containsKey(path)) {
 	    DriverDocumentManager.logger.debug("New document creates for "
 		    + path.lastSegment().toString());
 	    /* create new instance of this documents. */
 	    doc = new DriverDocument(path);
 	    this.documents.put(path, doc);
 	}
 	if (!doc.isDriver(jid)) {
 	    doc.addDriver(jid);
 	}
     }
 
     public void removeDriver(JID jid) {
 	DriverDocumentManager.logger.debug("remove driver " + jid);
 
 	/* remove driver from all documents */
 	for (IPath path : this.documents.keySet()) {
 	    removeDriverFromDocument(path, jid);
 	}
 
 	this.activeDriver.remove(jid);
 
     }
 
     /**
      * remove driver from document and delete empty documents.
      * 
      * @param path
      *            of the document
      * @param jid
      *            removable driver
      */
     private void removeDriverFromDocument(IPath path, JID jid) {
 	DriverDocument doc = this.documents.get(path);
 	if (doc.isDriver(jid)) {
 	    doc.removeDriver(jid);
 	}
 	/* check for other driver or delete if no other driver exists. */
 	if (doc.noDriver()) {
 	    DriverDocumentManager.logger.debug("no driver exists for document "
 		    + path.lastSegment().toString()
 		    + ". Document delete from driver manager.");
 	    /* delete driver document. */
 	    this.documents.remove(doc.getEditor());
 	}
     }
 
     /**
      * new driver activity received and has to be managed.
      * 
      * @param activity
      */
     public void receiveActivity(IActivity activity) {
 	JID jid = new JID(activity.getSource());
 
 	/* if user is an active driver */
 	if (isDriver(jid)) {
 
 	    /* editor activities. */
 	    if (activity instanceof EditorActivity) {
 		EditorActivity edit = (EditorActivity) activity;
 
 		DriverDocumentManager.logger.debug("receive activity of " + jid
 			+ " for editor " + edit.getPath().lastSegment()
 			+ " and action " + edit.getType());
 
 		/* editor has activated. */
 		if (edit.getType() == EditorActivity.Type.Activated) {
 		    /* add driver to new document. */
 		    addDriverToDocument(edit.getPath(), jid);
 		}
 		/* editor has closed. */
 		if (edit.getType() == EditorActivity.Type.Closed) {
 		    /* remove driver */
 		    if (this.documents.containsKey(edit.getPath())) {
 
 			removeDriverFromDocument(edit.getPath(), jid);
 
 		    } else {
 			DriverDocumentManager.logger
 				.warn("No driver document exists for "
 					+ edit.getPath());
 		    }
 		}
 	    }
 	    if (activity instanceof RoleActivity) {
 
 	    }
 
 	    if (activity instanceof FileActivity) {
 		FileActivity file = (FileActivity) activity;
 		/* if file has been removed, delete appropriate driver document. */
 		if (file.getType() == FileActivity.Type.Removed) {
 		    this.documents.remove(file.getPath());
 		}
 	    }
 	} else {
 	    DriverDocumentManager.logger.debug("JID " + jid
 		    + " isn't an active driver.");
 	}
     }
 
     public void driverChanged(JID driver, boolean replicated) {
 	if (isDriver(driver)) {
 	    removeDriver(driver);
 	} else {
 	    addDriver(driver);
 	}
 
     }
 
     public void userJoined(JID user) {
 	// nothing to do
 
     }
 
     public void userLeft(JID user) {
 	if (isDriver(user)) {
 	    /* remove driver status. */
 	    removeDriver(user);
 	}
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.fu_berlin.inf.dpp.concurrent.IDriverManager#exclusiveDriver()
      */
     public boolean exclusiveDriver() {
 	boolean result = true;
 	if (this.activeDriver.size() > 1) {
 	    result = false;
 	}
 	return result;
     }
 
    public void invitationReceived(IIncomingInvitationProcess invitation) {
	// ignore

    }

    public void sessionEnded(ISharedProject session) {
	activeDriver.clear();

    }

    public void sessionStarted(ISharedProject session) {
	// ignore

    }

 }
