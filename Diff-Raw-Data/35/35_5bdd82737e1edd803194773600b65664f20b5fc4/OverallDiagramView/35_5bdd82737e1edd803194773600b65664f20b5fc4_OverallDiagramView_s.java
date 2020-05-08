 /*
  * Copyright 2007-2009 Alexander Fabisch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ev.gui.views;
 
 import com.ev.logic.OverallConsumption;
 import org.jfree.chart.*;
 import org.jfree.chart.plot.PlotOrientation;
 import static com.ev.global.GlobalConfiguration.*;
 
 /**
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  * @since 0.8.2
  */
 public class OverallDiagramView extends AbstractDiagramView {
     private static final long serialVersionUID = 8257281350524801775L;
 
     public OverallDiagramView() {
         OverallConsumption oc = new OverallConsumption();
         chart = ChartFactory.createBarChart3D(getLang().getString("Overallview"),
                 getLang().getString("Year"), getLang().getString("Consumption"),
                oc.generateAllDataset(), PlotOrientation.VERTICAL, true, true, true);
         final ChartPanel cp = new ChartPanel(chart);
         add(cp);
     }
 
 }
