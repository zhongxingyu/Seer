 package kkckkc.jsourcepad.ui;
 
 import kkckkc.jsourcepad.Presenter;
 import kkckkc.jsourcepad.action.ActionContextKeys;
 import kkckkc.jsourcepad.action.text.IndentAction;
 import kkckkc.jsourcepad.action.text.TabAction;
 import kkckkc.jsourcepad.model.*;
 import kkckkc.jsourcepad.model.SettingsManager.Listener;
 import kkckkc.jsourcepad.model.SettingsManager.Setting;
 import kkckkc.jsourcepad.util.action.ActionContext;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.syntaxpane.ScrollableSourcePane;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeListener;
 
 public class DocPresenter implements Presenter<DocView> {
 	private ScrollableSourcePane sourcePane;
 	private Doc doc;
     private ActionContext actionContext;
 	
 	// Collaborators
 	private DocView view;
 
 	@Autowired
     public void setView(DocView view) {
 	    this.view = view;
     }
 
 	public DocView getView() {
 	    return view;
     }
 	
 	@Autowired
 	public void setDoc(Doc doc) {
 	    this.doc = doc;
     }
 	
 	@PostConstruct
     public void init() {
 		sourcePane = view.getComponent();
 
         actionContext = new ActionContext();
         actionContext.put(ActionContextKeys.ACTIVE_DOC, doc);
         actionContext.put(ActionContextKeys.FOCUSED_COMPONENT, this);
         actionContext.commit();
         
         ActionContext.set(sourcePane, actionContext);
 
         doc.getDocList().getWindow().topic(Buffer.SelectionListener.class).subscribe(DispatchStrategy.ASYNC_EVENT, ACTION_CONTEXT_UPDATER);
         doc.getDocList().getWindow().topic(Doc.StateListener.class).subscribe(DispatchStrategy.ASYNC_EVENT, ACTION_CONTEXT_UPDATER);
 
  		doc.getActiveBuffer().bind(sourcePane.getEditorPane());
 
 		Application app = Application.get();
 		
 		app.getSettingsManager().subscribe(new SettingsListener(), false, app, doc);
 
 		sourcePane.getEditorPane().getKeymap().addActionForKeyStroke(
 				KeyStroke.getKeyStroke((char) KeyEvent.VK_ENTER), new IndentAction(doc));
 
 		sourcePane.getEditorPane().getKeymap().removeKeyStrokeBinding(
 				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
 		sourcePane.getEditorPane().getKeymap().addActionForKeyStroke(
 				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new TabAction(doc));
 
        sourcePane.getEditorPane().getKeymap().addActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
                sourcePane.getEditorPane().getActionMap().get("caret-begin-line"));
        sourcePane.getEditorPane().getKeymap().addActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
                sourcePane.getEditorPane().getActionMap().get("caret-end-line"));

 		sourcePane.getEditorPane().requestFocus();
 		
 		sourcePane.setFont(Application.get().getSettingsManager().get(FontSettings.class).asFont());
 
         wrapClipboardAction("copy", sourcePane);
         wrapClipboardAction("cut", sourcePane);
     }
 	
 	public String getTitle() {
 		return doc.getTitle();
 	}
 
     private void wrapClipboardAction(String action, ScrollableSourcePane sourcePane) {
         final Action a = sourcePane.getEditorPane().getActionMap().get(action);
 
         sourcePane.getEditorPane().getActionMap().put(action, new Action() {
             @Override
             public Object getValue(String key) {
                 return a.getValue(key);
             }
 
             @Override
             public void putValue(String key, Object value) {
                 a.putValue(key, value);
             }
 
             @Override
             public void setEnabled(boolean b) {
                 a.setEnabled(b);
             }
 
             @Override
             public boolean isEnabled() {
                 return a.isEnabled();
             }
 
             @Override
             public void addPropertyChangeListener(PropertyChangeListener listener) {
                 a.addPropertyChangeListener(listener);
             }
 
             @Override
             public void removePropertyChangeListener(PropertyChangeListener listener) {
                 a.removePropertyChangeListener(listener);
             }
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 a.actionPerformed(e);
                 Application.get().getClipboardManager().register();
             }
         });
     }
 
     public JComponent getComponent() {
         return this.sourcePane;
     }
 
     public Point getInsertionPointLocation() {
         return this.sourcePane.getEditorPane().getCaret().getMagicCaretPosition();
     }
 
 
 	
 	@SuppressWarnings("unchecked")
     private final class SettingsListener implements Listener {
 	    private SettingsListener() {
 		    // Init
 			settingUpdated(Application.get().getSettingsManager().get(TabSettings.class));
 			settingUpdated(Application.get().getSettingsManager().get(StyleSettings.class));
 			settingUpdated(Application.get().getSettingsManager().get(FontSettings.class));
 			settingUpdated(Application.get().getSettingsManager().get(EditModeSettings.class));
             settingUpdated(Application.get().getSettingsManager().get(GutterSettings.class));
 	    }
 
 	    @Override
 	    public void settingUpdated(Setting settings) {
 	        if (settings instanceof TabSettings) {
 	        	TabSettings tabSettings = (TabSettings) settings;
 	        	view.updateTabSize(tabSettings.getTabSize());
 	        } else if (settings instanceof StyleSettings) {
 	        	StyleSettings styleSettings = (StyleSettings) settings;
 	        	view.getComponent().setStyleScheme(Application.get().getStyleScheme(styleSettings));
                 view.getComponent().setShowInvisibles(styleSettings.isShowInvisibles());
                 view.getComponent().setWrapColumn(styleSettings.getWrapColumn());
 	        } else if (settings instanceof FontSettings) {
 	        	FontSettings fontSettings = (FontSettings) settings;
 	        	view.getComponent().setFont(fontSettings.asFont());
 	        } else if (settings instanceof EditModeSettings) {
 	        	EditModeSettings editModeSettings = (EditModeSettings) settings;
 	        	view.getComponent().setOverwriteMode(editModeSettings.isOverwriteMode());
 	        } else if (settings instanceof GutterSettings) {
 	        	GutterSettings gutterSettings = (GutterSettings) settings;
 	        	view.getComponent().setFoldings(gutterSettings.isFoldings());
                 view.getComponent().setLineNumbers(gutterSettings.isLineNumbers());
 	        }
 
 
 	        view.redraw();
 	    }
     }
 
 
 	public void cut() {
 		sourcePane.getEditorPane().cut();
 	}
 	
 	public void copy() {
  		sourcePane.getEditorPane().copy();
 	}
 	
 	public void paste() {
 		sourcePane.getEditorPane().paste();
 	}
 
 
     private class ActionContextUpdater implements Doc.StateListener, Buffer.SelectionListener {
         @Override
         public void modified(Doc doc, boolean newState, boolean oldState) {
             // TODO: Why is this needed. Why do we need to change the state for all document modifications
             actionContext.update();
         }
 
         @Override
         public void selectionModified(Buffer buffer) {
             if (buffer.getSelection() == null) {
                 actionContext.remove(ActionContextKeys.SELECTION);
             } else {
                 actionContext.put(ActionContextKeys.SELECTION, new Object[] { buffer.getSelection() });
             }
             actionContext.commit();
         }
     };
     private ActionContextUpdater ACTION_CONTEXT_UPDATER = new ActionContextUpdater();
 }
