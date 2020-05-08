 /*
  * Project :iCardea
  * File : MedicationPorcessor.java 
  * Encoding : UTF-8
  * Date : Apr 20, 2011
  * User : Mihai Radulescu
  */
 package at.srfg.kmt.ehealth.phrs.pcc10ws.impl;
 

 import static at.srfg.kmt.ehealth.phrs.pcc10ws.impl.Constants.DEFAULT_PCC_10_END_POINT;
 import at.srfg.kmt.ehealth.phrs.dataexchange.api.DynaClassException;
 import at.srfg.kmt.ehealth.phrs.dataexchange.api.DynamicBeanRepository;
 import at.srfg.kmt.ehealth.phrs.dataexchange.api.DynamicPropertyTypeException;
 import at.srfg.kmt.ehealth.phrs.dataexchange.impl.DynamicUtil;
 import at.srfg.kmt.ehealth.phrs.pcc10ws.api.PCC10BuildException;
 import at.srfg.kmt.ehealth.phrs.pcc10ws.api.Processor;
 import at.srfg.kmt.ehealth.phrs.util.JBossJNDILookup;
 import java.util.HashSet;
 import java.util.Set;
 import javax.naming.NamingException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import org.apache.commons.beanutils.DynaBean;
 import org.hl7.v3.QUPCIN043200UV01;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 

 /**
  *
  * @version 0.1
  * @since 0.1
  * @author Mihai
  */
 class MedicationPorcessor implements Processor<Response> {
 
     /**
      * The Logger instance. All log messages from this class
      * are routed through this member. The Logger name space
     * is <code>at.srfg.kmt.ehealth.phrs.security.impl.MedicationPorcessor</code>.
      */
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicationPorcessor.class);

     private static final MedicationFactory MEDICATION_FACTORY =
             new MedicationFactory();

     private Response result;

     private Set<Exception> exceptions;
 
     /**
      * 
      */
     MedicationPorcessor() {
         result = Response.status(Status.NOT_MODIFIED).build();
         exceptions = new HashSet<Exception>();
     }
 
     @Override
     public boolean canProcess(Object input) {
         if (input == null) {
            LOGGER.warn("Null input can not be processed.");
             return false;
         }
 
        if (!(input instanceof String)) {
            LOGGER.warn("Non String input can not be processed.");
            return false;
        }

        final String in = input.toString();
        final int indexOf = in.indexOf("-");
        if (indexOf == -1) {
            LOGGER.warn("This input [{}] has the wrong syntax", in);
            return false;
        }
        
        final boolean isMedList = in.endsWith("MEDLIST");
        return isMedList;
     }
 
     @Override
     public Set<Exception> getExceptions() {
         return exceptions;
     }
 
     @Override
     public Response getResult() {
         return result;
     }
 
     @Override
     public boolean process(Object input) {
         final QUPCIN043200UV01 medication;
         try {
             medication = buildMedication();
         } catch (Exception exception) {
             LOGGER.error(exception.getMessage(), exception);
             exceptions.add(exception);
             return false;
         }

         QUPCAR004030UVServiceUtil.sendPCC10(medication, DEFAULT_PCC_10_END_POINT);
         result = Response.status(Status.OK).build();
         return exceptions.isEmpty();
     }
 
     private QUPCIN043200UV01 buildMedication() throws
             PCC10BuildException, DynamicPropertyTypeException, DynaClassException {
         final DynamicBeanRepository beanRepository;
 
         try {
             beanRepository = JBossJNDILookup.lookupLocal(DynamicBeanRepository.class);
         } catch (NamingException namingException) {
             LOGGER.error(namingException.getMessage(), namingException);
             return null;
         }
 
         // FIXME : use the constats here
         final String medURI =
                 "at.srfg.kmt.ehealth.phrs.datamodel.impl.Medication";
         final Set<DynaBean> allMedications = beanRepository.getAllForClass(medURI);
 
         LOGGER.debug("Try to generate HL7 V3 conform medications for :");
         for (DynaBean medication : allMedications) {
             LOGGER.debug(DynamicUtil.toString(medication));
         }
 
         MEDICATION_FACTORY.setMedication(allMedications);
         final QUPCIN043200UV01 build = MEDICATION_FACTORY.build();
         return build;
     }
 }
