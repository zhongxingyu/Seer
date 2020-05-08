 package utilities;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.Date;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.JPasswordField;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerModel;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import net.miginfocom.swing.MigLayout;
 import userinterface.MainInterfaceWindow;
 
 public class SwingHelper {
 //-----------------------------------------------------------------------------
 	/** length in columns = 2 */
 	public static final int ONE_CHAR_TEXT_FIELD_LENGTH = 2;
 	/** length in columns = 3 */
 	public static final int EXTRA_SMALL_TEXT_FIELD_LENGTH = 3;
 	/** length in columns = 5 */
 	public static final int SMALL_TEXT_FIELD_LENGTH = 5;
 	/** length in columns = 10 */
 	public static final int MEDIUM_TEXT_FIELD_LENGTH = 10;
 	/** length in columns = 25 */
 	public static final int LARGE_TEXT_FIELD_LENGTH = 25;
 	/** length in columns = 35 */
 	public static final int EXTRA_LARGE_TEXT_FIELD_LENGTH = 35;
 	/** length in columns = 20 */
 	public static final int DEFAULT_TEXT_FIELD_LENGTH = 20;
 	
 	public static final Dimension SEARCH_DIALOG_DIMENSION = new Dimension(500, 200);
 	public static final Dimension LOGIN_DIALOG_DIMENSION = new Dimension(450, 250);
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JSpinner</code> with the specified label text from the given
 	 * <code>SpinnerModel</code> and adds the <code>JSpinner</code> to the given 
 	 * <code>Container</code>.
 	 * @param c - the container to add the spinner to
 	 * @param label - the text which to add to the spinner
 	 * @param model - the <code>SpinnerModel</code> to make the spinner from
 	 * @return a JSpinner created from the <code>SpinnerModel</code> with the 
 	 * specified label text
 	 */
 	public static JSpinner addLabeledSpinner(Container c,String label,SpinnerModel model) {
 		JLabel l = new JLabel(label);
 		c.add(l);
 		
 		JSpinner spinner = new JSpinner(model);
 		l.setLabelFor(spinner);
 		c.add(spinner);
 		
 		return spinner;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JSpinner</code> with the specified label text from the given
 	 * <code>SpinnerModel</code> and adds the <code>JSpinner</code> to the given
 	 * <code>Container</code>. If the wrap parameter is set to true, MigLayout's 
 	 * "wrap" attribute will be applied to the <code>JTextField</code>, meaning 
 	 * the next component added will appear on the next line.
 	 * (Exception: Will not work if MigLayout's flowy layout constraint is applied,
 	 * but it is rarely used MigLayout feature and thus not a common concern; however
 	 * if you have set this layout constraint on your <code>JComponent</code> do not
 	 * attempt to use the wrap option of this method.)
 	 * @param c - the container to add the spinner to
 	 * @param label - the text which to add to the spinner
 	 * @param model - the <code>SpinnerModel</code> to make the spinner from
 	 * @param wrap - indicates whether the MigLayout "wrap" attribute should be
 	 * present when this <code>JSpinner</code> is added to the container; if 
 	 * component does not have the MigLayout as it's layout manager then this 
 	 * property has no effect
 	 * @return a JSpinner created from the <code>SpinnerModel</code> with the 
 	 * specified label text
 	 */
 	public static JSpinner addLabeledSpinner(Container c,String label,SpinnerModel model, boolean wrap) {
 		JLabel l = new JLabel(label);
 		c.add(l, "align left");
 		
 		JSpinner spinner = new JSpinner(model);
 
 		l.setLabelFor(spinner);
 		if(wrap){
 			c.add(spinner, "align left, wrap");
 		} else{
 			c.add(spinner, "align left, split");
 		}
 		
 		return spinner;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JTextField</code> with the specified text as a label
 	 * appearing before the text field.
 	 * @param label - the text to appear in front of the text field
 	 * @param length - the length in columns of the text field 
 	 * @return a <code>JPanel</code> with the labeled text field as the only element
 	 */
 	public static JPanel createLabeledTextField(String label, int length) {
 		JPanel labeledTextBox = new JPanel();
 
 		JLabel l = new JLabel(label);
 		labeledTextBox.add(l);
 		
 		JTextField text = new JTextField(length);
 		labeledTextBox.add(text);
 
 		return labeledTextBox;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Adds a <code>JTextField</code> with the specified text as a label
 	 * appearing before the text field to the given <code>JComponent</code>. 
 	 * Assuming the <code>JComponent</code> has the MigLayout set, the label
 	 * and text field are both left aligned within the <code>JComponent</code>.
 	 * If the wrap parameter is set to true, MigLayout's "wrap" attribute will be
 	 * applied to the <code>JTextField</code>, meaning the next component added
 	 * will appear on the next line.
 	 * (Exception: Will not work if MigLayout's flowy layout constraint is applied,
 	 * but it is rarely used MigLayout feature and thus not a common concern; however
 	 * if you have set this layout constraint on your <code>JComponent</code> do not
 	 * attempt to use the wrap option of this method.)
 	 * @param label - the text to appear in front of the text field
 	 * @param length - the length in columns of the text field 
 	 * @param wrap - indicates is this component should be the last on the current line
 	 * @return the <code>JTextField</code> added to the component
 	 */
 	public static JTextField addLabeledTextField(JComponent c, String label, int length, boolean wrap){
 		JLabel l = new JLabel(label);
 		c.add(l, "align left");
 		
 		JTextField text = new JTextField(length);
 		if(wrap){
 			c.add(text, "align left, wrap");
 		} else{
 			c.add(text, "align left");
 		}
 		
 		return text;
 	}
 //-----------------------------------------------------------------------------
	public static JPasswordField addLabeledPwdField(JComponent c, String label, int length, boolean wrap){
		JLabel l = new JLabel(label);
		c.add(l, "align left");
		
		JPasswordField pwd = new JPasswordField(length);
		if(wrap){
			c.add(pwd, "align left, wrap");
		} else{
			c.add(pwd, "align left");
		}
		
		return pwd;
	}
//-----------------------------------------------------------------------------
 	/** 
 	 * Adds a <code>JTextArea</code> with the specified text as a label
 	 * appearing before the text field to the given <code>JComponent</code>. 
 	 * Assuming the <code>JComponent</code> has the MigLayout set, the label
 	 * and text field are both left aligned within the <code>JComponent</code>.
 	 * If the wrap parameter is set to true, MigLayout's "wrap" attribute will be
 	 * applied to the <code>JTextField</code>, meaning the next component added
 	 * will appear on the next line.
 	 * (Exception: Will not work if MigLayout's flowy layout constraint is applied,
 	 * but it is rarely used MigLayout feature and thus not a common concern; however
 	 * if you have set this layout constraint on your <code>JComponent</code> do not
 	 * attempt to use the wrap option of this method.)
 	 * @param label - the text to appear in front of the text field
 	 * @param rows - the length in rows of the text field 
 	 * @param cols - the length in columns of the text field
 	 * @param wrap - indicates is this component should be the last on the current line
 	 * @return the <code>JTextArea</code> added to the component
 	 */
 	public static JTextArea addLabeledTextArea(JComponent c, String label, int rows,
 			int cols, boolean wrap){
 		JLabel l = new JLabel(label);
 		c.add(l, "align left");
 		
 		JTextArea text = new JTextArea(rows, cols);
 		if(wrap){
 			c.add(text, "align left, wrap");
 		} else{
 			c.add(text, "align left");
 		}
 		
 		return text;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JLabel</code> containing the image located at the
 	 * specified image path. Image path should be given relative to the 
 	 * <code>userinterface</code> package.
 	 * @param path - the image's path relative to the <code>userinterface</code> package
 	 * @return a <code>JLabel</code> containing the image or <code>null</code> if 
 	 * the path was invalid 
 	 */
 	public static JLabel createImageLabel(String path) {
 		ImageIcon imgIcon = null;
 	    java.net.URL imgURL = MainInterfaceWindow.class.getResource(path);
 	    if (imgURL != null) {
 	    	imgIcon = new ImageIcon(imgURL);
 	    } else {
 	        System.err.println("Couldn't find file: " + path);
 	        return null;
 	    }
 	    
 	    return (new JLabel(imgIcon));
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JPanel</code> containing two labeled date spinners 
 	 * labeled "To" and "From". Used to specify a specific date range. The 
 	 * default range is -10 years from today's date through today's date. 
 	 * @return a JPanel containing two date spinners used for specifying a date range
 	 */
 	public static JPanel createDateRangePanel(){
 		JPanel dateRangePanel = new JPanel();
 		Calendar calendar = Calendar.getInstance();
 		JSpinner dateSpinner;
 		
 		//Set up dates
 		Date initDate = calendar.getTime();
 		Date latestDate = calendar.getTime();		
 	    calendar.add(Calendar.YEAR, -10);        
 	    Date earliestDate = calendar.getTime();
 		
 	   //Date Spinners
 	    SpinnerModel fromModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	    		Calendar.DAY_OF_MONTH);
 	    dateSpinner = SwingHelper.addLabeledSpinner(dateRangePanel, "From: ", fromModel);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	    
 	    SpinnerModel toModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	            Calendar.DAY_OF_MONTH);
 	    dateSpinner = SwingHelper.addLabeledSpinner(dateRangePanel, "To: ", toModel);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	    
 	    return dateRangePanel;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Adds to the given <code>JComponent</code> two date spinners 
 	 * labeled "To" and "From". Used to specify a specific date range. The 
 	 * default range is -10 years from today's date through today's date. 
 	 * @param c - <code>JComponent</code> to add the date spinners to
 	 */
 	public static void addDateRangePanel(JComponent c){
 		Calendar calendar = Calendar.getInstance();
 		JSpinner dateSpinner;
 		
 		//Set up dates
 		Date initDate = calendar.getTime();
 		Date latestDate = calendar.getTime();		
 	    calendar.add(Calendar.YEAR, -10);        
 	    Date earliestDate = calendar.getTime();
 		
 	   //Date Spinners
 	    SpinnerModel fromModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	    		Calendar.DAY_OF_MONTH);
 	    dateSpinner = addLabeledSpinner(c, "From: ", fromModel, false);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	    
 	    SpinnerModel toModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	            Calendar.DAY_OF_MONTH);
 	    dateSpinner = addLabeledSpinner(c, "To: ", toModel, true);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Creates a <code>JSpinner</code> used for selecting the a date to the given
 	 * <code>JComponent</code>. The default range is -10 years from today's date
 	 * through today's date. 
 	 * @return a JPanel containing one date spinner used for specifying a date
 	 */
 	public static JPanel createDateSpinnerPanel(){
 		JPanel datePanel = new JPanel();
 		Calendar calendar = Calendar.getInstance();
 		JSpinner dateSpinner;
 		
 		//Set up dates
 		Date initDate = calendar.getTime();
 		Date latestDate = calendar.getTime();		
 	    calendar.add(Calendar.YEAR, -10);        
 	    Date earliestDate = calendar.getTime();
 		
 	   //Date Spinner    
 	    SpinnerModel toModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	            Calendar.DAY_OF_MONTH);
 	    dateSpinner = SwingHelper.addLabeledSpinner(datePanel, "", toModel);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	    
 	    return datePanel;
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Adds a <code>JSpinner</code> used for selecting the a date to the given
 	 * <code>JComponent</code>. The default range is -10 years from today's 
 	 * date through today's date. 
 	 * @param c - the <code>JCompoent</code> to add the date spinner to
 	 * @param label - the text label to attach to the date spinner; set to ""
 	 * to add an unlabeled date spinner
 	 * @return the <code>JSpinner</code>date spinner added to the component
 	 */
 	public static JSpinner addDateSpinner(JComponent c, String label){
 		Calendar calendar = Calendar.getInstance();
 		JSpinner dateSpinner;
 		
 		//Set up dates
 		Date initDate = calendar.getTime();
 		Date latestDate = calendar.getTime();		
 	    calendar.add(Calendar.YEAR, -10);        
 	    Date earliestDate = calendar.getTime();
 		
 	   //Date Spinner
 	    
 	    SpinnerModel toModel = new SpinnerDateModel(initDate,earliestDate,latestDate,
 	            Calendar.DAY_OF_MONTH);
 	    dateSpinner = SwingHelper.addLabeledSpinner(c, label, toModel, true);       
 	    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
 	    
 	    return dateSpinner;
 	}
 //-----------------------------------------------------------------------------	
 	/**  
 	 * Creates a <code>JPanel</code> with a <code>JSpinner</code> to select
 	 * a time.
 	 * @return a JPanel containing one time spinner used for specifying a time
 	 */
 	public static JPanel createTimeSpinnerPanel(){
 	// create and set panels
 	JPanel timePanel = new JPanel();
 	timePanel.setLayout(new MigLayout());
 
     //Set up times
 	Calendar calendar = Calendar.getInstance();
     calendar.set(Calendar.HOUR_OF_DAY, 1);
     calendar.set(Calendar.MINUTE, 0);
     Date initTime = calendar.getTime();
     calendar.set(Calendar.HOUR_OF_DAY, 24);
     calendar.set(Calendar.MINUTE, 59);
     Date finalTime = calendar.getTime();
     
     JSpinner timeSpinner;
     
 	SpinnerModel toTimeModel = new SpinnerDateModel(initTime,initTime,finalTime,Calendar.HOUR);
 	timeSpinner = SwingHelper.addLabeledSpinner(timePanel, "Time of Incident: ", toTimeModel);       
 	timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "hh:mm a"));
 	timePanel.add(timeSpinner);
 	return timePanel;
 	
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Adds a <code>JSpinner</code> used to select a time to the 
 	 * given <code>JComponent</code>.
 	 * @param c - <code>JComponent</code> to add the time spinner to
 	 * @param label - the text label to attach to the date spinner; set to ""
 	 * to add an unlabeled time spinner
 	 */
 	public static JSpinner addTimeSpinner(JComponent c, String label){
 	    //Set up times
 		Calendar calendar = Calendar.getInstance();
 	    calendar.set(Calendar.HOUR_OF_DAY, 1);
 	    calendar.set(Calendar.MINUTE, 0);
 	    Date initTime = calendar.getTime();
 	    calendar.set(Calendar.HOUR_OF_DAY, 24);
 	    calendar.set(Calendar.MINUTE, 59);
 	    Date finalTime = calendar.getTime();
 	    
 	    JSpinner timeSpinner;
 	    
 		SpinnerModel toTimeModel = new SpinnerDateModel(initTime,initTime,finalTime,Calendar.HOUR);
 		timeSpinner = SwingHelper.addLabeledSpinner(c, label, toTimeModel, true);       
 		timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "hh:mm a"));
 		
 		return timeSpinner;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Creates a <code>JPanel</code> containing the given text placed in the
 	 * center as the only component.
 	 * @param text - the text to put on the panel
 	 * @return a JPanel containing only the given text
 	 */
 	public static JPanel makeTextPanel(String text) {
 	    JPanel panel = new JPanel(false);
 	    JLabel filler = new JLabel(text);
 	    filler.setHorizontalAlignment(JLabel.CENTER);
 	    panel.setLayout(new GridLayout(1, 1));
 	    panel.add(filler);
 	    return panel;
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Adds a basic black line border around the given component.
 	 * @param c - the component to put the border around
 	 */
 	public static void addLineBorder(JComponent c){
 		Border lineBorder;
 		lineBorder = BorderFactory.createLineBorder(Color.black);
 		c.setBorder(lineBorder);
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Adds a basic black line border with the specified title around the given component.
 	 * @param c - the component to put the border around
 	 * @param title - the title to give the titled border
 	 */
 	public static void addTitledBorder(JComponent c, String title){
 		TitledBorder titledBorder;
 		titledBorder = BorderFactory.createTitledBorder(title);
 		c.setBorder(titledBorder);
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Adds a line of vertical buttons to the given panel.
 	 * @param panel - the <code>JPanel</code> to add the buttons to
 	 * @param buttonArray - an array filled with buttons to add to panel
 	 */
 	public static void addVerticalButtons(JPanel panel, JButton[] buttonArray){
 		panel.setLayout(new MigLayout());
 		
 		for(int i=0; i<buttonArray.length; i++){
 			panel.add(buttonArray[i], "align, wrap");
 		}
 	}
 //-----------------------------------------------------------------------------
 	/** 
 	 * Adds a line of vertical buttons with accompaning text to the given panel.
 	 * The text will precede the panel and both buttons and panels will be left 
 	 * aligned.
 	 * @param panel - the <code>JPanel</code> to add the buttons to
 	 * @param buttonArray - an array filled with buttons to add to panel
 	 * @param labels - an array filled with the text to precede each button
 	 * 
 	 * <u>Note</u>: buttons and accompaning labels should be in the same 
 	 * order w/in their respective arrays
 	 */
 	public static void addVerticalLabeledButtons(JPanel panel, JButton[] buttonArray, String[] labels){
 		panel.setLayout(new MigLayout());
 		
 		JLabel label;
 		
 		for(int i=0; i<buttonArray.length; i++){
 			panel.add(buttonArray[i], "align");
 			label = new JLabel(labels[i]);
 			panel.add(label, "aligny top, wrap");
 		}
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Creates and returns a new <code>JButton</code> with the image found at the given
 	 * filename and the given text. Image will appear on top and text will appear centered
 	 * below the image. In the case that no image is found at the given path, a regular
 	 * <code>JButton</code> with the given text will be returned.
 	 * @param buttonText - the text to appear on the button 
 	 * @param imagePath - the pathname of the image to place on the button given relative 
 	 * to userinterface package.
 	 * 
 	 * @return <code>JButton</code> with specified icon image and text
 	 */
 	public static JButton createImageButton(String buttonText, String imagePath){
 		
 		ImageIcon buttonIcon = ImageHandler.createImageIcon(imagePath);
 		JButton imageButton= new JButton(buttonText, buttonIcon);
 		imageButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 		imageButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		
 		return imageButton;
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Creates and returns a new <code>JButton</code> with the image found at the given
 	 * filename.In the case that no image is found at the given path, a <code>JButton</code>
 	 * with no text or image will be returned.
 	 * @param buttonText - the text to appear on the button 
 	 * @param imagePath - the pathname of the image to place on the button given relative 
 	 * to userinterface package.
 	 * 
 	 * @return <code>JButton</code> with specified icon image and text
 	 */
 	public static JButton createImageButton(String imagePath){
 		
 		ImageIcon buttonIcon = ImageHandler.createImageIcon(imagePath);
 		JButton imageButton= new JButton(buttonIcon);
 		
 		return imageButton;
 	}
 //-----------------------------------------------------------------------------	
 	/** 
 	 * Adds armed check boxes and text used to inquire if a suspect
 	 * was armed. It contains text reading "Armed?" along with two
 	 * check boxes (one yes, one no). If the yes check box is selected a 
 	 * <code>JTextField</code> appears for the user to input the weapon.
 	 * @param armedPanel - <code>JPanel</code> to add the Armed? question to
 	 * @param ifYesField - <code>JTextField</code> to appear if yes check box is selected
 	 * this <code>JTextField</code> must be set up and initialized by the calling class
 	 * in order for entered text to be retrieved 
 	 */
 	public static void addArmedQuestionCheckboxes(final JPanel armedPanel, final JTextField ifYesField){
 		ifYesField.setColumns(SwingHelper.MEDIUM_TEXT_FIELD_LENGTH);
 		
         JLabel armedLabel = new JLabel("Armed?");
 		JCheckBox armedFieldNo = new JCheckBox("No");
 		armedFieldNo.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae) {
 				ifYesField.setVisible(false);
 			}
 		});
 		
 		JCheckBox armedFieldYes = new JCheckBox("Yes");
 		armedFieldYes.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae) {
 				ifYesField.setVisible(true);
 			}
 		});
 		
 		//Group the check boxes
         ButtonGroup group = new ButtonGroup();
         group.add(armedFieldNo);
         group.add(armedFieldYes);
 
         //Add the components
         armedPanel.add(armedLabel);
         armedPanel.add(armedFieldNo, "split");
         armedPanel.add(armedFieldYes);
         
         armedPanel.add(ifYesField);
         ifYesField.setVisible(false);
 	}
 //-----------------------------------------------------------------------------	
 }
