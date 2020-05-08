 package de.hswt.hrm.misc.ui.part;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.Config;
 import de.hswt.hrm.common.Hrm;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.table.ColumnComparator;
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.common.ui.swt.table.TableViewerController;
 import de.hswt.hrm.misc.reportPreferences.model.ReportPreference;
 
 public class ReportPreferencesComposite extends Composite {
 
     private final static Logger LOG = LoggerFactory.getLogger(ReportPreferencesComposite.class);
 
     // @Inject
     // private ReportPreferencesService prefService;
 
     @Inject
     private IShellProvider shellProvider;
 
     @Inject
     private IEclipseContext context;
 
     private Table table;
     private TableViewer tableViewer;
 
     private Composite composite;
 
     private Collection<ReportPreference> preferences;
     private Text directoryText;
     private Label lblNewLabel;
     private Button browseButton;
     private Label lblStyles;
 
     /**
      * Do not use this constructor when instantiate this composite! It is only included to make the
      * WindowsBuilder working.
      * 
      * @param parent
      * @param style
      */
     private ReportPreferencesComposite(Composite parent, int style) {
         super(parent, style);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public ReportPreferencesComposite(Composite parent) {
         super(parent, SWT.NONE);
     }
 
     /**
      * Create contents of the view part.
      */
     @PostConstruct
     public void createControls() {
         this.setLayout(new FillLayout());
         this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 
         composite = new Composite(this, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));
 
         lblNewLabel = new Label(composite, SWT.NONE);
         lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
         lblNewLabel.setText("StandardDirectory");
 
         directoryText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
         directoryText.setToolTipText("A local directory where all the reports are created.");
         directoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
         browseButton = new Button(composite, SWT.NONE);
         browseButton
                 .setToolTipText("Browse for a local directory where all the reports are created.");
         browseButton.setText("Browse ...");
         browseButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 setStandardReportDirectory();
             }
         });
 
         lblStyles = new Label(composite, SWT.NONE);
         lblStyles.setText("Layouts");
 
         tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
         tableViewer.addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 editPreference();
             }
         });
         table = tableViewer.getTable();
         GridData gd_table = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
         gd_table.widthHint = 401;
         table.setLayoutData(gd_table);
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
         table.setLayoutData(LayoutUtil.createFillData());
         new Label(composite, SWT.NONE);
 
         initializeTable();
         refreshTable();
         
         Config cfg = Config.getInstance();
         String dir = cfg.getProperty(Config.Keys.REPORT_STYLE_FOLDER);
        directoryText.setText(dir);
 
         // TODO if (prefService == null) {
         // LOG.error("EvaluationService not injected to EvaluationPart.");
         // }
     }
 
     private void refreshTable() {
         // TODO try {
         // this.preferences = prefService.findAll();
         // tableViewer.setInput(this.preferences);
         // }
         // catch (DatabaseException e) {
         // LOG.error("Unable to retrieve list of Evaluations.", e);
         // showDBConnectionError();
         // }
     }
 
     private void initializeTable() {
         List<ColumnDescription<ReportPreference>> columns = ReportPreferenceUtil.getColumns();
 
         // Create columns in tableviewer
         TableViewerController<ReportPreference> filler = new TableViewerController<>(tableViewer);
         filler.createColumns(columns);
 
         // Enable column selection
         filler.createColumnSelectionMenu();
 
         // Enable sorting
         ColumnComparator<ReportPreference> comperator = new ColumnComparator<>(columns);
         filler.enableSorting(comperator);
 
         // Add dataprovider that handles our collection
         tableViewer.setContentProvider(ArrayContentProvider.getInstance());
 
     }
 
     private void showDBConnectionError() {
         // TODO translate
         MessageDialog.openError(shellProvider.getShell(), "Connection Error",
                 "Could not load preferences from Database.");
     }
 
     /**
      * This Event is called whenever the add button is pressed.
      * 
      * @param event
      */
 
     public void addPrefernence() {
         ReportPreference preference = null;
 
         Optional<ReportPreference> newPreference = ReportPreferenceUtil.showWizard(context,
                 shellProvider.getShell(), Optional.fromNullable(preference));
 
         if (newPreference.isPresent()) {
             preferences.add(newPreference.get());
             tableViewer.refresh();
         }
     }
 
     /**
      * This method is called whenever a doubleClick onto the Tableviewer occurs. It obtains the
      * summaryfrom the selected column of the TableViewer. The Contact is passed to the
      * EvaluationWizard. When the Wizard has finished, the contact will be updated in the Database
      * 
      * @param event
      *            Event which occured within SWT
      */
     public void editPreference() {
         // obtain the contact in the column where the doubleClick happend
         ReportPreference selectedPreference = (ReportPreference) tableViewer
                 .getElementAt(tableViewer.getTable().getSelectionIndex());
         if (selectedPreference == null) {
             return;
         }
         // TODO try {
         // prefService.refresh(selectedPreference);
         // Optional<ReportPreference> updatedPreference = ReportPreferenceUtil.showWizard(context,
         // shellProvider.getShell(), Optional.of(selectedPreference));
         //
         // if (updatedPreference.isPresent()) {
         // tableViewer.refresh();
         // }
         // }
         // catch (DatabaseException e) {
         // LOG.error("Could not retrieve the Preferences from database.", e);
         // showDBConnectionError();
         // }
     }
 
     private void setStandardReportDirectory() {
         DirectoryDialog dialog = new DirectoryDialog(this.getShell());
         dialog.setText("Report standard directory selection");
         dialog.setMessage("Select a directory as root of all created reports.");
         String dir = dialog.open();
         if (dir != null) {
             directoryText.setText(dir);
         
 	        Config cfg = Config.getInstance();
 	        cfg.setProperty(Config.Keys.REPORT_STYLE_FOLDER, dir);
 	
 	        Path configPath = Hrm.getConfigPath();
 	        try {
 	            cfg.store(configPath, true, true);
 	        }
 	        catch (IOException e) {
 	            e.printStackTrace();
 	        }
         }
     }
 }
