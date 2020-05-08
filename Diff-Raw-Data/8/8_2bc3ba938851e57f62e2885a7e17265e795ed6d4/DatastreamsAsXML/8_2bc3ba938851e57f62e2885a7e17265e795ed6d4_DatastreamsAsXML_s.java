 package fedora.server.utilities.XMLConversions;
 
 import java.io.IOException;
 import java.util.Date;
 
 import fedora.server.errors.ServerException;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.types.Datastream;
 import fedora.server.utilities.DateUtility;
 
 
 /**
  * <p>Use an object reader to get datastream information out of the object
  * and encode the in XML.
  *
  * XML Schemas used for output:
 * http://www.fedora.info/definitions/1/0/access/objectItemList.xsd.
  * (To be created at this URL asap!) </p>
  *
  * @author Sandy Payette payette@cs.cornell.edu
  */
 public class DatastreamsAsXML
 {
 
     public DatastreamsAsXML()
     {
     }
 
     // FIXIT!! Make the output of this valid XML with namespaces for fedora
     public String getItemIndex(String reposBaseURL, DOReader reader, Date versDateTime)
             throws ServerException {
         Datastream[] datastreams = reader.GetDatastreams(versDateTime);
         StringBuffer out = new StringBuffer();
 
         out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<objectItemList"
               + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
               + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
               + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
               + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
               + " http://www.fedora.info/definitions/1/0/access/objectItemList.xsd\""
               + " PID=\"" + reader.GetObjectPID() + "\">\n");
 
         for (int i=0; i<datastreams.length; i++)
         {
           out.append("<item>\n");
           out.append("<itemId>" + datastreams[i].DatastreamID + "</itemId>\n");
           String label = datastreams[i].DSLabel;
           if (label==null) label="";
           out.append("<itemLabel>" + label + "</itemLabel>\n");
           String itemDissURL = getItemDissURL(reposBaseURL, reader.GetObjectPID(),
               datastreams[i].DatastreamID, versDateTime);
           out.append("<itemURL>" + itemDissURL + "</itemURL>\n");
           out.append("<itemMIMEType>" + datastreams[i].DSMIME + "</itemMIMEType>\n");
           out.append("</item>\n");
         }
        out.append("</objectItemList>");
         return out.toString();
     }
 
     private String getItemDissURL(String reposBaseURL, String PID,
         String datastreamID, Date versDateTime)
     {
         String itemDissURL = null;
 
         if (versDateTime == null)
         {
           itemDissURL = reposBaseURL + "/fedora/get/"
             + PID + "/fedora-system:3/getItem?itemID=" + datastreamID;
         }
         else
         {
             itemDissURL = reposBaseURL + "/fedora/get/"
               + PID + "/fedora-system:3/getItem/"
               + DateUtility.convertDateToString(versDateTime)
               + "/?itemID=" + datastreamID;
         }
         return itemDissURL;
     }
 
     public String getDatastreamList(DOReader reader, Date versDateTime)
             throws ServerException {
         Datastream[] datastreams = reader.GetDatastreams(versDateTime);
         StringBuffer out = new StringBuffer();
         out.append("<datastreamSet>\n");
         out.append("<pid>" + reader.GetObjectPID() + "</pid>\n");
         for (int i=0; i<datastreams.length; i++)
         {
           out.append("<datastream>\n");
           out.append("<dsId>" + datastreams[i].DatastreamID + "</dsId>\n");
           out.append("<dsVersionId>" + datastreams[i].DSVersionID + "</dsVersionId>\n");
           String label = datastreams[i].DSLabel;
           if (label==null) label="";
           out.append("<dsCreateDT>" + datastreams[i].DSCreateDT + "</dsCreateDT>\n");
           out.append("<dsLabel>" + label + "</dsLabel>\n");
           out.append("<dsLocationURL>" + datastreams[i].DSLocation + "</dsLocationURL>\n");
           out.append("<dsMIMEType>" + datastreams[i].DSMIME + "<dsMIMEType>\n");
           out.append("<dsControlGrp>" + datastreams[i].DSControlGrp + "<dsControl>\n");
           out.append("</datastream>\n");
         }
         out.append("</datastreamSet>");
         return out.toString();
     }
 }
