 package de.atlassoft.ui;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import de.atlassoft.application.ApplicationService;
 import de.atlassoft.model.TrainType;
 import de.atlassoft.util.I18NService;
 import de.atlassoft.util.I18NSingleton;
 import de.atlassoft.util.ImageHelper;
 
 /**
  * This class is for the TrainType Dialog where
  * you can create a new train type.
  * 
  * @author Tobias Ilg
  */
 public class TrainTypeDialog {
 	private Shell shell;
 	private ApplicationService application;
 	
 	/**
 	 * Public constructor for the TrainType Dialog. Creates a new
 	 * about shell and open it.
 	 * 
 	 * @param application
 	 * 			to access application layer
 	 */
 	
 	public TrainTypeDialog(ApplicationService application) {
 		this.application = application;
 		I18NService I18N = I18NSingleton.getInstance();
 		
 		Display display = Display.getCurrent();
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		shell.setText(I18N.getMessage("TrainTypeDialog.titleGenerate"));
 		shell.setSize(450, 200);
 		shell.setImage(ImageHelper.getImage("trainIcon"));
 		shell.setLayout(new GridLayout(3, false));
 		
 		initUI();
 		MainWindow.center(shell);
 		shell.open();
 	}
 	
 	/**
 	 * A method which creates the content, 
 	 * label, text, buttons and tool tips of
 	 * the shell of the TrainType Dialog.
 	 * 
 	 * @param display
 	 * 			The currently used display.
 	 */
	// TODO: 32x32 berprfen
 	private void initUI() {
 		final I18NService I18N = I18NSingleton.getInstance();
 		final Display display = Display.getCurrent();
 		
 		// First row with a label and a text for the name
 		new Label(shell, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.labelNameOfTrainType"));
 		final Text name = new Text(shell, SWT.BORDER);
 		final Label errorName = new Label(shell, SWT.NONE);
 		errorName.setText(I18N.getMessage("TrainTypeDialog.labelErrorExistingName"));
 		errorName.setForeground(display.getSystemColor(SWT.COLOR_RED));
 		errorName.setVisible(false);		
 		name.setToolTipText(I18N.getMessage("TrainTypeDialog.textNameOfTrainType"));
 		final java.util.List<TrainType> trainTypes = application.getModel().getTrainTypes();
 		
 		// Second row with two labels and a text for the topSpeed
 		new Label (shell, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.label1Speed"));
 		final Text textSpeed = new Text(shell, SWT.BORDER);
 		Composite trainTypeDialogSpeed = new Composite (shell, SWT.NULL);
 		trainTypeDialogSpeed.setLayout(new GridLayout(2, false));
 		new Label(trainTypeDialogSpeed, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.label2Speed"));
 		textSpeed.setToolTipText(I18N.getMessage("TrainTypeDialog.textSpeed"));
 		// Only numbers are allowed to enter
 		textSpeed.addVerifyListener(new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				if (!Character.isDigit(e.character) && !Character.isISOControl(e.character)) {
 		          e.doit = false;
 		          display.beep();
 		        }
 			}
 		});
 		
 		// Third row with a label and a combo for the Priority (1-5)
 		new Label(shell, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.labelPriority"));
 		final String[] prioritySelection = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
 		final Combo comboPriority = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    comboPriority.setItems(prioritySelection);
 	    comboPriority.select(0);
 		new Label(shell, SWT.NONE);
 		comboPriority.setToolTipText(I18N.getMessage("TrainTypeDialog.textPriority"));
 		
 		// Third row with text and button, to get an image
 		new Label(shell, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.labelImage1"));
 		final Text textImage = new Text(shell, SWT.BORDER);
 		new Label(shell, SWT.NONE).setText(I18N.getMessage("TrainTypeDialog.labelImage2"));
 		
 		
 		// Search row for the optics
 		new Label(shell, SWT.NONE);
 		Button imageSearch = new Button(shell, SWT.NULL);
 		imageSearch.setText(I18N.getMessage("TrainTypeDialog.buttonImage"));
 		imageSearch.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
	    		final String selected;
 	        	FileDialog fd = new FileDialog(shell, SWT.OPEN);
 	            fd.setText(I18N.getMessage("TrainTypeDialog.openFileDialog"));
 	            fd.setFilterPath("C:/");
 	            String[] filterN = {"Alle Bilddateien", "Bitmap (*.bmp)", "JointPhotographersExpertGroup (*.jpg, *.jpeg, *.jpe)",
 	            		"GraphicImageFormat (*.gif)", "Icon (*.ico)", "WindowsMetaFile (*.wmf)", "Komprimierte Bitmap (*.pcx)",
 	            		"Portable Network Graphics (*.png)", "CorelDraw (*.cdr)", "CorelPaint (*.cpt)", "Picture-Bitmap-Date (*.pic)"};
 	            fd.setFilterNames(filterN);
 	            String[] filterExt = { "*.jpg; *.jpeg; *.jpe; *.tif; *.tiff; *.ico; " +
 	            		"*.ani; *.wmf; *.pcx; *.cdr; *.cpt; *.pci; *.png; *.gif; *.bmp",
 	            		"*.bmp", "*.jpg; *.jpeg; *.jpe", "*.gif", "*.ico", "*.wmf", "*.pcx",
 	            		"*.png", "*.cdr", "*.cpt", "*.pci" };
 	            fd.setFilterExtensions(filterExt);
	            if ((selected = fd.open()) != null) {
 	            	Image imageTrainType = new Image (null, selected);
 	            	Rectangle xy = imageTrainType.getBounds();
 	            	if (xy.width == 32 && xy.height == 32) {
 	            		textImage.setText(selected);
 	            	} else {
 		        		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
 		        		messageBox.setText("TrainTypeDialog.errorWrongBoundsTitle");
 		        	    messageBox.setMessage(I18N.getMessage("TrainTypeDialog.errorWrongBoundsMessage"));
 		        	    messageBox.open();
		        	    fd.open();
 	            	}
 	            }
 			}
 	    });
 		new Label(shell, SWT.NONE);
 		
 		//Empty Row
 		new Label(shell, SWT.NONE);
 		new Label(shell, SWT.NONE);
 		new Label(shell, SWT.NONE);		
 		
 		// Fourth row with three buttons for Save, Information and Cancel
 		// Save Button
 		final Button save = new Button(shell, SWT.PUSH);
 		save.setText(I18N.getMessage("TrainTypeDialog.buttonSave"));
 	    save.setImage(ImageHelper.getImage("greenCheck"));
 	    save.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
 	        	// read values
 	        	final String nameOfTrainType = name.getText();
 	        	final String topSpeed = textSpeed.getText();
 	        	final String priority = comboPriority.getText();	        	
 	        	
 	        	// check constraints
 	        	List<String> errorNameMessages = new ArrayList<String>();
 	        	
 	        	if (name.getText().trim().equals("")) {
 	        	    errorNameMessages.add(I18N.getMessage("TrainTypeDialog.errorEmptyName"));   	    
 	        	}
 	        	if (textSpeed.getText().equals("")) {
 	        		errorNameMessages.add(I18N.getMessage("TrainTypeDialog.errorEmptySpeed"));
 	        	}
 	        	if (!textSpeed.getText().isEmpty()) {
 	        		if (textSpeed.getText().substring(0, 1).equals("0")){
 	        			errorNameMessages.add(I18N.getMessage("TrainTypeDialog.errorZeroSpeed"));
 	        		}
 	        	}
 	        	
 	        	// generating and open errorNameMessageBox
 	        	if (!errorNameMessages.isEmpty()) {
 	        		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
 	        	    messageBox.setText(I18N.getMessage("TrainTypeDialog.errorMissingInput"));
 	        	    int number = errorNameMessages.size();
 	        	    if (number == 2) {
 	        	    	messageBox.setMessage(errorNameMessages.get(0) + "\n" + errorNameMessages.get(1));
 		        	    messageBox.open();
 	        	    } else {
 	        	    	messageBox.setMessage(errorNameMessages.get(0));
 		        	    messageBox.open();
 	        	    }
 	        	
 	        	// surrender Inputs to ApplicationService and generating and open InformationMessageBox
 	        	} else {
 	        		TrainType type = new TrainType(nameOfTrainType, Double.parseDouble(topSpeed), Integer.parseInt(priority));
 	        		application.addTrainType(type);
 	        		if (!textImage.getText().equals("")){
 	        			Image img = new Image(null, textImage.getText());
 	        			type.setImg(img);
 	        		}
 	        		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
 	        		messageBox.setText(I18N.getMessage("TrainTypeDialog.informationSaved"));
 	        	    messageBox.setMessage(I18N.getMessage("TrainTypeDialog.informationTrainTypeSaved"));
 	        	    shell.setVisible(false);
 	        	    messageBox.open();
 	        	    shell.close();
 	        	}
 	        }
 	    });
 	    
 	    // Information Button
 		Button help = new Button(shell, SWT.PUSH);
 		help.setImage(ImageHelper.getImage("questionMark"));
 		help.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
 	        	MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
         		messageBox.setText(I18N.getMessage("TrainTypeDialog.helpTitle"));
         	    messageBox.setMessage(I18N.getMessage("TrainTypeDialog.helpText"));
         	    messageBox.open();
 	        }
 	    });
 		
 		// Cancel Button
 		Button cancel = new Button(shell, SWT.PUSH | SWT.RIGHT);
 		cancel.setText(I18N.getMessage("TrainTypeDialog.buttonCancel"));
 	    cancel.setImage(ImageHelper.getImage("cancelIcon"));
 	    cancel.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
 	        	MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION |SWT.YES | SWT.NO);
 	            messageBox.setMessage(I18N.getMessage("TrainTypeDialog.cancelQuestion"));
 	            int rc = messageBox.open();
 	            if (rc == SWT.YES) {
 	            	shell.close();
 	            }
 	        }
 	    });
 	    
 	    // Name On the Fly Exception
 		name.addListener(SWT.Verify, new Listener() {
 			public void handleEvent(Event e) {
 		    	Boolean twice = false;
 		    	if (e.keyCode != 8) {
 		    		for (TrainType type : trainTypes) {
 		    			if (type.getName().equals(name.getText() + e.text)) {
 		    				twice = true;
 		    			}
 		    		}
 		    	} else {
 		    		for (TrainType type : trainTypes) {
 		    			if (type.getName().equals(name.getText().substring(0, name.getText().length() - 1))) {
 		    				twice = true;
 		    			}
 		    		}
 		    	}
 		    	if (twice == true) {
 		    		errorName.setVisible(true);
 		   		  	save.setEnabled(false);
 		    	} else {
 		    		errorName.setVisible(false);
 		    		save.setEnabled(true);
 		    	}
 			}
 		});
 	    
 		shell.pack();
 	}
 }
