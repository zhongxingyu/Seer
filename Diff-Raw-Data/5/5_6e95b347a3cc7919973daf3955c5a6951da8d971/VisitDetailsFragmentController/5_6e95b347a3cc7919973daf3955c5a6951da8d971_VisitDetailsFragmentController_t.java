 package org.openmrs.module.emr.fragment.controller.visit;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.openmrs.*;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.EncounterService;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.consult.Diagnosis;
 import org.openmrs.module.emr.consult.DiagnosisMetadata;
 import org.openmrs.module.emr.visit.ParserEncounterIntoSimpleObjects;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiFrameworkConstants;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.fragment.action.FailureResult;
 import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
 import org.openmrs.ui.framework.fragment.action.SuccessResult;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.text.ParseException;
 import java.util.*;
 
 public class VisitDetailsFragmentController {
 
     public SimpleObject getVisitDetails(
             @SpringBean("adminService") AdministrationService administrationService,
             @RequestParam("visitId") Visit visit,
             UiUtils uiUtils,
             EmrContext emrContext) throws ParseException {
 
         SimpleObject simpleObject = SimpleObject.fromObject(visit, uiUtils, "id", "location");
         User authenticatedUser = emrContext.getUserContext().getAuthenticatedUser();
         boolean canDelete = authenticatedUser.hasPrivilege(EmrConstants.PRIVILEGE_DELETE_ENCOUNTER);
         Date startDatetime = visit.getStartDatetime();
         Date stopDatetime = visit.getStopDatetime();
 
         simpleObject.put("startDatetime", DateFormatUtils.format(startDatetime, "dd MMM yyyy hh:mm a", emrContext.getUserContext().getLocale()));
 
         if (stopDatetime!=null){
             simpleObject.put("stopDatetime", DateFormatUtils.format(stopDatetime, "dd MMM yyyy hh:mm a", emrContext.getUserContext().getLocale()));
         } else {
             simpleObject.put("stopDatetime", null);
         }
 
         List<SimpleObject> encounters = new ArrayList<SimpleObject>();
         simpleObject.put("encounters", encounters);
 
         String[] datePatterns = { administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATETIME_FORMAT) };
         for (Encounter e : visit.getEncounters()) {
             if (!e.getVoided()) {
                SimpleObject simpleEncounter = SimpleObject.fromObject(e, uiUtils,  "encounterId", "location", "encounterDatetime", "encounterProviders.provider", "voided", "form");
 
                // manually set the date and time components so we can control how we format them
                 simpleEncounter.put("encounterDate", DateFormatUtils.format(e.getEncounterDatetime(), "dd MMM yyyy", emrContext.getUserContext().getLocale()));
                 simpleEncounter.put("encounterTime", DateFormatUtils.format(e.getEncounterDatetime(), "hh:mm a", emrContext.getUserContext().getLocale()));
 
                 EncounterType encounterType = e.getEncounterType();
                 simpleEncounter.put("encounterType", SimpleObject.create("uuid", encounterType.getUuid(), "name", uiUtils.format(encounterType)));
                 if(canDelete){
                     simpleEncounter.put("canDelete", true);
                 }
                 encounters.add(simpleEncounter);
             }
         }
 
         return simpleObject;
     }
 
 
     public SimpleObject getEncounterDetails(@RequestParam("encounterId") Encounter encounter,
                                             @SpringBean("emrProperties") EmrProperties emrProperties,
                                             UiUtils uiUtils){
 
         ParserEncounterIntoSimpleObjects parserEncounter = new ParserEncounterIntoSimpleObjects(encounter, uiUtils, emrProperties);
 
         List<SimpleObject> observations = parserEncounter.parseObservations();
 
         List<SimpleObject> diagnoses = parserEncounter.parseDiagnoses();
 
         List<SimpleObject> orders = parserEncounter.parseOrders();
 
 
         return SimpleObject.create("observations", observations, "orders", orders, "diagnoses", diagnoses);
     }
 
 
 
     public FragmentActionResult deleteEncounter(UiUtils ui,
                                         @RequestParam("encounterId")Encounter encounter,
                                         @SpringBean("encounterService")EncounterService encounterService,
                                         EmrContext emrContext){
 
        if(encounter!=null){
            User authenticatedUser = emrContext.getUserContext().getAuthenticatedUser();
            boolean canDelete = authenticatedUser.hasPrivilege(EmrConstants.PRIVILEGE_DELETE_ENCOUNTER);
            if(canDelete){
                encounterService.voidEncounter(encounter, "delete encounter");
                encounterService.saveEncounter(encounter);
            }else{
                return new FailureResult(ui.message("emr.patientDashBoard.deleteEncounter.notAllowed"));
            }
        }
        return new SuccessResult(ui.message("emr.patientDashBoard.deleteEncounter.successMessage"));
     }
 }
 
