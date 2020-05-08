 package org.openmrs.module.pacsintegration.handler;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.app.Application;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
 import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
 import ca.uhn.hl7v2.model.v23.message.ORU_R01;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTime;
 import org.openmrs.Concept;
 import org.openmrs.Provider;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.pacsintegration.util.HL7Utils;
 import org.openmrs.module.radiologyapp.RadiologyReport;
 
 import java.util.Date;
 
 import static org.openmrs.module.pacsintegration.PacsIntegrationConstants.GP_LISTENER_PASSWORD;
 import static org.openmrs.module.pacsintegration.PacsIntegrationConstants.GP_LISTENER_USERNAME;
 
 public class ORU_R01Handler extends HL7Handler implements Application {
 
     protected final Log log = LogFactory.getLog(this.getClass());
 
     public ORU_R01Handler() {
     }
 
     @Override
     public Message processMessage(Message message) throws HL7Exception {
 
         ORU_R01 oruR01 = (ORU_R01) message;
         String messageControlID = oruR01.getMSH().getMessageControlID().getValue();
         String sendingFacility = null;
 
         Context.openSession();
 
         try {
             Context.openSession();
             Context.authenticate(adminService.getGlobalProperty(GP_LISTENER_USERNAME),
                     adminService.getGlobalProperty(GP_LISTENER_PASSWORD));
 
             sendingFacility = getSendingFacility();
 
             String patientIdentifier = oruR01.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();
 
             RadiologyReport report = new RadiologyReport();
 
             report.setAccessionNumber(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue());
             report.setPatient(getPatient(patientIdentifier));
             report.setReportDate(syncReportTimeWithCurrentServerTime(HL7Utils.getHl7DateFormat().parse(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR()
                     .getObservationDateTime().getTimeOfAnEvent().getValue())));
             report.setAssociatedRadiologyOrder(getRadiologyOrder(report.getAccessionNumber(), report.getPatient()));
             report.setProcedure(getProcedure(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue()));
             report.setPrincipalResultsInterpreter(getResultsInterpreter(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getPrincipalResultInterpreter().getOPName().getIDNumber().getValue()));
             report.setReportType(getReportType(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getResultStatus().getValue()));
             report.setReportLocation(getLocationByName(oruR01.getMSH().getSendingFacility().getNamespaceID().getValue()));
             report.setReportBody(getReportBody(oruR01.getRESPONSE().getORDER_OBSERVATION()));
 
             radiologyService.saveRadiologyReport(report);
         }
         catch (Exception e) {
             log.error("Unable to parse incoming ORU_RO1 message", e);
             return HL7Utils.generateErrorACK(messageControlID, sendingFacility,
                 e.getMessage());
         }
         finally {
             Context.closeSession();
         }
 
         return HL7Utils.generateACK(oruR01.getMSH().getMessageControlID().getValue(), sendingFacility);
     }
 
     @Override
     public boolean canProcess(Message message) {
         return message != null && "ORM_O01".equals(message.getName());
     }
 
     private String getReportBody(ORU_R01_ORDER_OBSERVATION obsSet) throws HL7Exception {
         StringBuffer reportBody = new StringBuffer();
 
        for (int i = 0; i < obsSet.getOBSERVATIONReps(); i++) {
 
             ORU_R01_OBSERVATION obs = obsSet.getOBSERVATION(i);
 
             if (obs.getOBX().getObservationValue().length > 0 &&
                    obs.getOBX().getObservationValue()[0].getData() != null &&
                    obs.getOBX().getObservationValue()[0].getData().toString() != null) {
                 reportBody.append(obs.getOBX().getObservationValue()[0].getData().toString());
             }
 
             reportBody.append("\r\n");
         }
 
         return reportBody.toString();
     }
 
 
     private Provider getResultsInterpreter(String providerId) {
         if (StringUtils.isNotBlank(providerId)) {
             return providerService.getProviderByIdentifier(providerId);
         }
 
         return null;
     }
 
     private Concept getReportType(String reportCode) {
         if (StringUtils.isNotBlank(reportCode)) {
             if (reportCode.equalsIgnoreCase("P"))  {
                 return pacsIntegrationProperties.getReportTypePrelimConcept();
             }
             if (reportCode.equalsIgnoreCase("F")) {
                 return pacsIntegrationProperties.getReportTypeFinalConcept();
             }
             if (reportCode.equalsIgnoreCase("C"))  {
                 return pacsIntegrationProperties.getReportTypeCorrectionConcept();
             }
         }
 
         return null;
     }
 
     // this method is used to handle issues caused by slight time differences between OpenMRS and the source server
     // if the passed time is in the future, but less than 5 minutes in the future, return the current time (because
     // OpenMRS can't accept encounter dates in the future); if it is more than 5 minutes in the future, throw
     // an exception
     private Date syncReportTimeWithCurrentServerTime(Date reportDate) {
 
         DateTime now = new DateTime();
         DateTime reportDateTime = new DateTime(reportDate);
 
         // just return the passed-in date if it is in the past
         if (reportDateTime.isBefore(now)) {
             return reportDateTime.toDate();
         }
         else {
             if (reportDateTime.minusMinutes(5).isBefore(now)) {
                 return now.toDate();
             }
             else {
                 throw new IllegalArgumentException("Date cannot be more than 5 minutes in the future.");
             }
         }
     }
 }
