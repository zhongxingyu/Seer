 package de.lmu.ifi.dbs.medmon.life.e4.wizards.pages;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.eclipse.e4.core.di.annotations.Creatable;
 import org.eclipse.e4.ui.services.IStylingEngine;
 import org.eclipse.gemini.ext.di.GeminiPersistenceContext;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.lmu.ifi.dbs.medmon.database.entity.Patient;
 import de.lmu.ifi.dbs.medmon.life.e4.provider.PatientContentProvider;
 import de.lmu.ifi.dbs.medmon.life.e4.provider.PatientLabelProvider;
 
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 @Creatable
 public class PatientSelection extends WizardPage {
 
 	private static final Logger log = LoggerFactory.getLogger(PatientSelection.class);
 
 	private Text txtPatientInput;
 	private TableViewer patientTable;
 	private Composite compositePatientSelection;
 	private Patient selectedPatient;
 	private boolean flipToNextPage = false;
 	
 	public boolean newPatient = false;
 
 	@Inject
 	IStylingEngine styleEngine;
 
 	@Inject
 	@GeminiPersistenceContext(unitName = "medmon")
 	EntityManager em;
 	
 	@Inject
 	PatientContentProvider patientProvider;
 	
 	@Inject
 	PatientLabelProvider patientLabel;
 	private Table table;
 
 	/**
 	 * Create the wizard.
 	 */
 	@Inject
 	public PatientSelection() {
 		super("wizardPage");
 		setTitle("Patientenauswahl");
 	}
 
 	/**
 	 * Create contents of the wizard.
 	 * 
 	 * @param parent
 	 */
 	public void createControl(Composite parent) {
 		styleEngine.setId(parent, "WizardStyle");
 		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		compositePatientSelection = new Composite(parent, SWT.NULL);
 		setPageComplete(false);
 
 		setControl(compositePatientSelection);
 		GridLayout gl_compositePatientSelection = new GridLayout(2, true);
 		compositePatientSelection.setLayout(gl_compositePatientSelection);
 
 		Label lblSelectPatient = new Label(compositePatientSelection, SWT.NONE);
 		lblSelectPatient.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
 		lblSelectPatient.setText("Bitte Patient ausw\u00E4hlen");
 
 		txtPatientInput = new Text(compositePatientSelection, SWT.BORDER);
 		/*txtPatientInput.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				patientTable.setInput(searchPatient(txtPatientInput.getText()));
 				patientTable.refresh();
 			}
 		});*/
 
 		GridData gd_txtPatientInput = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
 		gd_txtPatientInput.widthHint = 400;
 		txtPatientInput.setLayoutData(gd_txtPatientInput);
 		
 		patientTable = new TableViewer(compositePatientSelection, SWT.BORDER | SWT.FULL_SELECTION);
 		table = patientTable.getTable();
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
 		patientTable.setContentProvider(patientProvider);
 		patientTable.setLabelProvider(patientLabel);
 		patientTable.setInput(em.createNamedQuery("Patient.findAll", Patient.class).getResultList());
 		
 		patientTable.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				selectedPatient = (Patient) selection.getFirstElement();
				log.debug("Selected : "+ selectedPatient.getFirstname() + " " + selectedPatient.getLastname());
 				newPatient = false;
 				flipToNextPage = true;
 				getWizard().getContainer().updateButtons();
 			}
 		});
 		
 
 		Button btnDeletePatient = new Button(compositePatientSelection, SWT.NONE);
 		btnDeletePatient.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				MessageBox messageDialog = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
 				messageDialog.setText("Patienten lschen");
 				messageDialog.setMessage("Patient " + selectedPatient.getFirstname() + " " + selectedPatient.getLastname() + " lschen?");
 				int returnCode = messageDialog.open();
 				if (returnCode == SWT.OK){
 					deletePatient(selectedPatient);
 					patientTable.refresh();
 				}
 			}
 		});
 		btnDeletePatient.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		btnDeletePatient.setText("Patient l\u00F6schen");
 
 		Button btnAddPatient = new Button(compositePatientSelection, SWT.NONE);
 		btnAddPatient.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				newPatient = true;
 				getContainer().showPage(getNextPage());
 			}
 		});
 		btnAddPatient.setText("Patient hinzuf\u00FCgen");
 
 	}
 
 	/*private List<Patient> searchPatient(String search){
 		Query query = em.createNamedQuery("Patient.likeName");
 		query.setParameter("firstname", search.toLowerCase() + "%");
 		query.setParameter("lastname", search.toLowerCase() + "%");
 		List<Patient> result = query.getResultList();
 		return result;
 	}*/
 	
 	
 	private void deletePatient(Patient patient){
 		em.getTransaction().begin();
 		Patient mPatient = em.find(Patient.class, patient.getId());
 		em.remove(mPatient);
 		em.getTransaction().commit();
 	}
 	
 	@Override
 	public boolean canFlipToNextPage() {
 		return flipToNextPage;
 	}
 
 }
