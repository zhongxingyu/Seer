 package jfmi.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.border.EmptyBorder;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import jfmi.control.TaggedFile;
 import jfmi.control.TaggedFileHandler;
 
 /** A TaggedFileViewPanel displays all information contained in a TaggedFile
   to the user, and allows the user to communicate changes to a
   TaggedFileHandler.
   */
 public class TaggedFileViewDialog extends JDialog implements ActionListener {
 
 	// PRIVATE INSTANCE Fields
 	private JLabel fileNameLabel;
 	private JLabel filePathLabel;
 
 	private JButton changePathButton;
 	private JButton addTagButton;
 	private JButton removeTagButton;
 	private JButton saveFileButton;
 
 	private JList fileTagList;
 	private JTextArea tagCommentArea;
 
 	private Box fileInfoBox;
 	private Box fileTaggingBox;
 
 	private TaggedFileHandler fileHandler;
 	private TaggedFile displayedFile;
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Constructs a TaggedFileViewPanel.
 	  @param parent a JFrame to server as this dialog's owner
 	  @param fileHandler_ file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	public TaggedFileViewDialog(JFrame parent, TaggedFileHandler fileHandler_)
 	{
 		super(parent, "File Viewer", true);
 
 		// Initialize fields and child components
 		init(fileHandler_);
 		initFileInfoBox();
 		initFileTaggingBox();
 
 		// Add child components
 		Box content = Box.createVerticalBox();
 		content.add(Box.createVerticalStrut(5));
 		content.add(fileInfoBox);
 		content.add(Box.createVerticalStrut(10));
 		content.add(fileTaggingBox);
 		content.add(Box.createVerticalStrut(5));
 
 		add(content);
 
 		// Not visible initially	
 		setVisible(false);
 	}
 
 	/** Sets the TaggedFile whose information this instance is displaying.
 	  @param file the TaggedFile to display
 	  @throws IllegalArgumentException if file is null
 	  */
 	public void setDisplayedFile(TaggedFile file)
 	{
 		if (file == null) {
 			throw new IllegalArgumentException("file cannot be null");
 		}
 
 		displayedFile = file;
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
 
 	/** Updates the information displayed by all of this instance's components.
 	  */
 	public void updateDisplay()
 	{
 
 	}
 
 	/** Tells this instance to display the specified file, and updates the
 	  information contained in all of the instance's components accordingly.
 	  @param file the file to display
 	  @throws IllegalArgumentException if file is null
 	  */
 	public void updateDisplayedFile(TaggedFile file)
 	{
 		setDisplayedFile(file);
 		updateDisplay();
 	}
 
 
 	//************************************************************
 	// PRIVATE INSTANCE Methods
 	//************************************************************
 
 	/** Initializes a TaggedFileViewPanel instance.
 	  @param fileHandler_ file handler to associate with this instance
 	  @throws IllegalArgumentException if fileHandler_ is null
 	  */
 	private final void init(TaggedFileHandler fileHandler_)
 	{
 		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		setFileHandler(fileHandler_);	
 	}
 
 	/** Initializes the file information box, which displays the file path
 	  information.
 	  */
 	private final void initFileInfoBox()
 	{
 		// Set up left part of fileInfoBox
 		fileNameLabel = new JLabel("");
 
 		filePathLabel = new JLabel("");
 
 		Box leftBox = Box.createVerticalBox();
 		leftBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		leftBox.add(fileNameLabel);
 		leftBox.add(Box.createVerticalStrut(5));
 		leftBox.add(filePathLabel);
 
 		// Set up right part of fileInfoBox
 		changePathButton = new JButton("Change Path");
 
 		Box rightBox = Box.createVerticalBox();
 		rightBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		rightBox.add(changePathButton);
 
 		// Set up fileInfoBox
 		fileInfoBox = Box.createHorizontalBox();
 		fileInfoBox.add(leftBox);
 		fileInfoBox.add(rightBox);
 	}
 
 	/** Initializes the file tagging box, which displays information on the
 	  file's tags and comments.
 	  */
 	private final void initFileTaggingBox()
 	{
 		// Set up left part of box
 		fileTagList = new JList();		
 		fileTagList.setBorder(new EmptyBorder(2, 2, 2, 2));
 
 		addTagButton = new JButton("Add Tag");
 
 		removeTagButton = new JButton("Remove Tag");
 		removeTagButton.setForeground(Styles.DANGER_COLOR);
 
 		Box leftBox = Box.createVerticalBox();
 		leftBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		leftBox.add(new JLabel("File's Tags"));
 		leftBox.add(fileTagList);
 		leftBox.add(addTagButton);
 		leftBox.add(removeTagButton);
 
 		// Set up right part of box
 		tagCommentArea = new JTextArea();
 		tagCommentArea.setBorder(new EmptyBorder(2, 2, 2, 2));
 
 		saveFileButton = new JButton("Save File");
 
 		Box rightBox = Box.createVerticalBox();
 		rightBox.setBorder(new EmptyBorder(2, 2, 2, 2));
 		rightBox.add(new JLabel("Tag Comments"));
 		rightBox.add(tagCommentArea);
 		rightBox.add(saveFileButton);
 
 		// Set up fileTaggingBox
 		fileTaggingBox = Box.createHorizontalBox();
 		fileTaggingBox.add(leftBox);
 		fileTaggingBox.add(rightBox);
 	}
 
 
 	//************************************************************
 	// IMPLEMENTATION ActionListener 
 	//************************************************************
 
 	/** Determines what action is taken when the user generates an ActionEvent
 	  on one of the components in the TaggedFileViewPanel.
 	  @param e a user-generated ActionEvent
 	  */
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 
 		if (source == changePathButton) {
 
 		} else if (source == addTagButton) {
 
 		} else if (source == removeTagButton) {
 
 		} else if (source == saveFileButton) {
 
 		}
 
 	}
 
 }
