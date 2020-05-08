 package org.extensiblecatalog.ncip.v2.aleph.util;
 
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
 import org.extensiblecatalog.ncip.v2.service.BibliographicItemId;
 import org.extensiblecatalog.ncip.v2.service.BibliographicRecordId;
 import org.extensiblecatalog.ncip.v2.service.ElectronicResource;
 import org.extensiblecatalog.ncip.v2.service.ItemDescription;
 import org.extensiblecatalog.ncip.v2.service.ItemDescriptionLevel;
 import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
 import org.extensiblecatalog.ncip.v2.service.Location;
 import org.extensiblecatalog.ncip.v2.service.LocationName;
 import org.extensiblecatalog.ncip.v2.service.LocationNameInstance;
 import org.extensiblecatalog.ncip.v2.service.LocationType;
 import org.extensiblecatalog.ncip.v2.service.MediumType;
 import org.extensiblecatalog.ncip.v2.service.SchemeValuePair;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.Version1BibliographicItemIdentifierCode;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemDescriptionLevel;
 import org.extensiblecatalog.ncip.v2.service.Version1MediumType;
 import org.extensiblecatalog.ncip.v2.service.XcCirculationStatus;
 
 public class AlephUtil {
 	public static BibliographicDescription getBibliographicDescription(AlephItem alephItem, AgencyId agencyId) {
 		BibliographicDescription bibliographicDescription = new BibliographicDescription();
 
 		if (alephItem.getAuthor() != null) {
 			bibliographicDescription.setAuthor(alephItem.getAuthor());
 		}
 		if (alephItem.getIsbn() != null) {
 			BibliographicItemId bibliographicItemId = new BibliographicItemId();
 			bibliographicItemId.setBibliographicItemIdentifier(alephItem.getIsbn());
 			bibliographicItemId.setBibliographicItemIdentifierCode((Version1BibliographicItemIdentifierCode.ISBN));
 			List<BibliographicItemId> bibIds = new ArrayList<BibliographicItemId>();
 			bibIds.add(bibliographicItemId);
 			bibliographicDescription.setBibliographicItemIds(bibIds);
 		}
 
 		if (alephItem.getMediumType() != null) {
 			Version1MediumType.loadAll();
 			MediumType mediumType;
 			try {
				mediumType = MediumType.find(Version1MediumType.VERSION_1_MEDIUM_TYPE, alephItem.getMediumType());
 				if (mediumType != null) {
 					bibliographicDescription.setMediumType(mediumType);
 				}
 			} catch (ServiceException e) {
 				e.printStackTrace();
 			}
 			
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
 
 		if (alephItem.getBibId() != null) {
 			BibliographicRecordId bibliographicRecordId = new BibliographicRecordId();
 			bibliographicRecordId.setAgencyId(agencyId);
 			bibliographicRecordId.setBibliographicRecordIdentifier(alephItem.getBibId());
 			List<BibliographicRecordId> bibIds = new ArrayList<BibliographicRecordId>();
 			bibIds.add(bibliographicRecordId);
 			bibliographicDescription.setBibliographicRecordIds(bibIds);
 		}
 
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
 			if (alephItem.getCirculationStatus().getValue().equalsIgnoreCase(AlephConstants.ON_SHELF)) {
 				iof.setCirculationStatus(alephItem.getCirculationStatus());
 			}
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
 
 		if (alephItem.getLocation() != null) {
 
 			LocationNameInstance locationNameInstance = new LocationNameInstance();
 
 			locationNameInstance.setLocationNameValue(alephItem.getLocation());
 			// TODO: more to come from requirement for level
 			locationNameInstance.setLocationNameLevel(new BigDecimal("1"));// temperarily set to 1.
 
 			List<LocationNameInstance> locationNameInstances = new ArrayList<LocationNameInstance>();
 			locationNameInstances.add(locationNameInstance);
 
 			LocationName locationName = new LocationName();
 			locationName.setLocationNameInstances(locationNameInstances);
 
 			Location location = new Location();
 			location.setLocationName(locationName);
 			location.setLocationType(new LocationType(AlephConstants.DEFAULT_SCHEME, AlephConstants.LOCATION_TYPE_PERMANENT));
 			List<Location> locations = new ArrayList<Location>();
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
 }
