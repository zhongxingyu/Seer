 
 package edu.wpi.cs.jburge.SEURAT.editors;
 
 
 import java.io.Serializable;
 import java.util.Enumeration;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.events.*;
 
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 
 /**
  * Used to edit an entry in the argument ontology
  * @author burgeje
  *
  */
 public class EditOntEntry extends NewRationaleElementGUI implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Shell shell;
 	/**
 	 * The ontology entry being edited/created
 	 */	
 	private OntEntry ourOntEntry;
 	/**
 	 * The "parent" entry in the hierarchy
 	 */
 	private OntEntry ourParent;
 	/**
 	 * The name of the ontology entry
 	 */
 	private Text nameField;
 	/**
 	 * The description of the ontology entry
 	 */
 	private Text descArea;
 //	private boolean newItem;
 	/**
 	 * Button for adding the entry
 	 */
 	private Button addButton;
 	/**
 	 * Button to cancel edits
 	 */
 	private Button cancelButton;
 	/**
 	 * The importance of this entry. This value will be inherited by any claims
 	 * referencing it.
 	 */
 	private Combo importanceBox;
 	
 	/**
 	 * Constructor to create an editor to update/create an ontology entry
 	 * @param display - points back to the display
 	 * @param oureditOntEntry - the entry being edited
 	 * @param ontParent - the edited item's parent in the hierarchy
 	 * @param newItem - true if this is a new item
 	 */
 	public EditOntEntry(Display display, OntEntry oureditOntEntry, OntEntry ontParent, boolean newItem)
 	{
 		super();
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
 		shell.setText("OntEntry Information");
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		gridLayout.marginHeight = 5;
 		gridLayout.makeColumnsEqualWidth = true;
 		shell.setLayout(gridLayout);
 		
 		ourOntEntry = oureditOntEntry;
 		ourParent = ontParent;
 		
 		if (newItem)
 		{
 			ourOntEntry.setName("");
 			ourOntEntry.setImportance(Importance.MODERATE);
 		}
 		
 		
 		new Label(shell, SWT.NONE).setText("Name:");
 		
 		nameField =  new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
 		nameField.setText(ourOntEntry.getName());
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		DisplayUtilities.setTextDimensions(nameField, gridData, 75);
 		gridData.horizontalSpan = 2;
 		nameField.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Description:");
 		
 		descArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		descArea.setText(ourOntEntry.getDescription());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(descArea, gridData, 75, 5);
 		gridData.horizontalSpan = 2;
 		gridData.heightHint = descArea.getLineHeight() * 3;
 		descArea.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Importance:");
 		importanceBox = new Combo(shell, SWT.NONE);
 		Enumeration impEnum = Importance.elements();
 		int l=0;
 		Importance itype;
 		while (impEnum.hasMoreElements())
 		{
 			itype = (Importance) impEnum.nextElement();
 			importanceBox.add( itype.toString() );
 			if (itype.toString().compareTo(ourOntEntry.getImportance().toString()) == 0)
 			{
 				importanceBox.select(l);
 			}
 			l++;
 		}
 		importanceBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 		
 		new Label(shell, SWT.NONE).setText(" ");
 		new Label(shell, SWT.NONE).setText(" ");
 		
 		addButton = new Button(shell, SWT.PUSH); 
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		addButton.setLayoutData(gridData);
 		if (newItem)
 		{
 			addButton.setText("Add");
 			addButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent event) 
 				{
 					canceled = false;
 					if (!nameField.getText().trim().equals(""))
 					{
 						ConsistencyChecker checker = new ConsistencyChecker(ourOntEntry.getID(), nameField.getText(), "OntEntries");
 						
 						if(ourOntEntry.getName() == nameField.getText() || checker.check())
 						{
 							ourParent.addChild(ourOntEntry);
 							ourOntEntry.setLevel(ourParent.getLevel() + 1);
 							ourOntEntry.setName(nameField.getText());
 							ourOntEntry.setDescription(descArea.getText());
 							ourOntEntry.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 							
 							//comment before this made no sense...
 							ourOntEntry.setID(ourOntEntry.toDatabase(ourParent.getID()));	
 							System.out.println("Name of added item = " + ourOntEntry.getName());
 							
 							shell.close();
 							shell.dispose();	
 						}
 					}
 					else
 					{
 						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 						mbox.setMessage("Need to provide the OntEntry name");
 						mbox.open();
 					}
 				}
 			});
 			
 		}
 		else
 		{
 			addButton.setText("Save");
 			addButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent event) 
 				{
 					canceled = false;
 					
 					ConsistencyChecker checker = new ConsistencyChecker(ourOntEntry.getID(), nameField.getText(), "OntEntries");
 					
 					if(ourOntEntry.getName() == nameField.getText() || checker.check())
 					{
 						ourOntEntry.setName(nameField.getText());
 						ourOntEntry.setDescription(descArea.getText());
 						ourOntEntry.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 						//since this is a save, not an add, the type and parent are ignored
 						ourOntEntry.setID(ourOntEntry.toDatabase(0));
 						
 						//			RationaleDB db = RationaleDB.getHandle();
 						//			db.addOntEntry(ourOntEntry);
 						
 						shell.close();
 						shell.dispose();	
 					}
 					
 				}
 			});
 		}
 		
 		
 		
 		cancelButton = new Button(shell, SWT.PUSH); 
 		cancelButton.setText("Cancel");
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		cancelButton.setLayoutData(gridData);
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent event) 
 			{
 				canceled = true;
 				shell.close();
 				shell.dispose();
 			}
 		});
 		
 		shell.pack();
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 	}
 	
 	
 	/**
 	 * Get the ontology entry
 	 */  
 	public RationaleElement getItem()
 	{
 		return ourOntEntry;
 	}
 	
 	
 }
 
 
 
 
 
 
