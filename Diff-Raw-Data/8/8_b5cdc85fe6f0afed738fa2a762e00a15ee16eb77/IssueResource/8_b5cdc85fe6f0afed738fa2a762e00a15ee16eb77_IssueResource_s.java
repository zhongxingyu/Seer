 package org.hackystat.dailyprojectdata.resource.issue;
 
 import java.io.StringWriter;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.hackystat.dailyprojectdata.resource.dailyprojectdata.DailyProjectDataResource;
 import org.hackystat.dailyprojectdata.resource.issue.jaxb.IssueDailyProjectData;
 import org.hackystat.dailyprojectdata.resource.issue.jaxb.IssueData;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Variant;
 import org.w3c.dom.Document;
 
 /**
  * Implements the Resource for processing GET {host}/issue/{user}/{project}/{starttime} requests.
  * Requires the authenticated user to be {user} or else the Admin user for the sensorbase 
  * connected to this service. 
  * @author Shaoxuan Zhang
  */
 public class IssueResource extends DailyProjectDataResource {
 
   private String status;
   
   /**
    * The standard constructor.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public IssueResource(Context context, Request request, Response response) {
     super(context, request, response);
     this.status = (String) request.getAttributes().get("Status");
   }
 
   /**
    * Returns an IssueDailyProjectData instance representing the Issue associated with the 
    * Project data, or null if not authorized. 
    * Authenticated user must be the uriUser, or Admin, or project member. 
    * @param variant The representational variant requested.
    * @return The representation. 
    */
   @Override
   public Representation represent(Variant variant) {
     Logger logger = this.server.getLogger();
     logger.fine("Issue DPD: Starting");
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       try {
         // [1] get the SensorBaseClient for the user making this request.
         SensorBaseClient client = super.getSensorBaseClient();
         // [2] Check the front side cache and return if the DPD is found and is OK to access.
         String cachedDpd = this.server.getFrontSideCache().get(uriUser, project, uriString);
         if ((cachedDpd != null) && client.inProject(uriUser, project)) {
           return super.getStringRepresentation(cachedDpd);
         }
         // [2] get a SensorDataIndex of all Issue data for this Project on the requested day.
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(this.timestamp);
         XMLGregorianCalendar endTime = Tstamp.incrementDays(startTime, 1);
         logger.fine("Issue DPD: Requesting index: " + uriUser + " " + project);
         
         XMLGregorianCalendar projectStartTime = client.getProject(uriUser, project).getStartTime();
         
         SensorDataIndex index = client.getProjectSensorData(uriUser, project, projectStartTime, 
             endTime, "Issue");
         logger.fine("Issue DPD: Got index: " + index.getSensorDataRef().size() + " instances");
         // [3] prepare the IssueDailyProjectData
         IssueDailyProjectData issueDpd = new IssueDailyProjectData();
         issueDpd.setOwner(uriUser);
         issueDpd.setProject(project);
         issueDpd.setStartTime(startTime);
         // [4] parse Issue SensorData. 
         int openIssue = 0;
         IssueDataParser parser = new IssueDataParser(this.server.getLogger());
         for (SensorDataRef ref : index.getSensorDataRef()) {
           IssueData issueData = parser.getIssueDpd(client.getSensorData(ref), endTime);
           boolean isOpen = parser.isOpenStatus(issueData.getStatus());
           boolean include = false;
          if ((status == null) || 
              ((status.equalsIgnoreCase("open") && isOpen) || 
               (status.equalsIgnoreCase("closed") && !isOpen) ||
               (status.equalsIgnoreCase(issueData.getStatus())))) {
             include = true;
             issueDpd.getIssueData().add(issueData);
           }
           if (include && isOpen) {
             openIssue++;
           }
         }
         // [5] finish the IssueDailyProjectData and send.
         issueDpd.setOpenIssues(openIssue);
         String xmlData = makeIssues(issueDpd);
         if (!Tstamp.isTodayOrLater(startTime)) {
           this.server.getFrontSideCache().put(uriUser, project, uriString, xmlData);
         }
         logRequest("Issue");
         return super.getStringRepresentation(xmlData);
       }
       catch (Exception e) {
         setStatusError("Error creating Issue DPD.", e);
         return null;
       }
     }
     return null;
   }
 
   /**
    * Returns the passed SensorData instance as a String encoding of its XML representation.
    * @param data The SensorData instance. 
    * @return The XML String representation.
    * @throws Exception If problems occur during translation. 
    */
   private String makeIssues (IssueDailyProjectData data) throws Exception {
     JAXBContext devTimeJAXB = 
       (JAXBContext)this.server.getContext().getAttributes().get("IssueJAXB");
     Marshaller marshaller = devTimeJAXB.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(data, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     return writer.toString();
   }
 }
