 package fr.cg95.cvq.service.document;
 
 import java.util.List;
 
 import fr.cg95.cvq.business.document.DocumentType;
 
 public interface IDocumentTypeService {
 
     Integer NO_TYPE = new Integer(0);
     Integer OLD_CNI_TYPE = new Integer(1);
     Integer SCHOOL_INSURANCE_TYPE = new Integer(2);
     Integer INSURANCE_CERTIFICATE_TYPE = new Integer(3);
     Integer VITAL_CARD_CERTIFICATE_TYPE = new Integer(4);
     Integer EXSPOUSE_PERMISSION_TYPE = new Integer(5);
     Integer TAXES_NOTIFICATION_TYPE = new Integer(6);
     Integer PAYROLL_TYPE = new Integer(7);
     Integer HEALTH_NOTEBOOK_TYPE = new Integer(8);
     Integer VACATING_CERTIFICATE_TYPE = new Integer(9);
     Integer SCHOOL_CERTIFICATE_TYPE = new Integer(10);
     Integer MEDICAL_CERTIFICATE_TYPE = new Integer(11);
     Integer FRENCH_NATIONALITY_ACQUISITION_TYPE = new Integer(12);
     Integer TUTOR_APPOINTMENT_DECLARATION_TYPE = new Integer(13);
     Integer ID_CARD_LOSS_DECLARATION_TYPE = new Integer(14);
     Integer BIRTH_CERTIFICATE_TYPE = new Integer(15);
     Integer ADOPTION_JUDGMENT_TYPE = new Integer(16);
     Integer SPECIFIC_REQUEST_RECEIPT_TYPE = new Integer(17);
     Integer FRENCH_NATIONALITY_RECEIPT_TYPE = new Integer(18);
     Integer FAMILY_NOTEBOOK_TYPE = new Integer(19);
     Integer BANK_IDENTITY_RECEIPT_TYPE = new Integer(20);
     Integer DOMICILE_RECEIPT_TYPE = new Integer(21);
     Integer IDENTITY_RECEIPT_TYPE = new Integer(22);
     Integer INDIVIDUAL_ALIGNMENT_CERTIFICATE_TYPE = new Integer(23);
     Integer BUILDING_SITUATION_PLAN_TYPE = new Integer(24);
     Integer GROUND_SITUATION_PLAN_TYPE = new Integer(25);
     Integer MASS_PLAN_TYPE = new Integer(26);
     Integer BANK_STATEMENT_TYPE = new Integer(27);
     Integer SAVING_ACCOUNT_TYPE = new Integer(28);
     Integer LOCATION_RECEIPT_TYPE = new Integer(29);
     Integer HOUSING_TAXES_NOTIFICATION_TYPE = new Integer(30);
     Integer REVENUE_TAXES_NOTIFICATION_TYPE = new Integer(31);
     Integer HANDICAP_CARD_TYPE = new Integer(32);
     Integer FAMILY_HELP_CERTIFICATE_TYPE = new Integer(33);
     Integer IDENTITY_PHOTO_TYPE = new Integer(34);
     Integer REGISTRATION_CERTIFICATE = new Integer(35);
     Integer REVENUE_TAXES_NOTIFICATION_TWO_YEARS_AGO = new Integer(36);
     Integer BAFA_INTERNSHIP_CERTIFICATE = new Integer(37);
     Integer BAFA_BLOCK_RELEASE_CERTIFICATE = new Integer(38);
     Integer BAFA_GENERAL_TRAINING_CERTIFICATE = new Integer(39);
     Integer SCHOOL_ASSIGNMENT_CERTIFICATE = new Integer(40);
     Integer MEDICAL_FORM = new Integer(41);
     Integer SWIMMING_CERTIFICATE = new Integer(42);
     Integer TRAINING_AGREEMENT = new Integer(43);
     Integer LAST_GENERAL_ASSEMBLY_MINUTE = new Integer(44);
     Integer PAST_SEASON_BUDGET = new Integer(45);
     Integer ESTIMATED_BUDGET_INCLUDING_REQUESTED_GRANT = new Integer(46);
     Integer PAST_SEASON_LICENCES_PAYMENT_RECEIPT = new Integer(47);
     Integer RECEPISSE_DECLARATION_CONSTITUION_ASSOCIATION = new Integer(48);
     Integer INSERTION_JOURNAL_OFFICIEL = new Integer(49);
    Integer STATUTS_ASSOCIATION = new Integer(50);
     Integer LISTE_MEMBRES_BUREAU = new Integer(51);
     Integer RAPPORT_ANNUEL_ACTIVITE = new Integer(52);
     Integer MODIFICATIONS_DEPUIS_CREATION_ASSOCIATION = new Integer(53);
 
     /**
      * Get a document type by type id.
      *
      * @param id the id of the document type, one among the (long) list of static
      *           integer constant defined in this class
      */
     DocumentType getDocumentTypeByType(final Integer type);
 
     DocumentType getDocumentTypeById(final Long id);
 
     /**
      * Get all known document types.
      */
     List<DocumentType> getAllDocumentTypes();
 }
