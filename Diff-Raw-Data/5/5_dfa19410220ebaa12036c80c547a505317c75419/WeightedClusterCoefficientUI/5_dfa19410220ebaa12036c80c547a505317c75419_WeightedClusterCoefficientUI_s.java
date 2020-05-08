 /*
  Authors : Umberto Griffo <umberto.griffo@gmail.com>
  Linkedin : it.linkedin.com/pub/umberto-griffo/31/768/99
  Twitter : @UmbertoGriffo
  
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
  The contents of this file are subject to the terms of either the GNU
  General Public License Version 3 only ("GPL") or the Common
  Development and Distribution License("CDDL") (collectively, the
  "License"). You may not use this file except in compliance with the
  License. 
  You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.txt.
 
  */
 package umberto.WeightedClusterCoefficient;
 
 import java.text.DecimalFormat;
 import javax.swing.JPanel;
 import org.gephi.statistics.spi.Statistics;
 import org.gephi.statistics.spi.StatisticsUI;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * User interface for the {@link WeightedClusterCoefficient} statistic. <p> It's
  * responsible for retrieving the settings from the panel and set it to the
  * statistics instance. <p>
  * <code>StatisticsUI</code> implementations are singleton (as they have a
  * <code>@ServiceProvider</code> annotation) so the panel and statistic are
  * unset after
  * <code>unsetup()</code> is called so they can be GCed.
  *
  * @author Umberto Griffo.
  */
 @ServiceProvider(service = StatisticsUI.class)
 public class WeightedClusterCoefficientUI implements StatisticsUI {
 
     private weightedClusterCoefficientPanel panel;
     private WeightedClusterCoefficient myMetric;
 
     @Override
     //Returns a settings panel instance.
     public JPanel getSettingsPanel() {
         panel = new weightedClusterCoefficientPanel();
         return panel;
     }
 
     @Override
     //Push a statistics instance to the UI to load its settings.
     //Note that this method is always called after getSettingsPanel 
     //and before the panel is displayed.
     public void setup(Statistics stat) {
         this.myMetric = (WeightedClusterCoefficient) stat;
         if (panel != null) {
             panel.setDirected(myMetric.isDirected());
         }
     }
 
     @Override
     //Notify the settings panel has been closed and that the settings values 
     //can be saved to the statistics instance.
     public void unsetup() {
         if (panel != null) {
             myMetric.setDirected(panel.isDirected());
         }
         this.panel = null;
         this.myMetric = null;
     }
 
     @Override
     //Returns the statistics' class this UI belongs to.
     public Class<? extends Statistics> getStatisticsClass() {
         return WeightedClusterCoefficient.class;
     }
 
     @Override
     //Returns this statistics result as a String, if exists
     public String getValue() {
         DecimalFormat df = new DecimalFormat("###.###");
         return "" + df.format(myMetric.getAverageWeightedClusteringCoefficient());
     }
 
     @Override
     //Returns this statistics display name
     public String getDisplayName() {
         return "Avg. Weighted Cluster Coefficient";
     }
 
     @Override
     //Returns the category of this metric.
     public String getCategory() {
         return StatisticsUI.CATEGORY_NODE_OVERVIEW;
     }
 
     @Override
     // Returns a position value, around 1 and 1000, that indicates the position of the Statistics in the UI.
     public int getPosition() {
         return 303;
     }
 }
