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
 
 package org.sonar.plugins.sigmm.it;
 
 import org.junit.Test;
 import org.junit.BeforeClass;
 
 import static org.hamcrest.Matchers.anyOf;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.core.Is.is;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.ResourceQuery;
 import org.sonar.wsclient.services.Measure;
 import static junit.framework.Assert.assertNull;
 
 public class StrutsIT {
 
   private static Sonar sonar;
   private static final String PROJECT_STRUTS = "org.apache.struts:struts-parent";
   private static final String MODULE_CORE = "org.apache.struts:struts-core";
   private static final String FILE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action.Action";
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
     assertThat(sonar.find(new ResourceQuery(PACKAGE_ACTION)).getName(), is("org.apache.struts.action"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("sigmm-analysability").getIntValue(), is(-1));
     assertThat(getProjectMeasure("sigmm-changeability").getIntValue(), is(-2));
     assertThat(getProjectMeasure("sigmm-testability").getIntValue(), is(-2));
     assertThat(getProjectMeasure("sigmm-stability").getIntValue(), is(-2));
     assertThat(getProjectMeasure("sigmm-maintainability").getIntValue(), is(-2));
     assertThat(getProjectMeasure("sigmm-ncloc-by-cc").getData(), anyOf(is("0=28910;10=5842;20=7739;50=742"), is("0=28689;10=5794;20=7739;50=742"))); // < 2.6, >= 2.6
     assertThat(getProjectMeasure("sigmm-ncloc-by-ncloc").getData(), anyOf(is("0=15698;10=16352;50=4266;100=6917"), is("0=15477;10=16304;50=4266;100=6917"))); // < 2.6, >= 2.6
   }
 
   @Test
   public void modulesMetrics() {
     assertThat(getCoreModuleMeasure("sigmm-analysability").getIntValue(), is(1));
     assertThat(getCoreModuleMeasure("sigmm-changeability").getIntValue(), is(1));
     assertThat(getCoreModuleMeasure("sigmm-testability").getIntValue(), is(-1));
     assertThat(getCoreModuleMeasure("sigmm-stability").getIntValue(), is(-1));
     assertThat(getCoreModuleMeasure("sigmm-maintainability").getIntValue(), is(0));
     assertThat(getCoreModuleMeasure("sigmm-ncloc-by-cc").getData(), anyOf(is("0=9341;10=1733;20=552;50=526"), is("0=9120;10=1733;20=552;50=526"))); // < 2.6, >= 2.6
     assertThat(getCoreModuleMeasure("sigmm-ncloc-by-ncloc").getData(), anyOf(is("0=4140;10=6234;50=1422;100=356"), is("0=3919;10=6234;50=1422;100=356"))); // < 2.6, >= 2.6
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("sigmm-analysability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-changeability").getIntValue(), is(1));
     assertThat(getPackageMeasure("sigmm-testability").getIntValue(), is(-1));
     assertThat(getPackageMeasure("sigmm-stability").getIntValue(), is(-1));
     assertThat(getPackageMeasure("sigmm-maintainability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-ncloc-by-cc").getData(), anyOf(is("0=1908;10=378;20=159;50=0"), is("0=1904;10=378;20=159;50=0"))); // < 2.6, >= 2.6
     assertThat(getPackageMeasure("sigmm-ncloc-by-ncloc").getData(), anyOf(is("0=578;10=1643;50=224;100=0"), is("0=574;10=1643;50=224;100=0"))); // < 2.6, >= 2.6
   }
 
   @Test
   public void filesMetrics() {
     assertNull(getFileMeasure("sigmm-analysability"));
     assertNull(getFileMeasure("sigmm-changeability"));
     assertNull(getFileMeasure("sigmm-testability"));
     assertNull(getFileMeasure("sigmm-stability"));
     assertNull(getFileMeasure("sigmm-maintainability"));
     assertNull(getFileMeasure("sigmm-ncloc-by-cc"));
     assertNull(getFileMeasure("sigmm-ncloc-by-ncloc"));
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
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_STRUTS, metricKey)).getMeasure(metricKey);
   }
 }
