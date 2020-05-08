 /*
  * Created on May 23, 2005
  *
  */
 package fedora.test.integration;
 
 import java.io.UnsupportedEncodingException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 
 import junit.extensions.TestSetup;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.custommonkey.xmlunit.SimpleXpathEngine;
 import fedora.client.APIMStubFactory;
 import fedora.client.FedoraClient;
 import fedora.server.management.FedoraAPIM;
 
 
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.Disseminator;
 import fedora.server.types.gen.DatastreamBinding;
 import fedora.server.types.gen.DatastreamBindingMap;
 import fedora.test.FedoraServerTestCase;
 import fedora.test.FedoraServerTestSetup;
 
 import org.apache.axis.types.NonNegativeInteger;
 
 /**
  * @author rlw@virginia.edu
  * @version $Id$
  * 
  */
 public class TestAPIM extends FedoraServerTestCase {
     private FedoraAPIM apim;
     
     private static DocumentBuilderFactory factory;
     private static DocumentBuilder builder;
     private static FedoraClient client;    
     
     public static byte[] dsXML;
     public static byte[] demo998FOXMLObjectXML;
     public static byte[] demo999METSObjectXML;
     public static byte[] changeme1FOXMLObjectXML;
     public static byte[] changeme2METSObjectXML;
     
     static {
         
         // create test xml datastream
         StringBuffer sb = new StringBuffer();
         sb.append("<oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\">");
         sb.append("<dc:title>Dublin Core Record 5</dc:title>");
         sb.append("<dc:creator>Author 5</dc:creator>");
         sb.append("<dc:subject>Subject 5</dc:subject>");
         sb.append("<dc:description>Description 5</dc:description>");
         sb.append("<dc:publisher>Publisher 5</dc:publisher>");
         sb.append("<dc:format>MIME type 5</dc:format>");
         sb.append("<dc:identifier>Identifier 5</dc:identifier>");
         sb.append("</oai_dc:dc>");
         try {
             dsXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee)
         {}
         
         // create test FOXML object specifying pid=demo:998
         sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<foxml:digitalObject PID=\"demo:998\" xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:audit=\"info:fedora/fedora-system:def/audit#\" xmlns:fedoraAudit=\"http://fedora.comm.nsdlib.org/audit\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\" xmlns:uvalibadmin=\"http://dl.lib.virginia.edu/bin/dtd/admin/admin.dtd\" xmlns:uvalibdesc=\"http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd\" xmlns:xlink=\"http://www.w3.org/TR/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd\">");
         sb.append("  <foxml:objectProperties>");
         sb.append("    <foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"Image of Coliseum in Rome\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\"2004-12-10T00:21:57Z\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\"2004-12-10T00:21:57Z\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"UVA_STD_IMAGE\"/>");
         sb.append("  </foxml:objectProperties>");
         sb.append("  <foxml:datastream ID=\"DC\" CONTROL_GROUP=\"X\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DC1.0\" MIMETYPE=\"text/xml\" LABEL=\"DC Record for Coliseum image object\">");
         sb.append("	     <foxml:xmlContent>");
         sb.append("        <oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\">");
         sb.append("          <dc:title>Coliseum in Rome</dc:title>");
         sb.append("          <dc:creator>Thornton Staples</dc:creator>");
         sb.append("          <dc:subject>Architecture, Roman</dc:subject>");
         sb.append("          <dc:description>Image of Coliseum in Rome</dc:description>");
         sb.append("          <dc:publisher>University of Virginia Library</dc:publisher>");
         sb.append("          <dc:format>image/jpeg</dc:format>");
         sb.append("          <dc:identifier>demo:5</dc:identifier>");
         sb.append("        </oai_dc:dc>");
         sb.append("      </foxml:xmlContent>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS1\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS1.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum thumbnail jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-thumb.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS2\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS2.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum medium jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-medium.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS3\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS3.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum high jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-high.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS4\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS4.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum veryhigh jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-veryhigh.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:disseminator ID=\"DISS1\" BDEF_CONTRACT_PID=\"demo:1\" STATE=\"A\">");
         sb.append("    <foxml:disseminatorVersion ID=\"DISS1.0\" BMECH_SERVICE_PID=\"demo:2\" LABEL=\"UVA Simple Image Behaviors\">");
         sb.append("      <foxml:serviceInputMap>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS1\" KEY=\"THUMBRES_IMG\" LABEL=\"Binding to thumbnail photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS2\" KEY=\"MEDRES_IMG\" LABEL=\"Binding to medium resolution photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS3\" KEY=\"HIGHRES_IMG\" LABEL=\"Binding to high resolution photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS4\" KEY=\"VERYHIGHRES_IMG\" LABEL=\"Binding to very high resolution photo of Coliseum\"/>");
         sb.append("      </foxml:serviceInputMap>");
         sb.append("    </foxml:disseminatorVersion>");
         sb.append("  </foxml:disseminator>");
         sb.append("</foxml:digitalObject>");
                
         try {
             demo998FOXMLObjectXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee)
         {}             
 
         // create test FOXML object not specifying pid (allow server to assign)
         sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<foxml:digitalObject xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:audit=\"info:fedora/fedora-system:def/audit#\" xmlns:fedoraAudit=\"http://fedora.comm.nsdlib.org/audit\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\" xmlns:uvalibadmin=\"http://dl.lib.virginia.edu/bin/dtd/admin/admin.dtd\" xmlns:uvalibdesc=\"http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd\" xmlns:xlink=\"http://www.w3.org/TR/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd\">");
         sb.append("  <foxml:objectProperties>");
         sb.append("    <foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"Image of Coliseum in Rome\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\"2004-12-10T00:21:57Z\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\"2004-12-10T00:21:57Z\"/>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"UVA_STD_IMAGE\"/>");
         sb.append("  </foxml:objectProperties>");
         sb.append("  <foxml:datastream ID=\"DC\" CONTROL_GROUP=\"X\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DC1.0\" MIMETYPE=\"text/xml\" LABEL=\"DC Record for Coliseum image object\">");
         sb.append("	     <foxml:xmlContent>");
         sb.append("        <oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\">");
         sb.append("          <dc:title>Coliseum in Rome</dc:title>");
         sb.append("          <dc:creator>Thornton Staples</dc:creator>");
         sb.append("          <dc:subject>Architecture, Roman</dc:subject>");
         sb.append("          <dc:description>Image of Coliseum in Rome</dc:description>");
         sb.append("          <dc:publisher>University of Virginia Library</dc:publisher>");
         sb.append("          <dc:format>image/jpeg</dc:format>");
         sb.append("          <dc:identifier>demo:5</dc:identifier>");
         sb.append("        </oai_dc:dc>");
         sb.append("      </foxml:xmlContent>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS1\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS1.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum thumbnail jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-thumb.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS2\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS2.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum medium jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-medium.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS3\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS3.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum high jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-high.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:datastream ID=\"DS4\" CONTROL_GROUP=\"M\" STATE=\"A\">");
         sb.append("    <foxml:datastreamVersion ID=\"DS4.0\" MIMETYPE=\"image/jpeg\" LABEL=\"Thorny's Coliseum veryhigh jpg image\">");
         sb.append("      <foxml:contentLocation REF=\"http://localhost:8080/demo/simple-image-demo/coliseum-veryhigh.jpg\" TYPE=\"URL\"/>");
         sb.append("    </foxml:datastreamVersion>");
         sb.append("  </foxml:datastream>");
         sb.append("  <foxml:disseminator ID=\"DISS1\" BDEF_CONTRACT_PID=\"demo:1\" STATE=\"A\">");
         sb.append("    <foxml:disseminatorVersion ID=\"DISS1.0\" BMECH_SERVICE_PID=\"demo:2\" LABEL=\"UVA Simple Image Behaviors\">");
         sb.append("      <foxml:serviceInputMap>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS1\" KEY=\"THUMBRES_IMG\" LABEL=\"Binding to thumbnail photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS2\" KEY=\"MEDRES_IMG\" LABEL=\"Binding to medium resolution photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS3\" KEY=\"HIGHRES_IMG\" LABEL=\"Binding to high resolution photo of Coliseum\"/>");
         sb.append("        <foxml:datastreamBinding DATASTREAM_ID=\"DS4\" KEY=\"VERYHIGHRES_IMG\" LABEL=\"Binding to very high resolution photo of Coliseum\"/>");
         sb.append("      </foxml:serviceInputMap>");
         sb.append("    </foxml:disseminatorVersion>");
         sb.append("  </foxml:disseminator>");
         sb.append("</foxml:digitalObject>");
                
         try {
             changeme1FOXMLObjectXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee)
         {}        
         
         // create test METS object specifying pid=demo:999        
         sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<!---************************************************************************-->");
         sb.append("<!-- SAMPLE FEDORA DIGITAL OBJECT ENCODED USING METS -->");
         sb.append("<!-- Creator: Ross Wayland -->");
         sb.append("<!-- Custodian: University of Virginia Library -->");
         sb.append("<!-- Note: Demonstration Digital Object.  This Fedora data object demonstrates the UVA Simple Image -->");
         sb.append("<!-- behaviors being fulfilled via the Fedora HTTP Image Getter service.  There are 4 datastreams in the -->");
         sb.append("<!-- object, one for each image resolution specified in the behavior definition.  The fulfillment of the behavior -->");
         sb.append("<!-- contract entails the Fedora HTTP Image Getter resolving the URL of the appropriate datastream for -->");
         sb.append("<!-- each of the UVA Simple Image behaviors.  There are no transformations performed on the datastreams. -->");
         sb.append("<!---************************************************************************-->");
         sb.append("<METS:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:fedoraAudit=\"http://fedora.comm.nsdlib.org/audit\" xmlns:uvalibdesc=\"http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd\" xmlns:uvalibadmin=\"http://dl.lib.virginia.edu/bin/dtd/admin/admin.dtd\" xmlns:xlink=\"http://www.w3.org/TR/xlink\" xsi:schemaLocation=\"http://www.loc.gov/standards/METS/ http://www.fedora.info/definitions/1/0/mets-fedora-ext.xsd\" OBJID=\"demo:999\" TYPE=\"FedoraObject\" LABEL=\"Image of Coliseum in Rome\" PROFILE=\"UVA_STD_IMAGE\">");
        	sb.append("  <!---*******************************************************************************************************************************************-->");
        	sb.append("  <!---User-Defined XML METADATA DATASTREAMS-->");
       	sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:dmdSecFedora ID=\"DC\" STATUS=\"A\">");
         sb.append("    <METS:descMD ID=\"DC1.0\">");
         sb.append("      <METS:mdWrap MIMETYPE=\"text/xml\" MDTYPE=\"OTHER\" LABEL=\"DC Record for Coliseum image object\">");
         sb.append("        <METS:xmlData>");
         sb.append("          <!-- This schema described at http://www.openarchives.org/OAI/openarchivesprotocol.html#dublincore -->");
         sb.append("          <oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
         sb.append("            <dc:title>Coliseum in Rome</dc:title>");
         sb.append("            <dc:creator>Thornton Staples</dc:creator>");
         sb.append("            <dc:subject>Architecture, Roman</dc:subject>");
         sb.append("            <dc:description>Image of Coliseum in Rome</dc:description>");
         sb.append("            <dc:publisher>University of Virginia Library</dc:publisher>");
         sb.append("            <dc:format>image/jpeg</dc:format>");
         sb.append("            <dc:identifier>demo:5</dc:identifier>");
         sb.append("          </oai_dc:dc>");
         sb.append("        </METS:xmlData>");
         sb.append("      </METS:mdWrap>");
         sb.append("    </METS:descMD>");
         sb.append("  </METS:dmdSecFedora>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- DATASTREAMS:  EXTERNAL-REFERENCED CONTENT and REPOSITORY-MANAGED CONTENT-->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:fileSec>");
         sb.append("    <METS:fileGrp ID=\"DATASTREAMS\">");
         sb.append("      <METS:fileGrp ID=\"DS1\" STATUS=\"A\">");
         sb.append("        <!--This is the thumbnail resolution image -->");
         sb.append("        <METS:file ID=\"DS1.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-thumb.jpg\" xlink:title=\"Thorny's Coliseum thumbnail jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS2\" STATUS=\"A\">");
         sb.append("        <!-- This is the medium resoluion image -->");
         sb.append("        <METS:file ID=\"DS2.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-medium.jpg\" xlink:title=\"Thorny's Coliseum medium jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS3\">");
         sb.append("        <!--This is the high resolution image -->");
         sb.append("          <METS:file ID=\"DS3.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("            <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-high.jpg\" xlink:title=\"Thorny's Coliseum high jpg image\"/>");
         sb.append("          </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS4\">");
         sb.append("        <!--This is the very high resolution image -->");
         sb.append("        <METS:file ID=\"DS4.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-veryhigh.jpg\" xlink:title=\"Thorny's Coliseum veryhigh jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("    </METS:fileGrp>");
         sb.append("  </METS:fileSec>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- FEDORA DATASTREAM BINDING MAPS  -->");
         sb.append("  <!--  In Fedora the METS structure maps are associated with the DISSEMINATOR mechanisms (see METS:behaviorSec).-->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:structMap ID=\"S1\" TYPE=\"fedora:dsBindingMap\">");
         sb.append("    <METS:div TYPE=\"demo:2\" LABEL=\"DS Binding Map for Fedora HTTP Image Getter Mechanism\">");
         sb.append("      <METS:div TYPE=\"THUMBRES_IMG\" LABEL=\"Binding to thumbnail photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS1\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"MEDRES_IMG\" LABEL=\"Binding to medium resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS2\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"HIGHRES_IMG\" LABEL=\"Binding to high resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS3\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"VERYHIGHRES_IMG\" LABEL=\"Binding to very high resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS4\"/>");
         sb.append("      </METS:div>");
         sb.append("    </METS:div>");
         sb.append("  </METS:structMap>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- DISSEMINATORS-->");
         sb.append("  <!-- A disseminator provides a \"behavior-centric\" view of the object.  It has an attribute STRUCTID -->");
         sb.append("  <!-- which is an IDREF to a mechanism-specific structMap.  The structMap labels datastreams in -->");
         sb.append("  <!-- a manner that is understood by the mechanism defined in the disseminator.  -->");
         sb.append("  <!-- -->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- THE UVA STANDARD IMAGE DISSEMINATOR -->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:behaviorSec ID=\"DISS1\" STATUS=\"A\">");
         sb.append("    <METS:serviceBinding ID=\"DISS1.0\" STRUCTID=\"S1\" BTYPE=\"demo:1\" LABEL=\"UVA Simple Image Behaviors\">");
         sb.append("      <!-- Use xlink to point to a Fedora Behavior Definition object which is stored as a separate METS object.-->");
         sb.append("      <!--The Behavior Definition object stores WSDL interface descriptions that formally define the set methods to which -->");
         sb.append("      <!-- the disseminator subscribes -->");
         sb.append("      <METS:interfaceMD LABEL=\"UVA Simple Image Behavior Definition\" LOCTYPE=\"URN\" xlink:href=\"demo:1\"/>");
         sb.append("        <!--Use xlink  to point to the Behavior Mechanism object which will be stored as a separate METS object.-->");
         sb.append("        <!--The Behavior Mechanism object stores WSDL bindings to run methods described in Behavior Definition object.-->");
         sb.append("      <METS:serviceBindMD LABEL=\"Fedora HTTP Image Getter Behavior Mechanism\" LOCTYPE=\"URN\" xlink:href=\"demo:2\"/>");
         sb.append("    </METS:serviceBinding>");
         sb.append("  </METS:behaviorSec>");
         sb.append("</METS:mets>");
 
         try {
             demo999METSObjectXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee)
         {}           
         
         // create test METS object not specifying pid (allowing server to assign)        
         sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<!---************************************************************************-->");
         sb.append("<!-- SAMPLE FEDORA DIGITAL OBJECT ENCODED USING METS -->");
         sb.append("<!-- Creator: Ross Wayland -->");
         sb.append("<!-- Custodian: University of Virginia Library -->");
         sb.append("<!-- Note: Demonstration Digital Object.  This Fedora data object demonstrates the UVA Simple Image -->");
         sb.append("<!-- behaviors being fulfilled via the Fedora HTTP Image Getter service.  There are 4 datastreams in the -->");
         sb.append("<!-- object, one for each image resolution specified in the behavior definition.  The fulfillment of the behavior -->");
         sb.append("<!-- contract entails the Fedora HTTP Image Getter resolving the URL of the appropriate datastream for -->");
         sb.append("<!-- each of the UVA Simple Image behaviors.  There are no transformations performed on the datastreams. -->");
         sb.append("<!---************************************************************************-->");
         sb.append("<METS:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:METS=\"http://www.loc.gov/METS/\" xmlns:fedoraAudit=\"http://fedora.comm.nsdlib.org/audit\" xmlns:uvalibdesc=\"http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd\" xmlns:uvalibadmin=\"http://dl.lib.virginia.edu/bin/dtd/admin/admin.dtd\" xmlns:xlink=\"http://www.w3.org/TR/xlink\" xsi:schemaLocation=\"http://www.loc.gov/standards/METS/ http://www.fedora.info/definitions/1/0/mets-fedora-ext.xsd\" TYPE=\"FedoraObject\" LABEL=\"Image of Coliseum in Rome\" PROFILE=\"UVA_STD_IMAGE\">");
        	sb.append("  <!---*******************************************************************************************************************************************-->");
        	sb.append("  <!---User-Defined XML METADATA DATASTREAMS-->");
       	sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:dmdSecFedora ID=\"DC\" STATUS=\"A\">");
         sb.append("    <METS:descMD ID=\"DC1.0\">");
         sb.append("      <METS:mdWrap MIMETYPE=\"text/xml\" MDTYPE=\"OTHER\" LABEL=\"DC Record for Coliseum image object\">");
         sb.append("        <METS:xmlData>");
         sb.append("          <!-- This schema described at http://www.openarchives.org/OAI/openarchivesprotocol.html#dublincore -->");
         sb.append("          <oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
         sb.append("            <dc:title>Coliseum in Rome</dc:title>");
         sb.append("            <dc:creator>Thornton Staples</dc:creator>");
         sb.append("            <dc:subject>Architecture, Roman</dc:subject>");
         sb.append("            <dc:description>Image of Coliseum in Rome</dc:description>");
         sb.append("            <dc:publisher>University of Virginia Library</dc:publisher>");
         sb.append("            <dc:format>image/jpeg</dc:format>");
         sb.append("            <dc:identifier>demo:5</dc:identifier>");
         sb.append("          </oai_dc:dc>");
         sb.append("        </METS:xmlData>");
         sb.append("      </METS:mdWrap>");
         sb.append("    </METS:descMD>");
         sb.append("  </METS:dmdSecFedora>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- DATASTREAMS:  EXTERNAL-REFERENCED CONTENT and REPOSITORY-MANAGED CONTENT-->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:fileSec>");
         sb.append("    <METS:fileGrp ID=\"DATASTREAMS\">");
         sb.append("      <METS:fileGrp ID=\"DS1\" STATUS=\"A\">");
         sb.append("        <!--This is the thumbnail resolution image -->");
         sb.append("        <METS:file ID=\"DS1.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-thumb.jpg\" xlink:title=\"Thorny's Coliseum thumbnail jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS2\" STATUS=\"A\">");
         sb.append("        <!-- This is the medium resoluion image -->");
         sb.append("        <METS:file ID=\"DS2.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-medium.jpg\" xlink:title=\"Thorny's Coliseum medium jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS3\">");
         sb.append("        <!--This is the high resolution image -->");
         sb.append("          <METS:file ID=\"DS3.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("            <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-high.jpg\" xlink:title=\"Thorny's Coliseum high jpg image\"/>");
         sb.append("          </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("      <METS:fileGrp ID=\"DS4\">");
         sb.append("        <!--This is the very high resolution image -->");
         sb.append("        <METS:file ID=\"DS4.0\" MIMETYPE=\"image/jpeg\" OWNERID=\"M\" STATUS=\"A\">");
         sb.append("          <METS:FLocat LOCTYPE=\"URL\" xlink:href=\"http://localhost:8080/demo/simple-image-demo/coliseum-veryhigh.jpg\" xlink:title=\"Thorny's Coliseum veryhigh jpg image\"/>");
         sb.append("        </METS:file>");
         sb.append("      </METS:fileGrp>");
         sb.append("    </METS:fileGrp>");
         sb.append("  </METS:fileSec>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- FEDORA DATASTREAM BINDING MAPS  -->");
         sb.append("  <!--  In Fedora the METS structure maps are associated with the DISSEMINATOR mechanisms (see METS:behaviorSec).-->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:structMap ID=\"S1\" TYPE=\"fedora:dsBindingMap\">");
         sb.append("    <METS:div TYPE=\"demo:2\" LABEL=\"DS Binding Map for Fedora HTTP Image Getter Mechanism\">");
         sb.append("      <METS:div TYPE=\"THUMBRES_IMG\" LABEL=\"Binding to thumbnail photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS1\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"MEDRES_IMG\" LABEL=\"Binding to medium resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS2\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"HIGHRES_IMG\" LABEL=\"Binding to high resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS3\"/>");
         sb.append("      </METS:div>");
         sb.append("      <METS:div TYPE=\"VERYHIGHRES_IMG\" LABEL=\"Binding to very high resolution photo of Coliseum \" ORDER=\"0\">");
         sb.append("        <METS:fptr FILEID=\"DS4\"/>");
         sb.append("      </METS:div>");
         sb.append("    </METS:div>");
         sb.append("  </METS:structMap>");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- DISSEMINATORS-->");
         sb.append("  <!-- A disseminator provides a \"behavior-centric\" view of the object.  It has an attribute STRUCTID -->");
         sb.append("  <!-- which is an IDREF to a mechanism-specific structMap.  The structMap labels datastreams in -->");
         sb.append("  <!-- a manner that is understood by the mechanism defined in the disseminator.  -->");
         sb.append("  <!-- -->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <!--- THE UVA STANDARD IMAGE DISSEMINATOR -->");
         sb.append("  <!---*******************************************************************************************************************************************-->");
         sb.append("  <METS:behaviorSec ID=\"DISS1\" STATUS=\"A\">");
         sb.append("    <METS:serviceBinding ID=\"DISS1.0\" STRUCTID=\"S1\" BTYPE=\"demo:1\" LABEL=\"UVA Simple Image Behaviors\">");
         sb.append("      <!-- Use xlink to point to a Fedora Behavior Definition object which is stored as a separate METS object.-->");
         sb.append("      <!--The Behavior Definition object stores WSDL interface descriptions that formally define the set methods to which -->");
         sb.append("      <!-- the disseminator subscribes -->");
         sb.append("      <METS:interfaceMD LABEL=\"UVA Simple Image Behavior Definition\" LOCTYPE=\"URN\" xlink:href=\"demo:1\"/>");
         sb.append("        <!--Use xlink  to point to the Behavior Mechanism object which will be stored as a separate METS object.-->");
         sb.append("        <!--The Behavior Mechanism object stores WSDL bindings to run methods described in Behavior Definition object.-->");
         sb.append("      <METS:serviceBindMD LABEL=\"Fedora HTTP Image Getter Behavior Mechanism\" LOCTYPE=\"URN\" xlink:href=\"demo:2\"/>");
         sb.append("    </METS:serviceBinding>");
         sb.append("  </METS:behaviorSec>");
         sb.append("</METS:mets>");
 
         try {
             changeme2METSObjectXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee)
         {}              
         
     }
     
     public static Test suite() {
         TestSuite suite = new TestSuite("APIM TestSuite");
         suite.addTestSuite(TestAPIM.class);
         
         TestSetup wrapper = new TestSetup(suite) {
             public void setUp() throws Exception {
                 TestIngestDemoObjects.ingestDemoObjects();
                 SimpleXpathEngine.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                 SimpleXpathEngine.registerNamespace("dc", "http://purl.org/dc/elements/1.1/");
             }
             
             public void tearDown() throws Exception {
                 TestIngestDemoObjects.purgeDemoObjects();
                 SimpleXpathEngine.clearNamespaces();
             }
         };
         return new FedoraServerTestSetup(wrapper);
     }
     
     public void setUp() throws Exception {
         apim = APIMStubFactory.getStub(getProtocol(), getHost(), 
                 Integer.parseInt(getPort()), getUsername(), getPassword());
         SimpleXpathEngine.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
         SimpleXpathEngine.registerNamespace("dc", "http://purl.org/dc/elements/1.1/");
         SimpleXpathEngine.registerNamespace("foxml", "info:fedora/fedora-system:def/foxml#");
     }
     
     public void tearDown() throws Exception {
         SimpleXpathEngine.clearNamespaces();
     }
     
     public void testGetObjectXML() throws Exception {
         
         // test getting xml for object demo:5
         byte [] objectXML = apim.getObjectXML("demo:5");
         assertTrue(objectXML.length > 0);
         String xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testGetObjectXML demo:5\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:5']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
     }    
     
     public void testObjectMethods() throws Exception {
         
         // test the object methods
         // 1) ingest
         // 2) modifyObject
         // 3) export
         // 4) purgeObject
         
         
         // (1) test ingest
         String pid = apim.ingest(demo998FOXMLObjectXML, "foxml1.0", "ingesting new foxml object");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest demo998FOXML: "+pid);
         assertNotNull(pid);
         
         byte [] objectXML = apim.getObjectXML(pid);
         assertTrue(objectXML.length > 0);
         String xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest demo998FOXML XML \n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='"+pid+"']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
         
         pid = apim.ingest(changeme1FOXMLObjectXML, "foxml1.0", "ingesting new foxml object");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest changeme1FOXML: "+pid);
         assertNotNull(pid);
         
         objectXML = apim.getObjectXML(pid);
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest changeme1FOXML XML \n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='"+pid+"']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
         
         pid = apim.ingest(demo999METSObjectXML, "metslikefedora1", "ingesting new foxml object");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest demo999METS: "+pid);
         assertNotNull(pid);
         
         objectXML = apim.getObjectXML(pid);
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest demo999METS XML\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='"+pid+"']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);        
         
         pid = apim.ingest(changeme2METSObjectXML, "metslikefedora1", "ingesting new foxml object");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest changeme2METS: "+pid);
         assertNotNull(pid);
         
         objectXML = apim.getObjectXML(pid);
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testIngestObject ingest changeme2METS XML \n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='"+pid+"']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
         
         // (2) test modifyObject
         
         // test changing object demo:5 by modifying state to Inactive; leave label unchanged
         String result = apim.modifyObject("demo:5", "I", null, "changed state to Inactive");
         
         objectXML = apim.getObjectXML("demo:5");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyObject demo:5\n"+xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state'and @VALUE='Inactive']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label'and @VALUE='Image of Coliseum in Rome']",xmlIn);
         
         // test changing object demo:5 by modifying label to "changed label"; leave state unchanged from last value
         result = apim.modifyObject("demo:5", null, "changed label", "changed label");
         objectXML = apim.getObjectXML("demo:5");
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyObject demo:5\n"+xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Inactive']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='changed label']",xmlIn);
         
         // test changing object demo:5 by modifying both state and label
         result = apim.modifyObject("demo:5", "D", "label of object to be deleted", "changed label and state");
         objectXML = apim.getObjectXML("demo:5");
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyObject demo:5\n"+xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Deleted']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='label of object to be deleted']",xmlIn);
     
         // (3) test export
         
         // test exporting object as foxml with exportContext of default
         objectXML = apim.export("demo:998", "foxml1.0", "default");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:998 as FOXML\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:998']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
 
         // test exporting object as foxml with exportContext of default
         objectXML = apim.export("demo:998", "foxml1.0", "public");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:998 as FOXML\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:998']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
         
         // test exporting object as foxml with exportContext of migrate
         objectXML = apim.export("demo:998", "foxml1.0", "migrate");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:998 as FOXML\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:998']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);        
         
         // test exporting object as mets with exportContext of default
         objectXML = apim.export("demo:999", "metslikefedora1", "default");
         objectXML = apim.getObjectXML("demo:999");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:999 as METS\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:999']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);           
         
         // test exporting object as mets with exportContext of public
         objectXML = apim.export("demo:999", "metslikefedora1", "public");
         objectXML = apim.getObjectXML("demo:999");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:999 as METS\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:999']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);
         
         // test exporting object as mets with exportContext of migrate
         objectXML = apim.export("demo:999", "metslikefedora1", "migrate");
         objectXML = apim.getObjectXML("demo:999");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testExport demo:999 as METS\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:999']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']",xmlIn);        
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#label' and @VALUE='Image of Coliseum in Rome']",xmlIn);
         assertXpathExists("//foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#contentModel' and @VALUE='UVA_STD_IMAGE']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         assertXpathEvaluatesTo("1", "count(//foxml:disseminator)",xmlIn);        
         
         // (4) test purgeObject
         
         // test puring object demo:999
         result = apim.purgeObject("demo:999", "purging object demo:999", false);
         System.out.println("***** Testcase: TestAPIM.testPurgeObject demo:999\n");
         assertNotNull(result);       
         
     }
     
     public void testDatastreamMethods() throws Exception {
         
         // test datastream methods
         // 1) addDatastream
         // 2) modifyDatastreamByReference
         // 3) modifyDatastreamByValue
         // 4) purgeDatastream
         // 5) getDatastream
         // 6) getDatastreams
         // 7) getDatastreamHistory
         
         // (1) test addDatastream
         
         // test adding M type datastream
         String[] altIds = new String[1];
         altIds[0] = "Datastream 1 Alternate ID";
         String result = apim.addDatastream("demo:14", "NEWDS1", altIds, "A New M-type Datastream", true, "text/xml", "info:myFormatURI/Mtype/stuff#junk", "http://www.fedora.info/junit/datastream1.xml", "M", "A", "adding new datastream");
 
         // test that datastream was added
         byte [] objectXML = apim.getObjectXML("demo:14");
         assertTrue(objectXML.length > 0);
         String xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testAddDatastream NEWDS1 as type M\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:14']",xmlIn);
         assertXpathExists("//foxml:datastream[@ID='NEWDS1' and @CONTROL_GROUP='M' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:datastreamVersion[@ID='NEWDS1.0' and @MIMETYPE='text/xml' and @LABEL='A New M-type Datastream' and @ALT_IDS='Datastream 1 Alternate ID' and @FORMAT_URI='info:myFormatURI/Mtype/stuff#junk']",xmlIn);
         assertXpathEvaluatesTo("5", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         
         //test adding X type datastream
         altIds[0] = "Datastream 2 Alternate ID";
         result = apim.addDatastream("demo:14", "NEWDS2", altIds, "A New X-type Datastream", true, "text/xml", "info:myFormatURI/Xtype/stuff#junk", "http://www.fedora.info/junit/datastream2.xml", "X", "A", "adding new datastream");
 
         // test that datastream was added
         objectXML = apim.getObjectXML("demo:14");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testAddDatastream NEWDS2 as type X\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:14']",xmlIn);
         assertXpathExists("//foxml:datastream[@ID='NEWDS2' and @CONTROL_GROUP='X' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:datastreamVersion[@ID='NEWDS2.0' and @MIMETYPE='text/xml' and @LABEL='A New X-type Datastream' and @ALT_IDS='Datastream 2 Alternate ID' and @FORMAT_URI='info:myFormatURI/Xtype/stuff#junk']",xmlIn);
         assertXpathEvaluatesTo("6", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);
         
         altIds[0] = "Datastream 3 Alternate ID";
         result = apim.addDatastream("demo:14", "NEWDS3", altIds, "A New E-type Datastream", true, "text/xml", "info:myFormatURI/Etype/stuff#junk", "http://www.fedora.info/junit/datastream3.xml", "E", "A", "adding new datastream");
 
         // test adding E type datastream
         objectXML = apim.getObjectXML("demo:14");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testAddDatastream NEWDS3 as type E\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:14']",xmlIn);
         assertXpathExists("//foxml:datastream[@ID='NEWDS3' and @CONTROL_GROUP='E' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:datastreamVersion[@ID='NEWDS3.0' and @MIMETYPE='text/xml' and @LABEL='A New E-type Datastream' and @ALT_IDS='Datastream 3 Alternate ID' and @FORMAT_URI='info:myFormatURI/Etype/stuff#junk']",xmlIn);
         assertXpathEvaluatesTo("7", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);             
 
         // (2) test modifyDatastreamByReference
         
         altIds = new String[1];
         altIds[0] = "Datastream 1 Modified Alternate ID";
         result = apim.modifyDatastreamByReference("demo:14", "NEWDS1", altIds, "Modified M-type Datastream", true, "text/xml", "info:newMyFormatURI/Mtype/stuff#junk", "http://www.fedora.info/junit/datastream2.xml", "A", "modified datastream", false);
 
         // test that datastream was modified
         objectXML = apim.getObjectXML("demo:14");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyDatastreamByReference NEWDS1\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:14']",xmlIn);
         assertXpathExists("//foxml:datastream[@ID='NEWDS1' and @CONTROL_GROUP='M' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:datastreamVersion[@ID='NEWDS1.1' and @MIMETYPE='text/xml' and @LABEL='Modified M-type Datastream' and @ALT_IDS='Datastream 1 Modified Alternate ID' and @FORMAT_URI='info:newMyFormatURI/Mtype/stuff#junk']",xmlIn);
         assertXpathEvaluatesTo("7", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);           
 
         // (3) test modifyDatastreamByValue
         
         altIds = new String[1];
         altIds[0] = "Datastream 2 Modified Alternate ID";
         result = apim.modifyDatastreamByValue("demo:14", "NEWDS2", altIds, "Modified X-type Datastream", true, "text/xml", "info:newMyFormatURI/Xtype/stuff#junk", dsXML, "A", "modified datastream", false);
 
         // test that datastream was modified
         objectXML = apim.getObjectXML("demo:14");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyDatastreamByValue NEWDS2\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:14']",xmlIn);
         assertXpathExists("//foxml:datastream[@ID='NEWDS2' and @CONTROL_GROUP='X' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:datastreamVersion[@ID='NEWDS2.1' and @MIMETYPE='text/xml' and @LABEL='Modified X-type Datastream' and @ALT_IDS='Datastream 2 Modified Alternate ID' and @FORMAT_URI='info:newMyFormatURI/Xtype/stuff#junk']",xmlIn);
         assertXpathExists("foxml:digitalObject/foxml:datastream[@ID='NEWDS2'][//dc:identifier='Identifier 5']",xmlIn);
         assertXpathEvaluatesTo("7", "count(//foxml:datastream[@ID!='AUDIT'])",xmlIn);          
 
         // (4) test purgeDatastream
         
         // test specifying null for endDate
         String[] results = apim.purgeDatastream("demo:14", "NEWDS2", null, "purgind datastream NEWDS3", false);
         for (int i=0; i<results.length; i++) {
             System.out.println("***** Testcase: TestAPIM.testPurgeDatastream specifying endDate=null dsID: "+results[i]);
         }
         assertTrue(results.length > 0);
         
         // test specifying datetime for endDate
         results = apim.purgeDatastream("demo:14", "NEWDS3", "2006-01-01T00:00:00.000Z", "purgind datastream NEWDS3", false);
         for (int i=0; i<results.length; i++) {
             System.out.println("***** Testcase: TestAPIM.testPurgeDatastream specifying endDate=\"2006-01-01T00:00:00.000Z\" dsID: "+results[i]);
         }        
         assertTrue(results.length > 0);
 
         // (5) test getDatastream
         
         // test getting datastream id FOPDISSEM for object demo:26 specifying null for datetime
         Datastream ds = apim.getDatastream("demo:26", "FOPDISSEM", null);
         assertNotNull(ds);
         Datastream[] dsArray = new Datastream[1];
         dsArray[0] = ds;
         System.out.println("***** Testcase: TestAPIM.testGetDatastream getDatastream(\"demo:26\", \"FOPDISSEM\", null)");
         verifyDatastreams(dsArray, "***** Testcase: TestAPIM.testGetDatastream");
 
         // assert datastream FOPDISSEM matches            
         assertEquals(dsArray[0].getID(),"FOPDISSEM");
         assertEquals(dsArray[0].getFormatURI(),null);
         assertEquals(dsArray[0].getLabel(),"FOP Dissemination as Datastream");
         assertEquals(dsArray[0].getLocation(),"http://localhost:8080/fedora/get/demo:26/demo:22/getFO");
         assertEquals(dsArray[0].getMIMEType(),"text/xml");
         assertEquals(dsArray[0].getState(),"A");
         assertEquals(dsArray[0].getVersionID(),"FOPRENDITION");
         assertEquals(dsArray[0].isVersionable(),true);
         assertEquals(dsArray[0].getControlGroup().getValue(),"E");
         assertEquals(dsArray[0].getSize(),0);
         if (dsArray[0].getAltIDs().length>0) {
             assertEquals(dsArray[0].getAltIDs()[0],"");
         }
         
                             
         // test getting datastream id FOPDISSEM for object demo:26 specifying datetime
         ds = apim.getDatastream("demo:26", "FOPDISSEM", "2006-01-01T00:00:00.000Z");
         dsArray[0] = ds;
         System.out.println("***** Testcase: TestAPIM.testGetDatastream getDatastream(\"demo:26\", ,\"FOPDISSEM\", \"2006-01-01T00:00:00.000Z\")");
         verifyDatastreams(dsArray, "***** Testcase: TestAPIM.testGetDatastream");
         
         // assert datastream FOPDISSEM matches            
         assertEquals(dsArray[0].getID(),"FOPDISSEM");
         assertEquals(dsArray[0].getFormatURI(),null);
         assertEquals(dsArray[0].getLabel(),"FOP Dissemination as Datastream");
         assertEquals(dsArray[0].getLocation(),"http://localhost:8080/fedora/get/demo:26/demo:22/getFO");
         assertEquals(dsArray[0].getMIMEType(),"text/xml");
         assertEquals(dsArray[0].getState(),"A");
         assertEquals(dsArray[0].getVersionID(),"FOPRENDITION");
         assertEquals(dsArray[0].isVersionable(),true);
         assertEquals(dsArray[0].getControlGroup().getValue(),"E");
         assertEquals(dsArray[0].getSize(),0);
         if (dsArray[0].getAltIDs().length>0) {
             assertEquals(dsArray[0].getAltIDs()[0],"");
         }
         
         // (6) test getDatastreams
         
         // test getting all datastreams for object demo:26 specifying null for datetime and state
         dsArray = apim.getDatastreams("demo:26", null, null);
         assertTrue(dsArray.length > 0);
         assertEquals(dsArray.length, 3);      
         verifyDatastreams(dsArray, "***** Testcase: TestAPIM.testGetDatastreams");
         System.out.println("***** Testcase: TestAPIM.testGetDatastreams getDatastreams(\"demo:26\", null, null) number of Datastreams: "+dsArray.length);
 
         // assert datastream FOPDISSEM matches            
         assertEquals(dsArray[0].getID(),"FOPDISSEM");
         assertEquals(dsArray[0].getFormatURI(),null);
         assertEquals(dsArray[0].getLabel(),"FOP Dissemination as Datastream");
         assertEquals(dsArray[0].getLocation(),"http://localhost:8080/fedora/get/demo:26/demo:22/getFO");
         assertEquals(dsArray[0].getMIMEType(),"text/xml");
         assertEquals(dsArray[0].getState(),"A");
         assertEquals(dsArray[0].getVersionID(),"FOPRENDITION");
         assertEquals(dsArray[0].isVersionable(),true);
         assertEquals(dsArray[0].getControlGroup().getValue(),"E");
         assertEquals(dsArray[0].getSize(),0);
         if (dsArray[0].getAltIDs().length>0) {
             assertEquals(dsArray[0].getAltIDs()[0],"");
         }        
         
         // assert datastream DC matches
         assertEquals(dsArray[1].getID(),"DC");
         assertEquals(dsArray[1].getFormatURI(),null);
         assertEquals(dsArray[1].getLabel(),"Dublin Core for the Document object");
         assertEquals(dsArray[1].getLocation(),null);
         assertEquals(dsArray[1].getMIMEType(),"text/xml");
         assertEquals(dsArray[1].getState(),"A");
         assertEquals(dsArray[1].getVersionID(),"DC1.0");
         assertEquals(dsArray[1].isVersionable(),true);
         assertEquals(dsArray[1].getControlGroup().getValue(),"X");
         assertEquals(dsArray[1].getSize(),550);
         if (dsArray[1].getAltIDs().length>0) {
             assertEquals(dsArray[1].getAltIDs()[0],"");
         }
 
         // assert datastream TEISOURCE matches
         assertEquals(dsArray[2].getID(),"TEISOURCE");
         assertEquals(dsArray[2].getFormatURI(),null);
         assertEquals(dsArray[2].getLabel(),"TEI Source");
         assertEquals(dsArray[2].getLocation(),null);
         assertEquals(dsArray[2].getMIMEType(),"text/xml");
         assertEquals(dsArray[2].getState(),"A");
         assertEquals(dsArray[2].getVersionID(),"TEISOURCE1.0");
         assertEquals(dsArray[2].isVersionable(),true);
         assertEquals(dsArray[2].getControlGroup().getValue(),"X");
         assertEquals(dsArray[2].getSize(),901);
         if (dsArray[2].getAltIDs().length>0) {
             assertEquals(dsArray[2].getAltIDs()[0],"");
         }            
             
         // test getting all datastreams for object demo:26 specifying null for state
         dsArray = apim.getDatastreams("demo:26", "2006-01-01T00:00:00.000Z", null);
         System.out.println("***** Testcase: TestAPIM.testGetDatastreams getDatastreams(\"demo:26\", \"2006-01-01T00:00:00.000Z\", null) number of Datastreams: "+dsArray.length);
         assertEquals(dsArray.length, 3);
         verifyDatastreams(dsArray, "***** Testcase: TestAPIM.testGetDatastreams");
         
         // assert datastream FOPDISSEM matches            
         assertEquals(dsArray[0].getID(),"FOPDISSEM");
         assertEquals(dsArray[0].getFormatURI(),null);
         assertEquals(dsArray[0].getLabel(),"FOP Dissemination as Datastream");
         assertEquals(dsArray[0].getLocation(),"http://localhost:8080/fedora/get/demo:26/demo:22/getFO");
         assertEquals(dsArray[0].getMIMEType(),"text/xml");
         assertEquals(dsArray[0].getState(),"A");
         assertEquals(dsArray[0].getVersionID(),"FOPRENDITION");
         assertEquals(dsArray[0].isVersionable(),true);
         assertEquals(dsArray[0].getControlGroup().getValue(),"E");
         assertEquals(dsArray[0].getSize(),0);
         if (dsArray[0].getAltIDs().length>0) {
             assertEquals(dsArray[0].getAltIDs()[0],"");
         }
         
         // assert datastream DC  matches
         assertEquals(dsArray[1].getID(),"DC");
         assertEquals(dsArray[1].getFormatURI(),null);
         assertEquals(dsArray[1].getLabel(),"Dublin Core for the Document object");
         assertEquals(dsArray[1].getLocation(),null);
         assertEquals(dsArray[1].getMIMEType(),"text/xml");
         assertEquals(dsArray[1].getState(),"A");
         assertEquals(dsArray[1].getVersionID(),"DC1.0");
         assertEquals(dsArray[1].isVersionable(),true);
         assertEquals(dsArray[1].getControlGroup().getValue(),"X");
         assertEquals(dsArray[1].getSize(),550);
         if (dsArray[1].getAltIDs().length>0) {
             assertEquals(dsArray[1].getAltIDs()[0],"");
         }
 
         // assert datastream TEISOURCE matches
         assertEquals(dsArray[2].getID(),"TEISOURCE");
         assertEquals(dsArray[2].getFormatURI(),null);
         assertEquals(dsArray[2].getLabel(),"TEI Source");
         assertEquals(dsArray[2].getLocation(),null);
         assertEquals(dsArray[2].getMIMEType(),"text/xml");
         assertEquals(dsArray[2].getState(),"A");
         assertEquals(dsArray[2].getVersionID(),"TEISOURCE1.0");
         assertEquals(dsArray[2].isVersionable(),true);
         assertEquals(dsArray[2].getControlGroup().getValue(),"X");
         assertEquals(dsArray[2].getSize(),901);
         if (dsArray[2].getAltIDs().length>0) {
             assertEquals(dsArray[2].getAltIDs()[0],"");
         }            
         
         // test getting all disseminators for object demo:26 specifying both datetime and state
         dsArray = apim.getDatastreams("demo:26", "2006-01-01T00:00:00.000Z", "A");
         System.out.println("***** Testcase: TestAPIM.testGetDatastreams getDatastreams(\"demo:26\", \"2006-01-01T00:00:00.000Z\", \"A\") number of Datastreams: "+dsArray.length);
         assertEquals(dsArray.length, 3);
         verifyDatastreams(dsArray, "***** Testcase: TestAPIM.testGetDatastreams");
         
         // assert datastream FOPDISSEM matches            
         assertEquals(dsArray[0].getID(),"FOPDISSEM");
         assertEquals(dsArray[0].getFormatURI(),null);
         assertEquals(dsArray[0].getLabel(),"FOP Dissemination as Datastream");
         assertEquals(dsArray[0].getLocation(),"http://localhost:8080/fedora/get/demo:26/demo:22/getFO");
         assertEquals(dsArray[0].getMIMEType(),"text/xml");
         assertEquals(dsArray[0].getState(),"A");
         assertEquals(dsArray[0].getVersionID(),"FOPRENDITION");
         assertEquals(dsArray[0].isVersionable(),true);
         assertEquals(dsArray[0].getControlGroup().getValue(),"E");
         assertEquals(dsArray[0].getSize(),0);
         if (dsArray[0].getAltIDs().length>0) {
             assertEquals(dsArray[0].getAltIDs()[0],"");
         }
         
         // assert datastream DC matches
         assertEquals(dsArray[1].getID(),"DC");
         assertEquals(dsArray[1].getFormatURI(),null);
         assertEquals(dsArray[1].getLabel(),"Dublin Core for the Document object");
         assertEquals(dsArray[1].getLocation(),null);
         assertEquals(dsArray[1].getMIMEType(),"text/xml");
         assertEquals(dsArray[1].getState(),"A");
         assertEquals(dsArray[1].getVersionID(),"DC1.0");
         assertEquals(dsArray[1].isVersionable(),true);
         assertEquals(dsArray[1].getControlGroup().getValue(),"X");
         assertEquals(dsArray[1].getSize(),550);
         if (dsArray[1].getAltIDs().length>0) {
             assertEquals(dsArray[1].getAltIDs()[0],"");
         }
 
         // assert datastream TEISOURCE matches
         assertEquals(dsArray[2].getID(),"TEISOURCE");
         assertEquals(dsArray[2].getFormatURI(),null);
         assertEquals(dsArray[2].getLabel(),"TEI Source");
         assertEquals(dsArray[2].getLocation(),null);
         assertEquals(dsArray[2].getMIMEType(),"text/xml");
         assertEquals(dsArray[2].getState(),"A");
         assertEquals(dsArray[2].getVersionID(),"TEISOURCE1.0");
         assertEquals(dsArray[2].isVersionable(),true);
         assertEquals(dsArray[2].getControlGroup().getValue(),"X");
         assertEquals(dsArray[2].getSize(),901);
         if (dsArray[2].getAltIDs().length>0) {
             assertEquals(dsArray[2].getAltIDs()[0],"");
         }            
         
         // (7) test getDatastreamHistory
         
         // test getting datastream history for datastream DS1 of object demo:10
         dsArray = apim.getDatastreamHistory("demo:10", "DS1");
         assertTrue(dsArray.length > 0);
         for (int i=0; i<dsArray.length; i++) {
             ds = dsArray[i];
             System.out.println("***** Testcase: TestAPIM.testGetDatastreamHistry createDate: "+ds.getCreateDate());
         }
         assertEquals(dsArray.length, 1);        
     }
     
     public void testDisseminatorMethods() throws Exception {
         
         // test disseminator methods
         // 1) addDisseminator
         // 2) modifyDisseminator
         // 3) purgeDisseminator
         // 4) getDisseminator
         // 5) getDisseminators
         // 6) getDisseminatorHistory
         
         
         // test (1) addDisseminator
         
         DatastreamBindingMap dsBindMap = new DatastreamBindingMap();
         DatastreamBinding dsBinding = new DatastreamBinding();
         DatastreamBinding[] dsBindings = new DatastreamBinding[1];
         dsBinding.setBindKeyName("url");
         dsBinding.setBindLabel("Binding to big pic of coliseum");
         dsBinding.setDatastreamID("DS2");
         dsBinding.setSeqNo("0");
         dsBindings[0] = dsBinding;
         dsBindMap.setDsBindings(dsBindings);
         dsBindMap.setDsBindMapID("dsBindMapID");
         dsBindMap.setDsBindMapLabel("dsBind Map Label");
         dsBindMap.setDsBindMechanismPID("demo:28");
         dsBindMap.setState("A");
         String result = apim.addDisseminator("demo:5", "demo:27", "demo:28", "Image Manip Disseminator", dsBindMap, "A", "adding new disseminator");
         assertNotNull(result);
         
         // test that disseminator was added
         byte [] objectXML = apim.getObjectXML("demo:5");
         assertTrue(objectXML.length > 0);
         String xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testAddDisseminator demo:28/demo:29 to demo:5\n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:5']",xmlIn);
         assertXpathExists("//foxml:disseminator[@ID='DISS2' and @BDEF_CONTRACT_PID='demo:27' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:disseminatorVersion[@ID='DISS2.0' and @BMECH_SERVICE_PID='demo:28' and @LABEL='Image Manip Disseminator']",xmlIn);
         assertXpathExists("//foxml:disseminatorVersion[@ID='DISS2.0' and @BMECH_SERVICE_PID='demo:28' and @LABEL='Image Manip Disseminator']/foxml:serviceInputMap/foxml:datastreamBinding[@DATASTREAM_ID='DS2' and @KEY='url' and @LABEL='Binding to big pic of coliseum']",xmlIn);
         assertXpathEvaluatesTo("2", "count(//foxml:disseminator)",xmlIn);
 
         // (2) test modifyDisseminator
         
         dsBindMap = new DatastreamBindingMap();
         dsBinding = new DatastreamBinding();
         dsBindings = new DatastreamBinding[1];
         dsBinding.setBindKeyName("url");
         dsBinding.setBindLabel("New Binding Label");
         dsBinding.setDatastreamID("DS4");
         dsBinding.setSeqNo("0");
         dsBindings[0] = dsBinding;
         dsBindMap.setDsBindings(dsBindings);
         dsBindMap.setDsBindMapID("dsBindMapID");
         dsBindMap.setDsBindMapLabel("dsBind Map Label");
         dsBindMap.setDsBindMechanismPID("demo:28");
         dsBindMap.setState("A");        
         result = apim.modifyDisseminator("demo:5", "DISS2", "demo:28", "Modified Disseminator Label", dsBindMap, "A", "modifying disseminator", false);
         System.out.println("***** Testcase: TestAPIM.testModifyDisseminator demo:5 DISS2 result: "+result);
         assertNotNull(result);
         
         // test that disseminator was added
         objectXML = apim.getObjectXML("demo:5");
         assertTrue(objectXML.length > 0);
         xmlIn = new String(objectXML, "UTF-8");
         System.out.println("***** Testcase: TestAPIM.testModifyDisseminator \n"+xmlIn);
         assertXpathExists("foxml:digitalObject[@PID='demo:5']",xmlIn);
         assertXpathExists("//foxml:disseminator[@ID='DISS2' and @BDEF_CONTRACT_PID='demo:27' and @STATE='A']",xmlIn);
         assertXpathExists("//foxml:disseminatorVersion[@ID='DISS2.1' and @BMECH_SERVICE_PID='demo:28' and @LABEL='Modified Disseminator Label']",xmlIn);
         assertXpathExists("//foxml:disseminatorVersion[@ID='DISS2.1' and @BMECH_SERVICE_PID='demo:28' and @LABEL='Modified Disseminator Label']/foxml:serviceInputMap/foxml:datastreamBinding[@DATASTREAM_ID='DS4' and @KEY='url' and @LABEL='New Binding Label']",xmlIn);
         assertXpathEvaluatesTo("2", "count(//foxml:disseminator)",xmlIn);        
         
         // (3) test purgeDisseminator
 
         // test specifying null for endDate
         String[] results = apim.purgeDisseminator("demo:5", "DISS2", null, "purging disseminator DISS2");
         for (int i=0; i<results.length; i++) {
             System.out.println("***** Testcase: TestAPIM.testPurgeDisseminator demo:5 DISS2 specifying endDate=null dissID: "+results[i]);
         }
         //assertTrue(results.length > 0);
         dsBindMap = new DatastreamBindingMap();
         dsBinding = new DatastreamBinding();
         dsBindings = new DatastreamBinding[1];
         dsBinding.setBindKeyName("url");
         dsBinding.setBindLabel("Binding to big pic of coliseum");
         dsBinding.setDatastreamID("DS2");
         dsBinding.setSeqNo("0");
         dsBindings[0] = dsBinding;
         dsBindMap.setDsBindings(dsBindings);
         dsBindMap.setDsBindMapID("dsBindMapID");
         dsBindMap.setDsBindMapLabel("dsBind Map Label");
         dsBindMap.setDsBindMechanismPID("demo:28");
         dsBindMap.setState("A");        
         result = apim.addDisseminator("demo:5", "demo:27", "demo:28", "Image Manip Disseminator", dsBindMap, "A", "adding new disseminator");
         
         // test specifying datetime for endDate
         results = apim.purgeDisseminator("demo:5", "DISS2", "2006-01-01T00:00:00.000Z", "purging disseminator DISS2");
         for (int i=0; i<results.length; i++) {
             System.out.println("***** Testcase: TestAPIM.testPurgeDisseminator specifying endDate=\"2006-01-01T00:00:00.000Z\" dissID: "+results[i]);
         }          
         //assertTrue(results.length > 0);
         assertEquals("1","1");
         
         // (4) test getDisseminator
 
         // test getting disseminator id DISS1 for object demo:26 specifying null for datetime
         Disseminator diss = apim.getDisseminator("demo:26", "DISS1", null);
         assertNotNull(diss);
         Disseminator[] dissArray = new Disseminator[1];
         dissArray[0] = diss;
         System.out.println("***** Testcase: TestAPIM.testGetDisseminator getDisseminator(\"demo:26\", \"DISS1\", null)");
         verifyDisseminators(dissArray, "***** Testcase: TestAPIM.testGetDisseminator");
 
         // assert DISS1 matches
         assertEquals(dissArray[0].getBDefPID(),"demo:19");
         assertEquals(dissArray[0].getBMechPID(),"demo:20");
         assertEquals(dissArray[0].getID(),"DISS1");
         assertEquals(dissArray[0].getLabel(),"PDF Disseminator");
         assertEquals(dissArray[0].getState(),"A");
         assertEquals(dissArray[0].getVersionID(),"DISS1.0");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapID(),"DISS1.0b");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMechanismPID(),"demo:20");
         assertEquals(dissArray[0].getDsBindMap().getState(),"A");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindKeyName(),"XML_SOURCE");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindLabel(),"FOP Source file to be transformed");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getDatastreamID(),"FOPDISSEM");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
                 
         // test getting disseminator id DISS1 for object demo:26 specifying datetime
         diss = apim.getDisseminator("demo:26", "DISS1", "2006-01-01T00:00:00.000Z");
         dissArray[0] = diss;
         System.out.println("***** Testcase: TestAPIM.testGetDisseminator getDisseminator(\"demo:26\", \"DISS1\", \"2006-01-01T00:00:00.000Z\")");
         verifyDisseminators(dissArray, "***** Testcase: TestAPIM.testGetDisseminator");
 
         // assert DISS1 matches
         assertEquals(dissArray[0].getBDefPID(),"demo:19");
         assertEquals(dissArray[0].getBMechPID(),"demo:20");
         assertEquals(dissArray[0].getID(),"DISS1");
         assertEquals(dissArray[0].getLabel(),"PDF Disseminator");
         assertEquals(dissArray[0].getState(),"A");
         assertEquals(dissArray[0].getVersionID(),"DISS1.0");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapID(),"DISS1.0b");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMechanismPID(),"demo:20");
         assertEquals(dissArray[0].getDsBindMap().getState(),"A");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindKeyName(),"XML_SOURCE");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindLabel(),"FOP Source file to be transformed");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getDatastreamID(),"FOPDISSEM");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
         
         // (5) test getDisseminators
         
         // test getting all disseminators for object demo:26 specifying null for datetime and state
         dissArray = apim.getDisseminators("demo:26", null, null);
         assertTrue(dissArray.length > 0);
         System.out.println("***** Testcase: TestAPIM.testGetDisseminators getDisseminators(\"demo:26\", null, null) number of Disseminators: "+dissArray.length);
         assertEquals(dissArray.length, 2);
         verifyDisseminators(dissArray, "***** Testcase: TestAPIM.testGetDisseminators");
 
         // assert DISS1 matches
         assertEquals(dissArray[0].getBDefPID(),"demo:19");
         assertEquals(dissArray[0].getBMechPID(),"demo:20");
         assertEquals(dissArray[0].getID(),"DISS1");
         assertEquals(dissArray[0].getLabel(),"PDF Disseminator");
         assertEquals(dissArray[0].getState(),"A");
         assertEquals(dissArray[0].getVersionID(),"DISS1.0");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapID(),"DISS1.0b");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMechanismPID(),"demo:20");
         assertEquals(dissArray[0].getDsBindMap().getState(),"A");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindKeyName(),"XML_SOURCE");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindLabel(),"FOP Source file to be transformed");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getDatastreamID(),"FOPDISSEM");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
         
         // assert DISS2 matches
         assertEquals(dissArray[1].getBDefPID(),"demo:22");
         assertEquals(dissArray[1].getBMechPID(),"demo:25");
         assertEquals(dissArray[1].getID(),"DISS2");
         assertEquals(dissArray[1].getLabel(),"FO Disseminator");
         assertEquals(dissArray[1].getState(),"A");
         assertEquals(dissArray[1].getVersionID(),"DISS2.0");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapID(),"DISS2.0b");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMechanismPID(),"demo:25");
         assertEquals(dissArray[1].getDsBindMap().getState(),"A");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindKeyName(),"TEI_SOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindLabel(),"TEI Source file to be transformed to FO");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getDatastreamID(),"TEISOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
         
         // test getting all disseminators for object demo:26 specifying null for state
         dissArray = apim.getDisseminators("demo:26", "2006-01-01T00:00:00.000Z", null);
         System.out.println("***** Testcase: TestAPIM.testGetDissemintors getDisseminators(\"demo:26\", \"2006-01-01T00:00:00.000Z\", null) number of Disseminators: "+dissArray.length);
         assertEquals(dissArray.length, 2);
         verifyDisseminators(dissArray, "***** Testcase: TestAPIM.testGetDissemintors");
 
         // assert DISS1 matches
         assertEquals(dissArray[0].getBDefPID(),"demo:19");
         assertEquals(dissArray[0].getBMechPID(),"demo:20");
         assertEquals(dissArray[0].getID(),"DISS1");
         assertEquals(dissArray[0].getLabel(),"PDF Disseminator");
         assertEquals(dissArray[0].getState(),"A");
         assertEquals(dissArray[0].getVersionID(),"DISS1.0");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapID(),"DISS1.0b");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMechanismPID(),"demo:20");
         assertEquals(dissArray[0].getDsBindMap().getState(),"A");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindKeyName(),"XML_SOURCE");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindLabel(),"FOP Source file to be transformed");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getDatastreamID(),"FOPDISSEM");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
         
         // assert DISS2 matches
         assertEquals(dissArray[1].getBDefPID(),"demo:22");
         assertEquals(dissArray[1].getBMechPID(),"demo:25");
         assertEquals(dissArray[1].getID(),"DISS2");
         assertEquals(dissArray[1].getLabel(),"FO Disseminator");
         assertEquals(dissArray[1].getState(),"A");
         assertEquals(dissArray[1].getVersionID(),"DISS2.0");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapID(),"DISS2.0b");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMechanismPID(),"demo:25");
         assertEquals(dissArray[1].getDsBindMap().getState(),"A");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindKeyName(),"TEI_SOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindLabel(),"TEI Source file to be transformed to FO");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getDatastreamID(),"TEISOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getSeqNo(),"");                    
 
         // test getting all disseminators for object demo:26 specifying both datetime and state
         dissArray = apim.getDisseminators("demo:26", "2006-01-01T00:00:00.000Z", "A");
         System.out.println("***** Testcase: TestAPIM.testGetDissemintors getDisseminators(\"demo:26\", \"2006-01-01T00:00:00.000Z\", \"A\") number of Disseminators: "+dissArray.length);
         assertEquals(dissArray.length, 2);
         verifyDisseminators(dissArray, "***** Testcase: TestAPIM.testGetDissemintors");
 
         // assert DISS1 matches
         assertEquals(dissArray[0].getBDefPID(),"demo:19");
         assertEquals(dissArray[0].getBMechPID(),"demo:20");
         assertEquals(dissArray[0].getID(),"DISS1");
         assertEquals(dissArray[0].getLabel(),"PDF Disseminator");
         assertEquals(dissArray[0].getState(),"A");
         assertEquals(dissArray[0].getVersionID(),"DISS1.0");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapID(),"DISS1.0b");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[0].getDsBindMap().getDsBindMechanismPID(),"demo:20");
         assertEquals(dissArray[0].getDsBindMap().getState(),"A");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindKeyName(),"XML_SOURCE");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getBindLabel(),"FOP Source file to be transformed");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getDatastreamID(),"FOPDISSEM");
         assertEquals(dissArray[0].getDsBindMap().getDsBindings()[0].getSeqNo(),"");
         
         // assert DISS2 matches
         assertEquals(dissArray[1].getBDefPID(),"demo:22");
         assertEquals(dissArray[1].getBMechPID(),"demo:25");
         assertEquals(dissArray[1].getID(),"DISS2");
         assertEquals(dissArray[1].getLabel(),"FO Disseminator");
         assertEquals(dissArray[1].getState(),"A");
         assertEquals(dissArray[1].getVersionID(),"DISS2.0");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapID(),"DISS2.0b");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMapLabel(),"");
         assertEquals(dissArray[1].getDsBindMap().getDsBindMechanismPID(),"demo:25");
         assertEquals(dissArray[1].getDsBindMap().getState(),"A");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindKeyName(),"TEI_SOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getBindLabel(),"TEI Source file to be transformed to FO");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getDatastreamID(),"TEISOURCE");
         assertEquals(dissArray[1].getDsBindMap().getDsBindings()[0].getSeqNo(),"");            
 
         	// (6) test getDisseminatorHistory
         
         // test getting disseminator history for disseminator DISS1 of object demo:10
         dissArray = apim.getDisseminatorHistory("demo:10", "DISS1");
         assertTrue(dissArray.length > 0);
         for (int i=0; i<dissArray.length; i++) {
             diss = dissArray[i];
             System.out.println("***** Testcase: TestAPIM.testGetDatastreamHistory createDate: "+diss.getCreateDate());
         }
         assertEquals(dissArray.length, 1);
     }
     
     public void testSetDatastreamState() throws Exception {
         
         // test setting datastream state to "I" for datastream id DC of object demo:5
         String result = apim.setDatastreamState("demo:5", "DC", "I", "changed state to Inactive");
         assertNotNull(result);
         Datastream ds = apim.getDatastream("demo:5", "DC", null);
         assertEquals("I", ds.getState());
         System.out.println("***** Testcase: TestAPIM.testSetDatastreamState new state: "+ds.getState());
     }    
     
     public void testSetDisseminatorState() throws Exception {
         
         // test setting disseminator state to "I" for disseminator id DISS1 of object demo:5
         String result = apim.setDisseminatorState("demo:5", "DISS1", "I", "changed state to Inactive");
         assertNotNull(result);
         Disseminator diss = apim.getDisseminator("demo:5", "DISS1", null);
         assertEquals("I", diss.getState()); 
         System.out.println("***** Testcase: TestAPIM.testSetDisseminatorState new state: "+diss.getState());
     }
     
     public void testGetNextPID() throws Exception {
         
         // test null for both arguments
         String [] pids = apim.getNextPID(null, null);
         assertTrue(pids.length > 0);
         System.out.println("***** Testcase: TestAPIM.testGetNextPID  nextPid(null, null): "+pids[0]);       
         assertEquals(pids.length,1);
         assertTrue(pids[0].startsWith("changeme"));
         
         
         // test null for numPids argument
         pids = apim.getNextPID(null,"dummy");
         assertTrue(pids.length > 0);
         System.out.println("***** Testcase: TestAPIM.testGetNextPID  nextPid(null, \"dummy\"): "+pids[0]);     
         assertEquals(pids.length,1);
        assertTrue(pids[0].startsWith("dummy:"));
         
         // test null for namespace argument
         pids = apim.getNextPID(new NonNegativeInteger("1"), null);
         assertTrue(pids.length > 0);
         System.out.println("***** Testcase: TestAPIM.testGetNextPID  nextPid(1, null): "+pids[0]);      
         assertEquals(pids.length,1);
         assertTrue(pids[0].startsWith("changeme"));
         
         // test both arguments non-null
         pids = apim.getNextPID(new NonNegativeInteger("2"), "namespace");
         assertTrue(pids.length > 0);
         System.out.println("***** Testcase: TestAPIM.testGetNextPID  nextPid(2, \"namespace\"): "+pids[0]+" , "+pids[1]);
         assertEquals(pids.length,2);
        assertTrue(pids[0].startsWith("namespace:"));
        assertTrue(pids[1].startsWith("namespace:"));
     }    
      
     public void verifyDatastreams(Datastream[] dsArray, String msg) throws Exception {
 
         String dsID = null;
         String createDate = null;
         String state = null;
         String label = null;
         String formatURI = null;
         String dsLocation = null;
         String versionID = null;
         String dsControlGrp = null;
         String [] altIds = null;
         String mimeType = null;
         long size = 0;
         boolean isVersionable = false;
         Datastream ds = null;
         
         for (int i=0; i<dsArray.length; i++) {
             ds = dsArray[i];
             dsID = ds.getID();
             createDate = ds.getCreateDate();
             state = ds.getState();
             formatURI = ds.getFormatURI();
             dsLocation = ds.getLocation();
             label = ds.getLabel();
             versionID = ds.getVersionID();
             dsControlGrp = ds.getControlGroup().getValue();
             altIds = ds.getAltIDs();
             if (altIds.length==0) {
                 altIds = new String[1];
                 altIds[0] = "";
             }
             mimeType = ds.getMIMEType();
             size = ds.getSize();
             isVersionable = ds.isVersionable();
             System.out.println(msg + " datastreams["+i+"] "
                     + "dissID: "+dsID);
             System.out.println(msg + " datastreams["+i+"] "
                     + "dsControlGroup: "+dsControlGrp);            
             System.out.println(msg + " datastreams["+i+"] "
                     + "createDate: "+createDate);
             System.out.println(msg + " datastreams["+i+"] "
                     + " state: "+state);
             System.out.println(msg + " datastreams["+i+"] "
                     + "formatURI: "+formatURI);
             System.out.println(msg + " datastreams["+i+"] "
                     + "altIds[0]: "+altIds[0]);
             System.out.println(msg + " datastreams["+i+"] "
                     + "label: '"+label+"'");
             System.out.println(msg + " datastreams["+i+"] "
                     + "versionID: "+versionID);
             System.out.println(msg + " datastreams["+i+"] "
                     + "dsLocation: "+dsLocation);
             System.out.println(msg + " datastreams["+i+"] "
                     + "mimeType: "+mimeType);
             System.out.println(msg + " datastreams["+i+"] "
                     + "size: "+size);
             System.out.println(msg + " datastreams["+i+"] "
                     + "isVersionable: "+isVersionable);
 
         }
         
     }    
     
     public void verifyDisseminators(Disseminator[] dissArray, String msg) throws Exception {
         
         String dissID = null;
         String createDate = null;
         String state = null;
         String label = null;
         String bDefPid = null;
         String bMechPid = null;
         String versionID = null;
         String dsBindMapID = null;
         String dsBindMapLabel = null;
         String dsBindMapBmechPid = null;
         String dsBindMapState = null;
         String dsBindKeyName = null;
         String dsBindKeyLabel = null;
         String dsSeqNo = null;
         String dsID = null;
         Disseminator diss = null;
         DatastreamBindingMap dsBindMap = null;
         DatastreamBinding[] dsBindings = null;
         
         for (int i=0; i<dissArray.length; i++) {
             diss = dissArray[i];
             dissID = diss.getID();
             createDate = diss.getCreateDate();
             state = diss.getState();
             bDefPid = diss.getBDefPID();
             bMechPid = diss.getBMechPID();
             label = diss.getLabel();
             versionID = diss.getVersionID();
             dsBindMap = diss.getDsBindMap();
             dsBindings = dsBindMap.getDsBindings();
             dsBindMapID = dsBindMap.getDsBindMapID();
             dsBindMapLabel = dsBindMap.getDsBindMapLabel();
             dsBindMapBmechPid = dsBindMap.getDsBindMechanismPID();
             dsBindMapState = dsBindMap.getState();
             System.out.println(msg + " disseminators["+i+"] "
                     + "dissID: "+dissID);
             System.out.println(msg + " disseminators["+i+"] "
                     + "createDate: "+createDate);
             System.out.println(msg + " disseminators["+i+"] "
                     + " state: "+diss.getState());
             System.out.println(msg + " disseminators["+i+"] "
                     + "bDefPid: "+bDefPid);
             System.out.println(msg + " disseminators["+i+"] "
                     + "bMechPid: "+bMechPid);
             System.out.println(msg + " disseminators["+i+"] "
                     + "label: '"+label+"'");
             System.out.println(msg + " disseminators["+i+"] "
                     + "versionID: "+versionID);
             System.out.println(msg + " disseminators["+i+"] "
                     + "dsBindMapID: "+dsBindMapID);
             System.out.println(msg + " disseminators["+i+"] "
                     + "dsBindMapLabel: '"+dsBindMapLabel+"'");
             System.out.println(msg + " disseminators["+i+"] "
                     + "dsBindMapBmechPid: "+dsBindMapBmechPid);
             System.out.println(msg + " disseminators["+i+"] "
                     + "dsBindMapState: "+dsBindMapState);
             for (int j=0; j<dsBindings.length; j++) {
                 
                 dsBindKeyName = dsBindings[0].getBindKeyName();
                 dsBindKeyLabel = dsBindings[0].getBindLabel();
                 dsID = dsBindings[0].getDatastreamID();
                 dsSeqNo = dsBindings[0].getSeqNo();                
                 System.out.println(msg + " dsBindings["+j+"] "
                         + "dsBindKeyName: "+dsBindKeyName);
                 System.out.println(msg + " dsBindings["+j+"] "
                         + "dsBindKeyLabel: '"+dsBindKeyLabel+"'");
                 System.out.println(msg + " dsBindings["+j+"] "
                         + "dsID: "+dsID);
                 System.out.println(msg + " dsBindings["+j+"] "
                         + "dsSeqNo: "+dsSeqNo);
             }
         }  
     }
     
     public static void main(String[] args) {
         junit.textui.TestRunner.run(TestAPIM.class);
     }
 
 }
