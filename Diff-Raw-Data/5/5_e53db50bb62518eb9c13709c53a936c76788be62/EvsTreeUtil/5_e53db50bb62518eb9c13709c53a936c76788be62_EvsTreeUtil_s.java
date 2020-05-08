 /**
  *  @author georgeda 
  *  
  *  $Id: EvsTreeUtil.java,v 1.22 2009-06-05 16:52:15 pandyas Exp $  
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.21  2009/06/04 18:48:50  pandyas
  *  Testing disease issue
  *
  *  Revision 1.20  2009/06/04 16:57:56  pandyas
  *  getting ready for QA build
  *
  *  Revision 1.19  2009/06/04 16:27:34  pandyas
  *  The Property object for the Zebrafish vocabulary returns only  Preferred_Name, Synonym, NCI_Preferred_Term.  We used to display the "display_name" for both the NCI_Thesaurus and Zebrafish vocabs
  *
  *  Revision 1.18  2009/06/04 16:12:38  pandyas
  *  testing preferred description in new methods
  *
  *  Revision 1.17  2009/06/04 15:02:00  pandyas
  *  testing preferred description in new methods
  *
  *  Revision 1.16  2009/06/01 16:53:42  pandyas
  *  getting ready for QA build
  *
  *  Revision 1.15  2009/05/28 19:10:13  pandyas
  *  getting ready for QA build
  *
  *  Revision 1.14  2009/05/20 17:11:50  pandyas
  *  modified for gforge #17325 Upgrade caMOD to use caBIO 4.x and EVS 4.x to get data
  *
  *  Revision 1.13  2008/08/14 06:27:33  schroedn
  *  Check for null first
  *
  *  Revision 1.12  2008/01/15 19:31:28  pandyas
  *  Modified debug statements to build to dev tier
  *
  *  Revision 1.11  2008/01/14 21:04:56  pandyas
  *  Enabled logging for dev tier instability issue testing
  *
  *  Revision 1.10  2008/01/14 17:17:48  pandyas
  *  Added to dev instance to look at get Preferred Description error iwth caCORE
  *
  *  Revision 1.9  2007/08/27 15:38:08  pandyas
  *  hide debug code printout
  *
  *  Revision 1.8  2007/08/23 16:11:50  pandyas
  *  Removed extra code
  *
  *  Revision 1.7  2007/08/14 17:05:02  pandyas
  *  Bug #8414:  getEVSPreferredDiscription needs to be implemented for Zebrafish vocabulary source
  *
  *  Revision 1.6  2007/08/14 12:03:59  pandyas
  *  Implementing EVSPreferredName for Zebrafish models
  *
  *  Revision 1.5  2006/08/17 17:59:34  pandyas
  *  Defect# 410: Externalize properties files - Code changes to get properties
  *
  *  Revision 1.4  2006/04/21 13:42:12  georgeda
  *  Cleanup
  *
  *  Revision 1.3  2005/11/03 21:47:56  georgeda
  *  Changed EVS api
  *
  *  Revision 1.2  2005/09/22 13:04:31  georgeda
  *  Added app server call
  *
  *  Revision 1.1  2005/09/21 20:34:59  georgeda
  *  Create util for fetching/caching EVS data
  *
  *  
  */
 package gov.nih.nci.camod.util;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.applicationservice.EVSApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
 import org.LexGrid.concepts.Concept;
 import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
 //import org.LexGrid.relations.Relations;
 import org.LexGrid.commonTypes.Property;
 
 
 /**
  * Static helper class for caching EVS values.
  *
  */
 public class EvsTreeUtil
 {
     static private final Log log = LogFactory.getLog(EvsTreeUtil.class);
     static private Map<String, String> ourDescriptions = new HashMap<String, String>();
 
     private EvsTreeUtil()
     {}
 
 
     /**
      * Get the application service based on the properties file
      *
      * @return the preferred name, or an empty string if something goes wrong.
      */     
     public static ApplicationService getCabioApplicationService()
     {
 		ApplicationService appService = null;
 
 		try {
 			log.info("CaBioApplicationService.getCabioApplicationService Enter : " );
 		
 			appService=ApplicationServiceProvider.getApplicationService("ServiceInfo");
 			
 			log.info("ApplicationService : " + appService.toString());
 
 		}		
 		catch (FileNotFoundException e) {
 			log.error("Caught FileNotFoundException properties for caBIO: ", e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error("Caught IOException finding file for properties for caBIO: ", e);
 			e.printStackTrace();
 		} 
 		catch (Exception e) {
 			log.error("Caught Exception e for caBIO: ", e);
 			e.printStackTrace();
 		}		
 		return appService;
     }
     
 
 
     /**
      * Get the application service based on the properties file
      *
      * @return the application service.
      */
     public static EVSApplicationService getApplicationService()
     {
         // Get the app service uri
 		Properties camodProperties = new Properties();
 		String camodPropertiesFileName = null;
 		EVSApplicationService appService = null;
 
 		camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 
 		try {
 			log.info("EVSApplicationService.getApplicationService Enter : " );
 			// load properties from external file
 			FileInputStream in = new FileInputStream(camodPropertiesFileName);
 			camodProperties.load(in);
 			String serverURL = camodProperties.getProperty("evs.uri");
 			//String serverURL = "http://lexevsapi.nci.nih.gov/lexevsapi42";
 
 			log.info("serverURL : " + serverURL);
 
 			ApplicationServiceProvider applicationServiceProvider = new ApplicationServiceProvider();
 			appService =
 				(EVSApplicationService)applicationServiceProvider.
 				getApplicationService(serverURL);
 
 			log.info("EVSApplicationService : " + appService.toString());
 		}
 		catch (FileNotFoundException e) {
 			log.error("Caught exception finding file for properties for EVS: ", e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error("Caught exception finding file for properties for EVS: ", e);
 			e.printStackTrace();
 		} catch (Exception e) {
 			log.error("Caught exception finding file for properties for EVS: ", e);
 			e.printStackTrace();
 		}
 		return appService;
     }
 
 	public static ConceptReferenceList createConceptReferenceList(String[] codes, String codingSchemeName)
 	{
 		if (codes == null)
 		{
 			return null;
 		}
 		ConceptReferenceList list = new ConceptReferenceList();
 		for (int i = 0; i < codes.length; i++)
 		{
 			ConceptReference cr = new ConceptReference();
 			cr.setCodingScheme(codingSchemeName);
 			cr.setConceptCode(codes[i]);
 			list.addConceptReference(cr);
 		}
 		return list;
 	}
 
 	public static Concept getConceptByCode(String codingSchemeName, String vers, String ltag, String code)
 	{
         try {
 			RemoteServerUtil rsu = new RemoteServerUtil();
 			EVSApplicationService lbSvc = rsu.createLexBIGService();
 			if (lbSvc == null)
 			{
 				System.out.println("lbSvc == null???");
 				return null;
 			}
 
 			CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
 			versionOrTag.setVersion(vers);
 
 			ConceptReferenceList crefs =
 				createConceptReferenceList(
 					new String[] {code}, codingSchemeName);
 
 			CodedNodeSet cns = null;
 
 			try {
 				cns = lbSvc.getCodingSchemeConcepts(codingSchemeName, versionOrTag);
 		    } catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 			cns = cns.restrictToCodes(crefs);
 			ResolvedConceptReferenceList matches = cns.resolveToList(null, null, null, 1);
 
 			if (matches == null)
 			{
 				System.out.println("Concept not found.");
 				return null;
 			}
 
 			// Analyze the result ...
 			if (matches.getResolvedConceptReferenceCount() > 0) {
 				ResolvedConceptReference ref =
 					(ResolvedConceptReference) matches.enumerateResolvedConceptReference().nextElement();
 
 				Concept entry = ref.getReferencedEntry();
 				return entry;
 			}
 		 } catch (Exception e) {
 			 e.printStackTrace();
 			 return null;
 		 }
 		 return null;
 	}
 
 	public static String outputPropertyDetails(Property[] properties)
     {
 		log.info("EvsTreeUtil.outputPropertyDetails Entered");
 
 		String prop_value = "";
 		String evsDisplayNameValue = "";
 		
 		for (int i=0; i<properties.length; i++)
 		{
 			Property property = (Property) properties[i];		
 			String prop_name = property.getPropertyName();
 			log.info("prop_name: " + prop_name);			
 			prop_value = property.getText().getContent();
 			log.info("prop_value: " + prop_value);
			/*if(property.getPropertyName().equals(Constants.Evs.DISPLAY_NAME_TAG) || property.getPropertyName().equals(Constants.Evs.PREFERRED_NAME_TAG)) {
 				log.info("property.getPropertyName(): "  + property.getPropertyName());
 				log.info("prop_value: " + property.getText().getContent());
 				evsDisplayNameValue = property.getText().getContent();				
 				log.info("evsDisplayNameValue: " + evsDisplayNameValue);
 				break;
			} */
 		}
 		log.info("EvsTreeUtil.outputPropertyDetails Exit ");
 		log.info("Final evsDisplayNameValue: " + evsDisplayNameValue);
 		return evsDisplayNameValue;
 	}
 
 	public static String getConceptDetails(String version, String code)
 	{
 		log.info("EvsTreeUtil.getConceptDetails Entered: ");
         String scheme = "";
         String theDescription = ""; 
 		
 		if( code != null ){
             if(code.contains("ZFA")){
                 log.info("Zebrafish modelSpecies");
         		scheme = Constants.Evs.ZEBRAFISH_SCHEMA;
         		//DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG_LOWER_CASE;
         	//Define parameters for all NCI_Thesaurus schema
         	} else {
                 log.info("NOT Zebrafish modelSpecies");
                 scheme = Constants.Evs.NCI_SCHEMA;
         		//DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG;
         	}
 		}
 
         Concept ce = getConceptByCode(scheme, null, null, code);
         if (ce == null)
         {
         	log.info("Concept not found -- " + code);
 		}
 		else
 		{
 			log.info("Concept found -- " + code);
 			log.info("Concept ce.getEntityDescription().getContent()");
 
 			int num_properties = 0;
 
 			Property[] properties = ce.getPresentation();
 			num_properties = num_properties + properties.length;
 
 			theDescription = outputPropertyDetails(properties);
 			log.info("\n theDescription: " + theDescription);
 
 			log.info("\nTotal number of properties: " + num_properties + "\n\n");
 	    }
         return theDescription;
 	}
 
     public static void main(String[] args)
  	{
   		EvsTreeUtil test = new EvsTreeUtil();
 		String scheme = "NCI Thesaurus";
 		String version = null;
 		String code = "C17763";
   		//test.getConceptDetails(scheme, version, code);
 		test.getConceptDetails(version, code);
 
 		scheme = "Zebrafish";
 		version = null;
 		code = "ZFA_0000315";
 		//test.getConceptDetails(scheme, version, code);
 		test.getConceptDetails(version, code);
     }
 }
