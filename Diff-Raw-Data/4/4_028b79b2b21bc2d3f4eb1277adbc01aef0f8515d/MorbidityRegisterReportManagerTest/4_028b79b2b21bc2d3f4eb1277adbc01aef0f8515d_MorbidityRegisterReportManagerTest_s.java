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
 
 package org.openmrs.module.mirebalaisreports.definitions;
 
 import org.junit.Test;
 import org.openmrs.module.mirebalaisreports.visit.api.VisitDataService;
 import org.openmrs.module.mirebalaisreports.visit.definition.VisitStartDateDataDefinition;
 import org.openmrs.module.mirebalaisreports.visit.evaluator.EvaluatedVisitData;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.dataset.DataSet;
 import org.openmrs.module.reporting.dataset.DataSetRow;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.test.BaseContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Date;
 
 import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  */
public class MorbidityRegisterReportManagerTest extends BaseContextSensitiveTest {
 
     @Autowired
     VisitDataService visitDataService;
 
     @Test
     public void testquery() throws Exception {
         Date expectedDate = DateUtil.parseDate("2005-01-01", "yyyy-MM-dd");
 
         VisitStartDateDataDefinition def = new VisitStartDateDataDefinition();
         EvaluatedVisitData evaluated = visitDataService.evaluate(def, new EvaluationContext());
         assertThat(evaluated.getData().size(), is(6));
         assertThat((Date) evaluated.getData().get(1), is(expectedDate));
     }
 
     @Test
     public void testReport() throws Exception {
         Date start = DateUtil.parseDate("2013-01-01", "yyyy-MM-dd");
         Date end = DateUtil.parseDate("2013-01-31", "yyyy-MM-dd");
         MorbidityRegisterReportManager manager = new MorbidityRegisterReportManager();
         DataSet evaluated = manager.evaluate(start, end);
 
         int rows = 0;
         for (DataSetRow dataSetRow : evaluated) {
             ++rows;
         }
         assertThat(rows, is(3));
     }
 }
