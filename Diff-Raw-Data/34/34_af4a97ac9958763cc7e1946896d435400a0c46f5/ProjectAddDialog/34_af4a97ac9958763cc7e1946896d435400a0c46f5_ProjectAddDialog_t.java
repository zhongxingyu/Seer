 package de.hohenheim.view.dialouge;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import de.hohenheim.controller.events.ProjectEvents;
 import de.hohenheim.controller.main.Main;
 import de.hohenheim.modell.timetable.Timetable;
 import de.hohenheim.modell.train.TrainData;
 import de.hohenheim.view.canvas.AnimationControllerCanvas;
 import de.hohenheim.view.mobile.ImageHelper;
 
 /**
  * The class ProjectAddDialog contains the GUI of adding a project.
  * 
  * @author Arthur Kaul, Besim Gashi, Mathias Zwiesele, Daniel Intili, Bernd
  *         Hofs
  * 
  * @version 1.0
  */
 public class ProjectAddDialog extends Dialog {
 
 	Shell parent;
 	public static Shell dialog;
 	public static Text idText;
 	public static Text nameText;
 	public static Combo comboChooseTrain;
 	public static Combo comboChooseTimeTable;
 
 	String message = "";
 
 	public static Table linkTable;
 
 	/**
 	 * A Constructor for a new ProjectAdd dialog.
 	 * 
 	 * @param parent
 	 *            - component in which it's inside
 	 * @param style
 	 *            - how the style looks like
 	 */
 	public ProjectAddDialog(Shell parent, int style) {
 		super(parent, style);
 		parent = this.parent;
 	}
 
 	/**
 	 * The method open is responsible for the view elements of adding an
 	 * project.
 	 */
 	public void open() {
 
 		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		dialog.setSize(340, 310);
 
 		// Set the window in the middle
 
 		Toolkit myToolkit = Toolkit.getDefaultToolkit();
 		Dimension myDimension = myToolkit.getScreenSize();
 		dialog.setLocation(
 				(int) ((myDimension.getWidth() - dialog.getSize().x) / 2),
 				(int) ((myDimension.getHeight() - dialog.getSize().y) / 2));
 
 		dialog.setText("Projekt hinzufgen");
 		dialog.setImage(ImageHelper.add);
 		;
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		dialog.setLayout(gridLayout);
 
 		// Project ID
 
 		Label id = new Label(dialog, SWT.NONE);
 		id.setText("ID : ");
 
 		idText = new Text(dialog, SWT.NONE);
 		GridData gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		gridData.horizontalAlignment = SWT.FILL;
 		idText.setLayoutData(gridData);
 
 		// Project Name
 
 		Label name = new Label(dialog, SWT.NONE);
 		name.setText("Name : ");
 
 		nameText = new Text(dialog, SWT.NONE);
 		gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		gridData.horizontalAlignment = SWT.FILL;
 		nameText.setLayoutData(gridData);
 		nameText.setText("Unbennant");
 
 		// Choose Train
 
 		Label chooseTrain = new Label(dialog, SWT.NONE);
 		chooseTrain.setText("Zug whlen : ");
 
 		comboChooseTrain = new Combo(dialog, SWT.READ_ONLY);
 		String[] trainsID = new String[Main.getTrainListAll().size()];
 		comboChooseTrain.setItems(loadTrainList(trainsID));
 		gridData = new GridData();
 
 		gridData.horizontalAlignment = SWT.FILL;
 		comboChooseTrain.setLayoutData(gridData);
 		comboChooseTrain.select(0);
 
 		// Add Button
 
 		Button addButton = new Button(dialog, SWT.NONE);
 		addButton.setText("Add");
 		addButton.setImage(ImageHelper.add);
 		addButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				if (linkTableCheckOk()) {
 
 					ProjectEvents.addLink(true);
 
 				} else {
 
 					MessageBox messageBox = new MessageBox(dialog, SWT.ERROR
 							| SWT.OK);
 					messageBox.setMessage(message);
 					messageBox.open();
 
 				}
 
 			}
 		});
 
 		// Choose TimeTable
 
 		Label chooseTimeTable = new Label(dialog, SWT.NONE);
 		chooseTimeTable.setText("Fahrplan whlen : ");
 
 		comboChooseTimeTable = new Combo(dialog, SWT.READ_ONLY);
 		String[] timetableID = new String[Main.getTimetableListAll().size()];
 		comboChooseTimeTable.setItems(loadTimetableList(timetableID));
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.FILL;
 		comboChooseTimeTable.setLayoutData(gridData);
 		comboChooseTimeTable.select(0);
 
 		// Remove Button
 
 		Button removeButton = new Button(dialog, SWT.NONE);
 		removeButton.setText("Remove");
 		removeButton.setImage(ImageHelper.remove);
 		removeButton.setImage(new Image(null, "img/Clear.png"));
 		removeButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				if (linkTable.getItemCount() > 0) {
 					boolean showText = false;
 
 					for (int i = 0; i < linkTable.getItemCount(); i++) {
 
 						if (linkTable.isSelected(i)) {
 							ProjectEvents.removeLink(true);
 							showText = true;
 						}
 					}
 
 					if (showText == false) {
 						MessageBox messageBox = new MessageBox(dialog,
 								SWT.ERROR | SWT.OK);
 						messageBox
 								.setMessage("Sie haben keine Zug/Fahrplan Kombination gewhlt!"
 										+ "\r\n"
 										+ "\r\n"
 										+ "Whlen Sie eine Zug/Fahrplan Kombination aus !");
 						messageBox.open();
 					}
 				} else {
 					MessageBox messageBox = new MessageBox(dialog, SWT.ERROR
 							| SWT.OK);
 					messageBox
 							.setMessage("Es sind keine Zwischenstationen vorhanden, die gelscht werden knnen!");
 					messageBox.open();
 				}
 
 			}
 		});
 
 		// Linked List
 
 		Label room6 = new Label(dialog, SWT.NONE);
 		room6.setText("");
 
 		Composite tableComposite = new Composite(dialog, SWT.NONE);
 		gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		gridData.horizontalAlignment = SWT.FILL;
 		tableComposite.setLayoutData(gridData);
 		tableComposite.setLayout(new FillLayout());
 
 		linkTable = new Table(tableComposite, SWT.NONE);
 
 		linkTable.setLinesVisible(true);
 
 		TableColumn trains = new TableColumn(linkTable, SWT.None);
 		TableColumn timetable = new TableColumn(linkTable, SWT.NONE);
 		trains.setWidth(100);
 		timetable.setWidth(100);
 
 		// Button Composite
 
 		Composite buttonComposite = new Composite(dialog, SWT.NONE);
 		GridLayout gridLayout2 = new GridLayout();
 		gridLayout2.numColumns = 2;
 		buttonComposite.setLayout(gridLayout2);
 
 		// OK Button
 
 		Button okButton = new Button(dialog, SWT.NONE);
 		okButton.setText("OK");
 		okButton.setImage(ImageHelper.ok);
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.CENTER;
 		okButton.setLayoutData(gridData);
 
 		okButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 				if (projectCheckOk()) {
 					ProjectEvents.addProject();
 					String[] projectIDs = new String[Main.getProjectListAll()
 							.size()];
 					AnimationControllerCanvas.comboProjects
 							.setItems(AnimationControllerCanvas
 									.loadProjectIDs(projectIDs));
 				} else {
 					MessageBox messageBox = new MessageBox(dialog, SWT.ERROR
 							| SWT.OK);
 					messageBox.setMessage(message);
 					messageBox.open();
 				}
 			}
 		});
 
 		// Cancel Button
 
 		Button cancelButton = new Button(dialog, SWT.NONE);
 		cancelButton.setText("Cancel");
 		cancelButton.setImage(ImageHelper.cancel);
 		cancelButton.setImage(new Image(null, "img/Cancel.png"));
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.CENTER;
 		cancelButton.setLayoutData(gridData);
 
 		cancelButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				dialog.close();
 
 			}
 		});
 
 		dialog.open();
 	}
 
 	/**
 	 * The method linkTableCheckOK proves the items. If there is an identical
 	 * train or timetable, errors will be displays
 	 * 
 	 * @return check - if the statements are wrong, errors will be displays
 	 */
 	protected boolean linkTableCheckOk() {
 		message = "";
 		boolean check = true;
 
 		Timetable tt = null;
		for(int k = 0; k < Main.getTimetableListAll().size(); k++){
 			if (Integer.parseInt(comboChooseTimeTable.getText()) == Main.getTimetableListAll().get(k).getId()){ 
 				tt = Main.getTimetableListAll().get(k);
 			}
 			
 		}
 		
 		for (int j = 0; j < linkTable.getItemCount(); j++) {
 
 			if (Integer.parseInt(comboChooseTrain.getText()) == Integer
 					.parseInt(linkTable.getItem(j).getText(0))) {
 
 				message = message
 						+ "Der ausgewhlte Zug ist schon vorhanden !\n";
 				check = false;
 			}
 
 			if (Integer.parseInt(comboChooseTimeTable.getText()) == Integer
 					.parseInt(linkTable.getItem(j).getText(1))) {
 
 				message = message
 						+ "Der ausgewhlte Fahrplan ist schon vorhanden !\n";
 				check = false;
 			}
 			
 			Timetable tt2 = null;
 			for(int i = 0; i < Main.getTimetableListAll().size(); i++){
 				
 				if (Integer.parseInt(linkTable.getItem(j).getText(1)) == Main.getTimetableListAll().get(i).getId()){ 
 					tt2 = Main.getTimetableListAll().get(i);
 				}
 			}
 			
 			if(tt.getStartstation() == tt2.getStartstation()){
 				
 				message = message + "Es knnen keine zwei Fahrplne mit der selben \n"+
 				                    "Startstation in einem Projekt existieren !\n";
 				check = false;
 			}
 
 		}
 
 		return check;
 	}
 
 	/**
 	 * The method projectCheckOK prove the entries. If there is a wrong input or
 	 * a blank box, exceptions will be called in terms of error messages.
 	 * 
 	 * @return check - if the statements are wrong, an error message will be
 	 *         display
 	 */
 	protected boolean projectCheckOk() {
 
 		message = "";
 		boolean check = true;
 
 		try {
 			if (linkTable.getItemCount() < 1) {
 				message = message
 						+ "Es muss mindestens ein Zug und mindestens ein Fahrplan"
 						+ "\n" + "eingetragen sein!\n" + "\r\n";
 				check = false;
 			}
 
 			if (nameText.getText().equalsIgnoreCase("")) {
 				message = message
 						+ "Bitte geben Sie einen Projekt-Namen ein!\n" + "\r\n";
 				check = false;
 			}
 
 			int id = Integer.parseInt(idText.getText());
 			if (id < 0) {
 				message = message
 						+ "Die Projekt-ID muss eine positive Zahl enthalten!\n"
 						+ "\r\n";
 				check = false;
 			}
 			for (int j = 0; j < Main.getProjectListAll().size(); j++) {
 				if (id == Main.getProjectListAll().get(j).getId()) {
 
 					message = message
 							+ "Die ID-Eingabe ist bereits vorhanden. Bitte  \n "
 							+ " geben sie eine andere 6-stellige Ziffer ein und versuchen Sie es erneut. \n";
 					check = false;
 				}
 			}
 
 		} catch (NumberFormatException e) {
 			message = message
 					+ "Die Zug ID darf nur aus Zahlen bestehen und muss mindestens \n"
 					+ "eine Ziffer enthalten!" + "\r\n";
 
 			check = false;
 		}
 
 		return check;
 	}
 
 	/**
 	 * Load the trainId's into a String[]
 	 * 
 	 * @param trainsID
 	 * @return trainsID - the id's of the trains
 	 */
 	private String[] loadTrainList(String[] trainsID) {
 
 		for (int i = 0; i < Main.getTrainListAll().size(); i++) {
 			Integer id = Main.getTrainListAll().get(i).getID();
 			trainsID[i] = id.toString();
 		}
 
 		return trainsID;
 	}
 
 	/**
 	 * Load the timetableID's into a String[]
 	 * 
 	 * @param timetableID
 	 * @return timetableID - the id's of the timetables
 	 */
 	private String[] loadTimetableList(String[] timetableID) {
 
 		for (int i = 0; i < Main.getTimetableListAll().size(); i++) {
 			Integer id = Main.getTimetableListAll().get(i).getId();
 			timetableID[i] = id.toString();
 		}
 
 		return timetableID;
 	}
 
 }
