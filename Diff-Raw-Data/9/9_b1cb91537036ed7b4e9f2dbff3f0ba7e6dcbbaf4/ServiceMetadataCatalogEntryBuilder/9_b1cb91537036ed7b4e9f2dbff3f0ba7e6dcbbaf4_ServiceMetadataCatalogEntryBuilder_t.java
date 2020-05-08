 /**
  *
  */
 package gov.nih.nci.cagrid.portal.aggr.catalog;
 
 import gov.nih.nci.cagrid.portal.dao.PortalUserDao;
 import gov.nih.nci.cagrid.portal.dao.catalog.*;
 import gov.nih.nci.cagrid.portal.domain.Address;
 import gov.nih.nci.cagrid.portal.domain.GridDataService;
 import gov.nih.nci.cagrid.portal.domain.GridService;
 import gov.nih.nci.cagrid.portal.domain.catalog.*;
 import gov.nih.nci.cagrid.portal.domain.metadata.common.PointOfContact;
 import gov.nih.nci.cagrid.portal.domain.metadata.common.ResearchCenter;
 import gov.nih.nci.cagrid.portal.domain.metadata.dataservice.DomainModel;
 import gov.nih.nci.cagrid.portal.util.BeanUtils;
 import gov.nih.nci.cagrid.portal.util.PortalDBRuntimeException;
 import gov.nih.nci.cagrid.portal.util.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.net.MalformedURLException;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author <a href="mailto:joshua.phillips@semanticbits.com">Joshua Phillips</a>
  * @author kherm manav.kher@semanticbits.com
  */
 @Transactional
 public class ServiceMetadataCatalogEntryBuilder {
 
     private static final Log logger = LogFactory.getLog(ServiceMetadataCatalogEntryBuilder.class);
 
     private GridServiceEndPointCatalogEntryDao gridServiceEndPointCatalogEntryDao;
     private GridServiceInterfaceCatalogEntryDao gridServiceInterfaceCatalogEntryDao;
     private InformationModelCatalogEntryDao informationModelCatalogEntryDao;
     private CatalogEntryRelationshipTypeDao catalogEntryRelationshipTypeDao;
     private CatalogEntryRelationshipInstanceDao catalogEntryRelationshipInstanceDao;
     private InstitutionCatalogEntryDao institutionCatalogEntryDao;
     private PersonCatalogEntryDao personCatalogEntryDao;
     private PortalUserDao portalUserDao;
 
 
     /**
      * @param service
      */
     public GridServiceEndPointCatalogEntry build(GridService service) throws PortalDBRuntimeException {
 
         String serviceName = BeanUtils.traverse(service,
                 "serviceMetadata.serviceDescription.name");
         String serviceVersion = BeanUtils.traverse(service,
                 "serviceMetadata.serviceDescription.version");
         String institutionName = BeanUtils.traverse(service,
                 "serviceMetadata.hostingResearchCenter.displayName");
         if (StringUtils.isEmpty(institutionName)) {
             institutionName = BeanUtils.traverse(service,
                     "serviceMetadata.hostingResearchCenter.shortName");
             if (StringUtils.isEmpty(institutionName)) {
                 institutionName = service.getUrl();
             }
         }
         if (StringUtils.isEmpty(serviceName)) {
             logger.warn("Service has no name. Will not create a CE for service no name" + service.getUrl());
             return null;
         }
 
         GridServiceEndPointCatalogEntry endpointCe = getGridServiceEndPointCatalogEntryDao()
                 .isAbout(service);
         if (endpointCe == null) {
             endpointCe = new GridServiceEndPointCatalogEntry();
             endpointCe.setAbout(service);
             endpointCe.setCreatedAt(new Date());
             endpointCe.setPublished(true);
             service.setCatalog(endpointCe);
 
             getGridServiceEndPointCatalogEntryDao().save(endpointCe);
         }
         endpointCe.setUpdatedAt(new Date());
 
         endpointCe.setName(serviceName + " (" + serviceVersion + ") @ "
                 + institutionName);
         endpointCe.setDescription(BeanUtils.traverse(service,
                 "serviceMetadata.serviceDescription.description"));
 
         // See if interface exists
         String intfName = serviceName + " (" + serviceVersion + ") Interface";
         GridServiceInterfaceCatalogEntry interfaceCe = getGridServiceInterfaceCatalogEntryDao()
                 .getDynamicInterfaceForNameAndVersion(intfName,
                         serviceVersion);
         if (interfaceCe == null) {
             interfaceCe = new GridServiceInterfaceCatalogEntry();
             interfaceCe.setName(intfName);
             interfaceCe.setVersion(serviceVersion);
             interfaceCe.setDescription(endpointCe.getDescription());
             interfaceCe.setPublished(true);
             interfaceCe.setCreatedAt(new Date());
             getGridServiceInterfaceCatalogEntryDao().save(interfaceCe);
         }
         interfaceCe.setUpdatedAt(new Date());
 
         // See if the implements relationship already exists
         CatalogEntryRelationshipInstance implRelInst = getCatalogEntryRelationshipInstanceDao()
                 .getDynamicRelationship(
                         RelationshipTypeConstants.GRID_SERVICE_ENDPOINT_IMPLEMENTS_GRID_SERVICE_INTERFACE,
                         endpointCe, interfaceCe);
         if (implRelInst == null) {
 
             implRelInst = assertRelationship(
                     RelationshipTypeConstants.GRID_SERVICE_ENDPOINT_IMPLEMENTS_GRID_SERVICE_INTERFACE,
                     endpointCe, interfaceCe, interfaceCe.getName()
                             + " is implemented by " + endpointCe.getName(),
                     endpointCe.getName() + " implements "
                             + interfaceCe.getName());
         }
         implRelInst.setUpdatedAt(new Date());
         getCatalogEntryRelationshipInstanceDao().save(implRelInst);
 
 //		handlePOCRelationships(endpointCe, service.getServiceMetadata()
 //				.getServiceDescription().getPointOfContactCollection(),
 //				RelationshipTypeConstants.PERSON_POC_FOR_GRID_SERVICE_ENDPOINT);
 
         // Handle the hosting institution
         ResearchCenter researchCenter = BeanUtils.traverse(service,
                 "serviceMetadata.hostingResearchCenter", ResearchCenter.class);
         if (researchCenter != null) {
             InstitutionCatalogEntry institutionCe = handleHostingInstitution(
                     researchCenter, endpointCe);
 //			if (institutionCe != null) {
 //				List<ResearchCenterPointOfContact> pocs = researchCenter
 //						.getPointOfContactCollection();
 
 //				handlePOCRelationships(institutionCe, pocs,
 //						RelationshipTypeConstants.PERSON_POC_FOR_INSTITUTION);
 
 //			}
         }
 
         // Create the information model
         if (service instanceof GridDataService) {
 
             GridDataService dataService = (GridDataService) service;
             DomainModel domainModel = dataService.getDomainModel();
             if (domainModel != null) {
 
                 String projectLongName = domainModel.getProjectLongName();
                 if (StringUtils.isEmpty(projectLongName)) {
                     projectLongName = domainModel.getProjectShortName() + " "
                             + domainModel.getProjectVersion();
                 }
                 if (StringUtils.isEmpty(projectLongName)) {
                     logger.info("DomainModel for " + dataService.getUrl() + " has no name. No information model catalog entry will be created.");
                 } else {
 
                     // See if information model already exists
                     InformationModelCatalogEntry infoCe = getInformationModelCatalogEntryDao()
                            .getDynamicModelByProjectLongName(projectLongName);
                     if (infoCe == null) {
                         infoCe = new InformationModelCatalogEntry();
 
                        infoCe.setProjectLongName(projectLongName);
                        infoCe.setName(projectLongName);
                         infoCe.setDescription(domainModel
                                 .getProjectDescription());
                         infoCe.setCreatedAt(new Date());
                         getInformationModelCatalogEntryDao().save(infoCe);
                     }
                     infoCe.setUpdatedAt(new Date());
 
                     // Create relationship to service interface and endpoint
                     createSupportsRelationship(
                             endpointCe,
                             infoCe,
                             RelationshipTypeConstants.GRID_SERVICE_ENDPOINT_SUPPORTS_INFORMATION_MODEL);
                     createSupportsRelationship(
                             interfaceCe,
                             infoCe,
                             RelationshipTypeConstants.GRID_SERVICE_INTERFACE_SUPPORTS_INFORMATION_MODEL);
                 }
 
             }
 
         }
 
         getGridServiceInterfaceCatalogEntryDao().save(interfaceCe);
         getGridServiceEndPointCatalogEntryDao().save(endpointCe);
 
         return endpointCe;
     }
 
 //	private void handlePOCRelationships(CatalogEntry ce, List pocs,
 //			String relTypeName) {
 //
 //		if (pocs != null) {
 //			for (Iterator i = pocs.iterator(); i.hasNext();) {
 //				PointOfContact about = (PointOfContact) i.next();
 //				Person person = about.getPerson();
 //				if (person != null
 //						&& !StringUtils.isEmpty(person.getEmailAddress())
 //						&& !StringUtils.isEmpty(person.getFirstName())
 //						&& !StringUtils.isEmpty(person.getLastName())) {
 //
 //					List<PersonCatalogEntry> personCes = getPersonCatalogEntryDao()
 //							.searchByNameAndEmail(person.getFirstName(),
 //									person.getLastName(),
 //									person.getEmailAddress());
 //					if (personCes.size() == 0) {
 //						personCes.add(createPersonCatalogEntry(person));
 //					}
 //
 //					handlePOCRelationship(personCes, ce, relTypeName, about);
 //
 //				}
 //			}
 //		}
 //	}
 
     private void createSupportsRelationship(CatalogEntry ce,
                                             InformationModelCatalogEntry infoCe, String relTypeName) {
         CatalogEntryRelationshipInstance suppRelInst = getCatalogEntryRelationshipInstanceDao()
                 .getDynamicRelationship(relTypeName, ce, infoCe);
         if (suppRelInst == null) {
 
             String desc = ce.getName() + " supports " + infoCe.getName() + ".";
             suppRelInst = assertRelationship(relTypeName, ce, infoCe, desc,
                     desc);
         }
         suppRelInst.setUpdatedAt(new Date());
         getCatalogEntryRelationshipInstanceDao().save(suppRelInst);
     }
 
     private void handlePOCRelationship(List<PersonCatalogEntry> personCes,
                                        CatalogEntry ce, String relTypeName, PointOfContact poc) {
 
         for (PersonCatalogEntry personCe : personCes) {
 
             CatalogEntryRelationshipInstance pocRelInst = getCatalogEntryRelationshipInstanceDao()
                     .getDynamicRelationship(relTypeName, personCe, ce);
             if (pocRelInst == null) {
 
                 String roleADescription = ce.getName() + " has "
                         + personCe.getName() + " as a point-of-contact. ";
                 if (!StringUtils.isEmpty(poc.getRole())) {
                     roleADescription += "Role is '" + poc.getRole() + "'. ";
                 }
                 if (!StringUtils.isEmpty(poc.getAffiliation())) {
                     roleADescription += "Affiliation is '"
                             + poc.getAffiliation() + "'. ";
                 }
 
                 String roleBDescription = personCe.getName()
                         + " is point-of-contact for " + ce.getName() + ". ";
                 if (!StringUtils.isEmpty(poc.getRole())) {
                     roleBDescription += "Role is '" + poc.getRole() + "'. ";
                 }
                 if (!StringUtils.isEmpty(poc.getAffiliation())) {
                     roleBDescription += "Affiliation is '"
                             + poc.getAffiliation() + "'. ";
                 }
 
                 pocRelInst = assertRelationship(relTypeName, personCe, ce,
                         roleADescription, roleBDescription);
             }
             pocRelInst.setUpdatedAt(new Date());
 
             getCatalogEntryRelationshipInstanceDao().save(pocRelInst);
         }
     }
 
     private InstitutionCatalogEntry handleHostingInstitution(
             ResearchCenter researchCenter,
             GridServiceEndPointCatalogEntry endpointCe) {
 
         InstitutionCatalogEntry institutionCe = null;
         String displayName = researchCenter.getDisplayName();
         if (!StringUtils.isEmpty(displayName)) {
 
             List<InstitutionCatalogEntry> l = getInstitutionCatalogEntryDao()
                     .searchByName(displayName);
             if (l.size() == 1) {
                 institutionCe = l.iterator().next();
             } else {
                 for (InstitutionCatalogEntry ice : l) {
                     if (ice.getAuthor() == null) {
                         institutionCe = ice;
                         break;
                     }
                 }
             }
             if (institutionCe == null) {
                 institutionCe = createInstitutionCatalogEntry(researchCenter);
             }
             institutionCe.setUpdatedAt(new Date());
 
             // Check if hosts relationship exists
             CatalogEntryRelationshipInstance hostRelInst = getCatalogEntryRelationshipInstanceDao()
                     .getDynamicRelationship(
                             RelationshipTypeConstants.HOSTING_INSTITUTION_OF_GRID_SERVICE_ENDPOINT,
                             institutionCe, endpointCe);
             if (hostRelInst == null) {
 
                 hostRelInst = assertRelationship(
                         RelationshipTypeConstants.HOSTING_INSTITUTION_OF_GRID_SERVICE_ENDPOINT,
                         institutionCe, endpointCe, institutionCe.getName()
                                 + " hosts " + endpointCe.getName(), endpointCe
                                 .getName()
                                 + " is hosted by " + institutionCe.getName());
             }
             hostRelInst.setUpdatedAt(new Date());
             getCatalogEntryRelationshipInstanceDao().save(hostRelInst);
         }
         return institutionCe;
     }
 
     private InstitutionCatalogEntry createInstitutionCatalogEntry(
             ResearchCenter researchCenter) {
         InstitutionCatalogEntry institutionCe = new InstitutionCatalogEntry();
         institutionCe.setName(researchCenter.getDisplayName());
         institutionCe.setDescription(researchCenter.getDescription());
         institutionCe.setCreatedAt(new Date());
         institutionCe.setPublished(true);
 
         Address addr = researchCenter.getAddress();
         if (addr != null) {
             institutionCe.setAddressPublic(true);
             institutionCe.setStreet1(addr.getStreet1());
             institutionCe.setStreet2(addr.getStreet2());
             institutionCe.setStateProvince(addr.getStateProvince());
             institutionCe.setCountryCode(addr.getCountry());
             institutionCe.setLocality(addr.getLocality());
             institutionCe.setLatitude(addr.getLatitude());
             institutionCe.setLongitude(addr.getLatitude());
             try {
                 institutionCe.setWebSite(researchCenter.getHomepageUrl());
             } catch (MalformedURLException ex) {
 
             }
         }
         getInstitutionCatalogEntryDao().save(institutionCe);
 
         return institutionCe;
     }
 
 //	private PersonCatalogEntry createPersonCatalogEntry(Person person) {
 //
 //		PortalUser user = getPortalUserDao().getByPersonId(person.getId());
 //
 //		PersonCatalogEntry personCe = new PersonCatalogEntry();
 //		if(user != null){
 //			personCe.setAbout(user);
 //		}
 //		personCe.setCreatedAt(new Date());
 //		personCe.setPublished(true);
 //		personCe.setName(person.getFirstName() + " " + person.getLastName());
 //		personCe.setEmailAddress(person.getEmailAddress());
 //		personCe.setEmailAddressPublic(true);
 //		if (!StringUtils.isEmpty(person.getPhoneNumber())) {
 //			personCe.setPhoneNumber(person.getPhoneNumber());
 //			personCe.setPhoneNumberPublic(true);
 //		}
 //		getPersonCatalogEntryDao().save(personCe);
 //		return personCe;
 //	}
 
     private CatalogEntryRelationshipInstance assertRelationship(
             String relTypeName, CatalogEntry ceA, CatalogEntry ceB,
             String roleADesc, String roleBDesc) {
 
         CatalogEntryRelationshipType relType = getCatalogEntryRelationshipTypeDao()
                 .getByName(relTypeName);
 
         CatalogEntryRelationshipInstance relInst = new CatalogEntryRelationshipInstance();
         relInst.setType(relType);
         relInst.setCreatedAt(new Date());
         getCatalogEntryRelationshipInstanceDao().save(relInst);
 
         CatalogEntryRoleInstance roleA = new CatalogEntryRoleInstance();
         roleA.setType(relType.getRoleTypeA());
         roleA.setDescription(roleADesc);
         roleA.setRelationship(relInst);
         roleA.setCatalogEntry(ceA);
         getCatalogEntryRelationshipInstanceDao().getHibernateTemplate().save(
                 roleA);
         relInst.setRoleA(roleA);
 
         CatalogEntryRoleInstance roleB = new CatalogEntryRoleInstance();
         roleB.setType(relType.getRoleTypeB());
         roleB.setDescription(roleBDesc);
         roleB.setRelationship(relInst);
         roleB.setCatalogEntry(ceB);
         getCatalogEntryRelationshipInstanceDao().getHibernateTemplate().save(
                 roleB);
         relInst.setRoleB(roleB);
 
         getCatalogEntryRelationshipInstanceDao().save(relInst);
 
         return relInst;
     }
 
     public GridServiceEndPointCatalogEntryDao getGridServiceEndPointCatalogEntryDao() {
         return gridServiceEndPointCatalogEntryDao;
     }
 
     public void setGridServiceEndPointCatalogEntryDao(
             GridServiceEndPointCatalogEntryDao gridServiceEndPointCatalogEntryDao) {
         this.gridServiceEndPointCatalogEntryDao = gridServiceEndPointCatalogEntryDao;
     }
 
     public GridServiceInterfaceCatalogEntryDao getGridServiceInterfaceCatalogEntryDao() {
         return gridServiceInterfaceCatalogEntryDao;
     }
 
     public void setGridServiceInterfaceCatalogEntryDao(
             GridServiceInterfaceCatalogEntryDao gridServiceInterfaceCatalogEntryDao) {
         this.gridServiceInterfaceCatalogEntryDao = gridServiceInterfaceCatalogEntryDao;
     }
 
     public InformationModelCatalogEntryDao getInformationModelCatalogEntryDao() {
         return informationModelCatalogEntryDao;
     }
 
     public void setInformationModelCatalogEntryDao(
             InformationModelCatalogEntryDao informationModelCatalogEntryDao) {
         this.informationModelCatalogEntryDao = informationModelCatalogEntryDao;
     }
 
     public CatalogEntryRelationshipTypeDao getCatalogEntryRelationshipTypeDao() {
         return catalogEntryRelationshipTypeDao;
     }
 
     public void setCatalogEntryRelationshipTypeDao(
             CatalogEntryRelationshipTypeDao catalogEntryRelationshipTypeDao) {
         this.catalogEntryRelationshipTypeDao = catalogEntryRelationshipTypeDao;
     }
 
     public CatalogEntryRelationshipInstanceDao getCatalogEntryRelationshipInstanceDao() {
         return catalogEntryRelationshipInstanceDao;
     }
 
     public void setCatalogEntryRelationshipInstanceDao(
             CatalogEntryRelationshipInstanceDao catalogEntryRelationshipInstanceDao) {
         this.catalogEntryRelationshipInstanceDao = catalogEntryRelationshipInstanceDao;
     }
 
     public InstitutionCatalogEntryDao getInstitutionCatalogEntryDao() {
         return institutionCatalogEntryDao;
     }
 
     public void setInstitutionCatalogEntryDao(
             InstitutionCatalogEntryDao institutionCatalogEntryDao) {
         this.institutionCatalogEntryDao = institutionCatalogEntryDao;
     }
 
     public PersonCatalogEntryDao getPersonCatalogEntryDao() {
         return personCatalogEntryDao;
     }
 
     public void setPersonCatalogEntryDao(
             PersonCatalogEntryDao personCatalogEntryDao) {
         this.personCatalogEntryDao = personCatalogEntryDao;
     }
 
     public PortalUserDao getPortalUserDao() {
         return portalUserDao;
     }
 
     public void setPortalUserDao(PortalUserDao portalUserDao) {
         this.portalUserDao = portalUserDao;
     }
 
 }
