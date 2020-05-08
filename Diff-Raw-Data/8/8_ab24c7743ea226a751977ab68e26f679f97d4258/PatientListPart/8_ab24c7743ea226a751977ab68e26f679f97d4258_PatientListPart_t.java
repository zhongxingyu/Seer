 package edu.utah.cdmcc.e4.glucose.application.parts;
 
 import glucose.IntensiveCareUnit;
 import glucose.IntensiveCareUnitService;
 import glucose.provider.GlucoseItemProviderAdapterFactory;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 
 
 public class PatientListPart {
 	
 	private TableViewer tableViewer;
 	
 	@Inject IntensiveCareUnitService service;
 	@Inject ESelectionService selectionService;
 	
 	@Inject
 	PatientListPart(Composite parent){
 		tableViewer = new TableViewer(parent);
 		AdapterFactory adapterFactory = new GlucoseItemProviderAdapterFactory();
 		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
 		tableViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
 		
 	}
 	
 	@PostConstruct
 	void init(){
 		tableViewer.setInput(service.getRootGroup());
 	}
	
	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
 //	@PostConstruct
 //	public void createComposite(Composite parent, IntensiveCareUnit icu){
 //		Table table = new PatientTable(parent).getTable();
 		
 
 }
