 package org.openstreetmap.josm.plugins.notes.gui.action;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.event.ActionEvent;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
 import org.openstreetmap.josm.plugins.notes.ConfigKeys;
 import org.openstreetmap.josm.plugins.notes.Note;
 import org.openstreetmap.josm.plugins.notes.NotesPlugin;
 import org.openstreetmap.josm.plugins.notes.api.util.NotesApi;
 import org.openstreetmap.josm.plugins.notes.gui.NotesDialog;
 import org.openstreetmap.josm.plugins.notes.gui.dialogs.TextInputDialog;
 
 public class ReopenAction extends NotesAction {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Note note;
 	private String comment;
 
 	public ReopenAction(NotesDialog dialog) {
 		super(tr("Reopen note"), dialog);
 	}
 
 	@Override
 	protected void doActionPerformed(ActionEvent e) throws Exception {
         List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.NOTES_COMMENT_HISTORY, new LinkedList<String>()));
         HistoryChangedListener l = new HistoryChangedListener() {
             public void historyChanged(List<String> history) {
                 Main.pref.putCollection(ConfigKeys.NOTES_COMMENT_HISTORY, history);
             }
         };
 		note = dialog.getSelectedNote();
         comment = TextInputDialog.showDialog(Main.map,
                tr("Really reopen?"),
                 tr("<html>Really reopen this note?<br><br>You may add an optional comment:</html>"),
                NotesPlugin.loadIcon("reopen_note22.png"),
                 history, l);
         
         if(comment == null) {
             canceled = true;
             return;
         }
         note.setState(Note.State.open);
         dialog.refreshNoteStatus();
 	}
 
 	@Override
 	public void execute() throws Exception {
 		if(note == null) {
 			return;
 		}
 		NotesApi.getNotesApi().reopenNote(note, comment);
 		note.setState(Note.State.open);
 		Main.map.mapView.repaint();
 	}
 	
 	@Override
 	public String toString() {
 		return tr("Reopen: Note " + note.getId() + " - Comment: " + comment);
 	}
 
 	@Override
 	public NotesAction clone() {
         ReopenAction action = new ReopenAction(dialog);
         action.canceled = canceled;
         action.comment = comment;
         action.note = note;
         return action;
 	}
 
 }
