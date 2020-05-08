 package fr.cg95.cvq.service.document.impl;
 
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.document.DocumentTypeValidity;
 import fr.cg95.cvq.business.document.DocumentUsageType;
 import fr.cg95.cvq.dao.document.IDocumentTypeDAO;
 
 /**
  * Simple bootstrapper class for document types : create a set of commonly used
  * document types.
  * 
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class DocumentBootstrapper {
 
     private IDocumentTypeDAO documentTypeDAO;
     
     private void checkDocumentType(final String name, final Integer type,
             final DocumentUsageType documentUsageType, final Integer duration,
             final DocumentTypeValidity documentTypeValidity) {
         
         if (documentTypeDAO.findByType(type) != null) {
             // document type already exists, nothing to do
             return;
         }
         
         DocumentType documentType = new DocumentType();
         documentType.setName(name);
         documentType.setType(type);
         documentType.setUsageType(documentUsageType);
         documentType.setValidityDuration(duration);
         documentType.setValidityDurationType(documentTypeValidity);
         documentTypeDAO.create(documentType);
     }
     
     public void bootstrapForCurrentLocalAuthority() {
         
         checkDocumentType("Old CNI", 1, DocumentUsageType.REUSABLE, 10, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("School Insurance", 2, DocumentUsageType.REUSABLE, 1, 
                 DocumentTypeValidity.YEAR);
         
         checkDocumentType("Insurance Certificate", 3, DocumentUsageType.REUSABLE, 1, 
                 DocumentTypeValidity.YEAR);
         
         checkDocumentType("Vital Card Certificate", 4, DocumentUsageType.REUSABLE, 3, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Exspouse Permission", 5, DocumentUsageType.REUSABLE, 3, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Taxes Notification", 6, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.END_YEAR);
 
         checkDocumentType("Payroll", 7, DocumentUsageType.REUSABLE, 1, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Health Notebook", 8, DocumentUsageType.REUSABLE, 3, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Vacating Certificate", 9, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.END_SCHOOL_YEAR);
 
         checkDocumentType("School Certificate", 10, DocumentUsageType.REUSABLE, 1, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Medical Certificate", 11, DocumentUsageType.SINGLE_USE, 0, 
                 DocumentTypeValidity.END_SCHOOL_YEAR);
 
         checkDocumentType("French Nationality Acquisition by Marriage Declaration", 12, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Tutor Appointment Declaration", 13, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Id Card Loss Declaration", 14, DocumentUsageType.REUSABLE, 2, 
                 DocumentTypeValidity.MONTH);
 
         checkDocumentType("Birth Certificate", 15, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Adoption Judgment", 16, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Specific Request Receipt", 17, DocumentUsageType.REUSABLE, 3, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("French Nationality Receipt", 18, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Family Notebook", 19, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Bank Identity Receipt", 20, DocumentUsageType.REUSABLE, 1, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Domicile Receipt", 21, DocumentUsageType.REUSABLE, 3, 
                 DocumentTypeValidity.YEAR);
 
         checkDocumentType("Identity Receipt", 22, DocumentUsageType.REUSABLE, 0, 
                 DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Individual Alignment Certificate", 23, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Building Situation Plan", 24, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Ground Situation Plan", 25, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Mass Plan", 26, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Bank Statement", 27, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Saving Account", 28, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Location Receipt", 29, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Housing Taxes Notification", 30, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Revenue Taxes Notification", 31, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Handicap Card", 32, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Family Help Certificate", 33, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Identity Photo", 34, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Registration Certificate", 35, 
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Revenue Taxes Notification Two Years Ago", 36, 
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Bafa Internship Certificate", 37,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Bafa Block Release Certificate", 38,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Bafa General Training Certificate", 39,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("School Assignment Certificate", 40,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Medical Form", 41,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
         
         checkDocumentType("Swimming Certificate", 42,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Training Agreement", 43,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Last General Assembly Minute", 44,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Past Season Budget", 45,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Estimated Budget Including Requested Grant", 46,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Past Season Licences Payment Receipt", 47,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Recepisse Declaration Constitution Association", 48,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Insertion Journal Officiel", 49,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
        checkDocumentType("Statuts Association", 50,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Liste Membres Bureau", 51,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
 
         checkDocumentType("Rapport Annuel Activite", 52,
                 DocumentUsageType.REUSABLE, 1, DocumentTypeValidity.YEAR);
 
         checkDocumentType("Modifications Depuis Creation Association", 53,
                 DocumentUsageType.REUSABLE, 0, DocumentTypeValidity.UNLIMITED);
     }
 
     public void setDocumentTypeDAO(IDocumentTypeDAO documentTypeDAO) {
         this.documentTypeDAO = documentTypeDAO;
     }
 }
