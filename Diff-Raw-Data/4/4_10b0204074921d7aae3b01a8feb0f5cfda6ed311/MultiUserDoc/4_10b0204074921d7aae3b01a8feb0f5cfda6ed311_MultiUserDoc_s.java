 package org.vaadin.mideaas.editor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.commons.io.FileUtils;
 import org.vaadin.aceeditor.client.AceDoc;
 import org.vaadin.mideaas.editor.DocDiffMediator.Guard;
 
 public class MultiUserDoc implements SharedDoc.Listener {
 	
 	public interface DifferingChangedListener {
 		public void differingChanged(Map<EditorUser, DocDifference> diffs);
 	}
 	
 	private final Guard guard;
 	private final File saveBaseTo;
 	private final SharedDoc base;
 	private final HashMap<EditorUser, UserDoc> userDocs
 		= new HashMap<EditorUser, UserDoc>();
 	
 	private final CopyOnWriteArrayList<DifferingChangedListener> dcListeners =
 			new CopyOnWriteArrayList<DifferingChangedListener>();
 	
 	// ...
 	private final Timer baseChangeTimer = new Timer();
 	private boolean fireScheduled = false;
 		
 	public MultiUserDoc(AceDoc initial, File saveBaseTo,  Guard guard) {
 		this.saveBaseTo = saveBaseTo;
 		this.guard = guard;
 		base = new SharedDoc(initial);
 		base.addListener(this);
 	}
 	
 	public SharedDoc getBase() {
 		return base;
 	}
 	
 	public synchronized UserDoc getUserDoc(EditorUser user) {
 		UserDoc ud = userDocs.get(user);
 		if (ud==null) {
 			ud = createUserDoc(user);
 			userDocs.put(user, ud);
 		}
 		return ud;
 	}
 	
 	public synchronized void removeUserDoc(EditorUser user) {
 		UserDoc ud = userDocs.remove(user);
 		if (ud!=null) {
 			ud.getDoc().removeListener(this);
 			ud.getMed().detach();
 		}
 	}
 	
 	public synchronized Map<EditorUser, DocDifference> getDifferences() {
 		HashMap<EditorUser, DocDifference> diffs = new HashMap<EditorUser, DocDifference>();
 		for (UserDoc ud : userDocs.values()) {
 			DocDifference dd = ud.getDiff();
 			if (dd.isChanged()) {
 				diffs.put(ud.getUser(), dd);
 			}
 		}
 		return diffs;
 	}
 	
 	private UserDoc createUserDoc(EditorUser user) {
 		SharedDoc doc = new SharedDoc(getBase().getDoc());
 		DocDiffMediator med = new DocDiffMediator(base, doc);
 		med.setUpwardsGuard(guard);
 		UserDoc ud = new UserDoc(user, doc, med, base);
 		doc.addListener(this);
 		return ud;
 	}
 
 	@Override
 	public void changed() {
 		// Delaying a bit. Not acting on each change, only after a while.
 		// This is a bit so so...
 		synchronized (baseChangeTimer) {
 			if (fireScheduled) {
 				return;
 			}
 			baseChangeTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					synchronized (baseChangeTimer) {
						saveBaseToDisk();
 						fireDifferingChanged(getDifferences());
 						fireScheduled = false;
 					}
 				}
 			}, 400);
 			fireScheduled = true;
 		}
 	}
 	
 	protected void saveBaseToDisk() {
 		try {
 			FileUtils.write(saveBaseTo, getBaseText());
 		} catch (IOException e) {
 			System.err.println("WARNING: could not save to "+saveBaseTo);
 		}
 	}
 
 	public synchronized void addDifferingChangedListener(DifferingChangedListener li) {
 		dcListeners.add(li);
 	}
 	
 	public synchronized void removeDifferingChangedListener(DifferingChangedListener li) {
 		dcListeners.remove(li);
 	}
 	
 	private void fireDifferingChanged(Map<EditorUser, DocDifference> diffs) {
 		for (DifferingChangedListener li : dcListeners) {
 			li.differingChanged(diffs);
 		}
 	}
 
 	public String getBaseText() {
 		return base.getDoc().getText();
 	}
 
 	public void setBaseNoFire(String xml) {
 		base.setDoc(new AceDoc(xml));
 	}
 	
 }
