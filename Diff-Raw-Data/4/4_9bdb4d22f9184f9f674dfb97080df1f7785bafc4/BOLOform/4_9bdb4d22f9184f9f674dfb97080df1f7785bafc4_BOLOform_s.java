 package boloTab;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerDateModel;
 
 import net.miginfocom.swing.MigLayout;
 import program.ResourceManager;
 import utilities.FileHelper;
 import utilities.ui.ImageHandler;
 import utilities.ui.ImagePreview;
 import utilities.ui.ResizablePhotoDialog;
 import utilities.ui.SwingHelper;
 //-----------------------------------------------------------------------------
 /**
  * The <code>BOLOform</code> class is where the information of a given <code>Bolo</code>
  * is entered by the user. 
  */
 public class BOLOform extends JDialog {
 private static final long serialVersionUID = 1L;
 	JTextField ageField,raceField,sexField,heightField,weightField,buildField;
 	JTextField eyesField,hairField;
 	JTextField toiField,referenceField,caseNumField,ifYesField;
 	JTextField preparedByField,approvedByField,dateField,timeField;
 	JTextArea otherDescriptField,narrativeText; 
 	JComboBox<String> statusField;
 	JSpinner incidentDate, incidentTime, preparedDate, preparedTime;
 	Bolo bolo;
 	ResourceManager rm;
 	JFrame parent;
 	JPanel photoArea;
 	JPanel dialogPanel;
 	/** a reference to the main <code>BOLOtab</code> used to tell 
 	 * <code>BOLOtab</code> to refresh its contents after a delete operation */
 	BOLOtab bolotab;
 	/** lets the main BOLOtab know if a new BOLO was created during the last
 	 * invocation of this dialog */
 	boolean newBOLOWascreated; 
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates a new window, sets the window and creates a new <code>Bolo</code> instance
 	 * 
 	 * @param parent
 	 */
 	public BOLOform(ResourceManager rm, BOLOtab bolotab){
 		super(rm.getGuiParent(), "New BOLO", true);
 		this.rm=rm;
 		this.parent = parent;
 		this.bolotab=bolotab;
 
 		//Create the BOLO object to add info to
 		bolo = new Bolo();
 
 		//Set the size of the form
 		this.setPreferredSize(new Dimension(1050,900));
 		this.setSize(new Dimension(950,900));
 
 		dialogPanel = new JPanel(new MigLayout("ins 20", "[]5%[]", ""));
 
 		//Make the form scrollable
 		JScrollPane dialogPanelScroller = new JScrollPane(dialogPanel);
 		dialogPanelScroller.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		//Put the form in the middle of the screen
 		this.setLocationRelativeTo(null);
 
 		//Make sure that if the user hits the 'x', the window calls the closeAndCancel method
 		this.addWindowListener(new WindowAdapter( ) {
 			public void windowClosing(WindowEvent e) {
 				closeAndCancel();
 			}
 		});
 
 		//Set up the new BOLO form
 
 		//Add the BOLO "letter head" image to the top
 		ImageIcon boloHeaderIcon = 
 				ImageHandler.createImageIcon("images/boloHeader2.png");
 		JPanel boloHeaderPanel = new JPanel();
 		boloHeaderPanel.add(new JLabel(boloHeaderIcon));
 		dialogPanel.add(boloHeaderPanel, "dock north");
 
 		//Add photo/video panel
 		JPanel photoVideoPanel = createPhotoVideoPanel();
 		dialogPanel.add(photoVideoPanel, "align left");
 
 		//Add physical description panel
 	    JPanel physicalDescriptPanel = createPhysicalDescriptionPanel();
 	    dialogPanel.add(physicalDescriptPanel, "align left, wrap");
 
 	    //Add incident info panel
 	    JPanel incidentInfoPanel = createIncidentInfoPanel();
 	    dialogPanel.add(incidentInfoPanel, "align left, growx");
 
 	    //Add narrative area
 	    JPanel narrativePanel = createNarrativePanel();
 	    dialogPanel.add(narrativePanel, "align left, wrap, growx");
 
 	    //Add administrative panel
 	    JPanel adminPanel = createAdministrativePanel();
 	    dialogPanel.add(adminPanel, "align left");
 
 //TODO: Add standard footer
 
 	    //Add buttons panel to top of scroll panel as the row header
 	    //	(the buttons panel stays at the top of the screen even if the top of the form isn't
 	    //	currently visible) 
 	    JPanel buttonsPanel = createButtonsPanel();
 	    dialogPanelScroller.setColumnHeaderView(buttonsPanel);	    
 
 	    //Add the BOLO form scrolling pane dialog to the screen
 	    Container contentPane = getContentPane();
 	    contentPane.add(dialogPanelScroller);
 
 	}
 //-----------------------------------------------------------------------------	
 	/**
 	 * JDOC
 	 * 
 	 * @param parent
 	 * @param bolo
 	 * @see loadFromExistingBOLO()
 	 */
 	public BOLOform(ResourceManager rm, BOLOtab bolotab, Bolo bolo){
 		this(rm, bolotab);
 		this.bolo = bolo;
 		loadFromExistingBOLO();
 	}
 //-----------------------------------------------------------------------------	
 	/**
 	 * 
 	 */
 	private JPanel createPhysicalDescriptionPanel(){
 		JPanel infoPanel = new JPanel(new MigLayout("","","[][][][][][nogrid]"));
 
 		SwingHelper.addTitledBorder(infoPanel, "Physical Description");
 
         // create labels
 		JLabel ageLabel = new JLabel("<html>Approx.<br>Age</html>");
 		JLabel raceLabel = new JLabel("Race");
 		JLabel sexLabel = new JLabel("Sex");
 		JLabel heightLabel = new JLabel("<html>Approx.<br>Height</html>");
 		JLabel weightLabel = new JLabel("<html>Approx.<br>Weight</html>");
 		JLabel buildLabel = new JLabel("Build");
 		JLabel eyesLabel = new JLabel("Eyes");
 		JLabel hairLabel = new JLabel("Hair");
 		JLabel otherDescriptionLabel = new JLabel("Other Description/Info");
 
 		// create fields
 		ageField = new JTextField(4);
 		raceField = new JTextField(SwingHelper.EXTRA_SMALL_TEXT_FIELD_LENGTH);
 		sexField = new JTextField(SwingHelper.ONE_CHAR_TEXT_FIELD_LENGTH);
 		heightField = new JTextField(SwingHelper.EXTRA_SMALL_TEXT_FIELD_LENGTH);
 		weightField = new JTextField(SwingHelper.EXTRA_SMALL_TEXT_FIELD_LENGTH);
 		buildField = new JTextField(10);
 		eyesField = new JTextField(10);
 		hairField = new JTextField(10);
 		otherDescriptField = new JTextArea(5, 40);
 		otherDescriptField.setLineWrap(true);
 		JScrollPane otherDescriptScrollPane = new JScrollPane(otherDescriptField);
 		otherDescriptScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		ifYesField = new JTextField(SwingHelper.MEDIUM_TEXT_FIELD_LENGTH);
 
 		//add row 1 fields
 		infoPanel.add(ageLabel, "align left");
 		infoPanel.add(ageField, "align left");
 		infoPanel.add(raceLabel, "align left");
 		infoPanel.add(raceField, "align left");
 		infoPanel.add(sexLabel, "align left");
 		infoPanel.add(sexField, "align left, wrap");
 		//add row 2 fields
 		infoPanel.add(heightLabel, "align left");
 		infoPanel.add(heightField, "align left");
 		infoPanel.add(weightLabel, "align left");
 		infoPanel.add(weightField, "align left");
 		infoPanel.add(buildLabel, "align left");
 		infoPanel.add(buildField, "align left, wrap");
 		//add row 3 fiels
 		infoPanel.add(eyesLabel, "align left");
 		infoPanel.add(eyesField, "align left");
 		infoPanel.add(hairLabel, "align left");
 		infoPanel.add(hairField, "align left, wrap");
 		//add other description area
 		infoPanel.add(otherDescriptionLabel, "spanx");
 		infoPanel.add(otherDescriptScrollPane, "spanx, wrap");
 		//add "armed?" area
 		SwingHelper.addArmedQuestionCheckboxes(infoPanel, ifYesField);
 
 		return infoPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private JPanel createIncidentInfoPanel(){
 		JPanel infoPanel = new JPanel(new MigLayout());
 
 		SwingHelper.addTitledBorder(infoPanel, "Incident Info");
 
         // create labels
 		JLabel referenceLabel = new JLabel("Reference");
 		JLabel caseNumLabel = new JLabel("Case #");
 		JLabel statusLabel = new JLabel("Status");
 
 
 		// create fields
 		String[] statusStrings = { "", "Need to Identify", "Identified", "Apprehended", "Cleared" };
 		referenceField = new JTextField(15);
 		caseNumField = new JTextField(15);
 		statusField = new JComboBox<String>(statusStrings);
 		
 		//row 1
 		incidentDate = SwingHelper.addDateSpinner(infoPanel, "Date of Incident");
 		incidentTime = SwingHelper.addTimeSpinner(infoPanel, "Time of Incident");
 		infoPanel.add(referenceLabel, "align");
 		infoPanel.add(referenceField, "align, wrap");
 		infoPanel.add(caseNumLabel, "align");
 		infoPanel.add(caseNumField, "align, wrap");
 		infoPanel.add(statusLabel, "align");
 		infoPanel.add(statusField, "align, wrap");
 
 		return infoPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private JPanel createNarrativePanel(){
 		JPanel narrativePanel = new JPanel(new MigLayout());
 
 		SwingHelper.addTitledBorder(narrativePanel, "Narrative/Remarks");
 
 		// create fields
 		narrativeText = new JTextArea(10, 30);
 		narrativeText.setLineWrap(true);
 		JScrollPane otherDescriptScrollPane = new JScrollPane(narrativeText);
 		otherDescriptScrollPane.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		//add other description area
 		narrativePanel.add(otherDescriptScrollPane, "growx, align center");
 
 		return narrativePanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private JPanel createAdministrativePanel(){
 		JPanel adminPanel = new JPanel(new MigLayout());
 
 		SwingHelper.addTitledBorder(adminPanel, "Administrative Info");
 
         // create labels
 		JLabel preparedByLabel = new JLabel("BOLO prepared by");
 		JLabel approvedByLabel = new JLabel("BOLO approved by");
 
 		// create fields
 		preparedByField = new JTextField(15);
 		approvedByField = new JTextField(15);
 
 		//add labels & text fields to  panel
 		adminPanel.add(preparedByLabel, "align");
 		adminPanel.add(preparedByField, "align, wrap");
 		adminPanel.add(approvedByLabel, "align");
 		adminPanel.add(approvedByField, "align, wrap");
 		preparedDate = SwingHelper.addDateSpinner(adminPanel, "Date BOLO prepared");
 		preparedTime = SwingHelper.addTimeSpinner(adminPanel, "Time BOLO prepared");
 
 
 		return adminPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private JPanel createPhotoVideoPanel(){
 		JPanel photoVideoPanel = new JPanel(new MigLayout("fill"));
 
 		//Create initial no-photo place holder photo
 		final JPanel photoPanel = new JPanel();
 		photoArea = photoPanel;
 		ImageIcon noPhotoImage = ImageHandler.createImageIcon("images/unknownPerson.jpeg");
 		JLabel noPhotoLabel = new JLabel(noPhotoImage);
 		photoPanel.add(noPhotoLabel);
 		photoVideoPanel.add(photoPanel, "spanx,grow,wrap");
 
 		JButton addPhotoButton = SwingHelper.createImageButton("Add a Photo", 
 				"icons/camera.png");
 		addPhotoButton.setToolTipText("Attach a photo to this BOLO");
 		addPhotoButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae) {
 				chooseAndAddPhoto(photoPanel);
 			}
 		});
 
 
 
 		JButton addVideoButton = SwingHelper.createImageButton("Add a Video", 
 				"icons/videoCamera.png");
 		addVideoButton.setToolTipText("Attach a video to this BOLO");
 		JPanel buttonsPanel = new JPanel();
 		buttonsPanel.add(addPhotoButton);
 		buttonsPanel.add(addVideoButton);
 
 		photoVideoPanel.add(buttonsPanel, "dock south");
 
 		return photoVideoPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	private JPanel createButtonsPanel(){
 
 		JPanel buttonsPanel = new JPanel(new MigLayout("fillx", "push"));
 
 		//Cancel button
 		JButton cancelButton = SwingHelper.createImageButton("Cancel", 
 				"icons/cancel_48.png");
 		cancelButton.setToolTipText("Cancel and do not save");
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae) {
 				closeAndCancel();
 			}
 		});
 
 	    //Save button
 	    JButton saveButton = SwingHelper.createImageButton("Save", 
 	    		"icons/save_48.png");
 	    saveButton.setToolTipText("Save BOLO");
 	    saveButton.addActionListener(new ActionListener( ) {
 	    	public void actionPerformed(ActionEvent e) {
 	    		saveAndClose();
 	    	}
 	    });
 
 	    //Preview button
 	    JButton previewButton = new JButton("Preview");
 	    previewButton.setToolTipText("Preview and print final BOLO document");
 	    previewButton.addActionListener(new ActionListener() {
 	    	public void actionPerformed(ActionEvent e) {
 	    		//setVisible(false);
 	    		putInfoIntoBoloObject();
 	    		BOLOpreview preview = new BOLOpreview(rm, bolotab, bolo);
 	    		preview.setVisible(true);
 	    		preview.setModal(true);
 	    		if(preview.isNewBOLOWascreated()){
 	    			setVisible(false);
 	    			newBOLOWascreated=true;
 	    			eraseForm();
 	    		} else{
 	    			newBOLOWascreated=false;
 	    			setVisible(true);
 	    		}
 	    	}
 	    });
 
 
 	    JPanel saveAndCancelButtonsPanel = new JPanel();
 	    saveAndCancelButtonsPanel.add(saveButton, "tag ok, dock west");
 	    saveAndCancelButtonsPanel.add(cancelButton, "tag cancel, dock west");
 	    JPanel previewButtonPanel = new JPanel(new MigLayout("rtl"));
 	    previewButtonPanel.add(previewButton, "tag right");
 	    buttonsPanel.add(saveAndCancelButtonsPanel, "shrinky");
 	    buttonsPanel.add(previewButtonPanel, "growx, shrinky");
 	    return buttonsPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Save the information input into this form and close the dialog.
 	 */
 	private void saveAndClose(){
 
 		//place the info from the fields into a BOLO object
 		 putInfoIntoBoloObject();
 		 bolo.createItemToReview();
 
 		 //add the BOLO object's info to the database
 		 try {
 			bolo.addToDB();
 		 } catch (Exception e) {
 			System.out.println("error: unable to add BOLO to DB");
 			e.printStackTrace();
 		 }
 //TODO: Create a pdf from the input data
 
 		 //reset the form
 		 eraseForm();
 
 		 newBOLOWascreated=true;
 
 		 this.setVisible(false);
 		 //close the window
 		 this.dispose();	
 	}
 //-----------------------------------------------------------------------------
 	 /**
 	  * Places the info from the input fields into the global BOLO object.
 	  */
 	 private void putInfoIntoBoloObject(){
 		 String age, race, sex, height, weight, build, eyes, hair;
 		 String reference, caseNum, status, weapon;
 		 String preparedBy, approvedBy;
 		 String otherDescrip, narrative;
 
 		 //set the filled in fields in the global BOLO object
 		 age = ageField.getText();
 		 if(!age.isEmpty()){ bolo.setAge(age); }
 		 race = raceField.getText();
 		 if(!race.isEmpty()){ bolo.setRace(race); }
 		 sex = sexField.getText();
 		 if(!sex.isEmpty()){ bolo.setSex(sex); }
 		 height = heightField.getText();
 		 if(!height.isEmpty()){ bolo.setHeight(height); }
 		 weight=weightField.getText();
 		 if(!weight.isEmpty()){ bolo.setWeight(weight); }
 		 build=buildField.getText();
 		 if(!build.isEmpty()){ bolo.setBuild(build); }
 		 eyes=eyesField.getText();
 		 if(!eyes.isEmpty()){ bolo.setEyes(eyes); }
 		 hair=hairField.getText();
 		 if(!hair.isEmpty()){ bolo.setHair(hair); }
 		 reference=referenceField.getText();
 		 if(!reference.isEmpty()){ bolo.setReference(reference); }
 		 caseNum=caseNumField.getText();
 		 if(!caseNum.isEmpty()){ bolo.setCaseNum(caseNum); }
 		 status=(String)statusField.getSelectedItem();
 		 if(!status.isEmpty()){ bolo.setStatus(status); }
 		 weapon=ifYesField.getText();
 		 if(!weapon.isEmpty()){ bolo.setWeapon(weapon); }
 		 preparedBy= preparedByField.getText();
 		 if(!preparedBy.isEmpty()){ bolo.setPreparedBy(preparedBy); }
 		 approvedBy= approvedByField.getText();
 		 if(!approvedBy.isEmpty()){ bolo.setApprovedBy(approvedBy); }
 		 otherDescrip= otherDescriptField.getText();
 		 if(!otherDescrip.isEmpty()){ bolo.setOtherDescrip(otherDescrip); }
 		 narrative=narrativeText.getText();
 		 if(!narrative.isEmpty()){ bolo.setNarrative(narrative); }
 
 
 		 //set the times
 		 bolo.setprepDate(getPrepDateEpoch());
 		 bolo.setincidentDate(getIncidentDateEpoch());
 
 	}
 //-----------------------------------------------------------------------------
 	 /**
 	  * Places the info from the input fields into the global BOLO object.
 	  */
 	 private void loadFromExistingBOLO(){
 		 //set the filled in fields in the global BOLO object
 		 ageField.setText(bolo.getAge());
 
 		 raceField.setText(bolo.getRace());		
 		 sexField.setText(bolo.getSex());		 
 		 heightField.setText(bolo.getHeight());
 		 weightField.setText(bolo.getWeight());
 		 buildField.setText(bolo.getBuild());
 		 eyesField.setText(bolo.getEyes());
 		 hairField.setText(bolo.getHair());
 		 referenceField.setText(bolo.getReference());
 		 caseNumField.setText(bolo.getCaseNum());
 		 statusField.setSelectedItem(bolo.getStatus());
 		 ifYesField.setText(bolo.getWeapon());
 		 preparedByField.setText(bolo.getPreparedBy());
 		 approvedByField.setText(bolo.getApprovedBy());
 		 otherDescriptField.setText(bolo.getOtherDescrip());
 		 narrativeText.setText(bolo.getNarrative());
 
 		 //TODO: set the times

 		 //set picture
 		 if(bolo.getPhotoFilePath()!=null){
 			 ImageIcon photo = ImageHandler.getScaledImageIcon(
 				 bolo.getPhotoFilePath(), 200, 299);
 		 
 			photoArea.removeAll();
 			photoArea.add(new JLabel(photo));
 		 }
 		 dialogPanel.validate();
 
 	}
 //-----------------------------------------------------------------------------
 	 /**
 	  * 
 	  */
 	 private void chooseAndAddPhoto(final JPanel photoPanel){
 		//show choose photo dialog
 		final JFileChooser fc = new JFileChooser();
 		fc.addChoosableFileFilter(FileHelper.getImageFilter());
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.setAccessory(new ImagePreview(fc));
 		int returnVal = fc.showOpenDialog(parent);
 
 		//if a photo was selected, add it to BOLO and load into photo area
 		if(returnVal==JFileChooser.APPROVE_OPTION){
 			//copy the chosen photo into the program's 'Photos' directory
 			final File file = fc.getSelectedFile();
 
 //DEBUG System.out.printf("filepath = %s\n", file.getPath());
 
 			ImageIcon chosenPhoto = new ImageIcon(file.getPath());
 
 			final ResizablePhotoDialog resizeDialog = new ResizablePhotoDialog(
 						chosenPhoto, this, file.getName());
 
 			//if the user pressed the set photo button
 			if(resizeDialog.getNewPhotoFilePath()!=null){
 				bolo.setPhotoFilePath(resizeDialog.getNewPhotoFilePath());
 				photoPanel.removeAll();
 
 				photoPanel.add(new JLabel(resizeDialog.getResizedImgIcon()));
 
 				(photoPanel.getParent()).validate();
 			}
 
 		}
 
 	 }
 //-----------------------------------------------------------------------------
 	/**
 	 * Erase any fields in the form that have been filled in and close the
 	 * dialog.
 	 */
 	 private void closeAndCancel() {
 		//reset the form
 		eraseForm();
 
 		//delete the photo(if any)
 		if(bolo.getPhotoFilePath()!=null){
 
 //DEBUG
 			System.out.printf("\nBOLOform: closeAndCancel(): deleting " +
 					"bolo.getPhotoFilePath().toString() " +
 							"= %s\n", bolo.getPhotoFilePath().toString());
 
 			File f=new File(bolo.getPhotoFilePath().toString());
 			if(f.exists() && f.isFile()){
 				f.delete();
 			}
 		}
 
 
 		newBOLOWascreated=false;
 		//close the dialog
 		this.dispose();	
 	 }
 //-----------------------------------------------------------------------------
 	 /**
 	  * Erases <code>BOLOfrom</code>
 	  */
 	 private void eraseForm(){
 		//set the text of all the form's fields to null
 		ageField.setText(null);
 		raceField.setText(null);
 		sexField.setText(null);
 		heightField.setText(null);
 		weightField.setText(null);
 		buildField.setText(null);
 		eyesField.setText(null);
 		hairField.setText(null);
 		referenceField.setText(null);
 		caseNumField.setText(null);
 		statusField.setSelectedItem("");
 		ifYesField.setText(null);
 		preparedByField.setText(null);
 		approvedByField.setText(null);
 		otherDescriptField.setText(null);
 		narrativeText.setText(null);
 
 		//recreate the photo/video section
 		photoArea.removeAll();
 		ImageIcon noPhotoImage = ImageHandler.createImageIcon("images/unknownPerson.jpeg");
 		JLabel noPhotoLabel = new JLabel(noPhotoImage);
 		photoArea.add(noPhotoLabel);
 		(photoArea.getParent()).validate();
 	 }
 //-----------------------------------------------------------------------------
 	 /**
 	  *  
 	  * @return preparedCal.getTimeInMillis()/1000
 	  */
 	 public long getPrepDateEpoch(){
 		  Date day = new Date();
 		  Date time = new Date();
 
 		  Calendar preparedCal = Calendar.getInstance();
 		  Calendar timeCal = Calendar.getInstance();
 
 		  day = ((SpinnerDateModel) preparedDate.getModel()).getDate();
 		  time = ((SpinnerDateModel) preparedTime.getModel()).getDate();
 		  timeCal.setTime(time);
 
 		  preparedCal.setTime(day);
 		  preparedCal.set(Calendar.HOUR, timeCal.get(Calendar.HOUR));
 		  preparedCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
 		  preparedCal.set(Calendar.AM_PM, timeCal.get(Calendar.AM_PM));
 
 		  return (preparedCal.getTimeInMillis()/1000);
 	  }
 //-----------------------------------------------------------------------------
 	  /**
 	   * 
 	   * @return incidentCal.getTimeInMillis()/1000
 	   */
 	  public long getIncidentDateEpoch(){
 		  Date day = new Date();
 		  Date time = new Date();
 
 		  Calendar incidentCal = Calendar.getInstance();
 		  Calendar timeCal = Calendar.getInstance();
 
 		  day = ((SpinnerDateModel) preparedDate.getModel()).getDate();
 		  time = ((SpinnerDateModel) preparedTime.getModel()).getDate();
 		  timeCal.setTime(time);
 
 		  incidentCal.setTime(day);
 		  incidentCal.set(Calendar.HOUR, timeCal.get(Calendar.HOUR));
 		  incidentCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
 		  incidentCal.set(Calendar.AM_PM, timeCal.get(Calendar.AM_PM));
 
 		  return (incidentCal.getTimeInMillis()/1000); 
 	  }
 //-----------------------------------------------------------------------------	
 	  /**
 	   * JDOC
 	   * @return this.newBOLOWascreated
 	   */
 	  public boolean isNewBOLOWascreated(){
 		  return this.newBOLOWascreated;
 	  }
 //-----------------------------------------------------------------------------
 }
