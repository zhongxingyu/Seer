 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.jcolon.events.SemiColonErrorLocationEvent;
 import com.laboki.eclipse.plugin.jcolon.instance.AbstractEventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.task.AsyncTask;
 
 final class Inserter extends AbstractEventBusInstance {
 
 	private final Problem problem = new Problem();
 	private final IEditorPart editor = EditorContext.getEditor();
 	private final IDocument document = EditorContext.getDocument(this.editor);
 	private static final String SEMICOLON = ";";
 
 	public Inserter(final EventBus eventBus) {
 		super(eventBus);
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public void semiColonErrorLocation(final SemiColonErrorLocationEvent event) {
 		new AsyncTask(EditorContext.ERROR_CHECKING_TASK, EditorContext.SHORT_DELAY_TIME) {
 
 			@Override
 			public void asyncExecute() {
 				this.tryToInsertSemiColon(event.getLocation());
 			}
 
 			private void tryToInsertSemiColon(final int location) {
 				try {
 					this.insertSemiColon(location);
				} catch (final Exception e) {}
 			}
 
 			private void insertSemiColon(final int location) throws Exception {
 				if (this.canNotInsertSemiColon(location)) return;
 				Inserter.this.document.replace(location, 0, Inserter.SEMICOLON);
 			}
 
 			private boolean canNotInsertSemiColon(final int location) throws Exception {
 				return this.semiColonIsAlreadyInserted(location) || this.locationErrorMismatch(location) || EditorContext.isInEditMode(Inserter.this.editor);
 			}
 
 			private boolean semiColonIsAlreadyInserted(final int location) throws Exception {
 				return String.valueOf(Inserter.this.document.getChar(location)).equals(Inserter.SEMICOLON);
 			}
 
 			private boolean locationErrorMismatch(final int location) {
 				try {
 					return location != Inserter.this.problem.location();
 				} catch (final Exception e) {
 					return false;
 				}
 			}
 		}.begin();
 	}
 }
