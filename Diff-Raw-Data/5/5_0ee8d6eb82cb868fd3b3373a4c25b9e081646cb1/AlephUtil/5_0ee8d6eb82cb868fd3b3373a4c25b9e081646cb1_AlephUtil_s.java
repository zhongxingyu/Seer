 package org.extensiblecatalog.ncip.v2.aleph.util;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephLocalization;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.SAXException;
 
 public class AlephUtil {
 
 	private static Map<String, List<String>> itemRestrictionClasses;
 	private static boolean itemRestrictionClassesInitialized = false;
 
 	private static void initializeItemRestrictionClasses() {
 		itemRestrictionClasses = new HashMap<String, List<String>>();
 
 		// Define & add list of item statuses defining restriction 'In Library Use Only'
 		List<String> inLibraryUseOnly = new ArrayList<String>();
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_TO_THE_MUSIC_CORNER_ONLY_4F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_IN_HOUSE_LOAN);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_SPN_2F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_ST_4F);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_REFERENCE_SHELF);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_STUDY_ROOM);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_IN_HOUSE_ILL);
 		inLibraryUseOnly.add(AlephConstants.ITEM_STATUS_RETRO);
 		itemRestrictionClasses.put(AlephConstants.ITEM_RESTRICTION_IN_LIBRARY_USE_ONLY, inLibraryUseOnly);
 
 		// Define & add list of item statuses defining restrictions of type 'Loan Period'
 		List<String> loanPeriods = new ArrayList<String>();
 		loanPeriods.add(AlephConstants.ITEM_STATUS_OPEN_STOCK_MONTH);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_LONG_TERM_LOAN);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_MONTH);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_WEEK);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_14_DAYS);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_7_DAYS);
 		loanPeriods.add(AlephConstants.ITEM_STATUS_2_HOURS);
 		itemRestrictionClasses.put(AlephConstants.ITEM_RESTRICTION_LOAN_PERIOD, loanPeriods);
 
 		// Note that other item statuses returned by Aleph RESTful APIs ILS are non-restrictive
 		itemRestrictionClassesInitialized = true;
 	}
 
 	public static BibliographicDescription getBibliographicDescription(AlephItem alephItem, AgencyId agencyId) {
 
 		BibliographicDescription bibliographicDescription = new BibliographicDescription();
 		BibliographicItemId bibliographicItemId;
 		BibliographicRecordId bibliographicRecordId;
 
 		List<BibliographicItemId> bibIds = new ArrayList<BibliographicItemId>();
 		List<BibliographicRecordId> bibRecIds = new ArrayList<BibliographicRecordId>();
 		if (alephItem.getAuthor() != null) {
 			bibliographicDescription.setAuthor(alephItem.getAuthor());
 		}
 		if (alephItem.getIsbn() != null) {
 			bibliographicItemId = new BibliographicItemId();
 			bibliographicItemId.setBibliographicItemIdentifier(alephItem.getIsbn());
 			bibliographicItemId.setBibliographicItemIdentifierCode((Version1BibliographicItemIdentifierCode.ISBN));
 			bibIds.add(bibliographicItemId);
 		}
 		if (alephItem.getBarcode() != null) {
 			bibliographicItemId = new BibliographicItemId();
 			bibliographicItemId.setBibliographicItemIdentifier(alephItem.getBarcode());
 			bibliographicItemId.setBibliographicItemIdentifierCode(Version1BibliographicItemIdentifierCode.LEGAL_DEPOSIT_NUMBER);
 			bibIds.add(bibliographicItemId);
 		}
 
 		if (alephItem.getMediumType() != null) {
 			MediumType mediumType = AlephUtil.detectMediumType(alephItem.getMediumType());
 			bibliographicDescription.setMediumType(mediumType);
 		}
 		if (alephItem.getPublisher() != null) {
 			bibliographicDescription.setPublisher(alephItem.getPublisher());
 		}
 		if (alephItem.getSeries() != null) {
 			bibliographicDescription.setSeriesTitleNumber(alephItem.getSeries());
 		}
 		if (alephItem.getTitle() != null) {
 			bibliographicDescription.setTitle(alephItem.getTitle());
 		}
 
 		if (alephItem.getBibId() != null || alephItem.getDocNumber() != null) {
 			if (alephItem.getDocNumber() != null) {
 				bibliographicRecordId = new BibliographicRecordId();
 				bibliographicRecordId.setAgencyId(agencyId);
 				bibliographicRecordId.setBibliographicRecordIdentifier(alephItem.getDocNumber());
 				bibliographicRecordId.setBibliographicRecordIdentifierCode(Version1BibliographicRecordIdentifierCode.ACCESSION_NUMBER);
 				bibRecIds.add(bibliographicRecordId);
 			} else {
 				bibliographicRecordId = new BibliographicRecordId();
 				bibliographicRecordId.setAgencyId(agencyId);
 				bibliographicRecordId.setBibliographicRecordIdentifier(alephItem.getBibId());
 				bibliographicRecordId.setBibliographicRecordIdentifierCode(Version1BibliographicRecordIdentifierCode.ACCESSION_NUMBER);
 				bibRecIds.add(bibliographicRecordId);
 			}
 		}
 
 		bibliographicDescription.setBibliographicItemIds(bibIds);
 		bibliographicDescription.setBibliographicRecordIds(bibRecIds);
 
 		// FIXME: NCIP cuts multiple bibliographic items/records although they are set properly - forwarded is only first bibItemId
 		// all other item ids & record ids are cut off
 		return bibliographicDescription;
 	}
 
 	public static Location getLocation(AlephItem alephItem) {
 		Location location = new Location();
 		alephItem.getLocation();
 		LocationNameInstance locationNameInstance = new LocationNameInstance();
 
 		locationNameInstance.setLocationNameValue(alephItem.getLocation());
 		// TODO: more to come from requirement for level
 		locationNameInstance.setLocationNameLevel(new BigDecimal("1"));// temporarily set to 1.
 
 		List<LocationNameInstance> locationNameInstances = new ArrayList<LocationNameInstance>();
 		locationNameInstances.add(locationNameInstance);
 
 		LocationName locationName = new LocationName();
 		locationName.setLocationNameInstances(locationNameInstances);
 
 		location = new Location();
 		location.setLocationName(locationName);
 		location.setLocationType(new LocationType(AlephConstants.DEFAULT_SCHEME, AlephConstants.LOCATION_TYPE_PERMANENT));
 		return location;
 	}
 
 	public static ItemOptionalFields getItemOptionalFields(AlephItem alephItem) {
 		ItemOptionalFields iof = new ItemOptionalFields();
 		if (alephItem.getCirculationStatus() != null) {
 			iof.setCirculationStatus(alephItem.getCirculationStatus());
 		}
 
 		if (alephItem.getElectronicResource() != null) {
 			ElectronicResource resource = new ElectronicResource();
 			resource.setReferenceToResource(alephItem.getElectronicResource());
 			iof.setElectronicResource(resource);
 		}
 
 		if (alephItem.getHoldQueueLength() >= 0) {
 			iof.setHoldQueueLength(new BigDecimal(alephItem.getHoldQueueLength()));
 		} else if (alephItem.getHoldQueue() != null) {
 			iof.setHoldQueue(alephItem.getHoldQueue());
 		}
 
 		ItemDescription description = null;
 
 		if (alephItem.getCallNumber() != null) {
 			description = new ItemDescription();
 			description.setCallNumber(alephItem.getCallNumber());
 			iof.setItemDescription(description);
 		}
 
 		if (alephItem.getCopyNumber() != null) {
 			description = new ItemDescription();
 			description.setCopyNumber(alephItem.getCopyNumber());
 			iof.setItemDescription(description);
 		}
 
 		if (alephItem.getItemRestrictions().size() > 0) {
 
 			List<ItemUseRestrictionType> itemUseRestrictionTypes = new ArrayList<ItemUseRestrictionType>();
 
 			for (String itemRestriction : alephItem.getItemRestrictions()) {
 
 				Version1ItemUseRestrictionType itemUseRestrictionTypeScheme = parseItemUseRestrictionType(itemRestriction);
 
 				ItemUseRestrictionType itemUseRestrictionType = itemUseRestrictionTypeScheme;
 
 				if (itemUseRestrictionType != null)
 					itemUseRestrictionTypes.add(itemUseRestrictionType);
 			}
 
 			if (itemUseRestrictionTypes.size() > 0)
 				iof.setItemUseRestrictionTypes(itemUseRestrictionTypes);
 		}
 
 		if (alephItem.getLocation() != null || alephItem.getCollection() != null) {
 			List<Location> locations = new ArrayList<Location>();
 			Location location = new Location();
 
 			LocationName locationName = new LocationName();
 
 			List<LocationNameInstance> locationNameInstances = new ArrayList<LocationNameInstance>();
 			LocationNameInstance locationNameInstance;
 
 			if (alephItem.getLocation() != null) {
 				locationNameInstance = new LocationNameInstance();
 
 				locationNameInstance.setLocationNameValue(alephItem.getLocation());
 				// TODO: more to come from requirement for level
 				locationNameInstance.setLocationNameLevel(new BigDecimal("1"));// temporarily set to 1.
 
 				locationNameInstances.add(locationNameInstance);
 			}
 
 			if (alephItem.getCollection() != null) {
 				locationNameInstance = new LocationNameInstance();
 
 				locationNameInstance.setLocationNameValue(alephItem.getCollection());
 				// TODO: more to come from requirement for level
 				locationNameInstance.setLocationNameLevel(new BigDecimal("2"));// temporarily set to 2.
 
 				locationNameInstances.add(locationNameInstance);
 			}
 
 			locationName.setLocationNameInstances(locationNameInstances);
 
 			location.setLocationName(locationName);
 			location.setLocationType(new LocationType(AlephConstants.DEFAULT_SCHEME, AlephConstants.LOCATION_TYPE_PERMANENT));
 
 			locations.add(location);
 			iof.setLocations(locations);
 		}
 
 		if (alephItem.getDescription() != null) {
 			if (description == null)
 				description = new ItemDescription();
 			description.setNumberOfPieces(alephItem.getNumberOfPieces());
 			description.setItemDescriptionLevel(new ItemDescriptionLevel(AlephConstants.DEFAULT_SCHEME, alephItem.getDescription()));
 			iof.setItemDescription(description);
 		}
 		return iof;
 	}
 
 	public static Version1ItemUseRestrictionType parseItemUseRestrictionType(String itemRestriction) {
 		Version1ItemUseRestrictionType itemUseRestrictionType = null;
 
 		if (!itemRestrictionClassesInitialized)
 			initializeItemRestrictionClasses();
 
 		boolean isLibraryOnlyRestrictionType = itemRestrictionClasses.get(AlephConstants.ITEM_RESTRICTION_IN_LIBRARY_USE_ONLY).contains(itemRestriction);
 		boolean isLoanPeriodRestrictionType = itemRestrictionClasses.get(AlephConstants.ITEM_RESTRICTION_LOAN_PERIOD).contains(itemRestriction);
 
 		if (isLibraryOnlyRestrictionType) {
 
 			if (itemRestriction.equals(AlephConstants.ITEM_STATUS_TO_THE_MUSIC_CORNER_ONLY_4F))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.SUPERVISION_REQUIRED;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_REFERENCE_ONLY_SPN_2F))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.USE_ONLY_IN_CONTROLLED_ACCESS;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_RETRO))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.NOT_FOR_LOAN;
 			else
 				itemUseRestrictionType = Version1ItemUseRestrictionType.IN_LIBRARY_USE_ONLY;
 
 		} else if (isLoanPeriodRestrictionType) {
 
 			if (itemRestriction.equals(AlephConstants.ITEM_STATUS_LONG_TERM_LOAN))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_LONG_LOAN_PERIOD;
 
 			else if (itemRestriction.equals(AlephConstants.ITEM_STATUS_MONTH) || itemRestriction.equals(AlephConstants.ITEM_STATUS_OPEN_STOCK_MONTH))
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_NORMAL_LOAN_PERIOD;
 
 			else
 				itemUseRestrictionType = Version1ItemUseRestrictionType.LIMITED_CIRCULATION_SHORT_LOAN_PERIOD;
 		}
 
 		return itemUseRestrictionType;
 	}
 
 	public static ItemTransaction getItemTransaction(AlephItem alephItem) {
 		ItemTransaction itemTransaction = new ItemTransaction();
 		if (alephItem.getBorrowingUsers() != null && alephItem.getBorrowingUsers().size() > 0) {
 			CurrentBorrower borrower = new CurrentBorrower();
 			UserId userId = new UserId();
 			AlephUser alephUser = alephItem.getBorrowingUsers().get(0);
 			userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 			userId.setUserIdentifierType(new UserIdentifierType("Username"));
 			userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 			borrower.setUserId(userId);
 			itemTransaction.setCurrentBorrower(borrower);
 		}
 
 		if (alephItem.getRequestingUsers() != null && alephItem.getRequestingUsers().size() > 0) {
 			Iterator<AlephUser> i = alephItem.getRequestingUsers().iterator();
 			while (i.hasNext()) {
 				AlephUser alephUser = i.next();
 				CurrentRequester requester = new CurrentRequester();
 				UserId userId = new UserId();
 				userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 				userId.setUserIdentifierType(new UserIdentifierType("Username"));
 				userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 				requester.setUserId(userId);
 				List<CurrentRequester> requesters = itemTransaction.getCurrentRequesters();
 				if (requesters == null)
 					requesters = new ArrayList<CurrentRequester>();
 				requesters.add(requester);
 				itemTransaction.setCurrentRequesters(requesters);
 			}
 
 		}
 		return itemTransaction;
 	}
 
 	public static boolean inDaylightTime() {
 		return TimeZone.getDefault().inDaylightTime(new java.util.Date());
 	}
 
 	public static String convertToAlephDate(GregorianCalendar gregorianCalendar) {
 		// We need: 20141231
 
 		String month = Integer.toString(gregorianCalendar.get(Calendar.MONTH) + 1);
 		if (month.length() < 2)
 			month = "0" + month;
 		String day = Integer.toString(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
 		if (day.length() < 2)
 			day = "0" + day;
 		return Integer.toString(gregorianCalendar.get(Calendar.YEAR)) + month + day;
 	}
 
 	public static String parseRecordIdFromAlephItemId(String itemId) {
 		return itemId.split(AlephConstants.UNIQUE_ITEM_ID_SEPARATOR)[0];
 	}
 
 	public static String parseItemIdFromAlephItemId(String itemId) {
 		return itemId.split(AlephConstants.UNIQUE_ITEM_ID_SEPARATOR)[1];
 	}
 
 	public static MediumType detectMediumType(String mediumTypeParsed) {
 		MediumType mediumType = null;
 
 		if (mediumTypeParsed.matches(AlephLocalization.BOOK + "|" + AlephLocalization.GRAPHICS + "|" + AlephLocalization.MAP))
 			mediumType = Version1MediumType.BOOK;
 		else if (mediumTypeParsed.matches(AlephLocalization.MAGAZINE1 + "|" + AlephLocalization.MAGAZINE2))
 			mediumType = Version1MediumType.MAGAZINE;
 		else if (mediumTypeParsed.equalsIgnoreCase(AlephLocalization.COMPACT_DISC))
 			mediumType = Version1MediumType.CD_ROM;
 		else if (mediumTypeParsed.equalsIgnoreCase(AlephLocalization.AUDIO_TAPE))
 			mediumType = Version1MediumType.AUDIO_TAPE;
 		else {
 			try {
 				Version1MediumType.loadAll();
 				mediumType = MediumType.find(Version1MediumType.VERSION_1_MEDIUM_TYPE, mediumTypeParsed);
 			} catch (ServiceException e) {
 				e.printStackTrace();
 			}
 		}
 		return mediumType;
 	}
 
 	public static PhysicalAddress parsePhysicalAddress(String address) {
 
 		PhysicalAddress physicalAddress = new PhysicalAddress();
 		StructuredAddress structuredAddress = new StructuredAddress();
 
 		String[] addressParts = address.split(AlephConstants.UNSTRUCTURED_ADDRESS_SEPARATOR);
 
 		int addressPartsLength = addressParts.length;
 
 		if (addressPartsLength > 2) {
 			// In this case address = "Vysokoškolské koleje VUT - Kolejní 2, A05/237, 612 00 Brno";
 			structuredAddress.setStreet(addressParts[0]);
 			structuredAddress.setPostOfficeBox(addressParts[1]);
 
 			String postalCode = addressParts[2].substring(0, AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setPostalCode(postalCode);
 
 			String city = addressParts[2].substring(AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setLocality(city.trim());
 		} else if (addressPartsLength > 1) {
 			// F.e. Trvalá ulice 123, 12345 Trvalé
 			structuredAddress.setStreet(addressParts[0]);
 
 			String postalCode = addressParts[1].substring(0, AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setPostalCode(postalCode);
 
 			String city = addressParts[1].substring(AlephConstants.POSTAL_CODE_LENGTH);
 			structuredAddress.setLocality(city.trim());
 		} else
 			structuredAddress.setStreet(addressParts[0]);
 
 		physicalAddress.setStructuredAddress(structuredAddress);
 		physicalAddress.setPhysicalAddressType(Version1PhysicalAddressType.POSTAL_ADDRESS);
 
 		return physicalAddress;
 	}
 
 	/**
 	 * Pattern for building unique item Id from document number & item sequence item is:<br/>
 	 * bibLibrary + docNumber + "-" + admLibrary + docNumber + itemSequenceNumber<br/>
 	 * <br/>
 	 * 
 	 * Output should look like this example:<br />
 	 * MZK01000000421-MZK50000000421000010
 	 * 
 	 * @param bibLibrary
 	 * @param admLibrary
 	 * @param docNumber
 	 * @param itemSequenceNumber
 	 * @return
 	 */
 	public static String buildAlephItemId(String bibLibrary, String admLibrary, String docNumber, String itemSequenceNumber) {
 		String itemId = bibLibrary + docNumber + AlephConstants.UNIQUE_ITEM_ID_SEPARATOR + admLibrary + docNumber + itemSequenceNumber;
 		return itemId;
 	}
 
 	public static BlockOrTrap parseBlockOrTrap(String parsedBlock) {
 		BlockOrTrap blockOrTrap = new BlockOrTrap();
 
 		blockOrTrap.setBlockOrTrapType(new BlockOrTrapType("http://www.niso.org/ncip/v1_0/imp1/schemes/blockortraptype/blockortraptype.scm", parsedBlock));
 
 		return blockOrTrap;
 	}
 
 	/**
 	 * Tries to convert date string parsed from aleph response to GregorianCalendar format.<br />
 	 * Throws SAXException if not successful.
 	 * 
 	 * @param alephDateParsed
 	 * @return
 	 * @throws SAXException
 	 */
 	public static GregorianCalendar parseGregorianCalendarFromAlephDate(String alephDateParsed) throws SAXException {
 		if (!alephDateParsed.equalsIgnoreCase("00000000")) {
 			GregorianCalendar loanDate = new GregorianCalendar(TimeZone.getDefault());
 
 			try {
 				loanDate.setTime(AlephConstants.ALEPH_DATE_FORMATTER.parse(alephDateParsed));
 				loanDate.add(Calendar.HOUR_OF_DAY, 2);
 			} catch (ParseException e) {
 				throw new SAXException(e);
 			}
 
 			return loanDate;
 		} else
 			return null;
 	}
 
 	public static CirculationStatus parseCirculationStatus(String circulationStatusVal) {
 		CirculationStatus circulationStatus;
 		if (circulationStatusVal.matches(AlephConstants.CIRC_STATUS_ON_SHELF + "|" + AlephConstants.CIRC_STATUS_REQUESTED + "|" + AlephConstants.CIRC_STATUS_PROCESSING)) {
 			if (circulationStatusVal.equalsIgnoreCase(AlephConstants.CIRC_STATUS_ON_SHELF)) {
				circulationStatus = Version1CirculationStatus.AVAILABLE_FOR_PICKUP;
 			} else if (circulationStatusVal.equalsIgnoreCase(AlephConstants.CIRC_STATUS_REQUESTED)) {
 				circulationStatus = Version1CirculationStatus.ON_LOAN;
 			} else
 				// Status = processing
 				circulationStatus = Version1CirculationStatus.IN_PROCESS;
 
 		} else
			circulationStatus = Version1CirculationStatus.AVAILABLE_ON_SHELF;
 		return circulationStatus;
 	}
 }
