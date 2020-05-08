 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
 import org.extensiblecatalog.ncip.v2.aleph.util.ItemToken;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.BibInformation;
 import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
 import org.extensiblecatalog.ncip.v2.service.BibliographicId;
 import org.extensiblecatalog.ncip.v2.service.BibliographicRecordIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.HoldingsSet;
 import org.extensiblecatalog.ncip.v2.service.ItemDescription;
 import org.extensiblecatalog.ncip.v2.service.ItemId;
 import org.extensiblecatalog.ncip.v2.service.ItemInformation;
 import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
 import org.extensiblecatalog.ncip.v2.service.LookupItemSetInitiationData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemSetResponseData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemSetService;
 import org.extensiblecatalog.ncip.v2.service.Problem;
 import org.extensiblecatalog.ncip.v2.service.ProblemType;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.ServiceContext;
 import org.extensiblecatalog.ncip.v2.service.ServiceError;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.ServiceHelper;
 import org.extensiblecatalog.ncip.v2.service.Version1BibliographicRecordIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.Version1GeneralProcessingError;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemDescriptionLevel;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.Version1LookupItemProcessingError;
 import org.xml.sax.SAXException;
 
 public class AlephLookupItemSetService implements LookupItemSetService {
 
 	static Logger log = Logger.getLogger(AlephLookupItemSetService.class);
 
 	private static HashMap<String, ItemToken> tokens = new HashMap<String, ItemToken>();
 
 	/**
 	 * Construct an AlephRemoteServiceManager; this class is not configurable so there are no parameters.
 	 */
 	public AlephLookupItemSetService() {
 	}
 
 	@Override
 	public LookupItemSetResponseData performService(LookupItemSetInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager)
 			throws ServiceException {
 		Date sService = new Date();
 		LookupItemSetResponseData responseData = new LookupItemSetResponseData();
 		AlephRemoteServiceManager alephSvcMgr = (AlephRemoteServiceManager) serviceManager;
 		boolean getBibDescription = initData.getBibliographicDescriptionDesired();
 		boolean getCircStatus = initData.getCirculationStatusDesired();
 		boolean getElectronicResource = initData.getElectronicResourceDesired();
 		boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
 		boolean getItemDescription = initData.getItemDescriptionDesired();
 		boolean getLocation = initData.getLocationDesired();
 
 		List<BibliographicId> bibIds = initData.getBibliographicIds();
 
 		String token = initData.getNextItemToken();
 		ItemToken nextItemToken = null;
 		// remove any bib ids from bibIds list that may have already been processed
 		if (token != null) {
 			nextItemToken = tokens.get(token);
 			if (nextItemToken != null) {
 				int index = getBibIdIndex(bibIds, nextItemToken.getBibliographicId());
 				if (index != -1) {
 					// remove the ones already processed
 					bibIds.subList(0, index).clear();
 				}
 
 				// Remove token from memory hashmap
 				tokens.remove(token);
 			} else {
 				Problem problem = new Problem();
 				problem.setProblemDetail("Invalid nextItemToken");
 				problem.setProblemValue("nextItemToken =" + token);
 				List<Problem> problems = new ArrayList<Problem>();
 				problems.add(problem);
 				responseData.setProblems(problems);
 
 				return responseData;
 			}
 			log.debug("after removing already processed Bib ids =" + bibIds);
 
 		}
 		List<BibInformation> bibInformations = new ArrayList<BibInformation>();
 
 		if (bibIds != null && bibIds.size() > 0) {
 
 			for (BibliographicId bibId : bibIds) {
 				BibInformation bibInformation = new BibInformation();
 
 				try {
 					if (bibId.getBibliographicRecordId() != null) {
 
 						String id = bibId.getBibliographicRecordId().getBibliographicRecordIdentifier();
 						log.debug("Processing Bib id = " + id);
 
 						bibInformation.setBibliographicId(bibId);
 
 						List<AlephItem> alephItems = alephSvcMgr.lookupItems(id, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);
 						if (alephItems != null) {
 
 							AgencyId suppliedAgencyId;
 							if (getBibDescription) {
 								if (initData.getInitiationHeader() == null || initData.getInitiationHeader().getFromAgencyId() == null)
 									suppliedAgencyId = alephSvcMgr.getDefaultAgencyId();
 								else
 									suppliedAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId();
 							} else
 								suppliedAgencyId = null;
 							List<HoldingsSet> holdingSets = parseHoldingsSets(alephItems, suppliedAgencyId, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);;							
 							bibInformation.setHoldingsSets(holdingSets);
 
 							bibInformations.add(bibInformation);
 
 						} else if (alephSvcMgr.echoParticularProblemsToLUIS) {
 							Problem p = new Problem();
 							p.setProblemType(Version1LookupItemProcessingError.UNKNOWN_ITEM);
 							p.setProblemDetail("Item " + id + " you are searching for doesn't exists.");
 
 							List<Problem> problems = new ArrayList<Problem>();
 							problems.add(p);
 
 							bibInformation.setProblems(problems);
 							bibInformations.add(bibInformation);
 						}
 
 					} else if (bibId.getBibliographicItemId() != null) {
 						String id = bibId.getBibliographicItemId().getBibliographicItemIdentifier();
 						bibInformation.setBibliographicId(bibId);
 
 						AlephItem alephItem = alephSvcMgr.lookupItem(id, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);
 						if (alephItem != null) {
 
 							AgencyId suppliedAgencyId;
 							if (getBibDescription) {
 								if (initData.getInitiationHeader() == null || initData.getInitiationHeader().getFromAgencyId() == null)
 									suppliedAgencyId = alephSvcMgr.getDefaultAgencyId();
 								else
 									suppliedAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId();
 							} else
 								suppliedAgencyId = null;
 
 							HoldingsSet holdingsSet = parseHoldingsSet(alephItem, suppliedAgencyId, getBibDescription, getCircStatus, getHoldQueueLength, getItemDescription);
 
 							List<HoldingsSet> holdingsSets = new ArrayList<HoldingsSet>();
 							holdingsSets.add(holdingsSet);
 							bibInformation.setHoldingsSets(holdingsSets);
 
 							bibInformations.add(bibInformation);
 
 						} else if (alephSvcMgr.echoParticularProblemsToLUIS) {
 							Problem p = new Problem();
 							p.setProblemType(Version1LookupItemProcessingError.UNKNOWN_ITEM);
 							p.setProblemDetail("Item " + id + " you are searching for doesn't exists.");
 
 							List<Problem> problems = new ArrayList<Problem>();
 							problems.add(p);
 
 							bibInformation.setProblems(problems);
 							bibInformations.add(bibInformation);
 						}
 
 					} else {
 
 						bibInformation.setProblems(ServiceHelper.generateProblems(Version1GeneralProcessingError.NEEDED_DATA_MISSING, "BibliographicItemId/RecordId", null,
 								"BibliographicItemId/RecordId was not properly set."));
 						bibInformations.add(bibInformation);
 					}
 				} catch (IOException ie) {
 					Problem p = new Problem();
 					p.setProblemType(new ProblemType("Procesing IOException error"));
 					p.setProblemDetail(ie.getMessage());
 					List<Problem> problems = new ArrayList<Problem>();
 					problems.add(p);
 
 					bibInformation.setProblems(problems);
 					bibInformations.add(bibInformation);
 				} catch (ParserConfigurationException pce) {
 					Problem p = new Problem();
 					p.setProblemType(new ProblemType("Procesing ParserConfigurationException error"));
 					p.setProblemDetail(pce.getMessage());
 					List<Problem> problems = new ArrayList<Problem>();
 					problems.add(p);
 
 					bibInformation.setProblems(problems);
 					bibInformations.add(bibInformation);
 				} catch (SAXException se) {
 					Problem p = new Problem();
 					p.setProblemType(new ProblemType("Procesing SAXException error"));
 					p.setProblemDetail(se.getMessage());
 					List<Problem> problems = new ArrayList<Problem>();
 					problems.add(p);
 
 					bibInformation.setProblems(problems);
 					bibInformations.add(bibInformation);
 				} catch (AlephException ae) {
 					Problem p = new Problem();
 					p.setProblemType(new ProblemType("Procesing AlephException error"));
 					p.setProblemDetail(ae.getMessage());
 					List<Problem> problems = new ArrayList<Problem>();
 					problems.add(p);
 
 					bibInformation.setProblems(problems);
 					bibInformations.add(bibInformation);
 				} catch (Exception e) {
 					Problem p = new Problem();
 					p.setProblemType(new ProblemType("Unknown procesing exception error"));
 					p.setProblemDetail(e.getMessage());
 					List<Problem> problems = new ArrayList<Problem>();
 					problems.add(p);
 					responseData.setProblems(problems);
 				}
 
 			}
 
 		} else if (initData.getItemIds() != null && initData.getItemIds().size() > 0) {
 
 			for (ItemId itemId : initData.getItemIds()) {
 
 				ItemInformation itemInformation = new ItemInformation();
 				itemInformation.setProblems(ServiceHelper.generateProblems(Version1LookupItemProcessingError.UNKNOWN_ITEM, "//BibliographicRecordId",
 						itemId.getItemIdentifierValue(), "Item # '" + itemId.getItemIdentifierValue() + "' not found."));
 				List<ItemInformation> itemInformationList = new ArrayList<ItemInformation>();
 				itemInformationList.add(itemInformation);
 				HoldingsSet holdingsSet = new HoldingsSet();
 				holdingsSet.setItemInformations(itemInformationList);
 				List<HoldingsSet> holdingsSetList = new ArrayList<HoldingsSet>();
 				BibInformation bibInformation = new BibInformation();
 				bibInformation.setHoldingsSets(holdingsSetList);
 				bibInformations.add(bibInformation);
 
 			}
 
 		} // /else - Do nothing, the fact that the bibInformationsList is empty will cause a Problem to be created below
 
 		if (responseData.getProblems() == null || responseData.getProblem(0) == null)
 			responseData.setBibInformations(bibInformations);
 
 		Date eService = new Date();
 
 		log.debug("Service time log : " + (eService.getTime() - sService.getTime()) + "  " + ((eService.getTime() - sService.getTime()) / 1000) + " sec");
 		log.debug(responseData);
 		log.debug("Tokens in memory: " + tokens);
 		return responseData;
 	}
 
 	private HoldingsSet parseHoldingsSet(AlephItem alephItem, AgencyId suppliedAgencyId, boolean getBibDescription, boolean getCircStatus, boolean getHoldQueueLength,
 			boolean getItemDescription) {
 		HoldingsSet holdingsSet = new HoldingsSet();
 		List<ItemInformation> itemInfoList = new ArrayList<ItemInformation>();
 
 		ItemOptionalFields iof = new ItemOptionalFields();
 
 		if (getBibDescription) {
 			BibliographicDescription bDesc = AlephUtil.getBibliographicDescription(alephItem, suppliedAgencyId);
 			holdingsSet.setBibliographicDescription(bDesc);
 		}
 
 		// Schema v2_02 defines to forward alephItemInfo even though there is no optional information desired
 		// EDIT: alephItemInformation does not transfer only optional fields ...
 		ItemInformation info = new ItemInformation();
 
 		// TODO: Is it neccessary to set particular item ids? -> implement parsing those if yes
 		ItemId itemId = new ItemId();
		itemId.setItemIdentifierType(Version1ItemIdentifierType.BARCODE);
 		itemId.setItemIdentifierValue(alephItem.getBarcode());
 		info.setItemId(itemId);
 
 		if (getHoldQueueLength || getCircStatus || getItemDescription) {
 			if (getHoldQueueLength)
 				iof.setHoldQueueLength(new BigDecimal(alephItem.getHoldQueueLength()));
 			if (getCircStatus)
 				iof.setCirculationStatus(alephItem.getCirculationStatus());
 			if (getItemDescription) {
 				ItemDescription itemDescription = new ItemDescription();
 				itemDescription.setItemDescriptionLevel(Version1ItemDescriptionLevel.ITEM);
 				itemDescription.setCallNumber(alephItem.getCallNumber());
 				itemDescription.setCopyNumber(alephItem.getCopyNumber());
 				itemDescription.setNumberOfPieces(alephItem.getNumberOfPieces());
 				iof.setItemDescription(itemDescription);
 			}
 			info.setItemOptionalFields(iof);
 		}
 
 		itemInfoList.add(info);
 		holdingsSet.setItemInformations(itemInfoList);
 		
 		return holdingsSet;
 	}
 
 	private List<HoldingsSet> parseHoldingsSets(List<AlephItem> alephItems, AgencyId suppliedAgencyId, boolean getBibDescription, boolean getCircStatus,
 			boolean getHoldQueueLength, boolean getItemDescription) {
 		List<HoldingsSet> holdingsSets = new ArrayList<HoldingsSet>();
 		HoldingsSet holdingsSet = new HoldingsSet();
 		List<ItemInformation> itemInfoList = new ArrayList<ItemInformation>();
 		for (AlephItem item : alephItems) {
 			item.setNumberOfPieces(new BigDecimal(alephItems.size()));
 
 			ItemOptionalFields iof = new ItemOptionalFields();
 
			// bibInformation.setBibliographicDescription(null);(new BigDecimal(alephItems.size()));
 			if (getBibDescription) {
 				BibliographicDescription bDesc = AlephUtil.getBibliographicDescription(item, suppliedAgencyId);
 				holdingsSet.setBibliographicDescription(bDesc);
 			}
 
 			// Schema v2_02 defines to forward itemInfo even though there is no optional information desired
 			// EDIT: ItemInformation does not transfer only optional fields ...
 			ItemInformation info = new ItemInformation();
 
 			// TODO: Is it neccessary to set particular item ids? -> implement parsing those if yes
 			ItemId itemId = new ItemId();
 			itemId.setItemIdentifierValue(item.getBarcode());
 			info.setItemId(itemId);
 
 			if (getHoldQueueLength || getCircStatus || getItemDescription) {
 				if (getHoldQueueLength)
 					iof.setHoldQueueLength(new BigDecimal(item.getHoldQueueLength()));
 				if (getCircStatus)
 					iof.setCirculationStatus(item.getCirculationStatus());
 				if (getItemDescription) {
 					ItemDescription itemDescription = new ItemDescription();
 					itemDescription.setItemDescriptionLevel(Version1ItemDescriptionLevel.ITEM);
 					itemDescription.setCallNumber(item.getCallNumber());
 					itemDescription.setCopyNumber(item.getCopyNumber());
 					itemDescription.setNumberOfPieces(item.getNumberOfPieces());
 					iof.setItemDescription(itemDescription);
 				}
 				info.setItemOptionalFields(iof);
 			}
 
 			itemInfoList.add(info);
 		}
 		holdingsSet.setItemInformations(itemInfoList);
 
 		holdingsSets.add(holdingsSet);
 		return holdingsSets;
 	}
 
 	private int getBibIdIndex(List<BibliographicId> bibIds, String bibId) {
 
 		for (int i = 0; i < bibIds.size(); i++) {
 
 			if (bibIds.get(i).getBibliographicItemId().getBibliographicItemIdentifier().equalsIgnoreCase(bibId)) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 }
