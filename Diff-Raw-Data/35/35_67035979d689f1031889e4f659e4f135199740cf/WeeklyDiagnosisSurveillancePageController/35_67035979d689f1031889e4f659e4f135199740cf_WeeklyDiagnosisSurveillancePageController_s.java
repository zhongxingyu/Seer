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
 
 import org.openmrs.module.mirebalaisreports.definitions.WeeklyDiagnosisSurveillanceReportManager;
 import org.openmrs.module.reporting.common.ContentType;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.EvaluationException;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.ReportDesignResource;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.FileDownload;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  */
 public class WeeklyDiagnosisSurveillancePageController {
 
     public void get() {
         // GSP for this is static for now
     }
 
     public Object post(@RequestParam("format") String format,
                        @SpringBean WeeklyDiagnosisSurveillanceReportManager reportManager,
                        @SpringBean ReportDefinitionService reportDefinitionService,
                        @RequestParam("startOfWeek") Date startOfWeek ) throws EvaluationException, IOException {
 
         if ("excel".equals(format)) {
             Map<String, Object> params = new HashMap<String, Object>();
             params.put("startOfWeek", startOfWeek);
             return runReportAsExcel(reportDefinitionService, reportManager, params);
         } else {
             throw new IllegalArgumentException("Unrecognized format: " + format);
         }
     }
 
     private FileDownload runReportAsExcel(ReportDefinitionService reportDefinitionService, WeeklyDiagnosisSurveillanceReportManager reportManager, Map<String, Object> params) throws EvaluationException, IOException {
 
 		EvaluationContext context = reportManager.initializeContext(params);
        ReportDefinition reportDefinition = reportManager.constructReportDefinition(context);
         ReportData reportData = reportDefinitionService.evaluate(reportDefinition, context);
 
         // this is a hack, copied from ExcelRendererTest in the reporting module, to avoid needing to save the template
         // and report design in the database
 
         ReportDesignResource resource = new ReportDesignResource();
         resource.setName("template.xls");
         resource.setContents(reportManager.loadExcelTemplate());
 
         final ReportDesign design = new ReportDesign();
         design.setName("Excel report design (not persisted)");
         design.setReportDefinition(reportDefinition);
         design.setRendererType(ExcelTemplateRenderer.class);
         design.addResource(resource);
 
         ExcelTemplateRenderer renderer = new ExcelTemplateRenderer() {
             @Override
             public ReportDesign getDesign(String argument) {
                 return design;
             }
         };
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         renderer.render(reportData, "xxx:xls", out);
 
         return new FileDownload(reportManager.getExcelDownloadFilename(context), ContentType.EXCEL.getContentType(), out.toByteArray());
     }
 
 }
