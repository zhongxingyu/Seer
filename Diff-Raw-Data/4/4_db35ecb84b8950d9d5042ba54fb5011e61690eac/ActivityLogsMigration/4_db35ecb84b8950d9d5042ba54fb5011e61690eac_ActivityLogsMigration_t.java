 package org.youthnet.export.migration;
 
 import org.youthnet.export.domain.vb25.TblActivityLog;
 import org.youthnet.export.domain.vb3.*;
 import org.youthnet.export.io.CSVFileReader;
 import org.youthnet.export.util.CSVUtil;
 
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * User: ActivityLogsMigration
  * Date: 07-Jul-2010
  */
 public class ActivityLogsMigration implements Migratable {
 
     private Map<String, Map<String, Lookups>> lookupsMap;
 
     public ActivityLogsMigration(Map<String, Map<String, Lookups>> lookupsMap) {
         this.lookupsMap = lookupsMap;
     }
 
     @Override
     public void migrate(String csvDir, String outputDir) {
         CSVFileReader csvFileReader = null;
         BufferedWriter activityLogsWriter = null;
 
         BufferedWriter opportunityActLogWriter = null;
         BufferedWriter organisationActivityLogWriter = null;
         BufferedWriter volunteerActivityLogWriter = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(csvDir + "tblActivityLog.csv"));
 
             activityLogsWriter = new BufferedWriter(new FileWriter(outputDir + "ActivityLogs.csv"));
 
             opportunityActLogWriter = new BufferedWriter(new FileWriter(outputDir + "OpportunityActLogs.csv"));
             organisationActivityLogWriter = new BufferedWriter(new FileWriter(outputDir + "OrganisationActivityLogs.csv"));
             volunteerActivityLogWriter = new BufferedWriter(new FileWriter(outputDir + "VolunteerActivityLogs.csv"));
 
             Map<Long, Opportunities> opportunitiesVb2idMap =
                     CSVUtil.createVb2idMap(outputDir + "Opportunities.csv", Opportunities.class);
             Map<Long, Organisations> organisationsVb2idMap =
                     CSVUtil.createVb2idMap(outputDir + "Organisations.csv", Organisations.class);
             Map<Long, Contacts> contactsVb2idMap =
                     CSVUtil.createVb2idMap(outputDir + "Contacts.csv", Contacts.class);
             Map<Long, Volunteers> volunteersVb2idMap =
                     CSVUtil.createVb2idMap(outputDir + "Volunteers.csv", Volunteers.class);
 
 
             TblActivityLog tblActivityLog = null;
             ActivityLogs activityLogs = null;
             OpportunityActLogs linkedOpportunityActLog = null;
             OpportunityActLogs opportunityActLog = null;
             OrganisationActivityLogs organisationActivityLog = null;
             VolunteerActivityLogs volunteerActivityLog = null;
             String record = "";
             while ((record = csvFileReader.readRecord()) != null) {
                 tblActivityLog = new TblActivityLog(record);
                 activityLogs = new ActivityLogs();
 
                 linkedOpportunityActLog = null;
                 opportunityActLog = null;
                 organisationActivityLog = null;
                 volunteerActivityLog = null;
 
                 activityLogs.setId(UUID.randomUUID());
                 activityLogs.setVbase2Id(tblActivityLog.getLid());
                 activityLogs.setActivityTypeId(lookupsMap.get("activitytype") != null &&
                         lookupsMap.get("activitytype").get(tblActivityLog.getActivity().toLowerCase()) != null ?
                         lookupsMap.get("activitytype").get(tblActivityLog.getActivity().toLowerCase()).getId() :
                         null);
                 activityLogs.setStartDate(tblActivityLog.getStarttime());
                 activityLogs.setEndDate(tblActivityLog.getEndtime());
                 activityLogs.setIsAllDayEvent(tblActivityLog.getAlldayevent());
                 activityLogs.setNotes(tblActivityLog.getNotes());
                 activityLogs.setShowInCalender(tblActivityLog.getShowincalendar());
                 activityLogs.setSubject(tblActivityLog.getSubject());
 
                 if (tblActivityLog.getLinkedoid() != null) {
                     linkedOpportunityActLog = new OpportunityActLogs();
                     linkedOpportunityActLog.setId(UUID.randomUUID());
                     linkedOpportunityActLog.setOpportunityId(opportunitiesVb2idMap.get(tblActivityLog.getLinkedoid()) == null ?
                             null : opportunitiesVb2idMap.get(tblActivityLog.getLinkedoid()).getId());
                 }
 
                 if (tblActivityLog.getOid() != null) {
                     opportunityActLog = new OpportunityActLogs();
                     opportunityActLog.setId(UUID.randomUUID());
                     opportunityActLog.setOpportunityId(opportunitiesVb2idMap.get(tblActivityLog.getOid()) == null ?
                             null : opportunitiesVb2idMap.get(tblActivityLog.getOid()).getId());
                 }
 
                 if (tblActivityLog.getOrgid() != null) {
                     organisationActivityLog = new OrganisationActivityLogs();
                     organisationActivityLog.setId(UUID.randomUUID());
                    organisationActivityLog.setOrganisationId(organisationsVb2idMap.get(tblActivityLog.getOrgid()) == null ?
                            null : organisationsVb2idMap.get(tblActivityLog.getOrgid()).getId());
 
                     activityLogs.setPersonHereId(contactsVb2idMap.get(tblActivityLog.getOrgid()) == null ?
                             null : contactsVb2idMap.get(tblActivityLog.getOrgid()).getId());
                 }
 
                 if (tblActivityLog.getVid() != null) {
                     volunteerActivityLog = new VolunteerActivityLogs();
                     volunteerActivityLog.setId(UUID.randomUUID());
                     volunteerActivityLog.setVolunteerId(volunteersVb2idMap.get(tblActivityLog.getVid()) == null ?
                             null : volunteersVb2idMap.get(tblActivityLog.getVid()).getId());
                 }
 
 
                 activityLogsWriter.write(activityLogs.getRecord() + "\n");
 
                 if (linkedOpportunityActLog != null) {
                     opportunityActLogWriter.write(linkedOpportunityActLog.getRecord() + "\n");
                 }
                 if (opportunityActLog != null) opportunityActLogWriter.write(opportunityActLog.getRecord() + "\n");
                 if (organisationActivityLog != null) {
                     organisationActivityLogWriter.write(organisationActivityLog.getRecord() + "\n");
                 }
                 if (volunteerActivityLog != null) {
                     volunteerActivityLogWriter.write(volunteerActivityLog.getRecord() + "\n");
                 }
             }
 
         } catch (IOException e) {
             System.out.println("Error while migrating activity logs. Error:" + e.getMessage());
         } finally {
             try {
                 if (csvFileReader != null) csvFileReader.close();
                 if (activityLogsWriter != null) {
                     activityLogsWriter.flush();
                     activityLogsWriter.close();
                 }
                 if (csvFileReader != null) {
                     opportunityActLogWriter.flush();
                     opportunityActLogWriter.close();
                 }
                 if (csvFileReader != null) {
                     organisationActivityLogWriter.flush();
                     organisationActivityLogWriter.close();
                 }
                 if (csvFileReader != null) {
                     volunteerActivityLogWriter.flush();
                     volunteerActivityLogWriter.close();
                 }
 
             } catch (IOException e) {
                 System.out.println("Error closing activity logs streams. Error:" + e.getMessage());
             }
 
         }
     }
 }
