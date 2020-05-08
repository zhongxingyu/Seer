 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.serializeddatastore;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ISessionManager;
 import net.sf.jmoney.model2.Session;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.IWorkbenchWindow;
 
 /**
  * Provides the ISessionManager implementation.
  * All plug-ins that provide a datastore
  * implementation must provide an implementation of the
  * ISessionManager interface.
  */
 public class SessionManager implements ISessionManager {
 
 	private Session session = null;
 
 	private String fileDatastoreId = null;
 	
 	private IFileDatastore fileDatastore = null;
 	
     private File sessionFile = null;
 
     private boolean modified = false;
 
     private Map accountEntriesListsMap = new HashMap();
 	
 	/**
 	 * Construct the session manager.
 	 * <P>
 	 * Note that the session manager is constructed without a session. A session
 	 * must be created and set using the <code>setSession</code> method before
 	 * this session manager is usable. You may think that the session should be
 	 * passed to the constructor. However this is not done because then we would
 	 * have a 'chicken and egg' problem. The session object constructor needs
 	 * the SessionManager object. Hence the two part initialization of the
 	 * session manager.
 	 * 
 	 * @param fileDatastore
 	 * @param fileFormatId
 	 * @param sessionFile
 	 */
 	public SessionManager(String fileFormatId, IFileDatastore fileDatastore, File sessionFile) {
 		this.fileDatastoreId = fileFormatId;
 		this.fileDatastore = fileDatastore;
 		this.sessionFile = sessionFile;
 		this.session = null;
 	}
 	
 	/**
 	 * Used for two-part construction.
 	 */
 	public void setSession(Session session) {
 		this.session = session;
 	}
 	
 	public Session getSession() {
 		return session;
 	}
 	
     public File getFile() {
         return sessionFile;
     }
     
     public void setFile(File file) {
         this.sessionFile = file;
         
         // The brief description of this session contains the file name, so we must
         // fire a change so views that show this session description are updated.
         // FIXME: Title is not updated because this is commented out.
 /*
         fireEvent(
         	new ISessionChangeFirer() {
         		public void fire(SessionChangeListener listener) {
         			listener.sessionPropertyChange("briefDescription", null, getBriefDescription());
         		}
        		});
 */       		
     }
 
     private boolean isModified() {
         return modified;
     }
 
     /**
      * This plug-in needs to know if a session has been modified so
      * that it knows whether to save the session.  This method must
      * be called whenever the session is modified.
      */
     void setModified() {
         modified = true;
     }
 
 	boolean requestSave(IWorkbenchWindow window) {
 		String title = SerializedDatastorePlugin.getResourceString("MainFrame.saveOldSessionTitle");
 		String question =
 			SerializedDatastorePlugin.getResourceString("MainFrame.saveOldSessionQuestion");
 		MessageDialog dialog = new MessageDialog(
 				window.getShell(),
 				title,
 				null,	// accept the default window icon
 				question, 
 				MessageDialog.QUESTION, 
 				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 
 				2); 	// CANCEL is the default
 		
 		int answer = dialog.open();
 		switch (answer) {
 		case 0: // YES
 			saveSession(window);
 			return true;
 		case 1: // NO
 			return true;
 		case 2: // CANCEL
 			return false;
 		default:
 			throw new RuntimeException("bad switch value");
 		}
 	}
 	
 	/**
 	 * Saves the session in the selected file.
 	 */
 	public void saveSession(IWorkbenchWindow window) {
 		if (getFile() == null) {
 			saveSessionAs(window);
 		} else {
 			fileDatastore.writeSession(this, getFile(), window);
 	        modified = false;
 		}
 	}
 	
 	/**
 	 * Prompt the user for a file name and save the session
 	 * to that file.  The user is prompted for a file even
 	 * if a file is already set in this session manager.
 	 *
 	 * @param window the workbench window to be used for UI
 	 */
     public void saveSessionAs(IWorkbenchWindow window) {
     	File newSessionFile = obtainFileName(window);
     	if (newSessionFile != null) {
     		String fileName = newSessionFile.getName();
             IConfigurationElement elements[] = SerializedDatastorePlugin.getElements(fileName);
             
             // TODO: It is possible that multiple plug-ins may
             // use the same file extension.  The only solution to
             // this is to ask the user which format to use.
 
             // For time being, we simply use the first entry.
 			try {
 				fileDatastore = (IFileDatastore)elements[0].createExecutableExtension("class");
 				fileDatastoreId = elements[0].getDeclaringExtension().getNamespace() + '.' + elements[0].getAttribute("id");
 			} catch (CoreException e) {
 				e.printStackTrace();
 				throw new RuntimeException("internal error");
 			}
     		
 			// Write the file and then set the session file to the new file.
 			// Note that we do not set the new session file until the file is
 			// successfully written.  If the file cannot be written to the new
 			// file then we must leave the old file as the current file.
 			fileDatastore.writeSession(this, newSessionFile, window);
 			this.sessionFile = newSessionFile;
 			
 	        modified = false;
     	}
     }
 	
 	/**
 	 * Obtain the file name if a file is not already associated with this session.
 	 *
 	 * @return true if a file name was obtained from the user,
 	 *      false if no file name was obtained.
 	 */
 	public File obtainFileName(IWorkbenchWindow window) {
		FileDialog dialog = new FileDialog(window.getShell());
 		dialog.setFilterExtensions(SerializedDatastorePlugin.getFilterExtensions());
 		dialog.setFilterNames(SerializedDatastorePlugin.getFilterNames());
 		String fileName = dialog.open();
 		
 		if (fileName != null) {
 			File file = new File(fileName);
 			if (dontOverwrite(file, window))
 				return null;
 			
 			return file;
 		}
 		return null;
 	}
 	
 	private boolean dontOverwrite(File file, IWorkbenchWindow window) {
 		if (file.exists()) {
 			String question = SerializedDatastorePlugin.getResourceString("MainFrame.OverwriteExistingFile")
 			+ " "
 			+ file.getPath()
 			+ "?";
 			String title = SerializedDatastorePlugin.getResourceString("MainFrame.FileExists");
 			
 			boolean answer = MessageDialog.openQuestion(
 					window.getShell(),
 					title,
 					question);
 			return !answer;
 		} else {
 			return false;
 		}
 	}
 	
     public boolean canClose(IWorkbenchWindow window) {
         if (isModified()) {
             return requestSave(window);
         } else {
             return true;
         }
     }
 
     public void close() {
         // There is nothing to do here.  No files, connections or other resources
         // are kept open so there is nothing to close.
     }
     
     public String getBriefDescription() {
         if (sessionFile == null) {
             return null;
         } else {
             return sessionFile.getName();
         }
     }
 
 	private IPersistableElement persistableElement 
 	= new IPersistableElement() {
 		public String getFactoryId() {
 			return "net.sf.jmoney.serializeddatastore.SessionFactory";
 		}
 		public void saveState(IMemento memento) {
 			// If no session file is set then the session has never been saved.
 			// Although the canClose method will have been called prior to this
 			// during the shutdown process, it is possible that the user answered
 			// 'no' when asked if the session should be saved.  We write no file name
 			// in this situation and the code to re-create the session will, in this case,
 			// re-create an empty session.
 			if (sessionFile != null) {
 				memento.putString("fileFormatId", fileDatastoreId);
 				memento.putString("fileName", sessionFile.getPath());
 			}
 		}
 	};
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (adapter == IPersistableElement.class) {
 			return persistableElement;
 		}
 		return null;
 	}
 
 	/**
 	 * @param account
 	 * @param entry
 	 */
 	public void addEntryToList(Account account, Entry entry) {
 		Collection accountEntriesList = (Collection)accountEntriesListsMap.get(account);
 		accountEntriesList.add(entry);
 	}
 
 	/**
 	 * @param account
 	 * @param entry
 	 */
 	public void removeEntryFromList(Account account, Entry entry) {
 		Collection accountEntriesList = (Collection)accountEntriesListsMap.get(account);
 		accountEntriesList.remove(entry);
 	}
 
 	/**
 	 * @param account
 	 */
 	public void addAccountList(Account account) {
 		JMoneyPlugin.myAssert(!accountEntriesListsMap.containsKey(account));
 		accountEntriesListsMap.put(account, new Vector());
 	}
 
 	/**
 	 * @param account
 	 */
 	public void removeAccountList(Account account) {
 		JMoneyPlugin.myAssert(accountEntriesListsMap.containsKey(account));
 		accountEntriesListsMap.remove(account);
 	}
 
 	public boolean hasEntries(Account account) {
 		Collection entriesList = (Collection)accountEntriesListsMap.get(account);
 		JMoneyPlugin.myAssert(entriesList != null);
 		return !entriesList.isEmpty();
 	}
 
 	public Collection getEntries(Account account) {
 		Collection entriesList = (Collection)accountEntriesListsMap.get(account);
 		JMoneyPlugin.myAssert(entriesList != null);
 		return Collections.unmodifiableCollection(entriesList);
 	}
 
 	public void startTransaction() {
 		// Nothing to do
 	}
 
 	public void commitTransaction() {
 		// Nothing to do
 	}
 }
