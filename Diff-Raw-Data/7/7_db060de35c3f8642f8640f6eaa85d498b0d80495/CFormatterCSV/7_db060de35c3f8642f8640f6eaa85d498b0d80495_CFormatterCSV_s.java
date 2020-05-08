 package com.dovico.importexporttool;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import com.dovico.importexporttool.CFieldItem.FieldItemType;
 
 
 public class CFormatterCSV implements IExportFormatter, IImportFormatter {
 	//------------------
 	// EXPORT methods:
 	//------------------
 	// Helper functions for the Save As dialog to know how to save a file of this format
 	public String getSaveAsFileFilterDescription() { return "CSV (Comma delimited) (*.csv)"; }
 	public String getSaveAsFileFilterExtension() { return ".csv"; }
 	
 	// This class will be used in a drop-down list. The toString method must be overridden to display an appropriate value in the list.
 	public String toString() { return getSaveAsFileFilterDescription(); }
 	
 	
 	// Function that is called if the fields should be written out to the file as headers
 	public boolean WriteHeaders(ArrayList<CFieldItem> alFields, BufferedWriter bwWriter) { 
 		boolean bAllOK = true;
 		
 		try {
 			boolean bFirstValue = true;
 			
 			// Loop through the list of fields...
 			for (CFieldItem fiFieldItem: alFields) {
 				// Only write out a comma separator if we have already added a value
 				if(bFirstValue){ bFirstValue = false; }
 				else { bwWriter.write(","); }
 				
 				
 				// Write out the field's caption/display name 
 				bwWriter.write(("\"" + fiFieldItem.toString() + "\"")); 
 			} // End of the for (CFieldItem fiFieldItem: alFields) loop.
 			
 			
 			// Add a new line following the data we just added so the next data that gets written will be on it's own line
 			bwWriter.newLine();	
 		} 
 		catch (IOException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			bAllOK = false;
 		} 
 		
 		// Tell the calling function if everything went OK
 		return bAllOK; 
 	}
 	
 	
 	// Function that is called to write out the field values to the file
 	public boolean WriteData(ArrayList<CFieldItem> alFields, BufferedWriter bwWriter) {
 		boolean bAllOK = true;
 		
 		try {
 			CFieldItem.FieldItemType iFieldType = CFieldItem.FieldItemType.String;
 			boolean bFirstValue = true;
 			boolean bAddQuotes = false;
 			
 			// Loop through the list of fields to save to the file... 
 			for (CFieldItem fiFieldItem: alFields) {
 				// Only write out a comma separator if we have already added a value
 				if(bFirstValue){ bFirstValue = false; }
 				else { bwWriter.write(","); }
 				
 				
 				// Get the field type that we're dealing with. If we're dealing with a String or Date value then we add double quotes around the value. 
 				// Otherwise, we do not.
 				iFieldType = fiFieldItem.getFieldType();
 				bAddQuotes = ((iFieldType == CFieldItem.FieldItemType.String) || (iFieldType == CFieldItem.FieldItemType.Date));
 				
 				// Write out the value (surround with double-quotes if need be) 
 				if(bAddQuotes) { bwWriter.write("\""); }
				bwWriter.write(fiFieldItem.getValue());
 				if(bAddQuotes) { bwWriter.write("\""); }
 			} // End of the for (CFieldItem fiFieldItem: alFields) loop.
 			
 			
 			// Add a new line following the data we just added so the next data that gets written will be on it's own line
 			bwWriter.newLine();		
 		} 
 		catch (IOException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			bAllOK = false;
 		} 
 		
 		// Tell the calling function if everything went OK
 		return bAllOK; 
 	}
 	
 	
 	//------------------
 	// IMPORT methods:
 	//------------------
 	// Function that is called to read in the file's column headers (if there are column headers for the file type)
 	public Result ReadHeaders(BufferedReader brReader, ArrayList<CFieldItem> alReturnColumnsInTheFile) {
 		Result iResult = Result.AllOK; 
 		
 		try {
 			// Grab the first line from the text file (this is our column headers line) and then split the string at the comma (,)
 			String sFirstLine = brReader.readLine();
 			String[] arrColumns = sFirstLine.split(",");
 			
 			// Loop through our list of columns...
 			for (String sColumn : arrColumns) {
 				// Replace the first double-quote (") character in the string and then replace the last double-quote (") character in the string (doing it this way
 				// just in case double quotes are ever somewhere else in the column name too)
 				sColumn = sColumn.replaceFirst("^\"", "");
 				sColumn = sColumn.replaceFirst("\"$", "");
 				
 				// Create the field item (we just specify the field type is string because we really have no idea at this point)
 				alReturnColumnsInTheFile.add(new CFieldItem(0, sColumn, sColumn, FieldItemType.String));
 			} // End of the for (String sColumn : arrColumns) loop.			
 		}
 		catch (IOException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			iResult = Result.Error;
 		} 
 		
 		// Tell the calling function if everything went OK
 		return iResult; 
 	}
 	
 	
 	// Function that is called to read in one record from the file at a time. A flag is passed in indicating if this function is being called for the first
 	// line (for CSV files, the first line is the column headers so we will skip that line) 
 	// The record's values are stored in the alCurrentMappings' Destination field item.
 	public Result ReadRecord(BufferedReader brReader, boolean bFirstLine, ArrayList<CFieldItem> alColumnsInTheFile, ArrayList<CFieldItemMap> alCurrentMappings) {
 		Result iResult = Result.AllOK; 
 		
 		try {
 			// Read in the current line of data. Loop while the string is not null but it IS empty...(needed to prevent an empty element from being sent to the
 			// REST API for the POST when all we have is an extra line feed at the end of the file)
 			String sLine = null;
 			do { 
 				sLine = brReader.readLine(); 
 			} while((sLine != null) && (sLine.isEmpty()));
 			
 			// If this is the first line of data in the file AND we have not yet reached the end of the file then...(our first line is the column headers so we 
 			// want to skip to the next line)
 			if(bFirstLine && (sLine != null)) { sLine = brReader.readLine(); }
 												
 			
 			// If we've reached the end of the file then...
 			if(sLine == null) { iResult = Result.EndOfFile; }
 			else { // We have a line of data to parse...
 				// We write out our CSV data with strings and dates being surrounded by double quotes and all other data types (integer, float, etc) not being
 				// surrounded by double quotes. We don't want to split a string column at a comma that is part of the string so we manually step through the
 				// string instead...
 				String sCurrentColumnValue = "";
 				int iAdjustIndex = 0;
 				int iNextPos = 0;
 				int iColIndex = 0;
 				
 				// Get the length of the string and loop while the string is longer than 0 characters....
 				int iLineLength = sLine.length();
 				while(iLineLength > 0) {
 					iAdjustIndex = 0;
 					
 					// If the first character is a double-quote character then...(the current column is a string)
 					if(sLine.charAt(0) == '\"') {
 						// Find the end of the current column by specifying the double-quote and a comma (",) so that we don't accidently split the column if it 
 						// happens to contain a comma
 						iNextPos = sLine.indexOf("\",");
 						iAdjustIndex = 1;
 					}
 					else { // The current column is a number...					
 						iNextPos = sLine.indexOf(",");
 					} // End if(sLine.charAt(0) == '\"') 
 					
 					
 					// If we did not find a comma then we've reached the end of the line of data. Set the iNextPos value to be the last character in the string
 					// (if we're dealing with a string column then the last character in the string that we want is the one before the closing double-quote which
 					// is what the iAdjustIndex value will do for us)
 					if(iNextPos == -1) { iNextPos = (iLineLength - iAdjustIndex); }
 										
 					// Grab the current column's data. If we're dealing with a string column then start pulling the current column's data at character 2 (skipping
 					// the first double-quote character)
 					sCurrentColumnValue = sLine.substring(iAdjustIndex, iNextPos);
 					
 					// Pass the value to the following function so that the value can be added to the correct Destination item in the Mapping's list (we also pass
 					// in the column from alColumnsInTheFile that matches the current column value's index so that the function can find the proper 
 					// source/destination maps to modify)
 					setMappingValue((CFieldItem)alColumnsInTheFile.get(iColIndex), alCurrentMappings, sCurrentColumnValue);
 										
 					
 					// Adjust the iNextPos value to be past the comma (and double-quote if iAdjustIndex is 1. if not a string, iAdjustIndex is 0). If we have 
 					// passed the end of the line then exit this loop.
 					iNextPos += (1 + iAdjustIndex);
 					if(iNextPos >= iLineLength) { break; }
 
 					// Grab the next section of the string following the column we just pulled the data for and get the new line's length
 					sLine = sLine.substring(iNextPos);
 					iLineLength = sLine.length(); 
 					
 					// Increment the column index value
 					iColIndex++; 
 				} // End of the while(iLineLength > 0) loop.
 			} // End if(sLine == null)		
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			iResult = Result.Error;
 		}
 		
 		// Tell the caller if everything is OK or not
 		return iResult;
 	}
 	
 	
 	// Puts the column value into the Destination item's value for each mapping that contains our source column  
 	private void setMappingValue(CFieldItem fiColumnInTheFile, ArrayList<CFieldItemMap> alCurrentMappings, String sColumnValue) throws Exception {
 		// Grab the name of the column
 		String sColumnName = fiColumnInTheFile.getElementName();
 		
 		// Loop through our ArrayList of mappings (the same source column could be mapped to multiple destination fields so we can't simply stop looping if we
 		// find a match)...
 		for (CFieldItemMap fiFieldItemMap : alCurrentMappings) {
 			// If the current mapping has the source column we're looking for then...
 			if(fiFieldItemMap.getSourceItem().getElementName().equals(sColumnName)) {
 				// Set the value in the destination item to be the value passed into this function
 				fiFieldItemMap.getDestinationItem().setValue(sColumnValue, false);
 			} // End if			
 		} // End of the for (CFieldItemMap fiFieldItemMap : alCurrentMappings) loop.
 	}
 }
