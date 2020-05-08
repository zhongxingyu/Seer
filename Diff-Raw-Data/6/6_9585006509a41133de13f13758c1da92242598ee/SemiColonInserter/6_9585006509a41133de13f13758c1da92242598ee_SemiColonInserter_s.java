 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import lombok.ToString;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.DelayedTask;
 import com.laboki.eclipse.plugin.jcolon.Instance;
 import com.laboki.eclipse.plugin.jcolon.inserter.events.SemiColonErrorLocationEvent;
 import com.laboki.eclipse.plugin.jcolon.inserter.events.SyncFilesEvent;
 
 @ToString
 final class SemiColonInserter implements Instance {
 
 	private EventBus eventBus;
 	private Problem problem = new Problem();
 	private IEditorPart editor = EditorContext.getEditor();
 	private IDocument document = EditorContext.getDocument(this.editor);
 	private static final String SEMICOLON = ";";
 
 	public SemiColonInserter(final EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void semiColonErrorLocation(final SemiColonErrorLocationEvent event) {
 		EditorContext.asyncExec(new DelayedTask(EditorContext.TASK_FAMILY_NAME, EditorContext.DELAY_TIME_IN_MILLISECONDS) {
 
 			@Override
 			public void execute() {
 				SemiColonInserter.this.insertSemiColon(event.getLocation());
 				SemiColonInserter.this.postEvent();
 			}
 		});
 	}
 
 	private void insertSemiColon(final int location) {
 		try {
 			this.tryToInsertSemiColon(location);
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void tryToInsertSemiColon(final int location) throws BadLocationException {
 		if (this.semiColonIsAlreadyInserted(location)) return;
 		if (this.locationErrorMismatch(location)) return;
 		if (EditorContext.isInEditMode(this.editor)) return;
 		SemiColonInserter.this.document.replace(location, 0, SemiColonInserter.SEMICOLON);
 	}
 
 	private boolean semiColonIsAlreadyInserted(final int location) throws BadLocationException {
 		return String.valueOf(this.document.getChar(location)).equals(SemiColonInserter.SEMICOLON);
 	}
 
 	private boolean locationErrorMismatch(final int location) {
		return location != this.problem.location();
 	}
 
 	protected void postEvent() {
 		this.eventBus.post(new SyncFilesEvent());
 	}
 
 	@Override
 	public Instance begin() {
 		this.eventBus.register(this);
 		return this;
 	}
 
 	@Override
 	public Instance end() {
 		this.eventBus.unregister(this);
 		this.nullifyFields();
 		return this;
 	}
 
 	private void nullifyFields() {
 		this.eventBus = null;
 		this.document = null;
 		this.editor = null;
 		this.problem = null;
 	}
 }
