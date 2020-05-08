 package ch.bfh.bti7081.s2013.blue.ui;
 
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ch.bfh.bti7081.s2013.blue.entities.MedicalDrug;
 import ch.bfh.bti7081.s2013.blue.entities.Patient;
 import ch.bfh.bti7081.s2013.blue.service.DailyPrescription;
 import ch.bfh.bti7081.s2013.blue.service.PatientService;
 import ch.bfh.bti7081.s2013.blue.service.PrescriptionService;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.converter.Converter.ConversionException;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table.ColumnHeaderMode;
 import com.vaadin.ui.TreeTable;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("serial")
 public class PatientDetailView extends VerticalLayout implements View, IBackButtonView {
 	private Label firstNameLabel;
 	private Label lastNameLabel;
 	private TreeTable prescriptionTable;
 	private Button scanButton;
 
 	public PatientDetailView() {
 		setSizeFull();
 		
 		// information form
 		FormLayout formLayout = new FormLayout();
 		firstNameLabel = new Label();
 		lastNameLabel = new Label();
 		firstNameLabel.setCaption("Vorname:");
 		lastNameLabel.setCaption("Nachname:");
 		formLayout.addComponent(firstNameLabel);
 		formLayout.addComponent(lastNameLabel);
 		addComponent(formLayout);
 		
 		// prescriptions
 		scanButton = new Button("Scan Medikamente");
 		addComponent(scanButton);
 		prescriptionTable = new TreeTable();
 		prescriptionTable.setWidth("400px");
 		addComponent(prescriptionTable);
 		prescriptionTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
 		prescriptionTable.addContainerProperty("checkbox", CheckBox.class, "");
 		
 		initScanButton();
 	}
 	
 	@Override
 	public void enter(ViewChangeEvent event) {
 		Long id = Long.parseLong(event.getParameters());
 		Patient patient = PatientService.getInstance().createContainer().getItem(id).getEntity();
 		firstNameLabel.setValue(patient.getFirstName());
 		lastNameLabel.setValue(patient.getLastName());
 		
 		List<DailyPrescription> dailyPrescriptions = PrescriptionService.getInstance().getDailyPrescriptions(patient);
 		for (DailyPrescription dailyPrescription : dailyPrescriptions) {
 			String date = DateFormat.getInstance().format(dailyPrescription.getDate());
 			
 			TreeCheckBox box = new TreeCheckBox(date);
 			Object dateId = prescriptionTable.addItem(new Object[] {box}, date);
 			box.setData(dateId);
 			
 			addDailyPrescription("Morgens", dateId, dailyPrescription.getMorningDrugs());
 			addDailyPrescription("Mittags", dateId, dailyPrescription.getNoonDrugs());
 			addDailyPrescription("Abends", dateId, dailyPrescription.getEveningDrugs());
 			addDailyPrescription("Nachts", dateId, dailyPrescription.getNightDrugs());
 		}
 	}
 	
 	private void addDailyPrescription(String name, Object parentId, Map<MedicalDrug, Integer> drugs) {
 		if (drugs.size() == 0) {
 			return;
 		}
 		TreeCheckBox timeCheckBox = new TreeCheckBox(name);
 		Object id = prescriptionTable.addItem(new Object[] {timeCheckBox}, null);
 		timeCheckBox.setData(id);
 		prescriptionTable.setParent(id, parentId);
 		
 		for (Entry<MedicalDrug, Integer> drug : drugs.entrySet()) {
 			String value = drug.getValue().toString() + " " + drug.getKey().getName();
 			CheckBox drugBox = new CheckBox(value);
 			drugBox.setData(drug);
 			Object entryId = prescriptionTable.addItem(new Object[] {drugBox}, null);
 			
 			prescriptionTable.setParent(entryId, id);
 			prescriptionTable.setChildrenAllowed(entryId, false);
 		}
 	}
 	
 	/*
 	 * CheckBox which checks/unchecks children items if checked/unchecked
 	 */
 	class TreeCheckBox extends CheckBox {
 		public TreeCheckBox(String caption) {
 			super(caption);
 		}
 		@Override
 		public void setValue(Boolean newFieldValue) throws
 				ReadOnlyException,
 				ConversionException {
 			super.setValue(newFieldValue);
 			Collection<?> children = prescriptionTable.getChildren(getData());
 			if (children != null) {
 				for (Object id : children) {
 					@SuppressWarnings("unchecked")
 					Property<CheckBox> property = prescriptionTable.getItem(id).getItemProperty("checkbox");
 					CheckBox checkBox = property.getValue();
 					checkBox.setValue(newFieldValue);
 				}
 			}
 		}
 		
 	};
 	
 	/*
 	 * write selected items to session and navigate to the scan view
 	 */
 	private void initScanButton() {
 		scanButton.addClickListener(new Button.ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				Map<String, ScanPrescription> drugsToScan = new HashMap<String, ScanPrescription>();
 				
 				Collection<?> itemIds = prescriptionTable.getItemIds();
 				for (Object id : itemIds) {
 					@SuppressWarnings("unchecked")
 					Property<CheckBox> property = prescriptionTable.getItem(id).getItemProperty("checkbox");
 					CheckBox checkBox = property.getValue();
					if (checkBox.getValue() && (checkBox.getData() instanceof Entry<?, ?>)) {
 						@SuppressWarnings("unchecked")
 						Entry<MedicalDrug, Integer> entry = (Entry<MedicalDrug, Integer>) checkBox.getData();
 						
 						ScanPrescription scanPrescription = drugsToScan.get(entry.getKey().getSwissmedicNumber());
 						if (scanPrescription == null) {
 							scanPrescription = new ScanPrescription();
 							scanPrescription.setCount(entry.getValue());
 							scanPrescription.setMedicalDrug(entry.getKey());
 						} else {
 							scanPrescription.setCount(scanPrescription.getCount() + entry.getValue());
 						}
 						drugsToScan.put(entry.getKey().getSwissmedicNumber(), scanPrescription);
 					}
 				}
 				getSession().setAttribute(NavigatorUI.DRUGS_TO_SCAN_SESSION, drugsToScan);
 				UI.getCurrent().getNavigator().navigateTo(NavigatorUI.SCAN_VIEW);
 			}
 		});
 	}
 
 	@Override
 	public String getBackView() {
 		return NavigatorUI.PATIENT_SEARCH_VIEW;
 	}
 }
