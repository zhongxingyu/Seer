 package gov.nih.nci.evs.reportwriter.test.lexevs;
 
 import java.util.*;
 
 import gov.nih.nci.evs.reportwriter.test.utils.*;
 import gov.nih.nci.evs.reportwriter.utils.*;
 import gov.nih.nci.evs.utils.StringUtils;
 
 import org.LexGrid.concepts.*;
 import org.apache.log4j.*;
 import org.lexgrid.valuesets.*;
 
 public class GetAssociationSourceCodesTest {
     private static Logger _logger = Logger
         .getLogger(GetAssociationSourceCodesTest.class);
 
     public static void run() throws Exception {
         LexEVSValueSetDefinitionServices definitionServices =
             DataUtils.getValueSetDefinitionService();
         String scheme = "NCI Thesaurus";
         String version = "10.08e";
         String uri = DataUtils.codingSchemeName2URI(scheme, version);
         String code = "C35221";
         code = "C50488";
 
//        Vector<Entity> v =
//            DataUtils.getSuperconceptCodes(definitionServices, uri, scheme,
//                version, code);
//        print(v);
     }
 
     private static void print(Vector<Entity> vector) {
         _logger.debug(StringUtils.SEPARATOR);
         if (vector == null || vector.size() <= 0) {
             _logger.debug("Empty.");
             return;
         }
 
         int i = 0;
         Iterator<Entity> iterator = vector.iterator();
         while (iterator.hasNext()) {
             Entity entity = iterator.next();
             _logger.debug(++i + ") "
                 + entity.getEntityDescription().getContent() + " (Code "
                 + entity.getEntityCode() + ")");
         }
     }
 
     public static void main(String[] args) throws Exception {
         args = SetupEnv.getInstance().parse(args);
         GetAssociationSourceCodesTest.run();
     }
 }
