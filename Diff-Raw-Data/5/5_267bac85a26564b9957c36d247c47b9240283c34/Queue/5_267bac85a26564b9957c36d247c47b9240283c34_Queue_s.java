 /*  
  *	CMISBox - Synchronize and share your files with your CMIS Repository
  *
  *	Copyright (C) 2011 - Andrea Agili 
  *  
  * 	CMISBox is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  CMISBox is distributed in the hope that it will be useful,
  *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with CMISBox.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package com.github.cmisbox.core;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.DelayQueue;
 import java.util.regex.Pattern;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.github.cmisbox.persistence.Storage;
 import com.github.cmisbox.persistence.StoredItem;
 import com.github.cmisbox.remote.CMISRepository;
 import com.github.cmisbox.remote.ChangeItem;
 import com.github.cmisbox.remote.Changes;
 import com.github.cmisbox.ui.UI;
 import com.github.cmisbox.ui.UI.Status;
 
 public class Queue implements Runnable {
 
 	private static Queue instance = new Queue();
 
 	public static Queue getInstance() {
 		return Queue.instance;
 	}
 
 	private boolean active = true;
 
 	// by default do not synch files starting with a dot
 	private Pattern filter = Pattern.compile("^\\..*");
 
 	private Thread thread;
 
 	private DelayQueue<LocalEvent> delayQueue = new DelayQueue<LocalEvent>();
 
 	private Log log;
 
 	private Queue() {
 		this.thread = new Thread(this, "Queue");
 		this.thread.start();
 		this.log = LogFactory.getLog(this.getClass());
 	}
 
 	public synchronized void add(LocalEvent localEvent) {
 		this.log.debug("Asked to queue" + localEvent);
 		if (!this.active) {
 			return;
 		}
 
 		if ((localEvent.getName() != null)
 				&& this.filter.pattern().matches(localEvent.getName())) {
 			this.log.debug("Filtered " + localEvent);
 			return;
 		}
 
 		if (this.delayQueue.contains(localEvent) || localEvent.isRename()) {
 			Iterator<LocalEvent> i = this.delayQueue.iterator();
 			while (i.hasNext()) {
 				LocalEvent queuedEvent = i.next();
 				if (queuedEvent.equals(localEvent)) {
 					localEvent.merge(queuedEvent);
 					i.remove();
 					this.log.debug("" + "Merged " + queuedEvent);
 
 				} else if (Config.getInstance().isMacOSX()
 						&& localEvent.isRename() && queuedEvent.isDelete()) {
 					if (localEvent.isParent(queuedEvent)) {
 						i.remove();
 					}
 				}
 			}
 		}
 
 		if (!(localEvent.isCreate() && localEvent.isDelete())) {
 			this.delayQueue.put(localEvent);
 			this.log.debug("Queued " + localEvent);
 		}
 	}
 
 	public Pattern getFilter() {
 		return this.filter;
 	}
 
 	private StoredItem getSingleItem(String path) throws Exception {
 		List<StoredItem> itemList = Storage.getInstance().findByPath(path);
 		if (itemList.size() == 1) {
 			return itemList.get(0);
 		} else {
 			throw new Exception(String.format(
 					"Expected one result in index: %s -> %s", path, itemList));
 		}
 	}
 
 	public void manageEvent(LocalEvent event) {
 		Log log = LogFactory.getLog(this.getClass());
 		log.debug("managing: " + event);
 
 		// any platform
 		// - a folder can be renamed before containing files are managed: on
 		// folder rename all children must be updated while still in queue;
 
 		// linux
 		// - if a file or folder is moved out of a watched folder it is reported
 		// as a rename to null (check if it's still there)
 
 		// mac osx
 		// - recursive folder operations (e.g. unzip an archive or move a folder
 		// inside a watched folder) are not reported, only root folder is
 		// reported as create
 		// - folder rename causes children to be notified as deleted (with old
 		// path)
 
 		try {
 			if (event.isSynch()) {
 				this.synchAllWatches();
 				return;
 			}
 
 			File f = new File(event.getFullFilename());
 			if (event.isCreate()) {
 				StoredItem item = this.getSingleItem(event.getLocalPath());
 
 				if ((item != null)
 						&& (item.getLocalModified().longValue() >= f
 								.lastModified())) {
 					return;
 				}
 				String parent = f.getParent().substring(
 						Config.getInstance().getWatchParent().length());
 
 				CmisObject obj = CMISRepository.getInstance().addChild(
 						this.getSingleItem(parent).getId(), f);
 				Storage.getInstance().add(f, obj);
 			} else if (event.isDelete()) {
 				StoredItem item = this.getSingleItem(event.getLocalPath());
 				if (f.exists()) {
 					throw new Exception(String.format(
 							"File %s reported to be deleted but stil exists",
 							f.getAbsolutePath()));
 				}
 				CMISRepository.getInstance().delete(item.getId());
 				Storage.getInstance().delete(item, true);
 			} else if (event.isModify()) {
 				if (f.isFile()) {
 					StoredItem item = this.getSingleItem(event.getLocalPath());
 
 					if (item.getLocalModified().longValue() < f.lastModified()) {
 
 						Document doc = CMISRepository.getInstance().update(
 								item, f);
 
 						Storage.getInstance().localUpdate(item, f, doc);
 					} else {
 						log.debug("file" + f + " modified in the past");
 					}
 
 				}
 			} else if (event.isRename()) {
 				StoredItem item = this.getSingleItem(event.getLocalPath());
 				CmisObject obj = CMISRepository.getInstance().rename(
 						item.getId(), f);
 				Storage.getInstance().localUpdate(item, f, obj);
 			}
 
 		} catch (Exception e) {
 			log.error(e);
 			if (log.isDebugEnabled()) {
 				e.printStackTrace();
 			}
 			if (UI.getInstance().isAvailable()) {
 				UI.getInstance().notify(e.toString());
 				UI.getInstance().setStatus(Status.KO);
 			}
 		}
 	}
 
 	private String resolvePath(Folder parent) throws Exception {
 		StoredItem item = Storage.getInstance().findById(parent.getId());
 		String path = File.separator + parent.getName();
 		while (item == null) {
			Folder ancestor = parent.getFolderParent();
 			if (ancestor == null) {
 				return null;
 			}
 			item = Storage.getInstance().findById(ancestor.getId());
 			if (item == null) {
 				path = File.separator + ancestor.getName() + path;
 			} else {
 				path = item.getPath() + path;
 			}
 		}
 
 		return Config.getInstance().getWatchParent() + path;
 	}
 
 	public void run() {
 		while (this.active) {
 			try {
 				this.manageEvent(this.delayQueue.take());
 				if (this.delayQueue.isEmpty()) {
 
 				}
 			} catch (InterruptedException e) {
 				LogFactory.getLog(this.getClass()).info(this, e);
 			}
 		}
 
 	}
 
 	public void setFilter(Pattern filter) {
 		this.filter = filter;
 	}
 
 	public void stop() {
 		this.active = false;
 		this.delayQueue.clear();
 		this.thread.interrupt();
 	}
 
 	private void synchAllWatches() throws Exception {
 		Storage storage = Storage.getInstance();
 		CMISRepository cmisRepository = CMISRepository.getInstance();
 		UI ui = UI.getInstance();
 		Config config = Config.getInstance();
 
 		if (ui.isAvailable()) {
 			ui.setStatus(Status.SYNCH);
 		}
 
 		List<String[]> updates = new ArrayList<String[]>();
 
 		Changes changes = cmisRepository
 				.getContentChanges(storage.getRootIds());
 
 		LinkedHashMap<String, File> downloadList = new LinkedHashMap<String, File>();
 
 		boolean errors = false;
 
 		for (ChangeItem item : changes.getEvents()) {
 			try {
 				if (item == null) {
 					continue;
 				}
 				String id = "workspace://SpacesStore/" + item.getId();
 				String type = item.getT();
 				StoredItem storedItem = storage.findById(id);
 				if (type.equals("D")) {
 					if (storedItem != null) {
 						File f = new File(config.getWatchParent()
 								+ storedItem.getPath());
 						f.delete();
 						storage.delete(storedItem, true);
 					}
 				} else if (type.equals("C") || type.equals("U")) {
 					CmisObject remoteObject = cmisRepository.findObject(id);
 					remoteObject.refresh();
 					if (remoteObject.getType().getBaseTypeId()
 							.equals(BaseTypeId.CMIS_FOLDER)) {
 						Folder folder = (Folder) remoteObject;
 						File newFile = new File(this.resolvePath(folder
 								.getFolderParent()), folder.getName());
 						if (storedItem == null) {
 							storage.add(newFile, folder, false);
 						} else {
 							if ((folder.getLastModificationDate()
 									.getTimeInMillis() > storedItem
 									.getRemoteModified())
 									&& !storedItem.getName().equals(
 											folder.getName())) {
 								if (new File(storedItem.getAbsolutePath())
 										.renameTo(newFile)) {
 									storage.localUpdate(storedItem, newFile,
 											folder);
 								} else {
 									if (ui.isAvailable()) {
 										ui.notify(Messages.renameError + " "
 												+ storedItem.getAbsolutePath()
 												+ " -> "
 												+ newFile.getAbsolutePath());
 									}
 									this.log.error("Unable to rename "
 											+ storedItem.getAbsolutePath()
 											+ " to "
 											+ newFile.getAbsolutePath());
 
 								}
 							}
 						}
 					} else {
 						Document document = (Document) remoteObject;
 						this.log.debug("preparing to update or create "
 								+ document.getName());
 						File newFile = new File(this.resolvePath(document
 								.getParents().get(0)), document.getName());
 						if (storedItem == null) {
 							downloadList.put(id, newFile);
 						} else {
 							File current = new File(
 									storedItem.getAbsolutePath());
 							if (storedItem.getLocalModified() < document
 									.getLastModificationDate()
 									.getTimeInMillis()) {
 								if (!current.getAbsolutePath().equals(
 										newFile.getAbsolutePath())) {
 									if (!current.renameTo(newFile)) {
 										if (ui.isAvailable()) {
 											ui.notify(Messages.renameError
 													+ " "
 													+ storedItem
 															.getAbsolutePath()
 													+ " -> "
 													+ newFile.getAbsolutePath());
 										}
 										this.log.error("Unable to rename "
 												+ storedItem.getAbsolutePath()
 												+ " to "
 												+ newFile.getAbsolutePath());
 									}
 								}
 								downloadList.put(id, newFile);
 							}
 						}
 					}
 				}
 			} catch (Exception e1) {
 				errors = true;
 				this.log.error("Error getting remote chahges for " + item, e1);
 			}
 		}
 
 		if (ui.isAvailable() && (downloadList.size() > 0)) {
 			ui.notify(Messages.downloading + " " + downloadList.size() + " "
 					+ Messages.files);
 		}
 		for (Entry<String, File> e : downloadList.entrySet()) {
 			try {
 				storage.deleteById(e.getKey());
 				e.getValue().delete();
 				cmisRepository.download(cmisRepository.getDocument(e.getKey()),
 						e.getValue());
 				storage.add(e.getValue(),
 						cmisRepository.getDocument(e.getKey()));
 			} catch (Exception e1) {
 				errors = true;
 				this.log.error("Error downloading " + e, e1);
 				if (ui.isAvailable()) {
 					ui.notify(Messages.errorDownloading + " " + e);
 				}
 			}
 		}
 
 		if (!errors) {
 			config.setChangeLogToken(changes.getToken());
 		}
 
 		if (ui.isAvailable()) {
 			ui.setStatus(Status.OK);
 			if (updates.size() == 1) {
 				ui.notify(updates.get(0)[0] + " " + Messages.updatedBy + " "
 						+ updates.get(0)[1]);
 			} else if (updates.size() > 1) {
 				ui.notify(Messages.updated + " " + updates.size()
 						+ Messages.files);
 			}
 
 		}
 	}
 }
