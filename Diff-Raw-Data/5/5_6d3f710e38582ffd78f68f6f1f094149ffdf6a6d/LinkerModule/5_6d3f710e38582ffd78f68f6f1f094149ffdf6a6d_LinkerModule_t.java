 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import simulanator.ScanWrap;
 
 /**
  * 
  * @author Eric
  * @date May 12, 2012; 3:59:15 PM
  */
 public class LinkerModule implements Comparable<LinkerModule>{
 
 	/**
 	 * The type of record being added to.
 	 * @author Noah
 	 * @date May 25, 2012; 6:50:23 PM
 	 */
 	public enum AddType {
 		/**Header Record*/
 		HEADER, 
 		/**Linker Record*/
 		LINKER, 
 		/**Text Record*/
 		TEXT, 
 		/** Mod Record */
 		MOD,
 		/**End Record*/
 		END;
 	}
 	
 	/**
 	 * A user report for a single linker module.
 	 * @author Noah
 	 * @date May 25, 2012; 7:09:53 PM
 	 */
 	public class UserReport {
 		/** what type of record being added to */
 		public AddType addType = AddType.HEADER;
 		/** the object file code for header record + errors in header record. */
 		private String headerRecord = "";
 		/** the object file codes for linker records + errors in the linker records. */
 		private Map<Integer, String> linkerRecords = new TreeMap<Integer, String>(); 
 		/** the object file codes for text records + errors in the text records. */
 		private Map<Integer, String> textRecords = new TreeMap<Integer, String>(); 
 		/** the object file code for end record + errors in end record. */
 		private String endRecord = "";
 		
 		/**
 		 * Adds content to the contents of record with addType with the key address.
 		 * Requires: addType = LINKER, or TEXT.
 		 * @author Noah
 		 * @date May 25, 2012; 6:50:43 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param address the address of contents.
 		 * @param content the string to add to contents.
 		 * @specRef N/A
 		 */
 		public void add(int address, String content) {
 			String newContent;
 			switch(this.addType) {
 			case LINKER:
 				if(this.linkerRecords.get(address) == null) {
 					this.linkerRecords.put(address, "");
 				}
 				newContent = this.linkerRecords.get(address) + content;
 				this.linkerRecords.put(address, newContent);
 			break;
 			case TEXT:
 				if(this.textRecords.get(address) == null) {
 					this.textRecords.put(address, "");
 				}
 				newContent = this.textRecords.get(address) + content;
 				this.textRecords.put(address, newContent);
 			}
 		}
 		
 		/**
 		 * Adds content to headerRecord or endRecord.
 		 * Requires:addType = HEADER or END.
 		 * @author Noah
 		 * @date May 25, 2012; 6:51:09 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param content content to add.
 		 * @specRef N/A
 		 */
 		public void add(String content) {
 			switch(this.addType) {
 			case HEADER:
 				this.headerRecord = this.headerRecord + content;
 			break;
 			case END:
 				this.endRecord = this.endRecord + content;
 			break;
 			}
 		}
 		
 		
 		/** 
 		 * Returns a string representation of the object
 		 * 
 		 * @author Ratul Khosla
 		 * @date May 25, 2012; 8:23:09 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @specRef N/A
 		 */
 		@Override
 		public String toString() {
 			String report;
 			report = headerRecord;
 			
 			Map<Integer, String> contents = this.linkerRecords;
 			
 			for(Map.Entry<Integer, String> record : contents.entrySet()) { 		
 				report += record.getValue();  
 			}
 			
 			contents = this.textRecords;
 			
 			for(Map.Entry<Integer, String> record : contents.entrySet()) { 		
 				report += record.getValue();				
 			}
 			
 			report += endRecord; 
 			return report;
 		}
 	}
 
 	/** user report */
 	public UserReport userRep = new UserReport();
 	/** Contains all the link records. */
 	public Map<String, Integer> linkRecord = new HashMap<String, Integer>();
 	/** Contains all the mod and text records. */
 	public List<TextModRecord> textMod = new ArrayList<TextModRecord>();
 	/**Contains all the errors. */
 	public List<TextRecord> errorText = new ArrayList<TextRecord>();
 	/** Name of program. */
 	public String progName;
 	/** Program load address. */
 	public int loadAddr;
 	/** Length of program. */
 	public int prgTotalLen;
 	/** Start address of the program */
 	public int execStart;
 	/** Date the object file was made */
 	public String date;
 	/** Version that was used to make outputfile */
 	public int version;
 	/** Total number of records */
 	public int endRec;
 	/** Total number of link records */
 	public int endLink;
 	/** Total number of text records */
 	public int endText;
 	/** Total number of mod records */
 	public int endMod;
 	/** the offset amount from program address. */
 	public int offset;
 	/** Success failure boolean */
 	public boolean success = false;
 	/** End reached */
 	public boolean done = true;
 	/** file name being read. */
 	public String filename;
 	/**
 	 * 
 	 * @author Eric
 	 * @date May 20, 2012; 11:42:52 PM
 	 */
 	public static class TextModRecord {
 		/**Text Records*/
 		public TextRecord text = new TextRecord();
 		/**Mod Records*/
 		public List<ModRecord> mods = new ArrayList<ModRecord>();
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:31 PM
 	 */
 	public static class TextRecord {
 		/** Program assigned LC for text record */
 		public int assignedLC;
 		/** Assembled instruction */
 		public String instrData;
 		/** Flag for high order bits */
 		public char flagHigh;
 		/** Flag for low order bits */
 		public char flagLow;
 		/** Number of modifications for high order bits */
 		public int modHigh;
 		/** Number of modifications for low order bits */
 		public int modLow;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:53 PM
 	 */
 	public static class ModRecord {
 		/** 4 hex nybbles */
 		public int hex;
 		/** H, L, or S */
 		public char HLS;
 		/** The middle of modifications records*/
 		public List<MiddleMod> midMod = new ArrayList<MiddleMod>();
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 17, 2012; 5:50:57 PM
 	 */
 	public static class MiddleMod {
 		/** Plus or minus sign */
 		public char plusMin;
 		/** Flag A or E or N */
 		public char addrType;
 		/** The linkers label for mods */
 		public String linkerLabel;
 	}
 
 	/**
 	 * 
 	 * @param read
 	 *            the outputFile containing all the records
 	 * @param error errorhandler for constructor
 	 */
 	public LinkerModule(Scanner read, ErrorHandler error){
 		// scan wrap
 		ScanWrap reader = new ScanWrap(read, error);
 
 		//String used for name
 		String ender = "";
 		String temp = "";
 
 
 		//Number of records
 		int mod = 0;
 		int totalMod = 0;
 		int numberOfMod = 0;
 		int link = 0;
 		int text = 0;
 
 		//value checking 
 		boolean isValid = true;
 		boolean add = true;
 		boolean addLink = true;
 		boolean addHeader = true;
 		
 		//checks for an H
 		String check = reader.readString(ScanWrap.notcolon, "loaderNoHeader");
 		if (!reader.go("disreguard"))
 			return;
 
 		//Runs through header record
 		if (check.equalsIgnoreCase("H")) {
 			String completeString = "";
 			String errorMessage = "";
 			//Program Name
 			this.progName = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			completeString = "H:" + this.progName + ":";
 			//Load Adrress
 			this.loadAddr = reader.readInt(ScanWrap.hex4, "loaderHNoAddr", 16);
 			if (!reader.go("disreguard"))
 				return;
 			isValid = OperandChecker.isValidMem(this.loadAddr);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			completeString = completeString + this.loadAddr+ ":";
 			//total length
 			this.prgTotalLen = reader
 					.readInt(ScanWrap.hex4, "loaderHNoPrL", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.prgTotalLen);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			completeString = completeString + this.prgTotalLen + ":";
 			//excution start
 			this.execStart = reader.readInt(ScanWrap.hex4, "loaderNoEXS", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.execStart);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			completeString = completeString + this.execStart+ ":";
 			//date of program
 			this.date = reader.readString(ScanWrap.datep, "loaderHNoDate");
 			if (!reader.go("disreguard")){
 				errorMessage = "Invalid Date";
 				addHeader = false;
 			}
 			completeString = completeString + this.date+ ":";
 			//version of program
 			this.version = reader.readInt(ScanWrap.dec4, "loaderHNoVer", 10);
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\nInvalid Version Number";
 				addHeader = false;
 			}
 			completeString = completeString + this.version+ ":";
 			//filler stuff
 			temp = reader.readString(ScanWrap.notcolon, "loaderHNoLLMM");
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\nInvalid must read URBAN-ASM";
 				addHeader = false;
 			}
 			completeString = completeString + temp+ ":";
 			
 			//end of the header record
 			ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard")){
 				addHeader = false;
 				errorMessage = errorMessage + "\n303: Program Name does not match end of record name.";
 			}
 			if(!ender.equals(this.progName)){
 				addHeader = false;
 				errorMessage = errorMessage + "\n303: Program Name does not match end of record name.";
 			}
 			//Adding info to User Report
 			if(addHeader){
 				completeString = completeString + ender + ":\n";
 			}else{
 				completeString = completeString + ender + ":" + errorMessage +"\n";
 			}
 			this.userRep.addType = AddType.HEADER;
 			this.userRep.add(completeString);
 		}else{
 			error.reportError(makeError("loaderNoHeader"),0,0); 
 			return;
 		}
 		
 		
 		//checks for L or T record
 		check = reader.readString(ScanWrap.notcolon, "");
 		if (!reader.go("disreguard"))
 			return;
 		//loops to get all the L and T records from object file
 		while (check.equals("L") || check.equals("T")) {
 			TextModRecord theRecordsForTextMod = new TextModRecord();
 			String entryLabel = "";
 			int entryAddr = 0;
 			//gets all information from linker record
 			if (check.equals("L")) {
 				String errorMessage = "";
 				String completeString = "";
 				link++;
 				//entry Label
 				entryLabel = reader.readString(ScanWrap.notcolon, "");
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n814: Invalid label in linker record.";
 					addLink = false;
 				}
 				completeString = "L:"  + entryLabel + ":";
 				//entry Addr
 				entryAddr = reader.readInt(ScanWrap.hex4, "loaderNoEXS",
 						16);
 				if (!reader.go("disreguard"))
 				{
 					addLink = false;
 					errorMessage =errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				}
 				isValid = OperandChecker.isValidMem(entryAddr);
 				if(!isValid){
 					errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 					addLink = false;
 				}
 				completeString = completeString + entryAddr + ":";
 
 				//end of link record
 				boolean hack = true;
 				ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 				if(!ender.equals(this.progName)){
 					errorMessage = errorMessage + "\nLabel does not match Program Name";
 					hack = false;
 				}
 				//add link record to user report and linkerModule
 				if(addLink){
 					linkRecord.put(entryLabel, entryAddr);
 					if(hack){
						completeString =  completeString + ender + ":\n";
 					}else{
						completeString =  completeString + ender + ":" +errorMessage +"\n";
 					}
 				}else{
 					completeString =  completeString + ender + ":" +errorMessage +"\n";
 				}
 				this.userRep.addType = AddType.LINKER;
 				this.userRep.add(entryAddr, completeString);
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all information out of Text record
 			} else if (check.equals("T")) {
 				String errorMessage = "";
 				String completeString = "";
 				text++;
 				//assigned location counter
 				theRecordsForTextMod.text.assignedLC = reader.readInt(ScanWrap.hex4, "textLC", 16);
 				completeString = "T:" + theRecordsForTextMod.text.assignedLC + ":";
 				if (!reader.go("disreguard")){
 					errorMessage = errorMessage + "\n800: Text record missing valid program assigned location.";
 					add = false;
 				}
 				isValid = OperandChecker.isValidMem(theRecordsForTextMod.text.assignedLC);
 				if(!isValid){
 					errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 					add = false;
 				}
 				//assembled hex
 				theRecordsForTextMod.text.instrData = reader.readString(ScanWrap.notcolon, "textData");
 				completeString = completeString + theRecordsForTextMod.text.instrData+ ":";
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n801: Text record missing valid assembled instruction/data.";
 					add = false;
 				}
 				//flag for high order
 				theRecordsForTextMod.text.flagHigh = reader.readString(ScanWrap.notcolon, "textStatus").charAt(0);
 				completeString = completeString + theRecordsForTextMod.text.flagHigh + ":";
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n802: Text record missing valid status flag.";
 					add = false;
 				}
 				if(!(theRecordsForTextMod.text.flagHigh == 'A' || theRecordsForTextMod.text.flagHigh == 'R' || theRecordsForTextMod.text.flagHigh == 'E' || theRecordsForTextMod.text.flagHigh == 'C')){
 					errorMessage = errorMessage + "\n802: Text record missing valid status flag.";
 					add = false;
 				}		
 				//flag for low order
 				theRecordsForTextMod.text.flagLow = reader.readString(ScanWrap.notcolon, "textStatus").charAt(0);
 				completeString = completeString + theRecordsForTextMod.text.flagLow + ":";
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n802: Text record missing valid status flag.";
 					add = false;
 				}
 				if(!(theRecordsForTextMod.text.flagLow == 'A' || theRecordsForTextMod.text.flagLow == 'R' || theRecordsForTextMod.text.flagLow == 'E' || theRecordsForTextMod.text.flagLow == 'C')){
 					errorMessage = errorMessage + "\n802: Text record missing valid status flag.";
 					add = false;
 				}
 				//mods for high order
 				theRecordsForTextMod.text.modHigh = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				totalMod = theRecordsForTextMod.text.modHigh;
 				completeString = completeString + theRecordsForTextMod.text.modHigh + ":";
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n803: Text record missing valid number of modifications.";
 					add = false;
 				}
 				if(theRecordsForTextMod.text.modHigh>16 || theRecordsForTextMod.text.modHigh<0)
 				{
 					errorMessage = errorMessage + "\n803: Text record missing valid number of modifications.";
 					add = false;
 				}
 				//mods for low order
 				theRecordsForTextMod.text.modLow = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				totalMod = totalMod + theRecordsForTextMod.text.modLow;
 				completeString = completeString + theRecordsForTextMod.text.modLow + ":";
 				if (!reader.go("disreguard"))
 				{
 					errorMessage = errorMessage + "\n803: Text record missing valid number of modifications.";
 					add = false;
 				}
 				if(theRecordsForTextMod.text.modLow>16 || theRecordsForTextMod.text.modLow<0)
 				{
 					errorMessage = errorMessage + "\n803: Text record missing valid number of modifications.";
 					add = false;
 				}
 				//end of text record
 				ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 				boolean hack = true;
 				if(!ender.equals(this.progName)){
 					errorMessage = errorMessage + "\nLabel does not match Program Name.";
 					hack = false;
 				}
 				if(add && hack){
 					completeString = completeString + ender + ":\n";
 				}else{
 					completeString = completeString + ender + ":" + errorMessage +"\n";
 					errorMessage = "";
 				}
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all mod records for a text record
 				while (check.equals("M")) {
 					ModRecord modification = new ModRecord();
 					mod++;
 					//four hex nyblles
 					modification.hex = reader.readInt(ScanWrap.hex4, "modHex", 16);
 					completeString = completeString + "M:" + modification.hex + ":";
 					if (!reader.go("disreguard"))
 					{
 						errorMessage = errorMessage + "\n804: Modification record missing 4 hex nybbles.";
 						add = false;
 					}
 					isValid = OperandChecker.isValidMem(modification.hex);
 					if(!isValid){
 						errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 						add = false;
 					}
 					boolean run = true;
 					String loop = "";
 					boolean firstRun = true;
 					//gets the middle of modification records
 					while (run) {
 						numberOfMod++;
 						MiddleMod midtemp = new MiddleMod();
 						//Plus or minus sign
 						if(firstRun){
 						midtemp.plusMin = reader.readString(ScanWrap.notcolon,
 								"modPm").charAt(0);
 						firstRun=false;
 						}else{
 						midtemp.plusMin = loop.charAt(0);
 						}
 						completeString = completeString +midtemp.plusMin + ":";
 						if (!reader.go("disreguard"))
 						{
 							errorMessage = errorMessage + "\n805: Modification record missing plus or minus sign.";
 							add = false;
 						}
 						isValid = OperandChecker.isValidPlusMin(midtemp.plusMin);
 						if(!isValid){
 							errorMessage = errorMessage + "\n812: Modification records must contain plus or minus sign.";
 							add = false;
 						}
 						//Address type
 						midtemp.addrType = reader.readString(ScanWrap.notcolon,
 								"modFlag").charAt(0);
 						completeString = completeString + midtemp.addrType + ":";
 						if (!reader.go("disreguard"))
 						{
 							errorMessage = errorMessage + "\n806: Modification record missing correct flag R, E, or N.";
 							add = false;
 						}
 						if(!(midtemp.addrType == 'E' || midtemp.addrType == 'R' || midtemp.addrType == 'N')){
 							errorMessage = errorMessage + "\n806: Modification record missing correct flag R, E, or N.";
 							add = false;
 						}
 						//Linker Label
 						midtemp.linkerLabel = reader.readString(
 								ScanWrap.notcolon, "modLink");
 						completeString = completeString +midtemp.linkerLabel + ":";
 						if (!reader.go("disreguard"))
 						{
 							errorMessage = errorMessage + "\n807: Modification record missing valid label for address.";
 							add = false;
 						}
 						//either another plus/minus or HLS
 						loop = reader.readString(ScanWrap.notcolon, "modHLS");
 						if (!reader.go("disreguard"))
 						{
 							errorMessage = errorMessage + "\n808: Modification record missing correct char H, L, or S.";
 							add = false;
 						}
 						if (loop.equals("")) {
 							run = false;
 						}
 						modification.midMod.add(midtemp);
 					}
 					//HLS of modification record
 					loop = reader.readString(ScanWrap.notcolon, "modHLS");
 					if (!reader.go("disreguard"))
 					{
 						errorMessage = errorMessage + "\n808: Modification record missing correct char H, L, or S.";
 						add = false;
 					}
 					modification.HLS = loop.charAt(0);
 					completeString = completeString +modification.HLS + ":";
 					if(!(modification.HLS == 'H' || modification.HLS == 'L' || modification.HLS == 'S')){
 						errorMessage = errorMessage + "\n808: Modification record missing correct char H, L, or S.";
 							add = false;
 					}
 					
 					//End of Modification Record
 					boolean boobies = true;
 					ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 					if(!ender.equals(this.progName)){
 						errorMessage = errorMessage + "\nLabel does not match Program Name.";
 						boobies = false;
 					}
 					theRecordsForTextMod.mods.add(modification);
 					if(add && boobies){
 						completeString = completeString +ender + ":\n";
 					}else{
 						completeString = completeString +ender + ":" + errorMessage + "\n";	
 					}
 					check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 					if (!reader.go("disreguard"))
 						return;
 				}// end of mod record
 				
 				if(totalMod != numberOfMod){
 					errorMessage = "WARNING:Number of modifications done to a text record is not same in text record.\n";
 					completeString = completeString + errorMessage;
 				}
 				totalMod = 0;
 				numberOfMod = 0;
 				//Adds to the User Report and linkerModule
 				this.userRep.addType = AddType.TEXT;
 				this.userRep.add(theRecordsForTextMod.text.assignedLC, completeString);
 				if(add){
 					textMod.add(theRecordsForTextMod);
 				}else{
 					errorText.add(theRecordsForTextMod.text);
 					add = true;
 				}
 			}// end of text record
 			
 		}//end of while loop checking for linking records and text records
 		
 		boolean addEnd = true;
 		//checks for an end record
 		if (check.equals("E")) {
 			String errorMessage = "";
 			String completeString = "";
 			//Total number of records
 			this.endRec = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			completeString = "E:" + this.endRec + ":";
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			isValid = OperandChecker.isValidMem(this.endRec);
 			if(!isValid){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			//Total number of Link records
 			this.endLink = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			completeString = completeString + this.endLink + ":";
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			isValid = OperandChecker.isValidMem(this.endLink);
 			if(!isValid){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			//Total number of Text Records
 			this.endText = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			completeString = completeString + this.endText + ":";
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			isValid = OperandChecker.isValidMem(this.endText);
 			if(!isValid){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			//Total number of Modification Records
 			this.endMod = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			completeString = completeString + this.endMod + ":";
 			if (!reader.go("disreguard")){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			isValid = OperandChecker.isValidMem(this.endMod);
 			if(!isValid){
 				errorMessage = errorMessage + "\n811: Invalid value. Records values must be from 0 - 4095.";
 				addEnd = false;
 			}
 			//End of end records
 			ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if(!ender.equals(this.progName)){
 				errorMessage = errorMessage + "\nLabel does not match Program Name.";
 				addEnd = false;
 			}
 			if(link != this.endLink){
 				errorMessage = errorMessage + "\n304: Number of link records does not match end record amount.";
 				addEnd = false;
 			}
 			if(text != this.endText){
 				errorMessage = errorMessage + "\n306: Number of text records does not match end record amount.";
 				addEnd = false;
 			}
 			if(mod != this.endMod){
 				errorMessage = errorMessage + "\n305: Number of modification records does not match end record amount.";
 				addEnd = false;
 			}
 			if((link+text+mod+2) != this.endRec){
 				errorMessage = errorMessage + "\n307: Number of  total records does not match end record amount."; 
 				addEnd = false;
 			}
 			if(addEnd){
 				completeString = completeString + ender +":\n";
 			}else{
 				completeString = completeString + ender +":" + errorMessage + "\n";
 			}
 			//adds info to user Report
 			this.userRep.addType = AddType.END;
 			this.userRep.add(completeString);
 		}else{
 			error.reportError(makeError("loaderNoEnd"),0,0); 
 			return;
 		}
 		//program ran successful. Checks for more in file
 		this.success = true;
 		if (read.hasNext()) {
 			this.done = false;
 		}
 	}
 	/**
 	 * Compares loadAddr of the LinkerModules
 	 */
 	@Override
 	public int compareTo(LinkerModule cmp) {
 		if(this.loadAddr > cmp.loadAddr){
 			return 1;
 		}else if(this.loadAddr < cmp.loadAddr){
 			return -1;
 		}else{
 			return 0;
 		}
 	}
 	
 	/** 
 	 * Returns a string representation of the object
 	 * 
 	 * @author Ratul Khosla
 	 * @date May 25, 2012; 8:25:27 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @specRef N/A
 	 */
 	@Override
 	public String toString() {
 		return userRep.toString();
 	}
 
 }
