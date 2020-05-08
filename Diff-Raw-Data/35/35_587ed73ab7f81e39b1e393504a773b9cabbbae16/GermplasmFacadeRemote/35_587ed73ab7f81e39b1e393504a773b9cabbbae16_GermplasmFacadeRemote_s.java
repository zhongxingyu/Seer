 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.inbio.ara.facade.germplasm;
 
 import java.util.List;
 import javax.ejb.Remote;
 import org.inbio.ara.dto.germplasm.AccessionDTO;
 import org.inbio.ara.dto.germplasm.AccessionMovementDTO;
 import org.inbio.ara.dto.germplasm.PassportDTO;
 import org.inbio.ara.dto.inventory.PersonDTO;
 import org.inbio.ara.dto.inventory.SelectionListDTO;
 import org.inbio.ara.dto.inventory.TaxonDTO;
 import org.inbio.ara.dto.security.NomenclaturalGroupDTO;
 
 /**
  *
  * @author dasolano
  */
 @Remote
 public interface GermplasmFacadeRemote {
 
     /**
      * Return all elements for a selection list id
      * @param selectionListId
      * @return
      */
     //cambiar <<---------------
     public List<SelectionListDTO>getElementsForSelectionList(long selectionListId);
 
 
 
 
     /**
      * Return a list of nomenclatural groups for a taxon id
      * @param taxonId
      * @return
      */
     public List<NomenclaturalGroupDTO> getNomenclaturalGroupsByTaxon(Long taxonId);
 
 
     /**
      * Save a Passport
      * @param passportDTO
      */
     public void savePassport(PassportDTO passportDTO, Long[] selectedNomenclaturalGroups);
 
     /**
      * Get all donor persons
      * @return
      */
     public List<PersonDTO> getDonorPersons();
 
     /**
      * Get All taxons for a collection id
      * @param collectionId
      * @return
      */
     public List<TaxonDTO> getAllTaxonsByCollectionIdAndTaxonomicalRangeId(Long collectionId, Long taxonomicalRangeId);
 
     /**
      * Get a passport list paginated
      * @param firstResult
      * @param lastResult
      * @return
      */
     public List<PassportDTO> getPassportListPaginated(int firstResult, int maxResults);
 
     /**
      * Return the quantity of passports
      * @return
      */
     public Long countPassport();
 
     /**
      * Update a passport
      * @param passportDTO
      */
     public void updatePassport(PassportDTO passportDTO, Long[] selectedNomenclaturalGroups);
 
     /**
      * Get the taxonomical level of a taxon
      * @param taxonId
      * @return
      */
     public Long getTaxonomicalLevelForTaxon(Long taxonId);
 
     /**
      * Get a passport list for a simple search with lot of elements
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<PassportDTO> getPassportSimpleSearch(String query, Long collectionId, int firstResult, int maxResult);
 
     /**
      * Get a list of passport for the advanced search
      * @param passportDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<PassportDTO> getPassportAdvancedSearch(PassportDTO passportDTO, Long collectionId, int firstResult, int maxResult);
 
     /**
      * Count the quantity of passports for the advanced search
      * @param passportDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countPassportAdvancedSearch(PassportDTO passportDTO, Long collectionId);
 
     /**
      * Count the quantity of passports for a simple search
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countPassportSimpleSearch(String query, Long collectionId);
 
 
 
     /**
      * Count the quantity of accessions for the advanced search
      * @param passportDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countAccessionAdvancedSearch(AccessionDTO accessionDTO, Long collectionId);
 
     /**
      * Count the quantity of accessions for a simple search
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countAccessionSimpleSearch(String query, Long collectionId);
 
 
 
     /**
      * Get a accessions list paginated
      * @param firstResult
      * @param lastResult
      * @return
      */
     public List<AccessionDTO> getAccessionListPaginated(int firstResult, int maxResults);
 
     /**
      * Return the quantity of accessions
      * @return
      */
     public Long countAccessions();
 
 
     /**
      * Get a passport list for a simple search with lot of elements
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<AccessionDTO> getAccessionSimpleSearch(String query, Long collectionId, int firstResult, int maxResult);
 
     /**
      * Get a list of accession for the advanced search
      * @param accessionDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<AccessionDTO> getAccessionAdvancedSearch(AccessionDTO accessionDTO, Long collectionId, int firstResult, int maxResult);
 
     /**
      * Save an Accession
      * @param accessionDTO
      */
     public Long saveAccession(AccessionDTO accessionDTO)throws IllegalArgumentException;
 
     /**
      * Update an Accession
      * @param accessionDTO
      */
    public void updateAccession(AccessionDTO accessionDTO);
 
 
 
 
 
     /**
      * Get all donor persons
      * @return
      */
     public List<PersonDTO> getResponsablePersons();
 
     public void saveAccessionMovement(AccessionMovementDTO accessionMovementDTO);
 
     public void updateAccessionMovement(AccessionMovementDTO accessionMovementDTO);
 
     public void deleteAccessionMovement(AccessionMovementDTO accessionMovementDTO);
     
     public List<AccessionMovementDTO> getAllAccessionMovementByAccesionIdPaginated(Long accessionId, int firstResult, int maxResult);
 
     public Long countAccessionMovements(Long accessionId);
 
     public List<AccessionMovementDTO> getAccessionMovementsSimpleSearch(String query, Long collectionId, int firstResult, int maxResult);
 
     public List<AccessionMovementDTO> getAccessionMovementsAdvancedSearch(AccessionMovementDTO accessionMovement, Long collectionId, int firstResult, int maxResult);
 
     public Long countAccessionMovementsSimpleSearch(String query, Long collectionId);
 
     public Long countAccessionMovementsAdvancedSearch(AccessionMovementDTO accessionMovement, Long collectionId);
 
 
 }
