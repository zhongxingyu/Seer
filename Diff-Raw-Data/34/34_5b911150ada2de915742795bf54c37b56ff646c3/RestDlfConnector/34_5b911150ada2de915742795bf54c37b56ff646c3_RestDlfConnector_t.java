 package org.extensiblecatalog.ncip.v2.aleph.util;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers.AlephItemHandler;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers.AlephLoanHandler;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers.AlephRequestHandler;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers.AlephRequestItemHandler;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.handlers.AlephUserHandler;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.*;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.user.*;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.AlephMediator;
 import org.extensiblecatalog.ncip.v2.aleph.util.*;
 import org.extensiblecatalog.ncip.v2.binding.ncipv2_02.jaxb.elements.MandatedAction;
 import org.extensiblecatalog.ncip.v2.common.*;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.HandlerBase;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 public class RestDlfConnector extends AlephMediator {
 
 	private static final long serialVersionUID = -4425639616999642735L;
 	protected AlephConfiguration alephConfig = null;
 	public boolean echoParticularProblemsToLUIS;
 	private boolean requiredAtLeastOneService;
 	private boolean iofDesiredWithinRequestItem;
 	private boolean uofDesiredWithinRequestItem;
 	private boolean iofDesiredWithinCancelRequestItem;
 	private boolean uofDesiredWithinCancelRequestItem;
 
 	private String defaultAgency;
 	private String agencyAddress;
 	private String agencyName;
 	private String NCIPVersion;
 	private String userRegistrationLink;
 	private String authDataFormatType;
 
 	private String serverName;
 	private String serverPort;
 	private String serverSuffix;
 	private String bibLibrary;
 	private String admLibrary;
 	private String itemPathElement;
 	private String userPathElement;
 	private String itemsElement;
 	private String patronElement;
 
 	private int bibIdLength;
 	private int sequenceNumberLength;
 	private SAXParser parser;
 	private String patronInfoElement;
 	private String addressElement;
 	private String circActionsElement;
 	private String loansElement;
 	private String requestsElement;
 	private String holdsElement;
 	private String patronStatusElement;
 	private String registrationElement;
 	private String recordPathElement;
 	private String blocksElement;
 
 	public RestDlfConnector() throws ServiceException {
 
 		try {
 			DefaultConnectorConfiguration config = (DefaultConnectorConfiguration) new ConnectorConfigurationFactory(new Properties()).getConfiguration();
 			alephConfig = new AlephConfiguration(config);
 			serverName = alephConfig.getProperty(AlephConstants.REST_DLF_SERVER);
 			serverPort = alephConfig.getProperty(AlephConstants.REST_DLF_PORT);
 			if (serverName == null || serverPort == null) {
 				throw new ServiceException(ServiceError.CONFIGURATION_ERROR, "Aleph server or port not set");
 			}
 			// Get configuration from toolkit.properties file
 
 			serverSuffix = alephConfig.getProperty(AlephConstants.REST_DLF_SUFFIX);
 			bibLibrary = alephConfig.getProperty(AlephConstants.BIBLIOGRAPHIC_LIBRARY);
 			admLibrary = alephConfig.getProperty(AlephConstants.ALEPH_ADMINISTRATIVE_LIBRARY);
 			defaultAgency = alephConfig.getProperty(AlephConstants.DEFAULT_AGENCY);
 
 			NCIPVersion = alephConfig.getProperty(AlephConstants.NCIP_TOOLKIT_VERSION);
 			agencyAddress = alephConfig.getProperty(AlephConstants.AGENCY_UNSTRUCTURED_ADDRESS);
 			agencyName = alephConfig.getProperty(AlephConstants.AGENCY_TRANSLATED_NAME);
 			userRegistrationLink = alephConfig.getProperty(AlephConstants.USER_REGISTRATION_LINK);
 			authDataFormatType = alephConfig.getProperty(AlephConstants.AUTH_DATA_FORMAT_TYPE);
 
 			echoParticularProblemsToLUIS = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_PARTICULAR_PROBLEMS_TO_LUIS));
 			requiredAtLeastOneService = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.REQUIRE_AT_LEAST_ONE_SERVICE));
 			iofDesiredWithinRequestItem = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_ITEM_INFO_IN_REQUEST_ITEM));
 			uofDesiredWithinRequestItem = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_USER_INFO_IN_REQUEST_ITEM));
 			iofDesiredWithinCancelRequestItem = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_ITEM_INFO_IN_CANCEL_REQUEST_ITEM));
 			uofDesiredWithinCancelRequestItem = Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_USER_INFO_IN_CANCEL_REQUEST_ITEM));
 
 			parser = SAXParserFactory.newInstance().newSAXParser();
 
 		} catch (ToolkitException e) {
 			throw new ExceptionInInitializerError(e);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 		bibIdLength = AlephConstants.BIB_ID_LENGTH;
 		sequenceNumberLength = AlephConstants.ITEM_ID_UNIQUE_PART_LENGTH;
 		itemPathElement = AlephConstants.ITEM_PATH_ELEMENT;
 		userPathElement = AlephConstants.USER_PATH_ELEMENT;
 		itemsElement = AlephConstants.PARAM_ITEMS;
 		patronInfoElement = AlephConstants.PARAM_PATRON_INFO;
 		addressElement = AlephConstants.PARAM_ADDRESS;
 		circActionsElement = AlephConstants.PARAM_CIRC_ACTIONS;
 		loansElement = AlephConstants.PARAM_LOANS;
 		holdsElement = AlephConstants.PARAM_HOLDS;
 		blocksElement = AlephConstants.PARAM_BLOCKS;
 		requestsElement = AlephConstants.PARAM_REQUESTS;
 		patronStatusElement = AlephConstants.PARAM_PATRON_STATUS;
 		registrationElement = AlephConstants.PARAM_REGISTRATION;
 		recordPathElement = AlephConstants.PARAM_RECORD;
 
 	}
 
 	public String getDefaultAgency() {
 		return defaultAgency;
 	}
 
 	public String getAgencyAddress() {
 		return agencyAddress;
 	}
 
 	public String getAgencyName() {
 		return agencyName;
 	}
 
 	public String getNCIPVersion() {
 		return NCIPVersion;
 	}
 
 	public String getRegistrationLink() {
 		return userRegistrationLink;
 	}
 
 	public String getAuthDataFormatType() {
 		return authDataFormatType;
 	}
 
 	public AgencyId toAgencyId(String agencyId) {
 		return new AgencyId(Version1AgencyElementType.VERSION_1_AGENCY_ELEMENT_TYPE, agencyId);
 	}
 
 	private boolean validateRecordId(String recordId) {
 		if (recordId.length() == bibIdLength + bibLibrary.length()) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean validateItemId(String sequenceNumber) {
 		if (sequenceNumber.length() == bibIdLength + bibLibrary.length() + sequenceNumberLength) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean validateLoanId(String alephLoanId) {
 		// Loan Id has the same length specifications as Record Id
 		return validateRecordId(alephLoanId);
 	}
 
 	/**
 	 * Looks up item with desired services in following order:
 	 * 
 	 * @param recordId
 	 * @param bibliographicDescription
 	 * @param circulationStatus
 	 * @param holdQueueLength
 	 * @param itemDesrciption
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public List<AlephItem> lookupItems(String recordId, boolean bibliographicDescription, boolean circulationStatus, boolean holdQueueLength, boolean itemDesrciption)
 			throws ParserConfigurationException, IOException, SAXException, AlephException {
 
 		if (!validateRecordId(recordId)) {
 			throw new AlephException("Record Id is accepted only in strict format with strict length. e.g. MZK01000000421");
 		}
 
 		URL url = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, itemPathElement, recordId, itemsElement).addRequest("view", "full").toURL();
 
 		InputSource streamSource = new InputSource(url.openStream());
 
 		AlephItemHandler itemHandler = new AlephItemHandler(bibLibrary, requiredAtLeastOneService, bibliographicDescription, circulationStatus, holdQueueLength, itemDesrciption);
 
 		parser.parse(streamSource, itemHandler);
 
 		return itemHandler.getListOfItems();
 
 	}
 
 	/**
 	 * @param alephItemId
 	 * @param bibliographicDescription
 	 * @param circulationStatus
 	 * @param holdQueueLength
 	 * @param itemDesrciption
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws AlephException
 	 */
 	public AlephItem lookupItem(String alephItemId, boolean bibliographicDescription, boolean circulationStatus, boolean holdQueueLength, boolean itemDesrciption)
 			throws ParserConfigurationException, IOException, SAXException, AlephException {
 
 		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
 		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);
 
 		if (!validateRecordId(recordId) || !validateItemId(itemId)) {
 			throw new AlephException("Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
 		}
 
 		URL url = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, itemPathElement, recordId, itemsElement, itemId).toURL();
 
 		InputSource streamSource = new InputSource(url.openStream());
 
 		AlephItemHandler itemHandler = new AlephItemHandler(bibLibrary, requiredAtLeastOneService, bibliographicDescription, circulationStatus, holdQueueLength, itemDesrciption);
 
 		parser.parse(streamSource, itemHandler);
 
 		return itemHandler.getAlephItem();
 	}
 
 	/**
 	 * @param patronId
 	 * @param initData
 	 * @return
 	 * @throws AlephException
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public AlephUser lookupUser(String patronId, LookupUserInitiationData initData) throws AlephException, IOException, SAXException {
 
 		boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();
 		boolean loanedItemsDesired = initData.getLoanedItemsDesired();
 		boolean nameInformationDesired = initData.getNameInformationDesired();
 		boolean requestedItemsDesired = initData.getRequestedItemsDesired();
 		boolean userAddressInformationDesired = initData.getUserAddressInformationDesired();
 		boolean userFiscalAccountDesired = initData.getUserFiscalAccountDesired();
 		boolean userIdDesired = initData.getUserIdDesired();
 		boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();
 
 		boolean atLeastOneDesired = false;
 
 		// If there are only loanedItems desired, that means to include loans history to output
 		boolean loanedItemsDesiredOnly = loanedItemsDesired
 				&& !(blockOrTrapDesired || nameInformationDesired || requestedItemsDesired || userAddressInformationDesired || userFiscalAccountDesired || userIdDesired || userPrivilegeDesired);
 
 		// Create URL request only if specified service was desired
 		URL addressUrl = null;
 		if (nameInformationDesired || userIdDesired || userAddressInformationDesired || userPrivilegeDesired) {
 			atLeastOneDesired = true;
 			addressUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, patronInfoElement, addressElement).toURL();
 		}
 
 		URL circulationsUrl = null;
 		URL cashUrl = null;
 		if (userFiscalAccountDesired) {
 			atLeastOneDesired = true;
 			circulationsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement).toURL();
 			cashUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, "cash").addRequest("view", "full")
 					.toURL();
 		}
 
 		URL loansUrl = null;
 		URL loansHistoryUrl = null;
 		if (loanedItemsDesired) {
 			atLeastOneDesired = true;
 			if (!loanedItemsDesiredOnly) {
 				// If there is not history expected, parse regular loans
 				loansUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, loansElement)
 						.addRequest("view", "full").toURL();
 
 			} else {
 				loansHistoryUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, loansElement)
 						.addRequest("view", "full").addRequest("type", "history").toURL();
 			}
 		}
 
 		URL requestsUrl = null;
 		if (requestedItemsDesired) {
 			atLeastOneDesired = true;
 			// We suppose desired requests are at http://aleph.mzk.cz:1892/rest-dlf/patron/930118BXGO/circulationActions/requests/holds?view=full
 			requestsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement)
 					.addRequest("view", "full").toURL();
 		}
 
 		URL blocksOrTrapsUrl = null;
 		if (blockOrTrapDesired) {
 			atLeastOneDesired = true;
 			blocksOrTrapsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, patronStatusElement, blocksElement).toURL();
 		}
 
 		URL registrationUrl = null;
 		if (userPrivilegeDesired) {
 			atLeastOneDesired = true;
 			registrationUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, patronStatusElement, registrationElement).toURL();
 		}
 
 		if (atLeastOneDesired) {
 			AlephUser alephUser = new AlephUser();
 
 			AlephUserHandler userHandler = new AlephUserHandler(initData);
 
 			InputSource streamSource;
 
 			if (loansUrl != null || loansHistoryUrl != null) {
 
				AlephLoanHandler loanHandler = new AlephLoanHandler(bibLibrary);
 
 				if (loansUrl != null) {
 					streamSource = new InputSource(loansUrl.openStream());
 					parser.parse(streamSource, loanHandler);
 					alephUser.setLoanedItems(loanHandler.getListOfLoanedItems());
 				}
 				if (loansHistoryUrl != null) {
 					streamSource = new InputSource(loansHistoryUrl.openStream());
 					parser.parse(streamSource, loanHandler);
 					alephUser.setLoanedItems(loanHandler.getListOfLoanedItems());
 				}
 				
 			}
 			if (requestsUrl != null) {
 				AlephRequestItemHandler requestItemHandler = new AlephRequestItemHandler(bibLibrary);
 				
 				streamSource = new InputSource(requestsUrl.openStream());
 				parser.parse(streamSource, requestItemHandler);
 				
 				alephUser.setRequestedItems(requestItemHandler.getRequestedItems());
 			}
 
 			userHandler.setAlephUser(alephUser);
 
 			if (addressUrl != null) {
 				streamSource = new InputSource(addressUrl.openStream());
 
 				parser.parse(streamSource, userHandler);
 			}
 			if (cashUrl != null) {
 				streamSource = new InputSource(cashUrl.openStream());
 				
 				parser.parse(streamSource, userHandler);
 			}
 			if (circulationsUrl != null) {
 				streamSource = new InputSource(circulationsUrl.openStream());
 
 				parser.parse(streamSource, userHandler);
 			}
 			if (blocksOrTrapsUrl != null) {
 				streamSource = new InputSource(blocksOrTrapsUrl.openStream());
 
 				parser.parse(streamSource, userHandler);
 			}
 			if (registrationUrl != null) {
 				streamSource = new InputSource(registrationUrl.openStream());
 
 				parser.parse(streamSource, userHandler);
 			}
 
 			return userHandler.getAlephUser();
 		} else
 			return null;
 	}
 
 	public AlephRequestItem requestItem(RequestItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {
 
 		// Let requesting application know it is impossible to return response with multiple item ids, ergo it is not possible to accept more than one request at once
 		// Another problem is Aleph can't actually return one request Id for separate requests.
 		if (initData.getItemIds().size() > 1) {
 			throw new IOException("Aleph cannot process more requests at once, please supply these in a row.");
 		}
 
 		String alephItemId = initData.getItemId(0).getItemIdentifierValue();
 
 		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
 		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);
 
 		if (!validateRecordId(recordId) || !validateItemId(itemId)) {
 			throw new AlephException("Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
 		}
 
 		AlephRequestItem requestItem = new AlephRequestItem();
 
 		String pickupLocation = initData.getPickupLocation().getValue();
 
 		String needBeforeDate = AlephUtil.convertToAlephDate(initData.getNeedBeforeDate());
 		String earliestDateNeeded = AlephUtil.convertToAlephDate(initData.getEarliestDateNeeded());
 
 		String patronId = initData.getUserId().getUserIdentifierValue();
 
 		// Build e.g. this: MZK50001365071000010
 		// String alephItemId = admLibrary + recordId.substring(AlephConstants.LIBRARY_PARAM_LENGTH) + sequenceNumber;
 
 		// We need to put HTML PUT request to e.g. http://aleph.mzk.cz:1892/rest-dlf/patron/700/record/MZK01001330134/holds/MZK50001365071000010
 		URL holdUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, recordPathElement, recordId, holdsElement, itemId).toURL();
 
 		String firstNote = null;
 		// Our own testing identifier system (Moravian Library's)
 		if (patronId.equalsIgnoreCase("700") || true) {
 			firstNote = "dg";
 		}
 
 		String XMLRequest = new XMLBuilder().setParent("hold-request-parameters").setPickupLocation(pickupLocation).setLastInterestDate(needBeforeDate)
 				.setStartInterestDate(earliestDateNeeded).setFirstNote(firstNote).setRush("N").toString();
 
 		InputSource streamSource;
 		HttpURLConnection httpCon = (HttpURLConnection) holdUrl.openConnection();
 		httpCon.setDoOutput(true);
 		httpCon.setRequestMethod("PUT");
 
 		OutputStreamWriter outWriter = new OutputStreamWriter(httpCon.getOutputStream());
 		outWriter.write(XMLRequest);
 		outWriter.close();
 
 		streamSource = new InputSource(httpCon.getInputStream());
 
 		AlephRequestItemHandler requestItemHandler = new AlephRequestItemHandler(bibLibrary);
 		parser.parse(streamSource, requestItemHandler);
 
 		if (!requestItemHandler.returnedError()) {
 			requestItem.setRequestType(initData.getRequestType());
 			requestItem.setRequestScopeType(initData.getRequestScopeType());
 
 			// Parse sequence number
 			URL holdsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement)
 					.toURL();
 
 			streamSource = new InputSource(holdsUrl.openStream());
 			parser.parse(streamSource, requestItemHandler.parseSequenceNumber(itemId));
 
 			String seqNumber = requestItemHandler.getSequenceNumber();
 
 			// Sample URL: http://aleph.mzk.cz:1892/rest-dlf/patron/700/circulationActions/requests/holds/MZK500013118150000200001
 			URL requestsUrl = new URLBuilder().setBase(serverName, serverPort)
 					.setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement, itemId + seqNumber).toURL();
 
 			streamSource = new InputSource(requestsUrl.openStream());
 			parser.parse(streamSource, requestItemHandler);
 
 			RequestId requestId = new RequestId();
 			requestId.setRequestIdentifierValue(requestItemHandler.getRequestId());
 			requestItem.setRequestId(requestId);
 
 			if (uofDesiredWithinRequestItem) {
 				LookupUserInitiationData userInitData = new LookupUserInitiationData();
 				userInitData.setNameInformationDesired(true);
 				userInitData.setUserAddressInformationDesired(true);
 				userInitData.setUserIdDesired(true);
 				userInitData.setUserPrivilegeDesired(true);
 				userInitData.setBlockOrTrapDesired(true);
 
 				AlephUser user = lookupUser(patronId, userInitData);
 				requestItem.setUserOptionalFields(user.getUserOptionalFields());
 			}
 
 			if (iofDesiredWithinRequestItem) {
 				AlephItem item = lookupItem(alephItemId, true, true, true, true);
 				if (item.getAgency() == null) {
 					item.setAgency(defaultAgency);
 				}
 				requestItem.setItemOptionalFields(item.getItemOptionalFields());
 			}
 
 		} else {
 			Problem problem = new Problem();
 			problem.setProblemDetail(requestItemHandler.getNoteValue());
 			problem.setProblemValue(requestItemHandler.getReplyText());
 			problem.setProblemType(new ProblemType("Aleph returned error while processing the request. See details below."));
 			requestItem.setProblem(problem);
 		}
 		return requestItem;
 	}
 
 	public AlephRequestItem cancelRequestItem(CancelRequestItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {
 
 		String alephItemId = initData.getItemId().getItemIdentifierValue();
 
 		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
 		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);
 
 		if (!validateRecordId(recordId) || !validateItemId(itemId)) {
 			throw new AlephException("Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
 		}
 
 		AlephRequestItem requestItem = new AlephRequestItem();
 
 		String patronId = initData.getUserId().getUserIdentifierValue();
 
 		// Parse sequence number
 		URL holdsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement).toURL();
 
 		AlephRequestItemHandler requestItemHandler = new AlephRequestItemHandler(bibLibrary).parseSequenceNumber(itemId);
 
 		InputSource streamSource = new InputSource(holdsUrl.openStream());
 		parser.parse(streamSource, requestItemHandler);
 
 		if (requestItemHandler.requestWasFound() && requestItemHandler.isDeletable()) {
 
 			String seqNumber = requestItemHandler.getSequenceNumber();
 
 			// Sample URL: http://aleph.mzk.cz:1892/rest-dlf/patron/700/circulationActions/requests/holds/MZK500013118150000200001
 			URL holdRequestUrl = new URLBuilder().setBase(serverName, serverPort)
 					.setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement, itemId + seqNumber).toURL();
 
 			HttpURLConnection httpCon = (HttpURLConnection) holdRequestUrl.openConnection();
 			httpCon.setDoOutput(true);
 			httpCon.setRequestMethod("DELETE");
 
 			streamSource = new InputSource(httpCon.getInputStream());
 
 			parser.parse(streamSource, requestItemHandler);
 
 			if (!requestItemHandler.returnedError()) {
 				if (uofDesiredWithinCancelRequestItem) {
 					LookupUserInitiationData userInitData = new LookupUserInitiationData();
 					userInitData.setNameInformationDesired(true);
 					userInitData.setUserAddressInformationDesired(true);
 					userInitData.setUserIdDesired(true);
 					userInitData.setUserPrivilegeDesired(true);
 					userInitData.setBlockOrTrapDesired(true);
 
 					AlephUser user = lookupUser(patronId, userInitData);
 					requestItem.setUserOptionalFields(user.getUserOptionalFields());
 				}
 
 				if (iofDesiredWithinCancelRequestItem) {
 					AlephItem item = lookupItem(alephItemId, true, true, true, true);
 					if (item.getAgency() == null) {
 						item.setAgency(defaultAgency);
 					}
 					requestItem.setItemOptionalFields(item.getItemOptionalFields());
 				}
 			} else {
 				Problem problem = new Problem();
 				problem.setProblemValue(requestItemHandler.getReplyText());
 				problem.setProblemType(new ProblemType("Aleph returned error while processing cancel request. See details below."));
 				requestItem.setProblem(problem);
 			}
 		} else if (requestItemHandler.requestWasFound()) {
 			Problem problem = new Problem();
 			problem.setProblemValue("Delete value of specified hold request is set to \"N\"");
 			problem.setProblemType(new ProblemType("Requested deletion is not allowed."));
 			requestItem.setProblem(problem);
 		} else {
 			Problem problem = new Problem();
 			problem.setProblemType(new ProblemType("Request does not exist."));
 			requestItem.setProblem(problem);
 		}
 		return requestItem;
 	}
 
 	public AlephRequestItem lookupRequest(LookupRequestInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {
 
 		String alephItemId = initData.getItemId().getItemIdentifierValue();
 
 		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
 		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);
 
 		if (!validateRecordId(recordId) || !validateItemId(itemId)) {
 			throw new AlephException("Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
 		}
 
 		AlephRequestItem requestItem = new AlephRequestItem();
 
 		String patronId = initData.getUserId().getUserIdentifierValue();
 
 		URL holdsUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, requestsElement, holdsElement).toURL();
 
 		AlephRequestHandler requestHandler = new AlephRequestHandler(itemId, requestItem);
 		InputSource streamSource = new InputSource(holdsUrl.openStream());
 		parser.parse(streamSource, requestHandler);
 
 		if (requestHandler.requestWasFound() && requestHandler.getRequestLink() != null) {
 
 			requestItem.setRequestType(initData.getRequestType());
 			requestItem.setRequestScopeType(Version1RequestScopeType.ITEM);
 
 			URL requestLink = new URL(requestHandler.getRequestLink());
 
 			streamSource = new InputSource(requestLink.openStream());
 
 			requestHandler.setParsingRequests();
 			parser.parse(streamSource, requestHandler);
 
 			boolean nameInformationDesired = initData.getNameInformationDesired();
 			boolean userAddressInformationDesired = initData.getUserAddressInformationDesired();
 			boolean userIdDesired = initData.getUserIdDesired();
 			boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();
 			boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();
 
 			if (nameInformationDesired || userAddressInformationDesired || userIdDesired || userPrivilegeDesired || blockOrTrapDesired) {
 				LookupUserInitiationData userInitData = new LookupUserInitiationData();
 				userInitData.setNameInformationDesired(nameInformationDesired);
 				userInitData.setUserAddressInformationDesired(userAddressInformationDesired);
 				userInitData.setUserIdDesired(userIdDesired);
 				userInitData.setUserPrivilegeDesired(userPrivilegeDesired);
 				userInitData.setBlockOrTrapDesired(blockOrTrapDesired);
 
 				AlephUser user = lookupUser(patronId, userInitData);
 				requestItem.setUserOptionalFields(user.getUserOptionalFields());
 			}
 
 			boolean getBibDescription = initData.getBibliographicDescriptionDesired();
 			boolean getCircStatus = initData.getCirculationStatusDesired();
 			boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
 			boolean getItemDescription = initData.getItemDescriptionDesired();
 
 			if (getBibDescription || getCircStatus || getHoldQueueLength || getItemDescription) {
 				AlephItem item = lookupItem(alephItemId, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);
 				if (item.getAgency() == null) {
 					item.setAgency(defaultAgency);
 				}
 				requestItem.setItemOptionalFields(item.getItemOptionalFields());
 			}
 		} else {
 			Problem problem = new Problem();
 			problem.setProblemType(new ProblemType("Request does not exist."));
 			requestItem.setProblem(problem);
 		}
 
 		return requestItem;
 	}
 
 	public AlephRenewItem renewItem(RenewItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {
 
 		String alephLoanId = initData.getItemId().getItemIdentifierValue();
 
 		if (!validateLoanId(alephLoanId)) {
 			throw new AlephException("Loan Id is accepted only in strict format with strict length. e.g. MZK50004929137");
 		}
 
 		AlephRenewItem renewItem = new AlephRenewItem();
 
 		String patronId = initData.getUserId().getUserIdentifierValue();
 
 		URL loansUrl = new URLBuilder().setBase(serverName, serverPort).setPath(serverSuffix, userPathElement, patronId, circActionsElement, loansElement).toURL();
 
 		AlephLoanHandler loanHandler = new AlephLoanHandler(alephLoanId, renewItem);
 
 		InputSource streamSource = new InputSource(loansUrl.openStream());
 
 		parser.parse(streamSource, loanHandler);
 
 		if (loanHandler.loanWasFound() && loanHandler.isRenewable()) {
 
 			URL loanLink = new URL(loanHandler.getLoanLink());
 
 			String desiredDueDate = AlephUtil.convertToAlephDate(initData.getDesiredDateDue());
 
 			String XMLRequest = "<z36-due-date>" + desiredDueDate + "</z36-due-date>";
 
 			HttpURLConnection httpCon = (HttpURLConnection) loanLink.openConnection();
 			httpCon.setDoOutput(true);
 			httpCon.setRequestMethod("POST");
 
 			OutputStreamWriter outWriter = new OutputStreamWriter(httpCon.getOutputStream());
 			outWriter.write(XMLRequest);
 			outWriter.close();
 
 			streamSource = new InputSource(httpCon.getInputStream());
 
 			parser.parse(streamSource, loanHandler);
 
 			if (loanHandler.actionSucceeded()) {
 
 				streamSource = new InputSource(loanLink.openStream());
 
 				parser.parse(streamSource, loanHandler);
 
 				boolean nameInformationDesired = initData.getNameInformationDesired();
 				boolean userAddressInformationDesired = initData.getUserAddressInformationDesired();
 				boolean userIdDesired = initData.getUserIdDesired();
 				boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();
 				boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();
 
 				if (nameInformationDesired || userAddressInformationDesired || userIdDesired || userPrivilegeDesired || blockOrTrapDesired) {
 					LookupUserInitiationData userInitData = new LookupUserInitiationData();
 					userInitData.setNameInformationDesired(nameInformationDesired);
 					userInitData.setUserAddressInformationDesired(userAddressInformationDesired);
 					userInitData.setUserIdDesired(userIdDesired);
 					userInitData.setUserPrivilegeDesired(userPrivilegeDesired);
 					userInitData.setBlockOrTrapDesired(blockOrTrapDesired);
 
 					AlephUser user = lookupUser(patronId, userInitData);
 					renewItem.setUserOptionalFields(user.getUserOptionalFields());
 				}
 
 				boolean getBibDescription = initData.getBibliographicDescriptionDesired();
 				boolean getCircStatus = initData.getCirculationStatusDesired();
 				boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
 				boolean getItemDescription = initData.getItemDescriptionDesired();
 
 				if (getBibDescription || getCircStatus || getHoldQueueLength || getItemDescription) {
 					String alephItemId = AlephUtil.buildAlephItemId(bibLibrary, admLibrary, loanHandler.getDocNumber(), loanHandler.getItemSequenceNumber());
 
 					AlephItem item = lookupItem(alephItemId, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);
 					if (item.getAgency() == null) {
 						item.setAgency(defaultAgency);
 					}
 					renewItem.setItemOptionalFields(item.getItemOptionalFields());
 				}
 			} else {
 				Problem problem = new Problem();
 				problem.setProblemDetail(loanHandler.getStatusText());
 				problem.setProblemType(new ProblemType(loanHandler.getReplyText()));
 				renewItem.setProblem(problem);
 			}
 		} else if (loanHandler.loanWasFound()) {
 			Problem problem = new Problem();
 			problem.setProblemType(new ProblemType("Loan is marked as not renewable."));
 			renewItem.setProblem(problem);
 		} else {
 			Problem problem = new Problem();
 			problem.setProblemType(new ProblemType("Loan does not exist."));
 			renewItem.setProblem(problem);
 		}
 		return renewItem;
 	}
 
 	public AgencyAddressInformation getAgencyPhysicalAddressInformation() {
 		AgencyAddressInformation agencyAddressInformation = new AgencyAddressInformation();
 
 		PhysicalAddress physicalAddress = new PhysicalAddress();
 
 		UnstructuredAddress unstructuredAddress = new UnstructuredAddress();
 
 		unstructuredAddress.setUnstructuredAddressData(agencyAddress);
 		unstructuredAddress.setUnstructuredAddressType(Version1UnstructuredAddressType.NEWLINE_DELIMITED_TEXT);
 
 		physicalAddress.setUnstructuredAddress(unstructuredAddress);
 
 		physicalAddress.setPhysicalAddressType(Version1PhysicalAddressType.STREET_ADDRESS);
 		agencyAddressInformation.setPhysicalAddress(physicalAddress);
 
 		AgencyAddressRoleType agencyAddressRoleType = Version1AgencyAddressRoleType.OFFICIAL;
 		agencyAddressInformation.setAgencyAddressRoleType(agencyAddressRoleType);
 
 		return agencyAddressInformation;
 	}
 
 	public List<OrganizationNameInformation> getOrganizationNameInformations() {
 		List<OrganizationNameInformation> organizationNameInformations = new ArrayList<OrganizationNameInformation>();
 		OrganizationNameInformation organizationNameInfo = new OrganizationNameInformation();
 
 		organizationNameInfo.setOrganizationName(agencyName);
 		organizationNameInfo.setOrganizationNameType(Version1OrganizationNameType.TRANSLATED_NAME);
 		organizationNameInformations.add(organizationNameInfo);
 		return organizationNameInformations;
 	}
 
 	public List<ApplicationProfileSupportedType> getApplicationProfileSupportedTypes() {
 		List<ApplicationProfileSupportedType> applicationProfileSupportedTypes = new ArrayList<ApplicationProfileSupportedType>();
 		ApplicationProfileSupportedType applicationProfileSupportedType = new ApplicationProfileSupportedType("unknown", "Development profile");
 		applicationProfileSupportedTypes.add(applicationProfileSupportedType);
 		return applicationProfileSupportedTypes;
 	}
 
 	public List<ConsortiumAgreement> getConsortiumAgreements() {
 		List<ConsortiumAgreement> consortiumAgreements = new ArrayList<ConsortiumAgreement>();
 		ConsortiumAgreement consortiumAgreement = new ConsortiumAgreement("some-agreement-scheme", "Testing consortium agreement value.");
 		consortiumAgreements.add(consortiumAgreement);
 
 		consortiumAgreement = new ConsortiumAgreement("some-agreement-scheme", "Another testing consortium agreement value.");
 		consortiumAgreements.add(consortiumAgreement);
 		return consortiumAgreements;
 	}
 }
