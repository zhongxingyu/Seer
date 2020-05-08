 package org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephRenewItem;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
 import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
 import org.extensiblecatalog.ncip.v2.service.BibliographicItemId;
 import org.extensiblecatalog.ncip.v2.service.ItemId;
 import org.extensiblecatalog.ncip.v2.service.LoanedItem;
 import org.extensiblecatalog.ncip.v2.service.MediumType;
 import org.extensiblecatalog.ncip.v2.service.Version1BibliographicItemIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemIdentifierType;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * @author jirislav
  *
  */
 public class AlephLoanHandler extends DefaultHandler {
 	private BibliographicDescription bibliographicDescription;
 
 	private AlephRenewItem renewItem;
 	private List<LoanedItem> loanedItems;
 	private LoanedItem currentLoanedItem;
 
 	private String itemIdToLookFor;
 	private String loanLink;
 	private String docNumber;
 	private String status;
 	private String replyText;
 	private String itemSequenceNumber;
 
 	private boolean loanFound = false;
 	private boolean renewable;
 	private boolean docNoReached;
 	private boolean itemSeqReached;
 	private boolean returnedOkResponseCode;
 	private boolean replyCodeReached;
 	private boolean replyTextReached;
 	private boolean statusReached;
 	private boolean newDueDateReached;
 
 	private boolean dueDateReached = false;
 	private boolean loanDateReached = false;
 	private boolean loanNumberReached = false;
 
 	private boolean authorReached = false;
 	private boolean isbnReached = false;
 	private boolean titleReached = false;
 	private boolean publisherReached = false;
 	private boolean bibDocNoReached = false;
 	private boolean itemDocNoReached = false;
 	private boolean materialReached = false;
 	private boolean itemSequenceReached = false;
 	private boolean agencyReached = false;
 
 	private String agencyId;
 	private String bibDocNumber;
 	private String itemSequence;
 	private String itemDocNumber;
 	private String bibLibrary;
 	private String loanNumber;
 	private boolean itemIdNotFound;
 
 	/**
 	 * This constructor is used to initialize parser for outputs of successful renewals.
 	 * 
 	 * @param itemIdToLookFor
 	 * @param renewItem
 	 */
 	public AlephLoanHandler(String itemIdToLookFor, AlephRenewItem renewItem) {
 		this.itemIdToLookFor = itemIdToLookFor;
 		this.renewItem = renewItem;
 	}
 
 	/**
 	 * This initializes SAX parser for parsing loans.
 	 */
	public AlephLoanHandler(String bibLibrary) {
		this.bibLibrary = bibLibrary;
 		bibliographicDescription = new BibliographicDescription();
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 		if (qName.equalsIgnoreCase(AlephConstants.Z36_DOC_NUMBER_NODE)) {
 			docNoReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE)) {
 			authorReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE)) {
 			isbnReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE)) {
 			titleReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE)) {
 			publisherReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DOC_NUMBER_NODE)) {
 			itemDocNoReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_DOC_NUMBER_NODE)) {
 			bibDocNoReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.TRANSLATE_CHANGE_ACTIVE_LIBRARY_NODE)) {
 			agencyReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE)) {
 			materialReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.LOAN_ITEM_NODE)) {
 			currentLoanedItem = new LoanedItem();
 
 			if (loanedItems == null)
 				loanedItems = new ArrayList<LoanedItem>();
			
			String link = attributes.getValue(AlephConstants.HREF_NODE_ATTR);
			if (link != null && itemIdToLookFor != null && link.indexOf(itemIdToLookFor) > -1) {
				loanLink = link;
				loanFound = true;
			}
			String renewAttr = attributes.getValue(AlephConstants.RENEW_NODE_ATTR);
			if (renewAttr.equalsIgnoreCase(AlephConstants.YES)) {
				renewable = true;
			} else
				renewable = false;
 		} else if (qName.matches(AlephConstants.Z36_DUE_DATE_NODE + "|" + AlephConstants.Z36H_DUE_DATE_NODE)) {
 			dueDateReached = true;
 		} else if (qName.matches(AlephConstants.Z36_LOAN_DATE_NODE + "|" + AlephConstants.Z36H_LOAN_DATE_NODE)) {
 			loanDateReached = true;
 		} else if (qName.matches(AlephConstants.Z36_ITEM_SEQUENCE_NODE + "|" + AlephConstants.Z36H_ITEM_SEQUENCE_NODE)) {
 			itemSequenceReached = true;
 		} else if (qName.matches(AlephConstants.Z36_NUMBER_NODE + "|" + AlephConstants.Z36H_NUMBER_NODE)) {
 			loanNumberReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.REPLY_CODE_NODE)) {
 			replyCodeReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.REPLY_TEXT_NODE)) {
 			replyTextReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.STATUS_NODE)) {
 			statusReached = true;
 		} else if (qName.equalsIgnoreCase(AlephConstants.NEW_DUE_DATE_NODE)) {
 			newDueDateReached = true;
 		}
 
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 
 		if (qName.equalsIgnoreCase(AlephConstants.Z36_DOC_NUMBER_NODE) && docNoReached) {
 			docNoReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z36_ITEM_SEQUENCE_NODE) && itemSeqReached) {
 			itemSeqReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_AUTHOR_NODE) && authorReached) {
 			authorReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_ISBN_NODE) && isbnReached) {
 			isbnReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_TITLE_NODE) && titleReached) {
 			titleReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_PUBLISHER_NODE) && publisherReached) {
 			publisherReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z30_DOC_NUMBER_NODE) && itemDocNoReached) {
 			itemIdNotFound = true;
 			itemDocNoReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z13_DOC_NUMBER_NODE) && bibDocNoReached) {
 			itemIdNotFound = true;
 			bibDocNoReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.TRANSLATE_CHANGE_ACTIVE_LIBRARY_NODE) && agencyReached) {
 			itemIdNotFound = true;
 			agencyReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.Z30_MATERIAL_NODE) && materialReached) {
 			materialReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.LOAN_ITEM_NODE)) {
 			if (!itemIdNotFound) {
 				// Create unique bibliographic item id from bibLibrary, bibDocNo, admLibrary, itemDocNo & itemSequence
 				List<BibliographicItemId> bibliographicItemIds = new ArrayList<BibliographicItemId>();
 				BibliographicItemId bibliographicItemId = new BibliographicItemId();
 				String bibliographicItemIdentifier = bibLibrary + bibDocNumber.trim() + "-" + agencyId.trim() + itemDocNumber.trim() + itemSequence;
 				bibliographicItemId.setBibliographicItemIdentifier(bibliographicItemIdentifier);
 				bibliographicItemId.setBibliographicItemIdentifierCode(Version1BibliographicItemIdentifierCode.URI);
 				bibliographicItemIds.add(bibliographicItemId);
 				bibliographicDescription.setBibliographicItemIds(bibliographicItemIds);
 
 				// Create loan identifier from admLibrary & loanNumber
 				ItemId itemId = new ItemId();
 
 				if (renewable) {
 					// Loan number is needed to apply RenewItemService
 					itemId.setItemIdentifierValue(agencyId.trim() + loanNumber);
 				} else {
 					// This will be significant for loaned items marked as not renewable
 					itemId.setItemIdentifierValue("-1");
 				}
 				itemId.setItemIdentifierType(Version1ItemIdentifierType.ACCESSION_NUMBER);
 				currentLoanedItem.setItemId(itemId);
 			}
 			currentLoanedItem.setBibliographicDescription(bibliographicDescription);
 
 			if (currentLoanedItem.getDateDue() == null)
 				currentLoanedItem.setIndeterminateLoanPeriodFlag(true);
 
 			loanedItems.add(currentLoanedItem);
 			itemIdNotFound = false;
 			bibliographicDescription = new BibliographicDescription();
 		} else if (qName.matches(AlephConstants.Z36_DUE_DATE_NODE + "|" + AlephConstants.Z36H_DUE_DATE_NODE) && dueDateReached) {
 			dueDateReached = false;
 		} else if (qName.matches(AlephConstants.Z36_LOAN_DATE_NODE + "|" + AlephConstants.Z36H_LOAN_DATE_NODE) && loanDateReached) {
 			loanDateReached = false;
 		} else if (qName.matches(AlephConstants.Z36_ITEM_SEQUENCE_NODE + "|" + AlephConstants.Z36H_ITEM_SEQUENCE_NODE) && itemSequenceReached) {
 			itemIdNotFound = true;
 			itemSequenceReached = false;
 		} else if (qName.matches(AlephConstants.Z36_NUMBER_NODE + "|" + AlephConstants.Z36H_NUMBER_NODE) && loanNumberReached) {
 			loanNumberReached = false;
 
 		} else if (qName.equalsIgnoreCase(AlephConstants.REPLY_CODE_NODE) && replyCodeReached) {
 			replyCodeReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.REPLY_TEXT_NODE) && replyTextReached) {
 			replyTextReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.STATUS_NODE) && statusReached) {
 			statusReached = false;
 		} else if (qName.equalsIgnoreCase(AlephConstants.NEW_DUE_DATE_NODE) && newDueDateReached) {
 			newDueDateReached = false;
 		}
 
 	}
 
 	@Override
 	public void characters(char ch[], int start, int length) throws SAXException {
 
 		if (docNoReached) {
 			docNumber = new String(ch, start, length);
 			docNoReached = false;
 		} else if (itemSeqReached) {
 			itemSequenceNumber = new String(ch, start, length);
 			itemSeqReached = false;
 		} else if (materialReached) {
 			String mediumTypeParsed = new String(ch, start, length);
 			MediumType mediumType = AlephUtil.detectMediumType(mediumTypeParsed);
 			bibliographicDescription.setMediumType(mediumType);
 			materialReached = false;
 		} else if (authorReached) {
 			bibliographicDescription.setAuthor(new String(ch, start, length));
 			authorReached = false;
 		} else if (isbnReached) {
 			bibliographicDescription.setBibliographicLevel(null);
 			isbnReached = false;
 		} else if (titleReached) {
 			bibliographicDescription.setTitle(new String(ch, start, length));
 			titleReached = false;
 		} else if (publisherReached) {
 			bibliographicDescription.setPublisher(new String(ch, start, length));
 			publisherReached = false;
 		} else if (itemDocNoReached) {
 			itemDocNumber = new String(ch, start, length);
 			itemDocNoReached = false;
 		} else if (bibDocNoReached) {
 			String parsedBibId = new String(ch, start, length);
 			bibDocNumber = parsedBibId;
 			bibDocNoReached = false;
 		} else if (itemSequenceReached) {
 			itemSequence = new String(ch, start, length);
 			itemSequenceReached = false;
 		} else if (agencyReached) {
 			agencyId = new String(ch, start, length);
 			agencyReached = false;
 		} else if (dueDateReached) {
 			String dateDueParsed = new String(ch, start, length);
 			GregorianCalendar dateDue = AlephUtil.parseGregorianCalendarFromAlephDate(dateDueParsed);
 
 			currentLoanedItem.setDateDue(dateDue);
 
 			dueDateReached = false;
 		} else if (loanDateReached) {
 			String loanDateParsed = new String(ch, start, length);
 			GregorianCalendar loanDate = AlephUtil.parseGregorianCalendarFromAlephDate(loanDateParsed);
 
 			currentLoanedItem.setDateCheckedOut(loanDate);
 
 			loanDateReached = false;
 		} else if (loanNumberReached) {
 			loanNumber = new String(ch, start, length);
 			loanNumberReached = false;
 
 		} else if (replyCodeReached) {
 			String replyCodeParsed = new String(ch, start, length);
 			if (replyCodeParsed.equalsIgnoreCase(AlephConstants.SUCCESS_REPLY_CODE)) {
 				returnedOkResponseCode = true;
 			} else
 				returnedOkResponseCode = false;
 		} else if (replyTextReached) {
 			replyText = new String(ch, start, length);
 			replyTextReached = false;
 		} else if (statusReached) {
 			status = new String(ch, start, length);
 			statusReached = false;
 		} else if (newDueDateReached) {
 			String newDueDateParsed = new String(ch, start, length);
 			GregorianCalendar newDueDate = AlephUtil.parseGregorianCalendarFromAlephDate(newDueDateParsed);
 
 			renewItem.setDateDue(newDueDate);
 
 			newDueDateReached = false;
 		}
 	}
 
 	public String getDocNumber() {
 		return docNumber;
 	}
 
 	public String getItemSequenceNumber() {
 		return itemSequenceNumber;
 	}
 
 	public String getLoanLink() {
 		return loanLink;
 	}
 
 	public boolean loanWasFound() {
 		return loanFound;
 	}
 
 	public boolean isRenewable() {
 		return renewable;
 	}
 
 	public boolean actionSucceeded() {
 		return returnedOkResponseCode;
 	}
 
 	public String getReplyText() {
 		return replyText;
 	}
 
 	public String getStatusText() {
 		return status;
 	}
 
 	public List<LoanedItem> getListOfLoanedItems() {
 		return loanedItems;
 	}
 }
