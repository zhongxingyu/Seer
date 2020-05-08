 package de.hswt.hrm.inspection.ui.part;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.observer.Observer;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.inspection.model.BiologicalRating;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.PhysicalRating;
 import de.hswt.hrm.inspection.model.SamplingPointType;
 import de.hswt.hrm.inspection.ui.wizard.ReportExportWizard;
 import de.hswt.hrm.inspection.ui.grid.BiologicalDisplay;
 import de.hswt.hrm.inspection.ui.grid.CombinedDisplay;
 import de.hswt.hrm.inspection.ui.grid.PhysicalDisplay;
 import de.hswt.hrm.inspection.ui.grid.SamplingPoints;
 import de.hswt.hrm.inspection.ui.listener.ComponentSelectionChangedListener;
 import de.hswt.hrm.inspection.ui.listener.InspectionObserver;
 import de.hswt.hrm.inspection.ui.listener.PlantChangedListener;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.report.latex.service.ReportService;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 import de.hswt.hrm.scheme.service.SchemeService;
 
 public class InspectionPart implements ComponentSelectionChangedListener, PlantChangedListener {
     
     private static final I18n I18N = I18nFactory.getI18n(InspectionPart.class);
     
     private final static Logger LOG = LoggerFactory.getLogger(InspectionPart.class);
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private IShellProvider shellProvider;
 
     @Inject
     private SchemeService schemeService;
 
     @Inject
     private ReportService reportService;
     
     private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
     private Form form;
     
     private boolean first = true;
 
     private ReportsOverviewComposite reportsOverviewComposite;
 
     private TabFolder tabFolder;
 
     private TabItem overviewTab;
     private TabItem generalTab;
     private TabItem biolocicalRatingTab;
     private TabItem physicalRatingTab;
     private TabItem performanceTab;
 
     private ActionContributionItem saveContribution;
     private ActionContributionItem addContribution;
     private ActionContributionItem copyContribution;
     private ActionContributionItem editContribution;
     private ActionContributionItem evaluateContribution;
 
     private ReportGeneralComposite reportGeneralComposite;
 
     private ComponentSelectionComposite performanceComposite;
     private ComponentSelectionComposite physicalComposite;
     private ComponentSelectionComposite biologicalComposite;
 
     private Inspection selectedInspection;
 
     private List<InspectionObserver> inspectionObeserver;
     
     public InspectionPart() {
         // toolkit can be created in PostConstruct, but then then
         // WindowBuilder is unable to parse the code
         formToolkit.dispose();
         formToolkit = FormUtil.createToolkit();
 
         inspectionObeserver = new ArrayList<>();
         
         if (reportService != null) {
             LOG.debug("Injected ReportService to InspectionPart");
         }
     }
 
     /**
      * Create contents of the view part.
      */
     @PostConstruct
     public void createControls(final Composite parent) {
         parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
 
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         composite.setLayout(new FillLayout());
 
         form = formToolkit.createForm(composite);
         form.getHead().setOrientation(SWT.RIGHT_TO_LEFT);
         form.getBody().setBackgroundMode(SWT.INHERIT_FORCE);
         form.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.paintBordersFor(form);
         form.setText(I18N.tr("Inspection"));
         FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
         fillLayout.marginHeight = 5;
         fillLayout.marginWidth = 5;
         form.getBody().setLayout(fillLayout);
         formToolkit.decorateFormHeading(form);
 
         tabFolder = new TabFolder(form.getBody(), SWT.NONE);
         tabFolder.setBackgroundMode(SWT.INHERIT_FORCE);
         tabFolder.addSelectionListener(new SelectionAdapter() {
             @Override
 			public void widgetSelected(SelectionEvent e) {
             	if (first){
             		first = false;
             		return;
             	}
 				setInspection(reportsOverviewComposite.getSelectedInspection());
 			}
 		});
 
         formToolkit.adapt(tabFolder);
         formToolkit.paintBordersFor(tabFolder);
 
         overviewTab = new TabItem(tabFolder, SWT.NONE);
         overviewTab.setText(I18N.tr("Overview"));
 
         // reports overview composite
         reportsOverviewComposite = new ReportsOverviewComposite(tabFolder);
         ContextInjectionFactory.inject(reportsOverviewComposite, context);
         overviewTab.setControl(reportsOverviewComposite);
 
         generalTab = new TabItem(tabFolder, SWT.NONE);
         generalTab.setText(I18N.tr("General"));
 
         reportGeneralComposite = new ReportGeneralComposite(tabFolder, this);
         ContextInjectionFactory.inject(reportGeneralComposite, context);
         inspectionObeserver.add(reportGeneralComposite);
         generalTab.setControl(reportGeneralComposite);
 
         biolocicalRatingTab = new TabItem(tabFolder, SWT.NONE);
         biolocicalRatingTab.setText(I18N.tr("Biological rating"));
 
         biologicalComposite = new ComponentSelectionComposite(tabFolder,
                 ReportBiologicalComposite.class);
         ContextInjectionFactory.inject(biologicalComposite, context);
         inspectionObeserver.add(biologicalComposite);
         biologicalComposite.addComponentSelectionListener(this);
         biolocicalRatingTab.setControl(biologicalComposite);
 
         physicalRatingTab = new TabItem(tabFolder, SWT.NONE);
         physicalRatingTab.setText(I18N.tr("Physical rating"));
 
         physicalComposite = new ComponentSelectionComposite(tabFolder,
                 ReportPhysicalComposite.class);
         ContextInjectionFactory.inject(physicalComposite, context);
         inspectionObeserver.add(physicalComposite);
         physicalComposite.addComponentSelectionListener(this);
         physicalRatingTab.setControl(physicalComposite);
 
         performanceTab = new TabItem(tabFolder, SWT.NONE);
         performanceTab.setText(I18N.tr("Performance"));
 
         performanceComposite = new ComponentSelectionComposite(tabFolder,
                 ReportPerformanceComposite.class);
         ContextInjectionFactory.inject(performanceComposite, context);
         inspectionObeserver.add(performanceComposite);
         performanceComposite.addComponentSelectionListener(this);
         performanceTab.setControl(performanceComposite);
         tabFolder.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(overviewTab)) {
                     showOverviewActions(true);
                 }
                 else {
                     showOverviewActions(false);
                 }
             }
         });
         ReportBiologicalComposite reportBio = ((ReportBiologicalComposite) biologicalComposite.getRatingComposite());
         reportBio.addGradeSelectionObserver(new Observer<Integer>() {
 			
 			@Override
 			public void changed(Integer item) {
 				SchemeComponent c = biologicalComposite.getSelectedSchemeComponent();
 				if(c != null){
 					selectedInspection.setBiologicalRatingRating(c, item);
 				}
 			}
 		});
         reportBio.addSamplePointObserver(new Observer<SamplingPointType>() {
 			
 			@Override
 			public void changed(SamplingPointType item) {
 				SchemeComponent c = biologicalComposite.getSelectedSchemeComponent();
 				if(c != null){
 					selectedInspection.setBiologicalRatingSamplingPoint(c, item);
 				}
 			}
 		});
         ReportPhysicalComposite reportPhy = (ReportPhysicalComposite)physicalComposite.getRatingComposite();
         reportPhy.addGradeSelectionObserver(new Observer<Integer>() {
 
 					@Override
 					public void changed(Integer item) {
 						SchemeComponent c = physicalComposite.getSelectedSchemeComponent();
 						if(c != null){
 							selectedInspection.setPhysicalRatingRating(c, item);
 						}
 					}
 				});
         reportPhy.addSamplePointObserver(new Observer<SamplingPointType>() {
 			
 			@Override
 			public void changed(SamplingPointType item) {
 				SchemeComponent c = physicalComposite.getSelectedSchemeComponent();
 				if(c != null){
 					selectedInspection.setPhysicalRatingSamplingPoint(c, item);
 				}
 			}
 		});
         createActions();
     }
 
     private void setInspection(Inspection inspection) {
 
         if (inspection == null) {
             MessageDialog.openError(shellProvider.getShell(), I18N.tr("Selection Error"),
                     I18N.tr("No inspection selected."));
             tabFolder.setSelection(0);
             return;
         }
 
         if (selectedInspection != inspection) {
             selectedInspection = inspection;
 
             for (InspectionObserver observer : inspectionObeserver) {
             	observer.inspectionChanged(inspection);
             }
             if (inspection != null) {
                 initInspectionObservers();
             }
         }
 
     }
 
     private void initInspectionObservers() {
     	// TODO
         SamplingPoints points = new SamplingPoints(biolocicalRatingTab.getDisplay());
         final CombinedDisplay combinedDisplay = new CombinedDisplay(performanceComposite.getInspectionSchemeGrid(), points);
         final BiologicalDisplay bDisplay = new BiologicalDisplay(biologicalComposite.getInspectionSchemeGrid(), points);
         selectedInspection
                 .addBiologicalRatingObserver(new Observer<Collection<BiologicalRating>>() {
 
                     @Override
                     public void changed(Collection<BiologicalRating> item) {
                     	bDisplay.update(item, selectedInspection.getScheme());
                     	combinedDisplay.updateBiological(item);
                     }
                 });
         final PhysicalDisplay pDisplay = new PhysicalDisplay(physicalComposite.getInspectionSchemeGrid(), points);
         selectedInspection.addPhysicalRatingObserver(new Observer<Collection<PhysicalRating>>() {
 			
 			@Override
 			public void changed(Collection<PhysicalRating> item) {
 				pDisplay.update(item, selectedInspection.getScheme());
 				combinedDisplay.updatePhysical(item);
 			}
 		});
     }
 
     protected void showOverviewActions(boolean visible) {
         addContribution.setVisible(visible);
         copyContribution.setVisible(visible);
         editContribution.setVisible(visible);
         evaluateContribution.setVisible(visible);
         form.getToolBarManager().update(true);
     }
 
     private void createActions() {
         // TODO translate
         Action evaluateAction = new Action(I18N.tr("Report")) {
             @Override
             public void run() {
             	createReport();
             }
         };
         evaluateAction.setDescription("Create a report.");
         evaluateContribution = new ActionContributionItem(evaluateAction);
         form.getToolBarManager().add(evaluateContribution);
 
         form.getToolBarManager().add(new Separator());
 
         Action saveAction = new Action(I18N.tr("Save")) {
             @Override
             public void run() {
                 super.run();
                 reportsOverviewComposite.addInspection();
             }
         };
         saveAction.setDescription(I18N.tr("Save the current edited report."));
         saveAction.setEnabled(false);
         saveContribution = new ActionContributionItem(saveAction);
         form.getToolBarManager().add(saveContribution);
 
         form.getToolBarManager().add(new Separator());
 
         Action editAction = new Action(I18N.tr("Edit")) {
             @Override
             public void run() {
                 super.run();
 
                 setInspection(reportsOverviewComposite.getSelectedInspection());
                 tabFolder.setSelection(generalTab);
             }
         };
         editAction.setDescription(I18N.tr("Edit an existing report."));
         editContribution = new ActionContributionItem(editAction);
         form.getToolBarManager().add(editContribution);
 
         Action copyAction = new Action(I18N.tr("Copy")) {
             @Override
             public void run() {
                 super.run();
                 // TODO copy a report
             }
         };
         copyAction.setDescription(I18N.tr("Copy a report."));
         copyAction.setEnabled(false);
         copyContribution = new ActionContributionItem(copyAction);
         form.getToolBarManager().add(copyContribution);
 
         Action addAction = new Action(I18N.tr("Add")) {
             @Override
             public void run() {
                 super.run();
                 reportsOverviewComposite.addInspection();
             }
         };
         addAction.setDescription(I18N.tr("Add a new report."));
         addContribution = new ActionContributionItem(addAction);
         form.getToolBarManager().add(addContribution);
 
         form.getToolBarManager().update(true);
     }
 
     protected void createReport() {
     	// Create wizard with injection support
     	ReportExportWizard wizard = new ReportExportWizard(
     			reportsOverviewComposite.getSelectedInspection());
     	ContextInjectionFactory.inject(wizard, context);
 
     	// Show wizard
    	WizardDialog wd = new WizardDialog(shellProvider.getShell(), wizard);
     	wd.open();
 	}
     
     @PreDestroy
     public void dispose() {
         formToolkit.dispose();
     }
 
 	@Override
 	public void componentSelectionChanged(SchemeComponent component) {
 		for (InspectionObserver observer : inspectionObeserver) {
 			observer.inspectionComponentSelectionChanged(component);
 		}
 	}
 	
 	@Override
 	public void plantChanged(Plant plant) {
 		for (InspectionObserver observer : inspectionObeserver) {
 			observer.plantChanged(plant);
 		}
 	}
 
 }
