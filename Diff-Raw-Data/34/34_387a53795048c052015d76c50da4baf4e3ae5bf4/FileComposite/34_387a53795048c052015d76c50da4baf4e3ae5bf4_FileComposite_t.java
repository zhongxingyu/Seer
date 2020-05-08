 package de.ptb.epics.eve.editor.views.motoraxisview.filecomposite;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.axismode.FileMode;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.views.motoraxisview.MotorAxisViewComposite;
 
 /**
  * <code>MotorAxisFileComposite</code> is a composite to input a filename
  * with positions of the motor axis.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class FileComposite extends MotorAxisViewComposite implements
 		PropertyChangeListener {
 
 	private static Logger LOGGER = 
 			Logger.getLogger(FileComposite.class.getName());
 	
 	private FileMode fileMode;
 	
 	private Label filenameLabel;
 	private Text filenameText;
 	private Binding filenameBinding;
 	private IObservableValue fileNameModelObservable;
 	private IObservableValue fileNameGUIObservable;
 	private ISWTObservableValue fileNameGUIDelayedObservable;
 	private ControlDecorationSupport filenameTextControlDecoration;
 	
 	private Button searchButton;
 	private SearchButtonSelectionListener searchButtonSelectionListener;
 	
 	private TableViewer viewer;
 	
 	/**
 	 * Constructs a <code>MotorAxisFileComposite</code>.
 	 * 
 	 * @param parent the parent composite
 	 * @param style the style
 	 * @param parentView the view the composite is contained in
 	 */
 	public FileComposite(Composite parent, int style) {
 		super(parent, style);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		this.setLayout(gridLayout);
 		
 		// "Filename:"
 		this.filenameLabel = new Label(this, SWT.NONE);
 		this.filenameLabel.setText("Filename:");
 		
 		// Filename Text field
 		this.filenameText = new Text(this, SWT.BORDER);
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		this.filenameText.setLayoutData(gridData);
 		this.filenameText.addFocusListener(new FileNameTextFocusListener());
 
 		// Search Button
 		this.searchButton = new Button(this, SWT.NONE);
 		this.searchButton.setText("Search...");
 		searchButtonSelectionListener = new SearchButtonSelectionListener();
 		this.searchButton.addSelectionListener(searchButtonSelectionListener);
 		
 		this.createViewer(this);
		this.createColumns();
 		this.viewer.setContentProvider(new FileNameTableContentProvider());
 		this.viewer.setLabelProvider(new FileNameTableLabelProvider());
 		this.viewer.getTable().setEnabled(false);
 		
 		this.fileMode = null;
 	}
 
 	private void createViewer(final Composite parent) {
 		this.viewer = new TableViewer(parent, SWT.BORDER);
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		gridData.heightHint = 40;
 		this.viewer.getTable().setLayoutData(gridData);
 		this.viewer.getTable().setHeaderVisible(true);
 	}
 	
	private void createColumns() {
 		TableViewerColumn countColumn = new TableViewerColumn(this.viewer,
 				SWT.CENTER);
 		countColumn.getColumn().setText("# points");
 		countColumn.getColumn().setWidth(80);
 		
 		TableViewerColumn minColumn = new TableViewerColumn(this.viewer, 
 				SWT.RIGHT);
 		minColumn.getColumn().setText("Minimum");
 		minColumn.getColumn().setWidth(120);
 		
 		TableViewerColumn maxColumn = new TableViewerColumn(this.viewer, 
 				SWT.RIGHT);
 		maxColumn.getColumn().setText("Maximum");
 		maxColumn.getColumn().setWidth(120);
 		
 		TableViewerColumn emptyColumn = new TableViewerColumn(this.viewer, 
 				SWT.NONE);
 		emptyColumn.getColumn().setText("");
 		emptyColumn.getColumn().setWidth(10);
 	}
 	
 	/**
 	 * calculate the height to see all entries of this composite
 	 * 
 	 * @return the needed height of Composite to see all entries
 	 */
 	public int getTargetHeight() {
 		return (filenameText.getBounds().y + filenameText.getBounds().height
 				+ viewer.getTable().getBounds().height + 10);
 	}
 
 	/**
 	 * calculate the width to see all entries of this composite
 	 * 
 	 * @return the needed width of Composite to see all entries
 	 */
 	public int getTargetWidth() {
 		return (filenameText.getBounds().x + filenameText.getBounds().width + 10);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setAxis(Axis axis) {
 		this.reset();
 		if (axis == null) {
 			return;
 		}
 		if (!(axis.getMode() instanceof FileMode)) {
 			LOGGER.warn("invalid axis mode");
 			return;
 		}
 		this.fileMode = ((FileMode)axis.getMode());
 		this.createBinding();
 		this.fileMode.getAxis().getMotorAxis()
 				.addPropertyChangeListener("highlimit", this);
 		this.fileMode.getAxis().getMotorAxis()
 				.addPropertyChangeListener("lowlimit", this);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createBinding() {
 		this.fileNameModelObservable = BeansObservables.observeValue(
 				this.fileMode, FileMode.FILE_PROP);
 		this.fileNameGUIObservable = SWTObservables.observeText(
 				this.filenameText, SWT.Modify);
 		UpdateValueStrategy targetToModel = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		targetToModel.setConverter(new FileNameTargetToModelConverter());
 		targetToModel.setAfterGetValidator(new FileNameValidator(this.fileMode
 				.getAxis(), this.viewer));
 		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		modelToTarget.setConverter(new FileNameModelToTargetConverter());
 		this.fileNameGUIDelayedObservable = SWTObservables.observeDelayedValue(
 				500, (ISWTObservableValue) this.fileNameGUIObservable);
 		filenameBinding = context.bindValue(fileNameGUIDelayedObservable,
 				fileNameModelObservable, targetToModel, modelToTarget);
 		this.filenameTextControlDecoration = ControlDecorationSupport.create(
 				filenameBinding, SWT.LEFT);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	protected void reset() {
 		LOGGER.debug("reset");
 		if (this.fileMode != null) {
 			if (this.filenameTextControlDecoration != null) {
 				this.filenameTextControlDecoration.dispose();
 			}
 			this.context.removeBinding(this.filenameBinding);
 			this.filenameBinding.dispose();
 			this.fileNameModelObservable.dispose();
 			this.fileNameGUIDelayedObservable.dispose();
 			this.fileNameGUIObservable.dispose();
 			
 			this.fileMode.getAxis().getMotorAxis()
 					.removePropertyChangeListener("highlimit", this);
 			this.fileMode.getAxis().getMotorAxis()
 					.removePropertyChangeListener("lowlimit", this);
 		}
 		this.fileMode = null;
 		this.viewer.setInput(null);
 		this.filenameText.setText("");
 		this.viewer.getTable().setEnabled(false);
 		this.redraw();
 	}
 	
 	// **********************************************************************
 	// ************************** Listeners *********************************
 	// **********************************************************************
 	
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>searchButton</code>.
 	 */
 	private class SearchButtonSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			int lastSeperatorIndex;
 			final String filePath;
 
 			if (fileMode.getFile() == null
 					|| fileMode.getFile().getAbsolutePath().isEmpty()) {
 				filePath = Activator.getDefault().getRootDirectory();
 			} else {
 				// als filePath wird das vorhandene Verzeichnis gesetzt
 				lastSeperatorIndex = fileMode.getFile().getAbsolutePath()
 						.lastIndexOf(File.separatorChar);
 				filePath = fileMode.getFile().getAbsolutePath()
 						.substring(0, lastSeperatorIndex + 1);
 			}
 			FileDialog fileWindow = new FileDialog(getShell(), SWT.SAVE);
 			fileWindow.setFilterPath(filePath);
 			String name = fileWindow.open();
 			if (name == null) {
 				// dialog was cancelled
 				return;
 			}
 			 filenameText.setText(name);
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.7
 	 */
 	private class FileNameTextFocusListener implements FocusListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (filenameText.getText().isEmpty()) {
 				filenameBinding.updateModelToTarget();
 				filenameBinding.validateTargetToModel();
 				filenameBinding.validateModelToTarget();
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals("highlimit") || 
 				e.getPropertyName().equals("lowlimit")) {
 			this.filenameBinding.updateTargetToModel();
 		}
 	}
 }
