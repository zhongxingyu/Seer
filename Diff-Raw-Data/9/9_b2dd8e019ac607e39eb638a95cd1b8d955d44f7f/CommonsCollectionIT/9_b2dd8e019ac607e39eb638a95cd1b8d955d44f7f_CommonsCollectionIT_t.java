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
import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.ResourceQuery;
 import org.sonar.wsclient.services.Measure;
 import static junit.framework.Assert.assertNull;
 
 public class CommonsCollectionIT {
 
   private static Sonar sonar;
   private static final String PROJECT_COMMONS_COLLECTIONS = "commons-collections:commons-collections";
   private static final String FILE_BAG_UTILS = "commons-collections:commons-collections:org.apache.commons.collections.BagUtils";
   private static final String PACKAGE_COLLECTIONS = "commons-collections:commons-collections:org.apache.commons.collections";
 
   @BeforeClass
   public static void buildServer() {
     sonar = Sonar.create("http://localhost:9000");
   }
 
   @Test
   public void commonsCollectionsIsAnalyzed() {
     assertThat(sonar.find(new ResourceQuery(PROJECT_COMMONS_COLLECTIONS)).getName(), is("Commons Collections"));
     assertThat(sonar.find(new ResourceQuery(PROJECT_COMMONS_COLLECTIONS)).getVersion(), is("3.3"));
     assertThat(sonar.find(new ResourceQuery(PACKAGE_COLLECTIONS)).getName(), is("org.apache.commons.collections"));
   }
 
   @Test
   public void projectsMetrics() {
     assertThat(getProjectMeasure("sigmm-analysability").getIntValue(), is(1));
     assertThat(getProjectMeasure("sigmm-changeability").getIntValue(), is(1));
     assertThat(getProjectMeasure("sigmm-testability").getIntValue(), is(0));
     assertThat(getProjectMeasure("sigmm-stability").getIntValue(), is(1));
     assertThat(getProjectMeasure("sigmm-maintainability").getIntValue(), is(1));
     assertThat(getProjectMeasure("sigmm-ncloc-by-cc").getData(), anyOf(is("0=20696;10=2367;20=598;50=0"), is("0=20123;10=2307;20=598;50=0"))); // < 2.6, >= 2.6
     assertThat(getProjectMeasure("sigmm-ncloc-by-ncloc").getData(), anyOf(is("0=13755;10=9261;50=502;100=143"), is("0=13371;10=9012;50=502;100=143"))); // < 2.6, >= 2.6
   }
 
   @Test
   public void packagesMetrics() {
     assertThat(getPackageMeasure("sigmm-analysability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-changeability").getIntValue(), is(1));
     assertThat(getPackageMeasure("sigmm-testability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-stability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-maintainability").getIntValue(), is(0));
     assertThat(getPackageMeasure("sigmm-ncloc-by-cc").getData(), anyOf(is("0=7393;10=1424;20=255;50=0"), is("0=6909;10=1394;20=255;50=0"))); // < 2.6, >= 2.6
     assertThat(getPackageMeasure("sigmm-ncloc-by-ncloc").getData(), anyOf(is("0=4239;10=4532;50=301;100=0"), is("0=3895;10=4362;50=301;100=0"))); // < 2.6, >= 2.6
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
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_BAG_UTILS, metricKey));
    Measure measure = resource!=null ? resource.getMeasure(metricKey) : null;

    return measure;
   }
 
   private Measure getPackageMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PACKAGE_COLLECTIONS, metricKey)).getMeasure(metricKey);
   }
 
   private Measure getProjectMeasure(String metricKey) {
     return sonar.find(ResourceQuery.createForMetrics(PROJECT_COMMONS_COLLECTIONS, metricKey)).getMeasure(metricKey);
   }
 }
