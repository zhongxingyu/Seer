 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.emr.page.controller;
 
 import org.openmrs.Patient;
import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.adt.AdtService;
 import org.openmrs.module.emr.patient.PatientDomainWrapper;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.InjectBeans;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class MergePatientsPageController {
 
     public String get(@RequestParam(required = false, value = "patient1") Patient patient1,
                     @RequestParam(required = false, value = "patient2") Patient patient2,
                     @RequestParam(value = "confirmed", defaultValue = "false") Boolean confirmed,
                     @RequestParam(value = "unknown-patient", defaultValue = "false") boolean isUnknownPatient,
                     @InjectBeans PatientDomainWrapper wrapper1,
                     @InjectBeans PatientDomainWrapper wrapper2,
                     HttpServletRequest request,
                     PageModel pageModel) {
 
         pageModel.addAttribute("patient1", null);
         pageModel.addAttribute("patient2", null);
         pageModel.addAttribute("confirmed", confirmed);
         pageModel.addAttribute("isUnknownPatient", isUnknownPatient);
 
        if (patient1!= null && patient2==null && isUnknownPatient){
             wrapper1.setPatient(patient1);
             pageModel.addAttribute("patient1", wrapper1);
             return "mergePatients-chooseRecords";
         }
 
         if (patient1 == null && patient2 == null) {
             return "mergePatients-chooseRecords";
         }
 
         if (patient1.equals(patient2)) {
             request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "emr.mergePatients.error.samePatient");
             return "mergePatients-chooseRecords";
         }
 
         wrapper1.setPatient(patient1);
         wrapper2.setPatient(patient2);
         pageModel.addAttribute("patient1", wrapper1);
         pageModel.addAttribute("patient2", wrapper2);
 
         if (!confirmed) {
             return "mergePatients-confirmSamePerson";
         }
         else {
             // do extra checks
             pageModel.addAttribute("overlappingVisits", wrapper1.hasOverlappingVisitsWith(patient2));
             return "mergePatients-chooseMergeOptions";
         }
     }
 
     public String post(UiUtils ui,
                        HttpServletRequest request,
                        @RequestParam("patient1") Patient patient1,
                        @RequestParam("patient2") Patient patient2,
                        @RequestParam("preferred") Patient preferred,
                        @SpringBean("adtService") AdtService adtService) {
         Patient notPreferred = patient1.equals(preferred) ? patient2 : patient1;
         adtService.mergePatients(preferred, notPreferred);
 
         request.getSession().setAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, "emr.mergePatients.success");
         return "redirect:" + ui.pageLink("emr", "patient", SimpleObject.create("patientId", preferred.getId()));
     }
 
 }
