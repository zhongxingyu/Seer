 package org.fritzing.fritzing.diagram.export;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.fritzing.fritzing.diagram.part.FritzingDiagramEditorUtil;
 
 
 public class ScriptExporter {	
 	// the following header text string is prepended to all schematic-
 	// generating script files we produce - it sets up the schematic 
 	// editing environment, opens a schematic sheet, and generally gets
 	// things all set up
 	private String headerText = "GRID INCH 0.005 \n" + // finer grid to enable exact placement of wires
 		// the DISPLAY command chooses the visible layers.
 		// only visible layers can be selected, connected to, or 
 		// otherwise acted on.
 		// layers must first be entered into the layer setup 
 		// (here accomplished with 'LAYER <number> <name>')
 		// first, turn on all needed schematic layers in case they are 
 		// not already available.
 		"LAYER 91 Nets; \n" + 	
 		"LAYER 92 Busses; \n" + 
 		"LAYER 93 Pins; \n" + 
 		"LAYER 94 Symbols; \n" + 
 		"LAYER 95 Names; \n" + 
 		"LAYER 96 Values; \n" + 
 		// act on the layers already entered in the layer setup AND
 		// turn OFF visibility of the PINS layer
 		"DISPLAY -PINS \n" +	 
 		" \n" + 
 		// load the drawing to be acted on - here schematic sheet 1 (.S1)
 		"EDIT .S1 \n" + 
 		// set the bend angle for wires in the schematic sheet 
 		// bend angle 2 used here produces straight-line connections at
 		// arbitrary angles
 		"SET WIRE_BEND 2; \n" + 
 		// set the wire style visual appearance - we want continuous lines
 		"CHANGE STYLE 'Continuous' \n" + 
 		// tell the schematic editor to use the Fritzing Eagle library
 		// it is not loaded by default
 		"USE '" + FritzingDiagramEditorUtil.getFritzingLocation() +
				"lbr/fritzing.lbr';\n";
 	
 	// the following footer text is appended to all schematic-generating
 	// script files we produce.  these commands are used primarily to return
 	// the Eagle environment to its default state
 	// the GRID LAST instruction tells Eagle to return the grid units
 	// to their previous state.  simple.
 	private String footerText = "GRID LAST; \n";
 	
 	// getHeaderText() and getFooterText() return the header and footer
 	// strings respectively
 	public String getHeaderText() {
 		return headerText;		
 	}
 	
 	public String getFooterText() {
 		return footerText;
 	}
 	
 	public String getPartEntry(EagleSCRPart part) {
 		// place a schematic symbol 
 		// Eagle syntax is "ADD <libraryPart>@<libraryName> 'partName' 'gateName' R<rotationAngle> (<componentCoords>)"
 		String result = "ADD " + 
 			part.partType.toUpperCase() + 
 			"@" + part.libraryName + " " + 
 			"'" + part.partName.toUpperCase() + "' " + 
 			"'" + part.gateNumber + "' " + 
 			part.rotationPrefix + part.rotationVal + " " +  
 			"(" + part.partPos.xVal + " " + part.partPos.yVal + "); \n";		
 		return result;
 	}
 	
 	public String getNetEntry(EagleSCRNet net) {
 		// place a net to connect two terminals - we are using simple,
 		// straight line, arbitrary-angle nets
 		// Eagle syntax is "NET '<netName>' (<sourceCoords>) (<targetCoords>)"
 		String result = "NET '" + net.netName + "' (" + 
 			net.sourcePos.xVal + " " + net.sourcePos.yVal + ") (" +
 			net.targetPos.xVal + " " + net.targetPos.yVal + "); \n";
 		return result;
 	}
 	
 	public String getNetNameEntry(EagleSCRNet net) {
 		// name the net (not strictly required, but it's nice)
 		// Eagle script syntax is "NAME '<netName>' (<xpos> <ypos>)"
 		// where netName unless otherwise specified follows a N$1 format
 		// and xpos and ypos are usually the same as for the start or 
 		// end terminal
 		String result = "NAME '" + net.netName + "' " + 
 			"(" + net.sourcePos.xVal + " " + net.sourcePos.yVal + "); \n";
 		return result;
 	}
 	
 	public String export(ArrayList<EagleSCRPart> partList, ArrayList<EagleSCRNet> netList) {
 		// this should take a file handle and the pre-populated part and net listArrays as arguments
 		// iterate through each entry, printing corresponding lines plus header and footer text
 		String result = "";
 		result = result.concat(getHeaderText());
 		
 		for (int i=0; i<partList.size(); i++) {
 			result = result.concat(getPartEntry((EagleSCRPart)partList.get(i)));
 		}
 		
 		for (int i=0; i<netList.size(); i++) {
 			result = result.concat(getNetEntry((EagleSCRNet)netList.get(i)));
 		}
 		
 		result = result.concat(getFooterText());
 		
 		return result;
 	}
 }
