 package nl.digitalica.skydivekompasroos;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 
 public class CanopyType extends CanopyBase {
 
 	final public static String EVERYOTHERCANOPYIDSTRING = "5E4D5563-2196-4EC2-8558-0491082D0626";
 
 	final public static String DEFAULTSIZE = "170";
 
 	final private static int UNKNOWNCATEGORY = 0;
 
 	// properties
 	public UUID id;
 	private int category;
 	public UUID manufacturerId;
 	public String manufacturerName;
 	public String manufacturerShortName;
 	public String name;
 	public String url;
 	public String cells;
 	public boolean commontype;
 	public String dropzoneId;
 	public String minSize;
 	public String maxSize;
 	public String firstYearOfProduction;
 	public String lastYearOfProduction;
 	private String remarks;
 	private String remarks_nl;
 	public boolean isSpecialCatchAllCanopy = false;
 
 	public CanopyType(UUID canopyId, int canopyCategory,
 			UUID canopyManufacturer, String canopyName, String canopyUrl,
 			String canopyCells, boolean canopyCommonType,
 			String canopyDropzoneId, String canopyMinSize,
 			String canopyMaxSize, String canopyFirstYearOfProduction,
 			String canopyLastYearOfProduction, String canopyRemarks,
 			String canopyRemarks_nl, boolean isSpecialCatchAllCanopy) {
 		this.id = canopyId;
 		this.category = canopyCategory;
 		this.manufacturerId = canopyManufacturer;
 		this.name = canopyName;
 		this.url = canopyUrl;
 		this.cells = canopyCells;
 		this.commontype = canopyCommonType;
 		this.dropzoneId = canopyDropzoneId;
 		this.minSize = canopyMinSize;
 		this.maxSize = canopyMaxSize;
 		this.firstYearOfProduction = canopyFirstYearOfProduction;
 		this.lastYearOfProduction = canopyLastYearOfProduction;
 		this.remarks = canopyRemarks;
 		this.remarks_nl = canopyRemarks_nl;
 		this.isSpecialCatchAllCanopy = isSpecialCatchAllCanopy;
 
 		// to be able to have the special catch all canopy in
 		// both languages, we have the strings hard coded here,
 		// and not in the XML. Using strings doesn't work as
 		// we have no context here.
 		if (isSpecialCatchAllCanopy) {
 			this.manufacturerId = UUID
 					.fromString(Manufacturer.EVERYOTHERMANUFACTURERIDSTRING);
 			if (Calculation.isLanguageDutch()) {
 				this.name = "Elk ander type";
 			} else {
 				this.name = "Every other type";
 			}
 		}
 	}
 
 	/***
 	 * Constructor to create a specific canopy (mostly convenient for testing)
 	 * 
 	 * @param canopyCategory
 	 * @param canopyName
 	 * @param size
 	 */
 	public CanopyType(int canopyCategory, String canopyName, String size) {
 		this(UUID.randomUUID(), canopyCategory, Manufacturer
 				.everyOtherManufactuerId(), canopyName, null, null, true, null,
 				size, size, null, null, null, null, false);
 	}
 
 	/***
 	 * Constructor to create a specific canopy (mostly convenient for testing)
 	 * 
 	 * @param canopyCategory
 	 * @param canopyName
 	 * @param size
 	 */
 	public CanopyType(int canopyCategory, String canopyName) {
 		this(UUID.randomUUID(), canopyCategory, Manufacturer
 				.everyOtherManufactuerId(), canopyName, null, null, true, null,
 				DEFAULTSIZE, DEFAULTSIZE, null, null, null, null, false);
 	}
 
 	/**
 	 * Return true if the category is unknown
 	 * 
 	 * @return
 	 */
 	public boolean isCategoryUnknown() {
 		return this.category == UNKNOWNCATEGORY;
 	}
 
 	/**
 	 * Return the category
 	 */
 	public int category() {
 		return this.category;
 	}
 
 	/**
 	 * Return the category to be used for the calculations. This means for
 	 * unknown category (0) return 6.
 	 * 
 	 * @return
 	 */
 	public int calculationCategory() {
 		int calculationCategory = 6;
 		if (this.category != UNKNOWNCATEGORY)
 			calculationCategory = this.category;
 		return calculationCategory;
 	}
 
 	/**
 	 * Return the category to display: a ? if unknown, the number otherwise
 	 * 
 	 * @return
 	 */
 	public String displayCategory() {
 		String displayCategory = "";
 		if (this.category == UNKNOWNCATEGORY)
 			displayCategory = "?";
 		else
 			displayCategory = Integer.toString(this.category);
 		return displayCategory;
 	}
 
 	/***
 	 * Determine if we would like to know more details about this canopy used to
 	 * decide if a text should be shown in Canopy Details screen
 	 * 
 	 * @return
 	 */
 	public boolean addtionalInformationNeeded() {
 		if (this.firstYearOfProduction == null
 				|| this.firstYearOfProduction.equals(""))
 			return true;
 
 		if (this.cells == null || this.cells.equals(""))
 			return true;
 
 		if (this.minSize == null || this.minSize.equals(""))
 			return true;
 
 		if (this.maxSize == null || this.maxSize.equals(""))
 			return true;
 
 		// seems we know all we want to...
 		return false;
 	}
 
 	/**
 	 * Return a specific canopy based on its id
 	 * 
 	 * @param canopyId
 	 * @param c
 	 * @return
 	 */
 	static public CanopyType getCanopy(UUID canopyId, Context c) {
 		CanopyType canopy = null;
 		List<CanopyType> canopyList = getCanopyTypesInList(canopyId, c);
 		if (canopyList.size() == 1)
 			canopy = canopyList.get(0);
 		return canopy;
 	}
 
 	/**
 	 * Return all canopyTypes as a hashmap based on their id.
 	 * 
 	 * @param c
 	 * @return
 	 */
 	static public HashMap<UUID, CanopyType> getCanopyTypeHash(Context c) {
 		List<CanopyType> canopyTypesList = getAllCanopyTypesInList(c);
 		HashMap<UUID, CanopyType> canopyTypes = new HashMap<UUID, CanopyType>();
 		for (CanopyType ct : canopyTypesList) {
 			canopyTypes.put(ct.id, ct);
 		}
 		return canopyTypes;
 	}
 
 	/***
 	 * Reads a specific canopies from the XML in a list. This is ok as the
 	 * number will always be limited anyway
 	 * 
 	 * @return
 	 */
 	static public List<CanopyType> getAllCanopyTypesInList(Context c) {
 		return getCanopyTypesInList(null, c);
 	}
 
 	/***
 	 * Reads the canopy with a specific id (or all, if id is null) from the XML
 	 * in a list.
 	 * 
 	 * @return
 	 */
 	static public List<CanopyType> getCanopyTypesInList(UUID id, Context c) {
 
 		HashMap<UUID, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(c);
 
 		XmlResourceParser canopiesParser = c.getResources().getXml(
 				R.xml.canopies);
 		int eventType = -1;
 
 		List<CanopyType> canopyList = new ArrayList<CanopyType>();
 		while (eventType != XmlResourceParser.END_DOCUMENT) {
 			if (eventType == XmlResourceParser.START_TAG) {
 				String strName = canopiesParser.getName();
 				if (strName.equals("canopy")) {
 					String canopyCategoryString = canopiesParser
 							.getAttributeValue(null, "category");
 					int canopyCategory;
 					try {
 						if (canopyCategoryString.equals(""))
 							canopyCategory = UNKNOWNCATEGORY;
 						else
 							canopyCategory = Integer
 									.parseInt(canopyCategoryString);
 					} catch (NumberFormatException e) {
 						throw new RuntimeException("Canopy category no Int", e);
 					}
 					String canopyIdString = canopiesParser.getAttributeValue(
 							null, "id");
 					UUID canopyId = UUID.fromString(canopyIdString);
 
 					String canopyManufacturerIdAndName = canopiesParser
 							.getAttributeValue(null, "manufacturerid");
 					String canopyManufacturerIdString = canopyManufacturerIdAndName
 							.split(" ")[0];
 					UUID canopyManufacturerId = UUID
 							.fromString(canopyManufacturerIdString);
 					String manufacturerName = manufacturers
 							.get(canopyManufacturerId).name;
 					String manufacturerShortName = manufacturers
 							.get(canopyManufacturerId).shortName;
 
 					String canopyName = canopiesParser.getAttributeValue(null,
 							"name");
 					String canopyUrl = canopiesParser.getAttributeValue(null,
 							"url");
 					String canopyCells = canopiesParser.getAttributeValue(null,
 							"cells");
 					String canopyCommonTypeString = canopiesParser
 							.getAttributeValue(null, "commontype");
 					String canopyDropzoneId = canopiesParser.getAttributeValue(
 							null, "dropzoneid");
 					boolean canopyCommonType = true;
 					if (Integer.parseInt(canopyCommonTypeString) == 0)
 						canopyCommonType = false;
 					String canopyMinSize = canopiesParser.getAttributeValue(
 							null, "minsize");
 					String canopyMaxSize = canopiesParser.getAttributeValue(
 							null, "maxsize");
 					String canopyFirstyearOfProduction = canopiesParser
 							.getAttributeValue(null, "firstyearofproduction");
 					String canopyLastyearOfProduction = canopiesParser
 							.getAttributeValue(null, "lastyearofproduction");
 					String canopyRemarks = canopiesParser.getAttributeValue(
 							null, "remarks");
 					String canopyRemarks_nl = canopiesParser.getAttributeValue(
 							null, "remarks_nl");
 					String isSpecialCatchAllCanopyString = canopiesParser
 							.getAttributeValue(null, "isspecialcatchallcanopy");
 					boolean isSpecialCatchAllCanopy = false;
 					if (isSpecialCatchAllCanopyString != ""
 							&& isSpecialCatchAllCanopyString != null
 							&& Integer.parseInt(isSpecialCatchAllCanopyString) != 0)
 						isSpecialCatchAllCanopy = true;
 					CanopyType canopy = new CanopyType(canopyId,
 							canopyCategory, canopyManufacturerId, canopyName,
 							canopyUrl, canopyCells, canopyCommonType,
 							canopyDropzoneId, canopyMinSize, canopyMaxSize,
 							canopyFirstyearOfProduction,
 							canopyLastyearOfProduction, canopyRemarks,
 							canopyRemarks_nl, isSpecialCatchAllCanopy);
 					// TODO: maybe the assignment below should move to the
 					// constructor...
 					canopy.manufacturerName = manufacturerName;
 					canopy.manufacturerShortName = manufacturerShortName;
 					if (id == null)
 						canopyList.add(canopy);
 					else if (canopy.id.equals(id)) {
 						canopyList.add(canopy);
 						return canopyList;
 					}
 				}
 			}
 			try {
 				eventType = canopiesParser.next();
 			} catch (XmlPullParserException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// add every other canopy
 		CanopyType eoc = everyOtherCanopyType();
 		canopyList.add(eoc);
 
 		// return the result
 		return canopyList;
 	}
 
 	/***
 	 * Returns the manufacturer and name of this canopy as a human-readable
 	 * unique key
 	 * 
 	 * @return
 	 */
 	public String uniqueName() {
 		return manufacturerName + '|' + name;
 	}
 
 	/***
 	 * Comparator, to be used for sorting
 	 * 
 	 * @author robbert
 	 */
 	public static class ComparatorByCategoryName implements
 			Comparator<CanopyType> {
 
 		public int compare(CanopyType c1, CanopyType c2) {
 			if (c1.isSpecialCatchAllCanopy)
 				return 1;
 			if (c2.isSpecialCatchAllCanopy)
 				return -1;
 			if (c1.category != c2.category)
				return c1.calculationCategory() < c2.calculationCategory() ? -1 : 1;
 			int result = c1.name.compareTo(c2.name);
 			if (result != 0)
 				return result;
 			return c1.manufacturerName.compareTo(c2.manufacturerName);
 		}
 
 	}
 
 	/***
 	 * Comparator, to be used for sorting
 	 * 
 	 * @author robbert
 	 */
 	public static class ComparatorByNameManufacturer implements
 			Comparator<CanopyType> {
 
 		public int compare(CanopyType c1, CanopyType c2) {
 			if (c1.isSpecialCatchAllCanopy)
 				return 1;
 			if (c2.isSpecialCatchAllCanopy)
 				return -1;
 			if (c1.name != c2.name)
 				return c1.name.compareTo(c2.name);
 			return c1.manufacturerName.compareTo(c2.manufacturerName);
 		}
 
 	}
 
 	/***
 	 * Comparator, to be used for sorting For each manufacturer we sort on cat
 	 * first, so the colored bars will show up nicely in list and help
 	 * separating suppliers
 	 * 
 	 * @author robbert
 	 */
 	public static class ComparatorByManufacturerCategoryName implements
 			Comparator<CanopyType> {
 
 		public int compare(CanopyType c1, CanopyType c2) {
 			if (c1.isSpecialCatchAllCanopy)
 				return 1;
 			if (c2.isSpecialCatchAllCanopy)
 				return -1;
 			if (c1.manufacturerName != c2.manufacturerName)
 				return c1.manufacturerName.compareTo(c2.manufacturerName);
 			if (c1.category != c2.category)
				return c1.calculationCategory() < c2.calculationCategory() ? -1 : 1;
 			return c1.name.compareTo(c2.name);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return this.displayCategory() + " " + this.name + " ("
 				+ this.manufacturerId + ")";
 	}
 
 	/***
 	 * Determines if a canopy is acceptable for a given jumper
 	 * 
 	 * @param jumperCategory
 	 * @param exitWeightInKg
 	 * @return
 	 */
 	public AcceptabilityEnum acceptablility(int jumperCategory,
 			int exitWeightInKg) {
 		if (jumperCategory < this.calculationCategory())
 			return AcceptabilityEnum.CATEGORYTOOHIGH; // not acceptable
 		if (this.maxSize != "" && this.maxSize != null)
 			if (Integer.parseInt(this.maxSize) < Calculation.minArea(
 					jumperCategory, exitWeightInKg))
 				return AcceptabilityEnum.NEEDEDSIZENOTAVAILABLE;
 		return AcceptabilityEnum.ACCEPTABLE;
 	}
 
 	/**
 	 * returns the url in dropzone.com for this canopy
 	 * 
 	 * @return
 	 */
 	public String dropZoneUrl() {
 		String url = "";
 		if (dropzoneId != null && !dropzoneId.equals(""))
 			url = "http://www.dropzone.com/gear/Detailed/" + dropzoneId
 					+ ".html";
 		return url;
 	}
 
 	/**
 	 * returns a text showing the number of cells and min/max size, to use in
 	 * the canopy list, as an alternative to the manufacturer when the sorting
 	 * is by manufacturer
 	 * 
 	 * @return
 	 */
 	public String alternativeDetailsText(Context c) {
 		String detailsText = "";
 		if (cells != null)
 			detailsText = String.format(c.getString(R.string.alternativeCells),
 					cells);
 
 		if (minSize != null && maxSize != null && minSize != ""
 				&& maxSize != "") {
 			if (!detailsText.equals(""))
 				detailsText += ", ";
 			detailsText += String.format(
 					c.getString(R.string.alternativeSizes), minSize, maxSize);
 		}
 		return detailsText;
 	}
 
 	/***
 	 * Return remarks in current locale
 	 * 
 	 * @return
 	 */
 	public String remarks() {
 		boolean dutch = Calculation.isLanguageDutch();
 		return remarks(dutch);
 	}
 
 	/***
 	 * Return remarks in Dutch or English
 	 * 
 	 * @param inDutch
 	 * @return
 	 */
 	public String remarks(boolean inDutch) {
 		return inDutch ? this.remarks_nl : this.remarks;
 	}
 
 	/**
 	 * Returns the ID of the special 'catch all' canopy
 	 * 
 	 * @return
 	 */
 	public static UUID everyOtherCanopyTypeId() {
 		return UUID.fromString(EVERYOTHERCANOPYIDSTRING);
 	}
 
 	/**
 	 * Returns the special 'catch all' canopy for 'every other canopy'
 	 * 
 	 * @return
 	 */
 	public static CanopyType everyOtherCanopyType() {
 		String name;
 		if (Calculation.isLanguageDutch()) {
 			name = "Elk ander type";
 		} else {
 			name = "Every other type";
 		}
 		return new CanopyType(everyOtherCanopyTypeId(), 6,
 				Manufacturer.everyOtherManufactuerId(), name, null, null,
 				false, null, null, null, null, null, null, null, true);
 	}
 
 	/**
 	 * Returns the name to use in the specific canopy list. This name must me
 	 * unique, and clearly identify a canopy type.
 	 * 
 	 * @return
 	 */
 	public String specificName() {
 		return this.name + " - " + this.manufacturerShortName;
 	}
 
 }
