 package gov.nih.nci.evs.reportwriter.test.lexevs;
 
 import java.util.*;
 
 import gov.nih.nci.evs.reportwriter.service.*;
 import gov.nih.nci.evs.reportwriter.test.utils.*;
 import gov.nih.nci.evs.reportwriter.utils.*;
 import gov.nih.nci.evs.utils.*;
 
 import org.LexGrid.concepts.*;
 import org.apache.log4j.*;
 
 public class DataUtilsTest {
     private static Logger _logger = Logger.getLogger(DataUtilsTest.class);
     
     public static void main(String[] args) {
         args = SetupEnv.getInstance().parse(args);
         DataUtilsTest test = new DataUtilsTest();
         test.getAssociations();
         //test.generateStandardReport();
     }
     
     public void getAssociations() {
         String scheme = "NCI Thesaurus";
         String version = "09.12d";
         String code = "C74456";  // CDISC SDTM Anatomical Location Terminology
         code = "C63923"; // FDA Established Names and Unique Ingredient Identifier Codes Terminology
         String assocName = "A8";
         boolean retrieveTargets = false;
         boolean showAll = true;
 
         _logger.info(StringUtils.SEPARATOR);
         _logger.info("Calling: getAssociations");
         _logger.info("  * retrieveTargets: " + retrieveTargets);
         _logger.info("  * scheme: " + scheme);
         _logger.info("  * version: " + version);
         _logger.info("  * code: " + code);
         _logger.info("  * assocName: " + assocName);
        Vector<Concept> v = DataUtils.getAssociations(
             retrieveTargets, scheme, version, code, assocName);
         int i=0, n=v.size();
        Iterator<Concept> iterator = v.iterator();
         _logger.info("Results: (size=" + n + ")");
         while (iterator.hasNext()) {
            Concept concept = iterator.next();
             if (showAll || i%100 == 0 || i+1 == n) {
                 String c_code = concept.getEntityCode();
                 String description = concept.getEntityDescription().getContent();
                 _logger.info("  " + i + ") " + c_code + ": " + description);
             }
             ++i;
         }
     }
     
     public void generateStandardReport() {
         String outputDir = "c:/apps/evs/ncireportwriter-webapp/downloads";
         String standardReportLabel = "FDA-UNII Subset REPORT Test";
         String uid = "rwadmin";
         String emailAddress = "yeed@mail.nih.gov";
         
         _logger.info("Calling: generateStandardReport");
         _logger.info("  * outputDir: " + outputDir);
         _logger.info("  * standardReportLabel: " + standardReportLabel);
         _logger.info("  * uid: " + uid);
 
         StringBuffer warningMsg = new StringBuffer();
         ReportGenerationThread thread = new ReportGenerationThread(
             outputDir, standardReportLabel, uid, emailAddress);
         Boolean successful = thread.generateStandardReport(outputDir,
             standardReportLabel, uid, warningMsg);
         _logger.info("  * successful: " + successful);
         _logger.info("  * warningMsg: " + warningMsg.toString());
     }
 }
