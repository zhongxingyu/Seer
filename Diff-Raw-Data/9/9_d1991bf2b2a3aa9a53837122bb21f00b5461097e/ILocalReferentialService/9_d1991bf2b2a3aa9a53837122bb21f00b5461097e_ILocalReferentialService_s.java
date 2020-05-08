 package fr.cg95.cvq.service.authority;
 
 import java.util.Map;
 import java.util.Set;
 
 import fr.cg95.cvq.business.authority.LocalReferentialType;
 import fr.cg95.cvq.exception.CvqException;
 
 public interface ILocalReferentialService {
 
     /** service name used by Spring's application context */
     String SERVICE_NAME = "localReferentialService";
 
     /**
      * Get a list of all known local referential data.
      *
      * @return a {@link Set} of {@link LocalReferentialType} objects
      */
     Set getAllLocalReferentialData()
         throws CvqException;
 
     /**
      * Get a summary of all local referential data names (data name being the name of the
      * corresponding element in the request's XML schema).
      *
      * @return a {@link Map} of (dataName, {@link Map}(lang, value))
      */
     Map getAllLocalReferentialDataNames()
         throws CvqException;
 
     /**
      * Get all information related to the given data.
      */
     LocalReferentialType getLocalReferentialDataByName(final String dataName)
         throws CvqException;
 
     /**
      * Get a list of all local referential data names belonging to a given request type.
      *
      * @return a {@link Set} of {@link LocalReferentialType} objects
      */
     Set getLocalReferentialDataByRequestType(final String requestTypeLabel)
         throws CvqException;
 
     /**
      * Test localreferential configuration for a requestType
      * <br />Return false if at least one localReferentialType has null or empty entries. 
      * <br />Otherwise return true.
      */
    public boolean isLocalReferentialConfigure(final String requestTypeLabel) throws CvqException;
     
     /**
      * Set local referential data.
      */
     void setLocalReferentialData(final LocalReferentialType localReferentialType)
         throws CvqException;
 }
