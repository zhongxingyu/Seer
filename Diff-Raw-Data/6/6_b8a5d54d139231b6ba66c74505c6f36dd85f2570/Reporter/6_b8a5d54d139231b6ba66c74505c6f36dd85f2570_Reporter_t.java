 package ca.charland.tanitascale;
 
 import java.util.Map;
 
 import ooo.connector.BootstrapSocketConnector;
 
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.comp.helper.BootstrapException;
 import com.sun.star.container.XIndexAccess;
 import com.sun.star.frame.XComponentLoader;
 import com.sun.star.lang.IndexOutOfBoundsException;
 import com.sun.star.lang.Locale;
 import com.sun.star.lang.WrappedTargetException;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.sheet.XSpreadsheet;
 import com.sun.star.sheet.XSpreadsheetDocument;
 import com.sun.star.sheet.XSpreadsheets;
 import com.sun.star.table.XCell;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.util.MalformedNumberFormatException;
 import com.sun.star.util.NumberFormat;
 import com.sun.star.util.XNumberFormats;
 import com.sun.star.util.XNumberFormatsSupplier;
 
 public class Reporter {
 
 	private String oooExeFolder;
 	private XComponentContext context;
 	private XMultiComponentFactory serviceManager;
 	private XSpreadsheetDocument document;
 	private XSpreadsheet sheet;
 	private int percentageFormat;
	private int percentageTwoDigitFormat;
 	private int doubleFormat;
 	private int dateFormat;
 	private int intFormat;
 
 	public static void main(String args[]) {
 		if (args[0].startsWith("Date")) {
 			parseSingleDay(args);
 		} else {
 			parseFiles(args);
 		}
 	}
 
 	private static void parseSingleDay(String[] args) {
 		Parser parser = new Parser();
 		Reporter content = new Reporter();
 		
 		Map<Column, String> values = parser.parseSingleDay(args);
 		content.printValues(values, 0);
 	}
 
 	private static void parseFiles(String[] args) {
 		Parser parser = new Parser();
 		Reporter content = new Reporter();
 		int y = 0;
 		for (String arg : args) {
 			Map<Column, String> values = parser.parseFileContents(LoadFile.load(arg));
 			content.printValues(values, y++);
 		}
 	}
 
 	public Reporter() {
 		
 		oooExeFolder = setLibreOfficeFolder();
 		
 		// get the remote office context. If necessary a new office
 		// process is started
 		try {
 			context = BootstrapSocketConnector.bootstrap(oooExeFolder);
 		} catch (BootstrapException e) {
 			throw new SpreadSheetException(e);
 		}
 
 		System.out.println("Connected to a running office ...");
 		serviceManager = context.getServiceManager();
 
 		// create a new spreadsheet document
 		XComponentLoader aLoader = null;
 		try {
 			aLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,
 					serviceManager.createInstanceWithContext("com.sun.star.frame.Desktop", context));
 		} catch (Exception e) {
 			throw new SpreadSheetException(e);
 		}
 
 		try {
 			document = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class,
 					aLoader.loadComponentFromURL("private:factory/scalc", "_blank", 0, new PropertyValue[0]));
 		} catch (Exception e) {
 			throw new SpreadSheetException(e);
 		}
 		initSpreadsheet();
 		setKeyFormats();
 	}
 
 	private static String setLibreOfficeFolder() {
 		String os = System.getProperty("os.name");
 		if(os.contains("Linux")) {
 			return "/usr/lib/libreoffice/program";
 		}
 		return "D:/Program Files (x86)/LibreOffice 3.6/program";
 	}
 	
 	private void setKeyFormats() {
 		// Query the number formats supplier of the spreadsheet document
 		XNumberFormatsSupplier numberFormatsSupplier = (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, document);
 
 		// Get the number formats from the supplier
 		XNumberFormats numberFormats = numberFormatsSupplier.getNumberFormats();
 
 		// Get the number format index key of the default currency format,
 		// note the empty locale for default locale
 		Locale aLocale = new Locale();
 		try {
 			dateFormat = numberFormats.addNew("MMM D", aLocale);
 			percentageFormat = numberFormats.addNew("##.0%", aLocale);
			percentageTwoDigitFormat = numberFormats.addNew("##.00%", aLocale);
 			doubleFormat = numberFormats.addNew("##.0", aLocale);
 			intFormat = numberFormats.addNew("##.#", aLocale);
 		} catch (MalformedNumberFormatException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void initSpreadsheet() {
 		XSpreadsheets aSheets = document.getSheets();
 		XIndexAccess aSheetsIA = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class, aSheets);
 		try {
 			sheet = (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, aSheetsIA.getByIndex(0));
 		} catch (IndexOutOfBoundsException e) {
 			e.printStackTrace();
 		} catch (WrappedTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void printValues(Map<Column, String> values, int y) {
 
 		int x = 0;
 		for (Column col : values.keySet()) {
 			String value = values.get(col);
 
 			switch (col) {
 			case DATE:
 				setDate((String) value, x++, y);
 				setMonth(x++, y);
 				break;
 			case WEIGHT:
 				setDouble(value, x++, y);
 				break;
 			case DAILY_CALORIC_INTAKE:
 			case METABOLIC_AGE:
 				setInt(value, x++, y);
 				break;
 			case BODY_WATER_PERCENTAGE:
 				setPercentage(value, x++, y);
 				break;
 			case VISCERAL_FAT:
 				setInt(value, x++, y);
 				break;
 			case BONE_MASS:
 				setDouble(value, x++, y);
 				break;
 			case BODY_FAT_TOTAL:
 			case BODY_FAT_LEFT_ARM:
 			case BODY_FAT_RIGHT_ARM:
 			case BODY_FAT_RIGHT_LEG:
 			case BODY_FAT_LEFT_LEG:
 			case BODY_FAT_TRUNK:
 				setPercentage(value, x++, y);
 				break;
 			case MUSCLE_MASS_TOTAL:
 			case MUSCLE_MASS_LEFT_ARM:
 			case MUSCLE_MASS_RIGHT_ARM:
 			case MUSCLE_MASS_RIGHT_LEG:
 			case MUSCLE_MASS_LEFT_LEG:
 			case MUSCLE_MASS_TRUNK:
 				setDouble(value, x++, y);
 				break;
 			case PHYSIC_RATING:
 				setInt(value, x++, y);
 				setMuscleMassPercentageOfTotalWeightFormula(x++, y);
 				break;
 			default:
 				throw new ColumnNotFoundException(col.toString());
 			}
 		}
 	}
 
 	private void setDate(String date, int x, int y) {
 		setFormula(date, x, y, dateFormat);
 	}
 
 	private void setMonth(int x, int y) {
 		setFormula(getMonthFormula(y + 1), x, y, NumberFormat.NUMBER);
 	}
 
 	private String getMonthFormula(int row) {
 		return "=MONTH(A" + row + ")";
 	}
 
 	private void setFormula(String formula, int x, int y, int format) {
 		XCell xCell = getCell(x, y);
 		xCell.setFormula(formula);
 		setFormat(x, y, format);
 	}
 
 	private void setDouble(String value, int x, int y) {
 		setValue(Double.parseDouble(value), x, y, doubleFormat);
 	}
 
 	private void setPercentage(String value, int x, int y) {
 		setValue(Double.parseDouble(value), x, y, percentageFormat);
 	}
 
 	private void setInt(String value, int x, int y) {
 		setValue(Integer.parseInt(value), x, y, intFormat);
 
 	}
 
 	private void setMuscleMassPercentageOfTotalWeightFormula(int x, int y) {
		setFormula(getMuscleMassPercentageOfTotalWeightFormula(y + 1), x, y, percentageTwoDigitFormat);
 	}
 
 	private String getMuscleMassPercentageOfTotalWeightFormula(int row) {
 		String muscleMass = "O" + row;
 		String weight = "C" + row;
 		return "=" + muscleMass + "/" + weight;
 	}
 
 	private void setValue(double value, int x, int y, int format) {
 		XCell xCell = getCell(x, y);
 		xCell.setValue(value);
 		setFormat(x, y, format);
 	}
 
 	private void setFormat(int x, int y, int format) {
 		XCell cell = getCell(x, y);
 		setNumberFormat(cell, format);
 	}
 
 	private XCell getCell(int x, int y) {
 		XCell cell = null;
 		try {
 			cell = sheet.getCellByPosition(x, y);
 		} catch (IndexOutOfBoundsException e) {
 			throw new SpreadSheetException(e);
 		}
 		return cell;
 	}
 
 	private void setNumberFormat(XCell cell, int format) {
 
 		// Query the property set of the cell range
 		XPropertySet xCellProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
 
 		try {
 			xCellProp.setPropertyValue("NumberFormat", format);
 		} catch (Exception e) {
 			throw new SpreadSheetException(e);
 		}
 	}
 }
