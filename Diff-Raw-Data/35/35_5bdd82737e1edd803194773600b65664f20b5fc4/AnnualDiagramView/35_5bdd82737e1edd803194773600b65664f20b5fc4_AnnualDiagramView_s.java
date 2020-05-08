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
 
 import com.ev.datamodel.CounterOfAYear;
 import com.ev.logic.AnnualConsumption;
 import java.awt.Dimension;
 import java.util.*;
 import org.jfree.chart.*;
 import org.jfree.chart.plot.PlotOrientation;
 import static com.ev.global.GlobalConfiguration.*;
 
 /**
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  * @since 0.8.2
  */
 public class AnnualDiagramView extends AbstractDiagramView implements Observer {
 
     /**
      * Creates a new diagram for a specific year.
      * @param counter The data to by displayed. The argument has to be a subclass of Observable.
      * @throws ClassCastException Casting to Observable failed.
      */
     public AnnualDiagramView(CounterOfAYear counter) throws ClassCastException {
         ((Observable) counter).addObserver(this);
         init(counter);
     }
 
     /** @param energyData Counter values. */
     private void init(final CounterOfAYear counter) {
         final AnnualConsumption ac = new AnnualConsumption(counter);
         chart = ChartFactory.createBarChart3D(Integer.toString(counter.getYear()),
                 getLang().getString("Month"), getLang().getString("Consumption"),
                ac.generateYearDatasetForDiagram(), PlotOrientation.VERTICAL, true, true, true);
         ChartPanel cp = new ChartPanel(chart);
         cp.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
         add(cp);
     }
 
     @Override
     public final void update(Observable o, Object arg) {
         if (o instanceof CounterOfAYear) {
             removeAll();
             init((CounterOfAYear) o);
         }
     }
 }
