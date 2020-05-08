 package org.motechproject.commcare.provider.sync.web.controller;
 
 import org.motechproject.commcare.provider.sync.constants.EventConstants;
 import org.motechproject.commcare.provider.sync.diagnostics.DiagnosticsResult;
 import org.motechproject.commcare.provider.sync.diagnostics.DiagnosticsStatus;
 import org.motechproject.commcare.provider.sync.diagnostics.SchedulerDiagnosticService;
 import org.quartz.SchedulerException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "/web-api")
 public class SchedulerDiagnosticController {
 
     private SchedulerDiagnosticService schedulerDiagnosticService;
 
     @Autowired
     public SchedulerDiagnosticController(SchedulerDiagnosticService schedulerDiagnosticService) {
         this.schedulerDiagnosticService = schedulerDiagnosticService;
     }
 
    @RequestMapping(value = "/diagnostics/scheduler-commcare-provider-sync", method = RequestMethod.GET)
     @ResponseBody
     public String commcareSyncSchedulerStatus() throws SchedulerException {
         DiagnosticsResult diagnosticsResult = schedulerDiagnosticService.diagnoseSchedules(getSchedulesToDiagnose());
         return diagnosticsResult.getStatus().equals(DiagnosticsStatus.PASS)
                 ? "SUCCESS"
                 : String.format("FAILURE\n%s", diagnosticsResult.getMessage());
     }
 
     private List<String> getSchedulesToDiagnose() {
         List<String> schedules = new ArrayList<>();
         schedules.add(EventConstants.GROUP_SYNC_EVENT + "-" + EventConstants.GROUP_SYNC_JOB_ID_KEY);
         schedules.add(EventConstants.PROVIDER_SYNC_EVENT + "-" + EventConstants.PROVIDER_SYNC_JOB_ID_KEY);
         return schedules;
     }
 }
