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
 
 package org.openmrs.module.mirebalaisreports.page.controller;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.openmrs.module.mirebalaisreports.definitions.NonCodedDiagnosesReportManager;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.EvaluationException;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  */
 public class NonCodedDiagnosesPageController {
 
     public void get(@SpringBean NonCodedDiagnosesReportManager reportManager,
 					@SpringBean ReportDefinitionService reportDefinitionService,
                     @RequestParam(required = false, value = "fromDate") Date fromDate,
                     @RequestParam(required = false, value = "toDate") Date toDate,
                     PageModel model) throws EvaluationException, IOException {
 
         if (fromDate == null) {
             fromDate = DateUtils.addDays(new Date(), -21);
         }
         if (toDate == null) {
             toDate = new Date();
         }
         fromDate = DateUtil.getStartOfDay(fromDate);
         toDate = DateUtil.getEndOfDay(toDate);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("fromDate", fromDate);
 		params.put("toDate", toDate);
         params.put("nonCoded", "");
 
 		EvaluationContext context = reportManager.initializeContext(params);
		ReportDefinition reportDefinition = reportManager.constructReportDefinition(context);
 		ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);
 
         model.addAttribute("reportManager", reportManager);
         model.addAttribute("data", reportData.getDataSets().get(NonCodedDiagnosesReportManager.DATA_SET_NAME));
         model.addAttribute("fromDate", fromDate);
         model.addAttribute("toDate", DateUtil.getStartOfDay(toDate));
     }
 
     public void post(@SpringBean NonCodedDiagnosesReportManager reportManager,
                        @SpringBean ReportDefinitionService reportDefinitionService,
                        @RequestParam(required = false, value = "fromDate") Date fromDate,
                        @RequestParam(required = false, value = "toDate") Date toDate,
                        @RequestParam(required = false, value = "nonCoded") String nonCoded,
                        PageModel model) throws EvaluationException, IOException {
 
         if (fromDate == null) {
             fromDate = DateUtils.addDays(new Date(), -21);
         }
         if (toDate == null) {
             toDate = new Date();
         }
         fromDate = DateUtil.getStartOfDay(fromDate);
         toDate = DateUtil.getEndOfDay(toDate);
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("fromDate", fromDate);
         params.put("toDate", toDate);
         if(StringUtils.isNotBlank(nonCoded)){
             params.put("nonCoded", nonCoded);
         } else {
             params.put("nonCoded", "");
         }
         EvaluationContext context = reportManager.initializeContext(params);
        ReportDefinition reportDefinition = reportManager.constructReportDefinition(context);
         ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);
 
         model.addAttribute("reportManager", reportManager);
         model.addAttribute("data", reportData.getDataSets().get(NonCodedDiagnosesReportManager.DATA_SET_NAME));
         model.addAttribute("fromDate", fromDate);
         model.addAttribute("toDate", DateUtil.getStartOfDay(toDate));
     }
 
 }
