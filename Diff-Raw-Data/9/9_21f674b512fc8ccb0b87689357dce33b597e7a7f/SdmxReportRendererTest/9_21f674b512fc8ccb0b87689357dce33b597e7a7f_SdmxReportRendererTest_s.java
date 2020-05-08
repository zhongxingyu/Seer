 /**
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
 package org.openmrs.module.sdmxhdintegration.reporting.extension;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.ReportingConstants;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.dataset.definition.SimpleIndicatorDataSetDefinition;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.reporting.indicator.SqlIndicator;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.ReportDesignResource;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.util.ReportUtil;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.util.OpenmrsClassLoader;
 
 /**
  * Tests the SdmxReportRenderer
  */
 public class SdmxReportRendererTest extends BaseModuleContextSensitiveTest {
 	
 	@Test
 	public void shouldRenderToSdmx() throws Exception {
 		
 		ReportDefinition reportDefinition = new ReportDefinition();
 		reportDefinition.setName("Test Report");
 		reportDefinition.addParameter(ReportingConstants.START_DATE_PARAMETER);
 		reportDefinition.addParameter(ReportingConstants.END_DATE_PARAMETER);
 		
 		SimpleIndicatorDataSetDefinition dsd = new SimpleIndicatorDataSetDefinition();
 		reportDefinition.addDataSetDefinition(dsd, null);
 		
 		SqlIndicator allPatients = new SqlIndicator();
 		allPatients.setName("AllPatients");
 		allPatients.setSql("select count(*) from patient where voided = 0;");
 		dsd.addColumn("1", "All Patients", new Mapped<SqlIndicator>(allPatients, null));
 		
 		SqlIndicator males = new SqlIndicator();
 		males.setName("Males");
 		males.setSql("select count(*) from patient p, person n where p.patient_id = n.person_id and n.gender = 'M' and p.voided = 0;");
 		dsd.addColumn("1a", "Males", new Mapped<SqlIndicator>(males, null));
 
 		SqlIndicator females = new SqlIndicator();
 		females.setName("Females");
 		females.setSql("select count(*) from patient p, person n where p.patient_id = n.person_id and n.gender = 'F' and p.voided = 0;");
 		dsd.addColumn("1b", "Females", new Mapped<SqlIndicator>(females, null));
 		
 		EvaluationContext context = new EvaluationContext();
 		context.addParameterValue("startDate", DateUtil.getDateTime(2012, 7, 1));
 		context.addParameterValue("endDate", DateUtil.getDateTime(2012, 7, 31));
 		ReportDefinitionService rs = Context.getService(ReportDefinitionService.class);
 		ReportData reportData = rs.evaluate(reportDefinition, context);
 		
 		final ReportDesign reportDesign = new ReportDesign();
 		reportDesign.setName("TestDesign");
 		reportDesign.setReportDefinition(reportDefinition);
 		reportDesign.setRendererType(SdmxReportRenderer.class);
 
 		{
 			ReportDesignResource resource = new ReportDesignResource();
 			resource.setName("template");
 			resource.setExtension("zip");
 			resource.setReportDesign(reportDesign);
 			InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("TestSdmxMessage.zip");
 			resource.setContents(IOUtils.toByteArray(is));
 			IOUtils.closeQuietly(is);
 			reportDesign.addResource(resource);
 		}
 		
 		{
 			ReportDesignResource resource = new ReportDesignResource();
 			resource.setName("config");
 			resource.setExtension("xml");
 			resource.setReportDesign(reportDesign);
 			InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("TestSdmxConfig.xml");
 			resource.setContents(IOUtils.toByteArray(is));
 			IOUtils.closeQuietly(is);
 			reportDesign.addResource(resource);
 		}
 		
 		SdmxReportRenderer renderer = new SdmxReportRenderer() {
 			public ReportDesign getDesign(String argument) {
 				return reportDesign;
 			}
 		};
 		
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		renderer.render(reportData, "ReportData", baos);
 		baos.close();
		ReportUtil.writeByteArrayToFile(new File("/home/mseaton/Desktop/testSdmxOutput.xml"), baos.toByteArray());
 	}
 }
