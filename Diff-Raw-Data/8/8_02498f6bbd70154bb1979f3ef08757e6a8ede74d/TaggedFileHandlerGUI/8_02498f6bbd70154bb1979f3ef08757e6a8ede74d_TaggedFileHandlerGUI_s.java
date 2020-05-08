 package jfmi.gui;
 
 import java.io.File;
 import javax.swing.JFileChooser;
 
 import jfmi.control.TaggedFileHandler;
 
 /** Provides an interface between a TaggedFileHandler and the user.
   */
 public class TaggedFileHandlerGUI {
 
 	// PRIVATE INSTANCE Fields
 	private TaggedFileHandler fileHandler;
 
 	private JFileChooser fileChooser;
 	private TaggedFileViewDialog fileViewer;
 	private JFMIFrame jfmiGUI;
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Constructs a TaggedFileHandlerGUI that will use the specified
 	  JFMIFrame as a parent component, and be associated with the specified
 	  TaggedFileHandler.
 	  @param jfmiGUI_ the JFMIFrame this object uses for a parent component
 	  @param handler_ the TaggedFileHandler this object is associated with
 	  */
 	public TaggedFileHandlerGUI(JFMIFrame jfmiGUI_, TaggedFileHandler handler_)
 	{
 		init(jfmiGUI_, handler_);
 		initFileChooser();
 	}
 
 	/** Displays a window for the user to select one or more files and/or
 	  directories.
 	  @return an array of selected files if the user indicated approval, else
 	  		null if the user cancelled
 	  */
 	public File[] displayFileChooser()
 	{
 		int returnVal = fileChooser.showDialog(jfmiGUI, "Choose File");
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			return fileChooser.getSelectedFiles();
 		} else {
 			return null;
 		}
 	}
 
 	/** Prompts a user to confirm an action with the specified message, and 
 	  returns the user's decision.
 	  @param confirmMsg the message to display to the user
 	  @return true if the user confirmed
 	  */
 	public boolean getConfirmation(String confirmMsg)
 	{
 		return jfmiGUI.getConfirmation(confirmMsg);	
 	}
 
 	/** Sets this object's associated TaggedFileHandler.
 	  @param fileHandler_ the file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	public void setFileHandler(TaggedFileHandler fileHandler_)
 	{
 		if (fileHandler_ == null) {
 			throw new IllegalArgumentException("fileHandler_ cannot be null");
 		}
 
 		fileHandler = fileHandler_;
 	}
 
 	/** Sets the JFMIFrame that this instance will use as the parent component
 	  for some of the components it generates.
 	  @param jfmiGUI_ the JFMIFrame to use as a parent component
 	  @throws IllegalArgumentException if jfmiGUI_ is null
 	  */
 	public void setJFMIGUI(JFMIFrame jfmiGUI_)
 	{
 		if (jfmiGUI_ == null) {
 			throw new IllegalArgumentException("jfmiGUI_ cannot be null");
 		}
 
 		jfmiGUI = jfmiGUI_;
 	}
 
 
 	//************************************************************
 	// PRIVATE INSTANCE Methods
 	//************************************************************
 
 	/** Initialize the instance.
 	  @param jfmiGUI_ the JFMIFrame to use as a parent component
 	  @param handler_ the TaggedFileHandler this object is associated with
 	  @throws IllegalArgumentException if jfmiGUI_ or handler_ is null
 	  */
 	private final void init(JFMIFrame jfmiGUI_, TaggedFileHandler handler_)
 	{
 		setFileHandler(handler_);
 		setJFMIGUI(jfmiGUI);
 
		fileViewer = new TaggedFileViewDialog(fileHandler);
 	}
 
 	/** Initialize the file chooser.
 	  */
 	private final void initFileChooser()
 	{
 		fileChooser = new JFileChooser();
 		fileChooser.setDialogTitle("Add a New File");
 		fileChooser.setMultiSelectionEnabled(true);
 		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 	}
 
 }
