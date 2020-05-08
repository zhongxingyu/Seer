 package net.todd.biblestudy.rcp;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import net.todd.biblestudy.common.AbstractMvpEventer;
 import net.todd.biblestudy.common.ViewerUtils;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 public class OpenNoteDialogView extends AbstractMvpEventer implements IOpenNoteDialogView {
 	private final OpenNoteDialog openNoteDialog;
 
 	public static final String NOTE_NAME_COLUMN_HEADER = "Note";
 	private static final int NOTE_NAME_COLUMN_WIDTH = 200;
 	public static final String LAST_MODIFIED_COLUMN_HEADER = "Last Modified";
 	private static final int LAST_MODIFIED_COLUMN_WIDTH = 100;
 	public static final String CREATED_COLUMN_HEADER = "Created On";
 	private static final int CREATED_COLUMN_WIDTH = 100;
 
 	private final Text filterText;
 
 	private final TableViewer notesTableViewer;
 
 	private final TableColumn noteNameColumn;
 	private final TableColumn lastModifiedColumn;
 	private final TableColumn createdColumn;
 	private final Table notesTable;
 	private final Button renameButton;
 	private final Button deleteButton;
 	private final TableEditor notesTableEditor;
 	private String renamedNoteName;
 
 	public OpenNoteDialogView(Composite composite, final OpenNoteDialog openNoteDialog) {
 		this.openNoteDialog = openNoteDialog;
 
 		GridDataFactory.fillDefaults().grab(true, true).hint(450, 200).applyTo(composite);
 		GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(composite);
 
 		filterText = new Text(composite, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(filterText);
 		filterText.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				notifyListeners(FILTER_TEXT);
 			}
 		});
 
 		filterText.setFocus();
 
 		notesTableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER | SWT.SHADOW_ETCHED_IN
 				| SWT.FULL_SELECTION);
 		notesTableViewer.setContentProvider(new ArrayContentProvider());
 		notesTableViewer.setLabelProvider(new NoteLabelProvider());
 
 		notesTableViewer.setSorter(new ViewerSorter());
 
 		notesTable = notesTableViewer.getTable();
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(notesTable);
 		notesTable.setHeaderVisible(true);
 		notesTable.setLinesVisible(true);
 
 		notesTableEditor = new TableEditor(notesTable);
 		notesTableEditor.horizontalAlignment = SWT.LEFT;
 		notesTableEditor.grabHorizontal = true;
 
 		notesTableViewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				notifyListeners(SELECTION);
 				notifyListeners(OK_BUTTON);
 				openNoteDialog.close();
 			}
 		});
 		notesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				notifyListeners(SELECTION);
 			}
 		});
 		notesTableViewer.setSorter(new ViewerSorter() {
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public void sort(Viewer viewer, Object[] elements) {
 				Arrays.sort(elements, new Comparator() {
 					@Override
 					public int compare(Object element1, Object element2) {
 						if (element1 instanceof String) {
 							return ((String) element1).compareTo((String) element2);
 						} else if (element1 instanceof Date) {
 							return ((Date) element1).compareTo((Date) element2);
 						}
 						return 0;
 					}
 				});
 			}
 		});
 
 		noteNameColumn = new TableColumn(notesTable, SWT.LEFT);
 		noteNameColumn.setText(NOTE_NAME_COLUMN_HEADER);
 		noteNameColumn.setWidth(NOTE_NAME_COLUMN_WIDTH);
 		noteNameColumn.setResizable(true);
 
 		lastModifiedColumn = new TableColumn(notesTable, SWT.LEFT);
 		lastModifiedColumn.setText(LAST_MODIFIED_COLUMN_HEADER);
 		lastModifiedColumn.setWidth(LAST_MODIFIED_COLUMN_WIDTH);
 		lastModifiedColumn.setResizable(true);
 
 		createdColumn = new TableColumn(notesTable, SWT.LEFT);
 		createdColumn.setText(CREATED_COLUMN_HEADER);
 		createdColumn.setWidth(CREATED_COLUMN_WIDTH);
 		createdColumn.setResizable(true);
 
 		Composite extraButtonsComposite = new Composite(composite, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(extraButtonsComposite);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(extraButtonsComposite);
 
 		renameButton = new Button(extraButtonsComposite, SWT.PUSH);
 		renameButton.setText("Rename");
 		GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER)
 				.hint(ViewerUtils.getButtonWidth(renameButton), SWT.DEFAULT).applyTo(renameButton);
 		renameButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(RENAME_BUTTON);
 			}
 		});
 
 		deleteButton = new Button(extraButtonsComposite, SWT.PUSH);
 		deleteButton.setText("Delete");
 		GridDataFactory.swtDefaults().hint(ViewerUtils.getButtonWidth(renameButton), SWT.DEFAULT).applyTo(deleteButton);
 		deleteButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(DELETE_BUTTON);
 			}
 		});
 	}
 
 	@Override
 	public String getFilterText() {
 		return filterText.getText();
 	}
 
 	@Override
 	public void resetFilter() {
 		notesTableViewer.resetFilters();
 	}
 
 	@Override
 	public void applyFilter(final String filter) {
 		notesTableViewer.addFilter(new NoteFilter(filter));
 	}
 
 	@Override
 	public void selectFirstNote() {
		ViewerUtils.setSingleSelection(notesTableViewer, notesTableViewer.getElementAt(0));
 	}
 
 	@Override
 	public void setOkButtonEnabled(boolean isEnabled) {
 		openNoteDialog.getOkButton().setEnabled(isEnabled);
 	}
 
 	@Override
 	public void setRenameButtonEnabled(boolean isEnabled) {
 		renameButton.setEnabled(isEnabled);
 	}
 
 	@Override
 	public void setDeleteButtonEnabled(boolean isEnabled) {
 		deleteButton.setEnabled(isEnabled);
 	}
 
 	@Override
 	public void okPressed() {
 		notifyListeners(OK_BUTTON);
 	}
 
 	@Override
 	public Note getSelectedNote() {
 		StructuredSelection selection = (StructuredSelection) notesTableViewer.getSelection();
 		Note selectedNote = (Note) selection.getFirstElement();
 		return selectedNote;
 	}
 
 	@Override
 	public void setAllNotes(List<Note> notes) {
 		notesTableViewer.setInput(notes);
 	}
 
 	@Override
 	public void makeSelectedNoteNameEditable() {
 		final TableItem selection = notesTable.getSelection()[0];
 		final Text text = new Text(notesTable, SWT.NONE);
 		text.setText(selection.getText(0));
 		text.setFocus();
 		text.selectAll();
 		text.addListener(SWT.FocusOut, new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				String newNoteName = text.getText();
 				selection.setText(0, newNoteName);
 				text.dispose();
 			}
 		});
 		text.addListener(SWT.Traverse, new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				if (e.detail == SWT.TRAVERSE_RETURN) {
 					renamedNoteName = text.getText();
 					selection.setText(0, renamedNoteName);
 					text.dispose();
 
 					notifyListeners(NOTE_RENAME);
 				} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
 					text.dispose();
 					e.doit = false;
 				}
 			}
 		});
 
 		notesTableEditor.setEditor(text, selection, 0);
 		notesTable.getShell().setDefaultButton(null);
 	}
 
 	@Override
 	public String getRenamedNoteName() {
 		return renamedNoteName;
 	}
 
 	@Override
 	public void setSelectedNote(Note note) {
 		if (note != null) {
 			notesTableViewer.setSelection(new StructuredSelection(note));
 		}
 	}
 }
