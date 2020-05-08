 package org.vaadin.mideaas.editor;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.vaadin.aceeditor.AceEditor;
 import org.vaadin.aceeditor.AceEditor.DiffEvent;
 import org.vaadin.aceeditor.AceEditor.DiffListener;
 import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
 import org.vaadin.aceeditor.AceMode;
 import org.vaadin.aceeditor.AceTheme;
 import org.vaadin.aceeditor.SuggestionExtension;
 import org.vaadin.aceeditor.TextRange;
 import org.vaadin.aceeditor.client.AceAnnotation;
 import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
 import org.vaadin.aceeditor.client.AceDoc;
 import org.vaadin.aceeditor.client.AceMarker;
 import org.vaadin.aceeditor.client.AceRange;
 import org.vaadin.mideaas.editor.AsyncErrorChecker.ResultListener;
 import org.vaadin.mideaas.editor.EditorState.DocType;
 import org.vaadin.mideaas.editor.ErrorChecker.Error;
 import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedEvent;
 import org.vaadin.mideaas.editor.MultiUserEditorUserGroup.EditorStateChangedListener;
 
 import com.vaadin.annotations.StyleSheet;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.event.Action;
 import com.vaadin.event.Action.Handler;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.event.ShortcutAction;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 
 @StyleSheet("ace-markers.css")
 @SuppressWarnings("serial")
 public class MultiUserEditor extends CustomComponent
 		implements DiffListener, ResultListener, UserDoc.Listener {
 		
 	private final String userId;
 	private final MultiUserDoc mud;
 	private final AceEditor editor;
 	private UserDoc activeDoc;
 
 	private EditorState currentState;
 	
 	private final HorizontalLayout hBar = new HorizontalLayout();
 	private MultiUserEditorUserGroup group;
 	private AsyncErrorChecker checker;
 	
 	private final CheckBox autoSync = new CheckBox("Autopush");
 	private final Button syncButton = new Button("Push");
 	
 	private UserDoc myDoc;
 
 	public MultiUserEditor(String userId, MultiUserDoc mud) {
 		super();
 		this.userId = userId;
 		this.mud = mud;
 		editor = new AceEditor();
 		
 		editor.setSizeFull();
 		
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSizeFull();
 		layout.addComponent(hBar);
 		layout.addComponent(editor);
 		layout.setExpandRatio(editor, 1);
 		setCompositionRoot(layout);
 	}
 	
 	//setAcePath("/mideaas/static/ace");
 	public void setAcePath(String path) {
 		editor.setThemePath(path);
 		editor.setModePath(path);
 		editor.setWorkerPath(path); 
 	}
 	
 	public void setErrorChecker(AsyncErrorChecker checker) {
 		this.checker = checker;
 	}
 	
 	public void setMode(AceMode mode) {
 		editor.setMode(mode);
 	}
 	
 	public void setTheme(AceTheme theme) {
 		editor.setTheme(theme);
 	}
 	
 	public void setWordWrap(boolean b) {
 		editor.setWordWrap(true);
 	}
 
 	public void addSelectionChangeListener(SelectionChangeListener listener) {
 		editor.addSelectionChangeListener(listener);
 	}
 
 	public void addTextChangeListener(TextChangeListener listener) {
 		editor.addTextChangeListener(listener);
 		
 	}
 
 	public TextRange getSelection() {
 		return editor.getSelection();
 	}
 
 	public AceDoc getDoc() {
 		return editor.getDoc();
 	}
 
 	public void setSuggestionExtension(SuggestionExtension se) {
 		se.extend(editor);
 	}
 	
 	@Override
 	public void attach() {
 		super.attach();
 		
 		myDoc = mud.createUserDoc(userId);
 		myDoc.editorAttached();
 		
 		getUI().addActionHandler(new Handler() {
 			Action action_ok = new ShortcutAction("Alt+S", ShortcutAction.KeyCode.S, new int[] { ShortcutAction.ModifierKey.ALT });
 			@Override
 			public Action[] getActions(Object target, Object sender) {
 				return new Action[]{action_ok};
 			}
 
 			@Override
 			public void handleAction(Action action, Object sender, Object target) {
 				syncDoc();
 			}
 			
 		});
 		
 		autoSync.setValue(true);
 		autoSync.setImmediate(true);
 		autoSync.addValueChangeListener(new ValueChangeListener() {
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				if (activeDoc!=null) {
 					boolean on = autoSync.getValue();
 					activeDoc.setSyncMode(on ? SyncMode.ASAP : SyncMode.MANUAL);
 					syncButton.setEnabled(!on);
 					activeDoc.syncDoc(editor.getDoc().withoutMarkers());
 				}
 			}
 			
 		});
 		hBar.addComponent(autoSync);
 		
 		syncButton.addClickListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				syncDoc();
 			}
 		});
 		syncButton.setEnabled(false);
 		hBar.addComponent(syncButton);
 		
 		group = new MultiUserEditorUserGroup(userId, mud);
 		setEditorState(group.getEditorState());
 		hBar.addComponent(group);
 		group.addDocStateChangedListener(new EditorStateChangedListener() {
 			@Override
 			public void stateChanged(EditorStateChangedEvent e) {
 				setEditorState(e.state);
 				editor.focus();
 			}
 		});
 		
 		editor.addDiffListener(this);
 		
 		setActiveDocToUser(userId);
 		
 		// Should we always check errors on attach?
 		//checkErrors();
 
 	}
 
 
 	protected void syncDoc() {
 		if (activeDoc!=null) {
 			activeDoc.syncDoc(editor.getDoc().withoutMarkers());
 		}
 	}
 	@Override
 	public void detach() {
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		
 		editor.removeDiffListener(this);
 		
 		myDoc.editorDetached();
 		
 		super.detach();
 	}
 	
 
 	private void setEditorState(EditorState state) {
 		if (currentState!=null && currentState.equals(state)) {
 			return;
 		}
 
 		currentState = state;
 		if (state.type==DocType.OTHERS || state.type==DocType.MINE) {
 			setActiveDocToUser(state.userId);
 		}
 		else {
 			setActiveDocToBase();
 		}
 	}
 	
 	private void setActiveDocToBase() {
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		activeDoc = null;
 		editor.setReadOnly(false);
 		editor.setDoc(mud.getBase());
 		editor.setReadOnly(true);
 	}
 	private void setActiveDocToUser(String userId) {
 		setActiveDoc(mud.createUserDoc(userId));
 	}
 	
 	private void setActiveDoc(UserDoc userDoc) {
 		if (activeDoc!=null) {
 			activeDoc.removeListener(this);
 		}
 		activeDoc = userDoc;
 		activeDoc.addListener(this);
 		editor.setReadOnly(false);
 		editor.setDoc(userDoc.getDoc());
 		editor.setReadOnly(!userDoc.getUserId().equals(userId));
 	}
 
 
 	@Override
 	public void changed(final AceDoc doc, ChangeType type) {
 		getUI().access(new Runnable() {
 			@Override
 			public void run() {
 				setEditorDoc(doc);
 			}
 		});
 		if (checker!=null) {
 			checker.checkErrors(doc.getText(), this);
 		}
 	}
 	
 	private void checkErrors() {
 		if (checker!=null) {
 			UserDoc doc = mud.getUserDoc(userId);
 			if (doc!=null) {
 				String code = doc.getDoc().getText();
 				checker.checkErrors(code, this);
 			}
 		}
 	}
 	
 	private void setEditorDoc(AceDoc doc) {
 		boolean wasReadOnly = editor.isReadOnly();
 		editor.setReadOnly(false);
 		editor.setDoc(doc);
 		editor.setReadOnly(wasReadOnly);
 	}
 	
 	@Override
 	public void errorsChecked(final List<Error> errors) {
 		getUI().access(new Runnable() {
 			@Override
 			public void run() {
 				setEditorDoc(docWithErrors(getDoc(), errors));
 			}
 		});
 	}
 
 	private AceDoc docWithErrors(AceDoc doc, List<Error> errors) {
 		HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>(errors.size());
 		HashSet<MarkerAnnotation> manns = new HashSet<MarkerAnnotation>(errors.size());
 		for (Error err : errors) {
 			AceMarker m = markerFromError(newMarkerId(), err, doc.getText());
 			markers.put(m.getMarkerId(), m);
 			AceAnnotation ann = new AceAnnotation(err.message, AceAnnotation.Type.error);
 			manns.add(new MarkerAnnotation(m.getMarkerId(), ann));
 		}
 		return doc.withMarkers(markers).withMarkerAnnotations(manns);		
 	}
 	
 	private long latestMarkerId = 0L;
 	private String newMarkerId() {
 		// TODO ?
 		return "error" + this.hashCode() + (++latestMarkerId);
 	}
 	
 	private static AceMarker markerFromError(String markerId, Error e, String text) {
 		AceRange range = new TextRange(text, e.start, e.start==e.end ? e.start+1 : e.end);
 		String cssClass = "myerrormarker1";
 		AceMarker.Type type = AceMarker.Type.text;
 		boolean inFront = true;
 		AceMarker.OnTextChange onChange = AceMarker.OnTextChange.ADJUST;
 		return new AceMarker(markerId, range, cssClass, type, inFront, onChange);
 	}
 
 	@Override
 	public void diff(DiffEvent e) {
 		if (activeDoc!=null) {
 			AceDoc doc = editor.getDoc().withoutMarkers();
			if (checker!=null) {
				checker.checkErrors(doc.getText(), this);
			}
 			activeDoc.editorChanged(doc);
 		}
 	}
 
 }
