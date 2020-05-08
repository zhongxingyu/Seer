 package com.solidstategroup.radar.service.impl;
 
 import com.solidstategroup.radar.model.Consultant;
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.DocumentData;
 import com.solidstategroup.radar.model.enums.ExportType;
 import com.solidstategroup.radar.model.user.PatientUser;
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.service.DocumentDataBuilder;
 import com.solidstategroup.radar.service.ExportManager;
 import com.solidstategroup.radar.service.UserManager;
 import com.solidstategroup.radar.service.UtilityManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ExportManagerImpl implements ExportManager {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ExportManagerImpl.class);
     public static final String DATE_PATTERN = "dd-MMM-yyyy";
 
     private UserManager userManager;
     private UtilityManager utilityManager;
     private DemographicsManager demographicsManager;
     private DiagnosisManager diagnosisManager;
 
     private DocumentDataBuilder csvDocumentDataBuilder;
     private DocumentDataBuilder excelDocumentDataBuilder;
     private DocumentDataBuilder pdfDocumentDataBuilder;
 
     private Map<ExportType, DocumentDataBuilder> exportTypeToDocumentDataBuilderMap;
 
     public byte[] getProfessionalUsersExportData(ExportType exportType) {
         List<ProfessionalUser> professionalUsers = userManager.getProfessionalUsers();
         DocumentData documentData = new DocumentData();
         // add the headers/columns
         documentData.setHeaders(Arrays.asList("Surname", "Forename", "Title", "Role", "Email", "Centre", "Date Reg",
                 "GMC"));
 
         // add the row data
         for (ProfessionalUser user : professionalUsers) {
             String username = "";
//            documentData.addRow(Arrays.asList(user.getSurname(), user.getForename(), user.getTitle(), user.getRole(),
//                    user.getEmail(), user.getCentre() != null ? user.getCentre().getName() :
//                    "", new SimpleDateFormat(DATE_PATTERN).format(user.getDateRegistered()), user.getGmc()));
         }
         return getExportData(exportType, documentData);
     }
 
     public byte[] getConsultantsExportData(ExportType exportType) {
         List<Consultant> consultants = utilityManager.getConsultants();
         DocumentData documentData = new DocumentData();
         documentData.setHeaders(Arrays.asList("Surname", "First Name", "Centre"));
 
         for (Consultant consultant : consultants) {
             documentData.addRow(Arrays.asList(consultant.getSurname(), consultant.getForename(),
                     consultant.getCentre() != null ? consultant.getCentre().getName() : ""));
         }
 
         return getExportData(exportType, documentData);
     }
 
     public byte[] getDemographicsExportData(ExportType exportType) {
         List<Demographics> demographicsList = demographicsManager.getDemographics();
         DocumentData documentData = new DocumentData();
         documentData.setHeaders(Arrays.asList("RADAR No", "Date Reg", "First Name", "Surname", "Address", "Diagnosis",
                 "Consultant", "", "Centre"));
 
         for (Demographics demographics : demographicsList) {
             String diagnosisCodeAbbr = diagnosisManager.getDiagnosisName(demographics);
 
             String dateRegistered = "";
             dateRegistered = demographics.getDateRegistered() != null ? new SimpleDateFormat(DATE_PATTERN).
                     format(demographics.getDateRegistered()) : "";
 
             documentData.addRow(Arrays.asList(demographics.getId().toString(), dateRegistered,
                     demographics.getForename(), demographics.getSurname(),
                     demographics.getAddress1() + ", " + demographics.getAddress2() + ", " +
                             demographics.getAddress3() + ". " + demographics.getAddress4(), diagnosisCodeAbbr,
                     demographics.getConsultant() != null ? demographics.getConsultant().getForename() : "",
                     demographics.getConsultant() != null ? demographics.getConsultant().getSurname() : "",
                     demographics.getRenalUnit() != null ? demographics.getRenalUnit().getAbbreviation() : ""));
         }
 
         return getExportData(exportType, documentData);
     }
 
     public byte[] getPatientsExportData(ExportType exportType) {
         List<PatientUser> patients = userManager.getPatientUsers();
         DocumentData documentData = new DocumentData();
         documentData.setHeaders(Arrays.asList("RadarNO", "User Name", "DOB", "Date Reg"));
 
         for (PatientUser user : patients) {
             String dob = "";
             dob = user.getDateOfBirth() != null ? new SimpleDateFormat(DATE_PATTERN).
                     format(user.getDateOfBirth()) : "";
 
             String dateRegistered = "";
//            dateRegistered = user.getDateRegistered() != null ? new SimpleDateFormat(DATE_PATTERN).
//                    format(user.getDateRegistered()) : "";
 
             documentData.addRow(Arrays.asList(user.getRadarNumber().toString(), user.getUsername(), dob,
                     dateRegistered));
         }
 
         return getExportData(exportType, documentData);
     }
 
     private byte[] getExportData(ExportType exportType, DocumentData documentData) {
         // build the map - maps export type to a document builder
         if (exportTypeToDocumentDataBuilderMap == null) {
             exportTypeToDocumentDataBuilderMap = new HashMap<ExportType,
                     DocumentDataBuilder>();
             exportTypeToDocumentDataBuilderMap.put(ExportType.CSV, csvDocumentDataBuilder);
             exportTypeToDocumentDataBuilderMap.put(ExportType.EXCEL, excelDocumentDataBuilder);
             exportTypeToDocumentDataBuilderMap.put(ExportType.PDF, pdfDocumentDataBuilder);
         }
 
         DocumentDataBuilder documentDataBuilder = exportTypeToDocumentDataBuilderMap.get(exportType);
         // build and return the data
         return documentDataBuilder.build(documentData);
     }
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     public void setUtilityManager(UtilityManager utilityManager) {
         this.utilityManager = utilityManager;
     }
 
     public void setDemographicsManager(DemographicsManager demographicsManager) {
         this.demographicsManager = demographicsManager;
     }
 
     public void setDiagnosisManager(DiagnosisManager diagnosisManager) {
         this.diagnosisManager = diagnosisManager;
     }
 
     public void setCsvDocumentDataBuilder(DocumentDataBuilder csvDocumentDataBuilder) {
         this.csvDocumentDataBuilder = csvDocumentDataBuilder;
     }
 
     public void setExcelDocumentDataBuilder(DocumentDataBuilder excelDocumentDataBuilder) {
         this.excelDocumentDataBuilder = excelDocumentDataBuilder;
     }
 
     public void setPdfDocumentDataBuilder(DocumentDataBuilder pdfDocumentDataBuilder) {
         this.pdfDocumentDataBuilder = pdfDocumentDataBuilder;
     }
 }
