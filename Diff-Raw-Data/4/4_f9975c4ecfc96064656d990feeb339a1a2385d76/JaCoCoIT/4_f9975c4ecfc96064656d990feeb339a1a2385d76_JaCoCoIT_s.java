 /*
  * Sonar JaCoCo Plugin
  * Copyright (C) 2010 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.jacoco;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.utils.KeyValueFormat;
 import org.sonar.plugins.jacoco.itcoverage.JaCoCoItMetrics;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Measure;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import java.util.Map;
 
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.CoreMatchers.anyOf;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 public class JaCoCoIT {
 
   private static Sonar sonar;
   private static final String PROJECT_STRUTS = "org.apache.struts:struts-parent";
   private static final String MODULE_CORE = "org.apache.struts:struts-core";
   private static final String MODULE_EL = "org.apache.struts:struts-el";
   private static final String FILE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action.DynaActionFormClass";
   private static final String PACKAGE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action";
 
   @BeforeClass
   public static void buildServer() {
     sonar = Sonar.create("http://localhost:9000");
   }
 
   @Test
   public void strutsIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getName(), is("Struts"));
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getVersion(), is("1.3.9"));
     assertThat(sonar.find(new ResourceQuery(MODULE_CORE)).getName(), is("Struts Core"));
     assertThat(sonar.find(new ResourceQuery(MODULE_EL)).getName(), is("Struts EL"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE_ACTION)).getName(), is("org.apache.struts.action"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("coverage").getValue(), is(14.7));
 
     assertThat(getProjectMeasure("line_coverage").getValue(), is(15.4));
     assertThat(getProjectMeasure("lines_to_cover").getValue(), anyOf(
         is(26126.0), // java 1.5.0_22
         is(26124.0), // java 1.6.0_20 and 1.6.0_22
         is(27124.0) // unknown
         ));
     assertThat(getProjectMeasure("uncovered_lines").getValue(), anyOf(
         is(22110.0), // java 1.5.0_22
         is(22108.0) // java 1.6.0_20 and 1.6.0_22
         ));
 
     assertThat(getProjectMeasure("branch_coverage").getValue(), is(13.2));
     assertThat(getProjectMeasure("conditions_to_cover").getValue(), is(10377.0));
     assertThat(getProjectMeasure("uncovered_conditions").getValue(), is(9010.0));
 
     assertThat(getProjectMeasure("tests").getValue(), is(323.0));
     assertThat(getProjectMeasure("test_success_density").getValue(), is(100.0));
   }
 
   @Test
   public void CoremoduleMetrics() {
     assertThat(getCoreModuleMeasure("coverage").getValue(), is(37.1));
 
     assertThat(getCoreModuleMeasure("line_coverage").getValue(), is(39.7));
     assertThat(getCoreModuleMeasure("lines_to_cover").getValue(), anyOf(
         is(7448.0), // java 1.5.0_22
         is(7447.0)));
     assertThat(getCoreModuleMeasure("uncovered_lines").getValue(), anyOf(
         is(4491.0), // java 1.5.0_22
         is(4490.0)));
 
     assertThat(getCoreModuleMeasure("branch_coverage").getValue(), is(31.4));
     assertThat(getCoreModuleMeasure("conditions_to_cover").getValue(), is(3355.0));
     assertThat(getCoreModuleMeasure("uncovered_conditions").getValue(), is(2303.0));
 
     assertThat(getCoreModuleMeasure("tests").getValue(), is(195.0));
     assertThat(getCoreModuleMeasure("test_success_density").getValue(), is(100.0));
   }
 
   @Test
   public void ElModuleMetrics() {
     assertThat(getElModuleMeasure("coverage").getValue(), is(0.0));
     assertThat(getElModuleMeasure("line_coverage").getValue(), is(0.0));
     assertThat(getElModuleMeasure("lines_to_cover").getValue(), is(8064.0));
     assertThat(getElModuleMeasure("uncovered_lines").getValue(), is(8064.0));
 
     assertThat(getElModuleMeasure("branch_coverage").getValue(), is(0.0));
     assertThat(getElModuleMeasure("conditions_to_cover").getValue(), is(3320.0));
     assertThat(getElModuleMeasure("uncovered_conditions").getValue(), is(3320.0));
 
     assertThat(getElModuleMeasure("tests").getValue(), is(0.0));
    assertNull(getElModuleMeasure("test_success_density"));
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("coverage").getValue(), is(32.4));
 
     assertThat(getPackageMeasure("line_coverage").getValue(), is(33.1));
     assertThat(getPackageMeasure("lines_to_cover").getValue(), is(1569.0));
     assertThat(getPackageMeasure("uncovered_lines").getValue(), is(1050.0));
 
     assertThat(getPackageMeasure("branch_coverage").getValue(), is(31.0));
     assertThat(getPackageMeasure("conditions_to_cover").getValue(), is(762.0));
     assertThat(getPackageMeasure("uncovered_conditions").getValue(), is(526.0));
 
     assertThat(getPackageMeasure("tests").getValue(), is(105.0));
     assertThat(getPackageMeasure("test_success_density").getValue(), is(100.0));
   }
 
   @Test
   public void filesMetrics() {
     assertThat(getFileMeasure("coverage").getValue(), is(63.8));
 
     assertThat(getFileMeasure("line_coverage").getValue(), is(64.7));
     assertThat(getFileMeasure("lines_to_cover").getValue(), is(51.0));
     assertThat(getFileMeasure("uncovered_lines").getValue(), is(18.0));
 
     assertThat(getFileMeasure("branch_coverage").getValue(), is(61.1));
     assertThat(getFileMeasure("conditions_to_cover").getValue(), is(18.0));
     assertThat(getFileMeasure("uncovered_conditions").getValue(), is(7.0));
 
     Map<String, String> lineHits = KeyValueFormat.parse(getFileMeasure(CoreMetrics.COVERAGE_LINE_HITS_DATA_KEY).getData());
     assertThat(lineHits.get("121"), is("1")); // fully covered
     assertThat(lineHits.get("191"), is("0")); // not covered
     assertThat(lineHits.get("228"), is("1")); // partly covered
     assertThat(lineHits.get("258"), is("1")); // partly covered
 
     Map<String, String> lineConditions = KeyValueFormat.parse(getFileMeasure(CoreMetrics.CONDITIONS_BY_LINE_KEY).getData());
     assertThat(lineConditions.get("121"), is("2"));
     assertThat(lineConditions.get("191"), is("2"));
     assertThat(lineConditions.get("228"), is("2"));
     assertThat(lineConditions.get("258"), is("4"));
 
     Map<String, String> coveredConditions = KeyValueFormat.parse(getFileMeasure(CoreMetrics.COVERED_CONDITIONS_BY_LINE_KEY).getData());
     assertThat(coveredConditions.get("121"), is("2"));
     assertThat(coveredConditions.get("191"), is("0"));
     assertThat(coveredConditions.get("228"), is("1"));
     assertThat(coveredConditions.get("258"), is("3"));
 
    assertNull(getFileMeasure("tests"));
    assertNull(getFileMeasure("test_success_density"));
   }
 
   private Measure getFileMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(FILE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getPackageMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PACKAGE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getCoreModuleMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(MODULE_CORE, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getElModuleMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(MODULE_EL, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_STRUTS, metricKey)).getMeasure(metricKey);
   }
 }
