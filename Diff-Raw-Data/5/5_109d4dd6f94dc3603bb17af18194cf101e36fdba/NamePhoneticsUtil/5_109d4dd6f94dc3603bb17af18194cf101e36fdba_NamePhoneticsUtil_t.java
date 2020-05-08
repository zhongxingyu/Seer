 package org.openmrs.module.namephonetics;
 
 import java.text.Normalizer;
 import java.util.List;
 
 import org.apache.commons.codec.EncoderException;
 import org.apache.commons.codec.StringEncoder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.PersonName;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 
 public class NamePhoneticsUtil {
 
         private final static Log log = LogFactory.getLog(NamePhoneticsUtil.class);
     
         public static void savePhoneticsForAllPatients(){
             List<Patient> pList = Context.getPatientService().getAllPatients(false);
             for (Patient p: pList){
                 log.info("beginning phonetics rebuild for patient " + p);
                 savePhoneticsForPerson(p);
             }
             
         }
         
         
         public static void savePhoneticsForPatient(Patient p){
         	savePhoneticsForPerson(p);
         }
         
         
         public static void savePhoneticsForPerson(Person p){
             for (PersonName pn : p.getNames()){
             	savePhoneticsForPersonName(pn);
             }
         }
         
         public static void savePhoneticsForPersonName(PersonName pn){
         	AdministrationService as = Context.getAdministrationService();
             String gpGivenName = as.getGlobalProperty(NamePhoneticsConstants.GIVEN_NAME_GLOBAL_PROPERTY);
             String gpMiddleName = as.getGlobalProperty(NamePhoneticsConstants.MIDDLE_NAME_GLOBAL_PROPERTY);
             String gpFamilyName = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME_GLOBAL_PROPERTY);
             String gpFamilyName2 = as.getGlobalProperty(NamePhoneticsConstants.FAMILY_NAME2_GLOBAL_PROPERTY);
             Context.getService(NamePhoneticsService.class).savePhoneticsForPersonName(pn,  gpGivenName, gpMiddleName,gpFamilyName, gpFamilyName2);
         }
         
         public static String encodeString(String stringToEncode, StringEncoder processor){
 			try {
 				try {
 					return processor.encode(stringToEncode);
 				} catch (IllegalArgumentException iex){
					log.info("An unmapped character was encountered while encoding " + stringToEncode + ".  Trying to normalize...");
 					try {
 						return processor.encode(Normalizer.normalize(stringToEncode, Normalizer.Form.NFD));
 					} catch (IllegalArgumentException e) {
						log.error("Failed to encode " + stringToEncode);
 						e.printStackTrace(System.out);
 						throw new RuntimeException(e.getMessage() + ".   You need to modify your algorithm for your person names.");
 					}
 				}
 			} catch (EncoderException ex){
                 ex.printStackTrace(System.out);
                 throw new RuntimeException("The encoder " + processor.getClass().getName() + " couldn't encode the string " + stringToEncode + ".  Check the system log for details." );
 			}
 
         }
         
 
         public static String encodeString(String stringToEncode, String processorCode){
             StringEncoder processor = null;
             String processorName = null;
             try {
                 
                 NamePhoneticsService nps = Context.getService(NamePhoneticsService.class);
                 log.debug(" trying to encode " + stringToEncode + " with " + processorCode);
                 processorName = nps.getProcessorClassName(processorCode); 
                 if (processorName == null)
                     throw new RuntimeException("Unable to retrieve " + processorCode + " from list of registered namePhoneticsHandlers.");
                 Class<?> clz =  Class.forName(processorName);
                 processor = (StringEncoder) clz.newInstance();
             } catch (ClassNotFoundException cnfe){     
                 log.error("Unable to instantiate class " + processorName); 
                 cnfe.printStackTrace();
             } catch (Exception ex){
                 log.error(ex);
                 ex.printStackTrace();
             }
             if (processor == null)
                 throw new RuntimeException("Could not encode string. Please pass in a valid processorName, such as Soundex, Metaphone, etc...");
             else
                 return encodeString(stringToEncode, processor);
         }
 }
