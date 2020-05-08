 package com.dovico.importexporttool;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.border.BevelBorder;
 import javax.swing.table.DefaultTableModel;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import java.util.ArrayList;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.*;
 
 import com.dovico.commonlibrary.APIRequestResult;
 import com.dovico.commonlibrary.CRESTAPIHelper;
 import com.dovico.commonlibrary.CXMLHelper;
 
 
 
 public class CPanel_Import extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private JTextField m_txtImportFrom = null;
 	private JComboBox m_ddlFormat = null;
 	private JComboBox m_ddlDestination = null;
 	
 	private DefaultTableModel m_tmMappingModel = null; 
 	
 	ArrayList<CFieldItem> m_alColumnsInTheFile = null; // Will hold the Columns for the selected import file (populated when the Map button is pressed for the first time after a file is selected
 	ArrayList<CFieldItemMap> m_alCurrentMappings = null; // Will hold the mappings
 
 	// Will hold a reference to the UI Logic parent class
 	private CCommonUILogic m_UILogic = null;
 		
 	
 	// Default constructor
 	public CPanel_Import(CCommonUILogic UILogic) {
 		// Remember the reference to the UI Logic parent class
 		m_UILogic = UILogic;
 		
 		this.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("65px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("125px"),
 				RowSpec.decode("75px"),
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("default:grow"),}));
 		
 		
 		// Import From controls:
 		JLabel lblImportFrom = new JLabel("Import from:");
 		lblImportFrom.setFont(new Font("Arial", Font.PLAIN, 11));
 		this.add(lblImportFrom, "2, 2, left, default");
 		
 		m_txtImportFrom = new JTextField();
 		m_txtImportFrom.setEditable(false);
 		m_txtImportFrom.setFont(new Font("Arial", Font.PLAIN, 11));		
 		m_txtImportFrom.setColumns(10);
 		m_txtImportFrom.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) { OnClick_cmdBrowse(); } // Call the Browse button's code when the text box is clicked
 		});
 		this.add(m_txtImportFrom, "4, 2, 3, 1, fill, default");
 		
 		
 		JButton cmdBrowse = new JButton("...");
 		cmdBrowse.setToolTipText("Choose the file to import.");
 		cmdBrowse.setFont(new Font("Arial", Font.PLAIN, 11));
 		cmdBrowse.addActionListener(new ActionListener() { 
 			public void actionPerformed(ActionEvent arg0) { OnClick_cmdBrowse(); } 
 		});
 		this.add(cmdBrowse, "8, 2");
 		
 		
 		// Format controls:
 		JLabel lblFormat = new JLabel("Format:");
 		lblFormat.setFont(new Font("Arial", Font.PLAIN, 11));
 		this.add(lblFormat, "2, 4, left, default");
 		
 		m_ddlFormat = new JComboBox(getFormatValues());
 		m_ddlFormat.setFont(new Font("Arial", Font.PLAIN, 11));
 		this.add(m_ddlFormat, "4, 4, 5, 1, fill, default");
 		
 		
 		// Destination controls:
 		JLabel lblDestination = new JLabel("Destination:");
 		lblDestination.setFont(new Font("Arial", Font.PLAIN, 11));
 		this.add(lblDestination, "2, 6, left, default");
 		
 		m_ddlDestination = new JComboBox(getDestinationValues());
 		m_ddlDestination.setFont(new Font("Arial", Font.PLAIN, 11));
 		m_ddlDestination.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent arg0) { OnSelChanged_ddlDestination(); }
 		});
 		this.add(m_ddlDestination, "4, 6, 5, 1, fill, default");
 				
 		m_tmMappingModel = new DefaultTableModel(getMappingColumnNames(), 0);
 		
 		
 		// Mapping controls:
 		JLabel lblMapping = new JLabel("Mapping:");
 		lblMapping.setFont(new Font("Arial", Font.PLAIN, 11));
 		this.add(lblMapping, "2, 8, left, top");
 				
 		JTable tblMapping = new JTable(m_tmMappingModel);
 		tblMapping.setShowGrid(false);
 		tblMapping.setFont(new Font("Arial", Font.PLAIN, 11));
 		
 		JScrollPane spMapping = new JScrollPane(tblMapping);
 		tblMapping.setFillsViewportHeight(true);
 		spMapping.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		this.add(spMapping, "4, 8, fill, fill");
 		
 		JButton cmdMap = new JButton("Map...");
 		cmdMap.setFont(new Font("Arial", Font.PLAIN, 11));
 		cmdMap.addActionListener(new ActionListener() { 
 			public void actionPerformed(ActionEvent arg0) { OnClick_cmdMap(); } 
 		});
 		this.add(cmdMap, "6, 8, 3, 1, default, top");
 		
 		
 		// Import button
 		JButton cmdImport = new JButton("Import");
 		cmdImport.setFont(new Font("Arial", Font.PLAIN, 11));
 		cmdImport.addActionListener(new ActionListener() { 
 			public void actionPerformed(ActionEvent arg0) { OnClick_cmdImport(); } 
 		});
 		this.add(cmdImport, "6, 10, 3, 1");
 	}
 	
 	
 	// Returns the available values for the Format drop-down
 	private IImportFormatter[] getFormatValues() {
 		IImportFormatter[] arrItems = {  new CFormatterCSV() };
 		return arrItems;
 	}
 	
 
 	// Returns the available values for the Destination drop-down
 	private String[] getDestinationValues() {
 		String[] arrItems = { 
 			Constants.API_RESOURCE_ITEM_CLIENTS, 
 			Constants.API_RESOURCE_ITEM_PROJECTS, 
 			Constants.API_RESOURCE_ITEM_TASKS,
 			Constants.API_RESOURCE_ITEM_EMPLOYEES, 
 			Constants.API_RESOURCE_ITEM_TIME_ENTRIES, 
 			Constants.API_RESOURCE_ITEM_EXPENSE_ENTRIES 
 		};
 		return arrItems;
 	}
 
 	
 	// Returns the names for the columns in the Mapping table
 	private String[] getMappingColumnNames() {
 		String[] arrColNames = { "Source", "Destination" };
 		return arrColNames;
 	}
 		
 	
 	// Called when the user clicks on the Browse button 
 	private void OnClick_cmdBrowse() {
 		// Create a File Open object. Show the File Open dialog. If the user clicked OK/Save then...
 		JFileChooser dlgFileOpen = new JFileChooser();		
 		if(dlgFileOpen.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			// Put the path for the file name into the text box and clear the mapping table since it most likely does not match the data in the newly selected
 			// file.
 			m_txtImportFrom.setText(dlgFileOpen.getSelectedFile().getPath());			
 			clearRowsFromMappingTable();
 			m_alColumnsInTheFile = null;
 		} // End if(dlgFileOpen.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)		
 	}
 	
 	
 	// Helper method for clearing all rows from the mapping table
 	private void clearRowsFromMappingTable(){
 		// Loop until there are no more rows in the table by removing the first row during each loop... 
 		while(m_tmMappingModel.getRowCount() > 0) { m_tmMappingModel.removeRow(0); }
 		
 		// Empty the ArrayList object too
 		if(m_alCurrentMappings != null) { m_alCurrentMappings.clear(); }
 	}
 	
 	
 	// Called when the selection changes for the Destination drop-down. Clear the mapping table since the destination mapping no longer matches
 	private void OnSelChanged_ddlDestination() { clearRowsFromMappingTable(); }
 	
 	
 	// Called when the user clicks on the Map button
 	private void OnClick_cmdMap() {
 		// If there is no Import From path then...
 		String sFilePath = m_txtImportFrom.getText(); 
 		if(sFilePath.isEmpty()) {
 			JOptionPane.showMessageDialog(null, "Please specify a file to import", "Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		} // End if(sFilePath.isEmpty())
 		
 		
 		// Have the following function load in the column information from the file (will only be loaded in the first time the function is called after the file
 		// is chosen - a check will be made to see if m_alColumnsInTheFile is null or not). If there was an issue then exit now.
 		if(!getColumnsFromFile(sFilePath)) { return; }
 		
 		
 		// If the mappings list has not yet been created then create it now
 		if(m_alCurrentMappings == null) { m_alCurrentMappings = new ArrayList<CFieldItemMap>(); }	
 		
 		// Create an instance of our Add/Remove Fields dialog. Tell the dialog which Data Source and which Fields are selected 
 		Dialog_ImportFieldMapping dlgMapping = new Dialog_ImportFieldMapping();
 		dlgMapping.setSourceColumnsDestinationAndMapping(m_alColumnsInTheFile, (String)m_ddlDestination.getSelectedItem(), m_alCurrentMappings);
 		dlgMapping.setVisible(true);
 				
 		// If the dialog was closed as a result of the user clicking on the OK button then...
 		if(dlgMapping.getClosedByOKButton()){
 			// Remove all of the rows from our Mapping table
 			clearRowsFromMappingTable();
 									
 			// Get the list of mappings from the dialog and then add it to the list of mappings here
 			dlgMapping.getSelectedMappings(m_alCurrentMappings);
 			for (CFieldItemMap fiFieldItemMap : m_alCurrentMappings) { 
 				m_tmMappingModel.addRow(new Object[] { fiFieldItemMap.getSourceItem(), fiFieldItemMap.getDestinationItem() }); 
 			} // End of the for (CFieldItemMap fiFieldItemMap : m_alCurrentMappings) loop.
 		} // End if(dlgFields.getClosedByOKButton())
 	}
 	
 	
 	// Reads in the columns from the file
 	private boolean getColumnsFromFile(String sFilePath) {
 		// If the columns have already been loaded in then we don't need to continue
 		if(m_alColumnsInTheFile != null){ return true; }
 
 		
 		m_alColumnsInTheFile = new ArrayList<CFieldItem>();
 		IImportFormatter.Result iResult = IImportFormatter.Result.AllOK;
 		BufferedReader brReader = null;
 		
 		try {
 			// Get the selected formatter object from the Format drop-down
 			IImportFormatter fFormatter = (IImportFormatter)m_ddlFormat.getSelectedItem();
 			
 			// Open the file for reading
 			FileReader frReader = new FileReader(sFilePath);
 			brReader = new BufferedReader(frReader);
 			
 			// Ask the formatter to read in the header information
 			iResult = fFormatter.ReadHeaders(brReader, m_alColumnsInTheFile);			
 		} 
 		catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			iResult = IImportFormatter.Result.Error;
 		}
 		finally { // Happens whether or not there was an exception...
 			// If we have a reader object then make sure it's closed
 			try { 
 				if(brReader != null) { brReader.close(); } 
 			} 
 			catch (IOException e) { 
 				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 				iResult = IImportFormatter.Result.Error;
 			}
 		} // End of the finally block
 	
 		
 		// If there was an error then...
 		if(iResult == IImportFormatter.Result.Error) { 
 			m_alColumnsInTheFile = null;
 			return false; // Indicate that this function call was not successful
 		} else { // No error...
 			return true; // Indicate that this function call was successful
 		} // End if(iResult == IImportFormatter.Result.Error)
 	}
 	
 	
 	// Called when the user clicks on the Import button 
 	/// <history>
     /// <modified author="C. Gerard Gallant" date="2011-12-15" reason="Added a prompt if the logged in user token is not the admin user token and we are importing time/expenses. When using the Admin token, time/expenses are imported as approved time/expenses. Any other user token results in the time/expenses being imported as unsubmitted and I felt it was important for the user to be aware of that before they imported the data."/>
     /// </history>
 	private void OnClick_cmdImport() {
 		// If the validation fails then...
 		if(!validateForImport()) { return; }
 		
 		String sResource = (String)m_ddlDestination.getSelectedItem();		
 		String sRootElementName = CResourceHelper.getRootElementNameForResource(sResource);
 		String sMainElementName = CResourceHelper.getMainElementNameForResource(sResource);
 		
 		// Grab the logged in employee id. If the logged in user's token is not the admin token then...
 		Long lEmployeeID = m_UILogic.getEmployeeID();
 		if(lEmployeeID != Constants.ADMIN_TOKEN_EMPLOYEE_ID) {
 			String sWarningMsg = "";
 			
 			// If we're importing time entries then...
 			if(sResource.equals(Constants.API_RESOURCE_ITEM_TIME_ENTRIES)) {
 				sWarningMsg = "WARNING: The time will be imported as 'unsubmitted' because the user token specified is not the Administrator Data Access Token. Continue with the import?";
 			} else if(sResource.equals(Constants.API_RESOURCE_ITEM_EXPENSE_ENTRIES)){// If we're importing expense entries... 
 				sWarningMsg = "WARNING: The expenses will be imported as 'unsubmitted' because the user token specified is not the Administrator Data Access Token. Continue with the import?";
 			} // End if		
 		
 			// If we have a warning message then...
 			if(!sWarningMsg.isEmpty()) { 
 				// Ask the user if he/she wants to continue with the import. If NO then exit now.
 				if(JOptionPane.showConfirmDialog(null, sWarningMsg, "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) { return; }
 			} // End if(!sWarningMsg.isEmpty())
 		} // End if(lEmployeeID != Constants.ADMIN_TOKEN_EMPLOYEE_ID)
 		
 		
 		// Determine if we're importing expense data and if the logged in user is the Admin user token
 		boolean bImportingExpenses = sResource.equals(Constants.API_RESOURCE_ITEM_EXPENSE_ENTRIES);
 		IImportFormatter.Result iResult = IImportFormatter.Result.AllOK;
 		BufferedReader brReader = null;
 		
 		try {
 			// Get the selected formatter object from the Format drop-down
 			IImportFormatter fFormatter = (IImportFormatter)m_ddlFormat.getSelectedItem();
 			
 			// Open the file for reading
 			FileReader frReader = new FileReader(m_txtImportFrom.getText());
 			brReader = new BufferedReader(frReader);
 			boolean bFirstLine = true;
 
 			
 			// Start off the root XML (e.g. <Clients>) for the request that will be passed to the REST API
 			String sXML = ("<" + sRootElementName + ">");
 			
 			
 			// Loop while all is OK (returns EndOfFile if the end of the file is reached. Returns Error if there was a problem)
 			do {
 				// Make sure the values in the Destination fields are cleared so that a previous loop's data does not impact the current loop. 
 				clearMappingValues();
 				
 				// Read in the current record. If we hit the end of the file then...(no record was read in so exit the loop now)
 				iResult = fFormatter.ReadRecord(brReader, bFirstLine, m_alColumnsInTheFile, m_alCurrentMappings);
 				if(iResult == IImportFormatter.Result.EndOfFile) { break; }
 				
 				// Change the flag to no longer indicate that we're at the first line in the file
 				bFirstLine = false;
 				
 				// Build up the XML for the current Main Element (e.g. <Client>...</Client>)
 				sXML += buildXMLForMainElement(sMainElementName, bImportingExpenses, m_alCurrentMappings);
 		
 			} while(iResult == IImportFormatter.Result.AllOK);
 			
 			
 			// Close off the root XML (e.g. </Clients>) and send the XML to the REST API to have the data inserted (POST). If an error was displayed to the user
 			// then flag that there was an error so that the user doesn't get the 'Done' prompt (so that the user doesn't have to deal with two prompts)
 			sXML += ("</" + sRootElementName + ">");
 			String sURI = CResourceHelper.getURIForResource(sResource, false, lEmployeeID, null, null);//the dates are not used by an import
 			APIRequestResult arResult = CRESTAPIHelper.makeAPIRequest(sURI, "POST", sXML, m_UILogic.getConsumerSecret(), m_UILogic.getDataAccessToken());
 			if(arResult.getDisplayedError()) { iResult = IImportFormatter.Result.Error; }
 		} 
 		catch (FileNotFoundException e) { 
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
 			iResult = IImportFormatter.Result.Error;
 		}
 		finally { // Happens whether or not there was an exception...
 			// If we have a reader object then make sure it's closed
 			try { 
 				if(brReader != null) { brReader.close(); } 
 			} 
 			catch (IOException e) { 
 				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 				iResult = IImportFormatter.Result.Error;
 			}
 		} // End of the finally block
 	
 		
 		// If there were no errors then tell the user we're done
 		if(iResult != IImportFormatter.Result.Error) { JOptionPane.showMessageDialog(null, "Done", "Export Complete", JOptionPane.INFORMATION_MESSAGE); }		
 	}
 	
 	
 	// Validates to make sure the necessary data is present in order to do an Import
 	private boolean validateForImport() { 
 		// If there is no Import From path then...
 		if(m_txtImportFrom.getText().isEmpty()) {
 			JOptionPane.showMessageDialog(null, "Please specify a file to import", "Error", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} // End if(m_txtImportFrom.getText().isEmpty())
 		
 		// If no fields have been mapped then...
 		if(m_tmMappingModel.getRowCount() == 0) {
 			JOptionPane.showMessageDialog(null, "Please map at least one field for import", "Error", JOptionPane.ERROR_MESSAGE);
 			return false;
 		} // End if(m_tmMappingModel.getRowCount() == 0)
 		
 		
 		// We made it to this point. All is OK.
 		return true;
 	}
 	
 	
 	// Make sure the values in the Destination fields are cleared so that a previous loop's data does not impact the current loop. 
 	private void clearMappingValues() {
 		// Loop through the mappings list clearing each Destination item's value
 		for (CFieldItemMap fiFieldItemMap : m_alCurrentMappings) { fiFieldItemMap.getDestinationItem().setValue(""); }
 	}
 	
 	
 	// Helper to build up the XML for a Main Element  
 	/// <history>
     /// <modified author="C. Gerard Gallant" date="2012-03-28" reason="I should write a string builder class for XML (if one doesn't already exist) because I forgot to encode the fiDestination.getValue() value for special characters like '&' and '<'."/>
    /// <modified author="C. Gerard Gallant" date="2012-05-31" reason="Changed the CXMLHelper function call from fixXmlString to encodeTextForElement because the encoding of the single & double quote characters was causing a parse error in the REST API and a Bad Request error to be returned. The import would fail if any of the entries had a single or double quote character."/>
 	/// </history>
 	private String buildXMLForMainElement(String sMainElementName, boolean bImportingExpenses, ArrayList<CFieldItemMap> alCurrentMappings) {
 		CFieldItem fiDestination = null;
 		String sElementName = "";
 		boolean bExpenseEntryOpeningTagsAdded = false; // Don't want to add closing tags if no opening tags were added in the loop
 	
 		
 		// Start off our main element (e.g. <Client>)
 		String sReturnXML = ("<" + sMainElementName + ">");
 		
 		// Loop through our ArrayList of mappings (the same source column could be mapped to multiple destination fields so we can't simply stop looping if we
 		// find a match)...
 		for (CFieldItemMap fiFieldItemMap : m_alCurrentMappings) {
 			// Grab the current destination item and grab the destination item's element name 
 			fiDestination = fiFieldItemMap.getDestinationItem();							
 			sElementName = fiDestination.getElementName();
 			
 			// If we're importing Expenses AND have not yet added the opening tags for the Expense Entries AND we're no longer dealing with the main element 
 			// then...(we've hit the list of expense entries)
 			if(bImportingExpenses && !bExpenseEntryOpeningTagsAdded && !fiDestination.getRootElementName().equals(sMainElementName)) { 
 				// Start off our <ExpenseEntries><ExpenseEntry> node and flag that we have now added the opening tags
 				sReturnXML += ("<"+ CResourceHelper.ROOT_ELEMENT_NAME_FOR_EXPENSE_SHEET_ENTRIES +"><"+ CResourceHelper.MAIN_ELEMENT_NAME_FOR_EXPENSE_ENTRIES +">");
 				bExpenseEntryOpeningTagsAdded = true;
 			} // End if(bImportingExpenses && !bExpenseEntryOpeningTagsAdded && !fiDestination.getRootElementName().equals(sMainElementName))
 			
 			// Build up the current element's XML containing the current destination item's value (e.g. <ID>100</ID>)
			sReturnXML += ("<" + sElementName + ">" + CXMLHelper.encodeTextForElement(fiDestination.getValue()) + "</" + sElementName + ">");		
 		} // End of the for (CFieldItemMap fiFieldItemMap : m_alCurrentMappings) loop.
 		
 		
 		// If Expense Entries were added then close off our </ExpenseEntries> and </ExpenseEntry> nodes 
 		if(bExpenseEntryOpeningTagsAdded) { sReturnXML += ("</"+ CResourceHelper.MAIN_ELEMENT_NAME_FOR_EXPENSE_ENTRIES +"></"+ CResourceHelper.ROOT_ELEMENT_NAME_FOR_EXPENSE_SHEET_ENTRIES +">"); }
 		
 		// Close off our main element (e.g. </Client>) and return the XML to the caller
 		sReturnXML += ("</" + sMainElementName + ">");
 		return sReturnXML;
 	}
 }
