 package de.edgesoft.abook2vcf;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.text.MessageFormat;
 import java.util.List;
 
 import de.edgesoft.utilities.commandline.AbstractMainClass;
 import de.edgesoft.utilities.commandline.CommandOption;
 
 
 /**
  * Exports a thunderbird addressbook to one or several vcard (vcf) files.
  * 
  * @author Ekkart Kleinod
  * @version 0.1
  * @since 0.1
  */
 public class ABook2VCF extends AbstractMainClass {
 
 	/** Argument input file. */
 	private final static CommandOption OPT_ABOOK = new CommandOption("i", "input", true, "inputfile (default: abook.mab)", false);
 	
 	/** Argument output file. */
 	private final static CommandOption OPT_OUTFILE = new CommandOption("o", "outputfile", true, "output file (default: abook.vcf)", false);
 	
 	/** Argument vcard count. */
 	private final static CommandOption OPT_VCFCOUNT = new CommandOption("c", "count", true, "number of vcards per file (default: 0 = unlimited)", false);
 	
 	/** Vcard file extension. */
 	private final static String VCF_FILE_EXTENSION = ".vcf";
 	
 	/**
 	 * Main method, called from command line.
 	 * 
 	 * @param args command line arguments
 	 * 
 	 * @version 0.1
 	 * @since 0.1
 	 */
 	public static void main(String[] args) {
 		printMessage("start.");
 		
 		addCommandOption(OPT_ABOOK);
 		addCommandOption(OPT_OUTFILE);
 		addCommandOption(OPT_VCFCOUNT);
 		
 		init(args, ABook2VCF.class);
 		
 		try {
 			String sInFile = (getOptionValue(OPT_ABOOK) == null) ? "abook.mab" : getOptionValue(OPT_ABOOK);
 			String sOutFile = (getOptionValue(OPT_OUTFILE) == null) ? "abook.vcf" : getOptionValue(OPT_OUTFILE);
 			int iVCFCount = 0;
 			try {
 				iVCFCount = Integer.parseInt(getOptionValue(OPT_VCFCOUNT));
 			} catch (Exception e) {
 				// do nothing, iVCFCount remains 0
 			}
 			if (iVCFCount < 0) {
 				iVCFCount = 0;
 			}
 			
 			convertABook(sInFile, sOutFile, iVCFCount);
 			
 		} catch (Exception e) {
 			printError("");
 			printError(getUsage());
 			printError("");
 			printError(e);
 			System.exit(1);
 		}
 		
 		printMessage("end.");
 	}
 	
 	/**
 	 * Converts abook to vcf file(s).
 	 * 
 	 * @param theInFile input file
 	 * @param theOutFile output file
 	 * @param theVCFCount max vcard count
 	 * 
 	 * @throws ABookException if an error occurred during execution
 	 * 
 	 * @version 0.1
 	 * @since 0.1
 	 */
 	public static void convertABook(String theInFile, String theOutFile, int theVCFCount) throws ABookException {
 		
 		try {
 			List<mozilla.thunderbird.Address> theAddresses = loadAdresses(theInFile);
 			printMessage(MessageFormat.format("address count: {0, number}", theAddresses.size()));
 
 			String sOutFilePattern = getOutFilePattern(theAddresses.size(), theOutFile, theVCFCount);
 
 			writeVCards(theAddresses, sOutFilePattern, theVCFCount);
 			
 		} catch (Exception e) {
 			throw new ABookException(e.getLocalizedMessage());
 		}
 		
 	}
 	
 	/**
 	 * Write vcf file(s).
 	 * 
 	 * @param theAddresses list of addresses
 	 * @param theOutFilePattern output file pattern
 	 * @param theVCFCount max vcard count
 	 * 
 	 * @throws ABookException if an error occurred during execution
 	 * 
 	 * @version 0.1
 	 * @since 0.1
 	 */
 	public static void writeVCards(List<mozilla.thunderbird.Address> theAddresses, String theOutFilePattern, int theVCFCount) throws ABookException {
 		
 		try {
 			int iFileCount = 1;
 			int iAddressesInFile = 0;
 			StringBuffer sbFileContent = null;
 			
 			for (mozilla.thunderbird.Address theAddress : theAddresses) {
 				
 				if (iAddressesInFile == 0) {
 					sbFileContent = new StringBuffer();
 				}
 				
 				sbFileContent.append("BEGIN:VCARD\n");
 				sbFileContent.append("VERSION:3.0\n");
				sbFileContent.append(theAddress.get("DisplayName"));
 				sbFileContent.append("\n");
 				
				printMessage(theAddress.get("DisplayName"));
 				
 				sbFileContent.append("END:VCARD\n\n");
 				iAddressesInFile++;
 				
 				if (iAddressesInFile == theVCFCount) {
 					writeFile(String.format(theOutFilePattern, iFileCount), sbFileContent.toString());
 					iAddressesInFile = 0;
 					iFileCount++;
 				}
 			}
 			
 			if (iAddressesInFile > 0) {
 				writeFile(String.format(theOutFilePattern, iFileCount), sbFileContent.toString());
 			}
 			
 		} catch (Exception e) {
 			throw new ABookException(e.getLocalizedMessage());
 		}
 		
 	}
 	
 	/**
 	 * Returns the output file pattern.
 	 * 
 	 * @param theAddressCount number of addresses
 	 * @param theOutFile output file
 	 * @param theVCFCount max vcard count
 	 * 
 	 * @throws ABookException if an error occurred during execution
 	 * 
 	 * @version 0.1
 	 * @since 0.1
 	 */
 	private static String getOutFilePattern(int theAddressCount, String theOutFile, int theVCFCount) {
 		
 		String sOutFilePattern = theOutFile;
 		if (!sOutFilePattern.endsWith(VCF_FILE_EXTENSION)) {
 			sOutFilePattern += VCF_FILE_EXTENSION;
 		}
 		
 		if (theVCFCount != 0) {
 			int iMaxFileCount = theAddressCount / theVCFCount;
 			if ((theAddressCount % theVCFCount) > 0) {
 				iMaxFileCount++;
 			}
 			sOutFilePattern = sOutFilePattern.replace(VCF_FILE_EXTENSION, String.format("%%0%dd%s", String.valueOf(iMaxFileCount).length(), VCF_FILE_EXTENSION));
 		}
 
 		return sOutFilePattern;
 	}
 	
 	/**
 	 * Loads the address book.
 	 * 
 	 * @param theInFile input file
 	 * 
 	 * @throws ABookException if an error occurred during execution
 	 * 
 	 * @version 0.1
 	 * @since 0.1
 	 */
 	private static List<mozilla.thunderbird.Address> loadAdresses(String theInFile) throws ABookException {
 		
 		mozilla.thunderbird.AddressBook theAddressBook = new mozilla.thunderbird.AddressBook();
 		
 		File fleABook = new File(theInFile);
 		
 		if (!fleABook.exists()) {
 			throw new ABookException(MessageFormat.format("File ''{0}'' does not exist.", theInFile));
 		}
 		if (!fleABook.canRead()) {
 			throw new ABookException(MessageFormat.format("File ''{0}'' is not readable.", theInFile));
 		}
 		if (!fleABook.isFile()) {
 			throw new ABookException(MessageFormat.format("File ''{0}'' is no file (maybe a directory?)", theInFile));
 		}
 		
 		try {
 			FileInputStream stmABook = null;
 			
 			try {
 				stmABook = new FileInputStream(fleABook);
 				
 				printMessage(MessageFormat.format("reading abook file: ''{0}''", fleABook.getAbsoluteFile()));
 				
 				theAddressBook.load(stmABook);
 				
 			} finally {
 				if (stmABook != null) {
 					printMessage(MessageFormat.format("closing abook file: ''{0}''", fleABook.getAbsoluteFile()));
 					stmABook.close();
 				}
 			}
 			
 		} catch (Exception e) {
 			throw new ABookException(e.getLocalizedMessage());
 		}
 		
 		return theAddressBook.getAddresses();
 		
 	}
 	
 }
 
 /* EOF */
