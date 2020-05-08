 /***************************************************************************
  * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
  * All Rights Reserved.
  *
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  ***************************************************************************/
 package org.gbif.portal.service.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gbif.portal.dao.geospatial.CountryDAO;
 import org.gbif.portal.dao.occurrence.IdentifierRecordDAO;
 import org.gbif.portal.dao.occurrence.ImageRecordDAO;
 import org.gbif.portal.dao.occurrence.LinkRecordDAO;
 import org.gbif.portal.dao.occurrence.OccurrenceRecordDAO;
 import org.gbif.portal.dao.occurrence.RawOccurrenceRecordDAO;
 import org.gbif.portal.dao.occurrence.TypificationRecordDAO;
 import org.gbif.portal.dao.resources.DataProviderDAO;
 import org.gbif.portal.dao.resources.DataResourceDAO;
 import org.gbif.portal.dao.resources.ResourceNetworkDAO;
 import org.gbif.portal.dao.taxonomy.TaxonConceptDAO;
 import org.gbif.portal.dto.DTOFactory;
 import org.gbif.portal.dto.SearchResultsDTO;
 import org.gbif.portal.dto.occurrence.ExtendedOccurrenceRecordDTO;
 import org.gbif.portal.dto.occurrence.IdentifierRecordDTO;
 import org.gbif.portal.dto.occurrence.ImageRecordDTO;
 import org.gbif.portal.dto.occurrence.KmlOccurrenceRecordDTO;
 import org.gbif.portal.dto.occurrence.LinkRecordDTO;
 import org.gbif.portal.dto.occurrence.OccurrenceRecordDTO;
 import org.gbif.portal.dto.occurrence.RawOccurrenceRecordDTO;
 import org.gbif.portal.dto.occurrence.TypificationRecordDTO;
 import org.gbif.portal.dto.util.BoundingBoxDTO;
 import org.gbif.portal.dto.util.SearchConstraints;
 import org.gbif.portal.dto.util.TimePeriodDTO;
 import org.gbif.portal.model.occurrence.BasisOfRecord;
 import org.gbif.portal.model.occurrence.OccurrenceRecord;
 import org.gbif.portal.model.occurrence.RawOccurrenceRecord;
 import org.gbif.portal.model.resources.DataProvider;
 import org.gbif.portal.model.resources.DataResource;
 import org.gbif.portal.model.resources.ResourceNetwork;
 import org.gbif.portal.model.taxonomy.TaxonConcept;
 import org.gbif.portal.service.OccurrenceManager;
 import org.gbif.portal.service.ServiceException;
 
 import net.sibcolombia.portal.dao.geospatial.*;
 
 /**
  * An implementation of the OccurrenceManager interface that makes use of the
  * DAO layer objects for data access.
  * 
  * @author dmartin
  * @author dhobern
  */
 public class OccurrenceManagerImpl implements OccurrenceManager {
 
 	protected static Log logger = LogFactory
 			.getLog(OccurrenceManagerImpl.class);
 
 	/** DAOs */
 	protected CountryDAO countryDAO;
 	protected DepartmentDAO departmentDAO;
 	protected CountyDAO countyDAO;
 	protected ParamoDAO paramoDAO;
 	protected MarineZoneDAO marineZoneDAO;
 	protected ProtectedAreaDAO protectedAreaDAO;
 	protected EcosystemDAO ecosystemDAO;
 	protected ZonificacionDAO zonificacionDAO;
 	protected DataProviderDAO dataProviderDAO;
 	protected DataResourceDAO dataResourceDAO;
 	protected ResourceNetworkDAO resourceNetworkDAO;
 	protected OccurrenceRecordDAO occurrenceRecordDAO;
 	protected RawOccurrenceRecordDAO rawOccurrenceRecordDAO;
 	protected ImageRecordDAO imageRecordDAO;
 	protected LinkRecordDAO linkRecordDAO;
 	protected TypificationRecordDAO typificationRecordDAO;
 	protected IdentifierRecordDAO identifierRecordDAO;
 	protected TaxonConceptDAO taxonConceptDAO;
 
 	/** The DTO factory for creating Brief Occurrence Record DTOs **/
 	protected DTOFactory briefOccurrenceRecordDTOFactory;
 	/** The DTO factory for creating Occurrence Record DTOs **/
 	protected DTOFactory occurrenceRecordDTOFactory;
 	/** The DTO factory for creating RawOccurrence Record DTOs **/
 	protected DTOFactory rawOccurrenceRecordDTOFactory;
 	/** The DTO factory for creating Image Record DTOs **/
 	protected DTOFactory imageRecordDTOFactory;
 	/** The DTO factory for creating Link Record DTOs **/
 	protected DTOFactory linkRecordDTOFactory;
 	/** The DTO factory for creating Identifier Record DTOs **/
 	protected DTOFactory identifierRecordDTOFactory;
 	/** The DTO factory for creating Typification Record DTOs **/
 	protected DTOFactory typificationRecordDTOFactory;
 	/** The DTO factory for creating Extended Occurrence Record DTOs **/
 	protected DTOFactory extendedOccurrenceRecordDTOFactory;
 	/** The DTO factory for creating Kml Occurrence Record DTOs **/
 	protected DTOFactory kmlOccurrenceRecordDTOFactory;
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getOccurrenceRecordFor(java.lang.String)
 	 */
 	public OccurrenceRecordDTO getOccurrenceRecordFor(String occurrenceRecordKey)
 			throws ServiceException {
 		Long occurrenceRecordId = parseKey(occurrenceRecordKey);
 		OccurrenceRecord occurrenceRecord = occurrenceRecordDAO
 				.getOccurrenceRecordFor(occurrenceRecordId);
 		return (OccurrenceRecordDTO) occurrenceRecordDTOFactory
 				.createDTO(occurrenceRecord);
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getExtendedOccurrenceRecordFor(java.lang.String)
 	 */
 	public ExtendedOccurrenceRecordDTO getExtendedOccurrenceRecordFor(
 			String occurrenceRecordKey) throws ServiceException {
 		Long occurrenceRecordId = parseKey(occurrenceRecordKey);
 		OccurrenceRecord occurrenceRecord = occurrenceRecordDAO
 				.getOccurrenceRecordFor(occurrenceRecordId);
 		return (ExtendedOccurrenceRecordDTO) extendedOccurrenceRecordDTOFactory
 				.createDTO(occurrenceRecord);
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getOccurrenceRecordFor(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<OccurrenceRecordDTO> getOccurrenceRecordByCodes(
 			String institutionCode, String collectionCode,
 			String catalogueNumber) throws ServiceException {
 		List<OccurrenceRecord> occurrenceRecords = occurrenceRecordDAO
 				.getOccurrenceRecordFor(institutionCode, collectionCode,
 						catalogueNumber);
 		return (List<OccurrenceRecordDTO>) occurrenceRecordDTOFactory
 				.createDTOList(occurrenceRecords);
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getTypificationRecordsForOccurrenceRecord(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<TypificationRecordDTO> getTypificationRecordsForOccurrenceRecord(
 			String occurrenceRecordKey) throws ServiceException {
 		return (List<TypificationRecordDTO>) typificationRecordDTOFactory
 				.createDTOList(typificationRecordDAO
 						.getTypificationRecordsForOccurrenceRecord(parseKey(occurrenceRecordKey)));
 	}
 
 	/**
 	 * TODO for performance reasons this should be refactored not to query for
 	 * model entities to pass into DAO methods. Rather the DAO methods should be
 	 * passed ids.
 	 * 
 	 * @see org.gbif.portal.service.OccurrenceManager#findOccurrenceRecords(java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String,
 	 *      java.lang.String, java.langString, org.gbif.portal.dto.util.BoundingBoxDTO,
 	 *      org.gbif.portal.dto.util.TimePeriodDTO, boolean,
 	 *      org.gbif.portal.dto.util.SearchConstraints)
 	 */
 	public SearchResultsDTO findOccurrenceRecords(String dataProviderKey,
 			String dataResourceKey, String resourceNetworkKey,
 			String taxonConceptKey, String scientificName,
 			String hostIsoCountryCode, String originIsoCountryCode,
 			String originIsoDepartmentCode, String originIsoCountyCode,
 			String complexId, String marineId, String protectedId, String ecosystem, 
 			String zonificacionId, String basisOfRecordCode,
 			String cellId, BoundingBoxDTO boundingBox,
 			TimePeriodDTO timePeriod, Date modifiedSince,
 			boolean georeferencedOnly, SearchConstraints searchConstraints)
 			throws ServiceException {
 
 		DataProvider dataProvider = null;
 		if (dataProviderKey != null) {
 			Long dataProviderId = parseKey(dataProviderKey);
 			dataProvider = dataProviderDAO.getDataProviderFor(dataProviderId);
 
 			if (dataProvider == null) {
 				throw new ServiceException("No DataProvider found for key "
 						+ dataProviderKey);
 			}
 		}
 
 		DataResource dataResource = null;
 		if (dataResourceKey != null) {
 			Long dataResourceId = parseKey(dataResourceKey);
 			dataResource = dataResourceDAO.getDataResourceFor(dataResourceId);
 
 			if (dataResource == null) {
 				throw new ServiceException("No DataResource found for key "
 						+ dataResourceKey);
 			}
 		}
 
 		ResourceNetwork resourceNetwork = null;
 		if (resourceNetworkKey != null) {
 			Long resourceNetworkId = parseKey(resourceNetworkKey);
 			resourceNetwork = resourceNetworkDAO
 					.getResourceNetworkFor(resourceNetworkId);
 
 			if (resourceNetwork == null) {
 				throw new ServiceException("No ResourceNetwork found for key "
 						+ resourceNetworkKey);
 			}
 		}
 
 		TaxonConcept taxonConcept = null;
 		if (taxonConceptKey != null) {
 			Long taxonConceptId = parseKey(taxonConceptKey);
 			taxonConcept = taxonConceptDAO.getTaxonConceptFor(taxonConceptId);
 
 			if (taxonConcept == null) {
 				throw new ServiceException("No TaxonConcept found for key "
 						+ taxonConceptKey);
 			}
 
 			// Perform our search using nub concepts
 			if (!taxonConcept.getIsNubConcept()
 					&& taxonConcept.getPartnerConcept() != null) {
 				taxonConcept = taxonConceptDAO.getTaxonConceptFor(taxonConcept
 						.getPartnerConceptId());
 			}
 		}
 
 		if (hostIsoCountryCode != null
 				&& countryDAO.getCountryForIsoCountryCode(hostIsoCountryCode,
 						null) == null) {
 			throw new ServiceException("No country found for host ISO code "
 					+ hostIsoCountryCode);
 		}
 
 		if (originIsoCountryCode != null
 				&& countryDAO.getCountryForIsoCountryCode(originIsoCountryCode,
 						null) == null) {
 			throw new ServiceException("No country found for origin ISO code "
 					+ originIsoCountryCode);
 		}
 
 		if (originIsoDepartmentCode != null
 				&& departmentDAO
 						.getDepartmentForIsoDepartmentCode(originIsoDepartmentCode) == null) {
 			throw new ServiceException(
 					"No department found for origin ISO code "
 							+ originIsoDepartmentCode);
 		}
 
 		if (originIsoCountyCode != null
 				&& countyDAO.getCountyForIsoCountyCode(originIsoCountyCode) == null) {
 			throw new ServiceException("No county found for origin ISO code "
 					+ originIsoCountyCode);
 		}
 
 		if (complexId != null
 				&& paramoDAO.getParamoForComplexId(complexId) == null) {
 			throw new ServiceException("No paramo found for complex id "
 					+ complexId);
 		}
 
 		if (marineId != null
 				&& marineZoneDAO.getMarineZoneForMask(marineId) == null) {
 			throw new ServiceException("No marine zone found for mask "
 					+ marineId);
 		}
 		
 		if (protectedId != null
 				&& protectedAreaDAO.getProtectedAreaForProtectedArea(protectedId) == null) {
 			throw new ServiceException("No protected area found for Protected area Id "
 					+ protectedId);
 		}
 		
 		if (ecosystem != null
 				&& ecosystemDAO.getEcosystemFor(Long.parseLong(ecosystem)) == null) {
 			throw new ServiceException("No ecosystem found for ecosystem Id "
 					+ ecosystem);
 		}
 		
 		if (zonificacionId != null
 				&& zonificacionDAO.getZonificacionForSZH(zonificacionId) == null) {
 			throw new ServiceException("No zonificacion hidrografica found for Zonificacion Id "
 					+ zonificacionId);
 		}
 		
 		BasisOfRecord basisOfRecord = null;
 		if (basisOfRecordCode != null) {
 			basisOfRecord = BasisOfRecord.getBasisOfRecord(basisOfRecordCode);
 
 			if (basisOfRecord == null) {
 				throw new ServiceException("No basis of record found for code "
 						+ basisOfRecordCode);
 			}
 		}
 
 		Integer cellIdValue = null;
 		if (cellId != null) {
 			cellIdValue = new Integer(cellId);
 
 			// This makes the georeferencedOnly flag redundant
 			georeferencedOnly = false;
 		}
 
 		Float minLongitude = null;
 		Float maxLongitude = null;
 		Float minLatitude = null;
 		Float maxLatitude = null;
 
 		if (boundingBox != null) {
 			minLongitude = boundingBox.getLeft();
 			maxLongitude = boundingBox.getRight();
 			minLatitude = boundingBox.getLower();
 			maxLatitude = boundingBox.getUpper();
 
 			// This makes the georeferencedOnly flag redundant
 			georeferencedOnly = false;
 		}
 
 		Date startDate = null;
 		Date endDate = null;
 
 		if (timePeriod != null) {
 			startDate = timePeriod.getStartPeriod();
 			endDate = timePeriod.getEndPeriod();
 		}
 
 		List<OccurrenceRecord> occurrenceRecords = occurrenceRecordDAO
 				.findOccurrenceRecords(taxonConcept, dataProvider,
 						dataResource, resourceNetwork, scientificName,
 						hostIsoCountryCode, originIsoCountryCode,
 						originIsoDepartmentCode, originIsoCountyCode,
 						complexId, marineId, protectedId, ecosystem,
 						zonificacionId, minLongitude, maxLongitude,
 						minLatitude, maxLatitude, cellIdValue, startDate,
 						endDate, basisOfRecord, modifiedSince,
 						georeferencedOnly, searchConstraints);
 		if (logger.isDebugEnabled())
 			logger.debug("occurrenceRecords: " + occurrenceRecords.size());
 		return occurrenceRecordDTOFactory.createResultsDTO(occurrenceRecords,
 				searchConstraints.getMaxResults());
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#findRawOccurrenceRecord(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public SearchResultsDTO findRawOccurrenceRecord(String dataResourceKey,
 			String catalogueNumber, SearchConstraints searchConstraints) {
 		List<RawOccurrenceRecord> results = rawOccurrenceRecordDAO
 				.findRawOccurrenceRecord(parseKey(dataResourceKey),
 						catalogueNumber, searchConstraints.getStartIndex(),
 						searchConstraints.getMaxResults() + 1);
 		return rawOccurrenceRecordDTOFactory.createResultsDTO(results,
 				searchConstraints.getMaxResults());
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getRawOccurrenceRecordFor(java.lang.String)
 	 */
 	public RawOccurrenceRecordDTO getRawOccurrenceRecordFor(
 			String rawOccurrenceRecordKey) throws ServiceException {
 		Long rawOccurrenceRecordId = parseKey(rawOccurrenceRecordKey);
 		RawOccurrenceRecord rawOccurrenceRecord = rawOccurrenceRecordDAO
 				.getRawOccurrenceRecordFor(rawOccurrenceRecordId);
 		return (RawOccurrenceRecordDTO) rawOccurrenceRecordDTOFactory
 				.createDTO(rawOccurrenceRecord);
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getKmlOccurrenceRecordFor(java.lang.String)
 	 */
 	public KmlOccurrenceRecordDTO getKmlOccurrenceRecordFor(
 			String occurrenceRecordKey) throws ServiceException {
 		Long occurrenceRecordId = parseKey(occurrenceRecordKey);
 		OccurrenceRecord occurrenceRecord = occurrenceRecordDAO
 				.getOccurrenceRecordFor(occurrenceRecordId);
 		return (KmlOccurrenceRecordDTO) kmlOccurrenceRecordDTOFactory
 				.createDTO(occurrenceRecord);
 	}
 
 	/**
 	 * TODO for performance reasons this should be refactored not to query for
 	 * model entities to pass into DAO methods. Rather the DAO methods should be
 	 * passed ids.
 	 * 
 	 * @see org.gbif.portal.service.OccurrenceManager#countOccurrenceRecords(java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String,
 	 *      java.lang.String, org.gbif.portal.dto.util.BoundingBoxDTO,
 	 *      org.gbif.portal.dto.util.TimePeriodDTO,
 	 *      org.gbif.portal.dto.util.SearchConstraints, boolean)
 	 */
 	public int countOccurrenceRecords(String dataProviderKey,
 			String dataResourceKey, String resourceNetworkKey,
 			String taxonConceptKey, String scientificName,
 			String hostIsoCountryCode, String originIsoCountryCode,
 			String originIsoDepartmentCode, String originIsoCountyCode,
 			String paramo, String marineZone, String protectedArea, 
 			String ecosystem, String zonificacion, String basisOfRecordCode,
 			String cellId, BoundingBoxDTO boundingBox,
 			TimePeriodDTO timePeriod, Date modifiedSince,
 			boolean georeferencedOnly) throws ServiceException {
 
 		DataProvider dataProvider = null;
 		if (dataProviderKey != null) {
 			Long dataProviderId = parseKey(dataProviderKey);
 			dataProvider = dataProviderDAO.getDataProviderFor(dataProviderId);
 
 			if (dataProvider == null) {
 				throw new ServiceException("No DataProvider found for key "
 						+ dataProviderKey);
 			}
 		}
 
 		DataResource dataResource = null;
 		if (dataResourceKey != null) {
 			Long dataResourceId = parseKey(dataResourceKey);
 			dataResource = dataResourceDAO.getDataResourceFor(dataResourceId);
 
 			if (dataResource == null) {
 				throw new ServiceException("No DataResource found for key "
 						+ dataResourceKey);
 			}
 		}
 
 		ResourceNetwork resourceNetwork = null;
 		if (resourceNetworkKey != null) {
 			Long resourceNetworkId = parseKey(resourceNetworkKey);
 			resourceNetwork = resourceNetworkDAO
 					.getResourceNetworkFor(resourceNetworkId);
 
 			if (resourceNetwork == null) {
 				throw new ServiceException("No ResourceNetwork found for key "
 						+ resourceNetworkKey);
 			}
 		}
 
 		TaxonConcept taxonConcept = null;
 		if (taxonConceptKey != null) {
 			Long taxonConceptId = parseKey(taxonConceptKey);
 			taxonConcept = taxonConceptDAO.getTaxonConceptFor(taxonConceptId);
 
 			if (taxonConcept == null) {
 				throw new ServiceException("No TaxonConcept found for key "
 						+ taxonConceptKey);
 			}
 
 			// Perform our search using nub concepts
 			if (taxonConcept.getIsNubConcept() != null
 					&& !taxonConcept.getIsNubConcept()
 					&& taxonConcept.getPartnerConcept() != null) {
 				taxonConcept = taxonConceptDAO.getTaxonConceptFor(taxonConcept
 						.getPartnerConceptId());
 			}
 
 		}
 
 		if (hostIsoCountryCode != null
 				&& countryDAO.getCountryForIsoCountryCode(hostIsoCountryCode,
 						null) == null) {
 			throw new ServiceException("No country found for host ISO code "
 					+ hostIsoCountryCode);
 		}
 
 		if (originIsoCountryCode != null
 				&& countryDAO.getCountryForIsoCountryCode(originIsoCountryCode,
 						null) == null) {
 			throw new ServiceException("No country found for origin ISO code "
 					+ originIsoCountryCode);
 		}
 
 		if (originIsoDepartmentCode != null
 				&& departmentDAO
 						.getDepartmentForIsoDepartmentCode(originIsoDepartmentCode) == null) {
 			throw new ServiceException(
 					"No department found for origin ISO code "
 							+ originIsoDepartmentCode);
 		}
 
 		if (originIsoCountyCode != null
 				&& countyDAO.getCountyForIsoCountyCode(originIsoCountyCode) == null) {
 			throw new ServiceException("No county found for origin ISO code "
 					+ originIsoCountyCode);
 		}
 
 		if (paramo != null && paramoDAO.getParamoForComplexId(paramo) == null) {
 			throw new ServiceException("No paramo found for complex id "
 					+ paramo);
 		}
 
 		if (marineZone != null
 				&& marineZoneDAO.getMarineZoneForMask(marineZone) == null) {
 			throw new ServiceException("No marine zone found for mask "
 					+ marineZone);
 		}
 		
 		if (protectedArea != null
 				&& protectedAreaDAO.getProtectedAreaForProtectedArea(protectedArea) == null) {
 			throw new ServiceException("No protected area found for protected area id "
 					+ protectedArea);
 		}
 		
 		if (ecosystem != null
 				&& ecosystemDAO.getEcosystemFor(Long.parseLong(ecosystem)) == null) {
 			throw new ServiceException("No ecosystem found for ecosystem id "
 					+ ecosystem);
 		}
 		
 		if (zonificacion != null
 				&& zonificacionDAO.getZonificacionForSZH(zonificacion) == null) {
 			throw new ServiceException("No zonificacion found for zonificacion hidrografica id "
 					+ zonificacion);
 		}
 		
 		BasisOfRecord basisOfRecord = null;
 		if (basisOfRecordCode != null) {
 			basisOfRecord = BasisOfRecord.getBasisOfRecord(basisOfRecordCode);
 
 			if (basisOfRecord == null) {
 				throw new ServiceException("No basis of record found for code "
 						+ basisOfRecordCode);
 			}
 		}
 
 		Integer cellIdValue = null;
 		if (cellId != null) {
 			cellIdValue = new Integer(cellId);
 
 			// This makes the georeferencedOnly flag redundant
 			georeferencedOnly = false;
 		}
 
 		Float minLongitude = null;
 		Float maxLongitude = null;
 		Float minLatitude = null;
 		Float maxLatitude = null;
 
 		if (boundingBox != null) {
 			minLongitude = boundingBox.getLeft();
 			maxLongitude = boundingBox.getRight();
 			minLatitude = boundingBox.getLower();
 			maxLatitude = boundingBox.getUpper();
 
 			// This makes the georeferencedOnly flag redundant
 			georeferencedOnly = false;
 		}
 
 		Date startDate = null;
 		Date endDate = null;
 
 		if (timePeriod != null) {
 			startDate = timePeriod.getStartPeriod();
 			endDate = timePeriod.getEndPeriod();
 		}
 
 		Long recordCount = occurrenceRecordDAO.countOccurrenceRecords(
 				taxonConcept, dataProvider, dataResource, resourceNetwork,
 				scientificName, hostIsoCountryCode, originIsoCountryCode,
 				originIsoDepartmentCode, originIsoCountyCode, paramo,
 				marineZone, protectedArea,ecosystem, zonificacion,
 				minLongitude, maxLongitude, minLatitude,
 				maxLatitude, cellIdValue, startDate, endDate, basisOfRecord,
 				modifiedSince, georeferencedOnly);
 		if (logger.isDebugEnabled())
 			logger.debug("occurrenceRecords: " + recordCount);
 		return recordCount.intValue();
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getOccurrenceRecordCount()
 	 */
 	public int getTotalOccurrenceRecordCount() throws ServiceException {
		return occurrenceRecordDAO.getTotalOccurrenceRecordCount()
				- occurrenceRecordDAO
						.getTotalOccurrenceRecordCountForDeletedProviders();
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getTotalSpeciesCount()
 	 */
 	public int getTotalSpeciesCount() throws ServiceException {
 		return occurrenceRecordDAO.getTotalSpeciesCount();
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#getTotalGeoreferencedOccurrenceRecordCount()
 	 **/
 	public int getTotalGeoreferencedOccurrenceRecordCount()
 			throws ServiceException {
 		return occurrenceRecordDAO.getTotalGeoreferencedOccurrenceRecordCount();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.gbif.portal.service.OccurrenceManager#
 	 * getIdentifierRecordsForOccurrenceRecord(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<IdentifierRecordDTO> getIdentifierRecordsForOccurrenceRecord(
 			String occurrenceRecordKey) throws ServiceException {
 		return (List<IdentifierRecordDTO>) identifierRecordDTOFactory
 				.createDTOList(identifierRecordDAO
 						.getIdentifierRecordsForOccurrenceRecord(parseKey(occurrenceRecordKey)));
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<ImageRecordDTO> getImageRecordsForOccurrenceRecord(
 			String occurrenceRecordKey) throws ServiceException {
 		return (List<ImageRecordDTO>) imageRecordDTOFactory
 				.createDTOList(imageRecordDAO
 						.getImageRecordsForOccurrenceRecord(parseKey(occurrenceRecordKey)));
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<LinkRecordDTO> getLinkRecordsForOccurrenceRecord(
 			String occurrenceRecordKey) throws ServiceException {
 		return (List<LinkRecordDTO>) linkRecordDTOFactory
 				.createDTOList(linkRecordDAO
 						.getLinkRecordsForOccurrenceRecord(parseKey(occurrenceRecordKey)));
 	}
 
 	/**
 	 * @see org.gbif.portal.service.OccurrenceManager#isValidOccurrenceRecordKey(java.lang.String)
 	 */
 	public boolean isValidOccurrenceRecordKey(String occurrenceRecordKey) {
 		return parseKey(occurrenceRecordKey) != null;
 	}
 
 	/**
 	 * Parses the supplied key. Returns null if supplied string invalid
 	 * 
 	 * @param key
 	 * @return a concept key. Returns null if supplied string invalid key
 	 */
 	protected static Long parseKey(String key) {
 		Long parsedKey = null;
 		try {
 			parsedKey = Long.parseLong(key);
 		} catch (NumberFormatException e) {
 			// expected behaviour for invalid keys
 		}
 		return parsedKey;
 	}
 
 	/**
 	 * @param countryDAO
 	 *            the countryDAO to set
 	 */
 	public void setCountryDAO(CountryDAO countryDAO) {
 		this.countryDAO = countryDAO;
 	}
 
 	/**
 	 * @param dataProviderDAO
 	 *            the dataProviderDAO to set
 	 */
 	public void setDataProviderDAO(DataProviderDAO dataProviderDAO) {
 		this.dataProviderDAO = dataProviderDAO;
 	}
 
 	/**
 	 * @param dataResourceDAO
 	 *            the dataResourceDAO to set
 	 */
 	public void setDataResourceDAO(DataResourceDAO dataResourceDAO) {
 		this.dataResourceDAO = dataResourceDAO;
 	}
 
 	/**
 	 * @param resourceNetworkDAO
 	 *            the resourceNetworkDAO to set
 	 */
 	public void setResourceNetworkDAO(ResourceNetworkDAO resourceNetworkDAO) {
 		this.resourceNetworkDAO = resourceNetworkDAO;
 	}
 
 	/**
 	 * @param taxonConceptDAO
 	 *            the taxonConceptDAO to set
 	 */
 	public void setTaxonConceptDAO(TaxonConceptDAO taxonConceptDAO) {
 		this.taxonConceptDAO = taxonConceptDAO;
 	}
 
 	/**
 	 * @param identifierRecordDAO
 	 *            the identifierRecordDAO to set
 	 */
 	public void setIdentifierRecordDAO(IdentifierRecordDAO identifierRecordDAO) {
 		this.identifierRecordDAO = identifierRecordDAO;
 	}
 
 	/**
 	 * @param imageRecordDAO
 	 *            the imageRecordDAO to set
 	 */
 	public void setImageRecordDAO(ImageRecordDAO imageRecordDAO) {
 		this.imageRecordDAO = imageRecordDAO;
 	}
 
 	/**
 	 * @param linkRecordDAO
 	 *            the linkRecordDAO to set
 	 */
 	public void setLinkRecordDAO(LinkRecordDAO linkRecordDAO) {
 		this.linkRecordDAO = linkRecordDAO;
 	}
 
 	/**
 	 * @param occurrenceRecordDAO
 	 *            the occurrenceRecordDAO to set
 	 */
 	public void setOccurrenceRecordDAO(OccurrenceRecordDAO occurrenceRecordDAO) {
 		this.occurrenceRecordDAO = occurrenceRecordDAO;
 	}
 
 	/**
 	 * @param briefOccurrenceRecordDTOFactory
 	 *            the briefOccurrenceRecordDTOFactory to set
 	 */
 	public void setBriefOccurrenceRecordDTOFactory(
 			DTOFactory briefOccurrenceRecordDTOFactory) {
 		this.briefOccurrenceRecordDTOFactory = briefOccurrenceRecordDTOFactory;
 	}
 
 	/**
 	 * @param occurrenceRecordDTOFactory
 	 *            the occurrenceRecordDTOFactory to set
 	 */
 	public void setOccurrenceRecordDTOFactory(
 			DTOFactory occurrenceRecordDTOFactory) {
 		this.occurrenceRecordDTOFactory = occurrenceRecordDTOFactory;
 	}
 
 	/**
 	 * @param rawOccurrenceRecordDAO
 	 *            the rawOccurrenceRecordDAO to set
 	 */
 	public void setRawOccurrenceRecordDAO(
 			RawOccurrenceRecordDAO rawOccurrenceRecordDAO) {
 		this.rawOccurrenceRecordDAO = rawOccurrenceRecordDAO;
 	}
 
 	/**
 	 * @param rawOccurrenceRecordDTOFactory
 	 *            the rawOccurrenceRecordDTOFactory to set
 	 */
 	public void setRawOccurrenceRecordDTOFactory(
 			DTOFactory rawOccurrenceRecordDTOFactory) {
 		this.rawOccurrenceRecordDTOFactory = rawOccurrenceRecordDTOFactory;
 	}
 
 	/**
 	 * @param identifierRecordDTOFactory
 	 *            the identifierRecordDTOFactory to set
 	 */
 	public void setIdentifierRecordDTOFactory(
 			DTOFactory identifierRecordDTOFactory) {
 		this.identifierRecordDTOFactory = identifierRecordDTOFactory;
 	}
 
 	/**
 	 * @param imageRecordDTOFactory
 	 *            the imageRecordDTOFactory to set
 	 */
 	public void setImageRecordDTOFactory(DTOFactory imageRecordDTOFactory) {
 		this.imageRecordDTOFactory = imageRecordDTOFactory;
 	}
 
 	/**
 	 * @return the extendedOccurrenceRecordDTOFactory
 	 */
 	public DTOFactory getExtendedOccurrenceRecordDTOFactory() {
 		return extendedOccurrenceRecordDTOFactory;
 	}
 
 	/**
 	 * @param extendedOccurrenceRecordDTOFactory
 	 *            the extendedOccurrenceRecordDTOFactory to set
 	 */
 	public void setExtendedOccurrenceRecordDTOFactory(
 			DTOFactory extendedOccurrenceRecordDTOFactory) {
 		this.extendedOccurrenceRecordDTOFactory = extendedOccurrenceRecordDTOFactory;
 	}
 
 	/**
 	 * @param linkRecordDTOFactory
 	 *            the linkRecordDTOFactory to set
 	 */
 	public void setLinkRecordDTOFactory(DTOFactory linkRecordDTOFactory) {
 		this.linkRecordDTOFactory = linkRecordDTOFactory;
 	}
 
 	/**
 	 * @param typificationRecordDTOFactory
 	 *            the typificationRecordDTOFactory to set
 	 */
 	public void setTypificationRecordDTOFactory(
 			DTOFactory typificationRecordDTOFactory) {
 		this.typificationRecordDTOFactory = typificationRecordDTOFactory;
 	}
 
 	/**
 	 * @param typificationRecordDAO
 	 *            the typificationRecordDAO to set
 	 */
 	public void setTypificationRecordDAO(
 			TypificationRecordDAO typificationRecordDAO) {
 		this.typificationRecordDAO = typificationRecordDAO;
 	}
 
 	/**
 	 * @param kmlOccurrenceRecordDTOFactory
 	 *            the kmlOccurrenceRecordDTOFactory to set
 	 */
 	public void setKmlOccurrenceRecordDTOFactory(
 			DTOFactory kmlOccurrenceRecordDTOFactory) {
 		this.kmlOccurrenceRecordDTOFactory = kmlOccurrenceRecordDTOFactory;
 	}
 
 }
