 package net.todd.biblestudy.rcp;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import net.todd.biblestudy.common.AbstractMvpEventer;
 import net.todd.biblestudy.common.ViewerUtils;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 
 public class ExportNotesDialogView extends AbstractMvpEventer implements IExportNotesDialogView {
 	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
 
 	private static final String NOTE_NAME_COLUMN_HEADER = "Note";
 	private static final String LAST_MODIFIED_COLUMN_HEADER = "Last modified";
 	private static final String CREATED_COLUMN_HEADER = "Created on";
 
 	private static final int NOTE_NAME_COLUMN_WIDTH = 200;
 	private static final int LAST_MODIFIED_COLUMN_WIDTH = 100;
 	private static final int CREATED_COLUMN_WIDTH = 100;
 
 	private final TableViewer notesTableViewer;
 	private final Text exportFileLocationText;
 
 	private final Button exportButton;
 
 	public ExportNotesDialogView(Composite composite, ExportNotesDialog exportNotesDialog) {
 		GridLayoutFactory.fillDefaults().margins(2, 2).numColumns(4).applyTo(composite);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
 
 		Button selectAllButton = new Button(composite, SWT.NORMAL);
 		selectAllButton.setText("All");
 		selectAllButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(SELECTION_ALL);
 			}
 		});
 		GridDataFactory.swtDefaults().applyTo(selectAllButton);
 
 		Button selectNoneButton = new Button(composite, SWT.NORMAL);
 		selectNoneButton.setText("None");
 		selectNoneButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(SELECTION_NONE);
 			}
 		});
 		GridDataFactory.swtDefaults().span(3, 1).applyTo(selectNoneButton);
 
		notesTableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER | SWT.SHADOW_ETCHED_IN
				| SWT.FULL_SELECTION | SWT.MULTI);
 		notesTableViewer.setContentProvider(new ArrayContentProvider());
 		notesTableViewer.setLabelProvider(new ExportNoteLabelProvider());
 		notesTableViewer.setSorter(new ViewerSorter());
 
 		Table notesTable = notesTableViewer.getTable();
 		GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(notesTable);
 		notesTable.setHeaderVisible(true);
 		notesTable.setLinesVisible(true);
 
 		notesTable.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(SELECTION);
 			}
 		});
 
 		TableColumn noteNameColumn = new TableColumn(notesTable, SWT.LEFT);
 		noteNameColumn.setText(NOTE_NAME_COLUMN_HEADER);
 		noteNameColumn.setWidth(NOTE_NAME_COLUMN_WIDTH);
 
 		TableColumn lastModifiedColumn = new TableColumn(notesTable, SWT.LEFT);
 		lastModifiedColumn.setText(LAST_MODIFIED_COLUMN_HEADER);
 		lastModifiedColumn.setWidth(LAST_MODIFIED_COLUMN_WIDTH);
 
 		TableColumn createdColumn = new TableColumn(notesTable, SWT.LEFT);
 		createdColumn.setText(CREATED_COLUMN_HEADER);
 		createdColumn.setWidth(CREATED_COLUMN_WIDTH);
 
 		exportFileLocationText = new Text(composite, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(exportFileLocationText);
 		exportFileLocationText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				notifyListeners(EXPORT_FILE_LOCATION);
 			}
 		});
 
 		Button exportFileBrowseButton = new Button(composite, SWT.BORDER);
 		exportFileBrowseButton.setText("Browse...");
 		GridDataFactory.swtDefaults().hint(ViewerUtils.getButtonWidth(exportFileBrowseButton), SWT.DEFAULT)
 				.applyTo(exportFileBrowseButton);
 		exportFileBrowseButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(FILE_BROWSE);
 			}
 		});
 
 		exportButton = exportNotesDialog.getExportButton();
 		exportButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				notifyListeners(OK_BUTTON_PRESSED);
 			}
 		});
 	}
 
 	@Override
 	public void setExportFileLocation(String exportFileLocation) {
 		exportFileLocationText.setText(exportFileLocation);
 	}
 
 	@Override
 	public String getExportFileLocation() {
 		return exportFileLocationText.getText();
 	}
 
 	@Override
 	public void okPressed() {
 		notifyListeners(OK_BUTTON_PRESSED);
 	}
 
 	@Override
 	public void populateAllNotes(List<Note> notes) {
 		notesTableViewer.setInput(notes);
 	}
 
 	@Override
 	public List<Note> getSelectedNotes() {
 		return ViewerUtils.getListSelection(notesTableViewer, Note.class);
 	}
 
 	@Override
 	public void setSelectedNotes(List<Note> selectedNotes) {
		ViewerUtils.setSelectionList(notesTableViewer, selectedNotes);
 	}
 
 	private class ExportNoteLabelProvider extends LabelProvider implements ITableLabelProvider {
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			Note note = (Note) element;
 
 			String columnText = "";
 			if (columnIndex == 0) {
 				columnText = note.getName();
 			} else if (columnIndex == 1) {
 				columnText = DATE_FORMATTER.format(note.getLastModified());
 			} else if (columnIndex == 2) {
 				columnText = DATE_FORMATTER.format(note.getCreatedTimestamp());
 			}
 
 			if (DATE_FORMATTER.format(new Date()).equals(columnText)) {
 				columnText = "Today";
 			}
 
 			return columnText;
 		}
 	}
 
 	@Override
 	public void setExportButtonEnabled(boolean enabled) {
 		exportButton.setEnabled(enabled);
 	}
 }
