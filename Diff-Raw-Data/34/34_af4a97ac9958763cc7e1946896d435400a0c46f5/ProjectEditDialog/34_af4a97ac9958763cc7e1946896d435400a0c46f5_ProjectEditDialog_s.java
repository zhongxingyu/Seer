 package de.hohenheim.view.dialouge;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import org.eclipse.swt.SWT;
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
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import de.hohenheim.controller.events.ProjectEvents;
 import de.hohenheim.controller.main.Main;
 
 import de.hohenheim.modell.project.Project;
 import de.hohenheim.modell.timetable.Timetable;
 import de.hohenheim.modell.train.TrainData;
 
 import de.hohenheim.view.composite.CompositeProject;
 import de.hohenheim.view.mobile.ImageHelper;
 
 /**
  * The class ProjectEditDialog contains the GUI of editing a project.
  * 
  * @author Arthur Kaul, Besim Gashi, Mathias Zwiesele, Daniel Intili, Bernd
  *         Hofs
  * 
  * @version 1.0
  */
 public class ProjectEditDialog extends Dialog {
 
 	Shell parent;
 	public static Text idText;
 	public static Text nameText;
 	public static Combo comboChooseTrain;
 	public static Combo comboChooseTimeTable;
 	String message = "";
 	public static Shell dialog;
 
 	public static Table linkTable;
 
 	/**
 	 * A Constructor for a new ProjectEdit dialog.
 	 * 
 	 * @param parent
 	 *            - component in which it's inside
 	 * @param style
 	 *            - how the style looks like
 	 */
 	public ProjectEditDialog(Shell parent, int style) {
 		super(parent, style);
 		parent = this.parent;
 	}
 
 	/**
 	 * The method open contains view elements of editing a project
 	 */
 	public void open() {
 
 		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		dialog.setSize(320, 310);
 
 		// Set the window in the middle of the window
 
 		Toolkit myToolkit = Toolkit.getDefaultToolkit();
 		Dimension myDimension = myToolkit.getScreenSize();
 		dialog.setLocation(
 				(int) ((myDimension.getWidth() - dialog.getSize().x) / 2),
 				(int) ((myDimension.getHeight() - dialog.getSize().y) / 2));
 
 		dialog.setText("Projekt bearbeiten");
 		dialog.setImage(ImageHelper.edit);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		dialog.setLayout(gridLayout);
 
 		TableItem[] rowData = CompositeProject.getProjectTable().getSelection();
 
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
 
 		// Choose Train
 
 		Label chooseTrain = new Label(dialog, SWT.NONE);
 		chooseTrain.setText("Zug whlen : ");
 
 		comboChooseTrain = new Combo(dialog, SWT.READ_ONLY);
 		String[] trainsID = new String[Main.getTrainListAll().size()];
 		comboChooseTrain.setItems(loadTrainList(trainsID));
 		gridData = new GridData();
 
 		gridData.horizontalAlignment = SWT.FILL;
 		comboChooseTrain.setLayoutData(gridData);
 
 		// Add Button
 
 		Button addButton = new Button(dialog, SWT.NONE);
 		addButton.setText("Add");
 		addButton.setImage(ImageHelper.addPlus);
 		addButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				if (linkTableCheckOk()) {
 
 					ProjectEvents.addLink(false);
 
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
 		String[] timetableID2 = new String[Main.getTimetableListAll().size()];
 		comboChooseTimeTable.setItems(loadTimetableList(timetableID2));
 		gridData = new GridData();
 
 		gridData.horizontalAlignment = SWT.FILL;
 		comboChooseTimeTable.setLayoutData(gridData);
 
 		// Remove Button
 
 		Button removeButton = new Button(dialog, SWT.NONE);
 		removeButton.setText("Remove");
 		removeButton.setImage(ImageHelper.remove);
 		removeButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				if (linkTable.getItemCount() > 0) {
 					boolean showText = false;
 
 					for (int i = 0; i < linkTable.getItemCount(); i++) {
 
 						if (linkTable.isSelected(i)) {
 							ProjectEvents.removeLink(false);
 							showText = true;
 						}
 					}
 
 					if (showText == false) {
 						MessageBox messageBox = new MessageBox(dialog,
 								SWT.ERROR | SWT.OK);
 						messageBox
 								.setMessage("Sie haben keine Zwischenstation gewhlt!"
 										+ "\r\n"
 										+ "\r\n"
 										+ "Whlen Sie einen Zwischenstation !");
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
 					ProjectEvents.editProject();
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
 		gridData = new GridData();
 		gridData.horizontalAlignment = SWT.CENTER;
 		cancelButton.setLayoutData(gridData);
 
 		cancelButton.addListener(SWT.Selection, new Listener() {
 
 			public void handleEvent(Event arg0) {
 
 				dialog.close();
 
 			}
 		});
 
 		for (int i = 0; i < Main.getProjectListAll().size(); i++) {
 
 			Project p = Main.getProjectListAll().get(i);
 
 			if (Integer.parseInt(rowData[0].getText()) == p.getId()) {
 
 				Integer idProject = Integer.valueOf(p.getId());
 
 				idText.setText(idProject.toString());
 				nameText.setText(p.getName());
 
 				Integer trainID;
 				Integer timetableID;
 
 				for (int j = 0; j < p.getTraindataProjectList().size(); j++) {
 
 					TrainData td = p.getTraindataProjectList().get(j);
 					trainID = Integer.valueOf(td.getID());
 
 					TableItem item = new TableItem(linkTable, SWT.NONE);
 					item.setText(0, trainID.toString());
 
 					Timetable tt = p.getTimeTableProjectList().get(j);
 					timetableID = Integer.valueOf(tt.getId());
 					item.setText(1, timetableID.toString());
 				}
 
 			}
 		}
 
 		dialog.open();
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
 	 * @param timetableID2
 	 * @return timetableID2 - the id's of the timetables
 	 */
 	private String[] loadTimetableList(String[] timetableID2) {
 
 		for (int i = 0; i < Main.getTimetableListAll().size(); i++) {
 			Integer id = Main.getTimetableListAll().get(i).getId();
 			timetableID2[i] = id.toString();
 		}
 
 		return timetableID2;
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
 
 		} catch (NumberFormatException e) {
 			message = message
 					+ "Die Zug ID darf nur aus Zahlen bestehen und muss mindestens \n"
 					+ "eine Ziffer enthalten!" + "\r\n";
 
 			check = false;
 		}
 
 		return check;
 	}
 
 }
