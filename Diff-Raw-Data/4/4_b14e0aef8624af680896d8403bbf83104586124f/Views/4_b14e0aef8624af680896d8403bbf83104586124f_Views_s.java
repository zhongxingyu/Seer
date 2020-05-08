 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.web.configuration.container;
 
 import static edu.dfci.cccb.mev.api.client.support.view.ViewBuilders.freemarker;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
 
 /**
  * @author levk
  * 
  */
 public class Views {
 
   @Bean
   public FreeMarkerView home () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/home.ftl").build ();
   }
 
   @Bean
   public FreeMarkerView api () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/api.ftl").build ();
   }
 
   // Elements
 
   @Bean (name = "elements/view1")
   public FreeMarkerView elementView1 () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/view1.ftl").build ();
   }
   
   @Bean (name = "elements/menubar")
   public FreeMarkerView elementMenubar () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/menubar.ftl").build ();
   }
   
   @Bean (name = "elements/expressionPanel")
   public FreeMarkerView elementExpressionPanel () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/expressionPanel.ftl").build ();
   }
 
   @Bean (name = "elements/analysisPanel")
   public FreeMarkerView elementanalysisPanel () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/analysisPanel.ftl").build ();
   }
   
   @Bean (name = "elements/hierarchicalbody")
   public FreeMarkerView elementHierarchicalbody () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/hierarchicalbody.ftl").build ();
   }
   
   @Bean (name = "elements/kMeansBody")
   public FreeMarkerView elementKMeansBody () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/kMeansBody.ftl").build ();
   }
   
   @Bean (name = "elements/limmaBody")
   public FreeMarkerView elementLimmaBody () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/limmaBody.ftl").build ();
   }
   
   @Bean (name = "elements/modal")
   public FreeMarkerView elementModal () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/modal.ftl").build ();
   }
   
   @Bean (name = "elements/prevlimmashell")
   public FreeMarkerView elementPrevlimmashell () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/prevlimmashell.ftl").build ();
   }
   
   @Bean (name = "elements/table")
   public FreeMarkerView elementTable () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/table.ftl").build ();
   }
   
   @Bean (name = "elements/mainNavigation")
   public FreeMarkerView elementMainNavigation () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/mainNavigation.ftl").build ();
   }
   
   @Bean (name = "elements/heatmapPanels")
   public FreeMarkerView elementHeatmapPanels () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/heatmapPanels.ftl").build ();
   }
   
   @Bean (name = "elements/visHeatmap")
   public FreeMarkerView elementVisHeatmap () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/visHeatmap.ftl").build ();
   }
   
   @Bean (name = "elements/uploadDragAndDrop")
   public FreeMarkerView elementUploadDragAndDrop () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/uploadDragAndDrop.ftl").build ();
   }
   
   @Bean (name = "elements/datasetSummary")
   public FreeMarkerView elementDatasetSummary () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/elements/datasetSummary.ftl").build ();
   }
   
 
   // Partials
 
   @Bean (name = "partials/partial1")
   public FreeMarkerView partial1 () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/partials/partial1.ftl").build ();
   }
   
   @Bean (name = "partials/heatmap")
   public FreeMarkerView heatmap () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/partials/heatmap.ftl").build ();
   }
   
   @Bean (name = "partials/importItems")
   public FreeMarkerView importitems () {
     return freemarker ().url ("/edu/dfci/cccb/mev/web/views/partials/importItems.ftl").build ();
   }
 }
