 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.technicaldebt.it;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Measure;
 import org.sonar.wsclient.services.ResourceQuery;
 
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.CoreMatchers.anyOf;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class CommonsLangModeLightIT {
 
   private static Sonar sonar;
   private static final String PROJECT_STRUTS = "commons-lang:commons-lang";
   private static final String FILE_ACTION = "commons-lang:commons-lang:org.apache.commons.lang.CharRange";
   private static final String PACKAGE_ACTION = "commons-lang:commons-lang:org.apache.commons.lang";
 
   @BeforeClass
   public static void buildServer() {
     sonar = Sonar.create("http://localhost:9000");
   }
 
   @Test
   public void strutsIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getName(), is("Commons Lang"));
     assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getVersion(), is("2.5"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE_ACTION)).getName(), is("org.apache.commons.lang"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("technical_debt").getValue(), is(40493.8));
    assertThat(getProjectMeasure("technical_debt_ratio").getValue(), is(10));
    assertThat(getProjectMeasure("technical_debt_days").getValue(), is(81));
     assertThat(getProjectMeasure("technical_debt_repart").getData(), 
         is("Complexity=52.55;Design=6.17;Duplication=14.5;Violations=26.76"));
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("technical_debt").getValue(), is(11856.3));
     assertThat(getPackageMeasure("technical_debt_ratio").getValue(), is(8.0));
     assertThat(getPackageMeasure("technical_debt_days").getValue(), is(23.7));
     assertThat(getPackageMeasure("technical_debt_repart").getData(),
         is("Complexity=68.26;Duplication=9.48;Violations=22.24"));
   }
 
   @Test
   public void filesMetrics() {
     assertThat(getFileMeasure("technical_debt").getValue(), is(81.3));
     assertThat(getFileMeasure("technical_debt_ratio").getValue(), is(2.8));
     assertNull(getFileMeasure("technical_debt_days"));
     assertThat(getFileMeasure("technical_debt_repart").getData(), is("Complexity=38.46;Violations=61.53"));
   }
 
   private Measure getFileMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(FILE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getPackageMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PACKAGE_ACTION, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_STRUTS, metricKey)).getMeasure(metricKey);
   }
 }
