 /*
  * Copyright (C) 2010 Evgeny Mandrikov
  *
  * Sonar-IDE is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar-IDE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar-IDE; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.wsclient;
 
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.hasProperty;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.util.List;
 
 import org.hamcrest.Matchers;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.sonar.ide.api.IMeasure;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class MeasuresTest extends AbstractRemoteTestCase {
 
   @Test
   public void testGetMeasures() throws Exception {
     List<IMeasure> measures = getMeasures(getProject("measures"), "Measures");
 
     assertThat(measures.size(), greaterThan(0));
   }
 
   @Test
   public void notLoadDataMeasures() throws Exception {
     List<IMeasure>  measures = getMeasures(getProject("measures"), "Measures");
 
     for (IMeasure measure : measures) {
       assertThat(measure.getMetricDef().getHidden(), not(true));
      assertThat(measure.getMetricDef().getType(), not("DATA")); // "Coverage hits data" not hiddent, but with type DATA
     }
   }
 
   private List<IMeasure> getMeasures(File project, String className) throws Exception {
     return getRemoteSonar().search(getProjectKey(project) + ":[default]." + className).getMeasures();
   }
 
 }
