 package org.openmrs.module.mirebalaisreports;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.db.SerializedObjectDAO;
 import org.openmrs.module.mirebalaisreports.definitions.BaseMirebalaisReportTest;
 import org.openmrs.module.mirebalaisreports.definitions.DailyRegistrationsReportManager;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.service.ReportService;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 
 import static junit.framework.Assert.assertNotNull;
 
 /**
  *
  */
 public class MirebalaisHospitalReportingModuleActivatorTest extends BaseMirebalaisReportTest {
 
     MirebalaisHospitalReportingModuleActivator activator;
 
     @Autowired
     DailyRegistrationsReportManager dailyRegistrationsReportManager;
 
     @Autowired
     SerializedObjectDAO serializedObjectDAO;
 
     @Autowired
     ReportDefinitionService reportDefinitionService;
 
     @Autowired
     ReportService reportService;
 
    @Autowired @Qualifier("adminService")
     AdministrationService administrationService;
 
     @Before
     public void setUp() throws Exception {
         activator = new MirebalaisHospitalReportingModuleActivator();
         activator.setReportDefinitionService(reportDefinitionService);
         activator.setReportService(reportService);
         activator.setSerializedObjectDAO(serializedObjectDAO);
         activator.setAdministrationService(administrationService);
     }
 
     /**
      * Tests the case where a persisted ReportDefinition is invalid (typically because of an incompatible change to a
      * definition class, while it is being developed)
      * @throws Exception
      */
     @Test
     public void testOverwritingInvalidSerializedReport() throws Exception {
         executeDataSet("org/openmrs/module/mirebalaisreports/badReportDefinition.xml");
 
         activator.setupReport(dailyRegistrationsReportManager);
 
         ReportDefinition reportDefinition = reportDefinitionService.getDefinitionByUuid(dailyRegistrationsReportManager.getUuid());
         assertNotNull(reportDefinition);
     }
 
 }
