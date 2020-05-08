 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.inbio.ara.facade.germplasm.impl;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.ejb.EJB;
 import org.inbio.ara.dto.germplasm.AccessionMovementDTO;
 import org.inbio.ara.dto.germplasm.PassportDTO;
 import org.inbio.ara.dto.inventory.PersonDTO;
 import org.inbio.ara.dto.inventory.SelectionListDTO;
 import org.inbio.ara.dto.inventory.TaxonDTO;
 import org.inbio.ara.dto.security.NomenclaturalGroupDTO;
 import org.inbio.ara.facade.germplasm.*;
 import javax.ejb.Stateless;
 import org.inbio.ara.dto.germplasm.AccessionDTO;
 import org.inbio.ara.dto.germplasm.AccessionDTOFactory;
 import org.inbio.ara.dto.germplasm.AccessionMovementDTOFactory;
 import org.inbio.ara.dto.germplasm.PassportDTOFactory;
 import org.inbio.ara.dto.germplasm.PassportNomenclaturalGroupDTO;
 import org.inbio.ara.dto.germplasm.PassportNomenclaturalGroupDTOFactory;
 import org.inbio.ara.dto.inventory.PersonDTOFactory;
 import org.inbio.ara.dto.inventory.SelectionListDTOFactory;
 import org.inbio.ara.dto.inventory.SelectionListEntity;
 import org.inbio.ara.dto.inventory.TaxonDTOFactory;
 import org.inbio.ara.dto.security.NomenclaturalGroupDTOFactory;
 import org.inbio.ara.eao.agent.InstitutionEAOLocal;
 import org.inbio.ara.eao.agent.PersonEAOLocal;
 import org.inbio.ara.eao.germplasm.AccessionEAOLocal;
 import org.inbio.ara.eao.germplasm.AccessionMovementEAOLocal;
 import org.inbio.ara.eao.germplasm.PassportEAOLocal;
 import org.inbio.ara.eao.germplasm.PassportNomenclaturalGroupEAOLocal;
 import org.inbio.ara.eao.security.NomenclaturalGroupEAOLocal;
 import org.inbio.ara.eao.selectionlist.SelectionListValueLocalEAO;
 import org.inbio.ara.eao.taxonomy.TaxonEAOLocal;
 import org.inbio.ara.eao.taxonomy.TaxonomicalRangeEAOLocal;
 import org.inbio.ara.facade.search.SearchFacadeRemote;
 import org.inbio.ara.persistence.SelectionListGenericEntity;
 import org.inbio.ara.persistence.germplasm.Accession;
 import org.inbio.ara.persistence.germplasm.AccessionMovement;
 import org.inbio.ara.persistence.germplasm.Passport;
 import org.inbio.ara.persistence.institution.Institution;
 import org.inbio.ara.persistence.person.Person;
 import org.inbio.ara.persistence.person.ProfileEntity;
 import org.inbio.ara.persistence.taxonomy.NomenclaturalGroup;
 import org.inbio.ara.persistence.taxonomy.Taxon;
 
 /**
  *
  * @author dasolano
  */
 @Stateless
 public class GermplasmFacadeImpl implements GermplasmFacadeRemote {
 
 
     /**
      * EAO
      */
     @EJB
     private SelectionListValueLocalEAO selectionListValueLocalEAO;
 
     @EJB
     private PersonEAOLocal personEAOLocal;
 
     @EJB
     private NomenclaturalGroupEAOLocal nomenclaturalGroupEAOLocal;
 
     @EJB
     private PassportEAOLocal passportEAOLocal;
 
     @EJB
     private TaxonEAOLocal taxonEAOLocal;
 
     @EJB
     private InstitutionEAOLocal institutionEAOLocal;
 
     @EJB
     private PassportNomenclaturalGroupEAOLocal passportNomenclaturalGroupEAOLocal;
 
     @EJB
     private TaxonomicalRangeEAOLocal taxonomicalRangeEAOLocal;
 
     @EJB
     private AccessionEAOLocal accessionEAOLocal;
 
     @EJB
     private AccessionMovementEAOLocal accessionMovementEAOLocal;
 
     
 
     /**
      * Factory
      */
     private SelectionListDTOFactory selecionListDTOFactory = new SelectionListDTOFactory();
     private PersonDTOFactory personDTOFactory = new PersonDTOFactory();
     private NomenclaturalGroupDTOFactory nomenclaturalGroupDTOFactory = new NomenclaturalGroupDTOFactory();
     private TaxonDTOFactory taxonDTOFactory = new TaxonDTOFactory();
 
     private PassportDTOFactory passportDTOFactory = new PassportDTOFactory();
     private PassportNomenclaturalGroupDTOFactory passportNomenclaturalGroupDTOFactory =
             new PassportNomenclaturalGroupDTOFactory();
 
     private AccessionDTOFactory accessionDTOFactory = new AccessionDTOFactory();
     private AccessionMovementDTOFactory accessionMovementDTOFactory = new AccessionMovementDTOFactory();
 
     /**
      * Return all the SelectionList values for a Selection List id
      * @param selectionListId
      * @return List<SelectionListDTO>
      */
     public List<SelectionListDTO> getElementsForSelectionList(long selectionListId) {
         return selecionListDTOFactory.createDTOList(selectionListValueLocalEAO.findAll(selectionListId));
     }
 
 
     /**
      * @return the personEAOLocal
      */
     public PersonEAOLocal getPersonEAOLocal() {
         return personEAOLocal;
     }
 
     /**
      * Return a list of nomenclatural groups for a taxon id
      * @param taxonId
      * @return
      */
     public List<NomenclaturalGroupDTO> getNomenclaturalGroupsByTaxon(Long taxonId) {
         return nomenclaturalGroupDTOFactory.createDTOList(
                 nomenclaturalGroupEAOLocal.getNomenclaturalGroupListByTaxon(taxonId));
     }
 
     public void savePassport(PassportDTO passportDTO,
             Long[] selectedNomenclaturalGroups)
     {
         Passport passport = passportDTOFactory.createPlainEntity(passportDTO);
         passportEAOLocal.create(passport);
         savePassportNomenclaturalGroups(passport.getPassportId(),
                 passportDTO.getUserName(),
                 selectedNomenclaturalGroups);
     }
 
     /**
      * Create the nomenclatural groups selected for a passport
      * @param passportId
      * @param userName
      * @param passportNomenclaturalGroupDTOs
      */
     private void savePassportNomenclaturalGroups(Long passportId, String userName,
             Long[] passportNomenclaturalGroupDTOs)
     {
         if(passportNomenclaturalGroupDTOs != null &&
                 passportNomenclaturalGroupDTOs.length > 0)
         {
             PassportNomenclaturalGroupDTO passportNomenclaturalGroupDTO;
             for (int i = 0; i < passportNomenclaturalGroupDTOs.length; i++)
             {
                 passportNomenclaturalGroupDTO =
                     new PassportNomenclaturalGroupDTO();
 
                 passportNomenclaturalGroupDTO.setNomenclaturalGroupId(
                         passportNomenclaturalGroupDTOs[i]);
                 passportNomenclaturalGroupDTO.setPassportId(passportId);
                 passportNomenclaturalGroupDTO.setUserName(userName);
 
                 passportNomenclaturalGroupEAOLocal.
                         create(
                         passportNomenclaturalGroupDTOFactory.
                         createPlainEntity(passportNomenclaturalGroupDTO));
             }
         }
     }
 
     /**
      * Retrieve all donor persons
      * @return
      */
     public List<PersonDTO> getDonorPersons() {
         return personDTOFactory.createDTOList(personEAOLocal.
                 findByProfile(ProfileEntity.DONOR_PERSON_PROFILE.getId()));
     }
 
     /**
      * Get all taxons for a collection Id
      * @param collectionId
      * @return
      */
     public List<TaxonDTO> getAllTaxonsByCollectionIdAndTaxonomicalRangeId(Long collectionId, Long taxonomicalRangeId) {
         return taxonDTOFactory.createDTOList(taxonEAOLocal.getTaxonsByCollectionIdAndTaxonomicalRangeId(collectionId,taxonomicalRangeId));
     }
 
     /**
      * Get Passport list paginated
      * @param firstResult
      * @param maxResults
      * @return
      */
     public List<PassportDTO> getPassportListPaginated(int firstResult, int maxResults) {
         String[] parts = {"passportId"};
         List<PassportDTO> listPassport =  passportDTOFactory.createDTOList(
                 //passportEAOLocal.findAllPaginated(Passport.class, firstResult, maxResults));
                 passportEAOLocal.findAllPaginatedFilterAndOrderBy(
                 Passport.class, firstResult, maxResults, parts, null));
         
         updatePassportDTOListValues(listPassport);
         /*for (PassportDTO passportDTO : listPassport) {
             updatePassportDTOValues(passportDTO);
         }*/
         return listPassport;
     }
 
     /**
      * Update the content of each passport in a list
      * @param passportDTOList
      * @return
      */
     private List<PassportDTO> updatePassportDTOListValues(List<PassportDTO> passportDTOList)
     {
         for (PassportDTO passportDTO : passportDTOList) {
             updatePassportDTOValues(passportDTO);
         }
         return passportDTOList;
     }
 
     /**
      * Update the content of a passportDTO
      * @param passportDTO
      * @return
      */
     private PassportDTO updatePassportDTOValues(PassportDTO passportDTO)
     {
         if(passportDTO.getDonorPersonId() != null)
         {
             passportDTO.setPerson(personEAOLocal.
                     findById(
                     Person.class, passportDTO.getDonorPersonId()).
                     getNaturalFullName());
         }
         if(passportDTO.getDonorInstitutionId() != null)
         {
             passportDTO.setInstitution(institutionEAOLocal.
                 findById(
                 Institution.class, passportDTO.getDonorInstitutionId()).
                 getName());
         }
         if(passportDTO.getMaterialTypeId() != null)
         {
             passportDTO.setMaterialType(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.MATERIAL_TYPE.getId(),
                 passportDTO.getMaterialTypeId()).getName());
         }
         if(passportDTO.getSampleStatusId() != null)
         {
             passportDTO.setSampleStatus(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.SAMPLE_STATUS.getId(),
                 passportDTO.getSampleStatusId()).getName());
         }
         if(passportDTO.getGatheringSourceId() != null)
         {
             passportDTO.setGatheringSource(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.GATHERING_SOURCE.getId(),
                 passportDTO.getGatheringSourceId()).getName());
         }
         if(passportDTO.getSoilColorId() != null)
         {
             passportDTO.setSoilColor(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.SOIL_COLOR.getId(),
                 passportDTO.getSoilColorId()).getName());
         }
         if(passportDTO.getSoilTextureId() != null)
         {
             passportDTO.setSoilTexture(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.SOIL_TEXTURE.getId(),
                 passportDTO.getSoilTextureId()).getName());
         }
         if(passportDTO.getCultivationPracticeId() != null)
         {
             passportDTO.setCultivationPractice(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.CULTIVATION_PRACTICE.getId(),
                 passportDTO.getCultivationPracticeId()).getName());
         }
         if(passportDTO.getCropSystemId() != null)
         {
             passportDTO.setCropSystem(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.CROP_SYSTEM.getId(),
                 passportDTO.getCropSystemId()).getName());
         }
         if(passportDTO.getCropTypeId() != null)
         {
             passportDTO.setCropType(selectionListValueLocalEAO.
                 findById(
                 SelectionListEntity.CROP_TYPE.getId(),
                 passportDTO.getCropTypeId()).getName());
         }
 
         //load the nomenclatural groups associated
         passportDTO.setPassportNomenclaturalGroupList(
                 getPassportNomenclaturalGroups(passportDTO.getPassportId()));
 
         return passportDTO;
     }
 
     /**
      *
      * @param passportId
      * @return
      */
     private List<PassportNomenclaturalGroupDTO> getPassportNomenclaturalGroups
             (Long passportId)
     {
         List<PassportNomenclaturalGroupDTO> listPNGDTO =
                 passportNomenclaturalGroupDTOFactory.createDTOList(
                 passportNomenclaturalGroupEAOLocal.getAllByPassportId(passportId));
         return updateNomenclaturalGroupsforPassport(listPNGDTO);
     }
 
     /**
      * Set the name to each PassportNomenclaturalGroupDTO of the list
      * @param listPNGDTO
      * @return
      */
     private List<PassportNomenclaturalGroupDTO> updateNomenclaturalGroupsforPassport(
             List<PassportNomenclaturalGroupDTO> listPNGDTO)
     {
         if(listPNGDTO != null && !listPNGDTO.isEmpty())
         {
             NomenclaturalGroup ng;
             for (PassportNomenclaturalGroupDTO passportNomenclaturalGroupDTO : listPNGDTO)
             {
                 ng = nomenclaturalGroupEAOLocal.findById(
                         NomenclaturalGroup.class,
                         passportNomenclaturalGroupDTO.getNomenclaturalGroupId());
 
                 //set the name to the PNG
                 passportNomenclaturalGroupDTO.setNomenclaturalGroupName(
                         ng.getName());
 
             }
             return listPNGDTO;
         }
         else
             return new ArrayList<PassportNomenclaturalGroupDTO>();
 
     }
 
     /**
      * Return the quantity of passports
      * @return
      */
     public Long countPassport() {
         return passportEAOLocal.count(Passport.class);
     }
 
     /**
      * Update a passport
      * @param passportDTO
      */
     public void updatePassport(PassportDTO passportDTO, Long[] selectedNomenclaturalGroups) {
         Passport passport = passportEAOLocal.findById(Passport.class, passportDTO.getPassportId());
 
         passport = passportDTOFactory.updatePlainEntity(passportDTO, passport);
 
         passportEAOLocal.update(passport);
 
         updatenomenclaturalGroupsForPassport(passport.getPassportId(),
                 passportDTO.getUserName(),
                 selectedNomenclaturalGroups);
     }
 
     /**
      * Update the nomenclatural groups for a passport
      * @param passportId
      * @param userName
      * @param selectedNomenclaturalGroups
      */
     private void updatenomenclaturalGroupsForPassport(Long passportId,
             String userName,
             Long[] selectedNomenclaturalGroups)
     {
         passportNomenclaturalGroupEAOLocal.deleteByPassportId(passportId);
 
         if(selectedNomenclaturalGroups != null &&
                 selectedNomenclaturalGroups.length > 0)
         {
             PassportNomenclaturalGroupDTO passportNomenclaturalGroupDTO;
             for (int i = 0; i < selectedNomenclaturalGroups.length; i++)
             {
                 passportNomenclaturalGroupDTO =
                     new PassportNomenclaturalGroupDTO();
 
                 passportNomenclaturalGroupDTO.setNomenclaturalGroupId(
                         selectedNomenclaturalGroups[i]);
                 passportNomenclaturalGroupDTO.setPassportId(passportId);
                 passportNomenclaturalGroupDTO.setUserName(userName);
 
                 passportNomenclaturalGroupEAOLocal.
                         create(
                         passportNomenclaturalGroupDTOFactory.
                         createPlainEntity(passportNomenclaturalGroupDTO));
             }
         }
 
     }
 
     /**
      * Get the taxnomomical range where a taxon id belong
      * @param taxonId
      * @return Long
      */
     public Long getTaxonomicalLevelForTaxon(Long taxonId) {
         if(taxonId != null)
         {
             Taxon taxon = taxonEAOLocal.findById(Taxon.class, taxonId);
             return taxon.getTaxonomicalRangeId();
         }
         else
             return null;
     }
 
     /**
      * Coun the quantity of passports for a simple search
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countPassportSimpleSearch(String query, Long collectionId)
     {
         Integer quantity = new Integer(unstructeredPassportQuery(splitQuery(query)).size());
         return quantity.longValue();
     }
 
     /**
      * Get a passport list for a simple search. Search by passportId, material type,
      * gatheringId and mission number
      * @param query
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<PassportDTO> getPassportSimpleSearch(String query, Long collectionId, int firstResult, int maxResult)
     {
         Set<Long> passportIds = unstructeredPassportQuery(splitQuery(query));
 
         //Retrieve entities
         List<Passport> passportList = getEntities(passportIds,
                 Passport.class, firstResult, maxResult);
 
         return updatePassportDTOListValues(passportDTOFactory.createDTOList(passportList));
     }
 
     /**
      * Search passports id for each part if the query string. It search for
      * passport Id, material type, mission number, and gathering id
      * @param parts
      * @return
      */
     private Set<Long> unstructeredPassportQuery(String[] parts)
     {
         Set<Long> passportList = new HashSet();
         Long id = null;
         List<Long> passportsId = null;
         Passport entity = null;
 
         //get All material types
         List<SelectionListGenericEntity> slge =
                 selectionListValueLocalEAO.findAll(
                 SelectionListEntity.MATERIAL_TYPE.getId());
         Long slId = null;
 
         for (int i = 0; i < parts.length; i++)
         {
             id = getSeleccionListByName(slge, parts[i]);
 
 
             if(id != null)
             {
                 passportsId = passportEAOLocal.findByMaterialTypeId(id);
                 if(passportsId != null && !passportsId.isEmpty())
                     passportList.addAll(passportsId);
             }
 
             //try to cast it
             try
             {
                 id = Long.parseLong(parts[i]);
 
                 //find by passport Id
                 entity = passportEAOLocal.findById(Passport.class, id);
                 if(entity != null)
                     passportList.add(entity.getPassportId());
 
                 //find by gathering id
                 passportsId = passportEAOLocal.findByGatheringId(id);
                 if(passportsId != null && !passportsId.isEmpty())
                     passportList.addAll(passportsId);
 
                 //find by mission number
                 passportsId = passportEAOLocal.findByMissionNumber(id);
                 if(passportsId != null && !passportsId.isEmpty())
                     passportList.addAll(passportsId);
 
                 
             }
             catch(Exception e){}            
         }
         return passportList;
     }
 
     /**
      * ESTE METODO FUE CREADO CON EL FIN DE NO MODIFICAR LA EAO DE LAS
      * LISTAS DE SELECCION
      * Metodo que se encarga de buscar el id de una lista de seleccion
      * dado un nombre dado y un listado de listas de seleccion
      * @param slge
      * @param name
      * @return
      */
     private Long getSeleccionListByName(List<SelectionListGenericEntity> slge, String name)
     {
         Long id = null;
         for (SelectionListGenericEntity selectionListGenericEntity : slge)
         {
             if(selectionListGenericEntity.getName().equalsIgnoreCase(name))
             {
                 id = selectionListGenericEntity.getId();
             }
         }
         return id;
     }
 
 
   
 
     /**
      * Split the query
      * @param query
      * @return
      */
     private String[] splitQuery(String query)
     {
             if(query == null || query.length() == 0)
                 return null;
             return query.split(" ");
     }
 
     /**
      * Get the entities for a list of Longs
      * @param ids
      * @param t
      * @param base
      * @param offset
      * @return
      */
     private List getEntities(Set<Long> ids, Class t, int base, int offset)
     {
         List entitiesList = new ArrayList();
         Object[] sortedIdentificationIds = ids.toArray();
         java.util.Arrays.sort(sortedIdentificationIds);
         int entitiesCounter = 0;
         int baseCounter = 0;
         if(sortedIdentificationIds.length > base)
         {
             for (Object id : sortedIdentificationIds)
             {
                 if (baseCounter < base)
                 {
                     baseCounter++;
                 } 
                 else if(entitiesCounter < offset)
                 {
                     if(t == Passport.class)
                     {
                         entitiesList.add(passportEAOLocal.
                             findById(Passport.class, (Long)id));
                     }
                     else if(t == Accession.class)
                     {
                         entitiesList.add(accessionEAOLocal.
                             findById(Accession.class, (Long)id));
                     }
                     
                     entitiesCounter++;
                 }
 
             }
         }
         return entitiesList;
     }
 
     /**
      * Count the quantity of passports for the advanced search
      * @param passportDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public Long countPassportAdvancedSearch(PassportDTO passportDTO, Long collectionId)
     {
         Integer quantity = new Integer(getPassportsByCriteria(passportDTO).size());
         return quantity.longValue();
     }
 
     /**
      * Get a list of passport for the advanced search
      * @param passportDTO
      * @param collectionId
      * @param firstResult
      * @param maxResult
      * @return
      */
     public List<PassportDTO> getPassportAdvancedSearch(PassportDTO passportDTO, Long collectionId, int firstResult, int maxResult) {
         Set<Long> passportIds = getPassportsByCriteria(passportDTO);
 
         //Retrieve entities
         List<Passport> passportList = getEntities(passportIds,
                 Passport.class, firstResult, maxResult);
 
         return updatePassportDTOListValues(passportDTOFactory.createDTOList(passportList));
     }
 
 
     /**
      * get a Set of passport by criteria
      * @param passportDTO
      * @return
      */
     private Set<Long> getPassportsByCriteria(PassportDTO passportDTO)
     {
         Set<Long> ids = new HashSet();
         boolean firstTime = true;
 
         List<Long> query = new ArrayList<Long>();
         Passport queryPassport = null;
 
         //find by passport id
         if(passportDTO.getPassportId() != null)
         {
             queryPassport = passportEAOLocal.
                     findById(Passport.class, passportDTO.getPassportId());
             if(queryPassport != null)
             {
                 ids.add(queryPassport.getPassportId());
                 firstTime = false;
             }
         }
 
         //find by donor person
         if(passportDTO.getDonorPersonId() != null)
         {
             query = passportEAOLocal.
                     findByDonorPersonId(passportDTO.getDonorPersonId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by donor institution
         if(passportDTO.getDonorInstitutionId() != null)
         {
             query = passportEAOLocal.
                     findByDonorInstitutionId(passportDTO.getDonorInstitutionId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by gathering id
         if(passportDTO.getGatheringId() != null)
         {
             query = passportEAOLocal.
                     findByGatheringId(passportDTO.getGatheringId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         
         //find by material type
         if(passportDTO.getMaterialTypeId() != null)
         {
             query = passportEAOLocal.
                     findByMaterialTypeId(passportDTO.getMaterialTypeId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         
         //find by mission number
         if(passportDTO.getMissionNumber() != null)
         {
             query = passportEAOLocal.
                     findByMissionNumber(passportDTO.getMissionNumber());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         
         //find by plant nursery date
         if(passportDTO.getPlantNurseryDate() != null)
         {
             query = passportEAOLocal.
                     findByPlantNurseryDate(passportDTO.getPlantNurseryDate());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         
         //find by planting season date
         if(passportDTO.getPlantingSeasonDate() != null)
         {
             query = passportEAOLocal.
                     findByPlantingSeasonDate(passportDTO.getPlantingSeasonDate());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         
         //find by harvesting season date
         if(passportDTO.getHarvestingSeasonDate() != null)
         {
             query = passportEAOLocal.
                     findByHarvestingSeasonDate(passportDTO.getHarvestingSeasonDate());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by sample status
         if(passportDTO.getSampleStatusId() != null)
         {
             query = passportEAOLocal.
                     findBySampleStatusId(passportDTO.getSampleStatusId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         return ids;
     }
 
 
 
     public Long countAccessionAdvancedSearch(AccessionDTO accessionDTO, Long collectionId)
     {
         Integer quantity = new Integer(getAccessionByCriteria(accessionDTO).size());
         return quantity.longValue();
     }
 
     public Long countAccessionSimpleSearch(String query, Long collectionId)
     {
         Integer quantity = new Integer(unstructeredAccessionQuery(splitQuery(query)).size());
         return quantity.longValue();
     }
 
     public List<AccessionDTO> getAccessionListPaginated(int firstResult, int maxResults)
     {
         String[] parts = {"accessionId"};
         return updateAccessionDTOListValues(accessionDTOFactory.createDTOList(
                 accessionEAOLocal.findAllPaginatedFilterAndOrderBy(
                 Accession.class, firstResult, maxResults, parts, null)));
     }
 
     public Long countAccessions()
     {
         return accessionEAOLocal.count(Accession.class);
     }
 
     public List<AccessionDTO> getAccessionAdvancedSearch(AccessionDTO accessionDTO, Long collectionId, int firstResult, int maxResult) {
         Set<Long> accessionIds = getAccessionByCriteria(accessionDTO);
 
         //Retrieve entities
         List<Accession> accessionList = getEntities(accessionIds,
                 Accession.class, firstResult, maxResult);
 
         return updateAccessionDTOListValues(accessionDTOFactory.createDTOList(accessionList));
     }
 
     public Set<Long> getAccessionByCriteria(AccessionDTO accessionDTO)
     {
         Set<Long> ids = new HashSet<Long>();
         boolean firstTime = true;
 
         List<Long> query = new ArrayList<Long>();
         Accession queryAccession = null;
 
         //find by accession id
         if(accessionDTO.getAccessionId() != null)
         {
             queryAccession = accessionEAOLocal.
                     findById(Accession.class, accessionDTO.getAccessionId());
             if(queryAccession != null)
             {
                 ids.add(queryAccession.getAccessionId());
                 firstTime = false;
             }
         }
 
         //find by
         if(accessionDTO.getAccessionNumber() != null)
         {
             query = accessionEAOLocal.findByAccessionNumber
                     (accessionDTO.getAccessionNumber());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getPackages() != null)
         {
             query = accessionEAOLocal.
                     findByPackages(accessionDTO.getPackages());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getOriginalWeigth() != null)
         {
             query = accessionEAOLocal.
                     findByOriginalWeigth(accessionDTO.getOriginalWeigth());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getCurrentWeigth() != null)
         {
             query = accessionEAOLocal.
                     findByCurrentWeigth(accessionDTO.getCurrentWeigth());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getResponsablePersonId() != null)
         {
             query = accessionEAOLocal.
                     findByResponsablePersonId(accessionDTO.getResponsablePersonId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getPassportId() != null)
         {
             query = accessionEAOLocal.
                     findByPassportId(accessionDTO.getPassportId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getAccessionParentId() != null)
         {
             query = accessionEAOLocal.
                     findByAccessionParentId(accessionDTO.getAccessionParentId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getGerminationMethodTypeId() != null)
         {
             query = accessionEAOLocal.
                     findByGerminationMethodId(accessionDTO.getGerminationMethodTypeId());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by
         if(accessionDTO.getGerminationRate() != null)
         {
             query = accessionEAOLocal.
                     findByGerminationRate(accessionDTO.getGerminationRate());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
         return ids;
 
     }
 
     public List<AccessionDTO> getAccessionSimpleSearch(String query, Long collectionId, int firstResult, int maxResult) {
         Set<Long> accessionIds = unstructeredAccessionQuery(splitQuery(query));
 
         //Retrieve entities
         List<Accession> accessionList = getEntities(accessionIds,
                 Accession.class, firstResult, maxResult);
 
         return updateAccessionDTOListValues(accessionDTOFactory.createDTOList(accessionList));
     }
 
     public List<AccessionDTO> updateAccessionDTOListValues(List<AccessionDTO> accessionDTOList)
     {
         for (AccessionDTO accessionDTO : accessionDTOList)
             updateAccessionDTOValues(accessionDTO);
 
         return accessionDTOList;
     }
 
     public AccessionDTO updateAccessionDTOValues(AccessionDTO accessionDTO)
     {
         if(accessionDTO.getAccessionParentId() != null)
         {
             accessionDTO.setAccessionParent((accessionEAOLocal.
                     findById(
                     Accession.class, accessionDTO.getAccessionParentId())).
                     getAccessionNumber());
         }
         if(accessionDTO.getResponsablePersonId() != null)
         {
             accessionDTO.setResponsablePerson(personEAOLocal.
                     findById(
                     Person.class, accessionDTO.getResponsablePersonId()).
                     getNaturalLongName());
         }
         if(accessionDTO.getGerminationMethodTypeId() != null)
         {
             accessionDTO.setGerminationMethodType(
                     selectionListValueLocalEAO.
                     findById(
                     SelectionListEntity.GERMINATION_METHOD_TYPE.getId(),
                     accessionDTO.getGerminationMethodTypeId()).getName());
 
         }
         return accessionDTO;
     }
 
     /**
      * Search passports id for each part if the query string. It search for
      * passport Id, material type, mission number, and gathering id
      * @param parts
      * @return
      */
     private Set<Long> unstructeredAccessionQuery(String[] parts)
     {
         Set<Long> accessionList = new HashSet();
         Long id = null;
         List<Long> accessionId = null;
         Accession entity = null;
 
 
 
         for (int i = 0; i < parts.length; i++)
         {
             accessionId = accessionEAOLocal.findByAccessionNumber(parts[i]);
             if(accessionId != null && !accessionId.isEmpty())
                 accessionList.addAll(accessionId);
 
             //try to cast it
             try
             {
                 id = Long.parseLong(parts[i]);
                 
 
                 //find by accession parent id
                 accessionId = accessionEAOLocal.findByAccessionParentId(id);
                 if(accessionId != null && !accessionId.isEmpty())
                     accessionList.addAll(accessionId);
 
                 //find by current weigth
                 accessionId = accessionEAOLocal.findByCurrentWeigth(id);
                 if(accessionId != null && !accessionId.isEmpty())
                     accessionList.addAll(accessionId);
 
                 //find by original weigth
                 accessionId = accessionEAOLocal.findByOriginalWeigth(id);
                 if(accessionId != null && !accessionId.isEmpty())
                     accessionList.addAll(accessionId);
 
                 //find by packages number
                 accessionId = accessionEAOLocal.findByPackages(id);
                 if(accessionId != null && !accessionId.isEmpty())
                     accessionList.addAll(accessionId);
 
                 //find by passport Id
                 accessionId = accessionEAOLocal.findByPassportId(id);
                 if(accessionId != null && !accessionId.isEmpty())
                     accessionList.addAll(accessionId);
             }
             catch(Exception e){}
         }
         return accessionList;
     }
 
     public Long saveAccession(AccessionDTO accessionDTO) {
         Accession accession = accessionDTOFactory.createPlainEntity(accessionDTO);
         accessionEAOLocal.create(accession);
         return accession.getAccessionId();
     }
 
    public void updateAccession(AccessionDTO accessionDTO) {
 
         Accession accession = accessionEAOLocal.findById(
                 Accession.class, accessionDTO.getAccessionId());
 
        Long weightRemoved = accession.getOriginalWeigth() - accession.getCurrentWeigth();
 
        if(accessionDTO.getOriginalWeigth() < weightRemoved)
            throw new IllegalArgumentException("error_low_weight");
 
         accessionDTOFactory.updatePlainEntity(accessionDTO, accession);
 
         accession.setCurrentWeigth(accessionDTO.getOriginalWeigth() - weightRemoved);
 
         accessionEAOLocal.update(accession);
     }
 
 
 
 
 
     
     public void saveAccessionMovement(AccessionMovementDTO accessionMovementDTO)throws IllegalArgumentException  {
         AccessionMovement accessionMovement = accessionMovementDTOFactory.createPlainEntity(accessionMovementDTO);
         accessionMovement.getAccessionMovementPK().setAccessionMovementDate(new GregorianCalendar().getTime());
 
         Accession accession = accessionEAOLocal.findById(Accession.class, accessionMovementDTO.getAccessionId());
         if(accession.getOriginalWeigth() != null && accession.getCurrentWeigth() != null)
         {
             if(accession.getCurrentWeigth() == 0)
                 throw new IllegalArgumentException("error_current_weight_zero");
 
             if((accession.getCurrentWeigth() < accessionMovementDTO.getWeight()))
                 throw new IllegalArgumentException("error_not_enough_weight");
 
             accession.setCurrentWeigth(accession.getCurrentWeigth() - accessionMovementDTO.getWeight());
             accessionEAOLocal.update(accession);
 
         }
        accessionMovementEAOLocal.create(accessionMovement);
     }
 
     public void updateAccessionMovement(AccessionMovementDTO accessionMovementDTO) {
         AccessionMovement accessionMovement =
                 accessionMovementEAOLocal.findByAccessionIdAndDateTime(
                 accessionMovementDTO.getAccessionId(), accessionMovementDTO.getAccessionMovementDate());
 
         Accession accession = accessionEAOLocal.findById(Accession.class, accessionMovementDTO.getAccessionId());
 
         accession.setCurrentWeigth(
                 accession.getCurrentWeigth() +
                 accessionMovement.getWeight());
 
         if(accession.getOriginalWeigth() != null && accession.getCurrentWeigth() != null)
         {
             if(accession.getCurrentWeigth() == 0)
                 throw new IllegalArgumentException("error_current_weight_zero");
 
             if((accession.getCurrentWeigth() < accessionMovementDTO.getWeight()))
                 throw new IllegalArgumentException("error_not_enough_weight");
 
             accession.setCurrentWeigth(accession.getCurrentWeigth() - accessionMovementDTO.getWeight());
             accessionEAOLocal.update(accession);
 
         }
 
         accessionMovement.setWeight(accessionMovementDTO.getWeight());
 
         accessionMovementEAOLocal.update(accessionMovement);
     }
 
     public void deleteAccessionMovement(AccessionMovementDTO accessionMovementDTO) {
         AccessionMovement accessionMovement =
                 accessionMovementEAOLocal.findById(
                 AccessionMovement.class, accessionMovementDTO.getAccessionId());
         accessionMovementEAOLocal.delete(accessionMovement);
     }
 
     public List<AccessionMovementDTO> getAllAccessionMovementByAccesionIdPaginated(Long accessionId, int firstResult, int maxResult) {
         List<AccessionMovementDTO> accessionMovementDTOList =
                 accessionMovementDTOFactory.createDTOList(
                 accessionMovementEAOLocal.findAllPaginatedByAccessionId(accessionId));
         return updateAccessionMovementDTOListValues(accessionMovementDTOList);
     }
 
     public List<AccessionMovementDTO> updateAccessionMovementDTOListValues(List<AccessionMovementDTO> accessionMovementDTOList)
     {
         for (AccessionMovementDTO accessionMovementDTO : accessionMovementDTOList)
         {
             updateAccessionMovementDTOValue(accessionMovementDTO);
         }
         return accessionMovementDTOList;
     }
     
     public AccessionMovementDTO updateAccessionMovementDTOValue(AccessionMovementDTO accessionMovementDTO)
     {
         accessionMovementDTO.setAccessionNumber(
                 accessionEAOLocal.findById(
                 Accession.class, accessionMovementDTO.getAccessionId()).getAccessionNumber());
         
         if(accessionMovementDTO.getResponsablePersonId() != null)
         {
             accessionMovementDTO.setResponsablePerson(personEAOLocal.
                     findById(
                     Person.class, accessionMovementDTO.getResponsablePersonId()).
                     getNaturalLongName());
         }
         if(accessionMovementDTO.getAccessionMovementTypeId() != null)
         {
             accessionMovementDTO.setAccessionMovementType(
             selectionListValueLocalEAO.
                     findById(
                     SelectionListEntity.ACCESSION_MOVEMENT_TYPE.getId(),
                     accessionMovementDTO.getAccessionMovementTypeId()).getName());
         }
         return accessionMovementDTO;
     }
 
     public Long countAccessionMovements(Long accessionId) {
         return accessionMovementEAOLocal.countAllByAccessionId(accessionId);
     }
 
     public List<AccessionMovementDTO> getAccessionMovementsSimpleSearch(String query, Long collectionId, int firstResult, int maxResult) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public List<AccessionMovementDTO> getAccessionMovementsAdvancedSearch(AccessionMovementDTO accessionMovement, Long collectionId, int firstResult, int maxResult) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Long countAccessionMovementsSimpleSearch(String query, Long collectionId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Long countAccessionMovementsAdvancedSearch(AccessionMovementDTO accessionMovement, Long collectionId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public List<PersonDTO> getResponsablePersons() {
         return personDTOFactory.createDTOList(personEAOLocal.
                 findByProfile(ProfileEntity.RESPONSABLE_PERSON_GERMPLASM_PROFILE.getId()));
     }
 }
